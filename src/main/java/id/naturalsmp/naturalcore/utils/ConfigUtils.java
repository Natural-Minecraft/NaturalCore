package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * ConfigUtils - Helper untuk mengakses konfigurasi plugin.
 * 
 * Mendukung multiple config files:
 * - config.yml: Pengaturan utama (prefix, chat format, economy, dll)
 * - messages.yml: Semua pesan yang ditampilkan ke player
 * - chatemojis.yml: Konfigurasi emoji (dihandle oleh EmojiManager)
 */
public class ConfigUtils {

    private static FileConfiguration messagesConfig;
    private static File messagesFile;
    private static FileConfiguration seasonConfig;
    private static File seasonFile;

    // --- CONFIG.YML HELPERS ---

    private static FileConfiguration getConfig() {
        return NaturalCore.getInstance().getConfig();
    }

    // --- MESSAGES.YML HELPERS ---

    private static FileConfiguration getMessages() {
        if (messagesConfig == null) {
            loadMessages();
        }
        return messagesConfig;
    }

    // --- SEASON.YML HELPERS ---

    public static FileConfiguration getSeasonConfig() {
        if (seasonConfig == null) {
            loadSeason();
        }
        return seasonConfig;
    }

    /**
     * Load messages.yml
     */
    private static void loadMessages() {
        NaturalCore plugin = NaturalCore.getInstance();

        // Save default if not exists
        if (!new File(plugin.getDataFolder(), "messages.yml").exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private static void loadSeason() {
        NaturalCore plugin = NaturalCore.getInstance();
        if (!new File(plugin.getDataFolder(), "season.yml").exists()) {
            plugin.saveResource("season.yml", false);
        }
        seasonFile = new File(plugin.getDataFolder(), "season.yml");
        seasonConfig = YamlConfiguration.loadConfiguration(seasonFile);
    }

    /**
     * Reload semua konfigurasi
     */
    public static void reload() {
        NaturalCore.getInstance().reloadConfig();
        loadMessages(); // Reload messages.yml juga
        loadSeason(); // Reload season.yml juga
    }

    // --- STRING GETTERS ---

    /**
     * Ambil string dari config.yml atau messages.yml (otomatis detect)
     * Path yang dimulai dengan "messages." akan diambil dari messages.yml
     */
    public static String getString(String path) {
        // Jika path dimulai dengan "messages.", ambil dari messages.yml
        if (path.startsWith("messages.")) {
            String msgPath = path.substring(9); // Hilangkan prefix "messages."
            if (!getMessages().contains(msgPath))
                return null;
            return ChatUtils.colorize(getMessages().getString(msgPath));
        }

        // Selain itu, ambil dari config.yml biasa
        if (!getConfig().contains(path))
            return null;
        return ChatUtils.colorize(getConfig().getString(path));
    }

    /**
     * Ambil string langsung dari messages.yml (tanpa prefix)
     */
    public static String getMessage(String path) {
        if (!getMessages().contains(path))
            return null;
        return ChatUtils.colorize(getMessages().getString(path));
    }

    // --- OTHER GETTERS ---

    public static int getInt(String path) {
        return getConfig().getInt(path);
    }

    public static boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    public static List<String> getStringList(String path) {
        // Jika path dimulai dengan "messages.", ambil dari messages.yml
        if (path.startsWith("messages.")) {
            String msgPath = path.substring(9);
            return getMessages().getStringList(msgPath);
        }
        return getConfig().getStringList(path);
    }

    /**
     * Get string list langsung dari messages.yml
     */
    public static List<String> getMessageList(String path) {
        return getMessages().getStringList(path);
    }
}