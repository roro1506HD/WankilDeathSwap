package ovh.roro.wankil.deathswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ovh.roro.wankil.deathswap.command.GetStarterCommand;
import ovh.roro.wankil.deathswap.command.SetStarterCommand;
import ovh.roro.wankil.deathswap.command.StartCommand;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.json.JsonConfig;
import ovh.roro.wankil.deathswap.listener.EntityListener;
import ovh.roro.wankil.deathswap.listener.PlayerListener;
import ovh.roro.wankil.deathswap.listener.WorldListener;
import ovh.roro.wankil.deathswap.util.json.ItemStackTypeAdapter;
import ovh.roro.wankil.deathswap.util.json.LocationTypeAdapter;
import ovh.roro.wankil.deathswap.world.WorldManager;

public class DeathSwap extends JavaPlugin {

    /**
     * Custom {@link com.google.gson.Gson} instance capable of handling {@link org.bukkit.Location} and {@link org.bukkit.inventory.ItemStack} serialization properly.
     */
    private final Gson gson;

    public DeathSwap() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Location.class, new LocationTypeAdapter(this))
                .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
                .registerTypeAdapter(CraftItemStack.class, new ItemStackTypeAdapter())
                .create();
    }

    /**
     * The world manager of this plugin. It handles lobby download and extraction, and provides lobby world and game world's {@link org.bukkit.World} instances.
     */
    private WorldManager worldManager;

    /**
     * The game manager of this plugin. Handles everything from game logic (start, win check, end) to player management, passing by all aspects of the game (including animations and swap management)
     */
    private GameManager gameManager;

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
            this.saveResource("config.json", false);
            this.setEnabled(false);
            return;
        }

        JsonConfig config = null;
        Throwable throwable = null;

        try (FileReader reader = new FileReader(new File(this.getDataFolder(), "config.json"))) {
            config = this.gson.fromJson(reader, JsonConfig.class);
        } catch (Exception ex) {
            throwable = ex;
        }

        if (config == null) {
            if (throwable != null) {
                this.getLogger().log(Level.SEVERE, "Could not read config.json", throwable);
            } else {
                this.getLogger().log(Level.SEVERE, "Could not read config.json");
            }

            return;
        }

        if (config.getSwaps().getSteps().length == 0) {
            this.getLogger().severe("Invalid swaps. There must be at least one step.");
            return;
        }

        this.worldManager = new WorldManager(this);

        if (!this.worldManager.ensureLobbyWorldExists()) {
            this.getLogger().severe("Could not load lobby world. Aborting load.");
            this.getServer().shutdown();
            return;
        }

        this.gameManager = new GameManager(this, config);

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(this, this.gameManager), this);
        pluginManager.registerEvents(new EntityListener(this.gameManager), this);
        pluginManager.registerEvents(new WorldListener(), this);

        CommandMap commandMap = this.getServer().getCommandMap();
        String pluginName = this.getDescription().getName();

        commandMap.register(pluginName, new StartCommand(this.gameManager));
        commandMap.register(pluginName, new SetStarterCommand(this, this.gameManager));
        commandMap.register(pluginName, new GetStarterCommand(this.gameManager));

        this.getServer().getOnlinePlayers().forEach(this.gameManager::addPlayer);
    }

    @Override
    public void onDisable() {
        if (this.gameManager != null) {
            this.gameManager.forAllPlayers(gamePlayer -> gamePlayer.getScoreboard().destroy());
        }
    }

    public void saveConfig() {
        try (PrintWriter writer = new PrintWriter(new File(getDataFolder(), "config.json"))) {
            this.gson.toJson(this.gameManager.getConfig(), writer);
            writer.flush();
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config.json", ex);
        }
    }

    public WorldManager getWorldManager() {
        return this.worldManager;
    }
}
