package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TraderCommand implements CommandExecutor {

    private final CurrencyManager currencyManager;
    private final TraderManager traderManager;
    private final TradeEditor tradeEditor;

    public TraderCommand(CurrencyManager currencyManager, TraderManager traderManager, TradeEditor tradeEditor) {
        this.currencyManager = currencyManager;
        this.traderManager = traderManager;
        this.tradeEditor = tradeEditor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("givecurrency")) {
            if (!sender.hasPermission("naturalcore.admin")) {
                sender.sendMessage(ChatUtils.format("&cYou do not have permission!"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(ChatUtils.format("&cUsage: /givecurrency <player> <amount>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatUtils.format("&cPlayer not found!"));
                return true;
            }

            try {
                int amount = Integer.parseInt(args[1]);
                currencyManager.giveCurrency(target, amount);
                sender.sendMessage(ChatUtils.format("&aGave " + amount + " Quest Paper to " + target.getName()));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatUtils.format("&cInvalid amount!"));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("tradeeditor")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatUtils.format("&cOnly players can use this command!"));
                return true;
            }

            if (!player.hasPermission("naturalcore.admin")) {
                player.sendMessage(ChatUtils.format("&cYou do not have permission!"));
                return true;
            }

            tradeEditor.openEditor(player);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("wanderingtrader")) {
             if (!sender.hasPermission("naturalcore.admin")) {
                sender.sendMessage(ChatUtils.format("&cYou do not have permission!"));
                return true;
            }
            
            if (args.length == 0) {
                sender.sendMessage(ChatUtils.format("&cUsage: /wanderingtrader <spawn|despawn|setloc|setinterval|next>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("spawn")) {
                if (traderManager.isTraderActive()) {
                    sender.sendMessage(ChatUtils.format("&cTrader is already spawned!"));
                    return true;
                }
                traderManager.forceSpawn();
                sender.sendMessage(ChatUtils.format("&aTrader spawned manually!"));
                return true;
            }
            
            if (args[0].equalsIgnoreCase("despawn")) {
                if (!traderManager.isTraderActive()) {
                    sender.sendMessage(ChatUtils.format("&cTrader is not currently spawned."));
                    return true;
                }
                traderManager.forceDespawn();
                sender.sendMessage(ChatUtils.format("&aTrader despawned manually!"));
                return true;
            }
            
            if (args[0].equalsIgnoreCase("setloc")) {
                if (sender instanceof Player player) {
                    traderManager.setSpawnLocation(player.getLocation());
                    sender.sendMessage(ChatUtils.format("&aTrader spawn location set!"));
                } else {
                    sender.sendMessage(ChatUtils.format("&cOnly players can set location!"));
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("setinterval")) {
                if (args.length < 2) {
                    sender.sendMessage(ChatUtils.format("&cUsage: /wanderingtrader setinterval <seconds>"));
                    return true;
                }
                try {
                    long seconds = Long.parseLong(args[1]);
                    if (seconds < 600) {
                        sender.sendMessage(ChatUtils.format("&cInterval must be at least 600 seconds (10 minutes)!"));
                        return true;
                    }
                    traderManager.setInterval(seconds);
                    sender.sendMessage(ChatUtils.format("&aSpawn interval set to " + seconds + " seconds."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatUtils.format("&cInvalid number!"));
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("next")) {
                sender.sendMessage(ChatUtils.format(traderManager.getStatusMessage()));
                return true;
            }
            
            return true;
        }

        return false;
    }
}
