package id.naturalsmp.naturalcore.banner;

import id.naturalsmp.naturalcore.NaturalCore;
import id.naturalsmp.naturalcore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

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

        File imagesFolder = new File(folder, "images");
        if (!imagesFolder.exists())
            imagesFolder.mkdirs();

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = file.getName().replace(".yml", "");
            String imageName = config.getString("image");
            Location loc = config.getLocation("location");
            int width = config.getInt("width");
            int height = config.getInt("height");
            BlockFace face = BlockFace.valueOf(config.getString("face", "NORTH"));
            List<String> left = config.getStringList("actions.left");
            List<String> right = config.getStringList("actions.right");

            Banner banner = new Banner(name, imageName, loc, width, height, face, left, right);
            activeBanners.put(name, banner);

            Bukkit.getScheduler().runTaskLater(plugin, () -> refreshBannerVisuals(banner), 100L);
        }
    }

    public void refreshBannerVisuals(Banner banner) {
        File imgFile = new File(plugin.getDataFolder(), "banners/images/" + banner.getImageName());
        if (imgFile.exists()) {
            try {
                BufferedImage fullImage = ImageUtils.loadAndScale(imgFile, banner.getWidth(), banner.getHeight());
                spawnBannerEntities(banner, fullImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createBanner(String name, String imageName, Location pos1, Location pos2, BlockFace face,
            List<String> leftActions, List<String> rightActions) {
        File imageFile = new File(plugin.getDataFolder(), "banners/images/" + imageName);
        if (!imageFile.exists()) {
            plugin.getLogger().warning("Image not found: " + imageName);
            return;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int blocksWidth;
        int blocksHeight = (maxY - minY) + 1;

        // Use the face to determine if selection is width-wise along X or Z
        if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            blocksWidth = (maxX - minX) + 1;
        } else {
            blocksWidth = (maxZ - minZ) + 1;
        }

        // Location correction based on face
        Location startLoc = new Location(pos1.getWorld(), minX, maxY, minZ);
        if (face == BlockFace.WEST)
            startLoc = new Location(pos1.getWorld(), minX, maxY, minZ);
        if (face == BlockFace.EAST)
            startLoc = new Location(pos1.getWorld(), maxX, maxY, minZ);
        if (face == BlockFace.NORTH)
            startLoc = new Location(pos1.getWorld(), minX, maxY, minZ);
        if (face == BlockFace.SOUTH)
            startLoc = new Location(pos1.getWorld(), maxX, maxY, maxZ);

        try {
            BufferedImage fullImage = ImageUtils.loadAndScale(imageFile, blocksWidth, blocksHeight);
            Banner banner = new Banner(name, imageName, startLoc, blocksWidth, blocksHeight, face, leftActions,
                    rightActions);

            spawnBannerEntities(banner, fullImage);
            activeBanners.put(name, banner);
            saveBanner(banner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnBannerEntities(Banner banner, BufferedImage fullImage) {
        cleanupBannerEntities(banner.getName());
        BlockFace face = banner.getFace();

        float yaw = 0;
        double dx = 0, dz = 0; // Direction for spreading columns
        double ox = 0, oz = 0; // Local offset from block face

        switch (face) {
            case NORTH -> {
                yaw = 180;
                dx = 1;
                oz = -0.05;
            }
            case SOUTH -> {
                yaw = 0;
                dx = -1;
                oz = 1.05;
            }
            case EAST -> {
                yaw = 270;
                dz = 1;
                ox = 1.05;
            }
            case WEST -> {
                yaw = 90;
                dz = -1;
                ox = -0.05;
            }
            default -> {
            }
        }

        Location origin = banner.getLocation().clone().add(ox, 0.5, oz);

        for (int row = 0; row < banner.getHeight(); row++) {
            for (int col = 0; col < banner.getWidth(); col++) {
                Location mapLoc = origin.clone().add(col * dx, -row, col * dz);

                MapView view = Bukkit.createMap(banner.getLocation().getWorld());
                renderMap(view, ImageUtils.getMapPart(fullImage, col, row));

                ItemDisplay display = mapLoc.getWorld().spawn(mapLoc, ItemDisplay.class);
                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                meta.setMapView(view);
                mapItem.setItemMeta(meta);

                display.setItemStack(mapItem);
                display.setBrightness(new Display.Brightness(15, 15));
                display.setRotation(yaw, 0);
                display.addScoreboardTag("naturalbanner_entity_" + banner.getName());
            }
        }

        // Interaction Hitbox
        double halfW = (banner.getWidth() - 1) / 2.0;
        double halfH = (banner.getHeight() - 1) / 2.0;
        Location center = origin.clone().add(halfW * dx, -halfH, halfW * dz);

        Interaction interaction = center.getWorld().spawn(center, Interaction.class);
        interaction.setInteractionWidth(banner.getWidth());
        interaction.setInteractionHeight(banner.getHeight());
        interaction.addScoreboardTag("naturalbanner_hitbox_" + banner.getName());
    }

    public void cleanupBannerEntities(String name) {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains("naturalbanner_entity_" + name) ||
                        entity.getScoreboardTags().contains("naturalbanner_hitbox_" + name)) {
                    entity.remove();
                }
            }
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
            if (clean.startsWith("[URL]")) {
                player.sendMessage(ChatUtils.colorize("&b&n" + clean.substring(5).trim()));
            } else if (clean.startsWith("[COMMAND]")) {
                Bukkit.dispatchCommand(player, clean.substring(9).trim().replace("%player%", player.getName()));
            } else if (clean.startsWith("[CONSOLE]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        clean.substring(9).trim().replace("%player%", player.getName()));
            }
        }
    }

    private void renderMap(MapView view, BufferedImage imagePart) {
        view.getRenderers().clear();
        view.addRenderer(new MapRenderer() {
            @Override
            public void render(@NotNull MapView map, @NotNull MapCanvas canvas,
                    @NotNull org.bukkit.entity.Player player) {
                canvas.drawImage(0, 0, imagePart);
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
                newRight != null ? newRight : old.getRightClickActions());

        activeBanners.put(name, updated);
        saveBanner(updated);
        refreshBannerVisuals(updated);
    }

    public void deleteBanner(String name) {
        cleanupBannerEntities(name);
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
