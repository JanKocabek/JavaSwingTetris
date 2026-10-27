package org.sehes.tetris.model;

import org.sehes.tetris.model.ShapeProvider.Transition;

/**
 * The Orientation enum represents the possible Orientations states of a
 * Tetromino piece in game<br>
 * It represents them as {@code NORTH}, {@code EAST}, {@code SOUTH},
 * and {@code WEST}.<br>
 * it exposes methods to rotate clockwise and counter-clockwise.
 */
public enum Orientation {
    NORTH(0), EAST(1), SOUTH(2), WEST(3);

    private static final Orientation[] ROTATION_ORDER = Orientation.values();
    private static final int SIZE = ROTATION_ORDER.length;
    private final int state;

    Orientation(int state) {
        this.state = state;
    }

     Transition getTransitionTo(final Orientation next) {
        return Transition.valueOf(this.name() + "_TO_" + next.name());
    }

    public Orientation rotateClockwise() {
        return next();
    }

    public Orientation rotateCounterClockwise() {
        return previous();
    }

    private Orientation next() {
        return ROTATION_ORDER[(state + 1) % SIZE];
    } // next rotation

    private Orientation previous() {
        return ROTATION_ORDER[(state + (SIZE - 1)) % SIZE];
    } // previous rotation
}
