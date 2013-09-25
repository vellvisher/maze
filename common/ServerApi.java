package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerApi extends Remote {
    String sayHello() throws RemoteException;
    Reply move(int id, Direction direction) throws RemoteException;
    Reply joinGame() throws RemoteException;
}
