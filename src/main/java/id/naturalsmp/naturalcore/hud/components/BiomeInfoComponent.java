package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shows the current biome name when a player enters a new biome.
 */
public class BiomeInfoComponent extends AbstractHUDComponent {

    private final Map<UUID, Biome> lastBiomes = new HashMap<>();
    private final Map<UUID, Integer> displayTicks = new HashMap<>();
    private static final int DISPLAY_DURATION = 60; // 3 seconds at 20tps

    public BiomeInfoComponent(NaturalCore plugin) {
        super(plugin, "biome", HUDPriority.LOW);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        // 1. Ignore if in spawn world (lobby)
        if (player.getWorld().getName().equalsIgnoreCase("world")) {
            return false;
        }

        Biome currentBiome = player.getLocation().getBlock().getBiome();
        UUID uuid = player.getUniqueId();

        // 2. Handle first check (Join)
        if (!lastBiomes.containsKey(uuid)) {
            lastBiomes.put(uuid, currentBiome);
            return false;
        }

        // 3. Handle Biome Change
        Biome lastBiome = lastBiomes.get(uuid);
        if (lastBiome != currentBiome) {
            lastBiomes.put(uuid, currentBiome);
            displayTicks.put(uuid, DISPLAY_DURATION);
            return true;
        }

        // 4. Handle Duration Display
        Integer ticks = displayTicks.get(uuid);
        if (ticks != null && ticks > 0) {
            displayTicks.put(uuid, ticks - 1);
            return true;
        }

        return false;
    }

    @Override
    public String getContent(Player player, int tick) {
        Biome biome = player.getLocation().getBlock().getBiome();
        String biomeName = formatBiomeName(biome.name());
        String icon = getBiomeIcon(biome);
        String gradient = getBiomeGradient(biome);

        // Center-focused design with abstract separators
        return gradient + "✦ " + icon + " " + biomeName.toUpperCase() + " ✦</gradient>";
    }

    private String getBiomeIcon(Biome biome) {
        String name = biome.name();
        if (name.contains("NETHER"))
            return "🔥";
        if (name.contains("END"))
            return "🔮";
        if (name.contains("OCEAN") || name.contains("RIVER"))
            return "🌊";
        if (name.contains("FOREST"))
            return "🌳";
        if (name.contains("DESERT"))
            return "🐪";
        if (name.contains("ICE") || name.contains("SNOW"))
            return "❄";
        if (name.contains("DEEP_DARK"))
            return "💀";
        return "📍";
    }

    private String getBiomeGradient(Biome biome) {
        String name = biome.name();
        if (name.contains("NETHER"))
            return "<gradient:#FF4500:#8B0000>"; // Fire
        if (name.contains("END"))
            return "<gradient:#DA70D6:#4B0082>"; // Void
        if (name.contains("DEEP_DARK"))
            return "<gradient:#008B8B:#000000>"; // Sculk
        if (name.contains("OCEAN"))
            return "<gradient:#00BFFF:#00008B>"; // Sea
        return "<gradient:#FFFFFF:#A9A9A9>"; // Default Silver
    }

    private String formatBiomeName(String name) {
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
