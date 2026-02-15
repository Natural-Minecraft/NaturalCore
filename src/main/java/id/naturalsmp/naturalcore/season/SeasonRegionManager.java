package id.naturalsmp.naturalcore.season;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.season.SeasonManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SeasonRegionManager {

    private final NaturalCore plugin;
    private final SeasonManager manager;

    public SeasonRegionManager(NaturalCore plugin, SeasonManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public Season getSeason(Location loc) {
        return manager.getCurrentSeason();
    }

    public String getRegionKey(Location loc) {
        return "global";
    }

    public void updatePlayerRegion(Player p) {
        // No-op for global season
    }

    public void removePlayer(Player p) {
        // No-op
    }
}
