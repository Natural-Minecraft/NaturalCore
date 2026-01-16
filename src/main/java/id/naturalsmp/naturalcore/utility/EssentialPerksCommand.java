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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;
        String cmdName = command.getName().toLowerCase(); // FIX: Gunakan getName() agar alias terbaca
        String prefix = ConfigUtils.getString("prefix.player");

        // --- HAT ---
        if (cmdName.equals("hat")) {
            if (!p.hasPermission("naturalsmp.hat"))
                return noPerm(p);

            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                p.sendMessage(ConfigUtils.getString("messages.utils.hat-fail"));
                return true;
            }

            ItemStack helmet = p.getInventory().getHelmet();
            p.getInventory().setHelmet(hand);
            p.getInventory().setItemInMainHand(helmet); // Tukar item
            p.sendMessage(prefix + ConfigUtils.getString("messages.utils.hat-success"));
            p.playSound(p.getLocation(), org.bukkit.Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1f);
            return true;
        }

        // --- REPAIR ---
        if (cmdName.equals("repair") || cmdName.equals("fix")) {
            if (!p.hasPermission("naturalsmp.repair"))
                return noPerm(p);

            ItemStack item = p.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                p.sendMessage(ChatUtils.colorize("&cPegang item yang mau diperbaiki!"));
                return true;
            }

            repairItem(item);
            p.sendMessage(prefix + ConfigUtils.getString("messages.utils.repair-success"));
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
            return true;
        }

        // --- NICK ---
        if (cmdName.equals("nick")) {
            if (!p.hasPermission("naturalsmp.nick"))
                return noPerm(p);

            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /nick <nama/off>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("reset")) {
                p.setDisplayName(p.getName());
                p.setPlayerListName(p.getName());
                p.sendMessage(prefix + ConfigUtils.getString("messages.utils.nick-reset"));
                p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else {
                String nickRaw = args[0];
                if (nickRaw.length() < 4) {
                    p.sendMessage(ChatUtils.colorize(ConfigUtils.getString("messages.utils.nick-too-short")));
                    p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    return true;
                }

                String nick = ChatUtils.colorize(nickRaw);
                p.setDisplayName(nick);
                p.sendMessage(ChatUtils.colorize("&aNickname diubah menjadi: &r" + nick));
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
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
        p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
        return true;
    }
}