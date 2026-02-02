package id.naturalsmp.naturalcore.utils;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;
import java.util.Random;

public class TipsManager {

    private final NaturalCore plugin;
    private List<String> tips;
    private FileConfiguration tipsConfig;
    private BukkitRunnable task;
    private int interval;
    private boolean soundEnabled;
    private String soundName;
    private String currentTip = null;

    // Animation States
    public enum TipState {
        IDLE, REVEALING, STAYING
    }

    private TipState state = TipState.IDLE;
    private int stayTicks = 0;

    public TipsManager(NaturalCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        // Load text/tips.yml
        File file = new File(plugin.getDataFolder(), "text/tips.yml");
        if (!file.exists()) {
            plugin.saveResource("text/tips.yml", false);
        }
        tipsConfig = YamlConfiguration.loadConfiguration(file);

        this.tips = tipsConfig.getStringList("tips");
        this.interval = tipsConfig.getInt("settings.interval", 300);
        this.soundEnabled = tipsConfig.getBoolean("settings.sound.enabled", true);
        this.soundName = tipsConfig.getString("settings.sound.name", "BLOCK_NOTE_BLOCK_HAT");

        // Reset state
        this.state = TipState.IDLE;
        this.currentTip = null;

        startBroadcast();
    }

    private void startBroadcast() {
        if (task != null) {
            task.cancel();
        }

        if (tips == null || tips.isEmpty()) {
            return;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateCurrentTip();
            }
        };
        task.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    public void tick() {
        if (state == TipState.REVEALING && currentTip != null) {
            state = TipState.STAYING;
            stayTicks = 0;
            if (soundEnabled)
                playTickSound();
        } else if (state == TipState.STAYING) {
            stayTicks += 2; // HUD ticks at 2L
            if (stayTicks >= 100) { // 5 seconds
                state = TipState.IDLE;
                currentTip = null;
            }
        }
    }

    private void updateCurrentTip() {
        if (tips != null && !tips.isEmpty()) {
            this.currentTip = tips.get(new Random().nextInt(tips.size()));
            this.state = TipState.REVEALING;
        } else {
            this.currentTip = null;
            this.state = TipState.IDLE;
        }
    }

    private void playTickSound() {
        try {
            Sound sound = Sound.valueOf(soundName);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), sound, 0.4f, 2.0f);
            }
        } catch (Exception ignored) {
        }
    }

    public String getDisplay(String season) {
        if (currentTip == null || state == TipState.IDLE)
            return null;

        return ChatUtils.colorize("TIPS: &f" + currentTip);
    }
}
