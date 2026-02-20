package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * HUD Component for NaturalDungeon.
 * Uses reflection to avoid hard dependency on NaturalDungeon.
 */
public class DungeonHUDComponent extends AbstractHUDComponent {

    private boolean initialized = false;
    private boolean enabled = false;

    private Object dungeonPlugin;
    private Method getDungeonManagerMethod;

    // DungeonManager methods
    private Method getActiveInstanceMethod;

    // DungeonInstance methods
    private Method getDurationMethod;
    private Method getPerformanceRankMethod;
    private Method getLivesMethod;
    private Method getTotalWavesInStageMethod;
    private Method getTotalStagesMethod;
    private Method getObjectiveTextMethod;
    private Method getRemainingMobsMethod;
    private Method getCurrentStageMethod;
    private Method getCurrentWaveMethod;

    public DungeonHUDComponent(NaturalCore plugin) {
        // Priority is inside between CRITICAL and LAGG, we want HIGH priority (80)
        // Wait, HUDPriority doesn't have 80. Let's use HUDPriority.HIGH (75)
        super(plugin, "dungeon", HUDPriority.HIGH);
    }

    private void tryInit() {
        if (initialized)
            return;
        initialized = true;

        Plugin nd = Bukkit.getPluginManager().getPlugin("NaturalDungeon");
        if (nd == null || !nd.isEnabled())
            return;

        try {
            this.dungeonPlugin = nd;
            this.getDungeonManagerMethod = nd.getClass().getMethod("getDungeonManager");
            Object dungeonManager = getDungeonManagerMethod.invoke(nd);

            this.getActiveInstanceMethod = dungeonManager.getClass().getMethod("getActiveInstance", Player.class);

            Class<?> instanceClass = Class.forName("id.naturalsmp.naturaldungeon.dungeon.DungeonInstance");
            this.getDurationMethod = instanceClass.getMethod("getDuration");
            this.getPerformanceRankMethod = instanceClass.getMethod("getPerformanceRank");
            this.getLivesMethod = instanceClass.getMethod("getLives", java.util.UUID.class);
            this.getTotalWavesInStageMethod = instanceClass.getMethod("getTotalWavesInStage");
            this.getTotalStagesMethod = instanceClass.getMethod("getTotalStages");
            this.getObjectiveTextMethod = instanceClass.getMethod("getObjectiveText");
            this.getRemainingMobsMethod = instanceClass.getMethod("getRemainingMobs");
            this.getCurrentStageMethod = instanceClass.getMethod("getCurrentStage");
            this.getCurrentWaveMethod = instanceClass.getMethod("getCurrentWave");

            this.enabled = true;
            plugin.getLogger().info("NaturalDungeon HUD hooked successfully!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into NaturalDungeon for HUD: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldDisplay(Player player) {
        if (!initialized)
            tryInit();
        if (!enabled)
            return false;

        try {
            Object dungeonManager = getDungeonManagerMethod.invoke(dungeonPlugin);
            Object activeInstance = getActiveInstanceMethod.invoke(dungeonManager, player);
            return activeInstance != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getContent(Player player, int tick) {
        if (!enabled)
            return null;

        try {
            Object dungeonManager = getDungeonManagerMethod.invoke(dungeonPlugin);
            Object activeInstance = getActiveInstanceMethod.invoke(dungeonManager, player);
            if (activeInstance == null)
                return null;

            long durationMs = (long) getDurationMethod.invoke(activeInstance);
            String time = formatTime(durationMs / 1000);
            String rank = (String) getPerformanceRankMethod.invoke(activeInstance);
            int lives = (int) getLivesMethod.invoke(activeInstance, player.getUniqueId());

            int currentStage = (int) getCurrentStageMethod.invoke(activeInstance);
            int totalStages = (int) getTotalStagesMethod.invoke(activeInstance);
            int currentWave = (int) getCurrentWaveMethod.invoke(activeInstance);
            int totalWaves = (int) getTotalWavesInStageMethod.invoke(activeInstance);

            String objective = (String) getObjectiveTextMethod.invoke(activeInstance);

            String heartColor = lives > 1 ? "&a" : "&c";

            // Format: ⚔ Stage 1/3 (Wave 2/5) | 🎯 Kill All (4 left) | ❤ 3 | ⏱ 02:30 | ⭐ S
            return "&7⚔ Stage &f" + currentStage + "/" + totalStages + " &8(&7Wave &f" + currentWave + "/" + totalWaves
                    + "&8) &8| " +
                    "&e🎯 " + objective + " &8| " +
                    "&7Lives: " + heartColor + lives + " ❤ &8| " +
                    "&7Waktu: &f" + time + " &8| " +
                    "&7Rank: &#FFBB00" + rank;

        } catch (Exception e) {
            return "&c[Dungeon HUD Error]";
        }
    }

    @Override
    public int getTransitionDuration() {
        return 10; // Smooth slide-in
    }

    private String formatTime(long seconds) {
        long m = seconds / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
