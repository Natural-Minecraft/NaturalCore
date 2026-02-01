package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankAddTempCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalsmp.admin")) {
            sender.sendMessage(ConfigUtils.getMessage("global.no-permission"));
            return true;
        }

        if (args.length < 2) {
            ConfigUtils.sendUsage(sender, "/rankaddtemp <player> <midi|vip|mvp|nature> [duration_days]");
            return true;
        }

        String targetName = args[0];
        String rank = args[1].toLowerCase();
        int days = 30;

        if (args.length >= 3) {
            try {
                days = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                ConfigUtils.sendError(sender, ConfigUtils.getMessage("messages.admin.rank.error-duration"));
                return true;
            }
        }

        // 1. Validation for Ranks (Safety)
        if (!rank.equals("midi") && !rank.equals("vip") && !rank.equals("mvp") && !rank.equals("nature")) {
            ConfigUtils.sendError(sender, ConfigUtils.getMessage("messages.admin.rank.error-invalid"));
            return true;
        }

        // 2. Dispatch LuckPerms Command
        String lpCmd = String.format("lp user %s parent addtemp %s %dd", targetName, rank, days);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), lpCmd);

        // 3. Broadcast and Notification
        sender.sendMessage(ChatUtils.colorize(
                "&aBerhasil menambahkan rank &e" + rank + " &aselama &e" + days + " hari &ake &f" + targetName));

        Player target = Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            GUIUtils.broadcastEmpty();
            GUIUtils.broadcast("  &b&lRANK UPGRADE &8┃ &f" + target.getName());
            GUIUtils.broadcast("  &7Mendapatkan Rank &e&l" + rank.toUpperCase() + " &7selama &a" + days + " Hari&7.");
            GUIUtils.broadcast("  &eTerima kasih telah mendukung server! ❤");
            GUIUtils.broadcastEmpty();
        }

        return true;
    }
}
