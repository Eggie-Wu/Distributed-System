package paxos;
import comp512.utils.FailCheck;

import java.util.ArrayList;
import java.util.Random;


class ProposeTracker extends Thread{
    volatile boolean live = true;
    private final Paxos paxos;
    Propose curPropose;
    AcceptRequest curAcceptRequest;

    BallotID max_id = null;
    ArrayList<String> order=null;

    volatile ArrayList<String> buffer = new ArrayList<>();


    ProposeTracker(Paxos paxos) {
        this.paxos = paxos;
    }

    @Override
    public void run(){
        while(live || paxos.canPropose()){
            if(paxos.canPropose()){
                if(this.paxos.debug) {
                    System.out.println("======================");
                    System.out.println("I am proposing");
                }
                int exitNumber = proposeOrder();
                if(this.paxos.debug){
                    System.out.println("I finish proposing");
                    System.out.println("======================");
                }
                this.order=null;
                if(exitNumber==1){
                    try{
                        Random random = new Random();
                        sleep(random.nextInt(500)+1500);
                    }catch(Exception Ignore){
                    }
                }else if(exitNumber==2){
                    try{
                        Random random = new Random();
                        sleep(random.nextInt(500)+1000);
                    }catch(Exception Ignore){
                    }
                }else if(exitNumber==3){
                    try{
                        Random random = new Random();
                        sleep(random.nextInt(700)+800);
                    }catch(Exception Ignore){
                    }
                }else{
                    try{
                        Random random = new Random();
                        sleep(random.nextInt(150)+50);
                    }catch(Exception Ignore){
                    }
                }
            }
            else{
                if(!live && !paxos.canPropose()) break;
                try{
                    Random random = new Random();
                    sleep(random.nextInt(150)+50);
                }catch(Exception e){
                    if(!live && !paxos.canPropose()) break;
                }
            }

        }
        System.out.println("ProposerTracker finished");
    }

    Promise getNextPromise(){
        while(this.paxos.promiseQueue.isEmpty()){
            if(!live && !paxos.canPropose()) return null;
        }
        return this.paxos.promiseQueue.poll();
    }

    AcceptAck getNextAcceptAck(){
        while(this.paxos.acceptAckQueue.isEmpty()){
            if(!live && !paxos.canPropose()) return null;
        }
        return this.paxos.acceptAckQueue.poll();
    }

    ConfirmAck getNextConfirmSummary(){
        while(this.paxos.confirmSummaryQueue.isEmpty()){
            if(!live && !paxos.canPropose()) return null;
        }
        return this.paxos.confirmSummaryQueue.poll();
    }


    public int proposeOrder(){
        sendPropose();
        this.paxos.failCheck.checkFailure(FailCheck.FailureType.AFTERSENDPROPOSE);
        if(!waitForPromise()){
            if(this.paxos.debug) System.out.println("proposeRequest failed "+curPropose.getBallotID());
            return 1;
        }
        if(this.paxos.debug) System.out.println("received all promised "+curPropose.getBallotID());
        this.paxos.failCheck.checkFailure(FailCheck.FailureType.AFTERBECOMINGLEADER);
        if(!sendAcceptRequest()){
            if(this.paxos.debug) System.out.println("acceptRequest order empty after cleaning "+curPropose.getBallotID());
            return 2;
        }
        if(!waitForAcceptAck()){
            if(this.paxos.debug) System.out.println("acceptRequest failed "+curPropose.getBallotID());
            return 3;
        }
        this.paxos.failCheck.checkFailure(FailCheck.FailureType.AFTERVALUEACCEPT);
        if(this.paxos.debug) System.out.println("received all AcceptACK "+curPropose.getBallotID());
        sendConfirm();
        if(!waitConfirmSummary()){
            if(this.paxos.debug) System.out.println("confirm failed "+curPropose.getBallotID());
            return 4;
        }
        if(this.paxos.debug){
            System.out.println("confirm successful "+curPropose.getBallotID());
        }
        return 5;
    }

    private void sendPropose() {
        this.curPropose = new Propose(new BallotID(paxos.myProcess));
        this.paxos.gcl.broadcastMsg(this.curPropose);
        if(this.paxos.debug) System.out.println("propose sent "+curPropose.getBallotID());
    }

    private boolean waitForPromise() {
        int count_promise = 0;
        boolean success = true;
        while (count_promise < (this.paxos.getNumProcess() / 2 + 1)){
            Promise p = this.getNextPromise();
            if(p==null) return false;
            // next promise = p
            if(p.getBallotID().compareTo(curPropose.getBallotID())==0){
                // ballotID increasing order 
                // current BallotID of p is equal to the ballotid of the current process
                if (p.isPromise()) {
                    if (p.getOrder() != null) {
                        // inherit order from the previous process
                        if(this.max_id==null){
                            this.max_id=p.getOtherID();
                            this.order = p.getOrder(); 
                        }else if(p.getOtherID().compareTo(this.max_id)>0){
                            // 
                            // 
                            this.max_id=p.getOtherID();
                            this.order = p.getOrder();
                        }
                    }
                }else {
                    success = false;
                }
                count_promise += 1;
            }
            // if next BallotID of the next promise is not equal to the BallotID of the current process
        }
        return success;
    }

    private boolean sendAcceptRequest() {
        this.order = new ArrayList<>(this.paxos.msgKeys()); // the desired order of the propose
        this.order.removeAll(this.paxos.deliveredKey);
        if(this.order.isEmpty()){
            return false;
        }
        this.curAcceptRequest = new AcceptRequest(this.curPropose.getBallotID(), this.order);
        this.paxos.gcl.broadcastMsg(this.curAcceptRequest);
        if(this.paxos.debug) System.out.println("acceptRequest sent "+curPropose.getBallotID());
        return true;
    }

    private boolean waitForAcceptAck() {
        int count_accept = 0;
        boolean success = true;
        while(count_accept < (this.paxos.getNumProcess()  / 2 + 1)){
            AcceptAck ack = this.getNextAcceptAck();
            if(ack == null) return false;
            if(ack.getBallotID().compareTo(curPropose.getBallotID())==0){
                // next ack ballotID = cur ballotID
                if (!ack.isAccept()){
                    success = false;
                }
                count_accept += 1;
            }
        }
        return success;
    }


    private void sendConfirm() {
        Confirm confirm = new Confirm(this.curPropose.getBallotID(), this.order);
        this.paxos.gcl.broadcastMsg(confirm);
        if(this.paxos.debug) System.out.println("confirm sent "+curPropose.getBallotID());
    }

    private boolean waitConfirmSummary() {
        while(true){
            ConfirmAck summary = this.getNextConfirmSummary();
            if(summary==null) return false;
            if(curPropose.getBallotID().compareTo(summary.getBallotID())==0){
                if(summary.isConfirm()){
                    return true;
                }else{
                    return false;
                }
            }
        }
    }
}
