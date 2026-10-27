package org.sehes.tetris.common;

// Define the possible game states
public enum GameState {
    /**
     * current game is over (game over screen is shown, game scoreBoard will be visible etc...)
     */
    GAME_OVER,
    /**
     * first state - preparing of app (creating,loading assets, assembling and starting gui and all other components etc..)
     */
    INIT,
    /**
     * new game preparing (creating new gameBoard,seting score to 0, etc...)
     */
    NEW_GAME,
    /**
     * current game is paused
     */
    PAUSED,
    /**
     * current game is running
     */
    PLAYING,
    /**
     * app is responding and prepared for starting a new game
     */
    PREPARED

}
