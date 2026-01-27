package id.naturalsmp.naturalcore.banner;

import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BannerListener implements Listener {

    private final BannerManager manager;
    private static final Map<UUID, Location> pos1 = new HashMap<>();
    private static final Map<UUID, Location> pos2 = new HashMap<>();
    private static final Map<UUID, BlockFace> face = new HashMap<>();

    public BannerListener(BannerManager manager) {
        this.manager = manager;
    }

    public static ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.colorize("&6&lBanner Wand"));
            meta.setLore(java.util.List.of(
                    ChatUtils.colorize("&7Left-click: Set Position 1"),
                    ChatUtils.colorize("&7Right-click: Set Position 2")));
            wand.setItemMeta(meta);
        }
        return wand;
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.BLAZE_ROD)
            return;
        if (!item.hasItemMeta() || !item.getItemMeta().getDisplayName().contains("Banner Wand"))
            return;

        event.setCancelled(true);
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;
        if (loc == null)
            return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            pos1.put(p.getUniqueId(), loc);
            face.put(p.getUniqueId(), event.getBlockFace());
            p.sendMessage(ChatUtils.colorize("&a&l[NaturalCore] &fPosition 1 & Face set: &e" + loc.getBlockX() + ", "
                    + loc.getBlockY() + ", " + loc.getBlockZ() + " (" + event.getBlockFace() + ")"));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pos2.put(p.getUniqueId(), loc);
            p.sendMessage(ChatUtils.colorize("&a&l[NaturalCore] &fPosition 2 set: &e" + loc.getBlockX() + ", "
                    + loc.getBlockY() + ", " + loc.getBlockZ()));
        }
    }

    @EventHandler
    public void onBannerRightClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Interaction))
            return;

        for (String tag : entity.getScoreboardTags()) {
            if (tag.startsWith("naturalbanner_hitbox_")) {
                String bannerName = tag.replace("naturalbanner_hitbox_", "");
                manager.handleInteract(event.getPlayer(), bannerName, false);
                return;
            }
        }
    }

    @EventHandler
    public void onBannerLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Interaction))
            return;

        for (String tag : entity.getScoreboardTags()) {
            if (tag.startsWith("naturalbanner_hitbox_")) {
                String bannerName = tag.replace("naturalbanner_hitbox_", "");
                manager.handleInteract(player, bannerName, true);
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        if (id.naturalsmp.naturalcore.utils.BedrockUtils.isBedrock(event.getPlayer())) {
            // Delay slightly to ensure entities are loaded/spawned for the player
            // But usually hideEntity works immediately if the entity exists in server
            // memory
            manager.hideAllBanners(event.getPlayer());
        }
    }

    public static Location getPos1(UUID uuid) {
        return pos1.get(uuid);
    }

    public static Location getPos2(UUID uuid) {
        return pos2.get(uuid);
    }

    public static BlockFace getFace(UUID uuid) {
        return face.get(uuid);
    }
}
