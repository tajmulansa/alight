package app.motionlab;

/** How a shape layer looks: a solid rectangle. (Temporary until styling lives in the project.) */
public final class ShapeStyle {
    public final float width;
    public final float height;
    public final float red;
    public final float green;
    public final float blue;

    public ShapeStyle(float width, float height, float red, float green, float blue) {
        this.width = width;
        this.height = height;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
