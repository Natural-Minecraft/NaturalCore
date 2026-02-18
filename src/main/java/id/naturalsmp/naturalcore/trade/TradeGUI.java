package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * TradeGUI - Mengelola tampilan dan interaksi GUI trade.
 * Menggunakan SHARED inventory agar kedua player melihat hal yang sama.
 *
 * Layout (54 slots / 6 rows):
 * Row 0-3: [P1 items (4 cols)] [SEPARATOR] [P2 items (4 cols)]
 * Row 4: [border][MONEY1][LOCK1][border][INFO][border][LOCK2][MONEY2][border]
 * Row 5:
 * [border][CONFIRM1][TRUST1][border][COUNTDOWN][border][TRUST2][CONFIRM2][border]
 */
public class TradeGUI implements Listener {

    private final NaturalCore plugin;

    public TradeGUI(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Membuat shared inventory dan membukanya untuk kedua player.
     */
    public void openTradeGUI(TradeSession session) {
        TradeHolder holder = new TradeHolder(session);
        Inventory inv = GUIUtils.createGUI(holder, 54, "&#6CCAFE❂ &#FFFFFFɴᴀᴛᴜʀᴀʟ ᴛʀᴀᴅᴇ &#6CCAFE❂");
        holder.setInventory(inv);
        session.setSharedInventory(inv);

        renderGUI(inv, session);

        session.getPlayer1().openInventory(inv);
        session.getPlayer2().openInventory(inv);
    }

    /**
     * Re-open GUI untuk satu player (setelah money input).
     */
    public void reopenForPlayer(Player p, TradeSession session) {
        Inventory inv = session.getSharedInventory();
        if (inv == null)
            return;
        renderGUI(inv, session);
        p.openInventory(inv);
    }

    /**
     * Render seluruh GUI border, separator, dan tombol-tombol.
     * Item player di slot mereka TIDAK di-clear (hanya non-player slots).
     */
    private void renderGUI(Inventory inv, TradeSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        // --- Separator column (kolom tengah) ---
        ItemStack separator = createDecor(Material.GRAY_STAINED_GLASS_PANE, "&#555555│");
        for (int s : TradeSession.SEPARATOR_SLOTS) {
            inv.setItem(s, separator);
        }

        // --- Row 4: Tools row ---
        ItemStack border = createDecor(Material.BLACK_STAINED_GLASS_PANE, " ");

        // Fill all row 4 & 5 with borders first
        for (int i = 36; i < 54; i++) {
            inv.setItem(i, border);
        }
        // row 4 separator
        inv.setItem(40, separator);
        // row 5 separator
        inv.setItem(45, border);
        inv.setItem(48, separator);
        inv.setItem(53, border);

        // --- Money buttons ---
        inv.setItem(TradeSession.SLOT_P1_MONEY, createMoneyButton(p1, session));
        inv.setItem(TradeSession.SLOT_P2_MONEY, createMoneyButton(p2, session));

        // --- Lock buttons ---
        inv.setItem(TradeSession.SLOT_P1_LOCK, createLockButton(p1, session));
        inv.setItem(TradeSession.SLOT_P2_LOCK, createLockButton(p2, session));

        // --- Info book ---
        inv.setItem(TradeSession.SLOT_INFO, createInfoBook());

        // --- Confirm buttons ---
        inv.setItem(TradeSession.SLOT_P1_CONFIRM, createConfirmButton(p1, session));
        inv.setItem(TradeSession.SLOT_P2_CONFIRM, createConfirmButton(p2, session));

        // --- Trust display ---
        inv.setItem(TradeSession.SLOT_P1_TRUST, createTrustHead(p1));
        inv.setItem(TradeSession.SLOT_P2_TRUST, createTrustHead(p2));

        // --- Countdown indicator ---
        inv.setItem(TradeSession.SLOT_COUNTDOWN, createCountdownItem(session));
    }

    /**
     * Update hanya tombol-tombol (tanpa clear item player).
     */
    public void updateButtons(TradeSession session) {
        Inventory inv = session.getSharedInventory();
        if (inv == null)
            return;

        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();

        inv.setItem(TradeSession.SLOT_P1_MONEY, createMoneyButton(p1, session));
        inv.setItem(TradeSession.SLOT_P2_MONEY, createMoneyButton(p2, session));
        inv.setItem(TradeSession.SLOT_P1_LOCK, createLockButton(p1, session));
        inv.setItem(TradeSession.SLOT_P2_LOCK, createLockButton(p2, session));
        inv.setItem(TradeSession.SLOT_P1_CONFIRM, createConfirmButton(p1, session));
        inv.setItem(TradeSession.SLOT_P2_CONFIRM, createConfirmButton(p2, session));
        inv.setItem(TradeSession.SLOT_COUNTDOWN, createCountdownItem(session));
    }

    // ==================== EVENT HANDLERS ====================

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TradeHolder holder))
            return;
        TradeSession session = holder.getSession();
        Player player = (Player) e.getWhoClicked();

        int rawSlot = e.getRawSlot();

        // Click di player inventory (bawah) — izinkan shift-click ke trade GUI
        if (rawSlot >= 54) {
            // Shift-click dari player inventory ke trade GUI
            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                // Cari slot kosong di sisi player
                int[] mySlots = session.getMySlots(player);
                Inventory inv = session.getSharedInventory();

                // Jika item locked, cancel
                if (session.isLocked(player)) {
                    e.setCancelled(true);
                    player.sendMessage(
                            ChatUtils.colorize("&#FF5555Item kamu sudah di-lock! Unlock dulu untuk mengubah."));
                    return;
                }

                // Cari slot kosong & taruh manual
                ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR)
                    return;

                e.setCancelled(true); // Cancel default shift-click behavior

                for (int slot : mySlots) {
                    ItemStack existing = inv.getItem(slot);
                    if (existing == null || existing.getType() == Material.AIR) {
                        inv.setItem(slot, clickedItem.clone());
                        e.setCurrentItem(null);
                        onItemChanged(session, player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);
                        return;
                    }
                }
                player.sendMessage(ChatUtils.colorize("&#FF5555Slot trade kamu sudah penuh!"));
            }
            return;
        }

        // Click di trade GUI —
        e.setCancelled(true); // Default cancel semua di GUI

        // --- Button clicks ---
        if (rawSlot == TradeSession.SLOT_P1_CONFIRM || rawSlot == TradeSession.SLOT_P2_CONFIRM) {
            handleConfirmClick(player, session, rawSlot);
            return;
        }
        if (rawSlot == TradeSession.SLOT_P1_MONEY || rawSlot == TradeSession.SLOT_P2_MONEY) {
            handleMoneyClick(player, session, rawSlot);
            return;
        }
        if (rawSlot == TradeSession.SLOT_P1_LOCK || rawSlot == TradeSession.SLOT_P2_LOCK) {
            handleLockClick(player, session, rawSlot);
            return;
        }

        // --- Item slot interaction ---
        if (session.isMySlot(player, rawSlot)) {
            // Player klik di SISI SENDIRI — izinkan ambil/taruh item
            if (session.isLocked(player)) {
                player.sendMessage(ChatUtils.colorize("&#FF5555Item kamu sudah di-lock! Unlock dulu untuk mengubah."));
                return;
            }
            e.setCancelled(false); // Izinkan interaksi
            // Reset confirmations setelah perubahan
            Bukkit.getScheduler().runTask(plugin, () -> onItemChanged(session, player));
        }
        // Klik di sisi LAWAN — selalu cancel (sudah cancelled di atas)
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof TradeHolder holder))
            return;
        TradeSession session = holder.getSession();
        Player player = (Player) e.getWhoClicked();

        // Cancel drag jika ada slot di luar area player
        for (int slot : e.getRawSlots()) {
            if (slot < 54 && !session.isMySlot(player, slot)) {
                e.setCancelled(true);
                return;
            }
        }

        if (session.isLocked(player)) {
            e.setCancelled(true);
            return;
        }

        // Drag valid — reset confirmations
        Bukkit.getScheduler().runTask(plugin, () -> onItemChanged(session, player));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof TradeHolder holder))
            return;
        TradeSession session = holder.getSession();
        Player closer = (Player) e.getPlayer();

        // Jika trade sudah selesai (dihapus dari manager), skip
        if (plugin.getTradeManager().getSession(closer) == null)
            return;

        // Jika player sedang input money, jangan cancel trade
        if (session.isInputtingMoney(closer))
            return;

        Player other = session.getOther(closer);

        // Cancel countdown jika ada
        session.cancelCountdown();

        // End trade di manager SEBELUM close inventory lawan (cegah recursion)
        plugin.getTradeManager().endTrade(session);

        // Return items ke masing-masing pemilik
        returnItems(session.getSharedInventory(), TradeSession.P1_SLOTS, session.getPlayer1());
        returnItems(session.getSharedInventory(), TradeSession.P2_SLOTS, session.getPlayer2());

        // Notify
        closer.sendMessage(ChatUtils.colorize("&#FF5555Trade dibatalkan."));
        other.sendMessage(
                ChatUtils.colorize("&#FF5555Trade dibatalkan oleh &#FFFFFF" + closer.getName() + "&#FF5555."));

        // Close other player's inventory
        if (other.getOpenInventory().getTopInventory().getHolder() instanceof TradeHolder) {
            other.closeInventory();
        }
    }

    // ==================== BUTTON HANDLERS ====================

    private void handleConfirmClick(Player player, TradeSession session, int slot) {
        // Hanya izinkan klik tombol SENDIRI
        boolean isP1Slot = (slot == TradeSession.SLOT_P1_CONFIRM);
        boolean isP1 = session.isPlayer1(player);

        if (isP1Slot != isP1)
            return; // Klik tombol lawan — ignore

        boolean newState = !session.isConfirmed(player);
        session.setConfirmed(player, newState);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, newState ? 2f : 0.5f);
        updateButtons(session);

        if (session.bothConfirmed()) {
            startCountdown(session);
        } else {
            // Jika ada yang un-confirm saat countdown, cancel
            if (session.isCountdownActive()) {
                session.cancelCountdown();
                updateButtons(session);
                sendBoth(session, "&#FFAA00Countdown dibatalkan! Kedua player harus konfirmasi ulang.");
            }
        }
    }

    private void handleMoneyClick(Player player, TradeSession session, int slot) {
        // Hanya izinkan klik tombol SENDIRI
        boolean isP1Slot = (slot == TradeSession.SLOT_P1_MONEY);
        boolean isP1 = session.isPlayer1(player);

        if (isP1Slot != isP1)
            return;

        // Start money input via TradeManager
        plugin.getTradeManager().startMoneyInput(player, session);
    }

    private void handleLockClick(Player player, TradeSession session, int slot) {
        boolean isP1Slot = (slot == TradeSession.SLOT_P1_LOCK);
        boolean isP1 = session.isPlayer1(player);

        if (isP1Slot != isP1)
            return;

        boolean newState = !session.isLocked(player);
        session.setLocked(player, newState);

        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 0.6f, newState ? 1.5f : 0.7f);

        if (!newState) {
            // Unlock → reset confirmations
            session.resetBothConfirmations();
            if (session.isCountdownActive()) {
                session.cancelCountdown();
                sendBoth(session, "&#FFAA00Countdown dibatalkan! " + player.getName() + " membuka lock item.");
            }
        }

        updateButtons(session);
    }

    // ==================== COUNTDOWN SYSTEM ====================

    private void startCountdown(TradeSession session) {
        session.setCountdownActive(true);
        session.setCountdownTicks(5);
        updateButtons(session);

        sendBoth(session, "&#55FF55Trade akan selesai dalam &#FFFFFF5 &#55FF55detik...");

        session.setCountdownTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int ticks = session.getCountdownTicks() - 1;
            session.setCountdownTicks(ticks);

            if (ticks <= 0) {
                // Complete!
                session.cancelCountdown();
                completeTrade(session);
                return;
            }

            // Update visual
            updateButtons(session);

            // Sound tick
            Player p1 = session.getPlayer1();
            Player p2 = session.getPlayer2();
            float pitch = 0.5f + ((5 - ticks) * 0.3f);
            p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, pitch);
            p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, pitch);
        }, 20L, 20L)); // 1 detik interval
    }

    // ==================== TRADE COMPLETION ====================

    private void completeTrade(TradeSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        Inventory inv = session.getSharedInventory();

        // 1. End trade di manager DULU (prevent onClose handler)
        plugin.getTradeManager().endTrade(session);

        // 2. Swap Money (Vault)
        if (plugin.getVaultManager().getEconomy() != null) {
            double m1 = session.getMoney(p1);
            double m2 = session.getMoney(p2);

            if (m1 > 0) {
                plugin.getVaultManager().getEconomy().withdrawPlayer(p1, m1);
                plugin.getVaultManager().getEconomy().depositPlayer(p2, m1);
            }
            if (m2 > 0) {
                plugin.getVaultManager().getEconomy().withdrawPlayer(p2, m2);
                plugin.getVaultManager().getEconomy().depositPlayer(p1, m2);
            }
        }

        // 3. Distribute Items (P1 items → P2, P2 items → P1)
        distributeItems(inv, TradeSession.P1_SLOTS, p2);
        distributeItems(inv, TradeSession.P2_SLOTS, p1);

        // 4. Log trade
        plugin.getTradeManager().logTrade(session, inv);

        // 5. Update trust scores
        plugin.getTradeManager().addTrustScore(p1.getUniqueId(), 2);
        plugin.getTradeManager().addTrustScore(p2.getUniqueId(), 2);

        // 6. Clear & close
        inv.clear();
        p1.closeInventory();
        p2.closeInventory();

        // 7. Success messages & sounds
        p1.sendMessage(ChatUtils.colorize("&#55FF55✔ Trade berhasil dengan &#FFFFFF" + p2.getName() + "&#55FF55!"));
        p2.sendMessage(ChatUtils.colorize("&#55FF55✔ Trade berhasil dengan &#FFFFFF" + p1.getName() + "&#55FF55!"));
        p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
    }

    private void distributeItems(Inventory inv, int[] slots, Player target) {
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                target.getInventory().addItem(item).values()
                        .forEach(remaining -> target.getWorld().dropItemNaturally(target.getLocation(), remaining));
            }
        }
    }

    private void returnItems(Inventory inv, int[] slots, Player owner) {
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                owner.getInventory().addItem(item).values()
                        .forEach(remaining -> owner.getWorld().dropItemNaturally(owner.getLocation(), remaining));
                inv.setItem(slot, null);
            }
        }
    }

    // ==================== HELPER: Item Changed ====================

    private void onItemChanged(TradeSession session, Player changer) {
        session.resetBothConfirmations();
        if (session.isCountdownActive()) {
            session.cancelCountdown();
            sendBoth(session, "&#FFAA00Countdown dibatalkan karena perubahan item!");
        }
        updateButtons(session);
    }

    // ==================== ITEM CREATORS ====================

    private ItemStack createMoneyButton(Player p, TradeSession session) {
        double money = session.getMoney(p);
        String moneyStr = ChatUtils.format(money);

        List<String> lore = new ArrayList<>();
        lore.add("&#777777─────────────");
        lore.add("&#AAAAAA Uang: &#FFEE00Rp " + moneyStr);
        lore.add("&#777777─────────────");
        if (session.isPlayer1(p) && session.isPlayer1(p) || !session.isPlayer1(p) && !session.isPlayer1(p)) {
            lore.add("&#00AAFF ➥ Klik untuk set nominal");
        }

        return createStyledItem(Material.GOLD_INGOT,
                "&#FFEE00&l$ &#FFFFFF" + p.getName(),
                lore, false);
    }

    private ItemStack createLockButton(Player p, TradeSession session) {
        boolean locked = session.isLocked(p);

        List<String> lore = new ArrayList<>();
        lore.add("&#777777─────────────");
        lore.add("&#AAAAAA Status: " + (locked ? "&#55FF55🔒 Terkunci" : "&#FF5555🔓 Terbuka"));
        lore.add("&#777777─────────────");
        lore.add(locked ? "&#FF5555 ➥ Klik untuk unlock" : "&#00AAFF ➥ Klik untuk lock item");

        return createStyledItem(
                locked ? Material.LIME_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE,
                (locked ? "&#55FF55&l🔒 " : "&#FFAA00&l🔓 ") + "&#FFFFFF" + p.getName(),
                lore, locked);
    }

    private ItemStack createConfirmButton(Player p, TradeSession session) {
        boolean confirmed = session.isConfirmed(p);

        List<String> lore = new ArrayList<>();
        lore.add("&#777777─────────────");
        lore.add("&#AAAAAA Status: " + (confirmed ? "&#55FF55✔ Confirmed" : "&#FF5555✘ Waiting"));
        lore.add("&#777777─────────────");
        lore.add(confirmed ? "&#FF5555 ➥ Klik untuk batal" : "&#55FF55 ➥ Klik untuk konfirmasi");

        return createStyledItem(
                confirmed ? Material.LIME_CONCRETE : Material.RED_CONCRETE,
                (confirmed ? "&#55FF55&l✔ " : "&#FF5555&l✘ ") + "KONFIRMASI",
                lore, confirmed);
    }

    private ItemStack createTrustHead(Player p) {
        int trustScore = plugin.getTradeManager().getTrustScore(p.getUniqueId());
        String stars = generateStars(trustScore);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(p);
            meta.displayName(ChatUtils.toComponent("&#6CCAFE" + p.getName()));

            List<Component> lore = new ArrayList<>();
            lore.add(ChatUtils.toComponent("&#777777─────────────"));
            lore.add(ChatUtils.toComponent("&#FFEE00 Trust: " + stars + " &#777777(" + trustScore + "%)"));
            lore.add(ChatUtils.toComponent("&#777777─────────────"));
            meta.lore(lore);

            skull.setItemMeta(meta);
        }
        return skull;
    }

    private ItemStack createInfoBook() {
        List<String> lore = new ArrayList<>();
        lore.add("&#777777─────────────");
        lore.add("&#AAAAAA 1. Taruh item di sisi kamu");
        lore.add("&#AAAAAA 2. Klik &#FFEE00$&#AAAAAA untuk set uang");
        lore.add("&#AAAAAA 3. Klik &#FFAA00🔓&#AAAAAA untuk lock item");
        lore.add("&#AAAAAA 4. Klik &#55FF55✔&#AAAAAA untuk konfirmasi");
        lore.add("&#AAAAAA 5. Tunggu countdown 5 detik");
        lore.add("&#777777─────────────");
        lore.add("&#6CCAFE Natural SMP Trade System");

        return createStyledItem(Material.BOOK, "&#6CCAFE&lINFORMATION", lore, false);
    }

    private ItemStack createCountdownItem(TradeSession session) {
        if (!session.isCountdownActive()) {
            List<String> lore = new ArrayList<>();
            lore.add("&#777777─────────────");
            lore.add("&#AAAAAA Kedua player harus");
            lore.add("&#AAAAAA confirm untuk memulai.");
            lore.add("&#777777─────────────");
            return createStyledItem(Material.CLOCK, "&#AAAAAA&l⏱ COUNTDOWN", lore, false);
        }

        int ticks = session.getCountdownTicks();

        // Warna berdasarkan countdown
        Material mat;
        String color;
        if (ticks > 3) {
            mat = Material.RED_STAINED_GLASS_PANE;
            color = "&#FF5555";
        } else if (ticks > 1) {
            mat = Material.YELLOW_STAINED_GLASS_PANE;
            color = "&#FFEE00";
        } else {
            mat = Material.LIME_STAINED_GLASS_PANE;
            color = "&#55FF55";
        }

        List<String> lore = new ArrayList<>();
        lore.add("&#777777─────────────");
        lore.add(color + " Trade dalam " + ticks + " detik...");
        lore.add("&#777777─────────────");

        ItemStack item = createStyledItem(mat, color + "&l⏱ " + ticks, lore, true);
        item.setAmount(Math.max(1, ticks));
        return item;
    }

    // ==================== UTILITY ====================

    private ItemStack createStyledItem(Material mat, String name, List<String> loreStrings, boolean enchanted) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));

            List<Component> lore = new ArrayList<>();
            for (String l : loreStrings) {
                lore.add(ChatUtils.toComponent(l));
            }
            meta.lore(lore);

            if (enchanted) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDecor(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String generateStars(int trustScore) {
        int fullStars = trustScore / 20;
        int halfStar = (trustScore % 20 >= 10) ? 1 : 0;
        int emptyStars = 5 - fullStars - halfStar;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fullStars; i++)
            sb.append("&#FFEE00★");
        for (int i = 0; i < halfStar; i++)
            sb.append("&#FFEE00☆");
        for (int i = 0; i < emptyStars; i++)
            sb.append("&#555555☆");
        return sb.toString();
    }

    private void sendBoth(TradeSession session, String message) {
        String formatted = ChatUtils.colorize(message);
        session.getPlayer1().sendMessage(formatted);
        session.getPlayer2().sendMessage(formatted);
    }
}
