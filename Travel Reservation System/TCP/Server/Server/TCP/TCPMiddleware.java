package Server.TCP;

import Server.Middleware.*;

import java.net.ServerSocket;
import java.net.Socket;

import Server.Common.*;

public class TCPMiddleware {
    private static String s_rmiPrefix = "group_42_";
    private static String s_serverName = "middleware";
    private static MiddlewareManager middleware;
    private static ServerSocket serverSocket;
    private static int port = 4342;

    public static void help(String s) {
        System.out.println("Could not parse argument \""+s+"\"."+"\nPlease use only the following arguments:");
        System.out.println("MiddlewarePort FlightServerConfig CarServerConfig RoomServerConfig");
        System.out.println("ServerConfig should be in form of \"name,hostname,port\"");
        System.exit(1);
        //./run_middleware.sh middleware flight,lab2-11.cs.mcgill.ca,4342
    }

    public static MiddlewareManager opts(String[] args) {
        StringBuilder sb = new StringBuilder();
        for(String str : args) sb.append(str).append(" ");
        if(args.length!=4){
            System.out.println(args.length);
            help(sb.toString());
        }
        else{
            try {
                try {
                    port = Integer.parseInt(args[0]); 
                    RemoteServer flight_server = null;
                    RemoteServer car_server = null;
                    RemoteServer room_server = null;
                    if(!args[1].equals("null")){
                        String[] flight = args[1].split(",");
                        flight_server = new RemoteServer(flight[1], flight[2]);
                    }
                    if(!args[2].equals("null")){
                        String[] car = args[2].split(",");
                        car_server = new RemoteServer(car[1], car[2]);
                    }
                    if(!args[3].equals("null")){
                        String[] room = args[3].split(",");
                        room_server = new RemoteServer(room[1], room[2]);
                    }
                    return new MiddlewareManager(s_rmiPrefix + s_serverName, flight_server, car_server, room_server);
                } catch (Exception e){
                    help(sb.toString());
                }
            } catch (Exception e){
                help(sb.toString());
            }
        }
        //should not reach here
        return null;
    }

    public static void createServer(MiddlewareManager manager){
        try{
            serverSocket = new ServerSocket(port);
            System.out.println("Start to listening on port: " + port);
            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("New socket created");
                new TCPRequestHandler(socket, manager).start();
            }     
        }catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}   
    }

    public static void main(String[] args) {
        middleware = opts(args);
        middleware.connectServers();
        TCPMiddleware.createServer(middleware);
    }
}
