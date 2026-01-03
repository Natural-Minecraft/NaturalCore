package id.naturalsmp.naturalcore.altar;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AltarManager {

    private final NaturalCore plugin;
    private final Map<String, AltarData> activeAltars;
    private Hologram altarHologram;
    
    // Multi-item donation system
    private final Map<Integer, ItemStack> requiredItems; // slot -> item
    private final Map<Integer, Integer> requiredAmounts; // slot -> amount needed
    private final Map<Integer, Integer> currentAmounts; // slot -> current amount
    private int itemCount = 0;
    private boolean altarActive = false;
    private Location triggerLocation;
    private Location hologramLocation;
    private Location warpLocation;
    private Location pos1;
    private Location pos2;
    private String targetWorld;
    
    public AltarManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.activeAltars = new HashMap<>();
        this.requiredItems = new HashMap<>();
        this.requiredAmounts = new HashMap<>();
        this.currentAmounts = new HashMap<>();
        
        loadAltarData();
    }
    
    private void loadAltarData() {
        // Load from config if exists
        if (plugin.getConfig().contains("altar.pos1")) {
            pos1 = (Location) plugin.getConfig().get("altar.pos1");
        }
        if (plugin.getConfig().contains("altar.pos2")) {
            pos2 = (Location) plugin.getConfig().get("altar.pos2");
        }
        if (plugin.getConfig().contains("altar.trigger")) {
            triggerLocation = (Location) plugin.getConfig().get("altar.trigger");
        }
        if (plugin.getConfig().contains("altar.hologram")) {
            hologramLocation = (Location) plugin.getConfig().get("altar.hologram");
        }
        if (plugin.getConfig().contains("altar.warp")) {
            warpLocation = (Location) plugin.getConfig().get("altar.warp");
        }
        if (plugin.getConfig().contains("altar.target_world")) {
            targetWorld = plugin.getConfig().getString("altar.target_world");
        }
    }
    
    public void saveAltarData() {
        plugin.getConfig().set("altar.pos1", pos1);
        plugin.getConfig().set("altar.pos2", pos2);
        plugin.getConfig().set("altar.trigger", triggerLocation);
        plugin.getConfig().set("altar.hologram", hologramLocation);
        plugin.getConfig().set("altar.warp", warpLocation);
        plugin.getConfig().set("altar.target_world", targetWorld);
        plugin.saveConfig();
    }
    
    // Setters
    public void setPos1(Location loc) {
        this.pos1 = loc;
        saveAltarData();
    }
    
    public void setPos2(Location loc) {
        this.pos2 = loc;
        saveAltarData();
    }
    
    public void setTriggerLocation(Location loc) {
        this.triggerLocation = loc;
        this.hologramLocation = loc.clone().add(0, 3.5, 0);
        saveAltarData();
    }
    
    public void setWarpLocation(Location loc) {
        this.warpLocation = loc;
        saveAltarData();
    }
    
    public void setTargetWorld(String world) {
        this.targetWorld = world;
        saveAltarData();
    }
    
    // Getters
    public Location getTriggerLocation() {
        return triggerLocation;
    }
    
    public Location getWarpLocation() {
        return warpLocation;
    }
    
    public String getTargetWorld() {
        return targetWorld;
    }
    
    public boolean isAltarActive() {
        return altarActive;
    }
    
    public boolean isInAltarZone(Location loc) {
        if (pos1 == null || pos2 == null) {
            return false;
        }
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
               loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
               loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }
    
    // Start altar with multiple items
    public void startAltar(List<ItemStack> items, List<Integer> amounts) {
        if (targetWorld == null) {
            plugin.getLogger().warning("Target world belum diset!");
            return;
        }
        
        if (items.size() != amounts.size()) {
            plugin.getLogger().warning("Item dan amount tidak match!");
            return;
        }
        
        // Clear previous data
        requiredItems.clear();
        requiredAmounts.clear();
        currentAmounts.clear();
        itemCount = items.size();
        
        // Set new requirements
        for (int i = 0; i < items.size(); i++) {
            requiredItems.put(i, items.get(i).clone());
            requiredAmounts.put(i, amounts.get(i));
            currentAmounts.put(i, 0);
        }
        
        altarActive = true;
        
        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &e&lGLOBAL QUEST DIMULAI!"));
        for (int i = 0; i < itemCount; i++) {
            String itemName = getItemName(requiredItems.get(i));
            Bukkit.broadcastMessage(ChatUtils.color("  &7- &b" + requiredAmounts.get(i) + " &fx &e" + itemName));
        }
        Bukkit.broadcastMessage("");
        
        updateHologram();
    }
    
    // Handle donation
    public void donate(org.bukkit.entity.Player player, ItemStack item) {
        if (!altarActive) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &7Altar sedang tidak menerima donasi."));
            return;
        }
        
        // Find matching required item
        int matchedSlot = -1;
        for (int i = 0; i < itemCount; i++) {
            if (isSameItem(item, requiredItems.get(i))) {
                if (currentAmounts.get(i) < requiredAmounts.get(i)) {
                    matchedSlot = i;
                    break;
                } else {
                    player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &aItem ini sudah terkumpul cukup!"));
                    player.playSound(player.getLocation(), "BLOCK_NOTE_BLOCK_BASS", 1f, 1f);
                    return;
                }
            }
        }
        
        if (matchedSlot == -1) {
            player.sendMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &cItem ini tidak dibutuhkan Altar!"));
            return;
        }
        
        // Add donated amount
        int amount = item.getAmount();
        currentAmounts.put(matchedSlot, currentAmounts.get(matchedSlot) + amount);
        
        // Remove from player inventory
        player.getInventory().removeItem(item);
        
        // Effects
        Location effectLoc = triggerLocation.clone().add(0.5, 0.5, 0.5);
        triggerLocation.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, effectLoc, 10, 0.3, 0.3, 0.3, 0.05);
        player.playSound(player.getLocation(), "BLOCK_AMETHYST_BLOCK_CHIME", 1f, 2f);
        player.sendActionBar(ChatUtils.color("&6&lALTAR: &a+" + amount + " Item diterima!"));
        
        updateHologram();
        checkCompletion();
    }
    
    private void checkCompletion() {
        int completed = 0;
        for (int i = 0; i < itemCount; i++) {
            if (currentAmounts.get(i) >= requiredAmounts.get(i)) {
                completed++;
            }
        }
        
        if (completed >= itemCount) {
            altarActive = false;
            
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatUtils.color("&6&l⛩ &x&F&F&D&7&0&0&lA&x&F&F&B&E&0&0&lL&x&F&F&A&6&0&0&lT&x&F&F&8&D&0&0&lA&x&F&F&7&5&0&0&lR &8» &d&lQUEST SELESAI! &b(100%)"));
            Bukkit.broadcastMessage(ChatUtils.color("  &fSemua item telah terkumpul!"));
            Bukkit.broadcastMessage(ChatUtils.color("  &fKetik &a/dungeon &funtuk masuk!"));
            Bukkit.broadcastMessage("");
            
            Bukkit.getOnlinePlayers().forEach(p -> 
                p.playSound(p.getLocation(), "UI_TOAST_CHALLENGE_COMPLETE", 1f, 1f)
            );
            
            updateHologram();
        }
    }
    
    public void updateHologram() {
        if (hologramLocation == null) {
            return;
        }
        
        // Delete old hologram
        if (altarHologram != null) {
            altarHologram.delete();
        }
        
        List<String> lines = new ArrayList<>();
        
        if (altarActive) {
            lines.add(ChatUtils.color("&6&l⛩ GLOBAL ALTAR ⛩"));
            lines.add(ChatUtils.color("&7Membuka: &c&lTHE LOST DUNGEON"));
            lines.add(ChatUtils.color("&f "));
            
            for (int i = 0; i < itemCount; i++) {
                String itemName = getItemName(requiredItems.get(i));
                int current = currentAmounts.get(i);
                int target = requiredAmounts.get(i);
                double percent = (double) current / target * 100;
                
                String statusColor = current >= target ? "&a&l✔" : "&e";
                lines.add(ChatUtils.color(String.format("&f%s %s: &f%d&7/&c%d &7(&b%.0f%%&7)", 
                    statusColor, itemName, current, target, percent)));
            }
            
            lines.add(ChatUtils.color("&f "));
            lines.add(ChatUtils.color("&7(Klik Kanan untuk Donasi)"));
        } else if (currentAmounts.get(0) != null && currentAmounts.get(0) > 0) {
            // Quest completed
            lines.add(ChatUtils.color("&b&l🔮 PORTAL TERBUKA 🔮"));
            lines.add(ChatUtils.color("&7Ketik &e/dungeon &7untuk masuk!"));
        }
        
        if (!lines.isEmpty()) {
            altarHologram = DHAPI.createHologram("global_altar", hologramLocation, lines);
        }
    }
    
    public void deleteAltar() {
        requiredItems.clear();
        requiredAmounts.clear();
        currentAmounts.clear();
        itemCount = 0;
        altarActive = false;
        
        if (altarHologram != null) {
            altarHologram.delete();
            altarHologram = null;
        }
    }
    
    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        
        if (item1.getType() != item2.getType()) {
            return false;
        }
        
        // Check if both have custom names
        boolean item1HasName = item1.hasItemMeta() && item1.getItemMeta().hasDisplayName();
        boolean item2HasName = item2.hasItemMeta() && item2.getItemMeta().hasDisplayName();
        
        if (item1HasName && item2HasName) {
            return item1.getItemMeta().getDisplayName().equals(item2.getItemMeta().getDisplayName());
        }
        
        return !item1HasName && !item2HasName;
    }
    
    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return ChatUtils.stripColor(item.getItemMeta().getDisplayName());
        }
        String typeName = item.getType().toString().toLowerCase().replace("_", " ");
        return Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
    }
    
    // Old methods for compatibility
    public void startAltar(String name, Location location) {
        plugin.getLogger().info("Legacy startAltar called. Use new multi-item version instead.");
    }
    
    public void stopAltar(String name) {
        deleteAltar();
    }
    
    public void stopAllAltars() {
        deleteAltar();
    }
    
    public boolean isAltarActive(String name) {
        return altarActive;
    }
    
    public AltarData getAltarData(String name) {
        return null; // Legacy, not used
    }
    
    // Inner class for legacy compatibility
    public static class AltarData {
        private final String name;
        private final Location location;
        
        public AltarData(String name, Location location) {
            this.name = name;
            this.location = location;
        }
        
        public String getName() {
            return name;
        }
        
        public Location getLocation() {
            return location;
        }
    }
}
