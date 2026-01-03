package id.naturalsmp.naturalcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Utility class for config management
 * Methods marked as unused are part of public API for future features
 */
@SuppressWarnings("unused")
public class ConfigUtils {

    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;
    
    public ConfigUtils(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public void saveLocation(String path, Location location) {
        if (location.getWorld() == null) {
            plugin.getLogger().warning("Cannot save location with null world!");
            return;
        }
        
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        save();
    }
    
    public Location loadLocation(String path) {
        if (!config.contains(path + ".world")) {
            return null;
        }
        
        String worldName = config.getString(path + ".world");
        if (worldName == null) {
            return null;
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found!");
            return null;
        }
        
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public void saveItem(String path, ItemStack item) {
        config.set(path, item);
        save();
    }
    
    public ItemStack loadItem(String path) {
        return config.getItemStack(path);
    }
    
    public void saveItems(String path, List<ItemStack> items) {
        config.set(path, items);
        save();
    }
    
    public List<ItemStack> loadItems(String path) {
        List<?> list = config.getList(path);
        List<ItemStack> items = new ArrayList<>();
        
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof ItemStack) {
                    items.add((ItemStack) obj);
                }
            }
        }
        
        return items;
    }
    
    public Object get(String path) {
        return config.get(path);
    }
    
    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }
    
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config file: " + configFile.getName(), e);
        }
    }
    
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}
