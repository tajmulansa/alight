package app.motionlab.engine;

/** One layer to draw for a frame: where it goes and how opaque it is. */
public final class DrawItem {
    public final String layerId;
    public final Layer.Type type;
    /** Maps the layer's own pixels (origin at its top-left) onto the canvas. */
    public final Matrix2D matrix;
    public final double opacity;

    public DrawItem(String layerId, Layer.Type type, Matrix2D matrix, double opacity) {
        this.layerId = layerId;
        this.type = type;
        this.matrix = matrix;
        this.opacity = opacity;
    }
}
