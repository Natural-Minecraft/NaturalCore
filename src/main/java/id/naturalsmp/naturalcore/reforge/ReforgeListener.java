package id.naturalsmp.naturalcore.reforge;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ReforgeListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        String title = ChatUtils.stripColor(event.getView().getTitle());
        
        if (!title.equals("Reforge Station")) {
            return;
        }
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        // Handle reforge button click
        if (clicked.getType() == Material.ANVIL) {
            player.sendMessage(ChatUtils.color("&a&l✔ &aItem berhasil di-reforge!"));
            player.sendMessage(ChatUtils.color("&7Stats baru: &f+" + (random.nextInt(10) + 1) + "% Damage"));
            player.closeInventory();
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        
        if (weapon == null || !weapon.hasItemMeta()) {
            return;
        }
        
        // Check for reforge stats and apply damage modifier
        // Ini bisa dikembangkan dengan NBT tags atau lore
    }
}
