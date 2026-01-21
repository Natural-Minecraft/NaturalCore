package id.naturalsmp.naturalcore.season;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsProvider;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.user.SkillsUser;
import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.tier.TierManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SeasonResetManager {

    private final NaturalCore plugin;

    public SeasonResetManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes a full season reset:
     * 1. 50% Tier Derank (Level / 2)
     * 2. 50% AuraSkills Level reduction
     */
    public void performFullReset(CommandSender admin) {
        admin.sendMessage(ChatUtils.colorize("&e[Reset] Starting global season reset process..."));

        // 1. Reset Tiers
        resetTiers(admin);

        // 2. Reset AuraSkills
        if (Bukkit.getPluginManager().isPluginEnabled("AuraSkills")) {
            resetAuraSkills(admin);
        } else {
            admin.sendMessage(ChatUtils.colorize("&7[Reset] AuraSkills not found/enabled. Skipping."));
        }

        admin.sendMessage(ChatUtils.colorize("&a&l[Reset] SEASON RESET COMPLETE!"));
        Bukkit.broadcastMessage(ChatUtils.colorize(
                "&6&lNaturalSMP &8» &fMusim telah berganti! Status Tier & Skill telah disesuaikan (50% Rebirth)."));
    }

    private void resetTiers(CommandSender admin) {
        TierManager tm = plugin.getTierManager();
        if (tm == null)
            return;

        Map<UUID, Integer> tiers = tm.getPlayerTiers();
        int count = 0;

        for (Map.Entry<UUID, Integer> entry : tiers.entrySet()) {
            int currentLevel = entry.getValue();
            int newLevel = Math.max(1, currentLevel / 2); // Divide by 2, min level 1

            tm.setPlayerLevel(entry.getKey(), newLevel);
            count++;
        }

        tm.savePlayerDataPublic();
        admin.sendMessage(ChatUtils.colorize("&7[Reset] Deranked &f" + count + " &7players in Tier system."));
    }

    private void resetAuraSkills(CommandSender admin) {
        try {
            // AuraSkills 2.2.4 pattern: AuraSkillsProvider.getInstance()
            AuraSkillsApi api = AuraSkillsProvider.getInstance();
            int userCount = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                SkillsUser user = api.getUser(p.getUniqueId());
                if (user == null)
                    continue;

                // AuraSkills 2.2.4 pattern: getGlobalRegistry().getSkills()
                Collection<Skill> skills = api.getGlobalRegistry().getSkills();
                for (Skill skill : skills) {
                    int currentLevel = user.getSkillLevel(skill);
                    int newLevel = currentLevel / 2;
                    user.setSkillLevel(skill, newLevel);
                    // Reset XP to 0 for the new level to ensure clean start
                    user.setSkillXp(skill, 0);
                }
                userCount++;
            }

            admin.sendMessage(
                    ChatUtils.colorize("&7[Reset] Reduced skills by 50% for &f" + userCount + " &7online players."));
            admin.sendMessage(ChatUtils.colorize(
                    "&e&oNote: For complete offline data reset, consider AuraSkills database commands if necessary."));

        } catch (Exception e) {
            admin.sendMessage(ChatUtils.colorize("&c[Reset] Error during AuraSkills reset: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
