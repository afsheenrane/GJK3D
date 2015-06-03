package gjk3d.entities;

public class Polyhedron extends Shape {

    private Vec3D[] vertices;

    public Polyhedron(Vec3D[] vertices) {
        this.vertices = vertices;
    }

    /**
     * @return the vertices
     */
    public Vec3D[] getVertices() {
        return vertices;
    }

    /**
     * @param vertices the vertices to set
     */
    public void setVertices(Vec3D[] vertices) {
        this.vertices = vertices;
    }

    @Override
    public Vec3D support(Vec3D dir) {
        Vec3D maxVec = vertices[0];
        double maxDot = vertices[0].dot(dir);

        double curDot;

        for (Vec3D v : vertices) {
            curDot = v.dot(dir);

            if (curDot > maxDot) {
                maxDot = curDot;
                maxVec = v;
            }

        }
        return maxVec;
    }
}
