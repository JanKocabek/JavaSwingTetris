package org.sehes.tetris.controller;

import org.sehes.tetris.model.Tetromino;

import java.util.concurrent.TimeUnit;

public class LockDelay {
    private static final Long LOCK_DELAY_MS = TimeUnit.MILLISECONDS.toNanos(500);
    private static final int MAX_LOCK_MOVES = 15;
    private boolean isLockMode = false;
    private int lockMoves = 0;
    private long delayLockAccumulator = 0;
    private int maxY;

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
        return delayLockAccumulator >= LOCK_DELAY_MS;
    }

    boolean isOn() {
        return isLockMode;
    }

    void setLockModeOn() {
        isLockMode = true;
    }

    void checkMove(int y, boolean isOnGround) {
        //rotated / kicked - end lock mode
        if (!isOnGround) {
            resetLockMode();
            return;
        }
        // Reaching new depth (probably just the kicks) handles state reset automatically
        if (isNewDepth(y)) {
            onGrounded(y);
            return;
        }
        // Main Path: Grounded move at an existing depth
        isLockMode = true;
        lockMoves++;
        tryResetLockTimer();
    }


    /**
     * applying states when reach ground and reset
     * </br>
     * reset lockDelay tetromino states if new depth is reached
     *
     * @param y tetromino current depth
     *
     */
    void onGrounded(int y) {
        isLockMode = true;
        resetStatesIfNewDepth(y);
    }

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
            maxY = y;
            lockMoves = 0;
            delayLockAccumulator = 0;
        }
    }

    private void resetLockMode() {
        isLockMode = false;
        tryResetLockTimer();
    }

    private void tryResetLockTimer() {
        if (lockMoves <= MAX_LOCK_MOVES) {
            delayLockAccumulator = 0;
        }
    }
}
