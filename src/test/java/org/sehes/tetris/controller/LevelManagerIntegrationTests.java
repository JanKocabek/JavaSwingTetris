package org.sehes.tetris.controller;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.*;
import org.sehes.tetris.model.score.ScoreEvent;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LevelManagerIntegrationTests {
    private GameManager gameManager;
    @Spy private LevelManager levelManager = new LevelManager();
    @Mock private PieceGenerator pieceGenerator;
    @Mock private Observer<ScoreEvent> scoreObserver;
    @Mock private Rendering rendering;
    @Mock private GameLoop gameLoop;
    private final ScoreMessenger scoreMessenger = new ScoreMessenger();
    private StateManager<GameState> stageManager;

    @BeforeEach
    void setUp() {
        when(pieceGenerator.getNextPiece()).thenReturn(TetrominoType.I);
        when(pieceGenerator.peekNext()).thenReturn(TetrominoType.I);
        scoreMessenger.addObserver(scoreObserver);
        stageManager = new GameStateManager(GameState.PREPARED);
    }

    private void gameManagerInit(String boardDescription) {
        final String fullBoard = UtilForTests.CreateFullBoardDescription(boardDescription);
        final Supplier<GameBoard> gbs = () -> UtilForTests.prepareBoard(fullBoard);
        gameManager = new GameManager(stageManager, scoreMessenger, pieceGenerator, gameLoop, rendering, levelManager, gbs);
        gameManager.handleInput(InputAction.CONFIRM);
    }

    private void gameManagerInit() {
        gameManager = new GameManager(stageManager, scoreMessenger, pieceGenerator, gameLoop, rendering);
        gameManager.handleInput(InputAction.CONFIRM);
    }

    @Test
    void testLevelManagerGet1RowClearWhen1RowIsCleared() {
        // 1. Arrange
        // created when spawn was 4,1(x,y) adjust if test broken after spawn point changes
        final String boardDescription = """
                III####III
                """;
        gameManagerInit(boardDescription);
        // 2. Act
        gameManager.handleInput(InputAction.HARD_DROP);
        // 3. Assert
        verify(levelManager).onCleanedRows(1);
        assertThat(levelManager.getLevel()).isEqualTo(1);
    }

    @Test
    void testLevelManagerSendNewLevelWhen10RowsIsCleared() {
        //Arrange
        final var boardDescription = setBoardForIClean(10);
        gameManagerInit(boardDescription);
        //Act
        for (int i = 0; i < 10; i++) {
            gameManager.handleInput(InputAction.HARD_DROP);
        }
        //Assert
        assertThat(levelManager.getLevel()).isEqualTo(2);
    }

    @Test
    void testLevelManagerDontGetNothingIf0Line() {
        gameManagerInit();
        gameManager.handleInput(InputAction.HARD_DROP);
        verifyNoInteractions(levelManager);
    }

    private @NonNull String setBoardForIClean(int rows) {
        return """
                III####III
                """.repeat(rows);
    }
}
