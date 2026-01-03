package id.naturalsmp.naturalcore.altar;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonCommand implements CommandExecutor {

    private final NaturalCore plugin;
    private final AltarManager altarManager;
    private final Map<UUID, Boolean> warpingPlayers;
    private final Map<UUID, Long> dungeonCooldowns;
    
    public DungeonCommand(NaturalCore plugin, AltarManager altarManager) {
        this.plugin = plugin;
        this.altarManager = altarManager;
        this.warpingPlayers = new HashMap<>();
        this.dungeonCooldowns = new HashMap<>();
        
        // Start auto-zone teleport checker
        startAutoZoneChecker();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cCommand ini hanya untuk player!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("naturalcore.dungeon")) {
            player.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        // Check if altar is complete
        if (altarManager.isAltarActive()) {
            if (altarManager.getWarpLocation() != null) {
                player.teleport(altarManager.getWarpLocation());
                player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cGerbang terkunci! Dipindahkan ke Altar."));
                player.playSound(player.getLocation(), "ENTITY_ENDERMAN_TELEPORT", 1f, 1f);
            } else {
                player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cLokasi Warp Altar belum diset!"));
            }
            return true;
        }
        
        // Check if already warping
        if (warpingPlayers.containsKey(player.getUniqueId())) {
            return true;
        }
        
        // Check target world
        if (altarManager.getTargetWorld() == null) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cAdmin belum mengatur world tujuan!"));
            return true;
        }
        
        // Check cooldown
        int cooldownSeconds = getCooldownSeconds(player);
        if (hasCooldown(player)) {
            long waited = (System.currentTimeMillis() - dungeonCooldowns.get(player.getUniqueId())) / 1000;
            long remaining = cooldownSeconds - waited;
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cTunggu &e" + formatTime(remaining) + " &clagi."));
            return true;
        }
        
        // Start warmup
        startWarmup(player);
        
        return true;
    }
    
    private void startWarmup(Player player) {
        dungeonCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        warpingPlayers.put(player.getUniqueId(), true);
        
        new BukkitRunnable() {
            int countdown = 5;
            
            @Override
            public void run() {
                if (!player.isOnline() || !warpingPlayers.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                
                if (countdown <= 0) {
                    // Teleport!
                    warpingPlayers.remove(player.getUniqueId());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvtp " + player.getName() + " " + altarManager.getTargetWorld());
                    player.playSound(player.getLocation(), "ENTITY_ENDERMAN_TELEPORT", 1f, 1f);
                    player.sendTitle(ChatUtils.color("&5&lWELCOME"), ChatUtils.color("&dTo The Dungeon"), 10, 60, 10);
                    cancel();
                    return;
                }
                
                player.sendTitle(ChatUtils.color("&b&l" + countdown), ChatUtils.color("&7Menuju Dungeon..."), 0, 20, 5);
                player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 2f);
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void startAutoZoneChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!altarManager.isAltarActive() && altarManager.getTriggerLocation() != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (altarManager.isInAltarZone(player.getLocation())) {
                            if (!warpingPlayers.containsKey(player.getUniqueId())) {
                                Bukkit.dispatchCommand(player, "dungeon");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private int getCooldownSeconds(Player player) {
        if (player.hasPermission("naturalcore.nature")) {
            return 0;
        } else if (player.hasPermission("naturalcore.mvp")) {
            return 60; // 1 minute
        } else {
            return 120; // 2 minutes
        }
    }
    
    private boolean hasCooldown(Player player) {
        if (player.hasPermission("naturalcore.nature")) {
            return false;
        }
        if (!dungeonCooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        long lastUse = dungeonCooldowns.get(player.getUniqueId());
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return elapsed < getCooldownSeconds(player);
    }
    
    private String formatTime(long seconds) {
        if (seconds >= 60) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + " menit " + secs + " detik";
        }
        return seconds + " detik";
    }
}
