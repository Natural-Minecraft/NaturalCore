package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.List;

public class GiveBalCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // 1. Cek Permission (Ambil pesan dari config)
        if (!sender.hasPermission("naturalcs.givebalance")) {
            sender.sendMessage(ConfigUtils.getString("messages.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatUtils.colorize("&cUsage: /givebal <player> <currency> <jumlah>"));
            return true;
        }

        // 2. Cek Target
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            String msg = ConfigUtils.getString("messages.player-not-found");
            sender.sendMessage(msg.replace("%player%", args[0]));
            return true;
        }

        // 3. Validasi Jumlah
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ConfigUtils.getString("messages.invalid-amount"));
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatUtils.colorize("&cJumlah harus lebih dari 0!"));
            return true;
        }

        // --- LOGIKA DINAMIS DARI CONFIG ---

        String inputCurrency = args[1].toLowerCase();

        // Ambil Data Config
        List<String> vaultAliases = ConfigUtils.getStringList("economy.vault.aliases");
        List<String> ceAliases = ConfigUtils.getStringList("economy.coins-engine.aliases");

        // A. CEK VAULT (RUPIAH)
        if (ConfigUtils.getBoolean("economy.vault.enabled") && vaultAliases.contains(inputCurrency)) {

            Economy eco = NaturalCore.getInstance().getVaultManager().getEconomy();
            if (eco == null) {
                sender.sendMessage(ChatUtils.colorize("&cError: Vault tidak terhubung!"));
                return true;
            }

            eco.depositPlayer(target, amount);

            // Kirim Pesan Sukses
            String symbol = ConfigUtils.getString("economy.vault.symbol");
            sendSuccessMessage(sender, target, symbol, amount);
            return true;
        }

        // B. CEK COINS ENGINE (NATURAL COIN)
        else if (ConfigUtils.getBoolean("economy.coins-engine.enabled") && ceAliases.contains(inputCurrency)) {

            if (!Bukkit.getPluginManager().isPluginEnabled("CoinsEngine")) {
                sender.sendMessage(ChatUtils.colorize("&cError: CoinsEngine plugin missing!"));
                return true;
            }

            // Ambil ID Currency dari Config (Biar gak hardcode "naturalcoin")
            String currencyId = ConfigUtils.getString("economy.coins-engine.currency-id");
            Currency currency = CoinsEngineAPI.getCurrency(currencyId);

            if (currency == null) {
                sender.sendMessage(ChatUtils.colorize("&cError: Currency ID '" + currencyId + "' tidak ditemukan di CoinsEngine!"));
                return true;
            }

            CoinsEngineAPI.addBalance(target, currency, amount);

            // Kirim Pesan Sukses
            String symbol = ConfigUtils.getString("economy.coins-engine.symbol");
            sendSuccessMessage(sender, target, symbol, amount);
            return true;
        }

        // C. JIKA TIDAK ADA YANG COCOK
        else {
            String msg = ConfigUtils.getString("messages.invalid-currency");
            // Gabungkan semua alias buat kasih tau user
            String allAliases = String.join(", ", vaultAliases) + ", " + String.join(", ", ceAliases);
            sender.sendMessage(msg.replace("%aliases%", allAliases));
        }

        return true;
    }

    private void sendSuccessMessage(CommandSender sender, Player target, String symbol, double amount) {
        String prefix = ConfigUtils.getString("prefix.economy");

        // Pesan ke Pengirim
        String senderMsg = ConfigUtils.getString("messages.give-success-sender")
                .replace("%symbol%", symbol)
                .replace("%amount%", String.valueOf(amount))
                .replace("%target%", target.getName());
        sender.sendMessage(prefix + senderMsg);

        // Pesan ke Penerima
        String targetMsg = ConfigUtils.getString("messages.give-success-target")
                .replace("%symbol%", symbol)
                .replace("%amount%", String.valueOf(amount));
        target.sendMessage(prefix + targetMsg);

        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
}