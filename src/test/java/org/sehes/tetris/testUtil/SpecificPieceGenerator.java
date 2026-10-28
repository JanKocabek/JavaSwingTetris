package org.sehes.tetris.testUtil;

import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.TetrominoType;

    public record SpecificPieceGenerator(TetrominoType minoType) implements PieceGenerator {

        @Override
        public TetrominoType peekNext() {
            return minoType;
        }

        @Override
        public TetrominoType getNextPiece() {
            return minoType;
        }

        @Override
        public void startNewMinoQueue() {
            // do nothing
        }
    }

