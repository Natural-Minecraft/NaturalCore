package id.naturalsmp.naturalcore.inventory;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InventoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only Player");
            return true;
        }
        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.admin");

        if (args.length == 0) {
            p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", "target"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
            return true;
        }

        // --- /INVSEE ---
        if (label.equalsIgnoreCase("invsee")) {
            if (!p.hasPermission("naturalsmp.invsee")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            p.openInventory(target.getInventory());
            p.sendMessage(prefix + ConfigUtils.getString("messages.invsee-open").replace("%target%", target.getName()));
        }

        // --- /ENDERCHEST ---
        else if (label.equalsIgnoreCase("enderchest") || label.equalsIgnoreCase("ec")) {
            if (!p.hasPermission("naturalsmp.enderchest")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            p.openInventory(target.getEnderChest());
            p.sendMessage(prefix + ConfigUtils.getString("messages.endersee-open").replace("%target%", target.getName()));
        }

        return true;
    }
}