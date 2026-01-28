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

        // Filter and Sort by weight if possible, but for now just display MIDI, VIP,
        // MVP, NATURE in order
        // We look for keys "naturalsmp.midi", etc.
        int[] slots = { 10, 12, 14, 16 };
        String[] keys = { "naturalsmp.midi", "naturalsmp.vip", "naturalsmp.mvp", "naturalsmp.nature" };
        Material[] mats = { Material.LAPIS_LAZULI, Material.EMERALD, Material.GOLD_INGOT, Material.DIAMOND };

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (ranks.containsKey(key)) {
                inv.setItem(slots[i], createRankItem(mats[i], key));
            }
        }

        // Close Button
        inv.setItem(22, createItem(Material.BARRIER, "&c&lTUTUP", List.of("&7Klik untuk keluar menu.")));

        p.openInventory(inv);
    }

    private ItemStack createRankItem(Material mat, String rankKey) {
        // We use the messages.yml paths (ranks.midi, etc) for lore/display because they
        // are prettier
        String baseKey = rankKey.contains(".") ? rankKey.substring(rankKey.lastIndexOf(".") + 1) : rankKey;
        String path = "ranks." + baseKey;

        String name = ConfigUtils.getMessage(path + ".name");
        List<String> lore = ConfigUtils.getMessageList(path + ".lore");

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
        } else if (e.getSlot() == 10 || e.getSlot() == 12 || e.getSlot() == 14 || e.getSlot() == 16) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            p.sendMessage(ChatUtils.toComponent(
                    "&6&lNaturalSMP &8» &7Silahkan hubungi administrator atau kunjungi &estore.naturalsmp.id &7untuk pembelian!"));
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
