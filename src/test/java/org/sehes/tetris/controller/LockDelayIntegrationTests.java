package org.sehes.tetris.controller;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.common.event.Observer;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.GameBoard;
import org.sehes.tetris.model.GameSnapshot;
import org.sehes.tetris.model.TetrominoFactory;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.generator.PieceGenerator;
import org.sehes.tetris.model.score.LockPieceEvent;
import org.sehes.tetris.model.score.ScoreEvent;
import org.sehes.tetris.view.Rendering;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockDelayIntegrationTests {
    private static final int TICK_MS = 16;
    private static final double TICK_S = (double) TICK_MS / 1000;
    private static final long TICK_NANO = TimeUnit.MILLISECONDS.toNanos(TICK_MS);
    private static final double LOCK_DELAY_TIME_S = 0.500;
    private static final int MAX_GROUNDING_TICKS = 3000;
    private GameManager gameManager;
    @Mock private PieceGenerator pieceGenerator;
    @Mock private Observer<ScoreEvent> scoreObserver;
    @Mock private Rendering rendering;
    @Captor private ArgumentCaptor<GameSnapshot> gameSnapshotCaptor;
    @Captor private ArgumentCaptor<ScoreEvent> scoreEventCaptor;
    private final AtomicReference<GameSnapshot> latestSnapshot = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        when(pieceGenerator.getNextPiece()).thenReturn(TetrominoType.T);
        when(pieceGenerator.peekNext()).thenReturn(TetrominoType.T);
        gameManager = GameManagerBuilder.create()
                .addScoreObserver(scoreObserver)
                .setGenerator(pieceGenerator)
                .setRender(rendering)
                .build();
        gameManager.handleInput(InputAction.CONFIRM);


        doAnswer(invocation -> {
            latestSnapshot.set(invocation.getArgument(0));
            return null;
        }).when(rendering).render(any());
    }

    @Test
    @DisplayName("should start lock delay when ground is reached-by user moving down")
    void shouldStartLockDelayWhenGroundIsReachedMoving() {
        //act
        moveToBottom();
        final var numberOfRun = GameParameters.ROWS - GameBoard.SPAWN_POINT.y();
        verify(rendering, times(numberOfRun)).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var firstMino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        //assert
        assertThat(gameSnapshotList.getFirst().lockTime()).isNull();
        assertThat(gameSnapshotList.getLast().currentTetromino()).contains(firstMino);
        assertThat(gameSnapshotList.getLast().currentTetromino().orElseThrow().getPositionY()).isEqualTo(GameParameters.ROWS - 1);
        assertThat(gameSnapshotList.getLast().lockTime()).isZero();
    }

    @Test
    @DisplayName("should set lock delay when ground is reached-by gravity")
    void shouldSetLockDelayWhenGroundIsReachedGravity() {
        //act
        tickUntilGrounded();
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        //assert
        final var mino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var droppedMinoFrameOpt = gameSnapshotList.stream().filter(snapshot -> snapshot.lockTime() != null).findFirst();
        assertThat(droppedMinoFrameOpt).isNotEmpty();
        final var dropped = droppedMinoFrameOpt.get();
        assertThat(gameSnapshotList.getFirst().lockTime()).isNull();
        assertThat(dropped.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"))).isEqualTo(mino);
    }

    @Test
    @DisplayName("should lock after 500ms - gravity fall")
    void shouldLockAfter500msGravity() {
        //act
        tickUntilGrounded();
        runTickNTimes(500);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        verify(scoreObserver, times(1)).update(scoreEventCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var scoreEvent = scoreEventCaptor.getValue();
        final var exactMino = gameSnapshotList.getFirst().currentTetromino().orElseGet(() -> fail("Tetromino should not be empty"));
        final var lockedFrames = gameSnapshotList.stream().filter(snapshot -> snapshot.lockTime() != null && snapshot.currentTetromino().orElseGet(() -> fail("Tetromino should not be empty")) == exactMino).toList();
        //assert
        assertThat(lockedFrames).hasSizeLessThan(600);
        assertThat(lockedFrames.getLast().lockTime()).isCloseTo(LOCK_DELAY_TIME_S, Offset.offset(TICK_S));
        assertThat(scoreEvent).isInstanceOf(LockPieceEvent.class);
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
        //+-one tick of lock delay because game manager accumulators always consume more than exact 1 tick
        assertThat(lockedFrames).hasSizeLessThan(600);
        assertThat(lockedFrames.getLast().lockTime()).isCloseTo(LOCK_DELAY_TIME_S, Offset.offset(TICK_S));
        assertThat(eventCaptured).filteredOn(LockPieceEvent.class::isInstance).hasSize(1);
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
        //assert
        assertThat(gameSnapshotList).filteredOn(GameSnapshot::lockTime, 0.0).hasSize(3);
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
        assertThat(lastCaptBeforeNew.lockTime()).isNotNull().isBetween(0.1, 0.3);
        assertThat(gameSnapshotList.getLast().lockTime()).isNull();
        assertThat(newMino.orElseThrow()).isNotEqualTo(firstMino).isNotNull();
    }

    @Test
    @DisplayName("Moving down to a new depth (maxY) should reset lock delay timer")
    void resetLockDelayWhenMoveFromOneDepthToLower() {
        //arrange
        final var firstPiece = pieceGenerator.peekNext();
        final var pieceData = TetrominoFactory.spawnTetromino(firstPiece, GameBoard.SPAWN_POINT);
        final var spawnY = GameBoard.SPAWN_POINT.y();
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
        assertThat(allSnapshots.get(snapWhenDrop).lockTime()).isEqualTo(tickStayDropped * TICK_S);
        assertThat(allSnapshots.get(snapWhenDrop + 1).lockTime()).isZero();
        assertThat(allSnapshots.get(snapWhenDrop + 2).lockTime()).isNull();
        assertThat(lastLockDealyTime).isZero();
    }

    @Test
    void should16MoveStopResettingTheTimer() {
        //arrange
        final var TO_SEC = 1000;
        final var framesToBottom = GameParameters.ROWS - GameBoard.SPAWN_POINT.y();
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
    void shouldTurnLockDelayOffWhenPieceIsKickedIntoAir() {
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

    @Test
    @DisplayName("Hold/swap piece should reset lock delay state for the newly spawned piece")
    void holdOrSwapShouldResetLockDelay() {
        //act
        moveToBottom();
        runTickNTimes(20);
        gameManager.handleInput(InputAction.HOLD);
        verify(rendering, atLeastOnce()).render(gameSnapshotCaptor.capture());
        final var gameSnapshotList = gameSnapshotCaptor.getAllValues();
        final var lastSnapshot = gameSnapshotList.getLast();
        //assert
        assertThat(lastSnapshot.currentTetromino()).isPresent();
        assertThat(lastSnapshot.currentTetromino().get().getPositionY()).isEqualTo(GameBoard.SPAWN_POINT.y());
        assertThat(lastSnapshot.lockTime()).isNull();
    }

    //internal helper methods for tests

    private void moveLeftAndBack() {
        gameManager.handleInput(InputAction.MOVE_LEFT);
        gameManager.handleInput(InputAction.MOVE_RIGHT);
    }

    private void tickUntilGrounded() {
        for (int ticks = 0; ticks < MAX_GROUNDING_TICKS; ticks++) {
            GameSnapshot snapshot = latestSnapshot.get();
            if (snapshot != null && snapshot.lockTime() != null) {
                return;
            }
            gameManager.tickObserver().update(TICK_NANO);
        }
        fail("Piece did not reach ground within " + MAX_GROUNDING_TICKS + " ticks");
    }

    private void runTickNTimes(int number) {
        for (var i = 0; i < number; i++) {
            gameManager.tickObserver().update(TICK_NANO);
        }
    }

    private void moveToBottom() {
        for (int i = GameBoard.SPAWN_POINT.y(); i < GameParameters.ROWS; i++) {
            gameManager.handleInput(InputAction.MOVE_DOWN);
        }
    }

}
