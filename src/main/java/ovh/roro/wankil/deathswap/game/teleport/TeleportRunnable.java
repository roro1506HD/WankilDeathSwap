package ovh.roro.wankil.deathswap.game.teleport;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import net.minecraft.server.v1_16_R3.Vec2F;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.scheduler.BukkitTask;
import ovh.roro.wankil.deathswap.DeathSwap;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.player.GamePlayer;
import ovh.roro.wankil.deathswap.util.spread.SpreadPosition;
import ovh.roro.wankil.deathswap.util.spread.SpreadUtil;

public class TeleportRunnable implements Runnable {

    private final DeathSwap plugin;
    private final GameManager gameManager;
    private final Runnable callback;
    private final Set<TeleportArea> areas;
    private final Queue<TeleportArea> queuedAreas;
    private final long start;
    private final BukkitTask task;

    private int logCounter;

    public TeleportRunnable(DeathSwap plugin, GameManager gameManager, List<GamePlayer> players, Runnable callback) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.callback = callback;

        this.areas = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.queuedAreas = new ConcurrentLinkedQueue<>();
        this.start = System.currentTimeMillis();

        World gameWorld = plugin.getWorldManager().getGameWorld();

        SpreadPosition[] spawns = SpreadUtil.createSpawns(((CraftWorld) gameWorld).getHandle(), new Vec2F(0.0F, 0.0F), players.size(), 1000.0F, 20_000_000);
        for (int i = 0; i < players.size(); i++) {
            SpreadPosition spawn = spawns[i];

            this.areas.add(new TeleportArea(plugin, gameManager, new Location(gameWorld, Location.locToBlock(spawn.x) + 0.5D, 240, Location.locToBlock(spawn.z) + 0.5D), players.get(i)));
        }

        this.queuedAreas.addAll(this.areas);

        this.task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this, 1L, 2L);
    }

    @Override
    public void run() {
        boolean finished = this.isFinished();
        int loadedCount = this.getLoadedCount();
        int totalAreas = this.areas.size();

        if (this.logCounter % 10 == 0) {
            this.plugin.getLogger().info("[TeleportRunnable] Task running (total areas: " + totalAreas + ", loaded areas: " + loadedCount + ", queued areas: " + this.queuedAreas.size() + ", finished: " + finished + ", tps: " + this.plugin.getServer().getTPS()[0] + ")");
        }

        this.logCounter = (this.logCounter + 1) % 10;

        int loadedChunks = 0;
        int chunksToLoad = 0;

        for (TeleportArea area : this.areas) {
            loadedChunks += area.getLoadedCount();
            chunksToLoad += area.getChunksToLoad();
        }

        this.sendProgress(loadedChunks / (float) chunksToLoad);

        if (!this.queuedAreas.isEmpty()) {
            this.queuedAreas.remove().load(area -> {
                area.generatePlatform();
                area.teleportPlayers();

                if (this.isFinished()) {
                    this.plugin.getLogger().info("[TeleportRunnable] All areas loaded (" + (System.currentTimeMillis() - this.start) + "ms)");
                }
            });
        } else if (finished) {
            this.task.cancel();
            this.plugin.getLogger().info("[TeleportRunnable] Task finished. Executing callback.");

            this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new Consumer<BukkitTask>() {
                private final TeleportRunnable runnable = TeleportRunnable.this;

                private int timer = 5;

                @Override
                public void accept(BukkitTask bukkitTask) {
                    if (--this.timer == 0) {
                        bukkitTask.cancel();

                        for (TeleportArea area : this.runnable.areas) {
                            area.breakPlatform();
                        }

                        this.runnable.callback.run();
                        return;
                    }

                    if (this.timer == 4) {
                        for (TeleportArea area : this.runnable.areas) {
                            area.teleportPlayers();
                        }
                    }

                    this.runnable.gameManager.sendTitle(this.runnable.gameManager.getAllPlayers(), "", "§e" + this.timer, 0, 30, 10);
                }
            }, 20L, 20L);
        }
    }

    private void sendProgress(float progress) {
        StringBuilder builder = new StringBuilder("§a||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");

        builder.insert(2 + (int) Math.floor(progress * 100.0F), "§c");

        this.gameManager.sendActionbar(this.gameManager.getAllPlayers(), builder.toString());
    }

    private boolean isFinished() {
        for (TeleportArea area : this.areas) {
            if (!area.isLoaded()) {
                return false;
            }
        }

        return true;
    }

    private int getLoadedCount() {
        int count = 0;

        for (TeleportArea area : this.areas) {
            if (area.isLoaded()) {
                count++;
            }
        }

        return count;
    }
}
