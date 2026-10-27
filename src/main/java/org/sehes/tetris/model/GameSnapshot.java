package org.sehes.tetris.model;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record GameSnapshot(BoardView boardView, Optional<Tetromino> currentTetromino, boolean isBoardDirty,
                           int ghostDistance, GhostType ghostType, @Nullable Double lockTime) {
}
