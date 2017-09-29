package boardcam.cutters;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.AbstractBoard;
import cadcore.AxisAlignedBoundingBox;

public abstract class AbstractCutter 
{
	protected boolean mStayAwayFromStringer = false;
	protected double mStringerWidth = 0.3;
	
	public abstract double[] calcOffset(Point3d pos, Vector3d normal, AbstractBoard board);
	
	public void init()
	{
	}
	
	public void setStayAwayFromStringer(boolean stayAwayFromStringer)
	{
		mStayAwayFromStringer = stayAwayFromStringer;
	}
	
	public void setStringerWidth(double stringerWidth)
	{
		mStringerWidth = stringerWidth;
	}

	public boolean checkCollision(Point3d pos, AbstractBoard board)
	{
		return false;
	}
	
	public boolean checkCollision(Point3d pos, AxisAlignedBoundingBox collisionBox)
	{
		return false;
	}
	
	public BranchGroup get3DModel()
	{
		return null;
	}

	public void update3DModel()
	{
	}
	
	public Point3d getNoseCutOffPoint(int step, AbstractBoard board, boolean deckSide)
	{
		return new Point3d(0.0, 0.0, 0.0);
	}
	
	public Vector3d getNoseCutOffNormal(int step, AbstractBoard board, boolean deckSide)
	{
		return new Vector3d(0.0, 0.0, 0.0);
	}

	public boolean isNoseCutOffFinished(int step)
	{
		return true;
	}
	
	public Point3d getTailCutOffPoint(int step, AbstractBoard board, boolean deckSide)
	{
		return new Point3d(0.0, 0.0, 0.0);
	}
	
	public Vector3d getTailCutOffNormal(int step, AbstractBoard board, boolean deckSide)
	{
		return new Vector3d(0.0, 0.0, 0.0);
	}

	public boolean isTailCutOffFinished(int step)
	{
		return true;
	}
	
	public AxisAlignedBoundingBox getBoundingBox(Point3d pos)
	{
		return null;	//Breaks out when returning null
	}
}

