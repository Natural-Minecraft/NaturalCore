package id.naturalsmp.naturalcore.chat.tags;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagsManager {

    private final NaturalCore plugin;
    private final File tagsFile;
    private final File playerFile;
    private FileConfiguration tagsConfig;
    private FileConfiguration playerConfig;

    // Cache: TagID -> Display Format
    private final Map<String, String> availableTags = new HashMap<>();
    // Cache: UUID -> TagID
    private final Map<UUID, String> playerTags = new HashMap<>();

    public TagsManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        this.playerFile = new File(plugin.getDataFolder(), "player_tags.yml");
        loadConfigs();
    }

    public void loadConfigs() {
        if (!tagsFile.exists()) {
            plugin.saveResource("tags.yml", false);
        }
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        availableTags.clear();
        if (tagsConfig.contains("tags")) {
            for (String key : tagsConfig.getConfigurationSection("tags").getKeys(false)) {
                availableTags.put(key, tagsConfig.getString("tags." + key));
            }
        }

        // Load player data to cache (optional, bisa lazy load)
        playerTags.clear();
        for (String uuidStr : playerConfig.getKeys(false)) {
            playerTags.put(UUID.fromString(uuidStr), playerConfig.getString(uuidStr));
        }
    }

    public String getPlayerTag(Player p) {
        if (!playerTags.containsKey(p.getUniqueId()))
            return "";
        String tagId = playerTags.get(p.getUniqueId());

        // Cek permission jika perlu (opsional)
        // if (!p.hasPermission("tags." + tagId)) return "";

        return availableTags.getOrDefault(tagId, "");
    }

    public boolean setPlayerTag(Player p, String tagId) {
        if (tagId == null) {
            playerTags.remove(p.getUniqueId());
            playerConfig.set(p.getUniqueId().toString(), null);
        } else {
            if (!availableTags.containsKey(tagId))
                return false;
            playerTags.put(p.getUniqueId(), tagId);
            playerConfig.set(p.getUniqueId().toString(), tagId);
        }
        savePlayerConfig();
        return true;
    }

    public Map<String, String> getAvailableTags() {
        return availableTags;
    }

    private void savePlayerConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
