package app.motionlab.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a project and a time into a flat, ordered draw list. Preview and export both use this,
 * so they cannot disagree about where a layer is.
 *
 * Parenting: a child's position, rotation and scale are relative to its parent's position,
 * rotation and scale. The parent's anchor point is not part of that, so a child orbits the
 * parent's position. Opacity is not inherited.
 */
public final class Evaluator {
    private Evaluator() {}

    public static List<DrawItem> evaluate(Project project, long timeUs) {
        Map<String, Matrix2D> spaces = new HashMap<>();
        List<Layer> active = new ArrayList<>();
        for (Layer l : project.layers()) {
            if (l.type().isDrawable() && l.isActiveAt(timeUs)) active.add(l);
        }
        // Stable sort: lower z first, equal z keeps the order layers were added.
        active.sort((x, y) -> Integer.compare(x.zOrder(), y.zOrder()));

        List<DrawItem> out = new ArrayList<>(active.size());
        for (Layer l : active) {
            Matrix2D space = space(project, l, timeUs, spaces, 0);
            double ax = l.valueAt(Layer.ANCHOR_X, timeUs);
            double ay = l.valueAt(Layer.ANCHOR_Y, timeUs);
            Matrix2D draw = space.multiply(Matrix2D.translate(-ax, -ay));
            double opacity = Math.max(0, Math.min(1, l.valueAt(Layer.OPACITY, timeUs)));
            out.add(new DrawItem(l.id(), l.type(), draw, opacity));
        }
        return out;
    }

    /** Position, rotation and scale of a layer combined with those of its parents. */
    private static Matrix2D space(Project p, Layer l, long t, Map<String, Matrix2D> memo, int depth) {
        Matrix2D cached = memo.get(l.id());
        if (cached != null) return cached;
        if (depth > p.layers().size()) throw new IllegalStateException("Parent loop at " + l.id());

        Matrix2D own = Matrix2D.translate(l.valueAt(Layer.POS_X, t), l.valueAt(Layer.POS_Y, t))
                .multiply(Matrix2D.rotateDegrees(l.valueAt(Layer.ROTATION, t)))
                .multiply(Matrix2D.scale(l.valueAt(Layer.SCALE_X, t), l.valueAt(Layer.SCALE_Y, t)));
        Layer parent = l.parentId() == null ? null : p.layer(l.parentId());
        Matrix2D result = parent == null ? own : space(p, parent, t, memo, depth + 1).multiply(own);
        memo.put(l.id(), result);
        return result;
    }
}
