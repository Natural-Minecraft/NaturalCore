package id.naturalsmp.naturalcore.media;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MediaGUI implements Listener {

    private final NaturalCore plugin;
    private final Map<UUID, Boolean> pendingLinkInput = new HashMap<>();

    // Border slots for 54-slot GUI (top row, bottom row, left column, right column)
    private static final int[] BORDER_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,  // top row
            9, 17,                         // row 2 edges
            18, 26,                        // row 3 edges
            27, 35,                        // row 4 edges
            36, 44,                        // row 5 edges
            45, 46, 47, 48, 49, 50, 51, 52, 53 // bottom row
    };

    // Content slots (inside the border, 5 rows x 7 columns = 35 slots)
    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public MediaGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // ─── Main GUI: Famous People Directory ────────────────────────────────────

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        boolean isMedia = p.hasPermission("naturalsmp.media");

        Inventory inv = GUIUtils.createGUI(new MediaHolder(), 54,
                "§8\u2b50 Famous People \u2b50");

        // Fill border with gray glass pane
        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int slot : BORDER_SLOTS) inv.setItem(slot, glass);

        // Fill content area with air first (clear any leftover)
        for (int slot : CONTENT_SLOTS) inv.setItem(slot, new ItemStack(Material.AIR));

        // Header item (top center)
        inv.setItem(4, createItem(Material.NETHER_STAR,
                "<gradient:#FF4444:#FF00FF><b>Famous People</b></gradient>",
                List.of("§7Daftar konten kreator resmi", "§7NaturalSMP yang terverifikasi.")));

        // Get all media players
        Map<UUID, String> allLinks = plugin.getMediaManager().getAllLinks();

        int slotIdx = 0;
        for (Map.Entry<UUID, String> entry : allLinks.entrySet()) {
            if (slotIdx >= CONTENT_SLOTS.length) break; // max 28 players per page

            UUID mediaUUID = entry.getKey();
            String link = entry.getValue();
            OfflinePlayer mediaPlayer = Bukkit.getOfflinePlayer(mediaUUID);

            inv.setItem(CONTENT_SLOTS[slotIdx], createMediaPlayerHead(mediaPlayer, link));
            slotIdx++;
        }

        // If no media players
        if (allLinks.isEmpty()) {
            inv.setItem(22, createItem(Material.BARRIER,
                    "§c§lBelum Ada Kreator",
                    List.of("§7Belum ada konten kreator yang", "§7terdaftar di server ini.")));
        }

        // Close button (bottom center)
        inv.setItem(49, createItem(Material.BARRIER, "§c§lTUTUP",
                List.of("§7Klik untuk menutup menu.")));

        // If player is media, add settings button at bottom-right corner
        if (isMedia) {
            inv.setItem(53, createItem(Material.WRITABLE_BOOK,
                    "§e§l\u2699 Media Panel",
                    List.of(
                            "§7Buka panel pengaturan Media.",
                            "§7Ubah link channel, lihat benefits.",
                            "",
                            "§aKlik untuk membuka."
                    )));
        }

        p.openInventory(inv);
    }

    // ─── Media Panel (Settings for Media Players) ─────────────────────────────

    public void openMediaPanel(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        Inventory inv = GUIUtils.createGUI(new MediaPanelHolder(), 27, "§8\u2699 Media Panel");

        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Setting Link Item
        String currentLink = plugin.getMediaManager().getLink(p.getUniqueId());
        ItemStack setting = createItem(Material.WRITABLE_BOOK, "§e§lUbah Link Channel",
                List.of(
                        "§7Link saat ini:",
                        "§f" + currentLink,
                        "",
                        "§aKlik untuk mengubah link channelmu."
                ));
        inv.setItem(11, setting);

        // Benefits Item
        ItemStack benefits = createItem(Material.DIAMOND, "§b§lKeuntungan Media",
                List.of(
                        "§7Klik untuk melihat daftar lengkap",
                        "§7keuntungan sebagai Media."
                ));
        inv.setItem(15, benefits);

        // Back
        inv.setItem(22, createItem(Material.ARROW, "§c§lKEMBALI",
                List.of("§7Kembali ke daftar Famous People.")));

        p.openInventory(inv);
    }

    // ─── Benefits GUI ─────────────────────────────────────────────────────────

    public void openBenefitsGUI(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        Inventory inv = GUIUtils.createGUI(new BenefitsHolder(), 27, "§8Keuntungan Media");
        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, createItem(Material.NAME_TAG, "§e§lPrefix Eksklusif",
                List.of("§7Mendapatkan Prefix khusus Media", "§7yang mencolok di Chat dan Tab.")));

        inv.setItem(12, createItem(Material.CLOCK, "§6§lWaktu & Cuaca Bebas",
                List.of("§7Akses ke command:", "§f/ptime §7dan §f/pweather", "§7Sangat membantu saat recording!")));

        inv.setItem(13, createItem(Material.FEATHER, "§f§lAkses Terbang",
                List.of("§7Bisa menggunakan command §f/fly", "§7Sangat membantu untuk taking cinematic!")));

        inv.setItem(14, createItem(Material.REDSTONE_TORCH, "§c§lHighlight Link (Sneak)",
                List.of("§7Jika player lain jongkok (sneak)", "§7melihat ke arahmu, mereka", "§7akan bisa mendapatkan link", "§7channel-mu secara langsung!")));

        inv.setItem(16, createItem(Material.DIAMOND_CHESTPLATE, "§b§lAkses Setara MVP",
                List.of("§7Semua keuntungan dari Rank", "§aMVP §7juga kamu dapatkan!")));

        inv.setItem(22, createItem(Material.ARROW, "§c§lKEMBALI", List.of("§7Klik untuk kembali.")));

        p.openInventory(inv);
    }

    // ─── Item Builders ────────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private ItemStack createMediaPlayerHead(OfflinePlayer target, String link) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        meta.setOwningPlayer(target);

        String name = target.getName() != null ? target.getName() : "Unknown";
        boolean isOnline = target.isOnline();

        meta.displayName(ChatUtils.toComponent(
                (isOnline ? "§a" : "§7") + "§l" + name
                + (isOnline ? " §a\u25cf" : " §c\u25cf")));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(ChatUtils.toComponent("§7Status: " + (isOnline ? "§aOnline" : "§cOffline")));
        lore.add(ChatUtils.toComponent("§7Channel: §f" + link));
        lore.add(Component.empty());
        lore.add(ChatUtils.toComponent("§eKlik untuk melihat link channel."));

        meta.lore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> cLore = new ArrayList<>();
            for (String s : lore) cLore.add(ChatUtils.toComponent(s));
            meta.lore(cLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ─── Click Handlers ───────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        // Main Media GUI (Famous People)
        if (e.getInventory().getHolder() instanceof MediaHolder) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;
            Player p = (Player) e.getWhoClicked();
            int slot = e.getSlot();

            // Close
            if (slot == 49) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
                return;
            }

            // Media Panel button (bottom-right, only for media players)
            if (slot == 53 && p.hasPermission("naturalsmp.media")) {
                openMediaPanel(p);
                return;
            }

            // Clicked on a player head in content area?
            if (isContentSlot(slot)) {
                ItemStack clicked = e.getCurrentItem();
                if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        UUID targetUUID = meta.getOwningPlayer().getUniqueId();
                        String link = plugin.getMediaManager().getLink(targetUUID);
                        String targetName = meta.getOwningPlayer().getName();

                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        p.sendMessage(ChatUtils.toComponent(
                                "§6§lNaturalSMP §8» §aLink Channel §e" + targetName + "§a:"));

                        if (link.startsWith("http")) {
                            Component clickable = Component.text(link)
                                    .color(NamedTextColor.AQUA)
                                    .decorate(TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.openUrl(link));
                            p.sendMessage(clickable);
                        } else {
                            p.sendMessage(ChatUtils.toComponent("§b" + link));
                        }
                    }
                }
            }
            return;
        }

        // Media Panel (Settings)
        if (e.getInventory().getHolder() instanceof MediaPanelHolder) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;
            Player p = (Player) e.getWhoClicked();

            if (e.getSlot() == 22) {
                // Back → reopen main GUI
                openGUI(p);
            } else if (e.getSlot() == 11) {
                // Change link
                p.closeInventory();
                pendingLinkInput.put(p.getUniqueId(), true);
                p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §eSilakan ketik Link Channel kamu di chat."));
                p.sendMessage(ChatUtils.colorize("§7Ketik 'cancel' atau 'batal' untuk membatalkan."));
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            } else if (e.getSlot() == 15) {
                openBenefitsGUI(p);
            }
            return;
        }

        // Benefits GUI
        if (e.getInventory().getHolder() instanceof BenefitsHolder) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;
            if (e.getSlot() == 22) {
                Player p = (Player) e.getWhoClicked();
                if (p.hasPermission("naturalsmp.media")) {
                    openMediaPanel(p);
                } else {
                    openGUI(p);
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MediaHolder
                || e.getInventory().getHolder() instanceof MediaPanelHolder
                || e.getInventory().getHolder() instanceof BenefitsHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (pendingLinkInput.getOrDefault(p.getUniqueId(), false)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("batal")) {
                pendingLinkInput.remove(p.getUniqueId());
                p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cPengaturan link dibatalkan."));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getMediaManager().setLink(p.getUniqueId(), msg);
                pendingLinkInput.remove(p.getUniqueId());
                p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §aLink berhasil disimpan: §f" + msg));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                openMediaPanel(p);
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        pendingLinkInput.remove(e.getPlayer().getUniqueId());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private boolean isContentSlot(int slot) {
        for (int s : CONTENT_SLOTS) if (s == slot) return true;
        return false;
    }

    // ─── Holders ──────────────────────────────────────────────────────────────

    public static class MediaHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class MediaPanelHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class BenefitsHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
