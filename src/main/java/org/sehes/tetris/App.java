package org.sehes.tetris;

import org.sehes.tetris.common.GameState;
import org.sehes.tetris.common.event.Observable;
import org.sehes.tetris.common.event.Observer;
import org.sehes.tetris.controller.GameLoop;
import org.sehes.tetris.controller.GameManager;
import org.sehes.tetris.controller.GameStateManager;
import org.sehes.tetris.controller.SwingTimerGameLoop;
import org.sehes.tetris.controller.input.*;
import org.sehes.tetris.model.GameBoard;
import org.sehes.tetris.model.GameSnapshot;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.generator.PieceGenerator;
import org.sehes.tetris.model.generator.SevenBagGenerator;
import org.sehes.tetris.model.level.LevelManager;
import org.sehes.tetris.model.score.ScoreManager;
import org.sehes.tetris.model.score.ScoreMessenger;
import org.sehes.tetris.view.render.AssetsManager;
import org.sehes.tetris.view.render.PreviewDrawingHandler;
import org.sehes.tetris.view.render.RenderingHintsFactory;
import org.sehes.tetris.view.render.TetrisDrawingHandler;
import org.sehes.tetris.view.swing.GuiFactory;

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
    private final PieceGenerator pieceGenerator = new SevenBagGenerator();
    private final GameLoop gameLoop = new SwingTimerGameLoop();
    private final RenderingHints qualityRenderingHints = RenderingHintsFactory.qualityRenderingHints();
    private final AssetsManager assetsManager = new AssetsManager(qualityRenderingHints);
    private final Painter<GameSnapshot> tetrisPainter = new TetrisDrawingHandler(qualityRenderingHints, assetsManager);
    private final Painter<TetrominoType> previewPainter = new PreviewDrawingHandler(qualityRenderingHints, assetsManager);
    private final LevelManager levelManager = new LevelManager();

    public App() {

    }

    public void run() {
        final var gui = GuiFactory.assembly(tetrisPainter, previewPainter);
        final GameManager gameManager = new GameManager(stateManager, scoreMessenger, pieceGenerator, gameLoop, gui.canvas(), levelManager, GameBoard::new);
        final InputRouter inputReceiver = new InputRouterImpl(inputMapper, gameManager, gui.exitAction());
        final KeyAdapter tetrisKeyAdapter = new TetrisKeyAdapter(inputReceiver);
        GuiFactory.addKeyListener(tetrisKeyAdapter, gui);
        addObserver(stateManager.gameStateObservable(), gui.infoObserver());
        addObserver(gameLoop.fpsObservable(), gui.fpsObserver());
        addObserver(gameLoop.tickObservable(), gameManager.tickObserver());
        addObserver(scoreMessenger, scoreManager.scoringObserver());
        addObserver(stateManager.gameStateObservable(), scoreManager.gameStateObserver());
        addObserver(scoreManager.scoreInfoObservable(), gui.scoreObserver());
        addObserver(gameManager.spawnObservable(), gui.previewObserver());
        addObserver(gameManager.holdObservable(), gui.holdCanvasObserver());
        addObserver(levelManager.levelObservable(), gui.levelObserver());
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


