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

    private int lastBroadcastSec = -1;

    @Override
    public boolean shouldDisplay(Player player) {
        NaturalLaggManager lagg = plugin.getLaggManager();
        return lagg != null && lagg.getState() != NaturalLaggManager.LaggState.IDLE;
    }

    @Override
    public void tick(int tick) {
        NaturalLaggManager lagg = plugin.getLaggManager();
        if (lagg == null)
            return;

        int countdown = lagg.getAutoRemovalCountdown();
        if (lagg.getState() == NaturalLaggManager.LaggState.COUNTDOWN) {
            if (countdown != lastBroadcastSec) {
                lastBroadcastSec = countdown;
                // Play sounds to get player attention
                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (countdown <= 3) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    } else if (countdown == 5 || countdown == 10) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                    }
                }
            }
        } else {
            lastBroadcastSec = -1;
        }
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

            return ChatUtils.colorize("<gradient:#55FF55:#FFFFFF:#55FF55>" + sparkle + " CLEANUP COMPLETE " + sparkle
                    + "</gradient> &8» &fRemoved &a" + count + " &fitems");
        }

        // Countdown states
        int countdown = lagg.getAutoRemovalCountdown();

        // Build countdown progress bar
        String progressBar = buildCountdownBar(countdown, tick);

        // Urgency-based styling
        String urgencyColor = getUrgencyColor(countdown);
        String icon = getUrgencyIcon(countdown, tick);

        // Aesthetic Notice: Pulsing color for critical time
        if (countdown <= 3 && tick % 4 < 2) {
            urgencyColor = "&f&l"; // Pulse white
        }

        return ChatUtils.colorize(icon + " &7ClearLagg &8» " + urgencyColor + countdown + "s " + progressBar);
    }

    private String buildCountdownBar(int countdown, int tick) {
        // 15 second countdown
        int maxTime = 15;
        int filledBlocks = Math.min(countdown, maxTime);
        int emptyBlocks = maxTime - filledBlocks;

        StringBuilder bar = new StringBuilder("&8[");

        // Color based on time remaining
        String fillColor;
        if (countdown > 10)
            fillColor = "&#55FF55"; // Green
        else if (countdown > 5)
            fillColor = "&#FFFF55"; // Yellow
        else if (countdown > 3)
            fillColor = "&#FFAA00"; // Orange
        else
            fillColor = "&#FF5555"; // Red

        for (int i = 0; i < filledBlocks; i++) {
            bar.append(fillColor).append("┃");
        }
        for (int i = 0; i < emptyBlocks; i++) {
            bar.append("&8·");
        }

        bar.append("&8]");
        return bar.toString();
    }

    private String getUrgencyColor(int countdown) {
        if (countdown <= 3)
            return "&#FF5555&l"; // Red bold
        if (countdown <= 5)
            return "&#FF5555"; // Red
        if (countdown <= 10)
            return "&#FFFF55"; // Yellow
        return "&#FFFFFF"; // White
    }

    private String getUrgencyIcon(int countdown, int tick) {
        if (countdown <= 3) {
            return (tick % 4 < 2) ? "&#FF5555⚠" : "&#FFFFFF⚠";
        } else if (countdown <= 5) {
            return "&#FFFF55🧹";
        }
        return "&#AAAAAA🧹";
    }

    @Override
    public int getTransitionDuration() {
        return 40; // Balanced scrolling speed
    }
}
