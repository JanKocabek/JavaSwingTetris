package org.sehes.tetris.controller;

import org.sehes.tetris.model.Tetromino;

import java.util.concurrent.TimeUnit;

public class LockDelay {
    private static final long LOCK_DELAY_NANO = TimeUnit.MILLISECONDS.toNanos(500);
    private static final int MAX_LOCK_MOVES = 15;
    private boolean isLockMode = false;
    private int lockMoves = 0;
    private long delayLockAccumulator = 0;
    private int maxY;

    long lockDelayElapsedInNANO() {
        return delayLockAccumulator;
    }
    /**
     * add ticked times into delay and check if is delay runs out
     * </br>
     * this method is fired only from {@code GameManager.onTickUpdate()} or subMethods
     *
     * @param elapsedTime times run from previous tick to this one
     * @return {@code true} if run out the delay time usually 500ms
     * otherwise {@code false}
     */
    boolean onTick(long elapsedTime) {
        delayLockAccumulator += elapsedTime;
        return delayLockAccumulator >= LOCK_DELAY_NANO;
    }

    boolean isOn() {
        return isLockMode;
    }

    void setLockModeOn() {
        isLockMode = true;
    }

    /**
     * used for check of lockDelay state after left/right or rotation move
     *
     * @param y          current depth of tetromino
     * @param isOnGround {@code true} if tetromino is touching any source of ground/block
     */
    void checkMove(int y, boolean isOnGround) {
        // Reaching new depth (probably just the kicks) handle state reset automatically
        if (isNewDepth(y)) {
            isLockMode = isOnGround;
            resetStatesForNewDepth(y);
            return;
        }
        lockMoves++;
        isLockMode = isOnGround;
        tryResetLockTimer();
    }

    /**
     * used for check of lockDelay state after soft or gravity drop
     *
     * @param y          current depth of tetromino
     * @param isOnGround {@code true} if tetromino is touching any source of ground/block
     */
    void checkDrop(int y, boolean isOnGround) {
        isLockMode = isOnGround;
        resetStatesIfNewDepth(y);
    }

    /**
     * initialize/reset lockDelay object for new Mino
     *
     * @param tetromino current Tetromino
     */
    void setFor(Tetromino tetromino) {
        isLockMode = false;
        lockMoves = 0;
        delayLockAccumulator = 0;
        maxY = tetromino.getPositionY();
    }

    private boolean isNewDepth(int y) {
        return y > maxY;
    }

    /**
     * check if tetromino current depth(y) is farther than previous maximum(y)
     * if so reset lockDelay tetromino state
     * which include  lockMoves, delayLockAccumulator
     *
     * @param y tetromino current depth
     */
    private void resetStatesIfNewDepth(int y) {
        if (isNewDepth(y)) {
            resetStatesForNewDepth(y);
        }
    }

    private void resetStatesForNewDepth(int y) {
        maxY = y;
        lockMoves = 0;
        delayLockAccumulator = 0;
    }

    private void tryResetLockTimer() {
        if (lockMoves <= MAX_LOCK_MOVES) {
            delayLockAccumulator = 0;
        }
    }
}
