package org.sehes.tetris.gui;

import org.sehes.tetris.controller.GameSnapshot;
import org.sehes.tetris.controller.Rendering;
import org.sehes.tetris.graphic.Config;

import javax.swing.JPanel;
import javax.swing.Painter;
import java.awt.Graphics;
import java.awt.Graphics2D;

import static org.sehes.tetris.gui.GuiFactory.CANVAS_SIZE;

/**
 * The TetrisCanvas class is responsible for rendering the game state onto the
 * screen. It extends JPanel and overrides the paintComponent method to draw the
 * game grid and the current Tetromino piece. The canvas interacts with the
 * GameManager to retrieve the current game state and update the display
 * accordingly. It also provides a method to repaint the canvas when the game
 * state changes, ensuring that the visual representation of the game is always
 * up to date.
 */
public class TetrisCanvas extends JPanel implements Rendering {

    private final transient Painter<GameSnapshot> painter;
    private transient GameSnapshot gameSnapshot;

    TetrisCanvas(Painter<GameSnapshot> painter) {
        this.setPreferredSize(CANVAS_SIZE);
        this.setMinimumSize(CANVAS_SIZE);
        this.setMaximumSize(CANVAS_SIZE);
        this.painter = painter;
        setBackground(Config.GAME_CANVAS_BCK_COLOR);
        this.setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        painter.paint((Graphics2D) g, gameSnapshot, getWidth(), getHeight());
    }

    /**
     * Repaints the canvas based on the provided game snapshot
     * @param gameSnapshot the snapshot of the current game state
     */
    @Override
    public void render(GameSnapshot gameSnapshot) {
        this.gameSnapshot = gameSnapshot;
        super.repaint();
    }


}
