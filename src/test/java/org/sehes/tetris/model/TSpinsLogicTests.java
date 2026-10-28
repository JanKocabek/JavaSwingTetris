package org.sehes.tetris.model;

import org.junit.jupiter.api.Test;
import org.sehes.tetris.model.score.LockPieceEvent;
import org.sehes.tetris.model.score.ScoreManager;
import org.sehes.tetris.model.score.TSpin;
import org.sehes.tetris.testUtil.UtilForTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sehes.tetris.model.MinoSpawn.*;
import static org.sehes.tetris.testUtil.UtilForTests.prepareBoard;

class TSpinsLogicTests {

    /*
     * '#' is empty and 'I' is a filled board cell. Coordinates grow right/down.
     *
     * The T starts facing WEST, then rotates clockwise to NORTH around P (2,20):
     *
     *        x: 0 1 2 3 4
     *   y=19    # # T I I       front corners: (1,19) empty, (3,19) filled
     *   y=20    I T P T I
     *   y=21    I I # I I       back corners:  (1,21) filled, (3,21) filled
     *
     * Therefore, the rotation leaves one front and two back corners filled: a MINI T-spin.
     */
    private static final String T_SPIN_BOARD = UtilForTests.CreateFullBoardDescription("""
            ###II#####
            I###I#####
            II#II#####""");

    // =========================================================================
    // =========================================================================
    // T-Spin Logic & Scoring Pipeline Tests
    // =========================================================================

    @Test
    void lockingTWithoutRotation_withThreeOccupiedCorners_returnsNone() {
        // This board has three occupied corners around the T pivot. Corner
        // occupancy alone must not classify a lock as a T-spin: the last action
        // also has to be a rotation.
        assertTSpin(T_SPIN_BOARD, spawnT(new Coordinate(2, 20), Orientation.WEST, null), Orientation.WEST, TSpin.NONE, 0, 0);
    }

    @Test
    void testScoreTSpinFullNoLines() {
        assertTSpin(T_SPIN_BOARD, spawnT(new Coordinate(2, 20), Orientation.WEST, RotationFlag.COUNTER_CLOCKWISE), Orientation.SOUTH, TSpin.FULL, 0, 400);
    }

    @Test
    void testScoreTSpinFullSingle() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                II#II#####
                I###I#####
                II#IIIIIII
                """);

        assertTSpin(board, spawnT(new Coordinate(2, 20), Orientation.WEST, RotationFlag.COUNTER_CLOCKWISE), Orientation.SOUTH, TSpin.FULL, 1, 800);
    }

    @Test
    void testScoreTSpinFullDouble() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                II#II#####
                I###IIIIII
                II#IIIIIII
                """);

        assertTSpin(board, spawnT(new Coordinate(2, 20), Orientation.WEST, RotationFlag.COUNTER_CLOCKWISE), Orientation.SOUTH, TSpin.FULL, 2, 1200);
    }

    @Test
    void testScoreTSpinFullTriple() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                ########I#
                ########II
                #########I
                IIIIIIII#I
                IIIIIII##I
                IIIIIIII#I
                """);

        assertTSpin(board, spawnT(new Coordinate(7, 18), Orientation.NORTH, RotationFlag.COUNTER_CLOCKWISE), Orientation.WEST, TSpin.FULL, 3, 1600);
    }

    @Test
    void testScoreTSpinMiniNoLines() {
        assertTSpin(T_SPIN_BOARD, spawnT(new Coordinate(2, 20), Orientation.WEST, RotationFlag.CLOCKWISE), Orientation.NORTH, TSpin.MINI, 0, 100);
    }

    @Test
    void testScoreTSpinMiniSingle() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                ###II#####
                I###IIIIII
                II#II#####""");

        assertTSpin(board, spawnT(new Coordinate(2, 20), Orientation.WEST, RotationFlag.CLOCKWISE), Orientation.NORTH, TSpin.MINI, 1, 200);
    }

    @Test
    void testScoreTSpinMiniDouble() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                ########II
                #######III
                #########I
                ########II
                IIIIII##II
                IIIIIII#II
                """);

        assertTSpin(board, spawnT(new Coordinate(7, 18), Orientation.SOUTH, RotationFlag.CLOCKWISE), Orientation.WEST, TSpin.MINI, 2, 400);
    }

    @Test
    void testTSpinWallKickTypeNoTSpinResult() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                ####I#####
                ####I#####
                #I##I#####
                #II#I#####
                #IIII#####
                """);
        assertTSpin(board, spawnT(new Coordinate(3, 19), Orientation.WEST, RotationFlag.CLOCKWISE), Orientation.NORTH, TSpin.NONE, 0, 0);
    }

    @Test
    void testTSpinWallKickTypeTSpinResult() {
        // 1 front corner (2,19) and 2 back corners (0,19), (0,21) are occupied (total 3).
        // Front corner (2,21) is empty. Under normal rules, 1 front + 2 back = MINI.
        // But because of the (-1, 2) Test 5 kick, it is promoted to FULL (T-Spin Double = 1200).
        final var board = UtilForTests.CreateFullBoardDescription("""
                II##IIIIII
                I###IIIIII
                I#IIIIIIII
                I##IIIIIII
                I##IIIIIII
                """);
        assertTSpin(board, spawnT(new Coordinate(2, 18), Orientation.NORTH, RotationFlag.CLOCKWISE), Orientation.EAST, TSpin.FULL, 2, 1200);
    }

    @Test
    void testNormalRotationInEmptySpace_returnsNone() {
        final var board = UtilForTests.CreateFullBoardDescription("""
                ##########
                ##########
                ##########
                """);
        assertTSpin(board, spawnT(new Coordinate(4, 18), Orientation.NORTH, RotationFlag.CLOCKWISE), Orientation.EAST, TSpin.NONE, 0, 0);
    }

    // =========================================================================
    // Unified Reusable Helper Methods & Configurations
    // =========================================================================

    /**
     * Executes a T-Spin test asserting only the resulting orientation and classified {@link TSpin} type.
     * Line clears and score are not asserted.
     * <p>
     * <b>Future usefulness:</b> Ideal for mechanics-only unit tests (e.g. wall-kick behavior or corner detection)
     * where line clearing and scoring pipelines are irrelevant.
     *
     * @param initialBoard        ASCII representation of the board state
     * @param spawnedTetromino    the tetromino fixture (spawn position, orientation, rotation)
     * @param expectedOrientation expected orientation of the tetromino after rotation
     * @param expectedTSpin       expected {@link TSpin} result (NONE, MINI, FULL)
     * @return {@link TSpinTestResult} containing the post-test game components for further custom assertions
     */
    public static TSpinTestResult assertTSpin(String initialBoard, MinoSpawn.SpawnedTetromino spawnedTetromino, Orientation expectedOrientation, TSpin expectedTSpin) {
        return assertTSpin(new TSpinTestCase(initialBoard, spawnedTetromino, expectedOrientation, expectedTSpin, null, null, null));
    }

    /**
     * Primary test runner overload. Spawns a piece, executes rotation, locks the piece, clears lines,
     * updates the score manager, and verifies orientation, T-Spin classification, line clear count, and final score.
     *
     * @param initialBoard         ASCII representation of the board state
     * @param spawnedTetromino     the tetromino fixture (spawn position, orientation, rotation)
     * @param expectedOrientation  expected orientation of the tetromino after rotation
     * @param expectedTSpin        expected {@link TSpin} result (NONE, MINI, FULL)
     * @param expectedLinesCleared expected number of completed lines cleared
     * @param expectedScore        expected score points awarded by {@link ScoreManager}
     * @return {@link TSpinTestResult} containing the post-test game components for further custom assertions
     */
    public static TSpinTestResult assertTSpin(String initialBoard, SpawnedTetromino spawnedTetromino, Orientation expectedOrientation, TSpin expectedTSpin, Integer expectedLinesCleared, Integer expectedScore) {
        return assertTSpin(new TSpinTestCase(initialBoard, spawnedTetromino, expectedOrientation, expectedTSpin, expectedLinesCleared, expectedScore, null));
    }

    /**
     * Convenience overload that builds a T-piece fixture inline without requiring an explicit {@code spawnT(...)} call.
     * <p>
     * <b>Future usefulness:</b> Allows concise one-line test definitions when all spawn parameters are literal values.
     *
     * @param initialBoard         ASCII representation of the board state
     * @param spawnCord            the starting grid coordinates of the tetromino pivot
     * @param startOrie            the starting orientation before rotation
     * @param rotation             the rotation flag (CLOCKWISE, COUNTER_CLOCKWISE, or null for no rotation)
     * @param expectedOrientation  expected orientation of the tetromino after rotation
     * @param expectedTSpin        expected {@link TSpin} result (NONE, MINI, FULL)
     * @param expectedLinesCleared expected number of completed lines cleared
     * @param expectedScore        expected score points awarded by {@link ScoreManager}
     * @return {@link TSpinTestResult} containing the post-test game components for further custom assertions
     */
    public static TSpinTestResult assertTSpin(String initialBoard, Coordinate spawnCord, Orientation startOrie, RotationFlag rotation, Orientation expectedOrientation, TSpin expectedTSpin, Integer expectedLinesCleared, Integer expectedScore) {
        return assertTSpin(new TSpinTestCase(initialBoard, spawnT(spawnCord, startOrie, rotation), expectedOrientation, expectedTSpin, expectedLinesCleared, expectedScore, null));
    }

    /**
     * Core unified test execution engine for all T-Spin logic and scoring tests.
     * <ol>
     *   <li>Initializes the {@link GameBoard} from the ASCII board string.</li>
     *   <li>Spawns the configured tetromino on the board.</li>
     *   <li>Applies the rotation if specified and asserts the resulting orientation.</li>
     *   <li>Locks the tetromino and evaluates the T-Spin condition.</li>
     *   <li>Clears full lines and records lines cleared.</li>
     *   <li>Notifies the {@link ScoreManager} with a {@link LockPieceEvent} and asserts scoring results.</li>
     * </ol>
     *
     * @param testCase configured {@link TSpinTestCase} containing all inputs and expectations
     * @return {@link TSpinTestResult} containing the post-test game components for further custom assertions
     */
    public static TSpinTestResult assertTSpin(TSpinTestCase testCase) {
        final GameBoard gameBoard = prepareBoard(testCase.initialBoard());
        final SpawnedTetromino spawned = testCase.spawnedTetromino();
        final Tetromino tetromino = spawned.tetromino();

        assertThat(gameBoard.trySpawnTetromino(tetromino)).withFailMessage("Tetromino failed to spawn on board").isTrue();

        if (spawned.rotation() != null) {
            assertThat(gameBoard.tryRotatePiece(spawned.rotation())).withFailMessage("Tetromino failed to rotate").isTrue();
        }

        if (testCase.expectedOrientation() != null) {
            assertThat(tetromino.getCurrentOrientation()).withFailMessage("Expected orientation: %s, but was: %s", testCase.expectedOrientation(), tetromino.getCurrentOrientation()).isEqualTo(testCase.expectedOrientation());
        }

        gameBoard.lockTetrominoInPlace();
        gameBoard.tryClearLines();

        final var lastAction = gameBoard.getLastAction();

        if (testCase.expectedTSpin() != null) {
            assertThat(lastAction.tSpin()).withFailMessage("Expected T-Spin: %s, but was: %s", testCase.expectedTSpin(), lastAction.tSpin()).isEqualTo(testCase.expectedTSpin());
        }

        if (testCase.expectedLinesCleared() != null) {
            assertThat(lastAction.linesCleared()).withFailMessage("Expected lines cleared: %d, but was: %d", testCase.expectedLinesCleared(), lastAction.linesCleared()).isEqualTo(testCase.expectedLinesCleared());
        }

        final ScoreManager scoreMgr = testCase.scoreManager() != null ? testCase.scoreManager() : new ScoreManager();
        scoreMgr.updateScore(new LockPieceEvent(lastAction.linesCleared(), lastAction.tSpin(), 1));

        if (testCase.expectedScore() != null) {
            assertThat(scoreMgr).extracting("score").isEqualTo(testCase.expectedScore());
        }

        return new TSpinTestResult(gameBoard, tetromino, lastAction.tSpin(), lastAction.linesCleared(), scoreMgr);
    }

    /**
     * Immutable test case data carrier defining all inputs, initial board configuration,
     * optional custom score manager, and expected outcomes.
     */
    public record TSpinTestCase(String initialBoard, SpawnedTetromino spawnedTetromino, Orientation expectedOrientation,
                                TSpin expectedTSpin, Integer expectedLinesCleared, Integer expectedScore,
                                ScoreManager scoreManager) {
        /**
         * Creates a new fluent {@link TSpinTestBuilder} instance.
         *
         * @return a new builder
         */
        public static TSpinTestBuilder builder() {
            return new TSpinTestBuilder();
        }

        /**
         * Fluent builder for constructing {@link TSpinTestCase} instances.
         * <p>
         * <b>Future usefulness:</b> Provides a flexible API for complex test scenarios where only a subset of
         * assertions is needed, or where a pre-configured {@link ScoreManager} (e.g. testing existing Back-to-Back
         * chains or combo multipliers) needs to be injected.
         */
        public static class TSpinTestBuilder {
            private String initialBoard;
            private SpawnedTetromino spawnedTetromino;
            private Orientation expectedOrientation;
            private TSpin expectedTSpin;
            private Integer expectedLinesCleared;
            private Integer expectedScore;
            private ScoreManager scoreManager;

            public TSpinTestBuilder initialBoard(String initialBoard) {
                this.initialBoard = initialBoard;
                return this;
            }

            public TSpinTestBuilder spawnedTetromino(SpawnedTetromino spawnedTetromino) {
                this.spawnedTetromino = spawnedTetromino;
                return this;
            }

            public TSpinTestBuilder spawnedTetromino(Coordinate spawnCord, Orientation startOrie, RotationFlag rotation) {
                this.spawnedTetromino = spawnT(spawnCord, startOrie, rotation);
                return this;
            }

            public TSpinTestBuilder spawnedTetromino(TetrominoType type, Coordinate spawnCord, Orientation startOrie, RotationFlag rotation) {
                this.spawnedTetromino = spawn(type, spawnCord, startOrie, rotation);
                return this;
            }

            public TSpinTestBuilder expectedOrientation(Orientation expectedOrientation) {
                this.expectedOrientation = expectedOrientation;
                return this;
            }

            public TSpinTestBuilder expectedTSpin(TSpin expectedTSpin) {
                this.expectedTSpin = expectedTSpin;
                return this;
            }

            public TSpinTestBuilder expectedLinesCleared(Integer expectedLinesCleared) {
                this.expectedLinesCleared = expectedLinesCleared;
                return this;
            }

            public TSpinTestBuilder expectedScore(Integer expectedScore) {
                this.expectedScore = expectedScore;
                return this;
            }

            /**
             * Injects a custom {@link ScoreManager} instance into the test pipeline.
             * <p>
             * <b>Future usefulness:</b> Enables testing continuous score state, such as verifying that a T-Spin
             * correctly increments an existing Back-to-Back (B2B) chain or combo multiplier.
             *
             * @param scoreManager pre-configured ScoreManager instance
             * @return this builder
             */
            public TSpinTestBuilder scoreManager(ScoreManager scoreManager) {
                this.scoreManager = scoreManager;
                return this;
            }

            /**
             * Builds the {@link TSpinTestCase}.
             *
             * @return constructed test case
             */
            public TSpinTestCase build() {
                return new TSpinTestCase(initialBoard, spawnedTetromino, expectedOrientation, expectedTSpin, expectedLinesCleared, expectedScore, scoreManager);
            }
        }
    }

    /**
     * Holds the post-execution state of game components after {@link #assertTSpin(TSpinTestCase)} completes.
     * <p>
     * <b>Future usefulness:</b> Allows test methods to chain further custom assertions on the game board,
     * remaining blocks, tetromino coordinates, or internal score manager state.
     *
     * @param gameBoard    the game board after piece lock and line clears
     * @param tetromino    the locked tetromino
     * @param tSpin        the calculated {@link TSpin} result
     * @param linesCleared the number of cleared lines
     * @param scoreManager the score manager after processing the lock event
     */
    public record TSpinTestResult(GameBoard gameBoard, Tetromino tetromino, TSpin tSpin, int linesCleared,
                                  ScoreManager scoreManager) {
    }
}
