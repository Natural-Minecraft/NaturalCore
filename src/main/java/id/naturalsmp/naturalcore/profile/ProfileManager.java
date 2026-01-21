package id.naturalsmp.naturalcore.profile;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsProvider;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;

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

    public boolean hasAuraSkills() {
        return Bukkit.getPluginManager().isPluginEnabled("AuraSkills");
    }

    // --- ECONOMY ---

    public double getVaultBalance(OfflinePlayer player) {
        if (plugin.getVaultManager().getEconomy() == null)
            return 0.0;
        return plugin.getVaultManager().getEconomy().getBalance(player);
    }

    public double getCoinsEngineBalance(OfflinePlayer player) {
        if (!hasCoinsEngine)
            return 0.0;
        try {
            return CoinsEngineAPI.getBalance(player.getUniqueId(), CoinsEngineAPI.getCurrency("naturalcoin"));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getFormattedVaultBalance(OfflinePlayer player) {
        if (plugin.getVaultManager().getEconomy() == null)
            return "Rp 0";
        return plugin.getVaultManager().getEconomy().format(getVaultBalance(player));
    }

    // --- STATS ---

    public String getKDR(OfflinePlayer p) {
        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        if (deaths == 0)
            return String.valueOf(kills);
        double kdr = (double) kills / deaths;
        return String.format("%.2f", kdr);
    }

    public String getPlaytimeFormatted(OfflinePlayer p) {
        long ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long seconds = ticks / 20;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public int getMobKills(OfflinePlayer p) {
        return p.getStatistic(Statistic.MOB_KILLS);
    }

    public int getDeaths(OfflinePlayer p) {
        return p.getStatistic(Statistic.DEATHS);
    }

    public String getFirstJoin(OfflinePlayer p) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy");
        return sdf.format(new java.util.Date(p.getFirstPlayed()));
    }

    // --- AURASKILLS ---

    public int getAuraSkillsPower(OfflinePlayer p) {
        if (!hasAuraSkills())
            return 0;
        try {
            AuraSkillsApi api = AuraSkillsProvider.getInstance();
            SkillsUser user = api.getUser(p.getUniqueId());
            if (user == null)
                return 0;

            int total = 0;
            for (Skill skill : api.getGlobalRegistry().getSkills()) {
                total += user.getSkillLevel(skill);
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getAuraSkillsLevel(OfflinePlayer p, String skillName) {
        if (!hasAuraSkills())
            return 0;
        try {
            AuraSkillsApi api = AuraSkillsProvider.getInstance();
            SkillsUser user = api.getUser(p.getUniqueId());
            if (user == null)
                return 0;
            Skill skill = api.getGlobalRegistry().getSkill(NamespacedId.fromDefault(skillName));
            return skill != null ? user.getSkillLevel(skill) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
