package org.sehes.tetris.view.swing;

import javax.swing.*;

public class GameWindow extends JFrame {

    /**
     * Constructs a new GameWindow. The window is set up with a specified content pane for better organization of components, and it initializes the Tetris canvas and score panel. The window is configured to close the application when the user initiates a close action. This constructor allows for a flexible setup of the game window, enabling the integration of various components while maintaining a clear separation of concerns between the game display and the user interface elements.
     */
    GameWindow(JPanel contentPane) {
        super("Tetris");
        setContentPane(contentPane);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
