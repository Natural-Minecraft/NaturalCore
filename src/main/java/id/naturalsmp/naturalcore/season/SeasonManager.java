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

    private id.naturalsmp.naturalcore.utils.TipsManager tipsManager;

    public SeasonManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadData();
        this.tipsManager = new id.naturalsmp.naturalcore.utils.TipsManager(plugin); // Init Tips
        startTasks();
    }

    private void loadData() {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        this.enabled = config.getBoolean("enabled", true);
        if (this.tipsManager != null)
            tipsManager.reload(); // Reload tips too
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
                // Update Tips Logic
                tipsManager.tick();

                // Update Players
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
        Bukkit.broadcastMessage(ChatUtils.colorize("&6&l[NaturalSMP] &fMusim telah berganti menjadi "
                + currentSeason.getIcon() + " &e" + currentSeason.name()));

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
            // Note: Calc temp is somewhat heavy.
            // Running every 2 ticks might be too much.
            // Optimization: Only calc temp every 20 ticks, but update action bar every 2
            // ticks.

            if (Bukkit.getCurrentTick() % 40 == 0) { // Update data valid every 2s
                double temp = calculateTemperature(p);
                playerTemps.put(p.getUniqueId(), temp);
                handleTempEffects(p, temp);
            }

            // Send Action Bar (Animation needs fast update)
            Double cachedTemp = playerTemps.getOrDefault(p.getUniqueId(), 20.0);
            sendActionBar(p, cachedTemp);
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
        }

        if (p.getEyeLocation().getBlock().getType() == Material.WATER) {
            base += config.getDouble("modifiers.in-water", -5.0);
        }

        for (Block b : getNearbyBlocks(loc, 3)) {
            if (b.getType() == Material.FIRE || b.getType() == Material.CAMPFIRE
                    || b.getType() == Material.SOUL_CAMPFIRE) {
                base += config.getDouble("modifiers.near-fire", 15.0);
                break;
            }
            if (b.getType() == Material.LAVA) {
                base += config.getDouble("modifiers.near-lava", 40.0);
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

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders(p, msg);
        } else {
            msg = msg.replace("%mana%", "N/A").replace("%max_mana%", "N/A");
        }

        // --- TIPS INTEGRATION ---
        // Check if Tips Animation is active
        String tipsOverride = tipsManager.getDisplay(msg);

        if (tipsOverride != null) {
            p.sendActionBar(ChatUtils.colorize(tipsOverride));
        } else {
            p.sendActionBar(ChatUtils.colorize(msg));
        }
    }

    private void handleTempEffects(Player p, double temp) {
        if (temp < 0) {
            if (currentSeason == Season.WINTER && p.getLocation().getBlock().getType() == Material.WATER) {
                p.damage(1.0);
                p.sendTitle("", ChatUtils.colorize("&b&lFREEZING!"), 0, 20, 10);
            }
        }
    }

    private java.util.List<Block> getNearbyBlocks(Location loc, int radius) {
        java.util.List<Block> blocks = new java.util.ArrayList<>();
        int pX = loc.getBlockX();
        int pY = loc.getBlockY();
        int pZ = loc.getBlockZ();

        for (int x = pX - radius; x <= pX + radius; x++) {
            for (int y = pY - radius; y <= pY + radius; y++) {
                for (int z = pZ - radius; z <= pZ + radius; z++) {
                    blocks.add(loc.getWorld().getBlockAt(x, y, z));
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
        }
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }
}
