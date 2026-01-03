package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TPAAcceptCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private final TeleportManager teleportManager;
    
    public TPAAcceptCommand(NaturalCore plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.tpa")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (!teleportManager.hasRequest(player)) {
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cTidak ada permintaan teleport."));
            player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            return true;
        }
        
        if (teleportManager.isRequestExpired(player)) {
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cPermintaan sudah kadaluarsa."));
            teleportManager.removeRequest(player);
            return true;
        }
        
        Player requester = teleportManager.getRequester(player);
        
        if (requester == null || !requester.isOnline()) {
            player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &cPlayer yang meminta sudah offline."));
            teleportManager.removeRequest(player);
            return true;
        }
        
        player.sendMessage(ChatUtils.color("&8[&x&2&6&D&0&7&C&lN&x&2&3&C&D&8&5&la&x&1&F&C&9&8&F&lt&x&1&C&C&6&9&9&lu&x&1&8&C&2&A&3&lr&x&1&5&B&F&A&C&la&x&1&1&B&B&B&6&ll&x&0&E&B&8&C&0&lS&x&0&A&B&4&C&A&lM&x&0&7&B&1&D&3&lP&8] &aMenerima TPA..."));
        
        // Start warmup countdown for requester
        requester.sendTitle(ChatUtils.color("&x&0&E&B&8&C&0&lTELEPORTING"), 
                           ChatUtils.color("&7Jangan bergerak..."), 10, 40, 10);
        requester.playSound(requester.getLocation(), "UI_BUTTON_CLICK", 1f, 1f);
        
        teleportManager.setWarmup(requester, true);
        
        final Location startLocation = requester.getLocation().clone();
        final int warmupSeconds = teleportManager.getWarmupSeconds();
        
        new BukkitRunnable() {
            int countdown = warmupSeconds;
            
            @Override
            public void run() {
                if (!requester.isOnline()) {
                    teleportManager.setWarmup(requester, false);
                    cancel();
                    return;
                }
                
                // Check if player moved
                Location currentLoc = requester.getLocation();
                if (currentLoc.distance(startLocation) > 0.5) {
                    requester.sendActionBar(ChatUtils.color("&c&lGAGAL! &7Kamu bergerak."));
                    requester.playSound(requester.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
                    teleportManager.setWarmup(requester, false);
                    teleportManager.removeRequest(player);
                    cancel();
                    return;
                }
                
                if (countdown <= 0) {
                    // Teleport!
                    teleportManager.setBackLocation(requester, requester.getLocation());
                    requester.teleport(player.getLocation());
                    
                    requester.playSound(requester.getLocation(), "ENTITY_ENDERMAN_TELEPORT", 1f, 1f);
                    player.playSound(player.getLocation(), "ENTITY_ENDERMAN_TELEPORT", 1f, 1f);
                    
                    requester.sendActionBar(ChatUtils.color("&a&lSUKSES! &7Teleportasi berhasil."));
                    
                    teleportManager.setWarmup(requester, false);
                    teleportManager.removeRequest(player);
                    cancel();
                    return;
                }
                
                requester.sendActionBar(ChatUtils.color("&eTeleportasi dalam &f" + countdown + " detik..."));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        return true;
    }
}
