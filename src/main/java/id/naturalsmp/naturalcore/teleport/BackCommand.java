package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BackCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private final TeleportManager teleportManager;
    
    public BackCommand(NaturalCore plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Admin version: /back <player>
        if (args.length > 0 && sender.hasPermission("naturalcore.back.others")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatUtils.color("&cPlayer tidak ditemukan!"));
                return true;
            }
            
            if (!teleportManager.hasBackLocation(target)) {
                sender.sendMessage(ChatUtils.color("&8[&bReforge&8] &cTidak ada lokasi tersimpan untuk " + target.getName()));
                return true;
            }
            
            target.teleport(teleportManager.getBackLocation(target));
            target.playSound(target.getLocation(), "ENTITY_ENDERMAN_TELEPORT", 1f, 1f);
            sender.sendMessage(ChatUtils.color("&8[&bReforge&8] &aMemindahkan &f" + target.getName() + " &ake lokasi terakhirnya."));
            return true;
        }
        
        // Player version
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.back")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (!teleportManager.hasBackLocation(player)) {
            player.sendMessage(ChatUtils.color("&8[&bReforge&8] &cTidak ada lokasi tersimpan."));
            player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            return true;
        }
        
        // Start warmup
        player.sendTitle(ChatUtils.color("&e&lTELEPORTING..."), 
                        ChatUtils.color("&7Jangan bergerak selama " + teleportManager.getWarmupSeconds() + " detik"), 
                        5, 20, 5);
        player.playSound(player.getLocation(), "UI_BUTTON_CLICK", 1f, 1f);
        
        teleportManager.setWarmup(player, true);
        
        final Location startLocation = player.getLocation().clone();
        final Location backLocation = teleportManager.getBackLocation(player);
        final int warmupSeconds = teleportManager.getWarmupSeconds();
        
        new BukkitRunnable() {
            int countdown = warmupSeconds;
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    teleportManager.setWarmup(player, false);
                    cancel();
                    return;
                }
                
                // Check if player moved
                Location currentLoc = player.getLocation();
                if (currentLoc.distance(startLocation) > 0.5) {
                    player.sendActionBar(ChatUtils.color("&c&lGAGAL! &7Kamu bergerak."));
                    player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
                    teleportManager.setWarmup(player, false);
                    cancel();
                    return;
                }
                
                if (countdown <= 0) {
                    // Teleport!
                    player.teleport(backLocation);
                    player.playSound(player.getLocation(), "ENTITY_ENDERMAN_TELEPORT", 1f, 1f);
                    player.sendActionBar(ChatUtils.color("&a&lSAMPAI! &7Kembali ke lokasi terakhir."));
                    
                    teleportManager.setWarmup(player, false);
                    cancel();
                    return;
                }
                
                player.sendActionBar(ChatUtils.color("&eTeleportasi dalam &f" + countdown + " detik..."));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        return true;
    }
}
