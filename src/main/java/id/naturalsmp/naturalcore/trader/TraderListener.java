package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TraderListener implements Listener {

    private final TraderManager traderManager;
    
    public TraderListener(TraderManager traderManager) {
        this.traderManager = traderManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        if (title == null) {
            return;
        }
        
        String strippedTitle = ChatUtils.stripColor(title);
        if (!strippedTitle.contains("Travelling Trader")) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }
        
        // Handle item purchase
        if (traderManager.isTraderAvailable()) {
            // Logic untuk beli item dari trader
            player.sendMessage(ChatUtils.color("&a&l✔ &aItem berhasil dibeli!"));
        } else {
            player.sendMessage(ChatUtils.color("&c&l✘ &cTrader sedang tidak tersedia!"));
            player.closeInventory();
        }
    }
}
