package org.sehes.tetris.model.generator;

import org.sehes.tetris.model.TetrominoType;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomPieceGenerator extends AbstractGenerator {
    private static final TetrominoType[] TETROMINO_SHAPES = TetrominoType.getTetrominoShapes();
    private final SecureRandom seedGenerator = new SecureRandom();
    private  RandomGenerator pieceGenerator;


    @Override
    protected void setNewQueue() {
        final long seed = seedGenerator.nextLong();
        pieceGenerator = RandomGeneratorFactory.getDefault().create(seed);
    }

    @Override
    protected TetrominoType generateNextPiece() {
        if (pieceGenerator == null) {
            setNewQueue();
        }
        return TETROMINO_SHAPES[pieceGenerator.nextInt(TETROMINO_SHAPES.length)];
    }

}
