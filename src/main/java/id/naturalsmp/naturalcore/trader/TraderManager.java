package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TraderManager {

    private final NaturalCore plugin;
    private final TradeEditor tradeEditor;
    private final File dataFile;
    private FileConfiguration dataConfig;
    
    private NPC currentTrader;
    private BukkitRunnable spawnTask;
    private BukkitRunnable despawnTask;
    
    // Fixed lifespan: 5 minutes (300 seconds)
    private final long DESPAWN_DELAY_SECONDS = 300;
    
    // Configurable settings
    private long spawnIntervalSeconds;
    private Location spawnLocation;
    
    // Runtime state
    private long despawnTimestamp = 0; // When the active trader will disappear

    public TraderManager(NaturalCore plugin, TradeEditor tradeEditor) {
        this.plugin = plugin;
        this.tradeEditor = tradeEditor;
        this.dataFile = new File(plugin.getDataFolder(), "trader_data.yml");
        
        loadData();
        loadConfig();
        
        // 1. Critical Fix: Single Instance & Restart Safety
        checkActiveTraderOnStartup();
        
        // 2. Start the scheduler loop
        scheduleNextSpawn();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadConfig() {
        this.spawnIntervalSeconds = plugin.getConfig().getLong("trader.spawn-interval-seconds", 10800); // Default 3 hours
        
        if (plugin.getConfig().contains("trader.location")) {
            this.spawnLocation = plugin.getConfig().getLocation("trader.location");
        }
    }

    private void checkActiveTraderOnStartup() {
        String uuidStr = dataConfig.getString("active_trader_uuid");
        if (uuidStr != null) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(uuid);
                
                if (npc != null && npc.isSpawned()) {
                    // Found valid existing trader
                    this.currentTrader = npc;
                    plugin.getLogger().info("Restored active trader session: " + uuid);
                    
                    // Restore despawn timer (or force despawn if expired while offline)
                    // For simplicity, we'll just restart the 5-minute timer or despawn immediately if we want strictness.
                    // Let's restart the 5-minute timer to be safe/generous after a restart.
                    startDespawnTimer(); 
                } else {
                    // Ghost UUID or dead entity -> Cleanup
                    if (npc != null) npc.destroy();
                    plugin.getLogger().info("Cleaned up ghost trader UUID: " + uuid);
                    clearActiveTraderData();
                }
            } catch (IllegalArgumentException e) {
                clearActiveTraderData();
            }
        }
    }

    private void scheduleNextSpawn() {
        // If trader is currently active, don't schedule the next spawn yet.
        // The despawn logic will trigger the next schedule.
        if (currentTrader != null && currentTrader.isSpawned()) {
            return;
        }

        long nextSpawnTime = dataConfig.getLong("next_spawn_timestamp", 0);
        long currentTime = System.currentTimeMillis();

        if (nextSpawnTime <= 0) {
            // First run or reset
            nextSpawnTime = currentTime + (spawnIntervalSeconds * 1000);
            dataConfig.set("next_spawn_timestamp", nextSpawnTime);
            saveData();
        }

        long delayMillis = nextSpawnTime - currentTime;
        
        if (delayMillis <= 0) {
            // Time has passed, spawn immediately
            spawnTrader();
        } else {
            // Schedule future spawn
            long delayTicks = delayMillis / 50;
            plugin.getLogger().info("Trader scheduled to spawn in " + (delayMillis / 1000) + " seconds.");
            
            if (spawnTask != null) spawnTask.cancel();
            spawnTask = new BukkitRunnable() {
                @Override
                public void run() {
                    spawnTrader();
                }
            };
            spawnTask.runTaskLater(plugin, delayTicks);
        }
    }

    private void updateNextSpawnTime() {
        long nextTime = System.currentTimeMillis() + (spawnIntervalSeconds * 1000);
        dataConfig.set("next_spawn_timestamp", nextTime);
        saveData();
        // Re-schedule loop
        scheduleNextSpawn();
    }

    public void spawnTrader() {
        // Critical Fix: Single Instance Check
        if (currentTrader != null && currentTrader.isSpawned()) {
            plugin.getLogger().warning("Attempted to spawn trader, but one is already active!");
            return;
        }

        if (spawnLocation == null) {
            plugin.getLogger().warning("Trader spawn location not set!");
            return;
        }

        // Spawn NPC
        currentTrader = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, ChatUtils.colorize("&6&lWandering Trader"));
        currentTrader.getOrAddTrait(LookClose.class).lookClose(true);
        currentTrader.spawn(spawnLocation);
        
        // Save UUID immediately
        dataConfig.set("active_trader_uuid", currentTrader.getUniqueId().toString());
        saveData();

        Bukkit.broadcast(ChatUtils.format("&6&l[Trader] &eThe Special Wandering Trader has arrived!"));

        // Start fixed 5-minute despawn timer
        startDespawnTimer();
    }
    
    private void startDespawnTimer() {
        this.despawnTimestamp = System.currentTimeMillis() + (DESPAWN_DELAY_SECONDS * 1000);
        
        if (despawnTask != null) despawnTask.cancel();
        despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                despawnTrader();
            }
        };
        despawnTask.runTaskLater(plugin, DESPAWN_DELAY_SECONDS * 20);
    }

    public void despawnTrader() {
        if (currentTrader != null) {
            currentTrader.destroy();
            currentTrader = null;
            
            clearActiveTraderData();
            
            Bukkit.broadcast(ChatUtils.format("&6&l[Trader] &cThe Special Wandering Trader has left!"));
            
            // Schedule the next appearance
            updateNextSpawnTime();
        }
    }
    
    private void clearActiveTraderData() {
        dataConfig.set("active_trader_uuid", null);
        saveData();
        this.despawnTimestamp = 0;
    }

    public boolean isTraderActive() {
        return currentTrader != null && currentTrader.isSpawned();
    }

    public void forceSpawn() {
        if (isTraderActive()) return; // Handled by command check usually, but safe double-check
        spawnTrader();
    }

    public void forceDespawn() {
        if (!isTraderActive()) return;
        despawnTrader();
    }

    public void setSpawnLocation(Location loc) {
        this.spawnLocation = loc;
        plugin.getConfig().set("trader.location", loc);
        plugin.saveConfig();
    }

    public void setInterval(long seconds) {
        this.spawnIntervalSeconds = seconds;
        plugin.getConfig().set("trader.spawn-interval-seconds", seconds);
        plugin.saveConfig();
        
        // If trader is NOT active, recalculate the schedule immediately
        if (!isTraderActive()) {
            // Reset the timestamp to now + new interval
            updateNextSpawnTime();
        }
    }

    public String getStatusMessage() {
        if (isTraderActive()) {
            long diff = despawnTimestamp - System.currentTimeMillis();
            if (diff < 0) diff = 0;
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            
            return String.format("&aTrader is currently Active! Despawning in: %dm %ds", minutes, seconds % 60);
        } else {
            long nextSpawnTime = dataConfig.getLong("next_spawn_timestamp", 0);
            long diff = nextSpawnTime - System.currentTimeMillis();
            if (diff < 0) return "&eSpawning soon...";
            
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            
            return String.format("&eNext spawn in: %dh %dm %ds", hours, minutes % 60, seconds % 60);
        }
    }

    public NPC getCurrentTrader() {
        return currentTrader;
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void onDisable() {
        // We do NOT despawn on disable anymore to allow persistence across restarts
        // But we must ensure the UUID is saved correctly.
        if (currentTrader != null && currentTrader.isSpawned()) {
            dataConfig.set("active_trader_uuid", currentTrader.getUniqueId().toString());
            saveData();
        }
    }
}
