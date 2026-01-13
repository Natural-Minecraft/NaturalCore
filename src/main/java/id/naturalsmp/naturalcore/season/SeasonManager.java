package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    public SeasonManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadData();
        startTasks();
    }

    private void loadData() {
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
        // Minecraft day is 24000 ticks. We check roughly every minute (1200 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkDayCycle();
            }
        }.runTaskTimer(plugin, 1200L, 1200L);

        // 2. Action Bar & Temperature Task (Setiap 2 detik agar tidak lag)
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayers();
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    private void checkDayCycle() {
        long fullTime = Bukkit.getWorlds().get(0).getFullTime();
        int day = (int) (fullTime / 24000);

        // Jika hari di game berubah
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
        Bukkit.broadcastMessage(ChatUtils.colorize("&6&l[NaturalSMP] &fMusim telah berganti menjadi "
                + currentSeason.getIcon() + " &e" + currentSeason.name()));

        // Update biomes for all loaded chunks if visuals enabled
        if (ConfigUtils.getSeasonConfig().getBoolean("visuals.enabled", true)) {
            refreshVisuals();
        }
        saveData();
    }

    public void forceSetSeason(Season season) {
        this.currentSeason = season;
        Bukkit.broadcastMessage(ChatUtils.colorize("&6&l[NaturalSMP] &fAdmin mengubah musim menjadi "
                + currentSeason.getIcon() + " &e" + currentSeason.name()));
        if (ConfigUtils.getSeasonConfig().getBoolean("visuals.enabled", true)) {
            refreshVisuals();
        }
        saveData();
    }

    private void updateAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            double temp = calculateTemperature(p);
            playerTemps.put(p.getUniqueId(), temp);
            sendActionBar(p, temp);
            handleTempEffects(p, temp);
        }
    }

    private double calculateTemperature(Player p) {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        double base = config.getDouble("seasons." + currentSeason.name() + ".temp-base", 20.0);

        Location loc = p.getLocation();
        long time = loc.getWorld().getTime();

        // 1. Time Modifiers (0-12000 is day, 12000-24000 is night)
        if (time > 13000 && time < 23000) {
            base += config.getDouble("modifiers.night", -10.0);
        } else if (time > 4000 && time < 8000) {
            base += config.getDouble("modifiers.noon", 5.0);
        }

        // 2. Weather
        if (loc.getWorld().hasStorm()) {
            base += config.getDouble("modifiers.storm", -5.0);
        }

        // 3. Environment
        if (p.getEyeLocation().getBlock().getType() == Material.WATER) {
            base += config.getDouble("modifiers.in-water", -5.0);
        }

        // Check surrounding for heat sources
        boolean nearHeat = false;
        for (Block b : getNearbyBlocks(loc, 3)) {
            if (b.getType() == Material.FIRE || b.getType() == Material.CAMPFIRE
                    || b.getType() == Material.SOUL_CAMPFIRE) {
                base += config.getDouble("modifiers.near-fire", 15.0);
                nearHeat = true;
                break;
            }
            if (b.getType() == Material.LAVA) {
                base += config.getDouble("modifiers.near-lava", 40.0);
                nearHeat = true;
                break;
            }
        }

        return base;
    }

    private void sendActionBar(Player p, double temp) {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        String format = config.getString("action-bar.format");
        if (format == null)
            return;

        // Custom placeholders
        String msg = format
                .replace("%icon%", currentSeason.getIcon())
                .replace("%name%",
                        config.getString("seasons." + currentSeason.name() + ".display-name", currentSeason.name()))
                .replace("%temp%", String.format("%.1f", temp));

        // AuraSkills & PAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            // Kita asumsikan user punya %auraskills_mana%
            msg = PlaceholderAPI.setPlaceholders(p, msg);
        } else {
            // Fallback jika tidak ada PAPI
            msg = msg.replace("%mana%", "N/A").replace("%max_mana%", "N/A");
        }

        p.sendActionBar(ChatUtils.colorize(msg));
    }

    private void handleTempEffects(Player p, double temp) {
        // Efek jika terlalu ekstrim
        if (temp < 0) {
            if (currentSeason == Season.WINTER && p.getLocation().getBlock().getType() == Material.WATER) {
                p.damage(1.0); // Kedinginan di air saat winter
                p.sendTitle("", ChatUtils.colorize("&b&lFREEZING!"), 0, 20, 10);
            }
        }
    }

    private java.util.List<Block> getNearbyBlocks(Location loc, int radius) {
        java.util.List<Block> blocks = new java.util.ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(loc.getBlock().getRelative(x, y, z));
                }
            }
        }
        return blocks;
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
            // Biome error
        }
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }
}
