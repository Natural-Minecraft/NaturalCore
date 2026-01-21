package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.bukkit.command.TabCompleter;

public class TierCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;

    public TierCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("top")) {
                // Open Leaderboard GUI (Nanti)
                new TierTopGUI(plugin).openGUI(p);
                return true;
            }
        }

        plugin.getTierGUI().openGUI(p);
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if ("top".startsWith(args[0].toLowerCase())) {
                return java.util.Collections.singletonList("top");
            }
        }
        return java.util.Collections.emptyList();
    }
}
