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

        this.tips = tipsConfig.getStringList("tips.messages");
        this.interval = tipsConfig.getInt("tips.interval", 300);
        this.soundEnabled = tipsConfig.getBoolean("tips.sound.enabled", true);
        this.soundName = tipsConfig.getString("tips.sound.name", "BLOCK_NOTE_BLOCK_PLING");

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
                broadcastTip();
            }
        };
        task.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    public void tick() {
        // No-op: handled by internal scheduler
    }

    public String getDisplay(String season) {
        if (tips == null || tips.isEmpty())
            return "";
        return tips.get(new Random().nextInt(tips.size()));
    }

    private void broadcastTip() {
        // Run async? No, sending messages usually main thread safe but better safe.
        // Actually scheduler sync is fine.
        task.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    private void broadcastTip() {
        if (tips.isEmpty())
            return;

        String tipRaw = tips.get(new Random().nextInt(tips.size()));
        String prefix = ConfigUtils.getString("prefix.tips", "&b&lTIPS &8» &f");
        String message = ChatUtils.colorize(prefix + tipRaw);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(message);
            if (soundEnabled) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
