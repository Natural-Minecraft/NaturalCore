package id.naturalsmp.naturalcore.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    
    /**
     * Create ItemBuilder from Material
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }
    
    /**
     * Create ItemBuilder from Material with amount
     */
    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }
    
    /**
     * Create ItemBuilder from existing ItemStack
     */
    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = item.getItemMeta();
    }
    
    /**
     * Set display name
     */
    public ItemBuilder setName(String name) {
        if (meta != null) {
            meta.setDisplayName(ChatUtils.color(name));
        }
        return this;
    }
    
    /**
     * Set lore (varargs)
     */
    public ItemBuilder setLore(String... lore) {
        if (meta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatUtils.color(line));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }
    
    /**
     * Set lore (list)
     */
    public ItemBuilder setLore(List<String> lore) {
        if (meta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatUtils.color(line));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }
    
    /**
     * Add lore line
     */
    public ItemBuilder addLoreLine(String line) {
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(ChatUtils.color(line));
            meta.setLore(lore);
        }
        return this;
    }
    
    /**
     * Add lore lines
     */
    public ItemBuilder addLoreLines(String... lines) {
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            for (String line : lines) {
                lore.add(ChatUtils.color(line));
            }
            meta.setLore(lore);
        }
        return this;
    }
    
    /**
     * Set amount
     */
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }
    
    /**
     * Add enchantment
     */
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }
    
    /**
     * Remove enchantment
     */
    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        if (meta != null) {
            meta.removeEnchant(enchantment);
        }
        return this;
    }
    
    /**
     * Add item flags
     */
    public ItemBuilder addItemFlags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }
    
    /**
     * Set unbreakable
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }
    
    /**
     * Make item glow (adds fake enchantment)
     */
    public ItemBuilder setGlow(boolean glow) {
        if (meta != null && glow) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }
    
    /**
     * Hide all item flags
     */
    public ItemBuilder hideAllFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }
    
    /**
     * Set custom model data
     */
    public ItemBuilder setCustomModelData(int data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }
    
    /**
     * Build the final ItemStack
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Get the ItemStack (alias for build)
     */
    public ItemStack toItemStack() {
        return build();
    }
}
