package id.naturalsmp.naturalcore.media;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MediaManager {

    private final NaturalCore plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    private final Map<UUID, String> mediaLinks = new HashMap<>();

    public MediaManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "media.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        mediaLinks.clear();

        if (dataConfig.contains("links")) {
            for (String uuidStr : dataConfig.getConfigurationSection("links").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String link = dataConfig.getString("links." + uuidStr);
                    mediaLinks.put(uuid, link);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveData() {
        dataConfig.set("links", null);
        for (Map.Entry<UUID, String> entry : mediaLinks.entrySet()) {
            dataConfig.set("links." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLink(UUID uuid) {
        return mediaLinks.getOrDefault(uuid, "Belum diatur");
    }

    public void setLink(UUID uuid, String link) {
        mediaLinks.put(uuid, link);
        saveData();
    }

    public boolean hasLink(UUID uuid) {
        return mediaLinks.containsKey(uuid);
    }

    public void removeLink(UUID uuid) {
        mediaLinks.remove(uuid);
        saveData();
    }

    public Map<UUID, String> getAllLinks() {
        return new HashMap<>(mediaLinks);
    }
}
