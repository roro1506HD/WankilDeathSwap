package ovh.roro.wankil.deathswap.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ovh.roro.wankil.deathswap.game.GameManager;
import ovh.roro.wankil.deathswap.game.GameState;

public class StartCommand extends Command {

    private final GameManager gameManager;

    public StartCommand(GameManager gameManager) {
        super("start");

        this.gameManager = gameManager;

        setPermission("deathswap.command.start");
    }

    /**
     * Executes the command, no arguments are required but the sender must be a player with the
     * "deathswap.command.starter" permission.
     *
     * @param sender The {@link CommandSender} executing the command
     * @param label  The label of the command, that is the command name executed without the slash. For example, if
     *               "/gamemode" is executed, the label will be "gamemode"
     * @param args   The arguments provided after the label. Arguments are space-separated
     * @return {@code true} if the command successfully executed, otherwise {@code false}
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }

        if (this.gameManager.getState() != GameState.WAITING) {
            sender.sendMessage("§cUne partie est déjà en cours !");
            return false;
        }

        if (this.gameManager.getPlayers().size() < 2) {
            sender.sendMessage("§cVous ne pouvez pas lancer une partie avec seulement un joueur !");
            return false;
        }

        this.gameManager.startGame();

        return true;
    }
}
