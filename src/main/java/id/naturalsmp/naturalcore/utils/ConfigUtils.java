package id.naturalsmp.naturalcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtils {

    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    
    public ConfigUtils(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Save location to config
     */
    public void saveLocation(String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        save();
    }
    
    /**
     * Load location from config
     */
    public Location loadLocation(String path) {
        if (!config.contains(path + ".world")) {
            return null;
        }
        
        String worldName = config.getString(path + ".world");
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");
        
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
    
    /**
     * Save ItemStack to config
     */
    public void saveItem(String path, ItemStack item) {
        config.set(path, item);
        save();
    }
    
    /**
     * Load ItemStack from config
     */
    public ItemStack loadItem(String path) {
        return config.getItemStack(path);
    }
    
    /**
     * Save list of items
     */
    public void saveItems(String path, List<ItemStack> items) {
        config.set(path, items);
        save();
    }
    
    /**
     * Load list of items
     */
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
    
    /**
     * Get config value
     */
    public Object get(String path) {
        return config.get(path);
    }
    
    /**
     * Set config value
     */
    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }
    
    /**
     * Check if path exists
     */
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    /**
     * Save config to file
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config file: " + configFile.getName());
            e.printStackTrace();
        }
    }
    
    /**
     * Reload config from file
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Get the FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
