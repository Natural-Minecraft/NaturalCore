package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("naturalcore.admin.broadcast")) {
            sender.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatUtils.color("&cUsage: /broadcast <pesan>"));
            return true;
        }
        
        String message = String.join(" ", args);
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.color("&6&l⚡ BROADCAST &6&l⚡"));
        Bukkit.broadcastMessage(ChatUtils.color("&f" + message));
        Bukkit.broadcastMessage("");
        
        return true;
    }
}
