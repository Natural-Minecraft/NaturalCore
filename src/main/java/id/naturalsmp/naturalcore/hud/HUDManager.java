package id.naturalsmp.naturalcore.hud;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.animations.ActionBarAnimator;
import id.naturalsmp.naturalcore.hud.components.*;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Central HUD Manager that orchestrates all HUD components.
 * Uses a priority-based system to determine which component to display.
 * Handles smooth transitions between components.
 */
public class HUDManager implements Listener {

    private final NaturalCore plugin;
    private final List<HUDComponent> components = new ArrayList<>();

    // Animation state per player
    private final Map<UUID, PlayerHUDState> playerStates = new HashMap<>();

    // Global tick counter
    private int globalTick = 0;
    private BukkitTask updaterTask;

    public HUDManager(NaturalCore plugin) {
        this.plugin = plugin;
        registerComponents();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTask();
    }

    public void stop() {
        if (updaterTask != null) {
            updaterTask.cancel();
            updaterTask = null;
        }
        playerStates.clear();
    }

    private void registerComponents() {
        // Register components in priority order (will be sorted anyway)
        components.add(new TemperatureWarningComponent(plugin));
        components.add(new LaggComponent(plugin));
        components.add(new DungeonHUDComponent(plugin));
        components.add(new CombatComponent(plugin));
        components.add(new BiomeInfoComponent(plugin));
        components.add(new TipsComponent(plugin));
        components.add(new SeasonComponent(plugin));
        components.add(new VanishHUDComponent(plugin));

        // Sort by priority (highest first)
        components.sort((a, b) -> Integer.compare(b.getPriority().getValue(), a.getPriority().getValue()));
    }

    public void reload() {
        // Reload tips component
        for (HUDComponent comp : components) {
            if (comp instanceof TipsComponent tips) {
                tips.reload();
            }
        }
    }

    private void startTask() {
        updaterTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            globalTick++;

            // Tick all components
            for (HUDComponent comp : components) {
                comp.tick(globalTick);
            }

            // Update each player
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateHUD(player);
            }
        }, 1L, 1L); // 1 Tick = 20Hz Butter Smooth Animation
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
            getCombatComponent().trackCombat(p, target);
        }
    }

    private void updateHUD(Player player) {
        // Get or create player state
        PlayerHUDState state = playerStates.computeIfAbsent(
                player.getUniqueId(),
                k -> new PlayerHUDState());

        // Find the highest priority component that wants to display
        HUDComponent activeComponent = null;
        String content = null;

        for (HUDComponent comp : components) {
            if (comp.shouldDisplay(player)) {
                String c = comp.getContent(player, globalTick);
                if (c != null && !c.isEmpty()) {
                    activeComponent = comp;
                    content = c;
                    break;
                }
            }
        }

        // Fallback to empty
        if (content == null) {
            content = "";
        }

        // Handle transitions
        String finalMessage = handleTransition(state, content, activeComponent);

        // Send to player
        if (finalMessage != null && !finalMessage.isEmpty()) {
            player.sendActionBar(ChatUtils.toComponent(ChatUtils.colorize(finalMessage)));
        }
    }

    private String handleTransition(PlayerHUDState state, String newContent, HUDComponent newComponent) {
        String componentId = (newComponent != null) ? newComponent.getId() : "none";

        // If target changed, prepare transition
        if (!componentId.equals(state.lastComponentId)) {
            state.previousContent = state.displayedContent;
            state.transitionFrame = 0;
            state.lastComponentId = componentId;
            state.transitioning = true;
        }

        int duration = (newComponent != null) ? newComponent.getTransitionDuration() : 20;

        // If duration is too short or if we're instantly switching from an empty state
        if (duration <= 2 || (state.previousContent == null || state.previousContent.isEmpty())) {
            state.transitioning = false;
            state.displayedContent = newContent;
            return newContent;
        }

        if (state.transitioning && state.transitionFrame < duration) {
            state.transitionFrame++;
            float progress = (float) state.transitionFrame / duration;

            // Use the new Reveal/Fade out transitions instead of scrolling
            // Fade out the old content for the first half, reveal the new for the second
            // half
            if (progress < 0.5f) {
                // Fade out previous
                float fadeOutProgress = progress * 2.0f;
                state.displayedContent = ActionBarAnimator.fadeOutEffect(state.previousContent, fadeOutProgress);
            } else {
                // Reveal the new
                float revealProgress = (progress - 0.5f) * 2.0f;
                state.displayedContent = ActionBarAnimator.revealEffect(newContent, revealProgress);
            }

            if (state.transitionFrame >= duration) {
                state.transitioning = false;
                state.displayedContent = newContent;
            }
        } else {
            state.displayedContent = newContent;
        }

        return state.displayedContent;
    }

    /**
     * Get the combat component for external combat tracking.
     */
    public CombatComponent getCombatComponent() {
        for (HUDComponent comp : components) {
            if (comp instanceof CombatComponent c)
                return c;
        }
        return null;
    }

    /**
     * Get the tips component for reloading.
     */
    public TipsComponent getTipsComponent() {
        for (HUDComponent comp : components) {
            if (comp instanceof TipsComponent t)
                return t;
        }
        return null;
    }

    /**
     * Player-specific HUD state for transitions.
     */
    private static class PlayerHUDState {
        String lastComponentId = "";
        String displayedContent = "";
        String previousContent = "";
        int transitionFrame = 0;
        boolean transitioning = false;
    }
}
