package org.sehes.tetris.graphic;

import org.jspecify.annotations.NullMarked;
import org.sehes.tetris.model.Coordinate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

@NullMarked
public class TetrominoRenderer {

    // =========================================================================
    // Lock Delay Pulse Animation Configuration
    // =========================================================================

    /** Color of the overlay pulse (e.g. Color.BLACK for dimming, Color.WHITE for flashing). */
    private static final Color LOCK_PULSE_COLOR = Color.BLACK;

    /** Resting opacity at the trough of the pulse (0.0 = invisible). */
    private static final float LOCK_PULSE_MIN_ALPHA = 0.38f;

    /** Peak opacity at the crest of the pulse (1.0 = fully opaque). */
    private static final float LOCK_PULSE_MAX_ALPHA = 1.00f;

    /** Pulse frequency in radians per second ( π rad/s → ~1 flash per second). */
    private static final double LOCK_PULSE_FREQUENCY = Math.PI*2;

    /**
     * Exponent controlling pulse sharpness.
     * Higher values create a sharper, snappier flash with a longer rest period.
     */
    private static final int LOCK_PULSE_EXPONENT = 4;

    /** Pre-calculated opacity range: (MAX_ALPHA - MIN_ALPHA). */
    private static final float LOCK_PULSE_ALPHA_SPAN = LOCK_PULSE_MAX_ALPHA - LOCK_PULSE_MIN_ALPHA;

    static void lockDelayAnimation(Graphics2D g2d, double lockTimerSeconds, List<Coordinate> coordinates, BufferedImage tile, int originX, int originY) {
        final var tileSize = tile.getWidth();
        float alpha = calculateLockDelayAlpha(lockTimerSeconds);

        // Isolate graphics settings using a temporary copy
        Graphics2D g = (Graphics2D) g2d.create();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(LOCK_PULSE_COLOR);
        for (var block : coordinates) {
            BlockCord cord = getBlockCord(originX, originY, 0, 0, 0, block, tileSize);
            g.fillRect(cord.x(), cord.y(), tileSize, tileSize);
        }
        g.dispose(); // Clean up isolated graphics context
    }

    /**
     * Calculates the alpha (opacity) for the lock delay overlay pulse animation.
     * <p>
     * Uses a power-curved sine wave to produce a snappy pulse that peaks at
     * {@value #LOCK_PULSE_MAX_ALPHA} and rests at {@value #LOCK_PULSE_MIN_ALPHA}.
     * </p>
     *
     * @param elapsedSeconds elapsed time in seconds since the lock delay timer started
     * @return alpha value representing the opacity of the lock delay overlay
     */
    private static float calculateLockDelayAlpha(double elapsedSeconds) {
        final float sin = (float) Math.sin(LOCK_PULSE_FREQUENCY * elapsedSeconds);
        final float pulse = (float) Math.pow(Math.abs(sin), LOCK_PULSE_EXPONENT);
        return LOCK_PULSE_MIN_ALPHA + LOCK_PULSE_ALPHA_SPAN * pulse;
    }

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

    private static BlockCord getBlockCord(int originX, int originY, int localOriginX, int localOriginY, int offsetY, Coordinate block, int tileSize) {
        int x = originX + ((block.x() - localOriginX) * tileSize);
        int y = originY + ((block.y() - localOriginY) * tileSize) + offsetY;
        return new BlockCord(x, y);
    }

    private record BlockCord(int x, int y) {
    }

    private TetrominoRenderer() {
    }
}
