package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.Reply.Direction;
import common.Reply.Status;

public interface ServerApi extends Remote {
	public String SERVER_REGISTRY_PREFIX = "ServerApi";

	Reply move(int id, Direction direction) throws RemoteException;

	Status movePlayer(Player player, Direction direction) throws RemoteException;
	
	void ping() throws RemoteException;

	void initializeBackup(Location[][] maze, List<Player> playerSet, int mainServerId)
			throws RemoteException;
}
