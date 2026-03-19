package id.naturalsmp.naturalcore.chat.suffix;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuffixGUI implements Listener {

    private final NaturalCore plugin;
    private final SuffixManager suffixManager;

    public SuffixGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.suffixManager = plugin.getSuffixManager();
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_BARREL_OPEN, 1.0f, 0.8f);
        Inventory inv = GUIUtils.createGUI(null, 54, "&d✦ Suffix Collection");

        String currentSuffixId = suffixManager.getPlayerSuffixId(p);
        Map<String, String> suffixes = suffixManager.getAvailableSuffixes();

        int slot = 0;

        // Reset Button at slot 53
        ItemStack reset = createItem(Material.BARRIER, "&c&lReset Suffix", "&7Hapus suffix saat ini.");
        inv.setItem(53, reset);

        for (Map.Entry<String, String> entry : suffixes.entrySet()) {
            if (slot >= 53) break;

            String id = entry.getKey();
            String display = entry.getValue();
            boolean hasPerm = p.hasPermission("naturalsmp.suffix." + id);
            boolean isEquipped = id.equals(currentSuffixId);

            Material icon = hasPerm ? Material.NAME_TAG : Material.STRUCTURE_VOID;
            String nameColor = hasPerm ? "&a" : "&c";
            String status = isEquipped ? "&a&lEQUIPPED" : (hasPerm ? "&eClick to Equip" : "&cLocked");

            List<String> lore = new ArrayList<>();
            lore.add("&7Preview:" + display);
            lore.add("");
            lore.add(status);

            ItemStack item = createItem(icon, nameColor + id, lore.toArray(new String[0]));

            // Store ID in PersistentDataContainer
            ItemMeta meta = item.getItemMeta();
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "suffix_id");
            pdc.set(key, org.bukkit.persistence.PersistentDataType.STRING, id);

            if (isEquipped) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
        }

        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> l = new ArrayList<>();
        for (String s : lore) {
            l.add(ChatUtils.toComponent(s));
        }
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().title().equals(ChatUtils.toComponent("&d✦ Suffix Collection"))) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory())
                return;

            if (e.getCurrentItem() == null)
                return;
            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();

            if (item.getType() == Material.BARRIER) {
                suffixManager.setPlayerSuffix(p, null);
                p.sendMessage(ChatUtils.toComponent("&aSuffix dihapus!"));
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                p.closeInventory();
                return;
            }

            if (item.hasItemMeta()) {
                org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "suffix_id");

                if (pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                    String id = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);

                    if (p.hasPermission("naturalsmp.suffix." + id)) {
                        suffixManager.setPlayerSuffix(p, id);
                        p.sendMessage(ChatUtils.toComponent(
                                "&aSuffix terpasang:" + suffixManager.getAvailableSuffixes().get(id)));
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        p.closeInventory();
                    } else {
                        p.sendMessage(ChatUtils.toComponent("&cKamu belum membuka suffix ini!"));
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
