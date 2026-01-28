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
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

public class TopupCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private final TopupSuccessGUI gui;

    public TopupCommand(NaturalCore plugin, TopupSuccessGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        // Usage: / <player> <amount> [transaction_id]
        if (!sender.hasPermission("naturalsmp.admin"))
            return true;

        if (args.length < 2) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /topupnotification <player> <amount> [transaction_id]"));
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtils.colorize("&cInvalid amount."));
            return true;
        }

        String txId = args.length > 2 ? args[2] : "WEB-" + (System.currentTimeMillis() / 1000);

        // 1. Give Coins via CoinsEngine (using Console to be safe if API differs)
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

        return true;
    }
}
