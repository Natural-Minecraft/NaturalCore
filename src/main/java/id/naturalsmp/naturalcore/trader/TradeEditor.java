package id.naturalsmp.naturalcore.trader;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TradeEditor implements Listener {

    private final NaturalCore plugin;
    private final File traderFile;
    private FileConfiguration traderConfig;
    private final String GUI_TITLE = ChatUtils.colorize("&8Matrix Trade Editor");

    public TradeEditor(NaturalCore plugin) {
        this.plugin = plugin;
        this.traderFile = new File(plugin.getDataFolder(), "trader.yml");
        loadTrades();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadTrades() {
        if (!traderFile.exists()) {
            try {
                traderFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create trader.yml!");
                e.printStackTrace();
            }
        }
        traderConfig = YamlConfiguration.loadConfiguration(traderFile);
    }

    public void openEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Fill background
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(ChatUtils.colorize(" "));
            filler.setItemMeta(fillerMeta);
        }

        // Fill rows 3 and 4 with filler (Slots 27-44)
        for (int i = 27; i < 45; i++) {
            inv.setItem(i, filler);
        }
        
        // Fill row 5 except save button (Slots 45-53)
        for (int i = 45; i < 54; i++) {
            if (i != 49) inv.setItem(i, filler);
        }

        // Load existing trades into GUI
        ConfigurationSection section = traderConfig.getConfigurationSection("trades");
        if (section != null) {
            int col = 0;
            for (String key : section.getKeys(false)) {
                if (col >= 9) break; // Max 9 trades in this editor version
                
                ItemStack cost1 = section.getItemStack(key + ".cost1");
                ItemStack cost2 = section.getItemStack(key + ".cost2");
                ItemStack result = section.getItemStack(key + ".result");

                if (cost1 != null) inv.setItem(col, cost1); // Row 0
                if (cost2 != null) inv.setItem(col + 9, cost2); // Row 1
                if (result != null) inv.setItem(col + 18, result); // Row 2
                
                col++;
            }
        }

        // Save Button at Slot 49
        inv.setItem(49, createGuiItem(Material.LIME_STAINED_GLASS_PANE, "&a&lSAVE TRADES", "&7Click to validate and save all columns"));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            meta.setLore(List.of(ChatUtils.colorize(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        // Allow clicking in top inventory (editor) only in valid slots
        // Rows 0, 1, 2 are editable. Others are locked.
        int slot = event.getRawSlot();
        if (slot < 54) {
            if (slot == 49) { // Save button
                event.setCancelled(true);
                handleSave((Player) event.getWhoClicked(), event.getInventory());
            } else if (slot >= 27) { // Filler area
                event.setCancelled(true);
            }
        }
    }

    private void handleSave(Player player, Inventory inv) {
        // Clear existing config
        traderConfig.set("trades", null);
        
        int tradeCount = 0;

        // Iterate columns 0-8
        for (int col = 0; col < 9; col++) {
            ItemStack cost1 = inv.getItem(col);      // Row 0
            ItemStack cost2 = inv.getItem(col + 9);  // Row 1
            ItemStack result = inv.getItem(col + 18); // Row 2

            boolean hasCost1 = isValidItem(cost1);
            boolean hasResult = isValidItem(result);

            // Fail-Safe 1: Result exists but no Cost
            if (hasResult && !hasCost1) {
                player.sendMessage(ChatUtils.colorize("&c&lERROR in Column " + (col + 1) + ": &7You cannot sell an item without a cost!"));
                player.closeInventory();
                return; // Cancel save completely
            }

            // Fail-Safe 2: Cost exists but no Result
            if (hasCost1 && !hasResult) {
                player.sendMessage(ChatUtils.colorize("&c&lERROR in Column " + (col + 1) + ": &7You specified a cost but no product!"));
                player.closeInventory();
                return; // Cancel save completely
            }

            // Success: Save valid trade
            if (hasCost1 && hasResult) {
                tradeCount++;
                String path = "trades." + tradeCount;
                traderConfig.set(path + ".cost1", cost1);
                if (isValidItem(cost2)) {
                    traderConfig.set(path + ".cost2", cost2);
                }
                traderConfig.set(path + ".result", result);
            }
        }

        try {
            traderConfig.save(traderFile);
            player.sendMessage(ChatUtils.colorize("&aSuccessfully saved " + tradeCount + " trades!"));
            player.closeInventory();
        } catch (IOException e) {
            player.sendMessage(ChatUtils.colorize("&cFailed to save trades to file!"));
            e.printStackTrace();
        }
    }

    private boolean isValidItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public List<MerchantRecipe> getRecipes() {
        List<MerchantRecipe> recipes = new ArrayList<>();
        ConfigurationSection section = traderConfig.getConfigurationSection("trades");
        
        if (section == null) return recipes;

        for (String key : section.getKeys(false)) {
            ItemStack cost1 = section.getItemStack(key + ".cost1");
            ItemStack cost2 = section.getItemStack(key + ".cost2");
            ItemStack result = section.getItemStack(key + ".result");

            if (cost1 != null && result != null) {
                MerchantRecipe recipe = new MerchantRecipe(result, 9999); // Infinite uses
                recipe.addIngredient(cost1);
                if (cost2 != null) {
                    recipe.addIngredient(cost2);
                }
                recipes.add(recipe);
            }
        }
        return recipes;
    }
}
