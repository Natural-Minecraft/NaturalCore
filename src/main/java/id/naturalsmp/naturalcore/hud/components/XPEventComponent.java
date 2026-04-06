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
        
        // Show with Mana for seamless integration similar to SeasonComponent
        String manaDisplay = getManaDisplay(player, tick);
        
        return baseText + " &7| " + manaDisplay;
    }

    private String getManaDisplay(Player player, int tick) {
        String mana = "0";
        String maxMana = "0";

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            mana = PlaceholderAPI.setPlaceholders(player, "%auraskills_mana_int%");
            maxMana = PlaceholderAPI.setPlaceholders(player, "%auraskills_mana_max_int%");

            if (mana.equals("%auraskills_mana_int%")) mana = "0";
            if (maxMana.equals("%auraskills_mana_max_int%")) maxMana = "0";

            boolean hasWarning = false;
            if (plugin.getHudManager().getNotificationComponent() != null) {
                hasWarning = plugin.getHudManager().getNotificationComponent().hasActiveWarning(player, NotificationComponent.NotificationType.MANA_WARNING);
            }

            if (hasWarning) {
                boolean blinkOn = (tick % 6) < 3;
                if (blinkOn) {
                    return "&c✦ &c" + mana + "&7/&c" + maxMana;
                } else {
                    return "&4✦ &4" + mana + "&7/&4" + maxMana;
                }
            }

            String manaColor = "&b";
            try {
                int current = Integer.parseInt(mana);
                int total = Integer.parseInt(maxMana);
                double ratio = total > 0 ? (double) current / total : 0;
                if (ratio >= 0.8) manaColor = "&b";
                else if (ratio >= 0.5) manaColor = "&3";
                else if (ratio >= 0.25) manaColor = "&e";
                else manaColor = "&c";
            } catch (Exception ignored) {}

            return manaColor + "✦ &f" + mana + "&7/&f" + maxMana;
        }

        return "&b✦ &f" + mana + "&7/&f" + maxMana;
    }

    @Override
    public int getTransitionDuration() {
        return 20; // Fast fade
    }
}
