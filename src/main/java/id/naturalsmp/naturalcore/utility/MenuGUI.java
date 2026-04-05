package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuGUI v2 — Redesigned hub with player head, rank info, and rich shortcuts.
 */
public class MenuGUI implements Listener {

    private final NaturalCore plugin;

    public MenuGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        Inventory inv = GUIUtils.createGUI(new MenuHolder(), 54,
                "<gradient:#00AAFF:#00FFAA><bold>✦ NATURAL MENU ✦</bold></gradient>");

        // ── Border ────────────────────────────────────────────────────────
        ItemStack border = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack accent = GUIUtils.createFiller(Material.CYAN_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        // Accent corners
        for (int s : new int[]{0, 8, 45, 53}) inv.setItem(s, accent);
        // Middle row dividers
        for (int s : new int[]{18, 26, 27, 35}) inv.setItem(s, accent);

        // ── Player Head (Slot 4, center top) ─────────────────────────────
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(p);
            skullMeta.displayName(ChatUtils.toComponent("<#00AAFF><bold>» " + p.getName() + " «</bold>"));
            List<Component> skullLore = new ArrayList<>();
            skullLore.add(Component.empty());

            // Live rank
            String rankDisplay = "Member";
            if (plugin.getPermissionManager() != null) {
                var ranks = plugin.getPermissionManager().getRanks();
                String[] rankOrder = {"investor","cakrawala","nature_plus_plus","nature_plus","nature",
                        "gold_plus","gold","mvp_plus","mvp","vip_plus","vip","midi"};
                for (String r : rankOrder) {
                    if (p.hasPermission("group." + r)) {
                        var rc = ranks.get(r);
                        if (rc != null) rankDisplay = rc.displayName;
                        break;
                    }
                }
            }

            skullLore.add(ChatUtils.toComponent("§7Rank: §f" + rankDisplay));
            skullLore.add(ChatUtils.toComponent("§7World: §b" + p.getWorld().getName()));
            skullLore.add(Component.empty());
            skullLore.add(ChatUtils.toComponent("<#FFAA00>➥ Klik untuk buka profil"));
            skullMeta.lore(skullLore);
            skull.setItemMeta(skullMeta);
        }
        inv.setItem(4, skull);

        // ── Row 1: Main Actions (Slots 19, 21, 23, 25) ───────────────────

        // Profil
        inv.setItem(19, buildItem(Material.NAME_TAG, "<#55FF55><bold>👤 PROFIL</bold>",
                "§7Statistik, rank, skill,",
                "§7dan informasi akunmu.",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // Rank Shop
        inv.setItem(21, buildItem(Material.DIAMOND, "<#FF88FF><bold>✦ RANK SHOP</bold>",
                "§7Beli rank premium dengan",
                "§7NaturalCoin eksklusif.",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // Warp
        inv.setItem(23, buildItem(Material.ENDER_PEARL, "<#00AAFF><bold>✈ WARP</bold>",
                "§7Teleport ke lokasi penting",
                "§7di seluruh dunia NaturalSMP.",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // Home
        inv.setItem(25, buildItem(Material.RED_BED, "<#FF5555><bold>🏠 HOME</bold>",
                "§7Kelola titik rumahmu",
                "§7dan teleport ke sana.",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // ── Row 2: Secondary Actions (Slots 28, 30, 32, 34) ─────────────

        // Tier (Rank Grind)
        inv.setItem(28, buildItem(Material.EXPERIENCE_BOTTLE, "<#FFFF55><bold>⭐ TIER RANK</bold>",
                "§7Sistem ranking dalam server.",
                "§7Kumpulkan syarat & naik tier!",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // Chat Color
        inv.setItem(30, buildItem(Material.BRUSH, "<#FF55FF><bold>🎨 CHAT COLOR</bold>",
                "§7Ubah warna & gaya chatmu",
                "§7agar makin kece.",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // Suffix Collection
        inv.setItem(32, buildItem(Material.FEATHER, "<#AAFFFF><bold>✎ SUFFIX KOLEKSI</bold>",
                "§7Pilih dan pakai suffix chat",
                "§7dari koleksi yang kamu miliki.",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // Playtime & Rewards
        inv.setItem(34, buildItem(Material.CLOCK, "<#AAFFAA><bold>⌛ PLAYTIME</bold>",
                "§7Lihat waktu bermainmu",
                "§7dan klaim hadiah milestone!",
                "", "<#FFAA00>➥ Klik untuk membuka"));

        // ── Bottom Bar ────────────────────────────────────────────────────

        // Discord
        inv.setItem(46, buildItem(Material.PAPER, "<#7289DA><bold>🌐 DISCORD</bold>",
                "§7Gabung komunitas NaturalSMP!",
                "§bdc.naturalsmp.net"));

        // Store link
        inv.setItem(47, buildItem(Material.GOLD_NUGGET, "<#FFD700><bold>🛒 STORE</bold>",
                "§7Beli rank & item eksklusif",
                "§bstore.naturalsmp.net"));

        // Vote
        inv.setItem(48, buildItem(Material.SUNFLOWER, "<#FFFF55><bold>🗳 VOTE</bold>",
                "§7Vote server dan dapatkan",
                "§7hadiah menarik!",
                "", "<#FFAA00>➥ Klik untuk vote"));

        // Close
        inv.setItem(49, buildItem(Material.BARRIER, "<#FF5555><bold>✖ TUTUP</bold>",
                "§7Klik untuk menutup menu."));

        // Tutorial
        inv.setItem(50, buildItem(Material.BOOK, "<#AAAAAA><bold>📖 TUTORIAL</bold>",
                "§7Panduan dasar bermain",
                "§7di NaturalSMP."));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
    }

    private ItemStack buildItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> l = new ArrayList<>();
        l.add(Component.empty());
        for (String s : lore) l.add(ChatUtils.toComponent(s));
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuHolder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE
                || clicked.getType() == Material.CYAN_STAINED_GLASS_PANE) return;

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.1f);

        switch (e.getRawSlot()) {
            case 4, 19 -> { p.closeInventory(); p.performCommand("profile"); }
            case 21    -> { p.closeInventory(); p.performCommand("ranks"); }
            case 23    -> { p.closeInventory(); p.performCommand("warps"); }
            case 25    -> { p.closeInventory(); p.performCommand("homes"); }
            case 28    -> { p.closeInventory(); p.performCommand("tier"); }
            case 30    -> { p.closeInventory(); p.performCommand("chatcolor"); }
            case 32    -> { p.closeInventory(); p.performCommand("suffix"); }
            case 34    -> { p.closeInventory(); p.performCommand("playtime"); }
            case 46    -> p.sendMessage(ChatUtils.toComponent("§b→ Discord: §fhttps://dc.naturalsmp.net/"));
            case 47    -> p.sendMessage(ChatUtils.toComponent("§b→ Store: §fhttps://store.naturalsmp.net/"));
            case 48    -> { p.closeInventory(); p.performCommand("vote"); }
            case 49    -> p.closeInventory();
            case 50    -> { p.closeInventory(); p.performCommand("warp tutorial"); }
        }
    }

    @EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) e.setCancelled(true);
    }

    public static class MenuHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
