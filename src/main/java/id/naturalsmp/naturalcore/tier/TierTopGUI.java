package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TierTopGUI implements Listener {

    private final NaturalCore plugin;
    private final TierManager tierManager;

    public TierTopGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.tierManager = plugin.getTierManager();
    }

    public void openGUI(Player viewer) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize("&8Top Global Season"));

        // Get Top 10
        Map<String, Integer> top = tierManager.getTopPlayers(10);
        int slot = 0;

        // Posisi cantik di inventory
        // Baris 1: 0-8 (Top 1-9?)
        // Kita pakai simple layout: 0, 1, 2...

        int rank = 1;
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
            int level = entry.getValue();
            TierManager.Tier t = tierManager.getTier(level);
            String display = (t != null) ? t.display : "&7Unranked";

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(p);
            meta.setDisplayName(ChatUtils.colorize("&e#" + rank + " &f" + p.getName()));

            List<String> lore = new ArrayList<>();
            lore.add(ChatUtils.colorize("&7Rank: " + display));
            lore.add(ChatUtils.colorize("&7Level: &f" + level));
            meta.setLore(lore);
            head.setItemMeta(meta);

            inv.setItem(getSlotForRank(rank), head);
            rank++;
        }

        // Fillers
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        fMeta.setDisplayName(" ");
        filler.setItemMeta(fMeta);

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }

        viewer.openInventory(inv);
    }

    private int getSlotForRank(int rank) {
        // Custom positions for podium effect?
        // 1 -> 13 (Center Top)
        // 2 -> 21
        // 3 -> 23
        // 4-10 -> Bawah
        switch (rank) {
            case 1:
                return 13;
            case 2:
                return 21;
            case 3:
                return 23;
            default:
                return 26 + rank; // 4 -> 30, 5 -> 31...
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatUtils.colorize("&8Top Global Season"))) {
            e.setCancelled(true);
        }
    }
}
