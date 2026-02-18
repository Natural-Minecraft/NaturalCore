package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TradeCommand - Handler untuk /trade command.
 * Subcommands:
 * /trade <player> - Kirim trade request
 * /trade accept <player> - Terima trade request
 * /trade deny <player> - Tolak trade request
 * /trade history [player] - Lihat trade history (admin: player lain)
 */
public class TradeCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public TradeCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555Only players can use this command."));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorize("&#FF5555Gunakan: /trade accept <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatUtils.colorize("&#FF5555Player tidak ditemukan."));
                    return true;
                }
                plugin.getTradeManager().acceptRequest(player, target);
            }
            case "deny" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatUtils.colorize("&#FF5555Gunakan: /trade deny <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatUtils.colorize("&#FF5555Player tidak ditemukan."));
                    return true;
                }
                plugin.getTradeManager().denyRequest(player, target);
            }
            case "history" -> {
                String targetName = args.length >= 2 ? args[1] : player.getName();

                // Permission check for viewing others
                if (!targetName.equalsIgnoreCase(player.getName()) &&
                        !player.hasPermission("naturalsmp.trade.history.others")) {
                    player.sendMessage(
                            ChatUtils.colorize("&#FF5555Kamu tidak punya izin untuk melihat history player lain."));
                    return true;
                }

                List<String> history = plugin.getTradeManager().getTradeHistory(targetName, 10);
                if (history.isEmpty()) {
                    player.sendMessage(ChatUtils.colorize("&#AAAAAABelum ada trade history untuk " + targetName + "."));
                    return true;
                }

                player.sendMessage(ChatUtils.colorize("&#6CCAFE&l═══ Trade History: " + targetName + " ═══"));
                for (String line : history) {
                    // Format: [timestamp] P1 ↔ P2 | ...
                    player.sendMessage(ChatUtils.colorize("&#777777" + line));
                }
                player.sendMessage(ChatUtils.colorize("&#6CCAFE&l═══════════════════════════"));
            }
            default -> {
                // /trade <player>
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ChatUtils.colorize("&#FF5555Player tidak ditemukan."));
                    return true;
                }

                if (target.equals(player)) {
                    player.sendMessage(ChatUtils.colorize("&#FF5555Tidak bisa trade dengan diri sendiri."));
                    return true;
                }

                plugin.getTradeManager().sendRequest(player, target);
            }
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatUtils.colorize(""));
        player.sendMessage(ChatUtils.colorize("&#6CCAFE&l═══ Natural Trade ═══"));
        player.sendMessage(ChatUtils.colorize("&#FFFFFF /trade <player>  &#777777- Kirim request"));
        player.sendMessage(ChatUtils.colorize("&#FFFFFF /trade accept <player>  &#777777- Terima"));
        player.sendMessage(ChatUtils.colorize("&#FFFFFF /trade deny <player>  &#777777- Tolak"));
        player.sendMessage(ChatUtils.colorize("&#FFFFFF /trade history [player]  &#777777- Riwayat"));
        player.sendMessage(ChatUtils.colorize("&#6CCAFE&l═══════════════════════════"));
        player.sendMessage(ChatUtils.colorize(""));
    }
}
