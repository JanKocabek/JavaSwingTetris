package org.sehes.tetris.model.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.model.TetrominoType;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class RandomBagGeneratorTests {
    PieceGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RandomPieceGenerator();
    }

@Test
    void testGenerateNextQueueIsSameWithSameSeed() {
        PieceGenerator g=new RandomPieceGenerator(1L);
        final var queue = new ArrayList<TetrominoType>();
        for (int i = 0; i < 7; i++) {
            queue.add(g.getNextPiece());
        }
        g.startNewQueue();
        final var q2=new  ArrayList<TetrominoType>();
        for (int i = 0; i < 7; i++) {
            q2.add(g.getNextPiece());
        }
        //assert
        assertThat(queue).isEqualTo(q2);
    }

    @Test
    void testGenerateNextPieceIsDifferentWithDifferentSeed() {
        PieceGenerator g=new RandomPieceGenerator(1L);
        final var queue = new ArrayList<TetrominoType>();
        for (int i = 0; i < 7; i++) {
            queue.add(g.getNextPiece());
        }
        PieceGenerator g2=new RandomPieceGenerator(2L);
        final var que2=new  ArrayList<TetrominoType>();
        for (int i = 0; i < 7; i++) {
            que2.add(g2.getNextPiece());
        }
        //assert
        assertThat(queue).isNotEqualTo(que2);
    }

    @Test
    void testThatPeakMethodDoesntMoveTheQueue(){
        PieceGenerator randGen = new RandomPieceGenerator();
        TetrominoType firstPeek = randGen.peekNext();
        TetrominoType secondPeek = randGen.peekNext();
        assertThat(randGen.getNextPiece()).isEqualTo(firstPeek).isEqualTo(secondPeek);
    }
}
