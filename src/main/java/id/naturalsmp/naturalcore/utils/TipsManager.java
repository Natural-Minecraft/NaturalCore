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
    private int animationFrame = 0;
    private int stayTicks = 0;
    private final int MAX_FRAMES = 25; // Reveal speed
    private int durationTicks = 80; // 4 seconds default stay

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
        this.durationTicks = tipsConfig.getInt("settings.duration", 4) * 20;

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
            animationFrame++;
            if (animationFrame >= MAX_FRAMES) {
                state = TipState.STAYING;
                stayTicks = 0;
                animationFrame = 0;
            }

            // Sound feedback every 2 animation ticks
            if (animationFrame % 2 == 0 && soundEnabled) {
                playTickSound();
            }
        } else if (state == TipState.STAYING) {
            stayTicks += 2; // HUD ticks at 2L
            if (stayTicks >= durationTicks) {
                state = TipState.IDLE;
                currentTip = null;
            }
        }
    }

    private void updateCurrentTip() {
        if (tips != null && !tips.isEmpty()) {
            this.currentTip = tips.get(new Random().nextInt(tips.size()));
            this.state = TipState.REVEALING;
            this.animationFrame = 0;
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

        String coloredTip = ChatUtils.colorize(currentTip);

        if (state == TipState.REVEALING) {
            float progress = (float) animationFrame / MAX_FRAMES;
            // Easing for smooth reveal
            float ease = 1 - (float) Math.pow(1 - progress, 3);
            int revealLen = (int) (ChatUtils.stripColor(coloredTip).length() * ease);

            return ChatUtils.colorAwareSubstring(coloredTip, 0, revealLen);
        }

        return coloredTip;
    }
}
