package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import id.naturalsmp.naturalcore.tier.TierManager.Tier;

import java.util.ArrayList;
import java.util.List;

public class TierGUI implements Listener {

    private final NaturalCore plugin;
    private final TierManager tierManager;

    public TierGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.tierManager = plugin.getTierManager();
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f); // Sound Effect Open
        Inventory inv = GUIUtils.createGUI(new TierHolder(), 27, "&8&lNatural Rank Progression");

        Tier current = tierManager.getCurrentTier(p);
        Tier next = tierManager.getNextTier(p);

        // 1. Current Info (Slot 11)
        List<String> curLore = new ArrayList<>();
        if (current == null) {
            curLore.add("&c&oError: Data not found."); // Should be Warrior 3 default now
        } else {
            curLore.add("&8&m------------------");
            curLore.add("&7Rank saat ini:");
            curLore.add(" " + current.display);
            curLore.add("");
            curLore.add("&7Suffix Chat:");
            curLore.add(" " + current.suffix);
            curLore.add("");
            curLore.add("&e&oTeruslah grinding untuk");
            curLore.add("&e&omencapai puncak!");
            curLore.add("&8&m------------------");
        }
        inv.setItem(11, GUIUtils.createItem(Material.KNOWLEDGE_BOOK, "&b&lINFO RANK", curLore));

        // 2. Rank Up (Slot 15)
        if (next != null) {
            List<String> reqLore = new ArrayList<>();
            reqLore.add("&7Next Target: " + next.display);
            reqLore.add("");
            reqLore.add("&6&lSYARAT NAIK RANK:");

            // Cek Money
            double bal = plugin.getProfileManager().getVaultBalance(p);
            String colorMoney = (bal >= next.reqMoney) ? "&a✔" : "&c✘";
            reqLore.add(" " + colorMoney + " &7Money: &e$" + (int) next.reqMoney);

            // Cek Kills
            int kills = p.getStatistic(Statistic.MOB_KILLS);
            String colorKills = (kills >= next.reqKills) ? "&a✔" : "&c✘";
            reqLore.add(" " + colorKills + " &7Mob Kills: &e" + kills + "/" + next.reqKills);

            reqLore.add("");
            if (tierManager.canRankUp(p)) {
                reqLore.add("&a&l[ KLIK UNTUK RANK UP ]");
                reqLore.add("&7Biaya uang akan otomatis");
                reqLore.add("&7terpotong dari akunmu.");
            } else {
                reqLore.add("&c&l[ BELUM TERPENUHI ]");
                reqLore.add("&7Penuhi syarat di atas");
                reqLore.add("&7untuk naik level.");
            }

            Material iconStart = tierManager.canRankUp(p) ? Material.EXPERIENCE_BOTTLE
                    : Material.RED_STAINED_GLASS_PANE;
            inv.setItem(15, GUIUtils.createItem(iconStart, "&6&lRANK UP", reqLore));
        } else {
            List<String> maxLore = new ArrayList<>();
            maxLore.add("&7Kamu sudah mencapai level");
            maxLore.add("&7tertinggi di server ini!");
            maxLore.add("");
            maxLore.add("&e&lGGWP!");
            inv.setItem(15, GUIUtils.createItem(Material.NETHER_STAR, "&d&lMYTHIC GLORY", maxLore));
        }

        // Filler
        ItemStack filler = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TierHolder))
            return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null)
            return;

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        if (item.getType() == Material.EXPERIENCE_BOTTLE) {
            // Cek juga nama item biar aman (atau cek holder)
            // Di sini holder sudah TierHolder, jadi aman asumsi ini item Rank Up

            if (tierManager.rankUp(p)) {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                p.sendMessage(ChatUtils
                        .colorize("&a&lCONGRATS! &fKamu naik rank ke " + tierManager.getCurrentTier(p).display));

                // Broadcast
                String prefix = ConfigUtils.getString("prefix.player");
                GUIUtils.broadcast(prefix + "&e" + p.getName()
                        + " &ftelah naik rank menjadi " + tierManager.getCurrentTier(p).display);

                p.closeInventory();
            } else {
                p.sendMessage(ChatUtils.colorize("&cGagal rank up. Periksa saldo/stats mu."));
                p.closeInventory();
            }
        }
    }
}
