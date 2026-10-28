package org.sehes.tetris.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sehes.tetris.model.UtilForTests.prepareBoard;

public class BoardClearLinesTests {
    private GameBoard gameBoard;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard();
    }

    @Test
    void testCheckAndClearLinesNoLines() {
        // when
        gameBoard.tryClearLines();
        final var result = gameBoard.getLastAction();
        // then
        assertThat(result.linesCleared()).isZero();
    }

    @Test
    void testSetLineForTest() {
        // given
        gameBoard.createGarbageLine();
        // when
        gameBoard.tryClearLines();
        final var result = gameBoard.getLastAction();
        // then
        assertThat(result.linesCleared()).isOne();
    }

    @Test
    void clearingFullLine_movesTheRowsAboveDown() {
        final GameBoard board = prepareBoard(UtilForTests.CreateFullBoardDescription("""
                    I#########
                    IIIIIIIIII
                    """));

        board.tryClearLines();

        assertThat(board.getLastAction().linesCleared()).isOne();
        assertThat(board.getBoardView().getBlockContent(21, 0)).isEqualTo(TetrominoType.I);
        assertThat(board.getBoardView().getBlockContent(21, 1)).isEqualTo(TetrominoType.NON);
    }

}
