package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class ConfigUtils {

    // Helper biar kodingan lebih pendek
    private static FileConfiguration getConfig() {
        return NaturalCore.getInstance().getConfig();
    }

    // --- OLD FEATURES (TETAP ADA) ---

    public static String getString(String path) {
        if (!getConfig().contains(path)) return null;
        return ChatUtils.colorize(getConfig().getString(path));
    }

    public static int getInt(String path) {
        return getConfig().getInt(path);
    }

    public static void reload() {
        NaturalCore.getInstance().reloadConfig();
    }

    // --- NEW FEATURES (UNTUK ECONOMY & FITUR BARU) ---

    public static boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    public static List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }
}