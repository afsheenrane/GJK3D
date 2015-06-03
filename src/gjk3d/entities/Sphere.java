package gjk3d.entities;

public class Sphere extends Shape {

    private Vec3D pos;
    private double radius;

    /**
     * Create a new sphere centered at 'pos' with a radius of 'radius'.
     * 
     * @param pos the center of the sphere.
     * @param radius the radius of the sphere.
     */
    public Sphere(Vec3D pos, double radius) {
        this.pos = pos;
        this.radius = radius;
    }

    /**
     * @return the pos
     */
    public Vec3D getPos() {
        return pos;
    }

    /**
     * @param pos the pos to set
     */
    public void setPos(Vec3D pos) {
        this.pos = pos;
    }

    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public Vec3D support(Vec3D dir) {
        Vec3D posDisp = dir.getNormalized();
        posDisp.scaleBy(radius);

        return Vec3D.add(pos, posDisp);

    }
}
