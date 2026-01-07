package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CurrencyManager {

    private final NaturalCore plugin;

    // --- [TAMBAHAN BARU] CONSTRUCTOR ---
    // Ini yang bikin error sebelumnya hilang.
    public CurrencyManager(NaturalCore plugin) {
        this.plugin = plugin;
    }

    // --- STATIC HELPER METHODS ---

    public static String getSymbol(boolean useCoinsEngine) {
        if (useCoinsEngine) {
            return ConfigUtils.getString("economy.coins-engine.symbol");
        } else {
            return ConfigUtils.getString("economy.vault.symbol");
        }
    }

    public static String formatPrice(double amount, boolean useCoinsEngine) {
        String symbol = getSymbol(useCoinsEngine);
        return ChatUtils.colorize("&a" + symbol + " " + ChatUtils.format(amount));
    }

    public static boolean hasMoney(Player p, double amount, boolean useCoinsEngine) {
        if (useCoinsEngine) {
            return true; // Placeholder CoinsEngine
        } else {
            return NaturalCore.getInstance().getVaultManager().getEconomy().has(p, amount);
        }
    }

    public static void takeMoney(Player p, double amount, boolean useCoinsEngine) {
        if (useCoinsEngine) {
            // Placeholder CoinsEngine
        } else {
            NaturalCore.getInstance().getVaultManager().getEconomy().withdrawPlayer(p, amount);
        }
    }

    public static ItemStack addPriceToItem(ItemStack item, double price, boolean useCoinsEngine) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = new ArrayList<>();

        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
        }

        lore.add("");
        lore.add(ChatUtils.colorize("&7Harga: " + formatPrice(price, useCoinsEngine)));
        lore.add(ChatUtils.colorize("&eKlik untuk membeli!"));

        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}