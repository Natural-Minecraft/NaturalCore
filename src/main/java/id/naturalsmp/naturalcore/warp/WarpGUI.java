package id.naturalsmp.naturalcore.warp;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WarpGUI implements Listener {

    private final NaturalCore plugin;
    // Menyimpan siapa yang sedang membuka menu editor
    private final HashMap<UUID, Boolean> editorMode = new HashMap<>();

    public WarpGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, boolean isEditor) {
        // Size 54 (6 baris) agar muat banyak
        String title = isEditor ? "&c&lWARP EDITOR (Drag to Move)" : "&8&lWARPS MENU";
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize(title));

        for (Warp w : plugin.getWarpManager().getWarps()) {
            if (w.getSlot() >= 54) continue; // Safety check

            ItemStack item = new ItemStack(w.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatUtils.colorize(w.getDisplayName()));

            // Copy lore dan tambah info admin jika mode editor
            List<String> lore = new java.util.ArrayList<>(w.getLore());
            lore.replaceAll(ChatUtils::colorize);

            if (isEditor) {
                lore.add("");
                lore.add(ChatUtils.colorize("&e&l[EDITOR MODE]"));
                lore.add(ChatUtils.colorize("&7Drag & Drop untuk pindah slot"));
                lore.add(ChatUtils.colorize("&7Klik Kanan untuk hapus"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(w.getSlot(), item);
        }

        // Dekorasi kaca (opsional, bisa ditambah nanti)

        player.openInventory(inv);
        if (isEditor) editorMode.put(player.getUniqueId(), true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        // Cek apakah ini GUI Warp
        if (!title.contains("WARPS MENU") && !title.contains("WARP EDITOR")) return;

        e.setCancelled(true); // Default cancel agar tidak bisa ambil item

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // --- MODE EDITOR ---
        if (editorMode.containsKey(p.getUniqueId())) {
            e.setCancelled(false); // Izinkan drag & drop di editor mode

            // Logic Save Slot saat inventory diclose nanti
            return;
        }

        // --- MODE NORMAL (TELEPORT) ---
        // Cari warp berdasarkan Display Name (Cara simpel)
        // Note: Sebaiknya pakai NBT/PersistentDataContainer untuk ID yang akurat, tapi ini cukup untuk basic.
        for (Warp w : plugin.getWarpManager().getWarps()) {
            if (ChatUtils.colorize(w.getDisplayName()).equals(clicked.getItemMeta().getDisplayName())) {
                p.closeInventory();
                p.teleport(w.getLocation());
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                p.sendTitle(ChatUtils.colorize(w.getDisplayName()), ChatUtils.colorize("&7Teleporting..."), 0, 20, 10);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (editorMode.containsKey(p.getUniqueId())) {
            // Save posisi baru saat editor ditutup
            Inventory inv = e.getInventory();
            WarpManager wm = plugin.getWarpManager();

            boolean changed = false;

            // Loop semua slot untuk cek perpindahan item
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    // Cari warp yang cocok dengan item ini
                    for (Warp w : wm.getWarps()) {
                        if (ChatUtils.colorize(w.getDisplayName()).equals(item.getItemMeta().getDisplayName())) {
                            if (w.getSlot() != i) {
                                w.setSlot(i); // Update slot baru
                                changed = true;
                            }
                        }
                    }
                }
            }

            if (changed) {
                wm.saveWarps();
                p.sendMessage(ChatUtils.colorize("&a&lWARP &8» &fPosisi warp berhasil disimpan!"));
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
            }

            editorMode.remove(p.getUniqueId());
        }
    }
}