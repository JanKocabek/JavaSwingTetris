package org.sehes.tetris.gui;

import org.sehes.tetris.controller.GameSnapshot;
import org.sehes.tetris.controller.GameState;
import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.score.ScoreInfoDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowEvent;

import static org.sehes.tetris.config.GameParameters.COLUMNS;
import static org.sehes.tetris.config.GameParameters.VISIBLE_ROWS;
import static org.sehes.tetris.graphic.Config.BLOCK_SIZE;

public class GuiFactory {
    public static final Dimension CANVAS_SIZE = new Dimension(BLOCK_SIZE * COLUMNS, BLOCK_SIZE * VISIBLE_ROWS);
    // Grid Layout Constants
    private static final int LEFT_COL = 0;
    private static final int CENTER_COL = 1;
    private static final int RIGHT_COL = 2;
    private static final int TOTAL_COLUMNS = 3;

    private GuiFactory() {
    }

    public static GameUI assembly(Painter<GameSnapshot> tetrisPainter, Painter<TetrominoType> previewPainter) {
        /*creation of ScorePanel is putted here if their be any needs of additional assembly in the future */

        final GameContainer gameContainer = assemblyGameContainer();
        final InfoPanel infoP = new InfoPanel();
        final SmallCanvas holdCanvas = new SmallCanvas(previewPainter);
        final LeftPanel leftP = new LeftPanel(holdCanvas);
        final SmallCanvas previewCanvas = new SmallCanvas(previewPainter);
        final RightPanel rightPanel = new RightPanel(previewCanvas);
        final MainPane mainPane = assemblyMainPane(gameContainer, rightPanel, infoP, leftP);
        final GameWindow window = new GameWindow(mainPane);
        final TetrisCanvas mainCanvas = new TetrisCanvas(tetrisPainter);
        gameContainer.add(mainCanvas, BorderLayout.CENTER);
        gameContainer.revalidate();
        gameContainer.repaint();
        window.revalidate();
        window.repaint();
        window.pack();
        window.setLocationRelativeTo(null);

        return new GameUI(mainCanvas, rightPanel.getScoreObserver(), infoP.infoUpdateObserver(), infoP.fpsUpdateObserver(), window, rightPanel.getPreviewObserver(), leftP.getHoldCanvasObserver());
    }

    public static void addKeyListener(KeyAdapter tetrisKeyAdapter, GameUI ui) {
        ui.canvas.addKeyListener(tetrisKeyAdapter);
    }

    private static MainPane assemblyMainPane(final GameContainer container, RightPanel rightP, InfoPanel infoP, LeftPanel leftP) {
        final MainPane pane = new MainPane(new GridBagLayout());

        // Column 0: Left Panel (Hold / Stats)
        pane.add(leftP, Gbc.at(LEFT_COL, 0)
                .weight(0, 1.0)
                .anchorThis(GridBagConstraints.NORTHWEST)
                .insetsThis(11, 8, 0, 4));

        // Column 1: Center Game Container
        pane.add(container, Gbc.at(CENTER_COL, 0)
                .weight(0, 0.0)
                .fillThis(GridBagConstraints.BOTH)
                .insetsThis(11, 4, 0, 4));

        // Column 2: Right Panel (Score / Preview)
        pane.add(rightP, Gbc.at(RIGHT_COL, 0)
                .weight(0, 1.0)
                .anchorThis(GridBagConstraints.NORTHEAST)
                .insetsThis(0, 4, 0, 8));

        // Row 1: Bottom Info Panel (Spans across all columns)
        pane.add(infoP, Gbc.at(LEFT_COL, 1)
                .span(TOTAL_COLUMNS, 1)
                .weight(1.0, 0.0)
                .fillThis(GridBagConstraints.BOTH)
                .anchorThis(GridBagConstraints.SOUTHWEST));

        return pane;

    }

    private static GameContainer assemblyGameContainer() {
        GameContainer container = new GameContainer(new BorderLayout());
        container.setPreferredSize(CANVAS_SIZE);
        return container;
    }

    public record GameUI(TetrisCanvas canvas, Observer<ScoreInfoDTO> scoreObserver, Observer<GameState> infoObserver,
                         Observer<Integer> fpsObserver, GameWindow window, Observer<TetrominoType> previewObserver,
                         Observer<TetrominoType> holdCanvasObserver) {
        public Runnable exitAction() {
            return () -> window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }

    }

    /**
     * A fluent builder wrapper around {@link GridBagConstraints} designed to make
     * layout constraints declarative, readable, and concise within GUI factory assemblies.
     */
    public static final class Gbc extends GridBagConstraints {

        private Gbc(int gridx, int gridy) {
            this.gridx = gridx;
            this.gridy = gridy;
        }

        /**
         * Creates a new fluent {@code Gbc} instance anchored at the specified grid position.
         *
         * @param gridx the initial column coordinate (0-indexed)
         * @param gridy the initial row coordinate (0-indexed)
         * @return a new {@code Gbc} instance
         */
        public static Gbc at(int gridx, int gridy) {
            return new Gbc(gridx, gridy);
        }

        /**
         * Sets the distribution weights for extra horizontal and vertical space.
         *
         * @param weightx the horizontal resizing weight (0.0 means fixed, higher values claim extra space)
         * @param weighty the vertical resizing weight (0.0 means fixed, higher values claim extra space)
         * @return this {@code Gbc} instance for method chaining
         */
        public Gbc weight(double weightx, double weighty) {
            this.weightx = weightx;
            this.weighty = weighty;
            return this;
        }

        /**
         * Sets the fill policy determining how the component is resized if its display area is larger than its requested size.
         *
         * @param fill the fill policy constant (e.g., {@link GridBagConstraints#BOTH}, {@link GridBagConstraints#HORIZONTAL})
         * @return this {@code Gbc} instance for method chaining
         */
        public Gbc fillThis(int fill) {
            this.fill = fill;
            return this;
        }

        /**
         * Sets the anchoring position for the component within its display area when it does not fill the entire space.
         *
         * @param anchor the anchor constant (e.g., {@link GridBagConstraints#NORTHEAST}, {@link GridBagConstraints#CENTER})
         * @return this {@code Gbc} instance for method chaining
         */
        public Gbc anchorThis(int anchor) {
            this.anchor = anchor;
            return this;
        }

        /**
         * Sets the number of columns and rows the component spans within the grid.
         *
         * @param gridwidth  the number of columns spanned (or relative constants like {@link GridBagConstraints#REMAINDER})
         * @param gridheight the number of rows spanned
         * @return this {@code Gbc} instance for method chaining
         */
        public Gbc span(int gridwidth, int gridheight) {
            this.gridwidth = gridwidth;
            this.gridheight = gridheight;
            return this;
        }

        /**
         * Sets the external padding (insets) around the component in pixels.
         *
         * @param top    the top inset padding
         * @param left   the left inset padding
         * @param bottom the bottom inset padding
         * @param right  the right inset padding
         * @return this {@code Gbc} instance for method chaining
         */
        public Gbc insetsThis(int top, int left, int bottom, int right) {
            this.insets = new Insets(top, left, bottom, right);
            return this;
        }
    }
}
