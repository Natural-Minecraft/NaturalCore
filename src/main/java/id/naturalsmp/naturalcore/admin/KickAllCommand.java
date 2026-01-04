package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KickAllCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // 1. Cek Permission (naturalcs.kickall)
        if (!sender.hasPermission("naturalcs.kickall")) {
            sender.sendMessage(ChatUtils.colorize("&cNo perms"));
            return true;
        }

        // 2. Cek Argumen (Minimal harus ada 'confirm' dan 'alasan')
        // Usage: /kickall confirm <alasan>
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /kickall confirm <alasan>"));
            return true;
        }

        // 3. Cek Konfirmasi
        String confirm = args[0];
        if (!confirm.equalsIgnoreCase("confirm") &&
                !confirm.equalsIgnoreCase("confirmation") &&
                !confirm.equalsIgnoreCase("konfirmasi")) {

            sender.sendMessage(ChatUtils.colorize("&cSilahkan ketik &aConfirmation &csebelum reason"));
            return true;
        }

        // 4. Gabungkan Alasan (args 1 sampai akhir)
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // 5. Nama Penendang
        String kickerName = (sender instanceof Player) ? sender.getName() : "Console";

        // 6. Format Pesan Kick (Sesuai Skript)
        String kickMessage = ChatUtils.colorize(
                "&a&lNatural &6&lService\n\n" +
                        "&cMaaf, " + kickerName + " telah melakukan kick\n" +
                        "&7Reason: &c" + reason
        );

        // 7. Eksekusi Kick Loop
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Opsional: Jangan kick diri sendiri atau admin lain (Bisa ditambahkan logic disini)
            if (onlinePlayer.equals(sender)) continue; // Jangan kick diri sendiri

            onlinePlayer.kickPlayer(kickMessage);
        }

        sender.sendMessage(ChatUtils.colorize("&aBerhasil meng-kick semua player!"));
        return true;
    }
}