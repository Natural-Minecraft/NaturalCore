package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class EssentialPerksCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        String cmd = label.toLowerCase();
        String prefix = ConfigUtils.getString("prefix.admin");

        // --- HAT ---
        if (cmd.equals("hat")) {
            if (!p.hasPermission("naturalsmp.hat")) return noPerm(p);

            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                p.sendMessage(ConfigUtils.getString("messages.hat-fail"));
                return true;
            }

            ItemStack helmet = p.getInventory().getHelmet();
            p.getInventory().setHelmet(hand);
            p.getInventory().setItemInMainHand(helmet); // Tukar item
            p.sendMessage(prefix + ConfigUtils.getString("messages.hat-success"));
            return true;
        }

        // --- REPAIR ---
        if (cmd.equals("repair")) {
            if (!p.hasPermission("naturalsmp.repair")) return noPerm(p);

            ItemStack item = p.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                p.sendMessage(ChatUtils.colorize("&cPegang item yang mau direpair!"));
                return true;
            }

            repairItem(item);
            p.sendMessage(prefix + ConfigUtils.getString("messages.repair-success"));
            return true;
        }

        // --- NICK ---
        if (cmd.equals("nick")) {
            if (!p.hasPermission("naturalsmp.nick")) return noPerm(p);

            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /nick <nama/off>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("reset")) {
                p.setDisplayName(p.getName());
                // p.setPlayerListName(p.getName()); // Optional: ubah di tablist juga
                p.sendMessage(prefix + ConfigUtils.getString("messages.nick-reset"));
            } else {
                String nick = ChatUtils.colorize(args[0]);
                p.setDisplayName(nick); // Ini yang dipakai di chat %displayname%
                // p.setPlayerListName(nick); // Optional
                p.sendMessage(prefix + ConfigUtils.getString("messages.nick-set").replace("{nick}", nick));
            }
            return true;
        }

        return true;
    }

    private void repairItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
            item.setItemMeta(meta);
        }
    }

    private boolean noPerm(Player p) {
        p.sendMessage(ConfigUtils.getString("messages.no-permission"));
        return true;
    }
}