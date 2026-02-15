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

        // Get mana from PlaceholderAPI
        String manaDisplay = getManaDisplay(player);

        // Premium format: Season | Temp | Mana
        return seasonGradient + seasonName + " &7| " + tempDisplay + " &7| " + manaDisplay;
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

    private String getManaDisplay(Player player) {
        String mana = "N/A";
        String maxMana = "N/A";

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            // Using AuraSkills placeholders specifically
            mana = PlaceholderAPI.setPlaceholders(player, "%auraskills_mana_int%");
            maxMana = PlaceholderAPI.setPlaceholders(player, "%auraskills_mana_max_int%");

            // Clean up if placeholders didn't resolve or returned empty
            if (mana == null || mana.isEmpty() || mana.equals("%auraskills_mana_int%"))
                mana = "0";
            if (maxMana == null || maxMana.isEmpty() || maxMana.equals("%auraskills_mana_max_int%"))
                maxMana = "0";

            // Calculate mana percentage for dynamic colors
            String manaColor = "&b"; // Default color
            try {
                int current = (int) Double.parseDouble(mana); // handle potential floats
                int total = (int) Double.parseDouble(maxMana);
                double ratio = total > 0 ? (double) current / total : 0;

                if (ratio >= 0.8)
                    manaColor = "&b"; // High/Full
                else if (ratio >= 0.5)
                    manaColor = "&3"; // Medium high
                else if (ratio >= 0.25)
                    manaColor = "&e"; // Low
                else
                    manaColor = "&c"; // Critical
            } catch (Exception ignored) {
            }

            return manaColor + "✦ &f" + mana + "&7/&f" + maxMana;
        }

        return "&b✦ &f" + mana + "&7/&f" + maxMana;
    }

    @Override
    public int getTransitionDuration() {
        return 40; // Allow smooth transition FROM other components (like Tips/Lagg) back to Season
    }
}
