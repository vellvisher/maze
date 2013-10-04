package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import common.Location;
import common.Player;
import common.PrimaryServerStub;
import common.Reply;
import common.ServerApi;
import common.Reply.Status;

public class PrimaryServer extends Server implements PrimaryServerStub {
	private static final int WAITING_PERIOD = 20000;

	private AtomicInteger nextPlayerId = new AtomicInteger(1);
	private AtomicBoolean joinEnd = new AtomicBoolean(false);
	private boolean firstPlayer = true;
	
	private static int M;
	
	public PrimaryServer(int n, int m) {
		super(1, n);
		M = m;
		initializeMaze();
	}

	public Reply joinGame() {
		if (joinEnd.get() || nextPlayerId.get() > N * N) {
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
					playerList = new TreeSet<Player>(syncPlayers.values());
				}
			}
		}
		if (player.getId() == 2) {
			// Init backup
		}
		return new Reply(maze, player, playerList, Status.JOIN_SUCCESSFUL);
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
			maze[mazeX][mazeY]
					.setTreasures(maze[mazeX][mazeY].getTreasures() + 1);
		}
	}
	
	@Override
	public void runServer() {
		PrimaryServerStub stub = null;
		Registry registry = null;

		try {
			stub = (PrimaryServerStub) UnicastRemoteObject.exportObject(this,
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
}