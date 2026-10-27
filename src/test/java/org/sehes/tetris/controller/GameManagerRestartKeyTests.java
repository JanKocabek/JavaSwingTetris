package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.common.GameState;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.score.ScoreInfoDTO;
import org.sehes.tetris.model.score.ScoreManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameManagerRestartKeyTests {

    @Mock private PieceGenerator generator;
    @Mock private GameLoop gameLoop;
    private GameManager gameManager;
    private final GameStateManager stateManager = new GameStateManager(GameState.PREPARED);
    private List<ScoreInfoDTO> scoreUpdates;

    @BeforeEach
    void setUp() {
        when(generator.getNextPiece()).thenReturn(TetrominoType.I);
        final ScoreManager scoreManager = new ScoreManager();
        scoreUpdates = new ArrayList<>();
        scoreManager.scoreInfoObservable().addObserver(scoreUpdates::add);
        stateManager.gameStateObservable().addObserver(scoreManager.gameStateObserver());
        gameManager = GameManagerBuilder.create()
                .setGenerator(generator)
                .setLoop(gameLoop)
                .addScoreObserver(scoreManager.scoringObserver())
                .setStateManager(stateManager).build();
    }

    @Test
    void shouldRestartGame() {
        //arrange
        gameManager.handleInput(InputAction.CONFIRM);
        gameManager.handleInput(InputAction.MOVE_DOWN);
        assertThat(scoreUpdates).anySatisfy(update -> assertThat(update.score()).isGreaterThan(0));
        //act
        gameManager.handleInput(InputAction.RESTART);
        //assert
        verify(generator, times(2)).startNewMinoQueue();
        verify(generator, times(2)).getNextPiece();
        verify(gameLoop, times(1)).start();
        verify(gameLoop, times(1)).restart();
        assertThat(scoreUpdates.getLast().score()).isZero();
        assertThat(stateManager.getState()).isEqualTo(GameState.PLAYING);
    }
}
