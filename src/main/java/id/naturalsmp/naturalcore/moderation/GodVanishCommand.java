package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodVanishCommand implements CommandExecutor {

    public static final Set<UUID> godPlayers = new HashSet<>();
    public static final Set<UUID> vanishPlayers = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.moderation");

        // --- /GOD ---
        if (label.equalsIgnoreCase("god")) {
            if (!p.hasPermission("naturalsmp.god")) return true;

            if (godPlayers.contains(p.getUniqueId())) {
                godPlayers.remove(p.getUniqueId());
                p.sendMessage(prefix + ConfigUtils.getString("messages.god-disabled"));
            } else {
                godPlayers.add(p.getUniqueId());
                p.setHealth(20);
                p.setFoodLevel(20);
                p.sendMessage(prefix + ConfigUtils.getString("messages.god-enabled"));
            }
            return true;
        }

        // --- /VANISH ---
        if (label.equalsIgnoreCase("vanish") || label.equalsIgnoreCase("v")) {
            if (!p.hasPermission("naturalsmp.vanish")) return true;

            if (vanishPlayers.contains(p.getUniqueId())) {
                // UN-VANISH
                vanishPlayers.remove(p.getUniqueId());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.showPlayer(NaturalCore.getInstance(), p);
                }
                p.sendMessage(prefix + ConfigUtils.getString("messages.vanish-disabled"));
            } else {
                // VANISH
                vanishPlayers.add(p.getUniqueId());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.hasPermission("naturalsmp.vanish.see")) {
                        online.hidePlayer(NaturalCore.getInstance(), p);
                    }
                }
                p.sendMessage(prefix + ConfigUtils.getString("messages.vanish-enabled"));
            }
            return true;
        }

        // --- /WHOIS <PLAYER> ---
        if (label.equalsIgnoreCase("whois")) {
            // (Logic sederhana menampilkan info player)
            if (args.length == 0) return true;
            Player t = Bukkit.getPlayer(args[0]);
            if (t == null) { p.sendMessage("Player not found"); return true; }

            p.sendMessage(ChatUtils.colorize("&8&m----------------"));
            p.sendMessage(ChatUtils.colorize("&6WHOIS: &e" + t.getName()));
            p.sendMessage(ChatUtils.colorize("&7UUID: " + t.getUniqueId()));
            p.sendMessage(ChatUtils.colorize("&7IP: " + (p.isOp() ? t.getAddress().toString() : "Hidden")));
            p.sendMessage(ChatUtils.colorize("&7Gamemode: " + t.getGameMode()));
            p.sendMessage(ChatUtils.colorize("&7GodMode: " + godPlayers.contains(t.getUniqueId())));
            p.sendMessage(ChatUtils.colorize("&7Fly: " + t.getAllowFlight()));
            p.sendMessage(ChatUtils.colorize("&8&m----------------"));
            return true;
        }

        return true;
    }
}