package org.sehes.tetris.model;

/**
 * Test utility class providing factory methods to create pre-configured
 * {@link Tetromino} instances for use across multiple test classes.
 *
 * <p>Removes the boilerplate of manually calling
 * {@link Tetromino#setNewState(java.util.List, Orientation)} in every test
 * that needs a piece placed at a specific board position with a specific
 * starting orientation. The returned {@link SpawnedTetromino} bundles the
 * ready-to-spawn tetromino together with an optional pending rotation, so
 * both can be passed around as a single unit.
 *
 * <p>Usage example:
 * <pre>{@code
 * // Spawn a T-piece at (4,1) facing NORTH, to be rotated CW in the test:
 * SpawnedTetromino piece = spawnT(new Coordinate(4, 1), Orientation.NORTH, RotationFlag.CLOCKWISE);
 * gameBoard.trySpawnTetromino(piece.t());
 * gameBoard.tryRotatePiece(piece.rotation());
 *
 * // Spawn an I-piece at (4,12) facing NORTH, no rotation:
 * SpawnedTetromino iBar = spawn(TetrominoType.I, new Coordinate(4, 12), Orientation.NORTH, null);
 * gameBoard.trySpawnTetromino(iBar.t());
 * }</pre>
 */
public final class MinoSpawn {

    private MinoSpawn() {
        // utility class, not instantiable
    }

    /**
     * Creates a T-tetromino at the given board position with the specified
     * starting orientation and a pending rotation.
     *
     * @param spawnCord the board coordinate of the T-piece pivot
     * @param startOrie the initial orientation before any rotation
     * @param rotation  the rotation to apply during the test,
     *                  or {@code null} if no rotation is needed
     * @return a {@link SpawnedTetromino} ready to be spawned onto a {@link GameBoard}
     */
    public static SpawnedTetromino spawnT(Coordinate spawnCord, Orientation startOrie, RotationFlag rotation) {
        return spawn(TetrominoType.T, spawnCord, startOrie, rotation);
    }

    /**
     * Creates a tetromino of the given type at the given board position with the
     * specified starting orientation and a pending rotation.
     *
     * @param type      the type of tetromino to spawn
     * @param spawnCord the board coordinate of the tetromino's pivot
     * @param startOrie the initial orientation before any rotation
     * @param rotation  the rotation to apply during the test,
     *                  or {@code null} if no rotation is needed
     * @return a {@link SpawnedTetromino} ready to be spawned onto a {@link GameBoard}
     */
    public static SpawnedTetromino spawn(TetrominoType type, Coordinate spawnCord, Orientation startOrie, RotationFlag rotation) {
        final Tetromino t = new Tetromino(type, spawnCord);
        t.setNewState(ShapeProvider.getTetrominoState(type, startOrie), startOrie);
        return new SpawnedTetromino(t, rotation);
    }

    /**
     * Carries a pre-configured {@link Tetromino} together with the rotation that
     * should be applied to it inside a test, so both can be passed around as one unit.
     *
     * @param tetromino the pre-configured tetromino
     * @param rotation  the rotation to apply, or {@code null} if no rotation is needed
     */
    public record SpawnedTetromino(Tetromino tetromino, RotationFlag rotation) {
        /**
         * Alias for {@link #tetromino()} for brevity in assertions and spawn calls.
         */
        public Tetromino t() {
            return tetromino;
        }
    }
}
