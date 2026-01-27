package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerInfoCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public ServerInfoCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        String cmd = label.toLowerCase();

        switch (cmd) {
            case "list", "online", "plist", "who" -> {
                return showOnlinePlayers(sender);
            }
            case "lag", "tps", "mem", "memory" -> {
                return showServerPerformance(sender);
            }
            case "info", "about" -> {
                return showServerInfo(sender);
            }
            case "help" -> {
                return showHelp(sender);
            }
        }
        return false;
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatUtils.colorize(
                "   &#00AAFF&lɴ&#55FF55&lᴀ&#AAFF55&lᴛ&#FFFF55&lᴜ&#FFAA00&lʀ&#FF5555&lᴀ&#FF55FF&lʟ &#00AAFF&lᴄ&#55FF55&lᴏ&#AAFF55&lʀ&#FFFF55&lᴇ &7- Command Guide"));
        sender.sendMessage("");
        sender.sendMessage(ChatUtils.colorize(" &8» &f/gm [mode] &7- Ganti gamemode"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/list &7- Daftar player online"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/lag &7- Cek performa server"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/itemname &7- Ubah nama item"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/lore &7- Ubah lore item"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/shout &7- Berteriak global"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/bcworld &7- Broadcast ke world"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/freeze &7- Bekukan player"));
        sender.sendMessage("");
        sender.sendMessage(ChatUtils.colorize(" &7Type &b/nacore &7for admin tools."));
        sender.sendMessage("");
        return true;
    }

    // --- /list ---
    private boolean showOnlinePlayers(CommandSender sender) {
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();

        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &6&lONLINE PLAYERS &7(" + online + "/" + max + ")"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));

        // Sort by Weight from rank-config.yml
        Map<String, id.naturalsmp.naturalcore.permissions.PermissionManager.RankConfig> ranks = plugin
                .getPermissionManager().getRanks();

        List<Player> sortedPlayers = Bukkit.getOnlinePlayers().stream()
                .sorted((p1, p2) -> {
                    int w1 = getPlayerWeight(p1, ranks);
                    int w2 = getPlayerWeight(p2, ranks);
                    return Integer.compare(w2, w1); // Higher weight first
                })
                .collect(Collectors.toList());

        String playerList = sortedPlayers.stream()
                .map(p -> ChatUtils.formatMessage(p, "%displayname%"))
                .collect(Collectors.joining("&7, &f"));

        if (online > 0) {
            sender.sendMessage(ChatUtils.colorize("&f" + playerList));
        } else {
            sender.sendMessage(ChatUtils.colorize("&7Tidak ada player online."));
        }
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        return true;
    }

    private int getPlayerWeight(Player p,
            Map<String, id.naturalsmp.naturalcore.permissions.PermissionManager.RankConfig> ranks) {
        int maxWeight = 0;
        for (Map.Entry<String, id.naturalsmp.naturalcore.permissions.PermissionManager.RankConfig> entry : ranks
                .entrySet()) {
            if (p.hasPermission(entry.getKey())) {
                maxWeight = Math.max(maxWeight, entry.getValue().weight);
            }
        }
        return maxWeight;
    }

    // --- /lag & /tps ---
    private boolean showServerPerformance(CommandSender sender) {
        // TPS (Paper API or Standard NMS fallback)
        double[] tps = Bukkit.getTPS();
        String tpsString = String.format("%.1f", tps[0]);
        String tpsColor = (tps[0] > 18.0) ? "&a" : (tps[0] > 15.0) ? "&e" : "&c";

        // RAM
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = allocatedMemory - freeMemory;

        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &b&lSERVER PERFORMANCE"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &7TPS (1m): " + tpsColor + tpsString + (tps[0] > 20 ? "*" : "")));
        sender.sendMessage(ChatUtils.colorize(" &7RAM: &a" + usedMemory + "MB &7/ &8" + maxMemory + "MB"));
        sender.sendMessage(ChatUtils.colorize(" &7Uptime: &e" + getUptime()));
        sender.sendMessage(ChatUtils.colorize(" &7Entities: &f" + Bukkit.getWorlds().get(0).getEntities().size())); // Main
                                                                                                                    // world
                                                                                                                    // only
                                                                                                                    // for
                                                                                                                    // quick
                                                                                                                    // check
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        return true;
    }

    // --- /info ---
    private boolean showServerInfo(CommandSender sender) {
        // Reads from config in future
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &a&lNaturalSMP &7- Season 1"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &7Version: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatUtils.colorize(" &7Authors: &fNaturalSMP Team"));
        sender.sendMessage(ChatUtils.colorize(" &7Website: &bstore.naturalsmp.id"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        return true;
    }

    private String getUptime() {
        long uptimeMillis = System.currentTimeMillis()
                - java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis % (1000 * 60 * 60)) / (1000 * 60);
        return hours + "h " + minutes + "m";
    }
}
