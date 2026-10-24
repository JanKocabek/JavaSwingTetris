package org.sehes.tetris.view.swing;

import org.sehes.tetris.controller.GameState;
import org.sehes.tetris.controller.Observer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/**
 * The info panel is the bottom panel of the tetris game. It is used to display
 * the current state of the game, such as new game, paused, game over, etc.
 */
public class InfoPanel extends JPanel {

    private final FpsLabel fpsLabel;
    private final InfoLabel infoLabel;

    InfoPanel() {
        super();

        setOpaque(true);
        setBackground(Color.green);
        setLayout(new BorderLayout());
        infoLabel = new InfoLabel();
        fpsLabel = new FpsLabel();
        add(infoLabel, BorderLayout.CENTER);
        add(fpsLabel, BorderLayout.EAST);
    }

    public Observer<GameState> infoUpdateObserver() {
        return infoLabel.infoObserver;
    }

    public Observer<Integer> fpsUpdateObserver() {
        return fpsLabel.fpsObserver;
    }


    private static class FpsLabel extends JLabel {
        private static final String BASE_STRING = "FPS: ";
        private static final Font FPS_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        private final Observer<Integer> fpsObserver = this::update;

        FpsLabel() {
            setFont(FPS_FONT);
            setForeground(Color.BLACK);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setVerticalAlignment(SwingConstants.TOP);
            setText(BASE_STRING + "0");
            setBorder(new EmptyBorder(0, 0, 0, 8));
        }

        private void update(Integer state) {
            setText(BASE_STRING + (int) state);
        }


    }

    private static class InfoLabel extends JLabel {

        private static final Font INFO_FONT = new Font(Font.SERIF, Font.BOLD, 18);
        private final Observer<GameState> infoObserver = this::update;

        private InfoLabel() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(INFO_FONT);
            setForeground(Color.black);
            setText("game is loading wait please");
            Border border = new EmptyBorder(5, 0, 10, 0);
            setBorder(border);
        }


        private void update(GameState state) {
            switch (state) {
                case INIT -> setText("game is loading wait please");

                case PREPARED -> setText("press enter to start a new game");

                case PLAYING -> setText("game is running");

                case PAUSED -> setText("game is paused - press enter to resume");

                case GAME_OVER -> setText("nobody can survive forever - press enter to start a new game");

                default -> setText("UNEXPECTED MOMENT send me bug report");

            }
        }
    }

}


