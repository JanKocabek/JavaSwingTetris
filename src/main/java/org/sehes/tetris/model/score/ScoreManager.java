package org.sehes.tetris.model.score;

import org.jspecify.annotations.NonNull;
import org.sehes.tetris.controller.GameState;
import org.sehes.tetris.controller.ObservableImpl;
import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.util.event.Observable;

public class ScoreManager {
    private static final byte COMBO_EMPTY = -1;
    private final Observable.Publisher<ScoreInfoDTO> scoreInfoObservable = new ObservableImpl<>();
    private int score;
    /**
     * Whether a difficult clear has started a B2B chain.
     */
    private boolean isBackToBackChain;
    private int combo = COMBO_EMPTY;

    public Observable<ScoreInfoDTO> scoreInfoObservable() {
        return scoreInfoObservable;
    }

    public Observer<ScoreEvent> scoringObserver() {
        return this::onScoringEvent;
    }

    public Observer<GameState> gameStateObserver() {
        return this::onGameStateChange;
    }

    private void onScoringEvent(ScoreEvent event) {
        updateScore(event);
    }

    private void onGameStateChange(GameState state) {
        if (state == GameState.NEW_GAME) {
            resetScore();
        }
    }

    private void resetScore() {
        score = 0;
        combo = COMBO_EMPTY;
        isBackToBackChain = false;
        scoreInfoObservable.notify(new ScoreInfoDTO(score, ScoreLineClearType.NONE, combo, false));
    }

    public void updateScore(ScoreEvent scoreEvent) {
        ScoreLineClearType clearType = ScoreLineClearType.NONE;
        boolean isBackToBackBonus = false;
        int points = switch (scoreEvent) {
            case SoftDropEvent(int cell) -> cell;
            case HardDropEvent(int cell) -> cell * 2;
            case LockPieceEvent(int clearedLines, TSpin tspin, int level) -> {
                clearType = geLineClearType(clearedLines, tspin);
                boolean difficultClear = isDifficultClear(clearedLines, tspin);
                isBackToBackBonus = difficultClear && isBackToBackChain;
                isBackToBackChain = getBackToBackChain(clearedLines, difficultClear);
                yield lockPieceScoreCalculation(clearType, clearedLines, isBackToBackBonus, level);
            }
        };
        score += points;
        scoreInfoObservable.notify(new ScoreInfoDTO(score, clearType, combo, isBackToBackBonus));
    }

    private boolean getBackToBackChain(int clearedLines, boolean difficultClear) {
        return difficultClear || (clearedLines == 0 && isBackToBackChain);
    }

    private int lockPieceScoreCalculation(ScoreLineClearType clearType, int clearedLines, boolean isBackToBackBonus, int level) {
        combo = (clearedLines != 0) ? ++combo : COMBO_EMPTY;
        int baseScore = calculateBaseScore(clearType, level);
        final int scoreWBonus = isBackToBackBonus ? (int) (baseScore * 1.5) : baseScore;
        return combo == COMBO_EMPTY ? scoreWBonus : scoreWBonus + (combo * 50 * level);
    }

    private int calculateBaseScore(ScoreLineClearType clearType, int level) {
        int baseScore = switch (clearType) {
            case NONE -> 0;
            case SINGLE, MINI_T_SPIN_NO_LINES -> 100;
            case DOUBLE -> 300;
            case TRIPLE -> 500;
            case TETRIS, T_SPIN_SINGLE -> 800;
            case T_SPIN_DOUBLE -> 1200;
            case T_SPIN_TRIPLE -> 1600;
            case MINI_T_SPIN_SINGLE -> 200;
            case MINI_T_SPIN_DOUBLE, T_SPIN_NO_LINES -> 400;
        };
        return baseScore * level;
    }

    private boolean isDifficultClear(int clearedLines, TSpin tspin) {
        return clearedLines == 4 || (clearedLines > 0 && tspin != TSpin.NONE);
    }

    private ScoreLineClearType geLineClearType(int clearedLines, @NonNull TSpin tspin) {
        return switch (tspin) {
            case NONE -> switch (clearedLines) {
                case 0 -> ScoreLineClearType.NONE;
                case 1 -> ScoreLineClearType.SINGLE;
                case 2 -> ScoreLineClearType.DOUBLE;
                case 3 -> ScoreLineClearType.TRIPLE;
                case 4 -> ScoreLineClearType.TETRIS;
                default ->
                        throw new IllegalStateException("Invalid number of cleared Lines TSpin.NONE: %d, Error must happened in upfront calling  ".formatted(clearedLines));
            };
            case FULL -> switch (clearedLines) {
                case 0 -> ScoreLineClearType.T_SPIN_NO_LINES;
                case 1 -> ScoreLineClearType.T_SPIN_SINGLE;
                case 2 -> ScoreLineClearType.T_SPIN_DOUBLE;
                case 3 -> ScoreLineClearType.T_SPIN_TRIPLE;
                default ->
                        throw new IllegalStateException("Invalid number of cleared Lines with TSpin_FULL: %d, Error must happened in upfront calling  ".formatted(clearedLines));
            };
            case MINI -> switch (clearedLines) {
                case 0 -> ScoreLineClearType.MINI_T_SPIN_NO_LINES;
                case 1 -> ScoreLineClearType.MINI_T_SPIN_SINGLE;
                case 2 -> ScoreLineClearType.MINI_T_SPIN_DOUBLE;
                default ->
                        throw new IllegalStateException("Invalid number of cleared Lines with TSpin_MINI: %d, Error must happened in upfront calling  ".formatted(clearedLines));
            };
        };
    }

}
