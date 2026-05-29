package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.season.Season;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Displays the default HUD: Season icon, Temperature, and Mana.
 * Premium visual design with gradients and icons.
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
        Season currentSeason = plugin.getSeasonManager().getRegionManager().getSeason(player.getLocation());
        Double temp = plugin.getSeasonManager().getPlayerTemperature(player);

        // Get season-specific gradient colors
        String seasonGradient = getSeasonGradient(currentSeason);
        String seasonName = getSeasonDisplayName(currentSeason);
        String tempDisplay = formatTemperature(temp);

        // Get IQ from PlaceholderAPI
        String iqDisplay = getIQDisplay(player, tick);

        // Premium format: Season | Temp | IQ
        return seasonGradient + seasonName + " &7| " + tempDisplay + " &7| " + iqDisplay;
    }

    private String getSeasonGradient(Season season) {
        return switch (season) {
            case SPRING -> "<gradient:#77DD77:#98FB98>"; // Soft green
            case SUMMER -> "<gradient:#FFD700:#FFA500>"; // Golden orange
            case AUTUMN -> "<gradient:#D2691E:#8B4513>"; // Brown orange
            case WINTER -> "<gradient:#87CEEB:#4169E1>"; // Ice blue
        };
    }

    private String getSeasonDisplayName(Season season) {
        String icon = switch (season) {
            case SPRING -> "🌸";
            case SUMMER -> "☀";
            case AUTUMN -> "🍂";
            case WINTER -> "❄";
        };

        FileConfiguration config = ConfigUtils.getSeasonConfig();
        String name = config.getString("seasons." + season.name() + ".display-name", season.name());

        // Strip existing legacy colors to ensure the premium gradient applies cleanly
        String cleanName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));

        return icon + " <b>" + cleanName + "</b></gradient>";
    }

    private String formatTemperature(Double temp) {
        if (temp == null)
            temp = 20.0;

        // Color based on temperature
        String color;
        String icon;
        if (temp < 0) {
            color = "&b"; // Cyan for freezing
            icon = "🥶";
        } else if (temp < 10) {
            color = "&3"; // Dark cyan for cold
            icon = "❄";
        } else if (temp < 25) {
            color = "&a"; // Green for comfortable
            icon = "🌡";
        } else if (temp < 35) {
            color = "&e"; // Yellow for warm
            icon = "🌡";
        } else if (temp < 45) {
            color = "&6"; // Gold for hot
            icon = "🔥";
        } else {
            color = "&c"; // Red for dangerous
            icon = "🥵";
        }

        return color + icon + " " + String.format("%.1f", temp) + "°C";
    }

    private String getIQDisplay(Player player, int tick) {
        String iq = "100"; // default placeholder fallback

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            // Using %naturalschool_iq% placeholder
            String resolved = PlaceholderAPI.setPlaceholders(player, "%naturalschool_iq%");
            if (resolved != null && !resolved.isEmpty() && !resolved.equals("%naturalschool_iq%")) {
                iq = resolved;
            }
        }

        return "&d🧠 &f" + iq + " &dIQ";
    }

    @Override
    public int getTransitionDuration() {
        return 40; // Allow smooth transition FROM other components (like Tips/Lagg) back to Season
    }
}
