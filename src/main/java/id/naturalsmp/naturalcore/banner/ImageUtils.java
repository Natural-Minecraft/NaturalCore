package id.naturalsmp.naturalcore.banner;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    /**
     * Loads an image and scales it to fit the specified dimensions (width * 128,
     * height * 128).
     */
    public static BufferedImage loadAndScale(File file, int blocksWidth, int blocksHeight) throws IOException {
        BufferedImage original = ImageIO.read(file);
        if (original == null)
            throw new IOException("Could not read image file: " + file.getName());

        int targetWidth = blocksWidth * 128;
        int targetHeight = blocksHeight * 128;

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();

        // Quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaled;
    }

    /**
     * Extracts a 128x128 sub-image from a larger image.
     */
    public static BufferedImage getMapPart(BufferedImage fullImage, int col, int row) {
        return fullImage.getSubimage(col * 128, row * 128, 128, 128);
    }
}
