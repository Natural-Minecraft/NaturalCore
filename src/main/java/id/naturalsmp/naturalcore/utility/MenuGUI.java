package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuGUI implements Listener {

    private final NaturalCore plugin;

    public MenuGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String title = ConfigUtils.getString("messages.gui.menu.title");
        Inventory inv = GUIUtils.createGUI(new MenuHolder(), 54, title);

        // Border
        fillBorder(inv);

        // 1. Profile (Slot 13)
        ItemStack profile = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta pMeta = profile.getItemMeta();
        pMeta.displayName(ChatUtils.toComponent("&#55FF55&l👤 PROFIL SAYA"));
        List<Component> pLore = new ArrayList<>();
        pLore.add(ChatUtils.toComponent("&7Klik untuk melihat statistik"));
        pLore.add(ChatUtils.toComponent("&7dan informasi akunmu."));
        pLore.add(Component.empty());
        pLore.add(ChatUtils.toComponent("&#FFAA00&l➥ KLIK UNTUK MEMBUKA"));
        pMeta.lore(pLore);
        profile.setItemMeta(pMeta);
        inv.setItem(13, profile);

        // 2. Warps (Slot 29)
        inv.setItem(29, createItem(Material.ENDER_PEARL, "&#00AAFF&l✈ TELEPORT WARP",
                "&7Akses cepat ke lokasi penting", "&7di seluruh dunia NaturalSMP.", "",
                "&#FFAA00&l➥ KLIK UNTUK MEMBUKA"));

        // 3. Homes (Slot 31)
        inv.setItem(31, createItem(Material.RED_BED, "&#FF5555&l🏠 DAFTAR HOME",
                "&7Lihat dan kelola titik", "&7rumah yang telah kamu buat.", "", "&#FFAA00&l➥ KLIK UNTUK MEMBUKA"));

        // 4. Shop (Slot 33)
        inv.setItem(33, createItem(Material.GOLD_INGOT, "&#FFFF55&l💰 TOKO VIRTUAL",
                "&7Beli barang-barang kebutuhan", "&7langsung dari menu ini.", "", "&#FFAA00&l➥ KLIK UNTUK MEMBUKA"));

        // 5. Settings / ChatColor (Slot 23)
        inv.setItem(23, createItem(Material.BRUSH, "&#FF55FF&l🎨 CHAT COLOR",
                "&7Ubah warna dan gaya chatmu", "&7agar terlihat lebih keren.", "", "&#FFAA00&l➥ KLIK UNTUK MEMBUKA"));

        // 6. Basic Commands (Slot 21)
        inv.setItem(21, createItem(Material.KNOWLEDGE_BOOK, "&#00AAFF&l📖 BASIC COMMANDS",
                "&7Kumpulan perintah dasar", "&7seperti /gg, /noob, dan lainnya.", "",
                "&#FFAA00&l➥ KLIK UNTUK MEMBUKA"));

        // 7. Help / Links (Bottom Row)
        inv.setItem(48, createItem(Material.BOOK, "&#AAAAAA&l📖 TUTORIAL WARP", "&7Bingung cara main?",
                "&7Kunjungi &b/warp tutorial"));
        inv.setItem(50, createItem(Material.PAPER, "&#55FFFF&l🌐 DISCORD", "&7Bergabung dengan komunitas",
                "&7dan dapatkan info terbaru."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
    }

    private void fillBorder(Inventory inv) {
        ItemStack filler = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++)
            inv.setItem(i, filler);
        for (int i = 45; i < 54; i++)
            inv.setItem(i, filler);
        for (int i = 9; i < 45; i += 9)
            inv.setItem(i, filler);
        for (int i = 17; i < 54; i += 9)
            inv.setItem(i, filler);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> l = new ArrayList<>();
        for (String s : lore)
            l.add(ChatUtils.toComponent(s));
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuHolder))
            return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

        switch (e.getRawSlot()) {
            case 13 -> p.performCommand("profile");
            case 21 -> new TutorialGUI(plugin).openGUI(p, null); // Open Basic Commands
            case 23 -> p.performCommand("chatcolor");
            case 29 -> p.performCommand("warps");
            case 48 -> p.performCommand("warp tutorial");
            case 50 -> p.sendMessage(ChatUtils.toComponent("&bDiscord: &fhttps://dc.naturalsmp.net/"));
        }
    }

    @EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            e.setCancelled(true);
        }
    }

    public static class MenuHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
