package org.sehes.tetris.controller;

import org.sehes.tetris.model.Tetromino;

import java.util.concurrent.TimeUnit;

public class LockDelay {
    private static final Long LOCK_DELAY_MS = TimeUnit.MILLISECONDS.toNanos(500);
    private static final int MAX_LOCK_MOVES = 15;
    private boolean isLockMode = false;
    private int lockMoves = 0;
    private long delayLockAccumulator = 0;
    private int currentY;
    private int maxY;

    /**
     * add ticked times into delay and check if is delay runs out
     * </br>
     * this method is fired only from {@code GameManager.onTickUpdate()} or subMethods
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

    private void resetLockMode() {
        isLockMode = false;
        tryResetLockTimer();
    }

    private void tryResetLockTimer() {
        if(lockMoves <= MAX_LOCK_MOVES) {
            delayLockAccumulator = 0;
        }
    }

    void setLockModeOn() {
        isLockMode = true;
    }

    void checkMove(int y, boolean isOnGround) {
        if (isOnGround) {
            final var reachNewDepth = onGrounded(y);
            if (!reachNewDepth) {
                lockMoves++;
                tryResetLockTimer();
            }
        } else {
            currentY = y;
            resetLockMode();
        }
    }

    boolean onGrounded(int y) {
        currentY = y;
        isLockMode = true;
        return resetIfMoveDown();
    }

    void checkDrop(int y) {
        currentY = y;
        isLockMode = false;
        resetIfMoveDown();
    }

    /**
     * initialize/reset lockDelay object for new Mino
     * @param tetromino current Tetromino
     */
    void setFor(Tetromino tetromino) {
        isLockMode = false;
        lockMoves = 0;
        delayLockAccumulator = 0;
        currentY = tetromino.getPositionY();
        maxY = tetromino.getPositionY();
    }

    private boolean resetIfMoveDown() {
        if (currentY > maxY) {
            maxY = currentY;
            lockMoves = 0;
            delayLockAccumulator = 0;
            return true;
        }
        return false;
    }
}
