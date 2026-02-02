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
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        UUID uuid = player.getUniqueId();

        Biome lastBiome = lastBiomes.get(uuid);
        if (lastBiome == null || lastBiome != currentBiome) {
            lastBiomes.put(uuid, currentBiome);
            displayTicks.put(uuid, DISPLAY_DURATION);
            return true;
        }

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

        return ChatUtils.colorize("&f📍 &a" + biomeName);
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
