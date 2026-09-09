package org.sehes.tetris;

import org.sehes.tetris.controller.GameLoop;
import org.sehes.tetris.controller.GameManager;
import org.sehes.tetris.controller.GameState;
import org.sehes.tetris.controller.GameStateManager;
import org.sehes.tetris.controller.Observable;
import org.sehes.tetris.controller.Observer;
import org.sehes.tetris.controller.ScoreManager;
import org.sehes.tetris.controller.ScoreMessenger;
import org.sehes.tetris.controller.SwingTimerGameLoop;
import org.sehes.tetris.controller.input.InputMapper;
import org.sehes.tetris.controller.input.InputReceiver;
import org.sehes.tetris.controller.input.InputRouter;
import org.sehes.tetris.controller.input.KeyMap;
import org.sehes.tetris.controller.input.TetrisKeyAdapter;
import org.sehes.tetris.graphic.AssetsManager;
import org.sehes.tetris.graphic.PreviewDrawingHandler;
import org.sehes.tetris.graphic.RenderingHintsFactory;
import org.sehes.tetris.graphic.TetrisDrawingHandler;
import org.sehes.tetris.gui.GuiFactory;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.RandomPieceGenerator;

import java.awt.RenderingHints;
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
    private final GameManager gameManager = new GameManager(stateManager, scoreMessenger, randomPieceGenerator,gameLoop);
    private final InputReceiver inputRouter = new InputRouter(inputMapper, gameManager);
    private final KeyAdapter tetrisKeyAdapter = new TetrisKeyAdapter(inputRouter);

    public void run() {
        final RenderingHints qualityRenderingHints = RenderingHintsFactory.qualityRenderingHints();
        final var assetsManager = new AssetsManager(qualityRenderingHints);
        final var tetrisPainter = new TetrisDrawingHandler(qualityRenderingHints, assetsManager);
        final var previewPainter = new PreviewDrawingHandler(qualityRenderingHints, assetsManager);
        final GuiFactory.gameUI gui = GuiFactory.assembly(tetrisPainter, tetrisKeyAdapter, previewPainter);
        addObserver(stateManager.GameStateObservable(), gui.infoObserver());
        addObserver(gameLoop.fpsObservable(), gui.fpsObserver());
        addObserver(gameLoop.tickObservable(),gameManager.tickObserver());
        addObserver(scoreMessenger, scoreManager.scoringObserver());
        addObserver(stateManager.GameStateObservable(), scoreManager.gameStateObserver());
        addObserver(scoreManager.ScoreInfoObservable(), gui.scoreObserver());
        addObserver(gameManager.spawnObservable(), gui.previewObserver());
        addObserver(gameManager.holdObservable(), gui.holdCanvasObserver());

        gameManager.prepareGame(gui.canvas(), gui.exitAction());
        gui.window().setVisible(true);
        gui.canvas().requestFocusInWindow();
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


