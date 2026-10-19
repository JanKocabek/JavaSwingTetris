package org.sehes.tetris.model;

import static org.sehes.tetris.Utils.NANOS_PER_SECOND;

public class LevelManager {
    private static final int LINES_PER_LEVEL = 10;
    private static final double BASE_SPEED_FACTOR = 0.8;
    private static final float SPEED_ACCELERATION_STEPS = 0.007f;
    private static final int MAX_LEVEL = 20;
    private int level;
    private int cleanedRows;
    private long movementSpeedNano;

    public LevelManager() {
        this.level = 1;
        this.cleanedRows = 0;
        this.movementSpeedNano = calculateMovementSpeedNano();
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
            this.movementSpeedNano = calculateMovementSpeedNano();
        }
    }

    public long getMovementSpeedNano() {
        return movementSpeedNano;
    }

    private long calculateMovementSpeedNano() {
        final int levelExponent = level - 1;
        final double speedSec =  Math.pow(BASE_SPEED_FACTOR - levelExponent * SPEED_ACCELERATION_STEPS, levelExponent);
        return (long) (speedSec * NANOS_PER_SECOND);
    }
}
