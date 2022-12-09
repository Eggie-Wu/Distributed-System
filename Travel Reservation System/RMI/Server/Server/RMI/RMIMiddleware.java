package Server.RMI;
import Server.Middleware.*;
import Server.Interface.*;
import Server.Common.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NotBoundException;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class RMIMiddleware {
    private static String s_rmiPrefix = "group_42_";
    private static String s_serverName;
    private static Middleware middleware;
    private static int port = 4242;

    public static void help(String s) {
        System.out.println("Could not parse argument \""+s+"\"."+"\nPlease use only the following arguments:");
        System.out.println("MiddlewareName FlightServerConfig CarServerConfig RoomServerConfig");
        System.out.println("ServerConfig should be in form of \"name,hostname,port\"");
        System.exit(1);
        //./run_middleware.sh middleware flight,lab2-11.cs.mcgill.ca,4242
    }

    public static Middleware opts(String[] args) {
        StringBuilder sb = new StringBuilder();
        for(String str : args) sb.append(str).append(" ");
        if(args.length!=4){
            System.out.println(args.length);
            help(sb.toString());
        }
        else{
            try {
                s_serverName = args[0];
                RemoteServer flight_server = null;
                RemoteServer car_server = null;
                RemoteServer room_server = null;
                if(!args[1].equals("null")){
                    String[] flight = args[1].split(",");
                    flight_server = new RemoteServer(s_rmiPrefix+flight[0], flight[1], flight[2]);
                }
                if(!args[2].equals("null")){
                    String[] car = args[2].split(",");
                    car_server = new RemoteServer(s_rmiPrefix+car[0], car[1], car[2]);
                }
                if(!args[3].equals("null")){
                    String[] room = args[3].split(",");
                    room_server = new RemoteServer(s_rmiPrefix+room[0], room[1], room[2]);
                }
                return new Middleware(s_rmiPrefix + s_serverName, flight_server, car_server, room_server);
            } catch (Exception e){
                help(sb.toString());
            }
        }
        //should not reach here
        return null;
    }

    public static void createServer(){
        // Create the RMI server entry
		try {
			// Dynamically generate the stub (client proxy)
			IResourceManager resourceManager = (IResourceManager)UnicastRemoteObject.exportObject(middleware, 0);

			// Bind the remote object's stub in the registry
			Registry l_registry;
			try {
				l_registry = LocateRegistry.createRegistry(port);
			} catch (RemoteException e) {
				l_registry = LocateRegistry.getRegistry(port);
			}
			final Registry registry = l_registry;
			registry.rebind(s_rmiPrefix + s_serverName, resourceManager);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						registry.unbind(s_rmiPrefix + s_serverName);
						System.out.println("'" + s_serverName + "' resource manager unbound");
					}
					catch(Exception e) {
						System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
						e.printStackTrace();
					}
				}
			});                                       
			System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName  + "' at port: "+port);
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
        }
    }

    public static void main(String[] args) {
        middleware = opts(args);
        middleware.connectServers();
        createServer();
    }
}
