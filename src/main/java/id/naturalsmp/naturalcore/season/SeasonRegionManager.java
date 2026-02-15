package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SeasonRegionManager {

    private final NaturalCore plugin;
    private final int REGION_SIZE = 500000;
    private final Map<UUID, String> playerLastRegion = new HashMap<>();

    public SeasonRegionManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public Season getSeason(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return Season.SPRING;

        // 1. Get Region Coordinates
        long regX = getRegionCoordinate(loc.getBlockX());
        long regZ = getRegionCoordinate(loc.getBlockZ());

        // 2. Deterministic Random based on Region & World Seed
        long seed = loc.getWorld().getSeed() + (regX * 341873128712L) + (regZ * 132897987541L);
        Random random = new Random(seed);

        // 3. Calculate Season based on World Time + Offset
        int duration = ConfigUtils.getSeasonConfig().getInt("season-duration-days", 7);
        long worldDay = loc.getWorld().getFullTime() / 24000L;

        // Offset ensures regions are in different seasons
        int offset = random.nextInt(duration * 4);

        long totalDays = worldDay + offset;
        int seasonIndex = (int) ((totalDays / duration) % 4);

        return Season.values()[seasonIndex];
    }

    private long getRegionCoordinate(int blockCoord) {
        // Floor division handling for negative numbers correctly
        return (long) Math.floor((double) blockCoord / REGION_SIZE);
    }

    public String getRegionKey(Location loc) {
        return getRegionCoordinate(loc.getBlockX()) + "_" + getRegionCoordinate(loc.getBlockZ());
    }

    public void updatePlayerRegion(Player p) {
        String currentKey = getRegionKey(p.getLocation());
        String lastKey = playerLastRegion.get(p.getUniqueId());

        if (!currentKey.equals(lastKey)) {
            // Region changed (or first join)
            Season newSeason = getSeason(p.getLocation());

            // Only notify if we actually have a last region (don't spam on join unless
            // intended)
            if (lastKey != null) {
                sendRegionChangeTitle(p, newSeason);
            }

            playerLastRegion.put(p.getUniqueId(), currentKey);
        }
    }

    public void removePlayer(Player p) {
        playerLastRegion.remove(p.getUniqueId());
    }

    private void sendRegionChangeTitle(Player p, Season season) {
        String title = ChatUtils.colorize(season.getIcon() + " &l" + season.name());
        String subtitle = ChatUtils.colorize("&7Entering new seasonal region...");
        p.sendTitle(title, subtitle, 10, 70, 20);
        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.5f);
    }
}
