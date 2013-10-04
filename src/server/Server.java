package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import common.Location;
import common.Player;
import common.Reply;
import common.Reply.Direction;
import common.Reply.Status;
import common.ServerApi;

public class Server implements ServerApi {
	protected static final int DEFAULT_PORT = 0;
	
	protected static int id;
	protected static int N;

	protected Location[][] maze;
	protected Map<Integer, Player> syncPlayers = Collections
			.synchronizedMap(new HashMap<Integer, Player>());
	protected Set<Player> playerList;
	protected Server backupServer;
	
	public Server(int id, int n) {
		Server.id = id;
		N = n;
		maze = new Location[N][N];
	}

	public Server(Location[][] maze, Map<Integer, Player> syncPlayers, Set<Player> playerList) {
		this.maze = maze;
		this.syncPlayers = syncPlayers;
		this.playerList = playerList;
	}

	protected void updatePlayerPosition(Player p, int pX, int pY) {
		p.setX(pX);
		p.setY(pY);
		p.addTreasures(maze[pX][pY].getTreasures());
		maze[pX][pY].clearTreasures();
		maze[pX][pY].setPlayer(p);
	}

	@Override
	public Reply move(int id, Direction d) {
		Player player;
		if (d == null) {
			return new Reply(null, null, null, Status.INVALID_MOVE);
		}
		synchronized (syncPlayers) {
			player = syncPlayers.get(id);
			if (player == null) {
				return new Reply(null, null, null, Status.INVALID_MOVE);
			}
		}
		synchronized (maze) {
			// backupServer.movePlayer(player, d);
			return new Reply(maze, player, playerList, movePlayer(player, d));
		}
	}

	private Status movePlayer(Player p, Direction d) {
		int newX = p.getX(), newY = p.getY();
		switch (d) {
		case N:
			newX--;
			break;
		case S:
			newX++;
			break;
		case W:
			newY--;
			break;
		case E:
			newY++;
			break;
		case NoMove:
			p.addTreasures(0);
			return Status.MOVE_SUCCESSFUL;
		default:
			return Status.INVALID_MOVE;
		}
		if (newX < 0 || newY < 0 || newX == N || newY == N) {
			return Status.OUT_OF_BOUNDS;
		} else if (maze[newX][newY].getPlayer() != null) {
			return Status.PLAYER_BLOCKING;
		}

		maze[p.getX()][p.getY()].clearPlayer();
		updatePlayerPosition(p, newX, newY);

		return Status.MOVE_SUCCESSFUL;
	}

	public void runServer() {
		ServerApi stub = null;
		Registry registry = null;

		try {
			stub = (ServerApi) UnicastRemoteObject.exportObject(this,
					DEFAULT_PORT);
			registry = LocateRegistry.getRegistry();
			registry.bind(ServerApi.SERVER_REGISTRY_PREFIX + id, stub);

			System.err.println("Server ready");
		} catch (Exception e) {
			try {
				e.printStackTrace();
				registry.unbind(ServerApi.SERVER_REGISTRY_PREFIX + id);
				registry.bind(ServerApi.SERVER_REGISTRY_PREFIX + id, stub);
				System.err.println("Server ready");
			} catch (Exception ee) {
				System.err.println("Server exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}

	@Override
	public void ping() throws RemoteException {
	}

	@Override
	public void initializeBackup(Location[][] maze,
			HashMap<Integer, Player> playerMap) {
		// no clue
	}
}
