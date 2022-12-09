/*
Copyright
All materials provided to the students as part of this course is the property of respective authors. Publishing them to third-party (including websites) is prohibited. Students may save it for their personal use, indefinitely, including personal cloud storage spaces. Further, no assessments published as part of this course may be shared with anyone else. Violators of this copyright infringement may face legal actions in addition to the University disciplinary proceedings.
©2022, Joseph D’Silva
*/
import java.io.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
// To get the name of the host.
import java.net.*;

//To get the process id.
import java.lang.management.*;
import java.nio.charset.StandardCharsets;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.KeeperException.*;
import org.apache.zookeeper.data.Stat;

// TODO
// Replace XX with your group number.
// You may have to add other interfaces such as for threading, etc., as needed.
// This class will contain the logic for both your master process as well as the worker processes.
//  Make sure that the callbacks and watch do not conflict between your master's logic and worker's logic.
//		This is important as both the master and worker may need same kind of callbacks and could result
//			with the same callback functions.
//	For a simple implementation I have written all the code in a single class (including the callbacks).
//		You are free it break it apart into multiple classes, if that is your programming style or helps
//		you manage the code more modularly.
//	REMEMBER !! ZK client library is single thread - Watches & CallBacks should not be used for time consuming tasks.
//		Ideally, Watches & CallBacks should only be used to assign the "work" to a separate thread inside your program.
public class DistProcess implements Watcher, AsyncCallback.ChildrenCallback {
	ZooKeeper zk;
	String zkServer, pinfo;
	boolean isMaster=false;
	boolean initalized=false;
	//ConcurrentLinkedQueue<String> taskBuffer = new ConcurrentLinkedQueue<>();
	//ConcurrentLinkedQueue<String> freeWorkerBuffer = new ConcurrentLinkedQueue<>();

	DistProcess(String zkhost){
		zkServer=zkhost;
		pinfo = ManagementFactory.getRuntimeMXBean().getName();
//		System.out.println("DISTAPP : ZK Connection information : " + zkServer);
//		System.out.println("DISTAPP : Process information : " + pinfo);
	}

	public void processResult(int rc, String path, Object ctx, List<String> children){}


	synchronized String findFreeWorker(){
		List<String> children = null;
		try{
			children = zk.getChildren("/dist42/workers", false);
		}
		catch(KeeperException ke){System.out.println(ke);}
		catch(InterruptedException ie){System.out.println(ie);}	
		if(children==null){
			//System.out.println("No worker exists when finding free worker");
			return null;
		}
		Collections.shuffle(children);
		for(String child : children){
			try{
				byte[] statusSerial = zk.getData("/dist42/workers/"+child, false, null);
				String status = new String(statusSerial, StandardCharsets.UTF_8);
				if(status.equals("free")){
					//System.out.println("**** Worker "+child+" is "+status);
					return "/dist42/workers/"+child;
				}else{
					//System.out.println("**** Worker "+child+" is busy");
				}
			}
			catch(KeeperException ke){System.out.println(ke);}
			catch(InterruptedException ie){System.out.println(ie);}	

		}
		//System.out.println("No worker is free when finding free worker");
		return null;
	}

	synchronized String findUnassignedTask(){
		List<String> children = null;
		try{
			children = zk.getChildren("/dist42/tasks", false);
		}
		catch(KeeperException ke){System.out.println(ke);}
		catch(InterruptedException ie){System.out.println(ie);}
		if(children==null){
			//System.out.println("No task exists when finding Unassigned Task");
			return null;
		}
		for(String t : children){
			try{
				if(zk.exists("/dist42/tasks/"+t+"/result", false)==null&&zk.exists("/dist42/tasks/"+t+"/assigned", false)==null){
					//System.out.println("**** Task "+t+" is unassigned");
					return "/dist42/tasks/"+t;
				}else{
					//System.out.println("**** Worker "+child+" is busy");
				}
			}
			catch(KeeperException ke){System.out.println(ke);}
			catch(InterruptedException ie){System.out.println(ie);}

		}
		//System.out.println("No task is unassigned when finding Unassigned Task");
		return null;
	}

	void assignTaskToWorker(String worker, String task){
		try{
			if (task==null&&worker==null){
				System.out.println("*** Both task and worker are null when assign task to worker, returning...");
				return;
			}else{
				if (task == null){
					System.out.println("*** task is null when assign task to worker, returning...");
					return;
				}
				if (worker == null){
					System.out.println("*** worker is null when assign task to worker, returning...");
					return;
				}
			}
			System.out.println("Assigning "+task+" to "+worker+" ...");
			zk.setData(worker, task.getBytes(), -1);
			zk.create(task + "/assigned", worker.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			installResultWatcher(task+"/result");
			System.out.println("Task "+task+" has been assigned to "+worker+" \n\n");
		}
		catch(KeeperException ke){System.out.println(ke);}
		catch(InterruptedException ie){System.out.println(ie);}	
	}

	void startProcess() throws IOException, UnknownHostException, KeeperException, InterruptedException{
		zk = new ZooKeeper(zkServer, 10000, this); //connect to ZK.
	}

	void initalize() {
		System.out.println("\n\n\n\n");
		try {
			runForMaster();	// See if you can become the master (i.e, no other master exists)
			isMaster=true;
			removeAllTasks();
			getWorkers();
			getTasks();
		}
		catch(NodeExistsException nee) {
			try{
				isMaster=false;
				createWorkerNode();
				registerWorkerWatcher();
			}
			catch(UnknownHostException uhe) { System.out.println(uhe); }
			catch(KeeperException ke) { System.out.println(ke); }
			catch(InterruptedException ie) { System.out.println(ie); }
		}
		catch(UnknownHostException uhe) { System.out.println(uhe); }
		catch(KeeperException ke) { System.out.println(ke); }
		catch(InterruptedException ie) { System.out.println(ie); }
		System.out.println("Role : I will be functioning as " +(isMaster?"master":"worker"));
	}

	public void process(WatchedEvent e){
		if(e.getType() == Watcher.Event.EventType.None){
			if(e.getPath() == null && e.getState() ==  Watcher.Event.KeeperState.SyncConnected && initalized == false){
				initalize();
				initalized = true;
			}
		}
	}

	void runForMaster() throws UnknownHostException, KeeperException, InterruptedException {
		zk.create("/dist42/master", pinfo.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	void removeAllTasks() throws UnknownHostException, KeeperException, InterruptedException{
		List<String> children = zk.getChildren("/dist42/tasks", false);
		for(String child : children){
			List<String> result = zk.getChildren("/dist42/tasks/"+child, false);
			if(result.size()!=0){
				for(String r: result){
					zk.delete("/dist42/tasks/"+child+"/"+r, -1);
				}
			}
			zk.delete("/dist42/tasks/"+child, -1);
		}
		System.out.println("Removed all previous tasks");
	}

	void getWorkers(){
		zk.getChildren("/dist42/workers", newWorkerAddedWatcher, workerAddedCallback, null);  
	}
	
	Watcher newWorkerAddedWatcher = new Watcher(){
		public void process(WatchedEvent e){
			if(e.getType() == Watcher.Event.EventType.NodeChildrenChanged && e.getPath().equals("/dist42/workers")){
				getWorkers();
			}else{
				getWorkers();
			}
		}
	};

	ChildrenCallback workerAddedCallback = new ChildrenCallback(){
		public void processResult(int rc, String path, Object ctx, List<String> children){
			String worker = findFreeWorker();
			if(worker!=null){
				String task = findUnassignedTask();
				if(task!=null){
					System.out.println("WorkerAddedCallback: " + " current unassigned task is " + task + ", free worker is " + worker);
					assignTaskToWorker(worker, task);
				}
			}else{
				//System.out.println("Find free worker failed in workerAddedCallback");
			}
		}
	};

	void getTasks(){
		zk.getChildren("/dist42/tasks", newTaskAddedWatcher, newTaskAddedCallback, null);
	}

	Watcher newTaskAddedWatcher = new Watcher(){
		public void process(WatchedEvent e){
			if(e.getType() == Watcher.Event.EventType.NodeChildrenChanged && e.getPath().equals("/dist42/tasks")){
				getTasks();
			}else{
				getTasks();
			}
		}
	};

	ChildrenCallback newTaskAddedCallback = new ChildrenCallback(){
		public void processResult(int rc, String path, Object ctx, List<String> children){
			String task = findUnassignedTask();
			if(task!=null){
				String worker = findFreeWorker();
				if(worker!=null) {
					System.out.println("NewTaskAddedCallback: " + " current unassigned task is " + task + ", free worker is " + worker);
					assignTaskToWorker(worker, task);
				}
			}else{
				//System.out.println("Find unassigned task failed in newTaskAddedCallback");
			}
		}
	};

	public void installResultWatcher(String taskPath){
		taskResultWatcherCB t= new taskResultWatcherCB(taskPath+"/result");
		zk.exists(taskPath+"/result", t, t, null);
	}

	public class taskResultWatcherCB implements Watcher,AsyncCallback.StatCallback{
		public String taskPath;

		public taskResultWatcherCB(String taskPath) {
			this.taskPath = taskPath;
		}

		@Override
		public void process(WatchedEvent e) {
			if(e.getType() == Watcher.Event.EventType.NodeCreated && e.getPath().equals(taskPath+"/result")){
				String worker = findFreeWorker();
				if(worker!=null){
					String task = findUnassignedTask();
					if(task!=null){
						System.out.println("WorkerAddedCallback: " + " current unassigned task is " + task + ", free worker is " + worker);
						assignTaskToWorker(worker, task);
					}
				}else{
					//System.out.println("Find free worker failed in taskResultWatcher");
				}
			}else{
				installResultWatcher(taskPath+"/result");
			}
		}
		@Override
		public void processResult(int i, String s, Object o, Stat stat) {
			String worker = findFreeWorker();
			if(worker!=null){
				String task = findUnassignedTask();
				if(task!=null){
					System.out.println("taskResultWatcherCallBack: " + " current unassigned task is " + task + ", free worker is " + worker);
					assignTaskToWorker(worker, task);
				}
			}else{
				//System.out.println("Find free worker failed in taskResultCallBack");
			}
		}
	}

	public void createWorkerNode()throws UnknownHostException, KeeperException, InterruptedException{
		System.out.println("CreateWorkerNode");
		zk.create("/dist42/workers/" + pinfo, "free".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void registerWorkerWatcher()throws UnknownHostException, KeeperException, InterruptedException{
		zk.exists("/dist42/workers/" + pinfo, workerWatcher);
	}

	Watcher workerWatcher = new Watcher(){
		public void process(WatchedEvent e){
			if(e.getType() == Watcher.Event.EventType.NodeDataChanged && e.getPath().equals("/dist42/workers/"+pinfo)){
				try{
					byte[] data = zk.getData("/dist42/workers/" + pinfo, false, null);
					String path = new String(data, StandardCharsets.UTF_8);
					if(!path.equals("free")&&!path.equals("hold")){
						byte[] taskSerial = zk.getData(path, false, null);
						System.out.println("\n\nWorking on "+path);
						ByteArrayInputStream bis = new ByteArrayInputStream(taskSerial);
						ObjectInput in = new ObjectInputStream(bis);
						DistTask dt = (DistTask) in.readObject();
						Thread computation = new Thread(){
							public void run() {};{
								dt.compute();
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								ObjectOutputStream oos = new ObjectOutputStream(bos);
								oos.writeObject(dt);
								oos.flush();
								final byte[] n_taskSerial = bos.toByteArray();
								zk.setData("/dist42/workers/" + pinfo, "free".getBytes(), -1);
								System.out.println("Worker "+ pinfo + " finished task "+ path +", set to be free\n\n");
								try{registerWorkerWatcher();}
								catch(UnknownHostException nee){System.out.println(nee);}
								catch(KeeperException ke){System.out.println(ke);}
								catch(InterruptedException ie){System.out.println(ie);}
								zk.create(path+"/result", n_taskSerial, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
							}
						};
						computation.start();
					}
				}
				catch(NodeExistsException nee){System.out.println(nee);}
				catch(KeeperException ke){System.out.println(ke);}
				catch(InterruptedException ie){System.out.println(ie);}
				catch(IOException io){System.out.println(io);}
				catch(ClassNotFoundException cne){System.out.println(cne);}
			}
			try{registerWorkerWatcher();}
			catch(UnknownHostException nee){System.out.println(nee);}
			catch(KeeperException ke){System.out.println(ke);}
			catch(InterruptedException ie){System.out.println(ie);}
		}
	};

	public static void main(String args[]) throws Exception
	{
		//Create a new process
		//Read the ZooKeeper ensemble information from the environment variable.
		DistProcess dt = new DistProcess(System.getenv("ZKSERVER"));
		dt.startProcess();
		//Replace this with an approach that will make sure that the process is up and running forever.
		while(true){}
	}
}
