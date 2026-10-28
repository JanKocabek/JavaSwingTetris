package org.sehes.tetris.model;

import org.junit.jupiter.api.Test;
import org.sehes.tetris.testUtil.UtilForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestForUtilTestsClass {

    // --- Tests for boardEmptyLine ---

    @Test
    void boardEmptyLine_generatesCorrectLineWithNewline() {
        String result = UtilForTests.boardEmptyLine(5);
        assertEquals("#####\n", result, "Should generate 5 hashtags followed by a newline");
    }

    @Test
    void boardEmptyLine_handlesZeroLength() {
        String result = UtilForTests.boardEmptyLine(0);
        assertEquals("\n", result, "Should generate only a newline if size is 0");
    }

    // --- Tests for getFullBoard ---

    @Test
    void getFullBoard_addsCorrectNumberOfEmptyRows() {
        // A 2-line shape
        String filledPart = """
                ###II###
                II#II###""";

        // We want a total of 4 rows, with 8 columns. It should add 2 empty rows.
        String expected = """
                ########
                ########
                ###II###
                II#II###""";

        String result = UtilForTests.CreateFullBoardDescription(filledPart, 4, 8);

        assertEquals(expected, result, "Should pad exactly 2 empty rows at the top");
    }

    @Test
    void getFullBoard_doesNotAddRowsIfBoardIsAlreadyFull() {
        // A 3-line shape
        String filledPart = """
                ###
                ###
                ###""";

        // We want exactly 3 rows. It should add 0 empty rows.
        String result = UtilForTests.CreateFullBoardDescription(filledPart, 3, 3);

        assertEquals(filledPart, result, "Should return the exact same string if no padding is needed");
    }

    @Test
    void getFullBoard_preventsCrashIfBoardIsLargerThanTarget() {
        // A 4-line shape
        String filledPart = """
                ###
                ###
                ###
                ###""";

        // Target is 2 rows. Math.max(0, -2) should prevent a crash and add 0 rows.
        String result = UtilForTests.CreateFullBoardDescription(filledPart, 2, 3);

        assertEquals(filledPart, result, "Should gracefully return the original board without throwing an exception");
    }

    @Test
    void getFullBoard_handlesEmptyInput() {
        String filledPart = "";

        // Target is 2 rows. It should generate 2 empty rows.
        String expected = """
                ###
                ###
                """;

        String result = UtilForTests.CreateFullBoardDescription(filledPart, 2, 3);

        assertEquals(expected, result, "Should return a completely empty board of the target dimensions");
    }
}
