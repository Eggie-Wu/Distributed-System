
package Server.Middleware;
import java.rmi.Remote;
import Server.Common.Trace;
import java.util.*;
import Server.Interface.IResourceManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RemoteServer {
    public String name;
    public String host;
    public int port;
    public IResourceManager instance = null;

    public RemoteServer(String name, String host, String port) {
        this.name = name;
        this.host = host;
        this.port = Integer.parseInt(port);
    }

    public void connectServer() {
        try {
            boolean flag = true;

            while(true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(this.host, this.port);
                    this.instance = (IResourceManager)registry.lookup(this.name);
                    System.out.println("Connected to '" + this.name + "' server [" + this.host + ":" + this.port + "/" + this.name + "]");
                    break;
                } catch (RemoteException | NotBoundException exception) {
                    if (flag) {
                        System.out.println("Waiting for '" + this.name + "' server [" + this.host + ":" + this.port + "/" + this.name + "]");
                        flag = false;
                    }
                    Thread.sleep(500L);
                }
            }
        } catch (Exception exception) {
            System.err.println("\u001b[31;1mServer exception: \u001b[0mUncaught exception");
            exception.printStackTrace();
            System.exit(1);
        }
    }
}
