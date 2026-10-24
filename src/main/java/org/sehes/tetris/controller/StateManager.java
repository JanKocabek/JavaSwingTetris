package org.sehes.tetris.controller;

public interface StateManager<T> {
    void setState(T state);

    T getState();
}
