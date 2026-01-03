package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final NaturalCore plugin;
    
    // TPA requests: target UUID -> requester UUID
    private final Map<UUID, UUID> tpaRequests;
    // Request time: target UUID -> timestamp
    private final Map<UUID, Long> requestTime;
    // TPA cooldown: player UUID -> last use time
    private final Map<UUID, Long> tpaCooldown;
    // Back locations: player UUID -> last death/teleport location
    private final Map<UUID, Location> backLocations;
    // Warmup tracking: player UUID -> true (if currently warming up)
    private final Map<UUID, Boolean> warmupActive;
    
    private final int TP_WARMUP_SECONDS;
    private final int TPA_COOLDOWN_SECONDS;
    private final int REQUEST_TIMEOUT_SECONDS;
    
    public TeleportManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.tpaRequests = new HashMap<>();
        this.requestTime = new HashMap<>();
        this.tpaCooldown = new HashMap<>();
        this.backLocations = new HashMap<>();
        this.warmupActive = new HashMap<>();
        
        // Load from config
        this.TP_WARMUP_SECONDS = plugin.getConfig().getInt("teleport.warmup-seconds", 3);
        this.TPA_COOLDOWN_SECONDS = plugin.getConfig().getInt("teleport.tpa-cooldown-seconds", 60);
        this.REQUEST_TIMEOUT_SECONDS = plugin.getConfig().getInt("teleport.request-timeout-seconds", 60);
    }
    
    // TPA Request
    public void createRequest(Player requester, Player target) {
        tpaRequests.put(target.getUniqueId(), requester.getUniqueId());
        requestTime.put(target.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean hasRequest(Player target) {
        return tpaRequests.containsKey(target.getUniqueId());
    }
    
    public Player getRequester(Player target) {
        if (!hasRequest(target)) {
            return null;
        }
        UUID requesterUUID = tpaRequests.get(target.getUniqueId());
        return plugin.getServer().getPlayer(requesterUUID);
    }
    
    public void removeRequest(Player target) {
        tpaRequests.remove(target.getUniqueId());
        requestTime.remove(target.getUniqueId());
    }
    
    public boolean isRequestExpired(Player target) {
        if (!requestTime.containsKey(target.getUniqueId())) {
            return true;
        }
        long requestedAt = requestTime.get(target.getUniqueId());
        long elapsed = (System.currentTimeMillis() - requestedAt) / 1000;
        return elapsed > REQUEST_TIMEOUT_SECONDS;
    }
    
    // Cooldown
    public void setCooldown(Player player) {
        tpaCooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean hasCooldown(Player player) {
        if (player.hasPermission("naturalcore.tpa.bypass")) {
            return false;
        }
        if (!tpaCooldown.containsKey(player.getUniqueId())) {
            return false;
        }
        long lastUse = tpaCooldown.get(player.getUniqueId());
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return elapsed < TPA_COOLDOWN_SECONDS;
    }
    
    public int getRemainingCooldown(Player player) {
        if (!tpaCooldown.containsKey(player.getUniqueId())) {
            return 0;
        }
        long lastUse = tpaCooldown.get(player.getUniqueId());
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return (int) (TPA_COOLDOWN_SECONDS - elapsed);
    }
    
    // Back location
    public void setBackLocation(Player player, Location location) {
        backLocations.put(player.getUniqueId(), location.clone());
    }
    
    public Location getBackLocation(Player player) {
        return backLocations.get(player.getUniqueId());
    }
    
    public boolean hasBackLocation(Player player) {
        return backLocations.containsKey(player.getUniqueId());
    }
    
    // Warmup
    public void setWarmup(Player player, boolean active) {
        if (active) {
            warmupActive.put(player.getUniqueId(), true);
        } else {
            warmupActive.remove(player.getUniqueId());
        }
    }
    
    public boolean isWarming(Player player) {
        return warmupActive.containsKey(player.getUniqueId());
    }
    
    public int getWarmupSeconds() {
        return TP_WARMUP_SECONDS;
    }
}
