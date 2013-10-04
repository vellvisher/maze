package common;

import java.io.Serializable;

public class Player implements Serializable {
	private static final long serialVersionUID = 3897111149407010591L;
	private int id;
	private int x;
	private int y;
	private int treasures = 0;
	private int newTreasures = 0;
	private String host;

	public Player(int id) {
		this(id, 0, 0, null);
	}

	public Player(int id, int x, int y, String host) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.setHost(host);
	}

	public void addTreasures(int treasures) {
		this.newTreasures = treasures;
		this.treasures += treasures;
	}

	public int getTreasures() {
		return treasures;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getNewTreasures() {
		return newTreasures;
	}

	public int getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
