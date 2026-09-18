package org.sehes.tetris.controller;

import org.jspecify.annotations.Nullable;
import org.sehes.tetris.config.GhostType;
import org.sehes.tetris.model.BoardView;
import org.sehes.tetris.model.Tetromino;

import java.util.Optional;

public record GameSnapshot(BoardView boardView, Optional<Tetromino> currentTetromino, boolean isBoardDirty,
                           int distance, GhostType ghostType, @Nullable Double lockInfo) {
}
