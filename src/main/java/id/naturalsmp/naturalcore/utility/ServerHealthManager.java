package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerHealthManager {

    private final NaturalCore plugin;
    private final List<Double> tpsHistory = Collections.synchronizedList(new LinkedList<>());
    private final List<Double> ramHistory = Collections.synchronizedList(new LinkedList<>());
    private static final int MAX_HISTORY = 30;
    private BukkitTask tickTask;

    public ServerHealthManager(NaturalCore plugin) {
        this.plugin = plugin;
        startTicking();
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private void startTicking() {
        this.tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Capture TPS (Safe to call Bukkit.getTPS() sync or async in most versions,
                // but history lists must be synchronized)
                double[] tps = Bukkit.getTPS();
                tpsHistory.add(0, tps[0]);
                if (tpsHistory.size() > MAX_HISTORY)
                    tpsHistory.remove(tpsHistory.size() - 1);

                // Capture RAM (MB)
                long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024
                        / 1024;
                ramHistory.add(0, (double) usedMemory);
                if (ramHistory.size() > MAX_HISTORY)
                    ramHistory.remove(ramHistory.size() - 1);
            }
        }.runTaskTimer(plugin, 0, 20L * 5); // Run synchronously to be safe with List access and world counting if
                                            // needed
    }

    public List<Double> getTpsHistory() {
        return tpsHistory;
    }

    public List<Double> getRamHistory() {
        return ramHistory;
    }

    public double getCurrentTps() {
        return tpsHistory.isEmpty() ? 20.0 : tpsHistory.getFirst();
    }

    public double getCurrentRam() {
        return ramHistory.isEmpty() ? 0 : ramHistory.getFirst();
    }

    public double getMaxRam() {
        return Runtime.getRuntime().maxMemory() / 1024 / 1024;
    }

    public int getTotalEntities() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getEntityCount();
        }
        return total;
    }

    public int getTotalChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }
}
