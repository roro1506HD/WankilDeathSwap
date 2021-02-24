package ovh.roro.wankil.deathswap.listener;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.GameState;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;

public class EntityListener implements Listener {

    private final GameManager gameManager;

    public EntityListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.gameManager.getState() != GameState.IN_GAME || event.getEntity() instanceof Player && this.gameManager.hasInvincibility()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            EnderPearl entity = (EnderPearl) event.getEntity();

            if (entity.getShooter() instanceof Player) {
                Player shooter = (Player) entity.getShooter();
                GamePlayer gamePlayer = this.gameManager.getPlayer(shooter);

                gamePlayer.saveEnderPearl(entity);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            EnderPearl entity = (EnderPearl) event.getEntity();

            if (entity.getShooter() instanceof Player) {
                Player shooter = (Player) entity.getShooter();
                GamePlayer gamePlayer = this.gameManager.getPlayer(shooter);

                gamePlayer.deleteEnderPearl(entity);
            }
        }
    }
}
