package server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.Reply;
import common.Reply.Direction;
import common.Player;
import common.Reply.Status;
import common.ServerApi;

public class Server implements ServerApi {
	private static final int DEFAULT_PORT = 0;
	private static final int WAITING_PERIOD = 20000;

	private static int N;
	private static int M;

	private Player[][] maze;
	private int[][] treasures;
	private List<Player> syncPlayers = Collections
			.synchronizedList(new ArrayList<Player>());
	private AtomicInteger nextPlayerId = new AtomicInteger(1);
	private AtomicBoolean joinEnd = new AtomicBoolean(false);
	private boolean firstPlayer = true;

	public Server() {
		maze = new Player[N][N];
		treasures = new int[N][N];
		initializeMaze();
	}

	public String sayHello() {
		return "Hello, world!";
	}

	public Reply joinGame() {
		if (joinEnd.get()) {
			return new Reply(null, null, null, Status.JOIN_UNSUCCESSFUL);
		}

		Player player = new Player(nextPlayerId.getAndIncrement());
		synchronized (syncPlayers) {
			syncPlayers.add(player);
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
					for (Player p : syncPlayers) {
						Random random = new Random();
						int pX, pY;

						do {
							pX = random.nextInt(N);
							pY = random.nextInt(N);

							if (maze[pX][pY] == null) {
								maze[pX][pY] = p;
								break;
							}
						} while (true);

						p.setX(pX);
						p.setY(pY);
						p.addTreasures(treasures[pX][pY]);
						treasures[pX][pY] = 0;
					}
				}
			}
		}

		return new Reply(treasures, player, syncPlayers, Status.JOIN_SUCCESSFUL);
	}

	@Override
	public Reply move(int id, Direction d) {
		System.out.println(id + ":" + d);
		return null;
	}

	private void initializeMaze() {
		Random random = new Random();
		for (int i = 1; i <= M; i++) {
			int mazeX = random.nextInt(N);
			int mazeY = random.nextInt(N);
			treasures[mazeX][mazeY]++;
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

		try {
			Server obj = new Server();
			stub = (ServerApi) UnicastRemoteObject.exportObject(obj,
					DEFAULT_PORT);
			registry = LocateRegistry.getRegistry();
			registry.bind("ServerApi", stub);

			System.err.println("Server ready");
		} catch (Exception e) {
			try {
				registry.unbind("Hello");
				registry.bind("Hello", stub);
				System.err.println("Server ready");
			} catch (Exception ee) {
				System.err.println("Server exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}
}
