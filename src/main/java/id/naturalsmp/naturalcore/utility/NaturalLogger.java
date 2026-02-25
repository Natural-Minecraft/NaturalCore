package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NaturalLogger {

    private static NaturalLogger instance;
    private final NaturalCore plugin;

    private final File logsDir;
    private final File todayDir;

    private final Map<String, String> rankMap = new HashMap<>();
    private final Map<String, String> tierMap = new HashMap<>();

    private final ConcurrentLinkedQueue<LogEntry> writeQueue = new ConcurrentLinkedQueue<>();
    private boolean isWriting = false;

    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat folderFormat;

    private NaturalLogger(NaturalCore plugin) {
        this.plugin = plugin;
        this.logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists())
            logsDir.mkdirs();

        this.todayDir = new File(logsDir, "today");
        if (!todayDir.exists())
            todayDir.mkdirs();

        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        this.folderFormat = new SimpleDateFormat("ddMMMMMyyyy", Locale.ENGLISH);
        this.folderFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

        loadMappings();
        startWriterTask();
        startRotationTask();
    }

    public static void init(NaturalCore plugin) {
        if (instance == null) {
            instance = new NaturalLogger(plugin);
        }
    }

    public static NaturalLogger getInstance() {
        return instance;
    }

    public void stop() {
        flushQueue(); // Try to save everything on shutdown
    }

    private void loadMappings() {
        File ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (ranksFile.exists()) {
            YamlConfiguration ranksCfg = YamlConfiguration.loadConfiguration(ranksFile);
            if (ranksCfg.contains("ranks")) {
                for (String key : ranksCfg.getConfigurationSection("ranks").getKeys(false)) {
                    String prefix = ranksCfg.getString("ranks." + key + ".prefix");
                    String display = ranksCfg.getString("ranks." + key + ".display");
                    if (prefix != null && display != null) {
                        String cleanPrefix = org.bukkit.ChatColor.stripColor(ChatUtils.decolorize(prefix));
                        String cleanDisplay = org.bukkit.ChatColor.stripColor(ChatUtils.decolorize(display));
                        rankMap.put(cleanPrefix, cleanDisplay);
                    }
                }
            }
            if (ranksCfg.contains("rank-lainnya")) {
                for (String key : ranksCfg.getConfigurationSection("rank-lainnya").getKeys(false)) {
                    String prefix = ranksCfg.getString("rank-lainnya." + key + ".prefix");
                    String display = ranksCfg.getString("rank-lainnya." + key + ".display");
                    if (prefix != null && display != null) {
                        String cleanPrefix = org.bukkit.ChatColor.stripColor(ChatUtils.decolorize(prefix));
                        String cleanDisplay = org.bukkit.ChatColor.stripColor(ChatUtils.decolorize(display));
                        rankMap.put(cleanPrefix, cleanDisplay);
                    }
                }
            }
        }

        File tiersFile = new File(plugin.getDataFolder(), "tiers.yml");
        if (tiersFile.exists()) {
            YamlConfiguration tiersCfg = YamlConfiguration.loadConfiguration(tiersFile);
            if (tiersCfg.contains("tiers")) {
                for (String key : tiersCfg.getConfigurationSection("tiers").getKeys(false)) {
                    String reqConfigVal = tiersCfg.getString("tiers." + key + ".prefix"); // often prefixes exist here
                                                                                          // or in tier keys

                    // The standard mapping in chat is [Member] Azka Warrior 3
                    // The unicode character for each tier can be searched and replaced by standard
                    // regex or dictionary
                    // Depending on what exactly is in the unicode string, we try to store its raw
                    // form to replace.
                    // For now, we will use a broader regex in the chat cleaning to strip all
                    // formatting and hex colors.
                }
            }
        }
    }

    // --- Logging Methods ---

    public void logCommand(String player, String command) {
        queueLog("commands.log", "[" + timestamp() + "] " + player + " issued server command: " + command);
    }

    public void logAdmin(String player, String command) {
        queueLog("admin.log", "[" + timestamp() + "] " + player + " executed admin command: " + command);
    }

    public void logPrivateChat(String player, String targetOrType, String message) {
        queueLog("prv-chat.log", "[" + timestamp() + "] " + player + " to " + targetOrType + ": " + message);
    }

    public void logClearLagg(int count) {
        queueLog("clearlaggs.log", "[" + timestamp() + "] items cleared: " + count);
    }

    public void logClearLaggItemDetail(String detail) {
        queueLog("clearlaggs.log", "[" + timestamp() + "] items: " + detail);
    }

    public void logChatGame(String resultMsg) {
        queueLog("chat-games.log", "[" + timestamp() + "] " + resultMsg);
    }

    public void logConnection(String username, UUID uuid, String action, String reason) {
        String base = "[" + timestamp() + "] " + username + " (" + uuid.toString() + ") " + action;
        if (reason != null && !reason.isEmpty()) {
            base += " [" + reason + "]";
        }
        queueLog("connections.log", base);
    }

    public void logChat(String playerName, String prefix, String tier, String formatInfo, String message) {
        String cleanPrefix = org.bukkit.ChatColor.stripColor(ChatUtils.decolorize(prefix == null ? "" : prefix));
        if (rankMap.containsKey(cleanPrefix)) {
            cleanPrefix = rankMap.get(cleanPrefix);
        } else if (rankMap.containsKey(cleanPrefix.trim())) {
            cleanPrefix = rankMap.get(cleanPrefix.trim());
        }
        cleanPrefix = cleanText(cleanPrefix).trim();

        String cleanTier = cleanText(tier).trim();
        String cleanMsg = cleanText(message).trim();

        String prefixPart = cleanPrefix.isEmpty() ? "" : "[" + cleanPrefix + "] ";
        String tierPart = cleanTier.isEmpty() ? "" : " [" + cleanTier + "]";

        // Output -> [23/02/2026 00:16:57] [Owner] AnakTentara [MYTHICAL IMMORTAL]
        // [color chat: { white }, format: { font: { default } }]: humm
        String finalLine = prefixPart + playerName + tierPart + formatInfo + ": " + cleanMsg;

        queueLog("chats.log", "[" + timestamp() + "] " + finalLine);
    }

    // --- Core Logic ---

    private String cleanText(String input) {
        if (input == null || input.isEmpty())
            return "";

        // Strip colors
        String plain = org.bukkit.ChatColor.stripColor(ChatUtils.decolorize(input));

        // Remove [Not Secure]
        plain = plain.replace("[Not Secure]", "").trim();

        // Real-time translation to English alphabet based on unicode fonts.
        plain = plain
                .replace("ᴀ", "a").replace("ʙ", "b").replace("ᴄ", "c").replace("ᴅ", "d")
                .replace("ᴇ", "e").replace("ꜰ", "f").replace("ɢ", "g").replace("ʜ", "h")
                .replace("ɪ", "i").replace("ᴊ", "j").replace("ᴋ", "k").replace("ʟ", "l")
                .replace("ᴍ", "m").replace("ɴ", "n").replace("ᴏ", "o").replace("ᴘ", "p")
                .replace("Q", "Q").replace("ʀ", "r").replace("ꜱ", "s").replace("ᴛ", "t")
                .replace("ᴜ", "u").replace("ᴠ", "v").replace("ᴡ", "w").replace("x", "x")
                .replace("ʏ", "y").replace("ᴢ", "z");

        return plain;
    }

    private String timestamp() {
        return dateFormat.format(new Date());
    }

    private void queueLog(String filename, String content) {
        writeQueue.add(new LogEntry(filename, content));
    }

    private void startWriterTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            flushQueue();
        }, 60L, 20L * 5); // Write queue every 5 seconds
    }

    private void flushQueue() {
        if (isWriting || writeQueue.isEmpty())
            return;
        isWriting = true;

        Map<String, List<String>> batchWrites = new HashMap<>();

        LogEntry entry;
        while ((entry = writeQueue.poll()) != null) {
            batchWrites.computeIfAbsent(entry.filename, k -> new ArrayList<>()).add(entry.content);
        }

        for (Map.Entry<String, List<String>> batch : batchWrites.entrySet()) {
            File logFile = new File(todayDir, batch.getKey());
            try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
                for (String line : batch.getValue()) {
                    out.println(line);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to write to natural log: " + batch.getKey());
                e.printStackTrace();
            }
        }

        isWriting = false;
    }

    private void startRotationTask() {
        // Check rotation every hour (if day changed, execute rotation)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::rotateAtMidnight, 200L, 20L * 60 * 60);
    }

    private void rotateAtMidnight() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
            // It's 12:XX AM (Midnight hours). Time to push "today" into "DDMonthYYYY" for
            // yesterday.
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String folderName = folderFormat.format(cal.getTime());

            File historyDir = new File(logsDir, folderName);
            if (!historyDir.exists()) {
                flushQueue(); // ensure nothing is left in memory

                try {
                    historyDir.mkdirs();

                    // Move files
                    File[] files = todayDir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile()) {
                                Path targetPath = new File(historyDir, f.getName()).toPath();
                                Files.move(f.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                    plugin.getLogger().info("Successfully rotated logs to " + folderName);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to rotate daily logs.");
                    e.printStackTrace();
                }
            }
        }
    }

    private static class LogEntry {
        String filename;
        String content;

        LogEntry(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }
    }
}
