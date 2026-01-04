package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class BroadcastCommand implements CommandExecutor {

    // Menyimpan data cooldown: UUID Player -> Waktu selesai cooldown (Epoch Millis)
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_SECONDS = 5;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("naturalcs.broadcast")) {
            sender.sendMessage(ChatUtils.colorize("&cTidak ada izin"));
            return true;
        }

        // --- CEK COOLDOWN ---
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if (cooldowns.containsKey(uuid)) {
                long secondsLeft = ((cooldowns.get(uuid) / 1000) + COOLDOWN_SECONDS) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    player.sendMessage(ChatUtils.colorize("&cTunggu &e" + secondsLeft + " detik &c!"));
                    return true;
                }
            }
            // Update waktu cooldown
            cooldowns.put(uuid, System.currentTimeMillis());
        }

        // --- LOGIKA COMMAND ---
        if (args.length > 0) {
            // Menggabungkan argumen menjadi satu kalimat string
            String message = String.join(" ", args);

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatUtils.colorize("&a&lBroadcast &b> &e" + message));
            Bukkit.broadcastMessage("");
        } else {
            sender.sendMessage(ChatUtils.colorize("&cSilahkan isi pesan broadcast!"));
        }

        return true;
    }
}