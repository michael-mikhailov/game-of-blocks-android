package com.example.games.blocks;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.games.blocks.model.Game;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Main Game Activity.
 */
public class MainActivity extends AppCompatActivity {

    private Game game;
    private Button pauseGame;
    private SwitchMaterial showNextFigureSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        SurfaceView gameSurfaceView = findViewById(R.id.game_surface);
        SurfaceHolder gameSurfaceHolder = gameSurfaceView.getHolder();
        SurfaceView showNextSurfaceView = findViewById(R.id.show_next_surface);
        SurfaceHolder showNextSurfaceHolder = showNextSurfaceView.getHolder();

        game = new Game();

        TextView levelNumberTextView = findViewById(R.id.level_number);

        Spinner beginLevelSpinner = findViewById(R.id.begin_level_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.levels, R.layout.starting_level_spinner_element_layout);
        beginLevelSpinner.setAdapter(adapter);
        beginLevelSpinner.setSelection(game.getStartingLevel() - 1);
        beginLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                game.setStartingLevel(position + 1);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button newGame = findViewById(R.id.button_new_game);
        newGame.setOnClickListener(listener -> {
            showNextFigureSwitch.setEnabled(false);
            beginLevelSpinner.setVisibility(View.GONE);
            levelNumberTextView.setVisibility(View.VISIBLE);
            game.newGame();
            newGame.setEnabled(false);
            pauseGame.setEnabled(true);
            setGameStateText(getResources().getString(R.string.game_state_play));
        });

        pauseGame = findViewById(R.id.button_pause_game);
        pauseGame.setOnClickListener(listener -> {
            boolean isPaused = game.togglePaused();
            pauseGame.setText(isPaused
                    ? R.string.button_unpause_game
                    : R.string.button_pause_game);
            setGameStateText(getResources().getString(isPaused
                    ? R.string.game_state_paused
                    : R.string.game_state_play));
        });

        showNextFigureSwitch = findViewById(R.id.show_next_switch);
        game.setShowNextFigure(showNextFigureSwitch.isChecked());
        showNextFigureSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                game.setShowNextFigure(isChecked));

        game.setOnGameOverListener(() ->
            MainActivity.this.runOnUiThread(() -> {
                setGameStateText(getResources().getString(R.string.game_state_game_over));

                beginLevelSpinner.setVisibility(View.VISIBLE);
                levelNumberTextView.setVisibility(View.GONE);

                pauseGame.setEnabled(false);
                showNextFigureSwitch.setEnabled(true);

                newGame.setEnabled(true);
                newGame.requestFocus();
            })
        );

        TextView scoreTextView = findViewById(R.id.score);
        TextView figuresCountTextView = findViewById(R.id.figures_count);
        TextView linesCountTextView = findViewById(R.id.lines_count);
        game.setOnScoreChangedListener((newScore, linesCount, figuresCount) ->
            MainActivity.this.runOnUiThread(() -> {
                scoreTextView.setText(String.format("%,d", newScore));
                figuresCountTextView.setText(String.format("%,d", figuresCount));
                linesCountTextView.setText(String.format("%,d", linesCount));
            })
        );

        game.setOnLevelChangedListener((newLevel) ->
            MainActivity.this.runOnUiThread(() ->
                levelNumberTextView.setText(String.format("%,d", newLevel))
            )
        );

        ImageButton buttonLeft = findViewById(R.id.button_left);
        buttonLeft.setOnClickListener(listener -> game.left());

        ImageButton buttonRotate = findViewById(R.id.button_rotate);
        buttonRotate.setOnClickListener(listener -> game.rotate());

        ImageButton buttonRight = findViewById(R.id.button_right);
        buttonRight.setOnClickListener(listener -> game.right());

        ImageButton buttonDrop = findViewById(R.id.button_drop);
        buttonDrop.setOnClickListener(listener -> game.drop());

        gameSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                game.setGameSurfaceHolder(holder);
                game.repaintField();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        showNextSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                game.setShowNextSurfaceHolder(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game if activity is paused.
        game.pause();
        setGameStateText(getResources().getString(R.string.game_state_paused));
        pauseGame.setText(R.string.button_unpause_game);
    }

    private void setGameStateText(String gameState) {
        this.setTitle(getResources().getString(R.string.activity_title, gameState));
    }
}