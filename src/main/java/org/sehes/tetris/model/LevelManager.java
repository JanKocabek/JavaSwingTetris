package org.sehes.tetris.model;

import static org.sehes.tetris.Utils.NANOS_PER_SECOND;

public class LevelManager {
    private static final int LINES_PER_LEVEL = 10;
    private static final double BASE_SPEED_FACTOR = 0.8;
    private static final float SPEED_ACCELERATION_STEPS = 0.007f;
    private static final int MAX_LEVEL = 20;
    private int level;
    private int cleanedRows;
    private long gravityIntervalNanos;

    public LevelManager(int level) {
        if (level < 1 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("Level must be between 1 and " + MAX_LEVEL + " (inclusive)");
        }
        this.level = level;
        this.cleanedRows = 0;
        this.gravityIntervalNanos = calculateGravityIntervalNanos();
    }

    public LevelManager() {
        this(1);
    }

    public int getLevel() {
        return level;
    }

    public void onCleanedRows(int cleanedRows) {
        if (this.level == MAX_LEVEL) return;
        this.cleanedRows += cleanedRows;
        if (this.cleanedRows >= LINES_PER_LEVEL) {
            this.level++;
            this.cleanedRows = 0;
            this.gravityIntervalNanos = calculateGravityIntervalNanos();
        }
    }

    public long getGravityNanos() {
        return gravityIntervalNanos;
    }

    private long calculateGravityIntervalNanos() {
        final int levelExponent = level - 1;
        final double speedSec = Math.pow(BASE_SPEED_FACTOR - levelExponent * SPEED_ACCELERATION_STEPS, levelExponent);
        return (long) (speedSec * NANOS_PER_SECOND);
    }
}
