package org.sehes.tetris.controller;

import org.sehes.tetris.util.event.Observable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableImpl<T> implements Observable.Publisher<T> {
    private final List<Observer<T>> observerList = new CopyOnWriteArrayList<>();

    @Override
    public void addObserver(Observer<T> observer) {
        observerList.add(observer);
    }

    @Override
    public void removeObserver(Observer<T> observer) {
        observerList.remove(observer);
    }

    @Override
    public void notify(T event) {
        for (Observer<T> observer : observerList) {
            observer.update(event);
        }
    }
}
