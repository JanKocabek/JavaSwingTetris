package org.sehes.tetris.graphic;

import org.jspecify.annotations.NullMarked;
import org.sehes.tetris.model.Coordinate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

@NullMarked
public class TetrominoRenderer {

    /**
     * Draws a tetromino at a specified pixel origin, applying optional normalization
     * of its local coordinate system.
     *
     * <p>This method is the fully parameterized version. It allows callers to specify
     * both the screen-space origin (originX, originY) and the local-space origin
     * (localOriginX, localOriginY). The local origin is typically the minimum X/Y
     * inside the tetromino's coordinate list, used to normalize rotated or preview
     * shapes so they start at (0,0).</p>
     *
     * @param g            the Graphics2D context used for drawing
     * @param coordinates  list of block coordinates in tetromino-local space
     * @param tile         the tile image used for each block
     * @param originX      pixel-space X origin where the tetromino is placed
     * @param originY      pixel-space Y origin where the tetromino is placed
     * @param localOriginX local-space X normalization offset (usually minX)
     * @param localOriginY local-space Y normalization offset (usually minY)
     * @param offsetY      additional vertical pixel offset (usually GhostPieceOffset)
     */
    static void drawMinoAt(Graphics2D g, List<Coordinate> coordinates, BufferedImage tile, int originX, int originY, int localOriginX, int localOriginY, int offsetY) {
        final var tileSize = tile.getWidth();
        for (var block : coordinates) {
            BlockCord cord = getBlockCord(originX, originY, localOriginX, localOriginY, offsetY, block, tileSize);
            g.drawImage(tile, cord.x(), cord.y(), null);
        }
    }

    /**
     * Overloaded version used for main board rendering.
     * and for GhostPiece Rendering
     *
     * <p>This variant assumes the tetromino's local coordinates already begin at (0,0),
     * which is true for standard board drawing. It forwards to the full method with
     * localOriginX and localOriginY set to zero.</p>
     * {@link #drawMinoAt(Graphics2D, List<Coordinate>, BufferedImage, int, int, int, int, int)}
     */
    static void drawMinoAt(Graphics2D g, List<Coordinate> coordinates, BufferedImage tile, int originX, int originY, int offsetY) {
        drawMinoAt(g, coordinates, tile, originX, originY, 0, 0, offsetY);
    }

    /**
     * Overloaded version used when normalization is required but no vertical offset is needed.
     *
     * <p>This is typically used for UI elements such as next-piece previews or hold-piece
     * rendering, where the tetromino should be normalized (localOriginX/localOriginY)
     * but drawn without additional board offset.</p>
     * {@link #drawMinoAt(Graphics2D, List<Coordinate>, BufferedImage, int, int, int, int, int)}
     */
    static void drawMinoAt(Graphics2D g, List<Coordinate> coordinates, BufferedImage tile, int originX, int originY, int localOriginX, int localOriginY) {
        drawMinoAt(g, coordinates, tile, originX, originY, localOriginX, localOriginY, 0);
    }

    static void lockDelayAnimation(Graphics2D g2d, double lockTimer, List<Coordinate> coordinates, BufferedImage tile, int originX, int originY) {
        final var tileSize = tile.getWidth();

        // Oscillate alpha between 0.1 (mostly transparent) and 0.9 (bright white)
        float alpha = getAlpha(lockTimer);
        // Isolate graphics settings using a temporary copy
        Graphics2D g = (Graphics2D) g2d.create();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(Color.WHITE);
        for (var block : coordinates) {
            BlockCord cord = getBlockCord(originX, originY, 0, 0, 0, block, tileSize);
            g.fillRect(cord.x(), cord.y(), tileSize, tileSize);
        }
        g.dispose(); // Clean up isolated graphics context
    }

    private static BlockCord getBlockCord(int originX, int originY, int localOriginX, int localOriginY, int offsetY, Coordinate block, int tileSize) {
        int x = originX + ((block.x() - localOriginX) * tileSize);
        int y = originY + ((block.y() - localOriginY) * tileSize) + offsetY;
        return new BlockCord(x, y);
    }

    /**
     * Returns the alpha value for the lock delay animation.
     * <p>
     * The function oscillates between 0.1 (mostly transparent) and 0.9 (bright white).
     * </p>
     *
     * @param lockTimer the time elapsed since the Tetromino was locked
     * @return alpha value representing the opacity of the lock delay animation
     */
    private static float getAlpha(double lockTimer) {
        final float frequency = 6.0f; // Speed of the pulse
        final var center = 0.5f;//centering the wave to be between 0.1 and 0.9
        final var amplitude = 0.4f;//how strong the pulse will be max is 0.5f
        return center + amplitude * (float) Math.sin(frequency * lockTimer);
    }

    private TetrominoRenderer() {
    }

    private record BlockCord(int x, int y) {

    }
}