package app.motionlab.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A project: canvas size, frame rate, length and its layers. */
public final class Project {
    private final String id;
    private String name;
    private final int width;
    private final int height;
    private final double frameRate;
    private long durationUs;
    private final List<Layer> layers = new ArrayList<>();

    public Project(String id, String name, int width, int height, double frameRate, long durationUs) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Canvas size must be positive");
        if (frameRate <= 0) throw new IllegalArgumentException("Frame rate must be positive");
        if (durationUs <= 0) throw new IllegalArgumentException("Duration must be positive");
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.durationUs = durationUs;
    }

    public String id() { return id; }
    public String name() { return name; }
    public void setName(String name) { this.name = name; }
    public int width() { return width; }
    public int height() { return height; }
    public double frameRate() { return frameRate; }
    public long durationUs() { return durationUs; }

    public void setDurationUs(long durationUs) {
        if (durationUs <= 0) throw new IllegalArgumentException("Duration must be positive");
        this.durationUs = durationUs;
    }

    public List<Layer> layers() {
        return Collections.unmodifiableList(layers);
    }

    public Layer layer(String layerId) {
        for (Layer l : layers) {
            if (l.id().equals(layerId)) return l;
        }
        return null;
    }

    public void addLayer(Layer layer) {
        if (layer(layer.id()) != null) {
            throw new IllegalArgumentException("Duplicate layer id: " + layer.id());
        }
        layers.add(layer);
    }

    /** Removes a layer. Layers that had it as their parent are left with no parent. */
    public boolean removeLayer(String layerId) {
        Layer target = layer(layerId);
        if (target == null) return false;
        layers.remove(target);
        for (Layer l : layers) {
            if (layerId.equals(l.parentId())) l.setParentId(null);
        }
        return true;
    }

    /**
     * Links a child to a parent, or clears the link when parentId is null.
     * Rejects unknown layers, a layer as its own parent, and any loop in the chain.
     */
    public void setParent(String childId, String parentId) {
        Layer child = layer(childId);
        if (child == null) throw new IllegalArgumentException("Unknown layer: " + childId);
        if (parentId == null) {
            child.setParentId(null);
            return;
        }
        if (layer(parentId) == null) throw new IllegalArgumentException("Unknown layer: " + parentId);
        if (childId.equals(parentId)) throw new IllegalArgumentException("A layer cannot be its own parent");
        String cursor = parentId;
        int guard = 0;
        while (cursor != null) {
            if (cursor.equals(childId)) throw new IllegalArgumentException("That would create a parent loop");
            Layer next = layer(cursor);
            cursor = next == null ? null : next.parentId();
            if (++guard > layers.size() + 1) throw new IllegalStateException("Existing parent loop");
        }
        child.setParentId(parentId);
    }

    /** Rounds a time to the nearest frame boundary. */
    public long snapToFrame(long timeUs) {
        long frames = Math.round(timeUs * frameRate / 1_000_000.0);
        return Math.round(frames * 1_000_000.0 / frameRate);
    }
}
