package id.naturalsmp.naturalcore.utility;

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
import java.util.stream.Collectors;

public class NaturalLaggCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public NaturalLaggCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length == 0) {
            // /lagg - Show Server Stats
            if (!sender.hasPermission("naturalsmp.lag")) { // Permission for stats
                sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            return showServerPerformance(sender);
        }

        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("clean")) {
            // /lagg clear - Trigger NaturalLagg cleanup
            if (!sender.hasPermission("naturalsmp.admin")) { // Admin only for cleanup
                ConfigUtils.sendGeneral(sender, "messages.global.no-permission");
                return true;
            }

            plugin.getLaggManager().startCleanup(15); // Default start time 15s to sync animation
            ConfigUtils.sendGeneral(sender, "messages.lagg.starting-clear");
            return true;
        }

        ConfigUtils.sendUsage(sender, "/lagg [clear]");
        return true;
    }

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

        // Entities (Weighted Count to be more aesthetic/useful)
        // Just counting main world for speed as per user request for simple check
        int entityCount = 0;
        if (Bukkit.getWorld("world") != null) {
            entityCount = Bukkit.getWorld("world").getEntities().size();
        } else if (!Bukkit.getWorlds().isEmpty()) {
            entityCount = Bukkit.getWorlds().get(0).getEntities().size();
        }

        // Players
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();

        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize("     &b&lNaturalSMP &8- &3Server Performance"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &8» &fTPS Status: " + tpsColor + tpsString + " &7(1m)"));
        sender.sendMessage(ChatUtils.colorize(" &8» &fMemory Usage: &a" + usedMemory + "MB &7/ &8" + maxMemory + "MB"));
        sender.sendMessage(ChatUtils.colorize(" &8» &fEntities: &e" + entityCount + " &7(Main World)"));
        sender.sendMessage(ChatUtils.colorize(" &8» &fPlayers: &b" + online + "&7/" + max));
        sender.sendMessage(ChatUtils.colorize(" &8» &fUptime: &6" + getUptime()));
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
