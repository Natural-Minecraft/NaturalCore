package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TraderCommand implements CommandExecutor {

    private final TraderManager traderManager;
    
    public TraderCommand(TraderManager traderManager) {
        this.traderManager = traderManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.trader")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(ChatUtils.color("&c&lUsage:"));
            player.sendMessage(ChatUtils.color("&7/trader open - Buka trader menu"));
            player.sendMessage(ChatUtils.color("&7/trader info - Lihat info trader"));
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "open":
                traderManager.openTraderGUI(player);
                break;
            case "info":
                player.sendMessage(ChatUtils.color("&6&l⚡ Travelling Trader Info"));
                player.sendMessage(ChatUtils.color("&7Status: &a" + (traderManager.isTraderAvailable() ? "Available" : "Unavailable")));
                player.sendMessage(ChatUtils.color("&7Next Arrival: &f" + traderManager.getNextArrivalTime()));
                break;
            default:
                player.sendMessage(ChatUtils.color("&cAction tidak valid!"));
                break;
        }
        
        return true;
    }
}
