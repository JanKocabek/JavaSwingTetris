package org.sehes.tetris.model;

import static org.sehes.tetris.Utils.NANOS_PER_SECOND;

public class LevelManager {
    private static final int LINES_PER_LEVEL = 10;
    private static final double BASE_SPEED_FACTOR = 0.8;
    private static final float SPEED_ACCELERATION_STEPS = 0.007f;
    private int level;
    private int previousLevel;
    private int cleanedRows;

    public LevelManager() {
        this.level = 1;
        this.previousLevel = 1;
        this.cleanedRows = 0;
    }

    public int getLevel() {
        return level;
    }

    public void onCleanedRows(int cleanedRows) {
        this.cleanedRows += cleanedRows;
        if (this.cleanedRows >= LINES_PER_LEVEL) {
            this.previousLevel = this.level;
            this.level++;
            this.cleanedRows = 0;
        }
    }

    public long getMovementSpeedNano() {
        final int speedExponent = level - 1;
        final double speedSec = BASE_SPEED_FACTOR - Math.pow((level - 1) * SPEED_ACCELERATION_STEPS, speedExponent);
        return (long) (speedSec * NANOS_PER_SECOND);
    }
}
