package org.sehes.tetris.model;

import org.junit.jupiter.api.Test;
import org.sehes.tetris.model.generator.PieceGenerator;
import org.sehes.tetris.model.generator.RandomPieceGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TetrominoTests {

    private final PieceGenerator generator = new RandomPieceGenerator();

    @Test
    void testTetrominoFactory_ValidTetrominoCreation() {
        // given
        Tetromino tetromino = TetrominoFactory.spawnTetromino(generator.getNextPiece(), GameBoard.SPAWN_POINT);
        // when
        // then
        assertNotNull(tetromino, "tetrominoFactory should not return null");
        assertEquals(GameBoard.SPAWN_POINT.x(), tetromino.getPositionX());
        assertEquals(GameBoard.SPAWN_POINT.y(), tetromino.getPositionY());
        assertNotNull(tetromino.getType());
        assertNotNull(tetromino.getStateCord());
    }
}
