package org.sehes.tetris.controller.input;

import org.sehes.tetris.controller.InputHandler;

public class InputRouterImpl implements InputRouter {

    private final InputMapper inputMapper;
    private final InputHandler inputHandler;
    private final Runnable exitAction;


    public InputRouterImpl(InputMapper inputMapper, InputHandler inputHandler, Runnable exitAction) {
        this.inputMapper = inputMapper;
        this.inputHandler = inputHandler;
        this.exitAction = exitAction;
    }


    /**
     * Handles the input forward to the InputHandler if key is mapped in mapper, except EXIT invokes the exit callback
     *
     * @param key the key taken as DTO object containing the key code and the edge on which it was fired
     *
     */
    @Override
    public void handleInput(KeyDTO key) {
        inputMapper.getAction(key).ifPresent(action -> {
            if(action == InputAction.EXIT)
                exitAction.run();
                else {
                inputHandler.handleInput(action);
            }
        });
    }


}
