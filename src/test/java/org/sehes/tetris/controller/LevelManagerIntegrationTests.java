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

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LevelManagerIntegrationTests {
    @Spy private final LevelManager levelManager = new LevelManager();
    @Mock private PieceGenerator pieceGenerator;
    @Mock private GameLoop gameLoop;
    private final ScoreMessenger scoreMessenger = new ScoreMessenger();
    private final StateManager<GameState> stageManager = new GameStateManager(GameState.PREPARED);
    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        when(pieceGenerator.getNextPiece()).thenReturn(TetrominoType.I);
        when(pieceGenerator.peekNext()).thenReturn(TetrominoType.I);
    }

    @Test
    void testLevelManagerGet1RowClearWhen1RowIsCleared() {
        // Arrange
        // created when spawn was 4,1(x,y) adjust if test broken after spawn point changes
        final String boardDescription = """
                III####III
                """;
        gameManagerInit(boardDescription);

        // Act
        gameManager.handleInput(InputAction.HARD_DROP);

        // Assert
        verify(levelManager).onCleanedRows(1);
        assertThat(levelManager.getLevel()).isEqualTo(1);
    }

    @Test
    void testLevelManagerSendNewLevelWhen10RowsIsCleared() {
        // Arrange
        final var boardDescription = setBoardForIClean(10);
        gameManagerInit(boardDescription);

        // Act
        for (int i = 0; i < 10; i++) {
            gameManager.handleInput(InputAction.HARD_DROP);
        }

        // Assert
        assertThat(levelManager.getLevel()).isEqualTo(2);
    }

    @Test
    void testLevelManagerDontGetNothingIf0Line() {
        // Arrange
        gameManagerInit(GameBoard::new);

        // Act
        gameManager.handleInput(InputAction.HARD_DROP);

        // Assert
        verify(levelManager, never()).onCleanedRows(anyInt());
    }

    private void gameManagerInit(String boardDescription) {
        final String fullBoard = UtilForTests.CreateFullBoardDescription(boardDescription);
        gameManagerInit(() -> UtilForTests.prepareBoard(fullBoard));
    }

    private void gameManagerInit(Supplier<GameBoard> gameBoardSupplier) {
        gameManager = new GameManager(stageManager, scoreMessenger, pieceGenerator, gameLoop, snapshot -> {
        }, levelManager, gameBoardSupplier);
        gameManager.handleInput(InputAction.CONFIRM);
    }

    private @NonNull String setBoardForIClean(int rows) {
        return """
                III####III
                """.repeat(rows);
    }
}
