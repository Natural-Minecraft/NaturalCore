package id.naturalsmp.naturalcore.topup;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Unified Topup Command - Handles both NaturalCoin and Rank grants.
 * 
 * Usage: /topupnotification <player> <amount> [transaction_id]
 * 
 * If <amount> is a NUMBER: Give NaturalCoin to player.
 * If <amount> is midi|vip|mvp|nature: Grant rank for 30 days.
 */
public class TopupCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private final TopupSuccessGUI gui;

    private static final Set<String> VALID_RANKS = Set.of("midi", "vip", "mvp", "nature");
    private static final int DEFAULT_RANK_DAYS = 30;

    public TopupCommand(NaturalCore plugin, TopupSuccessGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        // Usage: /topupnotification <player> <amount> [transaction_id]
        if (!sender.hasPermission("naturalsmp.admin"))
            return true;

        if (args.length < 2) {
            sender.sendMessage(
                    ChatUtils.colorize("&cUsage: /topupnotification <player> <amount|rank> [transaction_id]"));
            sender.sendMessage(ChatUtils.colorize("&7  amount: Number for NaturalCoin"));
            sender.sendMessage(ChatUtils.colorize("&7  rank: midi, vip, mvp, nature (30 days)"));
            return true;
        }

        String playerName = args[0];
        String amountOrRank = args[1].toLowerCase();
        String txId = args.length > 2 ? args[2] : "WEB-" + (System.currentTimeMillis() / 1000);

        Player target = Bukkit.getPlayer(playerName);

        // Check if it's a rank or a coin amount
        if (VALID_RANKS.contains(amountOrRank)) {
            // === RANK MODE ===
            handleRankGrant(sender, playerName, amountOrRank, target, txId);
        } else {
            // === COIN MODE ===
            try {
                double amount = Double.parseDouble(amountOrRank);
                handleCoinGrant(sender, playerName, amount, target, txId);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtils
                        .colorize("&cInvalid amount. Use a number for coins or midi/vip/mvp/nature for ranks."));
            }
        }

        return true;
    }

    /**
     * Handle NaturalCoin grant.
     */
    private void handleCoinGrant(CommandSender sender, String playerName, double amount, Player target, String txId) {
        // 1. Give Coins via CoinsEngine
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "naturalcoin give " + playerName + " " + (int) amount);

        // 2. Open GUI & Effects if player is online
        if (target != null && target.isOnline()) {
            gui.openGUI(target, amount, txId);

            // Global Broadcast with Premium Style
            GUIUtils.broadcastEmpty();
            GUIUtils.broadcast("  &e❂ &e&lTOPUP BERHASIL &e❂");
            GUIUtils.broadcast("  &f" + target.getName() + " &7baru saja mendukung server!");
            GUIUtils.broadcast("  &7Terima kasih atas dukungannya. &e❤");
            GUIUtils.broadcastEmpty();
        } else {
            sender.sendMessage(ChatUtils.colorize(
                    "&e[TopUp] Player " + playerName + " is offline. GUI will not be shown, but coins were given."));
        }

        sender.sendMessage(ChatUtils.colorize(
                "&aBerhasil memberikan &e" + (int) amount + " NC &ake &f" + playerName + " &7[" + txId + "]"));
    }

    /**
     * Handle Rank grant (30 days).
     */
    private void handleRankGrant(CommandSender sender, String playerName, String rank, Player target, String txId) {
        // 1. Dispatch LuckPerms Command (30 days default)
        String lpCmd = String.format("lp user %s parent addtemp %s %dd", playerName, rank, DEFAULT_RANK_DAYS);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), lpCmd);

        // 2. Broadcast and Notification
        sender.sendMessage(ChatUtils.colorize(
                "&aBerhasil menambahkan rank &e" + rank.toUpperCase() + " &aselama &e" + DEFAULT_RANK_DAYS
                        + " hari &ake &f" + playerName + " &7[" + txId + "]"));

        if (target != null && target.isOnline()) {
            GUIUtils.broadcastEmpty();
            GUIUtils.broadcast("  &b&lRANK UPGRADE &8┃ &f" + target.getName());
            GUIUtils.broadcast(
                    "  &7Mendapatkan Rank &e&l" + rank.toUpperCase() + " &7selama &a" + DEFAULT_RANK_DAYS + " Hari&7.");
            GUIUtils.broadcast("  &eTerima kasih telah mendukung server! ❤");
            GUIUtils.broadcastEmpty();
        }
    }
}
