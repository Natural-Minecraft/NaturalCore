package id.naturalsmp.naturalcore.trader;

import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class TraderListener implements Listener {

    private final TraderManager traderManager;

    public TraderListener(TraderManager traderManager) {
        this.traderManager = traderManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Villager) {
            TraderData data = traderManager.getTraderByEntity(e.getEntity());
            if (data != null) {
                e.setCancelled(true);
            }
        }
    }

    // Optional: If we want to intercept interactions or do something special
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Villager) {
            TraderData data = traderManager.getTraderByEntity(e.getRightClicked());
            if (data != null) {
                // Logic if we wanted to open custom GUI, but vanilla GUI works fine for NoAI
                // villagers usually.
                // If the user reports issues, we can force open it here:
                // e.setCancelled(true);
                // e.getPlayer().openMerchant((Villager) e.getRightClicked(), true);
            }
        }
    }
}
