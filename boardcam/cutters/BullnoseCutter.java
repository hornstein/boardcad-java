package boardcam.cutters;

import java.awt.geom.Point2D;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import cadcore.BezierSpline;
import boardcam.MachineConfig;
import board.AbstractBoard;;

class BullnoseCutter extends AbstractCutter
{
	static private final int CUTTER_SEGMENTS = 16;
	static private final int SEGMENT_POINTS = 16;
	
	private BranchGroup m3DModelBranchGroup = null;
	private Shape3D m3DModel = null;
	private LineArray m3DModelArray = null;

	protected BezierSpline mCutterProfile;
	
	BullnoseCutter(MachineConfig config)
	{
		//TODO: Add properties for cutter		
	}
	
	public double[] calcOffset(Point3d pos, Vector3d normal, AbstractBoard board)
	{
//DEBUG
//		double[] ret = new double[]{pos.x, pos.y, pos.z};
//		
//		return ret;

		//Make a left pointing vector and get normalvector as seen from above
		Vector2d leftVec2D = new Vector2d(0.0,1.0);
		Vector2d normalFromAbove = new Vector2d(normal.x, normal.y);
		
		//Find angle the vector is from pointing straight left as seen from above
		double headingAngle = normalFromAbove.angle(leftVec2D);
		
		//Rotate the normal so it's pointing left
		Matrix4d rotMatrix4d = new Matrix4d();
		rotMatrix4d.setIdentity();
		rotMatrix4d.rotZ(headingAngle);
		
		Vector3d normalRotated = new Vector3d(normal);
		rotMatrix4d.transform(normalRotated);

		//Get 2D vector of normal pointing sideways (left)
		Vector2d normalFromSide = new Vector2d(normalRotated.x, normalRotated.y);
		
		//Find angle the vector is from pointing straight left as seen from side
		double angle = normalFromSide.angle(leftVec2D);
		
		//Get the point on the bezier with same angle
		double s = mCutterProfile.getSByNormal(angle);
		
		Point2D.Double point = mCutterProfile.getPointByS(s);
		
		//Rotate the point on the surface to the direction of the normal
		rotMatrix4d.invert();
		
		Vector3d toolCompensation = new Vector3d(0.0, point.x, point.y);
		
		rotMatrix4d.transform(toolCompensation);
		
		Point3d offsetPoint = new Point3d(pos);
		offsetPoint.add(toolCompensation);
		
		double[] ret = new double[]{offsetPoint.x, offsetPoint.y, offsetPoint.z};
		
		return ret;	
	}
	
	public void fromString(String string)
	{
		mCutterProfile.fromString(string);
	}
	
	public String toString()
	{
		return mCutterProfile.toString();
	}
	
	public BranchGroup get3DModel()
	{
		if(m3DModelBranchGroup == null)
		{
			m3DModelBranchGroup = new BranchGroup(); 
			m3DModelBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
			
			m3DModel = new Shape3D();
			m3DModel.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
			m3DModel.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

			m3DModelArray = new LineArray((CUTTER_SEGMENTS*SEGMENT_POINTS)+2, LineArray.COORDINATES);
			m3DModelArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
			m3DModel.setGeometry(m3DModelArray);
			
			Appearance cutterApperance = new Appearance();
			ColoringAttributes cutterApperanceColor = new ColoringAttributes();
			cutterApperanceColor.setColor (0.1f, 0.1f, 0.3f);
			cutterApperance.setColoringAttributes(cutterApperanceColor);			
			m3DModel.setAppearance(cutterApperance);
			
			m3DModelBranchGroup.addChild(m3DModel);

			update3DModel();
		}
		
		return m3DModelBranchGroup;
	}

	public void update3DModel()
	{
		if(m3DModelArray == null)
			return;
		
		if(mCutterProfile==null)
			return;
		
		//Update model if parameters changed parameters

		
		//Rotate the normal so it's pointing left
		Matrix4d rotMatrix4d = new Matrix4d();

		//Circle
		int i = 0;
		for(; i < CUTTER_SEGMENTS; i++)
		{
			rotMatrix4d.setIdentity();
			rotMatrix4d.rotZ(Math.PI*2.0*i/CUTTER_SEGMENTS);

			Point3d[] points = new Point3d[SEGMENT_POINTS];
			for(int j = 0; j < SEGMENT_POINTS; j++)
			{
				Point2D.Double point = mCutterProfile.getPointByS((double)j/(double)SEGMENT_POINTS);
				Point3d point3d = new Point3d(point.x, 0.0, point.y);
				rotMatrix4d.transform(point3d);
				points[j] = point3d;
			}
				
			m3DModelArray.setCoordinates(i*SEGMENT_POINTS, points);
		}
		
		//Spindle
		m3DModelArray.setCoordinates(i*SEGMENT_POINTS, new Point3d[]{new Point3d(0,0,0),new Point3d(0, 0, mCutterProfile.getPointByS(0.99).y*2)});
			
	}


}