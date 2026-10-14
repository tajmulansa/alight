package app.motionlab.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class EvaluatorTest {
    private static final long SEC = 1_000_000L;
    private static final double EPS = 1e-9;

    private static Project project() {
        return new Project("p", "test", 1080, 1920, 30, 5 * SEC);
    }

    private static Layer add(Project p, String id, Layer.Type type) {
        Layer l = new Layer(id, type, id, 0, 5 * SEC);
        p.addLayer(l);
        return l;
    }

    private static DrawItem item(List<DrawItem> items, String id) {
        for (DrawItem d : items) if (d.layerId.equals(id)) return d;
        throw new AssertionError("not drawn: " + id);
    }

    private static void assertPoint(DrawItem d, double inX, double inY, double outX, double outY) {
        assertEquals("x", outX, d.matrix.applyX(inX, inY), EPS);
        assertEquals("y", outY, d.matrix.applyY(inX, inY), EPS);
    }

    @Test
    public void positionMovesTheLayer() {
        Project p = project();
        Layer a = add(p, "a", Layer.Type.SHAPE);
        a.track(Layer.POS_X).setBaseValue(100);
        a.track(Layer.POS_Y).setBaseValue(50);
        assertPoint(item(Evaluator.evaluate(p, 0), "a"), 0, 0, 100, 50);
    }

    @Test
    public void anchorPointStaysPutOnTheCanvas() {
        Project p = project();
        Layer a = add(p, "a", Layer.Type.SHAPE);
        a.track(Layer.ANCHOR_X).setBaseValue(10);
        a.track(Layer.ANCHOR_Y).setBaseValue(10);
        a.track(Layer.POS_X).setBaseValue(100);
        a.track(Layer.POS_Y).setBaseValue(100);
        a.track(Layer.ROTATION).setBaseValue(90);
        assertPoint(item(Evaluator.evaluate(p, 0), "a"), 10, 10, 100, 100);
    }

    @Test
    public void scaleAndRotation() {
        Project p = project();
        Layer a = add(p, "a", Layer.Type.SHAPE);
        a.track(Layer.SCALE_X).setBaseValue(2);
        assertPoint(item(Evaluator.evaluate(p, 0), "a"), 10, 0, 20, 0);

        a.track(Layer.SCALE_X).setBaseValue(1);
        a.track(Layer.ROTATION).setBaseValue(90);
        assertPoint(item(Evaluator.evaluate(p, 0), "a"), 10, 0, 0, 10);
    }

    @Test
    public void childFollowsParentPosition() {
        Project p = project();
        Layer parent = add(p, "parent", Layer.Type.SHAPE);
        Layer child = add(p, "child", Layer.Type.SHAPE);
        parent.track(Layer.POS_X).setBaseValue(100);
        child.track(Layer.POS_X).setBaseValue(10);
        p.setParent("child", "parent");
        assertPoint(item(Evaluator.evaluate(p, 0), "child"), 0, 0, 110, 0);
    }

    @Test
    public void childOrbitsWhenParentRotates() {
        Project p = project();
        Layer parent = add(p, "parent", Layer.Type.SHAPE);
        Layer child = add(p, "child", Layer.Type.SHAPE);
        parent.track(Layer.ROTATION).setBaseValue(90);
        child.track(Layer.POS_X).setBaseValue(10);
        p.setParent("child", "parent");
        assertPoint(item(Evaluator.evaluate(p, 0), "child"), 0, 0, 0, 10);
    }

    @Test
    public void childScalesWithParent() {
        Project p = project();
        Layer parent = add(p, "parent", Layer.Type.SHAPE);
        Layer child = add(p, "child", Layer.Type.SHAPE);
        parent.track(Layer.SCALE_X).setBaseValue(2);
        parent.track(Layer.SCALE_Y).setBaseValue(2);
        child.track(Layer.POS_X).setBaseValue(10);
        p.setParent("child", "parent");
        assertPoint(item(Evaluator.evaluate(p, 0), "child"), 0, 0, 20, 0);
    }

    @Test
    public void parentAnchorDoesNotMoveTheChildOrigin() {
        Project p = project();
        Layer parent = add(p, "parent", Layer.Type.SHAPE);
        Layer child = add(p, "child", Layer.Type.SHAPE);
        parent.track(Layer.ANCHOR_X).setBaseValue(50);
        parent.track(Layer.ANCHOR_Y).setBaseValue(50);
        parent.track(Layer.POS_X).setBaseValue(100);
        parent.track(Layer.POS_Y).setBaseValue(100);
        p.setParent("child", "parent");
        assertPoint(item(Evaluator.evaluate(p, 0), "child"), 0, 0, 100, 100);
    }

    @Test
    public void chainOfParents() {
        Project p = project();
        Layer a = add(p, "a", Layer.Type.SHAPE);
        Layer b = add(p, "b", Layer.Type.SHAPE);
        Layer c = add(p, "c", Layer.Type.SHAPE);
        a.track(Layer.POS_X).setBaseValue(100);
        b.track(Layer.POS_X).setBaseValue(20);
        c.track(Layer.POS_X).setBaseValue(3);
        p.setParent("b", "a");
        p.setParent("c", "b");
        assertPoint(item(Evaluator.evaluate(p, 0), "c"), 0, 0, 123, 0);
    }

    @Test
    public void nullLayerMovesChildrenButIsNotDrawn() {
        Project p = project();
        Layer rig = add(p, "rig", Layer.Type.NULL);
        add(p, "box", Layer.Type.SHAPE);
        rig.track(Layer.POS_X).setBaseValue(200);
        p.setParent("box", "rig");
        List<DrawItem> items = Evaluator.evaluate(p, 0);
        assertEquals(1, items.size());
        assertPoint(item(items, "box"), 0, 0, 200, 0);
    }

    @Test
    public void audioIsNeverDrawn() {
        Project p = project();
        add(p, "song", Layer.Type.AUDIO);
        assertTrue(Evaluator.evaluate(p, 0).isEmpty());
    }

    @Test
    public void layersOnlyAppearInsideTheirTimeRange() {
        Project p = project();
        Layer a = new Layer("a", Layer.Type.SHAPE, "a", SEC, 2 * SEC);
        p.addLayer(a);
        assertEquals(0, Evaluator.evaluate(p, SEC / 2).size());
        assertEquals(1, Evaluator.evaluate(p, SEC).size());
        assertEquals(1, Evaluator.evaluate(p, 2 * SEC - 1).size());
        assertEquals(0, Evaluator.evaluate(p, 2 * SEC).size());
    }

    @Test
    public void hiddenLayersAreSkipped() {
        Project p = project();
        Layer a = add(p, "a", Layer.Type.SHAPE);
        a.setVisible(false);
        assertTrue(Evaluator.evaluate(p, 0).isEmpty());
    }

    @Test
    public void drawListIsOrderedByZ() {
        Project p = project();
        add(p, "top", Layer.Type.SHAPE).setZOrder(5);
        add(p, "bottom", Layer.Type.SHAPE).setZOrder(1);
        add(p, "middle", Layer.Type.SHAPE).setZOrder(3);
        List<DrawItem> items = Evaluator.evaluate(p, 0);
        assertEquals("bottom", items.get(0).layerId);
        assertEquals("middle", items.get(1).layerId);
        assertEquals("top", items.get(2).layerId);
    }

    @Test
    public void keyframedPositionAndOpacity() {
        Project p = project();
        Layer a = add(p, "a", Layer.Type.SHAPE);
        a.track(Layer.POS_X).setKeyframe(0, 0);
        a.track(Layer.POS_X).setKeyframe(SEC, 100);
        a.track(Layer.OPACITY).setKeyframe(0, 0);
        a.track(Layer.OPACITY).setKeyframe(SEC, 1);
        DrawItem d = item(Evaluator.evaluate(p, SEC / 4), "a");
        assertPoint(d, 0, 0, 25, 0);
        assertEquals(0.25, d.opacity, EPS);
    }

    @Test
    public void opacityIsClampedAndNotInherited() {
        Project p = project();
        Layer parent = add(p, "parent", Layer.Type.SHAPE);
        Layer child = add(p, "child", Layer.Type.SHAPE);
        parent.track(Layer.OPACITY).setBaseValue(0.2);
        child.track(Layer.OPACITY).setBaseValue(1.7);
        p.setParent("child", "parent");
        List<DrawItem> items = Evaluator.evaluate(p, 0);
        assertEquals(1.0, item(items, "child").opacity, EPS);
        assertEquals(0.2, item(items, "parent").opacity, EPS);
    }
}
