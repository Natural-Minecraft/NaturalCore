package id.naturalsmp.naturalcore.altar;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AltarListener implements Listener {

    private final AltarManager altarManager;
    
    public AltarListener(AltarManager altarManager) {
        this.altarManager = altarManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Handle Altar Wand
        if (item != null && item.getType() == Material.GOLDEN_HOE) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = ChatUtils.stripColor(item.getItemMeta().getDisplayName());
                
                if (displayName.equals("Altar Wand")) {
                    event.setCancelled(true);
                    
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        // Set Pos1
                        altarManager.setPos1(event.getClickedBlock().getLocation());
                        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &bPosisi 1 &7disimpan: &e" + 
                            event.getClickedBlock().getX() + ", " + 
                            event.getClickedBlock().getY() + ", " + 
                            event.getClickedBlock().getZ()));
                        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        // Set Pos2
                        altarManager.setPos2(event.getClickedBlock().getLocation());
                        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &bPosisi 2 &7disimpan: &e" + 
                            event.getClickedBlock().getX() + ", " + 
                            event.getClickedBlock().getY() + ", " + 
                            event.getClickedBlock().getZ()));
                        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
                    }
                    return;
                }
            }
        }
        
        // Handle Altar Donation
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null && 
                altarManager.getTriggerLocation() != null &&
                event.getClickedBlock().getLocation().equals(altarManager.getTriggerLocation())) {
                
                event.setCancelled(true);
                
                if (item != null && item.getType() != Material.AIR) {
                    altarManager.donate(player, item);
                }
            }
        }
    }
}
