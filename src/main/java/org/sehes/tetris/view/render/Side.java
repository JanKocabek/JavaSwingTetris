package org.sehes.tetris.view.render;

public enum Side {
    TOP(0),
    RIGHT(1),
    BOTTOM(2),
    LEFT(3),
    CENTER(4);

    private final int number;

    Side(int side) {
        this.number = side;
    }

    private static final Side[] cachedValues = new Side[values().length];

    static {
        for (Side side : values()) {
            cachedValues[side.number] = side;
        }
    }
    public static final int SIDE_COUNT = cachedValues.length;

    static Side fromInt(int side) {
        return cachedValues[side];
    }

    public static Side[] sides() {
        return cachedValues;
    }

    public int getSide() {
        return number;
    }
}
