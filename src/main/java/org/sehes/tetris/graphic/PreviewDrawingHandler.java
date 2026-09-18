package org.sehes.tetris.graphic;

import org.sehes.tetris.model.Orientation;
import org.sehes.tetris.model.ShapeProvider;
import org.sehes.tetris.model.TetrominoType;

import javax.swing.Painter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class PreviewDrawingHandler implements Painter<TetrominoType> {

    private final AssetsManager assets;
    private final RenderingHints hints;

    public PreviewDrawingHandler(RenderingHints hints, AssetsManager assetsManager) {
        this.assets = assetsManager;
        this.hints = hints;
    }


    @Override
    public void paint(Graphics2D g, TetrominoType type, int width, int height) {
        if (type == null) return;
        g.setRenderingHints(hints);
        final var tile = assets.getPreviewTile(type);
        final var tileSize = tile.getWidth();
        final var coordinates = ShapeProvider.getTetrominoState(type, Orientation.NORTH);
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (var block : coordinates) {
            minX = Math.min(minX, block.x());
            maxX = Math.max(maxX, block.x());
            minY = Math.min(minY, block.y());
            maxY = Math.max(maxY, block.y());
        }
        // 2. Compute total pixel dimensions of the shape
        final int shapeWidth = (maxX - minX + 1) * tileSize;
        final int shapeHeight = (maxY - minY + 1) * tileSize;

        // 3. Compute top-left pixel offset to center the shape in the panel
        final int topLeftX = (width - shapeWidth) / 2;
        final int topLeftY = (height - shapeHeight) / 2;

        // 4. Draw each mino relative to the bounding box origin
        TetrominoRenderer.drawMinoAt(g, coordinates, tile, topLeftX, topLeftY, minX, minY);
    }
}
