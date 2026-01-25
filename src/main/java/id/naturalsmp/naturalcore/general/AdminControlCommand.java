package id.naturalsmp.naturalcore.general;

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

public class AdminControlCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public AdminControlCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmd = label.toLowerCase();

        if (args.length == 0) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /" + cmd + " <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
            return true;
        }

        switch (cmd) {
            case "freeze", "ice" -> {
                if (!sender.hasPermission("naturalsmp.freeze")) {
                    sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                UUID uuid = target.getUniqueId();
                if (frozenPlayers.contains(uuid)) {
                    frozenPlayers.remove(uuid);
                    sender.sendMessage(ChatUtils.colorize("&aPemain &f" + target.getName() + " &atelah dicairkan."));
                    target.sendMessage(ChatUtils.colorize("&aKamu telah dicairkan oleh admin."));
                } else {
                    frozenPlayers.add(uuid);
                    sender.sendMessage(ChatUtils.colorize("&cPemain &f" + target.getName() + " &ctelah dibekukan."));
                    target.sendMessage(ChatUtils.colorize("&c&lKAMU TELAH DIBEKUKAN OLEH ADMIN!"));
                }
                return true;
            }
            case "kill" -> {
                if (!sender.hasPermission("naturalsmp.kill")) {
                    sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                    return true;
                }
                target.setHealth(0);
                sender.sendMessage(ChatUtils.colorize("&aPemain &f" + target.getName() + " &atelah dibunuh."));
                return true;
            }
        }

        return false;
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }
}
