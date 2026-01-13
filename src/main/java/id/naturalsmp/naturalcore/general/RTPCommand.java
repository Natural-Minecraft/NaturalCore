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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.admin");

        // --- /RESOURCE ---
        if (label.equalsIgnoreCase("resource") || label.equalsIgnoreCase("rsc")) {
            if (!p.hasPermission("naturalsmp.resource")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }

            String resourceWorld = ConfigUtils.getString("rtp.resource-world");
            if (resourceWorld == null)
                resourceWorld = "Resource";

            p.sendMessage(ConfigUtils.getString("messages.rtp.resource"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "betterrtp:betterrtp player " + p.getName() + " " + resourceWorld);
            return true;
        }

        // --- /SURVIVAL (RTP) ---
        if (label.equalsIgnoreCase("survival") || label.equalsIgnoreCase("rtp")) {
            String survivalWorld = ConfigUtils.getString("rtp.survival-world");
            if (survivalWorld == null)
                survivalWorld = "world";

            p.sendMessage(ConfigUtils.getString("messages.rtp.survival"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "betterrtp:betterrtp player " + p.getName() + " " + survivalWorld);
            return true;
        }

        return true;
    }
}