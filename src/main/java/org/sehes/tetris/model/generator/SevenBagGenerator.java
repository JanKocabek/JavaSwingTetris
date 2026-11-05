package org.sehes.tetris.model.generator;

import org.sehes.tetris.model.TetrominoType;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class SevenBagGenerator extends AbstractGenerator {
    private final List<TetrominoType> minos;
    private final Queue<TetrominoType> bag = new ArrayDeque<>(7);
    private final RandomGenerator rg;

    public SevenBagGenerator() {
        this(RandomGeneratorFactory.getDefault().create());
    }

    SevenBagGenerator(RandomGenerator rg) {
        this.rg = rg;
        minos = new ArrayList<>(7);
        minos.addAll(Arrays.asList(TetrominoType.getTetrominoShapes()));
    }

    SevenBagGenerator(int seed) {
        this(RandomGeneratorFactory.getDefault().create(seed));
    }

    @Override
    protected void setNewQueue() {
        bag.clear();
    }

    @Override
    protected TetrominoType generateNextPiece() {
        if (bag.isEmpty()) {
            refillBag();
        }
        return bag.remove();
    }

    private void refillBag() {
        Collections.shuffle(minos, rg);
        bag.addAll(minos);
    }
}
