package id.naturalsmp.naturalcore.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    // Pattern untuk mendeteksi warna Hex: &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";

        // 1. Translate Hex Color (&#123456)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            // Ubah &#RRGGBB menjadi format BungeeCord (&x&r&r&g&g&b&b)
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        // 2. Translate Standard Color (&a, &b, etc)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}