package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Displays a slow-blinking warning when temperature is dangerous.
 * Critical priority to warn players of imminent danger.
 */
public class TemperatureWarningComponent extends AbstractHUDComponent {

    private static final double COLD_THRESHOLD = 0.0; // Below 0°C
    private static final double HOT_THRESHOLD = 45.0; // Above 45°C

    // Slow blink: 1 second ON, 0.5 second OFF (30 ticks total cycle)
    private static final int BLINK_ON_DURATION = 20; // 1 second
    private static final int BLINK_CYCLE = 30; // 1.5 second cycle

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
            return null; // Hidden phase of blink
        }

        // Determine warning type
        if (temp < COLD_THRESHOLD) {
            return ChatUtils
                    .colorize("&b⚠ &3&lTEMPERATURE DANGEROUS! &b⚠ &7(Freezing: " + String.format("%.1f", temp) + "°C)");
        } else {
            return ChatUtils.colorize(
                    "&c⚠ &4&lTEMPERATURE DANGEROUS! &c⚠ &7(Overheating: " + String.format("%.1f", temp) + "°C)");
        }
    }

    @Override
    public boolean supportsTransition() {
        return false; // Blink effect handles its own transitions
    }
}
