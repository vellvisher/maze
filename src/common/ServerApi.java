package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Reply.Direction;

public interface ServerApi extends Remote {
	public String SERVER_REGISTRY_PREFIX = "ServerApi";
    Reply move(int id, Direction direction) throws RemoteException;
    Reply joinGame() throws RemoteException;
    void ping() throws RemoteException;
}
