package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KickAllCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        // 1. Cek Permission (naturalcs.kickall)
        if (!sender.hasPermission("naturalcs.kickall")) {
            sender.sendMessage(ConfigUtils.getMessage("global.no-permission"));
            return true;
        }

        // 2. Cek Argumen (Minimal harus ada 'confirm' dan 'alasan')
        if (args.length < 2) {
            sender.sendMessage(ConfigUtils.getMessage("admin.kickall.usage"));
            return true;
        }

        // 3. Cek Konfirmasi
        String confirm = args[0];
        if (!confirm.equalsIgnoreCase("confirm") &&
                !confirm.equalsIgnoreCase("confirmation") &&
                !confirm.equalsIgnoreCase("konfirmasi")) {

            sender.sendMessage(ConfigUtils.getMessage("admin.kickall.confirm-required"));
            return true;
        }

        // 4. Gabungkan Alasan
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // 5. Nama Penendang
        String kickerName = (sender instanceof Player) ? sender.getName() : "Console";

        // 6. Format Pesan Kick
        String kickFormat = ConfigUtils.getMessage("admin.kickall.message-format");
        if (kickFormat == null) {
            kickFormat = "&cMaaf, %kicker% telah melakukan kick\n&7Reason: &c%reason%";
        }
        String kickMessage = ChatUtils.colorize(kickFormat
                .replace("%kicker%", kickerName)
                .replace("%reason%", reason));

        // 7. Eksekusi Kick Loop
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(sender))
                continue;
            onlinePlayer.kickPlayer(kickMessage);
        }

        sender.sendMessage(ConfigUtils.getMessage("admin.kickall.success"));
        return true;
    }
}
