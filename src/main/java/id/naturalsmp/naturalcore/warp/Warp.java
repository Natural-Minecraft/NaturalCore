package id.naturalsmp.naturalcore.warp;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

public class Warp {
    private final String id;
    private String displayName;
    private Location location;
    private Material icon;
    private int slot;
    private List<String> lore;

    public Warp(String id, Location location, int slot) {
        this.id = id;
        this.location = location;
        this.slot = slot;
        this.displayName = "&a" + id; // Default warna hijau
        this.icon = Material.ENDER_PEARL; // Default icon
        this.lore = List.of("&7Klik untuk teleport.");
    }

    // Constructor lengkap untuk load dari config
    public Warp(String id, String displayName, Location location, Material icon, int slot, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.location = location;
        this.icon = icon;
        this.slot = slot;
        this.lore = lore;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public Material getIcon() { return icon; }
    public void setIcon(Material icon) { this.icon = icon; }
    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
}