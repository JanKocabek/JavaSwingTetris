package org.sehes.tetris.model.score;

import org.sehes.tetris.common.event.Observable;
import org.sehes.tetris.common.event.Observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScoreMessenger implements Observable<ScoreEvent> {
    private final List<Observer<ScoreEvent>> observers = new CopyOnWriteArrayList<>();

    /**
     * Registers {@code Observer}. Held by strong reference — callers must
     * remove it via {@link #removeObserver} if the observer's owner
     * (e.g. a panel) is discarded before this manager.<br> Currently, observers
     * are registered once at startup and live for the app's lifetime.
     *
     * @param observer object that will be notified of score changes
     */
    public void addObserver(Observer<ScoreEvent> observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer<ScoreEvent> observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ScoreEvent event) {
        observers.forEach(observer -> observer.update(event));
    }
}
