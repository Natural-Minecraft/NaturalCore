package id.naturalsmp.naturalcore.media;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MediaCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;

    public MediaCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        // Admin: /media add <player> <youtube/tiktok>
        if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cKamu tidak memiliki izin."));
                return true;
            }
            String targetName = args[1];
            String platform = args[2].toLowerCase();

            if (!platform.equals("youtube") && !platform.equals("tiktok")) {
                sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cPlatform harus 'youtube' atau 'tiktok'."));
                return true;
            }

            // Execute LuckPerms command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " parent add " + platform);

            sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §aBerhasil menambahkan §e" + targetName + " §ake grup §f" + platform + "§a."));
            sender.sendMessage(ChatUtils.colorize("§7Media player tersebut bisa mengisi link melalui /media panel."));
            return true;
        }

        // Admin: /media remove <player>
        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cKamu tidak memiliki izin."));
                return true;
            }
            String targetName = args[1];

            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            // Remove from both groups
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " parent remove youtube");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + targetName + " parent remove tiktok");

            // Remove link from media.yml
            plugin.getMediaManager().removeLink(target.getUniqueId());

            sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cBerhasil menghapus §e" + targetName + " §cdari media rank dan link channelnya."));
            return true;
        }

        // /media benefits (open benefits GUI, player only)
        if (args.length > 0 && args[0].equalsIgnoreCase("benefits")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Hanya player yang dapat menggunakan command ini.");
                return true;
            }
            plugin.getMediaGUI().openBenefitsGUI(p);
            return true;
        }

        // Admin: /media reload
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cKamu tidak memiliki izin."));
                return true;
            }
            plugin.getMediaManager().loadData();
            sender.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §aMedia config berhasil di-reload."));
            return true;
        }

        // Default: open GUI (player only)
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Hanya player yang dapat menggunakan command ini.");
            sender.sendMessage("Admin usage: /media add <player> <youtube/tiktok>");
            sender.sendMessage("Admin usage: /media remove <player>");
            return true;
        }

        plugin.getMediaGUI().openGUI(p);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("naturalsmp.admin")) {
                completions.add("add");
                completions.add("remove");
                completions.add("reload");
            }
            completions.add("benefits");
        } else if (args.length == 2 && sender.hasPermission("naturalsmp.admin")) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("add") && sender.hasPermission("naturalsmp.admin")) {
            completions.add("youtube");
            completions.add("tiktok");
        }

        // Filter based on current input
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        return completions;
    }
}
