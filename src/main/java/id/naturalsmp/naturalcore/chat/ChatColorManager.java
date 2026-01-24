package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatColorManager {

    private final NaturalCore plugin;
    private File file;
    private FileConfiguration config;

    // Cache untuk performa (UUID -> Data)
    private final Map<UUID, String> colorCache = new HashMap<>(); // Color code (e.g. &b)
    private final Map<UUID, String> fontCache = new HashMap<>(); // Font type (Target)

    public ChatColorManager(NaturalCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "chatcolor.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create chatcolor.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveData() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- SETTERS ---

    public void setPlayerColor(Player p, String colorCode) {
        config.set(p.getUniqueId() + ".color", colorCode);
        colorCache.put(p.getUniqueId(), colorCode);
        saveData();
    }

    public void setPlayerFont(Player p, String fontName) {
        config.set(p.getUniqueId() + ".font", fontName);
        fontCache.put(p.getUniqueId(), fontName);
        saveData();
    }

    public void setBold(Player p, boolean bold) {
        config.set(p.getUniqueId() + ".bold", bold);
        saveData();
    }

    public void setItalic(Player p, boolean italic) {
        config.set(p.getUniqueId() + ".italic", italic);
        saveData();
    }

    // --- GETTERS ---

    public String getPlayerColor(Player p) {
        if (colorCache.containsKey(p.getUniqueId())) {
            return colorCache.get(p.getUniqueId());
        }
        String color = config.getString(p.getUniqueId() + ".color", "&f"); // Default White
        colorCache.put(p.getUniqueId(), color);
        return color;
    }

    public String getPlayerFont(Player p) {
        if (fontCache.containsKey(p.getUniqueId())) {
            return fontCache.get(p.getUniqueId());
        }
        String font = config.getString(p.getUniqueId() + ".font", "default");
        fontCache.put(p.getUniqueId(), font);
        return font;
    }

    public boolean isBold(Player p) {
        return config.getBoolean(p.getUniqueId() + ".bold", false);
    }

    public boolean isItalic(Player p) {
        return config.getBoolean(p.getUniqueId() + ".italic", false);
    }

    // --- UTILS ---

    public String applyFont(String message, String font) {
        if (font.equalsIgnoreCase("SmallCaps")) {
            return toSmallCaps(message);
        } else if (font.equalsIgnoreCase("MathSans")) {
            return toMathSans(message); // Placeholder if user wants MathSans
        }
        return message; // Default
    }

    private String toSmallCaps(String text) {
        String normal = "abcdefghijklmnopqrstuvwxyz";
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // Skip Bukkit/Legacy color codes: & or § followed by a char
            if ((c == '&' || c == '§') && i + 1 < chars.length) {
                sb.append(c);
                sb.append(chars[++i]);
                continue;
            }
            int index = normal.indexOf(Character.toLowerCase(c));
            if (index != -1) {
                sb.append(smallCaps.charAt(index));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Placeholder MathSans (Bold Sans Serif) implementation if needed
    // Can allow simple conversion for a-z A-Z 0-9
    private String toMathSans(String text) {
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String mathSans = "𝗮𝗯𝗰𝗱𝗲𝗳𝗴𝗵𝗶𝗷𝗸𝗹𝗺𝗻𝗼𝗽𝗾𝗿𝘀𝘁𝘂𝘃𝘄𝘅𝘆𝘇𝗔𝗕𝗖𝗗𝗘𝗙𝗚𝗛𝗜𝗝𝗞𝗟ＭＮＯＰＱＲＳＴＵＶＷＸＹＺ𝟬𝟭𝟮𝟯𝟰𝟱𝟲７𝟴𝟵";
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // Skip Bukkit/Legacy color codes
            if ((c == '&' || c == '§') && i + 1 < chars.length) {
                sb.append(c);
                sb.append(chars[++i]);
                continue;
            }
            int index = normal.indexOf(c);
            if (index != -1) {
                // Since MathSans characters are outside BMP (Supplementary),
                // we should handle them as strings or handle surrogates correctly.
                // For simplicity in Minecraft chat (which supports UTF-16),
                // we'll find the start in the mathSans string.
                // Each mathSans character is a surrogate pair (2 chars).
                sb.append(mathSans.substring(index * 2, (index * 2) + 2));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
