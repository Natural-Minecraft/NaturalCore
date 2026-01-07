package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerUtilCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String prefix = ConfigUtils.getString("prefix.admin");

        // Logic mencari target (Diri sendiri atau orang lain)
        Player target = (sender instanceof Player) ? (Player) sender : null;
        if (args.length > 0 && sender.hasPermission(getPerm(label) + ".others")) {
            target = Bukkit.getPlayer(args[0]);
        }

        if (target == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Console must specify player.");
                return true;
            }
            target = (Player) sender; // Fallback ke diri sendiri
        }

        // Check Permission
        if (!sender.hasPermission(getPerm(label))) {
            sender.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return true;
        }

        // --- /FLY ---
        if (label.equalsIgnoreCase("fly")) {
            boolean flight = !target.getAllowFlight();
            target.setAllowFlight(flight);

            String msgTarget = flight ? "messages.fly-enabled" : "messages.fly-disabled";
            target.sendMessage(prefix + ConfigUtils.getString(msgTarget));

            if (!sender.equals(target)) {
                sender.sendMessage(prefix + ConfigUtils.getString("messages.fly-other")
                        .replace("%target%", target.getName())
                        .replace("%status%", flight ? "ENABLED" : "DISABLED"));
            }
            return true;
        }

        // --- /HEAL ---
        if (label.equalsIgnoreCase("heal")) {
            target.setHealth(target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            target.setFoodLevel(20);
            target.setSaturation(20);
            target.setFireTicks(0);

            target.sendMessage(prefix + ConfigUtils.getString("messages.heal-success"));
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            if (!sender.equals(target)) {
                sender.sendMessage(prefix + ConfigUtils.getString("messages.heal-other").replace("%target%", target.getName()));
            }
            return true;
        }

        // --- /FEED ---
        if (label.equalsIgnoreCase("feed")) {
            target.setFoodLevel(20);
            target.setSaturation(20);

            target.sendMessage(prefix + ConfigUtils.getString("messages.feed-success"));
            target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);

            if (!sender.equals(target)) {
                sender.sendMessage(prefix + ConfigUtils.getString("messages.feed-other").replace("%target%", target.getName()));
            }
            return true;
        }

        return true;
    }

    private String getPerm(String label) {
        return "naturalsmp." + label.toLowerCase();
    }
}