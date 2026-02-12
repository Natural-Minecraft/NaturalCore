package id.naturalsmp.naturalcore;

import id.naturalsmp.naturalcore.admin.NaturalCoreGUI;
import id.naturalsmp.naturalcore.economy.EconomyCommand;
import id.naturalsmp.naturalcore.moderation.ModerationCommand;
import id.naturalsmp.naturalcore.utility.PlayerUtilCommand;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.TabCompleter;

public class NaturalCoreCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;

    // Instance command lain
    private final PlayerUtilCommand playerUtil;
    private final EconomyCommand economyUtil;
    private final ModerationCommand modUtil;

    public NaturalCoreCommand(NaturalCore plugin) {
        this.plugin = plugin;
        this.playerUtil = new PlayerUtilCommand();
        this.economyUtil = new EconomyCommand();
        this.modUtil = new ModerationCommand(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Hanya player yang bisa menggunakan perintah ini.");
                return true;
            }
            Player p = (Player) sender;
            if (p.hasPermission("naturalsmp.admin")) {
                new NaturalCoreGUI(plugin).openGUI(p);
            } else {
                ConfigUtils.sendGeneral(p, "messages.global.version", "%version%",
                        plugin.getDescription().getVersion());
                ConfigUtils.sendGeneral(p, "messages.global.help-hint");
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // --- SUB: ADMIN (Cascading Admin Branch) ---
        if (sub.equals("admin")) {
            if (!sender.hasPermission("naturalsmp.admin"))
                return noPerm(sender);
            if (args.length < 2) {
                ConfigUtils.sendUsage(sender, "/nacore admin <reload|ranksync|resetseason|gui|status>");
                return true;
            }
            String adminSub = args[1].toLowerCase();
            String[] adminArgs = Arrays.copyOfRange(args, 2, args.length);

            switch (adminSub) {
                case "reload" -> {
                    ConfigUtils.sendAdmin(sender, "messages.admin.reload.starting");
                    performDeepReload(sender);
                }
                case "ranksync" -> {
                    ConfigUtils.sendAdmin(sender, "messages.admin.ranksync.starting");
                    plugin.getPermissionManager().syncToLuckPerms();
                    ConfigUtils.sendAdmin(sender, "messages.admin.ranksync.success");
                }
                case "resetseason" -> {
                    if (adminArgs.length < 1 || !adminArgs[0].equalsIgnoreCase("confirm")) {
                        ConfigUtils.sendAdmin(sender, "messages.admin.resetseason.warning");
                        ConfigUtils.sendUsage(sender, "/nacore admin resetseason confirm");
                    } else {
                        plugin.getSeasonResetManager().performFullReset(sender);
                    }
                }
                case "status" -> {
                    if (sender instanceof Player p)
                        plugin.getStatusGUI().openGUI(p);
                }
                case "backup" -> {
                    if (plugin.getBackupManager() != null) {
                        ConfigUtils.sendAdmin(sender, "messages.admin.backup.starting");
                        plugin.getBackupManager().createBackup("ManualBackup");
                        ConfigUtils.sendAdmin(sender, "messages.admin.backup.success");
                    }
                }
                case "restart" -> {
                    new id.naturalsmp.naturalcore.admin.RestartAlertCommand().onCommand(sender, command, "restart",
                            adminArgs);
                }
                case "setfirstjoinkit" -> {
                    if (!(sender instanceof Player pKit)) {
                        ConfigUtils.sendError(sender, "Hanya untuk player.");
                        return true;
                    }
                    // Save Items
                    List<String> kitItems = new ArrayList<>();
                    for (org.bukkit.inventory.ItemStack item : pKit.getInventory().getStorageContents()) {
                        if (item != null && item.getType() != org.bukkit.Material.AIR) {
                            kitItems.add(item.getType().toString() + ":" + item.getAmount());
                        }
                    }
                    plugin.getConfig().set("first-join-kit.items", kitItems);

                    // Save Armor
                    org.bukkit.inventory.ItemStack[] armor = pKit.getInventory().getArmorContents();
                    plugin.getConfig().set("first-join-kit.armor.boots",
                            armor[0] != null ? armor[0].getType().toString() : "AIR");
                    plugin.getConfig().set("first-join-kit.armor.leggings",
                            armor[1] != null ? armor[1].getType().toString() : "AIR");
                    plugin.getConfig().set("first-join-kit.armor.chestplate",
                            armor[2] != null ? armor[2].getType().toString() : "AIR");
                    plugin.getConfig().set("first-join-kit.armor.helmet",
                            armor[3] != null ? armor[3].getType().toString() : "AIR");

                    plugin.saveConfig();
                    ConfigUtils.sendGeneral(sender, "messages.global.success-set", "%feature%", "First Join Kit");
                    return true;
                }
                case "gui" -> {
                    if (sender instanceof Player p)
                        new NaturalCoreGUI(plugin).openGUI(p);
                }
                default ->
                    ConfigUtils.sendError(sender, ConfigUtils.getMessage("messages.global.sub-command-not-found"));
            }
            return true;
        }

        // --- GLOBAL RELOAD (Legacy Support) ---
        if (sub.equals("reload"))

        {
            if (!sender.hasPermission("naturalsmp.admin"))
                return noPerm(sender);
            performDeepReload(sender);
            return true;
        }

        if (sub.equals("ver") || sub.equals("version")) {
            if (sender instanceof Player p) {
                ConfigUtils.sendGeneral(p, "messages.global.version", "%version%",
                        plugin.getDescription().getVersion());
            } else {
                sender.sendMessage("NaturalCore v" + plugin.getDescription().getVersion());
            }
            return true;
        }

        // --- DYNAMIC PROXY DISPATCH ---
        String[] proxyArgs = Arrays.copyOfRange(args, 1, args.length);

        // List of all mapped commands to proxy
        switch (sub) {
            case "heal", "feed", "fly" -> {
                playerUtil.onCommand(sender, command, sub, proxyArgs);
                return true;
            }
            case "bal", "pay", "setbal", "takebal" -> {
                economyUtil.onCommand(sender, command, sub, proxyArgs);
                return true;
            }
            case "god", "vanish", "v", "whois" -> {
                modUtil.onCommand(sender, command, sub, proxyArgs);
                return true;
            }
            case "ranksync" -> {
                if (sender.hasPermission("naturalsmp.admin")) {
                    ConfigUtils.sendAdmin(sender, "messages.admin.ranksync.starting");
                    plugin.getPermissionManager().syncToLuckPerms();
                    ConfigUtils.sendAdmin(sender, "messages.admin.ranksync.success");
                }
                return true;
            }
            case "spawn" -> {
                new id.naturalsmp.naturalcore.spawn.SpawnCommand(plugin.getSpawnManager()).onCommand(sender, command,
                        sub, proxyArgs);
                return true;
            }
            case "home", "homes", "sethome", "delhome" -> {
                new id.naturalsmp.naturalcore.home.HomeCommand(plugin.getHomeManager(),
                        plugin.getHomeGUI()).onCommand(sender, command, sub, proxyArgs);
                return true;
            }
        }

        ConfigUtils.sendError(sender, "Sub-command tidak ditemukan. Cek /nacore help.");
        return true;
    }

    private void performDeepReload(CommandSender sender) {
        plugin.reload();
        ConfigUtils.sendAdmin(sender, "messages.admin.reload.success");
    }

    private boolean noPerm(CommandSender s) {
        if (s instanceof Player p) {
            ConfigUtils.sendGeneral(p, "messages.global.no-permission");
        } else {
            s.sendMessage("You don't have permission.");
        }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            List<String> list = new ArrayList<>(
                    Arrays.asList("admin", "version", "help", "spawn", "home", "bal", "vanish", "heal", "feed", "fly"));
            if (sender.hasPermission("naturalsmp.admin"))
                list.add("reload");
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return Arrays.asList("reload", "resetseason", "ranksync", "gui", "status", "backup").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase())).collect(java.util.stream.Collectors.toList());
        }

        return java.util.Collections.emptyList();
    }
}
