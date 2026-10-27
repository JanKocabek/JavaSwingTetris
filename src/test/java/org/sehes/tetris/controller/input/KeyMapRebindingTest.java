package org.sehes.tetris.controller.input;

import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class KeyMapRebindingTest {
    @Test
    void testKeyRebind() {
        //arrange
        final var defaultKey = KeyEvent.VK_ENTER;
        final var defaultKeyMap = new HashMap<Integer, InputAction>();
        final var action = InputAction.CONFIRM;
        defaultKeyMap.put(defaultKey, action);
        final var keyMap = new KeyMap(defaultKeyMap);
        //act
        final var remappedKey = KeyEvent.VK_Y;
        keyMap.keyRebind(remappedKey, action);
        //assert
        assertThat(keyMap.getAction(new KeyDTO(remappedKey, true))).isPresent().get().isEqualTo(action);
    }


    //todo: cretae parametrized test in future
    @Test
    void testResetKeyBindingsRestoresDefaults() {
        // arrange
        KeyMap keyMap = KeyMap.createDefault();

        // act: rebind a key
        final int remappedKey = KeyEvent.VK_Y;
        final InputAction remappedAction = InputAction.CONFIRM;
        keyMap.keyRebind(remappedKey, remappedAction);

        // verify rebind took effect
        assertThat(keyMap.getAction(new KeyDTO(remappedKey, true)))
                .isPresent()
                .contains(remappedAction);

        // act: reset
        keyMap.resetKeyBindings();

        // assert: default key restored
        assertThat(keyMap.getAction(new KeyDTO(KeyEvent.VK_ENTER, true)))
                .isPresent()
                .contains(InputAction.CONFIRM);

        // assert: remapped key removed
        assertThat(keyMap.getAction(new KeyDTO(remappedKey, true)))
                .isNotPresent();
    }


}
