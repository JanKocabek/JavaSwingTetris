package org.sehes.tetris.controller;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.common.event.Observer;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.GameBoard;
import org.sehes.tetris.model.GameSnapshot;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.level.LevelManager;
import org.sehes.tetris.model.score.LockPieceEvent;
import org.sehes.tetris.model.score.ScoreEvent;
import org.sehes.tetris.model.score.ScoreMessenger;
import org.sehes.tetris.testUtil.UtilForTests;
import org.sehes.tetris.view.Rendering;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LevelManagerIntegrationTests {
    @Mock private Rendering renderer;
    // Arrange
    @Spy private final LevelManager levelManager = new LevelManager();
    @Captor private ArgumentCaptor<GameSnapshot> gameSnapshotCaptor;
    private GameManager gameManager;


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
        gameManager = GameManagerBuilder.create()
                .setGenerator(TetrominoType.I)
                .setLevelManager(levelManager)
                .setGameBoardSupplier(GameBoard::new)
                .build();
        gameManager.handleInput(InputAction.CONFIRM);
        // Act
        gameManager.handleInput(InputAction.HARD_DROP);
        // Assert
        verify(levelManager, never()).onCleanedRows(anyInt());
    }

    @Test
    void testLVLManagerRestartOnNewGame() {
        // Arrange
        final var boardDescription = setBoardForIClean(10);
        gameManagerInit(boardDescription);

        // Act
        for (int i = 0; i < 10; i++) {
            gameManager.handleInput(InputAction.HARD_DROP);
        }
        gameManager.handleInput(InputAction.RESTART);

        // Assert
        assertThat(levelManager.getLevel()).isEqualTo(1);

    }

    @Test
    void testLevelManagerProgressesWhenScoreObserverThrows() {
        // Arrange
        final String boardDescription = """
                III####III
                """;
        final String fullBoard = UtilForTests.CreateFullBoardDescription(boardDescription);
        final Observer<ScoreEvent> failingObserver = event -> {
            throw new RuntimeException("Simulated observer exception");
        };

        gameManager = GameManagerBuilder.create()
                .setGenerator(TetrominoType.I)
                .setLevelManager(levelManager)
                .addScoreObserver(failingObserver)
                .setGameBoardSupplier(() -> UtilForTests.prepareBoard(fullBoard))
                .build();
        gameManager.handleInput(InputAction.CONFIRM);

        // Act
        gameManager.handleInput(InputAction.HARD_DROP);

        // Assert
        verify(levelManager).onCleanedRows(1);
    }

    @Test
    void testLevelManagerProgressesWhenScoreMessengerThrowsDirectly() {
        // Arrange
        final String boardDescription = """
                III####III
                """;
        final String fullBoard = UtilForTests.CreateFullBoardDescription(boardDescription);
        final ScoreMessenger throwingMessenger = mock(ScoreMessenger.class);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof LockPieceEvent) {
                throw new RuntimeException("ScoreMessenger unexpected error");
            }
            return null;
        }).when(throwingMessenger).notifyObservers(any());

        gameManager = GameManagerBuilder.create()
                .setGenerator(TetrominoType.I)
                .setLevelManager(levelManager)
                .setScoreMessenger(throwingMessenger)
                .setGameBoardSupplier(() -> UtilForTests.prepareBoard(fullBoard))
                .build();
        gameManager.handleInput(InputAction.CONFIRM);

        // Act
        gameManager.handleInput(InputAction.HARD_DROP);

        // Assert
        verify(levelManager).onCleanedRows(1);
    }

    @Test
    void testLevel20SpeedIsInstant() {
        //Arrange
        final var TICK = TimeUnit.MILLISECONDS.toNanos(16);
        final String fullBoard = UtilForTests.CreateFullBoardDescription("III####III\n");
        gameManager = GameManagerBuilder.create()
                .setGenerator(TetrominoType.I)
                .setLevelManager(levelManager)
                .setGameBoardSupplier(() -> UtilForTests.prepareBoard(fullBoard))
                .setRender(renderer)
                .build();
        gameManager.handleInput(InputAction.CONFIRM);
        levelManager.set(20);
        // Act
        gameManager.tickObserver().update(TICK);
        verify(renderer,atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var gameSnapshot = gameSnapshotCaptor.getAllValues();
        final var firstMino=gameSnapshot.getFirst().currentTetromino().orElseThrow();
        // Assert
        assertThat(gameSnapshot.getLast().lockTime()).isNotNull();
        assertThat(gameSnapshot.getLast().ghostDistance()).isZero();
        assertThat(firstMino).isEqualTo(gameSnapshot.getLast().currentTetromino().orElseThrow());
        assertThat(gameSnapshot).hasSize(2);
    }

    private void gameManagerInit(String boardDescription) {
        final String fullBoard = UtilForTests.CreateFullBoardDescription(boardDescription);
        gameManager = GameManagerBuilder.create()
                .setGenerator(TetrominoType.I)
                .setLevelManager(levelManager)
                .setGameBoardSupplier(() -> UtilForTests.prepareBoard(fullBoard))
                .build();
        gameManager.handleInput(InputAction.CONFIRM);
    }

    private @NonNull String setBoardForIClean(int rows) {
        return """
                III####III
                """.repeat(rows);
    }
}
