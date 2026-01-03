package id.naturalsmp.naturalcore.reforge;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class ReforgeListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        if (event.getView() == null || event.getView().getTitle() == null) {
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
        
        if (weapon == null || weapon.getType() == Material.AIR) {
            return;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }
        
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }
        
        // Check for reforge stats and apply damage modifier
        // This can be expanded with NBT tags or advanced lore parsing
        for (String line : lore) {
            if (line != null && line.contains("Reforge")) {
                // Apply damage boost
                double currentDamage = event.getDamage();
                event.setDamage(currentDamage * 1.1); // 10% boost as example
                break;
            }
        }
    }
}
