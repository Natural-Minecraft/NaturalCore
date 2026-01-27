package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {

    private final NaturalCore plugin;
    private final Map<UUID, UUID> pendingRequests = new HashMap<>(); // target -> sender
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>(); // player -> session

    public TradeManager(NaturalCore plugin) {
        this.plugin = plugin;
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
        sender.sendMessage(ChatUtils.colorize("&6&lTrade &8» &7Undangan transaksi dikirim ke &e" + target.getName()));
        target.sendMessage(
                ChatUtils.colorize("&6&lTrade &8» &e" + sender.getName() + " &7ingin bertransaksi denganmu."));
        target.sendMessage(ChatUtils.colorize("&7Gunakan &f/trade accept " + sender.getName() + " &7untuk memulai."));
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
}
