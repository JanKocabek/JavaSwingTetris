package org.sehes.tetris.controller;

import org.sehes.tetris.common.GameState;
import org.sehes.tetris.common.event.Observable;
import org.sehes.tetris.common.event.Observer;
import org.sehes.tetris.model.GameBoard;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.level.LevelManager;
import org.sehes.tetris.model.score.ScoreEvent;
import org.sehes.tetris.model.score.ScoreMessenger;
import org.sehes.tetris.testUtil.SpecificPieceGenerator;
import org.sehes.tetris.view.Rendering;

import java.util.function.Supplier;

class GameManagerBuilder {

    private StateManager<GameState> stateManager = new GameStateManager(GameState.PREPARED);
    private ScoreMessenger scoreMessenger = new ScoreMessenger();
    private PieceGenerator generator = new SpecificPieceGenerator(TetrominoType.I);
    private GameLoop loop = new dummyGameLoop();
    private Rendering render = snapshot -> {
    };
    private LevelManager levelManager = new LevelManager();
    private Supplier<GameBoard> gameBoardSupplier = GameBoard::new;

    GameManagerBuilder setStateManager(StateManager<GameState> stateManager) {
        this.stateManager = stateManager;
        return this;
    }

    GameManagerBuilder setScoreMessenger(ScoreMessenger scoreMessenger) {
        this.scoreMessenger = scoreMessenger;
        return this;
    }

    GameManagerBuilder setGenerator(PieceGenerator generator) {
        this.generator = generator;
        return this;
    }

    GameManagerBuilder setGenerator(TetrominoType minoType) {
        this.generator = new SpecificPieceGenerator(minoType);
        return this;
    }

    GameManagerBuilder setLoop(GameLoop loop) {
        this.loop = loop;
        return this;
    }

    GameManagerBuilder setRender(Rendering render) {
        this.render = render;
        return this;
    }

    GameManagerBuilder setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
        return this;
    }

    GameManagerBuilder setGameBoardSupplier(Supplier<GameBoard> gameBoardSupplier) {
        this.gameBoardSupplier = gameBoardSupplier;
        return this;
    }

    GameManager build() {
        return new GameManager(stateManager, scoreMessenger, generator, loop, render, levelManager, gameBoardSupplier);
    }

    GameManagerBuilder addScoreObserver(Observer<ScoreEvent> observer) {
        scoreMessenger.addObserver(observer);
        return this;
    }

    static GameManagerBuilder create() {
        return new GameManagerBuilder();
    }

    private GameManagerBuilder() {
    }


    @SuppressWarnings("unused")
    private static class dummyGameLoop implements GameLoop {
        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void restart() {

        }

        @Override
        public void resume() {

        }

        @Override
        public Observable<Long> tickObservable() {
            return null;
        }

        @Override
        public Observable<Integer> fpsObservable() {
            return null;
        }
    }

}
