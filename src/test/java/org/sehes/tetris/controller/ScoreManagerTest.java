package org.sehes.tetris.controller;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.model.score.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreManagerTest {

    private ScoreManager scoreManager;
    private ScoreMessenger scoreMessenger;

    private @NonNull LockPieceEvent createLockPieceEvent(int clearedLines, TSpin tSpin) {
        return new LockPieceEvent(clearedLines, tSpin, 1);
    }

    @BeforeEach
    void setUp() {
        scoreManager = new ScoreManager();
        scoreMessenger = new ScoreMessenger();
        // Connect observer pattern
        scoreMessenger.addObserver(scoreManager.scoringObserver());
    }

    @Test
    void testSoftDropScoring() {
        // given
        scoreMessenger.notifyObservers(new SoftDropEvent(5));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(5);
    }

    @Test
    void testHardDropScoring() {
        // given
        scoreMessenger.notifyObservers(new HardDropEvent(10));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(20); // 10 * 2
    }

    @Test
    void testLockPieceScoring_SingleLine() {
        // given
        scoreMessenger.notifyObservers(createLockPieceEvent(1, TSpin.NONE));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(100);
    }


    @Test
    void testLockPieceScoring_DoubleLine() {
        // given
        scoreMessenger.notifyObservers(createLockPieceEvent(2, TSpin.NONE));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(300);
    }

    @Test
    void testLockPieceScoring_TripleLine() {
        // given
        scoreMessenger.notifyObservers(createLockPieceEvent(3, TSpin.NONE));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(500);
    }

    @Test
    void testLockPieceScoring_Tetris() {
        // given
        List<ScoreInfoDTO> updates = new ArrayList<>();
        scoreManager.scoreInfoObservable().addObserver(updates::add);
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(800);
        assertThat(scoreManager).extracting("isBackToBackChain").isEqualTo(true);
        assertThat(updates).extracting(ScoreInfoDTO::B2BBonus).containsExactly(false);
    }

    @Test
    void testDoubleTetrisB2BLogic() {
        //arrange
        // act
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));
        // assert
        assertThat(scoreManager).extracting("isBackToBackChain").isEqualTo(true);
        assertThat(scoreManager).extracting("score").isEqualTo(2050);
    }

    @Test
    void sendsB2BToGuiOnlyForTheClearThatReceivesTheBonus() {
        List<ScoreInfoDTO> updates = new ArrayList<>();
        scoreManager.scoreInfoObservable().addObserver(updates::add);

        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));
        scoreMessenger.notifyObservers(new HardDropEvent(1));
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));

        assertThat(updates)
                .extracting(ScoreInfoDTO::B2BBonus)
                .containsExactly(false, false, true);
    }

    @Test
    void noLineClearDoesNotBreakABackToBackChain() {
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));
        scoreMessenger.notifyObservers(createLockPieceEvent(0, TSpin.FULL));
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));

        assertThat(scoreManager).extracting("score").isEqualTo(2400);
    }

    @Test
    void testBreakB2BWhenNotDifficultAfterTetris() {
        //arrange
        //act
        scoreMessenger.notifyObservers(createLockPieceEvent(4, TSpin.NONE));
        scoreMessenger.notifyObservers(createLockPieceEvent(3, TSpin.NONE));
        // assert
        assertThat(scoreManager).extracting("isBackToBackChain").isEqualTo(false);
    }

    @Test
    void testLockPieceScoring_NoLines() {
        // given
        scoreMessenger.notifyObservers(createLockPieceEvent(0, TSpin.NONE));

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(0);
    }

    @Test
    void testMultipleEventsAccumulate() {
        // given
        scoreMessenger.notifyObservers(new SoftDropEvent(3));
        scoreMessenger.notifyObservers(new HardDropEvent(5));
        scoreMessenger.notifyObservers(createLockPieceEvent(2, TSpin.NONE));

        // then: 3 + (5*2) + 300 = 313
        assertThat(scoreManager).extracting("score").isEqualTo(313);
    }

    @Test
    void testScoreResetOnGameStateChange() {
        // given
        scoreMessenger.notifyObservers(new SoftDropEvent(10));
        assertThat(scoreManager).extracting("score").isEqualTo(10);

        // when
        scoreManager.gameStateObserver().update(GameState.NEW_GAME);

        // then
        assertThat(scoreManager).extracting("score").isEqualTo(0);
    }
}
