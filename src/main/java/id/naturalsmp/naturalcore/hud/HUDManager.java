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

    private String lastTargetHUD = "";
    private String displayedHUD = "";
    private int transitionFrame = 0;
    private static final int TRANSITION_TIME = 15; // 1.5 seconds at 0.1s interval

    private void updateHUD(Player player) {
        // 1. Fetch all possible components
        String baseHUD = plugin.getSeasonManager().getTemperatureActionBar(player);
        String tipsHUD = plugin.getSeasonManager().getTipsManager().getDisplay(baseHUD);
        String laggHUD = plugin.getLaggManager().getDisplay(baseHUD);

        // 2. Combat Info
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

        // 3. Determine actual Target based on Priority
        String target;
        if (laggHUD != null)
            target = laggHUD;
        else if (combatHUD != null)
            target = combatHUD;
        else if (tipsHUD != null)
            target = tipsHUD;
        else
            target = (baseHUD != null) ? baseHUD : "";

        // 4. Handle Transitions
        if (!target.equals(lastTargetHUD)) {
            if (displayedHUD.isEmpty()) {
                displayedHUD = target;
            } else {
                // Trigger Scroll Transition
                transitionFrame = 1;
            }
            lastTargetHUD = target;
        }

        String finalMessage;
        if (transitionFrame > 0 && transitionFrame <= TRANSITION_TIME) {
            finalMessage = performScroll(displayedHUD, target, transitionFrame);
            transitionFrame++;
            if (transitionFrame > TRANSITION_TIME) {
                displayedHUD = target;
                transitionFrame = 0;
            }
        } else {
            finalMessage = target;
            displayedHUD = target;
        }

        if (finalMessage != null && !finalMessage.isEmpty()) {
            player.sendActionBar(ChatUtils.toComponent(finalMessage));
        }
    }

    /**
     * Scroll Animation: Pushes 'oldH' out to the left, bringing 'newH' in from the
     * right.
     */
    private String performScroll(String oldH, String newH, int frame) {
        float progress = (float) frame / TRANSITION_TIME;

        // Width of the display "window" (approximate char count)
        int windowWidth = Math.max(ChatUtils.getVisualLength(oldH), ChatUtils.getVisualLength(newH));
        if (windowWidth < 20)
            windowWidth = 20;

        // Combined string with a spacer
        String spacer = "        "; // Approx spaces
        String combined = oldH + spacer + newH;

        // Calculate split point based on visual length
        int totalVisual = ChatUtils.getVisualLength(combined);
        int scrollPos = (int) (progress * (ChatUtils.getVisualLength(oldH) + ChatUtils.getVisualLength(spacer)));

        // We use visual length for calculating the window
        return ChatUtils.colorAwareSubstring(combined, scrollPos, combined.length());
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
