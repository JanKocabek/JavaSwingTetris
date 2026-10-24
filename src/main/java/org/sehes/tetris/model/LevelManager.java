package org.sehes.tetris.model;

import org.sehes.tetris.controller.ObservableImpl;
import org.sehes.tetris.util.event.Observable;

import static org.sehes.tetris.util.Utils.NANOS_PER_SECOND;

public class LevelManager {
    private static final int LINES_PER_LEVEL = 10;
    private static final double BASE_SPEED_FACTOR = 0.8;
    private static final float SPEED_ACCELERATION_STEPS = 0.007f;
    private static final int MAX_LEVEL = 20;
    private int level;
    private int cleanedRows;
    private long gravityIntervalNanos;
    private final Observable.Publisher<Integer> levelPublisher=new ObservableImpl<>();
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
            this.levelPublisher.notify(this.level);
            this.cleanedRows = 0;
            this.gravityIntervalNanos = calculateGravityIntervalNanos();
        }
    }

    public long getGravityNanos() {
        return gravityIntervalNanos;
    }
    
    public Observable<Integer> levelObservable() {
        return levelPublisher;
    }

    private long calculateGravityIntervalNanos() {
        final int levelExponent = level - 1;
        final double speedSec = Math.pow(BASE_SPEED_FACTOR - levelExponent * SPEED_ACCELERATION_STEPS, levelExponent);
        return (long) (speedSec * NANOS_PER_SECOND);
    }
}
