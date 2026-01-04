package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class TraderListener implements Listener {

    private final TraderManager traderManager;
    private final TradeEditor tradeEditor;

    public TraderListener(TraderManager traderManager, TradeEditor tradeEditor) {
        this.traderManager = traderManager;
        this.tradeEditor = tradeEditor;
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        if (traderManager.getCurrentTrader() == null) return;

        // Check if the clicked NPC is our Special Trader
        if (event.getNPC().getUniqueId().equals(traderManager.getCurrentTrader().getUniqueId())) {
            event.setCancelled(true); // Prevent default Citizens interaction if necessary
            
            Player player = event.getClicker();
            openTradeMenu(player);
        }
    }

    private void openTradeMenu(Player player) {
        List<MerchantRecipe> recipes = tradeEditor.getRecipes();
        
        if (recipes.isEmpty()) {
            player.sendMessage(ChatUtils.format("&cThe trader has no items to sell right now."));
            return;
        }

        Merchant merchant = Bukkit.createMerchant(ChatUtils.format("&6&lSpecial Trader"));
        merchant.setRecipes(recipes);
        
        player.openMerchant(merchant, true);
    }
}
