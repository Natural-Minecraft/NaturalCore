package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {

    private final NaturalCore plugin;
    private final Map<UUID, UUID> pendingRequests = new HashMap<>(); // target -> sender
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>(); // player -> session
    private final Map<UUID, TradeSession> pendingMoneyInput = new HashMap<>();

    public TradeManager(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new TradeChatListener(), plugin);
    }

    public void sendRequest(Player sender, Player target) {
        if (activeSessions.containsKey(sender.getUniqueId())) {
            sender.sendMessage(ChatUtils.colorize("&cKamu sedang dalam transaksi."));
            return;
        }
        if (activeSessions.containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatUtils.colorize("&c" + target.getName() + " sedang dalam transaksi lain."));
            return;
        }

        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        String prefix = ConfigUtils.getString("prefix.general");
        ConfigUtils.sendGeneral(sender, "messages.trade.request-sent", "%player%", target.getName());
        ConfigUtils.sendGeneral(target, "messages.trade.request-received", "%player%", sender.getName());
        ConfigUtils.sendGeneral(target, "messages.trade.accept-usage", "%player%", sender.getName());
    }

    public void acceptRequest(Player target, Player sender) {
        UUID senderId = pendingRequests.get(target.getUniqueId());
        if (senderId == null || !senderId.equals(sender.getUniqueId())) {
            target.sendMessage(ChatUtils.colorize("&cTidak ada permintaan transaksi dari player tersebut."));
            return;
        }

        pendingRequests.remove(target.getUniqueId());
        startTrade(sender, target);
    }

    private void startTrade(Player p1, Player p2) {
        TradeSession session = new TradeSession(p1, p2);
        activeSessions.put(p1.getUniqueId(), session);
        activeSessions.put(p2.getUniqueId(), session);

        // Open GUI
        plugin.getTradeGUI().openTradeGUI(p1, session);
        plugin.getTradeGUI().openTradeGUI(p2, session);
    }

    public void endTrade(TradeSession session) {
        activeSessions.remove(session.getPlayer1().getUniqueId());
        activeSessions.remove(session.getPlayer2().getUniqueId());
    }

    public TradeSession getSession(Player p) {
        return activeSessions.get(p.getUniqueId());
    }

    public void startCustomMoneyInput(Player p, TradeSession session) {
        // Use Sign Menu for input
        pendingMoneyInput.put(p.getUniqueId(), session);
        p.closeInventory();

        new id.naturalsmp.naturalcore.utils.SignMenu(plugin).open(p,
                new String[] { "", "^ ^ ^", "Masukan Nominal", "NaturalCoin" }, (lines) -> {
                    String input = lines[0].trim();
                    if (input.isEmpty())
                        return; // Ignored

                    try {
                        double amount = Double.parseDouble(input);
                        if (amount < 0)
                            throw new NumberFormatException();

                        // Check balance
                        double balance = plugin.getVaultManager().getEconomy().getBalance(p);
                        if (amount > balance) {
                            ConfigUtils.sendGeneral(p, "messages.trade.insufficient-balance", "%max%",
                                    String.valueOf((int) balance));
                        } else {
                            session.setMoney(p, amount);
                            session.setConfirmed(p, false);
                            session.setConfirmed(session.getOther(p), false);
                            ConfigUtils.sendGeneral(p, "messages.trade.money-set", "%amount%",
                                    String.valueOf((int) amount));
                        }
                    } catch (NumberFormatException ex) {
                        p.sendMessage(ChatUtils.colorize("&cJumlah tidak valid! Gunakan angka saja."));
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getTradeGUI().openTradeGUI(p, session);
                        plugin.getTradeGUI().openTradeGUI(session.getOther(p), session);
                    });

                    pendingMoneyInput.remove(p.getUniqueId());

                });

        // Also keep chat listener as backup or if Sign fails?
        // Logic handled in callback.
    }

    private class TradeChatListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent e) {
            Player p = e.getPlayer();
            if (!pendingMoneyInput.containsKey(p.getUniqueId()))
                return;

            e.setCancelled(true);
            TradeSession session = pendingMoneyInput.remove(p.getUniqueId());
            String message = e.getMessage().trim();

            if (message.equalsIgnoreCase("cancel")) {
                ConfigUtils.sendGeneral(p, "messages.trade.input-cancelled");
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getTradeGUI().openTradeGUI(p, session));
                return;
            }

            try {
                double amount = Double.parseDouble(message);
                if (amount < 0)
                    throw new NumberFormatException();

                // Check balance
                double balance = plugin.getVaultManager().getEconomy().getBalance(p);
                if (amount > balance) {
                    ConfigUtils.sendGeneral(p, "messages.trade.insufficient-balance", "%max%",
                            String.valueOf((int) balance));
                } else {
                    session.setMoney(p, amount);
                    session.setConfirmed(p, false);
                    session.setConfirmed(session.getOther(p), false);
                    ConfigUtils.sendGeneral(p, "messages.trade.money-set", "%amount%", String.valueOf((int) amount));
                }
            } catch (NumberFormatException ex) {
                p.sendMessage(ChatUtils.colorize("&cJumlah tidak valid! Gunakan angka saja."));
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getTradeGUI().openTradeGUI(p, session);
                plugin.getTradeGUI().openTradeGUI(session.getOther(p), session);
            });
        }
    }
}
