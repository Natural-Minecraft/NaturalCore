package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtils {

    // Helper untuk mengambil String berwarna dari config
    public static String getString(String path) {
        FileConfiguration config = NaturalCore.getInstance().getConfig();
        if (!config.contains(path)) return null;
        return ChatUtils.colorize(config.getString(path));
    }

    // Helper untuk mengambil Integer
    public static int getInt(String path) {
        return NaturalCore.getInstance().getConfig().getInt(path);
    }

    // Helper untuk reload config
    public static void reload() {
        NaturalCore.getInstance().reloadConfig();
    }
}