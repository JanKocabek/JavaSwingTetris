package org.sehes.tetris.model.level;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.common.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelManagerTests {
    LevelManager levelManager;

    List<Integer> levels;
    @BeforeEach
    void setUp() {
        levelManager = new LevelManager();
        levels = createLevels();
    }

    @Test
    @DisplayName("level Not Increased Before Lines reach Threshold")
    void testLevelNotIncreasedBeforeLinesReachThreshold() {
        //arrange
        final int expectedLevel = levelManager.getLevel();
        final var expectedSpeed = levelManager.getGravityNanos();
        //act
        levelManager.onCleanedRows(9);
        final int actualLevel = levelManager.getLevel();
        final var actualSpeed = levelManager.getGravityNanos();
        //assert
        assertThat(expectedLevel).isEqualTo(actualLevel);
        assertThat(expectedSpeed).isEqualTo(actualSpeed);
    }

    @Test
    void shouldDecreaseGravityIntervalAsLevelIncreases() {
        //arrange
        final int originalLevel = levelManager.getLevel();
        final var originalSpeed = levelManager.getGravityNanos();
        //act
        levelManager.onCleanedRows(10);
        final int actualLevel = levelManager.getLevel();
        final var actualSped = levelManager.getGravityNanos();
        //assert
        assertThat(actualLevel).isEqualTo(originalLevel + 1);
        assertThat(actualSped).isLessThan(originalSpeed);
    }

    @Test
    @DisplayName("should strictly decrease gravity nanos as level increases across all levels")
    void shouldStrictlyDecreaseGravityNanos() {
        long previousNanos = levelManager.getGravityNanos();

        for (int level = 1; level < 20; level++) {
            levelManager.onCleanedRows(10);
            long currentNanos = levelManager.getGravityNanos();
            assertThat(currentNanos)
                    .as("Level %d speed should be faster (smaller interval) than level %d", level + 1, level)
                    .isLessThan(previousNanos);
            previousNanos = currentNanos;
        }
    }

    @Test
    @DisplayName("should calculate exact gravity interval for known levels")
    void shouldCalculateExactGravityIntervalForKnownLevels() {
        // Level 1 baseline check
        assertThat(levelManager.getGravityNanos()/ Utils.NANOS_PER_SECOND).isEqualTo(1);

        // Level 2 exact check after 10 lines
        levelManager.onCleanedRows(10);
        assertThat(levelManager.getGravityNanos()/Utils.NANOS_PER_SECOND).isBetween(0.792, 0.793);
    }

    @Test
    void testLMDontCrossLevel20() {
        //arrange
        final var maxLM=new LevelManager(20);
        //act
        maxLM.onCleanedRows(10);
        //assert
        assertThat(maxLM.getLevel()).isEqualTo(20);
    }

    @Test
    void testLMThrowsExceptionForInvalidLevel() {
        //assert
        assertThrows(IllegalArgumentException.class, () -> new LevelManager(0));
        assertThrows(IllegalArgumentException.class, () -> new LevelManager(21));
    }

    @Test
    void testLevelMangerPublishing() {
        levelManager.onCleanedRows(10);
        assertThat(levels).containsExactly(2);
    }

    private @NonNull List<Integer> createLevels() {
        levels = new ArrayList<>();
        levelManager.levelObservable().addObserver(levels::add);
        return levels;
    }

    @Test
    void testResetResetLVL() {
        levelManager.onCleanedRows(10);
        levelManager.set(1);
        assertThat(levels.getLast()).isEqualTo(1);
    }
}
