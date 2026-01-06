package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class NaturalCoreGUI implements Listener {

    private final NaturalCore plugin;

    public NaturalCoreGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void openGUI(Player player) {
        // Title Estetik
        String title = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ ᴄᴏʀᴇ &8| &7ʜᴇʟᴘ";
        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.colorize(title));

        fillBackground(inv);

        // Ambil Versi dari plugin.yml dan config.yml
        String jarVersion = plugin.getDescription().getVersion();
        String configVersion = plugin.getConfig().getString("config-version", "Unknown");

        String versionStatus = jarVersion.equals(configVersion) ? "&a(Matched)" : "&c(Config Mismatch!)";

        // --- ITEMS ---
        inv.setItem(20, createItem(Material.NETHER_STAR, "&b&lCORE COMMANDS",
                "&7Command dasar plugin.",
                "",
                "&e/nacore reload",
                "&e/nacore version",
                "",
                "&7Plugin Ver: &fv" + jarVersion,
                "&7Config Ver: &fv" + configVersion + " " + versionStatus
        ));

        inv.setItem(21, createItem(Material.NETHERITE_AXE, "&c&lADMIN TOOLS",
                "&7Alat moderasi.", "", "&e/kickall", "&e/restartalert", "&e/bc"));

        inv.setItem(22, createItem(Material.ENDER_EYE, "&a&lWARP SYSTEM",
                "&7Manajemen warp.", "", "&e/warps", "&e/setwarp", "&e/delwarp"));

        inv.setItem(23, createItem(Material.EMERALD, "&6&lTRADER SYSTEM",
                "&7Pedagang keliling.", "", "&e/wt", "&e/settrader", "&e/setharga"));

        inv.setItem(24, createItem(Material.GOLD_INGOT, "&e&lECONOMY",
                "&7Sistem uang.", "", "&e/givebal <player> <currency> <jml>"));

        inv.setItem(40, createItem(Material.BARRIER, "&c&lCLOSE MENU", "&7Tutup menu."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
    }

    @SuppressWarnings("deprecation")
    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) lore.add(ChatUtils.colorize(line));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, "&8");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    // --- KEAMANAN SUPER KETAT ---

    // Method Helper untuk mengecek apakah ini GUI NaturalCore
    private boolean isNaturalCoreGUI(String title) {
        String stripped = ChatUtils.stripColor(title);
        // Kita cek karakter unik yang pasti ada
        // "ɴᴀᴛᴜʀᴀʟ" atau "CORE" atau simbol "|"
        return stripped.contains("ɴᴀᴛᴜʀᴀʟ") || stripped.contains("CORE") || stripped.contains("HELP");
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onClick(InventoryClickEvent e) {
        // Cek Judul
        if (isNaturalCoreGUI(e.getView().getTitle())) {

            // 1. BATALKAN SEMUA INTERAKSI
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;

            // 2. Pastikan yang diklik adalah GUI atas
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;

            Player p = (Player) e.getWhoClicked();

            if (e.getCurrentItem().getType() == Material.GOLD_INGOT) {
                p.closeInventory();
                p.sendMessage(ChatUtils.colorize("&e&lTIP: &7Gunakan: &f/givebal <player> <rupiah/nc> <jumlah>"));
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
            else if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (isNaturalCoreGUI(e.getView().getTitle())) {
            // BATALKAN GESER ITEM
            e.setCancelled(true);
        }
    }
}