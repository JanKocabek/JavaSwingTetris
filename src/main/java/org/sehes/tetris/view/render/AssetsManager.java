package org.sehes.tetris.view.render;

import org.sehes.tetris.model.GhostType;
import org.sehes.tetris.model.TetrominoType;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

public final class AssetsManager {
    private final EnumMap<TetrominoType, BufferedImage> tetrominoTiles;
    private final EnumMap<GhostType, BufferedImage> ghostTiles;
    private final EnumMap<TetrominoType, BufferedImage> previewTiles;

    public AssetsManager(RenderingHints hints) {
        final MinoBlock block = new MinoBlock(Config.BLOCK_SIZE, Config.BLOCK_THICKNESS);
        tetrominoTiles = prepareMinoTiles(block, hints);
        ghostTiles = prepareGhostTiles(hints);
        previewTiles = prepareMinoTiles(block, hints, Config.PREVIEW_BLOCK_SIZE);
    }

    private EnumMap<TetrominoType, BufferedImage> prepareMinoTiles(MinoBlock block, RenderingHints hints) {
        return prepareMinoTiles(block, hints, Config.BLOCK_SIZE);
    }

    private EnumMap<TetrominoType, BufferedImage> prepareMinoTiles(MinoBlock block, RenderingHints hints, int tileSize) {
        final var tilesMap = new EnumMap<TetrominoType, BufferedImage>(TetrominoType.class);
        for (TetrominoType type : TetrominoType.getTetrominoShapes()) {
            tilesMap.put(type, createMinoTile(type, block, hints, tileSize));
        }

        return tilesMap;
    }

    private EnumMap<GhostType, BufferedImage> prepareGhostTiles(RenderingHints hints) {
        final var BASIC_THICKNESS = 5f;
        final var DASH_THICKNESS = 1f;
        final float[] dashPattern = {10.0f, 10.0f};
        final var dashStroke = new BasicStroke(DASH_THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dashPattern, 0.0f);
        final var ghostTiles = new EnumMap<GhostType, BufferedImage>(GhostType.class);
        ghostTiles.put(GhostType.FULL, createGhostTile(hints, BASIC_THICKNESS, new BasicStroke(BASIC_THICKNESS), Color.WHITE));
        ghostTiles.put(GhostType.DASH, createGhostTile(hints, DASH_THICKNESS, dashStroke, Color.WHITE));
        return ghostTiles;
    }

    private BufferedImage createMinoTile(TetrominoType type, MinoBlock block, RenderingHints hints, int tileSize) {
        final var img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        final var g2d = img.createGraphics();
        g2d.setRenderingHints(hints);
        scaleCheck(tileSize, g2d);
        drawShape(type, block, g2d);
        g2d.dispose();
        return img;
    }

    private void scaleCheck(int tileSize, Graphics2D g2d) {
        if (tileSize != Config.BLOCK_SIZE) {
            double scale = (double) tileSize / Config.BLOCK_SIZE;
            g2d.scale(scale, scale);
        }
    }

    private void drawShape(TetrominoType type, MinoBlock block, Graphics2D g2d) {

        for (Renderable shape : block.getShapes()) {
            final var side = shape.getSide();
            final var points = shape.getPoints();
            g2d.setPaint(ColorPalette.getPaint(type, side));
            Path2D path = new Path2D.Double();
            path.moveTo(points[0][0], points[0][1]);
            for (int i = 1; i < points.length; i++) {
                path.lineTo(points[i][0], points[i][1]);
            }
            path.closePath();
            g2d.fill(path);
        }
    }

    private BufferedImage createGhostTile(RenderingHints hints, float thickness, Stroke stroke, Color strokeColor) {
        BufferedImage buffer = new BufferedImage(Config.BLOCK_SIZE, Config.BLOCK_SIZE, BufferedImage.TYPE_INT_ARGB);
        final var g2d = buffer.createGraphics();
        g2d.setRenderingHints(hints);
        final var sideSize = (int) (Config.BLOCK_SIZE - thickness - 2);
        g2d.setColor(strokeColor);
        g2d.setStroke(stroke);
        g2d.drawRect(0, 0, sideSize, sideSize);
        g2d.dispose();
        return buffer;
    }

    public BufferedImage getTile(TetrominoType type) {
        return tetrominoTiles.get(type);
    }

    public BufferedImage getGhostTile(GhostType type) {
        return switch (type) {
            case FULL, DASH -> ghostTiles.get(type);
            case NONE -> throw new IllegalArgumentException("Empty ghost Tile was called as regular one");
        };
    }

    public BufferedImage getPreviewTile(TetrominoType type) {
        return previewTiles.get(type);
    }
}
