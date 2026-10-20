package org.sehes.tetris.model.score;

public record LockPieceEvent(int clearedLines, TSpin tspin, int level) implements ScoreEvent {

}
