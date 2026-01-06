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
        // Title Estetik
        String title = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ ᴄᴏʀᴇ &8| &7ʜᴇʟᴘ";
        Inventory inv = Bukkit.createInventory(null, 45, ChatUtils.colorize(title));

        // 1. Fill Border & Background
        fillBackground(inv);

        // 2. Set Category Items
        // Slot 20: Core (Jantung)
        inv.setItem(20, createItem(Material.NETHER_STAR, "&b&lCORE COMMANDS",
                "&7Command dasar untuk manajemen plugin.",
                "",
                "&e/nacore reload",
                "&7Reload konfigurasi config.yml",
                "&8Usage: /nacore reload",
                "",
                "&e/nacore version",
                "&7Melihat versi plugin saat ini",
                "&8Usage: /nacore version"
        ));

        // Slot 21: Admin Tools
        inv.setItem(21, createItem(Material.NETHERITE_AXE, "&c&lADMIN TOOLS",
                "&7Alat bantu moderasi server.",
                "",
                "&e/kickall",
                "&7Kick semua player (Maintenance Mode)",
                "&8Usage: /kickall confirm <alasan>",
                "",
                "&e/restartalert",
                "&7Hitung mundur restart dengan Title",
                "&8Usage: /restartalert confirm",
                "",
                "&e/bc",
                "&7Broadcast pesan ke seluruh server",
                "&8Usage: /bc <pesan>"
        ));

        // Slot 22: Warp System
        inv.setItem(22, createItem(Material.ENDER_EYE, "&a&lWARP SYSTEM",
                "&7Manajemen lokasi teleportasi.",
                "",
                "&e/warps",
                "&7Membuka menu warp visual",
                "&8Usage: /warps [edit]",
                "",
                "&e/setwarp",
                "&7Membuat warp baru",
                "&8Usage: /setwarp <nama>",
                "",
                "&e/delwarp",
                "&7Menghapus warp",
                "&8Usage: /delwarp <nama>",
                "",
                "&e/setwarpicon",
                "&7Ubah icon warp jadi item di tangan",
                "&8Usage: /setwarpicon <nama>"
        ));

        // Slot 23: Trader System
        inv.setItem(23, createItem(Material.EMERALD, "&6&lTRADER SYSTEM",
                "&7Sistem pedagang keliling real-time.",
                "",
                "&e/wt (atau /trader)",
                "&7Membuka toko trader mingguan",
                "&8Usage: /wt",
                "",
                "&e/settrader",
                "&7Input barang jualan (Admin)",
                "&8Usage: /settrader",
                "",
                "&e/setharga",
                "&7Atur harga per slot",
                "&8Usage: /setharga <slot> <harga>"
        ));

        // Slot 24: Economy (Future)
        inv.setItem(24, createItem(Material.GOLD_INGOT, "&e&lECONOMY",
                "&7Sistem mata uang server.",
                "",
                "&e/givebal",
                "&7Memberi uang ke player",
                "&8Usage: /givebal <player> <jumlah>"
        ));

        // Slot 40: Close Button
        inv.setItem(40, createItem(Material.BARRIER, "&c&lCLOSE MENU", "&7Tutup menu bantuan."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
    }

    // --- HELPER METHODS ---

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));

        List<String> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(ChatUtils.colorize(line));
        }
        meta.setLore(lore);

        // Sembunyikan atribut (Damage, Attack Speed, dll) biar bersih
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, "&8");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }
    }

    // --- EVENT LISTENER ---

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().contains("NATURAL CORE")) {
            e.setCancelled(true); // Gak bisa ambil item

            if (e.getCurrentItem() == null) return;

            // Fitur Close Button
            if (e.getCurrentItem().getType() == Material.BARRIER) {
                e.getWhoClicked().closeInventory();
                ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
    }
}