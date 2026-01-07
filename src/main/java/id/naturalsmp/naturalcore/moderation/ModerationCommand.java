package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ModerationCommand implements CommandExecutor {

    private final PunishmentManager pm;

    public ModerationCommand(NaturalCore plugin) {
        this.pm = plugin.getPunishmentManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String prefix = ConfigUtils.getString("prefix.moderation");

        // /KICK <PLAYER> [REASON]
        if (label.equalsIgnoreCase("kick")) {
            if (!sender.hasPermission("naturalsmp.kick")) return true;
            if (args.length == 0) return true;

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage("Player not found"); return true; }

            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Dikick oleh Admin";

            target.kickPlayer(ChatUtils.colorize(ConfigUtils.getString("moderation.kick-message").replace("%reason%", reason)));
            sender.sendMessage(prefix + ConfigUtils.getString("messages.player-kicked").replace("%player%", target.getName()));
            return true;
        }

        // /BAN <PLAYER> [REASON]
        if (label.equalsIgnoreCase("ban")) {
            if (!sender.hasPermission("naturalsmp.ban")) return true;
            if (args.length == 0) return true;

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Banned by Admin";

            pm.ban(target.getUniqueId(), reason, -1); // -1 Permanent
            if (target.isOnline()) {
                target.getPlayer().kickPlayer(ChatUtils.colorize(ConfigUtils.getString("moderation.ban-message").replace("%reason%", reason).replace("%time%", "Permanen")));
            }

            sender.sendMessage(prefix + ConfigUtils.getString("messages.player-banned").replace("%player%", args[0]).replace("%time%", "Permanen").replace("%reason%", reason));
            return true;
        }

        // /TEMPBAN <PLAYER> <TIME> [REASON] (1d, 1h)
        if (label.equalsIgnoreCase("tempban")) {
            // Logic parsing waktu agak panjang, saya singkat:
            // 1. Parse args[1] (misal "1d") jadi milliseconds.
            // 2. panggil pm.ban(uuid, reason, System.currentTimeMillis() + parsedTime);
            sender.sendMessage(prefix + ChatUtils.colorize("&eFitur Tempban perlu parser waktu (Next update!)"));
            return true;
        }

        // /UNBAN
        if (label.equalsIgnoreCase("unban")) {
            if (!sender.hasPermission("naturalsmp.unban")) return true;
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            pm.unban(target.getUniqueId());
            sender.sendMessage(prefix + ConfigUtils.getString("messages.player-unbanned").replace("%player%", args[0]));
            return true;
        }

        return true;
    }
}