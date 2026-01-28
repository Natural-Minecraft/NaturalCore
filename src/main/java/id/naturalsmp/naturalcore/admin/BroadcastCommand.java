package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
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
            String cmd = label.toLowerCase();

            String formatKey = cmd.contains("world") ? "admin.broadcast.world-format" : "admin.broadcast.format";
            String format = ConfigUtils.getMessage(formatKey);
            if (format == null)
                format = cmd.contains("world") ? "&a&lWorld-BC &b> &e%message%" : "&a&lBroadcast &b> &e%message%";

            String coloredMsg = ChatUtils.colorize(format.replace("%message%", message));

            if (cmd.contains("world") && sender instanceof Player p) {
                p.getWorld().getPlayers().forEach(player -> {
                    player.sendMessage("");
                    player.sendMessage(coloredMsg);
                    player.sendMessage("");
                });
            } else {
                GUIUtils.broadcastEmpty();
                GUIUtils.broadcast(coloredMsg);
                GUIUtils.broadcastEmpty();
            }
        } else {
            sender.sendMessage(ConfigUtils.getMessage("admin.broadcast.usage"));
        }

        return true;
    }
}
