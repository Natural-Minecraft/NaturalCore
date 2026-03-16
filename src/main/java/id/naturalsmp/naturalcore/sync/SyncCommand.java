package id.naturalsmp.naturalcore.sync;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import id.naturalsmp.naturalcore.NaturalCore;
import net.md_5.bungee.api.ChatColor;

public class SyncCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public SyncCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("naturalcore.sync.admin")) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Insufficient arguments! Use /sync <command>.");
            return true;
        }
        
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < args.length; index++) {
            builder.append(args[index]);
            
            if (index < args.length - 1) {
                builder.append(" ");
            }
        }

        if (plugin.getConnectionManager() != null) {
            plugin.getConnectionManager().dispatchCommand(builder.toString());
            sender.sendMessage(ChatColor.GREEN + "Synced command /" + builder.toString() + " to the Velocity instance!");
        } else {
            sender.sendMessage(ChatColor.RED + "Command Sync is disabled or not connected.");
        }

        return true;
    }

}
