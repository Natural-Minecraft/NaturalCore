package id.naturalsmp.naturalcore.teleport;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players.");
            return true;
        }

        Player p = (Player) sender;
        String prefix = ConfigUtils.getString("prefix.teleport");

        // --- ADMIN COMMANDS ---

        // 1. /TP <player> [target]
        if (label.equalsIgnoreCase("tp")) {
            if (!p.hasPermission("naturalsmp.tp")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /tp <player> [target]"));
                return true;
            }

            Player target1 = Bukkit.getPlayer(args[0]);
            if (target1 == null) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            if (args.length == 1) {
                // /tp <target> (Admin ke Target)
                p.teleport(target1);
                p.sendMessage(prefix + ConfigUtils.getString("messages.tp-success").replace("%target%", target1.getName()));
            } else {
                // /tp <p1> <p2> (Admin mindahin P1 ke P2)
                Player target2 = Bukkit.getPlayer(args[1]);
                if (target2 == null) {
                    p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[1]));
                    return true;
                }
                target1.teleport(target2);
                p.sendMessage(prefix + ChatUtils.colorize("&aMemindahkan &e" + target1.getName() + " &ake &e" + target2.getName()));
            }
            return true;
        }

        // 2. /TPHERE <player> (Force Pull)
        if (label.equalsIgnoreCase("tphere")) {
            if (!p.hasPermission("naturalsmp.tphere")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /tphere <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }
            target.teleport(p);
            p.sendMessage(prefix + ConfigUtils.getString("messages.tphere-success").replace("%target%", target.getName()));
            return true;
        }

        // --- PLAYER COMMANDS (TPA SYSTEM) ---

        // 3. /TPA <player>
        if (label.equalsIgnoreCase("tpa")) {
            if (!p.hasPermission("naturalsmp.tpa")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            handleRequest(p, args, false); // False = TPA
            return true;
        }

        // 4. /TPAHERE <player>
        if (label.equalsIgnoreCase("tpahere")) {
            if (!p.hasPermission("naturalsmp.tpa")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
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
            sender.sendMessage(ChatUtils.colorize("&cUsage: /" + (isTpaHere ? "tpahere" : "tpa") + " <player>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
            return;
        }
        if (target.equals(sender)) {
            sender.sendMessage(ChatUtils.colorize("&cTidak bisa teleport ke diri sendiri!"));
            return;
        }
        manager.sendTpaRequest(sender, target, isTpaHere);
    }
}