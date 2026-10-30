package org.sehes.tetris.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.model.generator.PieceGenerator;
import org.sehes.tetris.model.generator.RandomPieceGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class HardDropTest {

    private PieceGenerator generator;
    private GameBoard gameBoard;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard();
        generator = new RandomPieceGenerator();
    }

    /**
     * This test verifies the hard drop functionality moves a tetromino to the bottom of the game board.
     * <br>
     * Mathematical parameters from {@link GameParameters}:
     * <ul>
     *   <li>Total board rows: {@link GameParameters#ROWS} = 22</li>
     *   <li>Visible rows: {@link GameParameters#VISIBLE_ROWS} = 20</li>
     *   <li>Hidden rows: {@link GameParameters#HIDDEN_ROWS} = ROWS - VISIBLE_ROWS = 2</li>
     *   <li>Spawn point: {@link GameBoard#SPAWN_POINT} = (x:4, y:1)</li>
     * </ul>
     * The tetromino spawns at y=1 (in the hidden rows, second row from top). After hard drop,
     * it should land at position y = VISIBLE_ROWS + initialY = 20 + 1 = 21, meaning it drops
     * 20 rows total to reach the bottom of the visible game area.
     *
     * @see GameParameters
     */
    @Test
    void hardDropBasicSuccessCallTest() {
        //arrange
        gameBoard.trySetNewTetromino(generator.getNextPiece());
        final var mino = gameBoard.getCurrentTetromino();
        //act
        final var result = gameBoard.tryHardDrop();
        final var newYPos = mino.getPositionY();
        //assert
        assertThat(result).isEqualTo(20);
        assertThat(gameBoard.trySoftDrop()).isFalse();
        assertThat(newYPos).isEqualTo(GameParameters.ROWS - 1);

    }


    @Test
    void hardDropOnOtherMinoTest() {
        //arrange
        gameBoard.trySpawnTetromino(new Tetromino(TetrominoType.T, GameBoard.SPAWN_POINT));
        gameBoard.tryHardDrop();
        gameBoard.lockTetrominoInPlace();
        gameBoard.trySetNewTetromino(generator.getNextPiece());
        final var mino = gameBoard.getCurrentTetromino();
        //act
        final var result = gameBoard.tryHardDrop();
        //assert
        assertThat(result).isEqualTo(18);
        assertThat(gameBoard.trySoftDrop()).isFalse();
        assertThat(mino.getPositionY()).isEqualTo(19);
    }

    @Test
    void hardDropNotHappenOnLastRowTest() {
        //arrange
        final var happened = gameBoard.trySpawnTetromino(new Tetromino(TetrominoType.T, new Coordinate(4, GameParameters.ROWS - 1)));
        //act
        final var result = gameBoard.tryHardDrop();
        //assert
        assertThat(happened).isTrue();
        assertThat(result).isZero();
    }

}
