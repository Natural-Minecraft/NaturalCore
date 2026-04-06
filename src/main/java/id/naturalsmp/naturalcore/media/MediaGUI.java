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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MediaGUI implements Listener {

    private final NaturalCore plugin;
    private final Map<UUID, Boolean> pendingLinkInput = new HashMap<>();

    public MediaGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        boolean isMedia = p.hasPermission("naturalsmp.media");

        Inventory inv = GUIUtils.createGUI(new MediaHolder(), 27, isMedia ? "§8Media Panel" : "§8Media Info");

        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        if (isMedia) {
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
                            "§7keuntungan yang kamu miliki sebagai Media."
                    ));
            inv.setItem(15, benefits);
        } else {
            // Apply Item
            ItemStack apply = createItem(Material.PAPER, "§a§lDaftar Menjadi Media",
                    List.of(
                            "§7Ingin menjadi bagian dari tim Media kami?",
                            "§7Kamu akan mendapatkan banyak keuntungan!",
                            "",
                            "§aKlik Kiri untuk mendapatkan link daftar."
                    ));
            inv.setItem(11, apply);

            // Benefits Item
            ItemStack benefits = createItem(Material.DIAMOND, "§b§lPreview Keuntungan",
                    List.of(
                            "§7Lihat apa saja yang didapatkan",
                            "§7jika kamu adalah seorang Media."
                    ));
            inv.setItem(15, benefits);
        }

        // Close
        inv.setItem(22, createItem(Material.BARRIER, "§c§lTUTUP", List.of("§7Klik untuk menutup menu.")));

        p.openInventory(inv);
    }

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

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof MediaHolder) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;
            Player p = (Player) e.getWhoClicked();
            if (e.getSlot() == 22) {
                p.closeInventory();
            } else if (e.getSlot() == 11) {
                boolean isMedia = p.hasPermission("naturalsmp.media");
                if (isMedia) {
                    p.closeInventory();
                    pendingLinkInput.put(p.getUniqueId(), true);
                    p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §eSilakan ketik Link Channel kamu di chat."));
                    p.sendMessage(ChatUtils.colorize("§7Ketik 'cancel' atau 'batal' untuk membatalkan."));
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                } else {
                    p.closeInventory();
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                    // Send clickable URL
                    p.sendMessage(ChatUtils.toComponent("§6§lNaturalSMP §8» §aKlik link ini untuk mendaftar menjadi kreator:"));
                    String url = "https://www.naturalsmp.net/#team";
                    Component clickable = Component.text(url)
                            .color(NamedTextColor.AQUA)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url));
                    p.sendMessage(clickable);
                }
            } else if (e.getSlot() == 15) {
                openBenefitsGUI(p);
            }
        } else if (e.getInventory().getHolder() instanceof BenefitsHolder) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;
            if (e.getSlot() == 22) {
                openGUI((Player) e.getWhoClicked());
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MediaHolder || e.getInventory().getHolder() instanceof BenefitsHolder) {
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
                openGUI(p);
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        pendingLinkInput.remove(e.getPlayer().getUniqueId());
    }

    public static class MediaHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class BenefitsHolder implements InventoryHolder {
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
