package org.sehes.tetris.common.event;

public interface Observer<T> {
    void update(T state);
}
