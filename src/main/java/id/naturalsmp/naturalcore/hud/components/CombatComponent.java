package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Displays combat information when a player is fighting.
 * Shows target name and health bar.
 */
public class CombatComponent extends AbstractHUDComponent {

    private final Map<UUID, CombatInfo> combatTracker = new HashMap<>();
    private static final long COMBAT_TIMEOUT = 4000; // 4 seconds

    public CombatComponent(NaturalCore plugin) {
        super(plugin, "combat", HUDPriority.MEDIUM);
    }

    @Override
    public boolean shouldDisplay(Player player) {
        CombatInfo info = combatTracker.get(player.getUniqueId());
        if (info == null)
            return false;
        if (System.currentTimeMillis() - info.lastHit > COMBAT_TIMEOUT)
            return false;
        return info.entity.isValid() && !info.entity.isDead();
    }

    @Override
    public String getContent(Player player, int tick) {
        CombatInfo info = combatTracker.get(player.getUniqueId());
        if (info == null || !info.entity.isValid())
            return null;

        double hp = info.entity.getHealth();
        double max = info.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int percent = (int) ((hp / max) * 100);
        String hearts = buildHealthBar(hp, max);

        return ChatUtils.colorize("&8⚔ &f" + info.entity.getName() + " " + hearts + " &7" + percent + "%");
    }

    /**
     * Track combat event between player and entity.
     */
    public void trackCombat(Player player, LivingEntity target) {
        combatTracker.put(player.getUniqueId(), new CombatInfo(target, System.currentTimeMillis()));
    }

    /**
     * Check if player's target HP is critical (< 20%)
     */
    public boolean isTargetCritical(Player player) {
        CombatInfo info = combatTracker.get(player.getUniqueId());
        if (info == null || !info.entity.isValid())
            return false;
        return (info.entity.getHealth() / info.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) < 0.2;
    }

    private String buildHealthBar(double hp, double max) {
        StringBuilder sb = new StringBuilder("&c");
        int total = 10;
        int filled = (int) Math.round((hp / max) * total);
        for (int i = 0; i < total; i++) {
            if (i < filled)
                sb.append("❤");
            else
                sb.append("&8❤");
        }
        return sb.toString();
    }

    private record CombatInfo(LivingEntity entity, long lastHit) {
    }
}
