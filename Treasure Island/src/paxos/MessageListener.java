package paxos;

import comp512.gcl.GCL;
import comp512.gcl.GCMessage;
import comp512.utils.FailCheck;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


class MessageListener extends Thread{
    GCL gcl;
    volatile boolean live = true;
    private BallotID maxConfirmID = null;
    private final Paxos paxos;
    private boolean hasPromised = false;
    private boolean hasAccepted = false;
    private Propose curPropose = null;
    private AcceptRequest latestAcceptRequest = null;

    private final ConcurrentHashMap<String, ArrayList<ConfirmAck>> confirmAckBuffer = new ConcurrentHashMap<>();
    final ConcurrentLinkedQueue<Confirm> tempConfirmQueue = new ConcurrentLinkedQueue<>();
    MessageReceiverThread mrt;


    volatile ArrayList<String> buffer = new ArrayList<>();

    MessageListener(GCL gcl, Paxos paxos){
        this.gcl = gcl;
        this.paxos = paxos;
    }
    
    class MessageReceiverThread extends Thread{
        volatile boolean live = true;
        ConcurrentLinkedQueue<GCMessage> buffer = new ConcurrentLinkedQueue<>(); // promise collected for process p

        synchronized public GCMessage getNext(){
            if(this.buffer.isEmpty()) return null;
            return this.buffer.remove();
        }

        public void run(){
            while(live){
                try{
                    GCMessage msg = gcl.readGCMessage();
                    buffer.add(msg);
                }catch(IllegalStateException | InterruptedException e){
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        mrt = new MessageReceiverThread();
        mrt.start();
        while(live || this.paxos.hasUndeliveredMsg()|| this.paxos.shutdownAckCount.get()<this.paxos.numProcess.get()){
            this.processTempConfirm();
            GCMessage msg = mrt.getNext();
            if(msg==null) continue;
            Object val = msg.val;
            String sender = msg.senderProcess;
            if(val instanceof Propose){
                handlePropose((Propose) val, sender);
            }
            else if(val instanceof Promise){
                handlePromise((Promise) val, sender);
                if(this.paxos.debug) System.out.println(((Promise) val).getBallotID()+" handle propose finished");
            }
            else if(val instanceof AcceptRequest){
                handleAcceptRequest((AcceptRequest) val, sender);
            }
            else if(val instanceof AcceptAck){
                handleAcceptAck((AcceptAck) val, sender);
            }
            else if(val instanceof Confirm){
                handleConfirm((Confirm) val, sender);
            }
            else if(val instanceof ConfirmAck){
                handleConfirmAck((ConfirmAck) val, sender);
            }
            else if(val instanceof Message){
                if(this.paxos.debug) System.out.println("Game msg received");
                this.paxos.addToBuffer((Message) val);
            }
            else if(val instanceof Shutdown){
                if(this.paxos.myProcess.equals(sender)==false){
                    this.paxos.numProcess.decrementAndGet();
                }
                this.paxos.gcl.sendMsg(new ShutdownAck(),sender);
                System.out.println("New shutdown Msg received, ShutdownAck sent");
            }else if(val instanceof ShutdownAck){
                this.paxos.shutdownAckCount.incrementAndGet();
                System.out.println("New shutdownAck Msg received, current count: "+this.paxos.shutdownAckCount.get()+"/"+this.paxos.getNumProcess());
            }
        }
        this.processTempConfirm();
        System.out.println("MessageListener finished");
    }

    private boolean processTempConfirm(){
        // Find the smallest BallotID with all confirmAck = True
        // discard all previous confirms in the buffer before such confirm
        while(!tempConfirmQueue.isEmpty()) {
            Confirm c = tempConfirmQueue.peek();
            int trueCount = 0;
            if (confirmAckBuffer.containsKey(c.getBallotID().toString())) {
                if (confirmAckBuffer.get(c.getBallotID().toString()).size() == this.paxos.getNumProcess()) {
                    for (ConfirmAck a : confirmAckBuffer.get(c.getBallotID().toString())) {
                        if (a.isConfirm()) {
                            trueCount += 1;
                        }
                    }
                    // get all processes confirmAck
                    // boolean exit = false;
                    assert tempConfirmQueue.peek() != null;
                    Confirm tempConfirm = tempConfirmQueue.poll();
                    if (trueCount == this.paxos.getNumProcess()) {
                        assert tempConfirm != null;
                        this.paxos.determinedOrder.addAll(c.getOrder());
                        this.hasPromised = false;
                        this.hasAccepted = false;
                        ConfirmAck summary = new ConfirmAck(true,tempConfirm.getBallotID());
                        this.paxos.confirmSummaryQueue.add(summary);
                        if(this.paxos.debug) System.out.println(c.getBallotID()+" confirm successful by acceptor");
                    }else{
                        ConfirmAck summary = new ConfirmAck(false,tempConfirm.getBallotID());
                        this.paxos.confirmSummaryQueue.add(summary);
                        if(this.paxos.debug) System.out.println(c.getBallotID()+" confirm failed by acceptor");
                    }
                }else{
                    // totalCount != trueCount
                    if(c.receiveTime==0){
                        c.receiveTime = System.nanoTime();
                    }
                    long curTime = System.nanoTime();
                    long eightSecond=5000000000L;
                    if(curTime-c.receiveTime>=eightSecond){
                        this.paxos.numProcess.set(confirmAckBuffer.get(c.getBallotID().toString()).size());
                        System.out.println("timeout");
                    }
                    return false;
                }
            }else{
                return false;
            }
        }
        return true;
    }

    private void handlePropose(Propose propose, String sender) {
        this.paxos.failCheck.checkFailure(FailCheck.FailureType.RECEIVEPROPOSE);
        if(this.curPropose != null){
            if(this.curPropose.getBallotID().compareTo(propose.getBallotID()) > 0){
                // if ballotID of this propose < curPropose => refuse

                Promise p = new Promise(false, this.paxos.myProcess, propose.getBallotID(), this.curPropose.getBallotID());
                gcl.sendMsg(p, sender);
                this.paxos.failCheck.checkFailure(FailCheck.FailureType.AFTERSENDVOTE);
                if(this.paxos.debug) System.out.println(propose.getBallotID()+" propose refuse");
            }else{
                Promise p = new Promise(true, this.paxos.myProcess,propose.getBallotID());
                // modify current propose to this incoming propose
                gcl.sendMsg(p, sender);
                this.paxos.failCheck.checkFailure(FailCheck.FailureType.AFTERSENDVOTE);
                if(this.paxos.debug) System.out.println(propose.getBallotID()+" propose promised");
                this.curPropose = propose;
                this.hasPromised=true;
            }
        }else{
            // if there is no previous propose received
            Promise promise = new Promise(true, this.paxos.myProcess,propose.getBallotID());
            this.hasPromised = true;
            this.curPropose = propose;
            gcl.sendMsg(promise, sender);
            this.paxos.failCheck.checkFailure(FailCheck.FailureType.AFTERSENDVOTE);
            if(this.paxos.debug) System.out.println(propose.getBallotID()+" propose promised");
        }
    }

    private void handlePromise(Promise promise, String sender) {
        this.paxos.promiseQueue.add(promise);
        if(this.paxos.debug) System.out.println(promise.getBallotID()+" new promise received");
    }

    private void handleAcceptRequest(AcceptRequest acceptRequest, String sender) {

        if(this.curPropose != null){
            //
            if(this.curPropose.getBallotID().compareTo(acceptRequest.getBallotID()) > 0){
                // deny 
                AcceptAck a = new AcceptAck(false, acceptRequest.getBallotID());
                gcl.sendMsg(a, sender);
                if(this.paxos.debug) System.out.println(acceptRequest.getBallotID()+" acceptRequest refuse");

            }else{

                this.curPropose = new Propose(acceptRequest.getBallotID());
                this.hasPromised=true;
                this.latestAcceptRequest=acceptRequest;
                this.hasAccepted=true;
                AcceptAck a = new AcceptAck(true, acceptRequest.getBallotID());
                gcl.sendMsg(a, sender);
                if(this.paxos.debug) System.out.println(acceptRequest.getBallotID()+" acceptRequest accept");
            }
        }else{
            // case when the current process received a accept? earlier than propose
            this.curPropose = new Propose(acceptRequest.getBallotID());
            this.hasPromised=true;
            this.latestAcceptRequest=acceptRequest;
            this.hasAccepted=true;
            AcceptAck a = new AcceptAck(true, acceptRequest.getBallotID());
            gcl.sendMsg(a, sender);
            if(this.paxos.debug) System.out.println(acceptRequest.getBallotID()+" acceptRequest true");
        }
    }

    private void handleAcceptAck(AcceptAck acceptAck, String sender) {
        this.paxos.acceptAckQueue.add(acceptAck);
        if(this.paxos.debug) System.out.println(acceptAck.getBallotID()+" new AcceptAck received");
    }

    private void handleConfirm(Confirm confirm, String sender) {
        if(this.maxConfirmID==null){
            // if we do not have a ConfirmID yet
            this.maxConfirmID=confirm.getBallotID();
            this.tempConfirmQueue.add(confirm); // tempConfirmQueue: buffers all confirm object 
            ConfirmAck c = new ConfirmAck(true, confirm.getBallotID());
            gcl.broadcastMsg(c);
            if(this.paxos.debug) System.out.println(confirm.getBallotID()+" confirm accepted");
        }else{
            if(maxConfirmID.compareTo(confirm.getBallotID())>0){
                // if we received a confirm with ID <  Confirm 
                // reject this confirm 
                ConfirmAck c = new ConfirmAck(false, confirm.getBallotID());
                gcl.broadcastMsg(c);
                if(this.paxos.debug) System.out.println(confirm.getBallotID()+" confirm rejected");
            }else if(maxConfirmID.compareTo(confirm.getBallotID())<0){

                this.maxConfirmID=confirm.getBallotID();
                this.tempConfirmQueue.add(confirm);
                ConfirmAck c = new ConfirmAck(true, confirm.getBallotID());
                gcl.broadcastMsg(c);
                if(this.paxos.debug) System.out.println(confirm.getBallotID()+" confirm accepted");
            }else{
                if(this.paxos.debug) System.out.println(this.maxConfirmID.toString());
                if(this.paxos.debug) System.out.println(confirm.getBallotID().toString());
                if(this.paxos.debug) System.out.println(confirm.getBallotID()+" confirm already accepted");
            }
        }
    }


    private void handleConfirmAck(ConfirmAck confirmAck, String sender) {
        // confirmAckBuffer Hashmap : key = BallotID
        // val = Arraylist of the corresponding confirmAck
        // check if a Reject is inside such confirm 
        if(confirmAckBuffer.containsKey(confirmAck.getBallotID().toString())){

            confirmAckBuffer.get(confirmAck.getBallotID().toString()).add(confirmAck);
            if(this.paxos.debug) System.out.println(confirmAck.getBallotID().toString()+" new confirmAck received1.");
        }else{
            ArrayList<ConfirmAck> newlist = new ArrayList<>();
            confirmAckBuffer.put(confirmAck.getBallotID().toString(),newlist);
            confirmAckBuffer.get(confirmAck.getBallotID().toString()).add(confirmAck);
            if(this.paxos.debug) System.out.println(confirmAck.getBallotID().toString()+" new confirmAck received2.");
        }
    }
}
