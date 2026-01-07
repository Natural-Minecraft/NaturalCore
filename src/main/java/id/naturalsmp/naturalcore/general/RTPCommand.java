package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RTPCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.admin");

        // --- /RESOURCE ---
        if (label.equalsIgnoreCase("resource") || label.equalsIgnoreCase("rsc")) {
            if (!p.hasPermission("naturalsmp.resource")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }

            if (p.getWorld().getName().equalsIgnoreCase("Resource")) {
                p.sendMessage(prefix + ConfigUtils.getString("messages.rtp-already-here"));
                return true;
            }

            p.sendMessage(prefix + ConfigUtils.getString("messages.rtp-resource"));
            // Execute BetterRTP
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "betterrtp player " + p.getName() + " Resource IGNORECOOLDOWN");
            return true;
        }

        // --- /SURVIVAL (RTP) ---
        if (label.equalsIgnoreCase("survival") || label.equalsIgnoreCase("rtp")) {
            p.sendMessage(prefix + ConfigUtils.getString("messages.rtp-survival"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "betterrtp player " + p.getName() + " world IGNORECOOLDOWN");
            return true;
        }

        return true;
    }
}