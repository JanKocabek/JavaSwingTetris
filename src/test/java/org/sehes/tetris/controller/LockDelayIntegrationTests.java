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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

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
    void testLockDelayIsSetTrueWhenGroundIsReached() {
        //arrange
        final var captor = ArgumentCaptor.forClass(GameSnapshot.class);
        verify(rendering, atLeastOnce()).render(captor.capture());
        final var mino = captor.getValue().currentTetromino().get();
        //act
        for(int i = GameParameters.SPAWN_POINT.y(); i<GameParameters.ROWS;i++)
        {
            gameManager.handleInput(InputAction.MOVE_DOWN);
        }
        verify(rendering, atLeastOnce()).render(captor.capture());
        GameSnapshot last = captor.getValue();
        //assert
        assertThat(last.currentTetromino()).contains(mino);
        assertThat(last.lockTime()).isNotNull();
        assertThat(last.distance()).isZero();
    }

}
