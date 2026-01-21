package id.naturalsmp.naturalcore.chat;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.Map;

public class EmojiGUI implements Listener {

    private final NaturalCore plugin;
    private final EmojiManager emojiManager;

    public EmojiGUI(NaturalCore plugin) {
        this.plugin = plugin;
        this.emojiManager = EmojiManager.getInstance();
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.2f);
        Inventory inv = Bukkit.createInventory(null, 54, ChatUtils.colorize("&8&lKoleksi Emoji Chat"));

        int slot = 0;
        for (Map.Entry<String, EmojiManager.EmojiData> entry : emojiManager.getEmojiRegistry().entrySet()) {
            if (slot >= 54)
                break; // Limit page 1 for now

            String trigger = entry.getKey();
            EmojiManager.EmojiData data = entry.getValue();

            // Check Perm
            boolean unlocked = !data.hasPermission() || p.hasPermission(data.getPermission());

            Material icon = unlocked ? Material.PAPER : Material.MAP;
            String name = unlocked ? "&a&l" + data.getCharacter() : "&c&lLOCKED";

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Trigger: &e" + trigger);
            lore.add("&7Permission: " + (data.hasPermission() ? data.getPermission() : "&aNone"));
            lore.add("");
            if (unlocked) {
                lore.add("&eKlik untuk preview di chat");
            } else {
                lore.add("&cKamu belum membuka emoji ini.");
            }

            inv.setItem(slot, createItem(icon, name, lore, trigger));
            slot++;
        }

        p.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore, String triggerKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtils.colorize(name));
        List<String> coloredLore = new ArrayList<>();
        for (String s : lore)
            coloredLore.add(ChatUtils.colorize(s));
        // Hide trigger in invisible lore or NBT if consistent clicking needed,
        // but here we just use it for display mostly.
        coloredLore.add(ChatUtils.colorize("&0id:" + triggerKey)); // Hidden ID
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatUtils.colorize("&8&lKoleksi Emoji Chat"))) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                return;

            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasLore()) {
                String hiddenId = ChatUtils.stripColor(meta.getLore().get(meta.getLore().size() - 1));
                if (hiddenId.startsWith("id:")) {
                    String trigger = hiddenId.substring(3);
                    EmojiManager.EmojiData data = emojiManager.getEmojiRegistry().get(trigger);

                    if (data != null) {
                        p.closeInventory();
                        p.chat(trigger); // Simulate typing
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                    }
                }
            }
        }
    }
}
