package org.sehes.tetris.model;

import java.util.List;

/**
 * The Tetromino class represents the individual Tetris pieces in the game. Each
 * Tetromino has a specific shape defined by a 2D boolean grid, a color for
 * rendering, and a position on the game board. The class provides methods for
 * moving and rotating the Tetromino, as well as a factory method for generating
 * random Tetromino pieces. The inner enum TETROMINO_TYPE defines the seven
 * standard Tetris pieces (I, J, L, O, S, T, Z), each with its own shape and
 * color. The Tetromino class interacts with the GameBoard to manage the current
 * piece's state and position during gameplay.
 * <p>
 * tetromino is defined by list of points where y is row and x is column
 * <p>
 * the base level is y = 0 and upper one is -1
 * center (pivot) is (0,0)
 * left side is x=-1, right side is x=1
 * <p>
 * The Tetromino is defined as List of points represent the tetromino shape,
 * color and int value The color field assigns a specific color to each
 * tetromino type for rendering purposes.
 * The intValue is a unique
 * identifier for each tetromino type, used for easy retrieval from the map.
 * The static block initializes a map that allows for quick lookup of
 * tetromino types based on their integer value, facilitating the random
 * generation of pieces in the factory method.
 * <p>
 * x is column, y is row
 *
 */
public class Tetromino {


    private final TetrominoType type;
    /**
     * The X (column) coordinate of the tetromino's position on the game board.
     * Represents the horizontal position where the tetromino is currently placed.
     * X values increase to the right, with 0 being the leftmost column.
     */
    private int positionX;
    /**
     * The Y (row) coordinate of the tetromino's position on the game board.
     * Represents the vertical position where the tetromino is currently placed.
     * Y values increase downward, with 0 being the top row.
     * Note: The game board has HIDDEN_ROWS at the top that are not visible to the player.
     */
    private int positionY;
    /**
     * List of coordinates representing the tetromino's shape relative to its position.
     * Each Coordinate contains (x,y) values where:<br>
     * - x is the column offset from the tetromino's {@link  positionX}<br>
     * - y is the row offset from the tetromino's {@link positionY}
     * <p>
     * The shape is defined relative to a center pivot point at (0,0).
     * This list changes when the tetromino rotates (see rotationState).
     * To get absolute board coordinates, add positionX/positionY to each coordinate.
     * <p>
     * coordinates are taken from {@link ShapeProvider}
     */
    private List<Coordinate> stateCoordination;
    private Orientation rotationState; // enum represent current rotation state

    Tetromino(final TetrominoType type, final Coordinate spawnPosition) {
        if (type == null || spawnPosition == null) {
            throw new IllegalArgumentException("Tetromino type and spawn position cannot be null.");
        }
        this.rotationState = Orientation.NORTH;
        this.positionX = spawnPosition.x();
        this.positionY = spawnPosition.y();
        this.type = type;
        updateStateCoordination();
    }

    public TetrominoType getType() {
        return type;
    }


    public List<Coordinate> getStateCord() {
        return stateCoordination;
    }


    /**
     * Returns the X (column) coordinate of the tetromino's position.
     *
     * @return The column position of the tetromino.
     */
    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPosition(final int x, final int y) {
        this.positionX = x;
        this.positionY = y;
    }

    public void move(final DirectionFlag flag) {
        if (flag == null) {
            return;
        }
        positionX += flag.getX();
        positionY += flag.getY();
    }


    /**
     * Sets the state of the Tetromino to the specified coordinates and orientation.
     *
     * @param coordinates The new grid configuration of the Tetromino.
     * @throws IllegalArgumentException If the coordinates are null or missing any type of next orientation.
     *                                  *
     */
    void setNewState(final List<Coordinate> coordinates, final Orientation nextOrientation) {
        if (coordinates == null || nextOrientation == null) {
            throw new IllegalArgumentException("Coordinates and next orientation cannot be null.");
        }
        this.stateCoordination = coordinates;
        this.rotationState = nextOrientation;
    }

    Orientation getCurrentOrientation() {
        return rotationState;
    }

    private void updateStateCoordination() {
        stateCoordination = ShapeProvider.getTetrominoState(type, rotationState);
    }
}
