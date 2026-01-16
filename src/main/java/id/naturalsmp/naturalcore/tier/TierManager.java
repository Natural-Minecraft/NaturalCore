package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TierManager {

    private final NaturalCore plugin;
    private final File tiersFile;
    private final File playerFile;
    private FileConfiguration tiersConfig;
    private FileConfiguration playerConfig;

    // Rank Level Data (1, 2, 3...)
    // Map<Level, TierObject>
    private final Map<Integer, Tier> tierLevels = new TreeMap<>();

    // Player Data Cache
    private final Map<UUID, Integer> playerTiers = new HashMap<>();

    public TierManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.tiersFile = new File(plugin.getDataFolder(), "tiers.yml");
        this.playerFile = new File(plugin.getDataFolder(), "player_tiers.yml");
        loadConfigs();
    }

    public void loadConfigs() {
        if (!tiersFile.exists())
            plugin.saveResource("tiers.yml", false);
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        tiersConfig = YamlConfiguration.loadConfiguration(tiersFile);
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        // Load Tiers
        tierLevels.clear();
        if (tiersConfig.contains("tiers")) {
            for (String key : tiersConfig.getConfigurationSection("tiers").getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    String id = tiersConfig.getString("tiers." + key + ".id");
                    String display = tiersConfig.getString("tiers." + key + ".display");
                    String suffix = tiersConfig.getString("tiers." + key + ".suffix");

                    // Req
                    double reqMoney = tiersConfig.getDouble("tiers." + key + ".requirements.money", 0);
                    int reqKills = tiersConfig.getInt("tiers." + key + ".requirements.mob_kills", 0);

                    tierLevels.put(level, new Tier(level, id, display, suffix, reqMoney, reqKills));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid tier level: " + key);
                }
            }
        }

        // Load Players
        playerTiers.clear();
        for (String uuidStr : playerConfig.getKeys(false)) {
            playerTiers.put(UUID.fromString(uuidStr), playerConfig.getInt(uuidStr));
        }
    }

    // --- API ---

    public int getPlayerLevel(Player p) {
        return playerTiers.getOrDefault(p.getUniqueId(), 0); // 0 = Unranked
    }

    public Tier getTier(int level) {
        return tierLevels.get(level);
    }

    public Tier getCurrentTier(Player p) {
        return getTier(getPlayerLevel(p));
    }

    public Tier getNextTier(Player p) {
        return getTier(getPlayerLevel(p) + 1);
    }

    public String getPlayerSuffix(Player p) {
        Tier t = getCurrentTier(p);
        return t != null ? ChatUtils.colorize(t.suffix) : "";
    }

    public boolean canRankUp(Player p) {
        Tier next = getNextTier(p);
        if (next == null)
            return false; // Max Rank

        // Check Money
        if (plugin.getProfileManager().getVaultBalance(p) < next.reqMoney)
            return false;

        // Check Kills
        if (p.getStatistic(Statistic.MOB_KILLS) < next.reqKills)
            return false;

        return true;
    }

    public boolean rankUp(Player p) {
        if (!canRankUp(p))
            return false;

        Tier next = getNextTier(p);

        // Deduct Money
        plugin.getVaultManager().getEconomy().withdrawPlayer(p, next.reqMoney);

        // Set Level
        playerTiers.put(p.getUniqueId(), next.level);
        playerConfig.set(p.getUniqueId().toString(), next.level);
        savePlayerData();

        return true;
    }

    private void savePlayerData() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- LEADERBOARD ---
    public Map<String, Integer> getTopPlayers(int limit) {
        // Sort playerConfig keys by value
        // Warning: This reads from file/memory cache. For heavy production, use
        // database.
        // For SMP, this map sort is fine.

        return playerTiers.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        e -> Bukkit.getOfflinePlayer(e.getKey()).getName(),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    // --- INNER CLASS ---
    public static class Tier {
        public int level;
        public String id;
        public String display;
        public String suffix;
        public double reqMoney;
        public int reqKills;

        public Tier(int level, String id, String display, String suffix, double reqMoney, int reqKills) {
            this.level = level;
            this.id = id;
            this.display = display;
            this.suffix = suffix;
            this.reqMoney = reqMoney;
            this.reqKills = reqKills;
        }
    }
}
