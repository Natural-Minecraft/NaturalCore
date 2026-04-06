package id.naturalsmp.naturalcore.chat.prefix;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PrefixCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public PrefixCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        // Admin: /prefix create <id> <display...>
        if (args.length >= 3 && args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ChatUtils.toComponent("&cNo permission."));
                return true;
            }

            String id = args[1].toLowerCase();

            // Combine remaining args as the display string
            StringBuilder display = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) display.append(" ");
                display.append(args[i]);
            }
            String displayStr = display.toString();

            // Remove surrounding quotes if present
            if (displayStr.startsWith("\"") && displayStr.endsWith("\"")) {
                displayStr = displayStr.substring(1, displayStr.length() - 1);
            }

            if (plugin.getPrefixManager().createPrefix(id, displayStr)) {
                sender.sendMessage(ChatUtils.toComponent("&a&l✔ &7Prefix &e" + id + " &7berhasil dibuat! Preview: " + displayStr));
            } else {
                sender.sendMessage(ChatUtils.toComponent("&c&l✘ &cPrefix dengan ID tersebut sudah ada!"));
            }
            return true;
        }

        // Admin: /prefix reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ChatUtils.toComponent("&cNo permission."));
                return true;
            }
            plugin.getPrefixManager().loadConfigs();
            sender.sendMessage(ChatUtils.toComponent("&aPrefix configuration reloaded!"));
            return true;
        }

        // Admin: /prefix grant <player> <prefixId>
        if (args.length >= 3 && args[0].equalsIgnoreCase("grant")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ChatUtils.toComponent("&cNo permission."));
                return true;
            }
            Player target = org.bukkit.Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatUtils.toComponent("&cPlayer not found!"));
                return true;
            }
            String prefixId = args[2].toLowerCase();
            plugin.getPrefixManager().grantPrefix(target, prefixId);
            sender.sendMessage(ChatUtils.toComponent("&a&l✔ &7Granted prefix &e" + prefixId + " &7to &f" + target.getName()));
            return true;
        }

        // Player: /prefix (open GUI)
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Hanya player yang bisa menggunakan perintah ini.");
            return true;
        }

        if (!p.hasPermission("naturalsmp.prefix")) {
            sender.sendMessage(ChatUtils.toComponent("&cKamu tidak memiliki izin untuk menggunakan prefix!"));
            return true;
        }

        new PrefixGUI(plugin).openGUI(p);
        return true;
    }
}
