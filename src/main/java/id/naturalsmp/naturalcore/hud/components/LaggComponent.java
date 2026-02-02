package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utility.NaturalLaggManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Displays ClearLagg notifications during cleanup operations.
 * High priority to ensure players see the countdown.
 */
public class LaggComponent extends AbstractHUDComponent {

    public LaggComponent(NaturalCore plugin) {
        super(plugin, "lagg", HUDPriority.HIGH);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        NaturalLaggManager lagg = plugin.getLaggManager();
        return lagg != null && lagg.getState() != NaturalLaggManager.LaggState.IDLE;
    }

    @Override
    public String getContent(Player player, int tick) {
        NaturalLaggManager lagg = plugin.getLaggManager();
        if (lagg == null)
            return null;

        NaturalLaggManager.LaggState state = lagg.getState();

        // Success states
        if (state == NaturalLaggManager.LaggState.SUCCESS_SLIDING_IN ||
                state == NaturalLaggManager.LaggState.SUCCESS_STATIC ||
                state == NaturalLaggManager.LaggState.SLIDING_OUT) {
            return ChatUtils.colorize("&a✨ &fClearing Complete! &a(Removed " + lagg.getCleanedCount() + " Items)");
        }

        // Countdown states
        int countdown = lagg.getAutoRemovalCountdown();

        // Color changes based on urgency
        String color;
        if (countdown <= 3)
            color = "&c&l"; // Red bold for critical
        else if (countdown <= 5)
            color = "&c"; // Red
        else if (countdown <= 10)
            color = "&e"; // Yellow
        else
            color = "&f"; // White

        return ChatUtils.colorize("&7🧹 &fClearing Items in " + color + countdown + "s");
    }

    @Override
    public boolean supportsTransition() {
        return true;
    }
}
