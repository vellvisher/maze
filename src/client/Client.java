package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import server.PrimaryServer;
import server.Server;

import common.Location;
import common.Player;
import common.PrimaryServerStub;
import common.Reply;
import common.Reply.Direction;
import common.Reply.Status;
import common.ServerApi;

public class Client {

	private static Scanner scanner;
	
	private static int N;
	private static int M;

	private static ServerApi server = null;
	private static int serverId = 1;
	private static List<Player> players;
	private static Player player = null;
	private static int logPlayers = 0;
	
	public static void main(String[] args) {

		String host = getServerHostname(args);
		boolean isPrimary = checkPrimaryArgument(args);

		if (isPrimary) {
			setNM(args);
			PrimaryServer server = new PrimaryServer(N, M);
			server.runServer();
		}

		scanner = new Scanner(System.in);
		Reply reply = null;
		PrimaryServerStub joinServer = null;
		Server peerServer = null;
		
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			joinServer = (PrimaryServerStub) registry.lookup(ServerApi.SERVER_REGISTRY_PREFIX + "1");
			reply = joinServer.joinGame();

			if (reply == null) {
				System.err.println("Null reply, exiting!");
				System.exit(0);
			}
			printStatus(reply.getStatus());
			
			if (reply.getStatus() == Status.JOIN_UNSUCCESSFUL) {
				System.exit(0);
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			System.exit(0);
		}

		player = reply.getPlayer();
		server = joinServer;
		players = reply.getPlayers();
		logPlayers = (int)(Math.log10(players.size())+1);
		
		if (!isPrimary) {
			N = reply.getMaze().length;
			peerServer = new Server(player.getId(), N);
			peerServer.runServer();
		}
		
		if (player.getId() == 2) {
			peerServer.initializeBackup(reply.getMaze(), players, 1);
		}

		printMaze(reply.getMaze());
		printTreasures(player);
		System.out.println("My id is " + player.getId());
		while (true) {
			playGame();
		}
	}
	
	private static void setNM(String[] args) {
		int i = 0;
		for (i = 0; i < args.length - 2; i++) {
			if ("-primary".equals(args[i])) {
				break;
			}
		}
		N = Integer.parseInt(args[i + 1]);
		M = Integer.parseInt(args[i + 2]);
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

	private static void playGame() {
		Direction direction = processInput(scanner.nextLine());
		if (direction == null) {
			System.out.println("Incorrect command");
		} else {
			Reply reply = playMove(direction);
			printMaze(reply.getMaze());
			printStatus(reply.getStatus());
		}
	}

	private static Reply playMove(Direction direction) {
		Reply reply = null;
		try {
			reply = server.move(player.getId(), direction);
		} catch (RemoteException e) {
			System.out.println("Could not contact server " + serverId + "...");
			server = findNextPeer();
			if (server == null) {
				System.out.println("Could not find replacement server...");
				System.out.println("Dying...");
				System.exit(0);
			}
			reply = playMove(direction);
		}
		return reply;
	}

	private static ServerApi findNextPeer() {
		for (int i = serverId + 1; i <= players.size(); i++) {
			ServerApi s = getServer(players.get(i).getHost(), i);
			if (s != null) {
				System.out.println("Switching to server " + i);
				serverId = i;
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
		}
		return server;
	}

	private static void printTreasures(Player player) {
		System.out.println("You currently have " + player.getTreasures()
				+ " treasures");
		System.out.println("You collected " + player.getNewTreasures()
				+ " new treasures.");
	}

	private static void printStatus(Status status) {
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
			StringBuilder output = new StringBuilder();
			for (int j = 0; j < maze.length; j++) {
				output.append("| T" + maze[i][j].getTreasures() + " ");
				if (maze[i][j].getPlayer() != null) {
					output.append("P" + maze[i][j].getPlayer().getId());
				} else {
					for (int k = 1; k <= logPlayers + 1; k++) {
						output.append("X");
					}
				}
				output.append(" ");
			}
			output.append("|");
			if (i == 0) {
				for (int j = 0; j < output.length(); j++) {
					System.out.print("-");
				}				
				System.out.println();
			}
			System.out.println(output);
			for (int j = 0; j < output.length(); j++) {
				System.out.print("-");
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
			scanner.close();
			System.exit(0);
		default:
			return null;
		}
	}
}
