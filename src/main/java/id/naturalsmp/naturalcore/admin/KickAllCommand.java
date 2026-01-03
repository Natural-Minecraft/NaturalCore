package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickAllCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("naturalcore.admin.kickall")) {
            sender.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        String reason = "Server Maintenance";
        if (args.length > 0) {
            reason = String.join(" ", args);
        }
        
        int kickedCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("naturalcore.admin.bypass")) {
                player.kickPlayer(ChatUtils.color("&c&l✘ Kicked!\n\n&7Reason: &f" + reason));
                kickedCount++;
            }
        }
        
        sender.sendMessage(ChatUtils.color("&a&l✔ &aBerhasil kick &f" + kickedCount + " &aplayer!"));
        return true;
    }
}
