package org.sehes.tetris.model;

public enum GhostType {
    FULL,
    DASH,
    NONE;

    private static final GhostType[] VALUES = values();

    public GhostType next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}
