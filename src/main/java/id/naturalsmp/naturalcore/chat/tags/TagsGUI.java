package id.naturalsmp.naturalcore.chat.tags;

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

public class TagsGUI implements Listener {

    private final NaturalCore plugin;
    private final TagsManager tagsManager;

    public TagsGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.tagsManager = plugin.getTagsManager();
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_BARREL_OPEN, 1.0f, 0.8f);
        Inventory inv = GUIUtils.createGUI(null, 54, "&8Chat Tags Collection");

        String currentTag = tagsManager.getPlayerTag(p);
        Map<String, String> tags = tagsManager.getAvailableTags();

        int slot = 0;

        // 1. Reset Button
        ItemStack reset = createItem(Material.BARRIER, "&c&lReset Tag", "&7Hapus tag saat ini.");
        inv.setItem(53, reset);

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (slot >= 53)
                break;

            String id = entry.getKey();
            String display = entry.getValue();
            boolean hasPerm = p.hasPermission("naturalsmp.tags." + id);
            boolean isEquipped = id.equals(currentTag);

            Material icon = hasPerm ? Material.NAME_TAG : Material.STRUCTURE_VOID;
            String nameColor = hasPerm ? "&a" : "&c";
            String status = isEquipped ? "&a&lEQUIPPED" : (hasPerm ? "&eClick to Equip" : "&cLocked");

            List<String> lore = new ArrayList<>();
            lore.add("&7Preview: " + display);
            lore.add("");
            lore.add(status);

            ItemStack item = createItem(icon, nameColor + id, lore.toArray(new String[0]));

            // Store ID in PersistentDataContainer
            ItemMeta meta = item.getItemMeta();
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "tag_id");
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
        if (e.getView().title().equals(ChatUtils.toComponent("&8Chat Tags Collection"))) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null)
                return;
            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();

            if (item.getType() == Material.BARRIER) {
                tagsManager.setPlayerTag(p, null);
                p.sendMessage(ChatUtils.toComponent("&aTag dihapus!"));
                p.closeInventory();
                return;
            }

            if (item.hasItemMeta()) {
                org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "tag_id");

                if (pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                    String id = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);

                    if (p.hasPermission("naturalsmp.tags." + id)) {
                        tagsManager.setPlayerTag(p, id);
                        p.sendMessage(
                                ChatUtils.toComponent("&aTag terpasang: " + tagsManager.getAvailableTags().get(id)));
                        p.closeInventory();
                    } else {
                        p.sendMessage(ChatUtils.toComponent("&cKamu belum membuka tag ini!"));
                    }
                }
            }
        }
    }
}
