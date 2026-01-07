package id.naturalsmp.naturalcore.spawn;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SpawnManager {

    private final NaturalCore plugin;
    private File file;
    private FileConfiguration config;
    private Location spawnLocation;

    public SpawnManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadSpawn();
    }

    public void setSpawn(Location loc) {
        this.spawnLocation = loc;
        saveSpawn();
    }

    public Location getSpawn() {
        return spawnLocation;
    }

    public void teleport(Player p) {
        // UBAH DISINI: Ambil "prefix.spawn" bukan "prefix.admin"
        String prefix = ConfigUtils.getString("prefix.spawn");

        // Jika null (lupa set di config), fallback ke admin
        if (prefix == null) prefix = ConfigUtils.getString("prefix.admin");

        if (spawnLocation == null) {
            p.sendMessage(prefix + ConfigUtils.getString("messages.spawn-not-set"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        p.teleport(spawnLocation);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        p.sendTitle(ChatUtils.colorize("&a&lSPAWN"), ChatUtils.colorize("&7Teleporting..."), 0, 20, 10);

        p.sendMessage(prefix + ConfigUtils.getString("messages.spawn-teleport"));
    }

    // --- FILE HANDLING (spawn.yml) ---

    private void loadSpawn() {
        file = new File(plugin.getDataFolder(), "spawn.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("spawn.world")) {
            try {
                String worldName = config.getString("spawn.world");
                double x = config.getDouble("spawn.x");
                double y = config.getDouble("spawn.y");
                double z = config.getDouble("spawn.z");
                float yaw = (float) config.getDouble("spawn.yaw");
                float pitch = (float) config.getDouble("spawn.pitch");

                if (Bukkit.getWorld(worldName) != null) {
                    this.spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal memuat lokasi spawn!");
            }
        }
    }

    private void saveSpawn() {
        if (spawnLocation == null) return;

        config.set("spawn.world", spawnLocation.getWorld().getName());
        config.set("spawn.x", spawnLocation.getX());
        config.set("spawn.y", spawnLocation.getY());
        config.set("spawn.z", spawnLocation.getZ());
        config.set("spawn.yaw", spawnLocation.getYaw());
        config.set("spawn.pitch", spawnLocation.getPitch());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}