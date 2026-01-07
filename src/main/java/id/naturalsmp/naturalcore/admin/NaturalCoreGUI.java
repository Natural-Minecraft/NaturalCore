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
        String title = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ ᴄᴏʀᴇ &8| &7ᴠ1.3";
        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.colorize(title));

        fillBackground(inv);

        // Ambil Versi
        String jarVersion = plugin.getDescription().getVersion();
        String configVersion = plugin.getConfig().getString("config-version", "Unknown");
        String versionStatus = jarVersion.equals(configVersion) ? "&a(Matched)" : "&c(Mismatch!)";

        // --- ROW 2: MAIN FEATURES ---

        // 1. CORE INFO (Slot 19)
        inv.setItem(19, createItem(Material.NETHER_STAR, "&b&lCORE INFO",
                "&7Status Plugin & Config.", "",
                "&7Plugin Ver: &f" + jarVersion,
                "&7Config Ver: &f" + configVersion + " " + versionStatus,
                "", "&e/nacore reload"));

        // 2. MODERATION (Slot 20) - BARU
        inv.setItem(20, createItem(Material.DIAMOND_SWORD, "&c&lMODERATION",
                "&7Sistem hukuman & pantauan.", "",
                "&e/ban, /unban, /kick",
                "&e/mute, /unmute",
                "&e/god, /vanish",
                "&e/invsee, /whois"));

        // 3. ESSENTIALS (Slot 21) - BARU
        inv.setItem(21, createItem(Material.CHEST, "&e&lESSENTIALS",
                "&7Alat bantu survival.", "",
                "&e/gm, /fly, /heal",
                "&e/feed, /trash, /craft",
                "&e/enderchest"));

        // 4. ECONOMY (Slot 22)
        inv.setItem(22, createItem(Material.GOLD_INGOT, "&6&lECONOMY",
                "&7Sistem keuangan server.", "",
                "&e/bal, /pay",
                "&e/baltop (GUI)",
                "&e/setbal, /takebal"));

        // 5. WARP & SPAWN (Slot 23)
        inv.setItem(23, createItem(Material.ENDER_EYE, "&a&lLOCATIONS",
                "&7Manajemen lokasi.", "",
                "&e/spawn, /setspawn",
                "&e/warps, /setwarp"));

        // 6. HOME & TP (Slot 24)
        inv.setItem(24, createItem(Material.RED_BED, "&d&lPLAYER TP",
                "&7Teleportasi player.", "",
                "&e/home, /sethome",
                "&e/tpa, /tpahere"));

        // 7. TRADER (Slot 25)
        inv.setItem(25, createItem(Material.EMERALD, "&2&lNPC TRADER",
                "&7Pedagang keliling.", "",
                "&e/wt, /settrader"));

        // CLOSE (Slot 40)
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
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, "&8");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    // --- KEAMANAN ---
    private boolean isNaturalCoreGUI(String title) {
        String stripped = ChatUtils.stripColor(title);
        return stripped.contains("ɴᴀᴛᴜʀᴀʟ") || stripped.contains("CORE") || stripped.contains("v1.3");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (isNaturalCoreGUI(e.getView().getTitle())) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;

            Player p = (Player) e.getWhoClicked();
            if (e.getCurrentItem().getType() == Material.BARRIER) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (isNaturalCoreGUI(e.getView().getTitle())) e.setCancelled(true);
    }
}