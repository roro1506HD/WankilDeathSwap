package ovh.roro.wankil.deathswap.world;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import ovh.roro.wankil.deathswap.DeathSwap;
import ovh.roro.wankil.deathswap.util.IOUtil;

public class WorldManager {

    private static final String LOBBY_URL = "https://i.roro.ovh/minecraft/lobby-1.16.tar.gz";

    private final DeathSwap plugin;
    private final World gameWorld;
    private final Location lobbyLocation;

    private World lobbyWorld;

    public WorldManager(DeathSwap plugin) {
        this.plugin = plugin;
        this.gameWorld = plugin.getServer().getWorlds().get(0);
        this.lobbyLocation = new Location(null, 0.5D, 68.5D, 0.5D, 0.0F, 0.0F);
    }

    /**
     * Ensures the Lobby world is existing and loaded.
     * <p>
     * Downloads the lobby if no folder is found, and creates a new world if the world is not loaded.
     * <p>
     * A lobby world is defined by its name being "lobby" and by being loaded in the server.
     *
     * @return {@code true} if the lobby world is successfully loaded or already loaded. {@code false} if download failed or decompression failed
     */
    public boolean ensureLobbyWorldExists() {
        World world = this.plugin.getServer().getWorld("lobbyworld");

        if (world == null) { // Lobby world does not exist
            this.plugin.getLogger().info("Lobby world not found...");

            File worldFolder = new File("lobbyworld");

            if (!worldFolder.exists()) { // Lobby world folder does not exist
                this.plugin.getLogger().info("Lobby folder not found...");

                File tempFile = new File("lobbyworld.tar.gz");

                if (!tempFile.exists()) { // Compressed lobby does not exist, download it
                    try {
                        this.plugin.getLogger().info("Downloading lobby...");
                        IOUtil.download(LOBBY_URL, tempFile);
                        this.plugin.getLogger().info("Downloaded lobby!");
                    } catch (IOException ex) {
                        this.plugin.getLogger().log(Level.SEVERE, "Could not download lobby", ex);
                        return false;
                    }
                }

                // Extract lobby world
                try {
                    this.plugin.getLogger().info("Extracting lobby...");
                    IOUtil.decompressGzip(tempFile, worldFolder);
                    this.plugin.getLogger().info("Extracted lobby!");
                } catch (IOException ex) {
                    this.plugin.getLogger().log(Level.SEVERE, "Could not extract lobby", ex);
                    return false;
                }
            }

            // Load lobby world
            World lobbyWorld = this.plugin.getServer().createWorld(WorldCreator.name("lobbyworld").environment(Environment.NORMAL));

            if (lobbyWorld != null) {
                this.plugin.getLogger().info("Lobby successfully loaded!");
            } else {
                this.plugin.getLogger().severe("Lobby failed to load.");
            }

            this.setLobbyWorld(lobbyWorld);

            return lobbyWorld != null;
        }

        this.plugin.getLogger().info("Lobby world already loaded!");

        this.setLobbyWorld(world);

        return true;
    }

    public World getGameWorld() {
        return this.gameWorld;
    }

    public World getLobbyWorld() {
        return this.lobbyWorld;
    }

    public Location getLobbyLocation() {
        return this.lobbyLocation;
    }

    private void setLobbyWorld(World lobbyWorld) {
        this.lobbyWorld = lobbyWorld;
        this.lobbyLocation.setWorld(lobbyWorld);
    }
}
