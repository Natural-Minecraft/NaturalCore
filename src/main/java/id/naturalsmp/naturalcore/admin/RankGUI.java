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
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

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
            double priceNC = priceDb.getPriceNC(rankId);
            double discountedRP = priceDb.getDiscountedPriceRP(rankId);
            double discountedNC = priceDb.getDiscountedPriceNC(rankId);
            int discount = priceDb.getDiscount(rankId);

            PermissionManager.RankConfig rankConfig = ranks.get(rankId);
            List<String> benefits = (rankConfig != null && rankConfig.guiBenefits != null)
                    ? rankConfig.guiBenefits
                    : List.of("&7Akses fitur eksklusif!");

            boolean isOwned = p.hasPermission("group." + rankId);

            inv.setItem(RANK_SLOTS[i], createRankItem(
                    p,
                    RANK_MATERIALS[i],
                    RANK_COLORS[i],
                    rankId.toUpperCase(),
                    priceRP,
                    priceNC,
                    discountedRP,
                    discountedNC,
                    discount,
                    benefits,
                    isOwned));
        }

        // Info Item (Center)
        inv.setItem(4, createInfoItem());

        // Close Button
        inv.setItem(22, createItem(Material.BARRIER, "&c&lTUTUP", List.of("&7Klik untuk keluar menu.")));

        p.openInventory(inv);
    }

    private ItemStack createRankItem(Player p, Material mat, String color, String rankName, double priceRP, double priceNC,
            double discountedRP, double discountedNC, int discount, List<String> benefits, boolean isOwned) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayNameRaw = color + "<b>✦ " + rankName + " ✦</b>" + color.replace("<", "</").replace(">", ">");
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                displayNameRaw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, displayNameRaw);
            }
            meta.displayName(ChatUtils.toComponent(displayNameRaw));

            List<Component> componentLore = new ArrayList<>();
            componentLore.add(Component.empty());
            componentLore.add(ChatUtils.toComponent("&7Durasi: &e30 Hari &7(Bulanan)"));
            componentLore.add(Component.empty());

            // Benefits
            componentLore.add(ChatUtils.toComponent("&e&lBenefits:"));
            for (String benefit : benefits) {
                String parsedBenefit = "&8• &7" + benefit;
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    parsedBenefit = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, parsedBenefit);
                }
                componentLore.add(ChatUtils.toComponent(parsedBenefit));
            }

            componentLore.add(Component.empty());

            if (isOwned) {
                componentLore.add(ChatUtils.toComponent("&6&lSUDAH DIMILIKI"));
                componentLore.add(ChatUtils.toComponent("&7Kamu sudah memiliki rank ini."));
            } else {
                componentLore.add(ChatUtils.toComponent("&e&lHarga:"));
                if (discount > 0) {
                    componentLore.add(
                            ChatUtils.toComponent(
                                    "&8• &7&mRp " + priceFormat.format(priceRP) + "&r &c-" + discount + "%"));
                    componentLore.add(ChatUtils.toComponent("&8• &a&lRp " + priceFormat.format(discountedRP)));
                    componentLore.add(ChatUtils.toComponent("&8• &7&m" + priceFormat.format(priceNC) + " NC"));
                    componentLore.add(ChatUtils.toComponent("&8• &6&l" + priceFormat.format(discountedNC) + " NC"));
                } else {
                    componentLore.add(ChatUtils.toComponent("&8• &fRp " + priceFormat.format(priceRP)));
                    componentLore.add(ChatUtils.toComponent("&8• &6" + priceFormat.format(priceNC) + " NC"));
                }
                componentLore.add(Component.empty());
                componentLore.add(ChatUtils.toComponent("&aKlik untuk membeli dengan NaturalCoin!"));
                componentLore.add(ChatUtils.toComponent("&7(Atau kunjungi store untuk Rupiah)"));
            }

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
            lore.add(ChatUtils.toComponent("&b→ store.naturalsmp.net"));

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
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == 22) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
        } else if (isRankSlot(slot)) {
            String rankId = RANK_ORDER[getRankIndex(slot)];
            if (p.hasPermission("group." + rankId)) {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                p.sendMessage(ChatUtils.colorize("&6&lNaturalSMP &8» &cKamu sudah memiliki rank ini!"));
                return;
            }
            openConfirmationGUI(p, rankId);
        }
    }

    private int getRankIndex(int slot) {
        for (int i = 0; i < RANK_SLOTS.length; i++) {
            if (RANK_SLOTS[i] == slot)
                return i;
        }
        return -1;
    }

    private void openConfirmationGUI(Player p, String rankId) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        Inventory inv = GUIUtils.createGUI(new ConfirmationHolder(rankId), 27, "&8Konfirmasi Pembelian");

        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++)
            inv.setItem(i, glass);

        double price = plugin.getRankPriceDatabase().getDiscountedPriceNC(rankId);
        String color = RANK_COLORS[getRankIndexById(rankId)];

        ItemStack info = createItem(RANK_MATERIALS[getRankIndexById(rankId)],
                color + "<b>" + rankId.toUpperCase() + " RANK</b>",
                List.of("&7Apakah kamu yakin ingin membeli",
                        "&7rank ini seharga &6" + priceFormat.format(price) + " NC&7?",
                        "",
                        "&8• &7Durasi: &e30 Hari"));
        inv.setItem(13, info);

        inv.setItem(11, createItem(Material.EMERALD_BLOCK, "&a&lKONFIRMASI", List.of("&7Klik untuk membeli rank.")));
        inv.setItem(15, createItem(Material.REDSTONE_BLOCK, "&c&lBATAL", List.of("&7Klik untuk membatalkan.")));

        p.openInventory(inv);
    }

    private int getRankIndexById(String rankId) {
        for (int i = 0; i < RANK_ORDER.length; i++) {
            if (RANK_ORDER[i].equalsIgnoreCase(rankId))
                return i;
        }
        return 0;
    }

    @EventHandler
    public void onConfirmClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ConfirmationHolder holder))
            return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;

        if (e.getCurrentItem() == null)
            return;
        Player p = (Player) e.getWhoClicked();
        String rankId = holder.getRankId();

        if (e.getSlot() == 11) {
            // Confirm
            processPurchase(p, rankId);
        } else if (e.getSlot() == 15) {
            // Cancel
            openGUI(p);
        }
    }

    private void processPurchase(Player p, String rankId) {
        double price = plugin.getRankPriceDatabase().getDiscountedPriceNC(rankId);

        // Use CoinsEngineAPI to check balance and deduct
        double balance = CoinsEngineAPI.getBalance(p.getUniqueId(),
                CoinsEngineAPI.getCurrency("naturalcoin"));

        if (balance < price) {
            p.sendMessage(ChatUtils.colorize("&6&lNaturalSMP &8» &cSaldo NaturalCoin tidak cukup!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            p.closeInventory();
            return;
        }

        // Deduct
        CoinsEngineAPI.removeBalance(p.getUniqueId(),
                CoinsEngineAPI.getCurrency("naturalcoin"), price);

        // Grant Rank
        String txId = "IG-" + (System.currentTimeMillis() / 1000);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "topupnotification " + p.getName() + " " + rankId + " " + txId);

        p.sendMessage(ChatUtils.colorize("&6&lNaturalSMP &8» &aPembelian berhasil!"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        p.closeInventory();
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

    public static class ConfirmationHolder implements InventoryHolder {
        private final String rankId;

        public ConfirmationHolder(String rankId) {
            this.rankId = rankId;
        }

        public String getRankId() {
            return rankId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
