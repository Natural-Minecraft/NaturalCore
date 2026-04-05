package id.naturalsmp.naturalcore.lang;

import id.naturalsmp.naturalcore.NaturalCore;
import org.bukkit.Bukkit;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LanguageGUI implements Listener {

    private final NaturalCore plugin;

    public LanguageGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        String currentLang = plugin.getLanguageManager().getLanguage(p.getUniqueId());
        
        // Translated title for GUI
        String title = "Language / Bahasa";
        Inventory inv = GUIUtils.createGUI(new LanguageHolder(), 27, title);

        ItemStack filler = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        // Indonesia
        inv.setItem(11, createLangItem(
            Material.RED_BANNER, 
            "&c&lBahasa Indonesia", 
            currentLang.equals("id"), 
            "&7Pilih ini untuk menggunakan", "&7Bahasa Indonesia dalam server."
        ));

        // English
        inv.setItem(15, createLangItem(
            Material.BLUE_BANNER, 
            "&9&lEnglish Language", 
            currentLang.equals("en"), 
            "&7Select this to use", "&7English within the server."
        ));

        p.openInventory(inv);
    }

    private ItemStack createLangItem(Material mat, String name, boolean selected, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(net.kyori.adventure.text.Component.empty());
            for (String line : loreLines) {
                lore.add(ChatUtils.toComponent(line));
            }
            lore.add(net.kyori.adventure.text.Component.empty());
            if (selected) {
                lore.add(ChatUtils.toComponent("&a&l[ SELECTED / DIPILIH ]"));
            } else {
                lore.add(ChatUtils.toComponent("&e> Klik untuk memilih <"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof LanguageHolder)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        Player p = (Player) e.getWhoClicked();
        int slot = e.getSlot();

        String currentLang = plugin.getLanguageManager().getLanguage(p.getUniqueId());

        if (slot == 11) {
            if (!currentLang.equals("id")) {
                plugin.getLanguageManager().setLanguage(p, "id");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                p.sendMessage(ChatUtils.colorize("&a[Language] Bahasa kamu telah diubah menjadi Indonesia!"));
                p.closeInventory();
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        } else if (slot == 15) {
            if (!currentLang.equals("en")) {
                plugin.getLanguageManager().setLanguage(p, "en");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                p.sendMessage(ChatUtils.colorize("&a[Language] Your language has been changed to English!"));
                p.closeInventory();
            } else {
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    public static class LanguageHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return Bukkit.createInventory(this, 27);
        }
    }
}
