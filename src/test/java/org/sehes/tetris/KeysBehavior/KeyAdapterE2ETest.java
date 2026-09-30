package org.sehes.tetris.KeysBehavior;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.controller.input.InputRouter;
import org.sehes.tetris.controller.input.KeyDTO;
import org.sehes.tetris.controller.input.TetrisKeyAdapter;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeyAdapterE2ETest {

    @Mock
    InputRouter inputReceiver;

    @Captor
    ArgumentCaptor<KeyDTO> dtoCaptor;

    @Test
    void testTetrisKeyAdapter_keyPressedDelegatesWithPressedDto() {
        //arrange
        final var keyAdapter = new TetrisKeyAdapter(inputReceiver);
        final var expectedKeyCode = KeyEvent.VK_ENTER;
        final var keyEvent = new KeyEvent(new JButton(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, expectedKeyCode, KeyEvent.CHAR_UNDEFINED);
        //act
        keyAdapter.keyPressed(keyEvent);
        //assert
        verify(inputReceiver).handleInput(dtoCaptor.capture());
        final var keyDTO = dtoCaptor.getValue();
        final var actualKeyCode = keyDTO.keyCode();
        assertThat(actualKeyCode).isEqualTo(expectedKeyCode);
        assertThat(keyDTO.isPressed()).isTrue();
    }

    @Test
    void testTetrisKeyAdapter_keyReleasedDelegatesWithReleasedDto() {
        // arrange
        TetrisKeyAdapter adapter = new TetrisKeyAdapter(inputReceiver);
        int keyCode = KeyEvent.VK_RIGHT;
        KeyEvent releasedEvent = new KeyEvent(
                new JButton(),
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                keyCode,
                KeyEvent.CHAR_UNDEFINED
        );

        // act
        adapter.keyReleased(releasedEvent);

        // assert

        verify(inputReceiver).handleInput(dtoCaptor.capture());
        KeyDTO dto = dtoCaptor.getValue();
        assertThat(keyCode).isEqualTo(dto.keyCode());
        assertThat(dto.isPressed()).isFalse();
    }
}
