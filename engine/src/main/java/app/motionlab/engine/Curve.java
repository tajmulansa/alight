package app.motionlab.engine;

/**
 * Easing between two keyframes. Maps progress u (0..1) to eased progress.
 * A Bezier curve has two handles; x must stay in 0..1 so time never runs backwards,
 * y may leave 0..1 for overshoot.
 */
public final class Curve {
    public enum Type { LINEAR, BEZIER, HOLD }

    public static final Curve LINEAR = new Curve(Type.LINEAR, 0, 0, 1, 1);
    public static final Curve HOLD = new Curve(Type.HOLD, 0, 0, 1, 1);
    public static final Curve EASE_IN = bezier(0.42, 0, 1, 1);
    public static final Curve EASE_OUT = bezier(0, 0, 0.58, 1);
    public static final Curve EASE_IN_OUT = bezier(0.42, 0, 0.58, 1);

    public final Type type;
    public final double x1, y1, x2, y2;

    private Curve(Type type, double x1, double y1, double x2, double y2) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public static Curve bezier(double x1, double y1, double x2, double y2) {
        if (x1 < 0 || x1 > 1 || x2 < 0 || x2 > 1) {
            throw new IllegalArgumentException("Bezier handle x must be within 0..1");
        }
        return new Curve(Type.BEZIER, x1, y1, x2, y2);
    }

    /** Eased progress for u in 0..1 (u outside the range is clamped). */
    public double apply(double u) {
        if (u <= 0) return 0;
        if (u >= 1) return 1;
        switch (type) {
            case HOLD:
                return 0;
            case BEZIER:
                return bezierY(u);
            case LINEAR:
            default:
                return u;
        }
    }

    private double bezierY(double u) {
        // Solve x(s) = u for the curve parameter s, then return y(s).
        double s = u;
        for (int i = 0; i < 8; i++) {
            double err = sample(s, x1, x2) - u;
            if (Math.abs(err) < 1e-9) return sample(s, y1, y2);
            double slope = slope(s, x1, x2);
            if (Math.abs(slope) < 1e-6) break;
            s -= err / slope;
            if (s < 0 || s > 1) break;
        }
        double lo = 0, hi = 1;
        s = u;
        for (int i = 0; i < 60; i++) {
            double x = sample(s, x1, x2);
            if (Math.abs(x - u) < 1e-9) break;
            if (x < u) lo = s; else hi = s;
            s = (lo + hi) / 2;
        }
        return sample(s, y1, y2);
    }

    private static double sample(double s, double p1, double p2) {
        double inv = 1 - s;
        return 3 * inv * inv * s * p1 + 3 * inv * s * s * p2 + s * s * s;
    }

    private static double slope(double s, double p1, double p2) {
        double inv = 1 - s;
        return 3 * inv * inv * p1 + 6 * inv * s * (p2 - p1) + 3 * s * s * (1 - p2);
    }
}
