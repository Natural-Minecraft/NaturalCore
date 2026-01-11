package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class NaturalCoreGUI implements Listener {

    private final NaturalCore plugin;
    // Title Modern (Hex Color)
    private final String GUI_TITLE = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ &#55FF55&lᴀᴅᴍɪɴ";

    public NaturalCoreGUI(NaturalCore plugin) {
        this.plugin = plugin;
        // Register Listener Otomatis saat GUI dibuat (Hati-hati duplikat listener jika
        // sering new)
        // Cara yang lebih aman: Daftarkan listener 1x di Main Class, tapi untuk
        // simplifikasi kita pakai static check atau register di sini
    }

    public void openGUI(Player p) {
        // PERMISSION CHECK: Hanya Admin yang bisa buka GUI ini
        if (!p.hasPermission("naturalsmp.admin")) {
            p.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return;
        }

        // Buat Inventory 3 Row (27 Slot)
        Inventory inv = Bukkit.createInventory(null, 27, ChatUtils.colorize(GUI_TITLE));

        // --- FILLER (Kaca) ---
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // --- ITEMS ---

        // 1. Reload Config (Slot 11)
        inv.setItem(11, createItem(Material.EMERALD, "&a&lReload Config", "&7Klik untuk reload config.yml"));

        // 2. Set Spawn (Slot 13)
        inv.setItem(13,
                createItem(Material.BEACON, "&b&lSet Spawn", "&7Set lokasi spawn utama", "&7di posisi kamu berdiri."));

        // 3. Creative Mode (Slot 15)
        inv.setItem(15, createItem(Material.DIAMOND_CHESTPLATE, "&e&lCreative Mode", "&7Ubah gamemode ke Creative"));

        // 4. Survival Mode (Slot 16 - Sebelahnya)
        inv.setItem(16, createItem(Material.IRON_CHESTPLATE, "&7&lSurvival Mode",
                "&7Ubah gamemode ke Survival")); // Opsional

        // 5. Close (Slot 22)
        inv.setItem(26, createItem(Material.BARRIER, "&c&lClose"));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLLING, 1f, 1f);

        // Register Listener SEMENTARA (Sebaiknya register di Main Class 1x saja)
        // Tapi untuk fix cepat, pastikan listener ini terdaftar di NaturalCore.java
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            List<String> loreList = new ArrayList<>();
            for (String l : lore) {
                loreList.add(ChatUtils.colorize(l));
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- EVENT LISTENER ---
    // Pastikan class ini di-register di NaturalCore.java:
    // getServer().getPluginManager().registerEvents(new NaturalCoreGUI(this),
    // this);

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        // Cek Title (Strip Color biar aman)
        String title = ChatUtils.stripColor(e.getView().getTitle());
        String expected = ChatUtils.stripColor(GUI_TITLE);

        if (!title.equals(expected))
            return;

        // 1. CANCEL EVENT - SEMUA INTERAKSI (Anti Steal)
        e.setCancelled(true);

        // 2. Pastikan hanya player yang bisa klik
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        // 3. Block ALL interactions in GUI (top inventory dan shift-click dari bottom)
        // Jika click di top inventory ATAU shift-click dari bottom ke top
        if (e.getClickedInventory() == null)
            return;

        // Safety: Cancel semua jenis klik termasuk shift, number keys, dll
        if (e.getClick().isShiftClick() || e.getClick().isKeyboardClick()) {
            return; // Sudah di-cancel, langsung return
        }

        // 4. Khusus klik item di TOP inventory (GUI kita)
        if (e.getClickedInventory().equals(e.getView().getTopInventory())) {
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                return;

            Material mat = e.getCurrentItem().getType();

            // 5. LOGIC per Item
            if (mat == Material.EMERALD) {
                p.performCommand("nacore reload");
                p.closeInventory();
            } else if (mat == Material.BEACON) {
                p.performCommand("setspawn");
                p.closeInventory();
            } else if (mat == Material.DIAMOND_CHESTPLATE) {
                p.performCommand("gmc");
                p.closeInventory();
            } else if (mat == Material.BARRIER) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }

    // 3. HANDLE DRAG EVENT (Anti Steal saat nge-drag item)
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String title = ChatUtils.stripColor(e.getView().getTitle());
        String expected = ChatUtils.stripColor(GUI_TITLE);

        if (title.equals(expected)) {
            e.setCancelled(true);
        }
    }
}