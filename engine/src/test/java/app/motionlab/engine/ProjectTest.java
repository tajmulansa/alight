package app.motionlab.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ProjectTest {
    private static final long SEC = 1_000_000L;

    private static Project project() {
        return new Project("p", "test", 1080, 1920, 30, 5 * SEC);
    }

    private static Layer shape(String id) {
        return new Layer(id, Layer.Type.SHAPE, id, 0, 5 * SEC);
    }

    @Test
    public void duplicateLayerIdIsRejected() {
        Project p = project();
        p.addLayer(shape("a"));
        assertThrows(IllegalArgumentException.class, () -> p.addLayer(shape("a")));
    }

    @Test
    public void aLayerCannotBeItsOwnParent() {
        Project p = project();
        p.addLayer(shape("a"));
        assertThrows(IllegalArgumentException.class, () -> p.setParent("a", "a"));
    }

    @Test
    public void unknownLayersAreRejected() {
        Project p = project();
        p.addLayer(shape("a"));
        assertThrows(IllegalArgumentException.class, () -> p.setParent("a", "nope"));
        assertThrows(IllegalArgumentException.class, () -> p.setParent("nope", "a"));
    }

    @Test
    public void parentLoopsAreRejected() {
        Project p = project();
        p.addLayer(shape("a"));
        p.addLayer(shape("b"));
        p.addLayer(shape("c"));
        p.setParent("b", "a");
        p.setParent("c", "b");
        assertThrows(IllegalArgumentException.class, () -> p.setParent("a", "c"));
        assertThrows(IllegalArgumentException.class, () -> p.setParent("a", "b"));
        assertNull(p.layer("a").parentId());
    }

    @Test
    public void reparentingAndClearing() {
        Project p = project();
        p.addLayer(shape("a"));
        p.addLayer(shape("b"));
        p.addLayer(shape("c"));
        p.setParent("c", "a");
        p.setParent("c", "b");
        assertEquals("b", p.layer("c").parentId());
        p.setParent("c", null);
        assertNull(p.layer("c").parentId());
    }

    @Test
    public void removingAParentLeavesChildrenWithNoParent() {
        Project p = project();
        p.addLayer(shape("a"));
        p.addLayer(shape("b"));
        p.setParent("b", "a");
        p.removeLayer("a");
        assertNull(p.layer("b").parentId());
        assertEquals(1, p.layers().size());
    }

    @Test
    public void snapToFrameAt30Fps() {
        Project p = project();
        assertEquals(0, p.snapToFrame(0));
        assertEquals(33_333, p.snapToFrame(33_000));
        assertEquals(0, p.snapToFrame(10_000));
        assertEquals(SEC, p.snapToFrame(SEC));
    }

    @Test
    public void layerNeedsAValidTimeRange() {
        assertThrows(IllegalArgumentException.class, () -> new Layer("x", Layer.Type.SHAPE, "x", 500, 100));
        assertThrows(IllegalArgumentException.class, () -> new Layer("x", Layer.Type.SHAPE, "x", -1, 100));
    }
}
