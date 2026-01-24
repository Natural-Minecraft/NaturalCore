package id.naturalsmp.naturalcore.banner;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Banner {
    private final String name;
    private final String imageName;
    private final Location location; // Top-left corner
    private final int width; // In blocks (maps)
    private final int height; // In blocks (maps)
    private final BlockFace face;
    private final List<String> leftClickActions;
    private final List<String> rightClickActions;

    // NEW: Tracking for performance and persistence
    private final List<UUID> entityUuids;
    private final List<Integer> mapIds;
    private final Map<Integer, byte[]> mapDataCache = new HashMap<>();

    public Banner(String name, String imageName, Location location, int width, int height, BlockFace face,
            List<String> leftClickActions, List<String> rightClickActions) {
        this(name, imageName, location, width, height, face, leftClickActions, rightClickActions, new ArrayList<>(),
                new ArrayList<>());
    }

    public Banner(String name, String imageName, Location location, int width, int height, BlockFace face,
            List<String> leftClickActions, List<String> rightClickActions,
            List<UUID> entityUuids, List<Integer> mapIds) {
        this.name = name;
        this.imageName = imageName;
        this.location = location;
        this.width = width;
        this.height = height;
        this.face = face;
        this.leftClickActions = leftClickActions;
        this.rightClickActions = rightClickActions;
        this.entityUuids = entityUuids != null ? entityUuids : new ArrayList<>();
        this.mapIds = mapIds != null ? mapIds : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public Location getLocation() {
        return location;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BlockFace getFace() {
        return face;
    }

    public List<String> getLeftClickActions() {
        return leftClickActions;
    }

    public List<String> getRightClickActions() {
        return rightClickActions;
    }

    public List<UUID> getEntityUuids() {
        return entityUuids;
    }

    public List<Integer> getMapIds() {
        return mapIds;
    }

    public Map<Integer, byte[]> getMapDataCache() {
        return mapDataCache;
    }
}
