package id.naturalsmp.naturalcore.playtime;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlaytimeManager {

    private final NaturalCore plugin;
    private final File dataFile;
    private FileConfiguration data;
    private final Set<Milestone> milestones = new HashSet<>();
    private BukkitTask checkTask;

    public PlaytimeManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playtime_data.yml");
        loadData();
        setupMilestones();
        startCheckTask();
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        saveData(); // Final save
    }

    private void setupMilestones() {
        milestones.add(new Milestone("Starter", 1 * 3600, 1000, "kit starter"));
        milestones.add(new Milestone("Loyal", 10 * 3600, 5000, "excellentcrates give common %player% 1"));
        milestones.add(new Milestone("Veteran", 50 * 3600, 25000, "excellentcrates give rare %player% 1"));
        milestones.add(new Milestone("Legend", 100 * 3600, 50000, "excellentcrates give epic %player% 1"));
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ignored) {
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException ignored) {
        }
    }

    private void startCheckTask() {
        this.checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkMilestones(player);
                }
            }
        }.runTaskTimer(plugin, 100L, 1200L); // Every minute
    }

    public void checkMilestones(Player player) {
        long seconds = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        UUID uuid = player.getUniqueId();

        for (Milestone m : milestones) {
            String path = uuid + "." + m.name;
            if (seconds >= m.requiredSeconds && !data.getBoolean(path)) {
                awardMilestone(player, m);
                data.set(path, true);
                saveData();
            }
        }
    }

    private void awardMilestone(Player player, Milestone m) {
        player.sendMessage(
                ChatUtils.colorize("&6&lNaturalRewards &8» &aSelamat! Kamu mencapai milestone &e&l" + m.name + "&a!"));
        player.sendMessage(ChatUtils.colorize("&7Hadiah: &eRp " + (int) m.money + " &7dan bonus item."));

        // Give Money (Vault)
        if (plugin.getVaultManager().getEconomy() != null) {
            plugin.getVaultManager().getEconomy().depositPlayer(player, m.money);
        }

        // Execute Command
        if (m.command != null && !m.command.isEmpty()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), m.command.replace("%player%", player.getName()));
        }
    }

    public String getPlaytime(Player p) {
        long seconds = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        return h + "j " + m + "m";
    }

    private static class Milestone {
        String name;
        long requiredSeconds;
        double money;
        String command;

        Milestone(String name, long req, double money, String cmd) {
            this.name = name;
            this.requiredSeconds = req;
            this.money = money;
            this.command = cmd;
        }
    }
}
