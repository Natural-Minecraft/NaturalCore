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
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

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

        // ASYNC TASK: Memproses gambar di background thread agar tidak menahan Server
        // Main Thread (No Lag)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                long start = System.currentTimeMillis();
                BufferedImage fullImage = ImageUtils.loadAndScale(imgFile, banner.getWidth(), banner.getHeight());

                // SYNC TASK: Kembali ke Main Thread untuk spawn entity (Wajib Sync)
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Safety check: pastikan banner masih ada dalam daftar aktif
                    if (!activeBanners.containsKey(banner.getName()))
                        return;

                    spawnBannerEntities(banner, fullImage);
                    long time = System.currentTimeMillis() - start;
                    plugin.getLogger().info("Banner '" + banner.getName() + "' visuals refreshed in " + time + "ms.");
                });
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading banner " + banner.getName() + ": " + e.getMessage());
            }
        });
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

        switch (face) {
            case NORTH -> {
                dx = -1;
            }
            case SOUTH -> {
                dx = 1;
            }
            case EAST -> {
                dz = -1;
            }
            case WEST -> {
                dz = 1;
            }
            default -> {
            }
        }

        Location origin = banner.getLocation().clone();
        org.bukkit.World world = banner.getLocation().getWorld();
        if (world == null)
            return;

        // SMART POP-OUT LOGIC:
        // Jika origin (posisi spawn) adalah blok solid, geser 1 blok ke arah face.
        if (origin.getBlock().getType().isSolid()) {
            origin.add(face.getModX(), face.getModY(), face.getModZ());
        }

        int mapIndex = 0;
        for (int row = 0; row < banner.getHeight(); row++) {
            for (int col = 0; col < banner.getWidth(); col++) {
                // GlowItemFrame harus di tengah block (x.5, y.5, z.5) untuk attach benar
                Location mapLoc = origin.clone().add(col * dx, -row, col * dz);

                // Pastikan koordinat integer untuk spawn
                Location spawnLoc = new Location(world, mapLoc.getBlockX(), mapLoc.getBlockY(), mapLoc.getBlockZ());

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

                // Use Cached Data if available, otherwise calculate and cache
                byte[] mapData = banner.getMapDataCache().get(mapIndex);
                if (mapData == null) {
                    mapData = ImageUtils.convertToMapColors(ImageUtils.getMapPart(fullImage, col, row));
                    banner.getMapDataCache().put(mapIndex, mapData);
                }

                renderMap(view, mapData);

                // --- GLOW ITEM FRAME (Native Map Rendering) ---
                GlowItemFrame frame = world.spawn(spawnLoc, GlowItemFrame.class);
                frame.setFacingDirection(face, true);

                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                if (meta != null) {
                    meta.setMapView(view);
                    mapItem.setItemMeta(meta);
                }

                frame.setItem(mapItem);
                frame.setInvisible(true); // Hanya gambar yang terlihat
                frame.setFixed(true); // Tidak bisa dihancurkan/diputar player

                // Rotation hack agar tidak terbalik (tergantung face)
                // ItemFrame otomatis handle rotasi map, tapi kadang perlu penyesuaian rotasi
                // item di dalamnya.
                // Default 0 biasanya sudah benar untuk map.

                frame.addScoreboardTag("naturalbanner_entity_" + banner.getName());
                banner.getEntityUuids().add(frame.getUniqueId());
                mapIndex++;
            }
        }

        banner.getMapIds().clear();
        banner.getMapIds().addAll(newMapIds);

        // Interaction Hitboxes (Tetap di depan frame)
        for (int col = 0; col < banner.getWidth(); col++) {
            double baseY = origin.getBlockY() - banner.getHeight() + 1.0; // Base Y adjusted
            Location hitboxLoc = origin.clone().add(col * dx, 0, col * dz);
            hitboxLoc.setY(baseY);

            // Pop hitbox slightly forward (0.1) from the frame face
            double hox = 0, hoz = 0;
            if (face == BlockFace.NORTH)
                hoz = -0.1;
            else if (face == BlockFace.SOUTH)
                hoz = 1.1; // 1.0 block + 0.1 offset
            else if (face == BlockFace.EAST)
                hox = 1.1;
            else if (face == BlockFace.WEST)
                hox = -0.1;

            // Adjust origin back to center for hitbox spawn if needed, but relative calc is
            // safer
            // Hitbox spawn location needs to be precise floating point
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
            private final Set<UUID> renderedPlayers = new HashSet<>();

            @Override
            public void render(@NotNull MapView map, @NotNull MapCanvas canvas,
                    @NotNull org.bukkit.entity.Player player) {
                // RENDER-ONCE LOGIC: Only draw pixels if this player hasn't seen it yet
                if (renderedPlayers.contains(player.getUniqueId()))
                    return;

                for (int y = 0; y < 128; y++) {
                    for (int x = 0; x < 128; x++) {
                        canvas.setPixel(x, y, mapData[y * 128 + x]);
                    }
                }

                renderedPlayers.add(player.getUniqueId());
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

    public void hideAllBanners(Player player) {
        // Run slightly later to ensure player is fully in world
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline())
                return;

            int count = 0;
            for (Banner banner : activeBanners.values()) {
                for (UUID uuid : banner.getEntityUuids()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity != null) {
                        player.hideEntity(plugin, entity);
                        count++;
                    }
                }
            }
            if (count > 0) {
                // Optional debug
                // plugin.getLogger().info("Hidden " + count + " banner entities for Bedrock
                // player " + player.getName());
            }
        }, 20L); // 1 second delay
    }

    public NaturalCore plugin() {
        return plugin;
    }
}
