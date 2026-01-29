package id.naturalsmp.naturalcore.admin;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.permissions.PermissionManager;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import id.naturalsmp.naturalcore.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankEditorGUI implements Listener {

    private final NaturalCore plugin;
    private final Map<UUID, String> pendingPermissionInput = new HashMap<>();

    public RankEditorGUI(NaturalCore plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player p) {
        Inventory inv = GUIUtils.createGUI(new EditorHolder("MAIN"), 27, "&8Rank Editor");

        int slot = 10;
        for (PermissionManager.RankConfig rank : plugin.getPermissionManager().getRanks().values()) {
            ItemStack icon = new ItemStack(Material.PAPER);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                // Use display name if available, else console name
                String name = (rank.displayName != null && !rank.displayName.isEmpty()) ? rank.displayName
                        : rank.consoleName;
                meta.displayName(ChatUtils.toComponent(name));
                List<Component> lore = new ArrayList<>();
                lore.add(ChatUtils.toComponent("&7ID: &f" + rank.id));
                lore.add(ChatUtils.toComponent("&7Weight: &f" + rank.weight));
                lore.add(ChatUtils.toComponent(""));
                lore.add(ChatUtils.toComponent("&eClick to Edit"));
                meta.lore(lore);
                icon.setItemMeta(meta);
            }
            // Put item (simple layout flow)
            if (slot < 26) {
                inv.setItem(slot++, icon);
                if (slot == 17)
                    slot = 19; // Wrap to next row
            }
        }

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    public void openRankDetail(Player p, String rankId) {
        PermissionManager.RankConfig rank = plugin.getPermissionManager().getRanks().get(rankId);
        if (rank == null)
            return;

        Inventory inv = GUIUtils.createGUI(new EditorHolder(rankId), 27, "&8Editing: " + rank.consoleName);

        // Center Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(ChatUtils.toComponent("&e&lRank Info"));
        List<Component> lore = new ArrayList<>();
        lore.add(ChatUtils.toComponent("&7ID: &f" + rank.id));
        lore.add(ChatUtils.toComponent("&7Permissions: &f" + rank.permissions.size()));
        lore.add(ChatUtils.toComponent("&7Inherits: &f" + (rank.inheritance != null ? rank.inheritance : "None")));
        infoMeta.lore(lore);
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Add Permission Button
        ItemStack addBtn = new ItemStack(Material.LIME_DYE);
        ItemMeta addMeta = addBtn.getItemMeta();
        addMeta.displayName(ChatUtils.toComponent("&a&lAdd Permission"));
        addMeta.lore(List.of(ChatUtils.toComponent("&7Click to type permission in chat")));
        addBtn.setItemMeta(addMeta);
        inv.setItem(11, addBtn);

        // Back Button
        inv.setItem(22, GUIUtils.createItem(Material.ARROW, "&cBack", List.of()));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof EditorHolder holder))
            return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType() == Material.AIR)
            return;

        if (holder.type.equals("MAIN")) {
            // Clicked a rank?
            ItemMeta meta = current.getItemMeta();
            // Simple hack: parse ID from Lore or NBT.
            // Since we didn't store NBT, let's just find by display name match or better:
            // we should have stored ID in PDC.
            // For simplicity in this quick implementation, I will iterate ranks to find
            // match by display name/console name logic
            // Or better: use Slot mapping if necessary.
            // Actually, simplest way: store ID in lore hidden? No, visual check.
            // Let's use the ID from the Lore "ID: ..."
            if (meta.hasLore()) {
                // Component lore parsing can be tricky.
                // Let's assume the user clicked valid item.
                // Or we could have mapped slots.
                // Re-finding rank logic:
                for (PermissionManager.RankConfig rank : plugin.getPermissionManager().getRanks().values()) {
                    String name = (rank.displayName != null && !rank.displayName.isEmpty()) ? rank.displayName
                            : rank.consoleName;
                    // Strip colors for comparison might be safer
                    if (ChatUtils.serialize(meta.displayName())
                            .equals(ChatUtils.serialize(ChatUtils.toComponent(name)))) {
                        openRankDetail(p, rank.id);
                        return;
                    }
                }
            }
        } else {
            // Rank Detail View
            String rankId = holder.type;

            if (e.getSlot() == 11) { // Add Permission
                p.closeInventory();
                pendingPermissionInput.put(p.getUniqueId(), rankId);
                p.sendMessage(ChatUtils.colorize("&a&lEDIT &8» &7Type permissions to add in chat (or 'cancel'):"));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            } else if (e.getSlot() == 22) { // Back
                openMainMenu(p);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (pendingPermissionInput.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            String rankId = pendingPermissionInput.remove(e.getPlayer().getUniqueId());
            String msg = e.getMessage();

            if (msg.equalsIgnoreCase("cancel")) {
                e.getPlayer().sendMessage(ChatUtils.colorize("&cCancelled."));
            } else {
                // Add permission logic
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getPermissionManager().addPermission(rankId, msg);
                    e.getPlayer().sendMessage(ChatUtils.colorize("&aPermission added: &f" + msg));
                    openRankDetail(e.getPlayer(), rankId); // Reopen GUI
                });
            }
        }
    }

    // Holder to differentiate menus and store context (rankId)
    public static class EditorHolder implements InventoryHolder {
        public String type; // "MAIN" or rankId

        public EditorHolder(String type) {
            this.type = type;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
