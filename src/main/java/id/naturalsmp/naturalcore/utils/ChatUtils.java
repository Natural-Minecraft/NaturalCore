package id.naturalsmp.naturalcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    // Regex untuk mendeteksi Hex Color (&#RRGGBB)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";

        // 1. Support Hex Color (&#FF0000)
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)) + "");
            matcher = HEX_PATTERN.matcher(message);
        }

        // 2. Support Kode Warna Lama (&a, &l, dll)
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // Untuk Paper API (Component) - Modern Style
    public static Component format(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(colorize(message));
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}