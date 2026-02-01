package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.permissions.PermissionManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankGUI implements Listener {

    private final NaturalCore plugin;

    public RankGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        Inventory inv = GUIUtils.createGUI(new RankHolder(), 27,
                ConfigUtils.getMessage("ranks.gui-title"));

        // --- GLASS BORDER ---
        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // --- DYNAMIC RANK ITEMS ---
        Map<String, PermissionManager.RankConfig> ranks = plugin.getPermissionManager().getRanks();

        int[] slots = { 10, 12, 14, 16 };
        // We look for ranks that HAVE gui settings
        int slotIndex = 0;
        for (PermissionManager.RankConfig rank : ranks.values()) {
            if (rank.guiItem != null && slotIndex < slots.length) {
                inv.setItem(slots[slotIndex], createRankItem(rank));
                slotIndex++;
            }
        }

        // Close Button
        inv.setItem(22, createItem(Material.BARRIER, "&c&lTUTUP", List.of("&7Klik untuk keluar menu.")));

        p.openInventory(inv);
    }

    private ItemStack createRankItem(PermissionManager.RankConfig rank) {
        Material mat = Material.matchMaterial(rank.guiItem);
        if (mat == null)
            mat = Material.PAPER;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(rank.guiName));

            List<Component> componentLore = new ArrayList<>();
            componentLore.add(Component.empty());
            for (String s : rank.guiBenefits) {
                componentLore.add(ChatUtils.toComponent(s));
            }
            componentLore.add(Component.empty());
            componentLore.add(ChatUtils.toComponent("&eKlik untuk melihat/beli!"));

            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> componentLore = new ArrayList<>();
            for (String s : lore)
                componentLore.add(ChatUtils.toComponent(s));
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof RankHolder))
            return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() == 22) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
        } else {
            // Clicked a rank item (Slots 10, 12, 14, 16 are typical)
            if (e.getClickedInventory() != e.getInventory())
                return;

            Material type = e.getCurrentItem().getType();
            if (type != Material.GRAY_STAINED_GLASS_PANE && type != Material.AIR) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                p.sendMessage(ChatUtils.toComponent(
                        "&6&lNaturalSMP &8» &7Silahkan hubungi administrator atau kunjungi &estore.naturalsmp.id &7untuk pembelian!"));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof RankHolder)
            e.setCancelled(true);
    }

    public static class RankHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
