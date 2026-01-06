package id.naturalsmp.naturalcore.warp;

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
import org.bukkit.event.inventory.InventoryDragEvent; // Wajib import
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WarpGUI implements Listener {

    private final NaturalCore plugin;
    private final HashMap<UUID, Boolean> editorMode = new HashMap<>();

    public WarpGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void openGUI(Player player, boolean isEditor) {
        // Title Estetik
        String title = isEditor
                ? "&c&lWARP EDITOR (Klik Kanan Hapus)"
                : "&#252525◤ ᴡᴀʀᴘꜱ ᴍᴇɴᴜ ◥";

        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize(title));

        fillBorder(inv);

        List<Warp> warpList = new ArrayList<>(plugin.getWarpManager().getWarps());
        warpList.sort(Comparator.comparing(Warp::getId));

        if (isEditor) {
            int[] validSlots = getPlayableSlots();
            for (int i = 0; i < warpList.size(); i++) {
                if (i >= validSlots.length) break;
                inv.setItem(validSlots[i], createWarpItem(warpList.get(i), true));
            }
            editorMode.put(player.getUniqueId(), true);
        } else {
            int count = warpList.size();
            List<Integer> slots = getAutoPositions(count);
            for (int i = 0; i < count; i++) {
                if (i >= slots.size()) break;
                inv.setItem(slots.get(i), createWarpItem(warpList.get(i), false));
            }
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    @SuppressWarnings("deprecation")
    private ItemStack createWarpItem(Warp w, boolean isEditor) {
        ItemStack item = new ItemStack(w.getIcon());
        ItemMeta meta = item.getItemMeta();

        String titleCasedName = toTitleCase(w.getId());
        meta.setDisplayName(ChatUtils.colorize("&a" + titleCasedName));

        List<String> lore = new ArrayList<>();
        if (isEditor) {
            lore.add(ChatUtils.colorize("&8" + w.getId()));
            lore.add("");
            lore.add(ChatUtils.colorize("&e&l[EDITOR MODE]"));
            lore.add(ChatUtils.colorize("&7Klik Kanan untuk &cHAPUS"));
        } else {
            lore.add(ChatUtils.colorize("&8" + w.getId()));
            lore.add("");
            lore.add(ChatUtils.colorize("&7Warp ke " + titleCasedName));
            lore.add("");
            lore.add(ChatUtils.colorize("&a➥ &lKLIK UNTUK WARP"));
            lore.add("");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
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

    @SuppressWarnings("deprecation")
    private void fillBorder(Inventory inv) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize("&7"));
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
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    private List<Integer> getAutoPositions(int count) {
        List<Integer> slots = new ArrayList<>();
        if (count == 0) return slots;
        if (count == 1) { slots.add(22); }
        else if (count == 2) { slots.add(21); slots.add(23); }
        else if (count == 3) { slots.add(20); slots.add(22); slots.add(24); }
        else if (count == 4) { slots.add(21); slots.add(23); slots.add(30); slots.add(32); }
        else if (count == 5) { slots.add(20); slots.add(22); slots.add(24); slots.add(30); slots.add(32); }
        else if (count == 6) { slots.add(20); slots.add(22); slots.add(24); slots.add(29); slots.add(31); slots.add(33); }
        else if (count == 7) { slots.add(19); slots.add(21); slots.add(23); slots.add(25); slots.add(29); slots.add(31); slots.add(33); }
        else if (count == 8) { slots.add(19); slots.add(21); slots.add(23); slots.add(25); slots.add(28); slots.add(30); slots.add(32); slots.add(34); }
        else { for (int slot : getPlayableSlots()) slots.add(slot); }
        return slots;
    }

    // --- EVENT HANDLER ---

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String strippedTitle = ChatUtils.stripColor(e.getView().getTitle());

        // FIX: Cek font estetik "ᴡᴀʀᴘꜱ" atau simbol unik "◤"
        // Kita pakai simbol "◤" karena itu paling unik dan pasti ada di menu
        if (strippedTitle.contains("◤") || strippedTitle.contains("WARP EDITOR")) {

            // CANCEL KLIK APAPUN DI GUI
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

            // Pastikan klik di Top Inventory
            if (e.getClickedInventory() != e.getView().getTopInventory()) return;

            ItemMeta meta = clicked.getItemMeta();
            if (!meta.hasLore() || meta.getLore().isEmpty()) return;

            String warpId = ChatUtils.stripColor(meta.getLore().get(0));
            Warp w = plugin.getWarpManager().getWarp(warpId);

            if (w == null) return;

            // Mode Editor
            if (editorMode.containsKey(p.getUniqueId())) {
                if (e.getClick().isRightClick()) {
                    plugin.getWarpManager().deleteWarp(w.getId());
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                    p.sendMessage(ChatUtils.colorize("&c&lWARP &8» &fWarp &c" + w.getId() + " &ftelah dihapus!"));
                    openGUI(p, true);
                }
                return;
            }

            // Mode Player
            p.closeInventory();
            p.teleport(w.getLocation());
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            p.sendTitle(ChatUtils.colorize("&a" + toTitleCase(w.getId())), ChatUtils.colorize("&7Teleporting..."), 0, 20, 10);
        }
    }

    // --- TAMBAHAN PENTING: DRAG EVENT ---
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        String strippedTitle = ChatUtils.stripColor(e.getView().getTitle());
        // Fix: Cek font estetik
        if (strippedTitle.contains("◤") || strippedTitle.contains("WARP EDITOR")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        editorMode.remove(e.getPlayer().getUniqueId());
    }
}