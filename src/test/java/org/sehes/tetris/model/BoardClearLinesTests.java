package org.sehes.tetris.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sehes.tetris.model.UtilForTests.prepareBoard;

public class BoardClearLinesTests {

    @Test
    void testClearLinesIsZeroIfNoLines() {
        // given
        final var gameBoard = new GameBoard();
        // act
        gameBoard.tryClearLines();
        final var result = gameBoard.getLastAction().linesCleared();
        // assert
        assertThat(result).isZero();
    }

    @Test
    void shouldClearLineClear1Line() {
        // given
        final var gb = prepareBoard(UtilForTests.CreateFullBoardDescription("""
                I#########
                IIIIIIIIII
                """));
        // when
        gb.tryClearLines();
        final var result = gb.getLastAction().linesCleared();
        // then
        assertThat(result).isOne();
    }

    @Test
    void shouldClear4Lines(){
        final var gb =prepareBoard(UtilForTests.CreateFullBoardDescription("""
               IIIIIIIIII
               IIIIIIIIII
               IIIIIIIIII
               IIIIIIIIII
               """));
        // when
        gb.tryClearLines();
        final var result = gb.getLastAction().linesCleared();
        // then
        assertThat(result).isEqualTo(4);
    }

    @Test
    void shouldClearLinesEvenWithSeparationBetweenThem(){
        final var gb =prepareBoard(UtilForTests.CreateFullBoardDescription("""
               IIIIIIIIII
               ###IIIII#I
               IIIIIIIIII
               IIIIIIIIII
               """));
        // when
        gb.tryClearLines();
        final var result = gb.getLastAction().linesCleared();
        // then
        int pieceLeft=0;
                for(int i = 0; i < 10; i++){
                   if( gb.getBoardView().getBlockContent(21, i) == TetrominoType.I)
                       pieceLeft++;
                }
        assertThat(result).isEqualTo(3);
        assertThat(pieceLeft).isEqualTo(6);
    }

}
