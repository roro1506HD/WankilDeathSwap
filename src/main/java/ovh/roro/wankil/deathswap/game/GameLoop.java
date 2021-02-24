package ovh.roro.wankil.deathswap.game;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import ovh.roro.wankil.deathswap.DeathSwap;
import ovh.roro.wankil.deathswap.game.scoreboard.defaults.GameScoreboard;

class GameLoop implements Runnable {

    private final DeathSwap plugin;
    private final GameManager gameManager;

    private int taskId;

    GameLoop(DeathSwap plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    void startTask() {
        this.taskId = this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this, 20L, 20L).getTaskId();
    }

    @Override
    public void run() {
        this.gameManager.increaseTimeElapsed();
        int nextSwap = this.gameManager.getNextSwap() - 1;
        boolean swap = this.gameManager.decrementNextSwap();
        int timerReveal = this.gameManager.getConfig().getSwaps().getTimerReveal();
        boolean updateSwapTime = nextSwap <= timerReveal;

        this.gameManager.forAllPlayers(gamePlayer -> {
            GameScoreboard scoreboard = gamePlayer.getGameScoreboard();
            CraftPlayer player = gamePlayer.getCraftPlayer();

            scoreboard.updateGameTime();
            scoreboard.updateEnemyHealth();

            if (nextSwap == timerReveal) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 10.0F, 0.1F);
                this.gameManager.sendTitle(this.gameManager.getAllPlayers(), "", "§6" + nextSwap, 0, 10, 10);
            } else if (nextSwap <= Math.min(5, timerReveal) && !swap) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 10.0F, 2.0F);
                this.gameManager.sendTitle(this.gameManager.getAllPlayers(), "", "§c" + nextSwap, 0, 10, 10);
            }

            if (updateSwapTime) {
                scoreboard.updateSwapTime();
            }

            if (swap) {
                scoreboard.updateSwapCount();
            }
        });
    }

    int getTaskId() {
        return this.taskId;
    }
}
