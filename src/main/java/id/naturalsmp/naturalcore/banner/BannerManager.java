package id.naturalsmp.naturalcore.banner;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BannerManager {

    private final NaturalCore plugin;
    private final Map<String, Banner> activeBanners = new HashMap<>();

    public BannerManager(NaturalCore plugin) {
        this.plugin = plugin;
        loadBanners();
    }

    public void loadBanners() {
        File folder = new File(plugin.getDataFolder(), "banners");
        if (!folder.exists())
            folder.mkdirs();

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = file.getName().replace(".yml", "");
                String imageName = config.getString("image");
                Location loc = config.getLocation("location");
                int width = config.getInt("width");
                int height = config.getInt("height");
                BlockFace face = BlockFace.valueOf(config.getString("face", "NORTH"));
                List<String> left = config.getStringList("actions.left");
                List<String> right = config.getStringList("actions.right");

                List<UUID> entityUuids = config.getStringList("entities").stream()
                        .map(UUID::fromString).collect(Collectors.toList());
                List<Integer> mapIds = config.getIntegerList("mapIds");

                Banner banner = new Banner(name, imageName, loc, width, height, face, left, right, entityUuids, mapIds);
                activeBanners.put(name, banner);

                Bukkit.getScheduler().runTaskLater(plugin, () -> refreshBannerVisuals(banner), 60L);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load banner config: " + file.getName());
            }
        }
    }

    public void refreshBannerVisuals(Banner banner) {
        File imgFile = new File(plugin.getDataFolder(), "banners/images/" + banner.getImageName());
        if (!imgFile.exists()) {
            plugin.getLogger().warning("Banner image not found: " + banner.getImageName());
            return;
        }

        try {
            BufferedImage fullImage = ImageUtils.loadAndScale(imgFile, banner.getWidth(), banner.getHeight());
            spawnBannerEntities(banner, fullImage);
            plugin.getLogger().info("Banner '" + banner.getName() + "' visuals refreshed.");
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading banner " + banner.getName() + ": " + e.getMessage());
        }
    }

    public void createBanner(String name, String imageName, Location pos1, Location pos2, BlockFace face,
            List<String> leftActions, List<String> rightActions) {

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int blocksWidth;
        int blocksHeight = (maxY - minY) + 1;

        if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            blocksWidth = (maxX - minX) + 1;
        } else {
            blocksWidth = (maxZ - minZ) + 1;
        }

        // FIXED startLoc: Persfektif Kiri-Atas Penonton
        Location startLoc;
        switch (face) {
            case NORTH -> startLoc = new Location(pos1.getWorld(), maxX, maxY, minZ);
            case SOUTH -> startLoc = new Location(pos1.getWorld(), minX, maxY, maxZ);
            case EAST -> startLoc = new Location(pos1.getWorld(), maxX, maxY, maxZ);
            case WEST -> startLoc = new Location(pos1.getWorld(), minX, maxY, minZ);
            default -> startLoc = new Location(pos1.getWorld(), minX, maxY, minZ);
        }

        Banner banner = new Banner(name, imageName, startLoc, blocksWidth, blocksHeight, face, leftActions,
                rightActions);
        activeBanners.put(name, banner);
        saveBanner(banner);
        refreshBannerVisuals(banner);
    }

    private void spawnBannerEntities(Banner banner, BufferedImage fullImage) {
        cleanupBannerEntities(banner);
        banner.getEntityUuids().clear();

        List<Integer> existingMapIds = new ArrayList<>(banner.getMapIds());
        List<Integer> newMapIds = new ArrayList<>();

        BlockFace face = banner.getFace();
        double dx = 0, dz = 0;
        double ox = 0.5, oz = 0.5; // Start at center of block face
        float yaw = 0;

        // FIXED ORIENTATION & OFFSET (0.02) to avoid "inside block" / z-fighting
        switch (face) {
            case NORTH -> {
                yaw = 180;
                dx = -1;
                oz = -0.02;
            }
            case SOUTH -> {
                yaw = 0;
                dx = 1;
                oz = 1.02;
            }
            case EAST -> {
                yaw = 270;
                dz = -1;
                ox = 1.02;
            }
            case WEST -> {
                yaw = 90;
                dz = 1;
                ox = -0.02;
            }
            default -> {
            }
        }

        Location origin = banner.getLocation().clone().add(ox, 0.5, oz);
        org.bukkit.World world = banner.getLocation().getWorld();
        if (world == null)
            return;

        int mapIndex = 0;
        for (int row = 0; row < banner.getHeight(); row++) {
            for (int col = 0; col < banner.getWidth(); col++) {
                Location mapLoc = origin.clone().add(col * dx, -row, col * dz);

                MapView view = null;
                if (mapIndex < existingMapIds.size()) {
                    view = Bukkit.getMap(existingMapIds.get(mapIndex).intValue());
                }

                if (view == null) {
                    view = Bukkit.createMap(world);
                }

                newMapIds.add((int) view.getId());

                view.setTrackingPosition(false);
                view.setUnlimitedTracking(false);
                view.setCenterX(0);
                view.setCenterZ(0);

                byte[] mapData = ImageUtils.convertToMapColors(ImageUtils.getMapPart(fullImage, col, row));
                renderMap(view, mapData);

                // BACK TO ItemDisplay: Precision positioning + Scaling to fix "fit"
                ItemDisplay display = world.spawn(mapLoc, ItemDisplay.class);
                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                if (meta != null) {
                    meta.setMapView(view);
                    mapItem.setItemMeta(meta);
                }

                display.setItemStack(mapItem);
                display.setBrightness(new Display.Brightness(15, 15));
                display.setRotation(yaw, 0);

                // Scale slightly larger than 1.0 (1.01) to remove gaps between maps
                Transformation trans = display.getTransformation();
                display.setTransformation(new Transformation(
                        trans.getTranslation(),
                        trans.getLeftRotation(),
                        new Vector3f(1.005f, 1.005f, 0.01f), // 1.005 to cover gaps, 0.01 thinness
                        trans.getRightRotation()));

                display.addScoreboardTag("naturalbanner_entity_" + banner.getName());
                banner.getEntityUuids().add(display.getUniqueId());
                mapIndex++;
            }
        }

        banner.getMapIds().clear();
        banner.getMapIds().addAll(newMapIds);

        // Interaction Hitboxes
        for (int col = 0; col < banner.getWidth(); col++) {
            double baseY = banner.getLocation().getY() - banner.getHeight() + 0.5;
            Location hitboxLoc = origin.clone().add(col * dx, 0, col * dz);
            hitboxLoc.setY(baseY);

            // Hitbox is slightly in FRONT of the map to capture clicks
            double hox = 0, hoz = 0;
            if (face == BlockFace.NORTH)
                hoz = -0.05;
            else if (face == BlockFace.SOUTH)
                hoz = 0.05;
            else if (face == BlockFace.EAST)
                hox = 0.05;
            else if (face == BlockFace.WEST)
                hox = -0.05;

            Interaction interaction = world.spawn(hitboxLoc.add(hox, 0, hoz), Interaction.class);
            interaction.setInteractionWidth(1.0f);
            interaction.setInteractionHeight((float) banner.getHeight());
            interaction.addScoreboardTag("naturalbanner_hitbox_" + banner.getName());

            banner.getEntityUuids().add(interaction.getUniqueId());
        }

        saveBanner(banner);
    }

    public void cleanupBannerEntities(Banner banner) {
        for (UUID uuid : banner.getEntityUuids()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null)
                entity.remove();
        }

        String entityTag = "naturalbanner_entity_" + banner.getName();
        String hitboxTag = "naturalbanner_hitbox_" + banner.getName();

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains(entityTag) ||
                        entity.getScoreboardTags().contains(hitboxTag)) {
                    entity.remove();
                }
            }
        }
    }

    public void purgeAllEntities() {
        int count = 0;
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                for (String tag : entity.getScoreboardTags()) {
                    if (tag.startsWith("naturalbanner_entity_") || tag.startsWith("naturalbanner_hitbox_")) {
                        entity.remove();
                        count++;
                        break;
                    }
                }
            }
        }
        plugin.getLogger().info("Purged " + count + " orphan banner entities.");
    }

    public void saveAll() {
        for (Banner banner : activeBanners.values()) {
            saveBanner(banner);
        }
    }

    private void saveBanner(Banner banner) {
        File file = new File(plugin.getDataFolder(), "banners/" + banner.getName() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("image", banner.getImageName());
        config.set("location", banner.getLocation());
        config.set("width", banner.getWidth());
        config.set("height", banner.getHeight());
        config.set("face", banner.getFace().name());
        config.set("actions.left", banner.getLeftClickActions());
        config.set("actions.right", banner.getRightClickActions());
        config.set("entities", banner.getEntityUuids().stream().map(UUID::toString).collect(Collectors.toList()));
        config.set("mapIds", banner.getMapIds());

        try {
            config.save(file);
        } catch (Exception ignored) {
        }
    }

    public void handleInteract(org.bukkit.entity.Player player, String bannerName, boolean isLeftClick) {
        Banner banner = activeBanners.get(bannerName);
        if (banner == null)
            return;

        List<String> actions = isLeftClick ? banner.getLeftClickActions() : banner.getRightClickActions();
        for (String action : actions) {
            String clean = action.trim();
            if (clean.length() < 6)
                continue;

            try {
                if (clean.startsWith("[URL]")) {
                    String url = clean.substring(5).trim();
                    if (!url.startsWith("http"))
                        url = "https://" + url;

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

                    TextComponent header = new TextComponent(
                            ChatUtils.colorize("\n&8&m      &f &b&lNATURAL SMP &f &8&m      "));
                    TextComponent body = new TextComponent(
                            ChatUtils.colorize("\n&7Silahkan klik teks di bawah ini untuk membuka halaman:"));

                    TextComponent link = new TextComponent(ChatUtils.colorize("\n&b&l▶ &n" + url + "\n"));
                    link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(ChatUtils.colorize("&fKlik untuk menuju:\n&b" + url)).create()));

                    TextComponent footer = new TextComponent(
                            ChatUtils.colorize("&8&m                                     \n"));

                    player.spigot().sendMessage(header, body, link, footer);
                } else if (clean.startsWith("[COMMAND]")) {
                    String cmd = clean.substring(9).trim().replace("%player%", player.getName());
                    if (!cmd.isEmpty())
                        Bukkit.dispatchCommand(player, cmd);
                } else if (clean.startsWith("[CONSOLE]")) {
                    String cmd = clean.substring(9).trim().replace("%player%", player.getName());
                    if (!cmd.isEmpty())
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to execute action for banner " + bannerName + ": " + clean);
            }
        }
    }

    private void renderMap(MapView view, byte[] mapData) {
        view.getRenderers().clear();
        view.addRenderer(new MapRenderer() {
            @Override
            public void render(@NotNull MapView map, @NotNull MapCanvas canvas,
                    @NotNull org.bukkit.entity.Player player) {
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        canvas.setPixel(x, y, mapData[y * 128 + x]);
                    }
                }
            }
        });
    }

    public void editBanner(String name, String newImage, List<String> newLeft, List<String> newRight) {
        Banner old = activeBanners.get(name);
        if (old == null)
            return;

        Banner updated = new Banner(name, newImage != null ? newImage : old.getImageName(),
                old.getLocation(), old.getWidth(), old.getHeight(), old.getFace(),
                newLeft != null ? newLeft : old.getLeftClickActions(),
                newRight != null ? newRight : old.getRightClickActions(),
                old.getEntityUuids(), old.getMapIds());

        activeBanners.put(name, updated);
        saveBanner(updated);
        refreshBannerVisuals(updated);
    }

    public void deleteBanner(String name) {
        Banner banner = activeBanners.get(name);
        if (banner != null)
            cleanupBannerEntities(banner);
        activeBanners.remove(name);
        File file = new File(plugin.getDataFolder(), "banners/" + name + ".yml");
        if (file.exists())
            file.delete();
    }

    public Map<String, Banner> getActiveBanners() {
        return activeBanners;
    }

    public NaturalCore plugin() {
        return plugin;
    }
}
