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
 * Premium visuals with gradient health bar and pulse effects.
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

        // Build premium health bar with gradient
        String healthBar = buildPremiumHealthBar(hp, max, percent, tick);

        // Entity name with combat icon
        String entityName = info.entity.getName();

        // Pulse effect when target is critical (< 20% HP)
        String prefix;
        String contentColor = "&f";
        if (percent < 20) {
            // Heartbeat pulse for critical
            boolean blink = (tick % 10 < 5);
            prefix = blink ? "&c❤" : "&4❤";
            contentColor = blink ? "&c" : "&f";
        } else {
            prefix = "&7⚔";
        }

        return ChatUtils.colorize(
                prefix + " " + contentColor + entityName + " &7| " + healthBar + " &7| " + getPercentColor(percent)
                        + percent + "%");
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

    private String buildPremiumHealthBar(double hp, double max, int percent, int tick) {
        StringBuilder sb = new StringBuilder("&8[");
        int total = 10;
        int filled = (int) Math.round((hp / max) * total);

        // Gradient based on HP percentage
        String fillColor = getHealthColor(percent);

        for (int i = 0; i < total; i++) {
            if (i < filled) {
                sb.append(fillColor).append("█");
            } else {
                sb.append("&8░");
            }
        }

        sb.append("&8]");
        return sb.toString();
    }

    private String getHealthColor(int percent) {
        if (percent > 70)
            return "&a"; // Green - healthy
        if (percent > 40)
            return "&e"; // Yellow - damaged
        if (percent > 20)
            return "&6"; // Orange - low
        return "&c"; // Red - critical
    }

    private String getPercentColor(int percent) {
        if (percent > 50)
            return "&a";
        if (percent > 25)
            return "&e";
        return "&c";
    }

    private record CombatInfo(LivingEntity entity, long lastHit) {
    }
}
