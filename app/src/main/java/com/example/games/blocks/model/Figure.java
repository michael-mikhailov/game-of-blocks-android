package com.example.games.blocks.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;

import static com.example.games.blocks.model.Game.CELL_IS_EMPTY;

/**
 * Representation of one block 'figure' on the game field.
 */
class Figure {

    // Figure dimension. All blocks have 4 elements.
    private static final int FIGURE_SIZE = 4;

    // Dimensions of the game field cell in pixels.
    // TODO: get this dynamically based on screen parameters.
    static final int FIGURE_WIDTH_PIXELS = 66;
    static final int FIGURE_HEIGHT_PIXELS = 66;

    private static final int FIGURE_INITIAL_LEFT_X = 4;
    private static final int FIGURE_INITIAL_Y = 1;

    @ColorInt
    static final int FIELD_COLOR = Color.BLACK;

    @ColorInt
    static final int SHOW_NEXT_SURFACE_BACKGROUND_COLOR = Color.LTGRAY;

    // Figure types.
    static final int FT_BRICK = 1;
    static final int FT_G_LEFT = 2;
    static final int FT_G_RIGHT = 3;
    static final int FT_CUBE = 4;
    static final int FT_Z_LEFT = 5;
    static final int FT_Z_RIGHT = 6;
    static final int FT_PIN = 7;
    static final int FT_MAX = 7;

    // Color of the figures, indexed by figure type above.
    @ColorInt
    static final int[] figuresColor = {
            FIELD_COLOR, Color.RED, Color.MAGENTA, Color.WHITE,
            Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW,
            FIELD_COLOR};

    // Type of the figure that this instance represents.
    private final int figureType;

    // X and Y coordinates of the parts of the figure.
    private final int[] aiX = new int[FIGURE_SIZE];
    private final int[] aiY = new int[FIGURE_SIZE];

    // Current rotation state for this instance.
    int rotationStateNumber = 0;

    // Pointer to the parent Game field to simplify methods signatures.
    int[][] gameField;

    /**
     * Paints this figure on a given Canvas, in the current position on the game field.
     *
     * @param canvas to paint this Figure.
     */
    void paint(Canvas canvas) {
        Paint paint = new Paint();
        // Draw figure parts using color of the figure type.
        paint.setColor(figuresColor[figureType]);
        for (int i = 0; i < FIGURE_SIZE; i++) {
            canvas.drawRect((aiX[i] - 1) * FIGURE_WIDTH_PIXELS,
                    (aiY[i] - 1) * FIGURE_WIDTH_PIXELS,
                    aiX[i] * FIGURE_WIDTH_PIXELS,
                    aiY[i] * FIGURE_HEIGHT_PIXELS,
                    paint);
        }
    }

    /**
     * Paints this figure as the 'next' figure on a given Canvas.
     *
     * @param canvas to paint this Figure.
     */
    void paintNext(Canvas canvas) {
        Paint paint = new Paint();
        // Fill all the 'show next' canvas with background.
        paint.setColor(SHOW_NEXT_SURFACE_BACKGROUND_COLOR);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        // Draw figure parts using color of the figure type.
        paint.setColor(figuresColor[figureType]);
        for (int i = 0; i < FIGURE_SIZE; i++) {
            canvas.drawRect((aiX[i] - FIGURE_INITIAL_LEFT_X) * FIGURE_WIDTH_PIXELS,
                    (aiY[i] - 1) * FIGURE_WIDTH_PIXELS,
                    (aiX[i] - FIGURE_INITIAL_LEFT_X + 1) * FIGURE_WIDTH_PIXELS,
                    aiY[i] * FIGURE_WIDTH_PIXELS, paint);
        }
    }

    /**
     * Creates a new Figure of a given figure type and puts it into the top of game field,
     * in the middle.
     *
     * @param gameField  reference to the game field.
     * @param figureType type of the figure to be created.
     */
    Figure(int[][] gameField, int figureType) {
        this.figureType = figureType;
        this.gameField = gameField;

        // Set initial positions of figure parts on the game field depending on figure type.
        switch (figureType) {
            case FT_BRICK:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                break;
            case FT_G_LEFT:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                aiX[0]++;
                aiY[0]++;
                break;
            case FT_G_RIGHT:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                aiX[3]--;
                aiY[3]++;
                break;
            case FT_CUBE:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                aiX[0]++;
                aiY[0]++;
                aiX[3]--;
                aiY[3]++;
                break;
            case FT_Z_LEFT:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                aiX[2]--;
                aiY[2]++;
                aiX[3]--;
                aiY[3]++;
                break;
            case FT_Z_RIGHT:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                aiX[0]++;
                aiY[0]++;
                aiX[1]++;
                aiY[1]++;
                break;
            case FT_PIN:
                for (int i = 0; i < FIGURE_SIZE; i++) {
                    aiX[i] = i + FIGURE_INITIAL_LEFT_X;
                    aiY[i] = FIGURE_INITIAL_Y;
                }
                aiX[0] += 2;
                aiY[0]++;
                break;
        }

    }

    /**
     * Determines if the game is over by checking if this Figure is positioned over already
     * occupied cells of the game field.
     */
    boolean isGameOver() {
        for (int i = 0; i < FIGURE_SIZE; i++) {
            if (gameField[aiX[i]][aiY[i]] != CELL_IS_EMPTY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns this Figure type.
     */
    int figureType() {
        return figureType;
    }

    /**
     * Moves this Figure one position to the left if possible.
     */
    void left() {
        // Check if it can be moved left.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            if (gameField[aiX[i] - 1][aiY[i]] != CELL_IS_EMPTY) {
                // Can not move. Exit early.
                return;
            }
        }
        // Move left.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            aiX[i]--;
        }
    }

    /**
     * Moves this Figure one position to the right if possible.
     */
    void right() {
        // Check if it can be moved right.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            if (gameField[aiX[i] + 1][aiY[i]] != CELL_IS_EMPTY) {
                // Can not move. Exit early.
                return;
            }
        }
        // Move right
        for (int i = 0; i < FIGURE_SIZE; i++) {
            aiX[i]++;
        }
    }

    /**
     * Moves this Figure one position down if possible.
     * If it can not be moved down, transfers this Figure current position to the game field.
     */
    boolean maybeOneStepDown() {
        boolean canGoDown = true;

        // Check if there is space below.
        for (int i = 0; i < FIGURE_SIZE; i++)
            if (gameField[aiX[i]][aiY[i] + 1] != CELL_IS_EMPTY) {
                canGoDown = false;
                break;
            }

        if (canGoDown) {
            // Move down.
            for (int i = 0; i < FIGURE_SIZE; i++) {
                aiY[i]++;
            }
        } else {
            // Fix in the field at current position.
            for (int i = 0; i < FIGURE_SIZE; i++)
                gameField[aiX[i]][aiY[i]] = figureType;
        }

        return canGoDown;
    }

    /**
     * 'Drops' this Figure down on top of the first 'occupied' cell.
     *
     * @return number of lines that this Figure was moved down, used for scoring.
     */
    int drop() {
        boolean isFoundNonEmptyLine = false;

        // Calculate how high is the vertical drop.
        int droppedLinesCount = 1;
        for (; droppedLinesCount < Game.FIELD_HEIGHT; droppedLinesCount++) {
            for (int i = 0; i < FIGURE_SIZE; i++)
                if (gameField[aiX[i]][aiY[i] + droppedLinesCount] != CELL_IS_EMPTY) {
                    isFoundNonEmptyLine = true;
                    break;
                }
            if (isFoundNonEmptyLine) break;
        }
        droppedLinesCount--;

        // Drop.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            aiY[i] += droppedLinesCount;
        }

        // Fix the dropped figure position to the field.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            gameField[aiX[i]][aiY[i]] = figureType;
        }

        return droppedLinesCount;
    }

    /**
     * Rotates this Figure if possible.
     */
    void rotate() {
        switch (this.figureType) {
            case FT_BRICK:
                rotateOne(ROTATE_BRICK_X_INCREMENTS, ROTATE_BRICK_Y_INCREMENTS, 2);
                break;
            case FT_G_LEFT:
                rotateOne(ROTATE_G_LEFT_X_INCREMENTS, ROTATE_G_LEFT_Y_INCREMENTS, 4);
                break;
            case FT_G_RIGHT:
                rotateOne(ROTATE_G_RIGHT_X_INCREMENTS, ROTATE_G_RIGHT_Y_INCREMENTS, 4);
                break;
            case FT_Z_LEFT:
                rotateOne(ROTATE_Z_LEFT_X_INCREMENTS, ROTATE_Z_LEFT_Y_INCREMENTS, 2);
                break;
            case FT_Z_RIGHT:
                rotateOne(ROTATE_Z_RIGHT_X_INCREMENTS, ROTATE_Z_RIGHT_Y_INCREMENTS, 2);
                break;
            case FT_PIN:
                rotateOne(ROTATE_PIN_X_INCREMENTS, ROTATE_PIN_Y_INCREMENTS, 4);
                break;
        }
    }

    /**
     * Rotation rules for X & Y coordinates for each figure type.
     */

    /*-----------------------------------------------------------*/
    private static final int[][] ROTATE_G_LEFT_X_INCREMENTS = {
            {+1, 0, -1, -2}, {+1, +2, +1, 0}, {-1, 0, +1, +2}, {-1, -2, -1, 0}
    };
    private static final int[][] ROTATE_G_LEFT_Y_INCREMENTS = {
            {0, +1, 0, -1}, {-1, 0, +1, +2}, {-1, -2, -1, 0}, {+2, +1, 0, -1}
    };
    /*-----------------------------------------------------------*/
    private static final int[][] ROTATE_G_RIGHT_X_INCREMENTS = {
            {0, -1, -2, -1}, {+2, +1, 0, -1}, {0, +1, +2, +1}, {-2, -1, 0, +1}
    };
    private static final int[][] ROTATE_G_RIGHT_Y_INCREMENTS = {
            {+1, 0, -1, -2}, {0, +1, +2, +1}, {-2, -1, 0, +1}, {+1, 0, -1, 0}
    };
    /*-----------------------------------------------------------*/
    private static final int[][] ROTATE_BRICK_X_INCREMENTS = {
            {+2, +1, 0, -1}, {-2, -1, 0, +1}
    };
    private static final int[][] ROTATE_BRICK_Y_INCREMENTS = {
            {-2, -1, 0, +1}, {+2, +1, 0, -1}
    };
    /*-----------------------------------------------------------*/
    private static final int[][] ROTATE_Z_LEFT_X_INCREMENTS = {
            {+2, +1, 0, -1}, {-2, -1, 0, +1}
    };
    private static final int[][] ROTATE_Z_LEFT_Y_INCREMENTS = {
            {-1, 0, -1, 0}, {+1, 0, +1, 0}
    };
    /*-----------------------------------------------------------*/
    private static final int[][] ROTATE_Z_RIGHT_X_INCREMENTS = {
            {+1, 0, +1, 0}, {-1, 0, -1, 0}
    };
    private static final int[][] ROTATE_Z_RIGHT_Y_INCREMENTS = {
            {-2, -1, 0, +1}, {+2, +1, 0, -1}
    };
    /*-----------------------------------------------------------*/
    private static final int[][] ROTATE_PIN_X_INCREMENTS = {
            {+1, +1, 0, -1}, {-1, +1, 0, -1}, {-1, -1, 0, +1}, {+1, -1, 0, +1}
    };
    private static final int[][] ROTATE_PIN_Y_INCREMENTS = {
            {-1, +1, 0, -1}, {0, 0, +1, +2}, {0, -2, -1, 0}, {+1, +1, 0, -1}
    };

    /***
     * Rotates this Figure is possible.
     *
     * @param rotateXIncrements array of increments for X coordinate
     * @param rotateYIncrements array of increments for Y coordinate
     * @param rotationStateLimit limit of rotation states for this figure type
     */
    void rotateOne(int[][] rotateXIncrements, int[][] rotateYIncrements, int rotationStateLimit) {
        // Check if it can be rotated.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            // Check that the new prospective position is not off the game field.
            int newX = aiX[i] + rotateXIncrements[rotationStateNumber][i];
            if (newX < 0) {
                return;
            }
            int newY = aiY[i] + rotateYIncrements[rotationStateNumber][i];
            if (newY < 0) {
                return;
            }
            // Check that the new position is not already occupied.
            if (gameField[newX][newY] != CELL_IS_EMPTY) {
                return;
            }
        }
        // Rotate.
        for (int i = 0; i < FIGURE_SIZE; i++) {
            aiX[i] += rotateXIncrements[rotationStateNumber][i];
            aiY[i] += rotateYIncrements[rotationStateNumber][i];
        }
        // Update the state of rotation.
        rotationStateNumber++;
        if (rotationStateNumber >= rotationStateLimit) {
            rotationStateNumber = 0;
        }
    }
}
