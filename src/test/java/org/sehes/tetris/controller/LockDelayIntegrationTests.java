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
import org.sehes.tetris.model.TetrominoFactory;
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
        //act
        moveToBottom();
        final var numberOfRun = GameParameters.ROWS - GameParameters.SPAWN_POINT.y();
        verify(rendering, times(numberOfRun)).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        //assert
        final var mino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));

        assertThat(gameSnapshotList.getFirst().lockTime()).isNull();
        assertThat(gameSnapshotList.getLast().currentTetromino()).contains(mino);
        assertThat(gameSnapshotList.getLast().currentTetromino().orElseThrow().getPositionY()).isEqualTo(GameParameters.ROWS - 1);
        assertThat(gameSnapshotList.getLast().lockTime()).isZero();
    }

    @Test
    @DisplayName("should set lock delay when ground is reached-by gravity")
    void shouldSetLockDelayWhenGroundIsReachedGravity() {
        //act
        runTickNTimes(1000);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        //assert
        final var mino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var droppedFrameOpt = gameSnapshotList.stream().filter(snapshot -> snapshot.lockTime() != null).findFirst();
        assertThat(droppedFrameOpt).isNotEmpty();
        final var dropped = droppedFrameOpt.get();
        assertThat(gameSnapshotList.getFirst().lockTime()).isNull();
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
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var scoreEvent = scoreEventCaptor.getValue();
        //assert
        final var exactMino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var lockedFrames = gameSnapshotList.stream().filter(snapshot -> snapshot.lockTime() != null && snapshot.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty")) == exactMino).toList();
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
        moveToBottom();
        runTickNTimes(500);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, atLeastOnce()).update(scoreEventCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var eventCaptured = scoreEventCaptor.getAllValues();
        //assert
        final var exactMino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var lockedFrames = gameSnapshotList.stream().filter(snapshot -> snapshot.lockTime() != null && snapshot.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty")) == exactMino).toList();
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
        moveToBottom();
        runTickNTimes(20);
        moveLeftAndBack();
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var lastLockDealyTime = gameSnapshotList.getLast().lockTime();
        final var counted = gameSnapshotList.stream().filter(snapshot -> Objects.equals(snapshot.lockTime(), lastLockDealyTime)).count();
        //assert
        assertThat(counted).isEqualTo(3L);
    }

    @Test
    @DisplayName("Hard drop ignore LockDealy Time")
    void hardDropIgnoreLockDelayTime() {
        //act
        moveToBottom();
        runTickNTimes(10);
        gameManager.handleInput(InputAction.HARD_DROP);
        runTickNTimes(1);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, atLeastOnce()).update(scoreEventCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var eventCaptured = scoreEventCaptor.getAllValues();
        final var capturedCnt = gameSnapshotList.size();
        final var lastCaptBeforeNew = gameSnapshotList.get(capturedCnt - 2);
        final var firstMino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var newMino = gameSnapshotList.getLast().currentTetromino();
        //assert
        assertThat(eventCaptured.getLast()).isInstanceOf(LockPieceEvent.class);
        assertThat(eventCaptured).filteredOn(LockPieceEvent.class::isInstance).hasSize(1);
        assertThat(lastCaptBeforeNew.lockTime()).isNotNull();
        assertThat(lastCaptBeforeNew.lockTime()).isBetween(0.1, 0.3);
        assertThat(gameSnapshotList.getLast().lockTime()).isNull();
        assertThat(newMino).isNotNull();
        assertThat(newMino.orElseThrow()).isNotEqualTo(firstMino);
    }

    @Test
    void resetLockDelayWhenMoveFromOneDepthToLower() {
        //arrange
        final var firstPiece = pieceGenerator.peekNext();
        final var pieceData = TetrominoFactory.spawnTetromino(firstPiece, GameParameters.SPAWN_POINT);
        final var spawnY = GameParameters.SPAWN_POINT.y();
        final var rowTakesAfterDrop = pieceData.getStateCord().stream().filter(cell -> cell.x() == 0).count();
        //act
        gameManager.handleInput(InputAction.HARD_DROP);//2 FRAME
        var cycleToDropNewPieceOn = GameParameters.ROWS - spawnY - rowTakesAfterDrop;
        var cycles = 0;
        for (int i = spawnY; i < cycleToDropNewPieceOn; i++) {
            gameManager.handleInput(InputAction.MOVE_DOWN);
            cycles++;
        }
        int tickStayDropped = 10;
        runTickNTimes(tickStayDropped);
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_DOWN);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var allSnapshots = gameSnapshotCaptor.getAllValues();
        final var lastLockDealyTime = allSnapshots.getLast().lockTime();

        //assert
        int hardDropFrames = 2;
        int snapWhenDrop = hardDropFrames + cycles + tickStayDropped - 1;//start on 0;
        assertThat(allSnapshots.getFirst().currentTetromino().orElseThrow()).isNotEqualTo(allSnapshots.get(2).currentTetromino().orElseThrow());
        assertThat(allSnapshots.get(snapWhenDrop).lockTime()).isEqualTo(0.16);
        assertThat(allSnapshots.get(snapWhenDrop + 1).lockTime()).isZero();
        assertThat(allSnapshots.get(snapWhenDrop + 2).lockTime()).isNull();
        assertThat(lastLockDealyTime).isZero();
    }

    @Test
    void should16MoveStopResettingTheTimer() {
        //arrange
        final var TO_SEC = 1000;
        final var framesToBottom = GameParameters.ROWS - GameParameters.SPAWN_POINT.y();
        var moves = 15;
        //act
        moveToBottom();
        for (int i = 0; i < 7; i++) {
            moveLeftAndBack();
        }
        gameManager.handleInput(InputAction.MOVE_LEFT);//15 move
        int tickWhoDontReset = 20;
        runTickNTimes(tickWhoDontReset);
        gameManager.handleInput(InputAction.MOVE_RIGHT);
        final int lastBatchAfter16Move = 12;
        runTickNTimes(lastBatchAfter16Move);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, atLeastOnce()).update(scoreEventCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var eventCaptured = scoreEventCaptor.getAllValues();
        //assert
        int lastFrameBefore16thMove = framesToBottom - 1 + moves + tickWhoDontReset;
        double expectedTicks = (double) (tickWhoDontReset * TICK_MS) / TO_SEC;
        Double actualTickBefore16Th = gameSnapshotList.get(lastFrameBefore16thMove).lockTime();
        assertThat(eventCaptured).filteredOn(LockPieceEvent.class::isInstance).hasSize(1);
        assertThat(eventCaptured.getLast()).isInstanceOf(LockPieceEvent.class);
        assertThat(actualTickBefore16Th).isEqualTo(expectedTicks);
        assertThat(gameSnapshotList.get(lastFrameBefore16thMove + 1).lockTime()).isEqualTo(actualTickBefore16Th);//16th moves
    }

    @Test
    void shouldTurnLockDelayOffWhenPieceIsKickedIntoAir(){
        //arrange
        //act
        moveToBottom();
        gameManager.handleInput(InputAction.ROTATE_CW);
        gameManager.handleInput(InputAction.ROTATE_CW);
        gameManager.handleInput(InputAction.ROTATE_CW);
        gameManager.handleInput(InputAction.ROTATE_CW);
        runTickNTimes(20);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        //assert
        GameSnapshot lastSnap = gameSnapshotList.getLast();
        assertThat(lastSnap.lockTime()).isNull();
        assertThat(lastSnap.currentTetromino().orElseThrow().getPositionY()).isEqualTo(20);
    }

    private void moveLeftAndBack() {
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_RIGHT);
    }

    //internal helper methods for tests

    private void runTickNTimes(int number) {
        for (var i = 0; i < number; i++) {
            gameManager.tickObserver().update(TICK);
        }
    }

    private void moveToBottom() {
        for (int i = GameParameters.SPAWN_POINT.y(); i < GameParameters.ROWS; i++) {
            gameManager.handleInput(InputAction.MOVE_DOWN);
        }
    }

}
