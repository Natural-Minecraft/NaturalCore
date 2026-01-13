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
            sender.sendMessage(ChatUtils.colorize("&6&lSeason Info:"));
            sender.sendMessage(ChatUtils.colorize("&fCurrent Season: " + manager.getCurrentSeason().getIcon() + " &e"
                    + manager.getCurrentSeason().name()));
            sender.sendMessage(ChatUtils.colorize("&fUse &e/season set <season> &fto change."));
            return true;
        }

        if (args[0].equalsIgnoreCase("set") && args.length > 1) {
            try {
                Season newSeason = Season.valueOf(args[1].toUpperCase());
                // Forcing season change (requires more logic in manager to be fully flushed)
                // We will implement a forceSet method in manager
                sender.sendMessage(ChatUtils.colorize("&aChanging season to " + newSeason.name() + "..."));
                manager.forceSetSeason(newSeason);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatUtils.colorize("&cInvalid season name!"));
            }
            return true;
        }

        return true;
    }
}
