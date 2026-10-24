package org.sehes.tetris.view.swing;

import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.view.render.Config;

import javax.swing.*;
import java.awt.*;

public class SmallCanvas extends JPanel {
    private static final Dimension WINDOW_SIZE = new Dimension(Config.PREVIEW_BLOCK_SIZE * 5, Config.PREVIEW_BLOCK_SIZE * 5);
    private static final Color backgroundColor = new Color(15, 15, 25);
    private final Observer<TetrominoType> canvasObserver = this::onUpdate;
    private final Painter<TetrominoType> painter;
    private TetrominoType type;

    SmallCanvas(Painter<TetrominoType> painter) {
        this.painter = painter;
        this.setPreferredSize(WINDOW_SIZE);
        this.setMinimumSize(WINDOW_SIZE);
        this.setMaximumSize(WINDOW_SIZE);
        setBackground(backgroundColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final var g2d = (Graphics2D) g;
        painter.paint(g2d, type, getWidth(), getHeight());
    }

    public Observer<TetrominoType> canvasObserver() {
        return canvasObserver;
    }

    private void onUpdate(TetrominoType tetrominoType) {
        type = tetrominoType;
        repaint();
    }
}
