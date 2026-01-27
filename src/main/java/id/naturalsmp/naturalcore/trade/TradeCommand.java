package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TradeCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public TradeCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(
                    ChatUtils.colorize("&6&lNaturalCore &8» &7Gunakan &f/trade <player> &7untuk mengajak transaksi."));
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                player.sendMessage(ChatUtils.colorize("&cUsage: /trade accept <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ChatUtils.colorize("&cPlayer tidak ditemukan."));
                return true;
            }
            plugin.getTradeManager().acceptRequest(player, target);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatUtils.colorize("&cPlayer tidak ditemukan."));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatUtils.colorize("&cKamu tidak bisa transaksi dengan diri sendiri."));
            return true;
        }

        plugin.getTradeManager().sendRequest(player, target);
        return true;
    }
}
