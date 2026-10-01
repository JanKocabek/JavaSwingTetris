package org.sehes.tetris.model;

public interface PieceGenerator {
    TetrominoType peekNext();
    TetrominoType getNextPiece();
    void startNewSequence();
}
