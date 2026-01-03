package id.naturalsmp.naturalcore.altar;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AltarListener implements Listener {

    private final AltarManager altarManager;
    
    public AltarListener(AltarManager altarManager) {
        this.altarManager = altarManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        
        ItemStack item = event.getItem();
        
        // Handle Altar Wand
        if (item != null && item.getType() == Material.GOLDEN_HOE) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatUtils.stripColor(meta.getDisplayName());
                
                if (displayName.equals("Altar Wand")) {
                    event.setCancelled(true);
                    
                    Block clickedBlock = event.getClickedBlock();
                    if (clickedBlock == null) {
                        return;
                    }
                    
                    Location blockLoc = clickedBlock.getLocation();
                    
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        // Set Pos1
                        altarManager.setPos1(blockLoc);
                        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &bPosisi 1 &7disimpan: &e" + 
                            blockLoc.getBlockX() + ", " + 
                            blockLoc.getBlockY() + ", " + 
                            blockLoc.getBlockZ()));
                        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        // Set Pos2
                        altarManager.setPos2(blockLoc);
                        player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &bPosisi 2 &7disimpan: &e" + 
                            blockLoc.getBlockX() + ", " + 
                            blockLoc.getBlockY() + ", " + 
                            blockLoc.getBlockZ()));
                        player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", 1f, 1f);
                    }
                    return;
                }
            }
        }
        
        // Handle Altar Donation
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && altarManager.getTriggerLocation() != null) {
                Location triggerLoc = altarManager.getTriggerLocation();
                Location clickedLoc = clickedBlock.getLocation();
                
                if (clickedLoc.equals(triggerLoc)) {
                    event.setCancelled(true);
                    
                    if (item != null && item.getType() != Material.AIR) {
                        altarManager.donate(player, item);
                    }
                }
            }
        }
    }
}
