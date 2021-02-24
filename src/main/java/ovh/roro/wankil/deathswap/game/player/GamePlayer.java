package ovh.roro.wankil.deathswap.game.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ovh.roro.wankil.deathswap.DeathSwap;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.GameState;
import ovh.roro.wankil.deathswap.game.player.holder.EnderPearlHolder;
import ovh.roro.wankil.deathswap.game.scoreboard.defaults.GameScoreboard;
import ovh.roro.wankil.deathswap.game.scoreboard.defaults.WaitingScoreboard;
import ovh.roro.wankil.deathswap.util.ScoreboardSign;

public class GamePlayer {

    private final DeathSwap plugin;
    private final GameManager gameManager;

    private final UUID uuid;
    private final String name;
    private final CraftPlayer craftPlayer;

    private final ScoreboardSign scoreboard;
    private final WaitingScoreboard waitingScoreboard;
    private final GameScoreboard gameScoreboard;

    private final Int2ObjectMap<EnderPearlHolder> pearlHolders;

    private boolean isSpectator;
    private boolean isDead;

    public GamePlayer(DeathSwap plugin, GameManager gameManager, Player player) {
        this.plugin = plugin;
        this.gameManager = gameManager;

        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.craftPlayer = (CraftPlayer) player;

        this.scoreboard = new ScoreboardSign(this, "temporary name");

        this.pearlHolders = new Int2ObjectArrayMap<>();

        this.waitingScoreboard = new WaitingScoreboard(gameManager, this);
        this.gameScoreboard = new GameScoreboard(gameManager, this);
    }

    public void initialize(boolean lobbyStuff) {
        this.craftPlayer.getInventory().clear();
        this.craftPlayer.setMaxHealth(20.0D);
        this.craftPlayer.setHealth(20.0D);
        this.craftPlayer.setFoodLevel(20);
        this.craftPlayer.setSaturation(20.0F);
        this.craftPlayer.setExhaustion(20.0F);
        this.craftPlayer.setWalkSpeed(0.2F);
        this.craftPlayer.setLevel(0);
        this.craftPlayer.setExp(0.0F);
        this.craftPlayer.setAllowFlight(false);
        this.craftPlayer.setFlying(false);

        for (PotionEffect effect : this.craftPlayer.getActivePotionEffects()) {
            this.craftPlayer.removePotionEffect(effect.getType());
        }

        if (lobbyStuff) {
            this.waitingScoreboard.show();

            this.craftPlayer.setGameMode(GameMode.ADVENTURE);
            this.craftPlayer.teleport(this.plugin.getWorldManager().getLobbyLocation());
        }
    }

    public void initScoreboard(String title) {
        this.scoreboard.setObjectiveName(title);
        this.scoreboard.create();
    }

    public void reset() {
        this.isDead = false;

        this.craftPlayer.getHandle().setArrowCount(0, true);

        this.initialize(true);
    }

    public void onDeath() {
        if (this.gameManager.getState() != GameState.IN_GAME) {
            return;
        }

        this.craftPlayer.setGameMode(GameMode.SPECTATOR);
        this.craftPlayer.setHealth(this.craftPlayer.getMaxHealth());

        this.isDead = true;
        this.gameManager.checkWin();
    }

    public void saveEnderPearl(EnderPearl enderPearl) {
        this.pearlHolders.put(enderPearl.getEntityId(), new EnderPearlHolder(this, enderPearl));
    }

    public void performToEnderPearl(Consumer<EnderPearlHolder> consumer) {
        this.pearlHolders.values().forEach(consumer);
    }

    public void deleteEnderPearl(EnderPearl enderPearl) {
        this.pearlHolders.remove(enderPearl.getEntityId());
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        PlayerConnection playerConnection = this.craftPlayer.getHandle().playerConnection;

        if (title == null && subtitle == null) {
            playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
            return;
        }

        playerConnection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
        playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle)));
        playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title)));
    }

    public void sendActionBar(String message) {
        this.craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, new ChatComponentText(message)));
    }

    public void sendMessage(String message) {
        this.craftPlayer.sendMessage(message);
    }

    public void sendPacket(Packet<?> packet) {
        this.craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    public void sendPacket(Packet<?> packet, Packet<?>... packets) {
        PlayerConnection playerConnection = this.craftPlayer.getHandle().playerConnection;

        playerConnection.sendPacket(packet);

        for (Packet<?> tempPacket : packets) {
            playerConnection.sendPacket(tempPacket);
        }
    }

    public boolean isSpectator() {
        return this.isSpectator;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public void setDead(boolean dead) {
        this.isDead = dead;
    }

    public ScoreboardSign getScoreboard() {
        return this.scoreboard;
    }

    public WaitingScoreboard getWaitingScoreboard() {
        return this.waitingScoreboard;
    }

    public GameScoreboard getGameScoreboard() {
        return this.gameScoreboard;
    }

    public CraftPlayer getCraftPlayer() {
        return this.craftPlayer;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }
}
