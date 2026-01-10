package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TraderCommand implements CommandExecutor {

    private final TraderManager traderManager;

    public TraderCommand(TraderManager traderManager) {
        this.traderManager = traderManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("naturalsmp.admin")) {
            sender.sendMessage(ChatUtils.colorize("&cYou do not have permission."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Player only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.colorize("&eUsage: /traderadmin create <name>"));
                    return true;
                }
                Player p = (Player) sender;
                String displayName = String.join(" ", args).substring(7); // "create " length is 7
                String id = "trader_" + System.currentTimeMillis();

                if (traderManager.createTrader(id, displayName, p.getLocation())) {
                    sender.sendMessage(ChatUtils.colorize("&eTrader &f" + displayName + " &ecreated successfully."));
                } else {
                    sender.sendMessage(ChatUtils.colorize("&eFailed to create trader."));
                }
                break;

            case "remove":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Player only.");
                    return true;
                }
                p = (Player) sender;
                Entity target = getTargetEntity(p);

                if (target == null) {
                    sender.sendMessage(ChatUtils.colorize("&eYou are not looking at any entity."));
                    return true;
                }

                TraderData data = traderManager.getTraderByEntity(target);
                if (data == null) {
                    sender.sendMessage(ChatUtils.colorize("&eThat is not a registered trader."));
                    return true;
                }

                if (traderManager.removeTrader(data.getId())) {
                    sender.sendMessage(ChatUtils.colorize("&eTrader removed."));
                }
                break;

            case "list":
                sender.sendMessage(ChatUtils.colorize("&e&lTrader List:"));
                for (TraderData t : traderManager.getTraders()) {
                    Location loc = t.getLocation();
                    String locStr = String.format("%s, %.0f, %.0f, %.0f", loc.getWorld().getName(), loc.getX(),
                            loc.getY(), loc.getZ());
                    sender.sendMessage(ChatUtils.colorize(" &7- &f" + t.getDisplayName() + " &7(" + locStr + ")"));
                }
                break;

            case "reload":
                traderManager.reload();
                sender.sendMessage(ChatUtils.colorize("&eTrader configuration reloaded."));
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtils.colorize("&e&lTrader Admin Help"));
        sender.sendMessage(ChatUtils.colorize("&7/traderadmin create <name> &f- Create trader at your location"));
        sender.sendMessage(ChatUtils.colorize("&7/traderadmin remove &f- Remove trader you are looking at"));
        sender.sendMessage(ChatUtils.colorize("&7/traderadmin list &f- List all traders"));
        sender.sendMessage(ChatUtils.colorize("&7/traderadmin reload &f- Reload traders from config"));
    }

    // Helper to get target entity
    private Entity getTargetEntity(Player player) {
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            // Simple ray trace check or distance check
            // For simplicity, checking if player is looking at it within 3 blocks
            org.bukkit.util.Vector toEntity = e.getLocation().toVector().subtract(player.getEyeLocation().toVector());
            org.bukkit.util.Vector direction = player.getEyeLocation().getDirection();
            double dot = toEntity.normalize().dot(direction);
            if (dot > 0.95) { // Threshold
                return e;
            }
        }
        return null;
    }
}
