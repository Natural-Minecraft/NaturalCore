package id.naturalsmp.naturalcore.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ConfigUpdater {

    /**
     * Updates a configuration file by adding missing keys from the template in
     * resources.
     * Preserves existing values and comments.
     */
    public static void updateConfig(JavaPlugin plugin, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            return;
        }

        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);
        InputStream resourceStream = plugin.getResource(fileName);
        if (resourceStream == null)
            return;

        YamlConfiguration templateConfig = YamlConfiguration
                .loadConfiguration(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));

        boolean modified = false;

        // 1. Add missing keys
        Set<String> templateKeys = templateConfig.getKeys(true);
        for (String key : templateKeys) {
            if (!currentConfig.contains(key)) {
                currentConfig.set(key, templateConfig.get(key));
                modified = true;
            }
        }

        // 2. Sync version if applicable
        if (templateConfig.contains("config-version")) {
            int templateVer = templateConfig.getInt("config-version");
            int currentVer = currentConfig.getInt("config-version", 0);
            if (templateVer > currentVer) {
                currentConfig.set("config-version", templateVer);
                modified = true;
            }
        }

        if (modified) {
            try {
                currentConfig.save(file);
                plugin.getLogger().info("Updated " + fileName + " with new configuration keys.");
            } catch (Exception e) {
                plugin.getLogger().severe("Could not save updated config " + fileName + ": " + e.getMessage());
            }
        }
    }
}
