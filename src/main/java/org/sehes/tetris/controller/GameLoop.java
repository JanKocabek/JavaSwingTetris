package org.sehes.tetris.controller;

public interface GameLoop {
    void start();

    void stop();

    void restart();

    void resume();

    Observable<Long> tickObservable();
    Observable<Integer> fpsObservable();
}
