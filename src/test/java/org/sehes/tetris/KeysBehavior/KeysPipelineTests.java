package org.sehes.tetris.KeysBehavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sehes.tetris.controller.InputHandler;
import org.sehes.tetris.controller.input.*;

import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import static java.awt.event.KeyEvent.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KeysPipelineTests {
    @Mock
    InputHandler handler;

    @Mock
    Runnable exitAction;

    private static Stream<Arguments> keysProvider() {
        return Stream.of(
                Arguments.of(new KeyDTO(VK_ENTER, true), InputAction.CONFIRM),
                Arguments.of(new KeyDTO(VK_SPACE, false), InputAction.HARD_DROP),
                Arguments.of(new KeyDTO(VK_LEFT, true), InputAction.MOVE_LEFT),
                Arguments.of(new KeyDTO(VK_UP, false), InputAction.ROTATE_CW),
                Arguments.of(new KeyDTO(VK_RIGHT, true), InputAction.MOVE_RIGHT),
                Arguments.of(new KeyDTO(VK_DOWN, true), InputAction.MOVE_DOWN),
                Arguments.of(new KeyDTO(VK_A, false), InputAction.ROTATE_CCW),
                Arguments.of(new KeyDTO(VK_F1, true), InputAction.RESTART));
    }

    private InputRouter inputRouter;

    @BeforeEach
    void setUp() {
        final KeyMap map = KeyMap.createDefault();
        final InputMapper mapper = new InputMapper(map);
        inputRouter = new InputRouterImpl(mapper, handler, exitAction);
    }

    @ParameterizedTest
    @MethodSource("keysProvider")
    void testKeyPipelining_mappedKeysTriggerExpectedAction(KeyDTO key, InputAction action) {
        //act
        inputRouter.handleInput(key);
        //assert
        verify(handler).handleInput(action);
        verifyNoInteractions(exitAction);
    }

    @Test
    void testKeyPipeline_ExitAction() {
        //act
        inputRouter.handleInput(new KeyDTO(VK_ESCAPE, true));
        //assert
        verify(exitAction).run();
        verifyNoInteractions(handler);
    }

    @Test
    void testKeyPipeLine_NotMappedKey() {
        //act
        inputRouter.handleInput(new KeyDTO(KeyEvent.VK_E, true));
        //assert
        verifyNoInteractions(handler);
    }

    @Test
    void testKeyPipelining_wrongEdge() {
        //act
        inputRouter.handleInput(new KeyDTO(KeyEvent.VK_ENTER, false));
        //assert
        verifyNoInteractions(handler);
    }
}