package common;

import java.rmi.RemoteException;

import common.Reply.Direction;

public interface PeerServerApi extends ServerApi {
    Reply backupMove(int id, Direction direction) throws RemoteException;
}
