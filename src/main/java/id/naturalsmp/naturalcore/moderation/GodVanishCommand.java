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

        // ... (WHOIS LOGIC SAMA SEPERTI SEBELUMNYA) ...
        if (label.equalsIgnoreCase("whois")) {
            if (args.length == 0) return true;
            Player t = Bukkit.getPlayer(args[0]);
            if (t == null) { sender.sendMessage("Player not found"); return true; }
            sender.sendMessage(ChatUtils.colorize("&6WHOIS: &e" + t.getName()));
            sender.sendMessage(ChatUtils.colorize("&7GodMode: " + godPlayers.contains(t.getUniqueId())));
            sender.sendMessage(ChatUtils.colorize("&7Vanish: " + vanishPlayers.contains(t.getUniqueId())));
            return true;
        }

        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.moderation");

        // --- GOD ---
        if (label.equalsIgnoreCase("god")) {
            if (!p.hasPermission("naturalsmp.god")) return noPerm(p);
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

        // --- VANISH (FIXED) ---
        if (label.equalsIgnoreCase("vanish") || label.equalsIgnoreCase("v")) {
            if (!p.hasPermission("naturalsmp.vanish")) return noPerm(p);

            if (vanishPlayers.contains(p.getUniqueId())) {
                // UN-VANISH
                vanishPlayers.remove(p.getUniqueId());

                // MUNCULKAN KEMBALI KE SEMUA PLAYER
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.showPlayer(NaturalCore.getInstance(), p);
                }

                p.sendMessage(prefix + ConfigUtils.getString("messages.vanish-disabled"));
            } else {
                // VANISH
                vanishPlayers.add(p.getUniqueId());

                // SEMBUNYIKAN DARI SEMUA PLAYER ONLINE SEKARANG
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.hasPermission("naturalsmp.vanish.see")) {
                        online.hidePlayer(NaturalCore.getInstance(), p);
                    }
                }

                p.sendMessage(prefix + ConfigUtils.getString("messages.vanish-enabled"));
                p.sendTitle("", ChatUtils.colorize("&b&lVANISHED"), 0, 40, 10);
            }
            return true;
        }

        return true;
    }

    private boolean noPerm(CommandSender s) {
        s.sendMessage(ConfigUtils.getString("messages.no-permission"));
        return true;
    }
}