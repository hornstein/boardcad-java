package cadcore;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.BezierBoard;


abstract public class AbstractBezierBoardSurfaceModel 
{
	//LinearInterpolation is obsolete, keep for reference
	public enum ModelType{LinearInterpolation, ControlPointInterpolation, SLinearInterpolation};
	
	static private BezierBoardControlPointInterpolationSurfaceModel mControlPointInterpolationInstance = new BezierBoardControlPointInterpolationSurfaceModel();
	
	static private BezierBoardSLinearInterpolationSurfaceModel mSLinearInterpolationInstance = new BezierBoardSLinearInterpolationSurfaceModel();
	
	static public AbstractBezierBoardSurfaceModel getBezierBoardSurfaceModel(ModelType modelType)
	{

		switch(modelType)
		{
			
		default:
		case ControlPointInterpolation:
			return mControlPointInterpolationInstance;
			
		case SLinearInterpolation:
			return mSLinearInterpolationInstance;
		}
	}
	
	public abstract Point3d getDeckAt(final BezierBoard brd, final double x, final double y);
	public Vector3d getDeckNormalAt(final BezierBoard brd, final double x, final double y)
	{
		final double OFFSET = 0.01;
		
		boolean flipNormal = false;
		double xo = x-OFFSET;
		if(xo < 0)
		{
			xo = x+OFFSET;
			flipNormal = !flipNormal;
		}
		double yo = y-OFFSET;
		if(yo < 0)
		{
			yo = y+OFFSET;
			flipNormal = !flipNormal;
		}
		
		
		Point3d p1 = getDeckAt(brd,x,y);
		Point3d p2 = getDeckAt(brd,x,yo);
		Point3d p3 = getDeckAt(brd,xo,y);

		Vector3d pv = new Vector3d(0, y-yo, p2.z-p1.z);
		Vector3d lv = new Vector3d(x-xo, 0, p3.z-p1.z);
		
		Vector3d normalVec = new Vector3d();
		normalVec.cross(pv,lv);
		normalVec.normalize();
		
		if(flipNormal)
		{
			normalVec.scale(-1.0);
		}
		
		return normalVec;
	}

	public abstract Point3d getBottomAt(final BezierBoard brd, final double x, final double y);
	public Vector3d getBottomNormalAt(final BezierBoard brd, final double x, final double y)
	{
		final double OFFSET = 0.01;
		
		boolean flipNormal = false;
		double xo = x-OFFSET;
		if(xo < 0)
		{
			xo = x+OFFSET;
			flipNormal = !flipNormal;
		}
		double yo = y-OFFSET;
		if(yo < 0)
		{
			yo = y+OFFSET;
			flipNormal = !flipNormal;
		}
		
		
		Point3d p1 = getBottomAt(brd,x,y);
		Point3d p2 = getBottomAt(brd,x,yo);
		Point3d p3 = getBottomAt(brd,xo,y);
		
		Vector3d pv = new Vector3d(0, y-yo, p2.z-p1.z);		
		Vector3d lv = new Vector3d(x-xo, 0, p3.z-p1.z);
		
		Vector3d normalVec = new Vector3d();
		normalVec.cross(pv,lv);
		normalVec.normalize();
		
		if(flipNormal)
		{
			normalVec.scale(-1.0);
		}
		
		return normalVec;
	}
	
	public abstract Point3d getPointAt(final BezierBoard brd, double x, double s, double minAngle, double maxAngle, boolean useMinimumAngleOnSharpCorners);
	public Vector3d getNormalAt(final BezierBoard brd, double x, double s, double minAngle,  double maxAngle, boolean useMinimumAngleOnSharpCorners)
	{
		final double X_OFFSET = 0.1;
		final double S_OFFSET = 0.01;
	
		boolean flipNormal = false;
		
		double so = s+S_OFFSET;
		
		if(so > 1.0)
		{
			so = s-S_OFFSET;
			flipNormal = true;
		}

		if(x < 1.0)
		{
			x = .0;
		}
		if(x > brd.getLength() - 1.0)
		{
			x = brd.getLength() - 1.0;
		}
		double xo = x+X_OFFSET;

		Point3d p = getPointAt(brd, x,s,minAngle,maxAngle, useMinimumAngleOnSharpCorners);	
		Point3d pso = getPointAt(brd, x,so,minAngle,maxAngle, useMinimumAngleOnSharpCorners);
		Point3d pxo = getPointAt(brd, xo,s,minAngle,maxAngle, useMinimumAngleOnSharpCorners);
		
		Vector3d vc = new Vector3d(0, p.y-pso.y, p.z-pso.z);		//Vector across
		vc.normalize();
			
		Vector3d vl = new Vector3d(pxo.x-p.x, pxo.y-p.y, pxo.z-p.z);	//Vector lengthwise
		vl.normalize();
		
		Vector3d normalVec = new Vector3d();
		normalVec.cross(vc,vl);
		normalVec.normalize();
		
		if(flipNormal == true)
		{
			normalVec.scale(-1.0);
		}
		
/*DEBUG
		if(cv.angle(lv)*180.0/Math.PI < 15)
		{
			System.out.printf("getSurfaceNormalAtPos() Low angle between vectors: %f\n", cv.angle(lv)*180.0/Math.PI);		
		}
		
		if(last != null && last.angle(normalVec)*180.0/Math.PI > 15)
		{
			System.out.printf("getSurfaceNormalAtPos() large angle between last and current normal:%f\n", last.angle(normalVec)*180.0/Math.PI);		
		}
	
		last = new Vector3d(normalVec);
		
	//	System.out.printf("getNormalAtPos() x:%f, xo:%f, sa:%f, sao:%f, sb:%f, sbo:%f\n", x, xo, sa, sao, sb, sbo);
	//	System.out.printf("getNormalAtPos() %f, %f, %f\n", normalVec.x,normalVec.y,normalVec.z);
*/		
		return normalVec;
	}
	
	public abstract double getCrosssectionAreaAt(final BezierBoard brd,double x, int splits);
}
