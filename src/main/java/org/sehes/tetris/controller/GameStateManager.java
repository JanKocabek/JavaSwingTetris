package org.sehes.tetris.controller;


import org.sehes.tetris.common.GameState;
import org.sehes.tetris.common.event.Observable;
import org.sehes.tetris.common.event.ObservableImpl;

public class GameStateManager implements StateManager<GameState> {
    private GameState currentState;
    private final Observable.Publisher<GameState> gameStateObservable = new ObservableImpl<>();

    public GameStateManager(GameState state) {
        this.currentState = state;
    }

    public Observable<GameState> gameStateObservable() {
        return gameStateObservable;
    }

    @Override
    public GameState getState() {
        return this.currentState;
    }

    @Override
    public void setState(GameState state) {
        this.currentState = state;
        gameStateObservable.notify(state);
    }

}
