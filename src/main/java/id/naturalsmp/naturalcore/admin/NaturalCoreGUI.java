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
import org.bukkit.event.inventory.InventoryDragEvent; // <-- Tambahan
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

    public void openGUI(Player player) {
        // Title Estetik (Pastikan konsisten)
        String title = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ ᴄᴏʀᴇ &8| &7ʜᴇʟᴘ";
        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.colorize(title));

        fillBackground(inv);

        // --- ITEMS (Sama seperti sebelumnya) ---
        inv.setItem(20, createItem(Material.NETHER_STAR, "&b&lCORE COMMANDS",
                "&7Command dasar untuk manajemen plugin.", "", "&e/nacore reload", "&e/nacore version"));

        inv.setItem(21, createItem(Material.NETHERITE_AXE, "&c&lADMIN TOOLS",
                "&7Alat bantu moderasi.", "", "&e/kickall", "&e/restartalert", "&e/bc"));

        inv.setItem(22, createItem(Material.ENDER_EYE, "&a&lWARP SYSTEM",
                "&7Manajemen lokasi teleportasi.", "", "&e/warps", "&e/setwarp", "&e/delwarp"));

        inv.setItem(23, createItem(Material.EMERALD, "&6&lTRADER SYSTEM",
                "&7Sistem pedagang keliling.", "", "&e/wt", "&e/settrader", "&e/setharga"));

        inv.setItem(24, createItem(Material.GOLD_INGOT, "&e&lECONOMY",
                "&7Sistem mata uang.", "", "&e/givebal <player> <currency> <jml>"));

        inv.setItem(40, createItem(Material.BARRIER, "&c&lCLOSE MENU", "&7Tutup menu."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
    }

    // --- HELPER METHODS ---
    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));
        List<String> lore = new ArrayList<>();
        for (String line : loreLines) lore.add(ChatUtils.colorize(line));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, "&8");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    // --- EVENT LISTENER (KEAMANAN) ---

    // 1. Cegah Klik & Shift-Klik
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (ChatUtils.stripColor(e.getView().getTitle()).contains("NATURAL CORE")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            Player p = (Player) e.getWhoClicked();

            // Jika klik Gold Ingot (Economy)
            if (e.getCurrentItem().getType() == Material.GOLD_INGOT) {
                p.closeInventory();
                p.sendMessage(ChatUtils.colorize("&e&lTIP: &7Gunakan command berikut:"));
                p.sendMessage(ChatUtils.colorize("&f/givebal <player> <rupiah/nc> <jumlah>"));
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }

            // Tombol Close
            if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }

    // 2. Cegah Dragging (Geser item pake mouse ditahan)
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (ChatUtils.stripColor(e.getView().getTitle()).contains("NATURAL CORE")) {
            e.setCancelled(true); // KUNCI MATI
        }
    }
}