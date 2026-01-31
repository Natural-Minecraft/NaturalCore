package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModerationCommand implements CommandExecutor {

    private final NaturalCore plugin;
    // Set sederhana untuk GodMode (tidak perlu manager ribet)
    private final Set<UUID> godPlayers = new HashSet<>();

    public ModerationCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmd = label.toLowerCase();
        String prefix = ConfigUtils.getString("prefix.moderation");

        // --- 1. WHOIS (Info Player) ---
        if (cmd.equals("whois")) {
            if (!sender.hasPermission("naturalsmp.whois"))
                return noPerm(sender);
            if (args.length == 0) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /whois <player>"));
                return true;
            }

            Player t = Bukkit.getPlayer(args[0]);
            if (t == null) {
                sender.sendMessage(ConfigUtils.getString("messages.global.player-not-found"));
                return true;
            }

            sender.sendMessage(ChatUtils.colorize("&8&m----------------"));
            sender.sendMessage(ChatUtils.colorize("&6WHOIS: &e" + t.getName()));
            sender.sendMessage(ChatUtils.colorize("&7UUID: " + t.getUniqueId()));
            sender.sendMessage(ChatUtils
                    .colorize("&7IP: " + (t.getAddress() != null ? t.getAddress().getHostString() : "Unknown")));
            sender.sendMessage(ChatUtils.colorize("&7Gamemode: " + t.getGameMode()));
            sender.sendMessage(ChatUtils.colorize("&7GodMode: " + godPlayers.contains(t.getUniqueId())));
            sender.sendMessage(ChatUtils.colorize("&7Vanish: " + plugin.getVanishManager().isVanished(t)));
            sender.sendMessage(ChatUtils.colorize("&7Health: " + (int) t.getHealth() + " / "
                    + (int) t.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
            sender.sendMessage(ChatUtils.colorize("&8&m----------------"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Command ini hanya untuk player.");
            return true;
        }
        Player p = (Player) sender;

        // --- 2. GOD MODE ---
        if (cmd.equals("god")) {
            if (!p.hasPermission("naturalsmp.god"))
                return noPerm(p);

            // Check if a target player is specified
            Player target = p;
            boolean newStatus;

            if (args.length > 0) {
                if (!p.hasPermission("naturalsmp.god.other")) {
                    return noPerm(p);
                }
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    ConfigUtils.sendMod(p, "messages.global.player-not-found");
                    return true;
                }
            }

            if (godPlayers.contains(target.getUniqueId())) {
                godPlayers.remove(target.getUniqueId());
                target.setInvulnerable(false); // Disable invulnerability
                newStatus = false;
            } else {
                godPlayers.add(target.getUniqueId());
                target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                target.setFoodLevel(20);
                target.setInvulnerable(true); // Enable invulnerability
                newStatus = true;
            }

            ConfigUtils.sendMod(target,
                    newStatus ? "messages.moderation.god-enabled" : "messages.moderation.god-disabled");
            if (!target.equals(sender)) {
                ConfigUtils.sendMod(sender, "messages.moderation.god-other", "%player%", target.getName(), "%status%",
                        newStatus ? "enabled" : "disabled");
            }
            return true;
        }

        // --- 3. VANISH ---
        if (cmd.equals("vanish") || cmd.equals("v")) {
            if (!p.hasPermission("naturalsmp.vanish"))
                return noPerm(p);

            VanishManager vm = plugin.getVanishManager();
            // Toggle status (Kalau true jadi false, kalau false jadi true)
            boolean newState = !vm.isVanished(p);
            vm.setVanished(p, newState);

            return true;
        }

        return true;
    }

    private boolean noPerm(CommandSender s) {
        ConfigUtils.sendMod((Player) s, "messages.global.no-permission");
        return true;
    }
}