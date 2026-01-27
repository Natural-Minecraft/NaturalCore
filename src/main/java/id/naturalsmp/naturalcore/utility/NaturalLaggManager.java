package id.naturalsmp.naturalcore.utility;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NaturalLaggManager implements Listener {

    private final NaturalCore plugin;

    // Configuration Fields
    private boolean enabled = true;
    private boolean autoRemovalEnabled = true;
    private int autoRemovalInterval = 300;
    private List<Integer> warningTimes = Arrays.asList(60, 30, 10, 5, 3, 2, 1);
    private boolean mergingEnabled = true;
    private int mergingThreshold = 5;
    private double mergingRadiusSq = 4.0;
    private Set<String> mergingBlacklist = new HashSet<>();
    private int defaultCleanupDuration = 10;
    private Set<String> itemWhitelist = new HashSet<>();
    private boolean removeDeathDrops = false;
    private boolean chunkLimiterEnabled = true;
    private int maxEntitiesPerChunk = 50;
    private int chunkLimiterInterval = 60;
    private double tpsThreshold = 10.0;
    private int optimizerCheckInterval = 30;
    private Set<String> excludedWorlds = new HashSet<>();

    // State
    private BukkitTask autoRemovalTask;
    private int autoRemovalCountdown;
    private final Set<UUID> recentDeathDrops = ConcurrentHashMap.newKeySet();

    // Animation States
    public enum LaggState {
        IDLE, SLIDING_IN, COUNTDOWN, SUCCESS_SLIDING_IN, SUCCESS_STATIC, SLIDING_OUT
    }

    private LaggState state = LaggState.IDLE;
    private int animationFrame = 0;
    private final int MAX_FRAMES = 20;
    private int countdownSeconds = 0;
    private int currentTick = 0;
    private int successStayTicks = 0;
    private int cleanedCount = 0;

    public NaturalLaggManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (enabled) {
            startTasks();
        }
    }

    public void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("lagg.enabled", true);
        this.autoRemovalEnabled = plugin.getConfig().getBoolean("lagg.auto-removal.enabled", true);
        this.autoRemovalInterval = plugin.getConfig().getInt("lagg.auto-removal.interval", 300);
        this.warningTimes = plugin.getConfig().getIntegerList("lagg.auto-removal.warning-times");
        if (warningTimes.isEmpty())
            warningTimes = Arrays.asList(60, 30, 10, 5, 3, 2, 1);

        this.mergingEnabled = plugin.getConfig().getBoolean("lagg.merging.enabled", true);
        this.mergingThreshold = plugin.getConfig().getInt("lagg.merging.threshold", 5);
        double radius = plugin.getConfig().getDouble("lagg.merging.radius", 2.0);
        this.mergingRadiusSq = radius * radius;
        this.mergingBlacklist = new HashSet<>(plugin.getConfig().getStringList("lagg.merging.blacklist"));

        this.defaultCleanupDuration = plugin.getConfig().getInt("lagg.cleanup.default-duration", 10);
        this.itemWhitelist = new HashSet<>(plugin.getConfig().getStringList("lagg.cleanup.item-whitelist"));
        this.removeDeathDrops = plugin.getConfig().getBoolean("lagg.cleanup.remove-death-drops", false);

        this.chunkLimiterEnabled = plugin.getConfig().getBoolean("lagg.chunk-limiter.enabled", true);
        this.maxEntitiesPerChunk = plugin.getConfig().getInt("lagg.chunk-limiter.max-entities-per-chunk", 50);
        this.chunkLimiterInterval = plugin.getConfig().getInt("lagg.chunk-limiter.check-interval", 60);

        this.tpsThreshold = plugin.getConfig().getDouble("lagg.optimizer.tps-threshold", 10.0);
        this.optimizerCheckInterval = plugin.getConfig().getInt("lagg.optimizer.check-interval", 30);
        this.excludedWorlds = new HashSet<>(plugin.getConfig().getStringList("lagg.excluded-worlds"));
    }

    private void startTasks() {
        // 1. Animation Tick (2 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                tickAnimation();
            }
        }.runTaskTimer(plugin, 2L, 2L);

        // 2. Mob Merging & TPS Optimizer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mergingEnabled)
                    performMobMerging();
                checkPerformanceOptimizer();
            }
        }.runTaskTimer(plugin, optimizerCheckInterval * 20L, optimizerCheckInterval * 20L);

        // 3. Scheduled Auto-Removal
        if (autoRemovalEnabled) {
            startAutoRemovalCycle();
        }

        // 4. Chunk Entity Limiter
        if (chunkLimiterEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    enforceChunkLimits();
                }
            }.runTaskTimer(plugin, chunkLimiterInterval * 20L, chunkLimiterInterval * 20L);
        }
    }

    private void startAutoRemovalCycle() {
        autoRemovalCountdown = autoRemovalInterval;
        autoRemovalTask = new BukkitRunnable() {
            @Override
            public void run() {
                autoRemovalCountdown--;
                if (warningTimes.contains(autoRemovalCountdown)) {
                    broadcastWarning(autoRemovalCountdown);
                }
                if (autoRemovalCountdown <= 0) {
                    startCleanup(defaultCleanupDuration);
                    autoRemovalCountdown = autoRemovalInterval;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void broadcastWarning(int seconds) {
        String msg;
        if (seconds >= 60) {
            msg = ConfigUtils.getString("messages.lagg.warning-minute",
                    "&#FFAA00&l⚠ &7Pembersihan item dalam &e%time% &7menit.");
            msg = msg.replace("%time%", String.valueOf(seconds / 60));
        } else {
            msg = ConfigUtils.getString("messages.lagg.warning-second",
                    "&#FFAA00&l⚠ &7Pembersihan item dalam &e%time% &7detik.");
            msg = msg.replace("%time%", String.valueOf(seconds));
        }
        Bukkit.broadcast(ChatUtils.toComponent(ChatUtils.colorize(msg)));
        if (seconds <= 10) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.5f);
            }
        }
    }

    public void startCleanup(int seconds) {
        if (state != LaggState.IDLE)
            return;
        this.countdownSeconds = seconds;
        this.state = LaggState.SLIDING_IN;
        this.animationFrame = 0;
        this.currentTick = 0;
    }

    private void tickAnimation() {
        if (state == LaggState.IDLE)
            return;
        animationFrame++;
        if (state == LaggState.SLIDING_IN) {
            if (animationFrame >= MAX_FRAMES) {
                state = LaggState.COUNTDOWN;
                animationFrame = 0;
            }
            if (animationFrame % 2 == 0)
                playTickSound(1.5f);
        } else if (state == LaggState.COUNTDOWN) {
            currentTick += 2;
            if (currentTick >= 20) {
                countdownSeconds--;
                currentTick = 0;
                if (countdownSeconds <= 0) {
                    performClean();
                    state = LaggState.SUCCESS_SLIDING_IN;
                    animationFrame = 0;
                } else if (countdownSeconds <= 5) {
                    playTickSound(2.0f);
                }
            }
        } else if (state == LaggState.SUCCESS_SLIDING_IN) {
            if (animationFrame >= MAX_FRAMES) {
                state = LaggState.SUCCESS_STATIC;
                animationFrame = 0;
                successStayTicks = 0;
            }
        } else if (state == LaggState.SUCCESS_STATIC) {
            successStayTicks += 2;
            if (successStayTicks >= 60) {
                state = LaggState.SLIDING_OUT;
                animationFrame = 0;
            }
        } else if (state == LaggState.SLIDING_OUT) {
            if (animationFrame >= MAX_FRAMES) {
                state = LaggState.IDLE;
                animationFrame = 0;
            }
            if (animationFrame % 2 == 0)
                playTickSound(1.2f);
        }
    }

    private void playTickSound(float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, pitch);
        }
    }

    public String getDisplay(String currentHUD) {
        if (state == LaggState.IDLE)
            return null;
        String hud = ChatUtils.colorize(currentHUD);
        String laggText = getLaggContent();
        if (state == LaggState.SLIDING_IN || state == LaggState.SUCCESS_SLIDING_IN) {
            float progress = (float) animationFrame / MAX_FRAMES;
            int cutLen = (int) (hud.length() * progress);
            int revealLen = (int) (laggText.length() * progress);
            return ChatUtils.colorAwareSubstring(hud, cutLen, hud.length()) + "     "
                    + ChatUtils.colorAwareSubstring(laggText, 0, revealLen);
        } else if (state == LaggState.SLIDING_OUT) {
            float progress = (float) animationFrame / MAX_FRAMES;
            int cutLen = (int) (laggText.length() * progress);
            int revealLen = (int) (hud.length() * progress);
            return ChatUtils.colorAwareSubstring(laggText, cutLen, laggText.length()) + "     "
                    + ChatUtils.colorAwareSubstring(hud, 0, revealLen);
        }
        return laggText;
    }

    private String getLaggContent() {
        if (state == LaggState.COUNTDOWN) {
            String arrows = "»»»»»";
            int offset = animationFrame % arrows.length();
            String scroll = arrows.substring(offset) + arrows.substring(0, offset);
            String msg = ConfigUtils.getString("messages.lagg.action-bar.countdown",
                    "&#FF5555&l%scroll% &fCLEARING ITEMS IN &#FF5555&l%time%s &f%scroll%");
            return ChatUtils
                    .colorize(msg.replace("%scroll%", scroll).replace("%time%", String.valueOf(countdownSeconds)));
        } else if (state == LaggState.SUCCESS_STATIC || state == LaggState.SUCCESS_SLIDING_IN) {
            String msg = ConfigUtils.getString("messages.lagg.cleanup-complete",
                    "&#55FF55&l✔ CLEANUP COMPLETE &7(Removed %count% items)");
            return ChatUtils.colorize(msg.replace("%count%", String.valueOf(cleanedCount)));
        }
        return "";
    }

    private void performClean() {
        AtomicInteger count = new AtomicInteger(0);
        Bukkit.getWorlds().forEach(world -> {
            if (excludedWorlds.contains(world.getName()))
                return;
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    // Skip whitelisted items
                    ItemStack stack = item.getItemStack();
                    if (itemWhitelist.contains(stack.getType().name()))
                        continue;
                    // Skip death drops if configured
                    if (!removeDeathDrops && recentDeathDrops.contains(entity.getUniqueId()))
                        continue;
                    entity.remove();
                    count.incrementAndGet();
                }
            }
        });
        this.cleanedCount = count.get();
        recentDeathDrops.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    private void performMobMerging() {
        for (World world : Bukkit.getWorlds()) {
            if (excludedWorlds.contains(world.getName()))
                continue;
            List<Entity> entities = new ArrayList<>(world.getEntities());
            Map<String, List<LivingEntity>> groups = new HashMap<>();
            for (Entity e : entities) {
                if (e instanceof LivingEntity le && !(e instanceof Player) && !e.hasMetadata("NPC")) {
                    String type = le.getType().name();
                    if (mergingBlacklist.contains(type))
                        continue;
                    groups.computeIfAbsent(type, k -> new ArrayList<>()).add(le);
                }
            }
            for (List<LivingEntity> group : groups.values()) {
                if (group.size() < mergingThreshold)
                    continue;
                for (int i = 0; i < group.size(); i++) {
                    LivingEntity base = group.get(i);
                    if (!base.isValid())
                        continue;
                    List<LivingEntity> nearby = new ArrayList<>();
                    for (int j = i + 1; j < group.size(); j++) {
                        LivingEntity other = group.get(j);
                        if (other.isValid()
                                && base.getLocation().distanceSquared(other.getLocation()) < mergingRadiusSq) {
                            nearby.add(other);
                        }
                    }
                    if (nearby.size() >= 4) {
                        int total = nearby.size() + 1;
                        for (LivingEntity le : nearby)
                            le.remove();
                        base.setCustomName(ChatUtils.colorize("&e&l" + base.getName() + " &7x" + total));
                        base.setCustomNameVisible(true);
                        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                            String holoName = "merged_" + base.getUniqueId();
                            Hologram holo = DHAPI.getHologram(holoName);
                            if (holo == null) {
                                holo = DHAPI.createHologram(holoName, base.getLocation().add(0, 2.2, 0), true);
                                DHAPI.setHologramLines(holo, Arrays.asList("&#FFEE00❂ &#FFEE00&lSTACKED MOB &#FFEE00❂",
                                        "&7Quantity: &f" + total));
                            } else {
                                DHAPI.moveHologram(holo, base.getLocation().add(0, 2.2, 0));
                                DHAPI.setHologramLine(holo, 1, "&7Quantity: &f" + total);
                            }
                        }
                    }
                }
            }
        }
    }

    private void enforceChunkLimits() {
        for (World world : Bukkit.getWorlds()) {
            if (excludedWorlds.contains(world.getName()))
                continue;
            for (Chunk chunk : world.getLoadedChunks()) {
                Entity[] entities = chunk.getEntities();
                if (entities.length > maxEntitiesPerChunk) {
                    int toRemove = entities.length - maxEntitiesPerChunk;
                    int removed = 0;
                    for (Entity e : entities) {
                        if (removed >= toRemove)
                            break;
                        if (e instanceof Item
                                || (e instanceof LivingEntity && !(e instanceof Player) && !e.hasMetadata("NPC"))) {
                            e.remove();
                            removed++;
                        }
                    }
                }
            }
        }
    }

    private void checkPerformanceOptimizer() {
        double tps = Bukkit.getTPS()[0];
        if (tps < tpsThreshold) {
            plugin.getLogger().warning(
                    "TPS CRITICAL (" + String.format("%.1f", tps) + ")! Performing aggressive chunk unloader...");
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    if (chunk.getInhabitedTime() < 2400 && !isPlayerNearby(chunk)) {
                        chunk.unload(true);
                    }
                }
            }
        }
    }

    private boolean isPlayerNearby(Chunk chunk) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getChunk().equals(chunk))
                return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Mark death drops to protect them
        if (!removeDeathDrops) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Entity entity : e.getEntity().getNearbyEntities(3, 3, 3)) {
                    if (entity instanceof Item) {
                        recentDeathDrops.add(entity.getUniqueId());
                    }
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onMobDie(EntityDeathEvent e) {
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            String holoName = "merged_" + e.getEntity().getUniqueId();
            if (DHAPI.getHologram(holoName) != null) {
                DHAPI.removeHologram(holoName);
            }
        }
    }

    public LaggState getState() {
        return state;
    }

    public int getAutoRemovalCountdown() {
        return autoRemovalCountdown;
    }
}
