package org.sehes.tetris.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sehes.tetris.config.GameParameters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sehes.tetris.model.TetrominoFixtures.spawn;
import static org.sehes.tetris.model.UtilForTests.getFullBoard;
import static org.sehes.tetris.model.UtilForTests.prepareBoard;
import static org.sehes.tetris.model.UtilForTests.prepareBoard2T;
import static org.sehes.tetris.model.UtilForTests.printBoardState;

class GameBoardTests {

    private GameBoard gameBoard;
    private PieceGenerator generator;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard();
        generator = new RandomPieceGenerator();
    }

    /**
     * Tests that the board is updated correctly after adding a tetromino to it.
     * It checks that all the blocks of the tetromino are added to the correct
     * positions
     * on the board and that the current tetromino is null after adding it to the
     * board.
     */
    @Test
    void testLockTetrominoInPlace() {
        // given
        gameBoard.trySetNewTetromino(generator.getNextPiece());
        final var tetromino = gameBoard.getCurrentTetromino();
        final var cord = tetromino.getStateCord();
        // when
        gameBoard.lockTetrominoInPlace();
        final var boardView = gameBoard.getBoardView();
        // then
        cord.forEach(coordinate -> assertEquals(tetromino.getType(), boardView
                        .getBlockContent(tetromino.getPositionY() + coordinate.y(), tetromino.getPositionX() + coordinate.x()),
                "Block should be added to board"));
        assertNull(gameBoard.getCurrentTetromino(), "Current tetromino should be null after adding to board");
    }

    @Test
    void testCheckAndClearLinesNoLines() {
        // when
        gameBoard.clearLines();
        final var result = gameBoard.getLastAction();
        // then
        assertThat(result.linesCleared()).isZero();
    }


    @Test
    void testSetLineForTest() {
        // given
        gameBoard.createGarbageLine();
        // when
        gameBoard.clearLines();
        final var result = gameBoard.getLastAction();
        // then
        assertThat(result.linesCleared()).isOne();
    }


    @Nested
    @DisplayName("basic functionality")
    class BasicFunctionality {
        @Test
        void testGameBoardInitialization() {
            assertNotNull(gameBoard);
            assertNull(gameBoard.getCurrentTetromino());
        }

        @Test
        void testGetBoardView() {
            BoardView boardView = gameBoard.getBoardView();
            assertNotNull(boardView);
            assertEquals(GameParameters.COLUMNS, boardView.getWidth());
            assertEquals(GameParameters.ROWS, boardView.getHeight());
        }

        @Test
        void testBoardViewGetBlockContent() {
            BoardView boardView = gameBoard.getBoardView();
            assertEquals(TetrominoType.NON, boardView.getBlockContent(0, 0));
        }

        @Test
        void testTrySetNewTetromino() {
            boolean result =    gameBoard.trySetNewTetromino(generator.getNextPiece());
            assertTrue(result);
            assertNotNull(gameBoard.getCurrentTetromino());
        }

        @Test
        void testGetCurrentTetrominoAfterSet() {
               gameBoard.trySetNewTetromino(generator.getNextPiece());
            Tetromino current = gameBoard.getCurrentTetromino();
            assertNotNull(current);
        }

    }

    @Nested
    @DisplayName("crash cases")
    class CrashCases {
        @ParameterizedTest
        @EnumSource(value = RotationFlag.class, names = {"CLOCKWISE", "COUNTER_CLOCKWISE"})
        void testTryRotatePieceWithoutTetromino(RotationFlag flag) {
            boolean result = gameBoard.tryRotatePiece(flag);
            assertFalse(result, "Rotation should not be possible without a tetromino");
        }

        @Test
        void testBoardViewGetBlockContentOutOfBounds() {
            BoardView boardView = gameBoard.getBoardView();
            assertThrows(IndexOutOfBoundsException.class, () -> boardView.getBlockContent(-1, 0));
        }

        @Test
        void spawningPieceOutsideBoard_isRejectedWithoutAnActivePiece() {
            final Tetromino piece = spawn(TetrominoType.I, new Coordinate(-2, 1), Orientation.NORTH, null).t();

            assertThat(gameBoard.trySpawnTetromino(piece)).isFalse();
            assertThat(gameBoard.getCurrentTetromino()).isNull();
        }

    }

    @Nested
    @DisplayName("movement cases")
    class MovementCases {

        @Test
        void testTryMovePieceLeft() {
            // given
               gameBoard.trySetNewTetromino(generator.getNextPiece());
            Tetromino tetromino = gameBoard.getCurrentTetromino();
            Coordinate initialPos = new Coordinate(tetromino.getPositionX(), tetromino.getPositionY());
            // when
            boolean moved = gameBoard.tryMovePiece(DirectionFlag.LEFT);
            // then
            assertTrue(moved);
            Coordinate newPos = new Coordinate(tetromino.getPositionX(), tetromino.getPositionY());
            assertEquals(initialPos.x() - 1, newPos.x());
            assertEquals(initialPos.y(), newPos.y());

        }

        @Test
        void testTryMovePieceRight() {
            // given
               gameBoard.trySetNewTetromino(generator.getNextPiece());
            Tetromino tetromino = gameBoard.getCurrentTetromino();
            assertNotNull(tetromino);
            Coordinate initialPos = new Coordinate(tetromino.getPositionX(), tetromino.getPositionY());
            // when
            boolean moved = gameBoard.tryMovePiece(DirectionFlag.RIGHT);
            // then
            assertTrue(moved);
            Coordinate newPos = new Coordinate(tetromino.getPositionX(), tetromino.getPositionY());
            assertNotNull(newPos);
            assertEquals(initialPos.x() + 1, newPos.x());
            assertEquals(initialPos.y(), newPos.y());

        }

        @Test
        void testTryMovePieceDown() {
            // given
               gameBoard.trySetNewTetromino(generator.getNextPiece());
            Tetromino tetromino = gameBoard.getCurrentTetromino();
            Coordinate initialPos = new Coordinate(tetromino.getPositionX(), tetromino.getPositionY());
            // when
            boolean moved = gameBoard.trySoftDrop();
            // then
            assertTrue(moved);
            Coordinate newPos = new Coordinate(tetromino.getPositionX(), tetromino.getPositionY());
            assertEquals(initialPos.x(), newPos.x());
            assertEquals(initialPos.y() + 1, newPos.y());

        }

        @Test
        void movingDownWithHorizontalMoveApi_explainsWhichApiToUse() {
               gameBoard.trySetNewTetromino(generator.getNextPiece());
            assertThatThrownBy(() -> gameBoard.tryMovePiece(DirectionFlag.DOWN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Use trySoftDrop() for down movement");
        }

    }

    @Nested
    @DisplayName("line clearing cases")
    class LineClearingCases {

        @Test
        void clearingFullLine_movesTheRowsAboveDown() {
            final GameBoard board = prepareBoard(getFullBoard("""
                    I#########
                    IIIIIIIIII
                    """));

            board.clearLines();

            assertThat(board.getLastAction().linesCleared()).isOne();
            assertThat(board.getBoardView().getBlockContent(21, 0)).isEqualTo(TetrominoType.I);
            assertThat(board.getBoardView().getBlockContent(21, 1)).isEqualTo(TetrominoType.NON);
        }
    }

    @Nested
    @DisplayName("rotation cases")
    class RotationCases {

        @Test
        void testTryRotatePiece() {
            // arrange
            final var happened = gameBoard.trySpawnTetromino(TetrominoFactory.spawnTetromino(TetrominoType.O, new Coordinate(4, 1)));
            // act
            boolean rotated = gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE);
            // assert
            assertThat(happened).isTrue();
            assertFalse(rotated, "O tetromino cannot be rotated");
        }

        /**
         * Tests that the position of the tetromino is not changed after rotation. it
         * tests both directions
         *
         * @param rotation The direction to rotate the tetromino.
         */
        @ParameterizedTest
        @EnumSource(value = RotationFlag.class, names = {"CLOCKWISE", "COUNTER_CLOCKWISE"})
        void testPositionIsTheSameAfterRotation(RotationFlag rotation) {
            // given
            Tetromino tetromino = TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 1));
            gameBoard.trySpawnTetromino(tetromino);
            Tetromino currentTetromino = gameBoard.getCurrentTetromino();
            Coordinate initialPos = new Coordinate(currentTetromino.getPositionX(), currentTetromino.getPositionY());
            // when
            boolean rotated = gameBoard.tryRotatePiece(rotation);
            // then
            assertTrue(rotated);
            Coordinate newPos = new Coordinate(currentTetromino.getPositionX(), currentTetromino.getPositionY());
            assertEquals(initialPos.x(), newPos.x());
            assertEquals(initialPos.y(), newPos.y());
        }

        @ParameterizedTest
        @EnumSource(value = TetrominoType.class, names = {"I", "O", "S", "Z", "L", "J", "T"})
        void testTryReturnIntoBaseState(TetrominoType type) {
            // given
            Tetromino tetromino = TetrominoFactory.spawnTetromino(type, new Coordinate(4, 2));
            gameBoard.trySpawnTetromino(tetromino);
            var baseCord = tetromino.getStateCord();
            Orientation tetrominoState = tetromino.getCurrentOrientation();
            // when
            for (int i = 0; i < 4; i++) {
                gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE);
            }
            // then
            List<Coordinate> finalCord = gameBoard.getCurrentTetromino().getStateCord();
            assertEquals(tetrominoState, gameBoard.getCurrentTetromino().getCurrentOrientation());
            assertEquals(baseCord, finalCord);

        }

        @Test
        void testTryRotatePieceRight() {
            // given
            Tetromino tetromino = TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 1));
            gameBoard.trySpawnTetromino(tetromino);
            // when
            boolean rotated = gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE);
            // then
            assertTrue(rotated);
            List<Coordinate> afterCord = gameBoard.getCurrentTetromino().getStateCord();
            List<Coordinate> expected = List.of(
                    new Coordinate(0, 0),
                    new Coordinate(0, -1),
                    new Coordinate(0, 1),
                    new Coordinate(1, 0));
            assertEquals(4, afterCord.size());
            assertTrue(afterCord.containsAll(expected));

        }

        @Test
        void testTryRotatePieceLeft() {
            // given
            Tetromino tetromino = TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 1));
            gameBoard.trySpawnTetromino(tetromino);
            // when
            boolean rotated = gameBoard.tryRotatePiece(RotationFlag.COUNTER_CLOCKWISE);
            // then
            assertTrue(rotated);
            List<Coordinate> afterCord = gameBoard.getCurrentTetromino().getStateCord();
            List<Coordinate> expected = List.of(
                    new Coordinate(0, 0),
                    new Coordinate(0, 1),
                    new Coordinate(0, -1),
                    new Coordinate(-1, 0));
            assertEquals(4, afterCord.size());
            assertTrue(afterCord.containsAll(expected));
        }

        @ParameterizedTest
        @EnumSource(value = RotationFlag.class, names = {"CLOCKWISE", "COUNTER_CLOCKWISE"})
        void testTryRotateOPiece(RotationFlag flag) {
            // given
            Tetromino tetromino = TetrominoFactory.spawnTetromino(TetrominoType.O, new Coordinate(4, 1));
            gameBoard.trySpawnTetromino(tetromino);
            // when
            boolean rotated = gameBoard.tryRotatePiece(flag);
            // then
            assertFalse(rotated);
        }

        /**
         * T-piece at pivot (4,17) facing NORTH cannot rotate CW to EAST because
         * the rotated shape and all four SRS wall-kick positions are blocked.
         *
         * <pre>
         *  col:  0123456789
         *  r16:  ####I#####   ← (4,16) blocks kick 2: pivot (3,16) → EAST cell (4,16)
         *  r17:  ##########   ← T pivot here (NORTH: left/center/right + top)
         *  r18:  ###II#####   ← (3,18)+(4,18) block in-place and kick 1
         *  r19:  ##########     kick 3+4 land at r19/r21 with cells already covered
         *  r20:  ##########
         *  r21:  ##########
         * </pre>
         *
         * SRS NORTH→EAST kicks and why each fails:
         * <ul>
         *   <li>In-place (4,17): EAST needs (4,18) → filled ✗</li>
         *   <li>Kick (-1,0) → (3,17): EAST needs (3,18) → filled ✗</li>
         *   <li>Kick (-1,-1) → (3,16): EAST needs (4,16) → filled ✗</li>
         *   <li>Kick (0,+2) → (4,19): EAST needs (4,18) and (4,20) — (4,18) filled ✗</li>
         *   <li>Kick (-1,+2) → (3,19): EAST needs (3,18) — filled ✗</li>
         * </ul>
         */
        @Test
        void testImpossibleRotation_T() {
            // 6 lines → getFullBoard prepends 22-6=16 empty rows → lines become rows 16-21
            final GameBoard localBoard = prepareBoard(getFullBoard("""
                    ####I#####
                    ##########
                    ###II#####
                    ##########
                    ##########
                    ##########
                    """));
            localBoard.trySpawnTetromino(spawn(TetrominoType.T, new Coordinate(4, 17), Orientation.NORTH, null).t());

            assertFalse(localBoard.tryRotatePiece(RotationFlag.CLOCKWISE),
                    "T-piece CW rotation must fail: in-place and all 4 SRS kick positions are blocked");
        }

        /**
         * I-piece at pivot (4,12) facing NORTH (horizontal bar) cannot rotate CW
         * to EAST (vertical) because both full rows at 9 and 14 block every cell
         * the vertical bar would occupy across all four I-piece SRS kick positions.
         *
         * <pre>
         *  col:  0123456789
         *  r9:   IIIIIIIIII   ← full row blocks top of any vertical I
         *  r10:  ##########
         *  r11:  ##########
         *  r12:  ##########   ← I pivot here (NORTH: cols 3-6)
         *  r13:  ##########
         *  r14:  IIIIIIIIII   ← full row blocks bottom of any vertical I
         * </pre>
         *
         * I EAST offsets are all at x = pivot.x+1, spanning y-1..y+2.
         * Every kick shifts the pivot horizontally; the vertical bar always
         * intersects at least one of row 9 or row 14.
         */
        @Test
        void testImpossibleRotation_I() {
            // 13 lines → getFullBoard prepends 22-13=9 empty rows → lines become rows 9-21
            final GameBoard localBoard = prepareBoard(getFullBoard("""
                    IIIIIIIIII
                    ##########
                    ##########
                    ##########
                    ##########
                    IIIIIIIIII
                    ##########
                    ##########
                    ##########
                    ##########
                    ##########
                    ##########
                    ##########
                    """));
            localBoard.trySpawnTetromino(spawn(TetrominoType.I, new Coordinate(4, 12), Orientation.NORTH, null).t());

            assertFalse(localBoard.tryRotatePiece(RotationFlag.CLOCKWISE),
                    "I-piece CW rotation must fail: full rows at 9 and 14 block all SRS kick positions");
        }

        /**
         * {@code tryRotatePiece(null)} must always return {@code false}.
         * The null guard is an early-exit branch independent of board state.
         */
        @Test
        void tryRotatePiece_withNullRotation_returnsFalse() {
            gameBoard.trySpawnTetromino(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 1)));
            assertFalse(gameBoard.tryRotatePiece(null),
                    "null rotation flag must always return false");
        }
    }

    @Nested
    @DisplayName("WallKicks")
    class WallKicksTest {


        @Test
        void testWallKicks_T() {
            // given
            prepareBoard(gameBoard);
            gameBoard.trySpawnTetromino(
                    TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(2, 4)));

            // when
            gameBoard.tryRotatePiece(RotationFlag.COUNTER_CLOCKWISE);
            // then
            assertTrue(gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE));
        }

        @Test
        void testWallKicks_T2() {
            //given
            gameBoard.trySpawnTetromino(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(2, 4)));
            prepareBoard2T(gameBoard);
            final var t = gameBoard.getCurrentTetromino();
            final var basePos = new Coordinate(t.getPositionX(), t.getPositionY());
            //when
            printBoardState(gameBoard.getBoardView(), t);
            final var result = gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE);
            final var afterPosition = new Coordinate(t.getPositionX(), t.getPositionY());
            //then
            assertNotEquals(basePos, afterPosition);
            assertTrue(result);
            printBoardState(gameBoard.getBoardView(), gameBoard.getCurrentTetromino());

            List<Coordinate> expected = List.of(
                    new Coordinate(0, -1),
                    new Coordinate(0, 0),
                    new Coordinate(0, 1),
                    new Coordinate(1, 0)
            );
            List<Coordinate> actual = gameBoard.getCurrentTetromino().getStateCord();
            assertEquals(expected.size(), actual.size());
            assertTrue(actual.containsAll(expected) && expected.containsAll(actual));
        }


        @Test
        void testWallKicks_L() {
            //given
            prepareBoard(gameBoard);
            gameBoard.trySpawnTetromino(TetrominoFactory.spawnTetromino(TetrominoType.L, new Coordinate(2, 4)));
            final var t = gameBoard.getCurrentTetromino();
            final var basePos = new Coordinate(t.getPositionX(), t.getPositionY());
            printBoardState(gameBoard.getBoardView(), t);
            // when
            gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE);
            printBoardState(gameBoard.getBoardView(), t);
            final var secrotate = gameBoard.tryRotatePiece(RotationFlag.CLOCKWISE);
            final var afterPosition = new Coordinate(t.getPositionX(), t.getPositionY());
            // then
            assertNotEquals(basePos, afterPosition);
            assertTrue(secrotate);
            printBoardState(gameBoard.getBoardView(), t);
        }
    }

}
