package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeleportCommand implements CommandExecutor {

    private final TeleportManager manager;

    public TeleportCommand(TeleportManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigUtils.getMessage("global.only-player"));
            return true;
        }

        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.teleport");

        // --- ADMIN COMMANDS ---

        // 1. /TP <player> [target]
        if (label.equalsIgnoreCase("tp")) {
            if (!p.hasPermission("naturalsmp.tp")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }

            // /tp <x> <y> <z>
            if (args.length == 3) {
                try {
                    double x = parseCoord(p.getLocation().getX(), args[0]);
                    double y = parseCoord(p.getLocation().getY(), args[1]);
                    double z = parseCoord(p.getLocation().getZ(), args[2]);
                    Location loc = new Location(p.getWorld(), x, y, z, p.getLocation().getYaw(),
                            p.getLocation().getPitch());
                    p.teleport(loc);
                    p.sendMessage(prefix + ConfigUtils.getString("messages.teleport.tp-coords")
                            .replace("%x%", String.format("%.2f", x))
                            .replace("%y%", String.format("%.2f", y))
                            .replace("%z%", String.format("%.2f", z)));
                } catch (NumberFormatException e) {
                    p.sendMessage(ConfigUtils.getString("messages.economy.invalid-amount"));
                }
                return true;
            }

            // /tp <player> <x> <y> <z>
            if (args.length == 4) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    p.sendMessage(
                            ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
                    return true;
                }
                try {
                    double x = parseCoord(target.getLocation().getX(), args[1]);
                    double y = parseCoord(target.getLocation().getY(), args[2]);
                    double z = parseCoord(target.getLocation().getZ(), args[3]);
                    Location loc = new Location(target.getWorld(), x, y, z, target.getLocation().getYaw(),
                            target.getLocation().getPitch());
                    target.teleport(loc);
                    p.sendMessage(prefix + ConfigUtils.getString("messages.teleport.tp-coords-other")
                            .replace("%target%", target.getName())
                            .replace("%x%", String.format("%.2f", x))
                            .replace("%y%", String.format("%.2f", y))
                            .replace("%z%", String.format("%.2f", z)));
                } catch (NumberFormatException e) {
                    p.sendMessage(ConfigUtils.getString("messages.economy.invalid-amount"));
                }
                return true;
            }

            if (args.length == 0) {
                p.sendMessage(ConfigUtils.getString("messages.global.usage").replace("%usage%",
                        "/tp <player> | /tp <x> <y> <z> | /tp <p1> <p2> | /tp <p1> <x> <y> <z>"));
                return true;
            }

            Player target1 = Bukkit.getPlayer(args[0]);
            if (target1 == null) {
                p.sendMessage(ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
                return true;
            }

            if (args.length == 1) {
                // /tp <player>
                p.teleport(target1);
                p.sendMessage(prefix
                        + ConfigUtils.getString("messages.teleport.tp-success").replace("%target%", target1.getName()));
            } else if (args.length == 2) {
                // /tp <p1> <p2> (Admin mindahin P1 ke P2)
                Player target2 = Bukkit.getPlayer(args[1]);
                if (target2 == null) {
                    p.sendMessage(
                            ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[1]));
                    return true;
                }
                target1.teleport(target2);
                p.sendMessage(prefix
                        + ConfigUtils.getString("messages.teleport.tp-move")
                                .replace("%target1%", target1.getName())
                                .replace("%target2%", target2.getName()));
            }
            return true;
        }

        // 2. /TPHERE <player> (Force Pull)
        if (label.equalsIgnoreCase("tphere")) {
            if (!p.hasPermission("naturalsmp.tphere")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            if (args.length == 0) {
                p.sendMessage(ConfigUtils.getString("messages.global.usage").replace("%usage%", "/tphere <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
                return true;
            }
            target.teleport(p);
            p.sendMessage(prefix
                    + ConfigUtils.getString("messages.teleport.tphere-success").replace("%target%", target.getName()));
            return true;
        }

        // --- PLAYER COMMANDS (TPA SYSTEM) ---

        // 3. /TPA <player>
        if (label.equalsIgnoreCase("tpa")) {
            if (!p.hasPermission("naturalsmp.tpa")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            handleRequest(p, args, false); // False = TPA
            return true;
        }

        // 4. /TPAHERE <player>
        if (label.equalsIgnoreCase("tpahere")) {
            if (!p.hasPermission("naturalsmp.tpa")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            handleRequest(p, args, true); // True = TPAHERE
            return true;
        }

        // 5. /TPACCEPT
        if (label.equalsIgnoreCase("tpaccept")) {
            manager.acceptRequest(p);
            return true;
        }

        // 6. /TPDENY
        if (label.equalsIgnoreCase("tpdeny")) {
            manager.denyRequest(p);
            return true;
        }

        return true;
    }

    private void handleRequest(Player sender, String[] args, boolean isTpaHere) {
        if (args.length == 0) {
            sender.sendMessage(ConfigUtils.getString("messages.global.usage").replace("%usage%",
                    (isTpaHere ? "/tpahere" : "/tpa") + " <player>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ConfigUtils.getString("messages.global.player-not-found").replace("%player%", args[0]));
            return;
        }
        if (target.equals(sender)) {
            sender.sendMessage(ConfigUtils.getString("messages.teleport.tp-self"));
            return;
        }
        manager.sendTpaRequest(sender, target, isTpaHere);
    }

    private double parseCoord(double current, String input) throws NumberFormatException {
        if (input.startsWith("~")) {
            if (input.length() == 1)
                return current;
            return current + Double.parseDouble(input.substring(1));
        }
        return Double.parseDouble(input);
    }
}