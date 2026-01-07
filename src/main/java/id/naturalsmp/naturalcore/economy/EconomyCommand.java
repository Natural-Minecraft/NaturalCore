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
        String cmd = label.toLowerCase();
        String symbol = ConfigUtils.getString("economy.vault.symbol");

        // --- /BALANCE ---
        if (cmd.equals("balance") || cmd.equals("bal") || cmd.equals("money")) {
            Player target = (sender instanceof Player) ? (Player) sender : null;
            if (args.length > 0) target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            double bal = eco.getBalance(target);
            sender.sendMessage(prefix + ConfigUtils.getString("messages.balance-check")
                    .replace("%target%", target.getName())
                    .replace("%symbol%", symbol)
                    .replace("%amount%", ChatUtils.format(bal)));
            return true;
        }

        // --- /SETBAL <PLAYER> <AMOUNT> ---
        if (cmd.equals("setbal")) {
            if (!sender.hasPermission("naturalsmp.economy.admin")) return noPerm(sender);
            if (args.length < 2) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /setbal <player> <amount>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            double amount;
            try { amount = Double.parseDouble(args[1]); }
            catch (Exception e) { sender.sendMessage(ConfigUtils.getString("messages.invalid-amount")); return true; }

            // Logic Set: Reset ke 0 lalu deposit
            double current = eco.getBalance(target);
            eco.withdrawPlayer(target, current);
            eco.depositPlayer(target, amount);

            sender.sendMessage(prefix + ConfigUtils.getString("messages.balance-set")
                    .replace("%target%", target.getName())
                    .replace("%symbol%", symbol)
                    .replace("%amount%", ChatUtils.format(amount)));
            return true;
        }

        // --- /TAKEBAL <PLAYER> <AMOUNT> ---
        if (cmd.equals("takebal")) {
            if (!sender.hasPermission("naturalsmp.economy.admin")) return noPerm(sender);
            if (args.length < 2) {
                sender.sendMessage(ChatUtils.colorize("&cUsage: /takebal <player> <amount>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            double amount;
            try { amount = Double.parseDouble(args[1]); }
            catch (Exception e) { sender.sendMessage(ConfigUtils.getString("messages.invalid-amount")); return true; }

            eco.withdrawPlayer(target, amount);

            sender.sendMessage(prefix + ConfigUtils.getString("messages.balance-take")
                    .replace("%target%", target.getName())
                    .replace("%symbol%", symbol)
                    .replace("%amount%", ChatUtils.format(amount)));
            return true;
        }

        // --- /PAY ---
        if (cmd.equals("pay")) {
            if (!(sender instanceof Player)) return true;
            Player p = (Player) sender;
            if (args.length < 2) {
                p.sendMessage(ChatUtils.colorize("&cUsage: /pay <player> <amount>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || target.equals(p)) {
                p.sendMessage(ConfigUtils.getString("messages.player-not-found").replace("%player%", args[0]));
                return true;
            }

            double amount;
            try { amount = Double.parseDouble(args[1]); }
            catch (Exception e) { p.sendMessage(ConfigUtils.getString("messages.invalid-amount")); return true; }

            if (amount <= 0 || !eco.has(p, amount)) {
                p.sendMessage(prefix + ConfigUtils.getString("messages.pay-fail"));
                return true;
            }

            eco.withdrawPlayer(p, amount);
            eco.depositPlayer(target, amount);

            p.sendMessage(prefix + ConfigUtils.getString("messages.pay-sent")
                    .replace("%symbol%", symbol).replace("%amount%", ChatUtils.format(amount)).replace("%target%", target.getName()));
            target.sendMessage(prefix + ConfigUtils.getString("messages.pay-received")
                    .replace("%symbol%", symbol).replace("%amount%", ChatUtils.format(amount)).replace("%player%", p.getName()));
            return true;
        }

        return true;
    }

    private boolean noPerm(CommandSender s) {
        s.sendMessage(ConfigUtils.getString("messages.no-permission"));
        return true;
    }
}