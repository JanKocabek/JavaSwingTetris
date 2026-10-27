package org.sehes.tetris.model.score;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ScoreMessengerTest {

    @Test
    void notifyObservers_whenObserverThrows_doesNotPropagateToCaller() {
        ScoreMessenger messenger = new ScoreMessenger();
        messenger.addObserver(event -> {
            throw new RuntimeException("Failing observer");
        });

        assertThatCode(() -> messenger.notifyObservers(new SoftDropEvent(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void notifyObservers_whenFirstObserverThrows_stillNotifiesSubsequentObservers() {
        ScoreMessenger messenger = new ScoreMessenger();
        AtomicBoolean secondObserverCalled = new AtomicBoolean(false);

        messenger.addObserver(event -> {
            throw new RuntimeException("Failing observer 1");
        });
        messenger.addObserver(event -> secondObserverCalled.set(true));

        messenger.notifyObservers(new SoftDropEvent(1));

        assertThat(secondObserverCalled).isTrue();
    }
}
