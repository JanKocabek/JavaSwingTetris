package org.sehes.tetris.model.generator;

import org.sehes.tetris.model.TetrominoType;

public abstract class AbstractGenerator implements PieceGenerator {
    private TetrominoType next;

    @Override
    public TetrominoType peekNext() {
        if (next == null) {
            next = generateNextPiece();
        }
        return next;
    }

    @Override
    public TetrominoType getNextPiece() {
        final var consume = this.peekNext();
        this.next = generateNextPiece();
        return consume;
    }

    @Override
    public final void startNewQueue() {
        next = null;
        setNewQueue();
    }

    protected abstract TetrominoType generateNextPiece();

    protected abstract void setNewQueue();
}
