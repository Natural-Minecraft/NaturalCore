package id.naturalsmp.naturalcore.chat.tags;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
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
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize("&8Chat Tags Collection"));

        // Reset Button (Slot 49)
        inv.setItem(49, createItem(Material.BARRIER, "&c&lReset Tag", "&7Hapus tag yang dipakai"));

        int slot = 0;
        String currentTag = tagsManager.getPlayerTag(p);

        for (Map.Entry<String, String> entry : tagsManager.getAvailableTags().entrySet()) {
            if (slot >= 45)
                break; // Limit 45 tags per page for now

            String id = entry.getKey();
            String display = entry.getValue();
            boolean hasPerm = p.hasPermission("naturalsmp.tags." + id);
            boolean isEquipped = currentTag.equals(display);

            Material icon = hasPerm ? Material.NAME_TAG : Material.GRAY_DYE;
            String name = hasPerm ? "&a&l" + id.toUpperCase() : "&c&l" + id.toUpperCase() + " (Locked)";

            List<String> lore = new ArrayList<>();
            lore.add("&7Preview: " + display);
            lore.add("");
            if (isEquipped) {
                lore.add("&e&lEQUIPPED");
                icon = Material.ENCHANTED_BOOK; // Highlight
            } else if (hasPerm) {
                lore.add("&eKlik untuk pakai!");
            } else {
                lore.add("&cKamu tidak memiliki tag ini.");
            }

            // NBT Tag ID hidden in lore or use NBT API if complex.
            // For simplicity, we rely on checking logic or slot mapping if sorted, but map
            // order is not guaranteed.
            // Better: Store ID in Item Name hidden color codes or PersistentDataContainer.
            // Since we don't carry PDC utils broadly yet, let's use the display name trick
            // or just match by text (risky).
            // Safer: Just iterate map again? No.
            // Let's assume we can re-fetch by ID. We will put ID in hidden lore/name or
            // just use logic.
            // Simpelnya: Kita simpan ID di NBT via library NBTAPI yang sudah di-shade user
            // (id.naturalsmp.naturalcore.utils.nbtapi).
            // Tapi untuk cepat tanpa intip library user, kita pakai lore hidden string.

            ItemStack item = createItem(icon, name, lore.toArray(new String[0]));

            // Simpan ID di item meta (LocalizedName) - Support 1.14+
            ItemMeta meta = item.getItemMeta();
            meta.setLocalizedName(id); // Storage key
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
        }

        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));
        List<String> l = new ArrayList<>();
        for (String s : lore) {
            l.add(ChatUtils.colorize(s));
        }
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatUtils.colorize("&8Chat Tags Collection"))) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null)
                return;
            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();

            if (item.getType() == Material.BARRIER) {
                tagsManager.setPlayerTag(p, null);
                p.sendMessage(ChatUtils.colorize("&aTag dihapus!"));
                p.closeInventory();
                return;
            }

            if (item.hasItemMeta() && item.getItemMeta().hasLocalizedName()) {
                String id = item.getItemMeta().getLocalizedName();

                if (p.hasPermission("naturalsmp.tags." + id)) {
                    tagsManager.setPlayerTag(p, id);
                    p.sendMessage(ChatUtils.colorize("&aTag terpasang: " + tagsManager.getAvailableTags().get(id)));
                    p.closeInventory();
                } else {
                    p.sendMessage(ChatUtils.colorize("&cKamu belum membuka tag ini!"));
                    // Play sound fail
                }
            }
        }
    }
}
