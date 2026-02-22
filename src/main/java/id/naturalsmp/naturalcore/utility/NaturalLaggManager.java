package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalLaggManager implements Listener {

    private final NaturalCore plugin;

    // Configuration Fields
    private boolean enabled = true;
    private boolean autoRemovalEnabled = true;
    private int autoRemovalInterval = 300;
    private List<Integer> warningTimes = Arrays.asList(60, 30, 10, 5, 3, 2, 1);
    private boolean mergingEnabled = true;
    private int mergingThreshold = 5;
    private double mergingRadiusSq = 16.0; // 4 block radius (4*4)
    private int maxStackSize = 256;
    private int mergeIntervalTicks = 100; // 5 seconds
    private Set<String> mergingBlacklist = new HashSet<>();
    private Set<String> itemWhitelist = new HashSet<>();
    private boolean removeDeathDrops = false;
    private boolean chunkLimiterEnabled = true;
    private int maxEntitiesPerChunk = 50;
    private int chunkLimiterInterval = 60;
    private double tpsThreshold = 10.0;
    private int optimizerCheckInterval = 30;
    private Set<String> excludedWorlds = new HashSet<>();

    private final NamespacedKey STACK_SIZE_KEY;
    private final NamespacedKey STACK_BASE_NAME_KEY;

    // State
    private BukkitTask autoRemovalTask;
    private BukkitTask mergeTask;
    private BukkitTask chunkTask;
    private BukkitTask performanceTask;
    private int autoRemovalCountdown;
    private final Set<UUID> recentDeathDrops = ConcurrentHashMap.newKeySet();

    // Track entities currently being processed to prevent recursion
    private final Set<UUID> processingDeath = ConcurrentHashMap.newKeySet();

    // Animation States
    public enum LaggState {
        IDLE, SLIDING_IN, COUNTDOWN, SUCCESS_SLIDING_IN, SUCCESS_STATIC, SLIDING_OUT
    }

    private LaggState state = LaggState.IDLE;
    private int animationFrame = 0;
    private final int MAX_FRAMES = 20;
    private int cleanedCount = 0;
    private int successStayTicks = 0;

    public NaturalLaggManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.STACK_SIZE_KEY = new NamespacedKey(plugin, "stack_size");
        this.STACK_BASE_NAME_KEY = new NamespacedKey(plugin, "stack_base_name");
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (enabled) {
            startTasks();
            // Purge orphan holograms on startup
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                int purged = HologramUtil.purgeAllHolograms();
                if (purged > 0) {
                    plugin.getLogger().info("[NaturalLagg] Cleaned " + purged + " orphan holograms.");
                }
            }, 100L);
        }
    }

    public void stop() {
        if (autoRemovalTask != null) {
            autoRemovalTask.cancel();
            autoRemovalTask = null;
        }
        if (mergeTask != null) {
            mergeTask.cancel();
            mergeTask = null;
        }
        if (chunkTask != null) {
            chunkTask.cancel();
            chunkTask = null;
        }
        if (performanceTask != null) {
            performanceTask.cancel();
            performanceTask = null;
        }
        recentDeathDrops.clear();
        processingDeath.clear();
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
        double radius = plugin.getConfig().getDouble("lagg.merging.radius", 4.0);
        this.mergingRadiusSq = radius * radius;
        this.maxStackSize = plugin.getConfig().getInt("lagg.merging.max-stack-size", 256);
        this.mergeIntervalTicks = plugin.getConfig().getInt("lagg.merging.merge-interval", 5) * 20;
        this.mergingBlacklist = new HashSet<>(plugin.getConfig().getStringList("lagg.merging.blacklist"));

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
        // Mob Merge Task
        if (mergingEnabled) {
            this.mergeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    performMobMerging();
                }
            }.runTaskTimer(plugin, 200L, mergeIntervalTicks);
        }

        // Chunk Limiter
        if (chunkLimiterEnabled) {
            this.chunkTask = new BukkitRunnable() {
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
        this.performanceTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkPerformanceOptimizer();
            }
        }.runTaskTimer(plugin, 200L, optimizerCheckInterval * 20L);
    }

    // ==================== AUTO REMOVAL LOGIC ====================

    private void startAutoRemovalCycle() {
        autoRemovalCountdown = autoRemovalInterval;
        autoRemovalTask = new BukkitRunnable() {
            @Override
            public void run() {
                autoRemovalCountdown--;

                if (autoRemovalCountdown == 15) {
                    broadcastWarning(15);
                    startAnimation();
                } else if (warningTimes.contains(autoRemovalCountdown) && autoRemovalCountdown != 15) {
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
                    "&#FFAA00&l⚠ §7Pembersihan item dalam &e%time% §7menit.");
            msg = msg.replace("%time%", String.valueOf(seconds / 60));
        } else {
            msg = ConfigUtils.getString("messages.system.lagg.warning-second",
                    "&#FFAA00&l⚠ §7Pembersihan item dalam &e%time% §7detik.");
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
        if (seconds <= 15) {
            startAnimation();
        }
        broadcastWarning(seconds);
    }

    // ==================== ANIMATION ====================

    public void startAnimation() {
        if (state != LaggState.IDLE)
            return;
        this.state = LaggState.SLIDING_IN;
        this.animationFrame = 0;
    }

    public void triggerSuccessAnimation() {
        this.state = LaggState.SUCCESS_SLIDING_IN;
        this.animationFrame = 0;
    }

    public void tickAnimation() {
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
                if (autoRemovalCountdown <= 5 && animationFrame % 10 == 0) {
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
                if (successStayTicks >= 100) {
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
        if (state == LaggState.SLIDING_IN || state == LaggState.SUCCESS_SLIDING_IN) {
            float progress = (float) animationFrame / MAX_FRAMES;
            float ease = 1 - (float) Math.pow(1 - progress, 3);
            int revealLen = (int) (ChatUtils.getVisualLength(laggText) * ease);
            return ChatUtils.getVisualSlice(laggText, 0, revealLen);
        } else if (state == LaggState.SLIDING_OUT) {
            float progress = (float) animationFrame / MAX_FRAMES;
            float ease = (float) Math.pow(progress, 3);
            int cutLen = (int) (ChatUtils.getVisualLength(laggText) * ease);
            return ChatUtils.getVisualSlice(laggText, cutLen, ChatUtils.getVisualLength(laggText) - cutLen);
        }
        return laggText;
    }

    private String getLaggContent() {
        if (state == LaggState.SUCCESS_SLIDING_IN || state == LaggState.SUCCESS_STATIC
                || state == LaggState.SLIDING_OUT) {
            return "&#55FF55&l✔ CLEANUP COMPLETE §7(" + cleanedCount + " items)";
        }
        return "&fClearing Items in &c" + autoRemovalCountdown + "s";
    }

    private void performClean() {
        AtomicInteger count = new AtomicInteger(0);
        // --- LOGGING ---
        StringBuilder details = new StringBuilder();
        // ---------------
        Bukkit.getWorlds().forEach(world -> {
            if (excludedWorlds.contains(world.getName()))
                return;
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    ItemStack stack = item.getItemStack();
                    if (itemWhitelist.contains(stack.getType().name()))
                        continue;
                    if (!removeDeathDrops && recentDeathDrops.contains(entity.getUniqueId()))
                        continue;

                    // --- LOGGING ---
                    if (details.length() > 0)
                        details.append(", ");
                    details.append(stack.getType().name().toLowerCase())
                            .append(" (").append(stack.getAmount()).append("x at ")
                            .append(item.getLocation().getBlockX()).append(", ")
                            .append(item.getLocation().getBlockY()).append(", ")
                            .append(item.getLocation().getBlockZ()).append(")");
                    // ---------------

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

        // --- LOGGING ---
        if (this.cleanedCount > 0) {
            id.naturalsmp.naturalcore.utility.NaturalLogger logger = id.naturalsmp.naturalcore.utility.NaturalLogger
                    .getInstance();
            logger.logClearLaggItemDetail(details.toString());
            logger.logClearLagg(this.cleanedCount);
        }
        // ---------------
    }

    // ==================== STACK MOB: DAMAGE HANDLER ====================

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!mergingEnabled)
            return;
        if (!(e.getEntity() instanceof LivingEntity entity))
            return;
        if (entity instanceof Player || entity.hasMetadata("NPC"))
            return;

        int stackSize = getStackSize(entity);
        if (stackSize <= 1) {
            // Mob biasa, biarkan mati natural. Cleanup hologram jika ada.
            if (e.getFinalDamage() >= entity.getHealth()) {
                Bukkit.getScheduler().runTask(plugin, () -> HologramUtil.removeHologram(entity));
            }
            return;
        }

        // Non-fatal damage — biarkan terkena damage biasa
        if (e.getFinalDamage() < entity.getHealth())
            return;

        // === Fatal damage pada stacked mob ===
        // Cancel event ONLY — do NOT modify entity state during event dispatch.
        // This prevents conflicts with AuraMobs and other plugins.
        e.setCancelled(true);

        // Extract killer NOW (event data won't be available next tick)
        Player killer = getKiller(e);
        String baseName = getStackBaseName(entity);
        int newSize = stackSize - 1;

        // === Delay all state modifications to next tick ===
        // Other plugins (AuraMobs, etc.) finish processing the event first.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!entity.isValid())
                return;

            // Reset HP
            if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            }

            // Update stack
            updateStackDisplay(entity, baseName, newSize);

            // Hurt animation & sound
            entity.playEffect(org.bukkit.EntityEffect.HURT);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1f);

            // Loot & XP
            if (entity instanceof Mob mob) {
                dropLoot(mob, killer);
                dropXp(mob, killer);
            }
        });
    }

    // ==================== STACK MOB: DEATH HANDLER ====================

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent e) {
        if (!mergingEnabled)
            return;
        if (e instanceof PlayerDeathEvent)
            return;

        LivingEntity entity = e.getEntity();
        if (entity.hasMetadata("NPC"))
            return;

        // Prevent recursion
        if (processingDeath.contains(entity.getUniqueId()))
            return;
        processingDeath.add(entity.getUniqueId());

        try {
            int stackSize = getStackSize(entity);
            if (stackSize <= 1) {
                // Mob biasa — cleanup hologram
                HologramUtil.removeHologram(entity);
                return;
            }

            // Stacked mob mati (dari void, /kill, suffocation, etc.)
            // Jangan multiply drops — cukup drop 1x (entity sudah mati, Bukkit handle
            // drops)
            // Tapi update PDC supaya hologram di-clean
            HologramUtil.removeHologram(entity);

            // Clear PDC
            entity.getPersistentDataContainer().remove(STACK_SIZE_KEY);
            entity.getPersistentDataContainer().remove(STACK_BASE_NAME_KEY);

        } finally {
            processingDeath.remove(entity.getUniqueId());
        }
    }

    // ==================== STACK MOB: EXPLOSION HANDLER ====================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent e) {
        if (!mergingEnabled)
            return;
        if (!(e.getEntity() instanceof LivingEntity entity))
            return;

        int stackSize = getStackSize(entity);
        if (stackSize <= 1) {
            HologramUtil.removeHologram(entity); // Clean if single
            return;
        }

        // It's a stacked Creeper/Ghast exploding.
        // We let the explosion happen but we SPAWN a new stack minus 1
        String baseName = getStackBaseName(entity);
        int newSize = stackSize - 1;

        // Clean up the hologram from the detonating entity
        HologramUtil.removeHologram(entity);

        // Spawn replacement entity
        Bukkit.getScheduler().runTask(plugin, () -> {
            LivingEntity newEntity = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(),
                    entity.getType());

            // Set stats and stack data
            if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                double maxHp = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                if (newEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    newEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHp);
                    newEntity.setHealth(maxHp);
                }
            }
            updateStackDisplay(newEntity, baseName, newSize);

            // Temporary stun / target clear to prevent instant chain explosion
            if (newEntity instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAware(false);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (mob.isValid())
                        mob.setAware(true);
                }, 40L); // 2 second stun
            }
        });
    }

    // ==================== STACK MOB: GENERIC REMOVE HANDLER ====================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(org.bukkit.event.entity.EntityRemoveEvent e) {
        if (!mergingEnabled)
            return;
        if (e.getEntity() instanceof LivingEntity entity) {
            // Ensure holograms are wiped if removed abruptly (e.g. /kill, chunk unload,
            // plugin force remove)
            HologramUtil.removeHologram(entity);
        }
    }

    // ==================== LOOT & XP HELPERS ====================

    /**
     * Drop loot untuk 1 mob dari stack menggunakan proper LootContext.
     */
    private void dropLoot(Mob mob, Player killer) {
        LootTable table = mob.getLootTable();
        if (table == null)
            return;

        try {
            LootContext.Builder builder = new LootContext.Builder(mob.getLocation())
                    .lootedEntity(mob);

            if (killer != null) {
                builder.killer(killer);
                // Looting enchant level
                ItemStack hand = killer.getInventory().getItemInMainHand();
                int lootingLevel = hand.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.LOOTING);
                builder.luck(lootingLevel);
            }

            Collection<ItemStack> drops = table.populateLoot(new java.util.Random(), builder.build());
            for (ItemStack drop : drops) {
                mob.getWorld().dropItemNaturally(mob.getLocation(), drop);
            }
        } catch (Exception ex) {
            // Fallback: silent fail, mob drops 1x naturally handled by damage
        }
    }

    /**
     * Drop XP orb untuk 1 mob dari stack.
     */
    private void dropXp(Mob mob, Player killer) {
        if (killer == null)
            return;

        // Base XP berdasarkan mob type (simplified — Bukkit default)
        int baseXp = mob.getType().name().contains("ENDER") ? 5 : 3;
        mob.getWorld().spawn(mob.getLocation(), ExperienceOrb.class, orb -> {
            orb.setExperience(baseXp);
        });
    }

    /**
     * Extract killer dari damage event chain.
     */
    private Player getKiller(EntityDamageEvent e) {
        if (e instanceof EntityDamageByEntityEvent edbe) {
            if (edbe.getDamager() instanceof Player p)
                return p;
            // Projectile → shooter
            if (edbe.getDamager() instanceof org.bukkit.entity.Projectile proj) {
                if (proj.getShooter() instanceof Player p)
                    return p;
            }
        }
        return null;
    }

    // ==================== MOB MERGING LOGIC ====================

    private void performMobMerging() {
        for (World world : Bukkit.getWorlds()) {
            if (excludedWorlds.contains(world.getName()))
                continue;

            for (Chunk chunk : world.getLoadedChunks()) {
                mergeEntitiesInChunk(chunk);
            }
        }
    }

    private void mergeEntitiesInChunk(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        if (entities.length < 2)
            return;

        Map<String, List<LivingEntity>> groups = new HashMap<>();

        for (Entity e : entities) {
            if (e instanceof LivingEntity le && !(e instanceof Player) && !(e instanceof org.bukkit.entity.ArmorStand)
                    && !e.hasMetadata("NPC") && !le.isDead()) {
                if (mergingBlacklist.contains(le.getType().name()))
                    continue;

                String signature = le.getType().name() + "::" + getStackBaseName(le);
                groups.computeIfAbsent(signature, k -> new ArrayList<>()).add(le);
            }
        }

        for (List<LivingEntity> group : groups.values()) {
            if (group.size() < 2)
                continue;

            LivingEntity base = group.get(0);
            if (!base.isValid())
                continue;

            int totalStack = getStackSize(base);
            boolean changed = false;

            for (int i = 1; i < group.size(); i++) {
                LivingEntity other = group.get(i);
                if (!other.isValid())
                    continue;

                // Distance check (squared)
                if (base.getLocation().distanceSquared(other.getLocation()) > mergingRadiusSq)
                    continue;

                int otherStack = getStackSize(other);

                // Max stack size check
                if (totalStack + otherStack > maxStackSize)
                    continue;

                totalStack += otherStack;

                // Merge FX
                other.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, other.getLocation().add(0, 0.5, 0), 3, 0.1,
                        0.1, 0.1, 0.05);
                HologramUtil.removeHologram(other);
                other.remove();
                changed = true;
            }

            if (changed) {
                updateStackDisplay(base, getStackBaseName(base), totalStack);
                // Despawn protection for stacked mobs
                base.setRemoveWhenFarAway(false);
            }
        }
    }

    // ==================== STACK UTILS ====================

    private int getStackSize(LivingEntity le) {
        PersistentDataContainer pdc = le.getPersistentDataContainer();
        if (pdc.has(STACK_SIZE_KEY, PersistentDataType.INTEGER)) {
            return pdc.get(STACK_SIZE_KEY, PersistentDataType.INTEGER);
        }
        // Legacy fallback: parse from custom name
        String name = le.getCustomName();
        if (name != null) {
            String stripped = ChatUtils.decolorize(name);
            Matcher m = Pattern.compile("x(\\d+)$").matcher(stripped);
            if (m.find())
                return Integer.parseInt(m.group(1));
        }
        return 1;
    }

    private String getStackBaseName(LivingEntity le) {
        PersistentDataContainer pdc = le.getPersistentDataContainer();
        if (pdc.has(STACK_BASE_NAME_KEY, PersistentDataType.STRING)) {
            return pdc.get(STACK_BASE_NAME_KEY, PersistentDataType.STRING);
        }
        // Legacy fallback
        String name = le.getCustomName();
        if (name == null) {
            // Capitalize type name: ZOMBIE → Zombie
            String type = le.getType().name().replace("_", " ");
            StringBuilder sb = new StringBuilder();
            for (String word : type.split(" ")) {
                if (sb.length() > 0)
                    sb.append(" ");
                sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
            }
            return sb.toString();
        }
        // Strip stack suffix from legacy name
        String stripped = ChatUtils.decolorize(name);
        return stripped.replaceAll("\\s*x\\d+$", "").trim();
    }

    /**
     * Update stack data dan hologram display.
     * Format: <gradient:#6CCAFE:#55FF55><bold>Cow</bold></gradient> <dark_gray>•
     * <white>x<bold>50</bold>
     */
    private void updateStackDisplay(LivingEntity le, String baseName, int size) {
        le.getPersistentDataContainer().set(STACK_SIZE_KEY, PersistentDataType.INTEGER, size);
        le.getPersistentDataContainer().set(STACK_BASE_NAME_KEY, PersistentDataType.STRING, baseName);

        if (size <= 1) {
            // Unstacked — remove hologram, restore name
            HologramUtil.removeHologram(le);
            String plainName = ChatUtils.decolorize(baseName);
            String typeName = le.getType().name().replace("_", " ");

            // Jika nama = type name default, hide custom name
            if (plainName.equalsIgnoreCase(typeName) ||
                    plainName.equalsIgnoreCase(typeName.replace(" ", ""))) {
                le.setCustomName(null);
                le.setCustomNameVisible(false);
            } else {
                le.setCustomName(baseName);
                le.setCustomNameVisible(true);
            }

            // Re-enable natural despawn
            le.setRemoveWhenFarAway(true);
            le.getPersistentDataContainer().remove(STACK_SIZE_KEY);
            le.getPersistentDataContainer().remove(STACK_BASE_NAME_KEY);
        } else {
            // Stacked — premium minimalist hologram
            le.setCustomNameVisible(false);
            le.setCustomName(null);

            String cleanName = ChatUtils.decolorize(baseName);

            // Premium hologram: gradient name • bold count
            String holoText = "<gradient:#6CCAFE:#55FF55><bold>" + cleanName
                    + "</bold></gradient> <dark_gray>• <white>x<bold>" + size + "</bold>";

            HologramUtil.updateHologram(le, holoText);

            // Despawn protection
            le.setRemoveWhenFarAway(false);
        }
    }

    // ==================== CHUNK LIMITER ====================

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

                        // Skip stacked mobs — removing them would lose many entities
                        if (e instanceof LivingEntity le) {
                            if (le instanceof Player || le.hasMetadata("NPC"))
                                continue;
                            if (getStackSize(le) > 1)
                                continue; // SKIP stacked — too valuable to delete
                        }

                        if (e instanceof Item) {
                            e.remove();
                            removed++;
                        } else if (e instanceof LivingEntity le && !(e instanceof Player)
                                && !(e instanceof org.bukkit.entity.ArmorStand) && !e.hasMetadata("NPC")) {
                            HologramUtil.removeHologram(le);
                            le.remove();
                            removed++;
                        }
                    }
                }
            }
        }
    }

    // ==================== PERFORMANCE OPTIMIZER ====================

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

    // ==================== PLAYER DEATH DROPS PROTECTION ====================

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

    // ==================== GETTERS ====================

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
