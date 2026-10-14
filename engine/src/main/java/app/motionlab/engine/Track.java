package app.motionlab.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One animatable number (for example "opacity"), with optional keyframes. */
public final class Track {
    private final String path;
    private double baseValue;
    private final List<Keyframe> keyframes = new ArrayList<>(); // sorted by time, unique times

    public Track(String path, double baseValue) {
        this.path = path;
        this.baseValue = baseValue;
    }

    public String path() {
        return path;
    }

    public double baseValue() {
        return baseValue;
    }

    public void setBaseValue(double value) {
        this.baseValue = value;
    }

    public boolean hasKeyframes() {
        return !keyframes.isEmpty();
    }

    public List<Keyframe> keyframes() {
        return Collections.unmodifiableList(keyframes);
    }

    /** Value at a time. Before the first keyframe and after the last one the value holds. */
    public double valueAt(long timeUs) {
        int n = keyframes.size();
        if (n == 0) return baseValue;
        Keyframe first = keyframes.get(0);
        if (timeUs <= first.timeUs) return first.value;
        Keyframe last = keyframes.get(n - 1);
        if (timeUs >= last.timeUs) return last.value;

        int i = segmentIndex(timeUs); // keyframes[i].time <= t < keyframes[i+1].time
        Keyframe a = keyframes.get(i);
        Keyframe b = keyframes.get(i + 1);
        double u = (double) (timeUs - a.timeUs) / (double) (b.timeUs - a.timeUs);
        double eased = a.curve.apply(u);
        return a.value + (b.value - a.value) * eased;
    }

    /** Adds a keyframe, or changes the value of the one at that time (its curve is kept). */
    public void setKeyframe(long timeUs, double value) {
        int idx = indexOfTime(timeUs);
        if (idx >= 0) {
            keyframes.set(idx, keyframes.get(idx).withValue(value));
        } else {
            Keyframe k = new Keyframe(timeUs, value, Curve.LINEAR);
            keyframes.add(insertionPoint(timeUs), k);
        }
    }

    /** Sets the easing from the keyframe at this time to the next one. */
    public void setCurve(long timeUs, Curve curve) {
        int idx = indexOfTime(timeUs);
        if (idx < 0) throw new IllegalArgumentException("No keyframe at " + timeUs);
        keyframes.set(idx, keyframes.get(idx).withCurve(curve));
    }

    public boolean removeKeyframe(long timeUs) {
        int idx = indexOfTime(timeUs);
        if (idx < 0) return false;
        keyframes.remove(idx);
        return true;
    }

    private int indexOfTime(long timeUs) {
        int lo = 0, hi = keyframes.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long t = keyframes.get(mid).timeUs;
            if (t == timeUs) return mid;
            if (t < timeUs) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }

    private int insertionPoint(long timeUs) {
        int lo = 0, hi = keyframes.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (keyframes.get(mid).timeUs < timeUs) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private int segmentIndex(long timeUs) {
        int lo = 0, hi = keyframes.size() - 2;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            if (keyframes.get(mid).timeUs <= timeUs) lo = mid; else hi = mid - 1;
        }
        return lo;
    }
}
