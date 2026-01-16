package id.naturalsmp.naturalcore.tier;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import id.naturalsmp.naturalcore.tier.TierManager.Tier;

public class TierGUI implements Listener {

    private final NaturalCore plugin;
    private final TierManager tierManager;

    public TierGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.tierManager = plugin.getTierManager();
    }

    public void openGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatUtils.colorize("&8Natural Tier System"));

        Tier current = tierManager.getCurrentTier(p);
        Tier next = tierManager.getNextTier(p);

        // 1. Current Info (Slot 11)
        List<String> curLore = new ArrayList<>();
        if (current == null) {
            curLore.add("&7Kamu belum memiliki Rank.");
        } else {
            curLore.add("&7Rank saat ini: &f" + current.display);
            curLore.add("&7Suffix: " + current.suffix);
        }
        inv.setItem(11, createItem(Material.BOOK, "&a&lCurrent Info", curLore));

        // 2. Rank Up (Slot 15)
        if (next != null) {
            List<String> reqLore = new ArrayList<>();
            reqLore.add("&7Next Rank: " + next.display);
            reqLore.add("");
            reqLore.add("&e&lRequirements:");

            // Cek Money
            double bal = plugin.getProfileManager().getVaultBalance(p);
            String colorMoney = (bal >= next.reqMoney) ? "&a✔" : "&c✘";
            reqLore.add(colorMoney + " &7Money: &f" + next.reqMoney);

            // Cek Kills
            int kills = p.getStatistic(Statistic.MOB_KILLS);
            String colorKills = (kills >= next.reqKills) ? "&a✔" : "&c✘";
            reqLore.add(colorKills + " &7Mob Kills: &f" + kills + "/" + next.reqKills);

            reqLore.add("");
            if (tierManager.canRankUp(p)) {
                reqLore.add("&a&lKLIK UNTUK RANK UP!");
            } else {
                reqLore.add("&cSyarat belum terpenuhi.");
            }

            Material iconStart = tierManager.canRankUp(p) ? Material.EXPERIENCE_BOTTLE : Material.BARRIER;
            inv.setItem(15, createItem(iconStart, "&6&lRANK UP", reqLore));
        } else {
            inv.setItem(15, createItem(Material.NETHER_STAR, "&d&lMAX RANK", "&7Kamu sudah mencapai level tertinggi!",
                    "&7Tunggu update selanjutnya."));
        }

        // Filler
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }

        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        return createItem(mat, name, lore.toArray(new String[0]));
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));
        List<String> l = new ArrayList<>();
        for (String s : lore)
            l.add(ChatUtils.colorize(s));
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatUtils.colorize("&8Natural Tier System"))) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null)
                return;

            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();

            if (item.getType() == Material.EXPERIENCE_BOTTLE) {
                if (tierManager.rankUp(p)) {
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    p.sendMessage(ChatUtils
                            .colorize("&a&lCONGRATS! &fKamu naik rank ke " + tierManager.getCurrentTier(p).display));

                    // Broadcast
                    Bukkit.broadcastMessage(ChatUtils.colorize("&6&lTIER &8» &e" + p.getName()
                            + " &ftelah naik rank menjadi " + tierManager.getCurrentTier(p).display));

                    p.closeInventory();
                } else {
                    p.sendMessage(ChatUtils.colorize("&cGagal rank up. Periksa saldo/stats mu."));
                    p.closeInventory();
                }
            }
        }
    }
}
