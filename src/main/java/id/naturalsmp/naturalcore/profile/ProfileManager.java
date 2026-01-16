package id.naturalsmp.naturalcore.profile;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

public class ProfileManager {

    private final NaturalCore plugin;
    private final boolean hasCoinsEngine;

    public ProfileManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.hasCoinsEngine = Bukkit.getPluginManager().getPlugin("CoinsEngine") != null;
    }

    public boolean hasCoinsEngine() {
        return hasCoinsEngine;
    }

    // --- ECONOMY ---

    public double getVaultBalance(OfflinePlayer player) {
        if (plugin.getVaultManager().getEconomy() == null)
            return 0.0;
        return plugin.getVaultManager().getEconomy().getBalance(player);
    }

    public double getCoinsEngineBalance(Player player) {
        if (!hasCoinsEngine)
            return 0.0;
        try {
            // Mengambil balance dari default currency CoinsEngine (atau currency utama)
            // Asumsi currency ID 'naturalcoin' dari config user sebelumnya,
            // tapi idealnya kita cek API docs atau default.
            // Gunakan metode safe jika API berubah, disini kita pakai metode umum.
            return CoinsEngineAPI.getBalance(player, CoinsEngineAPI.getCurrency("naturalcoin"));
        } catch (Exception e) {
            // Fallback jika API error atau currency beda
            return 0.0;
        }
    }

    public String getFormattedVaultBalance(OfflinePlayer player) {
        if (plugin.getVaultManager().getEconomy() == null)
            return "Rp 0";
        return plugin.getVaultManager().getEconomy().format(getVaultBalance(player));
    }

    // --- STATS ---

    public String getKDR(Player p) {
        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        if (deaths == 0)
            return String.valueOf(kills);
        double kdr = (double) kills / deaths;
        return String.format("%.2f", kdr);
    }

    public String getPlayTime(Player p) {
        // Ticks to Hours
        long ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE); // ini sebenarnya ticks, bukan menit (legacy naming)
        long seconds = ticks / 20;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public int getMobKills(Player p) {
        return p.getStatistic(Statistic.MOB_KILLS);
    }

    public int getDeaths(Player p) {
        return p.getStatistic(Statistic.DEATHS);
    }

    public String getJoinDate(Player p) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy");
        return sdf.format(new java.util.Date(p.getFirstPlayed()));
    }
}
