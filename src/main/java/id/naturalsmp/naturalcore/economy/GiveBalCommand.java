package id.naturalsmp.naturalcore.economy;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveBalCommand implements CommandExecutor {

    private final VaultManager vaultManager;
    
    public GiveBalCommand(VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("naturalcore.admin.givebal")) {
            sender.sendMessage(ChatUtils.color("&cKamu tidak memiliki permission untuk command ini!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatUtils.color("&cUsage: /givebal <player> <amount>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatUtils.color("&cPlayer tidak ditemukan!"));
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatUtils.color("&cMasukkan angka yang valid!"));
            return true;
        }
        
        if (vaultManager.getEconomy() != null) {
            vaultManager.getEconomy().depositPlayer(target, amount);
            sender.sendMessage(ChatUtils.color("&a&l✔ &aBerhasil memberikan &f$" + amount + " &akepada &f" + target.getName()));
            target.sendMessage(ChatUtils.color("&a&l✔ &aKamu menerima &f$" + amount + " &adari admin!"));
        } else {
            sender.sendMessage(ChatUtils.color("&cVault economy tidak ditemukan!"));
        }
        
        return true;
    }
}
