package id.naturalsmp.naturalcore.profile;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI implements Listener {

    private final NaturalCore plugin;
    private final ProfileManager profileManager;

    public ProfileGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.profileManager = plugin.getProfileManager();
    }

    public void openGUI(Player target, Player viewer) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize("&8Profile: &2" + target.getName()));

        // 1. Player Head (Slot 13 - Center Top)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.setDisplayName(ChatUtils.colorize("&a&l" + target.getName()));
        List<String> headLore = new ArrayList<>();
        headLore.add(ChatUtils.colorize("&7Rank: &fPlayer")); // Placeholder, nanti bisa ambil dari LuckPerms via Vault
        headLore.add(ChatUtils.colorize("&7Joined: &e" + profileManager.getJoinDate(target)));
        headLore.add("");
        headLore.add(ChatUtils.colorize("&7Status: " + (target.isOnline() ? "&aOnline" : "&cOffline")));
        headMeta.setLore(headLore);
        head.setItemMeta(headMeta);
        inv.setItem(13, head);

        // 2. Stats (Slot 29, 30, 32, 33)
        // KDR
        inv.setItem(29, createItem(Material.DIAMOND_SWORD, "&c&lCombat Stats",
                "&7Kills: &f" + profileManager.getMobKills(target) + " (Mobs)", // Sementara Mob Kills dulu
                "&7Deaths: &f" + profileManager.getDeaths(target),
                "&7KDR: &e" + profileManager.getKDR(target)));

        // Playtime
        inv.setItem(33, createItem(Material.CLOCK, "&e&lPlaytime",
                "&7Total Online:",
                "&f" + profileManager.getPlayTime(target)));

        // 3. Economy (Slot 31 - Center Middle)
        List<String> ecoLore = new ArrayList<>();
        ecoLore.add("&7Dompet (Vault): &a" + profileManager.getFormattedVaultBalance(target));
        if (profileManager.hasCoinsEngine()) {
            ecoLore.add("&7NaturalCoin: &6" + profileManager.getCoinsEngineBalance(target) + " NC");
        } else {
            ecoLore.add("&7NaturalCoin: &cFeature Disabled");
        }
        inv.setItem(31, createItem(Material.GOLD_INGOT, "&6&lEconomy Stats", ecoLore.toArray(new String[0])));

        // 4. Social Actions (Slot 48, 50) - Only if viewing others
        if (!target.getUniqueId().equals(viewer.getUniqueId())) {
            inv.setItem(49, createItem(Material.PAPER, "&b&lSend Message", "&7Klik untuk kirim pesan"));
            // Bisa tambah add friend dll
        }

        // Fillers (Black Glass)
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        viewer.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));
        List<String> l = new ArrayList<>();
        for (String s : lore) {
            l.add(ChatUtils.colorize(s));
        }
        meta.setLore(l);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith(ChatUtils.colorize("&8Profile:"))) {
            e.setCancelled(true); // Prevent taking items

            if (e.getCurrentItem() == null)
                return;

            // Logic click bisa ditambah disini
            // Misal klik profile sendiri buka settings
        }
    }
}
