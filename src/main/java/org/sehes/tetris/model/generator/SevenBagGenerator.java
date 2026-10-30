package org.sehes.tetris.model.generator;

import org.sehes.tetris.model.TetrominoType;

import java.util.*;

public class SevenBagGenerator extends AbstractGenerator {
    private final List<TetrominoType> minos;
    private final Queue<TetrominoType> bag = new ArrayDeque<>(7);

    public SevenBagGenerator() {
        minos = new ArrayList<>(7);
        minos.addAll(Arrays.asList(TetrominoType.getTetrominoShapes()));
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
        Collections.shuffle(minos);
        bag.addAll(minos);
    }
}
