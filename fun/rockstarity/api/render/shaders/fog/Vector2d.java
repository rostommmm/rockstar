package fun.rockstarity.api.render.shaders.fog;

import lombok.With;

public class Vector2d
{
    public static final Vector2d ZERO = new Vector2d(0.0F, 0.0F);
    public static final Vector2d ONE = new Vector2d(1.0F, 1.0F);
    public static final Vector2d UNIT_X = new Vector2d(1.0F, 0.0F);
    public static final Vector2d NEGATIVE_UNIT_X = new Vector2d(-1.0F, 0.0F);
    public static final Vector2d UNIT_Y = new Vector2d(0.0F, 1.0F);
    public static final Vector2d NEGATIVE_UNIT_Y = new Vector2d(0.0F, -1.0F);
    public static final Vector2d MAX = new Vector2d(Float.MAX_VALUE, Float.MAX_VALUE);
    public static final Vector2d MIN = new Vector2d(Float.MIN_VALUE, Float.MIN_VALUE);
    @With public final double x;
    @With public final double y;

    public Vector2d(double xIn, double yIn)
    {
        this.x = xIn;
        this.y = yIn;
    }

    public boolean equals(Vector2d other)
    {
        return this.x == other.x && this.y == other.y;
    }
    
    public float distance(Vector2d other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
}
