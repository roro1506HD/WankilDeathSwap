package ovh.roro.wankil.deathswap.game.scoreboard.defaults;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;
import ovh.roro.wankil.deathswap.game.scoreboard.IScoreboard;

public class GameScoreboard implements IScoreboard {

    private static final DateTimeFormatter SWAP_FORMATTER = DateTimeFormatter.ofPattern("mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("mm:ss");

    private final GameManager gameManager;
    private final GamePlayer player;

    private int gameTimeIndex = -1;
    private int swapCountIndex = -1;
    private int swapTimeIndex = -1;
    private int enemyHealthIndex = -1;

    private GamePlayer enemyPlayer;
    private int enemyHealth;
    private int enemyAbsorption;

    public GameScoreboard(GameManager gameManager, GamePlayer player) {
        this.gameManager = gameManager;
        this.player = player;
    }

    @Override
    public void init() {
        int index = 0;

        this.player.getScoreboard().setLine(index++, "");

        if (this.gameTimeIndex == -1) {
            this.gameTimeIndex = index;
        }

        index = this.updateGameTime(index);

        if (this.swapCountIndex == -1) {
            this.swapCountIndex = index;
        }

        index = this.updateSwapCount(index);

        this.player.getScoreboard().setLine(index++, "");
        this.player.getScoreboard().setLine(index++, "Prochain swap :");

        if (this.swapTimeIndex == -1) {
            this.swapTimeIndex = index;
        }

        index = this.updateSwapTime(index);

        if (this.gameManager.getPlayers().size() == 2) {
            this.enemyPlayer = this.gameManager.getPlayers(gamePlayer -> !gamePlayer.isDead() && !gamePlayer.isSpectator() && !gamePlayer.getUniqueId().equals(this.player.getUniqueId())).get(0);

            this.player.getScoreboard().setLine(index++, "");
            this.player.getScoreboard().setLine(index++, "Vie de §e" + this.enemyPlayer.getName() + " §f:");

            if (this.enemyHealthIndex == -1) {
                this.enemyHealthIndex = index;
            }

            index = this.updateEnemyHealth(index);
        }

        this.player.getScoreboard().setLine(index++, "");
        this.player.getScoreboard().setLine(index, "§ewankil.fr");
    }

    @Override
    public void show() {
        this.player.getScoreboard().clearLines();
        this.enemyHealth = -1;
        this.init();
    }

    public void updateGameTime() {
        this.updateGameTime(this.gameTimeIndex);
    }

    private int updateGameTime(int index) {
        this.player.getScoreboard().setLine(index++, "Temps de jeu : §6" + TIME_FORMATTER.format(LocalTime.ofSecondOfDay(this.gameManager.getTimeElapsed())));
        return index;
    }

    public void updateSwapCount() {
        this.updateSwapCount(this.swapCountIndex);
    }

    private int updateSwapCount(int index) {
        this.player.getScoreboard().setLine(index++, "Swaps : §e" + this.gameManager.getSwapCount());
        return index;
    }

    public void updateSwapTime() {
        this.updateSwapTime(this.swapTimeIndex);
    }

    private int updateSwapTime(int index) {
        int nextSwap = this.gameManager.getNextSwap();

        if (nextSwap > this.gameManager.getConfig().getSwaps().getTimerReveal()) {
            this.player.getScoreboard().setLine(index++, "§d§k00:00");
        } else {
            this.player.getScoreboard().setLine(index++, "§d" + SWAP_FORMATTER.format(LocalTime.ofSecondOfDay(nextSwap)));
        }

        return index;
    }

    public void updateEnemyHealth() {
        this.updateEnemyHealth(this.enemyHealthIndex);
    }

    private int updateEnemyHealth(int index) {
        CraftPlayer player = this.enemyPlayer.getCraftPlayer();
        int health = (int) Math.ceil(player.getHealth());
        int absorptionHearts = (int) Math.ceil(player.getAbsorptionAmount() / 2.0D);

        if (this.enemyHealth == health && this.enemyAbsorption == absorptionHearts) {
            return ++index;
        }

        this.enemyHealth = health;
        this.enemyAbsorption = absorptionHearts;

        int fullHearts = (int) Math.floor(health / 2.0D);
        StringBuilder builder = new StringBuilder("§c❤❤❤❤❤❤❤❤❤❤");

        builder.insert(2 + fullHearts + (health - fullHearts * 2), "§8");
        builder.insert(2 + fullHearts, "§6");

        if (absorptionHearts > 0) {
            builder.append(" §e");

            for (int i = 0; i < absorptionHearts; i++) {
                builder.append("❤");
            }
        }

        this.player.getScoreboard().setLine(index++, builder.toString());
        return index;
    }
}
