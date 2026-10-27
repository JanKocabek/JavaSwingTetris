package org.sehes.tetris.view.swing;

import javax.swing.*;
import java.awt.*;

public class GameContainer extends JPanel {

    GameContainer(LayoutManager2 lm) {
        super();

        setOpaque(true);
        setLayout(lm);

    }
}
