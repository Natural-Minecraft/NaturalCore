package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute; // IMPORT PENTING
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerUtilCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String cmdName = label.toLowerCase();

        Player target = (sender instanceof Player) ? (Player) sender : null;

        // Cek argumen jika admin ingin heal orang lain
        if (args.length > 0) {
            // Cek permission dulu sebelum memproses argumen
            String permNode = "naturalsmp." + (cmdName.equals("fly") ? "fly" : cmdName.equals("heal") ? "heal" : "feed");
            if (sender.hasPermission(permNode + ".others")) {
                Player t = Bukkit.getPlayer(args[0]);
                if (t != null) target = t;
            }
        }

        if (target == null) {
            if (!(sender instanceof Player)) { sender.sendMessage("Console must specify player"); return true; }
            // Jika target null tapi sender player, berarti target = diri sendiri
            target = (Player) sender;
        }

        String prefix = ConfigUtils.getString("prefix.admin");

        // --- HEAL ---
        if (cmdName.equals("heal")) {
            if (!sender.hasPermission("naturalsmp.heal")) return noPerm(sender);

            // FIX HEAL LOGIC
            double maxHealth = 20.0;
            if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            }
            target.setHealth(maxHealth);
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

        // --- FEED ---
        if (cmdName.equals("feed")) {
            if (!sender.hasPermission("naturalsmp.feed")) return noPerm(sender);

            target.setFoodLevel(20);
            target.setSaturation(20);

            target.sendMessage(prefix + ConfigUtils.getString("messages.feed-success"));
            target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);

            if (!sender.equals(target)) {
                sender.sendMessage(prefix + ConfigUtils.getString("messages.feed-other").replace("%target%", target.getName()));
            }
            return true;
        }

        // --- FLY ---
        if (cmdName.equals("fly")) {
            if (!sender.hasPermission("naturalsmp.fly")) return noPerm(sender);

            boolean flight = !target.getAllowFlight();
            target.setAllowFlight(flight);

            String status = flight ? "ENABLED" : "DISABLED";
            String msgTarget = flight ? "messages.fly-enabled" : "messages.fly-disabled";

            target.sendMessage(prefix + ConfigUtils.getString(msgTarget));

            if (!sender.equals(target)) {
                sender.sendMessage(prefix + ConfigUtils.getString("messages.fly-other")
                        .replace("%target%", target.getName())
                        .replace("%status%", status));
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