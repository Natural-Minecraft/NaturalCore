package id.naturalsmp.naturalcore.trader;

import org.bukkit.Location;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class TraderData {
    private final String id;
    private String displayName;
    private Location location;
    private List<MerchantRecipe> trades;
    private String profession; // e.g. FARMER, LIBRARIAN

    public TraderData(String id, String displayName, Location location, String profession) {
        this.id = id;
        this.displayName = displayName;
        this.location = location;
        this.profession = profession;
        this.trades = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public List<MerchantRecipe> getTrades() {
        return trades;
    }

    public void setTrades(List<MerchantRecipe> trades) {
        this.trades = trades;
    }
}
