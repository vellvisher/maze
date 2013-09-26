package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import common.Location;
import common.Player;
import common.Reply;
import common.Reply.Direction;
import common.Reply.Status;
import common.ServerApi;

public class Server implements ServerApi {
	private static final int DEFAULT_PORT = 0;
	private static final int WAITING_PERIOD = 20000;

	private static int N;
	private static int M;

	private Location[][] maze;
	private Map<Integer, Player> syncPlayers = Collections
			.synchronizedMap(new HashMap<Integer, Player>());
	private AtomicInteger nextPlayerId = new AtomicInteger(1);
	private AtomicBoolean joinEnd = new AtomicBoolean(false);
	private boolean firstPlayer = true;
	private ArrayList<Player> playerList;

	public Server() {
		maze = new Location[N][N];
		initializeMaze();
	}

	public String sayHello() {
		return "Hello, world!";
	}

	public Reply joinGame() {
		if (joinEnd.get() || nextPlayerId.get() > N*N) {
			return new Reply(null, null, null, Status.JOIN_UNSUCCESSFUL);
		}

		Player player = new Player(nextPlayerId.getAndIncrement());
		synchronized (syncPlayers) {
			syncPlayers.put(player.getId(), player);
		}
		synchronized (this) {
			if (firstPlayer) {
				firstPlayer = false;
				try {
					Thread.sleep(WAITING_PERIOD);
				} catch (InterruptedException ignored) {
					ignored.printStackTrace();
				}
				joinEnd.set(true);

				synchronized (syncPlayers) {
					for (Player p : syncPlayers.values()) {
						Random random = new Random();
						int pX, pY;

						do {
							pX = random.nextInt(N);
							pY = random.nextInt(N);

							if (maze[pX][pY].getPlayer() == null) {
								maze[pX][pY].setPlayer(p);
								break;
							}
						} while (true);
						updatePlayerPosition(p, pX, pY);
					}
					playerList = new ArrayList<Player>(syncPlayers.values());
				}
			}
		}
		return new Reply(maze, player, playerList, Status.JOIN_SUCCESSFUL);
	}

	private void updatePlayerPosition(Player p, int pX, int pY) {
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
		synchronized(syncPlayers) {
			player = syncPlayers.get(id);
			if (player == null) {
				return new Reply(null, null, null, Status.INVALID_MOVE);
			}
		}
		synchronized(maze) {
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
	
	private void initializeMaze() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				maze[i][j] = new Location();
			}
		}
		Random random = new Random();
		for (int i = 1; i <= M; i++) {
			int mazeX = random.nextInt(N);
			int mazeY = random.nextInt(N);
			maze[mazeX][mazeY].setTreasures(maze[mazeX][mazeY].getTreasures() + 1);
		}
	}

	public static void main(String args[]) {
		ServerApi stub = null;
		Registry registry = null;

		if (args.length < 2) {
			System.err.println("Enter N and M");
			System.exit(0);
		}

		N = Integer.parseInt(args[0]);
		M = Integer.parseInt(args[1]);

		if (N < 1 || M < 1) {
			System.err.println("Enter positive values for N and M");
			System.exit(0);
		}
		
		try {
			Server obj = new Server();
			stub = (ServerApi) UnicastRemoteObject.exportObject(obj,
					DEFAULT_PORT);
			registry = LocateRegistry.getRegistry();
			registry.bind("ServerApi", stub);

			System.err.println("Server ready");
		} catch (Exception e) {
			try {
				registry.unbind("ServerApi");
				registry.bind("ServerApi", stub);
				System.err.println("Server ready");
			} catch (Exception ee) {
				System.err.println("Server exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}
}
