package id.naturalsmp.naturalcore.home;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager implements Listener {

    private final NaturalCore plugin;
    private final File folder;
    private final Map<UUID, FileConfiguration> homeCache = new ConcurrentHashMap<>();

    public HomeManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "homes");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // --- LISTENER (AUTO-LOAD/UNLOAD) ---
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        loadUser(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        unloadUser(e.getPlayer().getUniqueId());
    }

    // --- CACHE MANAGEMENT ---
    public void loadUser(UUID uuid) {
        if (!homeCache.containsKey(uuid)) {
            File file = getPlayerFile(uuid);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            homeCache.put(uuid, config);
        }
    }

    public void unloadUser(UUID uuid) {
        homeCache.remove(uuid);
    }

    private FileConfiguration getPlayerConfig(UUID uuid) {
        // Fallback if not cached (e.g. offline lookup or failed load)
        if (!homeCache.containsKey(uuid)) {
            loadUser(uuid);
        }
        return homeCache.get(uuid);
    }

    // --- FILE HANDLING ---
    private File getPlayerFile(UUID uuid) {
        return new File(folder, uuid.toString() + ".yml");
    }

    private void savePlayerConfigAsync(UUID uuid, FileConfiguration config) {
        // Run I/O asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(getPlayerFile(uuid));
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save home data for " + uuid);
                e.printStackTrace();
            }
        });
    }

    // --- CORE METHODS ---
    public void setHome(Player p, String name, Location loc) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("homes." + name, loc);

        // Update cache is implicitly done since config is a reference
        // Push to disk async
        savePlayerConfigAsync(uuid, config);
    }

    public void deleteHome(Player p, String name) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("homes." + name, null);
        savePlayerConfigAsync(uuid, config);
    }

    public Location getHome(Player p, String name) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        return config.getLocation("homes." + name);
    }

    public boolean hasHome(Player p, String name) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        return config.contains("homes." + name);
    }

    public Set<String> getHomes(Player p) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        if (!config.contains("homes")) {
            return Collections.emptySet();
        }
        return config.getConfigurationSection("homes").getKeys(false);
    }

    // --- HELPER METHODS ---

    // 1. Get Sorted Homes (Dipakai GUI)
    public List<String> getSortedHomes(Player p) {
        Set<String> homes = getHomes(p);
        List<String> sortedList = new ArrayList<>(homes);
        Collections.sort(sortedList);
        return sortedList;
    }

    // 2. Teleport Home (Dipakai Command & GUI)
    public void teleportHome(Player p, String name) {
        Location loc = getHome(p, name);
        if (loc == null) {
            ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-not-found", "%name%", name);
            return;
        }
        p.teleport(loc);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        ConfigUtils.sendMessage(p, "prefix.home", "messages.home.home-teleport", "%location%", name);
    }

    // --- LIMITS ---
    public int getMaxHomes(Player p) {
        if (p.hasPermission("naturalsmp.home.limit.unlimited"))
            return 999;

        ConfigurationSection limitSection = plugin.getConfig().getConfigurationSection("home.limits");
        if (limitSection == null)
            return 2;

        int highestLimit = 0;
        for (String rank : limitSection.getKeys(false)) {
            if (p.hasPermission("naturalsmp.home.limit." + rank)) {
                int value = limitSection.getInt(rank);
                if (value > highestLimit)
                    highestLimit = value;
            }
        }
        return highestLimit > 0 ? highestLimit : limitSection.getInt("default", 2);
    }
}