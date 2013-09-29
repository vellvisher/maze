package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import common.Location;
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

		String host = (args.length < 1) ? null : args[0];
		scanner = new Scanner(System.in);

		try {
			Registry registry = LocateRegistry.getRegistry(host);
			ServerApi stub = (ServerApi) registry.lookup("ServerApi");
			Reply reply = stub.joinGame();
			printStatus(reply.getPlayer(), reply.getStatus());
			if (reply.getStatus() == Status.JOIN_UNSUCCESSFUL) {
				System.exit(0);
			}
			printMaze(reply.getMaze());
			printTreasures(reply.getPlayer());
			System.out.println("My id is " + reply.getPlayer().getId());
			while (true) {
				reply = playGame(stub, reply);
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		} finally {
			scanner.close();
		}
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
		System.out.println("You currently have " + player.getTreasures() + " treasures");
		System.out.println("You collected " + player.getNewTreasures() + " new treasures.");
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
		switch(input) {
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
