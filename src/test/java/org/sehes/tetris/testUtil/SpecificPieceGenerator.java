package org.sehes.tetris.testUtil;

import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.generator.PieceGenerator;

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
        public void startNewQueue() {
            // do nothing
        }
    }

