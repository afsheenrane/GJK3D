package gjk3d.entities;

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
    
    /**
     * Get the length of the vector.
     * @return the scalar length of this vector.
     */
    public double getLength(){
        return Math.sqrt(getSquaredLength());
    }

    /**
     * Get the squared length of the vector. Useful for cheap vector 
     * comparisons. 
     * @return the scalar squared length of this vector.
     */
    public double getSquaredLength(){
        return (x * x) + (y * y) + (z * z);
    }
    
    /**
     * Get a normalized copy of this vector. 
     * @return a normalized copy of this vector.
     */
    public Vec3D getNormalized(){
        double length = getLength();
        length = (length == 0 ? 1 : length);

        return new Vec3D(x / length, y / length, z / length);
    }

    /**
     * Get the dot product of v1 and v2.
     * @param v1 the first vector.
     * @param v2 the second vector.
     * @return the scalar dot product of v1 and v2.
     */
    public static double dot(Vec3D v1, Vec3D v2){
    	return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z);
    }
        
    /**
     * The dot product of this vector to the other vector v.
     * @param v the vector to dot product with.
     * @return the dot product of this vector and v.
     */
    public double dot(Vec3D v){
        return Vec3D.dot(this, v);
    }
    
    /**
     * Get the cross product of vectors v1 and v2.
     * @param v1 the first vector.
     * @param v2 the second vector.
     * @return the vector cross product of v1 and v2.
     */
    public static Vec3D cross(Vec3D v1, Vec3D v2){
    	double x, y, z;
    	x = (v1.y * v2.getZ()) - (v1.z * v2.getY());
    	y = (v1.z * v2.getX()) - (v1.x * v2.getZ());
    	z = (v1.x * v2.getY()) - (v1.y * v2.getX());
    	
    	return new Vec3D(x, y, z);
    }
    
    /**
     * Get the cross product of this vector and v.
     * @param v the vector to cross product with.
     * @return the cross product vector of this and v.
     */
    public Vec3D cross(Vec3D v){
    	return Vec3D.cross(this, v);
    }
    
    /**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(double z) {
		this.z = z;
	}
    
    
}
