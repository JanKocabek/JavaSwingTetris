package org.sehes.tetris.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LevelGravityTests {
    LevelManager levelManager;

    @BeforeEach
    void setUp() {
        levelManager = new LevelManager();
    }

    @Test
    @DisplayName("level Not Increased Before Lines reach Threshold")
    void testLevelNotIncreasedBeforeLinesReachThreshold() {
        final int expectedLevel = levelManager.getLevel();
        levelManager.onCleanedRows(9);
        final int actualLevel = levelManager.getLevel();
        assertThat(expectedLevel).isEqualTo(actualLevel);
    }

    @Test
    void shouldGravityIncreaseWithLevel() {
        final int originalLevel = levelManager.getLevel();
        levelManager.onCleanedRows(10);
        final int actualLevel = levelManager.getLevel();
        assertThat(actualLevel).isEqualTo(originalLevel + 1);
    }

}
