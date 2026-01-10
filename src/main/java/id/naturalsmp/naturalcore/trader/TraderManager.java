package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TraderManager {

    private final NaturalCore plugin;
    private final File configFile;
    private FileConfiguration config;
    private final Map<String, TraderData> traders = new HashMap<>();

    // Cache entities by ID to avoid duplicates
    private final Map<String, UUID> spawnedEntities = new HashMap<>();

    public TraderManager(NaturalCore plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "trader.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("trader.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        traders.clear();

        if (config.contains("traders")) {
            for (String key : config.getConfigurationSection("traders").getKeys(false)) {
                ConfigurationSection sec = config.getConfigurationSection("traders." + key);
                if (sec == null)
                    continue;

                String name = sec.getString("name", "Trader");
                String worldName = sec.getString("location.world");
                double x = sec.getDouble("location.x");
                double y = sec.getDouble("location.y");
                double z = sec.getDouble("location.z");
                float yaw = (float) sec.getDouble("location.yaw");
                float pitch = (float) sec.getDouble("location.pitch");
                String prof = sec.getString("profession", "VILLAGER");

                if (Bukkit.getWorld(worldName) == null)
                    continue;
                Location loc = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);

                TraderData data = new TraderData(key, name, loc, prof);

                // Load trades
                List<MerchantRecipe> recipes = new ArrayList<>();
                if (sec.contains("trades")) {
                    for (Map<?, ?> tradeMap : sec.getMapList("trades")) {
                        // Simple parser - expecting Material:Amount
                        try {
                            ItemStack item1 = parseItem((String) tradeMap.get("item1"));
                            ItemStack result = parseItem((String) tradeMap.get("result"));
                            ItemStack item2 = tradeMap.containsKey("item2") ? parseItem((String) tradeMap.get("item2"))
                                    : null;
                            int maxUses = tradeMap.containsKey("max-uses") ? (Integer) tradeMap.get("max-uses")
                                    : 999999;

                            if (item1 != null && result != null) {
                                MerchantRecipe recipe = new MerchantRecipe(result, maxUses);
                                recipe.addIngredient(item1);
                                if (item2 != null)
                                    recipe.addIngredient(item2);
                                recipes.add(recipe);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error loading trade for " + key + ": " + e.getMessage());
                        }
                    }
                }
                data.setTrades(recipes);
                traders.put(key, data);
            }
        }
    }

    private ItemStack parseItem(String str) {
        if (str == null || str.isEmpty())
            return null;
        String[] parts = str.split(":");
        Material mat = Material.matchMaterial(parts[0]);
        if (mat == null)
            return null;
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        return new ItemStack(mat, amount);
    }

    public void saveConfig() {
        config.set("traders", null); // Clear old
        for (TraderData trader : traders.values()) {
            String path = "traders." + trader.getId();
            config.set(path + ".name", trader.getDisplayName());
            config.set(path + ".profession", trader.getProfession());

            Location loc = trader.getLocation();
            config.set(path + ".location.world", loc.getWorld().getName());
            config.set(path + ".location.x", loc.getX());
            config.set(path + ".location.y", loc.getY());
            config.set(path + ".location.z", loc.getZ());
            config.set(path + ".location.yaw", loc.getYaw());
            config.set(path + ".location.pitch", loc.getPitch());

            List<Map<String, Object>> tradeList = new ArrayList<>();
            for (MerchantRecipe r : trader.getTrades()) {
                Map<String, Object> map = new HashMap<>();
                map.put("item1", serializeItem(r.getIngredients().get(0)));
                if (r.getIngredients().size() > 1) {
                    map.put("item2", serializeItem(r.getIngredients().get(1)));
                }
                map.put("result", serializeItem(r.getResult()));
                map.put("max-uses", r.getMaxUses());
                tradeList.add(map);
            }
            config.set(path + ".trades", tradeList);
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String serializeItem(ItemStack item) {
        return item.getType().name() + ":" + item.getAmount();
    }

    public void spawnAll() {
        for (TraderData data : traders.values()) {
            spawnTrader(data);
        }
    }

    public void despawnAll() {
        for (UUID uuid : spawnedEntities.values()) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null)
                e.remove();
        }
        spawnedEntities.clear();

        // Safety: Remove any villager with specific tag in loaded chunks (optional, but
        // good for cleanup)
        // For now, we rely on tracking.
    }

    public void reload() {
        despawnAll();
        loadConfig();
        spawnAll();
    }

    public boolean createTrader(String id, String displayName, Location loc) {
        if (traders.containsKey(id))
            return false;
        TraderData data = new TraderData(id, displayName, loc, "VILLAGER");
        traders.put(id, data);
        spawnTrader(data);
        saveConfig();
        return true;
    }

    public boolean removeTrader(String id) {
        if (!traders.containsKey(id))
            return false;

        UUID uuid = spawnedEntities.remove(id);
        if (uuid != null) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null)
                e.remove();
        }

        traders.remove(id);
        saveConfig();
        return true;
    }

    public void spawnTrader(TraderData data) {
        // Remove existing if any (in case of re-spawn logic)
        if (spawnedEntities.containsKey(data.getId())) {
            Entity e = Bukkit.getEntity(spawnedEntities.get(data.getId()));
            if (e != null)
                e.remove();
        }

        Location loc = data.getLocation();
        if (loc.getWorld() == null)
            return;

        // Load chunk
        if (!loc.getChunk().isLoaded())
            loc.getChunk().load();

        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setCustomName(ChatUtils.colorize(data.getDisplayName()));
        villager.setCustomNameVisible(true);
        villager.setProfession(Villager.Profession.NITWIT); // Default, can be changed if we add profession support

        // Trades
        if (!data.getTrades().isEmpty()) {
            villager.setRecipes(data.getTrades());
        }

        spawnedEntities.put(data.getId(), villager.getUniqueId());
    }

    public TraderData getTraderByEntity(Entity entity) {
        for (Map.Entry<String, UUID> entry : spawnedEntities.entrySet()) {
            if (entry.getValue().equals(entity.getUniqueId())) {
                return traders.get(entry.getKey());
            }
        }
        return null;
    }

    public Collection<TraderData> getTraders() {
        return traders.values();
    }
}
