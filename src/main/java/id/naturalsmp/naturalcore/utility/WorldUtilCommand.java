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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        String cmd = label.toLowerCase();

        if (!p.hasPermission("naturalsmp.time")) {
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        switch (cmd) {
            case "day" -> {
                p.getWorld().setTime(1000);
                ConfigUtils.sendGeneral(p, "messages.utils.time-day");
            }
            case "night" -> {
                p.getWorld().setTime(13000);
                ConfigUtils.sendGeneral(p, "messages.utils.time-night");
            }
            case "sun" -> {
                p.getWorld().setStorm(false);
                p.getWorld().setThundering(false);
                ConfigUtils.sendGeneral(p, "messages.utils.weather-sun");
            }
            case "rain" -> {
                p.getWorld().setStorm(true);
                ConfigUtils.sendGeneral(p, "messages.utils.weather-rain");
            }
        }
        return true;
    }
}