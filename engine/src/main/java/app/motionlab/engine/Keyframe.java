package app.motionlab.engine;

/** A value at a time. The curve shapes the motion from this keyframe to the next one. */
public final class Keyframe {
    public final long timeUs;
    public final double value;
    public final Curve curve;

    public Keyframe(long timeUs, double value, Curve curve) {
        if (timeUs < 0) throw new IllegalArgumentException("Keyframe time must be >= 0");
        if (curve == null) throw new IllegalArgumentException("Curve is required");
        this.timeUs = timeUs;
        this.value = value;
        this.curve = curve;
    }

    public Keyframe withValue(double newValue) {
        return new Keyframe(timeUs, newValue, curve);
    }

    public Keyframe withCurve(Curve newCurve) {
        return new Keyframe(timeUs, value, newCurve);
    }
}
