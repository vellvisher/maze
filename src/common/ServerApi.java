package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import common.Reply.Direction;

public interface ServerApi extends Remote {
	public String SERVER_REGISTRY_PREFIX = "ServerApi";

	Reply move(int id, Direction direction, Timestamp timestamp) throws RemoteException;

	Reply movePlayer(Player player, Direction direction, Timestamp timestamp) throws RemoteException;
	
	void ping() throws RemoteException;

	void initializeBackup(Location[][] maze, List<Player> players,
			int mainServerId, Map<Timestamp, Reply> movesPlayed) throws RemoteException;
}
