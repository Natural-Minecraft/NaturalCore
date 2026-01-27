package id.naturalsmp.naturalcore.utility;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CleanCommand implements CommandExecutor {

    private final NaturalCore plugin;

    public CleanCommand(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("ground")) {
            if (!sender.hasPermission("naturalsmp.admin")) {
                sender.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
                return true;
            }
            plugin.getLaggManager().startCleanup(10);
            sender.sendMessage(ChatUtils.colorize("&6&lNaturalCore &8» &7Memulai pembersihan item global..."));
            return true;
        }

        if (!(sender instanceof Player))
            return true;
        Player p = (Player) sender;

        if (!p.hasPermission("naturalsmp.clean")) {
            p.sendMessage(ConfigUtils.getString("messages.global.no-permission"));
            return true;
        }

        // Simpan Armor
        ItemStack[] armor = p.getInventory().getArmorContents();

        // Clear Semua (termasuk offhand & armor)
        p.getInventory().clear();

        // Restore Armor
        p.getInventory().setArmorContents(armor);

        String prefix = ConfigUtils.getString("prefix.player");
        p.sendMessage(ChatUtils.colorize(prefix + ConfigUtils.getString("messages.utils.clean-success")));
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1.2f);

        return true;
    }
}
