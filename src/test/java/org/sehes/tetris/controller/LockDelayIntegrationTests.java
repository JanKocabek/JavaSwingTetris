package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.score.LockPieceEvent;
import org.sehes.tetris.model.score.ScoreEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockDelayIntegrationTests {
    public static final int TICK_MS = 16;
    public static final long TICK = TimeUnit.MILLISECONDS.toNanos(TICK_MS);
    public static final int LOCK_DELAY_TIME_MS = 500;
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
    ScoreMessenger scoreMessenger;
    @Mock
    Observer<ScoreEvent> scoreObserver;
    @Mock
    Rendering rendering;
    @Mock
    GameLoop gameLoop;
    @Captor
    ArgumentCaptor<GameSnapshot> gameSnapshotCaptor;
    @Captor
    ArgumentCaptor<ScoreEvent> scoreEventCaptor;

    @BeforeEach
    void setUp() {
        scoreMessenger = new ScoreMessenger();
        scoreMessenger.addObserver(scoreObserver);
        stageManager = new GameStateManager(GameState.INIT);
        gameManager = new GameManager(stageManager, scoreMessenger, pieceGenerator, gameLoop);
        gameManager.prepareGame(rendering, null);
        gameManager.handleInput(InputAction.CONFIRM);
    }


    @Test
    @DisplayName("should start lock delay when ground is reached-by user moving down")
    void shouldStartLockDelayWhenGroundIsReachedMoving() {
        //arrange
        final var startY = GameParameters.SPAWN_POINT.y();
        //act
        moveToBottom(startY);
        final var numberOfRun = GameParameters.ROWS - startY;
        verify(rendering, times(numberOfRun)).render(gameSnapshotCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        //assert
        final var mino = captured.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));

        assertThat(captured.getFirst().lockTime()).isNull();
        assertThat(captured.getLast().currentTetromino()).contains(mino);
        assertThat(captured.getLast().distance()).isZero();
        assertThat(captured.getLast().lockTime()).isZero();
    }

    @Test
    @DisplayName("should set lock delay when ground is reached-by gravity")
    void shouldSetLockDelayWhenGroundIsReachedGravity() {
        //act
        runTickNTimes(1000);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        //assert
        final var mino = captured.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var droppedFrameOpt = captured.stream().filter(snapshot -> snapshot.lockTime() != null).findFirst();
        assertThat(droppedFrameOpt).isNotEmpty();
        final var dropped = droppedFrameOpt.get();
        assertThat(captured.getFirst().lockTime()).isNull();
        assertThat(dropped.lockTime()).isNotNull();
        assertThat(dropped.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"))).isEqualTo(mino);
    }

    @Test
    @DisplayName("should lock after 500ms - gravity fall")
    void shouldLockAfter500msGravity() {
        //act
        runTickNTimes(1000);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, times(1)).update(scoreEventCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        final var scoreEvent = scoreEventCaptor.getValue();
        //assert
        final var exactMino = captured.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var lockedFrames = captured.stream().filter(snapshot -> snapshot.lockTime() != null && snapshot.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty")) == exactMino).toList();
        assertThat(lockedFrames).hasSizeLessThan(600);
        //+-one tick of lock delay because game manager accumulators always consume more than exact 1 tick
        double atLeastBeforeLock = (double) (LOCK_DELAY_TIME_MS - TICK_MS) / 1000;
        double maxBeforeLock = (double) (LOCK_DELAY_TIME_MS + TICK_MS) / 1000;
        assertThat(lockedFrames.getLast().lockTime()).isBetween(atLeastBeforeLock, maxBeforeLock);
        assertThat(scoreEvent).isNotNull().isInstanceOf(LockPieceEvent.class);
    }

    @Test
    @DisplayName("should lock after 500ms sitting on bottom ")
    void shouldLockAfter500msSittingOnBottom() {
        //act
        moveToBottom(GameParameters.SPAWN_POINT.y());
        runTickNTimes(500);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, atLeastOnce()).update(scoreEventCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        final var eventCaptured = scoreEventCaptor.getAllValues();
        //assert
        final var exactMino = captured.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var lockedFrames = captured.stream().filter(snapshot -> snapshot.lockTime() != null && snapshot.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty")) == exactMino).toList();
        final var lockEventCount = eventCaptured.stream().filter(LockPieceEvent.class::isInstance).count();
        //+-one tick of lock delay because game manager accumulators always consume more than exact 1 tick
        double atLeastBeforeLock = (double) (LOCK_DELAY_TIME_MS - TICK_MS) / 1000;
        double maxBeforeLock = (double) (LOCK_DELAY_TIME_MS + TICK_MS) / 1000;
        assertThat(lockedFrames).hasSizeLessThan(600);
        assertThat(lockedFrames.getLast().lockTime()).isBetween(atLeastBeforeLock, maxBeforeLock);
        assertThat(lockEventCount).isOne();
    }


    @Test
    @DisplayName("should reset counting after move ")
    void shouldResetCountingAfterMove() {
        //act
        moveToBottom(GameParameters.SPAWN_POINT.y());
        runTickNTimes(20);
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_RIGHT);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        final var lastLockDealyTime = captured.getLast().lockTime();
        final var counted = captured.stream().filter(snapshot -> Objects.equals(snapshot.lockTime(), lastLockDealyTime)).count();
        //assert
        assertThat(counted).isEqualTo(3L);
    }

    @Test
    @DisplayName("Hard drop ignore LockDealy Time")
    void hardDropIgnoreLockDelayTime() {
        //act
        moveToBottom(GameParameters.SPAWN_POINT.y());
        runTickNTimes(10);
        gameManager.handleInput(InputAction.HARD_DROP);
        runTickNTimes(1);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, atLeastOnce()).update(scoreEventCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        final var eventCaptured = scoreEventCaptor.getAllValues();
        final var capturedCnt = captured.size();
        final var lastCaptBeforeNew = captured.get(capturedCnt - 2);
        final var hardDropEvt = eventCaptured.getLast();
        final var lockEventCount = eventCaptured.stream().filter(LockPieceEvent.class::isInstance).count();
        //assert
        final var firstMino = captured.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        assertThat(hardDropEvt).isInstanceOf(LockPieceEvent.class);
        assertThat(lockEventCount).isOne();
        assertThat(lastCaptBeforeNew.lockTime()).isNotNull();
        assertThat(lastCaptBeforeNew.lockTime()).isBetween(0.1, 0.3);
        assertThat(captured.getLast().lockTime()).isNull();
        final var newMino = captured.getLast().currentTetromino();
        assertThat(newMino).isNotNull();
        assertThat(newMino.get()).isNotEqualTo(firstMino);
    }

    @Test
    void resetLockDelayWhenMoveFromOneDepthToLower() {
        final var startY = GameParameters.SPAWN_POINT.y();
        //act
        gameManager.handleInput(InputAction.HARD_DROP);
        for (int i = startY; i < 19; i++) {
            gameManager.handleInput(InputAction.MOVE_DOWN);
        }
        runTickNTimes(10);
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_DOWN);
        runTickNTimes(33);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var captured = gameSnapshotCaptor.getAllValues();
        final var lastLockDealyTime = captured.getLast().lockTime();
        final var LockStartCount = captured.stream().filter(snapshot -> Objects.equals(snapshot.lockTime(), lastLockDealyTime)).count();
        //assert
        assertThat(LockStartCount).isEqualTo(3L);
        assertThat(lastLockDealyTime).isNotNull();
    }

    private void runTickNTimes(int number) {
        for (var i = 0; i < number; i++) {
            gameManager.tickObserver().update(TICK);
        }
    }

    private void moveToBottom(int startY) {
        for (int i = startY; i < GameParameters.ROWS; i++) {
            gameManager.handleInput(InputAction.MOVE_DOWN);
        }
    }

}
