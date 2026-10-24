package org.sehes.tetris.controller.input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class InputMapperTest {

    @Test
    void testGetAction_KeyIsMappedAndOnRightEdge() {
        //arrange
        final var expectedAction = InputAction.CONFIRM;
        final var keyData = new HashMap<Integer, InputAction>();
        keyData.put(KeyEvent.VK_ENTER, expectedAction);
        final var keyMap = new KeyMap(keyData);
        final var key = new KeyDTO(KeyEvent.VK_ENTER, true);
        final var inputMapper = new InputMapper(keyMap);
        //act
        final var action = inputMapper.getAction(key);
        //assert
        assertThat(action).isPresent().get().isEqualTo(expectedAction);
    }

    @Test
    void testGetAction_KeyIsMappedButNotOnRightEdge() {
        //arrange
        final var keyData = new HashMap<Integer, InputAction>();
        keyData.put(KeyEvent.VK_ENTER, InputAction.CONFIRM);
        final var keyMap = new KeyMap(keyData);
        final var key = new KeyDTO(KeyEvent.VK_ENTER, false);
        final var inputMapper = new InputMapper(keyMap);
        //act
        final var action = inputMapper.getAction(key);
        //assert
        assertThat(action).isNotPresent();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGetAction_KeyIsNotMapped_BothEdges(boolean isPressed) {
        //arrange
        final var pressedKey = KeyEvent.VK_Y;
        final var mappedKey = KeyEvent.VK_ENTER;
        final var mappedAction = InputAction.CONFIRM;

        final var keyData = new HashMap<Integer, InputAction>();
        keyData.put(mappedKey, mappedAction);
        final var keyMap = new KeyMap(keyData);
        final var inputMapper = new InputMapper(keyMap);

        final var pressedKeyDTO = new KeyDTO(pressedKey, isPressed);
        //act
        final var action = inputMapper.getAction(pressedKeyDTO);
        //assert
        assertThat(action).isNotPresent();
    }

}