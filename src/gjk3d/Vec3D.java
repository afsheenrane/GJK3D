package gjk3d;

public class Vec3D {

    private double x, y, z;

    public static final Vec3D ORIGIN = new Vec3D();

    /**
     * Initialize a vector to [0, 0, 0]
     */
    public Vec3D(){
        this(0,0,0);
    }

    /**
     * Initialize a vector to [x, y, z]
     * @param x the x extent of the vector.
     * @param y the y extent of the vector.
     * @param z the z extent of the vector.
     */
    public Vec3D(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getLength(){
        return Math.sqrt(getSquaredLength());
    }

    public double getSquaredLength(){
        return (x * x) + (y * y) + (z * z);
    }

    public Vec3D getNormalized(){
        double length = getLength();
        length = (length == 0 ? 1 : length);

        return new Vec3D(x / length, y / length, z / length);
    }

    public double dot(Vec3D v){
        return (x * v.x) + (y * v.y) + (z * v.z);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
