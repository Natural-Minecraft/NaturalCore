package id.naturalsmp.naturalcore.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }

    // --- FORMATTER ANGKA ---

    public static String format(double number) {
        return String.format("%,.0f", number);
    }

    public static String format(int number) {
        return String.format("%,d", number);
    }

    // --- [PENTING] FORMATTER STRING (PENYELAMAT ERROR) ---
    // Ini yang dicari oleh CurrencyManager
    public static String format(String input) {
        if (input == null) return "";
        try {
            // Coba ubah string jadi angka
            double val = Double.parseDouble(input);
            return format(val);
        } catch (NumberFormatException e) {
            // Kalau bukan angka, kembalikan teks berwarna
            return colorize(input);
        }
    }
}