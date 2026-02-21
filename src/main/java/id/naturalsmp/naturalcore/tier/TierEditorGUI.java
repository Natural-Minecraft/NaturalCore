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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TierEditorGUI implements Listener {

    private final NaturalCore plugin;
    private final int level;
    private static final Map<UUID, EditContext> editSessions = new HashMap<>();

    public TierEditorGUI(NaturalCore plugin, int level) {
        this.plugin = plugin;
        this.level = level;
    }

    public void openGUI(Player p) {
        TierManager.Tier tier = plugin.getTierManager().getTier(level);
        if (tier == null)
            return;

        Inventory inv = Bukkit.createInventory(new TierEditorHolder(level), 27,
                ChatUtils.colorize("&8Edit: " + tier.display));

        // 1. Money Requirement (Slot 11)
        List<String> moneyLore = new ArrayList<>();
        moneyLore.add("&7Current: &e$" + tier.reqMoney);
        moneyLore.add("");
        moneyLore.add("&a[ KLIK UNTUK EDIT ]");
        inv.setItem(11, GUIUtils.createItem(Material.GOLD_INGOT, "&6&lMONEY REQUIREMENT", moneyLore));

        // 2. Kills Requirement (Slot 15)
        List<String> killsLore = new ArrayList<>();
        killsLore.add("&7Current: &e" + tier.reqKills + " Kills");
        killsLore.add("");
        killsLore.add("&a[ KLIK UNTUK EDIT ]");
        inv.setItem(15, GUIUtils.createItem(Material.DIAMOND_SWORD, "&c&lKILLS REQUIREMENT", killsLore));

        // 3. Back button (Slot 22)
        inv.setItem(22, GUIUtils.createItem(Material.ARROW, "&7Kembali", new ArrayList<>()));

        // Fillers
        ItemStack filler = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TierEditorHolder))
            return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        int lvl = ((TierEditorHolder) e.getInventory().getHolder()).getLevel();

        if (item.getType() == Material.ARROW) {
            new TierAdminGUI(plugin).openGUI(p);
            return;
        }

        if (item.getType() == Material.GOLD_INGOT) {
            startEdit(p, lvl, EditType.MONEY);
        } else if (item.getType() == Material.DIAMOND_SWORD) {
            startEdit(p, lvl, EditType.KILLS);
        }
    }

    private void startEdit(Player p, int lvl, EditType type) {
        editSessions.put(p.getUniqueId(), new EditContext(lvl, type));
        p.closeInventory();
        p.sendMessage("");
        p.sendMessage(ChatUtils.colorize("&e&lTIER EDITOR: &fSilahkan ketik angka baru di chat."));
        p.sendMessage(ChatUtils.colorize("&7Ketik &ccancel &7untuk membatalkan."));
        p.sendMessage("");
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        // Register in ChatListener (we'll implement this next)
        id.naturalsmp.naturalcore.chat.ChatListener.setTierEditMode(p.getUniqueId());
    }

    public static EditContext getContext(UUID uuid) {
        return editSessions.remove(uuid);
    }

    public static class TierEditorHolder implements InventoryHolder {
        private final int level;

        public TierEditorHolder(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public enum EditType {
        MONEY, KILLS
    }

    public static class EditContext {
        public final int level;
        public final EditType type;

        public EditContext(int level, EditType type) {
            this.level = level;
            this.type = type;
        }
    }
}
