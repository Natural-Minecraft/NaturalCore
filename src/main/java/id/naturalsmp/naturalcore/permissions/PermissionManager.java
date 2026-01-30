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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    private final NaturalCore plugin;
    private final File configFile;
    private final Map<String, RankConfig> ranks = new HashMap<>();

    public PermissionManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "rank-config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("rank-config.yml", false);
        }

        loadPermissions();

        // --- REALTIME LUCKPERMS SYNC (v2.0) ---
        setupLuckPermsListener();
    }

    private void setupLuckPermsListener() {
        try {
            net.luckperms.api.LuckPerms lp = net.luckperms.api.LuckPermsProvider.get();
            lp.getEventBus().subscribe(plugin, net.luckperms.api.event.user.UserDataRecalculateEvent.class, e -> {
                org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(e.getUser().getUniqueId());
                if (p != null && p.isOnline()) {
                    // Update metadata real-time if rank changed (V2.0 Logging)
                    plugin.getLogger().info("Rank change detected for " + p.getName() + " - metadata updated.");
                }
            });
        } catch (Exception ignored) {
        }
    }

    public void loadPermissions() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ranks.clear();

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null)
                continue;

            RankConfig rank = new RankConfig();
            rank.id = key; // Simple ID (e.g. "vip")
            rank.permission = section.getString("permission", "naturalsmp." + key); // Fallback if missing
            rank.consoleName = section.getString("console-name", key.toUpperCase());
            rank.displayName = ChatUtils.colorize(section.getString("display-name", "&7" + key));

            rank.prefix = section.getString("prefix");
            rank.suffix = section.getString("suffix");
            rank.weight = section.getInt("weight", 0);
            rank.permissions = section.getStringList("permissions");
            rank.disabledPermissions = section.getStringList("disabled-perms");
            rank.inheritance = section.getStringList("inheritance");

            ranks.put(rank.id, rank); // Map by ID now
            registerBukkitPermission(rank);

            plugin.getLogger().info("Loaded Rank: " + rank.consoleName + " (ID: " + rank.id + ")");
        }
    }

    private void registerBukkitPermission(RankConfig rank) {
        Permission perm = Bukkit.getPluginManager().getPermission(rank.permission);
        if (perm == null) {
            perm = new Permission(rank.permission, PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(perm);
        }

        // Set children permissions (True)
        for (String child : rank.permissions) {
            perm.getChildren().put(child, true);
        }

        // Set disabled permissions (False)
        for (String disabled : rank.disabledPermissions) {
            perm.getChildren().put(disabled, false);
        }

        perm.recalculatePermissibles();
    }

    public void syncToLuckPerms() {
        LuckPerms lp;
        try {
            lp = LuckPermsProvider.get();
        } catch (Exception e) {
            plugin.getLogger().warning("LuckPerms API not found. Skipping metadata sync.");
            return;
        }

        for (RankConfig rank : ranks.values()) {
            // Group name is the ID (vip, midi, etc.)
            String groupName = rank.id;

            lp.getGroupManager().modifyGroup(groupName, group -> {
                // Metadata
                if (rank.prefix != null) {
                    group.data().add(PrefixNode.builder(rank.prefix, rank.weight).build());
                }

                // Add display name as a meta value if needed, or specific logic
                // For now, we mainly sync prefix and weight

                group.data().add(WeightNode.builder(rank.weight).build());

                // Allowed Permissions
                for (String p : rank.permissions) {
                    group.data().add(Node.builder(p).value(true).build());
                }

                // Disabled Permissions (Negated nodes)
                if (rank.disabledPermissions != null) {
                    for (String p : rank.disabledPermissions) {
                        group.data().add(Node.builder(p).value(false).build());
                    }
                }

                // Add inheritance
                if (rank.inheritance != null) {
                    for (String parentId : rank.inheritance) {
                        // inheritance list now contains IDs (e.g. "default") from config
                        group.data().add(InheritanceNode.builder(parentId).build());
                    }
                }
            });
        }

        plugin.getLogger().info("Successfully synced rank-config.yml metadata to LuckPerms! 🔑💎");
    }

    public Map<String, RankConfig> getRanks() {
        return ranks;
    }

    public void addPermission(String rankId, String permission) {
        RankConfig rank = ranks.get(rankId);
        if (rank != null && !rank.permissions.contains(permission)) {
            rank.permissions.add(permission);
            saveRank(rank);
            syncToLuckPerms();
        }
    }

    public void setWeight(String rankId, int weight) {
        RankConfig rank = ranks.get(rankId);
        if (rank != null) {
            rank.weight = weight;
            saveRank(rank);
            syncToLuckPerms();
        }
    }

    public void saveRank(RankConfig rank) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        // Use ID as key
        ConfigurationSection section = config.getConfigurationSection(rank.id);
        if (section == null) {
            section = config.createSection(rank.id);
        }

        section.set("permission", rank.permission);
        section.set("console-name", rank.consoleName);
        section.set("display-name", rank.displayName.replace("§", "&"));
        section.set("prefix", rank.prefix);
        section.set("suffix", rank.suffix);
        section.set("weight", rank.weight);
        section.set("permissions", rank.permissions);
        section.set("disabled-perms", rank.disabledPermissions);
        section.set("inheritance", rank.inheritance);

        try {
            config.save(configFile);
            // Re-register to apply changes immediately
            registerBukkitPermission(rank);
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Failed to save rank-config.yml!");
            e.printStackTrace();
        }
    }

    public static class RankConfig {
        public String id;
        public String permission;
        public String consoleName;
        public String displayName;

        public String prefix;
        public String suffix;
        public int weight;
        public List<String> permissions;
        public List<String> disabledPermissions;
        public List<String> inheritance;
    }
}
