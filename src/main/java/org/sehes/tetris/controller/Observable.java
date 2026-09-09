package org.sehes.tetris.controller;

public interface Observable<T> {
    /**
     * Registers {@code Observer}. Held by strong reference — callers must
     * remove it via {@link #removeObserver} if the observer's owner
     * (e.g. a panel) is discarded before this manager.<br> Currently, observers
     * are registered once at startup and live for the app's lifetime.
     */
    void addObserver(Observer<T> observer);

    void removeObserver(Observer<T> observer);

    interface Publisher<T> extends Observable<T> {
        void notify(T event);
    }
}
