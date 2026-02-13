package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import org.bukkit.entity.Player;

/**
 * HUD component that shows a persistent Vanish indicator for staff.
 */
public class VanishHUDComponent extends AbstractHUDComponent {

    public VanishHUDComponent(NaturalCore plugin) {
        super(plugin, "vanish", HUDPriority.CRITICAL);
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
