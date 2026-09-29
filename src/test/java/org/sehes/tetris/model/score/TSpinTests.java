package org.sehes.tetris.model.score;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.sehes.tetris.model.Orientation;

import static org.assertj.core.api.Assertions.assertThat;

class TSpinTests {

    // =========================================================================
    // getTSpin Branch Coverage Tests
    // =========================================================================



    @ParameterizedTest(name = "front={0}, back={1}, kick={2} -> NONE when total corners < 3")
    @CsvSource({
            "0, 0, false",
            "1, 0, false",
            "0, 1, false",
            "1, 1, false",
            "2, 0, false",
            "0, 2, false",
            "1, 1, true",
            "0, 0, true"
    })
    void getTSpin_whenCornersLessThanThree_returnsNone(int front, int back, boolean isKick) {
        TSpin result = TSpin.getTSpin(front, back, isKick);
        assertThat(result).isEqualTo(TSpin.NONE);
    }

    @ParameterizedTest(name = "front={0}, back={1}, kick={2} -> FULL when 2 front corners")
    @CsvSource({
            "2, 1, false",
            "2, 2, false",
            "2, 1, true",
            "2, 2, true"
    })
    void getTSpin_whenTwoFrontCorners_returnsFull(int front, int back, boolean isKick) {
        TSpin result = TSpin.getTSpin(front, back, isKick);
        assertThat(result).isEqualTo(TSpin.FULL);
    }

    @Test
    void getTSpin_whenOneFrontCornerAndTwoBackCornersWithTSpinKick_returnsFullPromoted() {
        // 3 corners total (1 front, 2 back) but promoted by SRS Test 5 (1x2 kick)
        TSpin result = TSpin.getTSpin(1, 2, true);
        assertThat(result).isEqualTo(TSpin.FULL);
    }

    @Test
    void getTSpin_whenZeroFrontCornersAndThreeBackCornersWithTSpinKick_returnsFullPromoted() {
        TSpin result = TSpin.getTSpin(0, 3, true);
        assertThat(result).isEqualTo(TSpin.FULL);
    }

    @Test
    void getTSpin_whenOneFrontCornerAndTwoBackCornersWithoutKick_returnsMini() {
        // Standard T-Spin Mini
        TSpin result = TSpin.getTSpin(1, 2, false);
        assertThat(result).isEqualTo(TSpin.MINI);
    }

    @Test
    void getTSpin_whenZeroFrontCornersAndThreeBackCornersWithoutKick_returnsMini() {
        TSpin result = TSpin.getTSpin(0, 3, false);
        assertThat(result).isEqualTo(TSpin.MINI);
    }

    // =========================================================================
    // Corner Offsets Coverage Tests
    // =========================================================================

    @ParameterizedTest
    @EnumSource(Orientation.class)
    void getFrontCornersOffset_returnsTwoCoordinatesForAllOrientations(Orientation orientation) {
        int[][] frontOffsets = TSpin.getFrontCornersOffset(orientation);
        assertThat(frontOffsets).isNotNull().hasDimensions(2, 2);
    }

    @ParameterizedTest
    @EnumSource(Orientation.class)
    void getBackCornersOffset_returnsTwoCoordinatesForAllOrientations(Orientation orientation) {
        int[][] backOffsets = TSpin.getBackCornersOffset(orientation);
        assertThat(backOffsets).isNotNull().hasDimensions(2, 2);
    }

    @Test
    void specificCornerOffsets_matchExpectedCoordinates() {
        // NORTH
        assertThat(TSpin.getFrontCornersOffset(Orientation.NORTH)).isDeepEqualTo(new int[][]{{-1, -1}, {1, -1}});
        assertThat(TSpin.getBackCornersOffset(Orientation.NORTH)).isDeepEqualTo(new int[][]{{-1, 1}, {1, 1}});

        // EAST
        assertThat(TSpin.getFrontCornersOffset(Orientation.EAST)).isDeepEqualTo(new int[][]{{1, -1}, {1, 1}});
        assertThat(TSpin.getBackCornersOffset(Orientation.EAST)).isDeepEqualTo(new int[][]{{-1, -1}, {-1, 1}});

        // SOUTH
        assertThat(TSpin.getFrontCornersOffset(Orientation.SOUTH)).isDeepEqualTo(new int[][]{{-1, 1}, {1, 1}});
        assertThat(TSpin.getBackCornersOffset(Orientation.SOUTH)).isDeepEqualTo(new int[][]{{-1, -1}, {1, -1}});

        // WEST
        assertThat(TSpin.getFrontCornersOffset(Orientation.WEST)).isDeepEqualTo(new int[][]{{-1, -1}, {-1, 1}});
        assertThat(TSpin.getBackCornersOffset(Orientation.WEST)).isDeepEqualTo(new int[][]{{1, -1}, {1, 1}});
    }

    @Test
    void enumValuesAndValueOf() {
        assertThat(TSpin.valueOf("NONE")).isEqualTo(TSpin.NONE);
        assertThat(TSpin.valueOf("FULL")).isEqualTo(TSpin.FULL);
        assertThat(TSpin.valueOf("MINI")).isEqualTo(TSpin.MINI);
        assertThat(TSpin.values()).containsExactly(TSpin.NONE, TSpin.FULL, TSpin.MINI);
    }
}
