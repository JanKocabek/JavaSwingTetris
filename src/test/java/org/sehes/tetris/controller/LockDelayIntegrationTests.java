package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.TetrominoType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockDelayIntegrationTests {


    GameManager gameManager;
    StateManager<GameState> stageManager;
    PieceGenerator pieceGenerator = new PieceGenerator() {
        @Override
        public TetrominoType peekNext() {
            return TetrominoType.T;
        }

        @Override
        public TetrominoType getNextPiece() {
            return TetrominoType.T;
        }
    };
    @Mock
    Rendering rendering;
    @Mock
    GameLoop gameLoop;

    @BeforeEach
    void setUp() {
        stageManager = new GameStateManager(GameState.INIT);
        gameManager = new GameManager(stageManager, new ScoreMessenger(), pieceGenerator, gameLoop);
        gameManager.prepareGame(rendering, null);
        gameManager.handleInput(InputAction.CONFIRM);
    }

    @Test
    void shouldSetLockDelayWhenGroundIsReached() {
        //arrange
        final var captor = ArgumentCaptor.forClass(GameSnapshot.class);
        final var startY = GameParameters.SPAWN_POINT.y();
        //act
        for (int i = startY; i < GameParameters.ROWS; i++) {
            gameManager.handleInput(InputAction.MOVE_DOWN);
        }
        final var numberOfRun = GameParameters.ROWS - startY;
        verify(rendering, times(numberOfRun)).render(captor.capture());
        final var captured = captor.getAllValues();
        //assert
        final var mino = captured.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        assertThat(captured.getLast().currentTetromino()).contains(mino);
        assertThat(captured.getLast()).isNotNull();
        assertThat(captured.getLast().lockTime()).isZero();
    }

}
