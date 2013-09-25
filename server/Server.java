package server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.Hello;

public class Server implements Hello {
    private static final DEFAULT_PORT = 0;

    public Server() {}

    public String sayHello() {
        return "Hello, world!";
    }

    private Location[][] maze;

    public static void main(String args[]) {
	Hello stub = null;
	Registry registry = null;
    if (args.length < 2) {
        throw new IllegalArgumentsException();
        System.exit(0);
    }

    int N = Integer.parseInt(args[0]);
    int M = Integer.parseInt(args[1]);

	try {
	    Server obj = new Server();
	    stub = (Hello) UnicastRemoteObject.exportObject(obj, DEFAULT_PORT);
	    registry = LocateRegistry.getRegistry();
	    registry.bind("Hello", stub);

	    System.err.println("Server ready");
	} catch (Exception e) {
	    try{
		registry.unbind("Hello");
		registry.bind("Hello",stub);
	    	System.err.println("Server ready");
	    }catch(Exception ee){
		System.err.println("Server exception: " + ee.toString());
	    	ee.printStackTrace();
	    }
	}
    }
}
