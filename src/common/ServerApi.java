package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Reply.Direction;

public interface ServerApi extends Remote {
    Reply move(int id, Direction direction) throws RemoteException;
    Reply joinGame() throws RemoteException;
}
