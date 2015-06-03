package gjk3d.entities;

public abstract class Shape {
    
    /**
     * Used for GJK computation. Calculates the maximum vertex on the shape 
     * in a given direction.
     * @param dir the direction to check in.
     * @return the maximum vertex in the direction dir on the shape.
     */
    public abstract Vec3D support(Vec3D dir);
    
}
