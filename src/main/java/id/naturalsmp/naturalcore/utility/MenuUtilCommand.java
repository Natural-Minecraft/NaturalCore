package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuUtilCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only Player");
            return true;
        }
        Player p = (Player) sender;

        // --- /TRASH ---
        if (label.equalsIgnoreCase("trash")) {
            // Trash biasanya tidak butuh permission (semua orang bisa buang barang)
            // Tapi jika ingin restrict, uncomment baris berikut:
            // if (!p.hasPermission("naturalsmp.trash")) {
            // p.sendMessage(ConfigUtils.getString("messages.no-permission"));
            // return true;
            // }

            p.openInventory(
                    Bukkit.createInventory(null, 36, ChatUtils.colorize("&cTrash Can (Items will be deleted)")));
            return true;
        }

        // --- /CRAFT ---
        if (label.equalsIgnoreCase("craft") || label.equalsIgnoreCase("wb")) {
            if (!p.hasPermission("naturalsmp.craft")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            p.openWorkbench(null, true);
            return true;
        }

        // --- /ANVIL ---
        if (label.equalsIgnoreCase("anvil") || label.equalsIgnoreCase("av")) {
            if (!p.hasPermission("naturalsmp.anvil")) {
                p.sendMessage(ConfigUtils.getString("messages.no-permission"));
                return true;
            }
            // Membuka Anvil Virtual (Bukan GUI custom, tapi native inventory)
            p.openInventory(Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.ANVIL));
            return true;
        }

        return true;
    }
}