package id.naturalsmp.naturalcore.announcement;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BroadcastManager {

    private final NaturalCore plugin;
    private final File configFile;
    private FileConfiguration config;
    private final List<String> messages = new ArrayList<>();
    private int currentIndex = 0;

    public BroadcastManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "announcements.yml");
        loadConfig();
        startTask();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("announcements.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        messages.clear();
        messages.addAll(config.getStringList("messages"));
        if (config.getBoolean("settings.randomize", true)) {
            Collections.shuffle(messages);
        }
    }

    private void startTask() {
        int interval = config.getInt("settings.interval", 300); // Default 5 mins
        new BukkitRunnable() {
            @Override
            public void run() {
                if (messages.isEmpty())
                    return;

                String message = messages.get(currentIndex);
                broadcast(message);

                currentIndex++;
                if (currentIndex >= messages.size()) {
                    currentIndex = 0;
                    if (config.getBoolean("settings.randomize", true)) {
                        Collections.shuffle(messages);
                    }
                }
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L);
    }

    public void broadcast(String rawMessage) {
        String prefix = ChatUtils.colorize(config.getString("settings.prefix", "&#6CCAFE&lINFO &8┃ &f"));
        String message = ChatUtils.colorize(rawMessage);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(prefix + message);
        Bukkit.broadcastMessage("");
    }

    public void reload() {
        loadConfig();
    }
}
