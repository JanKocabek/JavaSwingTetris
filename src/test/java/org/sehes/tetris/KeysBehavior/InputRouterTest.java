package org.sehes.tetris.KeysBehavior;

import org.junit.jupiter.api.Test;
import org.sehes.tetris.controller.InputHandler;
import org.sehes.tetris.controller.input.*;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InputRouterTest {

    @Test
    void testHandleInput_RightKey_onRightEdge() {
        //arrange
        final var expectedOutput = InputAction.CONFIRM;
        final var keyData = new HashMap<Integer, InputAction>();
        keyData.put(KeyEvent.VK_ENTER, expectedOutput);
        KeyMap keyMap = new KeyMap(keyData);
        InputMapper mapper = new InputMapper(keyMap);
        List<InputAction> receivedActions = new ArrayList<>();
        InputHandler handler = receivedActions::add;
        InputRouter inputRouter = new InputRouterImpl(mapper, handler,null);
        //act
        inputRouter.handleInput(new KeyDTO(KeyEvent.VK_ENTER, true));
        //assert
        assertThat((receivedActions)).containsExactly(expectedOutput);
    }

    @Test
    void testHandleInput_onOppositeEdge() {
        //arrange
        final var expectedOutput = InputAction.ROTATE_CW;
        final var keyData = new HashMap<Integer, InputAction>();
        keyData.put(KeyEvent.VK_ENTER, expectedOutput);
        KeyMap keyMap = new KeyMap(keyData);
        InputMapper mapper = new InputMapper(keyMap);
        List<InputAction> receivedActions = new ArrayList<>();
        InputHandler handler = receivedActions::add;
        InputRouter inputRouter = new InputRouterImpl(mapper, handler,null);
        //act
        inputRouter.handleInput(new KeyDTO(KeyEvent.VK_ENTER, true));
        //assert
        assertThat(receivedActions).isEmpty();
    }

    @Test
    void testHandleInput_onNotMappedKey() {
        //arrange
        final var expectedOutput = InputAction.ROTATE_CW;
        final var keyData = new HashMap<Integer, InputAction>();
        keyData.put(KeyEvent.VK_ENTER, expectedOutput);
        KeyMap keyMap = new KeyMap(keyData);
        InputMapper mapper = new InputMapper(keyMap);
        List<InputAction> receivedActions = new ArrayList<>();
        InputHandler handler = receivedActions::add;
        InputRouter inputRouter = new InputRouterImpl(mapper, handler,null);
        //act
        inputRouter.handleInput(new KeyDTO(KeyEvent.VK_A, false));
        //assert
        assertThat(receivedActions).isEmpty();
    }

}
