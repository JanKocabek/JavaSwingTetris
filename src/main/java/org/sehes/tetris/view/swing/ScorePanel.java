package org.sehes.tetris.view.swing;

import org.jspecify.annotations.NonNull;
import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.model.score.ScoreInfoDTO;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ScorePanel extends JPanel {

    private static final int SCORE_BORDER_THICK = 2;
    private static final String EMPTY = " ";
    private final ScoreLabel scoreUI;
    private final LevelLabel levelUI;
    private final Font scoreFont = new Font(Font.MONOSPACED, Font.BOLD, 20);
    private final JLabel clearingInfo = new JLabel(EMPTY, SwingConstants.CENTER);
    private final JLabel comboInfo = new JLabel(EMPTY, SwingConstants.CENTER);
    private final JLabel b2bInfo = new JLabel(EMPTY, SwingConstants.CENTER);
    private final Observer<ScoreInfoDTO> scoreObserver = this::onScoreChange;
    private final Observer<Integer> levelObserver = this::onLevelChange;

    public ScorePanel() {
        super();
        int row = 0;
        setOpaque(true);
        setBackground(Color.WHITE);
        //GridBagConstraints for stacking vertically
        setLayout(new GridBagLayout());
        this.scoreUI = new ScoreLabel();
        this.levelUI = new LevelLabel();
        final GridBagConstraints verStackConstr = createVerStackConstr();
        setupCompGridPos(verStackConstr, row++, new Insets(0, 0, 0, 0));
        add(scoreUI, verStackConstr);
        setupCompGridPos(verStackConstr, row++, new Insets(0, 0, 0, 0));
        add(levelUI, verStackConstr);
        setupCompGridPos(verStackConstr, row++, new Insets(0, 0, 2, 0));
        add(b2bInfo, verStackConstr);
        setupCompGridPos(verStackConstr, row++, new Insets(0, 0, 4, 0));
        //      clearingInfo.setOpaque(true);
//        clearingInfo.setBackground(Color.BLACK);
        add(clearingInfo, verStackConstr);
        setupCompGridPos(verStackConstr, row++, new Insets(0, 0, 4, 0));
//        comboInfo.setOpaque(true);
//        comboInfo.setBackground(Color.BLUE);
        add(comboInfo, verStackConstr);
        //uncomment if needed move up
//        GridBagConstraints fillerConstraints = new GridBagConstraints();
//        fillerConstraints.gridy = 4;
//        fillerConstraints.weighty = 1.0; // Pushes everything above it to the top
//        add(javax.swing.Box.createVerticalGlue(), fillerConstraints);
    }

    public Observer<ScoreInfoDTO> ScoreObserver() {
        return scoreObserver;
    }

    public Observer<Integer> levelObserver() {
        return levelObserver;
    }

    private void setupCompGridPos(GridBagConstraints constraints, int row, Insets insets) {
        constraints.gridy = row;
        constraints.insets = insets;
    }

    private @NonNull GridBagConstraints createVerStackConstr() {
        final GridBagConstraints verStackConstr = new GridBagConstraints();
        verStackConstr.gridx = 0;
        verStackConstr.fill = GridBagConstraints.HORIZONTAL;
        verStackConstr.weightx = 1.0;
        return verStackConstr;
    }


    private void onScoreChange(ScoreInfoDTO info) {
        scoreUI.updateScore(info.score());
        final var b2b = info.B2BBonus() ? "B2B" : EMPTY;
        b2bInfo.setText(b2b);
        if (!info.clearType().name().equals("NONE"))
            clearingInfo.setText("%s".formatted(info.clearType().toString().replace("_", EMPTY)));
        else clearingInfo.setText(EMPTY);
        if (info.combo() > 0) {
            comboInfo.setText("Combo x" + info.combo());
        } else {
            comboInfo.setText(EMPTY);
        }
    }
    private void onLevelChange(int level) {
        levelUI.updateLevel(level);
    }

    /**
     * Inner class representing the score label within the ScorePanel. It
     * extends JLabel and is responsible for displaying the current score in a
     * formatted manner. The label is styled with a monospaced font and has a
     * specific background and foreground color to enhance visibility. The
     * ScoreLabel class provides methods to update the displayed score and reset
     * it to zero when needed, ensuring that the score display is always
     * accurate and visually consistent with the overall design of the
     * ScorePanel.
     */
    private class ScoreLabel extends JLabel {
        private static final String SCORE_FORMAT = "%07d";

        ScoreLabel() {
            setOpaque(true);
            setFont(scoreFont);
            setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, SCORE_BORDER_THICK);
            Border lineBorderWithTitle = BorderFactory.createTitledBorder(lineBorder, "Score", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, scoreFont, Color.BLACK);
            setBorder(lineBorderWithTitle);
            setText(String.format(SCORE_FORMAT, 0));
        }

        private void updateScore(int score) {
            setText(String.format(SCORE_FORMAT, score));
        }
    }

    private class LevelLabel extends JLabel {
        private static final String LEVEL_FORMAT = "Level %02d";
        LevelLabel() {setOpaque(true);
            setFont(scoreFont);
            setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, SCORE_BORDER_THICK);
            Border lineBorderWithTitle = BorderFactory.createTitledBorder(lineBorder, "Level", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, scoreFont, Color.BLACK);
            setBorder(lineBorderWithTitle);
            setText(String.format(LEVEL_FORMAT, 1));}

        private void updateLevel(int level) {
            setText(String.format(LEVEL_FORMAT, level));
        }
    }
}
