package org.sehes.tetris.controller;

import org.sehes.tetris.util.event.Observable;

public interface GameLoop {
    void start();

    void stop();

    void restart();

    void resume();

    Observable<Long> tickObservable();
    Observable<Integer> fpsObservable();
}
