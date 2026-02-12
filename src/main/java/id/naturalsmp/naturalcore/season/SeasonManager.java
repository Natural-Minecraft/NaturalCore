package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeasonManager {

    private final NaturalCore plugin;
    private Season currentSeason;
    private int currentDay;
    private int seasonDuration;
    private boolean enabled;

    private final Map<UUID, Double> playerTemps = new HashMap<>();
    private int temperatureTickCounter = 0;

    // Deterministic noise for realistic temperature
    private final org.bukkit.util.noise.PerlinNoiseGenerator noiseGenerator = new org.bukkit.util.noise.PerlinNoiseGenerator(
            new java.util.Random(12345L));

    public SeasonManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadData();
        // Tips moved to dedicated HUD Component
        startTasks();
    }

    public void loadData() {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        this.enabled = config.getBoolean("enabled", true);
        this.seasonDuration = config.getInt("season-duration-days", 7);
        this.currentDay = config.getInt("current-day", 1);

        String seasonName = config.getString("current-season", "SPRING");
        try {
            this.currentSeason = Season.valueOf(seasonName);
        } catch (IllegalArgumentException e) {
            this.currentSeason = Season.SPRING;
        }
    }

    public void saveData() {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        config.set("current-season", currentSeason.name());
        config.set("current-day", currentDay);
        try {
            config.save(new File(plugin.getDataFolder(), "season.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTasks() {
        if (!enabled)
            return;

        // 1. Task per 1 menit (MC Day tracker)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkDayCycle();
            }
        }.runTaskTimer(plugin, 1200L, 1200L);

        // 2. Action Bar & Temperature Task (Dipercepat ke 2 Tick untuk Animasi)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Update Players
                temperatureTickCounter++;
                updateAllPlayers();
            }
        }.runTaskTimer(plugin, 2L, 2L); // 2 Ticks = Smooth Animation
    }

    private void checkDayCycle() {
        long fullTime = Bukkit.getWorlds().get(0).getFullTime();
        int day = (int) (fullTime / 24000);

        if (day > currentDay) {
            currentDay++;
            if (currentDay > seasonDuration) {
                currentDay = 1;
                nextSeason();
            }
            saveData();
        }
    }

    public void nextSeason() {
        this.currentSeason = currentSeason.next();
        GUIUtils.broadcast("&6&l[NaturalSMP] &fMusim telah berganti menjadi "
                + currentSeason.getIcon() + " &e" + currentSeason.name());

        if (ConfigUtils.getSeasonConfig().getBoolean("visuals.enabled", true)) {
            refreshVisuals();
        }
        saveData();
    }

    public void forceSetSeason(Season season) {
        this.currentSeason = season;
        GUIUtils.broadcast("&6&l[NaturalSMP] &fAdmin mengubah musim menjadi "
                + currentSeason.getIcon() + " &e" + currentSeason.name());
        if (ConfigUtils.getSeasonConfig().getBoolean("visuals.enabled", true)) {
            refreshVisuals();
        }
        saveData();
    }

    private void updateAllPlayers() {
        boolean recalculate = (temperatureTickCounter % 10 == 0); // Every 20 ticks (1s)

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (recalculate) {
                double temp = calculateTemperature(p);
                playerTemps.put(p.getUniqueId(), temp);
                handleTempEffects(p, temp);
            }
        }
    }

    private double calculateTemperature(Player p) {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        double base = config.getDouble("seasons." + currentSeason.name() + ".temp-base", 20.0);

        Location loc = p.getLocation();
        long time = loc.getWorld().getTime();

        if (time > 13000 && time < 23000) {
            base += config.getDouble("modifiers.night", -10.0);
        } else if (time > 4000 && time < 8000) {
            base += config.getDouble("modifiers.noon", 5.0);
        }

        if (loc.getWorld().hasStorm()) {
            base += config.getDouble("modifiers.storm", -5.0);
        } else if (loc.getWorld().isThundering() || loc.getWorld().hasStorm()) { // Handle rain too
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
            base += 2.0; // Warming up from running
        } else if (p.isConversing()) {
            // No heat change if talking (example usage if API existed, here just
            // placeholder or isWalking check)
        } else {
            // Basic movement check could be done via velocity, but Bukkit API is simpler:
            // If player key input is active? Hard to detect.
            // We'll stick to Sprinting for major heat, maybe check velocity magnitude next
            // time.
        }

        // 2. Realistic Environmental Fluctuation (Wind/Atmosphere)
        // Uses Perlin Noise for deterministic, shared, and smooth temperature changes.
        // Scale: 0.02 (Changes every ~50 blocks)
        // Time Scale: 0.005 (Changes every ~200 ticks / 10 seconds)
        double noise = noiseGenerator.noise(
                loc.getX() * 0.02,
                loc.getZ() * 0.02,
                (double) time * 0.005);

        // Noise returns -1 to 1. Scale to +/- 2.0 degrees.
        base += noise * 2.0;

        // Optimize: Check nearby blocks directly without allocation

        // Optimize: Check nearby blocks directly without allocation
        int radius = 2;
        int pX = loc.getBlockX();
        int pY = loc.getBlockY();
        int pZ = loc.getBlockZ();

        boolean foundHeat = false;

        for (int x = pX - radius; x <= pX + radius; x++) {
            for (int y = pY - radius; y <= pY + radius; y++) {
                for (int z = pZ - radius; z <= pZ + radius; z++) {
                    Material type = loc.getWorld().getBlockAt(x, y, z).getType();
                    if (type == Material.FIRE || type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE) {
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
                base += 30.0; // Nether is HOT
                break;
            case THE_END:
                base -= 10.0; // End is COLD
                break;
            default:
                break;
        }

        // --- ARMOR BUFFS (Insulation) ---
        // Leather armor neutralizes extreme temperatures (Buff)
        // Metal armor conducts heat (Debuff in heat/cold)
        if (p.getInventory().getArmorContents() != null) {
            for (org.bukkit.inventory.ItemStack armor : p.getInventory().getArmorContents()) {
                if (armor == null || armor.getType() == Material.AIR)
                    continue;
                String type = armor.getType().name();

                if (type.contains("LEATHER")) {
                    // Leather insulates: moves temp closer to neutral (20.0)
                    if (base > 25.0)
                        base -= 2.0; // Cools down in heat
                    else if (base < 15.0)
                        base += 2.0; // Warms up in cold
                } else if (type.contains("IRON") || type.contains("GOLD") || type.contains("DIAMOND")
                        || type.contains("NETHERITE")) {
                    // Metal conducts: makes hot hotter, cold colder
                    // Only applies if temp is already extreme to avoid annoying fluctuations
                    if (base > 35.0)
                        base += 1.5;
                    else if (base < 5.0)
                        base -= 1.5;
                }
            }
        }

        return base;
    }

    public String getTemperatureActionBar(Player p) {
        Double temp = playerTemps.getOrDefault(p.getUniqueId(), 20.0);
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        String format = config.getString("action-bar.format");
        if (format == null)
            return null;

        // Custom placeholders
        String msg = format
                .replace("%icon%", currentSeason.getIcon())
                .replace("%name%",
                        config.getString("seasons." + currentSeason.name() + ".display-name", currentSeason.name()))
                .replace("%temp%", String.format("%.1f", temp));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders(p, msg);
        } else {
            msg = msg.replace("%mana%", "N/A").replace("%max_mana%", "N/A");
        }

        return msg;
    }

    private void handleTempEffects(Player p, double temp) {
        if (temp < 0) {
            if (currentSeason == Season.WINTER && p.getLocation().getBlock().getType() == Material.WATER) {
                p.damage(1.0);
                p.sendTitle("", ChatUtils.colorize("&b&lFREEZING!"), 0, 20, 10);
            }
        }
    }

    public void refreshVisuals() {
        String biomeName = ConfigUtils.getSeasonConfig().getString("visuals.biomes." + currentSeason.name(), "PLAINS");
        try {
            org.bukkit.block.Biome targetBiome = org.bukkit.block.Biome.valueOf(biomeName);
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != org.bukkit.World.Environment.NORMAL)
                    continue;
                for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                    int minX = chunk.getX() << 4;
                    int minZ = chunk.getZ() << 4;
                    for (int x = minX; x <= minX + 15; x += 4) {
                        for (int z = minZ; z <= minZ + 15; z += 4) {
                            world.setBiome(x, 64, z, targetBiome);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    /**
     * Get the cached temperature for a player.
     * Used by HUD components.
     */
    public Double getPlayerTemperature(org.bukkit.entity.Player player) {
        return playerTemps.getOrDefault(player.getUniqueId(), 20.0);
    }
}
