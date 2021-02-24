package ovh.roro.wankil.deathswap.game.scoreboard.defaults;

import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;
import ovh.roro.wankil.deathswap.game.scoreboard.IScoreboard;

public class WaitingScoreboard implements IScoreboard {

    private final GameManager gameManager;
    private final GamePlayer player;

    public WaitingScoreboard(GameManager gameManager, GamePlayer player) {
        this.gameManager = gameManager;
        this.player = player;
    }

    @Override
    public void init() {
        int index = 0;

        this.player.getScoreboard().setLine(index++, "§a");
        this.player.getScoreboard().setLine(index++, "Mode de jeu créé");
        this.player.getScoreboard().setLine(index++, "par §eSethBling §f et");
        this.player.getScoreboard().setLine(index++, "développé par");
        this.player.getScoreboard().setLine(index++, "§broro1506_HD");
        this.player.getScoreboard().setLine(index, "§a");
    }

    @Override
    public void show() {
        this.player.getScoreboard().clearLines();
        this.init();
    }
}
