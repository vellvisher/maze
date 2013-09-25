package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.ServerApi;

public class Client {

    private Client() {}

    public static void main(String[] args) {

	String host = (args.length < 1) ? null : args[0];
	try {
	    Registry registry = LocateRegistry.getRegistry(host);
	    ServerApi stub = (ServerApi) registry.lookup("ServerApi");
	    String response = stub.sayHello();
	    System.out.println("response: " + response);
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
    }
}
