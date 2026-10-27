package org.sehes.tetris.controller;

import org.sehes.tetris.common.GameState;
import org.sehes.tetris.common.event.Observable;
import org.sehes.tetris.common.event.ObservableImpl;
import org.sehes.tetris.common.event.Observer;
import org.sehes.tetris.controller.input.InputAction;
import org.sehes.tetris.model.*;
import org.sehes.tetris.model.level.LevelManager;
import org.sehes.tetris.model.score.*;
import org.sehes.tetris.view.Rendering;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.sehes.tetris.common.GameState.*;
import static org.sehes.tetris.common.Utils.NANOS_PER_SECOND;

public class GameManager implements InputHandler {
    // 1. CONSTANTS
    // =========================================================================
    private static final Logger LOGGER = Logger.getLogger(GameManager.class.getName());
    // 2. INFRASTRUCTURE & SERVICES
    // =========================================================================
    private final StateManager<GameState> stateManager;
    private final PieceGenerator generator;
    private final ScoreMessenger scoreMessenger;
    private final GameLoop gameLoop;
    private final Rendering tetrisCanvas;
    private final LockDelay lockDelay = new LockDelay();
    private final LevelManager levelManager;
    private final Supplier<GameBoard> gameBoardSupplier;
    // =========================================================================
    // 3. OBSERVABLES & OBSERVERS
    // =========================================================================
    private final Observable.Publisher<TetrominoType> spawnObservable = new ObservableImpl<>();
    private final Observable.Publisher<TetrominoType> holdObservable = new ObservableImpl<>();
    private final Observer<Long> tickObserver = this::onTickUpdate;

    // =========================================================================
    // 4. GAME MODEL & PIECE STATE
    // =========================================================================
    private GameBoard gameBoard;
    private GhostType ghostType = GhostType.FULL;
    private TetrominoType holdTetromino = null;
    private boolean isHoldLock = false;

    // =========================================================================
    // 5. TIMING, PHYSICS & RENDER STATE
    // =========================================================================
    private final AtomicBoolean isDirty = new AtomicBoolean(false);
    private long gravityAccumulator;
    // =========================================================================
    // PUBLIC INTERFACE (CONSTRUCTOR & PUBLIC METHODS)
    // =========================================================================

    public GameManager(StateManager<GameState> stateManager, ScoreMessenger scoreMessenger, PieceGenerator generator, GameLoop loop, Rendering render, LevelManager levelManager, Supplier<GameBoard> gameBoardSupplier) {
        this.generator = generator;
        this.stateManager = stateManager;
        this.scoreMessenger = scoreMessenger;
        this.gameLoop = loop;
        this.tetrisCanvas = render;
        this.levelManager = levelManager;
        this.gameBoardSupplier = gameBoardSupplier;
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

    public Observer<Long> tickObserver() {
        return tickObserver;
    }

    public Observable<TetrominoType> spawnObservable() {
        return spawnObservable;
    }

    public Observable<TetrominoType> holdObservable() {
        return holdObservable;
    }

    // =========================================================================
    // PRIVATE METHODS: 1. INPUT ROUTING
    // =========================================================================

    private void runningGameInput(InputAction action) {
        switch (action) {
            case CONFIRM -> pauseGame();
            case MOVE_DOWN -> softDrop();
            case HARD_DROP -> hardDrop();
            case MOVE_LEFT -> movePiece(DirectionFlag.LEFT);
            case MOVE_RIGHT -> movePiece(DirectionFlag.RIGHT);
            case ROTATE_CW -> rotatePiece(RotationFlag.CLOCKWISE);
            case ROTATE_CCW -> rotatePiece(RotationFlag.COUNTER_CLOCKWISE);
            case TOGGLE_GHOST -> toggleGhostPiece();
            case HOLD -> holdOrSwap();
            case RESTART -> restartGame();
            default -> {
                break;
            }
        }
    }

    private void restartGame() {
        newGame();
        gameLoop.restart();
    }

    private void preparedInput(InputAction action) {
        if ((action) == InputAction.CONFIRM) startGame();
    }

    private void pauseGameInput(InputAction action) {
        switch (action) {
            case CONFIRM -> resumeGame();
            case RESTART -> restartGame();
            default -> {
                break;
            }
        }
    }

    private void gameOverInput(InputAction action) {
        if (action == InputAction.CONFIRM) {
            startGame();
        }
    }

    // =========================================================================
    // PRIVATE METHODS: 2. PIECE MOVEMENT & ACTIONS
    // =========================================================================

    private void movePiece(final DirectionFlag direction) {
        if (gameBoard.tryMovePiece(direction)) {
            lockDelay.checkMove(getCurrentTetromino().getPositionY(), gameBoard.isMinoGrounded());
            render();
        }
    }

    private void rotatePiece(final RotationFlag rotate) {
        if (gameBoard.tryRotatePiece(rotate)) {
            lockDelay.checkMove(getCurrentTetromino().getPositionY(), gameBoard.isMinoGrounded());
            render();
        }
    }

    private void softDrop() {
        if (gameBoard.trySoftDrop()) {
            scoreMessenger.notifyObservers(new SoftDropEvent(1));
            lockDelay.checkDrop(getCurrentTetromino().getPositionY(), gameBoard.isMinoGrounded());
            render();
        }
    }

    private void hardDrop() {
        final var distance = gameBoard.tryHardDrop();
        if (distance > 0) {
            scoreMessenger.notifyObservers(new HardDropEvent(distance));
            render();
        }
        lockClearAndScorePiece();

    }

    private void holdOrSwap() {
        if (!isHoldLock) {
            TetrominoType currentType = getCurrentTetromino().getType();
            TetrominoType previousHold = holdTetromino;
            setAndNotifyHoldMino(currentType);
            isHoldLock = true;

            boolean spawnSuccessful = (previousHold == null) ? trySpawnNewTetromino() : trySpawnMino(previousHold);

            if (!spawnSuccessful) {
                setGameOver();
            }

            render();
        }
    }

    private void toggleGhostPiece() {
        ghostType = ghostType.next();
        render();
    }

    // =========================================================================
    // PRIVATE METHODS: 3. PIECE LIFECYCLE & SOLIDIFICATION
    // =========================================================================

    private boolean trySpawnNewTetromino() {
        final var piece = generator.getNextPiece();
        if (!trySpawnMino(piece)) return false;
        spawnObservable.notify(generator.peekNext());
        return true;
    }

    private boolean trySpawnMino(TetrominoType minoType) {
        if (!gameBoard.trySetNewTetromino(minoType)) return false;
        lockDelay.setFor(getCurrentTetromino());
        resetGravityAcc();
        isDirty.set(true);
        return true;
    }

    private void setAndNotifyHoldMino(TetrominoType currentType) {
        holdTetromino = currentType;
        holdObservable.notify(holdTetromino);
    }

    //TODO: simplified output of related GameBoard methods make it direct return
    private void lockClearAndScorePiece() {
        gameBoard.lockTetrominoInPlace();
        gameBoard.tryClearLines();
        final var lastAction = gameBoard.getLastAction();
        try {
            notifyLockingEvent(lastAction.tSpin(), lastAction.linesCleared(), levelManager.getLevel());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to notify score observer on lock piece", e);
        }
        if (lastAction.linesCleared() > 0) {
            levelManager.onCleanedRows(lastAction.linesCleared());
        }
        if (!trySpawnNewTetromino()) {
            setGameOver();
        }
        isHoldLock = false;
    }


    private void notifyLockingEvent(TSpin tSpin, int linesCleared, int level) {
        final LockPieceEvent lockEvent = createLockEvent(tSpin, linesCleared, level);
        scoreMessenger.notifyObservers(lockEvent);
    }

    private LockPieceEvent createLockEvent(final TSpin tSpin, int clearedLines, int level) {
        return new LockPieceEvent(clearedLines, tSpin, level);
    }

    // =========================================================================
    // PRIVATE METHODS: 4. GAME LOOP & GRAVITY PHYSICS & LOCK DELAY
    // =========================================================================

    private void onTickUpdate(Long elapsedTime) {
        if (stateManager.getState() == NEW_GAME || stateManager.getState() == PLAYING) {
            gravityUpdate(elapsedTime);
            lockDelayUpdate(elapsedTime);
            render();
        }
    }

    private void lockDelayUpdate(Long elapsedTime) {
        if (lockDelay.isOn() && lockDelay.onTick(elapsedTime)) {
            lockClearAndScorePiece();
        }
    }

    private void gravityUpdate(Long elapsedTime) {
        gravityAccumulator += elapsedTime;
        final var gravityInterval=levelManager.getGravityNanos();
        while (gravityAccumulator >= gravityInterval) {
            if (gameBoard.tryGravityMove()) {
                gravityAccumulator -= gravityInterval;
                lockDelay.checkDrop(getCurrentTetromino().getPositionY(), gameBoard.isMinoGrounded());
            } else {
                gravityAccumulator = 0;
                lockDelay.setLockModeOn();
                break;
            }
        }
    }

    private void resetGravityAcc() {
        gravityAccumulator = 0;
    }

    // =========================================================================
    // PRIVATE METHODS: 5. GAME LIFECYCLE & STATE CONTROL
    // =========================================================================

    private void startGame() {
        newGame();
        gameLoop.start();
    }

    private void newGame() {
        stateManager.setState(NEW_GAME);
        levelManager.set();
        setAndNotifyHoldMino(null);
        gameBoard = gameBoardSupplier.get();
        generator.startNewMinoQueue();
        //TODO: unlock this when tehre will be implemented time delay to show first piece before game start
        // spawnObservable.notify(generator.peekNext());
        trySpawnNewTetromino();
        isHoldLock = false;
        render();
        stateManager.setState(PLAYING);
    }

    private void pauseGame() {
        stateManager.setState(PAUSED);
    }

    private void resumeGame() {
        resetGravityAcc();
        gameLoop.resume();
        stateManager.setState(PLAYING);
    }

    private void setGameOver() {
        gameLoop.stop();
        stateManager.setState(GAME_OVER);
    }

    // =========================================================================
    // PRIVATE METHODS: 6. RENDERING & SNAPSHOT HELPERS
    // =========================================================================

    private void render() {
        tetrisCanvas.render(createGameSnapshot());
    }

    private GameSnapshot createGameSnapshot() {
        final var wasDirty = isDirty.getAndSet(false);
        Tetromino current = getCurrentTetromino();
        double lockTimerInSec = lockDelay.lockDelayElapsedInNANO() / NANOS_PER_SECOND;
        final Double lockTimer = lockDelay.isOn() ? lockTimerInSec : null;
        return new GameSnapshot(getBoardView(), Optional.ofNullable(current), wasDirty, current == null ? 0 : gameBoard.calculateDropDistance(), current == null ? GhostType.NONE : ghostType, lockTimer);
    }

    private BoardView getBoardView() {
        return gameBoard.getBoardView();
    }

    private Tetromino getCurrentTetromino() {
        return gameBoard.getCurrentTetromino();
    }
}