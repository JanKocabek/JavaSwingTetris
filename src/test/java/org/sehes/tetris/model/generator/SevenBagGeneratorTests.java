package org.sehes.tetris.model.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sehes.tetris.model.TetrominoType;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class SevenBagGeneratorTests {
    PieceGenerator generator;
    private static final TetrominoType[] ALL_MINOS = TetrominoType.getTetrominoShapes();

    @BeforeEach
    void setUp() {
        generator = new SevenBagGenerator();
    }

    @Test
    void shouldGenerateSevenBag() {
        //arrange
        final var minosPeak = new ArrayList<TetrominoType>();
        final var minosGenerate = new ArrayList<TetrominoType>();
        //act
        for (int i = 0; i < 7; i++) {
            minosPeak.add(generator.peekNext());
            minosGenerate.add(generator.getNextPiece());
        }
        //assert
        assertThat(minosPeak).hasSize(7).containsOnlyOnce(ALL_MINOS);
        assertThat(minosGenerate).hasSize(7).containsOnlyOnce(ALL_MINOS);
        assertThat(minosPeak).isEqualTo(minosGenerate);
    }

    @Test
    void shouldRefillBagWhenBagIsEmpty() {
        //arrange
        final var bag = new ArrayList<TetrominoType>();
        //act
        for (int i = 0; i < 14; i++) {
            bag.add(generator.getNextPiece());
        }
        //assert
        assertThat(bag).hasSize(14);
    }

    @Test
    void shouldResetCompletelyWhenIsCallNewOne() {
        final var firstBag = new ArrayList<TetrominoType>();
        //act
        for (int i = 0; i < 4; i++) {
            firstBag.add(generator.getNextPiece());
        }
        generator.startNewQueue();
        final var secondBag = new ArrayList<TetrominoType>();
        for (int i = 0; i < 7; i++) {
            secondBag.add(generator.getNextPiece());
        }
        //assert
        assertThat(secondBag).hasSize(7).containsExactlyInAnyOrder(ALL_MINOS);
        assertThat(firstBag).isNotEqualTo(secondBag);
    }

    @Test
    void shouldBeShuffled() {
        PieceGenerator g= new SevenBagGenerator(1);
        //arrange
        final var bag = new ArrayList<TetrominoType>();
        //act
        for (int i = 0; i < 7; i++) {
            bag.add(g.getNextPiece());
        }
        //assert
        assertThat(bag).doesNotContainSequence(ALL_MINOS);
    }
}