package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class SeasonListener implements Listener {

    private final SeasonManager manager;

    public SeasonListener(SeasonManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        if (!config.getBoolean("visuals.enabled", true))
            return;

        applySeasonVisuals(event.getChunk());
    }

    private void applySeasonVisuals(Chunk chunk) {
        Season season = manager.getCurrentSeason();
        String biomeName = ConfigUtils.getSeasonConfig().getString("visuals.biomes." + season.name(), "PLAINS");

        try {
            Biome targetBiome = Biome.valueOf(biomeName);
            World world = chunk.getWorld();

            // Only apply in main world (Survival/Resource)
            if (world.getEnvironment() != World.Environment.NORMAL)
                return;

            int minX = chunk.getX() << 4;
            int minZ = chunk.getZ() << 4;
            int maxX = minX + 15;
            int maxZ = minZ + 15;

            // Minecraft keeps biomes in 4x4x4 grids since 1.15,
            // but setting it per block at a low resolution is enough for visual grass
            // color.
            for (int x = minX; x <= maxX; x += 4) {
                for (int z = minZ; z <= maxZ; z += 4) {
                    for (int y = world.getMinHeight(); y < world.getMaxHeight(); y += 16) {
                        chunk.getWorld().setBiome(x, y, z, targetBiome);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Biome not found in config
        }
    }
}
