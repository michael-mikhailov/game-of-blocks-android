package com.example.games.blocks.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.example.games.blocks.model.Figure.FIELD_COLOR;
import static com.example.games.blocks.model.Figure.SHOW_NEXT_SURFACE_BACKGROUND_COLOR;

/**
 * Game logic, also game surface updates.
 *
 * <p> Using 'synchronized' on public methods for simplicity.
 */
public class Game {
    // Field dimensions defined in block parts.
    static final int FIELD_WIDTH = 10;
    static final int FIELD_HEIGHT = 20;

    static final int CELL_IS_EMPTY = 0;
    static final int CELL_IS_BORDER = 8;

    private static final int MAX_LEVEL = 9;
    private static final int FIGURES_PER_LEVEL = 40;

    public interface OnScoreChangedListener {
        void onScoreChanged(int score, int lines, int figuresCount);
    }

    public interface OnLevelChangedListener {
        void onLevelChanged(int newLevel);
    }

    public interface OnGameOverListener {
        void onGameOver();
    }

    private SecureRandom randomGenerator;

    private SurfaceHolder gameSurfaceHolder;
    private SurfaceHolder showNextSurfaceHolder;

    private OnScoreChangedListener onScoreChangedListener;
    private OnLevelChangedListener onLevelChangedListener;
    private OnGameOverListener onGameOverListener;

    private Figure currentFigure;
    private Figure nextFigure;

    // Counts of figure types in the current game.
    // 0-element is total count, the rest is indexed by FigureType.
    // TODO: show detailed statistics in UI.
    private final int[] inGameFigureCounts = new int[Figure.FT_MAX + 1];

    private int reducedLinesCount;
    private int gameScore;

    private int startingLevel = 4;
    private int currentLevel;

    // Number of played figures, when the level increases to the next one.
    private int nextLevelFiguresCount;

    private int[][] gameField = new int[FIELD_WIDTH + 2][FIELD_HEIGHT + 2];

    private boolean isInGame = false;
    private boolean isPaused = false;
    private boolean isShowNextFigure = false;

    /**
     * Creates a Game instance, initializes random generator.
     */
    public Game() {
        // Get random generator.
        try {
            randomGenerator = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            randomGenerator = new SecureRandom();
        }
    }

    public synchronized void setGameSurfaceHolder(SurfaceHolder gameSurfaceHolder) {
        this.gameSurfaceHolder = gameSurfaceHolder;
    }

    public synchronized void setShowNextSurfaceHolder(SurfaceHolder showNextSurfaceHolder) {
        this.showNextSurfaceHolder = showNextSurfaceHolder;
        if (showNextSurfaceHolder != null) {
            Canvas canvas = showNextSurfaceHolder.lockCanvas();
            if (canvas != null) {
                Paint paint = new Paint();
                // Fill all the 'show next' canvas with background.
                paint.setColor(SHOW_NEXT_SURFACE_BACKGROUND_COLOR);
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
                if (isShowNextFigure && nextFigure != null) {
                    nextFigure.paintNext(canvas);
                }
                showNextSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void setOnScoreChangedListener(OnScoreChangedListener onScoreChangedListener) {
        this.onScoreChangedListener = onScoreChangedListener;
    }

    public void setOnLevelChangedListener(OnLevelChangedListener onLevelChangedListener) {
        this.onLevelChangedListener = onLevelChangedListener;
    }

    public void setOnGameOverListener(OnGameOverListener onGameOverListener) {
        this.onGameOverListener = onGameOverListener;
    }

    public synchronized void setShowNextFigure(boolean isShowNextFigure) {
        this.isShowNextFigure = isShowNextFigure;
    }

    public synchronized int getStartingLevel() {
        return startingLevel;
    }

    public synchronized void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    /**
     * Starts a new game.
     */
    public synchronized void newGame() {
        // Re-initialise the game field.
        gameField = new int[FIELD_WIDTH + 2][FIELD_HEIGHT + 2];

        // Fill the field borders.
        for (int i = 1; i <= FIELD_WIDTH; i++) {
            gameField[i][0] = CELL_IS_BORDER;
            for (int j = 1; j <= FIELD_HEIGHT; j++) {
                gameField[i][j] = CELL_IS_EMPTY;
            }
            gameField[i][FIELD_HEIGHT + 1] = CELL_IS_BORDER;
        }
        for (int j = 0; j <= FIELD_HEIGHT + 1; j++) {
            gameField[0][j] = CELL_IS_BORDER;
            gameField[FIELD_WIDTH + 1][j] = CELL_IS_BORDER;
        }

        // Resets Figure types statistics.
        for (int i = 0; i <= Figure.FT_MAX; i++) {
            inGameFigureCounts[i] = 0;
        }

        // Reset score and line counts.
        reducedLinesCount = 0;
        gameScore = 0;

        if (onScoreChangedListener != null) {
            onScoreChangedListener.onScoreChanged(gameScore, reducedLinesCount, inGameFigureCounts[0]);
        }

        currentLevel = startingLevel;

        if (onLevelChangedListener != null) {
            onLevelChangedListener.onLevelChanged(currentLevel);
        }

        nextLevelFiguresCount = currentLevel * FIGURES_PER_LEVEL;

        currentFigure = null;
        nextFigure = new Figure(gameField, nextFigureType());
        newFigure();

        isInGame = true;

        // Create a main Game thread.
        new Thread(() -> {
            while (isInGame) {
                delay((10 - currentLevel) * 70);
                processTimePassed();
                Thread.yield();
            }
        }).start();
    }

    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void processTimePassed() {
        if ((isInGame) && (!isPaused)) {
            if (currentFigure != null && !currentFigure.maybeOneStepDown()) {
                afterFigureIsDown();
            }
            repaintField();
        }
    }

    private void reduceLines() {
        int reducedLines = 0;

        for (int i = FIELD_HEIGHT; i >= 1; i--) {
            boolean isEmptyLine = true;
            boolean isFullyFilledLine = true;

            for (int j = 1; j <= FIELD_WIDTH; j++) {
                if (gameField[j][i] == CELL_IS_EMPTY) {
                    isFullyFilledLine = false;
                    if (!isEmptyLine) {
                        break;
                    }
                } else {
                    isEmptyLine = false;
                    if (!isFullyFilledLine) {
                        break;
                    }
                }
            }

            if (isFullyFilledLine) {
                reduceOneLine(i);
                reducedLines++;
                i++;
            }
            if (isEmptyLine) {
                break;
            }
        }

        reducedLinesCount += reducedLines;
        gameScore += maybeAdjustScoreIncrement(reducedLines * reducedLines *
                (currentLevel + 1) * (currentLevel + 1) * 10);

        if (onScoreChangedListener != null) {
            onScoreChangedListener.onScoreChanged(gameScore, reducedLinesCount, inGameFigureCounts[0]);
        }
    }

    private void reduceOneLine(int iLine) {
        for (int i = iLine; i > 1; i--) {
            boolean isEmptyLine = true;
            for (int j = 1; j <= FIELD_WIDTH; j++) {
                gameField[j][i] = gameField[j][i - 1];
                if (gameField[j][i] != CELL_IS_EMPTY) {
                    isEmptyLine = false;
                }
            }
            if (isEmptyLine) {
                break;
            }
        }
    }

    /**
     * Obtains a Canvas and fully repaints the game field on it.
     */
    public synchronized void repaintField() {
        if (gameSurfaceHolder != null) {
            Canvas canvas = gameSurfaceHolder.lockCanvas();
            if (canvas != null) {
                repaintField(canvas);
                gameSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * Fully repaints the game field on a given Canvas.
     *
     * @param canvas to paint the game field on.
     */
    private void repaintField(Canvas canvas) {
        Paint paint = new Paint();
        // Fill all field with field color.
        paint.setColor(FIELD_COLOR);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        for (int i = FIELD_HEIGHT; i > 0; i--)
            for (int j = 1; j <= FIELD_WIDTH; j++) {
                // Draw non-empty field cells.
                if (gameField[j][i] != 0) {
                    paint.setColor(Figure.figuresColor[gameField[j][i]]);
                    canvas.drawRect
                            ((j - 1) * Figure.FIGURE_WIDTH_PIXELS,
                                    (i - 1) * Figure.FIGURE_WIDTH_PIXELS,
                                    j * Figure.FIGURE_WIDTH_PIXELS,
                                    i * Figure.FIGURE_HEIGHT_PIXELS,
                                    paint);
                }
            }

        if (currentFigure != null) {
            currentFigure.paint(canvas);
        }
    }

    /**
     * Generates next Figure type.
     *
     * @return figure type
     */
    private int nextFigureType() {
        int randomValue = randomGenerator.nextInt();
        if (randomValue < 0) {
            randomValue = -randomValue;
        }
        return randomValue % Figure.FT_MAX + 1;
    }

    /**
     * Increases Figure count by 1, increases score, increases level if appropriate.
     */
    private void increaseFigureCount() {
        int iFigType = currentFigure.figureType();

        inGameFigureCounts[0]++;
        inGameFigureCounts[iFigType]++;
        gameScore += maybeAdjustScoreIncrement((currentLevel + 1) * (currentLevel + 1) * 3);

        if ((currentLevel <= MAX_LEVEL) && (inGameFigureCounts[0] >= nextLevelFiguresCount)) {
            currentLevel++;
            nextLevelFiguresCount += FIGURES_PER_LEVEL;
            if (onLevelChangedListener != null) {
                onLevelChangedListener.onLevelChanged(currentLevel);
            }
        }

        if (onScoreChangedListener != null) {
            onScoreChangedListener.onScoreChanged(gameScore, reducedLinesCount, inGameFigureCounts[0]);
        }
    }

    /**
     * Adjusts score increment down by 10% if the 'next' figure is being shown.
     *
     * @param scoreIncrement score increment to adjust.
     * @return adjusted score increment.
     */
    private int maybeAdjustScoreIncrement(int scoreIncrement) {
        if (isShowNextFigure) {
            scoreIncrement = (scoreIncrement * 9) / 10;
        }
        return scoreIncrement;
    }

    private void onGameOver() {
        isInGame = false;
        currentFigure = null;
        nextFigure = null;
        if (onGameOverListener != null) {
            onGameOverListener.onGameOver();
        }
    }

    private void newFigure() {
        if (nextFigure != null && nextFigure.isGameOver()) {
            onGameOver();
            return;
        }

        currentFigure = nextFigure;
        nextFigure = new Figure(gameField, nextFigureType());

        if (isShowNextFigure && showNextSurfaceHolder != null) {
            Canvas canvas = showNextSurfaceHolder.lockCanvas();
            if (canvas != null) {
                nextFigure.paintNext(canvas);
                showNextSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void afterFigureIsDown() {
        increaseFigureCount();
        reduceLines();
        newFigure();
    }

    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized boolean togglePaused() {
        isPaused = !isPaused;
        return isPaused;
    }

    public synchronized void left() {
        if (isInGame && !isPaused && currentFigure != null) {
            currentFigure.left();
            repaintField();
        }
    }

    public synchronized void rotate() {
        if (isInGame && !isPaused && currentFigure != null) {
            currentFigure.rotate();
            repaintField();
        }
    }

    public synchronized void right() {
        if (isInGame && !isPaused && currentFigure != null) {
            currentFigure.right();
            repaintField();
        }
    }

    public synchronized void drop() {
        if (isInGame && !isPaused && currentFigure != null) {
            int droppedLinesCount = currentFigure.drop();
            gameScore += maybeAdjustScoreIncrement((droppedLinesCount * currentLevel * currentLevel) / 5);
            afterFigureIsDown();
            repaintField();
        }
    }
}
