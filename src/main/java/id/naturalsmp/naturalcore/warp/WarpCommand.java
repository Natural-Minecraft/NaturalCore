package id.naturalsmp.naturalcore.warp;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.TabCompleter;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final NaturalCore plugin;
    private final WarpGUI gui;

    public WarpCommand(NaturalCore plugin) {
        this.plugin = plugin;
        this.gui = new WarpGUI(plugin);
        plugin.getServer().getPluginManager().registerEvents(gui, plugin); // Register Listener GUI disini
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            ConfigUtils.sendGeneral(sender, "messages.global.only-player");
            return true;
        }
        Player p = (Player) sender;
        WarpManager wm = plugin.getWarpManager();

        // 1. Command: /warps (Buka GUI)
        if (label.equalsIgnoreCase("warps")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("edit")) {
                if (!p.hasPermission("naturalsmp.admin"))
                    return noPerm(p);
                gui.openGUI(p, true); // Editor Mode
            } else {
                gui.openGUI(p, false); // Normal Mode
            }
            return true;
        }

        // 2. Command: /setwarp <nama>
        if (label.equalsIgnoreCase("setwarp")) {
            if (!p.hasPermission("naturalsmp.admin"))
                return noPerm(p);
            if (args.length < 1) {
                ConfigUtils.sendMessage(p, "prefix.general", "messages.global.usage", "%usage%", "/setwarp <nama>");
                return true;
            }
            if (wm.getWarp(args[0]) != null) {
                p.sendMessage(ConfigUtils.getString("messages.home.home-exist")); // Reuse or add warp specific
                return true;
            }
            wm.createWarp(args[0], p.getLocation());
            ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-set", "%name%", args[0]);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            return true;
        }

        // 3. Command: /delwarp <nama>
        if (label.equalsIgnoreCase("delwarp")) {
            if (!p.hasPermission("naturalsmp.admin"))
                return noPerm(p);
            if (args.length == 0)
                return true;
            if (wm.getWarp(args[0]) == null) {
                ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-not-found", "%name%", args[0]);
                return true;
            }
            wm.deleteWarp(args[0]);
            ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-deleted", "%name%", args[0]);
            return true;
        }

        // 4. Command: /setwarpicon <nama> (Set icon ke item di tangan)
        if (label.equalsIgnoreCase("setwarpicon")) {
            if (!p.hasPermission("naturalsmp.admin"))
                return noPerm(p);
            if (args.length == 0)
                return true;
            Warp w = wm.getWarp(args[0]);
            if (w == null) {
                ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-not-found", "%name%", args[0]);
                return true;
            }
            Material hand = p.getInventory().getItemInMainHand().getType();
            if (hand == Material.AIR)
                hand = Material.GRASS_BLOCK;
            w.setIcon(hand);
            wm.saveWarps();
            ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-icon-set", "%name%", w.getId(), "%icon%",
                    hand.name());
            return true;
        }

        // 5. Command: /warp <nama> (Teleport)
        if (label.equalsIgnoreCase("warp")) {
            if (args.length == 0) {
                gui.openGUI(p, false); // Kalau cuma ketik /warp, buka GUI aja
                return true;
            }
            Warp w = wm.getWarp(args[0]);
            if (w == null) {
                String prefix = ConfigUtils.getString("prefix.warp");
                p.sendMessage(ChatUtils.colorize(
                        prefix + ConfigUtils.getString("messages.warp.warp-not-found").replace("%name%", args[0])));
                return true;
            }
            p.teleport(w.getLocation());
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

            String title = ConfigUtils.getString("messages.warp.teleporting-title")
                    .replace("%displayname%", w.getDisplayName());
            String subTitle = ConfigUtils.getString("messages.warp.teleporting-subtitle");

            p.sendTitle(title, subTitle, 0, 20, 10);
            return true;
        }

        return true;
    }

    private boolean noPerm(Player p) {
        ConfigUtils.sendGeneral(p, "messages.global.no-permission");
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String cmd = command.getName().toLowerCase();
            WarpManager wm = plugin.getWarpManager();

            if (cmd.equals("warp") || cmd.equals("delwarp") || cmd.equals("setwarpicon")) {
                List<String> suggestions = new ArrayList<>();
                for (Warp w : wm.getWarps()) {
                    suggestions.add(w.getId());
                }
                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (cmd.equals("warps")) {
                if ("edit".startsWith(args[0].toLowerCase()) && sender.hasPermission("naturalsmp.admin")) {
                    return java.util.Collections.singletonList("edit");
                }
            }
        }
        return java.util.Collections.emptyList();
    }
}