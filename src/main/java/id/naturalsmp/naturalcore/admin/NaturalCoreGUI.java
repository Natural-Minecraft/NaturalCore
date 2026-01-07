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

    public void openGUI(Player player) {
        String title = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ ᴄᴏʀᴇ &8| &7ᴠ1.3";
        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.colorize(title));

        fillBackground(inv);

        String jarVersion = plugin.getDescription().getVersion();
        String configVersion = plugin.getConfig().getString("config-version", "Unknown");
        String versionStatus = jarVersion.equals(configVersion) ? "&a(Matched)" : "&c(Mismatch!)";

        // ITEMS
        inv.setItem(19, createItem(Material.NETHER_STAR, "&b&lCORE INFO", "&7Status Plugin & Config.", "", "&7Plugin Ver: &f" + jarVersion, "&7Config Ver: &f" + configVersion + " " + versionStatus, "", "&e/nacore reload"));
        inv.setItem(20, createItem(Material.DIAMOND_SWORD, "&c&lMODERATION", "&7Sistem hukuman & pantauan.", "", "&e/god, /vanish", "&e/whois"));
        inv.setItem(21, createItem(Material.CHEST, "&e&lESSENTIALS", "&7Alat bantu survival.", "", "&e/gm, /fly, /heal", "&e/feed, /trash, /craft", "&e/invsee, /enderchest"));
        inv.setItem(22, createItem(Material.GOLD_INGOT, "&6&lECONOMY", "&7Sistem keuangan.", "", "&e/bal, /pay", "&e/baltop", "&e/setbal, /takebal", "&e/givebal"));
        inv.setItem(23, createItem(Material.ENDER_EYE, "&a&lLOCATIONS", "&7Manajemen lokasi.", "", "&e/spawn, /setspawn", "&e/warps, /setwarp"));
        inv.setItem(24, createItem(Material.RED_BED, "&d&lPLAYER TP", "&7Teleportasi player.", "", "&e/home, /sethome", "&e/tpa, /tpahere"));
        inv.setItem(25, createItem(Material.EMERALD, "&2&lNPC TRADER", "&7Pedagang keliling.", "", "&e/wt, /settrader"));
        inv.setItem(40, createItem(Material.BARRIER, "&c&lCLOSE MENU", "&7Tutup menu."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
    }

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

    // --- KEAMANAN (FIXED) ---
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        // Cek Title dengan stripColor agar aman dari kode warna
        String title = ChatUtils.stripColor(e.getView().getTitle());

        // Logikanya: Jika title mengandung "NATURAL CORE", batalkan SEMUA interaksi
        if (title.contains("NATURAL CORE") || title.contains("v1.3")) {
            e.setCancelled(true); // <--- KUNCI UTAMA (Anti Maling)

            if (e.getCurrentItem() == null) return;

            Player p = (Player) e.getWhoClicked();
            if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (ChatUtils.stripColor(e.getView().getTitle()).contains("NATURAL CORE")) {
            e.setCancelled(true);
        }
    }
}