package org.sehes.tetris.model;

import org.jspecify.annotations.NonNull;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.controller.GameManager;
import org.sehes.tetris.model.ShapeProvider.WallKicks;
import org.sehes.tetris.model.ShapeProvider.WallKicks.WallKickType;
import org.sehes.tetris.model.score.TSpin;

import java.util.Arrays;
import java.util.List;

/**
 * The GameBoard class represents the game board in a Tetris game. It manages
 * the current tetromino, the state of the board, and provides methods for
 * moving, rotating, and adding tetrominoes to the board, as well as checking
 * for completed lines and clearing them. The class also provides a view of the
 * board through the IBoardView interface.
 * <p>
 * The GameBoard class is responsible for maintaining the game state and
 * ensuring that all operations on the board are valid according to the rules of
 * Tetris. It checks for collisions, boundaries, and line completions to provide
 * a seamless gaming experience.
 *
 * @author Sehes
 * @version 0.5
 * @see Tetromino
 * @see BoardView
 */
public class GameBoard {

    private static final System.Logger myLogger = System.getLogger(GameBoard.class.getName());
    private final TetrominoType[][] board;
    private final BoardView boardView;
    private final ActionSnapshot lastActionSnapshot;
    private Tetromino currentTetromino;

    public GameBoard() {
        this(createEmptyBoard());
    }

    private GameBoard(TetrominoType[][] board) {
        this.board = board;
        this.boardView = new MyBoardView();
        lastActionSnapshot = new ActionSnapshot();
    }

    public static GameBoard createGameBoard(TetrominoType[][] board) {
        return new GameBoard(board);
    }

    private static TetrominoType @NonNull [][] createEmptyBoard() {
        return fillBoard(new TetrominoType[GameParameters.ROWS][GameParameters.COLUMNS]);
    }

    private static TetrominoType[][] fillBoard(TetrominoType[][] board) {
        for (final var cell : board) {
            Arrays.fill(cell, TetrominoType.NON);
        }
        return board;
    }

    public Tetromino getCurrentTetromino() {
        return currentTetromino;
    }

    /**
     * This method returns the IBoardView instance that provides a <b> read-only
     * view of the game board.</b> <br>
     * Don't use for the changes of the Board state or its components!!!<br>
     * The IBoardView interface allows other components of the game, such as the
     * GUI, to access the state of the board without being able to modify it
     * directly. This encapsulation ensures that all changes to the board state
     * are controlled through the GameBoard class, maintaining the integrity of
     * the game logic.
     *
     * @return the IBoardView instance representing the current state of the
     * game board.
     */
    public BoardView getBoardView() {
        return boardView;
    }

    public boolean trySetNewTetromino(TetrominoType type) {
        final Coordinate startingPosition = GameParameters.SPAWN_POINT;
        return trySpawnTetromino((TetrominoFactory.spawnTetromino(type, startingPosition)));
    }

    public boolean tryMovePiece(final DirectionFlag flag) {
        if (currentTetromino == null) return false;
        if (flag == DirectionFlag.DOWN) {
            throw new IllegalArgumentException("Use trySoftDrop() for down movement");
        }
        if (canMove(currentTetromino, flag)) {
            currentTetromino.move(flag);
            lastActionSnapshot.lastActionType = LastActionType.MOVE;
            return true;
        }
        return false;
    }

    public boolean trySoftDrop() {
        boolean result = tryGravityMove();
        if (result) {
            lastActionSnapshot.lastActionType = LastActionType.DROP;
        }
        return result;
    }

    public boolean tryGravityMove() {
        if (currentTetromino == null) return false;
        if (canMove(currentTetromino, DirectionFlag.DOWN)) {
            currentTetromino.move(DirectionFlag.DOWN);
            lastActionSnapshot.lastActionType = LastActionType.MOVE;
            return true;
        }
        return false;
    }

    /**
     * This method is responsible for rotating the current tetromino in the
     * specified direction (right or left). It first checks if there is a
     * current tetromino to rotate. If there isn't, it simply returns without
     * doing anything. Then, it calculates the next grid configuration of the
     * tetromino after rotation using the rotate method of the Tetromino class.
     * Before applying the rotation, it checks for potential collisions that
     * might occur due to the new grid configuration using the checkCollisions
     * method. If there are no collisions, it updates the tetromino's grid to
     * the new rotated configuration. Finally, it prints the position of the
     * tetromino before and after rotation for debugging purposes
     *
     * @param rotation The direction in which to rotate the tetromino (
     *                 {@code CLOCKWISE} for right rotation,
     *                 {@code COUNTER_CLOCKWISE}
     *                 for left rotation)
     *                 <br>
     *                 {@code null} is not a valid rotation
     * @return {@code true} if the rotation was successful, {@code false} otherwise
     */
    public boolean tryRotatePiece(final RotationFlag rotation) {
        if (this.currentTetromino == null || rotation == null || currentTetromino.getType() == TetrominoType.O) {
            return false;
        }

        final Orientation nextOrientation = getNextOrientation(rotation);
        final List<Coordinate> rotatedPosition = ShapeProvider.getTetrominoState(currentTetromino.getType(), nextOrientation);
        final boolean rotatesInPlace = canRotate(rotatedPosition);
        final TSpinKickType kick = rotatesInPlace ? TSpinKickType.NONE : tryWallKick(rotatedPosition, nextOrientation);

        lastActionSnapshot.tSpinKickType = kick;
        if (rotatesInPlace || kick != TSpinKickType.NONE) {
            currentTetromino.setNewState(rotatedPosition, nextOrientation);
            lastActionSnapshot.lastActionType = LastActionType.ROTATE;
            return true;
        }
        return false;
    }

    private Orientation getNextOrientation(RotationFlag rotation) {
        return rotation == RotationFlag.CLOCKWISE ? currentTetromino.getCurrentOrientation().rotateClockwise() : currentTetromino.getCurrentOrientation().rotateCounterClockwise();
    }

    /**
     * This should be called only from {@link GameManager#lockClearAndScorePiece()}  <br>
     * This method is responsible for locking the current tetromino in place on the game board.
     * It's last places where can be reliably find if the T-spin happened
     * then locks the tetromino.
     *
     */
    public void lockTetrominoInPlace() {
        checkTSpin();
        lockTetromino();
    }

    private void checkTSpin() {
        if (currentTetromino.getType() != TetrominoType.T || lastActionSnapshot.lastActionType != LastActionType.ROTATE) {
            lastActionSnapshot.tSpin = TSpin.NONE;
            return;
        }
        final int[][] frontCornersOffset = TSpin.getFrontCornersOffset(currentTetromino.getCurrentOrientation());
        final int[][] backCornersOffset = TSpin.getBackCornersOffset(currentTetromino.getCurrentOrientation());
        final int frontCornersCount = checkCornersAroundT(frontCornersOffset);
        final int backCornersCount = checkCornersAroundT(backCornersOffset);
        final var isTSpinKick = lastActionSnapshot.tSpinKickType == TSpinKickType.T_SPIN_KICK;
        lastActionSnapshot.tSpin = TSpin.getTSpin(frontCornersCount, backCornersCount, isTSpinKick);
    }

    /**
     *
     * This method is responsible for adding the current tetromino to the game
     * board when it can no longer move down. It iterates through the grid of
     * the current tetromino and updates the corresponding positions on the game
     * board with the appropriate TetrominoType.
     * This effectively "locks" the tetromino in place on the board,
     * allowing the elimination of completed lines and the spawning of a new
     * tetromino to occur in subsequent game logic.
     */
    private void lockTetromino() {
        for (Coordinate coordinate : currentTetromino.getStateCord()) {
            final int x = currentTetromino.getPositionX() + coordinate.x();
            final int y = currentTetromino.getPositionY() + coordinate.y();
            this.board[y][x] = currentTetromino.getType();
        }
        currentTetromino = null;
    }

    /**
     * This method checks for completed lines on the game board and clears them
     * if found. It iterates through each row of the board and uses the
     * checkLine method to determine if a line is full (i.e., contains no EMPTY
     * blocks). If a full line is detected, it sets all blocks in that row to
     * EMPTY and shifts all rows above it down by one.
     * The method doesn't return anything but set number of cleared lines into {@link ActionSnapshot.LastActionSnapshot}
     */
    public void clearLines() {
        int linesClearedCount = 0;
        for (int row = 0; row < board.length; row++) {
            if (isLineFull(board[row])) {
                shiftLinesDown(row);
                linesClearedCount++;
                row--;// todo: in future make the counting of line and clearing of lines separated to
                // prevent this
            }
        }
        lastActionSnapshot.linesCleared = linesClearedCount;
    }

    private int checkCornersAroundT(int[][] offset) {
        final var y = currentTetromino.getPositionY();
        final var x = currentTetromino.getPositionX();
        return (int) Arrays.stream(offset).filter(o -> isValidFilledCorner(x + o[0], y + o[1])).count();
    }

    private boolean isValidFilledCorner(int x, int y) {
        final var isInBoard = x >= 0 && y >= 0 && x < board[0].length && y < board.length;
        return !isInBoard || board[y][x] != TetrominoType.NON;
    }

    /**
     * This method is responsible for attempting to wall kick the current tetromino
     * if it cannot be rotated into its next orientation without collision. It uses
     * the wall kick table to find the possible wall kicks that can be applied to
     * the current tetromino, and then checks each wall kick to see if it results in
     * a valid position for the tetromino. If a valid wall kick is found, it updates
     * the tetromino's position accordingly.
     *
     * @param rotatedPosition the grid configuration of the tetromino after
     *                        rotation
     * @return value of {@link TSpinKickType} that represents if a valid wall kick
     * was found and if the kick matches the special 1×2 T-Spin kick
     */
    private TSpinKickType tryWallKick(List<Coordinate> rotatedPosition, Orientation nextOrientation) {
        WallKickType wallKickType = currentTetromino.getType() == TetrominoType.I ? WallKickType.I_KICKS : WallKickType.NORMAL;
        List<Coordinate> wallKicks = WallKicks.getWallKicks(wallKickType, currentTetromino.getCurrentOrientation().getTransitionTo(nextOrientation));
        for (Coordinate cord : wallKicks) {
            int testX = currentTetromino.getPositionX() + cord.x();
            int testY = currentTetromino.getPositionY() + cord.y();
            if (tetrominoPositionValidCheck(rotatedPosition, testX, testY)) {
                currentTetromino.setPosition(testX, testY);
                return Math.abs(cord.x()) == 1 && Math.abs(cord.y()) == 2 ? TSpinKickType.T_SPIN_KICK : TSpinKickType.ORDINARY;
            }
        }
        return TSpinKickType.NONE;
    }

    private boolean isLineFull(final TetrominoType[] boardRow) {
        for (final var cell : boardRow) {
            if (cell == TetrominoType.NON) {
                return false;
            }
        }
        return true;
    }

    /**
     * checks if a tetromino can move to a new position without colliding with
     * existing pieces on the game board or going out of boundaries.
     *
     * @param tetromino The tetromino to be moved
     * @param direction The direction in which to move the tetromino
     * @return {@code true} if the move is valid, {@code false} otherwise
     */
    private boolean canMove(final Tetromino tetromino, final DirectionFlag direction) {
        final int futureX = tetromino.getPositionX() + direction.getX();
        final int futureY = tetromino.getPositionY() + direction.getY();
        return canMove(tetromino.getStateCord(), futureX, futureY);
    }

    /**
     * Overloaded and sub part version of {@link #canMove(Tetromino, DirectionFlag)} that takes directly a list of coordinates and future positions<br>
     * this method is used directly by {@link #tryHardDrop()}
     *
     * @param coordinates current tetromino shape as list of points
     * @param futureX     where it will move on axis X horizontal
     * @param futureY     where it will move on axis Y vertical
     * @return {@code true} if the move is valid, {@code false} otherwise
     */
    private boolean canMove(List<Coordinate> coordinates, int futureX, int futureY) {
        return tetrominoPositionValidCheck(coordinates, futureX, futureY);
    }

    public int tryHardDrop() {
        if (currentTetromino == null) {
            return 0;
        }
        final var distance = calculateDropDistance();
        if (distance != 0) {
            currentTetromino.setPosition(currentTetromino.getPositionX(), currentTetromino.getPositionY() + distance);
        }
        lastActionSnapshot.lastActionType = LastActionType.DROP;
        return distance;
    }

    public int calculateDropDistance() {
        if (currentTetromino == null) {
            throw new IllegalStateException("Cannot calculate drop distance when no tetromino is present");
        }
        var distance = 0;
        final var coordinates = currentTetromino.getStateCord();
        final var posX = currentTetromino.getPositionX();
        final var posY = currentTetromino.getPositionY();
        while (canMove(coordinates, posX, posY + 1 + distance)) {
            distance++;
        }
        return distance;
    }

    /**
     * Checks if the position after moving or rotating a piece would collide with
     * any existing pieces on the game board.
     *
     * @param stateCord    The list of coordinates representing the tetromino's new
     *                     state
     * @param newPositionX The expected new X coordination of the tetromino pivot on the
     *                     board
     * @param newPositionY The expected new position of the tetromino pivot on the
     *                     board
     * @return {@code true} if there is a collision, {@code false} otherwise
     */
    private boolean isCollisionFree(final List<Coordinate> stateCord, final int newPositionX, final int newPositionY) {

        for (final var cord : stateCord) {
            if (this.board[newPositionY + cord.y()][newPositionX + cord.x()] != TetrominoType.NON) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the position after rotating a piece is within the game board
     * boundaries and does not collide with any other pieces.
     *
     * @param rotatedStateCord The list of coordinates representing the tetromino's
     *                         state after the expected rotation
     * @return {@code true} if the original position is valid, {@code false}
     * otherwise
     */
    private boolean canRotate(final List<Coordinate> rotatedStateCord) {
        if (rotatedStateCord == null || currentTetromino == null) {
            return false;
        }

        return tetrominoPositionValidCheck(rotatedStateCord, currentTetromino.getPositionX(), currentTetromino.getPositionY());
    }

    /**
     * Checks if the position of the tetromino after a move or rotation is out of
     * the game board boundaries. *
     *
     * @param newStateCord The coordinates of the tetromino after the move or
     *                     rotation.
     * @param positionX    The current X Coordinate of the tetromino on the board
     * @param positionY    The current Y Coordinate of the tetromino on the board
     * @return {@code true} if the final position is out of the board
     * boundaries, {@code false} otherwise.
     */
    private boolean isInTheBoundaries(List<Coordinate> newStateCord, int positionX, int positionY) {
        int width = board[0].length;
        int height = board.length;
        for (Coordinate cord : newStateCord) {
            int newX = cord.x() + positionX;
            int newY = cord.y() + positionY;
            if (newX < 0 || newY < 0 || newX >= width || newY >= height) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    private void shiftLinesDown(final int currentRow) {
        if (currentRow == 0) {
            return;
        }
        for (int cr = currentRow; cr > 0; cr--) {
            System.arraycopy(board[cr - 1], 0, board[cr], 0, board[cr].length);
        }
        Arrays.fill(board[0], TetrominoType.NON);
    }

    /**
     * this method set the current tetromino to the specific tetromino
     *
     * @param tetromino the tetromino you need to spawn
     */

    boolean trySpawnTetromino(final Tetromino tetromino) {
        if (tetrominoCurrentPositionValidCheck(tetromino)) {
            this.currentTetromino = tetromino;
            lastActionSnapshot.reset();
            return true;
        }
        return false;
    }

    /**
     * Checks if the current position of the tetromino is valid.
     *
     * @param tetromino the tetromino to check
     * @return {@code true} if the current position is valid, {@code false} otherwise
     */
    private boolean tetrominoCurrentPositionValidCheck(Tetromino tetromino) {
        final var stateCord = tetromino.getStateCord();
        final var posX = tetromino.getPositionX();
        final var posY = tetromino.getPositionY();
        return tetrominoPositionValidCheck(stateCord, posX, posY);
    }


    /**
     * Checks if the tetromino based on given coordinates and position(current or future) is in the boundaries and does not collide with any other pieces.
     *
     * @param coordinates
     * @param positionX
     * @param positionY
     * @return {@code true} if the tetromino is in the boundaries and does not collide with any other pieces, {@code false} otherwise
     */
    private boolean tetrominoPositionValidCheck(List<Coordinate> coordinates, int positionX, int positionY) {
        return isInTheBoundaries(coordinates, positionX, positionY) && isCollisionFree(coordinates, positionX, positionY);
    }


    void createGarbageLine() {
        int lastLine = board.length - 1;
        Arrays.fill(board[lastLine], TetrominoType.L);
    }

    /**
     * replace this in the near future
     *
     * @param row
     * @param column
     * @param type
     */

    /**
     * JUNIT TEST ONLY<br>
     * This method sets a specific {@link TetrominoType} to a specific position on the game board.
     * This method is used for testing purposes to fill the game board with {@link TetrominoType}s.
     *
     * @param row    the row of the block to be set
     * @param column the column of the block to be set
     * @param type   the {@link TetrominoType} to be set
     */
    @Deprecated
    void fillBlockForTestOnly(final int row, final int column, final TetrominoType type) {
        board[row][column] = type;
    }

    public ActionSnapshot.LastActionSnapshot getLastAction() {
        return lastActionSnapshot.getActionSnapshot();
    }

    private static final class ActionSnapshot {
        private TSpinKickType tSpinKickType;
        private int linesCleared;
        private LastActionType lastActionType;
        private TSpin tSpin;

        ActionSnapshot() {
            reset();
        }

        private void reset() {
            this.tSpinKickType = TSpinKickType.NONE;
            this.linesCleared = 0;
            this.lastActionType = null;
            this.tSpin = TSpin.NONE;
        }

        /**
         * this method should be call only from {@link GameManager#lockClearAndScorePiece()}
         * its purpose is to return the last action info for score calculation
         *
         * @return record {@link LastActionSnapshot} containing tSpin and lines cleared information
         */
        public LastActionSnapshot getActionSnapshot() {
            return new LastActionSnapshot(this.tSpin, this.linesCleared);
        }

        public record LastActionSnapshot(TSpin tSpin, int linesCleared) {
        }
    }

    private class MyBoardView implements BoardView {
        @Override
        public int getWidth() {
            return board[0].length;
        }

        @Override
        public int getHeight() {
            return board.length;
        }

        @Override
        public TetrominoType getBlockContent(final int row, final int column) {
            if (row < 0 || row >= board.length || column < 0 || column >= board[row].length) {
                throw new IndexOutOfBoundsException("Coordinates are out of bounds. Row: " + row + ", Column: " + column + ". Board size: " + board.length + "x" + board[0].length);
            }
            return board[row][column];
        }
    }
}
