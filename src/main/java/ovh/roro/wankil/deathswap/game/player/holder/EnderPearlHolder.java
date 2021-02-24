package ovh.roro.wankil.deathswap.game.player.holder;

import org.bukkit.entity.EnderPearl;
import org.bukkit.util.Vector;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;

public class EnderPearlHolder {

    private final GamePlayer gamePlayer;
    private final EnderPearl enderPearl;

    private Vector relativePosition;

    public EnderPearlHolder(GamePlayer gamePlayer, EnderPearl enderPearl) {
        this.gamePlayer = gamePlayer;
        this.enderPearl = enderPearl;
    }

    public void saveRelativePosition() {
        this.relativePosition = this.enderPearl.getLocation().subtract(this.gamePlayer.getCraftPlayer().getLocation()).toVector();
    }

    public void restoreRelativePosition() {
        this.enderPearl.teleport(this.gamePlayer.getCraftPlayer().getLocation().add(this.relativePosition));
    }
}
