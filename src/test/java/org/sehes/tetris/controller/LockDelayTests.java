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
    void testLockDoesntHappenedBefore500() {
        //arrange
        final var time = TimeUnit.MILLISECONDS.toNanos(499);
        //Act
        final var result = lockDelay.onTick(time);
        //Assert
        assertThat(result).isFalse();
    }

    @Test
    void testLockTrueWIth500() {
        //Act
        final var time = TimeUnit.MILLISECONDS.toNanos(500);
        final var result = lockDelay.onTick(time);
        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void testLockTrueWithMoreThen500() {
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
    void testGravityFallNotAddIntoLockMoveCounter() {
        final var startY = 20;
        lockDelay.onGrounded(startY);
        lockDelay.onGrounded(startY + 1);
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(0);
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
    void testFallIntoAlreadyAchieveDont() {
        //arrange
        lockDelay.setFor(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 21)));
        lockDelay.setLockModeOn();
        lockDelay.checkMove(20, false);
        //Act
        lockDelay.checkDrop(21, true);
        //assert
        assertThat(lockDelay.isOn()).isTrue();
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(0);
    }
}
