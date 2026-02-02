package id.naturalsmp.naturalcore.hud.components;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.hud.HUDPriority;
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
 * Shows every 5 minutes for 5 seconds.
 */
public class TipsComponent extends AbstractHUDComponent {

    private List<String> tips;
    private int interval = 300; // seconds
    private boolean soundEnabled = true;
    private String soundName = "BLOCK_NOTE_BLOCK_HAT";

    private String currentTip = null;
    private int stayTicks = 0;
    private static final int STAY_DURATION = 100; // 5 seconds

    private BukkitTask scheduleTask;

    public TipsComponent(NaturalCore plugin) {
        super(plugin, "tips", HUDPriority.LOW);
        reload();
        startSchedule();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "text/tips.yml");
        if (!file.exists()) {
            plugin.saveResource("text/tips.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        this.tips = config.getStringList("tips");
        this.interval = config.getInt("settings.interval", 300);
        this.soundEnabled = config.getBoolean("settings.sound.enabled", true);
        this.soundName = config.getString("settings.sound.name", "BLOCK_NOTE_BLOCK_HAT");

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

            if (soundEnabled) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), sound, 0.4f, 2.0f);
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
            if (stayTicks >= STAY_DURATION) {
                currentTip = null;
            }
        }
    }

    @Override
    public boolean shouldDisplay(Player player) {
        return currentTip != null;
    }

    @Override
    public String getContent(Player player, int tick) {
        if (currentTip == null)
            return null;
        return ChatUtils.colorize("&e💡 TIPS: &f" + currentTip);
    }
}
