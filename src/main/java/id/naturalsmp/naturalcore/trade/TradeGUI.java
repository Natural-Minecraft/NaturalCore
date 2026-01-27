package id.naturalsmp.naturalcore.trade;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
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
        Inventory inv = Bukkit.createInventory(new TradeHolder(session), 54,
                ChatUtils.colorize("&#6CCAFE&lＴＲＡＤＥ &8| &f" + session.getOther(p).getName()));

        renderTrade(inv, p, session);
        p.openInventory(inv);
    }

    private void renderTrade(Inventory inv, Player viewer, TradeSession session) {
        ItemStack separator = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 4; i < 54; i += 9)
            inv.setItem(i, separator);

        // Confirmation Status
        boolean isP1 = viewer.equals(session.getPlayer1());
        boolean myConfirmed = session.isConfirmed(viewer);
        boolean otherConfirmed = session.isConfirmed(session.getOther(viewer));

        ItemStack confirmBtn = createItem(myConfirmed ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS,
                myConfirmed ? "&a&lAKAN SELESAI" : "&c&lKLIK UNTUK KONFIRMASI",
                "&7Status kamu: " + (myConfirmed ? "&aConfirmed" : "&cWaiting"),
                "&7Status lawan: " + (otherConfirmed ? "&aConfirmed" : "&cWaiting"));

        inv.setItem(48, confirmBtn); // Viewer side confirm
        inv.setItem(50, createItem(Material.GOLD_INGOT, "&#FFEE00&lVAULT MONEY",
                "&7Uang yang ditawarkan:",
                "&fKamu: &eRp " + (int) session.getMoney(viewer),
                "&fLawan: &eRp " + (int) session.getMoney(session.getOther(viewer)),
                "",
                "&bLeft-Click to +1000",
                "&bRight-Click to +10000"));

        // Sync items from session map to inventory
        // (This part is complex because we need to map session items to GUI slots)
        // For simplicity: Players just put items into the GUI slots, and we track them.
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
        if (slot == 48) { // Confirm
            session.setConfirmed(player, !session.isConfirmed(player));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            updateBoth(session);

            if (session.bothConfirmed()) {
                completeTrade(session);
            }
        } else if (slot == 50) { // Money
            double current = session.getMoney(player);
            double add = e.isLeftClick() ? 1000 : 10000;

            // Check balance (Vault)
            double balance = plugin.getVaultManager().getEconomy() != null
                    ? plugin.getVaultManager().getEconomy().getBalance(player)
                    : 0;
            if (current + add > balance) {
                player.sendMessage(ChatUtils.colorize("&cSaldo Rp tidak cukup!"));
                return;
            }

            session.setMoney(player, current + add);
            session.setConfirmed(player, false);
            session.setConfirmed(session.getOther(player), false);
            updateBoth(session);
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

        p1.sendMessage(ChatUtils.colorize("&6&lTrade &8» &aTransaksi berhasil!"));
        p2.sendMessage(ChatUtils.colorize("&6&lTrade &8» &aTransaksi berhasil!"));
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

        // Cancel trade and return items
        closer.sendMessage(ChatUtils.colorize("&6&lTrade &8» &cTransaksi dibatalkan."));
        other.sendMessage(ChatUtils.colorize("&6&lTrade &8» &e" + closer.getName() + " &cdibatalkan transaksi."));

        // Return items to respective owners
        returnItems(closer.getOpenInventory().getTopInventory(),
                closer.equals(session.getPlayer1()) ? p1Slots : p2Slots, closer);

        if (other.getOpenInventory().getTopInventory().getHolder() instanceof TradeHolder) {
            returnItems(other.getOpenInventory().getTopInventory(),
                    other.equals(session.getPlayer1()) ? p1Slots : p2Slots, other);
            other.closeInventory();
        }

        plugin.getTradeManager().endTrade(session);
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

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize(name));
            List<String> loreList = new ArrayList<>();
            for (String l : lore)
                loreList.add(ChatUtils.colorize(l));
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}
