package id.naturalsmp.naturalcore.inventory;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InventoryCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya player yang bisa menggunakan command ini.");
            return true;
        }

        Player p = (Player) sender;
        String cmd = label.toLowerCase();
        String prefix = ConfigUtils.getString("prefix.admin");

        // --- 1. INVSEE (Intip Inventory) ---
        if (cmd.equals("invsee")) {
            if (!p.hasPermission("naturalsmp.invsee")) return noPerm(p);

            if (args.length < 1) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /invsee <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found"));
                return true;
            }

            p.openInventory(target.getInventory());
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
            p.sendMessage(prefix + ChatUtils.colorize("&7Membuka inventory &e" + target.getName()));
            return true;
        }

        // --- 2. ENDERSEE (Intip Enderchest Orang) ---
        if (cmd.equals("endersee") || cmd.equals("ecsee")) {
            if (!p.hasPermission("naturalsmp.endersee")) return noPerm(p);

            if (args.length < 1) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /endersee <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found"));
                return true;
            }

            p.openInventory(target.getEnderChest());
            p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
            p.sendMessage(prefix + ChatUtils.colorize("&7Membuka enderchest &e" + target.getName()));
            return true;
        }

        // --- 3. ENDERCHEST / EC (Buka Punya Sendiri) ---
        if (cmd.equals("enderchest") || cmd.equals("ec")) {
            if (!p.hasPermission("naturalsmp.enderchest")) return noPerm(p);

            // Logic simple: buka punya sendiri
            p.openInventory(p.getEnderChest());
            p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
            // Tidak perlu pesan, biar clean seperti essentials
            return true;
        }

        return true;
    }

    private boolean noPerm(Player p) {
        p.sendMessage(ConfigUtils.getString("messages.no-permission"));
        return true;
    }
}