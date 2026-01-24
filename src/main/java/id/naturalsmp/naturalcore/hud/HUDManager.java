package id.naturalsmp.naturalcore.hud;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HUDManager implements Listener {

    private final NaturalCore plugin;
    private final Map<UUID, CombatInfo> combatTracker = new HashMap<>();

    public HUDManager(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTask();
    }

    public void reload() {
        // No config to reload yet, but method required by NaturalCore
    }

    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateHUD(player);
            }
        }, 2L, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        Player p = null;
        LivingEntity target = null;

        if (event.getDamager() instanceof Player attacker) {
            p = attacker;
            if (event.getEntity() instanceof LivingEntity le)
                target = le;
        } else if (event.getEntity() instanceof Player victim) {
            p = victim;
            if (event.getDamager() instanceof LivingEntity le)
                target = le;
        }

        if (p != null && target != null) {
            combatTracker.put(p.getUniqueId(), new CombatInfo(target, System.currentTimeMillis()));
        }
    }

    private void updateHUD(Player player) {
        String finalMessage = "";

        // 1. Priority: Combat Info
        CombatInfo ci = combatTracker.get(player.getUniqueId());
        if (ci != null && System.currentTimeMillis() - ci.lastHit < 4000) { // 4s Fade out
            if (ci.entity.isValid() && !ci.entity.isDead()) {
                double hp = ci.entity.getHealth();
                double max = ci.entity.getMaxHealth();
                int percent = (int) ((hp / max) * 100);
                String heartStr = getHearts(hp, max);
                finalMessage = ChatUtils
                        .colorize("&8[&f" + ci.entity.getName() + "&8] " + heartStr + " &7" + percent + "%");
            }
        }

        // 2. Priority: Environment (Temperature) if not in combat or as secondary?
        // Let's stick to priority: if combat is active, show ONLY combat.
        // If not combat, show Temp / Tips.
        if (finalMessage.isEmpty()) {
            String tempBar = plugin.getSeasonManager().getTemperatureActionBar(player);

            // 3. Priority: Tips (handled inside SeasonManager/TipsManager currently)
            // We'll let SeasonManager provide the Final Env+Tips combination.
            finalMessage = tempBar;
        }

        if (finalMessage != null && !finalMessage.isEmpty()) {
            player.sendActionBar(ChatUtils.colorize(finalMessage));
        }
    }

    private String getHearts(double hp, double max) {
        StringBuilder sb = new StringBuilder("&c");
        int total = 10;
        int filled = (int) Math.ceil((hp / max) * total);
        for (int i = 0; i < total; i++) {
            if (i < filled)
                sb.append("❤");
            else
                sb.append("&8❤");
        }
        return sb.toString();
    }

    private static class CombatInfo {
        final LivingEntity entity;
        final long lastHit;

        CombatInfo(LivingEntity entity, long lastHit) {
            this.entity = entity;
            this.lastHit = lastHit;
        }
    }
}
