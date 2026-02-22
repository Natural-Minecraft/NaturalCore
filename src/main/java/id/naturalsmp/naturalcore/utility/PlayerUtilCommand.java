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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmdName = label.toLowerCase();

        Player target = (sender instanceof Player) ? (Player) sender : null;

        // Cek argumen jika admin ingin heal orang lain
        if (args.length > 0) {
            // Cek permission dulu sebelum memproses argumen
            String permNode = "naturalsmp."
                    + (cmdName.equals("fly") ? "fly" : cmdName.equals("heal") ? "heal" : "feed");
            if (sender.hasPermission(permNode + ".others")) {
                Player t = Bukkit.getPlayer(args[0]);
                if (t != null)
                    target = t;
            }
        }

        if (target == null) {
            if (args.length > 0) {
                ConfigUtils.sendError(sender,
                        ConfigUtils.getString("messages.global.player-not-found", "Player not found")
                                .replace("%player%", args[0]));
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("Console must specify player");
                return true;
            }

            // Jika tidak ada argumen, baru fallback ke sender
            target = (Player) sender;
        }

        String prefix = ConfigUtils.getString("prefix.admin");

        // --- HEAL ---
        if (cmdName.equals("heal")) {
            if (!sender.hasPermission("naturalsmp.heal"))
                return noPerm(sender);

            // FIX HEAL LOGIC
            double maxHealth = 20.0;
            if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            }
            target.setHealth(maxHealth);
            target.setFoodLevel(20);
            target.setSaturation(20);
            target.setFireTicks(0);

            ConfigUtils.sendGeneral(target, "messages.utils.essentials.heal-success");
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            if (!target.equals(sender)) {
                ConfigUtils.sendGeneral(sender, "messages.utils.essentials.heal-other", "%player%", target.getName());
            }
            return true;
        }

        // --- FEED ---
        if (cmdName.equals("feed")) {
            if (!sender.hasPermission("naturalsmp.feed"))
                return noPerm(sender);

            target.setFoodLevel(20);
            target.setSaturation(20);

            ConfigUtils.sendGeneral(target, "messages.utils.essentials.feed-success");
            target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);

            if (!target.equals(sender)) {
                ConfigUtils.sendGeneral(sender, "messages.utils.essentials.feed-other", "%player%", target.getName());
            }
            return true;
        }

        // --- FLY ---
        if (cmdName.equals("fly")) {
            if (!sender.hasPermission("naturalsmp.fly"))
                return noPerm(sender);

            boolean newStatus = !target.getAllowFlight();
            target.setAllowFlight(newStatus);

            String msgPath = newStatus ? "messages.utils.essentials.fly-enabled"
                    : "messages.utils.essentials.fly-disabled";
            ConfigUtils.sendGeneral(target, msgPath);

            if (!target.equals(sender)) {
                ConfigUtils.sendGeneral(sender, "messages.utils.essentials.fly-other", "%player%", target.getName(),
                        "%status%", newStatus ? "enabled" : "disabled");
            }
            return true;
        }

        return true;
    }

    private boolean noPerm(CommandSender s) {
        s.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
        return true;
    }
}