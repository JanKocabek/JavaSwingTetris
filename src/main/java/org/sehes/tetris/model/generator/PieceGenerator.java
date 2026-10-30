package org.sehes.tetris.model.generator;

import org.sehes.tetris.model.TetrominoType;

public interface PieceGenerator {
    TetrominoType peekNext();
    TetrominoType getNextPiece();
    void startNewQueue();
}
