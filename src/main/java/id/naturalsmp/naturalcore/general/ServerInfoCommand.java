package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import id.naturalsmp.naturalcore.permissions.PermissionManager.RankConfig;

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
        sender.sendMessage(ChatUtils.colorize(" &8» &f/spawn &7- Teleport ke spawn"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/rtp &7- Random Teleport"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/tpa <player> &7- Request teleport"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/sethome <name> &7- Set home"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/home [name] &7- Teleport ke home"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/bal &7- Cek uang"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/pay <player> <amount> &7- Kirim uang"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/trade <player> &7- Trade dengan player"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/profile &7- Cek profil & statistik"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/tags &7- Ganti tag chat"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/chatcolor &7- Ganti warna chat"));
        sender.sendMessage(ChatUtils.colorize(" &8» &f/afk &7- Set status AFK"));
        sender.sendMessage("");
        sender.sendMessage(ChatUtils.colorize(" &7Type &b/nacore &7for admin tools."));
        sender.sendMessage("");
        return true;
    }

    // --- /list ---
    private boolean showOnlinePlayers(CommandSender sender) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();

        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &6&lONLINE PLAYERS &7(" + onlineCount + "/" + max + ")"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));

        if (onlineCount == 0) {
            sender.sendMessage(ChatUtils.colorize("&7Tidak ada player online."));
            sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
            return true;
        }

        Map<RankConfig, List<Player>> groupedPlayers = new HashMap<>();
        List<Player> defaultPlayers = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            RankConfig highest = plugin.getPermissionManager().getHighestRank(p);
            if (highest != null) {
                groupedPlayers.computeIfAbsent(highest, k -> new ArrayList<>()).add(p);
            } else {
                defaultPlayers.add(p);
            }
        }

        // Sort ranks by weight descending
        List<RankConfig> sortedRanks = new ArrayList<>(groupedPlayers.keySet());
        sortedRanks.sort((r1, r2) -> Integer.compare(r2.weight, r1.weight));

        for (RankConfig rank : sortedRanks) {
            List<Player> players = groupedPlayers.get(rank);
            String playerNames = players.stream()
                    .map(p -> ChatUtils.formatMessage(p, "%displayname%"))
                    .collect(Collectors.joining("&7, "));
            sender.sendMessage(ChatUtils.colorize(" " + rank.displayName + "&8: &f" + playerNames));
        }

        if (!defaultPlayers.isEmpty()) {
            String playerNames = defaultPlayers.stream()
                    .map(p -> ChatUtils.formatMessage(p, "%displayname%"))
                    .collect(Collectors.joining("&7, "));
            sender.sendMessage(ChatUtils.colorize(" &7Member&8: &f" + playerNames));
        }

        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        return true;
    }



    // --- /info ---
    private boolean showServerInfo(CommandSender sender) {
        // Reads from config in future
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &a&lNaturalSMP &bReborn"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        sender.sendMessage(ChatUtils.colorize(" &7Version: &f" + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatUtils.colorize(" &7Authors: &fNaturalSMP Team"));
        sender.sendMessage(ChatUtils.colorize(" &7Website: &bwww.naturalsmp.net"));
        sender.sendMessage(ChatUtils.colorize("&8&m----------------------------------------"));
        return true;
    }

}
