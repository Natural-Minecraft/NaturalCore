package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.entity.Player;

/**
 * Reminds players to set their home if they haven't yet.
 */
public class HomeReminderComponent extends AbstractHUDComponent {

    public HomeReminderComponent(NaturalCore plugin) {
        super(plugin, "homereminder", HUDPriority.MEDIUM);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        // Only show every 200 ticks (~10 seconds) to avoid being annoying
        // AND if they have no homes
        return (System.currentTimeMillis() / 1000) % 30 < 5 && // Show for 5 seconds every 30 seconds
                plugin.getHomeManager().getHomes(player).isEmpty();
    }

    @Override
    public String getContent(Player player, int tick) {
        return ChatUtils.colorize("&e⚠ Belum ada Home! &7Gunakan &f/sethome");
    }
}
