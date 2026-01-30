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
        Inventory inv = GUIUtils.createGUI(new ProfileHolder(), 54, "❂ ɴᴀᴛᴜʀᴀʟ ᴘʀᴏꜰɪʟᴇ ❂");

        // Fill loading state
        ItemStack loading = createItem(Material.CLOCK, "&e&lLoading Data...", "&7Please wait...");
        inv.setItem(22, loading);
        viewer.openInventory(inv);

        // 2. Async Data Fetch
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ProfileData data = new ProfileData();

            // Tier & Rank
            if (plugin.getTierManager() != null) {
                var tier = plugin.getTierManager().getCurrentTier(target);
                data.rankDisplay = (tier != null) ? tier.display : "Unknown";
            } else {
                data.rankDisplay = "Loading...";
            }

            // LuckPerms Rank (Via Vault)
            try {
                if (plugin.getVaultManager() != null && plugin.getVaultManager().getChat() != null) {
                    data.rankName = plugin.getVaultManager().getChat().getPrimaryGroup(null, target);
                    if (data.rankName != null) {
                        data.rankName = data.rankName.substring(0, 1).toUpperCase() + data.rankName.substring(1);
                    }
                }
            } catch (Exception e) {
                data.rankName = "Default";
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
            data.moneyFormatted = profileManager.getFormattedVaultBalance(target);
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

                // Update visual title (if possible via NMS but we keep it static for stability)
                // For profile, let's keep it clean.

                // Clear loading item
                inv.setItem(22, null);

                renderProfile(inv, viewer, target, data);
            });
        });
    }

    private void renderProfile(Inventory inv, Player viewer, OfflinePlayer target, ProfileData data) {
        // 1. Head Info (Slot 13) - The "Identity" focus
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(target);
        headMeta.displayName(ChatUtils.toComponent("&#00AAFF❂ &l" + target.getName() + " &#00AAFF❂"));
        List<Component> headLore = new ArrayList<>();
        headLore.add(ChatUtils.toComponent("&8&m----------------------------"));
        headLore.add(ChatUtils.toComponent("&7Rank: &f" + data.rankName));
        headLore.add(ChatUtils.toComponent("&7Tier: " + data.rankDisplay));
        headLore.add(ChatUtils.toComponent(""));
        headLore.add(ChatUtils.toComponent("&7Uang: &aRp " + data.moneyFormatted));
        if (data.hasCoins) {
            headLore.add(ChatUtils.toComponent("&7NaturalCoin: &6" + data.coins + " NC"));
        }
        headLore.add(ChatUtils.toComponent(""));
        String status = data.isOnline ? "&aOnline" : "&cOffline";
        headLore.add(ChatUtils.toComponent("&7Status: " + status));
        if (data.isOnline) {
            headLore.add(ChatUtils.toComponent("&7Ping: &e" + data.ping + "ms"));
        }
        headLore.add(ChatUtils.toComponent("&8&m----------------------------"));
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

        // 3. Economy Details (Slot 31) - Replaced with detailed view
        List<String> ecoLore = new ArrayList<>();
        ecoLore.add("");
        ecoLore.add("&7Saldo Bank: &aRp " + data.moneyFormatted);
        ecoLore.add("&7Saldo Koin: &6" + (data.hasCoins ? data.coins : 0) + " NC");
        inv.setItem(31, createItem(Material.GOLD_INGOT, "&6&lDOMPET DIGITAL", ecoLore.toArray(new String[0])));

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
            skillLore.add("&#55FF55&l➥ KLIK UNTUK BUKA SKILLS");
            inv.setItem(33, createItem(Material.EXPERIENCE_BOTTLE, "&b&lAURASKILLS", skillLore.toArray(new String[0])));
        }

        // 5. Social Actions (Slot 49) - Only if viewing others
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
        String rankName; // LuckPerms Rank
        String moneyFormatted;
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
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();

            if (e.getCurrentItem() == null)
                return;

            Material mat = e.getCurrentItem().getType();
            if (mat == Material.EXPERIENCE_BOTTLE) {
                p.closeInventory();
                p.performCommand("skills");
            } else if (mat == Material.PAPER) {
                // Future: open chat input for message
                p.closeInventory();
                p.sendMessage(ChatUtils.colorize("&6&lProfile &8» &7Gunakan &e/msg <player> &7untuk berkirim pesan."));
            }
        }
    }

    public static class ProfileHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
