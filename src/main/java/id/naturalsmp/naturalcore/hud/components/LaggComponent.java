package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utility.NaturalLaggManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Displays ClearLagg notifications during cleanup operations.
 * Premium visuals with countdown bar and celebration effects.
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

        // Success states - celebration effect
        if (state == NaturalLaggManager.LaggState.SUCCESS_SLIDING_IN ||
                state == NaturalLaggManager.LaggState.SUCCESS_STATIC ||
                state == NaturalLaggManager.LaggState.SLIDING_OUT) {

            // Sparkle animation
            String sparkle = (tick % 10 < 5) ? "✨" : "⭐";
            int count = lagg.getCleanedCount();

            return ChatUtils.colorize("<gradient:#55FF55:#00FF00>" + sparkle + " CLEANUP COMPLETE " + sparkle
                    + "</gradient> &8» &fRemoved &a" + count + " &fitems");
        }

        // Countdown states
        int countdown = lagg.getAutoRemovalCountdown();

        // Build countdown progress bar
        String progressBar = buildCountdownBar(countdown, tick);

        // Urgency-based styling
        String urgencyColor = getUrgencyColor(countdown);
        String icon = getUrgencyIcon(countdown, tick);

        return ChatUtils.colorize(icon + " &fClearing in " + urgencyColor + countdown + "s " + progressBar);
    }

    private String buildCountdownBar(int countdown, int tick) {
        // 15 second countdown = full bar
        int maxTime = 15;
        int filledBlocks = Math.min(countdown, maxTime);
        int emptyBlocks = maxTime - filledBlocks;

        StringBuilder bar = new StringBuilder("&8[");

        // Color based on time remaining
        String fillColor;
        if (countdown > 10)
            fillColor = "&a"; // Green - plenty of time
        else if (countdown > 5)
            fillColor = "&e"; // Yellow - hurry up
        else if (countdown > 3)
            fillColor = "&6"; // Orange - urgent
        else
            fillColor = "&c"; // Red - critical

        for (int i = 0; i < filledBlocks; i++) {
            bar.append(fillColor).append("▌");
        }
        for (int i = 0; i < emptyBlocks; i++) {
            bar.append("&8░");
        }

        bar.append("&8]");
        return bar.toString();
    }

    private String getUrgencyColor(int countdown) {
        if (countdown <= 3)
            return "&c&l"; // Red bold
        if (countdown <= 5)
            return "&c"; // Red
        if (countdown <= 10)
            return "&e"; // Yellow
        return "&f"; // White
    }

    private String getUrgencyIcon(int countdown, int tick) {
        if (countdown <= 3) {
            // Rapid blink for critical
            return (tick % 6 < 3) ? "&c🧹" : "&4🧹";
        } else if (countdown <= 5) {
            // Slow blink for urgent
            return (tick % 10 < 5) ? "&e🧹" : "&6🧹";
        }
        return "&7🧹";
    }

    @Override
    public boolean supportsTransition() {
        return true;
    }
}
