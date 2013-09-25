package common;

public class Player
{
    private int id;
    private int x;
    private int y;
    private int treasures = 0;
    private int newTreasures = 0;

    public Player(int id) {
        this(id, 0, 0);
    }

    public Player(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
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
}
