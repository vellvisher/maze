package common;

import java.io.Serializable;
import java.util.Set;

public class Reply implements Serializable {
	private static final long serialVersionUID = 175422823511136942L;
	private Location[][] maze;
	private Player player;
	private Set<Player> players;
	private Status status;

	public enum Status {
		MOVE_SUCCESSFUL, PLAYER_BLOCKING, OUT_OF_BOUNDS, JOIN_SUCCESSFUL, JOIN_UNSUCCESSFUL, INVALID_MOVE
	}

	public enum Direction {
		N, S, W, E, NoMove
	}

	public Reply(Location[][] maze, Player player, Set<Player> players,
			Status status) {
		this.maze = maze;
		this.player = player;
		this.players = players;
		this.status = status;
	}

	public Location[][] getMaze() {
		return maze;
	}

	public void setMaze(Location[][] maze) {
		this.maze = maze;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public void setPlayers(Set<Player> players) {
		this.players = players;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
