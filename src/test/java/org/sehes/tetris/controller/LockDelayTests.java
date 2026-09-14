package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.model.Coordinate;
import org.sehes.tetris.model.TetrominoFactory;
import org.sehes.tetris.model.TetrominoType;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


public class LockDelayTests {
    LockDelay lockDelay;

    @BeforeEach
    public void setUp() {
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
    void testLockTrueWIthMore500() {
        //Act
        final var time = TimeUnit.MILLISECONDS.toNanos(600);
        final var result = lockDelay.onTick(time);
        //Assert
        assertThat(result).isTrue();
    }

    @Test
    void testIsLockModeIsNotChangeOnTickItself() {
        //Act
        final var expected = false;
        final var time = TimeUnit.MILLISECONDS.toNanos(500);
        lockDelay.onTick(time);
        final var result = lockDelay.isOn();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testGravityFallNotAddIntoLockMoveCounter() {
        lockDelay.onGrounded(20);
        lockDelay.onGrounded(20);
        assertThat(lockDelay).extracting("lockMoves").isEqualTo(0);
    }

    @Test
    void testLockTimerResetAfter15moves() {
        //arrange
        lockDelay.setFor(TetrominoFactory.spawnTetromino(TetrominoType.T, new Coordinate(4, 21)));
        lockDelay.setLockModeOn();
        //Act
        for (int i = 0; i < 15; i++) {
            lockDelay.onTick(500);
            lockDelay.checkMove(21, true);
        }
        //asser
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(0L);
        lockDelay.onTick(500);
        lockDelay.checkMove(21, true);
        assertThat(lockDelay).extracting("delayLockAccumulator").isEqualTo(500L);
    }
}
