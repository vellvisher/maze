package common;

import common.Player;
import java.util.List;

public class Reply
{
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
        this.treasures;
        this.player = player;
        this.players = players;
        this.status = status;
    }
}
