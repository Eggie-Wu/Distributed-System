// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.TCP;

import Server.Common.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class TCPResourceManager extends ResourceManager 
{
	private static String s_serverName = "Server";
	private static String s_rmiPrefix = "group_42_";
	private static int s_serverPort = 43342;
	private ServerSocket serverSocket;

	public TCPResourceManager(String name)
	{
		super(name);
	}

	public static void main(String args[])
	{
		if (args.length > 0)
		{
			s_serverName = args[0];
		}
		if(args.length > 1){
			s_serverPort = Integer.parseInt(args[1]);
		}
			
		// Create the RMI server entry
		try {
			// Create a new Server object
			TCPResourceManager manager = new TCPResourceManager(s_rmiPrefix + s_serverName);
			// Dynamically generate the stub (client proxy)
			manager.serverSocket = new ServerSocket(s_serverPort);
			System.out.println("Start to listening on port: " + s_serverPort);
			while(true){
				Socket socket = manager.serverSocket.accept();
                System.out.println("New socket created");
                new TCPRequestHandler(socket, manager).start();
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
