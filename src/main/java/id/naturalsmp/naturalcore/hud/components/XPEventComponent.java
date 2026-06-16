package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Displays the NaturalPass XP Event text dynamically in the rotation cycle.
 */
public class XPEventComponent extends AbstractHUDComponent {

    private int tickCounter = 0;
    private boolean isShowing = false;
    private final int CYCLE_INTERVAL = 300; // Trigger every 15 seconds
    private final int DISPLAY_DURATION = 80;  // Stay roughly 4 seconds per cycle

    public XPEventComponent(NaturalCore plugin) {
        super(plugin, "xpevent", HUDPriority.LOW); // Low priority, sits above Season but below Tips/Lagg
    }

    @Override
    public boolean shouldDisplay(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") || !Bukkit.getPluginManager().isPluginEnabled("NaturalPass")) {
            return false;
        }

        String isActive = PlaceholderAPI.setPlaceholders(player, "%naturalpass_xp_event_active%");
        if (isActive == null || !isActive.equalsIgnoreCase("yes")) {
            // Force hide if no event is running
            return false;
        }

        return isShowing;
    }

    @Override
    public void tick(int globalTick) {
        tickCounter++;
        
        if (isShowing) {
            if (tickCounter >= DISPLAY_DURATION) {
                isShowing = false;
                tickCounter = 0;
            }
        } else {
            if (tickCounter >= CYCLE_INTERVAL) {
                isShowing = true;
                tickCounter = 0;
            }
        }
    }

    @Override
    public String getContent(Player player, int tick) {
        String multiplier = PlaceholderAPI.setPlaceholders(player, "%naturalpass_xp_event_multiplier%");
        String timeRemain = PlaceholderAPI.setPlaceholders(player, "%naturalpass_xp_event_time%");
        
        String baseText = "<gradient:#FFD700:#FF6B6B>✦ NaturalPass XP Event (" + multiplier + ") ✦</gradient> <gradient:#00FF88:#45B7D1>" + timeRemain + "</gradient>";
        
        // Show with IQ for seamless integration similar to SeasonComponent
        String iqDisplay = getIQDisplay(player, tick);
        
        return baseText + " &7| " + iqDisplay;
    }

    private String getIQDisplay(Player player, int tick) {
        String iq = "100"; // default placeholder fallback

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            // Using %nskills_iq% placeholder
            String resolved = PlaceholderAPI.setPlaceholders(player, "%nskills_iq%");
            if (resolved != null && !resolved.isEmpty() && !resolved.equals("%nskills_iq%")) {
                iq = resolved;
            }
        }

        return "&d🧠 &f" + iq + " &dIQ";
    }

    @Override
    public int getTransitionDuration() {
        return 20; // Fast fade
    }
}
