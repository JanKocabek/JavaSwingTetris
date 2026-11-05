package org.sehes.tetris.model.generator;

import org.sehes.tetris.model.TetrominoType;

import java.security.SecureRandom;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomPieceGenerator extends AbstractGenerator {
    private static final TetrominoType[] TETROMINO_SHAPES = TetrominoType.getTetrominoShapes();
    private RandomGenerator pieceGenerator;
    private final Supplier<Long> seedSupplier;

    public RandomPieceGenerator() {
        super();
        SecureRandom seedGenerator = new SecureRandom();
        seedSupplier = seedGenerator::nextLong;
    }

    RandomPieceGenerator(Long seed) {
        super();
        seedSupplier = () -> seed;
    }

    @Override
    protected void setNewQueue() {
        pieceGenerator = RandomGeneratorFactory.getDefault().create(seedSupplier.get());
    }

    @Override
    protected TetrominoType generateNextPiece() {
        if (pieceGenerator == null) {
            setNewQueue();
        }
        return TETROMINO_SHAPES[pieceGenerator.nextInt(TETROMINO_SHAPES.length)];
    }

}
