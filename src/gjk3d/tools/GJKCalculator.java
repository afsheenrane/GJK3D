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

        GJKStruct gjkInfo = new GJKStruct();

        gjkInfo.dir = Vec3D.XAXIS;

        Vec3D newPt;

        gjkInfo.simplex.add(getSupport(s1, s2, gjkInfo.dir));
        gjkInfo.dir.negate();

        while (true) {

            newPt = getSupport(s1, s2, gjkInfo.dir);

            // If the new point in the new direction cannot even make it past
            // the origin, then there is no way to encapsulate the origin.
            if (newPt.dot(gjkInfo.dir) < 0) {
                return false;
            }

            gjkInfo.simplex.add(newPt);

            if (computeSimplex(gjkInfo)) {
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
    private boolean computeSimplex(GJKStruct gjkInfo) {

        switch (gjkInfo.simplex.size()) {
            case 2:
                return computeLineSimplex(gjkInfo);
            case 3:
                return computeTriangleSimplex(gjkInfo);
            case 4:
                return computeTetraSimplex(gjkInfo);
            default:
                System.err.println("Simplex size error: "
                        + gjkInfo.simplex.size());
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
    private boolean computeLineSimplex(GJKStruct gjkInfo) {

        Vec3D AB, AO;

        AB = Vec3D.sub(gjkInfo.simplex.get(0), gjkInfo.simplex.get(1));
        AO = gjkInfo.simplex.get(1).getNegated();

        // If AB . AO > 0, the body of the line is closest.
        if (AB.dot(AO) > 0) {
            gjkInfo.dir = AB.cross(AO).cross(AB);
        }
        // Otherwise point A is closest.
        else {
            gjkInfo.simplex.remove(1);
            gjkInfo.dir = AO;
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
    private boolean computeTriangleSimplex(GJKStruct gjkInfo) {

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

        AB = Vec3D.sub(gjkInfo.simplex.get(1), gjkInfo.simplex.get(2)); // B - A
                                                                        // = AB
        AC = Vec3D.sub(gjkInfo.simplex.get(0), gjkInfo.simplex.get(2)); // C - A
                                                                        // = AC
        AO = gjkInfo.simplex.get(2).getNegated();

        ABCnorm = AB.cross(AC);

        ABplaneNorm = AB.cross(ABCnorm);
        ACplaneNorm = ABCnorm.cross(AC);

        if (ABplaneNorm.dot(AO) > 0) { // Somewhere past the AB plane
            if (AB.dot(AO) > 0) { // Past the A vor region, inside AB's vor
                                  // region
                gjkInfo.simplex.remove(0); // So remove C
                gjkInfo.dir = AB.cross(AO).cross(AB);
                return false;
            }
            else { // Inside A's voro region
                gjkInfo.simplex.remove(0);
                gjkInfo.simplex.remove(1);
                gjkInfo.dir = AO;
                return false;
            }
        }

        else if (ACplaneNorm.dot(AO) > 0) { // Somewhere past the AC plane
            if (AC.dot(AO) > 0) { // Past the A voronoi region, inside AC's vor
                                  // region
                gjkInfo.simplex.remove(1); // So remove B
                gjkInfo.dir = AC.cross(AO).cross(AC);
                return false;
            }
            else { // Inside A's voronoi region
                gjkInfo.simplex.remove(0); // Remove C.
                gjkInfo.simplex.remove(1); // Remove B.
                gjkInfo.dir = AO;
                return false;
            }
        }
        else { // On top of or below the triangle.
            double ABCnormDotAO = ABCnorm.dot(AO);
            if (ABCnormDotAO > 0) { // Above plane of triangle.
                // Simplex stays the same.
                gjkInfo.dir = ABCnorm;
                return false;
            }
            else if (ABCnormDotAO < 0) { // Below plane of triangle.
                gjkInfo.dir = ABCnorm.getNegated();

                // Swap B, C to correctly reorient triangle.
                Vec3D tempC = gjkInfo.simplex.get(0); // Hold C in temp.
                gjkInfo.simplex.set(0, gjkInfo.simplex.get(1)); // Put B into
                                                                // C's slot.
                gjkInfo.simplex.set(1, tempC); // Put C into B's slot.
                return false;
            }
            else { // Origin is in the triangle's plane..?
                return true;
                // TODO because i dont actually know if anything bad happens now
            }

        }

    }

    /**
     * Compute the new search direction and new simplex if it is currently a
     * tetraheadron. <br>
     *
     * @param simplex the current tetrahedron simplex.
     * @param dir the current search direction.
     * @return true if the origin is contained within the simplex. False
     *         otherwise.
     */
    private boolean computeTetraSimplex(GJKStruct gjkInfo) {

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

        AO = gjkInfo.simplex.get(3).getNegated();

        AB = Vec3D.sub(gjkInfo.simplex.get(2), gjkInfo.simplex.get(3));
        AC = Vec3D.sub(gjkInfo.simplex.get(1), gjkInfo.simplex.get(3));

        // First test the ABC surface.
        surfaceNorm = AB.cross(AC);

        // If the origin is outside the tetrahedron, update the simplex, search
        // direction and return.
        if (surfaceNorm.dot(AO) > 0) {
            refineSimplex(gjkInfo, surfaceNorm, gjkInfo.simplex.get(2),
                    gjkInfo.simplex.get(1), AB, AC, AO);
            return false;
        }

        AD = Vec3D.sub(gjkInfo.simplex.get(0), gjkInfo.simplex.get(3));
        // Next, test the ADB surface.
        surfaceNorm = AD.cross(AB);

        if (surfaceNorm.dot(AO) > 0) {
            refineSimplex(gjkInfo, surfaceNorm, gjkInfo.simplex.get(0),
                    gjkInfo.simplex.get(2), AD, AB, AO);
            return false;
        }

        // Finally, test the ACD surface.
        surfaceNorm = AC.cross(AD);

        if (surfaceNorm.dot(AO) > 0) {
            refineSimplex(gjkInfo, surfaceNorm, gjkInfo.simplex.get(1),
                    gjkInfo.simplex.get(0), AC, AD, AO);
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
     * added to the simplex. <br>
     * <i> The large number of parameters is mainly to skip unnecessary
     * computation of values which have already been found earlier. </i>
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
    private void refineSimplex(GJKStruct gjkInfo, Vec3D surfaceNorm, Vec3D P,
            Vec3D Q, Vec3D AP, Vec3D AQ, Vec3D AO) {

        Vec3D APnorm = AP.cross(surfaceNorm);
        Vec3D A = gjkInfo.simplex.get(3);

        if (APnorm.dot(AO) > 0) {
            gjkInfo.dir = AP.cross(AO).cross(AP);

            // The new simplex should be a line again.
            gjkInfo.simplex.clear();
            gjkInfo.simplex.add(P);
            gjkInfo.simplex.add(A); // Set the "last added" pt to be A.
            return;
        }

        Vec3D AQnorm = surfaceNorm.cross(AQ);

        if (AQnorm.dot(AO) > 0) {
            gjkInfo.dir = AQ.cross(AO).cross(AQ);

            // The new simplex should be a line again.
            gjkInfo.simplex.clear();
            gjkInfo.simplex.add(Q);
            gjkInfo.simplex.add(A); // Set the "last added" pt to be A.
            return;
        }

        // Else the triangle surface is closest to the origin.
        gjkInfo.dir = surfaceNorm;

        // The new simplex should be a line again.
        gjkInfo.simplex.clear();
        gjkInfo.simplex.add(Q);
        gjkInfo.simplex.add(P);
        gjkInfo.simplex.add(A); // Set the "last added" pt to be A.

    }
}

/**
 * This class holds all the vital information used by th GJK algorithm while it
 * computes whether a collision has taken place.
 * 
 * @author Afsheen
 *
 */
class GJKStruct {

    /**
     * The simplex for the gjk algorithm.
     */
    ArrayList<Vec3D> simplex;

    /**
     * The current search direction.
     */
    Vec3D dir;

    /**
     * Initialize a new GJKStruct with an empty simplex of size 3 and a search
     * direction = [0,0].
     */
    GJKStruct() {
        this.simplex = new ArrayList<Vec3D>(4);
        this.dir = new Vec3D();
    }

}