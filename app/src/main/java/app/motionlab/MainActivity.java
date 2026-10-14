package app.motionlab;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

/** Preview of the demo project with a play button, a time slider and a time readout. */
public class MainActivity extends Activity {
    private PreviewRenderer renderer;
    private GLSurfaceView glView;
    private SeekBar seekBar;
    private TextView timeLabel;
    private Button playButton;
    private long durationUs;
    private boolean dragging;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            refreshControls();
            handler.postDelayed(this, 50);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DemoProject demo = DemoProject.create();
        durationUs = demo.project.durationUs();
        renderer = new PreviewRenderer(demo.project, demo.styles);

        glView = new GLSurfaceView(this);
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(renderer);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        playButton = new Button(this);
        playButton.setText("Play");
        playButton.setOnClickListener(v -> {
            renderer.setPlaying(!renderer.isPlaying());
            refreshControls();
        });

        seekBar = new SeekBar(this);
        seekBar.setMax((int) (durationUs / 1000));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) renderer.seekToUs(progress * 1000L);
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                dragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                dragging = false;
            }
        });

        timeLabel = new TextView(this);
        timeLabel.setText(formatTime(0));

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(playButton);
        controls.addView(seekBar, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        controls.addView(timeLabel);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(glView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(controls, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Android 15 draws apps edge to edge, so keep the controls clear of the system bars.
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
        renderer.setPlaying(false);
        refreshControls();
        glView.onPause();
    }

    private void refreshControls() {
        long positionUs = renderer.positionUs();
        playButton.setText(renderer.isPlaying() ? "Pause" : "Play");
        if (!dragging) seekBar.setProgress((int) (positionUs / 1000));
        timeLabel.setText(formatTime(positionUs));
    }

    private static String formatTime(long us) {
        long totalCentis = us / 10_000;
        long minutes = totalCentis / 6000;
        long seconds = (totalCentis / 100) % 60;
        long centis = totalCentis % 100;
        return String.format(Locale.US, "%d:%02d.%02d", minutes, seconds, centis);
    }
}
