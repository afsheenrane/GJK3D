package gjk3d.tools;

import gjk3d.entities.Shape;
import gjk3d.entities.Vec3D;

import java.util.ArrayList;

public class GJKCalculator {

    /**
     * Using GJK, return whether the shapes s1 and s2 are colliding.
     * 
     * @param s1 the first shape.
     * @param s2 the second shape.
     * @return true if s1 and s2 intersect, false otherwise.
     */
    public boolean isColliding(Shape s1, Shape s2) {

        Vec3D dir = Vec3D.XAXIS;

        Vec3D newPt;

        ArrayList<Vec3D> simplex = new ArrayList<>(4);

        simplex.add(getSupport(s1, s2, dir));
        dir.negate();

        while (true) {

            newPt = getSupport(s1, s2, dir);

            // If the new point in the new direction cannot even make it past
            // the origin, then there is no way to encapsulate the origin.
            if (newPt.dot(dir) < 0) {
                return false;
            }

            simplex.add(newPt);

            if (computeSimplex(simplex, dir)) {
                return true;
            }

        }

    }

    /**
     * Returns the support point of the minkowski difference of s1 and s2 in
     * direction dir.
     * 
     * @param s1 the first shape.
     * @param s2 the second shape.
     * @param dir the direction to get the support point in.
     * @return the corresponding support mapping of dir for s1 - s2.
     */
    private Vec3D getSupport(Shape s1, Shape s2, Vec3D dir) {
        return Vec3D.sub(s1.support(dir), s2.support(Vec3D.getNegated(dir)));
    }

    /**
     * Modifies the simplex according to it's current characteristics and change
     * the search direction if needed.
     * 
     * @param simplex the simplex computed so far.
     * @param dir the search direction.
     * @return true if the origin is inside the simplex, false otherwise.
     */
    private boolean computeSimplex(ArrayList<Vec3D> simplex, Vec3D dir) {

        switch (simplex.size()) {
            case 2:
                return computeLineSimplex(simplex, dir);
            case 3:
                return computeTriangleSimplex(simplex, dir);
            case 4:
                return computeTetraSimplex(simplex, dir);
            default:
                System.err.println("Simplex size error: " + simplex.size());
                System.exit(0);
        }

        return false;
    }

    /**
     * Compute the new search direction and new simplex if it is currently a
     * line. <br>
     * The first point cannot be the closest feature to the origin. This is
     * because it was already deduced that the newly added point is in the
     * direction of the origin. Therefore, we only need to check if the new
     * point is closest to the origin, or whether the line body is closest.
     * 
     * @param simplex the simplex computed thus far.
     * @param dir the current search direction.
     * @return false because it is not possible to enclose the origin with only
     *         two points in R3.
     */
    private boolean computeLineSimplex(ArrayList<Vec3D> simplex, Vec3D dir) {

        Vec3D AB, AO;

        AB = Vec3D.sub(simplex.get(0), simplex.get(1));
        AO = simplex.get(1).getNegated();

        // If AB . AO > 0, the body of the line is closest.
        if (AB.dot(AO) > 0) {
            dir = AB.cross(AO).cross(AB);
        }
        // Otherwise point A is closest.
        else {
            simplex.remove(1);
            dir = AO;
        }

        return false;
    }

    /**
     * Compute the new search direction and new simplex if it is currently a
     * triangle. <br>
     * Like the line case, B, C, or BC cannot the closest features to the
     * origin. So they are automatically discarded and checks are not done.
     * 
     * @param simplex the simplex computed thus far.
     * @param dir the current search direction.
     * @return
     */
    private boolean computeTriangleSimplex(ArrayList<Vec3D> simplex, Vec3D dir) {

        //@formatter:off 
        
        /* 
         * Triangle:
         * ....A....
         * .../.\...
         * ../...\..
         * .B_____C.
         */ 
        
        //@formatter:on

        /*
         * A is the newest point added. So we dont have to check edge plane BC
         * because the origin is not there. We also don't have to check B or C.
         */

        Vec3D AB, AC, AO;
        Vec3D ABplaneNorm, ACplaneNorm, ABCnorm;

        AB = Vec3D.sub(simplex.get(1), simplex.get(2)); // B - A = AB
        AC = Vec3D.sub(simplex.get(0), simplex.get(2)); // C - A = AC
        AO = simplex.get(2).getNegated();

        ABCnorm = AB.cross(AC);

        ABplaneNorm = AB.cross(ABCnorm);
        ACplaneNorm = ABCnorm.cross(AC);

        if (ABplaneNorm.dot(AO) > 0) { // Somewhere past the AB plane
            if (AB.dot(AO) > 0) { // Past the A vor region, inside AB's vor
                                  // region
                simplex.remove(0); // So remove C
                dir = AB.cross(AO).cross(AB);
                return false;
            }
            else { // Inside A's voro region
                simplex.remove(0);
                simplex.remove(1);
                dir = AO;
                return false;
            }
        }

        else if (ACplaneNorm.dot(AO) > 0) { // Somewhere past the AC plane
            if (AC.dot(AO) > 0) { // Past the A voronoi region, inside AC's vor
                                  // region
                simplex.remove(1); // So remove B
                dir = AC.cross(AO).cross(AC);
                return false;
            }
            else { // Inside A's voronoi region
                simplex.remove(0); // Remove C.
                simplex.remove(1); // Remove B.
                dir = AO;
                return false;
            }
        }
        else { // On top of or below the triangle.
            double ABCnormDotAO = ABCnorm.dot(AO);
            if (ABCnormDotAO > 0) { // Above plane of triangle.
                // Simplex stays the same.
                dir = ABCnorm;
                return false;
            }
            else if (ABCnormDotAO < 0) { // Below plane of triangle.
                dir = ABCnorm.getNegated();

                // Swap B, C to correctly reorient triangle.
                Vec3D tempC = simplex.get(0); // Hold C in temp.
                simplex.set(0, simplex.get(1)); // Put B into C's slot.
                simplex.set(1, tempC); // Put C into B's slot.
                return false;
            }
            else { // Origin is in the triangle's plane..?
                return true;
                // TODO because i dont actually know if anything bad happens now
            }

        }

    }

    private boolean computeTetraSimplex(ArrayList<Vec3D> simplex, Vec3D dir) {

        //@formatter:off 
        
        /* 
         * Tetrahedron: (I tried).
         * ....D....
         * .../|\...
         * ../ A \..
         * ./ / \ \.
         * B_*___*_C
         * 
         * A=3,B=2,C=1,D=0
         */ 
        
        //@formatter:on

        // The normal of the current triangle surface being tested.
        Vec3D surfaceNorm;
        Vec3D AB, AC, AO, AD;

        AO = simplex.get(3).getNegated();

        AB = Vec3D.sub(simplex.get(2), simplex.get(3));
        AC = Vec3D.sub(simplex.get(1), simplex.get(3));

        // First test the ABC surface.
        surfaceNorm = AB.cross(AC);

        // If the origin is outside the tetrahedron, update the simplex, search
        // direction and return.
        if (surfaceNorm.dot(AO) > 0) {
            refineSimplex(simplex, dir, surfaceNorm, simplex.get(2),
                    simplex.get(1), AB, AC, AO);
            return false;
        }

        AD = Vec3D.sub(simplex.get(0), simplex.get(3));
        // Next, test the ADB surface.
        surfaceNorm = AD.cross(AB);

        if (surfaceNorm.dot(AO) > 0) {
            refineSimplex(simplex, dir, surfaceNorm, simplex.get(0),
                    simplex.get(2), AD, AB, AO);
            return false;
        }

        // Finally, test the ACD surface.
        surfaceNorm = AC.cross(AD);

        if (surfaceNorm.dot(AO) > 0) {
            refineSimplex(simplex, dir, surfaceNorm, simplex.get(1),
                    simplex.get(0), AC, AD, AO);
            return false;
        }

        // No need to test BCD surface because with the addition of A, we
        // already know that the origin is not in front of it.
        // Therefore, the origin is contained within the tetrahedron simplex.
        return true;

    }

    /**
     * Remove all unnecessary vertices from the simplex (those which are not
     * closest to the origin) and refine the search direction. The surface is
     * defined by the CCW triangle APQ. <br>
     * 'A' does not need to be passed in because it is known to be the last item
     * added to the simplex.
     * 
     * @param simplex the current tetrahedron simplex.
     * @param dir the current search direction.
     * @param surfaceNorm the normal of the surface being checked.
     * @param P the 2nd vertex of the triangle.
     * @param Q the 3nd vertex of the triangle.
     * @param AP the AP vector.
     * @param AQ the AQ vector.
     * @param AO the reference vector pointing at the origin.
     */
    private void refineSimplex(ArrayList<Vec3D> simplex, Vec3D dir,
            Vec3D surfaceNorm, Vec3D P, Vec3D Q, Vec3D AP, Vec3D AQ, Vec3D AO) {

        Vec3D APnorm = AP.cross(surfaceNorm);
        Vec3D A = simplex.get(3);

        if (APnorm.dot(AO) > 0) {
            dir = AP.cross(AO).cross(AP);

            // The new simplex should be a line again.
            simplex.clear();
            simplex.add(P);
            simplex.add(A); // Set the "last added" pt to be A.
            return;
        }

        Vec3D AQnorm = surfaceNorm.cross(AQ);

        if (AQnorm.dot(AO) > 0) {
            dir = AQ.cross(AO).cross(AQ);

            // The new simplex should be a line again.
            simplex.clear();
            simplex.add(Q);
            simplex.add(A); // Set the "last added" pt to be A.
            return;
        }

        // Else the triangle surface is closest to the origin.
        dir = surfaceNorm;

        // The new simplex should be a line again.
        simplex.clear();
        simplex.add(Q);
        simplex.add(P);
        simplex.add(A); // Set the "last added" pt to be A.

    }
}
