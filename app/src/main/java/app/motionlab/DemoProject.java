package app.motionlab;

import app.motionlab.engine.Curve;
import app.motionlab.engine.Layer;
import app.motionlab.engine.Project;
import java.util.HashMap;
import java.util.Map;

/**
 * A built-in project used to prove the preview: a box that travels with easing and spins,
 * and a smaller square parented to it that orbits as the box rotates.
 */
public final class DemoProject {
    private static final long SEC = 1_000_000L;

    public final Project project;
    public final Map<String, ShapeStyle> styles;

    private DemoProject(Project project, Map<String, ShapeStyle> styles) {
        this.project = project;
        this.styles = styles;
    }

    public static DemoProject create() {
        Project p = new Project("demo", "Demo", 1080, 1920, 30, 5 * SEC);
        Map<String, ShapeStyle> styles = new HashMap<>();

        Layer box = new Layer("box", Layer.Type.SHAPE, "Box", 0, 5 * SEC);
        box.setZOrder(0);
        box.track(Layer.ANCHOR_X).setBaseValue(150);
        box.track(Layer.ANCHOR_Y).setBaseValue(150);
        box.track(Layer.POS_X).setKeyframe(0, 200);
        box.track(Layer.POS_X).setKeyframe(5 * SEC / 2, 880);
        box.track(Layer.POS_X).setKeyframe(5 * SEC, 200);
        box.track(Layer.POS_X).setCurve(0, Curve.EASE_IN_OUT);
        box.track(Layer.POS_X).setCurve(5 * SEC / 2, Curve.EASE_OUT);
        box.track(Layer.POS_Y).setKeyframe(0, 300);
        box.track(Layer.POS_Y).setKeyframe(5 * SEC / 2, 960);
        box.track(Layer.POS_Y).setKeyframe(5 * SEC, 1620);
        box.track(Layer.POS_Y).setCurve(0, Curve.EASE_IN_OUT);
        box.track(Layer.POS_Y).setCurve(5 * SEC / 2, Curve.EASE_OUT);
        box.track(Layer.ROTATION).setKeyframe(0, 0);
        box.track(Layer.ROTATION).setKeyframe(5 * SEC, 360);
        p.addLayer(box);
        styles.put("box", new ShapeStyle(300, 300, 0.90f, 0.25f, 0.25f));

        Layer orbiter = new Layer("orbiter", Layer.Type.SHAPE, "Orbiter", 0, 5 * SEC);
        orbiter.setZOrder(1);
        orbiter.track(Layer.ANCHOR_X).setBaseValue(50);
        orbiter.track(Layer.ANCHOR_Y).setBaseValue(50);
        orbiter.track(Layer.POS_X).setBaseValue(250);
        orbiter.track(Layer.POS_Y).setBaseValue(0);
        p.addLayer(orbiter);
        p.setParent("orbiter", "box");
        styles.put("orbiter", new ShapeStyle(100, 100, 0.20f, 0.40f, 0.90f));

        return new DemoProject(p, styles);
    }
}
