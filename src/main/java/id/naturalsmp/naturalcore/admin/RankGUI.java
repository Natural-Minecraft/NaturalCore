package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.database.RankPriceDatabase;
import id.naturalsmp.naturalcore.permissions.PermissionManager;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rank Shop GUI - Displays ranks with prices fetched from MySQL.
 * Players can view rank benefits and purchase instructions.
 */
public class RankGUI implements Listener {

    private final NaturalCore plugin;
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");

    // Rank display data (in order)
    private static final String[] RANK_ORDER = { "midi", "vip", "mvp", "nature" };
    private static final int[] RANK_SLOTS = { 10, 12, 14, 16 };
    private static final Material[] RANK_MATERIALS = {
            Material.PINK_DYE, // MIDI
            Material.LIME_DYE, // VIP
            Material.LIGHT_BLUE_DYE, // MVP
            Material.YELLOW_DYE // NATURE
    };
    private static final String[] RANK_COLORS = {
            "<#FF55FF>", // MIDI - Pink
            "<#55FF55>", // VIP - Lime
            "<#55FFFF>", // MVP - Cyan
            "<#FFFF55>" // NATURE - Yellow
    };

    public RankGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        Inventory inv = GUIUtils.createGUI(new RankHolder(), 27,
                ConfigUtils.getMessage("ranks.gui-title"));

        // --- GLASS BORDER ---
        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // --- RANK ITEMS WITH PRICES ---
        RankPriceDatabase priceDb = plugin.getRankPriceDatabase();
        Map<String, PermissionManager.RankConfig> ranks = plugin.getPermissionManager().getRanks();

        for (int i = 0; i < RANK_ORDER.length; i++) {
            String rankId = RANK_ORDER[i];
            double priceRP = priceDb.getPriceRP(rankId);
            double discountedRP = priceDb.getDiscountedPriceRP(rankId);
            int discount = priceDb.getDiscount(rankId);

            PermissionManager.RankConfig rankConfig = ranks.get(rankId);
            List<String> benefits = (rankConfig != null && rankConfig.guiBenefits != null)
                    ? rankConfig.guiBenefits
                    : List.of("&7Akses fitur eksklusif!");

            inv.setItem(RANK_SLOTS[i], createRankItem(
                    RANK_MATERIALS[i],
                    RANK_COLORS[i],
                    rankId.toUpperCase(),
                    priceRP,
                    discountedRP,
                    discount,
                    benefits));
        }

        // Info Item (Center)
        inv.setItem(4, createInfoItem());

        // Close Button
        inv.setItem(22, createItem(Material.BARRIER, "&c&lTUTUP", List.of("&7Klik untuk keluar menu.")));

        p.openInventory(inv);
    }

    private ItemStack createRankItem(Material mat, String color, String rankName, double priceRP,
            double discountedRP, int discount, List<String> benefits) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils
                    .toComponent(color + "<b>✦ " + rankName + " ✦</b>" + color.replace("<", "</").replace(">", ">")));

            List<Component> componentLore = new ArrayList<>();
            componentLore.add(Component.empty());
            componentLore.add(ChatUtils.toComponent("&7Durasi: &e30 Hari &7(Bulanan)"));
            componentLore.add(Component.empty());

            // Benefits
            componentLore.add(ChatUtils.toComponent("&e&lBenefits:"));
            for (String benefit : benefits) {
                componentLore.add(ChatUtils.toComponent("&8• &7" + benefit));
            }

            componentLore.add(Component.empty());
            componentLore.add(ChatUtils.toComponent("&e&lHarga:"));
            if (discount > 0) {
                componentLore.add(
                        ChatUtils.toComponent("&8• &7&mRp " + priceFormat.format(priceRP) + "&r &c-" + discount + "%"));
                componentLore.add(ChatUtils.toComponent("&8• &a&lRp " + priceFormat.format(discountedRP)));
            } else {
                componentLore.add(ChatUtils.toComponent("&8• &fRp " + priceFormat.format(priceRP)));
            }
            componentLore.add(Component.empty());
            componentLore.add(ChatUtils.toComponent("&aKlik untuk info pembelian!"));

            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent("<gradient:#00AAFF:#00FF00><b>RANK SHOP</b></gradient>"));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("&7Beli rank untuk akses"));
            lore.add(ChatUtils.toComponent("&7fitur eksklusif!"));
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("&e• Semua rank berlaku &f30 Hari"));
            lore.add(ChatUtils.toComponent("&e• Perpanjang otomatis via website"));
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("&b→ store.naturalsmp.id"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> componentLore = new ArrayList<>();
            for (String s : lore)
                componentLore.add(ChatUtils.toComponent(s));
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof RankHolder))
            return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == 22) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
        } else if (isRankSlot(slot)) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            p.sendMessage(ChatUtils.toComponent(
                    "&6&lNaturalSMP &8» &7Kunjungi &estore.naturalsmp.id &7untuk pembelian rank!"));
            p.closeInventory();
        }
    }

    private boolean isRankSlot(int slot) {
        for (int s : RANK_SLOTS) {
            if (s == slot)
                return true;
        }
        return false;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof RankHolder)
            e.setCancelled(true);
    }

    public static class RankHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
