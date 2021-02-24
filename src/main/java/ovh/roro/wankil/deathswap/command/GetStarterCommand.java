package ovh.roro.wankil.deathswap.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.json.JsonStarter;

public class GetStarterCommand extends Command {

    private final GameManager gameManager;

    /**
     * Instantiate the /getstarter command.
     * <p>
     * This command gives the current starter configuration to the sender. 'Starter' means the inventory that is given when the game starts.
     *
     * @param gameManager The game manager containing the configuration
     * @see SetStarterCommand
     * @since 1.0.0
     */
    public GetStarterCommand(@NotNull GameManager gameManager) {
        super("getstarter");

        this.gameManager = gameManager;

        setPermission("deathswap.command.starter");
    }

    /**
     * Executes the command, no arguments are required but the sender must be a player with the "deathswap.command.starter" permission.
     *
     * @param sender The {@link CommandSender} executing the command
     * @param label  The label of the command, that is the command name executed without the slash. For example, if "/gamemode" is executed, the label will be "gamemode"
     * @param args   The arguments provided after the label. Arguments are space-separated
     * @return {@code true} if the command successfully executed, otherwise {@code false}
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!this.testPermission(sender) || !(sender instanceof Player)) {
            return false;
        }

        JsonStarter starter = this.gameManager.getConfig().getStarter();
        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();

        // No need to set Armor Contents/Extra Contents (Off hand) because setContents(...) already includes them
        inventory.setContents(starter.getInventory());

        player.sendMessage("§aStarter actuel récupéré !");

        return true;
    }
}
