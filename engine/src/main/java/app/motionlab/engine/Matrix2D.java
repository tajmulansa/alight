package app.motionlab.engine;

/**
 * Immutable 2D affine transform. Maps (x, y) to (a*x + c*y + tx, b*x + d*y + ty).
 * The canvas has y pointing down, so a positive rotation turns clockwise on screen.
 */
public final class Matrix2D {
    public static final Matrix2D IDENTITY = new Matrix2D(1, 0, 0, 1, 0, 0);

    public final double a, b, c, d, tx, ty;

    public Matrix2D(double a, double b, double c, double d, double tx, double ty) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.tx = tx;
        this.ty = ty;
    }

    public static Matrix2D translate(double x, double y) {
        return new Matrix2D(1, 0, 0, 1, x, y);
    }

    public static Matrix2D scale(double sx, double sy) {
        return new Matrix2D(sx, 0, 0, sy, 0, 0);
    }

    public static Matrix2D rotateDegrees(double degrees) {
        double r = Math.toRadians(degrees);
        double cos = Math.cos(r), sin = Math.sin(r);
        return new Matrix2D(cos, sin, -sin, cos, 0, 0);
    }

    /** Returns this * other: other is applied to a point first, then this. */
    public Matrix2D multiply(Matrix2D o) {
        return new Matrix2D(
                a * o.a + c * o.b,
                b * o.a + d * o.b,
                a * o.c + c * o.d,
                b * o.c + d * o.d,
                a * o.tx + c * o.ty + tx,
                b * o.tx + d * o.ty + ty);
    }

    public double applyX(double x, double y) {
        return a * x + c * y + tx;
    }

    public double applyY(double x, double y) {
        return b * x + d * y + ty;
    }
}
