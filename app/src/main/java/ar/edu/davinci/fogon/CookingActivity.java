package ar.edu.davinci.fogon;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class CookingActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_NAME = "recipeName";
    public static final String EXTRA_STEPS = "steps";
    public static final String EXTRA_COMPLETED = "completed";

    private static final String STATE_STEP = "currentStep";

    public String recipeName;
    public ArrayList<Step> steps = new ArrayList<>();
    public int currentStep = 0;

    private CountDownTimer timer;
    private long remainingMillis;
    private boolean timerRunning = false;

    public void closeCooking(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void previousStep(View view) {
        if (currentStep == 0) return;
        currentStep--;
        showStep();
    }

    public void nextStep(View view) {
        if (currentStep == steps.size() - 1) {
            finishCooking();
            return;
        }
        currentStep++;
        showStep();
    }

    private void finishCooking() {
        Intent result = new Intent();
        result.putExtra(EXTRA_COMPLETED, true);
        setResult(RESULT_OK, result);
        finish();
    }

    private void showStep() {
        stopTimer();
        Step step = steps.get(currentStep);

        TextView counter = findViewById(R.id.cookingStepCounter);
        counter.setText(getString(R.string.cooking_step_counter, currentStep + 1, steps.size()));

        TextView text = findViewById(R.id.cookingStepText);
        text.setText(step.getText());

        findViewById(R.id.buttonPreviousStep).setEnabled(currentStep > 0);
        Button next = findViewById(R.id.buttonNextStep);
        next.setText(currentStep == steps.size() - 1 ? R.string.cooking_finish : R.string.cooking_next_step);

        renderProgress();

        View timerCard = findViewById(R.id.timerCard);
        if (step.hasTimer()) {
            timerCard.setVisibility(View.VISIBLE);
            remainingMillis = step.getMinutes() * 60_000L;
            updateTimerViews();
        } else {
            timerCard.setVisibility(View.GONE);
        }
    }

    private void renderProgress() {
        LinearLayout progress = findViewById(R.id.cookingProgress);
        progress.removeAllViews();
        int gap = getResources().getDimensionPixelSize(R.dimen.progress_gap);

        for (int i = 0; i < steps.size(); i++) {
            View segment = new View(this);
            int color = i <= currentStep ? R.color.cooking_progress_done : R.color.cooking_progress_todo;
            segment.setBackgroundColor(ContextCompat.getColor(this, color));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            if (i > 0) params.setMarginStart(gap);
            progress.addView(segment, params);
        }
    }

    public void toggleTimer(View view) {
        if (timerRunning) {
            stopTimer();
            updateTimerViews();
            return;
        }
        if (remainingMillis <= 0) return;

        timerRunning = true;
        timer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateTimerViews();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                remainingMillis = 0;
                updateTimerViews();
                Toast.makeText(CookingActivity.this, R.string.cooking_timer_finished_toast, Toast.LENGTH_LONG).show();
            }
        }.start();
        updateTimerViews();
    }

    private void stopTimer() {
        if (timer != null) timer.cancel();
        timer = null;
        timerRunning = false;
    }

    private void updateTimerViews() {
        long totalSeconds = (remainingMillis + 999) / 1000;
        TextView clock = findViewById(R.id.timerClock);
        clock.setText(getString(R.string.cooking_clock, totalSeconds / 60, totalSeconds % 60));

        Button button = findViewById(R.id.timerButton);
        long stepMillis = steps.get(currentStep).getMinutes() * 60_000L;
        if (timerRunning) {
            button.setText(R.string.cooking_timer_pause);
        } else if (remainingMillis <= 0) {
            button.setText(R.string.cooking_timer_done);
        } else if (remainingMillis < stepMillis) {
            button.setText(R.string.cooking_timer_resume);
        } else {
            button.setText(R.string.cooking_timer_start);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_STEP, currentStep);
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cooking);
        View root = findViewById(R.id.main);
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightStatusBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(EXTRA_STEPS)) {
            finish();
            return;
        }
        recipeName = extras.getString(EXTRA_RECIPE_NAME);
        steps = (ArrayList<Step>) extras.getSerializable(EXTRA_STEPS);
        if (steps == null || steps.isEmpty()) {
            finish();
            return;
        }
        if (savedInstanceState != null) currentStep = savedInstanceState.getInt(STATE_STEP, 0);

        TextView name = findViewById(R.id.cookingRecipeName);
        name.setText(recipeName);
        showStep();
    }
}
