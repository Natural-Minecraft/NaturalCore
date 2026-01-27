package id.naturalsmp.naturalcore.utility;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NaturalLaggManager implements Listener {

    private final NaturalCore plugin;

    // Animation States for ActionBar (Matching TipsManager style)
    public enum LaggState {
        IDLE,
        SLIDING_IN,
        COUNTDOWN,
        SUCCESS_SLIDING_IN,
        SUCCESS_STATIC,
        SLIDING_OUT
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
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTasks();
    }

    private void startTasks() {
        // 1. Tick Task (Animations)
        new BukkitRunnable() {
            @Override
            public void run() {
                tickAnimation();
            }
        }.runTaskTimer(plugin, 2L, 2L);

        // 2. Mob Merging & Optimizer Task (Every 30s)
        new BukkitRunnable() {
            @Override
            public void run() {
                performMobMerging();
                checkPerformanceOptimizer();
            }
        }.runTaskTimer(plugin, 600L, 600L);
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
            currentTick += 2; // Tick skip 2
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
            if (successStayTicks >= 60) { // Stay for 3s
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

            String partMain = ChatUtils.colorAwareSubstring(hud, cutLen, hud.length());
            String partLagg = ChatUtils.colorAwareSubstring(laggText, 0, revealLen);
            return partMain + "     " + partLagg;
        } else if (state == LaggState.SLIDING_OUT) {
            float progress = (float) animationFrame / MAX_FRAMES;
            int cutLen = (int) (laggText.length() * progress);
            int revealLen = (int) (hud.length() * progress);

            String partLagg = ChatUtils.colorAwareSubstring(laggText, cutLen, laggText.length());
            String partMain = ChatUtils.colorAwareSubstring(hud, 0, revealLen);
            return partLagg + "     " + partMain;
        } else {
            return laggText;
        }
    }

    private String getLaggContent() {
        if (state == LaggState.COUNTDOWN) {
            String arrows = "»»»»»";
            int offset = (animationFrame) % arrows.length();
            String scroll = arrows.substring(offset) + arrows.substring(0, offset);
            return "&#FF5555&l" + scroll + " &fCLEARING ITEMS IN &#FF5555&l" + countdownSeconds + "s &f" + scroll;
        } else if (state == LaggState.SUCCESS_STATIC || state == LaggState.SUCCESS_SLIDING_IN) {
            return "&#55FF55&l✔ CLEANUP COMPLETE &7(Removed " + cleanedCount + " items)";
        }
        return "";
    }

    private void performClean() {
        AtomicInteger count = new AtomicInteger(0);
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                    count.incrementAndGet();
                }
            }
        });
        this.cleanedCount = count.get();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    private void performMobMerging() {
        for (World world : Bukkit.getWorlds()) {
            List<Entity> entities = new ArrayList<>(world.getEntities());
            Map<String, List<LivingEntity>> groups = new HashMap<>();

            for (Entity e : entities) {
                if (e instanceof LivingEntity le && !(e instanceof Player) && !e.hasMetadata("NPC")) {
                    String type = le.getType().name();
                    groups.computeIfAbsent(type, k -> new ArrayList<>()).add(le);
                }
            }

            for (List<LivingEntity> group : groups.values()) {
                if (group.size() < 5)
                    continue;

                for (int i = 0; i < group.size(); i++) {
                    LivingEntity base = group.get(i);
                    if (!base.isValid())
                        continue;

                    List<LivingEntity> nearby = new ArrayList<>();
                    for (int j = i + 1; j < group.size(); j++) {
                        LivingEntity other = group.get(j);
                        if (other.isValid() && base.getLocation().distanceSquared(other.getLocation()) < 4) {
                            nearby.add(other);
                        }
                    }

                    if (nearby.size() >= 4) {
                        int total = nearby.size() + 1;
                        for (LivingEntity le : nearby)
                            le.remove();

                        // Update base mob
                        base.setCustomName(ChatUtils.colorize("&e&l" + base.getName() + " &7x" + total));
                        base.setCustomNameVisible(true);

                        // DecentHolograms Aesthetic (If enabled)
                        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                            String holoName = "merged_" + base.getUniqueId();
                            Hologram holo = DHAPI.getHologram(holoName);
                            if (holo == null) {
                                holo = DHAPI.createHologram(holoName, base.getLocation().add(0, 2.2, 0), true);
                                DHAPI.setHologramLines(holo, Arrays.asList(
                                        "&#FFEE00❂ &#FFEE00&lSTACKED MOB &#FFEE00❂",
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

    private void checkPerformanceOptimizer() {
        double tps = Bukkit.getTPS()[0];
        if (tps < 10.0) {
            plugin.getLogger().warning(
                    "TPS CRITICAL (" + String.format("%.1f", tps) + ")! Performing aggressive chunk unloader...");
            for (World world : Bukkit.getWorlds()) {
                for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                    if (chunk.getInhabitedTime() < 2400 && !isPlayerNearby(chunk)) {
                        chunk.unload(true);
                    }
                }
            }
        }
    }

    private boolean isPlayerNearby(org.bukkit.Chunk chunk) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().getChunk().equals(chunk))
                return true;
        }
        return false;
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
}
