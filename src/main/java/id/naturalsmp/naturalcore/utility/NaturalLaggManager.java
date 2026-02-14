package id.naturalsmp.naturalcore.utility;

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

    public void startTasks() {
        // Mob Merge Task (every 10 seconds)
        if (mergingEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    performMobMerging();
                }
            }.runTaskTimer(plugin, 200L, 200L);
        }

        // Hologram position update (every 5 ticks = 0.25s)
        new BukkitRunnable() {
            @Override
            public void run() {
                HologramUtil.tickHolograms();
            }
        }.runTaskTimer(plugin, 20L, 5L);

        // Chunk Limiter
        if (chunkLimiterEnabled) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    enforceChunkLimits();
                }
            }.runTaskTimer(plugin, 100L, chunkLimiterInterval * 20L);
        }

        // Auto Removal Cycle
        if (autoRemovalEnabled) {
            startAutoRemovalCycle();
        }

        // Performance Optimizer
        new BukkitRunnable() {
            @Override
            public void run() {
                checkPerformanceOptimizer();
            }
        }.runTaskTimer(plugin, 200L, optimizerCheckInterval * 20L);
    }

    private void startAutoRemovalCycle() {
        autoRemovalCountdown = autoRemovalInterval;
        autoRemovalTask = new BukkitRunnable() {
            @Override
            public void run() {
                autoRemovalCountdown--;

                // 15s Sync: Chat msg AND Start Animation
                if (autoRemovalCountdown == 15) {
                    broadcastWarning(15);
                    startAnimation();
                }
                // Other warnings (only if not 15, to avoid double broadcast if 15 is in list)
                else if (warningTimes.contains(autoRemovalCountdown) && autoRemovalCountdown != 15) {
                    broadcastWarning(autoRemovalCountdown);
                }

                if (autoRemovalCountdown <= 0) {
                    performClean();
                    triggerSuccessAnimation();
                    autoRemovalCountdown = autoRemovalInterval;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void broadcastWarning(int seconds) {
        String msg;
        if (seconds >= 60) {
            msg = ConfigUtils.getString("messages.system.lagg.warning-minute",
                    "&#FFAA00&l⚠ &7Pembersihan item dalam &e%time% &7menit.");
            msg = msg.replace("%time%", String.valueOf(seconds / 60));
        } else {
            msg = ConfigUtils.getString("messages.system.lagg.warning-second",
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
        this.autoRemovalCountdown = seconds;
        // If countdown is less than or equal to 15 (animation start threshold), start
        // animation immediately
        // Otherwise, the main loop will catch it when it reaches 15.
        if (seconds <= 15) {
            startAnimation();
        }
        broadcastWarning(seconds);
    }

    // Trigger visual slide-in
    public void startAnimation() {
        if (state != LaggState.IDLE)
            return;
        this.state = LaggState.SLIDING_IN;
        this.animationFrame = 0;
    }

    // Trigger visual success
    public void triggerSuccessAnimation() {
        this.state = LaggState.SUCCESS_SLIDING_IN;
        this.animationFrame = 0;
    }

    private void tickAnimation() {
        if (state == LaggState.IDLE)
            return;

        animationFrame++;

        switch (state) {
            case SLIDING_IN -> {
                if (animationFrame >= MAX_FRAMES) {
                    state = LaggState.COUNTDOWN;
                    animationFrame = 0;
                }
                if (animationFrame % 2 == 0)
                    playTickSound(1.5f);
            }
            case COUNTDOWN -> {
                // Visual ticks for arrows, logic driven by autoRemovalCountdown
                // Check if we should stop (master countdown reset or finished)
                if (autoRemovalCountdown > 15 || autoRemovalCountdown <= 0) {
                    // Handled by triggerSuccessAnimation call in main loop
                } else if (autoRemovalCountdown <= 5 && animationFrame % 10 == 0) {
                    playTickSound(2.0f);
                }
            }
            case SUCCESS_SLIDING_IN -> {
                if (animationFrame >= MAX_FRAMES) {
                    state = LaggState.SUCCESS_STATIC;
                    animationFrame = 0;
                    successStayTicks = 0;
                }
            }
            case SUCCESS_STATIC -> {
                successStayTicks += 2;
                if (successStayTicks >= 100) { // 5 seconds
                    state = LaggState.SLIDING_OUT;
                    animationFrame = 0;
                }
            }
            case SLIDING_OUT -> {
                if (animationFrame >= MAX_FRAMES) {
                    state = LaggState.IDLE;
                    animationFrame = 0;
                }
                if (animationFrame % 2 == 0)
                    playTickSound(1.2f);
            }
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

        String laggText = getLaggContent();

        // Slide In: Text enters from Right to Left
        if (state == LaggState.SLIDING_IN || state == LaggState.SUCCESS_SLIDING_IN) {
            float progress = (float) animationFrame / MAX_FRAMES;
            // Easing function for smooth slide
            float ease = 1 - (float) Math.pow(1 - progress, 3);

            // We want it to look like it's sliding in from the right.
            // Simplified "Typewriter" or "Reveal" effect for Action Bar constraints
            int revealLen = (int) (ChatUtils.getVisualLength(laggText) * ease);
            return ChatUtils.getVisualSlice(laggText, 0, revealLen);
        }

        // Slide Out: Text exits to Left (or fades)
        else if (state == LaggState.SLIDING_OUT) {
            float progress = (float) animationFrame / MAX_FRAMES;
            float ease = (float) Math.pow(progress, 3);

            int cutLen = (int) (ChatUtils.getVisualLength(laggText) * ease);
            return ChatUtils.getVisualSlice(laggText, cutLen, ChatUtils.getVisualLength(laggText) - cutLen);
        }

        return laggText;
    }

    private String getLaggContent() {
        if (state == LaggState.SLIDING_IN || state == LaggState.COUNTDOWN || state == LaggState.SLIDING_OUT) {
            // Show countdown content
            if (state == LaggState.SLIDING_OUT && successStayTicks > 0) {
                // Actually if we are in sliding out, we are likely showing the success message
                // sliding out
                // But wait, the state transition logic goes Success -> Static -> Slide Out
                // So Slide Out should show Success message fading out.
                // However, the block above shares logic. Let's separate "Countdown content" vs
                // "Success content".
            }
        }

        if (state == LaggState.SUCCESS_SLIDING_IN || state == LaggState.SUCCESS_STATIC
                || state == LaggState.SLIDING_OUT) {
            return "&#55FF55&l✔ CLEANUP COMPLETE &7(Removed " + cleanedCount + " items)";
        }

        // Default: Countdown Content
        return "&fClearing Items in &c" + autoRemovalCountdown + "s";
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

    @EventHandler
    public void onMobDie(EntityDeathEvent e) {
        if (!mergingEnabled)
            return;
        LivingEntity entity = e.getEntity();
        if (entity instanceof Player)
            return;
        if (entity.hasMetadata("NPC"))
            return;

        int stackSize = getStackSize(entity);
        // Always remove hologram from dying entity
        HologramUtil.removeHologram(entity);

        if (stackSize > 1) {
            int newSize = stackSize - 1;
            String baseNameRaw = getStackBaseName(entity);

            // Spawn replacement
            LivingEntity newEntity = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(),
                    entity.getType());

            // Copy Equipment
            if (entity.getEquipment() != null && newEntity.getEquipment() != null) {
                newEntity.getEquipment().setArmorContents(entity.getEquipment().getArmorContents());
                newEntity.getEquipment().setItemInMainHand(entity.getEquipment().getItemInMainHand());
                newEntity.getEquipment().setItemInOffHand(entity.getEquipment().getItemInOffHand());
            }

            // Copy Attributes if possible (Health, Damage)
            org.bukkit.attribute.AttributeInstance oldMax = entity
                    .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            org.bukkit.attribute.AttributeInstance newMax = newEntity
                    .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (oldMax != null && newMax != null) {
                newMax.setBaseValue(oldMax.getBaseValue());
            }
            newEntity.setHealth(newEntity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());

            // Set Name and hologram
            updateStackName(newEntity, baseNameRaw, newSize);
        }
    }

    private void performMobMerging() {
        for (World world : Bukkit.getWorlds()) {
            if (excludedWorlds.contains(world.getName()))
                continue;

            List<LivingEntity> mergeable = new ArrayList<>();
            for (Entity e : world.getEntities()) {
                if (e instanceof LivingEntity le && !(e instanceof Player) && !e.hasMetadata("NPC") && !le.isDead()) {
                    if (mergingBlacklist.contains(le.getType().name()))
                        continue;
                    mergeable.add(le);
                }
            }

            // Group by Signature (Type + Name)
            Map<String, List<LivingEntity>> groups = new HashMap<>();
            for (LivingEntity le : mergeable) {
                String signature = le.getType().name() + "::" + getStackBaseName(le);
                groups.computeIfAbsent(signature, k -> new ArrayList<>()).add(le);
            }

            for (List<LivingEntity> group : groups.values()) {
                if (group.size() < 2)
                    continue;

                for (int i = 0; i < group.size(); i++) {
                    LivingEntity base = group.get(i);
                    if (!base.isValid() || base.isDead())
                        continue;

                    int totalStack = getStackSize(base);
                    List<LivingEntity> toMerge = new ArrayList<>();

                    for (int j = i + 1; j < group.size(); j++) {
                        LivingEntity other = group.get(j);
                        if (other.isValid() && !other.isDead() &&
                                base.getLocation().distanceSquared(other.getLocation()) <= mergingRadiusSq) {

                            toMerge.add(other);
                            totalStack += getStackSize(other);
                        }
                    }

                    if (!toMerge.isEmpty()) {
                        String baseName = getStackBaseName(base);
                        for (LivingEntity other : toMerge) {
                            other.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, other.getLocation().add(0, 1, 0),
                                    5, 0.2, 0.2, 0.2, 0.05);
                            HologramUtil.removeHologram(other);
                            other.remove();
                        }
                        updateStackName(base, baseName, totalStack);
                    }
                }
            }
        }
    }

    private int getStackSize(LivingEntity le) {
        String name = le.getCustomName();
        if (name == null)
            return 1;

        String stripped = ChatUtils.decolorize(name);
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(" x(\\d+)$").matcher(stripped);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception ignored) {
        }
        return 1;
    }

    private String getStackBaseName(LivingEntity le) {
        String name = le.getCustomName();
        if (name == null) {
            String type = le.getType().name();
            return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        }

        // Remove the suffix from the RAW string to preserve colors
        return name.replaceAll("(?i)( §7)? x\\d+$", "");
    }

    private void updateStackName(LivingEntity le, String baseNameRaw, int size) {
        if (size <= 1) {
            // Single mob — remove hologram, restore original name
            HologramUtil.removeHologram(le);
            String typeName = le.getType().name();
            // If baseNameRaw looks like the default type name, clear custom name
            if (ChatUtils.decolorize(baseNameRaw).equalsIgnoreCase(typeName)) {
                le.setCustomName(null);
                le.setCustomNameVisible(false);
            } else {
                le.setCustomName(baseNameRaw);
                le.setCustomNameVisible(true);
            }
        } else {
            // Stacked mob — use hologram instead of custom name
            le.setCustomNameVisible(false);

            // Build pretty display name
            String cleanName = ChatUtils.decolorize(baseNameRaw);
            String holoText = "<gradient:#FFD700:#FFA500>" + cleanName + "</gradient> <gray>x" + size;
            HologramUtil.updateHologram(le, holoText);

            // Still store count in custom name for internal tracking (hidden)
            if (!baseNameRaw.contains("§") && !baseNameRaw.contains("&")) {
                baseNameRaw = "&e" + baseNameRaw;
            }
            le.setCustomName(ChatUtils.colorize(baseNameRaw + " §7x" + size));
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

    public LaggState getState() {
        return state;
    }

    public int getAutoRemovalCountdown() {
        return autoRemovalCountdown;
    }

    public int getCleanedCount() {
        return cleanedCount;
    }
}
