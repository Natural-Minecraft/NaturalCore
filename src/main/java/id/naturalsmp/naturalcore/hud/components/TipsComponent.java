package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
import id.naturalsmp.naturalcore.utility.NaturalLaggManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Displays periodic tips to players.
 * Features marquee scrolling for long tips with typing sound effects.
 * All settings are configurable via text/tips.yml.
 */
public class TipsComponent extends AbstractHUDComponent {

    private List<String> tips;

    // --- Configurable Settings ---
    private int interval = 300;
    private int stayDuration = 200; // ticks

    // Appear sound
    private boolean soundEnabled = true;
    private String soundName = "BLOCK_NOTE_BLOCK_HAT";
    private float soundVolume = 0.4f;
    private float soundPitch = 2.0f;

    // Marquee
    private int maxWidth = 50;
    private int scrollSpeed = 4; // ticks per char
    private int pauseTicks = 80; // ticks pause at each end

    // Typing sound
    private boolean typingSoundEnabled = true;
    private Sound typingSound = Sound.BLOCK_NOTE_BLOCK_HAT;
    private float typingSoundVolume = 0.15f;
    private float typingSoundPitch = 1.8f;

    // --- Runtime State ---
    private String currentTip = null;
    private int stayTicks = 0;
    private BukkitTask scheduleTask;
    private int lastScrollOffset = -1;

    public TipsComponent(NaturalCore plugin) {
        super(plugin, "tips", HUDPriority.MEDIUM);
        reload();
        startSchedule();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "text/tips.yml");
        if (!file.exists()) {
            plugin.saveResource("text/tips.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Core settings
        this.tips = config.getStringList("tips");
        this.interval = config.getInt("settings.interval", 300);
        this.stayDuration = config.getInt("settings.duration", 10) * 20; // seconds -> ticks

        // Appear sound
        this.soundEnabled = config.getBoolean("settings.sound.enabled", true);
        this.soundName = config.getString("settings.sound.name", "BLOCK_NOTE_BLOCK_HAT");
        this.soundVolume = (float) config.getDouble("settings.sound.volume", 0.4);
        this.soundPitch = (float) config.getDouble("settings.sound.pitch", 2.0);

        // Marquee settings
        this.maxWidth = config.getInt("settings.marquee.max-width", 50);
        this.scrollSpeed = config.getInt("settings.marquee.scroll-speed", 4);
        this.pauseTicks = (int) (config.getDouble("settings.marquee.pause-duration", 4.0) * 20); // sec -> ticks

        // Typing sound
        this.typingSoundEnabled = config.getBoolean("settings.marquee.typing-sound.enabled", true);
        this.typingSoundVolume = (float) config.getDouble("settings.marquee.typing-sound.volume", 0.15);
        this.typingSoundPitch = (float) config.getDouble("settings.marquee.typing-sound.pitch", 1.8);
        try {
            this.typingSound = Sound.valueOf(
                    config.getString("settings.marquee.typing-sound.name", "BLOCK_NOTE_BLOCK_HAT"));
        } catch (Exception e) {
            this.typingSound = Sound.BLOCK_NOTE_BLOCK_HAT;
        }

        this.currentTip = null;
        startSchedule();
    }

    private void startSchedule() {
        if (scheduleTask != null)
            scheduleTask.cancel();
        if (tips == null || tips.isEmpty())
            return;

        scheduleTask = new BukkitRunnable() {
            @Override
            public void run() {
                showRandomTip();
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    private void showRandomTip() {
        if (tips != null && !tips.isEmpty()) {
            currentTip = tips.get(new Random().nextInt(tips.size()));
            stayTicks = 0;
            lastScrollOffset = -1;

            if (soundEnabled) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), sound, soundVolume, soundPitch);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void tick(int tick) {
        if (currentTip != null) {
            stayTicks += 2; // Called every 2 ticks
            if (stayTicks >= stayDuration) {
                currentTip = null;
                lastScrollOffset = -1;
            }
        }
    }

    @Override
    public boolean shouldDisplay(Player player) {
        // Don't show tips if LaggManager is active (cleanup in progress)
        if (plugin.getLaggManager() != null && plugin.getLaggManager()
                .getState() != NaturalLaggManager.LaggState.IDLE) {
            return false;
        }
        return currentTip != null;
    }

    @Override
    public String getContent(Player player, int tick) {
        if (currentTip == null)
            return null;

        String prefix = "<gradient:#FFD700:#FFA500>💡 TIPS</gradient> &8» &f";
        String fullKey = prefix + currentTip;

        // Check visual length
        int len = ChatUtils.getVisualLength(fullKey);

        if (len <= maxWidth) {
            return fullKey;
        }

        // Marquee Logic for long tips
        int scrollRange = len - maxWidth;
        int scrollTicks = scrollRange * scrollSpeed;
        int cycleTicks = pauseTicks + scrollTicks + pauseTicks; // Pause -> Scroll -> Pause
        int currentCycle = tick % cycleTicks;

        int offset = 0;
        boolean isScrolling = false;
        if (currentCycle < pauseTicks) {
            offset = 0; // Pause start (reading time)
        } else if (currentCycle < pauseTicks + scrollTicks) {
            offset = (currentCycle - pauseTicks) / scrollSpeed; // Scroll
            isScrolling = true;
        } else {
            offset = scrollRange; // Pause end (reading time)
        }

        // Typing sound effect during scroll (plays when offset changes)
        if (typingSoundEnabled && isScrolling && offset != lastScrollOffset) {
            lastScrollOffset = offset;
            try {
                player.playSound(player.getLocation(), typingSound, typingSoundVolume, typingSoundPitch);
            } catch (Exception ignored) {
            }
        } else if (!isScrolling) {
            lastScrollOffset = -1;
        }

        return ChatUtils.getVisualSlice(fullKey, offset, maxWidth);
    }

    @Override
    public int getTransitionDuration() {
        return 40; // Match LaggComponent scrolling speed
    }
}
