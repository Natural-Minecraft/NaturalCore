package id.naturalsmp.naturalcore.reforge;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ReforgeGUI {

    public static void openReforgeMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatUtils.color("&8⚒ &lReforge Station"));
        
        // Slot untuk item yang akan di-reforge
        inv.setItem(11, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName("&7Place Item Here")
                .setLore("&7Taruh item yang ingin di-reforge")
                .build());
        
        // Reforge button
        inv.setItem(15, new ItemBuilder(Material.ANVIL)
                .setName("&a&lREFORGE!")
                .setLore(
                    "&7Cost: &f$5000",
                    "",
                    "&eKlik untuk reforge item!"
                )
                .build());
        
        // Info panel
        inv.setItem(13, new ItemBuilder(Material.BOOK)
                .setName("&e&lReforge Info")
                .setLore(
                    "&7Reforge dapat meningkatkan:",
                    "&f• Damage",
                    "&f• Attack Speed",
                    "&f• Crit Chance",
                    "&f• Crit Damage",
                    "",
                    "&cHati-hati! Stats bisa turun juga!"
                )
                .build());
        
        // Fill empty slots
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .setName(" ")
                        .build());
            }
        }
        
        player.openInventory(inv);
    }
}
