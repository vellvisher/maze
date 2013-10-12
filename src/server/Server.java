package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Location;
import common.Player;
import common.Reply;
import common.Reply.Direction;
import common.Reply.Status;
import common.ServerApi;
import common.Timestamp;

public class Server extends RemoteServer implements ServerApi {
	private static final long serialVersionUID = 3126187241824135686L;

	protected final int DEFAULT_PORT = 0;
	
	protected int id;
	protected int backupId;
	protected int N;

	protected Location[][] maze;
	protected List<Player> playerList;
	protected ServerApi backupServer;

	private Thread pingThread;

	private Map<Timestamp, Reply> movesPlayed = new HashMap<Timestamp, Reply>();

	private ServerApi mainServer;

	class PingManager implements Runnable {
		private static final long PING_FREQUENCY = 15000;
		private ServerApi pingServer;
		public PingManager(ServerApi pingServer) {
			this.pingServer = pingServer;
		}
		@Override
		public void run() {
			while (pingServer(pingServer)) {
				try {
					Thread.sleep(PING_FREQUENCY);
				} catch (InterruptedException e) {
					return;
				}
			}
			if (Thread.interrupted()) {
				return;
			}
			pingThreadInitializeRemoteBackup(pingServer);
		}
	};
	
	public Server(int id, int n) {
		this.id = id;
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
	public Reply move(int id, Direction d, Timestamp timestamp, boolean backupMove) {
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

		player = playerList.get(id - 1);
		if (player == null) {
			return new Reply(null, null, null, Status.INVALID_MOVE);
		}

		Reply reply;

		synchronized (this) {
			reply = movePlayer(player, d, timestamp);
			if (reply.getStatus() != Status.MOVE_SUCCESSFUL) {
				return reply;
			}
			if (backupMove == true) {
				return reply;
			}
			try {
				backupServer.move(id, d, timestamp, true);
			} catch (Exception e) {
				initializeRemoteBackup();
			}
		}
		return reply;
	}

	private synchronized void pingThreadInitializeRemoteBackup(ServerApi pingServer) {
		if (backupServer != pingServer && mainServer != pingServer) {
			return;
		}
		initializeRemoteBackup();
	}
	
	private synchronized void initializeRemoteBackup() {
		backupServer = findNextPeer();
		try {
			if (backupServer == null) {
				throw new RemoteException();
			}
			backupServer.initializeBackup(maze, playerList, id, movesPlayed);
			if (pingThread != null) {
				pingThread.interrupt();
			}
			pingThread = new Thread(new PingManager(backupServer));
			pingThread.start();
		} catch (RemoteException e) {
			System.err.println("No backup server...");
			System.exit(0);
		}
	}

	private ServerApi findNextPeer() {
		for (int i = backupId + 1; i <= playerList.size(); i++) {
			ServerApi s = getServer(null, i);
			if (s != null) {
				System.err.println("Switching to backup server " + i);
				backupId = i;
				return s;
			}
		}
		return null;
	}

	private ServerApi getServer(String host, int id) {
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

	public synchronized Reply movePlayer(Player p, Direction d, Timestamp t) {
		if (movesPlayed.containsKey(t)) {
			return movesPlayed.get(t);
		}
		Reply reply = new Reply(maze, p, playerList, moveWithStatus(p, d));
		movesPlayed.put(t, reply);
		return reply;
	}
	
	private Status moveWithStatus(Player p, Direction d) {
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
				if (! (e instanceof AlreadyBoundException)) {
					e.printStackTrace();
				}
				registry.unbind(ServerApi.SERVER_REGISTRY_PREFIX + id);
				registry.bind(ServerApi.SERVER_REGISTRY_PREFIX + id, stub);
				System.err.println("Server ready");
			} catch (Exception ee) {
				System.err.println("Server exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}

	private boolean pingServer(ServerApi server) {
		try {
			server.ping();
			return true;
		} catch (RemoteException e) {
			return false;
		}
	}
	
	@Override
	public void ping() throws RemoteException {
	}

	@Override
	public synchronized void initializeBackup(Location[][] maze, List<Player> players, int mainServerId, Map<Timestamp, Reply> movesPlayed) {
		System.err.println("Initializing backup id:" + id);
		mainServer = getServer(players.get(mainServerId).getHost(), mainServerId);
		this.maze = maze;
		this.playerList = players;
		this.movesPlayed = movesPlayed;
		if (this.movesPlayed == null) {
			this.movesPlayed = new HashMap<Timestamp, Reply>();
		}
		this.notifyAll();
		pingThread = new Thread(new PingManager(mainServer));
		pingThread.start();
	}
}
