package org.sehes.tetris.controller;


public class GameStateManager implements StateManager<GameState> {
    private GameState currentState;
    private final Observable.Publisher<GameState> gameStateObservable = new ObservableImpl<>();

    public GameStateManager(GameState state) {
        this.currentState = state;
    }

    public Observable<GameState> GameStateObservable() {
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
