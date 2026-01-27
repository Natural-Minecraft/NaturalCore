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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NaturalCoreGUI implements Listener {

    private final NaturalCore plugin;

    public NaturalCoreGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        if (!p.hasPermission("naturalsmp.admin")) {
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return;
        }

        // Standard 54 slots (6 rows) for more "Large Dashboard" feel
        Inventory inv = Bukkit.createInventory(new AdminHolder(), 54,
                ChatUtils.colorize("&#00FFD4&lＮＡＴＵＲＡＬ &8| &#00D4FF&lＡＤＭＩＮ"));

        // --- GLASSMORPHISM FILLER (Frosted Border Effect) ---
        ItemStack cyanGlass = createItem(Material.CYAN_STAINED_GLASS_PANE, " ");
        ItemStack limeGlass = createItem(Material.LIME_STAINED_GLASS_PANE, " ");
        ItemStack whiteGlass = createItem(Material.WHITE_STAINED_GLASS_PANE, " ");

        // Top and Bottom Borders
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, cyanGlass);
            inv.setItem(45 + i, cyanGlass);
        }
        // Side Accents
        int[] accents = { 9, 17, 18, 26, 27, 35, 36, 44 };
        for (int slot : accents)
            inv.setItem(slot, limeGlass);

        // Inner Glass Highlights (Frosted look)
        int[] frames = { 10, 16, 19, 25, 28, 34, 37, 43 };
        for (int slot : frames)
            inv.setItem(slot, whiteGlass);

        // --- CORE CATEGORIES (Premium Rows) ---

        // Row 1: System Control
        inv.setItem(13, createItem(Material.NETHER_STAR, "&#00FFD4&lSYSTEM CORE",
                "&7Akses utama ke jantung server.", "", "&eKlik untuk panduan cepat!"));

        // Row 2: Features
        inv.setItem(20, createItem(Material.DRAGON_BREATH, "&#FF00D4&lBANNER SYSTEM",
                "&7Visualisasi dinamis & Gifs.", "", "&f/banner create &7- Buat banner",
                "&f/banner list &7- Daftar aktif"));

        inv.setItem(21, createItem(Material.SADDLE, "&#FFAA00&lTELEPORTATION",
                "&7Manajemen pergerakan player.", "", "&f/tp, /tpa, /back, /rtp"));

        inv.setItem(22, createItem(Material.GOLD_BLOCK, "&#FFD400&lECONOMY ENG",
                "&7Sistem finansial server.", "", "&fRp (Vault) & NC (Coins)",
                "&7Gunakan &e/baltop &7untuk cek kaya."));

        inv.setItem(23, createItem(Material.TRIDENT, "&#00D4FF&lRANK & TIERS",
                "&7Leveling & Prestigasi pemain.", "", "&f/tier &7- Menu progress", "&f/tier top &7- Leaderboard"));

        inv.setItem(24, createItem(Material.DAYLIGHT_DETECTOR, "&#FFEE00&lSEASONS",
                "&7Waktu & Atmosfer dunia.", "", "&f/season &7- Ganti musim", "&f/day &7- Set pagi"));

        // Row 3: Admin Tools
        inv.setItem(29, createItem(Material.PAPER, "&#00FF00&lCHAT INTERACTIVE",
                "&7Filter & Otomasi Chat.", "", "&fEmoji support, Tagging,", "&fHoverable Items & Meta."));

        inv.setItem(30, createItem(Material.DIAMOND, "&#FFFF00&lTOPUP NOTIFICATION",
                "&7Kirimkan notifikasi topup.", "", "&eKlik untuk simulasikan ke diri sendiri."));

        inv.setItem(31, createItem(Material.NETHERITE_CHESTPLATE, "&#CC0000&lMODERATION",
                "&7Penertiban & Investigasi.", "", "&f/v, /god, /invsee, /whois"));

        inv.setItem(33, createItem(Material.TARGET, "&#FF4400&lSPAWN ADMIN",
                "&7Manajemen titik awal.", "", "&f/setspawn &7- Krusial!", "&f/spawn &7- Uji coba."));

        // Row 4: Final Actions
        inv.setItem(48, createItem(Material.REDSTONE_BLOCK, "&#FF0000&lDEEP RELOAD",
                "&c&lREFRESH SISTEM", "&7Memperbarui seluruh konfigurasi.", "&7Warps & Spawn akan diload ulang."));

        inv.setItem(49, createItem(Material.BARRIER, "&c&lCLOSE MENU", "&7Kembali ke dunia."));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
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

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof AdminHolder))
            return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player p))
            return;

        if (e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory()))
            return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR)
            return;

        Material mat = item.getType();

        // New Interaction Logic
        switch (mat) {
            case REDSTONE_BLOCK -> { // Deep Reload
                p.closeInventory();
                p.performCommand("nacore admin reload");
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
            }
            case BARRIER -> { // Close
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            case DIAMOND -> { // Topup Test
                p.closeInventory();
                p.performCommand("topupnotification " + p.getName() + " 50000 TEST-ID");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
            case COMMAND_BLOCK -> { // Reset Season
                p.closeInventory();
                p.sendMessage(ChatUtils.colorize(
                        "&6&lNaturalCore &8» &7Gunakan &e/nacore admin resetseason confirm &7di chat untuk konfirmasi."));
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
            }
            case NETHER_STAR -> { // Info
                p.sendMessage(ChatUtils.colorize("&6&lNaturalCore &8» &7Anda sedang menggunakan sistem &fCore v"
                        + plugin.getDescription().getVersion()));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2f);
            }
            default -> {
                // Secondary SFX for navigation items
                if (mat.name().contains("GLASS_PANE"))
                    return;
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof AdminHolder)
            e.setCancelled(true);
    }

    public static class AdminHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
