package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.hud.HUDPriority;
import org.bukkit.entity.Player;

/**
 * Interface for all HUD components.
 * Each component represents a distinct piece of information
 * that can be displayed on the player's action bar.
 */
public interface HUDComponent {

    /**
     * Get the unique identifier for this component.
     */
    String getId();

    /**
     * Get the priority of this component.
     * Higher priority components override lower priority ones.
     */
    HUDPriority getPriority();

    /**
     * Check if this component should currently be displayed.
     * 
     * @param player The player to check for
     * @return true if this component has content to show
     */
    boolean shouldDisplay(Player player);

    /**
     * Get the formatted content to display.
     * 
     * @param player The player to get content for
     * @param tick   Current animation tick (for animations)
     * @return The formatted string to display, or null if nothing to show
     */
    String getContent(Player player, int tick);

    /**
     * Called every tick to update internal state.
     * 
     * @param tick Current tick
     */
    default void tick(int tick) {
    }

    /**
     * Check if this component supports smooth transitions.
     * If true, HUDManager will animate transitions to/from this component.
     */
    default boolean supportsTransition() {
        return true;
    }
}
