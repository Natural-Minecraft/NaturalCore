package id.naturalsmp.naturalcore.banner;

import org.bukkit.map.MapPalette;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    /**
     * Loads an image and scales it to fit the specified dimensions (width * 128,
     * height * 128).
     * Now with memory-efficient scaling and palette conversion.
     */
    public static BufferedImage loadAndScale(File file, int blocksWidth, int blocksHeight) throws IOException {
        // LIMIT CHECK: Prevent loading massive images into memory
        // 50x50 maps is already a HUGE image (6400x6400 pixels)
        if (blocksWidth * blocksHeight > 2500) {
            throw new IOException("Image dimensions too large (" + blocksWidth + "x" + blocksHeight + "). " +
                    "Maximum total maps allowed is 2500 for safety.");
        }

        BufferedImage original = ImageIO.read(file);
        if (original == null)
            throw new IOException("Could not read image file: " + file.getName());

        int targetWidth = blocksWidth * 128;
        int targetHeight = blocksHeight * 128;

        // Scale image
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();

        // Quality vs Performance balance
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        // Clean up original immediately
        original.flush();

        return scaled;
    }

    /**
     * Converts a 128x128 sub-image to a byte array using Minecraft's map palette.
     * This is CRITICAL for performance.
     */
    @SuppressWarnings("deprecation")
    public static byte[] convertToMapColors(BufferedImage part) {
        byte[] colors = new byte[128 * 128];
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int color = part.getRGB(x, y);
                // MapPalette.matchColor is the standard way to find the closest MC color
                colors[y * 128 + x] = MapPalette.matchColor(new Color(color, true));
            }
        }
        return colors;
    }

    /**
     * Extracts a 128x128 sub-image from a larger image.
     */
    public static BufferedImage getMapPart(BufferedImage fullImage, int col, int row) {
        return fullImage.getSubimage(col * 128, row * 128, 128, 128);
    }
}
