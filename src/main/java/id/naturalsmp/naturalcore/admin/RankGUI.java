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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rank Shop GUI - 54-slot layout, ALL benefits shown, PlayerHead info, kit preview.
 * Left click = buy (NC), Right click = /kits preview <rank>
 */
public class RankGUI implements Listener {

    private final NaturalCore plugin;
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");

    // All 12 donator ranks in order
    private static final String[] ALL_RANKS = {
            "midi", "vip", "vip_plus", "mvp", "mvp_plus", "gold",
            "gold_plus", "nature", "nature_plus", "nature_plus_plus", "cakrawala", "investor"
    };

    // Kit name mapping (for /kits preview <name>)
    private static final Map<String, String> KIT_NAME_MAP = new java.util.HashMap<>() {{
        put("midi",             "MIDI");
        put("vip",              "VIP");
        put("vip_plus",         "VIPPLUS");
        put("mvp",              "MVP");
        put("mvp_plus",         "MVPPLUS");
        put("gold",             "GOLD");
        put("gold_plus",        "GOLDPLUS");
        put("nature",           "NATURE");
        put("nature_plus",      "NATUREPLUS");
        put("nature_plus_plus", "NATUREPLUSPLUS");
        put("cakrawala",        "CAKRAWALA");
        put("investor",         "INVESTOR");
    }};

    // Default materials fallback per rank
    private static final Map<String, Material> RANK_MATERIAL_MAP = new java.util.HashMap<>() {{
        put("midi",             Material.LAPIS_LAZULI);
        put("vip",              Material.IRON_INGOT);
        put("vip_plus",         Material.IRON_BLOCK);
        put("mvp",              Material.GOLD_INGOT);
        put("mvp_plus",         Material.GOLD_BLOCK);
        put("gold",             Material.RAW_GOLD);
        put("gold_plus",        Material.RAW_GOLD_BLOCK);
        put("nature",           Material.DIAMOND);
        put("nature_plus",      Material.DIAMOND_BLOCK);
        put("nature_plus_plus", Material.NETHERITE_SCRAP);
        put("cakrawala",        Material.NETHERITE_INGOT);
        put("investor",         Material.NETHERITE_BLOCK);
    }};

    // Rank color codes (MiniMessage)
    private static final Map<String, String> RANK_COLOR_MAP = new java.util.HashMap<>() {{
        put("midi",             "<#FF88FF>");
        put("vip",              "<#55FF55>");
        put("vip_plus",         "<#88FF88>");
        put("mvp",              "<#55FFFF>");
        put("mvp_plus",         "<#33DDDD>");
        put("gold",             "<#FFAA00>");
        put("gold_plus",        "<#FFCC44>");
        put("nature",           "<#FFFF55>");
        put("nature_plus",      "<#FFFF88>");
        put("nature_plus_plus", "<#AAFFAA>");
        put("cakrawala",        "<#FF5555>");
        put("investor",         "<#AA2200>");
    }};

    // 4x3 Grid slots (54-slot inventory, rows 1-4 middle columns)
    private static final int[] RANK_ITEM_SLOTS = { 10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34 };

    public RankGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // ─── Open Main GUI ────────────────────────────────────────────────────────

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);

        String title = ConfigUtils.getMessage("ranks.gui-title") != null ?
                       ConfigUtils.getMessage("ranks.gui-title") : "§8Rank Shop";
        Inventory inv = GUIUtils.createGUI(new RankHolder(), 54, title);

        // Glass border fill
        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        RankPriceDatabase priceDb = plugin.getRankPriceDatabase();
        Map<String, PermissionManager.RankConfig> ranks = plugin.getPermissionManager().getRanks();

        for (int i = 0; i < ALL_RANKS.length; i++) {
            String rankId = ALL_RANKS[i];

            double priceRP = priceDb.getPriceRP(rankId);
            double priceNC = priceDb.getPriceNC(rankId);
            double discountedRP = priceDb.getDiscountedPriceRP(rankId);
            double discountedNC = priceDb.getDiscountedPriceNC(rankId);
            int discount = priceDb.getDiscount(rankId);

            PermissionManager.RankConfig rankConfig = ranks.get(rankId);
            List<String> benefits = (rankConfig != null && rankConfig.guiBenefits != null && !rankConfig.guiBenefits.isEmpty())
                    ? rankConfig.guiBenefits
                    : List.of("§7Akses fitur eksklusif!");

            Material mat = getMaterial(rankId, rankConfig);
            String color = RANK_COLOR_MAP.getOrDefault(rankId, "<white>");
            String displayId = rankId.toUpperCase().replace("_PLUS_PLUS", "++").replace("_PLUS", "+");
            boolean isOwned = p.hasPermission("group." + rankId);

            inv.setItem(RANK_ITEM_SLOTS[i], createRankItem(
                    p, mat, color, displayId, rankId,
                    priceRP, priceNC, discountedRP, discountedNC, discount,
                    benefits, isOwned));
        }

        // Slot 4: Player Head (rank info + all benefits)
        inv.setItem(4, createPlayerHeadItem(p, ranks));

        // Slot 40: Info item
        inv.setItem(40, createInfoItem());

        // Close button
        inv.setItem(49, createItem(Material.BARRIER, "§c§lTUTUP", List.of("§7Klik untuk keluar menu.")));

        p.openInventory(inv);
    }

    // ─── Player Head Item ─────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private ItemStack createPlayerHeadItem(Player p, Map<String, PermissionManager.RankConfig> ranks) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        meta.setOwningPlayer(p);

        // Find player's highest rank
        String playerRankId = "member";
        String playerRankDisplay = "§7Member";
        for (int i = ALL_RANKS.length - 1; i >= 0; i--) {
            if (p.hasPermission("group." + ALL_RANKS[i])) {
                playerRankId = ALL_RANKS[i];
                PermissionManager.RankConfig rc = ranks.get(playerRankId);
                playerRankDisplay = rc != null ? rc.displayName : ALL_RANKS[i].toUpperCase();
                break;
            }
        }

        // Title: [Rank Prefix] PlayerName
        String headTitle = playerRankDisplay + " §f" + p.getName();
        meta.displayName(ChatUtils.toComponent(headTitle));

        // Lore: show all benefits of player's current rank
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(ChatUtils.toComponent("§e§lRank Aktifmu: §r" + playerRankDisplay));
        lore.add(Component.empty());

        PermissionManager.RankConfig currentRankCfg = ranks.get(playerRankId);
        if (currentRankCfg != null && currentRankCfg.guiBenefits != null && !currentRankCfg.guiBenefits.isEmpty()) {
            lore.add(ChatUtils.toComponent("§6§lSemua Benefits Rankmu:"));
            for (String benefit : currentRankCfg.guiBenefits) {
                String parsed = benefit;
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    parsed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, parsed);
                }
                lore.add(ChatUtils.toComponent("  " + parsed));
            }
        } else {
            lore.add(ChatUtils.toComponent("§7Kamu belum memiliki rank premium."));
            lore.add(ChatUtils.toComponent("§7Beli rank untuk akses fitur eksklusif!"));
        }

        lore.add(Component.empty());
        lore.add(ChatUtils.toComponent("§b→ store.naturalsmp.net"));

        meta.lore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    // ─── Rank Item ────────────────────────────────────────────────────────────

    private ItemStack createRankItem(Player p, Material mat, String color, String displayId, String rankId,
            double priceRP, double priceNC, double discountedRP, double discountedNC,
            int discount, List<String> benefits, boolean isOwned) {

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String nameRaw = color + "<b>✦ " + displayId + " ✦</b>";
        meta.displayName(ChatUtils.toComponent(nameRaw));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(ChatUtils.toComponent("§7Durasi: §e30 Hari §7(Bulanan)"));
        lore.add(Component.empty());
        lore.add(ChatUtils.toComponent("§e§lBenefits:"));

        // Show ALL benefits (no cap)
        for (String benefit : benefits) {
            String parsed = benefit;
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                parsed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, parsed);
            }
            lore.add(ChatUtils.toComponent(parsed));
        }

        lore.add(Component.empty());

        if (isOwned) {
            lore.add(ChatUtils.toComponent("§a§lSUDAH DIMILIKI ✔"));
            lore.add(ChatUtils.toComponent("§7Rank ini sudah aktif di akunmu."));
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("§e[➡] Klik Kanan: §fPreview Uang Kit Rank"));
        } else {
            lore.add(ChatUtils.toComponent("§e§lHarga:"));
            if (discount > 0) {
                lore.add(ChatUtils.toComponent("§8• §7§m" + priceFormat.format(priceNC) + " NC§r §c-" + discount + "%"));
                lore.add(ChatUtils.toComponent("§8• §6§l" + priceFormat.format(discountedNC) + " NC"));
            } else {
                lore.add(ChatUtils.toComponent("§8• §6" + priceFormat.format(priceNC) + " NC"));
            }
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("§a[⬅] Klik Kiri: §fBeli pakai NC"));
            lore.add(ChatUtils.toComponent("§e[➡] Klik Kanan: §fPreview Uang Kit Rank"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ─── Info Item ────────────────────────────────────────────────────────────

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent("<gradient:#00AAFF:#FFFF00><b>RANK SHOP</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("§7Beli rank untuk akses"));
            lore.add(ChatUtils.toComponent("§7fitur eksklusif!"));
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("§e• Semua rank berlaku §f30 Hari"));
            lore.add(ChatUtils.toComponent("§e• §f12 Rank Tersedia"));
            lore.add(ChatUtils.toComponent("§e• Perpanjang otomatis via website"));
            lore.add(Component.empty());
            lore.add(ChatUtils.toComponent("§b→ store.naturalsmp.net"));
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
            for (String s : lore) componentLore.add(ChatUtils.toComponent(s));
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Material getMaterial(String rankId, PermissionManager.RankConfig rankConfig) {
        if (rankConfig != null && rankConfig.guiItem != null) {
            try {
                return Material.valueOf(rankConfig.guiItem.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return RANK_MATERIAL_MAP.getOrDefault(rankId, Material.PAPER);
    }

    // ─── Click Handlers ────────────────────────────────────────────────────────

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof RankHolder)) return;
        e.setCancelled(true);

        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == 49) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
            return;
        }

        if (!isRankSlot(slot)) return;

        int rankArrayIdx = getRankSlotIndex(slot);
        if (rankArrayIdx < 0 || rankArrayIdx >= ALL_RANKS.length) return;

        String rankId = ALL_RANKS[rankArrayIdx];

        if (e.getClick() == ClickType.RIGHT) {
            // Right click → /kits preview <KitName>
            String kitName = KIT_NAME_MAP.getOrDefault(rankId, rankId.toUpperCase());
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            p.performCommand("kits preview " + kitName);
            return;
        }

        // Left click
        if (p.hasPermission("group." + rankId)) {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cKamu sudah memiliki rank ini! Klik Kanan untuk preview kit."));
            return;
        }
        openConfirmationGUI(p, rankId);
    }

    private boolean isRankSlot(int slot) {
        for (int s : RANK_ITEM_SLOTS) if (s == slot) return true;
        return false;
    }

    private int getRankSlotIndex(int slot) {
        for (int i = 0; i < RANK_ITEM_SLOTS.length; i++) {
            if (RANK_ITEM_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    // ─── Confirmation GUI ──────────────────────────────────────────────────────

    private void openConfirmationGUI(Player p, String rankId) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        Inventory inv = GUIUtils.createGUI(new ConfirmationHolder(rankId), 27, "§8Konfirmasi Pembelian");

        ItemStack glass = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        double price = plugin.getRankPriceDatabase().getDiscountedPriceNC(rankId);
        String displayId = rankId.toUpperCase().replace("_PLUS_PLUS", "++").replace("_PLUS", "+");
        String color = RANK_COLOR_MAP.getOrDefault(rankId, "<white>");

        PermissionManager.RankConfig rankConfig = plugin.getPermissionManager().getRanks().get(rankId);
        Material mat = getMaterial(rankId, rankConfig);

        ItemStack info = createItem(mat,
                color + "<b>" + displayId + " RANK</b>",
                List.of("§7Apakah kamu yakin ingin membeli",
                        "§7rank ini seharga §6" + priceFormat.format(price) + " NC§7?",
                        "",
                        "§8• §7Durasi: §e30 Hari"));
        inv.setItem(13, info);

        inv.setItem(11, createItem(Material.EMERALD_BLOCK, "§a§lKONFIRMASI", List.of("§7Klik untuk membeli rank.")));
        inv.setItem(15, createItem(Material.REDSTONE_BLOCK, "§c§lBATAL", List.of("§7Klik untuk membatalkan.")));

        p.openInventory(inv);
    }

    @EventHandler
    public void onConfirmClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof ConfirmationHolder holder)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (e.getCurrentItem() == null) return;

        Player p = (Player) e.getWhoClicked();
        String rankId = holder.getRankId();

        if (e.getSlot() == 11) {
            processPurchase(p, rankId);
        } else if (e.getSlot() == 15) {
            openGUI(p);
        }
    }

    private void processPurchase(Player p, String rankId) {
        double price = plugin.getRankPriceDatabase().getDiscountedPriceNC(rankId);
        double balance = CoinsEngineAPI.getBalance(p.getUniqueId(),
                CoinsEngineAPI.getCurrency("naturalcoin"));

        if (balance < price) {
            p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §cSaldo NaturalCoin tidak cukup!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            p.closeInventory();
            return;
        }

        CoinsEngineAPI.removeBalance(p.getUniqueId(),
                CoinsEngineAPI.getCurrency("naturalcoin"), price);

        String txId = "IG-" + (System.currentTimeMillis() / 1000);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "topupnotification " + p.getName() + " " + rankId + " " + txId);

        p.sendMessage(ChatUtils.colorize("§6§lNaturalSMP §8» §aPembelian berhasil!"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        p.closeInventory();
    }

    // ─── Drag Cancel ──────────────────────────────────────────────────────────

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof RankHolder
            || e.getInventory().getHolder() instanceof ConfirmationHolder)
            e.setCancelled(true);
    }

    // ─── Holders ──────────────────────────────────────────────────────────────

    public static class RankHolder implements InventoryHolder {
        public RankHolder() {}
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class ConfirmationHolder implements InventoryHolder {
        private final String rankId;
        public ConfirmationHolder(String rankId) { this.rankId = rankId; }
        public String getRankId() { return rankId; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
