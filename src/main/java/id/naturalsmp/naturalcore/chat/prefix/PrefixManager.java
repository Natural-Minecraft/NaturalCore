package id.naturalsmp.naturalcore.chat.prefix;

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

public class PrefixManager {

    private final NaturalCore plugin;
    private final File prefixFile;
    private final File playerFile;
    private FileConfiguration prefixConfig;
    private FileConfiguration playerConfig;

    // Cache: PrefixID -> Display Format
    private final Map<String, String> availablePrefixes = new HashMap<>();
    // Cache: UUID -> PrefixID
    private final Map<UUID, String> playerPrefixes = new HashMap<>();

    public PrefixManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.prefixFile = new File(plugin.getDataFolder(), "prefixes.yml");
        this.playerFile = new File(plugin.getDataFolder(), "player_prefixes.yml");
        loadConfigs();
    }

    public void loadConfigs() {
        if (!prefixFile.exists()) {
            plugin.saveResource("prefixes.yml", false);
        }
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        prefixConfig = YamlConfiguration.loadConfiguration(prefixFile);
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        availablePrefixes.clear();
        if (prefixConfig.contains("prefixes")) {
            for (String key : prefixConfig.getConfigurationSection("prefixes").getKeys(false)) {
                availablePrefixes.put(key, prefixConfig.getString("prefixes." + key));
            }
        }

        playerPrefixes.clear();
        for (String uuidStr : playerConfig.getKeys(false)) {
            try {
                playerPrefixes.put(UUID.fromString(uuidStr), playerConfig.getString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Get the display string for a player's active prefix.
     * Returns empty string if no prefix is active.
     */
    public String getPlayerPrefix(Player p) {
        String prefixId = playerPrefixes.get(p.getUniqueId());
        if (prefixId == null || prefixId.isEmpty()) return "";

        // Verify permission
        if (!p.hasPermission("naturalsmp.prefix." + prefixId)) return "";

        return availablePrefixes.getOrDefault(prefixId, "");
    }

    /**
     * Get the raw prefix ID for a player.
     */
    public String getPlayerPrefixId(Player p) {
        return playerPrefixes.getOrDefault(p.getUniqueId(), "");
    }

    /**
     * Set a player's active prefix.
     * @return true if successful
     */
    public boolean setPlayerPrefix(Player p, String prefixId) {
        if (prefixId == null) {
            playerPrefixes.remove(p.getUniqueId());
            playerConfig.set(p.getUniqueId().toString(), null);
        } else {
            if (!availablePrefixes.containsKey(prefixId)) return false;
            playerPrefixes.put(p.getUniqueId(), prefixId);
            playerConfig.set(p.getUniqueId().toString(), prefixId);
        }
        savePlayerConfig();
        return true;
    }

    /**
     * Create a new prefix option.
     * @param id The prefix ID (e.g. "vip_star")
     * @param display The display format with color codes
     * @return true if created successfully
     */
    public boolean createPrefix(String id, String display) {
        if (availablePrefixes.containsKey(id)) return false;

        availablePrefixes.put(id, display);
        prefixConfig.set("prefixes." + id, display);
        savePrefixConfig();
        return true;
    }

    /**
     * Grant a prefix permission to a player via LuckPerms.
     */
    public void grantPrefix(Player target, String prefixId) {
        if (!availablePrefixes.containsKey(prefixId)) return;

        // Grant permission via LuckPerms console command
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + target.getName() + " permission set naturalsmp.prefix." + prefixId + " true");
    }

    public Map<String, String> getAvailablePrefixes() {
        return availablePrefixes;
    }

    private void savePlayerConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePrefixConfig() {
        try {
            prefixConfig.save(prefixFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
