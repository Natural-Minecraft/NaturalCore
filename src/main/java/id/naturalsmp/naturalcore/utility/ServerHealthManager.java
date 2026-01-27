package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;

public class ServerHealthManager {

    private final NaturalCore plugin;
    private final LinkedList<Double> tpsHistory = new LinkedList<>();
    private final LinkedList<Double> ramHistory = new LinkedList<>();
    private static final int MAX_HISTORY = 30;

    public ServerHealthManager(NaturalCore plugin) {
        this.plugin = plugin;
        startTicking();
    }

    private void startTicking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Capture TPS
                double[] tps = Bukkit.getTPS();
                tpsHistory.addFirst(tps[0]);
                if (tpsHistory.size() > MAX_HISTORY)
                    tpsHistory.removeLast();

                // Capture RAM (MB)
                long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024
                        / 1024;
                ramHistory.addFirst((double) usedMemory);
                if (ramHistory.size() > MAX_HISTORY)
                    ramHistory.removeLast();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20L * 5); // Every 5 seconds
    }

    public LinkedList<Double> getTpsHistory() {
        return tpsHistory;
    }

    public LinkedList<Double> getRamHistory() {
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
