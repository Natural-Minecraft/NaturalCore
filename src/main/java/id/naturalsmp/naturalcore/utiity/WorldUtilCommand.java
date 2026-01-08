package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldUtilCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        World w = p.getWorld();
        String cmd = label.toLowerCase();
        String prefix = ConfigUtils.getString("prefix.admin");

        if (!p.hasPermission("naturalsmp.time")) {
            p.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return true;
        }

        switch (cmd) {
            case "day":
                w.setTime(1000);
                p.sendMessage(prefix + ConfigUtils.getString("messages.time-day"));
                break;
            case "night":
                w.setTime(13000);
                p.sendMessage(prefix + ConfigUtils.getString("messages.time-night"));
                break;
            case "sun":
                w.setStorm(false);
                w.setThundering(false);
                p.sendMessage(prefix + ConfigUtils.getString("messages.weather-sun"));
                break;
            case "rain":
                w.setStorm(true);
                p.sendMessage(prefix + ConfigUtils.getString("messages.weather-rain"));
                break;
        }
        return true;
    }
}