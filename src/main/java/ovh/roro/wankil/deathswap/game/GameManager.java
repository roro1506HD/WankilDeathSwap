package ovh.roro.wankil.deathswap.game;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ovh.roro.wankil.deathswap.DeathSwap;
import ovh.roro.wankil.deathswap.game.json.JsonConfig;
import ovh.roro.wankil.deathswap.game.json.JsonSwapStep;
import ovh.roro.wankil.deathswap.game.json.JsonSwaps;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;
import ovh.roro.wankil.deathswap.game.player.holder.EnderPearlHolder;
import ovh.roro.wankil.deathswap.game.teleport.TeleportRunnable;
import ovh.roro.wankil.deathswap.util.ScoreboardTitleAnimator;

public class GameManager {

    private final DeathSwap plugin;

    private final JsonConfig config;

    private final Map<UUID, GamePlayer> playersByUuid;
    private final Random random;
    private final ScoreboardTitleAnimator titleAnimator;

    private final GameLoop gameLoop;

    private GameState state;
    private int timeElapsed;
    private int nextSwap;
    private int swapCount;

    private boolean invincibility;

    public GameManager(DeathSwap plugin, JsonConfig config) {
        this.plugin = plugin;

        this.config = config;

        this.playersByUuid = new HashMap<>();
        this.random = new SecureRandom();
        this.titleAnimator = new ScoreboardTitleAnimator("WANKIL DEATH SWAP");

        this.gameLoop = new GameLoop(plugin, this);

        this.state = GameState.WAITING;
        this.timeElapsed = 0;
        this.nextSwap = 0;
        this.swapCount = 0;

        this.invincibility = true;

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            boolean changed = this.titleAnimator.next();

            if (!changed) {
                return;
            }

            String title = this.titleAnimator.getCurrentTitle();

            for (GamePlayer player : this.getAllPlayers()) {
                player.getScoreboard().setObjectiveName(title);
            }
        }, 2L, 2L);
    }

    public void startGame() {
        this.state = GameState.TELEPORTING;

        List<GamePlayer> players = this.getPlayers();
        new TeleportRunnable(this.plugin, this, players, () -> {
            this.setNextSwap();

            for (GamePlayer player : players) {
                player.initialize(false);
                player.setDead(false);
                player.getGameScoreboard().show();

                player.getCraftPlayer().setGameMode(GameMode.SURVIVAL);

                player.getCraftPlayer().getInventory().setContents(this.config.getStarter().getInventory());
            }

            this.state = GameState.IN_GAME;

            this.plugin.getWorldManager().getGameWorld().setTime(0L);
            this.gameLoop.startTask();

            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                this.invincibility = false;

                this.plugin.getServer().broadcastMessage("§aLa période d'invincibilité est terminée !");
            }, 600L);
        });
    }

    public void checkWin() {
        if (this.getPlayers().size() <= 1) {
            String winner = this.getPlayers().get(0).getName();

            this.plugin.getServer().broadcastMessage("§d" + winner + " §fa gagné la partie !");
            this.sendTitle(this.getAllPlayers(), "§6§l< GAGNANT >", "§e" + winner, 5, 65, 10);

            this.finishGame();
        }
    }

    public void finishGame() {
        this.state = GameState.FINISHED;
        this.invincibility = true;

        this.plugin.getServer().getScheduler().cancelTask(this.gameLoop.getTaskId());

        for (GamePlayer player : this.getAllPlayers()) {
            player.getCraftPlayer().setGameMode(GameMode.SPECTATOR);
        }

        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Consumer<BukkitTask>() {
            private int timer;

            @Override
            public void accept(BukkitTask bukkitTask) {
                if (++this.timer >= 10) {
                    GameManager.this.resetGame();
                    bukkitTask.cancel();
                }
            }
        }, 20, 20);
    }

    private void resetGame() {
        this.state = GameState.WAITING;
        this.timeElapsed = 0;
        this.nextSwap = 0;
        this.swapCount = 0;

        for (GamePlayer player : this.getAllPlayers()) {
            player.reset();
            player.getWaitingScoreboard().show();
        }
    }

    public int getTimeElapsed() {
        return this.timeElapsed;
    }

    void increaseTimeElapsed() {
        ++this.timeElapsed;
    }

    public int getNextSwap() {
        return this.nextSwap;
    }

    void setNextSwap() {
        JsonSwaps config = this.config.getSwaps();
        int stepIndex = Math.min(config.getSteps().length - 1, this.swapCount);
        JsonSwapStep step = config.getSteps()[stepIndex];

        int min = step.getMin();
        int max = step.getMax();

        if (min == max) {
            this.nextSwap = min;
        } else {
            this.nextSwap = min + this.random.nextInt(max - min);
        }
    }

    boolean decrementNextSwap() {
        boolean swap = --this.nextSwap == 0;

        if (swap) {
            this.swapCount++;
            this.setNextSwap();

            List<GamePlayer> players = this.getPlayers();

            Collections.shuffle(players, this.random);

            Queue<GamePlayer> queuedPlayed = new ArrayDeque<>(players);

            GamePlayer firstPlayer = null;
            Player lastPlayer = null;
            Location lastLocation = null;

            do {
                GamePlayer player = queuedPlayed.remove();

                if (firstPlayer == null) {
                    firstPlayer = player;
                }

                Location location = player.getCraftPlayer().getLocation();
                if (lastPlayer != null) {
                    this.plugin.getLogger().info("Teleporting " + player.getName() + " to " + lastLocation);

                    player.performToEnderPearl(EnderPearlHolder::saveRelativePosition);

                    player.getCraftPlayer().teleport(lastLocation);
                    player.getCraftPlayer().playSound(lastLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 10.0F, 1.0F);

                    player.performToEnderPearl(EnderPearlHolder::restoreRelativePosition);
                }

                lastPlayer = player.getCraftPlayer();
                lastLocation = location;
            } while (!queuedPlayed.isEmpty());

            this.plugin.getLogger().info("Teleporting last " + firstPlayer.getName() + " to " + lastLocation);

            firstPlayer.performToEnderPearl(EnderPearlHolder::saveRelativePosition);

            firstPlayer.getCraftPlayer().teleport(lastLocation);
            firstPlayer.getCraftPlayer().playSound(lastLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 10.0F, 1.0F);

            firstPlayer.performToEnderPearl(EnderPearlHolder::restoreRelativePosition);
        }

        return swap;
    }

    public int getSwapCount() {
        return this.swapCount;
    }

    public boolean hasInvincibility() {
        return this.invincibility;
    }

    public GamePlayer addPlayer(Player player) {
        GamePlayer gamePlayer = this.playersByUuid.computeIfAbsent(player.getUniqueId(), uuid -> new GamePlayer(this.plugin, this, player));

        gamePlayer.initScoreboard(this.titleAnimator.getCurrentTitle());
        gamePlayer.initialize(true);

        return gamePlayer;
    }

    public boolean removePlayer(Player player) {
        GamePlayer gamePlayer = this.playersByUuid.remove(player.getUniqueId());

        if (gamePlayer == null) {
            return false;
        }

        return this.state == GameState.IN_GAME || this.state == GameState.TELEPORTING;
    }

    public void sendActionbar(Collection<GamePlayer> players, String message) {
        PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, new ChatComponentText(message));

        for (GamePlayer player : players) {
            player.sendPacket(packet);
        }
    }

    public void sendTitle(Collection<GamePlayer> players, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null && subtitle == null) {
            PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.CLEAR, null);

            for (GamePlayer player : players) {
                player.sendPacket(packet);
            }
            return;
        }

        PacketPlayOutTitle timesPacket = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle));
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, new ChatComponentText(title));

        for (GamePlayer player : players) {
            player.sendPacket(timesPacket, subtitlePacket, titlePacket);
        }
    }

    public Random getRandom() {
        return this.random;
    }

    public GameState getState() {
        return this.state;
    }

    public List<GamePlayer> getPlayers() {
        List<GamePlayer> players = new ArrayList<>();

        for (GamePlayer player : this.playersByUuid.values()) {
            if (!player.isSpectator() && !player.isDead()) {
                players.add(player);
            }
        }

        return players;
    }

    public List<GamePlayer> getPlayers(Predicate<GamePlayer> predicate) {
        List<GamePlayer> players = new ArrayList<>();

        for (GamePlayer player : this.playersByUuid.values()) {
            if (predicate.test(player)) {
                players.add(player);
            }
        }

        return players;
    }

    public List<GamePlayer> getAllPlayers() {
        return new ArrayList<>(this.playersByUuid.values());
    }

    public void forPlayers(Consumer<GamePlayer> consumer) {
        this.getPlayers().listIterator().forEachRemaining(consumer);
    }

    public void forAllPlayers(Consumer<GamePlayer> consumer) {
        this.getAllPlayers().listIterator().forEachRemaining(consumer);
    }

    public GamePlayer getPlayer(Player player) {
        return this.playersByUuid.get(player.getUniqueId());
    }

    public GamePlayer getPlayer(UUID uuid) {
        return this.playersByUuid.get(uuid);
    }

    public JsonConfig getConfig() {
        return this.config;
    }
}
