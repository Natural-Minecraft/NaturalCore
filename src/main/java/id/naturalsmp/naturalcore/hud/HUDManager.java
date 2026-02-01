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

    private String lastDisplayedHUD = "";
    private String targetHUD = "";
    private float overlapProgress = 0f;

    private void updateHUD(Player player) {
        // 1. Get Base HUD (Season + Mana + Tips)
        String baseHUD = plugin.getSeasonManager().getTemperatureActionBar(player);

        // 2. ClearLagg Override (High Priority)
        String laggHUD = plugin.getLaggManager().getDisplay(baseHUD);

        // 3. Combat Info Priority
        String combatHUD = null;
        CombatInfo ci = combatTracker.get(player.getUniqueId());
        if (ci != null && System.currentTimeMillis() - ci.lastHit < 4000) {
            if (ci.entity.isValid() && !ci.entity.isDead()) {
                double hp = ci.entity.getHealth();
                double max = ci.entity.getMaxHealth();
                int percent = (int) ((hp / max) * 100);
                String heartStr = getHearts(hp, max);
                combatHUD = ChatUtils
                        .colorize("&8[&f" + ci.entity.getName() + "&8] " + heartStr + " &7" + percent + "%");
            }
        }

        // --- TRANSITION LOGIC ---
        String currentTarget = (laggHUD != null) ? laggHUD : (combatHUD != null ? combatHUD : baseHUD);

        if (!currentTarget.equals(targetHUD)) {
            targetHUD = currentTarget;
            overlapProgress = 0f; // Reset transition
        }

        String finalMessage;
        if (overlapProgress < 1.0f) {
            overlapProgress += 0.1f; // Transition lasts ~10 ticks (0.5s)
            finalMessage = lerpHUD(lastDisplayedHUD, targetHUD, overlapProgress);
        } else {
            finalMessage = targetHUD;
            lastDisplayedHUD = targetHUD;
        }

        if (finalMessage != null && !finalMessage.isEmpty()) {
            player.sendActionBar(ChatUtils.toComponent(finalMessage));
        }
    }

    private String lerpHUD(String oldH, String newH, float progress) {
        if (oldH == null || oldH.isEmpty())
            return newH;
        if (progress > 0.8f)
            return newH;
        if (progress < 0.2f)
            return oldH;

        // Simple "Glitch" or "Wipe" transition effect
        int split = (int) (newH.length() * progress);
        return ChatUtils.colorAwareSubstring(newH, 0, split)
                + ChatUtils.colorAwareSubstring(oldH, split, oldH.length());
    }

    private String getHearts(double hp, double max) {
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

    private static class CombatInfo {
        final LivingEntity entity;
        final long lastHit;

        CombatInfo(LivingEntity entity, long lastHit) {
            this.entity = entity;
            this.lastHit = lastHit;
        }
    }
}
