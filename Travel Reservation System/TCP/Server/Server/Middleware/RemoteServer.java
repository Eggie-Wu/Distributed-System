
package Server.Middleware;
import Server.Common.Trace;

import java.net.Socket;
import java.util.*;
import java.io.*;


public class RemoteServer {
    public String host;
    public int port;
    private Socket socket;
    private PrintWriter outToServer; 
    private BufferedReader inFromServer;
     
    public RemoteServer(String host, String port) {
        this.host = host;
        this.port = Integer.parseInt(port);
    }

    public void connectServer() {
        try {
			boolean first = true;
			while (true) {
				try {
					this.socket = new Socket(host, port);
					outToServer= new PrintWriter(socket.getOutputStream(),true); // open an output stream to the server...
    				inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					System.out.println("Connected to host: "+host+" at port: "+port);
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            outToServer.println("quit");
                        }
                    });
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
		if(message==null) return "IllegalArgument";
		String buffer;
		try{
			System.out.println("To remote server: " + message);
			outToServer.println(message); // send the user's input via the output stream to the server
		}catch(Exception e){
			System.out.println("Trying to reconnect to the server");
			this.connectServer();
			this.sendMessage(message);
		}
		if(message.equals("quit")) return "";
		try{
			while(true){
                buffer=inFromServer.readLine();
                if(buffer==null || buffer.length()==0) break;
                System.out.println("Result from remote server: " + buffer);
				sb.append(buffer).append("\n");
			}
			if(sb.toString().equals("")){
				System.out.println("Trying to reconnect to the server");
				this.connectServer();
			}
			sb.deleteCharAt(sb.length()-1);
			return sb.toString();
		}catch(Exception e){
			System.out.println("Trying to reconnect to the server");
			this.connectServer();
		}
        return "IllegalArgument";
	}
}
