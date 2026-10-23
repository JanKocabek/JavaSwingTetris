package org.sehes.tetris.controller;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sehes.tetris.model.score.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreManagerTest {

    private ScoreManager scoreManager;
    private ScoreMessenger scoreMessenger;

    @BeforeEach
    void setUp() {
        scoreManager = new ScoreManager();
        scoreMessenger = new ScoreMessenger();
        // Connect observer pattern
        scoreMessenger.addObserver(scoreManager.scoringObserver());
    }

    @Nested
    @DisplayName("testing basic scoring without level")
    class ScoringTestsLevel1 {

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
            List<ScoreInfoDTO> updates = prepareScoreObserver();
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
            List<ScoreInfoDTO> updates = prepareScoreObserver();

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

        @Test
        void testComboClearingHappensAfterConsecutiveClears() {
            // given
            List<ScoreInfoDTO> updates = prepareScoreObserver();
            scoreMessenger.notifyObservers(createLockPieceEvent(1, TSpin.NONE));
            scoreMessenger.notifyObservers(createLockPieceEvent(1, TSpin.NONE));
            // then
            assertThat(updates).extracting(ScoreInfoDTO::combo).containsExactly(0, 1);
            assertThat(scoreManager).extracting("score").isEqualTo(250);
        }

        @Test
        void testComboIsStoppedAfterNonClearingLock() {
            // arrange
            List<ScoreInfoDTO> updates = prepareScoreObserver();
            // act
            scoreMessenger.notifyObservers(createLockPieceEvent(1, TSpin.NONE));
            scoreMessenger.notifyObservers(createLockPieceEvent(1, TSpin.NONE));
            scoreMessenger.notifyObservers(createLockPieceEvent(0, TSpin.NONE));
            scoreMessenger.notifyObservers(createLockPieceEvent(1, TSpin.NONE));
            //assert
            assertThat(updates).extracting(ScoreInfoDTO::combo).containsExactly(0, 1, -1, 0);
            assertThat(scoreManager).extracting("score").isEqualTo(350);
        }

    }

    @Nested
    @DisplayName("testing level scoring")
    class ScoringLevels {
        @Test
        void testLineClearOnLevel2() {
            // given
            List<ScoreInfoDTO> updates = prepareScoreObserver();
            // when
            scoreMessenger.notifyObservers(new LockPieceEvent(1, TSpin.NONE, 2));
            // then
            assertThat(updates).extracting(ScoreInfoDTO::score).containsExactly(200);
        }

        @Test
        void testDoubleClearOnLevel() {
            List<ScoreInfoDTO> updates = prepareScoreObserver();
            // when
            scoreMessenger.notifyObservers(new LockPieceEvent(2, TSpin.NONE, 2));
            // then
            assertThat(updates).extracting(ScoreInfoDTO::score).containsExactly(600);
        }

        @ParameterizedTest(name = "{0} lines, {1} at level {3} -> {2} with score {4}")
        @CsvSource({
                "0, NONE, NONE, 1, 0",
                "0, NONE, NONE, 3, 0",
                "1, NONE, SINGLE, 1, 100",
                "1, NONE, SINGLE, 3, 300",
                "2, NONE, DOUBLE, 1, 300",
                "2, NONE, DOUBLE, 3, 900",
                "3, NONE, TRIPLE, 1, 500",
                "3, NONE, TRIPLE, 3, 1500",
                "4, NONE, TETRIS, 1, 800",
                "4, NONE, TETRIS, 3, 2400",
                "0, MINI, MINI_T_SPIN_NO_LINES, 1, 100",
                "0, MINI, MINI_T_SPIN_NO_LINES, 3, 300",
                "1, MINI, MINI_T_SPIN_SINGLE, 1, 200",
                "1, MINI, MINI_T_SPIN_SINGLE, 3, 600",
                "2, MINI, MINI_T_SPIN_DOUBLE, 1, 400",
                "2, MINI, MINI_T_SPIN_DOUBLE, 3, 1200",
                "0, FULL, T_SPIN_NO_LINES, 1, 400",
                "0, FULL, T_SPIN_NO_LINES, 3, 1200",
                "1, FULL, T_SPIN_SINGLE, 1, 800",
                "1, FULL, T_SPIN_SINGLE, 3, 2400",
                "2, FULL, T_SPIN_DOUBLE, 1, 1200",
                "2, FULL, T_SPIN_DOUBLE, 3, 3600",
                "3, FULL, T_SPIN_TRIPLE, 1, 1600",
                "3, FULL, T_SPIN_TRIPLE, 3, 4800"
        })
        void testAllLineClearTypesScaledByLevel(int clearedLines, TSpin tSpin, ScoreLineClearType expectedClearType, int level, int expectedScore) {
            List<ScoreInfoDTO> updates = prepareScoreObserver();

            scoreMessenger.notifyObservers(new LockPieceEvent(clearedLines, tSpin, level));

            assertThat(updates).singleElement().satisfies(dto -> {
                assertThat(dto.clearType()).isEqualTo(expectedClearType);
                assertThat(dto.score()).isEqualTo(expectedScore);
            });
            assertThat(scoreManager).extracting("score").isEqualTo(expectedScore);
        }

        @Test
        void testBackToBackTetrisScaledByLevel() {
            // given
            int level = 3;
            List<ScoreInfoDTO> updates = prepareScoreObserver();

            // when: first Tetris at level 3 -> baseScore = 800 * 3 = 2400, combo = 0
            scoreMessenger.notifyObservers(new LockPieceEvent(4, TSpin.NONE, level));
            // then: second Tetris at level 3 -> baseScore = 2400 * 1.5 = 3600, combo = 1 (1 * 50 * 3 = 150) -> points = 3750, total = 6150
            scoreMessenger.notifyObservers(new LockPieceEvent(4, TSpin.NONE, level));

            assertThat(updates).extracting(ScoreInfoDTO::score).containsExactly(2400, 6150);
            assertThat(updates).extracting(ScoreInfoDTO::B2BBonus).containsExactly(false, true);
            assertThat(scoreManager).extracting("score").isEqualTo(6150);
        }

        @Test
        void testBackToBackTSpinScaledByLevel() {
            // given
            int level = 2;
            List<ScoreInfoDTO> updates = prepareScoreObserver();

            // when: first T-Spin Double at level 2 -> baseScore = 1200 * 2 = 2400, combo = 0
            scoreMessenger.notifyObservers(new LockPieceEvent(2, TSpin.FULL, level));
            // second T-Spin Triple at level 2 -> baseScore = (1600 * 2) * 1.5 = 4800, combo = 1 (1 * 50 * 2 = 100) -> points = 4900, total = 7300
            scoreMessenger.notifyObservers(new LockPieceEvent(3, TSpin.FULL, level));

            assertThat(updates).extracting(ScoreInfoDTO::score).containsExactly(2400, 7300);
            assertThat(updates).extracting(ScoreInfoDTO::B2BBonus).containsExactly(false, true);
            assertThat(scoreManager).extracting("score").isEqualTo(7300);
        }

        @Test
        void testComboBonusScaledByLevel() {
            // given
            int level = 4;
            List<ScoreInfoDTO> updates = prepareScoreObserver();

            // Clear 1: single -> 100 * 4 = 400, combo = 0, bonus = 0 -> score = 400
            scoreMessenger.notifyObservers(new LockPieceEvent(1, TSpin.NONE, level));
            // Clear 2: single -> 100 * 4 + (1 * 50 * 4) = 400 + 200 = 600 -> score = 1000
            scoreMessenger.notifyObservers(new LockPieceEvent(1, TSpin.NONE, level));
            // Clear 3: single -> 100 * 4 + (2 * 50 * 4) = 400 + 400 = 800 -> score = 1800
            scoreMessenger.notifyObservers(new LockPieceEvent(1, TSpin.NONE, level));

            assertThat(updates).extracting(ScoreInfoDTO::combo).containsExactly(0, 1, 2);
            assertThat(updates).extracting(ScoreInfoDTO::score).containsExactly(400, 1000, 1800);
            assertThat(scoreManager).extracting("score").isEqualTo(1800);
        }
    }

    private @NonNull LockPieceEvent createLockPieceEvent(int clearedLines, TSpin tSpin) {
        return new LockPieceEvent(clearedLines, tSpin, 1);
    }

    private @NonNull List<ScoreInfoDTO> prepareScoreObserver() {
        List<ScoreInfoDTO> updates = new ArrayList<>();
        scoreManager.scoreInfoObservable().addObserver(updates::add);
        return updates;
    }
}
