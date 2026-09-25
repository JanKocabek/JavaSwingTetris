package org.sehes.tetris.model;

import java.util.EnumMap;
import java.util.List;

public class ShapeProvider {

    private static final EnumMap<TetrominoType, EnumMap<Orientation, List<Coordinate>>> tetrominoStates = new EnumMap<>(TetrominoType.class);

    /*
     * static block to initialize the tetrominoStates map with the default shapes in
     * each orientation for each tetromino type.<br>
     */
    static {
        for (TetrominoType type : TetrominoType.getTetrominoShapes()) {
            tetrominoStates.put(type, new EnumMap<>(Orientation.class));
        }

        final List<Coordinate> shapeO = List.of(new Coordinate(-1, -1), new Coordinate(-1, 0), new Coordinate(0, 0), new Coordinate(0, -1));
        for (Orientation o : Orientation.values()) {
            tetrominoStates.get(TetrominoType.O).put(o, shapeO);
        }
        tetrominoStates.get(TetrominoType.I).put(Orientation.NORTH, List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(-1, 0),
                new Coordinate(2, 0)));
        tetrominoStates.get(TetrominoType.I).put(Orientation.EAST, List.of(new Coordinate(1, 1), new Coordinate(1, -1), new Coordinate(1, 0), new Coordinate(1, 2)));
        tetrominoStates.get(TetrominoType.I).put(Orientation.SOUTH, List.of(new Coordinate(0, 1), new Coordinate(-1, 1), new Coordinate(1, 1), new Coordinate(2, 1)));
        tetrominoStates.get(TetrominoType.I).put(Orientation.WEST, List.of(new Coordinate(0, 0), new Coordinate(0, -1), new Coordinate(0, 1), new Coordinate(0, 2)));
        tetrominoStates.get(TetrominoType.J).put(Orientation.NORTH, List.of(new Coordinate(-1, -1), new Coordinate(-1, 0), new Coordinate(0, 0), new Coordinate(1, 0)));
        tetrominoStates.get(TetrominoType.J).put(Orientation.EAST, List.of(new Coordinate(1, -1), new Coordinate(0, -1), new Coordinate(0, 0), new Coordinate(0, 1)));
        tetrominoStates.get(TetrominoType.J).put(Orientation.SOUTH, List.of(new Coordinate(1, 1), new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(-1, 0)));
        tetrominoStates.get(TetrominoType.J).put(Orientation.WEST, List.of(new Coordinate(-1, 1), new Coordinate(0, 1), new Coordinate(0, 0), new Coordinate(0, -1)));
        tetrominoStates.get(TetrominoType.L).put(Orientation.NORTH, List.of(new Coordinate(-1, 0), new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, -1)));
        tetrominoStates.get(TetrominoType.L).put(Orientation.EAST, List.of(new Coordinate(0, -1), new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1)));
        tetrominoStates.get(TetrominoType.L).put(Orientation.SOUTH, List.of(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(-1, 0), new Coordinate(-1, 1)));
        tetrominoStates.get(TetrominoType.L).put(Orientation.WEST, List.of(new Coordinate(0, 1), new Coordinate(0, 0), new Coordinate(0, -1), new Coordinate(-1, -1)));
        tetrominoStates.get(TetrominoType.S).put(Orientation.NORTH, List.of(new Coordinate(1, -1), new Coordinate(0, -1), new Coordinate(0, 0), new Coordinate(-1, 0)));
        tetrominoStates.get(TetrominoType.S).put(Orientation.EAST, List.of(new Coordinate(1, 1), new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, -1)));
        tetrominoStates.get(TetrominoType.S).put(Orientation.SOUTH, List.of(new Coordinate(-1, 1), new Coordinate(0, 1), new Coordinate(0, 0), new Coordinate(1, 0)));
        tetrominoStates.get(TetrominoType.S).put(Orientation.WEST, List.of(new Coordinate(-1, -1), new Coordinate(-1, 0), new Coordinate(0, 0), new Coordinate(0, 1)));
        tetrominoStates.get(TetrominoType.T).put(Orientation.NORTH, List.of(new Coordinate(0, -1), new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(-1, 0)));
        tetrominoStates.get(TetrominoType.T).put(Orientation.EAST, List.of(new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(0, -1)));
        tetrominoStates.get(TetrominoType.T).put(Orientation.SOUTH, List.of(new Coordinate(0, 1), new Coordinate(0, 0), new Coordinate(-1, 0), new Coordinate(1, 0)));
        tetrominoStates.get(TetrominoType.T).put(Orientation.WEST, List.of(new Coordinate(-1, 0), new Coordinate(0, 0), new Coordinate(0, -1), new Coordinate(0, 1)));
        tetrominoStates.get(TetrominoType.Z).put(Orientation.NORTH, List.of(new Coordinate(-1, -1), new Coordinate(0, -1), new Coordinate(0, 0), new Coordinate(1, 0)));
        tetrominoStates.get(TetrominoType.Z).put(Orientation.EAST, List.of(new Coordinate(1, -1), new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(0, 1)));
        tetrominoStates.get(TetrominoType.Z).put(Orientation.SOUTH, List.of(new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0), new Coordinate(-1, 0)));
        tetrominoStates.get(TetrominoType.Z).put(Orientation.WEST, List.of(new Coordinate(-1, 1), new Coordinate(-1, 0), new Coordinate(0, 0), new Coordinate(0, -1)));
    }

    private ShapeProvider() {
    }

    public static List<Coordinate> getTetrominoState(TetrominoType type, Orientation orientation) {
        return tetrominoStates.get(type).get(orientation);
    }

    //      N
    //    W   E
    //      S
    //
    static class WallKicks {

        private static final EnumMap<WallKickType, EnumMap<Transition, List<Coordinate>>> WALL_KICKS = new EnumMap<>(WallKickType.class);

        static {
            for (WallKickType type : WallKickType.values()) {
                WALL_KICKS.put(type, new EnumMap<>(Transition.class));
            }
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.NORTH_TO_EAST, List.of(new Coordinate(-1, 0), new Coordinate(-1, -1), new Coordinate(0, 2), new Coordinate(-1, 2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.EAST_TO_NORTH, List.of(new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, -2), new Coordinate(1, -2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.EAST_TO_SOUTH, List.of(new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, -2), new Coordinate(1, -2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.SOUTH_TO_WEST, List.of(
                    new Coordinate(1, 0),
                    new Coordinate(1, -1),
                    new Coordinate(0, 2),
                    new Coordinate(1, 2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.SOUTH_TO_EAST, List.of(
                    new Coordinate(-1, 0),
                    new Coordinate(-1, -1),
                    new Coordinate(0, 2),
                    new Coordinate(-1, 2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.WEST_TO_SOUTH, List.of(
                    new Coordinate(-1, 0),
                    new Coordinate(-1, 1),
                    new Coordinate(0, -2),
                    new Coordinate(-1, -2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.WEST_TO_NORTH, List.of(new Coordinate(-1, 0), new Coordinate(-1, 1), new Coordinate(0, -2), new Coordinate(-1, -2)));
            WALL_KICKS.get(WallKickType.NORMAL).put(Transition.NORTH_TO_WEST, List.of(new Coordinate(1, 0), new Coordinate(1, -1), new Coordinate(0, 2), new Coordinate(1, 2)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.NORTH_TO_EAST, List.of(new Coordinate(-2, 0), new Coordinate(1, 0), new Coordinate(-2, 1), new Coordinate(1, -2)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.EAST_TO_NORTH, List.of(new Coordinate(2, 0), new Coordinate(-1, 0), new Coordinate(2, -1), new Coordinate(-1, 2)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.EAST_TO_SOUTH, List.of(new Coordinate(-1, 0), new Coordinate(2, 0), new Coordinate(-1, -2), new Coordinate(2, 1)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.SOUTH_TO_EAST, List.of(new Coordinate(1, 0), new Coordinate(-2, 0), new Coordinate(1, 2), new Coordinate(-2, -1)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.SOUTH_TO_WEST, List.of(new Coordinate(2, 0), new Coordinate(-1, 0), new Coordinate(2, -1), new Coordinate(-1, 2)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.WEST_TO_SOUTH, List.of(new Coordinate(-2, 0), new Coordinate(1, 0), new Coordinate(-2, 1), new Coordinate(1, -2)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.WEST_TO_NORTH, List.of(new Coordinate(1, 0), new Coordinate(-2, 0), new Coordinate(1, 2), new Coordinate(-2, -1)));
            WALL_KICKS.get(WallKickType.I_KICKS).put(Transition.NORTH_TO_WEST, List.of(new Coordinate(-1, 0), new Coordinate(2, 0), new Coordinate(-1, -2), new Coordinate(2, 1)));
        }

        static List<Coordinate> getWallKicks(WallKickType wallKickType, Transition transition) {
            return WALL_KICKS.get(wallKickType).get(transition);
        }

        enum WallKickType {
            NORMAL, I_KICKS
        }

        enum Transition {
            NORTH_TO_EAST, EAST_TO_NORTH, EAST_TO_SOUTH, SOUTH_TO_WEST, SOUTH_TO_EAST, WEST_TO_SOUTH, WEST_TO_NORTH, NORTH_TO_WEST,
        }
    }

}
