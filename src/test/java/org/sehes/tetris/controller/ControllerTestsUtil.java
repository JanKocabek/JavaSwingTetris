package org.sehes.tetris.controller;

import org.sehes.tetris.model.GameBoard;
import org.sehes.tetris.model.LevelManager;
import org.sehes.tetris.model.PieceGenerator;

public class ControllerTestsUtil {
    static GameManager createdGameManager(StateManager<GameState> stageManager, ScoreMessenger scoreMessenger, PieceGenerator generator, GameLoop gameLoop, Rendering rendering) {
        return new GameManager(stageManager, scoreMessenger, generator, gameLoop, rendering, new LevelManager(), GameBoard::new);
    }
}
