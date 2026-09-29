package org.sehes.tetris.controller.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.awt.event.KeyEvent.*;
import static java.util.Map.entry;
import static org.sehes.tetris.controller.input.InputAction.*;

public class KeyMap implements KeyRebinding {
    /**
     * Default non-modifiable key mapping for the game
     *
     * @see InputAction
     * @see java.awt.event.KeyEvent
     *
     */
    private static final Map<Integer, InputAction> DEFAULT_KEY_MAP = Map.ofEntries(
            entry(VK_ESCAPE, CANCEL),
            entry(VK_ENTER, CONFIRM),
            entry(VK_UP, ROTATE_CW),
            entry(VK_A, ROTATE_CCW),
            entry(VK_SPACE, HARD_DROP),
            entry(VK_DOWN, MOVE_DOWN),
            entry(VK_LEFT, MOVE_LEFT),
            entry(VK_RIGHT, MOVE_RIGHT),
            entry(VK_V, TOGGLE_GHOST),
            entry(VK_SHIFT, HOLD),
            entry(VK_F1, RESTART));


    private final Map<Integer, InputAction> map;

    public KeyMap(Map<Integer, InputAction> map) {
        this.map = map;
    }

    public static KeyMap createDefault() {
        return new KeyMap(new HashMap<>(DEFAULT_KEY_MAP));
    }

    public Optional<InputAction> getAction(KeyDTO key) {
        final var action = map.get(key.keyCode());
        return Optional.ofNullable(action).filter(a -> a.triggersOnPress() == key.isPressed());
    }

    //todo: create check for warning when key rebind unbind action
    @Override
    public boolean keyRebind(int key, InputAction action) {
        map.put(key, action);
        return true;
    }

    @Override
    public void resetKeyBindings() {
        map.clear();
        map.putAll(DEFAULT_KEY_MAP);
    }

}
