package paxos;
// Access to the GCL layer
import comp512.gcl.*;

import comp512.utils.*;

// Any other imports that you may need.
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import java.net.UnknownHostException;

import static java.lang.Thread.sleep;


// ANY OTHER classes, etc., that you add must be private to this package and not visible to the application layer.

// extend / implement whatever interface, etc. as required.
// NO OTHER public members / methods allowed. broadcastTOMsg, acceptTOMsg, and shutdownPaxos must be the only visible methods to the application layer.
//		You should also not change the signature of these methods (arguments and return value) other aspects maybe changed with reasonable design needs.
public class Paxos
{
	GCL gcl;
	FailCheck failCheck;
	MessageListener messageListener;
	ProposeTracker proposeTracker;
	String myProcess;
	AtomicInteger numProcess;
	int msgCount = 0;

	private final ConcurrentHashMap<String, Object> msgBuffer = new ConcurrentHashMap<>();
	final ConcurrentLinkedQueue<String> determinedOrder = new ConcurrentLinkedQueue<>();
	final ConcurrentLinkedQueue<String> deliveredKey = new ConcurrentLinkedQueue<>();

	volatile ConcurrentLinkedQueue<Promise> promiseQueue = new ConcurrentLinkedQueue<>(); // promise collected for process p
	volatile ConcurrentLinkedQueue<AcceptAck> acceptAckQueue = new ConcurrentLinkedQueue<>();
	volatile ConcurrentLinkedQueue<ConfirmAck> confirmSummaryQueue = new ConcurrentLinkedQueue<>();

	final boolean debug = false;

	long startTime;

	final AtomicInteger shutdownAckCount = new AtomicInteger(0);

	public Paxos(String myProcess, String[] allGroupProcesses, Logger logger, FailCheck failCheck) throws IOException, UnknownHostException
	{
		// Rember to call the failCheck.checkFailure(..) with appropriate arguments throughout your Paxos code to force fail points if necessary.
		this.failCheck = failCheck;

		// Initialize the GCL communication system as well as anything else you need to.
		this.gcl = new GCL(myProcess, allGroupProcesses, null, logger) ;

		this.myProcess = myProcess;

		this.numProcess = new AtomicInteger(allGroupProcesses.length);

		startTime = System.nanoTime();
		System.out.println("Starting at "+startTime);

		this.messageListener = new MessageListener(gcl, this);

		this.messageListener.start();

		this.proposeTracker = new ProposeTracker(this);

		this.proposeTracker.start();

	}

	boolean hasUndeliveredMsg(){
		return !this.msgBuffer.isEmpty();
	}

	public ArrayList<String> msgKeys(){
		return new ArrayList<>(this.msgBuffer.keySet());
	}

	public boolean canPropose(){
		// 
		return !msgBuffer.isEmpty() && determinedOrder.isEmpty();
	}

	public Object nextMsg() {
		while(this.determinedOrder.isEmpty()); // busy wait
		String key = this.determinedOrder.poll();

		while(deliveredKey.contains(key)){ // msg_id 
			while(this.determinedOrder.isEmpty()); 
			key = this.determinedOrder.poll();
		}
		while(!msgBuffer.containsKey(key));
		Object val = msgBuffer.get(key);
		msgBuffer.remove(key);
		deliveredKey.add(key);
		return val;
	}

	public void addToBuffer(Message message){
		this.msgBuffer.put(message.getId(), message.getVal());
	}
	int getNumProcess(){
		return this.numProcess.get();
	}

	void decrementNumProcess(){
		this.numProcess.decrementAndGet();
	}

	public void addOrder(ArrayList<String> order){
		this.determinedOrder.addAll(order);
	}

	public String newMsgID(){
		String id = myProcess + "_" + msgCount;
		msgCount++;
		return id;
	}

	// This is what the application layer is going to call to send a message/value, such as the player and the move
	public void broadcastTOMsg(Object val)
	{
		// This is just a placeholder.
		// Extend this to build whatever Paxos logic you need to make sure the messaging system is total order.
		// Here you will have to ensure that the CALL BLOCKS, and is returned ONLY when a majority (and immediately upon majority) of processes have accepted the value.
		String msgid = newMsgID();
		Message message = new Message(msgid, val);
		gcl.broadcastMsg(message);
		while(!this.deliveredKey.contains(msgid));
	}

	// This is what the application layer is calling to figure out what is the next message in the total order.
	// Messages delivered in ALL the processes in the group should deliver this in the same order.
	public Object acceptTOMsg() throws InterruptedException
	{
		// This is just a placeholder
		Object val = this.nextMsg();
		return val;
	}

	// Add any of your own shutdown code into this method.
	public void shutdownPaxos()
	{	
		while(hasUndeliveredMsg());
		try{
			sleep(5000);
		}catch(Exception Ignored){}
		this.gcl.broadcastMsg(new Shutdown());
		System.out.println("Shutdown broadcast");
		while(this.shutdownAckCount.get()<this.numProcess.get());
		while(hasUndeliveredMsg());
		this.messageListener.live = false;
		this.proposeTracker.live = false;
		try{
			this.messageListener.join();
			this.proposeTracker.join();
		}catch (Exception ignored){}
		long endTime = System.nanoTime();

		System.out.println("End time is "+endTime);
		System.out.println("Time used: "+ (endTime - this.startTime)/1000000 + " ms");
		gcl.shutdownGCL();
	}
}

