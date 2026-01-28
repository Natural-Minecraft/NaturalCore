package id.naturalsmp.naturalcore.home;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

import java.util.ArrayList;
import java.util.List;

public class HomeGUI implements Listener {

    private final NaturalCore plugin;
    // Title menggunakan Hex Color sesuai request
    private final String GUI_TITLE = ConfigUtils.getString("messages.gui.home.title");

    public HomeGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // --- BRIDGE UNTUK COMMAND ---
    // HomeCommand memanggil openGUI(p), kita arahkan ke halaman 0
    public void openGUI(Player p) {
        open(p, 0);
    }

    public void open(Player p, int page) {
        // 1. Ambil data home (List Nama)
        List<String> homes = plugin.getHomeManager().getSortedHomes(p);

        // Validasi jika kosong
        if (homes.isEmpty()) {
            p.sendMessage(ConfigUtils.getString("messages.home.list-empty"));
            return;
        }

        // 2. Pagination Logic
        int itemsPerPage = 5; // Slot 2, 3, 4, 5, 6
        int totalPages = (int) Math.ceil((double) homes.size() / itemsPerPage);

        // Safety check page boundaries
        if (page < 0)
            page = 0;
        if (page >= totalPages)
            page = totalPages - 1;

        // 3. Setup Inventory 1 Row (9 Slot)
        Inventory inv = GUIUtils.createGUI(new HomeHolder(), 9,
                GUI_TITLE + " &8(" + (page + 1) + ")");

        // 4. Pasang Pembatas (Slot 1 & 7)
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.displayName(Component.text(" "));
            pane.setItemMeta(paneMeta);
        }

        inv.setItem(1, pane);
        inv.setItem(7, pane);

        // 5. Isi Slot 2-6 dengan Home
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, homes.size());

        int slotGUI = 2; // Mulai dari slot 2

        for (int i = startIndex; i < endIndex; i++) {
            String homeName = homes.get(i);
            // Ambil lokasi dari Manager
            Location loc = plugin.getHomeManager().getHome(p, homeName);

            if (loc != null) {
                inv.setItem(slotGUI, createHomeItem(homeName, loc));
            }
            slotGUI++;
        }

        // 6. Tombol Navigasi (Arrow)
        // Tombol Previous (Slot 0)
        if (page > 0) {
            inv.setItem(0, createNavButton("&e&l« Previous Page"));
        } else {
            inv.setItem(0, pane); // Tutup pakai kaca kalau gak ada prev
        }

        // Tombol Next (Slot 8)
        if (page < totalPages - 1) {
            inv.setItem(8, createNavButton("&e&lNext Page »"));
        } else {
            inv.setItem(8, pane); // Tutup pakai kaca kalau gak ada next
        }

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    // --- HELPER ITEMS ---

    private ItemStack createHomeItem(String name, Location loc) {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(
                    ConfigUtils.getString("messages.gui.home.item-name").replace("%name%", name)));
            String world = (loc.getWorld() != null) ? loc.getWorld().getName() : "unknown";

            List<String> rawLore = ConfigUtils.getMessageList("gui.home.item-lore");
            List<Component> lore = new ArrayList<>();
            if (rawLore != null) {
                for (String s : rawLore) {
                    lore.add(ChatUtils.toComponent(s
                            .replace("%name%", name)
                            .replace("%world%", world)
                            .replace("%x%", String.valueOf(loc.getBlockX()))
                            .replace("%y%", String.valueOf(loc.getBlockY()))
                            .replace("%z%", String.valueOf(loc.getBlockZ()))));
                }
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavButton(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    // --- EVENT LISTENER ---

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof HomeHolder))
            return;

        e.setCancelled(true); // Anti Maling

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if (e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        String titleClean = ChatUtils.stripColor(e.getView().getTitle());

        // Parse halaman saat ini dari Title "Natural Home (1)" -> ambil angka 1
        int currentPage = 0;
        try {
            // Ambil text di dalam kurung terakhir
            String numStr = titleClean.substring(titleClean.lastIndexOf("(") + 1, titleClean.lastIndexOf(")"));
            currentPage = Integer.parseInt(numStr) - 1; // Karena index mulai dari 0
        } catch (Exception ignored) {
            // Fallback jika parsing gagal
        }

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
                // Ambil nama home dari Display Name item (Hapus kode warna)
                // Use Component extraction if necessary, otherwise use
                // getItemMeta().getDisplayName() (deprecated shim) or ChatUtils logic
                // Relying on legacy getDisplayName() for now since default API often patches
                // it.
                // Assuming ChatUtils.stripColor handles null/legacy string.
                @SuppressWarnings("deprecation")
                String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
                String homeName = ChatUtils.stripColor(displayName);

                p.closeInventory();
                p.performCommand("home " + homeName); // Cara paling aman memanggil teleport logic
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof HomeHolder) {
            e.setCancelled(true);
        }
    }

    public static class HomeHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
