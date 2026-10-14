package app.motionlab.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CurveTest {
    @Test
    public void linearIsIdentity() {
        for (double u = 0; u <= 1.0001; u += 0.1) {
            assertEquals(u, Curve.LINEAR.apply(u), 1e-9);
        }
    }

    @Test
    public void bezierHitsBothEnds() {
        assertEquals(0, Curve.EASE_IN_OUT.apply(0), 1e-9);
        assertEquals(1, Curve.EASE_IN_OUT.apply(1), 1e-9);
    }

    @Test
    public void easeInOutIsSymmetric() {
        assertEquals(0.5, Curve.EASE_IN_OUT.apply(0.5), 1e-6);
        assertEquals(1.0, Curve.EASE_IN_OUT.apply(0.25) + Curve.EASE_IN_OUT.apply(0.75), 1e-6);
    }

    @Test
    public void easeInStartsSlowAndEaseOutStartsFast() {
        assertTrue(Curve.EASE_IN.apply(0.25) < 0.25);
        assertTrue(Curve.EASE_OUT.apply(0.25) > 0.25);
    }

    @Test
    public void easeInOutNeverGoesBackwards() {
        double prev = 0;
        for (double u = 0; u <= 1.0001; u += 0.01) {
            double v = Curve.EASE_IN_OUT.apply(u);
            assertTrue("went backwards at " + u, v >= prev - 1e-9);
            prev = v;
        }
    }

    @Test
    public void overshootGoesPastOne() {
        Curve back = Curve.bezier(0.34, 1.56, 0.64, 1);
        double max = 0;
        for (double u = 0; u <= 1.0001; u += 0.01) max = Math.max(max, back.apply(u));
        assertTrue("max was " + max, max > 1.0);
        assertEquals(1, back.apply(1), 1e-9);
    }

    @Test
    public void handleXOutsideZeroToOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Curve.bezier(1.5, 0, 0.5, 1));
        assertThrows(IllegalArgumentException.class, () -> Curve.bezier(0.5, 0, -0.1, 1));
    }

    @Test
    public void holdStaysAtStartUntilTheEnd() {
        assertEquals(0, Curve.HOLD.apply(0.5), 1e-9);
        assertEquals(0, Curve.HOLD.apply(0.99), 1e-9);
        assertEquals(1, Curve.HOLD.apply(1), 1e-9);
    }
}
