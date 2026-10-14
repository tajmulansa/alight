package app.motionlab.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One layer on the timeline, with its transform and opacity as animatable tracks. */
public final class Layer {
    public enum Type {
        SHAPE, TEXT, IMAGE, VIDEO, AUDIO, NULL, CAMERA, GROUP, ELEMENT;

        /** Whether this type puts pixels on the canvas by itself. */
        public boolean isDrawable() {
            return this == SHAPE || this == TEXT || this == IMAGE || this == VIDEO || this == ELEMENT;
        }
    }

    public static final String POS_X = "position.x";
    public static final String POS_Y = "position.y";
    public static final String SCALE_X = "scale.x";
    public static final String SCALE_Y = "scale.y";
    public static final String ROTATION = "rotation"; // degrees
    public static final String OPACITY = "opacity";   // 0..1
    public static final String ANCHOR_X = "anchor.x";
    public static final String ANCHOR_Y = "anchor.y";

    private final String id;
    private final Type type;
    private String name;
    private int zOrder;
    private long startUs;
    private long endUs;
    private boolean visible = true;
    private String parentId; // null when there is no parent
    private final Map<String, Track> tracks = new LinkedHashMap<>();

    public Layer(String id, Type type, String name, long startUs, long endUs) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("Layer id is required");
        this.id = id;
        this.type = type;
        this.name = name;
        setTimeRange(startUs, endUs);
        addTrack(POS_X, 0);
        addTrack(POS_Y, 0);
        addTrack(SCALE_X, 1);
        addTrack(SCALE_Y, 1);
        addTrack(ROTATION, 0);
        addTrack(OPACITY, 1);
        addTrack(ANCHOR_X, 0);
        addTrack(ANCHOR_Y, 0);
    }

    private void addTrack(String path, double base) {
        tracks.put(path, new Track(path, base));
    }

    /** Gets a track by path, creating it with a base value of 0 if it does not exist yet. */
    public Track track(String path) {
        Track t = tracks.get(path);
        if (t == null) {
            t = new Track(path, 0);
            tracks.put(path, t);
        }
        return t;
    }

    public Collection<Track> tracks() {
        return Collections.unmodifiableCollection(tracks.values());
    }

    public double valueAt(String path, long timeUs) {
        return track(path).valueAt(timeUs);
    }

    public String id() { return id; }
    public Type type() { return type; }
    public String name() { return name; }
    public void setName(String name) { this.name = name; }
    public int zOrder() { return zOrder; }
    public void setZOrder(int zOrder) { this.zOrder = zOrder; }
    public long startUs() { return startUs; }
    public long endUs() { return endUs; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public String parentId() { return parentId; }

    public void setTimeRange(long startUs, long endUs) {
        if (startUs < 0 || endUs <= startUs) {
            throw new IllegalArgumentException("Layer needs 0 <= start < end");
        }
        this.startUs = startUs;
        this.endUs = endUs;
    }

    public boolean isActiveAt(long timeUs) {
        return visible && timeUs >= startUs && timeUs < endUs;
    }

    /** Package-private: Project owns parent links so it can reject cycles. */
    void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
