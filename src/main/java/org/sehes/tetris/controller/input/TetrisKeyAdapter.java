package org.sehes.tetris.controller.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The TetrisKeyInputHandler class is Adapter pattern implementation for handling keyboard input.
 * Its Separate GUI implementation part from the implementation independent logic.
 *
 * @author Sehes
 * @version 1.0
 */
public class TetrisKeyAdapter extends KeyAdapter {

    private final InputRouter receiver;

    /**
     * Constructor for the TetrisKeyInputHandler class.
     *
     * @param receiver The class responsible for transferring the input to the rest of the application
     */
    public TetrisKeyAdapter(InputRouter receiver) {
        this.receiver = receiver;
    }

    /**
     * react when a key is pressed down and create
     * {@link   KeyDTO} object carrying the key code and the fact that it was pressed down into application
     *
     * @param e The KeyEvent object containing information about the key that was pressed.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        receiver.handleInput(new KeyDTO(e.getKeyCode(), true));
    }

    /**
     * react when a key is released and create a {@link KeyDTO} object carrying the key code and the fact that it was released into application
     *
     * @param e The KeyEvent object containing information about the key that was released.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        receiver.handleInput(new KeyDTO(e.getKeyCode(), false));
    }

}

