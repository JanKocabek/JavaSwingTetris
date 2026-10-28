package org.sehes.tetris.model;

import org.junit.jupiter.api.Test;
import org.sehes.tetris.testUtil.UtilForTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sehes.tetris.testUtil.UtilForTests.prepareBoard;

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
    void shouldClear4Lines() {
        final var gb = prepareBoard(UtilForTests.CreateFullBoardDescription("""
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
    void shouldClearLinesEvenWithSeparationBetweenThem() {
        final var gb = prepareBoard(UtilForTests.CreateFullBoardDescription("""
                IIIIIIIIII
                ###IIIII#I
                IIIIIIIIII
                IIIIIIIIII
                """));
        // when
        gb.tryClearLines();
        final var result = gb.getLastAction().linesCleared();
        // then
        int pieceLeft = 0;
        for (int i = 0; i < 10; i++) {
            if (gb.getBoardView().getBlockContent(21, i) == TetrominoType.I)
                pieceLeft++;
        }
        assertThat(result).isEqualTo(3);
        assertThat(pieceLeft).isEqualTo(6);
    }

    @Test
    void testThatClearingLinesAccuratelyWriteDownAboveANdFillUpWithEmpty() {
        final var INIT_BOARD = """
                ########I#
                #######III
                II#######I
                IIII###III
                IIIIIIIIII
                IIIIIIIIII
                IIIIIIIIII
                IIIII#IIII
                """;
        final var FINAL_GRID = UtilForTests.CreateFullBoardDescription(
                """
                        ########I#
                        #######III
                        II#######I
                        IIII###III
                        IIIII#IIII
                        """);
        final var gb = prepareBoard(UtilForTests.CreateFullBoardDescription(INIT_BOARD));
        // Act
        gb.tryClearLines();
        final var boardView = gb.getBoardView();
        final var resultBuilder = new StringBuilder();
        for (int i = 0; i < boardView.getHeight(); i++) {
            for (int j = 0; j < boardView.getWidth(); j++) {
                var cell = boardView.getBlockContent(i, j);
                resultBuilder.append(cell == TetrominoType.I ? "I" : "#");
            }
            resultBuilder.append("\n");
        }
        // Assert
        assertThat(resultBuilder.toString()).hasToString(FINAL_GRID);
    }

    @Test
    void testThatClearingLinesDoesNotAffectThePieces() {
        final var INIT_BOARD = "I#########\n".repeat(21) + "IIIIIIIIII";
        final var FINAL_GRID = UtilForTests.CreateFullBoardDescription(
                """
                        ##########
                        """ + "I#########\n".repeat(21));
        final var gb = prepareBoard(UtilForTests.CreateFullBoardDescription(INIT_BOARD));
        // Act
        gb.tryClearLines();
        final var boardView = gb.getBoardView();
        final var resultBuilder = new StringBuilder();
        for (int i = 0; i < boardView.getHeight(); i++) {
            for (int j = 0; j < boardView.getWidth(); j++) {
                var cell = boardView.getBlockContent(i, j);
                resultBuilder.append(cell == TetrominoType.I ? "I" : "#");
            }
            resultBuilder.append("\n");
        }
        // Assert
        assertThat(resultBuilder.toString()).hasToString(FINAL_GRID);
    }

}
