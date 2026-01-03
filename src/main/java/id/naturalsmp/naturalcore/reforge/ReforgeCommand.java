package id.naturalsmp.naturalcore.reforge;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReforgeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.reforge")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        // Buka GUI Reforge
        ReforgeGUI.openReforgeMenu(player);
        
        return true;
    }
}
