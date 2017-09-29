package board;

import javax.vecmath.Vector3d;

public abstract class AbstractBoard 
{
	abstract public double getLength();
	
	abstract public double getWidthAt(double x);	
	abstract public double getDeckAt(double x, double y);
	abstract public double getBottomAt(double x, double y);
	abstract public Vector3d getDeckNormalAt(double x, double y);
	abstract public Vector3d getBottomNormalAt(double x, double y);
	
}
