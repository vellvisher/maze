package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Scanner;

import server.Server;
import common.Location;
import common.PeerServerApi;
import common.Player;
import common.Reply;
import common.Reply.Direction;
import common.Reply.Status;
import common.ServerApi;

public class Client {

	private static Scanner scanner;

	private Client() {
	}

	public static void main(String[] args) {

		String host = getServerHostname(args);
		boolean isPrimary = checkPrimaryArgument(args);

		if (isPrimary) {
			Server.main(getNM(args));
		}

		scanner = new Scanner(System.in);
		Reply reply = null;
		ServerApi server = null;
		Player player = null;

		try {
			Registry registry = LocateRegistry.getRegistry(host);
			server = (ServerApi) registry.lookup(ServerApi.SERVER_REGISTRY);
			reply = server.joinGame();

			if (reply == null) {
				System.err.println("Null reply, exiting!");
				System.exit(0);
			}

			player = reply.getPlayer();

			printStatus(player, reply.getStatus());

			if (reply.getStatus() == Status.JOIN_UNSUCCESSFUL) {
				System.exit(0);
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
			System.exit(0);
		}

		Iterator<Player> players = reply.getPlayers().iterator();
		// PeerServerApi backupServer = getServer(players.next());

		try {
			printMaze(reply.getMaze());
			printTreasures(player);
			System.out.println("My id is " + player.getId());
			while (true) {
				reply = playGame(server, reply);
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}

	private static String[] getNM(String[] args) {
		int i = 0;
		for (i = 0; i < args.length - 2; i++) {
			if ("-primary".equals(args[i])) {
				break;
			}
		}
		return new String[] { args[i + 1], args[i + 2] };
	}

	private static String getServerHostname(String[] args) {
		for (int i = 0; i < args.length - 1; i++) {
			if ("-host".equalsIgnoreCase(args[i])) {
				return args[i + 1];
			}
		}
		return null;
	}

	private static boolean checkPrimaryArgument(String[] args) {
		for (String s : args) {
			if ("-primary".equals(s)) {
				return true;
			}
		}
		return false;
	}

	private static Reply playGame(ServerApi stub, Reply reply)
			throws RemoteException {
		Direction direction = processInput(scanner.nextLine());
		if (direction == null) {
			System.out.println("Incorrect command");
		} else {
			reply = stub.move(reply.getPlayer().getId(), direction);
			printMaze(reply.getMaze());
			printStatus(reply.getPlayer(), reply.getStatus());
		}
		return reply;
	}

	private static void printTreasures(Player player) {
		System.out.println("You currently have " + player.getTreasures()
				+ " treasures");
		System.out.println("You collected " + player.getNewTreasures()
				+ " new treasures.");
	}

	private static void printStatus(Player player, Status status) {
		switch (status) {
		case JOIN_SUCCESSFUL:
			System.out.println("Joined");
			break;
		case JOIN_UNSUCCESSFUL:
			System.out.println("Unable to join");
			break;
		case MOVE_SUCCESSFUL:
			printTreasures(player);
			break;
		case OUT_OF_BOUNDS:
			System.out.println("Co-ordinates out of bounds");
			break;
		case PLAYER_BLOCKING:
			System.out.println("Player is blocking grid");
			break;
		case INVALID_MOVE:
			System.out.println("Invalid move!");
			break;
		default:
			break;
		}
	}

	private static void printMaze(Location[][] maze) {
		for (int i = 0; i < maze.length; i++) {
			for (int j = 0; j < maze.length; j++) {
				System.out.print(maze[i][j].getTreasures() + "p");
				if (maze[i][j].getPlayer() != null) {
					System.out.print(maze[i][j].getPlayer().getId());
				} else {
					System.out.print("0");
				}
				System.out.print("  ");
			}
			System.out.println();
		}
	}

	private static Direction processInput(String input) {
		input = input.trim();
		switch (input) {
		case "N":
			return Direction.N;
		case "S":
			return Direction.S;
		case "W":
			return Direction.W;
		case "E":
			return Direction.E;
		case "NoMove":
			return Direction.NoMove;
		case "exit":
			System.exit(0);
		default:
			return null;
		}
	}
}
