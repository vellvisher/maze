package common;

import java.io.Serializable;

public class Location implements Serializable {
	private static final long serialVersionUID = -4315375968577577953L;
	private Player player;
	private int treasures;

	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public int getTreasures() {
		return treasures;
	}
	public void setTreasures(int treasures) {
		this.treasures = treasures;
	}
}
