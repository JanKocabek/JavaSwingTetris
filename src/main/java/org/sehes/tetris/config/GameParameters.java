package org.sehes.tetris.config;

import org.sehes.tetris.model.Coordinate;

/**
 * The GameParameters class is a utility class that defines various constants
 * used throughout the Tetris gam preferable the model package. These constants include parameters for game
 * mechanics such as movement speed, and the dimensions of the game
 * board, as well as GUI-related parameters for window and canvas dimensions. By
 * centralizing these values in a single class, it allows for easy configuration
 * and maintenance of the game's settings, ensuring consistency across different
 * components of the application.
 */
public final class GameParameters {
    public static final int COLUMNS = 10;//number of cell in row (columns)
    public static final int ROWS = 22;//number of cell in column (rows)
    public static final int VISIBLE_ROWS = 20;//number of rows that are visible to player, the top 2 rows are for spawning new tetromino and are not visible to player
    public static final int HIDDEN_ROWS = ROWS - VISIBLE_ROWS;//the number of rows that are not visible to player, used for calculating the position of tetromino on the game board
    public static final Coordinate SPAWN_POINT = new Coordinate(COLUMNS / 2 - 1, 1);//the point where new tetromino spawns, it is in the middle of the top row of the game board

    private GameParameters() {
    }
}
