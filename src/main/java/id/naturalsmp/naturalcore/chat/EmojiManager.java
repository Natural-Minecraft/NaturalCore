package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EmojiManager - Mengelola registry emoji dan parsing emoticon ke emoji.
 * Terinspirasi dari plugin ChatEmojis by RMJTromp.
 * 
 * Cara kerja:
 * 1. Load emoji dari chatemojis.yml saat startup
 * 2. Saat chat, ganti semua trigger (contoh: ":)") dengan emoji (contoh: "☺")
 * 3. Mendukung permission per-emoji (opsional)
 */
public class EmojiManager {

    private static EmojiManager instance;
    private final NaturalCore plugin;

    // Map: trigger -> Emoji data
    private final Map<String, EmojiData> emojiRegistry = new HashMap<>();

    // Compiled pattern untuk matching (dibuild saat reload)
    private Pattern emojiPattern;

    // Config file terpisah untuk emoji
    private File emojiFile;
    private FileConfiguration emojiConfig;
    private boolean enabled = true;

    public EmojiManager(NaturalCore plugin) {
        this.plugin = plugin;
        instance = this;
        loadEmojis();
    }

    public static EmojiManager getInstance() {
        return instance;
    }

    /**
     * Load emoji dari chatemojis.yml
     */
    public void loadEmojis() {
        emojiRegistry.clear();

        // Save default config jika belum ada
        if (!new File(plugin.getDataFolder(), "chatemojis.yml").exists()) {
            plugin.saveResource("chatemojis.yml", false);
        }

        // Load config
        emojiFile = new File(plugin.getDataFolder(), "chatemojis.yml");
        emojiConfig = YamlConfiguration.loadConfiguration(emojiFile);

        // Check if enabled
        enabled = emojiConfig.getBoolean("enabled", true);

        ConfigurationSection emojiSection = emojiConfig.getConfigurationSection("list");
        if (emojiSection == null) {
            plugin.getLogger().warning("Emoji list not found in chatemojis.yml!");
            buildPattern();
            return;
        }

        for (String key : emojiSection.getKeys(false)) {
            ConfigurationSection emoji = emojiSection.getConfigurationSection(key);
            if (emoji == null)
                continue;

            String character = emoji.getString("char", "?");
            List<String> triggers = emoji.getStringList("triggers");
            String permission = emoji.getString("permission", ""); // Kosong = no permission needed

            EmojiData data = new EmojiData(key, character, permission);

            // Register semua trigger untuk emoji ini
            for (String trigger : triggers) {
                emojiRegistry.put(trigger.toLowerCase(), data);
            }
        }

        buildPattern();
        plugin.getLogger().info("Loaded " + emojiSection.getKeys(false).size() + " emojis with "
                + emojiRegistry.size() + " triggers from chatemojis.yml");
    }

    /**
     * Build regex pattern dari semua trigger yang terdaftar
     */
    private void buildPattern() {
        if (emojiRegistry.isEmpty()) {
            emojiPattern = null;
            return;
        }

        // Escape special regex characters dan join dengan |
        StringBuilder patternBuilder = new StringBuilder();
        for (String trigger : emojiRegistry.keySet()) {
            if (patternBuilder.length() > 0) {
                patternBuilder.append("|");
            }
            // Escape regex special chars
            patternBuilder.append(Pattern.quote(trigger));
        }

        emojiPattern = Pattern.compile("(" + patternBuilder.toString() + ")", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Cek apakah emoji system diaktifkan
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Parse message dan ganti emoticon dengan emoji
     *
     * @param player  Player yang mengirim pesan (untuk permission check)
     * @param message Pesan asli
     * @return Pesan dengan emoji yang sudah di-replace
     */
    public String parseEmojis(Player player, String message) {
        // Cek apakah fitur emoji diaktifkan
        if (!enabled) {
            return message;
        }

        // Cek permission dasar
        if (!player.hasPermission("naturalsmp.emoji.use")) {
            return message;
        }

        if (emojiPattern == null || message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = emojiPattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String trigger = matcher.group(1).toLowerCase();
            EmojiData emoji = emojiRegistry.get(trigger);

            if (emoji != null) {
                // Cek permission spesifik emoji (jika ada)
                if (emoji.hasPermission() && !player.hasPermission(emoji.getPermission())
                        && !player.hasPermission("naturalsmp.emoji.*")) {
                    // Player tidak punya permission untuk emoji ini, skip
                    continue;
                }

                // Replace dengan karakter emoji
                matcher.appendReplacement(result, Matcher.quoteReplacement(emoji.getCharacter()));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Parse emoji tanpa permission check (untuk admin/force)
     */
    public String parseEmojisForce(String message) {
        if (emojiPattern == null || message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = emojiPattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String trigger = matcher.group(1).toLowerCase();
            EmojiData emoji = emojiRegistry.get(trigger);

            if (emoji != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(emoji.getCharacter()));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Get semua emoji yang terdaftar (untuk /emoji list)
     */
    public Map<String, EmojiData> getEmojiRegistry() {
        return new HashMap<>(emojiRegistry);
    }

    /**
     * Inner class untuk menyimpan data emoji
     */
    public static class EmojiData {
        private final String name;
        private final String character;
        private final String permission;

        public EmojiData(String name, String character, String permission) {
            this.name = name;
            this.character = character;
            this.permission = permission;
        }

        public String getName() {
            return name;
        }

        public String getCharacter() {
            return character;
        }

        public String getPermission() {
            return permission;
        }

        public boolean hasPermission() {
            return permission != null && !permission.isEmpty();
        }
    }
}
