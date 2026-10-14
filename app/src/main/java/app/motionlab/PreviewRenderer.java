package app.motionlab;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import app.motionlab.engine.DrawItem;
import app.motionlab.engine.Evaluator;
import app.motionlab.engine.Project;
import app.motionlab.engine.QuadTransform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Draws the project at the current time. All engine reads happen on the GL thread;
 * the UI thread only sets the playing flag and seeks.
 */
public final class PreviewRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER =
            "attribute vec2 aPos;\n"
            + "uniform mat4 uMvp;\n"
            + "void main() {\n"
            + "  gl_Position = uMvp * vec4(aPos, 0.0, 1.0);\n"
            + "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
            + "uniform vec4 uColor;\n"
            + "void main() {\n"
            + "  gl_FragColor = uColor;\n"
            + "}\n";

    private final Project project;
    private final Map<String, ShapeStyle> styles;
    private final FloatBuffer quad;
    private final float[] mvp = new float[16];

    private volatile boolean playing;
    private volatile long positionUs;
    private long lastFrameNanos; // GL thread only; 0 means "not playing"

    private int program;
    private int aPos;
    private int uMvp;
    private int uColor;
    private int surfaceWidth;
    private int surfaceHeight;

    public PreviewRenderer(Project project, Map<String, ShapeStyle> styles) {
        this.project = project;
        this.styles = styles;
        float[] corners = {0, 0, 1, 0, 0, 1, 1, 1};
        quad = ByteBuffer.allocateDirect(corners.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        quad.put(corners);
        quad.position(0);
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void seekToUs(long timeUs) {
        positionUs = Math.max(0, Math.min(timeUs, project.durationUs() - 1));
    }

    public long positionUs() {
        return positionUs;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Called again after the GL context is lost, so everything is rebuilt here.
        program = link(compile(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER),
                compile(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER));
        aPos = GLES20.glGetAttribLocation(program, "aPos");
        uMvp = GLES20.glGetUniformLocation(program, "uMvp");
        uColor = GLES20.glGetUniformLocation(program, "uColor");
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        advanceTime();

        // Dark surround, then the canvas fitted in the middle with its own background.
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        GLES20.glClearColor(0.10f, 0.10f, 0.10f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int canvasW = project.width();
        int canvasH = project.height();
        float fit = Math.min((float) surfaceWidth / canvasW, (float) surfaceHeight / canvasH);
        int viewW = Math.max(1, (int) (canvasW * fit));
        int viewH = Math.max(1, (int) (canvasH * fit));
        int viewX = (surfaceWidth - viewW) / 2;
        int viewY = (surfaceHeight - viewH) / 2;

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(viewX, viewY, viewW, viewH);
        GLES20.glClearColor(1f, 1f, 1f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(viewX, viewY, viewW, viewH);

        GLES20.glUseProgram(program);
        GLES20.glEnableVertexAttribArray(aPos);
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, quad);

        List<DrawItem> items = Evaluator.evaluate(project, positionUs);
        for (DrawItem item : items) {
            ShapeStyle style = styles.get(item.layerId);
            if (style == null) continue;
            QuadTransform.mvp(item.matrix, style.width, style.height, canvasW, canvasH, mvp);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniform4f(uColor, style.red, style.green, style.blue, (float) item.opacity);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }
        GLES20.glDisableVertexAttribArray(aPos);
    }

    private void advanceTime() {
        if (!playing) {
            lastFrameNanos = 0;
            return;
        }
        long now = System.nanoTime();
        if (lastFrameNanos != 0) {
            long next = positionUs + (now - lastFrameNanos) / 1000;
            long duration = project.durationUs();
            positionUs = next >= duration ? next % duration : next; // loop
        }
        lastFrameNanos = now;
    }

    private static int compile(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] ok = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile failed: " + log);
        }
        return shader;
    }

    private static int link(int vertex, int fragment) {
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertex);
        GLES20.glAttachShader(program, fragment);
        GLES20.glLinkProgram(program);
        int[] ok = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, ok, 0);
        if (ok[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Program link failed: " + log);
        }
        return program;
    }
}
