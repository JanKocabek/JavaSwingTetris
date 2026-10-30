package org.sehes.tetris.model;

/**
 * The DirectionFlag enum defines the possible movement and rotation directions for the Tetromino pieces in the Tetris game. Each enum constant represents a specific direction (DOWN, LEFT, RIGHT) and is associated with a change in the x and y coordinates that can be used to calculate the new position of a Tetromino when it is moved. This enum provides a clear and organized way to represent the different actions that can be performed on the Tetromino pieces during gameplay.
 */

public enum DirectionFlag {
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dX;
    private final int dY;

    DirectionFlag(int dX, int dY) {
        this.dX = dX;
        this.dY = dY;
    }

    /**
     * Returns the change in the x-coordinate (column) for the given direction flag. For example, for LEFT it returns -1, for RIGHT it returns 1, and for DOWN it returns 0.
     * @return the change in the x-coordinate for the direction flag
     */
    public int getX() {
        return dX;
    }

    /**
     * Returns the change in the y-coordinate (row) for the given direction flag. For example, for DOWN it returns 1, for LEFT and RIGHT it returns 0. 
     * @return the change in the y-coordinate for the direction flag
     */
    public int getY() {
        return dY;
    }
}
