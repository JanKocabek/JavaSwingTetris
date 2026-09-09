package org.sehes.tetris.controller;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

/**
 * A Swing timer-driven game loop that measures elapsed time and publishes
 * tick and FPS events. Game logic is delegated to observers of these events.
 */
public class SwingTimerGameLoop implements ActionListener, GameLoop {
    private static final int FPS = 60;
    private static final int FRAME_TIME_MS = 1000 / FPS;
    private final Timer timer;
    private final Observable.Publisher<Integer> fpsObservablePublisher = new ObservableImpl<>();
    private final Observable.Publisher<Long> tickPublisher = new ObservableImpl<>();
    private long prevTime;
    private int frameCount = 0;
    private long fpsTimer = 0;

    public SwingTimerGameLoop() {
        timer = new Timer(FRAME_TIME_MS, this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        long currentTime = System.nanoTime();
        if (prevTime == 0) {
            prevTime = currentTime;// Safety guard in case this is invoked before newGame() initializes timing state. is it ever needed?
            return;
        }
        var elapsedTime = currentTime - prevTime;
        prevTime = currentTime;
        fpsCalculation(elapsedTime);
        tickPublisher.notify(elapsedTime);
    }

    public Observable<Integer> fpsObservable() {
        return fpsObservablePublisher;
    }

    @Override
    public void resume() {
        resetTime();
    }

    @Override
    public void start() {
        resetTime();
        timer.start();
    }

    @Override
    public void stop() {
        timer.stop();
    }

    @Override
    public void restart() {
        timer.restart();
        resetTime();
    }

    private void resetTime() {
        prevTime = System.nanoTime();
        frameCount = 0;
        fpsTimer = 0;
    }

    public Observable<Long> tickObservable() {
        return tickPublisher;
    }

    private void fpsCalculation(long elapsedTime) {
        int currentFPS;
        frameCount++;
        fpsTimer += elapsedTime;

        if (fpsTimer >= TimeUnit.SECONDS.toNanos(1)) {
            currentFPS = frameCount; // This is your actual FPS for the last second
            frameCount = 0;
            fpsTimer = 0;
            fpsObservablePublisher.notify(currentFPS);
        }
    }
}
