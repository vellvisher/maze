package common;

import common.Player;
import java.util.List;

public class Reply {
	private int[][] treasures;
	private Player player;
	private List<Player> players;
	private Status status;

	public enum Status {
		MOVE_SUCCESSFUL, PLAYER_BLOCKING, OUT_OF_BOUNDS, JOIN_SUCCESSFUL, JOIN_UNSUCCESSFUL
	};

	public enum Direction {
		N, S, W, E, NoMove;
	}

	public Reply(int[][] treasures, Player player, List<Player> players,
			Status status) {
		this.treasures = treasures;
		this.player = player;
		this.players = players;
		this.status = status;
	}

	public int[][] getTreasures() {
		return treasures;
	}

	public void setTreasures(int[][] treasures) {
		this.treasures = treasures;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
