package id.naturalsmp.naturalcore.chat.suffix;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuffixManager {

    private final NaturalCore plugin;
    private final File suffixFile;
    private final File playerFile;
    private FileConfiguration suffixConfig;
    private FileConfiguration playerConfig;

    // Cache: SuffixID -> Display Format
    private final Map<String, String> availableSuffixes = new HashMap<>();
    // Cache: UUID -> SuffixID
    private final Map<UUID, String> playerSuffixes = new HashMap<>();

    public SuffixManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.suffixFile = new File(plugin.getDataFolder(), "suffixes.yml");
        this.playerFile = new File(plugin.getDataFolder(), "player_suffixes.yml");
        loadConfigs();
    }

    public void loadConfigs() {
        if (!suffixFile.exists()) {
            plugin.saveResource("suffixes.yml", false);
        }
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        suffixConfig = YamlConfiguration.loadConfiguration(suffixFile);
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        availableSuffixes.clear();
        if (suffixConfig.contains("suffixes")) {
            for (String key : suffixConfig.getConfigurationSection("suffixes").getKeys(false)) {
                availableSuffixes.put(key, suffixConfig.getString("suffixes." + key));
            }
        }

        playerSuffixes.clear();
        for (String uuidStr : playerConfig.getKeys(false)) {
            try {
                playerSuffixes.put(UUID.fromString(uuidStr), playerConfig.getString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Get the display string for a player's active suffix.
     * Returns empty string if no suffix is active.
     */
    public String getPlayerSuffix(Player p) {
        String suffixId = playerSuffixes.get(p.getUniqueId());
        if (suffixId == null || suffixId.isEmpty()) return "";

        // Verify permission
        if (!p.hasPermission("naturalsmp.suffix." + suffixId)) return "";

        return availableSuffixes.getOrDefault(suffixId, "");
    }

    /**
     * Get the raw suffix ID for a player.
     */
    public String getPlayerSuffixId(Player p) {
        return playerSuffixes.getOrDefault(p.getUniqueId(), "");
    }

    /**
     * Set a player's active suffix.
     * @return true if successful
     */
    public boolean setPlayerSuffix(Player p, String suffixId) {
        if (suffixId == null) {
            playerSuffixes.remove(p.getUniqueId());
            playerConfig.set(p.getUniqueId().toString(), null);
        } else {
            if (!availableSuffixes.containsKey(suffixId)) return false;
            playerSuffixes.put(p.getUniqueId(), suffixId);
            playerConfig.set(p.getUniqueId().toString(), suffixId);
        }
        savePlayerConfig();
        return true;
    }

    /**
     * Grant a suffix permission to a player via LuckPerms.
     */
    public void grantSuffix(Player target, String suffixId) {
        if (!availableSuffixes.containsKey(suffixId)) return;

        // Grant permission via LuckPerms console command
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + target.getName() + " permission set naturalsmp.suffix." + suffixId + " true");
    }

    public Map<String, String> getAvailableSuffixes() {
        return availableSuffixes;
    }

    private void savePlayerConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
