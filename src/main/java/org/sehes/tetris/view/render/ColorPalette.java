package org.sehes.tetris.view.render;

import org.jspecify.annotations.NullMarked;
import org.sehes.tetris.model.TetrominoType;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;


/**
 *
 * Palette of colors and shades for each tetromino shape.
 * Each tetromino shape has a unique color palette, with 5 colors:
 * - 4 colors for the 4 sides of the tetromino
 * - 1 color for the center square
 * Array is ordered clockwise
 * as [top, right, bottom, left, center]
 */

@NullMarked
public enum ColorPalette {
    CYAN(TetrominoType.I, new Paint[]{Colors.I_SHADE[0], Colors.I_SHADE[1], Colors.I_SHADE[2], Colors.I_SHADE[3], Colors.I_SHADE[4]}),
    YELLOW(TetrominoType.O, new Paint[]{Colors.O_SHADE[0], Colors.O_SHADE[1], Colors.O_SHADE[2], Colors.O_SHADE[3], Colors.O_SHADE[4]}),
    GREEN(TetrominoType.S, new Color[]{Colors.S_SHADE[0], Colors.S_SHADE[1], Colors.S_SHADE[2], Colors.S_SHADE[3], Colors.S_SHADE[4]}),
    BLUE(TetrominoType.J, new Color[]{Colors.J_SHADE[0], Colors.J_SHADE[1], Colors.J_SHADE[2], Colors.J_SHADE[3], Colors.J_SHADE[4]}),
    ORANGE(TetrominoType.L, new Color[]{Colors.L_SHADE[0], Colors.L_SHADE[1], Colors.L_SHADE[2], Colors.L_SHADE[3], Colors.L_SHADE[4]}),
    RED(TetrominoType.Z, new Color[]{Colors.Z_SHADE[0], Colors.Z_SHADE[1], Colors.Z_SHADE[2], Colors.Z_SHADE[3], Colors.Z_SHADE[4]}),
    MAGENTA(TetrominoType.T, new Color[]{Colors.T_SHADE[0], Colors.T_SHADE[1], Colors.T_SHADE[2], Colors.T_SHADE[3], Colors.T_SHADE[4]});

    private final Paint[] colors;
    private final TetrominoType mino;
    private static final ColorPalette[] CACHE = values();
    private static final Map<TetrominoType, Paint[]> COLOR_MAP;


    static {
        COLOR_MAP = new EnumMap<>(TetrominoType.class);
        for (var e : CACHE) {
            if (e.mino != TetrominoType.NON) {
                COLOR_MAP.put(e.mino, e.colors);
            }
        }
    }

    ColorPalette(TetrominoType mino, Paint[] colors) {
        this.colors = colors;
        this.mino = mino;
    }

    public static Paint getPaint(TetrominoType type, Side side) {
        final var sideMap = COLOR_MAP.get(type);
        return switch (side) {
            case TOP -> sideMap[0];
            case RIGHT -> sideMap[1];
            case BOTTOM -> sideMap[2];
            case LEFT -> sideMap[3];
            case CENTER -> sideMap[4];
        };
    }


    /**
     * Colors for the Tetrominoes
     * each Color is Palette of 5 shades of the same color
     * ordered clockwise top, right, bottom, left, center
     * each shade is make as a HSB color shifted by hue, and adjusted by saturation and brightness
     *
     *
     */

    static class Colors {
        private Colors() {
        }

        private static float wrapHue(float h) {
            return (h = h % 1f) < 0 ? h + 1f : h;
        }

        // Shade formula (same for all):
        // top    → sat *0.45, bright +0.10 (bright highlight)
        // right  → hue +2°,  sat *1.00, bright *0.78
        // bottom → sat *1.00, bright *0.58 (deep shadow)
        // left   → hue -2°,  sat *0.85, bright *0.92
        // center → base

        // I - Vivid Cyan
        private static final Color I_BASE = new Color(0, 200, 220);
        private static final float[] I_H = Color.RGBtoHSB(I_BASE.getRed(), I_BASE.getGreen(), I_BASE.getBlue(), null);
        static final Color[] I_SHADE = {
                Color.getHSBColor(I_H[0], I_H[1] * 0.45f, Math.min(I_H[2] + 0.10f, 1f)),
                Color.getHSBColor(I_H[0] + 2 / 360f, I_H[1], I_H[2] * 0.78f),
                Color.getHSBColor(I_H[0], I_H[1], I_H[2] * 0.58f),
                Color.getHSBColor(I_H[0] - 2 / 360f, I_H[1] * 0.85f, I_H[2] * 0.92f),
                Color.getHSBColor(I_H[0], I_H[1], I_H[2])
        };

        // O - Golden Yellow
        private static final Color O_BASE = new Color(255, 205, 0);
        private static final float[] O_H = Color.RGBtoHSB(O_BASE.getRed(), O_BASE.getGreen(), O_BASE.getBlue(), null);
        static final Color[] O_SHADE = {
                Color.getHSBColor(O_H[0], O_H[1] * 0.45f, Math.min(O_H[2] + 0.10f, 1f)),
                Color.getHSBColor(O_H[0] + 2 / 360f, O_H[1], O_H[2] * 0.78f),
                Color.getHSBColor(O_H[0], O_H[1], O_H[2] * 0.58f),
                Color.getHSBColor(O_H[0] - 2 / 360f, O_H[1] * 0.85f, O_H[2] * 0.92f),
                Color.getHSBColor(O_H[0], O_H[1], O_H[2])
        };

        // S - Vivid Green
        private static final Color S_BASE = new Color(50, 185, 50);
        private static final float[] S_H = Color.RGBtoHSB(S_BASE.getRed(), S_BASE.getGreen(), S_BASE.getBlue(), null);
        static final Color[] S_SHADE = {
                Color.getHSBColor(S_H[0], S_H[1] * 0.45f, Math.min(S_H[2] + 0.10f, 1f)),
                Color.getHSBColor(S_H[0] + 2 / 360f, S_H[1], S_H[2] * 0.78f),
                Color.getHSBColor(S_H[0], S_H[1], S_H[2] * 0.58f),
                Color.getHSBColor(S_H[0] - 2 / 360f, S_H[1] * 0.85f, S_H[2] * 0.92f),
                Color.getHSBColor(S_H[0], S_H[1], S_H[2])
        };

        // J - Royal Blue
        private static final Color J_BASE = new Color(50, 100, 220);
        private static final float[] J_H = Color.RGBtoHSB(J_BASE.getRed(), J_BASE.getGreen(), J_BASE.getBlue(), null);
        static final Color[] J_SHADE = {
                Color.getHSBColor(J_H[0], J_H[1] * 0.45f, Math.min(J_H[2] + 0.10f, 1f)),
                Color.getHSBColor(J_H[0] + 2 / 360f, J_H[1], J_H[2] * 0.78f),
                Color.getHSBColor(J_H[0], J_H[1], J_H[2] * 0.58f),
                Color.getHSBColor(J_H[0] - 2 / 360f, J_H[1] * 0.85f, J_H[2] * 0.92f),
                Color.getHSBColor(J_H[0], J_H[1], J_H[2])
        };

        // L - Vivid Orange
        private static final Color L_BASE = new Color(235, 125, 15);
        private static final float[] L_H = Color.RGBtoHSB(L_BASE.getRed(), L_BASE.getGreen(), L_BASE.getBlue(), null);
        static final Color[] L_SHADE = {
                Color.getHSBColor(L_H[0], L_H[1] * 0.45f, Math.min(L_H[2] + 0.10f, 1f)),
                Color.getHSBColor(L_H[0] + 2 / 360f, L_H[1], L_H[2] * 0.78f),
                Color.getHSBColor(L_H[0], L_H[1], L_H[2] * 0.58f),
                Color.getHSBColor(L_H[0] - 2 / 360f, L_H[1] * 0.85f, L_H[2] * 0.92f),
                Color.getHSBColor(L_H[0], L_H[1], L_H[2])
        };

        // Z - Crimson Red
        private static final Color Z_BASE = new Color(215, 40, 60);
        private static final float[] Z_H = Color.RGBtoHSB(Z_BASE.getRed(), Z_BASE.getGreen(), Z_BASE.getBlue(), null);
        static final Color[] Z_SHADE = {
                Color.getHSBColor(Z_H[0], Z_H[1] * 0.45f, Math.min(Z_H[2] + 0.10f, 1f)),
                Color.getHSBColor(Z_H[0] + 2 / 360f, Z_H[1], Z_H[2] * 0.78f),
                Color.getHSBColor(Z_H[0], Z_H[1], Z_H[2] * 0.58f),
                Color.getHSBColor(Z_H[0] - 2 / 360f, Z_H[1] * 0.85f, Z_H[2] * 0.92f),
                Color.getHSBColor(Z_H[0], Z_H[1], Z_H[2])
        };

        // T - Vivid Purple
        private static final Color T_BASE = new Color(170, 55, 200);
        private static final float[] T_H = Color.RGBtoHSB(T_BASE.getRed(), T_BASE.getGreen(), T_BASE.getBlue(), null);
        static final Color[] T_SHADE = {
                Color.getHSBColor(T_H[0], T_H[1] * 0.45f, Math.min(T_H[2] + 0.10f, 1f)),
                Color.getHSBColor(T_H[0] + 2 / 360f, T_H[1], T_H[2] * 0.78f),
                Color.getHSBColor(T_H[0], T_H[1], T_H[2] * 0.58f),
                Color.getHSBColor(T_H[0] - 2 / 360f, T_H[1] * 0.85f, T_H[2] * 0.92f),
                Color.getHSBColor(T_H[0], T_H[1], T_H[2])
        };
    }


}
