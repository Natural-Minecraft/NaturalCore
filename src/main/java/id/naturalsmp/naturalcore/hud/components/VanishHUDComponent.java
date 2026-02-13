package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.entity.Player;

/**
 * HUD component that shows a persistent Vanish indicator for staff.
 */
public class VanishHUDComponent extends HUDComponent {

    public VanishHUDComponent(NaturalCore plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return "vanish";
    }

    @Override
    public Priority getPriority() {
        return Priority.CRITICAL; // High priority to ensure staff know they are vanished
    }

    @Override
    public boolean shouldDisplay(Player player) {
        return plugin.getVanishManager().isVanished(player);
    }

    @Override
    public String getContent(Player player, int tick) {
        // Simple but noticeable indicator
        return "&#6CCAFE&l👻 ᴠᴀɴɪsʜᴇᴅ";
    }

    @Override
    public int getTransitionDuration() {
        return 2; // Instant popup
    }
}
