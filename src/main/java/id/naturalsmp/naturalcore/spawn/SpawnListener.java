package id.naturalsmp.naturalcore.spawn;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class SpawnListener implements Listener {

    private final NaturalCore plugin;

    public SpawnListener(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String currentWorld = p.getWorld().getName();

        // 1. FIRST JOIN HANDLING (Priority) — Prologue Redirect
        if (!hasCompletedPrologue(p)) {
            // Prologue: teleport to quest_sky world, no kit yet (kit given after prologue)
            String questWorldName = plugin.getConfig().getString("prologue.quest-sky-world", "quest_sky");
            org.bukkit.World questWorld = org.bukkit.Bukkit.getWorld(questWorldName);

            if (questWorld != null) {
                // Read spawn location from config or use world spawn / sky-spawn
                double x = 0.0;
                double y = 226.0;
                double z = 0.0;
                float yaw = 0.0f;
                float pitch = 30.0f;
                boolean hasNI = false;

                org.bukkit.plugin.Plugin ni = org.bukkit.Bukkit.getPluginManager().getPlugin("NaturalInteraction");
                if (ni != null && ni.isEnabled()) {
                    hasNI = true;
                    x = ni.getConfig().getDouble("prologue.sky-spawn.x", 0.0);
                    y = ni.getConfig().getDouble("prologue.sky-spawn.y", 226.0);
                    z = ni.getConfig().getDouble("prologue.sky-spawn.z", 0.0);
                    yaw = (float) ni.getConfig().getDouble("prologue.sky-spawn.yaw", 0.0);
                    pitch = (float) ni.getConfig().getDouble("prologue.sky-spawn.pitch", 30.0);
                } else {
                    x = plugin.getConfig().getDouble("prologue.spawn.x", 0.5);
                    y = plugin.getConfig().getDouble("prologue.spawn.y", 100.0);
                    z = plugin.getConfig().getDouble("prologue.spawn.z", 0.5);
                    yaw = (float) plugin.getConfig().getDouble("prologue.spawn.yaw", 0.0);
                    pitch = (float) plugin.getConfig().getDouble("prologue.spawn.pitch", 0.0);
                }

                org.bukkit.Location questSpawn = new org.bukkit.Location(questWorld, x, y, z, yaw, pitch);
                p.teleport(questSpawn);

                // Set adventure mode to prevent breaking blocks
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);

                // Make player invisible for cinematic feel
                p.addPotionEffect(
                        new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY,
                                Integer.MAX_VALUE, 0, false, false, false));

                p.setAllowFlight(true);
                p.setFlying(true);
                p.setGravity(false);

                if (!hasNI) {
                    // Screen fade-in effect using ScreenEffects addon (fallback only)
                    org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
                                "screeneffect fullscreen BLACK 20 40 20 freeze " + p.getName());
                    }, 5L); // Small delay to ensure player is loaded

                    p.setAllowFlight(false);
                    p.setFlying(false);
                    p.setGravity(true);
                }
            } else {
                // Fallback: quest_sky world not loaded — use old behavior
                plugin.getLogger().warning("Prologue world '" + questWorldName + "' not found! Using default spawn.");
                plugin.getSpawnManager().teleport(p);
                p.setAllowFlight(false);
                p.setFlying(false);
            }

            return; // Done with first join
        }

        // 2. EXISTING PLAYER JOIN HANDLING
        List<String> allowed = ConfigUtils.getStringList("spawn.allowed-join-worlds");

        // Allow vanilla worlds implicitly for existing players
        if (currentWorld.equals("world") || currentWorld.equals("world_nether")
                || currentWorld.equals("world_the_end")) {
            return;
        }

        // If in an non-allowed world, force to spawn
        if (!allowed.contains(currentWorld)) {
            plugin.getSpawnManager().teleport(p);
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }

    private boolean hasCompletedPrologue(Player p) {
        org.bukkit.plugin.Plugin ni = org.bukkit.Bukkit.getPluginManager().getPlugin("NaturalInteraction");
        if (ni != null && ni.isEnabled()) {
            try {
                Object interactionManager = ni.getClass().getMethod("getInteractionManager").invoke(ni);
                Object tracker = interactionManager.getClass().getMethod("getCompletionTracker").invoke(interactionManager);
                String prologueId = ni.getConfig().getString("prologue.interaction-id", "prologue");
                return (boolean) tracker.getClass().getMethod("hasCompleted", java.util.UUID.class, String.class).invoke(tracker, p.getUniqueId(), prologueId);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check prologue completion via NaturalInteraction: " + e.getMessage());
            }
        }
        return p.hasPlayedBefore();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // Jika player tidak punya bed spawn atau anchor spawn
        if (!e.isBedSpawn() && !e.isAnchorSpawn()) {
            org.bukkit.Location spawn = plugin.getSpawnManager().getSpawn();
            if (spawn != null) {
                e.setRespawnLocation(spawn);
            }
        }
    }
}
