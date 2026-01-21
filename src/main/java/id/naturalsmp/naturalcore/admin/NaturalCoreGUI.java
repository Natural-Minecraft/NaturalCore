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
    private final String GUI_TITLE = ConfigUtils.getString("messages.gui.admin.title");

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

        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.colorize("&2&lNATURAL CORE &8| &2&lADMIN GUIDE"));

        // --- FILLER ---
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            inv.setItem(i, filler);
        }

        // --- HEADER ---
        inv.setItem(4, createItem(Material.WRITTEN_BOOK, "&a&lADMIN HANDBOOK",
                "&7Panduan lengkap fitur & perintah",
                "&7untuk Administrator NaturalSMP.",
                "",
                "&e&oHover item untuk melihat detail!"));

        // Row 2: DOCUMENTATION
        inv.setItem(19, createItem(Material.PAINTING, "&b&lBANNER SYSTEM",
                "&7Fitur Custom Images/Gifs.",
                "",
                "&eCommands:",
                "&f/banner create <name> <url>",
                "&f/banner delete <name>",
                "&f/banner list",
                "",
                "&aTips:",
                "&7Gunakan &f//wand &7untuk area selection."));

        inv.setItem(20, createItem(Material.ENDER_PEARL, "&d&lRTP & WORLD SYSTEM",
                "&7Sistem Teleportasi Random.",
                "",
                "&eCommands:",
                "&f/rtp &7(Survival)",
                "&f/resource &7(Resource World)",
                "",
                "&aConfig:",
                "&7Atur dunia tujuan di &fconfig.yml"));

        inv.setItem(21, createItem(Material.GOLD_INGOT, "&e&lECONOMY SYSTEM",
                "&7Dual Economy: Vault & CoinsEngine.",
                "",
                "&eCommands:",
                "&f/setbal <player> <amount>",
                "&f/givebal <player> <amount>",
                "&f/takebal <player> <amount>",
                "&f/baltop",
                "",
                "&aCurrency:",
                "&fRp (Vault) &7& &fNC (Coins)"));

        inv.setItem(22, createItem(Material.EXPERIENCE_BOTTLE, "&6&lRANK & TIER SYSTEM",
                "&7Progresi Level Pemain.",
                "",
                "&eCommands:",
                "&f/tier &7(Main GUI)",
                "&f/tier top &7(Leaderboard)",
                "",
                "&aData:",
                "&7Disimpan di &ftiers.yml &7dan &fplayer_tiers.yml"));

        inv.setItem(23, createItem(Material.CLOCK, "&b&lSEASON & TIME",
                "&7Sistem Musim & Waktu Realtime.",
                "",
                "&eCommands:",
                "&f/season set <season>",
                "&f/day, /night, /noon",
                "&f/sun, /rain, /storm",
                "",
                "&aFeatures:",
                "&7Visual biome change & Temperature scaling."));

        inv.setItem(24, createItem(Material.NAME_TAG, "&c&lCHAT & TAGS",
                "&7Sistem Chat Modern.",
                "",
                "&eFeatures:",
                "&fCustom Formatting (LuckPerms)",
                "&fRGB/Gradient Support",
                "&fInteractive Tags & Menus",
                "&fEmoji System (/emoji)",
                "&fMention System (@Player)"));

        inv.setItem(25, createItem(Material.IRON_CHESTPLATE, "&3&lMODERATION TOOLS",
                "&7Alat Bantu Staff.",
                "",
                "&eCommands:",
                "&f/god &7- Invulnerable",
                "&f/vanish &7- Invisible",
                "&f/invsee <player>",
                "&f/endersee <player>",
                "&f/tphere <player>",
                "&f/otp <player> &7(Offline TP)"));

        // Row 3: UTILITIES & ACTIONS
        inv.setItem(39, createItem(Material.REPEATING_COMMAND_BLOCK, "&c&lACTION: RELOAD",
                "&7Reload semua file konfigurasi.",
                "&7Gunakan jika ada perubahan di .yml",
                "",
                "&eKlik untuk Reload!"));

        inv.setItem(41, createItem(Material.TARGET, "&a&lSPAWN MANAGEMENT",
                "&7Panduan Spawn.",
                "",
                "&eCommand:",
                "&f/setspawn &7(Set lokasi spawn)",
                "&f/spawn &7(Teleport ke spawn)",
                "",
                "&cNote: Spawn sangat krucial!"));

        inv.setItem(40, createItem(Material.BARRIER, "&c&lCLOSE GUIDE", "&7Tutup menu ini."));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
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
        String title = ChatUtils.stripColor(e.getView().getTitle());
        String expected = "NATURAL CORE | ADMIN GUIDE";

        if (!title.equals(expected))
            return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory()))
            return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        Material mat = e.getCurrentItem().getType();

        // Actions
        if (mat == Material.REPEATING_COMMAND_BLOCK) {
            // Reload action kept as convenience
            p.performCommand("nacore reload");
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        } else if (mat == Material.BARRIER) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else {
            // General "Read" sound for documentation items
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 2f);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String title = ChatUtils.stripColor(e.getView().getTitle());
        String expected = "NATURAL CORE | ADMIN GUIDE";
        if (title.equals(expected))
            e.setCancelled(true);
    }
}