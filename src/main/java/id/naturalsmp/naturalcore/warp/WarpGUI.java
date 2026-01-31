package id.naturalsmp.naturalcore.warp;

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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WarpGUI implements Listener {

    private final NaturalCore plugin;
    private final HashMap<UUID, Boolean> editorMode = new HashMap<>();

    public WarpGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, boolean isEditor) {
        // Title Estetik (Gradient Simulation)
        String title = isEditor
                ? "&c&lWARP &4&lEDITOR &8| &7Admin Mode"
                : "&x&0&0&A&A&F&F&lN&x&1&1&B&B&F&F&la&x&2&2&C&C&F&F&lt&x&3&3&D&D&F&F&lu&x&4&4&E&E&F&F&lr&x&5&5&F&F&F&F&la&x&6&6&F&F&F&F&ll &x&7&7&F&F&F&F&lW&x&8&8&F&F&F&F&la&x&9&9&F&F&F&F&lr&x&A&A&F&F&F&F&lp&x&B&B&F&F&F&F&ls";

        Inventory inv = GUIUtils.createGUI(new WarpHolder(), 54, title);

        fillBorder(inv);

        List<Warp> warpList = new ArrayList<>(plugin.getWarpManager().getWarps());
        warpList.sort(Comparator.comparing(Warp::getId));

        if (isEditor) {
            int[] validSlots = getPlayableSlots();
            for (int i = 0; i < warpList.size(); i++) {
                if (i >= validSlots.length)
                    break;
                inv.setItem(validSlots[i], createWarpItem(warpList.get(i), true));
            }
            editorMode.put(player.getUniqueId(), true);
        } else {
            int count = warpList.size();
            List<Integer> slots = getAutoPositions(count);
            for (int i = 0; i < count; i++) {
                if (i >= slots.size())
                    break;
                inv.setItem(slots.get(i), createWarpItem(warpList.get(i), false));
            }
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    private ItemStack createWarpItem(Warp w, boolean isEditor) {
        ItemStack item = new ItemStack(w.getIcon());
        ItemMeta meta = item.getItemMeta();

        String cleanId = ChatUtils.stripColor(w.getId()); // Clean ID
        String titleCasedName = toTitleCase(cleanId);

        // Use clean name without weird formats
        meta.displayName(ChatUtils.toComponent("&b&l" + titleCasedName));

        List<String> rawLore = isEditor
                ? ConfigUtils.getMessageList("gui.warp.item-editor-lore")
                : ConfigUtils.getMessageList("gui.warp.item-lore");

        List<Component> lore = new ArrayList<>();
        if (rawLore != null) {
            for (String s : rawLore) {
                lore.add(ChatUtils.toComponent(s
                        .replace("%id%", cleanId)
                        .replace("%name%", titleCasedName)));
            }
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty())
            return input;
        StringBuilder sb = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.replace("_", " ").toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            } else {
                c = Character.toLowerCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private void fillBorder(Inventory inv) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(ChatUtils.toComponent("&7"));
        border.setItemMeta(meta);
        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) {
                inv.setItem(i, border);
            }
        }
    }

    private int[] getPlayableSlots() {
        return new int[] {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    private List<Integer> getAutoPositions(int count) {
        List<Integer> slots = new ArrayList<>();
        if (count == 0)
            return slots;
        if (count == 1) {
            slots.add(22);
        } else if (count == 2) {
            slots.add(21);
            slots.add(23);
        } else if (count == 3) {
            slots.add(20);
            slots.add(22);
            slots.add(24);
        } else if (count == 4) {
            slots.add(21);
            slots.add(23);
            slots.add(30);
            slots.add(32);
        } else if (count == 5) {
            slots.add(20);
            slots.add(22);
            slots.add(24);
            slots.add(30);
            slots.add(32);
        } else if (count == 6) {
            slots.add(20);
            slots.add(22);
            slots.add(24);
            slots.add(29);
            slots.add(31);
            slots.add(33);
        } else if (count == 7) {
            slots.add(19);
            slots.add(21);
            slots.add(23);
            slots.add(25);
            slots.add(29);
            slots.add(31);
            slots.add(33);
        } else if (count == 8) {
            slots.add(19);
            slots.add(21);
            slots.add(23);
            slots.add(25);
            slots.add(28);
            slots.add(30);
            slots.add(32);
            slots.add(34);
        } else {
            for (int slot : getPlayableSlots())
                slots.add(slot);
        }
        return slots;
    }

    // --- EVENT HANDLER ---

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();
        // CHANGE: Use InventoryHolder instead of unreliable title check
        if (!(e.getInventory().getHolder() instanceof WarpHolder))
            return;

        // CANCEL KLIK APAPUN DI GUI
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE)
            return;

        // Pastikan klik di Top Inventory
        if (e.getClickedInventory() != e.getView().getTopInventory())
            return;

        ItemMeta meta = clicked.getItemMeta();
        if (!meta.hasLore() || meta.lore() == null || meta.lore().isEmpty())
            return;

        // Note: Retrieving lore from Component can be tricky if we want plain text.
        // But here we just need the ID which is usually in the first line or known.
        // Wait, current logic: String warpId =
        // ChatUtils.stripColor(meta.getLore().get(0));
        // We need to support Component-based lore retrieval.
        // Or we can store the ID in PDC (PersistentDataContainer) for robust handling,
        // but for now let's try to extract text from Component.

        // This is risky if we change to Component.
        // Let's defer component migration for lore READING, or assume ID extraction
        // needs fix.
        // Actually, let's keep the helper method returning String lore in
        // 'meta.getLore()'
        // IF we didn't change it. But we DID change createWarpItem to use
        // meta.lore(List<Component>).
        // So meta.getLore() (deprecated) might return null or work if Spigot converts
        // it back.
        // To be safe, we should assume we need to extract from Component.

        // However, extracting plain text from Component lore:
        // We can use meta.lore() which returns List<Component>.
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty())
            return;

        // We need to serialize back to string to strip color.
        String firstLine = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                .serialize(lore.get(0));
        String warpId = ChatUtils.stripColor(firstLine);

        Warp w = plugin.getWarpManager().getWarp(warpId);

        if (w == null)
            return;

        // Mode Editor
        if (editorMode.containsKey(p.getUniqueId())) {
            // DELETE: Klik Kanan Biasa (Tanpa Shift) - Sesuai kode lama (line 230)
            if (e.getClick().isRightClick() && !e.isShiftClick()) {
                plugin.getWarpManager().deleteWarp(w.getId());
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-deleted", "%name%", w.getId());
                openGUI(p, true);
            }
            // UPDATE ICON: Shift + Klik Kanan
            else if (e.isShiftClick() && e.getClick().isRightClick()) {
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand == null || hand.getType() == Material.AIR) {
                    ConfigUtils.sendGeneral(p, "messages.warp.hold-item-icon");
                    return;
                }
                w.setIcon(hand.getType());
                plugin.getWarpManager().saveWarpToFile(w);

                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                ConfigUtils.sendMessage(p, "prefix.warp", "messages.warp.warp-icon-set", "%name%", w.getId(), "%icon%",
                        hand.getType().name());
                openGUI(p, true); // Refresh GUI
            }
            return;
        }

        // Mode Player
        p.closeInventory();
        p.teleport(w.getLocation());
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        p.sendTitle(ChatUtils.colorize("&a" + toTitleCase(w.getId())), ChatUtils.colorize("&7Teleporting..."), 0,
                20, 10);
    }

    // --- TAMBAHAN PENTING: DRAG EVENT ---
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof WarpHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        editorMode.remove(e.getPlayer().getUniqueId());
    }

    // --- INNER CLASS HOLDER ---
    public static class WarpHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
