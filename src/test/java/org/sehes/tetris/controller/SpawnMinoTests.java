package org.sehes.tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.config.GameParameters;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.GameSnapshot;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SpawnMinoTests {

    private GameManager gameManager;
    private final AtomicReference<GameSnapshot> latestSnapshot = new AtomicReference<>();

    @BeforeEach
    void setup() {
        gameManager = GameManagerBuilder.create()
                .setRender(latestSnapshot::set)
                .build();
        gameManager.handleInput(InputAction.CONFIRM);
    }

    @Test
    @DisplayName("Hold should reset gravity timer so newly spawned piece does not fall prematurely")
    void holdShouldResetGravityAccumulator() {
        // Arrange: Start the game (level 1 gravity interval = 1000ms)

        final int spawnY = GameParameters.SPAWN_POINT.y();

        accumulateAndPutHold(900);

        // Act 3: Tick another 200ms
        // If gravity timer was reset on spawn, only 200ms elapsed -> piece stays at spawn.
        // If gravity timer was NOT reset, 900ms + 200ms = 1100ms >= 1000ms -> piece drops to Y=2!
        gameManager.tickObserver().update(TimeUnit.MILLISECONDS.toNanos(200));

        // Assert: The newly spawned piece must still be at the spawn row (spawnY)
        final int currentY = latestSnapshot.get()
                .currentTetromino()
                .orElseThrow()
                .getPositionY();

        assertThat(currentY)
                .as("Newly spawned piece after HOLD should have a fresh gravity interval and not drop early")
                .isEqualTo(spawnY);
    }

    private void accumulateAndPutHold(int duration) {
        // Act 1: Tick 900ms — almost enough for gravity, piece remains at spawn
        gameManager.tickObserver().update(TimeUnit.MILLISECONDS.toNanos(duration));

        // Act 2: Swap/hold the piece
        gameManager.handleInput(InputAction.HOLD);
    }

    @Test
    @DisplayName("Hold reset gravity timer after spawned held piece")
    void holdResetGravityTimerAfterSpawnedHeldPiece() {
        // Arrange: Start the game (level 1 gravity interval = 1000ms)
        // Act 1: Spawn a piece hold it and hard drop next
        gameManager.handleInput(InputAction.HOLD);
        gameManager.handleInput(InputAction.HARD_DROP);
        // Act 2: Spawn a piece and hold it
        final int spawnY = GameParameters.SPAWN_POINT.y();
        accumulateAndPutHold(900);
        // Act 3: Tick another 200ms
        // If gravity timer was reset on spawn, only 200ms elapsed -> piece stays at spawn.
        // If gravity timer was NOT reset, 900ms + 200ms = 1100ms >= 1000ms -> piece drops to Y=2!
        gameManager.tickObserver().update(TimeUnit.MILLISECONDS.toNanos(200));

        // Assert: The newly spawned piece must still be at the spawn row (spawnY)
        final int currentY = latestSnapshot.get()
                .currentTetromino()
                .orElseThrow()
                .getPositionY();

        assertThat(currentY)
                .as("Newly spawned piece after HOLD should have a fresh gravity interval and not drop early")
                .isEqualTo(spawnY);
    }

}
