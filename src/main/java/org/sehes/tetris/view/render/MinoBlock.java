package org.sehes.tetris.view.render;


/**
 * Represents a block of a tetromino, which is a square with beveled edges.
 * The points are ordered clockwise from top to bottom, and left to right.
 * The points are:
 * 0. (lOutTop, tY) - the left outer top point
 * 1. (rOutTop, tY) - the right outer top point
 * 2. (lInTop, rInTop) - the left inner top point, and the right inner top point
 * 3. (rInBottom, rInTop) - the right inner bottom point, and the right inner top point
 * 4. (lInBottom, lInTop) - the left inner bottom point, and the left inner top point
 * 5. (rOutBottom, bY) - the right outer bottom point
 * 6. (lOutBottom, bY) - the left outer bottom point
 * 7. (lOutBottom, tY) - the left outer bottom point
 * <br>
 * *-------* 0 - 1 L_OUT_TOP - R_OUT_TOP
 *  \     |
 *   *---* 2 - 3 L_IN_TOP - R_IN_TOP
 *   \   |
 *   *---* 4 - 5 L_IN_BOTTOM - R_IN_BOTTOM
 *   |    \
 *  *------* 6 - 7 L_OUT_BOTTOM - R_OUT_BOTTOM
 *
 *
 */

public class MinoBlock {
    private static final int L_OUT_TOP = 0;
    private static final int R_OUT_TOP = 1;
    private static final int L_IN_TOP = 2;
    private static final int R_IN_TOP = 3;
    private static final int L_IN_BOTTOM = 4;
    private static final int R_IN_BOTTOM = 5;
    private static final int L_OUT_BOTTOM = 6;
    private static final int R_OUT_BOTTOM = 7;
    private static final int[][] indexBuffer = {
            {L_OUT_TOP, R_OUT_TOP, R_IN_TOP, L_IN_TOP},
            {R_OUT_TOP, R_OUT_BOTTOM, R_IN_BOTTOM, R_IN_TOP},
            {R_OUT_BOTTOM, L_OUT_BOTTOM, L_IN_BOTTOM, R_IN_BOTTOM},
            {L_OUT_BOTTOM, L_OUT_TOP, L_IN_TOP, L_IN_BOTTOM},
            {L_IN_BOTTOM, L_IN_TOP, R_IN_TOP, R_IN_BOTTOM}
    };

    private static final int L_X = 0;
    private static final int T_Y = 0;
    private final int[][] vBuffer;
    private final Renderable[] shapes;

    public MinoBlock(int blockSize, int thickness) {
        final int rX = L_X + blockSize;
        final int inRtx = rX - thickness;
        final int inLtx = L_X + thickness;
        final int inTy = T_Y + thickness;
        final int bY = T_Y + blockSize;
        final int inBy = bY - thickness;
        vBuffer = new int[][]{
                {L_X, rX, inLtx, inRtx, inLtx, inRtx, L_X, rX},
                {T_Y, T_Y, inTy, inTy, inBy, inBy, bY, bY}
        };
        shapes = createShapes();
    }

    private Renderable[] createShapes() {
        final var renderables = new Renderable[Side.SIDE_COUNT];
        for (final var side : Side.sides()) {
            final int sideValue = side.getSide();
            renderables[sideValue] =
                    new Renderable() {
                        private final Side s = side;
                        private static final int VERTEX = 4;
                        final int[][] points = setPoints();


                        private int[][] setPoints() {
                            final var p = new int[VERTEX][2];

                            for (int i = 0; i < indexBuffer[sideValue].length; i++) {
                                p[i][0] = vBuffer[0][indexBuffer[sideValue][i]];
                                p[i][1] = vBuffer[1][indexBuffer[sideValue][i]];
                            }
                            return p;
                        }

                        @Override
                        public int[][] getPoints() {
                            return points;
                        }

                        @Override
                        public Side getSide() {
                            return s;
                        }
                    };

        }

        return renderables;
    }

    public Renderable[] getShapes() {
        return shapes;
    }


}
