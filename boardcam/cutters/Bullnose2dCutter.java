package boardcam.cutters;

import cadcore.*;
import board.AbstractBoard;

import javax.vecmath.*;

/**
 * A flat bottom cutter with rounded corners which allows curved rails to 
 * be cut without the need for a 4th axis so the 4th axis has not been implemented.
 * 
 * The 2d profile is defined by the flatRadius and cornerRadius parameters.
 * The cut is made by a point contact along the 90 deg arc of the corner,
 * where the input surface normal lines up.
 */
public class Bullnose2dCutter extends AbstractCutter
{
	private double cornerRadius;
	private double flatRadius;
	
    /**
     * Constructor that sets the dimensions of the cutter
     *
     * @param flatRadius   	The radius of the flat bottom in millimeters.
     * @param cornerRadius	The surface normal in machine coordinates.
     */	public Bullnose2dCutter(double flatRadius, double cornerRadius)
	{
		this.flatRadius = flatRadius;
		this.cornerRadius = cornerRadius;
	}
	
    /**
     * Calculates tool compensation based on the surface normal and adds it to the position.
     * This cutter will always meet the board surface along the corner radius from 0-90 degrees normal. 
     *
     * @param pos   	The position in millimeters using machine coordinates.
     * @param normal	The surface normal in machine coordinates (0 deg = vertical, see [TBD] for machine coordinates).
     * @param board		Unused.
     * @return			The position in millimeters with tool compensation applied.
     */
	public double[] calcOffset(Point3d pos, Vector3d normal, AbstractBoard board)
	{	
		// Make a left pointing vector and get normal vector as seen from above
		Vector2d leftVec2D = new Vector2d(0.0, 1.0);
		Vector2d normalFromAbove = new Vector2d(normal.x, normal.y);
		
		// Find angle the vector is from pointing straight left as seen from above
		double headingAngle = Math.atan2(normalFromAbove.y, normalFromAbove.x) - Math.atan2(leftVec2D.y, leftVec2D.x);// Vector2d.angle() doesn't work because it ignores sign
		
		// Rotate the normal so it's pointing left
		Matrix4d rotateLeft = new Matrix4d();
		rotateLeft.setIdentity();
		rotateLeft.rotZ(-headingAngle);
		Vector3d normalRotated = new Vector3d(normal);
		rotateLeft.transform(normalRotated);

		// Get 2D vector of normal pointing sideways (left)
		Vector2d normalFromSide = new Vector2d(normalRotated.z, normalRotated.y);
		
		// Find angle the vector is from pointing straight left as seen from side
		double cornerAngle = normalFromSide.angle(leftVec2D); // we want abs(angle) here so use Vector2D.angle()
		
		// Get the point on the cutter profile with same angle
		double horzComp = flatRadius + cornerRadius * Math.sin(cornerAngle);
		double vertComp = -cornerRadius * Math.cos(cornerAngle);
		Vector3d toolCompensation = new Vector3d(0.0, horzComp, vertComp);

		// Rotate the point on the surface to the direction of the normal
		rotateLeft.invert();
		rotateLeft.transform(toolCompensation);
		
		// Add tool compensation to original position
		Point3d toolCompensatedPos = new Point3d(pos);
		toolCompensatedPos.add(toolCompensation);
		
		/* DEBUG
		pos.x /= 25.4;
		pos.y /= 25.4;
		pos.z /= 25.4;
		normal.x /= 25.4;
		normal.y /= 25.4;
		normal.z /= 25.4;
		toolCompensation.x /= 25.4;
		toolCompensation.y /= 25.4;
		toolCompensation.z /= 25.4;
		headingAngle *= 180 / Math.PI;
		cornerAngle *= 180 / Math.PI;
		hComp /= 25.4;
		vComp /= 25.4;
		
		System.out.printf("%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n", 
				pos.x, pos.y, pos.z,
				normal.x, normal.y, normal.z,
				toolCompensation.x, toolCompensation.y, toolCompensation.z,
				headingAngle, cornerAngle, hComp, vComp);
		 */
		
		return new double[] { toolCompensatedPos.x, toolCompensatedPos.y, toolCompensatedPos.z };
	}
	
    /**
     * Adapts method to transform inputs from boardcad coordinates to machine coordinates
     * and calls the other calcOffset().
     * Also, translates myPoint inputs to Point3d, Vector3d and double[].
     *
     * @param pos   	The position in boardcad coordinates and [TBD units].
     * @param normal	The surface normal in boardcad coordinates (0 deg = vertical, see [TBD] for boardcad coordinates).
     * @return			The position with tool compensation applied.
     */
	public NurbsPoint calcOffset(NurbsPoint pos, NurbsPoint normal)
	{	
		Vector3d normalMachineCoords = new Vector3d(normal.x, -normal.z, normal.y);
		double[] res = calcOffset(new Point3d(pos.x, -pos.z, pos.y), normalMachineCoords, null);
		return new NurbsPoint(res[0],res[2],res[1]);
	}
}

