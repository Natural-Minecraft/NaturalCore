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

    // Optimized: No PlayerMoveEvent needed for Global Season

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        FileConfiguration config = ConfigUtils.getSeasonConfig();
        if (!config.getBoolean("visuals.enabled", true))
            return;

        applySeasonVisuals(event.getChunk());
    }

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        // Check if placed block affects temperature/melting (Heat source)
        manager.checkBlockState(event.getBlock());
    }

    @EventHandler
    public void onBlockForm(org.bukkit.event.block.BlockFormEvent event) {
        // Ice / Snow forming
        if (event.getNewState().getType() == org.bukkit.Material.ICE
                || event.getNewState().getType() == org.bukkit.Material.SNOW) {

            Season season = manager.getRegionManager().getSeason(event.getBlock().getLocation());
            if (season != Season.WINTER) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFade(org.bukkit.event.block.BlockFadeEvent event) {
        // Ice / Snow melting
        if (event.getBlock().getType() == org.bukkit.Material.ICE
                || event.getBlock().getType() == org.bukkit.Material.SNOW) {

            Season season = manager.getRegionManager().getSeason(event.getBlock().getLocation());
            if (season == Season.WINTER) {
                // Prevent melting in winter unless near heat (Vanilla usually handles light)
                // But we want to STRICTLY prevent it unless we define heat logic.
                // Actually vanilla melts if light > 11.
                // We let vanilla handle melting if it's due to light, but we can verify heat
                // source?
                // For now, let's keep vanilla behavior but maybe cancel if it's just "daylight"
                // melting in winter?
                // User said: "musim lebih dynamic".
                // Let's allow natural melting if there is a heat source, otherwise cancel.

                // If it's fading (melting) in Winter, check if it's near heat.
                // If NOT near heat, cancel melt.
                // But `checkBlockState` checks for 3 block radius.
                // Let's re-use that logic but inverted?
                // Wait, `checkBlockState` effectively melts ice if near heat.
                // BlockFadeEvent happens when game wants to melt it.
                // So if we are in Winter, we CANCEL fade unless there is heat.

                // For now, let's just cancel melting in Winter for stability,
                // creating "Heat Source" melting via `checkBlockState` or task is better.
                // But `checkBlockState` is only called on PLACE.
                // We need a task to melt ice near torches?
                // For now: In Winter -> Cancel Fade.
                event.setCancelled(true);
            }
        }
    }

    private void applySeasonVisuals(Chunk chunk) {
        // Use center of chunk for season determination
        Season season = manager.getRegionManager().getSeason(chunk.getBlock(8, 64, 8).getLocation());
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
