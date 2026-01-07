package id.naturalsmp.naturalcore.moderation;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PunishmentManager {

    private final NaturalCore plugin;
    private File file;
    private FileConfiguration config;

    public PunishmentManager(NaturalCore plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "punishments.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    // --- BAN SYSTEM ---
    public void ban(UUID uuid, String reason, long expiry) {
        config.set("bans." + uuid + ".reason", reason);
        config.set("bans." + uuid + ".expiry", expiry); // -1 = Permanen
        save();
    }

    public void unban(UUID uuid) {
        config.set("bans." + uuid, null);
        save();
    }

    public boolean isBanned(UUID uuid) {
        if (!config.contains("bans." + uuid)) return false;
        long expiry = config.getLong("bans." + uuid + ".expiry");
        if (expiry != -1 && System.currentTimeMillis() > expiry) {
            unban(uuid); // Expired
            return false;
        }
        return true;
    }

    public String getBanReason(UUID uuid) {
        return config.getString("bans." + uuid + ".reason", "No Reason");
    }

    // --- MUTE SYSTEM ---
    public void mute(UUID uuid, long expiry) {
        config.set("mutes." + uuid + ".expiry", expiry);
        save();
    }

    public void unmute(UUID uuid) {
        config.set("mutes." + uuid, null);
        save();
    }

    public boolean isMuted(UUID uuid) {
        if (!config.contains("mutes." + uuid)) return false;
        long expiry = config.getLong("mutes." + uuid + ".expiry");
        if (System.currentTimeMillis() > expiry) {
            unmute(uuid);
            return false;
        }
        return true;
    }

    public long getMuteExpiry(UUID uuid) {
        return config.getLong("mutes." + uuid + ".expiry");
    }
}