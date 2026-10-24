package org.sehes.tetris.view.swing;

import org.jspecify.annotations.NonNull;
import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.score.ScoreInfoDTO;

import javax.swing.*;
import java.awt.*;

public class RightPanel extends JPanel {
    private final ScorePanel scorePanel;
    private final SmallCanvas previewWindow;

    RightPanel(SmallCanvas nextCanvas) {
        super();
        final var nextLabel = createNextLabel();
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // setBorder(new LineBorder(Color.ORANGE, 2));
        scorePanel = new ScorePanel();
        this.previewWindow = nextCanvas;
        add(scorePanel);
        add(nextLabel);
        add(nextCanvas);
        add(Box.createVerticalGlue());
    }

    private @NonNull JLabel createNextLabel() {
        final var nextLabel = new JLabel("NEXT:");
        nextLabel.setOpaque(true);
        nextLabel.setBackground(Color.WHITE);
        return nextLabel;
    }

    public Observer<TetrominoType> getPreviewObserver() {
        return previewWindow.canvasObserver();
    }

    public Observer<ScoreInfoDTO> getScoreObserver() {
        return scorePanel.ScoreObserver();
    }

    public Observer<Integer> getLevelObserver() {
        return scorePanel.levelObserver();
    }

}
