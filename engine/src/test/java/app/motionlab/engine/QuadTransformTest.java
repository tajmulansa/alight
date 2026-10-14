package app.motionlab.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QuadTransformTest {
    private static final double EPS = 1e-6;

    private static double clipX(float[] m, double x, double y) {
        return m[0] * x + m[4] * y + m[12];
    }

    private static double clipY(float[] m, double x, double y) {
        return m[1] * x + m[5] * y + m[13];
    }

    @Test
    public void fullCanvasQuadCoversClipSpace() {
        float[] m = new float[16];
        QuadTransform.mvp(Matrix2D.IDENTITY, 1000, 2000, 1000, 2000, m);
        assertEquals(-1, clipX(m, 0, 0), EPS);
        assertEquals(1, clipY(m, 0, 0), EPS);
        assertEquals(1, clipX(m, 1, 1), EPS);
        assertEquals(-1, clipY(m, 1, 1), EPS);
    }

    @Test
    public void translatedQuadLandsWhereExpected() {
        float[] m = new float[16];
        QuadTransform.mvp(Matrix2D.translate(100, 200), 100, 100, 1000, 2000, m);
        assertEquals(-0.8, clipX(m, 0, 0), EPS);
        assertEquals(0.8, clipY(m, 0, 0), EPS);
        assertEquals(-0.6, clipX(m, 1, 1), EPS);
        assertEquals(0.7, clipY(m, 1, 1), EPS);
    }

    @Test
    public void canvasCentreIsClipOrigin() {
        float[] m = new float[16];
        // A 200x100 quad whose centre is placed at the canvas centre (500, 1000).
        Matrix2D place = Matrix2D.translate(500, 1000).multiply(Matrix2D.translate(-100, -50));
        QuadTransform.mvp(place, 200, 100, 1000, 2000, m);
        assertEquals(0, clipX(m, 0.5, 0.5), EPS);
        assertEquals(0, clipY(m, 0.5, 0.5), EPS);
    }

    @Test
    public void matrixLayoutIsColumnMajorWithUnitDepth() {
        float[] m = new float[16];
        QuadTransform.mvp(Matrix2D.IDENTITY, 10, 10, 100, 100, m);
        assertEquals(1, m[10], EPS);
        assertEquals(1, m[15], EPS);
        assertEquals(0, m[2], EPS);
        assertEquals(0, m[14], EPS);
    }

    @Test
    public void rotatedLayerStillMapsItsOriginCorrectly() {
        float[] m = new float[16];
        Matrix2D rotated = Matrix2D.translate(500, 500).multiply(Matrix2D.rotateDegrees(90));
        QuadTransform.mvp(rotated, 100, 100, 1000, 1000, m);
        // Unit point (1,0) is 100px along the layer's x axis, which now points down the canvas.
        assertEquals(0, clipX(m, 1, 0), EPS);
        assertEquals(1 - 2 * 600.0 / 1000, clipY(m, 1, 0), EPS);
    }
}
