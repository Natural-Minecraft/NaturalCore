package id.naturalsmp.naturalcore.trade;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class TradeHolder implements InventoryHolder {
    private final TradeSession session;

    public TradeHolder(TradeSession session) {
        this.session = session;
    }

    public TradeSession getSession() {
        return session;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
