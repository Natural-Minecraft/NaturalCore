package id.naturalsmp.naturalcore.warp;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
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

public class WarpCommand implements CommandExecutor {

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
            sender.sendMessage("Hanya player yang bisa menggunakan command ini.");
            return true;
        }
        Player p = (Player) sender;
        WarpManager wm = plugin.getWarpManager();

        // 1. Command: /warps (Buka GUI)
        if (label.equalsIgnoreCase("warps")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("edit")) {
                if (!p.hasPermission("naturalsmp.admin"))
                    return true;
                gui.openGUI(p, true); // Editor Mode
            } else {
                gui.openGUI(p, false); // Normal Mode
            }
            return true;
        }

        // 2. Command: /setwarp <nama>
        if (label.equalsIgnoreCase("setwarp")) {
            if (!p.hasPermission("naturalsmp.admin"))
                return true;
            if (args.length == 0) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /setwarp <nama>"));
                return true;
            }
            if (wm.getWarp(args[0]) != null) {
                p.sendMessage(ChatUtils.colorize("&cWarp '" + args[0] + "' sudah ada!"));
                return true;
            }
            wm.createWarp(args[0], p.getLocation());
            p.sendMessage(ChatUtils.colorize("&aWarp '&f" + args[0] + "&a' berhasil dibuat!"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            return true;
        }

        // 3. Command: /delwarp <nama>
        if (label.equalsIgnoreCase("delwarp")) {
            if (!p.hasPermission("naturalsmp.admin"))
                return true;
            if (args.length == 0)
                return true;
            if (wm.getWarp(args[0]) == null) {
                p.sendMessage(ChatUtils.colorize("&cWarp tidak ditemukan!"));
                return true;
            }
            wm.deleteWarp(args[0]);
            p.sendMessage(ChatUtils.colorize("&cWarp '&f" + args[0] + "&c' telah dihapus."));
            return true;
        }

        // 4. Command: /setwarpicon <nama> (Set icon ke item di tangan)
        if (label.equalsIgnoreCase("setwarpicon")) {
            if (!p.hasPermission("naturalsmp.admin"))
                return true;
            if (args.length == 0)
                return true;
            Warp w = wm.getWarp(args[0]);
            if (w == null) {
                p.sendMessage(ChatUtils.colorize("&cWarp tidak ditemukan!"));
                return true;
            }
            Material hand = p.getInventory().getItemInMainHand().getType();
            if (hand == Material.AIR)
                hand = Material.GRASS_BLOCK;
            w.setIcon(hand);
            wm.saveWarps();
            p.sendMessage(ChatUtils.colorize("&aIcon warp '&f" + w.getId() + "&a' diubah menjadi " + hand.name()));
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
                p.sendMessage(ChatUtils.colorize("&cWarp '&f" + args[0] + "&c' tidak ditemukan!"));
                return true;
            }
            p.teleport(w.getLocation());
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            p.sendTitle(ChatUtils.colorize(w.getDisplayName()), ChatUtils.colorize("&7Teleporting..."), 0, 20, 10);
            return true;
        }

        return true;
    }
}