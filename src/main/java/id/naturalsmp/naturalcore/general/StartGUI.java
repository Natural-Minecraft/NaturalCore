package id.naturalsmp.naturalcore.general;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StartGUI implements Listener {

    private final NaturalCore plugin;

    public StartGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String title = ConfigUtils.getString("messages.gui.start.title");
        Inventory inv = GUIUtils.createGUI(new StartHolder(), 9, title);

        // Fill background with grey glass
        ItemStack filler = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++)
            inv.setItem(i, filler);

        // 1. Dungeon (Slot 2)
        ItemStack dungeon = new ItemStack(Material.DIAMOND_SWORD);
        dungeon.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        ItemMeta dMeta = dungeon.getItemMeta();
        dMeta.displayName(ChatUtils.toComponent("&#FF5555&l⚔ DUNGEON"));
        List<Component> dLore = new ArrayList<>();
        dLore.add(ChatUtils.toComponent("&7Uji keberanianmu di dunia"));
        dLore.add(ChatUtils.toComponent("&7penuh monster dan loot berharga!"));
        dLore.add(Component.empty());
        dLore.add(ChatUtils.toComponent("&#FFAA00&l➥ KLIK UNTUK MASUK"));
        dMeta.lore(dLore);
        dMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        dungeon.setItemMeta(dMeta);
        inv.setItem(2, dungeon);

        // 2. Survival (Slot 4)
        inv.setItem(4, createItem(Material.GRASS_BLOCK, "&#55FF55&l🌳 SURVIVAL",
                "&7Mulailah petualanganmu di", "&7dunia survival utama.", "", "&#FFAA00&l➥ KLIK UNTUK MASUK"));

        // 3. Resource (Slot 6)
        inv.setItem(6, createItem(Material.DIAMOND_HOE, "&#FFFF55&l⛏ RESOURCE",
                "&7Dunia khusus untuk mencari", "&7material tanpa merusak survival.", "",
                "&#FFAA00&l➥ KLIK UNTUK MASUK"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.2f);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> l = new ArrayList<>();
        for (String s : lore)
            l.add(ChatUtils.toComponent(s));
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof StartHolder))
            return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

        switch (e.getRawSlot()) {
            case 2 -> p.performCommand("dungeon");
            case 4 -> p.performCommand("rtp");
            case 6 -> p.performCommand("rsc");
        }
    }

    public static class StartHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
