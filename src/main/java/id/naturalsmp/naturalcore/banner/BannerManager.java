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
                plugin.getLogger().severe("Error loading banner " + banner.getName() + ": " + e.getMessage());
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

        // Memory Warning for massive images
        if (imageFile.length() > 5 * 1024 * 1024) { // 5MB limit check
            plugin.getLogger()
                    .warning("Banner image '" + imageName + "' is quite large (" + (imageFile.length() / 1024 / 1024)
                            + "MB). " +
                            "This may cause server lag or memory issues if many are loaded.");
        }

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

        Location startLoc = new Location(pos1.getWorld(), minX, maxY, minZ);
        if (face == BlockFace.WEST)
            startLoc = new Location(pos1.getWorld(), minX, maxY, maxZ);
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
                oz = -0.01;
            } // Slimmer offset
            case SOUTH -> {
                yaw = 0;
                dx = -1;
                oz = 1.01;
            }
            case EAST -> {
                yaw = 270;
                dz = 1;
                ox = 1.01;
            }
            case WEST -> {
                yaw = 90;
                dz = -1;
                ox = -0.01;
            }
            default -> {
            }
        }

        Location origin = banner.getLocation().clone().add(ox, 0.5, oz);
        org.bukkit.World world = banner.getLocation().getWorld();

        for (int row = 0; row < banner.getHeight(); row++) {
            for (int col = 0; col < banner.getWidth(); col++) {
                Location mapLoc = origin.clone().add(col * dx, -row, col * dz);

                MapView view = Bukkit.createMap(world);

                // PERBAIKAN VISUAL 1: Setting MapView agar statis
                view.setTrackingPosition(false);
                view.setUnlimitedTracking(false);
                view.setCenterX(0);
                view.setCenterZ(0);

                byte[] mapData = ImageUtils.convertToMapColors(ImageUtils.getMapPart(fullImage, col, row));
                renderMap(view, mapData);

                ItemDisplay display = world.spawn(mapLoc, ItemDisplay.class);
                ItemStack mapItem = new ItemStack(Material.FILLED_MAP); // Pastikan ini FILLED_MAP
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                meta.setMapView(view); // Hubungkan view ke item
                mapItem.setItemMeta(meta);

                display.setItemStack(mapItem);
                display.setBrightness(new Display.Brightness(15, 15));
                display.setRotation(yaw, 0);
                display.addScoreboardTag("naturalbanner_entity_" + banner.getName());
            }
        }

        // Interaction Hitboxes (one for each block horizontally to handle orientation
        // correctly)
        // This solves the "Interaction is a square" issue.
        for (int col = 0; col < banner.getWidth(); col++) {
            double hH = (banner.getHeight() - 1) / 2.0;
            Location colCenter = origin.clone().add(col * dx, -hH, col * dz);

            // Adjust offset to put hitbox SLIGHTLY in front of the banner
            double hox = 0, hoz = 0;
            if (face == BlockFace.NORTH)
                hoz = -0.05;
            else if (face == BlockFace.SOUTH)
                hoz = 0.05;
            else if (face == BlockFace.EAST)
                hox = 0.05;
            else if (face == BlockFace.WEST)
                hox = -0.05;

            Interaction interaction = world.spawn(colCenter.add(hox, 0, hoz), Interaction.class);
            interaction.setInteractionWidth(1.0f); // 1 block wide
            interaction.setInteractionHeight(banner.getHeight());
            interaction.addScoreboardTag("naturalbanner_hitbox_" + banner.getName());
        }
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
        if (banner == null) return;

        List<String> actions = isLeftClick ? banner.getLeftClickActions() : banner.getRightClickActions();
        for (String action : actions) {
            String clean = action.trim();

            if (clean.startsWith("[URL]")) {
                player.sendMessage(ChatUtils.colorize("&b&n" + clean.substring(5).trim()));
            }
            else if (clean.startsWith("[COMMAND]")) {
                // PERBAIKAN CRASH: Cek apakah ada commandnya
                String cmdToRun = clean.substring(9).trim().replace("%player%", player.getName());
                if (!cmdToRun.isEmpty()) {
                    Bukkit.dispatchCommand(player, cmdToRun);
                }
            }
            else if (clean.startsWith("[CONSOLE]")) {
                // PERBAIKAN CRASH: Cek apakah ada commandnya
                String cmdToRun = clean.substring(9).trim().replace("%player%", player.getName());
                if (!cmdToRun.isEmpty()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
                }
            }
        }
    }

    private void renderMap(MapView view, byte[] mapData) {
        view.getRenderers().clear();
        view.addRenderer(new MapRenderer() {
            private boolean rendered = false;

            @Override
            public void render(@NotNull MapView map, @NotNull MapCanvas canvas,
                    @NotNull org.bukkit.entity.Player player) {
                if (rendered)
                    return; // Only draw once per player session to save CPU
                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        canvas.setPixel(x, y, mapData[y * 128 + x]);
                    }
                }
                rendered = true;
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
