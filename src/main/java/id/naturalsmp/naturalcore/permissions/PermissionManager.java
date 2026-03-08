package id.naturalsmp.naturalcore.permissions;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.WeightNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    private final NaturalCore plugin;
    private final File configFile;
    private final Map<String, RankConfig> ranks = new java.util.LinkedHashMap<>();

    public PermissionManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "ranks.yml");

        if (!configFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }

        loadRanks();
        setupLuckPermsListener();
    }

    private void setupLuckPermsListener() {
        try {
            LuckPerms lp = LuckPermsProvider.get();
            lp.getEventBus().subscribe(plugin, net.luckperms.api.event.user.UserDataRecalculateEvent.class, e -> {
                org.bukkit.entity.Player p = Bukkit.getPlayer(e.getUser().getUniqueId());
                if (p != null && p.isOnline()) {
                    plugin.getLogger().info("Rank change detected for " + p.getName());
                }
            });
        } catch (Exception ignored) {
        }
    }

    public void loadRanks() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ranks.clear();

        // Load dari section "ranks" (donator ranks: member, midi, vip, mvp, nature)
        loadSection(config, "ranks");

        // Load dari section "rank-lainnya" (staff, content creator, dll)
        loadSection(config, "rank-lainnya");

        plugin.getLogger().info("Loaded " + ranks.size() + " ranks from ranks.yml");
    }

    private void loadSection(YamlConfiguration config, String sectionName) {
        ConfigurationSection section = config.getConfigurationSection(sectionName);
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection rankSection = section.getConfigurationSection(key);
            if (rankSection == null)
                continue;

            RankConfig rank = new RankConfig();
            rank.id = key;
            rank.section = sectionName;
            rank.displayName = ChatUtils.colorize(rankSection.getString("display", "\u00267" + key));
            rank.prefix = rankSection.getString("prefix", "");
            rank.weight = rankSection.getInt("weight", 0);
            rank.permissions = rankSection.getStringList("permissions");
            rank.inheritance = rankSection.getString("inherit");

            // GUI related fields (for /ranks)
            if (rankSection.contains("gui")) {
                ConfigurationSection guiSec = rankSection.getConfigurationSection("gui");
                if (guiSec != null) {
                    rank.guiItem = guiSec.getString("item", "PAPER");
                    rank.guiName = guiSec.getString("name", rank.displayName);
                    rank.guiBenefits = guiSec.getStringList("benefits");
                }
            }

            ranks.put(rank.id, rank);
            registerBukkitPermission(rank);
        }
    }

    private void registerBukkitPermission(RankConfig rank) {
        String permName = "naturalsmp.rank." + rank.id;
        Permission perm = Bukkit.getPluginManager().getPermission(permName);
        if (perm == null) {
            perm = new Permission(permName, PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(perm);
        }

        // Apply children
        for (String p : rank.permissions) {
            perm.getChildren().put(p, true);
        }

        // Apply inheritance if exists
        if (rank.inheritance != null && ranks.containsKey(rank.inheritance)) {
            perm.getChildren().put("naturalsmp.rank." + rank.inheritance, true);
        }

        perm.recalculatePermissibles();
    }

    public void syncToLuckPerms() {
        LuckPerms lp;
        try {
            lp = LuckPermsProvider.get();
        } catch (Exception e) {
            plugin.getLogger().warning("LuckPerms API not found. Skipping sync.");
            return;
        }

        for (RankConfig rank : ranks.values()) {
            lp.getGroupManager().createAndLoadGroup(rank.id).thenAcceptAsync(group -> {
                // Clear existing nodes to avoid duplicates/conflicts during sync
                group.data().clear();

                // Add Weight
                group.data().add(WeightNode.builder(rank.weight).build());

                // Add Prefix
                if (rank.prefix != null && !rank.prefix.isEmpty()) {
                    group.data().add(PrefixNode.builder(rank.prefix, rank.weight).build());
                }

                // Add Permissions
                for (String p : rank.permissions) {
                    group.data().add(Node.builder(p).build());
                }

                // Add Inheritance
                if (rank.inheritance != null) {
                    group.data().add(InheritanceNode.builder(rank.inheritance).build());
                }

                // IMPORTANT: Add the rank permission itself node so hasPermission checks work
                group.data().add(Node.builder("naturalsmp.rank." + rank.id).build());

                lp.getGroupManager().saveGroup(group);
            });
        }
        plugin.getLogger().info("Ranks synced to LuckPerms groups successfully! \uD83D\uDD11");
    }

    public RankConfig getHighestRank(org.bukkit.entity.Player player) {
        RankConfig highest = null;
        for (RankConfig rank : ranks.values()) {
            if (player.hasPermission("naturalsmp.rank." + rank.id)) {
                if (highest == null || rank.weight > highest.weight) {
                    highest = rank;
                }
            }
        }
        return highest;
    }

    public boolean isAtLeast(org.bukkit.entity.Player player, String rankId) {
        if (player.isOp() || player.hasPermission("naturalsmp.admin"))
            return true;

        RankConfig target = ranks.get(rankId);
        if (target == null)
            return false;

        RankConfig current = getHighestRank(player);
        if (current == null)
            return false;

        return current.weight >= target.weight;
    }

    public Map<String, RankConfig> getRanks() {
        return ranks;
    }

    public void addPermission(String rankId, String permission) {
        RankConfig rank = ranks.get(rankId);
        if (rank != null) {
            rank.permissions.add(permission);
            saveToConfig();
            syncToLuckPerms();
        }
    }

    public void setWeight(String rankId, int weight) {
        RankConfig rank = ranks.get(rankId);
        if (rank != null) {
            rank.weight = weight;
            saveToConfig();
            syncToLuckPerms();
        }
    }

    private void saveToConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (RankConfig rank : ranks.values()) {
            String sectionName = rank.section != null ? rank.section : "ranks";
            ConfigurationSection parentSection = config.getConfigurationSection(sectionName);
            if (parentSection == null) {
                parentSection = config.createSection(sectionName);
            }
            ConfigurationSection rSec = parentSection.createSection(rank.id);
            rSec.set("display", rank.displayName.replace("\u00a7", "&"));
            rSec.set("prefix", rank.prefix);
            rSec.set("weight", rank.weight);
            rSec.set("permissions", rank.permissions);
            rSec.set("inherit", rank.inheritance);

            if (rank.guiItem != null) {
                ConfigurationSection g = rSec.createSection("gui");
                g.set("item", rank.guiItem);
                g.set("name", rank.guiName.replace("\u00a7", "&"));
                g.set("benefits", rank.guiBenefits);
            }
        }
        try {
            config.save(configFile);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static class RankConfig {
        public String id;
        public String section; // "ranks" atau "rank-lainnya"
        public String displayName;
        public String prefix;
        public int weight;
        public List<String> permissions;
        public String inheritance;

        // GUI Display Info
        public String guiItem;
        public String guiName;
        public List<String> guiBenefits;
    }
}
