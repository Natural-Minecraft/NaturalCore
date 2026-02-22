package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (label.equalsIgnoreCase("ptime")) {
            return handlePTime(sender, args);
        } else if (label.equalsIgnoreCase("pweather")) {
            return handlePWeather(sender, args);
        }
        return false;
    }

    private boolean handlePTime(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /ptime <player (opsional)> <time/range>/reset"));
            return true;
        }

        Player target;
        String timeArg;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[0]);
            timeArg = args[1];
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Harus spesifikasi player!");
                return true;
            }
            target = (Player) sender;
            timeArg = args[0];
        }

        if (target == null) {
            sender.sendMessage(ChatUtils.colorize("&cPlayer tidak ditemukan!"));
            return true;
        }

        if (timeArg.equalsIgnoreCase("reset")) {
            target.resetPlayerTime();
            sender.sendMessage(ConfigUtils.getString("messages.utils.visuals.ptime-reset")
                    .replace("%target%", target.getName()));
            return true;
        }

        long time;
        switch (timeArg.toLowerCase()) {
            case "day" -> time = 1000;
            case "noon" -> time = 6000;
            case "afternoon" -> time = 12000;
            case "night" -> time = 13000;
            case "midnight" -> time = 18000;
            default -> {
                try {
                    time = Long.parseLong(timeArg);
                } catch (NumberFormatException e) {
                    sender.sendMessage(
                            ChatUtils.colorize("&cFormat waktu tidak valid (Gunakan: day/night/noon/dll atau angka)."));
                    return true;
                }
            }
        }

        target.setPlayerTime(time, false);
        sender.sendMessage(ConfigUtils.getString("messages.utils.visuals.ptime-success")
                .replace("%target%", target.getName())
                .replace("%time%", timeArg));
        return true;
    }

    private boolean handlePWeather(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /pweather <player (opsional)> <rain/sun/thunder/reset>"));
            return true;
        }

        Player target;
        String weatherArg;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[0]);
            weatherArg = args[1];
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Harus spesifikasi player!");
                return true;
            }
            target = (Player) sender;
            weatherArg = args[0];
        }

        if (target == null) {
            sender.sendMessage(ChatUtils.colorize("&cPlayer tidak ditemukan!"));
            return true;
        }

        if (weatherArg.equalsIgnoreCase("reset")) {
            target.resetPlayerWeather();
            sender.sendMessage(ConfigUtils.getString("messages.utils.visuals.pweather-reset")
                    .replace("%target%", target.getName()));
            return true;
        }

        switch (weatherArg.toLowerCase()) {
            case "sun", "clear" -> target.setPlayerWeather(WeatherType.CLEAR);
            case "rain", "storm" -> target.setPlayerWeather(WeatherType.DOWNFALL);
            case "thunder" -> {
                target.setPlayerWeather(WeatherType.DOWNFALL);
                // Note: Thunder visualization in player-weather is limited in some versions,
                // but standard is DOWNFALL
                sender.sendMessage(
                        ChatUtils.colorize("&eNote: Thunder visualization is linked to rain on client-side."));
            }
            default -> {
                sender.sendMessage(ChatUtils.colorize("&cCuaca tidak valid (Gunakan: sun/rain/thunder/reset)."));
                return true;
            }
        }

        sender.sendMessage(ConfigUtils.getString("messages.utils.visuals.pweather-success")
                .replace("%target%", target.getName())
                .replace("%weather%", weatherArg));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if (alias.equalsIgnoreCase("ptime")) {
                list.addAll(Arrays.asList("day", "night", "noon", "afternoon", "midnight", "reset"));
            } else {
                list.addAll(Arrays.asList("sun", "rain", "thunder", "reset"));
            }
            return list.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (alias.equalsIgnoreCase("ptime")) {
                return Arrays.asList("day", "night", "noon", "afternoon", "midnight", "reset");
            } else {
                return Arrays.asList("sun", "rain", "thunder", "reset");
            }
        }
        return new ArrayList<>();
    }
}
