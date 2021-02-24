package ovh.roro.wankil.deathswap.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ovh.roro.wankil.deathswap.DeathSwap;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.GameState;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;

public class PlayerListener implements Listener {

    private final DeathSwap plugin;
    private final GameManager gameManager;

    public PlayerListener(DeathSwap plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = this.gameManager.addPlayer(player);

        event.setJoinMessage(null);

        this.plugin.getLogger().info("Player " + gamePlayer.getName() + " has joined as " + (gamePlayer.isSpectator() ? "spectator" : "player"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(null);

        if (this.gameManager.removePlayer(player)) {
            event.setQuitMessage("§e" + player.getName() + " §7s'est déconnecté, et est par conséquent éliminé.");
            this.gameManager.checkWin();
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (this.gameManager.getState() != GameState.IN_GAME) {
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        GamePlayer gamePlayer = this.gameManager.getPlayer(event.getEntity());

        event.getDrops().clear();

        if (gamePlayer != null) {
            gamePlayer.onDeath();
        }
    }
}
