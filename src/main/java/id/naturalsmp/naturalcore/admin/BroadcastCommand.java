package id.naturalsmp.naturalcore.admin;

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

public class BroadcastCommand implements CommandExecutor {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_SECONDS = 5;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!sender.hasPermission("naturalcs.broadcast")) {
            sender.sendMessage(ConfigUtils.getMessage("global.no-permission"));
            return true;
        }

        // --- CEK COOLDOWN ---
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if (cooldowns.containsKey(uuid)) {
                long secondsLeft = ((cooldowns.get(uuid) / 1000) + COOLDOWN_SECONDS)
                        - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    String msg = ConfigUtils.getMessage("fun.cooldown-msg");
                    if (msg == null)
                        msg = "&e&l⚠ &7Tunggu &c%time% detik &7lagi.";
                    player.sendMessage(msg.replace("%time%", String.valueOf(secondsLeft)));
                    return true;
                }
            }
            cooldowns.put(uuid, System.currentTimeMillis());
        }

        // --- LOGIKA COMMAND ---
        if (args.length > 0) {
            String message = String.join(" ", args);

            String format = ConfigUtils.getMessage("admin.broadcast.format");
            if (format == null)
                format = "&a&lBroadcast &b> &e%message%";

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatUtils.colorize(format.replace("%message%", message)));
            Bukkit.broadcastMessage("");
        } else {
            sender.sendMessage(ConfigUtils.getMessage("admin.broadcast.usage"));
        }

        return true;
    }
}
