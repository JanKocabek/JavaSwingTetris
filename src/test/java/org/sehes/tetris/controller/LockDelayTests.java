package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.model.Coordinate;
import org.sehes.tetris.model.TetrominoFactory;
import org.sehes.tetris.model.TetrominoType;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


class LockDelayTests {
    LockDelay lockDelay;

    @BeforeEach
    void setUp() {
        //Arrange
        lockDelay = new LockDelay();
        lockDelay.setFor(TetrominoFactory.spawnTetromino(TetrominoType.T, GameParameters.SPAWN_POINT));
    }

    @Test
    void testLockDoesntHappenedBefore500ms() {
        //arrange
        final var time = TimeUnit.MILLISECONDS.toNanos(499);
        //Act
        final var result = lockDelay.onTick(time);
        //Assert
        assertThat(result).isFalse();
    }

    @Test
    void testLockTrueWIth500ms() {
        //Act
        final var time = TimeUnit.MILLISECONDS.toNanos(500);
        final var result = lockDelay.onTick(time);
        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void testLockTrueWithMoreThen500ms() {
        //Act
        final var time = TimeUnit.MILLISECONDS.toNanos(600);
        final var result = lockDelay.onTick(time);
        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void testIsLockModeVarNotChangeOnTickItSelf() {
        //arrange
        final var expected = false;
        final var time = TimeUnit.MILLISECONDS.toNanos(500);
        //Act
        lockDelay.onTick(time);
        final var result = lockDelay.isOn();
        //Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testGravityFallResetEverything() {
        var startY = 19;
        lockDelay.checkDrop(startY, false);
        lockDelay.checkDrop(++startY, true);
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(0);
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(0L);
        assertThat(lockDelay).extracting("maxY").isEqualTo(startY);
    }

    @Test
    void testLockTimerResetAfterMove() {
        lockDelay.setFor(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 21)));
        lockDelay.setLockModeOn();
        //Act
        lockDelay.onTick(500);
        lockDelay.checkMove(21, true);
        //asser
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(0L);
    }

    @Test
    void testLockTimerStopResetAfter15moves() {
        //arrange
        lockDelay.setFor(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 21)));
        lockDelay.setLockModeOn();
        //Act
        final var elapsedTime = 500L;
        for (int i = 0; i < 16; i++) {
            lockDelay.onTick(elapsedTime);
            lockDelay.checkMove(21, true);
        }
        //assert
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(elapsedTime);
    }

    @Test
    void testKickIntoAirTurnsLockOff() {
        //arrange
        lockDelay.setFor(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 21)));
        lockDelay.setLockModeOn();
        //Act
        lockDelay.checkMove(20, false);
        //assert
        assertThat(lockDelay.isOn()).isFalse();
    }

    @Test
    void testKickIntoAirCancelLockModeCancelTimerLeftMovementOnCurrentValue() {
        //arrange
        final var time = TimeUnit.MILLISECONDS.toNanos(100);
        //act
        lockDelay.checkDrop(20, true);
        lockDelay.checkMove(20, true);
        lockDelay.onTick(time);
        lockDelay.checkMove(18, false);
        //assert
        assertThat(lockDelay.isOn()).isFalse();
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(0L);
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(2);
    }

    @Test
    void testFallIntoAlreadyAchievedDepthDoesntIncrementLockMoves() {
        //arrange
        lockDelay.checkDrop(20, true);
        lockDelay.checkMove(20, true);
        //Act
        lockDelay.checkMove(19, false);
        lockDelay.checkDrop(20, true);
        //assert
        assertThat(lockDelay.isOn()).isTrue();
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(2);
    }

    @Test
    void testKickLowerThenCurrentMaxDepthResetEverything() {
        //arrange
        final var time = TimeUnit.MILLISECONDS.toNanos(100);
        final var lastIsOn = false;
        //act
        lockDelay.checkDrop(20, true);
        lockDelay.checkMove(20, true);
        lockDelay.onTick(time);
        lockDelay.checkMove(21, lastIsOn);
        //assert
        assertThat(lockDelay.isOn()).isEqualTo(lastIsOn);
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(0L);
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(0);
    }
}
