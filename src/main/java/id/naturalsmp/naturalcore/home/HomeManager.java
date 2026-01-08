package id.naturalsmp.naturalcore.home;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final NaturalCore plugin;
    private final File folder;

    public HomeManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "homes");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    // --- FILE HANDLING ---
    private File getPlayerFile(UUID uuid) {
        return new File(folder, uuid.toString() + ".yml");
    }

    private FileConfiguration getPlayerConfig(UUID uuid) {
        return YamlConfiguration.loadConfiguration(getPlayerFile(uuid));
    }

    private void savePlayerConfig(UUID uuid, FileConfiguration config) {
        try {
            config.save(getPlayerFile(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CORE METHODS ---
    public void setHome(Player p, String name, Location loc) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("homes." + name, loc);
        savePlayerConfig(uuid, config);
    }

    public void deleteHome(Player p, String name) {
        UUID uuid = p.getUniqueId();
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("homes." + name, null);
        savePlayerConfig(uuid, config);
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

    // --- HELPER METHODS (YANG HILANG TADI) ---

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
        if (loc != null) {
            p.teleport(loc);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

            String msg = ConfigUtils.getString("messages.teleporting");
            // Fallback jika message config belum ada
            if (msg == null) msg = "&aTeleporting to &e%location%...";

            p.sendMessage(ChatUtils.colorize(ConfigUtils.getString("prefix.admin") + msg.replace("%location%", name)));
        } else {
            p.sendMessage(ChatUtils.colorize("&cHome &e" + name + " &ctidak ditemukan!"));
        }
    }

    // --- LIMITS ---
    public int getMaxHomes(Player p) {
        if (p.hasPermission("naturalsmp.home.limit.unlimited")) return 999;

        ConfigurationSection limitSection = plugin.getConfig().getConfigurationSection("home.limits");
        if (limitSection == null) return 2;

        int highestLimit = 0;
        for (String rank : limitSection.getKeys(false)) {
            if (p.hasPermission("naturalsmp.home.limit." + rank)) {
                int value = limitSection.getInt(rank);
                if (value > highestLimit) highestLimit = value;
            }
        }
        return highestLimit > 0 ? highestLimit : limitSection.getInt("default", 2);
    }
}