package id.naturalsmp.naturalcore.fun;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class FunCommand implements CommandExecutor {

    // Simpan data cooldown: UUID -> Waktu habis (epoch millis)
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Command ini hanya untuk player.");
            return true;
        }
        Player p = (Player) sender;

        // Cek Cooldown
        if (isOnCooldown(p)) {
            long timeLeft = (cooldowns.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000;
            p.sendMessage(ConfigUtils.getString("messages.cooldown-msg").replace("%time%", String.valueOf(timeLeft)));
            return true;
        }

        String rawMsg = "";

        // --- /GG ---
        if (label.equalsIgnoreCase("gg")) {
            rawMsg = ConfigUtils.getString("messages.gg-message");
        }

        // --- /NOOB ---
        else if (label.equalsIgnoreCase("noob")) {
            rawMsg = ConfigUtils.getString("messages.noob-message");
        }

        // FIX: Gunakan formatMessage agar %displayname% terbaca
        // formatMessage otomatis handle warna dan replace %player% juga
        String finalMsg = ChatUtils.formatMessage(p, rawMsg);

        // Broadcast
        Bukkit.broadcastMessage(finalMsg);

        // Set Cooldown
        int seconds = ConfigUtils.getInt("fun.cooldown");
        cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));

        return true;
    }

    private boolean isOnCooldown(Player p) {
        return cooldowns.containsKey(p.getUniqueId()) && cooldowns.get(p.getUniqueId()) > System.currentTimeMillis();
    }
}