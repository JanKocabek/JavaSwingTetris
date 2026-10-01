package org.sehes.tetris.model;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomPieceGenerator implements PieceGenerator {
    private static final TetrominoType[] TETROMINO_SHAPES = TetrominoType.getTetrominoShapes();
    private final SecureRandom seedGenerator = new SecureRandom();
    private RandomGenerator pieceGenerator;
    private TetrominoType next;

    @Override
    public TetrominoType peekNext() {
        if (next == null) {
            next = generate();
        }
        return next;
    }

    @Override
    public TetrominoType getNextPiece() {
        final var consume = this.peekNext();
        this.next = generate();
        return consume;
    }

    @Override
    public void startNewSequence() {
        next = null;
        final long seed = seedGenerator.nextLong();
        pieceGenerator = RandomGeneratorFactory.getDefault().create(seed);
    }

    //
    private TetrominoType generate() {
        if (pieceGenerator == null) {
            startNewSequence();
        }
        return TETROMINO_SHAPES[pieceGenerator.nextInt(TETROMINO_SHAPES.length)];
    }
}
