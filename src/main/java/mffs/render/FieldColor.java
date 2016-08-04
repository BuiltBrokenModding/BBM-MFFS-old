package mffs.render;

public enum FieldColor
{
    RED(1f, 0f, 0f), GREEN(0f, 1f, 0f), BLUE(0.5f, 0.9f, 1f);

    public final float r, g, b;
    public final float[] array;

    private FieldColor(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        array = new float[]{r, g, b};
    }

    public float[] toArray()
    {
        return array;
    }
}
