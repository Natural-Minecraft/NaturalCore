package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utility.NaturalLaggManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Displays periodic tips to players.
 * Shows every 5 minutes for 5 seconds.
 */
public class TipsComponent extends AbstractHUDComponent {

    private List<String> tips;
    private int interval = 120; // seconds (2 minutes)
    private boolean soundEnabled = true;
    private String soundName = "BLOCK_NOTE_BLOCK_HAT";

    private String currentTip = null;
    private int stayTicks = 0;
    private static final int STAY_DURATION = 200; // 10 seconds (was 5s)
    private static final int MAX_WIDTH = 45; // Max ActionBar width for static text

    private BukkitTask scheduleTask;

    public TipsComponent(NaturalCore plugin) {
        super(plugin, "tips", HUDPriority.MEDIUM); // Medium priority (below Lagg)
        reload();
        startSchedule();
    }

    // ... (existing methods remain unchanged)

    @Override
    public void tick(int tick) {
        if (currentTip != null) {
            stayTicks += 2; // Called every 2 ticks
            if (stayTicks >= STAY_DURATION) {
                currentTip = null;
            }
        }
    }

    // ... (shouldDisplay remain unchanged)

    @Override
    public String getContent(Player player, int tick) {
        if (currentTip == null)
            return null;

        String prefix = "<gradient:#FFD700:#FFA500>💡 TIPS</gradient> &8» &f";
        String fullKey = prefix + currentTip;

        // Check visual length
        int len = ChatUtils.getVisualLength(fullKey);

        if (len <= MAX_WIDTH) {
            return fullKey;
        }

        // Marquee Logic for long tips
        int scrollRange = len - MAX_WIDTH;
        int cycleTicks = (scrollRange * 5) + 60; // 5 ticks per char + 60 ticks pause at ends
        int currentCycle = tick % cycleTicks;

        int offset = 0;
        if (currentCycle < 30) {
            offset = 0; // Pause start
        } else if (currentCycle < 30 + (scrollRange * 5)) {
            offset = (currentCycle - 30) / 5; // Scroll
        } else {
            offset = scrollRange; // Pause end
        }

        return ChatUtils.getVisualSlice(fullKey, offset, MAX_WIDTH);
    }

    @Override
    public int getTransitionDuration() {
        return 40; // Match LaggComponent scrolling speed
    }
}
