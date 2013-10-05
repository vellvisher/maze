package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Location;
import common.Player;
import common.Reply;
import common.Reply.Direction;
import common.Reply.Status;
import common.ServerApi;

public class Server extends RemoteServer implements ServerApi {
	private static final long serialVersionUID = 3126187241824135686L;

	protected static final int DEFAULT_PORT = 0;
	
	protected static int id;
	protected static int backupId;
	protected static int N;

	protected Location[][] maze;
	protected Map<Integer, Player> syncPlayers =
			Collections.synchronizedMap(new HashMap<Integer, Player>());
	protected List<Player> playerList;
	protected ServerApi backupServer;
	
	public Server(int id, int n) {
		Server.id = id;
		backupId = id;
		N = n;
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
		System.err.println("Player " + id + " wants to move " + d.toString());
		synchronized (this) {
			while (maze == null) {
				System.err.println("Waiting to get initialized...");
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Player player;

		synchronized (syncPlayers) {
			player = syncPlayers.get(id);
			if (player == null) {
				return new Reply(null, null, null, Status.INVALID_MOVE);
			}
		}
		synchronized (maze) {
			if (backupServer == null) {
				backupServer = findNextPeer(playerList);
				try {
					backupServer.initializeBackup(maze, playerList);
				} catch (RemoteException e) {
					System.err.println("No backup server...");
					System.exit(0);
				}
			}
			try {
				backupServer.movePlayer(player, d);
			} catch (RemoteException e) {
				backupServer = findNextPeer(playerList);
				if (backupServer == null) {
					System.err.println("No backup server...");
					System.exit(0);
				}
				try {
					backupServer.initializeBackup(maze, playerList);
					backupServer.movePlayer(player, d);
				} catch (RemoteException e1) {
					System.err.println("No backup server...");
					System.exit(0);
				}
			}
			return new Reply(maze, player, playerList, movePlayer(player, d));
		}
	}

	private static ServerApi findNextPeer(List<Player> playerList2) {
		for (int i = backupId + 1; i <= playerList2.size(); i++) {
			ServerApi s = getServer(null, i);
			if (s != null) {
				System.err.println("Switching to backup server " + i);
				backupId = i;
				return s;
			}
		}
		return null;
	}

	private static ServerApi getServer(String host, int id) {
		ServerApi server = null;
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			server = (ServerApi) registry.lookup(ServerApi.SERVER_REGISTRY_PREFIX + id);
			server.ping();
		} catch (Exception e) {
			System.err.println("Server " + id + " is down");
			e.printStackTrace();
		}
		return server;
	}
	
	@Override
	public Status movePlayer(Player p, Direction d) {
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
	public synchronized void initializeBackup(Location[][] maze, List<Player> players) {
		System.err.println("Initializing backup id:" + id);
		this.maze = maze;
		this.playerList = players;
		synchronized (syncPlayers) {
			for (Player p : playerList) {
				syncPlayers.put(p.getId(), p);
			}
		}
		this.notifyAll();
	}
}
