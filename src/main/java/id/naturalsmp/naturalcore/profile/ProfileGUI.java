package id.naturalsmp.naturalcore.profile;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI implements Listener {

    private final NaturalCore plugin;
    private final ProfileManager profileManager;

    public ProfileGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.profileManager = plugin.getProfileManager();
    }

    public void openGUI(Player viewer, OfflinePlayer target) {
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.0f); // Sound Effect

        Inventory inv = Bukkit.createInventory(new ProfileHolder(), 54,
                ChatUtils.colorize("&#00AAFF❂ &#00AAFFᴘʀᴏꜰɪʟᴇ &#55FF55❂"));

        // 1. Head Info (Slot 13)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.setDisplayName(ChatUtils.colorize("&a&l" + target.getName()));
        List<String> headLore = new ArrayList<>();
        headLore.add("&8&m------------------");
        headLore.add(
                "&7Rank: &f" + (plugin.getTierManager() != null ? plugin.getTierManager().getCurrentTier(viewer).display
                        : "Loading...")); // Use viewer for now or fetch target rank properly
        String status = target.isOnline() ? "&aOnline" : "&cOffline";
        headLore.add("&7Status: " + status);
        if (target.isOnline()) {
            int ping = target.getPlayer().getPing();
            headLore.add("&7Ping: &e" + ping + "ms");
        }
        headLore.add("&8&m------------------");
        headMeta.setLore(ChatUtils.colorize(headLore));
        head.setItemMeta(headMeta);
        inv.setItem(13, head);

        // 2. Statistics (Slot 29)
        List<String> statsLore = new ArrayList<>();
        statsLore.add("");
        statsLore.add("&f⚔ Marksman: &7" + profileManager.getKDR(target));
        statsLore.add("&f☠ Deaths: &c" + profileManager.getDeaths(target));
        statsLore.add("&f⌚ Playtime: &b" + profileManager.getPlaytimeFormatted(target));
        statsLore.add("&f📅 Join: &e" + profileManager.getFirstJoin(target));
        inv.setItem(29, createItem(Material.CLOCK, "&b&lSTATISTIK", statsLore.toArray(new String[0])));

        // 3. Economy (Slot 31)
        List<String> ecoLore = new ArrayList<>();
        if (profileManager.hasCoinsEngine()) {
            ecoLore.add("&7NaturalCoin: &6" + profileManager.getCoinsEngineBalance(target) + " NC");
        } else {
            ecoLore.add("&7NaturalCoin: &cFeature Disabled");
        }
        inv.setItem(31, createItem(Material.GOLD_INGOT, "&6&lEconomy Stats", ecoLore.toArray(new String[0])));

        // 4. AuraSkills (Slot 33)
        if (profileManager.hasAuraSkills()) {
            List<String> skillLore = new ArrayList<>();
            skillLore.add("");
            skillLore.add("&f⭐ Total Power: &b" + profileManager.getAuraSkillsPower(target));
            skillLore.add("&f⛏ Mining: &eLvl " + profileManager.getAuraSkillsLevel(target, "mining"));
            skillLore.add("&f⚔ Fighting: &eLvl " + profileManager.getAuraSkillsLevel(target, "fighting"));
            skillLore.add("&f🌳 Foraging: &eLvl " + profileManager.getAuraSkillsLevel(target, "foraging"));
            skillLore.add("&f🏹 Archery: &eLvl " + profileManager.getAuraSkillsLevel(target, "archery"));
            skillLore.add("");
            skillLore.add("&7Buka &b/skills &7untuk detail lanjut.");
            inv.setItem(33, createItem(Material.EXPERIENCE_BOTTLE, "&b&lAURASKILLS", skillLore.toArray(new String[0])));
        }

        // 5. Social Actions (Slot 48, 50) - Only if viewing others
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
        if (e.getInventory().getHolder() instanceof ProfileHolder) {
            e.setCancelled(true); // Prevent taking items

            if (e.getCurrentItem() == null)
                return;

            // Logic click bisa ditambah disini
            // Misal klik profile sendiri buka settings
        }
    }

    public static class ProfileHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
