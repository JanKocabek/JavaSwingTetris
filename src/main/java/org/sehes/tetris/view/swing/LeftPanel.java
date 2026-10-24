package org.sehes.tetris.view.swing;

import org.jspecify.annotations.NonNull;
import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.model.TetrominoType;

import javax.swing.*;
import java.awt.*;

public class LeftPanel extends JPanel {

    private final SmallCanvas holdCanvas;

    LeftPanel(SmallCanvas HoldCanvas) {
        super();
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final var holdLabel=createHoldLabel();
        add(holdLabel);
        this.holdCanvas = HoldCanvas;
        add(holdCanvas);
    }
    private @NonNull JLabel createHoldLabel() {
        final var label=new JLabel("HOLD:");
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    public Observer<TetrominoType> getHoldCanvasObserver() {
        return holdCanvas.canvasObserver();
    }
}
