package app.motionlab.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TrackTest {
    private static final long SEC = 1_000_000L;

    @Test
    public void noKeyframesGivesBaseValue() {
        Track t = new Track("opacity", 0.8);
        assertEquals(0.8, t.valueAt(0), 1e-9);
        assertEquals(0.8, t.valueAt(5 * SEC), 1e-9);
    }

    @Test
    public void singleKeyframeHoldsEverywhere() {
        Track t = new Track("x", 0);
        t.setKeyframe(SEC, 42);
        assertEquals(42, t.valueAt(0), 1e-9);
        assertEquals(42, t.valueAt(SEC), 1e-9);
        assertEquals(42, t.valueAt(9 * SEC), 1e-9);
    }

    @Test
    public void linearInterpolationBetweenTwoKeyframes() {
        Track t = new Track("x", 0);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 100);
        assertEquals(0, t.valueAt(0), 1e-9);
        assertEquals(25, t.valueAt(SEC / 4), 1e-9);
        assertEquals(50, t.valueAt(SEC / 2), 1e-9);
        assertEquals(100, t.valueAt(SEC), 1e-9);
    }

    @Test
    public void valueHoldsBeforeFirstAndAfterLast() {
        Track t = new Track("x", 0);
        t.setKeyframe(SEC, 10);
        t.setKeyframe(2 * SEC, 20);
        assertEquals(10, t.valueAt(0), 1e-9);
        assertEquals(20, t.valueAt(3 * SEC), 1e-9);
    }

    @Test
    public void threeKeyframesGiveTwoSegments() {
        Track t = new Track("x", 0);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 100);
        t.setKeyframe(2 * SEC, 0);
        assertEquals(50, t.valueAt(SEC / 2), 1e-9);
        assertEquals(100, t.valueAt(SEC), 1e-9);
        assertEquals(50, t.valueAt(SEC + SEC / 2), 1e-9);
    }

    @Test
    public void keyframesStaySortedWhenAddedOutOfOrder() {
        Track t = new Track("x", 0);
        t.setKeyframe(2 * SEC, 20);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 10);
        assertEquals(3, t.keyframes().size());
        assertEquals(0, t.keyframes().get(0).timeUs);
        assertEquals(SEC, t.keyframes().get(1).timeUs);
        assertEquals(2 * SEC, t.keyframes().get(2).timeUs);
    }

    @Test
    public void settingAKeyframeAtTheSameTimeReplacesItAndKeepsItsCurve() {
        Track t = new Track("x", 0);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 100);
        t.setCurve(0, Curve.EASE_IN_OUT);
        t.setKeyframe(0, 50);
        assertEquals(2, t.keyframes().size());
        assertEquals(50, t.keyframes().get(0).value, 1e-9);
        assertEquals(Curve.EASE_IN_OUT, t.keyframes().get(0).curve);
    }

    @Test
    public void easeInOutSegmentIsStillHalfwayAtHalfTime() {
        Track t = new Track("x", 0);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 100);
        t.setCurve(0, Curve.EASE_IN_OUT);
        assertEquals(50, t.valueAt(SEC / 2), 1e-4);
        assertTrue(t.valueAt(SEC / 4) < 25);
    }

    @Test
    public void holdCurveJumpsAtTheNextKeyframe() {
        Track t = new Track("x", 0);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 100);
        t.setCurve(0, Curve.HOLD);
        assertEquals(0, t.valueAt(SEC - 1), 1e-9);
        assertEquals(100, t.valueAt(SEC), 1e-9);
    }

    @Test
    public void curveOnlyAffectsItsOwnSegment() {
        Track t = new Track("x", 0);
        t.setKeyframe(0, 0);
        t.setKeyframe(SEC, 100);
        t.setKeyframe(2 * SEC, 200);
        t.setCurve(0, Curve.HOLD);
        assertEquals(0, t.valueAt(SEC / 2), 1e-9);
        assertEquals(150, t.valueAt(SEC + SEC / 2), 1e-9);
    }

    @Test
    public void removeKeyframe() {
        Track t = new Track("x", 5);
        t.setKeyframe(SEC, 10);
        assertTrue(t.removeKeyframe(SEC));
        assertFalse(t.removeKeyframe(SEC));
        assertFalse(t.hasKeyframes());
        assertEquals(5, t.valueAt(SEC), 1e-9);
    }

    @Test
    public void invalidInputsAreRejected() {
        Track t = new Track("x", 0);
        assertThrows(IllegalArgumentException.class, () -> t.setKeyframe(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> t.setCurve(SEC, Curve.LINEAR));
    }
}
