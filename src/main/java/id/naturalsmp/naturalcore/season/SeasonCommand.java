package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SeasonCommand implements CommandExecutor {

    private final SeasonManager manager;

    public SeasonCommand(SeasonManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalsmp.season.admin")) {
            sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        if (args.length == 0) {
            Season season = manager.getRegionManager().getSeason(((Player) sender).getLocation());
            sender.sendMessage(ChatUtils.colorize("&6&lSeason Info (Regional):"));
            sender.sendMessage(ChatUtils.colorize("&fCurrent Region Season: " + season.getIcon() + " &e"
                    + season.name()));
            sender.sendMessage(
                    ChatUtils.colorize("&7Seasons are now determined by region coordinates and world time."));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            sender.sendMessage(ChatUtils.colorize("&cManual season control is disabled in Regional Mode."));
            return true;
        }

        return true;
    }
}
