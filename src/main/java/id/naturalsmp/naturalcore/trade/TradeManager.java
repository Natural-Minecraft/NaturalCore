package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TradeManager - Mengelola request, sesi aktif, trust score, dan trade history.
 */
public class TradeManager {

    private final NaturalCore plugin;

    // Pending requests: target UUID → sender UUID
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private final Map<UUID, Long> requestTimestamps = new HashMap<>();
    private static final long REQUEST_TIMEOUT_MS = 60_000; // 60 detik

    // Active sessions: player UUID → session
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>();

    // Money input: player UUID → session (saat player ketik di chat)
    private final Map<UUID, TradeSession> pendingMoneyInput = new HashMap<>();

    // Trust scores: player UUID → score (0-100)
    private YamlConfiguration trustConfig;
    private File trustFile;

    // Request cleanup task
    private BukkitTask cleanupTask;

    public TradeManager(NaturalCore plugin) {
        this.plugin = plugin;

        // Load trust data
        this.trustFile = new File(plugin.getDataFolder(), "trade_trust.yml");
        if (!trustFile.exists()) {
            try {
                trustFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.trustConfig = YamlConfiguration.loadConfiguration(trustFile);

        // Create trade log directory
        File logDir = new File(plugin.getDataFolder(), "logs");
        if (!logDir.exists())
            logDir.mkdirs();

        // Register chat listener
        Bukkit.getPluginManager().registerEvents(new TradeChatListener(), plugin);

        // Periodic cleanup task for expired requests
        this.cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanExpiredRequests, 200L, 200L);
    }

    // ==================== REQUEST SYSTEM ====================

    public void sendRequest(Player sender, Player target) {
        // Check combat
        if (plugin.getCombatManager() != null && plugin.getCombatManager().isInCombat(sender)) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555Tidak bisa trade saat dalam pertempuran!"));
            return;
        }

        // Check active session
        if (activeSessions.containsKey(sender.getUniqueId())) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555Kamu sedang dalam trade."));
            return;
        }
        if (activeSessions.containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555" + target.getName() + " sedang dalam trade lain."));
            return;
        }

        // Check distance
        if (!sender.getWorld().equals(target.getWorld()) ||
                sender.getLocation().distance(target.getLocation()) > 20) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555" + target.getName() + " terlalu jauh! (max 20 blocks)"));
            return;
        }

        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        requestTimestamps.put(target.getUniqueId(), System.currentTimeMillis());

        // Message to sender
        sender.sendMessage(ChatUtils.colorize(
                "&#6CCAFE&lNatural Trade &#777777» &#AAAAAAPermintaan trade dikirim ke &#FFFFFF" + target.getName()
                        + "&#AAAAAA."));

        // Interactive message to target (clickable accept/deny)
        Component msg = ChatUtils.toComponent(
                "&#6CCAFE&lNatural Trade &#777777» &#FFFFFF" + sender.getName() + " &#AAAAAAingin trade denganmu! ");

        Component accept = ChatUtils.toComponent("&#55FF55&l[ACCEPT]")
                .clickEvent(ClickEvent.runCommand("/trade accept " + sender.getName()))
                .hoverEvent(HoverEvent.showText(ChatUtils.toComponent("&#55FF55Klik untuk menerima")));

        Component deny = ChatUtils.toComponent(" &#FF5555&l[DENY]")
                .clickEvent(ClickEvent.runCommand("/trade deny " + sender.getName()))
                .hoverEvent(HoverEvent.showText(ChatUtils.toComponent("&#FF5555Klik untuk menolak")));

        target.sendMessage(msg.append(accept).append(deny));
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }

    public void acceptRequest(Player target, Player sender) {
        UUID senderId = pendingRequests.get(target.getUniqueId());
        if (senderId == null || !senderId.equals(sender.getUniqueId())) {
            target.sendMessage(ChatUtils.colorize("&#FF5555Tidak ada permintaan trade dari " + sender.getName() + "."));
            return;
        }

        // Check expiry
        Long timestamp = requestTimestamps.get(target.getUniqueId());
        if (timestamp == null || System.currentTimeMillis() - timestamp > REQUEST_TIMEOUT_MS) {
            pendingRequests.remove(target.getUniqueId());
            requestTimestamps.remove(target.getUniqueId());
            target.sendMessage(ChatUtils.colorize("&#FF5555Request trade sudah expired!"));
            return;
        }

        // Check distance
        if (!target.getWorld().equals(sender.getWorld()) ||
                target.getLocation().distance(sender.getLocation()) > 20) {
            target.sendMessage(ChatUtils.colorize("&#FF5555" + sender.getName() + " terlalu jauh! (max 20 blocks)"));
            return;
        }

        // Check combat
        if (plugin.getCombatManager() != null) {
            if (plugin.getCombatManager().isInCombat(target) || plugin.getCombatManager().isInCombat(sender)) {
                target.sendMessage(ChatUtils.colorize("&#FF5555Tidak bisa trade saat dalam pertempuran!"));
                return;
            }
        }

        pendingRequests.remove(target.getUniqueId());
        requestTimestamps.remove(target.getUniqueId());

        startTrade(sender, target);
    }

    public void denyRequest(Player target, Player sender) {
        UUID senderId = pendingRequests.get(target.getUniqueId());
        if (senderId == null || !senderId.equals(sender.getUniqueId())) {
            target.sendMessage(ChatUtils.colorize("&#FF5555Tidak ada permintaan trade dari " + sender.getName() + "."));
            return;
        }

        pendingRequests.remove(target.getUniqueId());
        requestTimestamps.remove(target.getUniqueId());

        target.sendMessage(
                ChatUtils.colorize("&#FF5555Kamu menolak trade dari &#FFFFFF" + sender.getName() + "&#FF5555."));
        sender.sendMessage(ChatUtils.colorize("&#FF5555" + target.getName() + " menolak permintaan trade kamu."));
    }

    private void startTrade(Player p1, Player p2) {
        TradeSession session = new TradeSession(p1, p2);
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);

        // Open shared GUI
        plugin.getTradeGUI().openTradeGUI(session);

        p1.sendMessage(ChatUtils.colorize("&#6CCAFE&lNatural Trade &#777777» &#AAAAAABerhasil membuka trade!"));
        p2.sendMessage(ChatUtils.colorize("&#6CCAFE&lNatural Trade &#777777» &#AAAAAABerhasil membuka trade!"));
    }

    // ==================== SESSION MANAGEMENT ====================

    public void endTrade(TradeSession session) {
        activeSessions.remove(session.getPlayer1().getUniqueId());
        activeSessions.remove(session.getPlayer2().getUniqueId());
        pendingMoneyInput.remove(session.getPlayer1().getUniqueId());
        pendingMoneyInput.remove(session.getPlayer2().getUniqueId());
        session.cancelCountdown();
    }

    public TradeSession getSession(Player p) {
        return activeSessions.get(p.getUniqueId());
    }

    // ==================== MONEY INPUT ====================

    public void startMoneyInput(Player p, TradeSession session) {
        // Set flag agar onClose TIDAK cancel trade
        session.setInputtingMoney(p, true);
        pendingMoneyInput.put(p.getUniqueId(), session);

        // Close inventory (will trigger onClose but flag prevents cancellation)
        p.closeInventory();

        // Send chat prompt
        p.sendMessage(ChatUtils.colorize(""));
        p.sendMessage(ChatUtils.colorize("&#6CCAFE&l═══════════════════════════"));
        p.sendMessage(ChatUtils.colorize("&#6CCAFE&lNatural Trade &#777777» &#FFFFFFSet Nominal"));
        p.sendMessage(ChatUtils.colorize("&#AAAAAAKetik jumlah uang di chat."));
        p.sendMessage(ChatUtils.colorize("&#AAAAAAKetik '&#FFFFFFcancel&#AAAAAA' untuk batal."));

        // Show balance
        if (plugin.getVaultManager().getEconomy() != null) {
            double balance = plugin.getVaultManager().getEconomy().getBalance(p);
            p.sendMessage(ChatUtils.colorize("&#AAAAAA Saldo: &#FFEE00Rp " + ChatUtils.format(balance)));
        }

        p.sendMessage(ChatUtils.colorize("&#6CCAFE&l═══════════════════════════"));
        p.sendMessage(ChatUtils.colorize(""));

        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
    }

    // ==================== TRUST SYSTEM ====================

    public int getTrustScore(UUID uuid) {
        return trustConfig.getInt("trust." + uuid.toString(), 50); // Default 50%
    }

    public void addTrustScore(UUID uuid, int amount) {
        int current = getTrustScore(uuid);
        int newScore = Math.min(100, current + amount);
        trustConfig.set("trust." + uuid.toString(), newScore);
        saveTrust();
    }

    public void removeTrustScore(UUID uuid, int amount) {
        int current = getTrustScore(uuid);
        int newScore = Math.max(0, current - amount);
        trustConfig.set("trust." + uuid.toString(), newScore);
        saveTrust();
    }

    private void saveTrust() {
        try {
            trustConfig.save(trustFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save trade trust data: " + e.getMessage());
        }
    }

    // ==================== TRADE HISTORY LOG ====================

    public void logTrade(TradeSession session, Inventory inv) {
        try {
            File logFile = new File(plugin.getDataFolder(), "logs/trade_history.log");
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());

            Player p1 = session.getPlayer1();
            Player p2 = session.getPlayer2();

            StringBuilder p1Items = new StringBuilder();
            for (int slot : TradeSession.P1_SLOTS) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (p1Items.length() > 0)
                        p1Items.append(", ");
                    p1Items.append(item.getType().name()).append(" x").append(item.getAmount());
                }
            }

            StringBuilder p2Items = new StringBuilder();
            for (int slot : TradeSession.P2_SLOTS) {
                ItemStack item = inv.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (p2Items.length() > 0)
                        p2Items.append(", ");
                    p2Items.append(item.getType().name()).append(" x").append(item.getAmount());
                }
            }

            double m1 = session.getMoney(p1);
            double m2 = session.getMoney(p2);

            String line = String.format("[%s] %s ↔ %s | P1 Items: [%s] + Rp %.0f | P2 Items: [%s] + Rp %.0f",
                    timestamp, p1.getName(), p2.getName(),
                    p1Items.length() > 0 ? p1Items.toString() : "none", m1,
                    p2Items.length() > 0 ? p2Items.toString() : "none", m2);

            writer.println(line);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to log trade: " + e.getMessage());
        }
    }

    public List<String> getTradeHistory(String playerName, int limit) {
        List<String> results = new ArrayList<>();
        File logFile = new File(plugin.getDataFolder(), "logs/trade_history.log");
        if (!logFile.exists())
            return results;

        try {
            List<String> allLines = java.nio.file.Files.readAllLines(logFile.toPath());
            // Search from end for efficiency
            for (int i = allLines.size() - 1; i >= 0 && results.size() < limit; i--) {
                String line = allLines.get(i);
                if (playerName == null || line.toLowerCase().contains(playerName.toLowerCase())) {
                    results.add(line);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read trade history: " + e.getMessage());
        }
        return results;
    }

    // ==================== CLEANUP ====================

    private void cleanExpiredRequests() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> it = requestTimestamps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (now - entry.getValue() > REQUEST_TIMEOUT_MS) {
                UUID targetId = entry.getKey();
                UUID senderId = pendingRequests.remove(targetId);
                it.remove();

                // Notify sender if online
                if (senderId != null) {
                    Player sender = Bukkit.getPlayer(senderId);
                    Player target = Bukkit.getPlayer(targetId);
                    if (sender != null && target != null) {
                        sender.sendMessage(ChatUtils.colorize(
                                "&#FF5555Request trade ke " + target.getName() + " sudah expired."));
                    }
                }
            }
        }
    }

    // ==================== CHAT LISTENER (Money Input) ====================

    private class TradeChatListener implements Listener {

        @EventHandler
        public void onChat(AsyncPlayerChatEvent e) {
            Player p = e.getPlayer();
            if (!pendingMoneyInput.containsKey(p.getUniqueId()))
                return;

            e.setCancelled(true);
            TradeSession session = pendingMoneyInput.remove(p.getUniqueId());
            String message = e.getMessage().trim();

            // Process on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (message.equalsIgnoreCase("cancel")) {
                    p.sendMessage(ChatUtils.colorize("&#FFAA00Input money dibatalkan."));
                    session.setInputtingMoney(p, false);
                    plugin.getTradeGUI().reopenForPlayer(p, session);
                    return;
                }

                try {
                    double amount = Double.parseDouble(message.replace(",", ""));
                    if (amount < 0)
                        throw new NumberFormatException();

                    // Check balance
                    if (plugin.getVaultManager().getEconomy() != null) {
                        double balance = plugin.getVaultManager().getEconomy().getBalance(p);
                        if (amount > balance) {
                            p.sendMessage(ChatUtils.colorize(
                                    "&#FF5555Saldo tidak cukup! Saldo kamu: &#FFEE00Rp " + ChatUtils.format(balance)));
                            session.setInputtingMoney(p, false);
                            plugin.getTradeGUI().reopenForPlayer(p, session);
                            return;
                        }
                    }

                    session.setMoney(p, amount);
                    // Reset confirmations and countdown
                    session.resetBothConfirmations();
                    if (session.isCountdownActive()) {
                        session.cancelCountdown();
                    }

                    p.sendMessage(ChatUtils.colorize(
                            "&#55FF55Nominal set: &#FFEE00Rp " + ChatUtils.format(amount)));
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);

                } catch (NumberFormatException ex) {
                    p.sendMessage(ChatUtils.colorize("&#FF5555Jumlah tidak valid! Gunakan angka saja."));
                }

                session.setInputtingMoney(p, false);
                plugin.getTradeGUI().reopenForPlayer(p, session);

                // Update for other player too
                plugin.getTradeGUI().updateButtons(session);
            });
        }
    }
}
