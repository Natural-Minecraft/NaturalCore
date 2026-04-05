package id.naturalsmp.naturalcore.lang;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.database.NaturalCoreDatabase;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LanguageManager implements Listener {

    private final NaturalCore plugin;
    private final NaturalCoreDatabase database;
    private final Map<UUID, String> playerLanguages = new HashMap<>();

    // Cached language configs (e.g., "id" -> YamlConfiguration)
    private final Map<String, FileConfiguration> languageConfigs = new HashMap<>();

    public LanguageManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.database = plugin.getCoreDatabase();
        
        loadLanguages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void loadLanguages() {
        languageConfigs.clear();
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        // Save default languages if not exist
        saveDefaultLanguage("id");
        saveDefaultLanguage("en");

        // Load all .yml files in the lang directory
        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String langCode = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                languageConfigs.put(langCode, config);
                plugin.getLogger().info("Loaded language file: " + file.getName());
            }
        }
    }

    private void saveDefaultLanguage(String langCode) {
        File file = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");
        if (!file.exists()) {
            // Attempt to save from resource if available
            InputStream in = plugin.getResource("lang/" + langCode + ".yml");
            if (in != null) {
                plugin.saveResource("lang/" + langCode + ".yml", false);
            } else {
                // If resource doesn't exist, try copying from main messages.yml as a fallback for 'id'
                if (langCode.equals("id")) {
                    File mainMsg = new File(plugin.getDataFolder(), "messages.yml");
                    if (mainMsg.exists()) {
                        try {
                            java.nio.file.Files.copy(mainMsg.toPath(), file.toPath());
                            plugin.getLogger().info("Created lang/id.yml from messages.yml");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Create empty
                        try { file.createNewFile(); } catch (Exception ignored) {}
                    }
                } else {
                    try { file.createNewFile(); } catch (Exception ignored) {}
                }
            }
        }
    }

    public String getLanguage(UUID uuid) {
        return playerLanguages.getOrDefault(uuid, "id");
    }

    public void setLanguage(Player player, String langCode) {
        playerLanguages.put(player.getUniqueId(), langCode);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            database.setLanguage(player.getUniqueId(), langCode);
        });
    }

    public String getRawMessage(String langCode, String path) {
        if (!languageConfigs.containsKey(langCode)) {
            // Fallback to id
            langCode = "id";
        }

        FileConfiguration config = languageConfigs.get(langCode);
        if (config != null && config.contains(path)) {
            return config.getString(path);
        }

        // Final fallback: try internal messages if available
        return null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String lang = database.getLanguage(uuid);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                playerLanguages.put(uuid, lang);
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLanguages.remove(event.getPlayer().getUniqueId());
    }
}
