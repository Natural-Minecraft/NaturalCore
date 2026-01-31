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
    private static FileConfiguration disabledCommandsConfig;
    private static File disabledCommandsFile;

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

    // --- DISABLED COMMANDS HELPERS ---
    public static FileConfiguration getDisabledCommandsConfig() {
        if (disabledCommandsConfig == null) {
            loadDisabledCommands();
        }
        return disabledCommandsConfig;
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

    private static void loadDisabledCommands() {
        NaturalCore plugin = NaturalCore.getInstance();
        if (!new File(plugin.getDataFolder(), "essentials_disabled_commands.yml").exists()) {
            plugin.saveResource("essentials_disabled_commands.yml", false);
        }
        disabledCommandsFile = new File(plugin.getDataFolder(), "essentials_disabled_commands.yml");
        disabledCommandsConfig = YamlConfiguration.loadConfiguration(disabledCommandsFile);
    }

    /**
     * Reload semua konfigurasi
     */
    public static void reload() {
        NaturalCore.getInstance().reloadConfig();
        loadMessages(); // Reload messages.yml juga
        loadSeason(); // Reload season.yml juga
        loadDisabledCommands(); // Reload disabled commands juga
    }

    // --- STRING GETTERS ---

    /**
     * Ambil string dari config.yml atau messages.yml (otomatis detect)
     * Path yang dimulai dengan "messages." akan diambil dari messages.yml
     */
    public static String getString(String path) {
        return getString(path, null);
    }

    /**
     * Ambil string dengan nilai default
     */
    public static String getString(String path, String def) {
        String result = null;
        // Jika path dimulai dengan "messages.", ambil dari messages.yml
        if (path.startsWith("messages.")) {
            String msgPath = path.substring(9); // Hilangkan prefix "messages."
            if (getMessages().contains(msgPath)) {
                result = getMessages().getString(msgPath);
            }
        } else {
            // Selain itu, ambil dari config.yml biasa
            if (getConfig().contains(path)) {
                result = getConfig().getString(path);
            }
        }

        if (result == null)
            return def;
        return ChatUtils.colorize(result);
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

    public static int getInt(String path, int def) {
        return getConfig().getInt(path, def);
    }

    public static boolean getBoolean(String path, boolean def) {
        return getConfig().getBoolean(path, def);
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

    // --- SMART MESSAGING (CLEAN CODE) ---

    public static void sendGeneral(org.bukkit.entity.Player p, String path, String... placeholders) {
        sendMessage(p, "prefix.general", path, placeholders);
    }

    public static void sendAdmin(org.bukkit.entity.Player p, String path, String... placeholders) {
        sendMessage(p, "prefix.admin", path, placeholders);
    }

    public static void sendMod(org.bukkit.entity.Player p, String path, String... placeholders) {
        sendMessage(p, "prefix.moderation", path, placeholders);
    }

    public static void sendMessage(org.bukkit.entity.Player p, String prefixPath, String path,
            String... placeholders) {
        String msg = getString(path);
        if (msg == null || msg.isEmpty())
            return;

        // Apply placeholders (pairs: key, value)
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        String prefix = getString(prefixPath, "");
        p.sendMessage(prefix + msg);
    }
}