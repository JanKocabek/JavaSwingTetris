package org.sehes.tetris.testUtil;

import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.model.*;

import java.util.List;


public final class UtilForTests {

    public static void printBoardState(BoardView boardView, Tetromino tetromino) {
        final char TETROMINO = 'T';
        final char BLOCK = '#';
        final char EMPTY = '.';
        final var tetrominoBoardCord = getTetrominoBoardCord(tetromino);

        for (int curY = 0; curY < boardView.getHeight(); curY++) {
            for (int curX = 0; curX < boardView.getWidth(); curX++) {
                final int finalCurX = curX;
                final int finalCurY = curY;
                final var isTetromino = tetrominoBoardCord.stream().anyMatch(c -> c.x() == finalCurX && c.y() == finalCurY);
                if (boardView.getBlockContent(curY, curX) == TetrominoType.NON) {
                    System.out.print(isTetromino ? TETROMINO : EMPTY);
                } else {
                    if (isTetromino) throw new RuntimeException("Tetromino should be placed on empty block");
                    System.out.print(BLOCK);
                }
            }
            System.out.println();
        }
        System.out.println("----");
    }

    public static List<Coordinate> getTetrominoBoardCord(Tetromino tetromino) {
        List<Coordinate> cord = tetromino.getStateCord();
        return cord.stream().map(c -> new Coordinate(c.x() + tetromino.getPositionX(), c.y() + tetromino.getPositionY())).toList();
    }

    private static void checkBoardSize(String boardDescription) {
        final var lines = boardDescription.lines().toList();

        if (lines.size() > GameParameters.ROWS) {
            throw new AssertionError("given rows are %s but can be maximum %s".formatted(lines.size(), GameParameters.ROWS));
        }

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            final var length = line.length();
            if (length > GameParameters.COLUMNS) {
                throw new AssertionError("row %s have %s characters but can be maximum %s".formatted(i, length, GameParameters.COLUMNS));
            }
        }
    }

    /**
     *
     * @param boardDescription board describe as strings of '#' for {@link  TetrominoType#NON} and 'I' for {@link TetrominoType#I}
     * @return prepared {@link GameBoard}
     */
    public static GameBoard prepareBoard(String boardDescription) {
        String descriptionStrip = boardDescription.strip();
        checkBoardSize(descriptionStrip);
        final TetrominoType[][] board = new TetrominoType[GameParameters.ROWS][GameParameters.COLUMNS];
        int row = 0;
        int collumn = 0;

        for (var c : descriptionStrip.toCharArray()) {
            switch (c) {
                case '#' -> board[row][collumn] = TetrominoType.NON;
                case 'I' -> board[row][collumn] = TetrominoType.I;
                default -> {
                    continue;
                }
            }
            collumn++;
            if (collumn == GameParameters.COLUMNS) {
                collumn = 0;
                row++;
            }
        }
        return GameBoard.createGameBoard(board);
    }

    public static String boardEmptyLine(int sizeOfLine) {
        return "#".repeat(sizeOfLine) + "\n";
    }

    public static String boardEmptyLine() {
        return boardEmptyLine(GameParameters.COLUMNS);
    }

    public static String CreateFullBoardDescription(String filledBoardPart, int rows, int cols) {
        final var lines = filledBoardPart.lines().count();
        final var emptyRows = (int) Math.max(0, rows - lines);
        return boardEmptyLine(cols).repeat(emptyRows) + filledBoardPart;
    }

    public static String CreateFullBoardDescription(String filledBoardPart) {
        return CreateFullBoardDescription(filledBoardPart, GameParameters.ROWS, GameParameters.COLUMNS);
    }

    private UtilForTests() {
    }
}



