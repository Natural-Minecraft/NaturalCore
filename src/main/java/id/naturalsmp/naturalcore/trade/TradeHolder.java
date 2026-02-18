package id.naturalsmp.naturalcore.trade;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * TradeHolder - InventoryHolder untuk GUI Trade.
 * Menyimpan reference ke TradeSession dan shared Inventory.
 */
public class TradeHolder implements InventoryHolder {

    private final TradeSession session;
    private Inventory inventory;

    public TradeHolder(TradeSession session) {
        this.session = session;
    }

    public TradeSession getSession() {
        return session;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
