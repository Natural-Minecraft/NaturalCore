package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeasonManager {

    private final NaturalCore plugin;
    private final SeasonRegionManager regionManager; // Keep for compatibility but it will wrap global
    private boolean enabled;

    private Season currentSeason = Season.SPRING;
    private final Map<UUID, Double> playerTemps = new HashMap<>();
    private int temperatureTickCounter = 0;

    // Deterministic noise for realistic temperature
    private final org.bukkit.util.noise.PerlinNoiseGenerator noiseGenerator = new org.bukkit.util.noise.PerlinNoiseGenerator(
            new java.util.Random(12345L));

    public SeasonManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.regionManager = new SeasonRegionManager(plugin, this); // Modified constructor
        loadData();
        startTasks();
    }

    public void loadData() {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        this.enabled = config.getBoolean("enabled", true);
    }

    public void saveData() {
        // No global data to save anymore
    }

    private void startTasks() {
        if (!enabled)
            return;

        // 1. Action Bar, Temperature & Region Check Task
        new BukkitRunnable() {
            @Override
            public void run() {
                temperatureTickCounter++;
                updateAllPlayers();
            }
        }.runTaskTimer(plugin, 2L, 2L); // 2 Ticks = Smooth Animation
    }

    private void updateAllPlayers() {
        boolean recalculate = (temperatureTickCounter % 10 == 0); // Every 20 ticks (1s)

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Region Check (every 1s is enough, or even slower)
            if (temperatureTickCounter % 20 == 0) {
                regionManager.updatePlayerRegion(p);
            }

            if (recalculate) {
                double temp = calculateTemperature(p);
                playerTemps.put(p.getUniqueId(), temp);
                handleTempEffects(p, temp);
            }
        }
    }

    public double calculateTemperature(Player p) {
        Location loc = p.getLocation();
        Season currentSeason = regionManager.getSeason(loc);

        FileConfiguration config = ConfigUtils.getSeasonConfig();
        double base = config.getDouble("seasons." + currentSeason.name() + ".temp-base", 20.0);

        long time = loc.getWorld().getTime();

        if (time > 13000 && time < 23000) {
            base += config.getDouble("modifiers.night", -10.0);
        } else if (time > 4000 && time < 8000) {
            base += config.getDouble("modifiers.noon", 5.0);
        }

        if (loc.getWorld().hasStorm()) {
            base += config.getDouble("modifiers.storm", -5.0);
        } else if (loc.getWorld().isThundering() || loc.getWorld().hasStorm()) {
            base += config.getDouble("modifiers.rain", -3.0);
        }

        if (p.getLocation().getBlockY() < 60) {
            base += config.getDouble("modifiers.underground", -4.0);
        }

        if (p.getLocation().getBlock().getType() == Material.WATER
                || p.getEyeLocation().getBlock().getType() == Material.WATER) {
            base += config.getDouble("modifiers.in-water", -5.0);
        }

        // --- DYNAMIC FLUCTUATIONS ---

        // 1. Movement Heat
        if (p.isSprinting()) {
            base += 2.0;
        }

        // 2. Realistic Environmental Fluctuation
        double noise = noiseGenerator.noise(
                loc.getX() * 0.02,
                loc.getZ() * 0.02,
                (double) time * 0.005);
        base += noise * 2.0;

        // 3. Heat Sources (Torches, Fire, Lava)
        int radius = 3;
        int pX = loc.getBlockX();
        int pY = loc.getBlockY();
        int pZ = loc.getBlockZ();

        boolean foundHeat = false;

        for (int x = pX - radius; x <= pX + radius; x++) {
            for (int y = pY - radius; y <= pY + radius; y++) {
                for (int z = pZ - radius; z <= pZ + radius; z++) {
                    Material type = loc.getWorld().getBlockAt(x, y, z).getType();
                    if (isHeatSource(type)) {
                        base += config.getDouble("modifiers.near-fire", 15.0);
                        foundHeat = true;
                        break;
                    }
                    if (type == Material.LAVA) {
                        base += config.getDouble("modifiers.near-lava", 40.0);
                        foundHeat = true;
                        break;
                    }
                }
                if (foundHeat)
                    break;
            }
            if (foundHeat)
                break;
        }

        // --- WORLD BASED TEMPERATURE ---
        switch (loc.getWorld().getEnvironment()) {
            case NETHER:
                base += 30.0;
                break;
            case THE_END:
                base -= 10.0;
                break;
            default:
                break;
        }

        // --- ARMOR BUFFS (Insulation) ---
        if (p.getInventory().getArmorContents() != null) {
            for (org.bukkit.inventory.ItemStack armor : p.getInventory().getArmorContents()) {
                if (armor == null || armor.getType() == Material.AIR)
                    continue;
                String type = armor.getType().name();

                if (type.contains("LEATHER")) {
                    // Leather insulates towards neutral (20.0)
                    if (base > 25.0)
                        base -= 3.0;
                    else if (base < 15.0)
                        base += 3.0;
                } else if (type.contains("IRON") || type.contains("GOLD") || type.contains("DIAMOND")
                        || type.contains("NETHERITE")) {
                    // Metal conducts extremes
                    if (base > 35.0)
                        base += 2.0;
                    else if (base < 5.0)
                        base -= 2.0;
                }
            }
        }

        return base;
    }

    private boolean isHeatSource(Material mat) {
        return mat == Material.FIRE || mat == Material.CAMPFIRE || mat == Material.SOUL_CAMPFIRE
                || mat == Material.TORCH || mat == Material.LANTERN || mat == Material.JACK_O_LANTERN
                || mat == Material.GLOWSTONE || mat == Material.MAGMA_BLOCK;
    }

    public String getTemperatureActionBar(Player p) {
        Double temp = playerTemps.getOrDefault(p.getUniqueId(), 20.0);
        Season season = regionManager.getSeason(p.getLocation());

        FileConfiguration config = ConfigUtils.getSeasonConfig();
        String format = config.getString("action-bar.format");
        if (format == null)
            return null;

        String msg = format
                .replace("%icon%", season.getIcon())
                .replace("%name%", config.getString("seasons." + season.name() + ".display-name", season.name()))
                .replace("%temp%", String.format("%.1f", temp));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders(p, msg);
        }

        return msg;
    }

    private void handleTempEffects(Player p, double temp) {
        Season season = regionManager.getSeason(p.getLocation());
        if (temp < 0) {
            if (season == Season.WINTER && p.getLocation().getBlock().getType() == Material.WATER) {
                p.damage(1.0);
                p.sendTitle("", ChatUtils.colorize("&b&lFREEZING!"), 0, 20, 10);
            }
        }
    }

    // --- BLOCK PHYSICS LOGIC ---
    public void checkBlockState(Block block) {
        if (!enabled)
            return;
        Season season = regionManager.getSeason(block.getLocation());
        Material type = block.getType();

        // Winter Logic
        if (season == Season.WINTER) {
            // Water freezing logic is handled by vanilla usually, but we can enforce it.
            // Here we want to MELT ice if near heat.
            if (type == Material.ICE || type == Material.SNOW || type == Material.SNOW_BLOCK) {
                if (isNearHeat(block)) {
                    if (type == Material.ICE)
                        block.setType(Material.WATER);
                    else if (type == Material.SNOW)
                        block.setType(Material.AIR);
                }
            }
        } else {
            // Summer/Spring/Autumn -> Melt Ice/Snow
            if (type == Material.ICE) {
                block.setType(Material.WATER);
            } else if (type == Material.SNOW) {
                block.setType(Material.AIR);
            }
        }
    }

    // Check heat source in 3 block radius
    private boolean isNearHeat(Block center) {
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (isHeatSource(center.getRelative(x, y, z).getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Season getSeason(Location loc) {
        return regionManager.getSeason(loc);
    }

    // Deprecated/Unused legacy methods kept to prevent compilation errors if called
    // elsewhere
    public Season getCurrentSeason() {
        return Season.SPRING;
    }

    public void nextSeason() {
    }

    public void forceSetSeason(Season s) {
    }

    public Double getPlayerTemperature(org.bukkit.entity.Player player) {
        return playerTemps.getOrDefault(player.getUniqueId(), 20.0);
    }

    public SeasonRegionManager getRegionManager() {
        return regionManager;
    }
}
