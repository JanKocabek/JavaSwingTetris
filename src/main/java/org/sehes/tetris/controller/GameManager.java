package org.sehes.tetris.controller;

import org.sehes.tetris.config.GhostType;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.BoardView;
import org.sehes.tetris.model.DirectionFlag;
import org.sehes.tetris.model.GameBoard;
import org.sehes.tetris.model.PieceGenerator;
import org.sehes.tetris.model.RotationFlag;
import org.sehes.tetris.model.Tetromino;
import org.sehes.tetris.model.TetrominoType;
import org.sehes.tetris.model.score.HardDropEvent;
import org.sehes.tetris.model.score.LockPieceEvent;
import org.sehes.tetris.model.score.SoftDropEvent;
import org.sehes.tetris.model.score.TSpin;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.sehes.tetris.controller.GameState.GAME_OVER;
import static org.sehes.tetris.controller.GameState.INIT;
import static org.sehes.tetris.controller.GameState.NEW_GAME;
import static org.sehes.tetris.controller.GameState.PAUSED;
import static org.sehes.tetris.controller.GameState.PLAYING;
import static org.sehes.tetris.controller.GameState.PREPARED;


/**
 * The GameManager class is responsible for managing the overall game state,
 * handling user input, and coordinating the game loop. It initializes the game
 * components, starts the game loop, and provides methods for moving and
 * rotating pieces, as well as pausing and resuming the game.
 */
public class GameManager implements InputHandler {
    private final Observable.Publisher<TetrominoType> spawnObservable = new ObservableImpl<>();
    private final Observable.Publisher<TetrominoType> holdObservable = new ObservableImpl<>();
    private final Observer<Long> tickObserver = this::onTickUpdate;
    private final StateManager<GameState> stateManager;
    private final PieceGenerator generator;
    private final AtomicBoolean isDirty = new AtomicBoolean(false); // is full redraw needed?
    private final ScoreMessenger scoreMessenger;
    private Rendering tetrisCanvas; // Reference to the canvas for repainting
    private GameBoard gameBoard; // reference to the game board for managing game logic
    private Runnable gameExit = () -> System.exit(0);
    private GhostType ghostType;
    private TetrominoType holdTetromino;
    private boolean isHoldLock = false;
    private static final int BASE_SPEED = 600;
    //loop Variables
    private final long movementSpeed = TimeUnit.MILLISECONDS.toNanos(BASE_SPEED);
    private long gravityAccumulator;
    private final GameLoop gameLoop;

    public GameManager(StateManager<GameState> stateManager, ScoreMessenger scoreMessenger, PieceGenerator generator, GameLoop loop) {
        this.generator = generator;
        this.ghostType = GhostType.FULL;
        this.stateManager = stateManager;
        this.scoreMessenger = scoreMessenger;
        this.holdTetromino = null;
        this.gameLoop = loop;
    }

    public Observer<Long> tickObserver() {
        return tickObserver;
    }

    /**
     * initialize the GameManger by wiring the main canvas and exitGameCallBack
     */
    public void prepareGame(Rendering canvas, Runnable exitAction) {
        if (stateManager.getState() == INIT) {
            this.tetrisCanvas = canvas;
            gameExit = exitAction;
            stateManager.setState(PREPARED);
        }
    }

    @Override
    public void handleInput(InputAction action) {
        switch (stateManager.getState()) {
            case PREPARED -> preparedInput(action);
            case PLAYING -> runningGameInput(action);
            case PAUSED -> pauseGameInput(action);
            case GAME_OVER -> gameOverInput(action);
            default -> {
                break;
            }

        }

    }

    public Observable<TetrominoType> spawnObservable() {
        return spawnObservable;
    }

    public Observable<TetrominoType> holdObservable() {
        return holdObservable;
    }

    private void onTickUpdate(Long elapsedTime) {
        if (stateManager.getState() == NEW_GAME || stateManager.getState() == PLAYING) {
            gravityUpdate(elapsedTime);
            render();
        }
    }

    private void gravityUpdate(Long elapsedTime) {
        gravityAccumulator += elapsedTime;
        while (gravityAccumulator >= movementSpeed) {
            if (!gameBoard.tryGravityMove()) {
                //todo: this will in future update here replaced by delayLock mechanism (soft drop) after GameLoop is made own class and Gui/swing-agnostic!
                lockClearAndScorePiece();
                isDirty.set(true);
                break;//break the while loop
            }
            gravityAccumulator -= movementSpeed;
        }
    }

    private BoardView getBoardView() {
        return gameBoard.getBoardView();
    }

    private Tetromino getCurrentTetromino() {
        return gameBoard.getCurrentTetromino();
    }

    private void gameOverInput(InputAction action) {
        switch (action) {
            case CONFIRM -> startGame();
            case CANCEL -> exitGame();
            default -> {
                break;
            }
        }
    }

    private void pauseGameInput(InputAction action) {
        switch (action) {
            case CANCEL -> exitGame();
            case CONFIRM -> resumeGame();
            default -> {
                break;
            }
        }
    }

    private void runningGameInput(InputAction action) {
        switch (action) {
            case CANCEL -> exitGame();
            case CONFIRM -> pauseGame();
            case MOVE_DOWN -> softDrop();
            case HARD_DROP -> hardDrop();
            case MOVE_LEFT -> movePiece(DirectionFlag.LEFT);
            case MOVE_RIGHT -> movePiece(DirectionFlag.RIGHT);
            case ROTATE_CW -> rotatePiece(RotationFlag.CLOCKWISE);
            case ROTATE_CCW -> rotatePiece(RotationFlag.COUNTER_CLOCKWISE);
            case TOGGLE_GHOST -> toggleGhostPiece();
            case HOLD -> holdOrSwap();
            default -> {
                break;
            }
        }
    }

    private void holdOrSwap() {
        if (isHoldLock) return;

        TetrominoType currentType = getCurrentTetromino().getType();
        TetrominoType previousHold = holdTetromino;
        setHoldAndNotify(currentType);
        isHoldLock = true;

        boolean spawnSuccessful = (previousHold == null) ? trySpawnNewTetromino() : trySpawnMino(previousHold);

        if (!spawnSuccessful) {
            setGameOver();
        }
        render();
    }

    private boolean trySpawnMino(TetrominoType previousHold) {
        return gameBoard.trySetNewTetromino(previousHold);
    }


    private void setHoldAndNotify(TetrominoType currentType) {
        holdTetromino = currentType;
        holdObservable.notify(holdTetromino);
    }

    /**
     * call new repainting on mainCanvas
     */
    private void render() {
        tetrisCanvas.render(createGameSnapshot());
    }

    private void toggleGhostPiece() {
        ghostType = ghostType.next();
        render();
    }

    private void hardDrop() {
        final var distance = gameBoard.tryHardDrop();
        if (distance > 0) {
            scoreMessenger.notifyObservers(new HardDropEvent(distance));
            render();
        }
        lockClearAndScorePiece();
        isDirty.set(true);
    }

    private void preparedInput(InputAction action) {
        switch (action) {
            case CONFIRM -> startGame();
            case CANCEL -> exitGame();
            default -> {
                break;
            }
        }
    }

    /**
     * Try to move the current piece in the specified direction. If the move is
     * successful, repaint the canvas to reflect the new position of the piece.
     *
     * @param direction The direction to move the piece (e.g., LEFT, RIGHT,
     *                  DOWN).
     */
    private void movePiece(final DirectionFlag direction) {
        if (gameBoard.tryMovePiece(direction)) {
            render();
        }
    }

    private void softDrop() {
        if (gameBoard.trySoftDrop()) {
            scoreMessenger.notifyObservers(new SoftDropEvent(1));
            render();
        }
    }

    /**
     * Try to rotate the current piece in the specified direction. If the
     * rotation is successful, repaint the canvas to reflect the new orientation
     * of the piece.
     *
     * @param rotate The direction to rotate the piece (CLOCKWISE, COUN
     */
    private void rotatePiece(final RotationFlag rotate) {
        if (gameBoard.tryRotatePiece(rotate)) {
            render();
        }
    }

    /**
     * Starts the game by resetting the game board and starting the game loop
     * timer. Sets the game state to PLAYING.
     */
    private void startGame() {
        GameState state = stateManager.getState();// Do nothing
        if (state == PREPARED || state == GAME_OVER) {
            newGame();
            gameLoop.start();
        }
    }

    private void newGame() {
        setHoldAndNotify(null);
        isHoldLock = false;
        stateManager.setState(NEW_GAME);
        isDirty.set(true);
        gameBoard = new GameBoard();
        spawnObservable.notify(generator.peekNext());
        if (spawnMinoOrGameOver()) {
            render();
            resetAccumulator();
            stateManager.setState(PLAYING);
        }
    }

    private void pauseGame() {
        //  gameLoop.stop();
        stateManager.setState(PAUSED);
    }

    private void resumeGame() {
        resetAccumulator();
        gameLoop.resume();
        stateManager.setState(PLAYING);
    }

    private void resetAccumulator() {
        gravityAccumulator = 0;
    }

    private void setGameOver() {
        gameLoop.stop();
        stateManager.setState(GAME_OVER);
    }

    private void exitGame() {
        gameExit.run();
    }

    private GameSnapshot createGameSnapshot() {
        final var wasDirty = isDirty.getAndSet(false);
        Tetromino current = getCurrentTetromino();
        return new GameSnapshot(getBoardView(), Optional.ofNullable(current), wasDirty, current == null ? 0 : gameBoard.calculateDropDistance(), current == null ? GhostType.NONE : ghostType);
    }

    private boolean trySpawnNewTetromino() {
        final var piece = generator.getNextPiece();
        if (trySpawnMino(piece)) {
            spawnObservable.notify(generator.peekNext());
            return true;
        }
        return false;
    }

    private void lockClearAndScorePiece() {
        isHoldLock = false;
        gameBoard.lockTetrominoInPlace();
        gameBoard.clearLines();
        final var lastAction = gameBoard.getLastAction();
        final LockPieceEvent lockEvent = createLockEvent(lastAction.tSpin(), lastAction.linesCleared());
        scoreMessenger.notifyObservers(lockEvent);
        spawnMinoOrGameOver();
        gravityAccumulator = 0;
    }

    private boolean spawnMinoOrGameOver() {
        if (trySpawnNewTetromino()) return true;
        setGameOver();
        return false;
    }

    private LockPieceEvent createLockEvent(final TSpin tSpin, int clearedLines) {
        return new LockPieceEvent(clearedLines, tSpin);
    }
}