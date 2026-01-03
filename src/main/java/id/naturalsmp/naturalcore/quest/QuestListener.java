package id.naturalsmp.naturalcore.quest;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class QuestListener implements Listener {

    private final QuestManager questManager;
    
    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    // Handle NPC Click
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc == null || npc.getEntity() == null) {
            return;
        }
        
        Entity npcEntity = npc.getEntity();
        if (!CitizensAPI.getNPCRegistry().isNPC(npcEntity)) {
            return;
        }
        
        Player player = event.getClicker();
        if (player == null) {
            return;
        }
        
        java.util.UUID npcUUID = npcEntity.getUniqueId();
        
        // Check if this is quest NPC
        if (npcUUID.equals(questManager.getNPCPenagihUUID())) {
            event.setCancelled(true);
            questManager.handlePenagihDialog(player);
        } else if (npcUUID.equals(questManager.getNPCPetaniUUID())) {
            String stage = questManager.getQuestStage(player);
            if (stage.equals("started")) {
                event.setCancelled(true);
                questManager.handlePetaniDialog(player);
            }
            // Jika bukan stage "started", biarkan Citizens handle normal
        }
    }
    
    // Prevent dropping quest item
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item == null) {
            return;
        }
        
        if (questManager.isQuestItem(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatUtils.color("&6&l📜 &x&F&F&C&E&4&7&lS&x&F&F&B&C&4&2&lT&x&F&F&A&B&3&D&lO&x&F&F&9&9&3&8&lR&x&F&F&8&8&3&3&lY &8» &cItem Quest tidak bisa dibuang!"));
            event.getPlayer().playSound(event.getPlayer().getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
        }
    }
    
    // Prevent storing quest item in chests/containers
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        
        // Check clicked item
        if (clickedItem != null && questManager.isQuestItem(clickedItem)) {
            if (event.getClickedInventory() != null && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                player.sendMessage(ChatUtils.color("&6&l📜 &x&F&F&C&E&4&7&lS&x&F&F&B&C&4&2&lT&x&F&F&A&B&3&D&lO&x&F&F&9&9&3&8&lR&x&F&F&8&8&3&3&lY &8» &cItem Quest tidak bisa disimpan di sini!"));
                player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            }
        }
        
        // Check cursor item
        if (cursorItem != null && questManager.isQuestItem(cursorItem)) {
            if (event.getClickedInventory() != null && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                player.sendMessage(ChatUtils.color("&6&l📜 &x&F&F&C&E&4&7&lS&x&F&F&B&C&4&2&lT&x&F&F&A&B&3&D&lO&x&F&F&9&9&3&8&lR&x&F&F&8&8&3&3&lY &8» &cItem Quest tidak bisa disimpan di sini!"));
                player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
            }
        }
    }
    
    // Remove quest item from drops on death
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item != null && questManager.isQuestItem(item)) {
                iterator.remove();
            }
        }
    }
    
    // Return quest item on respawn if player still has quest active
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.Bukkit.getPluginManager().getPlugin("NaturalCore"),
            () -> {
                if (player.isOnline()) {
                    questManager.returnQuestItem(player);
                }
            },
            20L
        );
    }
    
    // Clean up dialog cooldown on quit
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        questManager.endDialog(event.getPlayer());
    }
}
