package id.naturalsmp.naturalcore.banner;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import java.util.List;

public class Banner {
    private final String name;
    private final String imageName;
    private final Location location; // Top-left corner
    private final int width; // In blocks (maps)
    private final int height; // In blocks (maps)
    private final BlockFace face;
    private final List<String> leftClickActions;
    private final List<String> rightClickActions;

    public Banner(String name, String imageName, Location location, int width, int height, BlockFace face,
            List<String> leftClickActions, List<String> rightClickActions) {
        this.name = name;
        this.imageName = imageName;
        this.location = location;
        this.width = width;
        this.height = height;
        this.face = face;
        this.leftClickActions = leftClickActions;
        this.rightClickActions = rightClickActions;
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
}
