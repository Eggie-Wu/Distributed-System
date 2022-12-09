package Client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

import java.util.*;
import java.io.*;
import java.net.*;


public class TCPClient extends Client
{
	private static String host = "localhost";
	private static int port = 4342;

	private Socket socket; 
	// establish a socket with a server using the given port#
  
    PrintWriter outToServer; 
	// open an output stream to the server...

    BufferedReader inFromServer; 
	// open an input stream from the server...

	public TCPClient()
	{
		super();
	}

	public void connectServer(){
		try {
			boolean first = true;
			while (true) {
				try {
					this.socket = new Socket(host, port);
					outToServer= new PrintWriter(socket.getOutputStream(),true); // open an output stream to the server...
    				inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					System.out.println("Connected to host: "+host+" at port: "+port);
					break;
				}
				catch (Exception e) {
					if (first) {
						System.out.println("Waiting for host: "+host+" at port: "+port);
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public String sendMessage(String message){
		StringBuilder sb = new StringBuilder();
		String buffer;
		try{
			outToServer.println(message); // send the user's input via the output stream to the server
		}catch(Exception e){
			System.out.println("Trying to reconnect to the server");
			this.connectServer();
			this.sendMessage(message);
		}
		if(message.equals("quit")) return null;
		try{
			while(true){
                buffer=inFromServer.readLine();
				if(buffer==null || buffer.length()==0) break;
				sb.append(buffer);
			}
			if(sb.toString().equals("")){
				System.out.println("Trying to reconnect to the server");
				this.connectServer();
			}
			System.out.println();
			if(sb.toString().equals("true")) System.out.println("success");
			else System.out.println(sb); // print the server result to the user
			return sb.toString();
		}catch(Exception e){
			System.out.println("Trying to reconnect to the server");
			this.connectServer();
		}
		return null;
	}

	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			host = args[0];
		}
		if (args.length > 1)
		{
			port = Integer.parseInt(args[1]);
		}
		if (args.length > 2)
		{
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [host port]");
			System.exit(1);
		}

		// Get a reference to the RMIRegister
		try {
			TCPClient client = new TCPClient();
			client.connectServer();
			Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    client.sendMessage("quit");
                }
            });
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

