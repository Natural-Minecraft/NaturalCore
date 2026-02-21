package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TierAdminGUI implements Listener {

    private final NaturalCore plugin;

    public TierAdminGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        Inventory inv = Bukkit.createInventory(new TierAdminHolder(), 54, ChatUtils.colorize("&8Admin: Tier Editor"));

        // Get tiers safely
        // We'll use a hacky way to get the internal map or just loop 1-28
        for (int level = 1; level <= 28; level++) {
            TierManager.Tier tier = plugin.getTierManager().getTier(level);
            if (tier != null) {
                List<String> lore = new ArrayList<>();
                lore.add("&8&m------------------");
                lore.add("&7ID: &f" + tier.id);
                lore.add("&7Level: &e" + tier.level);
                lore.add("");
                lore.add("&6&lSYARAT:");
                lore.add(" &7Money: &e$" + tier.reqMoney);
                lore.add(" &7Mob Kills: &e" + tier.reqKills);
                lore.add("");
                lore.add("&e&l[ KLIK UNTUK EDIT ]");
                lore.add("&8&m------------------");

                inv.addItem(GUIUtils.createItem(Material.PAPER, tier.display, lore));
            }
        }

        // Fillers
        ItemStack filler = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TierAdminHolder))
            return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() != Material.PAPER)
            return;

        Player p = (Player) e.getWhoClicked();
        // Extract level from lore
        List<net.kyori.adventure.text.Component> lore = e.getCurrentItem().getItemMeta().lore();
        if (lore == null || lore.size() < 3)
            return;

        String levelStr = ChatUtils.stripColor(id.naturalsmp.naturalcore.utils.ChatUtils.serialize(lore.get(2)))
                .replace("Level: ", "");
        try {
            int level = Integer.parseInt(levelStr);
            new TierEditorGUI(plugin, level).openGUI(p);
        } catch (NumberFormatException ex) {
            p.sendMessage(ChatUtils.colorize("&cError extracting tier level."));
        }
    }

    public static class TierAdminHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
