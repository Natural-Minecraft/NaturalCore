package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPADenyCommand implements CommandExecutor {

    private final TeleportManager teleportManager;
    
    public TPADenyCommand(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!teleportManager.hasRequest(player)) {
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cTidak ada request."));
            return true;
        }
        
        Player requester = teleportManager.getRequester(player);
        
        player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cRequest ditolak."));
        
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cPermintaan TPA ditolak oleh " + player.getName() + "."));
            requester.playSound(requester.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
        }
        
        teleportManager.removeRequest(player);
        
        return true;
    }
}
