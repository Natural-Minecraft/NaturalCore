package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CurrencyManager {

    private final NaturalCore plugin;
    private final NamespacedKey currencyKey;

    public CurrencyManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.currencyKey = new NamespacedKey(plugin, "naturaltradercoin");
    }

    public ItemStack getCurrencyItem(int amount) {
        ItemStack item = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(ChatUtils.format("&6&lTrader Coins"));
            meta.lore(List.of(
                    ChatUtils.format("&7Digunakan untuk melakukan trade pada"),
                    ChatUtils.format("&7Wandering Trader.")
            ));

            meta.getPersistentDataContainer().set(currencyKey, PersistentDataType.BYTE, (byte) 1);
            
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isValidCurrency(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(currencyKey, PersistentDataType.BYTE);
    }

    public void giveCurrency(Player player, int amount) {
        ItemStack currency = getCurrencyItem(amount);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(currency);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), currency);
        }
        player.sendMessage(ChatUtils.format("&aReceived " + amount + " Quest Paper(s)!"));
    }
}
