package cadcore;

import java.awt.geom.Point2D;

public class VecMath {


	//Utility vector functions
	public static double getVecLength(double x0, double y0, double x1, double y1)
	{
		double dx = x0-x1;
		double dy = y0-y1;

		return Math.sqrt((dx*dx)+(dy*dy));
	}

	static double getVecLength(Point2D.Double p0, Point2D.Double p1)
	{
		return getVecLength(p0.x, p0.y, p1.x, p1.y);
	}

	public static double getVecLength(Point2D.Double p0)
	{
		return getVecLength(p0.x, p0.y, 0, 0);
	}

	public static void subVector(Point2D.Double p0, Point2D.Double p1, Point2D.Double ret)
	{
		ret.setLocation(p1.x-p0.x, p1.y-p0.y);
	}

	public static void addVector(Point2D.Double p0, Point2D.Double p1, Point2D.Double ret)
	{
		ret.setLocation(p1.x+p0.x, p1.y+p0.y);
	}

	public static void scaleVector(Point2D.Double p0, double v)
	{
		p0.setLocation(p0.x*v,p0.y*v);
	}
	
	public static void normalizeVector(Point2D.Double p0)
	{
		
		scaleVector(p0, 1.0/getVecLength(p0));
	}

	static double getVecDot(Point2D.Double p0, Point2D.Double p1)
	{
		return (p0.x*p1.x + p0.y*p1.y);
	}

	public static double getVecAngle(Point2D.Double p0, Point2D.Double p1)
	{
		double angle = Math.acos(getVecDot(p0,p1)/(getVecLength(p0)*getVecLength(p1)));
		
		return Double.isNaN(angle)?0:angle;
	}
	
}
