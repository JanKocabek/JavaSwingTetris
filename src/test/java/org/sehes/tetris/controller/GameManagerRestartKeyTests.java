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
import org.sehes.tetris.model.score.ScoreInfoDTO;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameManagerRestartKeyTests {

    @Mock private PieceGenerator generator;
    @Mock private GameLoop gameLoop;
    @Mock private Rendering rendering;
    @Captor private ArgumentCaptor<GameSnapshot> snapshotCaptor;
    private GameManager gameManager;
    private final GameStateManager stateManager = new GameStateManager(GameState.PREPARED);
    private List<ScoreInfoDTO> scoreUpdates;

    @BeforeEach
    void setUp() {
        stateManager.setState(GameState.PREPARED);
        when(generator.getNextPiece()).thenReturn(TetrominoType.I);
        final ScoreMessenger messenger = new ScoreMessenger();
        final ScoreManager scoreManager = new ScoreManager();
        messenger.addObserver(scoreManager.scoringObserver());
        scoreUpdates = new ArrayList<>();
        scoreManager.ScoreInfoObservable().addObserver(scoreUpdates::add);
        stateManager.gameStateObservable().addObserver(scoreManager.gameStateObserver());
        gameManager = new GameManager(stateManager, messenger, generator, gameLoop, rendering);
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
        verify(generator, times(2)).startNewSequence();
        verify(generator, times(2)).getNextPiece();
        verify(gameLoop, times(1)).start();
        verify(gameLoop, times(1)).restart();
        verify(rendering, times(3)).render(snapshotCaptor.capture());
        assertThat(scoreUpdates.getLast().score()).isZero();
        assertThat(stateManager.getState()).isEqualTo(GameState.PLAYING);
    }
}
