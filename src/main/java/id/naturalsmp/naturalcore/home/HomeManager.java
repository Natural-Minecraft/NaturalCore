package id.naturalsmp.naturalcore.home;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final NaturalCore plugin;
    private final File homesFolder;

    public HomeManager(NaturalCore plugin) {
        this.plugin = plugin;
        // Buat folder plugins/NaturalCore/homes/
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }
    }

    // --- LOGIC UTAMA ---

    public void setHome(Player p, String homeName) {
        // Cek Limit dulu
        int limit = getHomeLimit(p);
        Map<String, Home> currentHomes = getHomes(p);

        // Jika home baru (bukan update) dan sudah full
        if (!currentHomes.containsKey(homeName.toLowerCase()) && currentHomes.size() >= limit) {
            String msg = ConfigUtils.getString("messages.home-limit-reached");
            p.sendMessage(ConfigUtils.getString("prefix.home") + msg.replace("%limit%", String.valueOf(limit)));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Simpan
        saveHomeToFile(p, homeName, p.getLocation());

        String msg = ConfigUtils.getString("messages.home-set");
        p.sendMessage(ConfigUtils.getString("prefix.home") + msg.replace("%name%", homeName));
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    public void teleportHome(Player p, String homeName) {
        Home home = getHome(p, homeName);
        if (home == null) {
            String msg = ConfigUtils.getString("messages.home-not-found");
            p.sendMessage(ConfigUtils.getString("prefix.home") + msg.replace("%name%", homeName));
            return;
        }

        p.teleport(home.getLocation());
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        String msg = ConfigUtils.getString("messages.home-teleport");
        p.sendMessage(ConfigUtils.getString("prefix.home") + msg.replace("%name%", home.getName()));
        p.sendTitle(ChatUtils.colorize("&a" + home.getName()), ChatUtils.colorize("&7Welcome Home"), 0, 20, 10);
    }

    public void deleteHome(Player p, String homeName) {
        Home home = getHome(p, homeName);
        if (home == null) {
            String msg = ConfigUtils.getString("messages.home-not-found");
            p.sendMessage(ConfigUtils.getString("prefix.home") + msg.replace("%name%", homeName));
            return;
        }

        // Hapus dari file
        File file = new File(homesFolder, p.getUniqueId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("homes." + homeName.toLowerCase(), null);
        try {
            config.save(file);
            String msg = ConfigUtils.getString("messages.home-deleted");
            p.sendMessage(ConfigUtils.getString("prefix.home") + msg.replace("%name%", homeName));
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 2f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- DATA HANDLING ---

    public Map<String, Home> getHomes(Player p) {
        Map<String, Home> homes = new HashMap<>();
        File file = new File(homesFolder, p.getUniqueId() + ".yml");

        if (!file.exists()) return homes;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("homes")) return homes;

        ConfigurationSection sec = config.getConfigurationSection("homes");
        for (String key : sec.getKeys(false)) {
            String worldName = sec.getString(key + ".world");
            if (Bukkit.getWorld(worldName) == null) continue; // Skip jika world invalid

            double x = sec.getDouble(key + ".x");
            double y = sec.getDouble(key + ".y");
            double z = sec.getDouble(key + ".z");
            float yaw = (float) sec.getDouble(key + ".yaw");
            float pitch = (float) sec.getDouble(key + ".pitch");

            // Nama asli (case sensitive) disimpan di config atau pakai key
            String realName = sec.getString(key + ".name", key);

            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
            homes.put(key.toLowerCase(), new Home(realName, loc));
        }
        return homes;
    }

    public Home getHome(Player p, String name) {
        return getHomes(p).get(name.toLowerCase());
    }

    private void saveHomeToFile(Player p, String name, Location loc) {
        File file = new File(homesFolder, p.getUniqueId() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = "homes." + name.toLowerCase();
        config.set(path + ".name", name); // Simpan nama asli (Kapitalisasi)
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- LIMIT SYSTEM (PERMISSION BASED) ---

    public int getHomeLimit(Player p) {
        // Admin bypass
        if (p.hasPermission("naturalsmp.home.limit.admin") || p.isOp()) {
            return ConfigUtils.getInt("home.limits.admin");
        }

        // Cek permission dari config (MVP, VIP)
        // Format: naturalsmp.home.limit.<group>
        ConfigurationSection limits = plugin.getConfig().getConfigurationSection("home.limits");
        int max = limits.getInt("default", 3); // Nilai default minimal

        if (limits != null) {
            for (String key : limits.getKeys(false)) {
                if (p.hasPermission("naturalsmp.home.limit." + key)) {
                    int val = limits.getInt(key);
                    if (val > max) max = val; // Ambil limit tertinggi
                }
            }
        }
        return max;
    }

    // Helper untuk mengambil List Home yang sudah diurutkan A-Z (Penting untuk GUI)
    public List<Home> getSortedHomes(Player p) {
        List<Home> list = new ArrayList<>(getHomes(p).values());
        // Sortir berdasarkan nama (A-Z)
        list.sort(Comparator.comparing(Home::getName));
        return list;
    }

    /**
     * Mendapatkan batas maksimal home secara dinamis berdasarkan config.
     * Logika: Mencari nilai tertinggi dari permission yang dimiliki player.
     */
    public int getMaxHomes(Player p) {
        // 1. Shortcut untuk Unlimited
        if (p.hasPermission("naturalsmp.home.limit.unlimited")) {
            return 999;
        }

        // 2. Ambil semua key di bawah 'home.limits' secara otomatis
        org.bukkit.configuration.ConfigurationSection limitSection =
                NaturalCore.getInstance().getConfig().getConfigurationSection("home.limits");

        if (limitSection == null) return 2; // Safety fallback

        int highestLimit = 0;

        // 3. Loop semua rank yang ada di config (default, vip, mvp, pro, dll)
        for (String rank : limitSection.getKeys(false)) {
            // Cek apakah player punya permission: naturalsmp.home.limit.<rank>
            if (p.hasPermission("naturalsmp.home.limit." + rank)) {
                int value = limitSection.getInt(rank);
                // Ambil nilai tertinggi jika player punya banyak permission rank
                if (value > highestLimit) {
                    highestLimit = value;
                }
            }
        }

        // 4. Jika tidak punya permission rank apapun, kembalikan nilai 'default'
        return highestLimit > 0 ? highestLimit : limitSection.getInt("default", 2);
    }
}