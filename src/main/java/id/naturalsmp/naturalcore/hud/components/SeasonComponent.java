package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.season.Season;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Displays the default HUD: Season icon, Temperature, and Mana.
 * Always shows as the baseline when nothing else is active.
 */
public class SeasonComponent extends AbstractHUDComponent {

    public SeasonComponent(NaturalCore plugin) {
        super(plugin, "season", HUDPriority.DEFAULT);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        return true; // Always available as fallback
    }

    @Override
    public String getContent(Player player, int tick) {
        Season currentSeason = plugin.getSeasonManager().getCurrentSeason();
        Double temp = plugin.getSeasonManager().getPlayerTemperature(player);

        FileConfiguration config = ConfigUtils.getSeasonConfig();
        String format = config.getString("action-bar.format");
        if (format == null) {
            format = "%icon% %name% &8│ &7🌡 %temp%°C &8│ &b✦ %mana%/%max_mana%";
        }

        String msg = format
                .replace("%icon%", currentSeason.getIcon())
                .replace("%name%",
                        config.getString("seasons." + currentSeason.name() + ".display-name", currentSeason.name()))
                .replace("%temp%", String.format("%.1f", temp));

        // PlaceholderAPI for mana
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders(player, msg);
        } else {
            msg = msg.replace("%mana%", "N/A").replace("%max_mana%", "N/A");
        }

        return msg;
    }
}
