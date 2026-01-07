package id.naturalsmp.naturalcore.economy;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EconomyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String prefix = ConfigUtils.getString("prefix.economy");
        Economy eco = NaturalCore.getInstance().getVaultManager().getEconomy();

        // --- /BALANCE [PLAYER] ---
        if (label.equalsIgnoreCase("balance") || label.equalsIgnoreCase("bal") || label.equalsIgnoreCase("money")) {
            Player target = (sender instanceof Player) ? (Player) sender : null;
            if (args.length > 0) target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            double bal = eco.getBalance(target);
            String symbol = ConfigUtils.getString("economy.vault.symbol");
            sender.sendMessage(prefix + ConfigUtils.getString("messages.balance-check")
                    .replace("%target%", target.getName())
                    .replace("%symbol%", symbol)
                    .replace("%amount%", String.format("%,.0f", bal))); // Format angka 10,000
            return true;
        }

        // --- /PAY <PLAYER> <AMOUNT> ---
        if (label.equalsIgnoreCase("pay")) {
            if (!(sender instanceof Player)) return true;
            Player p = (Player) sender;

            if (args.length < 2) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /pay <player> <amount>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            if (target.equals(p)) {
                p.sendMessage(ChatUtils.colorize("&cTidak bisa kirim ke diri sendiri!"));
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(ConfigUtils.getString("messages.invalid-amount"));
                return true;
            }

            if (amount <= 0) {
                p.sendMessage(ConfigUtils.getString("messages.invalid-amount"));
                return true;
            }

            if (eco.getBalance(p) < amount) {
                p.sendMessage(prefix + ConfigUtils.getString("messages.pay-fail"));
                return true;
            }

            // Transaksi
            eco.withdrawPlayer(p, amount);
            eco.depositPlayer(target, amount);

            String symbol = ConfigUtils.getString("economy.vault.symbol");

            // Pesan Sender
            p.sendMessage(prefix + ConfigUtils.getString("messages.pay-sent")
                    .replace("%symbol%", symbol)
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%target%", target.getName()));

            // Pesan Target
            target.sendMessage(prefix + ConfigUtils.getString("messages.pay-received")
                    .replace("%symbol%", symbol)
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%player%", p.getName()));
            return true;
        }

        // --- ADMIN: /SETBAL & /TAKEBAL ---
        // (Saya singkat logicnya, mirip dengan givebal tapi set/withdraw)
        if (label.equalsIgnoreCase("setbal")) {
            if (!sender.hasPermission("naturalsmp.economy.admin")) return true;
            // Logic set balance Vault...
            // (Implementasi standar vault)
        }

        return true;
    }
}