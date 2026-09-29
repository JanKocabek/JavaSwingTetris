package org.sehes.tetris.model;

import org.junit.jupiter.api.Test;
import org.sehes.tetris.config.GameParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TetrominoTests {

    private final PieceGenerator generator = new RandomPieceGenerator();

    @Test
    void testTetrominoFactory_ValidTetrominoCreation() {
        // given
        Tetromino tetromino = TetrominoFactory.spawnTetromino(generator.getNextPiece(), GameParameters.SPAWN_POINT);
        // when
        // then
        assertNotNull(tetromino, "tetrominoFactory should not return null");
        assertEquals(GameParameters.SPAWN_POINT.x(), tetromino.getPositionX());
        assertEquals(GameParameters.SPAWN_POINT.y(), tetromino.getPositionY());
        assertNotNull(tetromino.getType());
        assertNotNull(tetromino.getStateCord());
    }
}
