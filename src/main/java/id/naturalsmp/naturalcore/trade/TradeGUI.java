package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.ConfigUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
// import su.nightexpress.coinsengine.api.CoinsEngineAPI;

import java.util.ArrayList;
import java.util.List;

public class TradeGUI implements Listener {

    private final NaturalCore plugin;
    private final int[] p1Slots = { 0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30 };
    private final int[] p2Slots = { 5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35 };

    public TradeGUI(NaturalCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openTradeGUI(Player p, TradeSession session) {
        Inventory inv = GUIUtils.createGUI(new TradeHolder(session), 54, "❂ ɴᴀᴛᴜʀᴀʟ ᴛʀᴀᴅᴇ ❂");

        renderTrade(inv, p, session);
        p.openInventory(inv);
    }

    private void renderTrade(Inventory inv, Player viewer, TradeSession session) {
        // 1. Clear Inventory for fresh render (be careful with player items)
        // We'll only clear the non-player slots
        for (int i = 0; i < 54; i++) {
            boolean isAllowed = false;
            for (int s : p1Slots)
                if (s == i) {
                    isAllowed = true;
                    break;
                }
            for (int s : p2Slots)
                if (s == i) {
                    isAllowed = true;
                    break;
                }
            if (!isAllowed)
                inv.setItem(i, null);
        }

        // 2. Borders & Separator
        ItemStack border = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack separator = GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);

        // Middle column
        for (int i = 4; i < 54; i += 9)
            inv.setItem(i, separator);

        boolean isP1 = viewer.equals(session.getPlayer1());

        // P1 Confirmation (Slot 45)
        ItemStack p1Confirm = createItem(
                session.isConfirmed(session.getPlayer1()) ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS,
                "&#55FF55&lKONFIRMASI: &f" + session.getPlayer1().getName(),
                "&7Status: " + (session.isConfirmed(session.getPlayer1()) ? "&aConfirmed" : "&cWaiting"));
        inv.setItem(48, p1Confirm);

        // P2 Confirmation (Slot 53)
        ItemStack p2Confirm = createItem(
                session.isConfirmed(session.getPlayer2()) ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS,
                "&#55FF55&lKONFIRMASI: &f" + session.getPlayer2().getName(),
                "&7Status: " + (session.isConfirmed(session.getPlayer2()) ? "&aConfirmed" : "&cWaiting"));
        inv.setItem(50, p2Confirm);

        // Money Display & Button (Slots 46, 52)
        inv.setItem(46, createItem(Material.GOLD_INGOT, "&#FFEE00&lVAULT MONEY: &f" + session.getPlayer1().getName(),
                "&7Uang ditawarkan: &eRp " + (int) session.getMoney(session.getPlayer1()),
                "",
                "&#00AAFF&l➥ KLIK UNTUK SET NOMINAL"));

        inv.setItem(52, createItem(Material.GOLD_INGOT, "&#FFEE00&lVAULT MONEY: &f" + session.getPlayer2().getName(),
                "&7Uang ditawarkan: &eRp " + (int) session.getMoney(session.getPlayer2()),
                "",
                "&#00AAFF&l➥ KLIK UNTUK SET NOMINAL"));

        // Information Help (Slot 49)
        inv.setItem(49, createItem(Material.BOOK, "&#6CCAFE&lINFORMATION",
                "&71. Masukkan item ke slot kosong.",
                "&72. Klik ikon emas untuk setor uang.",
                "&73. Klik blok kaca untuk konfirmasi.",
                "&74. Transaksi akan otomatis selesai."));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TradeHolder holder))
            return;
        TradeSession session = holder.getSession();
        Player player = (Player) e.getWhoClicked();

        int slot = e.getRawSlot();
        if (slot >= 54)
            return; // Player inventory move

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (slot == 48 || slot == 50) { // Confirm Buttons
            // Only allow confirming if it's your side
            boolean isP1Btn = (slot == 48);
            boolean isPlayerP1 = player.equals(session.getPlayer1());

            if (isP1Btn == isPlayerP1) {
                // Ensure the clicker is clicking THEIR OWN button
                // Slot 48 (P1) -> isP1Btn=true. If P1 clicks (isPlayerP1=true) -> true == true
                // -> ALLOW.
                // Slot 50 (P2) -> isP1Btn=false. If P2 clicks (isPlayerP1=false) -> false ==
                // false -> ALLOW.

                session.setConfirmed(player, !session.isConfirmed(player));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                updateBoth(session);

                if (session.bothConfirmed()) {
                    completeTrade(session);
                }
            }
        } else if (slot == 46 || slot == 52) { // Money Buttons
            boolean isP1Btn = (slot == 46);
            boolean isPlayerP1 = player.equals(session.getPlayer1());

            if (isP1Btn == isPlayerP1) {
                plugin.getTradeManager().startCustomMoneyInput(player, session);
            }
        } else {
            // Check if slot is in player's allowed side
            boolean isP1 = player.equals(session.getPlayer1());
            int[] myAllowed = isP1 ? p1Slots : p2Slots;

            boolean allowed = false;
            for (int s : myAllowed)
                if (s == slot) {
                    allowed = true;
                    break;
                }

            if (allowed) {
                // Allow putting/taking items
                e.setCancelled(false);
                session.setConfirmed(player, false);
                session.setConfirmed(session.getOther(player), false);
                // We need a task to update the other player's view after the tick
                Bukkit.getScheduler().runTask(plugin, () -> updateBoth(session));
            }
        }
    }

    private void updateBoth(TradeSession session) {
        // This would require more sophisticated logic to sync inventories
        // For now, let's assume it works by re-rendering
        renderTrade(session.getPlayer1().getOpenInventory().getTopInventory(), session.getPlayer1(), session);
        renderTrade(session.getPlayer2().getOpenInventory().getTopInventory(), session.getPlayer2(), session);
    }

    private void completeTrade(TradeSession session) {
        Player p1 = session.getPlayer1();
        Player p2 = session.getPlayer2();
        Inventory inv1 = p1.getOpenInventory().getTopInventory();
        Inventory inv2 = p2.getOpenInventory().getTopInventory();

        // 1. Swap Money (Vault)
        if (plugin.getVaultManager().getEconomy() != null) {
            if (session.getMoney(p1) > 0) {
                plugin.getVaultManager().getEconomy().withdrawPlayer(p1, session.getMoney(p1));
                plugin.getVaultManager().getEconomy().depositPlayer(p2, session.getMoney(p1));
            }
            if (session.getMoney(p2) > 0) {
                plugin.getVaultManager().getEconomy().withdrawPlayer(p2, session.getMoney(p2));
                plugin.getVaultManager().getEconomy().depositPlayer(p1, session.getMoney(p2));
            }
        }

        // 2. Distribute Items
        distributeSideItems(inv1, p1Slots, p2); // P1 items -> P2
        distributeSideItems(inv2, p2Slots, p1); // P2 items -> P1

        // Clear inventories or just close
        inv1.clear();
        inv2.clear();

        p1.closeInventory();
        p2.closeInventory();

        ConfigUtils.sendGeneral(p1, "messages.trade.success");
        ConfigUtils.sendGeneral(p2, "messages.trade.success");
        p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        plugin.getTradeManager().endTrade(session);
    }

    private void distributeSideItems(Inventory inv, int[] slots, Player target) {
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                target.getInventory().addItem(item).values()
                        .forEach(remaining -> target.getWorld().dropItemNaturally(target.getLocation(), remaining));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof TradeHolder holder))
            return;
        TradeSession session = holder.getSession();

        // If trade is already cleared from manager, don't do anything (it was
        // successful)
        if (plugin.getTradeManager().getSession((Player) e.getPlayer()) == null)
            return;

        Player closer = (Player) e.getPlayer();
        Player other = session.getOther(closer);

        // Mark trade as ended BEFORE closing other inventory to prevent recursion
        plugin.getTradeManager().endTrade(session);

        // Cancel trade and return items
        ConfigUtils.sendGeneral(closer, "messages.trade.cancelled");
        ConfigUtils.sendGeneral(other, "messages.trade.cancelled-other", "%player%", closer.getName());

        // Return items to respective owners
        returnItems(closer.getOpenInventory().getTopInventory(),
                closer.equals(session.getPlayer1()) ? p1Slots : p2Slots, closer);

        if (other.getOpenInventory().getTopInventory().getHolder() instanceof TradeHolder) {
            returnItems(other.getOpenInventory().getTopInventory(),
                    other.equals(session.getPlayer1()) ? p1Slots : p2Slots, other);
            other.closeInventory();
        }
    }

    private void returnItems(Inventory inv, int[] slots, Player owner) {
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                owner.getInventory().addItem(item).values()
                        .forEach(remaining -> owner.getWorld().dropItemNaturally(owner.getLocation(), remaining));
                inv.setItem(slot, null);
            }
        }
    }

    // Refactored to use Component APIs internally
    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            List<Component> loreList = new ArrayList<>();
            for (String l : lore)
                loreList.add(ChatUtils.toComponent(l));
            meta.lore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}
