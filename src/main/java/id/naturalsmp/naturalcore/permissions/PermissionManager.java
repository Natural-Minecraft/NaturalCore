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
    }

    public void loadPermissions() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ranks.clear();

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null)
                continue;

            RankConfig rank = new RankConfig();
            rank.permission = key;
            rank.prefix = section.getString("prefix");
            rank.suffix = section.getString("suffix");
            rank.weight = section.getInt("weight", 0);
            rank.permissions = section.getStringList("permissions");
            rank.inheritance = section.getStringList("inheritance");

            ranks.put(key, rank);
            registerBukkitPermission(rank);
        }
    }

    private void registerBukkitPermission(RankConfig rank) {
        Permission perm = Bukkit.getPluginManager().getPermission(rank.permission);
        if (perm == null) {
            perm = new Permission(rank.permission, PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(perm);
        }

        // Set children permissions
        for (String child : rank.permissions) {
            perm.getChildren().put(child, true);
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
            // Group name is usually the suffix of the permission (e.g. naturalsmp.midi ->
            // midi)
            String groupName = rank.permission.contains(".")
                    ? rank.permission.substring(rank.permission.lastIndexOf(".") + 1)
                    : rank.permission;

            lp.getGroupManager().modifyGroup(groupName, group -> {
                // Clear existing prefixes/weights of the same priority or just add new
                if (rank.prefix != null) {
                    group.data().add(PrefixNode.builder(rank.prefix, rank.weight).build());
                }

                group.data().add(WeightNode.builder(rank.weight).build());

                // Add inheritance
                if (rank.inheritance != null) {
                    for (String parentPerm : rank.inheritance) {
                        String parentGroup = parentPerm.contains(".")
                                ? parentPerm.substring(parentPerm.lastIndexOf(".") + 1)
                                : parentPerm;
                        group.data().add(InheritanceNode.builder(parentGroup).build());
                    }
                }
            });
        }

        plugin.getLogger().info("Successfully synced rank-config.yml metadata to LuckPerms! 🔑💎");
    }

    public static class RankConfig {
        String permission;
        String prefix;
        String suffix;
        int weight;
        List<String> permissions;
        List<String> inheritance;
    }
}
