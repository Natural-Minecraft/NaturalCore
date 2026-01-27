package id.naturalsmp.naturalcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class BedrockUtils {

    private static boolean floodgatePresent = false;
    private static Object floodgateApi = null;
    private static Method isFloodgatePlayerMethod = null;

    static {
        if (Bukkit.getPluginManager().getPlugin("floodgate") != null) {
            try {
                Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                Method getInstance = apiClass.getMethod("getInstance");
                floodgateApi = getInstance.invoke(null);
                isFloodgatePlayerMethod = apiClass.getMethod("isFloodgatePlayer", UUID.class);
                floodgatePresent = true;
            } catch (Exception e) {
                Bukkit.getLogger().warning("[NaturalCore] Failed to hook into Floodgate API: " + e.getMessage());
            }
        }
    }

    public static boolean isBedrock(Player player) {
        if (!floodgatePresent || player == null) {
            // Fallback: Check username prefix (common config)
            return player.getName().startsWith(".");
        }
        try {
            return (boolean) isFloodgatePlayerMethod.invoke(floodgateApi, player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }
}
