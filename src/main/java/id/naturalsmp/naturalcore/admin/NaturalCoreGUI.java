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
        if (!p.hasPermission("naturalsmp.admin")) {
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize(GUI_TITLE));

        // --- FILLER ---
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        // --- CATEGORY: CORE & SYSTEM ---
        inv.setItem(4, createItem(Material.NETHER_STAR, "&6&lNATURAL CORE v1.6.2",
                "&7Main core plugin untuk NaturalSMP.",
                "&7Dokumentasi sistem administrator."));

        // Row 2: INFO BLOCKS
        inv.setItem(10, createItem(Material.PAINTING, "&b&lBANNER SYSTEM (Display)",
                "&fFitur Papan Informasi Interaktif.",
                "",
                "&ePerintah Utama:",
                "&7/banner create <nama> <image.png>",
                "&7/banner delete <nama>",
                "&7/banner purge &c(Cleanup Hantu)",
                "",
                "&fTips: &7Gunakan &b//wand &7untuk select area.",
                "&7Banner kini presisi dengan offset &b0.02&7."));

        inv.setItem(12, createItem(Material.ENDER_PEARL, "&a&lRTP SYSTEM",
                "&fFitur Random Teleportasi.",
                "",
                "&bDunia Terdaftar:",
                "&7/rtp &8-> &f" + plugin.getConfig().getString("rtp.survival-world", "world"),
                "&7/resource &8-> &f" + plugin.getConfig().getString("rtp.resource-world", "Resource"),
                "",
                "&fPermission: &7naturalsmp.resource"));

        inv.setItem(14, createItem(Material.GOLD_INGOT, "&e&lECONOMY & COINS",
                "&fSistem Mata Uang Server.",
                "",
                "&bMata Uang:",
                "&7- &6Vault (Rp) &7- Utama",
                "&7- &eNaturalCoin (NC) &7- CoinsEngine",
                "",
                "&ePerintah:",
                "&7/bal, /pay, /baltop, /setbal"));

        inv.setItem(16, createItem(Material.CALIBRATED_SCULK_SENSOR, "&d&lSEASONS & WEATHER",
                "&fSistem Musim & Suhu.",
                "",
                "&7- &f/season &7- Cek status",
                "&7- &f/day, /night, /sun, /rain"));

        // Row 3: OTHER INFO
        inv.setItem(28, createItem(Material.WRITABLE_BOOK, "&f&lCHAT & EMOJI",
                "&fSistem Komunikasi.",
                "",
                "&7- &f:smile:, :love:, dll",
                "&7- &f/emoji &7- Daftar lengkap",
                "&7- &f/msg, /reply &7- Private message"));

        inv.setItem(30, createItem(Material.COMPASS, "&2&lWARP & HOME",
                "&fSistem Navigasi.",
                "",
                "&7- &a/warp &7- Menu Warp",
                "&7- &a/home &7- Menu Home",
                "&7- &a/spawn &7- Ke Spawn"));

        inv.setItem(32, createItem(Material.LEATHER_HELMET, "&d&lPLAYER PERKS",
                "&fKemampuan Tambahan.",
                "",
                "&7- &f/fly, /hat, /nick, /repair",
                "&7- &f/heal, /feed, /trash, /wb"));

        inv.setItem(34, createItem(Material.IRON_DOOR, "&c&lMODERATION",
                "&fAlat Pengawasan.",
                "",
                "&7- &f/vanish (v), /god, /whois",
                "&7- &f/invsee, /endersee, /ec"));

        // Bottom Row: ACTIONS
        inv.setItem(48, createItem(Material.EMERALD, "&a&lRELOAD CONFIG", "&7Muat ulang config.yml & messages.yml"));
        inv.setItem(49, createItem(Material.BEACON, "&b&lSET SPAWN", "&7Atur posisi spawn utama"));
        inv.setItem(50, createItem(Material.DIAMOND_CHESTPLATE, "&e&lCREATIVE MODE", "&7Ubah gamemode ke Creative"));

        inv.setItem(53, createItem(Material.BARRIER, "&c&lCLOSE"));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
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
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            } else if (mat == Material.BEACON) {
                p.performCommand("setspawn");
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            } else if (mat == Material.DIAMOND_CHESTPLATE) {
                p.performCommand("gmc");
                p.closeInventory();
            } else if (mat == Material.BARRIER) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            } else if (e.getSlot() >= 10 && e.getSlot() <= 40) {
                // Clicking documentation items
                p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
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