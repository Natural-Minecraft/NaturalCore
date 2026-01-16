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

        String prefix = ConfigUtils.getString("prefix.player");
        // --- /TRASH ---
        if (label.equalsIgnoreCase("trash")) {
            if (!p.hasPermission("naturalsmp.trash")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }

            p.openInventory(
                    Bukkit.createInventory(null, 36, ChatUtils.colorize("&cTrash Can (Items will be deleted)")));
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.trash-opened")));
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 1f);
            return true;
        }

        // --- /CRAFT ---
        if (label.equalsIgnoreCase("craft") || label.equalsIgnoreCase("wb")) {
            if (!p.hasPermission("naturalsmp.craft")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            p.openWorkbench(null, true);
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.craft-opened")));
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
            return true;
        }

        // --- /ANVIL ---
        if (label.equalsIgnoreCase("anvil") || label.equalsIgnoreCase("av")) {
            if (!p.hasPermission("naturalsmp.anvil")) {
                p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            // Membuka Anvil Virtual (Bukan GUI custom, tapi native inventory)
            p.openInventory(Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.ANVIL));
            p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.anvil-opened")));
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1f); // Use with care, loud
            return true;
        }

        return true;
    }
}