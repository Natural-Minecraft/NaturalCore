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

        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        // Cek Cooldown
        if (isOnCooldown(p)) {
            long timeLeft = (cooldowns.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000;
            p.sendMessage(ConfigUtils.getString("messages.cooldown-msg").replace("%time%", String.valueOf(timeLeft)));
            return true;
        }

        String msg = "";

        // --- /GG ---
        if (label.equalsIgnoreCase("gg")) {
            msg = ConfigUtils.getString("messages.gg-message").replace("%player%", p.getName());
        }

        // --- /NOOB ---
        else if (label.equalsIgnoreCase("noob")) {
            msg = ConfigUtils.getString("messages.noob-message").replace("%player%", p.getName());
        }

        // Broadcast
        Bukkit.broadcastMessage(ChatUtils.colorize(msg));

        // Set Cooldown
        int seconds = ConfigUtils.getInt("fun.cooldown");
        cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));

        return true;
    }

    private boolean isOnCooldown(Player p) {
        return cooldowns.containsKey(p.getUniqueId()) && cooldowns.get(p.getUniqueId()) > System.currentTimeMillis();
    }
}