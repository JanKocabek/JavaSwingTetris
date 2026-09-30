package org.sehes.tetris;

import org.sehes.tetris.controller.*;
import org.sehes.tetris.controller.input.*;
import org.sehes.tetris.graphic.AssetsManager;
import org.sehes.tetris.graphic.PreviewDrawingHandler;
import org.sehes.tetris.graphic.RenderingHintsFactory;
import org.sehes.tetris.graphic.TetrisDrawingHandler;
import org.sehes.tetris.gui.GuiFactory;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.RandomPieceGenerator;
import org.sehes.tetris.model.TetrominoType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.util.logging.Logger;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private final GameStateManager stateManager = new GameStateManager(GameState.INIT);
    private final InputMapper inputMapper = new InputMapper(KeyMap.createDefault());
    private final ScoreManager scoreManager = new ScoreManager();
    private final ScoreMessenger scoreMessenger = new ScoreMessenger();
    private final PieceGenerator randomPieceGenerator = new RandomPieceGenerator();
    private final GameLoop gameLoop = new SwingTimerGameLoop();
    private final RenderingHints qualityRenderingHints = RenderingHintsFactory.qualityRenderingHints();
    private final AssetsManager assetsManager = new AssetsManager(qualityRenderingHints);
    private final Painter<GameSnapshot> tetrisPainter = new TetrisDrawingHandler(qualityRenderingHints, assetsManager);
    private final Painter<TetrominoType> previewPainter = new PreviewDrawingHandler(qualityRenderingHints, assetsManager);

    public App() {

    }

    public void run() {
        final var gui = GuiFactory.assembly(tetrisPainter, previewPainter);
        final GameManager gameManager = new GameManager(stateManager, scoreMessenger, randomPieceGenerator, gameLoop, gui.canvas());
        final InputRouter inputReceiver = new InputRouterImpl(inputMapper, gameManager, gui.exitAction());
        final KeyAdapter tetrisKeyAdapter = new TetrisKeyAdapter(inputReceiver);
        GuiFactory.addKeyListener(tetrisKeyAdapter, gui);
        addObserver(stateManager.gameStateObservable(), gui.infoObserver());
        addObserver(gameLoop.fpsObservable(), gui.fpsObserver());
        addObserver(gameLoop.tickObservable(), gameManager.tickObserver());
        addObserver(scoreMessenger, scoreManager.scoringObserver());
        addObserver(stateManager.gameStateObservable(), scoreManager.gameStateObserver());
        addObserver(scoreManager.ScoreInfoObservable(), gui.scoreObserver());
        addObserver(gameManager.spawnObservable(), gui.previewObserver());
        addObserver(gameManager.holdObservable(), gui.holdCanvasObserver());
        gui.window().setVisible(true);
        gui.canvas().requestFocusInWindow();
        stateManager.setState(GameState.PREPARED);
    }

    /**
     * Adds an observer to the observable and returns a closeable that removes the observer when closed.
     * the closeable is for the future when Gui part will be closed before the application
     *
     * @param sender   the object who is sending the messages
     * @param receiver the object who needs receiving the messages
     * @param <T>      the type of the messages which will be sent
     * @return a closeable that removes the observer when closed
     */
    private <T> AutoCloseable addObserver(Observable<T> sender, Observer<T> receiver) {
        sender.addObserver(receiver);
        return () -> sender.removeObserver(receiver);
    }
}


