package id.naturalsmp.naturalcore.warp;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpManager {

    private final NaturalCore plugin;
    private final Map<String, Warp> warps = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public WarpManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadWarps();
    }

    public void createWarp(String id, Location loc) {
        // Cari slot kosong pertama
        int slot = 0;
        while (isSlotTaken(slot)) {
            slot++;
        }
        warps.put(id.toLowerCase(), new Warp(id, loc, slot));
        saveWarps();
    }

    public void deleteWarp(String id) {
        warps.remove(id.toLowerCase());
        saveWarps();
    }

    public Warp getWarp(String id) {
        return warps.get(id.toLowerCase());
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    public boolean isSlotTaken(int slot) {
        return warps.values().stream().anyMatch(w -> w.getSlot() == slot);
    }

    // --- CONFIG SYSTEM (warps.yml) ---

    public void loadWarps() {
        file = new File(plugin.getDataFolder(), "warps.yml");
        if (!file.exists()) {
            plugin.saveResource("warps.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        warps.clear();

        if (config.getConfigurationSection("warps") == null) return;

        for (String id : config.getConfigurationSection("warps").getKeys(false)) {
            String path = "warps." + id + ".";

            String displayName = config.getString(path + "display", "&a" + id);
            Material icon = Material.valueOf(config.getString(path + "icon", "ENDER_PEARL"));
            int slot = config.getInt(path + "slot", 0);
            List<String> lore = config.getStringList(path + "lore");

            // Load Location
            String worldName = config.getString(path + "world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) continue; // Skip jika world invalid

            double x = config.getDouble(path + "x");
            double y = config.getDouble(path + "y");
            double z = config.getDouble(path + "z");
            float yaw = (float) config.getDouble(path + "yaw");
            float pitch = (float) config.getDouble(path + "pitch");

            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

            warps.put(id.toLowerCase(), new Warp(id, displayName, loc, icon, slot, lore));
        }
    }

    public void saveWarps() {
        config.set("warps", null); // Reset data lama

        for (Warp w : warps.values()) {
            String path = "warps." + w.getId() + ".";
            config.set(path + "display", w.getDisplayName());
            config.set(path + "icon", w.getIcon().name());
            config.set(path + "slot", w.getSlot());
            config.set(path + "lore", w.getLore());
            config.set(path + "world", w.getLocation().getWorld().getName());
            config.set(path + "x", w.getLocation().getX());
            config.set(path + "y", w.getLocation().getY());
            config.set(path + "z", w.getLocation().getZ());
            config.set(path + "yaw", w.getLocation().getYaw());
            config.set(path + "pitch", w.getLocation().getPitch());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}