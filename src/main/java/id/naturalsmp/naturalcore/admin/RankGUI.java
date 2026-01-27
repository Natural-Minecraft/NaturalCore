package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class RankGUI implements Listener {

    private final NaturalCore plugin;

    public RankGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        Inventory inv = Bukkit.createInventory(null, 27, ChatUtils.colorize(ConfigUtils.getMessage("ranks.gui-title")));

        // --- GLASS BORDER ---
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // --- RANK ITEMS ---
        inv.setItem(10, createRankItem(Material.LAPIS_LAZULI, "ranks.midi"));
        inv.setItem(12, createRankItem(Material.EMERALD, "ranks.vip"));
        inv.setItem(14, createRankItem(Material.GOLD_INGOT, "ranks.mvp"));
        inv.setItem(16, createRankItem(Material.DIAMOND, "ranks.nature"));

        // Close Button
        inv.setItem(22, createItem(Material.BARRIER, "&c&lTUTUP", List.of("&7Klik untuk keluar menu.")));

        p.openInventory(inv);
    }

    private ItemStack createRankItem(Material mat, String path) {
        String name = ConfigUtils.getMessage(path + ".name");
        List<String> lore = ConfigUtils.getMessageList(path + ".lore");

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));

            List<net.kyori.adventure.text.Component> componentLore = new ArrayList<>();
            for (String s : lore)
                componentLore.add(ChatUtils.toComponent(s));
            meta.lore(componentLore);

            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<net.kyori.adventure.text.Component> componentLore = new ArrayList<>();
            for (String s : lore)
                componentLore.add(ChatUtils.toComponent(s));
            meta.lore(componentLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = ChatUtils.stripColor(e.getView().getTitle());
        if (title.contains("NATURAL") && title.contains("RANKS")) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                return;

            Player p = (Player) e.getWhoClicked();
            if (e.getSlot() == 22) {
                p.closeInventory();
                p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1.2f);
            } else if (e.getSlot() == 10 || e.getSlot() == 12 || e.getSlot() == 14 || e.getSlot() == 16) {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                p.sendMessage(ChatUtils.colorize(
                        "&6&lNaturalSMP &8» &7Silahkan hubungi administrator atau kunjungi &estore.naturalsmp.id &7untuk pembelian!"));
            }
        }
    }
}
