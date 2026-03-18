package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.entity.Player;

/**
 * Displays a slow-blinking warning when temperature is dangerous.
 * Premium styling with gradient effects and clear danger indicators.
 */
public class TemperatureWarningComponent extends AbstractHUDComponent {

    private double getColdThreshold() {
        return ConfigUtils.getDouble("temperature.freezing_threshold", -10.0);
    }

    private double getHotThreshold() {
        return ConfigUtils.getDouble("temperature.overheating_threshold", 75.0);
    }

    // Slow blink: 1.5s ON, 0.5s OFF (40 ticks total cycle)
    private static final int BLINK_ON_DURATION = 30; // 1.5 seconds
    private static final int BLINK_CYCLE = 40; // 2 second cycle

    public TemperatureWarningComponent(NaturalCore plugin) {
        super(plugin, "temp_warning", HUDPriority.CRITICAL);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        if (player.getWorld().getName().toLowerCase().startsWith("dungeon")) {
            return false;
        }

        Double temp = plugin.getSeasonManager().getPlayerTemperature(player);
        if (temp == null)
            return false;
        // STRICT check: ONLY display if actually critical.
        // This ensures lower priority HUDs (like Tips) can show up when temp is normal.
        return temp <= getColdThreshold() || temp >= getHotThreshold();
    }

    @Override
    public String getContent(Player player, int tick) {
        Double temp = plugin.getSeasonManager().getPlayerTemperature(player);
        if (temp == null)
            return null;

        // Slow blink effect
        int blinkPhase = tick % BLINK_CYCLE;
        if (blinkPhase >= BLINK_ON_DURATION) {
            // During OFF phase, show dimmed version instead of nothing
            return getDimmedWarning(temp);
        }

        // Full warning during ON phase
        return getFullWarning(temp, tick);
    }

    private String getFullWarning(double temp, int tick) {
        if (temp < getColdThreshold()) {
            // Freezing - ice blue gradient
            String icon = (tick % 20 < 10) ? "❄" : "🥶";
            return "<gradient:#00BFFF:#87CEEB>" + icon + " FREEZING! " + icon + "</gradient> &8» &b"
                    + String.format("%.1f", temp) + "°C";
        } else {
            // Overheating - fire gradient
            String icon = (tick % 20 < 10) ? "🔥" : "🥵";
            return "<gradient:#FF4500:#FF6347>" + icon + " OVERHEATING! " + icon
                    + "</gradient> &8» &c" + String.format("%.1f", temp) + "°C";
        }
    }

    private String getDimmedWarning(double temp) {
        // Dimmed version for blink OFF phase - keeps context visible
        if (temp < getColdThreshold()) {
            return "&8❄ &7FREEZING... &8" + String.format("%.1f", temp) + "°C";
        } else {
            return "&8🔥 &7OVERHEATING... &8" + String.format("%.1f", temp) + "°C";
        }
    }
}
