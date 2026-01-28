package id.naturalsmp.naturalcore.profile;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
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

        // 1. Open Placeholder Loading GUI
        Inventory inv = GUIUtils.createGUI(new ProfileHolder(), 54,
                "&#00AAFF❂ &#00AAFFᴘʀᴏꜰɪʟᴇ &8(Loading...)");

        // Fill loading state
        ItemStack loading = createItem(Material.CLOCK, "&e&lLoading Data...", "&7Please wait...");
        inv.setItem(22, loading);
        viewer.openInventory(inv);

        // 2. Async Data Fetch
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ProfileData data = new ProfileData();

            // Tier
            if (plugin.getTierManager() != null) {
                var tier = plugin.getTierManager().getCurrentTier(target);
                data.rankDisplay = (tier != null) ? tier.display : "Unknown";
            } else {
                data.rankDisplay = "Loading...";
            }

            // Status & Ping
            data.isOnline = target.isOnline();
            if (data.isOnline && target.getPlayer() != null) {
                data.ping = target.getPlayer().getPing();
            }

            // Stats
            data.kdr = profileManager.getKDR(target);
            data.deaths = profileManager.getDeaths(target);
            data.playtime = profileManager.getPlaytimeFormatted(target);
            data.firstJoin = profileManager.getFirstJoin(target);

            // Economy
            data.hasCoins = profileManager.hasCoinsEngine();
            if (data.hasCoins) {
                data.coins = profileManager.getCoinsEngineBalance(target);
            }

            // AuraSkills
            data.hasSkills = profileManager.hasAuraSkills();
            if (data.hasSkills) {
                data.power = profileManager.getAuraSkillsPower(target);
                data.mining = profileManager.getAuraSkillsLevel(target, "mining");
                data.fighting = profileManager.getAuraSkillsLevel(target, "fighting");
                data.foraging = profileManager.getAuraSkillsLevel(target, "foraging");
                data.archery = profileManager.getAuraSkillsLevel(target, "archery");
            }

            // 3. Sync Render
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!viewer.isOnline() || viewer.getOpenInventory().getTopInventory() == null
                        || !(viewer.getOpenInventory().getTopInventory().getHolder() instanceof ProfileHolder)) {
                    return; // Player closed GUI or switched
                }

                // Update Title (Hack: Re-open or just update items if title update impossible
                // without flicker)
                // For smooth UX, we just update items. Title remains "Loading..." briefly or we
                // can't change it easily on 1.16+ without packets.
                // Re-opening inventory causes a flicker, so we just populate the existing one.
                // Ideally, we'd start with correct title but empty items.

                // Clear loading item
                inv.setItem(22, null);

                renderProfile(inv, viewer, target, data);
            });
        });
    }

    private void renderProfile(Inventory inv, Player viewer, OfflinePlayer target, ProfileData data) {
        // 1. Head Info (Slot 13)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.displayName(ChatUtils.toComponent("&a&l" + target.getName()));
        List<Component> headLore = new ArrayList<>();
        headLore.add(ChatUtils.toComponent("&8&m------------------"));
        headLore.add(ChatUtils.toComponent("&7Rank: &f" + data.rankDisplay));

        String status = data.isOnline ? "&aOnline" : "&cOffline";
        headLore.add(ChatUtils.toComponent("&7Status: " + status));
        if (data.isOnline) {
            headLore.add(ChatUtils.toComponent("&7Ping: &e" + data.ping + "ms"));
        }
        headLore.add(ChatUtils.toComponent("&8&m------------------"));
        headMeta.lore(headLore);
        head.setItemMeta(headMeta);
        inv.setItem(13, head);

        // 2. Statistics (Slot 29)
        List<String> statsLore = new ArrayList<>();
        statsLore.add("");
        statsLore.add("&f⚔ Marksman: &7" + data.kdr);
        statsLore.add("&f☠ Deaths: &c" + data.deaths);
        statsLore.add("&f⌚ Playtime: &b" + data.playtime);
        statsLore.add("&f📅 Join: &e" + data.firstJoin);
        inv.setItem(29, createItem(Material.CLOCK, "&b&lSTATISTIK", statsLore.toArray(new String[0])));

        // 3. Economy (Slot 31)
        List<String> ecoLore = new ArrayList<>();
        if (data.hasCoins) {
            ecoLore.add("&7NaturalCoin: &6" + data.coins + " NC");
        } else {
            ecoLore.add("&7NaturalCoin: &cFeature Disabled");
        }
        inv.setItem(31, createItem(Material.GOLD_INGOT, "&6&lEconomy Stats", ecoLore.toArray(new String[0])));

        // 4. AuraSkills (Slot 33)
        if (data.hasSkills) {
            List<String> skillLore = new ArrayList<>();
            skillLore.add("");
            skillLore.add("&f⭐ Total Power: &b" + data.power);
            skillLore.add("&f⛏ Mining: &eLvl " + data.mining);
            skillLore.add("&f⚔ Fighting: &eLvl " + data.fighting);
            skillLore.add("&f🌳 Foraging: &eLvl " + data.foraging);
            skillLore.add("&f🏹 Archery: &eLvl " + data.archery);
            skillLore.add("");
            skillLore.add("&7Buka &b/skills &7untuk detail lanjut.");
            inv.setItem(33, createItem(Material.EXPERIENCE_BOTTLE, "&b&lAURASKILLS", skillLore.toArray(new String[0])));
        }

        // 5. Social Actions (Slot 48, 50) - Only if viewing others
        if (!target.getUniqueId().equals(viewer.getUniqueId())) {
            inv.setItem(49, createItem(Material.PAPER, "&b&lSend Message", "&7Klik untuk kirim pesan"));
        }

        // Fillers (Black Glass)
        ItemStack filler = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private static class ProfileData {
        String rankDisplay;
        boolean isOnline;
        int ping;
        String kdr;
        int deaths;
        String playtime;
        String firstJoin;
        boolean hasCoins;
        double coins;
        boolean hasSkills;
        int power;
        int mining;
        int fighting;
        int foraging;
        int archery;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> l = new ArrayList<>();
        for (String s : lore) {
            l.add(ChatUtils.toComponent(s));
        }
        meta.lore(l);
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
