package org.sehes.tetris.graphic;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.config.GhostType;
import org.sehes.tetris.controller.GameSnapshot;
import org.sehes.tetris.model.BoardView;
import org.sehes.tetris.model.Tetromino;
import org.sehes.tetris.model.TetrominoType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The TetrisDrawingHandler class is responsible for rendering the game state
 * onto the screen. It provides methods to initialize the graphics context, draw
 * the game grid, and render the current Tetromino piece based on the game
 * board's state. The drawing handler interacts with the GameManager to retrieve
 * necessary information about the game state and ensures that the visual
 * representation of the game is accurate and up to date.
 */
@NullMarked
public class TetrisDrawingHandler implements Painter<GameSnapshot> {
    private final BufferedImage boardImg;
    private final RenderingHints renderingHints;
    private final AssetsManager assets;

    public TetrisDrawingHandler(RenderingHints renderingHints, AssetsManager assets) {
        this.assets = assets;
        this.renderingHints = renderingHints;
        boardImg = new BufferedImage(GameParameters.COLUMNS * Config.BLOCK_SIZE,
                GameParameters.VISIBLE_ROWS * Config.BLOCK_SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    private void paintGameBoard(Graphics2D g2d, BoardView boardView, boolean isBoardDirty) {
        if (isBoardDirty) {
            bakeBoardImg(boardView);
        }
        g2d.drawImage(boardImg, 0, 0, null);
    }

    /**
     * Bakes the game board image based on the current board view.
     *
     * @param boardView the current board view
     * @throws NullPointerException if the board view is null
     */
    private void bakeBoardImg(BoardView boardView) {
        Graphics2D g2d = boardImg.createGraphics();
        g2d.setRenderingHints(renderingHints);
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, boardImg.getWidth(), boardImg.getHeight());
        g2d.setComposite(AlphaComposite.SrcOver);
        for (int row = boardView.getHeight() - 1; row >= GameParameters.HIDDEN_ROWS; row--) {
            for (int col = boardView.getWidth() - 1; col >= 0; col--) {
                TetrominoType content = boardView.getBlockContent(row, col);
                if (content != null && content != TetrominoType.NON) {
                    int x = col * Config.BLOCK_SIZE;
                    int y = (row - GameParameters.HIDDEN_ROWS) * Config.BLOCK_SIZE;
                    g2d.drawImage(assets.getTile(content), x, y, null);
                }
            }
        }
        g2d.dispose();
    }

    /**
     * Draws the current Tetromino on the given Graphics2D object.
     * The Tetromino is drawn with its assigned color and at its current position
     * on the game board, specified by its pixel coordinates.
     * <p>
     *
     * @param g2d the Graphics2D object to draw on
     * @param t   the Tetromino to draw - cannot be null
     */
    private void drawCurrentTetromino(Graphics2D g2d, Tetromino t, int ghostDistance, GhostType ghostType, @Nullable Double lockTime) {
        if (ghostType != GhostType.NONE)
            drawGhostMino(g2d, t, ghostDistance, ghostType); // draw first so the real block is draw over not otherwise around
        final var tile = assets.getTile(t.getType());
        final int originX = getOriginX(t, tile);
        final int originY = getOriginY(t, tile);
        TetrominoRenderer.drawMinoAt(g2d, t.getStateCord(), tile, originX, originY, 0);

        if (lockTime != null) {
            TetrominoRenderer.lockDelayAnimation(g2d, lockTime, t.getStateCord(), tile, originX, originY);
        }
    }

    private void drawGhostMino(Graphics2D g2d, Tetromino t, int ghostOffset, GhostType ghostType) {
        final var PIXEL_OFFSET = ghostOffset * Config.BLOCK_SIZE;
        BufferedImage tile = assets.getGhostTile(ghostType);
        final int originX = getOriginX(t, tile);
        final int originY = getOriginY(t, tile);
        TetrominoRenderer.drawMinoAt(g2d, t.getStateCord(), tile, originX, originY, PIXEL_OFFSET);

    }

    private int getOriginX(Tetromino t, BufferedImage tile) {
        return t.getPositionX() * tile.getWidth();
    }

    private int getOriginY(Tetromino t, BufferedImage tile) {
        return (t.getPositionY() - GameParameters.HIDDEN_ROWS) * tile.getHeight();
    }

    @Override
    public void paint(Graphics2D g, @Nullable GameSnapshot snapshot, int width, int height) {
        if (snapshot == null) {
            return;
        }
        final BoardView board = snapshot.boardView();
        final boolean wasBoardDirty = snapshot.isBoardDirty();
        paintGameBoard(g, board, wasBoardDirty);
        snapshot.currentTetromino().ifPresent(tetromino -> drawCurrentTetromino(g, tetromino, snapshot.ghostDistance(), snapshot.ghostType(), snapshot.lockTime()));
    }
}

