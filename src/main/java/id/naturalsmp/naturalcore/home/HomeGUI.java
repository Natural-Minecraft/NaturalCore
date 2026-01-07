package id.naturalsmp.naturalcore.home;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HomeGUI implements Listener {

    private final NaturalCore plugin;
    private final String GUI_TITLE = "&#00AAFF&lɴᴀᴛᴜʀᴀʟ ʜᴏᴍᴇ"; // Title Estetik

    public HomeGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player p, int page) {
        // 1. Ambil data home
        List<Home> homes = plugin.getHomeManager().getSortedHomes(p);

        // Validasi Page (Biar gak error index)
        if (homes.isEmpty()) {
            p.sendMessage(ChatUtils.colorize("&cKamu belum punya home! Gunakan /sethome <nama>"));
            return;
        }

        // 2. Setup Inventory 1 Row (9 Slot)
        Inventory inv = Bukkit.createInventory(null, 9, ChatUtils.colorize(GUI_TITLE + " &8(" + (page + 1) + ")"));

        // 3. Pasang Pembatas (Slot 1 & 7)
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.setDisplayName(" ");
        pane.setItemMeta(paneMeta);

        inv.setItem(1, pane);
        inv.setItem(7, pane);

        // 4. Logic Pagination
        int itemsPerPage = 5; // Slot 2, 3, 4, 5, 6
        int totalPages = (int) Math.ceil((double) homes.size() / itemsPerPage);

        // Safety check page
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, homes.size());

        // 5. Isi Slot 2-6 dengan Home
        int slotGUI = 2; // Mulai dari slot 2
        for (int i = startIndex; i < endIndex; i++) {
            Home home = homes.get(i);
            inv.setItem(slotGUI, createHomeItem(home));
            slotGUI++;
        }

        // 6. Tombol Navigasi (Arrow)
        // Tombol Previous (Slot 0)
        if (page > 0) {
            inv.setItem(0, createNavButton("&e&l« Previous Page", page - 1));
        } else {
            inv.setItem(0, pane); // Tutup pakai kaca kalau gak ada prev
        }

        // Tombol Next (Slot 8)
        if (page < totalPages - 1) {
            inv.setItem(8, createNavButton("&e&lNext Page »", page + 1));
        } else {
            inv.setItem(8, pane); // Tutup pakai kaca kalau gak ada next
        }

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    // --- HELPER ITEMS ---

    private ItemStack createHomeItem(Home home) {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize("&a&l" + home.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtils.colorize("&7World: &f" + home.getLocation().getWorld().getName()));
        lore.add(ChatUtils.colorize("&7Coords: &f" + (int)home.getLocation().getX() + ", " + (int)home.getLocation().getY() + ", " + (int)home.getLocation().getZ()));
        lore.add("");
        lore.add(ChatUtils.colorize("&eKlik untuk teleport!"));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavButton(String name, int targetPage) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));

        // Simpan target page di lore (Hidden trick, atau parsing title nanti)
        // Kita pakai parsing title saja di listener biar simpel

        item.setItemMeta(meta);
        return item;
    }

    // --- EVENT LISTENER ---

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = ChatUtils.stripColor(e.getView().getTitle());
        // Cek Font Estetik "ɴᴀᴛᴜʀᴀʟ ʜᴏᴍᴇ"
        if (!title.contains("ɴᴀᴛᴜʀᴀʟ ʜᴏᴍᴇ")) return;

        e.setCancelled(true); // Anti Maling

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        if (e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        // Ambil halaman saat ini dari Title "Natural Home (1)" -> ambil angka 1
        int currentPage = 0;
        try {
            String numStr = title.substring(title.lastIndexOf("(") + 1, title.lastIndexOf(")"));
            currentPage = Integer.parseInt(numStr) - 1; // Karena index mulai dari 0
        } catch (Exception ignored) {}

        // 1. Tombol Previous (Slot 0)
        if (slot == 0 && e.getCurrentItem().getType() == Material.ARROW) {
            open(p, currentPage - 1);
            return;
        }

        // 2. Tombol Next (Slot 8)
        if (slot == 8 && e.getCurrentItem().getType() == Material.ARROW) {
            open(p, currentPage + 1);
            return;
        }

        // 3. Klik Item Home (Slot 2-6)
        if (slot >= 2 && slot <= 6) {
            if (e.getCurrentItem().getType() == Material.RED_BED) {
                // Ambil nama home dari Display Name item
                String homeName = ChatUtils.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

                p.closeInventory();
                plugin.getHomeManager().teleportHome(p, homeName);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (ChatUtils.stripColor(e.getView().getTitle()).contains("ɴᴀᴛᴜʀᴀʟ ʜᴏᴍᴇ")) {
            e.setCancelled(true);
        }
    }
}