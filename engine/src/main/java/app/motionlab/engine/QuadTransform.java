package app.motionlab.engine;

/**
 * Math for drawing a layer as a rectangle with OpenGL, kept here (no Android imports) so it
 * can be unit tested. A quad is (0,0)-(1,1) in its own space and is drawn w by h pixels.
 */
public final class QuadTransform {
    private QuadTransform() {}

    /** Canvas pixels (origin top-left, y down) to OpenGL clip space (-1..1, y up). */
    public static Matrix2D canvasToClip(int canvasWidth, int canvasHeight) {
        return new Matrix2D(2.0 / canvasWidth, 0, 0, -2.0 / canvasHeight, -1, 1);
    }

    /**
     * Fills a column-major 4x4 matrix (out has 16 floats) that takes the unit quad to clip
     * space for a layer with the given draw matrix and size.
     */
    public static void mvp(Matrix2D layerMatrix, double width, double height,
                           int canvasWidth, int canvasHeight, float[] out) {
        Matrix2D m = canvasToClip(canvasWidth, canvasHeight)
                .multiply(layerMatrix)
                .multiply(Matrix2D.scale(width, height));
        out[0] = (float) m.a;  out[1] = (float) m.b;  out[2] = 0;  out[3] = 0;
        out[4] = (float) m.c;  out[5] = (float) m.d;  out[6] = 0;  out[7] = 0;
        out[8] = 0;            out[9] = 0;            out[10] = 1; out[11] = 0;
        out[12] = (float) m.tx; out[13] = (float) m.ty; out[14] = 0; out[15] = 1;
    }
}
