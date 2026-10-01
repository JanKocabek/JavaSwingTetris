package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.TetrominoType;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameManagerRestartKeyTests {

    @Mock private PieceGenerator generator;
    @Mock private GameLoop gameLoop;
    @Mock private Rendering rendering;
    @Captor private ArgumentCaptor<GameSnapshot> snapshotCaptor;
    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        when(generator.getNextPiece()).thenReturn(TetrominoType.I);
        ScoreMessenger messenger = new ScoreMessenger();
        StateManager<GameState> stateManager = new GameStateManager(GameState.PREPARED);
        gameManager = new GameManager(stateManager, messenger, generator, gameLoop,rendering);
    }

    @Test
    void shouldRestartGame() {
        //arrange
        gameManager.handleInput(InputAction.CONFIRM);
        //act
        gameManager.handleInput(InputAction.RESTART);
        //assert
        verify(generator, times(2)).getNextPiece();
        verify(gameLoop, times(1)).start();
        verify(gameLoop, times(1)).restart();
        verify(rendering, times(2)).render(snapshotCaptor.capture());
    }
}
