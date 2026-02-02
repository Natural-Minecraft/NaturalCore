package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Displays a slow-blinking warning when temperature is dangerous.
 * Premium styling with gradient effects and clear danger indicators.
 */
public class TemperatureWarningComponent extends AbstractHUDComponent {

    private static final double COLD_THRESHOLD = 0.0; // Below 0°C = freezing
    private static final double HOT_THRESHOLD = 45.0; // Above 45°C = overheating

    // Slow blink: 1.5s ON, 0.5s OFF (40 ticks total cycle)
    private static final int BLINK_ON_DURATION = 30; // 1.5 seconds
    private static final int BLINK_CYCLE = 40; // 2 second cycle

    public TemperatureWarningComponent(NaturalCore plugin) {
        super(plugin, "temp_warning", HUDPriority.CRITICAL);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        Double temp = plugin.getSeasonManager().getPlayerTemperature(player);
        if (temp == null)
            return false;
        return temp < COLD_THRESHOLD || temp > HOT_THRESHOLD;
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
        if (temp < COLD_THRESHOLD) {
            // Freezing - ice blue gradient
            String icon = (tick % 20 < 10) ? "❄" : "🥶";
            return ChatUtils.colorize("<gradient:#00BFFF:#87CEEB>" + icon + " FREEZING! " + icon + "</gradient> &8» &b"
                    + String.format("%.1f", temp) + "°C");
        } else {
            // Overheating - fire gradient
            String icon = (tick % 20 < 10) ? "🔥" : "🥵";
            return ChatUtils.colorize("<gradient:#FF4500:#FF6347>" + icon + " OVERHEATING! " + icon
                    + "</gradient> &8» &c" + String.format("%.1f", temp) + "°C");
        }
    }

    private String getDimmedWarning(double temp) {
        // Dimmed version for blink OFF phase - keeps context visible
        if (temp < COLD_THRESHOLD) {
            return ChatUtils.colorize("&8❄ &7FREEZING... &8" + String.format("%.1f", temp) + "°C");
        } else {
            return ChatUtils.colorize("&8🔥 &7OVERHEATING... &8" + String.format("%.1f", temp) + "°C");
        }
    }

    }
}
