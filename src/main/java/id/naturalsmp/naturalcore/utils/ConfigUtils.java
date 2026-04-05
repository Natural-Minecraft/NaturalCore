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
    private static FileConfiguration internalMessagesConfig; // Cadangan internal
    private static FileConfiguration seasonConfig;
    private static File seasonFile;
    private static FileConfiguration commandsConfig;
    private static File commandsFile;

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

    private static FileConfiguration getInternalMessages() {
        if (internalMessagesConfig == null) {
            NaturalCore plugin = NaturalCore.getInstance();
            java.io.InputStream is = plugin.getResource("messages.yml");
            if (is != null) {
                internalMessagesConfig = YamlConfiguration
                        .loadConfiguration(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        return internalMessagesConfig;
    }

    // --- SEASON.YML HELPERS ---
    public static FileConfiguration getSeasonConfig() {
        if (seasonConfig == null) {
            loadSeason();
        }
        return seasonConfig;
    }

    // --- COMMANDS.YML HELPERS ---
    public static FileConfiguration getCommandsConfig() {
        if (commandsConfig == null) {
            loadCommands();
        }
        return commandsConfig;
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

    private static void loadCommands() {
        NaturalCore plugin = NaturalCore.getInstance();
        if (!new File(plugin.getDataFolder(), "commands.yml").exists()) {
            plugin.saveResource("commands.yml", false);
        }
        commandsFile = new File(plugin.getDataFolder(), "commands.yml");
        commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
    }

    /**
     * Reload semua konfigurasi
     */
    public static void reload() {
        NaturalCore.getInstance().reloadConfig();
        loadMessages(); // Reload messages.yml juga
        loadSeason(); // Reload season.yml juga
        loadCommands(); // Reload commands.yml juga
        internalMessagesConfig = null; // Clear fallback cache
    }

    // --- STRING GETTERS ---

    /**
     * Ambil string dari config.yml atau messages.yml (otomatis detect)
     * Path yang dimulai dengan "messages." akan diambil dari messages.yml
     */
    public static String getString(String path) {
        return getString(path, "");
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
            } else if (getInternalMessages() != null && getInternalMessages().contains(msgPath)) {
                // FALLBACK ke resource internal jika di messages.yml luar tidak ada
                result = getInternalMessages().getString(msgPath);
            }
        } else {
            // Selain itu, ambil dari config.yml biasa
            if (getConfig().contains(path)) {
                result = getConfig().getString(path);
            }
        }

        if (result == null)
            return def != null ? def : "";
        return ChatUtils.colorize(result);
    }

    /**
     * Ambil string langsung dari messages.yml (tanpa prefix)
     */
    public static String getMessage(String path) {
        String result = null;
        if (getMessages().contains(path)) {
            result = getMessages().getString(path);
        } else if (getInternalMessages() != null && getInternalMessages().contains(path)) {
            result = getInternalMessages().getString(path);
        }

        if (result == null)
            return "";
        return ChatUtils.colorize(result);
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

    public static double getDouble(String path) {
        return getConfig().getDouble(path);
    }

    public static double getDouble(String path, double def) {
        return getConfig().getDouble(path, def);
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

    public static void sendGeneral(org.bukkit.command.CommandSender sender, String path, String... placeholders) {
        sendMessage(sender, "prefix.general", path, placeholders);
    }

    public static void sendAdmin(org.bukkit.command.CommandSender sender, String path, String... placeholders) {
        sendMessage(sender, "prefix.admin", path, placeholders);
    }

    public static void sendError(org.bukkit.command.CommandSender sender, String msg) {
        String prefix = getPrefix("prefix.general");
        sender.sendMessage(prefix + ChatUtils.colorize("&cError: &f" + msg));
    }

    public static void sendUsage(org.bukkit.command.CommandSender sender, String usage) {
        String prefix = getPrefix("prefix.general");
        sender.sendMessage(prefix + ChatUtils.colorize("&eGunakan: &f" + usage));
    }

    public static void sendMod(org.bukkit.command.CommandSender sender, String path, String... placeholders) {
        sendMessage(sender, "prefix.moderation", path, placeholders);
    }

    public static void sendGeneral(org.bukkit.entity.Player p, String path, String... placeholders) {
        sendMessage(p, "prefix.general", path, placeholders);
    }

    public static void sendAdmin(org.bukkit.entity.Player p, String path, String... placeholders) {
        sendMessage(p, "prefix.admin", path, placeholders);
    }

    public static void sendMod(org.bukkit.entity.Player p, String path, String... placeholders) {
        sendMessage(p, "prefix.moderation", path, placeholders);
    }

    public static String getPrefix(String prefixPath) {
        String prefix = getString("messages." + prefixPath, null);
        if (prefix == null && prefixPath.equals("prefix.player")) {
            prefix = getString("messages.prefix.general", "");
        }
        return prefix != null ? prefix : "";
    }

    public static void sendMessage(org.bukkit.command.CommandSender sender, String prefixPath, String path,
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

        String prefix = getPrefix(prefixPath);
        sender.sendMessage(prefix + msg);
    }
}