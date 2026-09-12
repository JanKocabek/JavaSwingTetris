package org.sehes.tetris.model;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class TetrominoFactory {
   public static Tetromino spawnTetromino(final TetrominoType tetrominoType, final Coordinate startPos) {
        return new Tetromino(tetrominoType, startPos);
    }

}
