package boardcam.cutters;

import cadcore.*;
import board.AbstractBoard;
import board.BezierBoard;

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

import boardcam.MachineConfig;
import boardcad.settings.Settings;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcad.i18n.LanguageResource;

public class SimpleBullnoseCutter extends AbstractCutter{
	
	static final String CUTTER_DIAMETER = "CutterDiameter";
	static final String CUTTER_CORNERRADIUS = "CutterCornerRadius";
	static final String CUTTER_HEIGHT = "CutterHeight";

	static private final int CUTTER_SEGMENTS = 16;
	static private final int CORNER_POINTS = 8;
	static private final int SEGMENT_POINTS = (CORNER_POINTS + 2)*2;

	double mDiameter;
	double mCornerRadius;
	double mHeight;
	
	double mLiftOverHeight = 5.0;
	
	MachineConfig mConfig = null;
	
	private BranchGroup m3DModelBranchGroup = null;
	private Shape3D m3DModel = null;
	private LineArray m3DModelArray = null;
	
	private double mConsideredFlatAngle = 8.0;

	public SimpleBullnoseCutter(double diameter, double cornerRadius, double height)
	{
		mDiameter = diameter;
		mCornerRadius = cornerRadius;
		mHeight = height;
	}

	public SimpleBullnoseCutter(MachineConfig config)
	{
		mConfig = config;
				
		Settings cutterSettings = mConfig.addCategory(LanguageResource.getString("CUTTERCATEGORY_STR"));
		SettingChangedCallback cb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				updateCutterDimensions();
				update3DModel();
			}	
		};
		cutterSettings.addObject(CUTTER_DIAMETER,  cutterSettings.new Measurement(2.54), LanguageResource.getString("CUTTERDIAMETER_STR"), cb);
		cutterSettings.addObject(CUTTER_CORNERRADIUS,  cutterSettings.new Measurement(0.5), LanguageResource.getString("CUTTERCORNERRADIUS_STR"), cb);
		cutterSettings.addObject(CUTTER_HEIGHT,  cutterSettings.new Measurement(6.5), LanguageResource.getString("CUTTERHEIGHT_STR"), cb);	
	}

	public void init()
	{
		updateCutterDimensions();
	}
	
	protected void updateCutterDimensions()
	{
		Settings cutterSettings = mConfig.getCategory(LanguageResource.getString("CUTTERCATEGORY_STR"));

		mDiameter = cutterSettings.getMeasurement(CUTTER_DIAMETER);
		mCornerRadius = cutterSettings.getMeasurement(CUTTER_CORNERRADIUS);
		mHeight = cutterSettings.getMeasurement(CUTTER_HEIGHT);		
	}
	
	
	@Override
	public double[] calcOffset(Point3d pos, Vector3d normal, AbstractBoard board) 
	{
//		System.out.printf("\nSimpleBullnoseCutter.calcOffset() begin!\n");
		
//		System.out.printf("normal: %f, %f, %f\n", normal.x, normal.y, normal.z);
//		System.out.printf("normal: %f, %f, %f\n", normal.x, normal.y, normal.z);

		//Make a left pointing vector and get normal vector as seen from above
		Vector2d leftVec2D = new Vector2d(0.0,1.0);
		Vector2d normalFromAbove = new Vector2d(normal.x, normal.y);
//		System.out.printf("normalFromAbove: %f, %f\n", normalFromAbove.x, normalFromAbove.y);
		
		if(normalFromAbove.length() == 0.0)
		{
			return new double[]{pos.x, pos.y, pos.z};
		}
		
		double y_angle = Math.atan2(normal.y, normal.z);
		
		//Find angle the vector is from pointing straight left as seen from above
		double headingAngle = normalFromAbove.angle(leftVec2D);
		if(normalFromAbove.x < 0)
			headingAngle = -headingAngle;

//		System.out.printf("headingAngle: %f y_angle: %f\n", headingAngle/BezierBoard.DEG_TO_RAD, y_angle/BezierBoard.DEG_TO_RAD);
		
		double[] ret = null;

		if(Math.abs(y_angle) /MathUtils.DEG_TO_RAD < mConsideredFlatAngle)
		{
			double flatAreaRadius = (mDiameter/2.0) - mCornerRadius;
			flatAreaRadius *= 10.0;
			
			double x_angle = Math.atan2(normal.x, normal.z);
//			double y_angle = Math.atan2(normal.y, normal.z);
			
//			System.out.printf("x_angle: %f y_angle: %f\n", x_angle/BezierBoard.DEG_TO_RAD, y_angle/BezierBoard.DEG_TO_RAD);

			double toolCompensationX = 0.0;
			if(Math.abs(x_angle) < mConsideredFlatAngle)
			{
				toolCompensationX = flatAreaRadius*MathUtils.clamp(Math.sin(x_angle)/Math.sin(mConsideredFlatAngle*MathUtils.DEG_TO_RAD), -1.0, 1.0);
			}
			else
			{
				toolCompensationX = (mDiameter*10.0/2.0) - mCornerRadius*10.0*(1.0-Math.sin(x_angle));
			}
			
			double toolCompensationY = flatAreaRadius*MathUtils.clamp(Math.sin(y_angle)/Math.sin(mConsideredFlatAngle*MathUtils.DEG_TO_RAD), -1.0,1.0);
			
//			System.out.printf("toolCompensationX: %f toolCompensationY: %f\n", toolCompensationX, toolCompensationY);

			ret = new double[]{pos.x+toolCompensationX, pos.y+toolCompensationY, pos.z};
			
		}
		else
		{			
			//Rotate the normal so it's pointing left
			Matrix4d rotMatrix4d = new Matrix4d();
			rotMatrix4d.setIdentity();
			rotMatrix4d.rotZ(headingAngle);
			
			Vector3d normalRotated = new Vector3d(normal);
			rotMatrix4d.transform(normalRotated);
//			System.out.printf("normalRotated: %f, %f, %f\n", normalRotated.x, normalRotated.y, normalRotated.z);
	
			//Get 2D vector of normal pointing sideways (left)
			Vector2d normalFromSide = new Vector2d(normalRotated.y, normalRotated.z);
//			System.out.printf("normalFromSide: %f, %f\n", normalFromSide.x, normalFromSide.y);
			
			//Find angle the vector is from pointing straight left as seen from side
			double angle = normalFromSide.angle(leftVec2D);
//			System.out.printf("angle: %f\n", angle/BezierBoard.DEG_TO_RAD);
			
			//Calculate a point on a circle with the corner radius and the angle
			double x = (mDiameter*10.0/2.0) - mCornerRadius*10.0*(1.0-Math.sin(angle));
			double y = -mCornerRadius*10.0*(1.0-Math.cos(angle));
	
			Point2D.Double point = new Point2D.Double(x,y);
						
			Vector3d toolCompensation = new Vector3d(0.0, point.x, point.y);
	
//			System.out.printf("toolCompensation 2D: %f, %f\n", point.x, point.y);
			
			//Rotate the point on the surface to the direction of the normal
			rotMatrix4d.invert();
	
			rotMatrix4d.transform(toolCompensation);
			
			Point3d offsetPoint = new Point3d(pos);
			offsetPoint.add(toolCompensation);
			
//			System.out.printf("Offset rotated 3D: %f, %f, %f\n", offsetPoint.x, offsetPoint.y,offsetPoint.z);
			
			ret = new double[]{offsetPoint.x, offsetPoint.y, offsetPoint.z};
		}

		if(mStayAwayFromStringer)
		{
			if(Math.abs(ret[1]) - (mDiameter*10.0/2.0) < mStringerWidth*10.0)
			{
//				System.out.printf("SimpleBullnoseCutter.calcOffset() collision with stringer handled\n");
				ret[1] = ((mDiameter*10.0/2.0) + mStringerWidth*10.0)*((ret[1]>0.0)?1.0:-1.0);
			}
		}

		return ret;
	}
	
	public boolean checkStringerCollision(Point3d pos)
	{
		return (Math.abs(pos.y) - (mDiameter*10.0/2.0)) < mStringerWidth*10.0;
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
			cutterApperanceColor.setColor (0.5f, 0.5f, 0.5f);
			cutterApperance.setColoringAttributes(cutterApperanceColor);			
			m3DModel.setAppearance(cutterApperance);
			
			m3DModelBranchGroup.addChild(m3DModel);

			update3DModel();
		}
		
		return m3DModelBranchGroup;
	}

	public void update3DModel()
	{
//		System.out.printf("update3DModel\n");
		if(m3DModelArray == null)
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
			int k=0;
			Point3d lastPoint = new Point3d(0.0, 0.0, 0.0);
			
			for(int j = 0; j < CORNER_POINTS; j++)
			{
				double angle = ((double)j/(CORNER_POINTS-1))*(Math.PI/2.0);
				
				double x = (mDiameter*10.0/2.0) - mCornerRadius*10.0*(1.0-Math.sin(angle));
				double y = mCornerRadius*10.0*(1.0-Math.cos(angle));
				
//				System.out.printf("angle:%f, x:%f, y:%f\n",angle,x,y);
				
				Point3d point3d = new Point3d(x, 0.0, y);
				points[k++] = new Point3d(lastPoint);
				points[k++] = point3d;
				lastPoint = point3d;
			}
			points[k++] = new Point3d(lastPoint);
			points[k++] = new Point3d(mDiameter*10.0/2.0, 0.0, mHeight*10.0);
			
			points[k++] = new Point3d(mDiameter*10.0/2.0, 0.0, mHeight*10.0);
			points[k++] = new Point3d(0.0, 0.0, mHeight*10.0);
			
			for(int q = 0; q < k; q++)
			{
				rotMatrix4d.transform(points[q]);				
			}
				
			m3DModelArray.setCoordinates(i*SEGMENT_POINTS, points);

		}
		
		//Spindle
		m3DModelArray.setCoordinates(i*SEGMENT_POINTS, new Point3d[]{new Point3d(0,0,mHeight*10.0),new Point3d(0, 0, (mHeight+10.0)*10.0)});
			
	}

	public Point3d getNoseCutOffPoint(int step, AbstractBoard board, boolean deckSide)
	{
		BezierBoard brd = (BezierBoard)board;	//Only Brd supported yet
		
		Point3d point = null;
		if(deckSide)
		{
			point = brd.getSurfacePoint(brd.getLength()-1.0, 0.0);
		}
		else
		{
			point = brd.getSurfacePoint(brd.getLength()-1.0, 360.0, 90.0, 0, 1, false);

			//Flip on bottom
			point.z = -point.z;
		}
				
		
		switch(step)
		{
		case 0:	//Go to above on left side of stringer
			point.y += mStringerWidth + mDiameter;
			point.z += (brd.getThicknessAtPos(brd.getLength()-1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		case 1:	//Lower left side of stringer
			point.y += mStringerWidth + mDiameter;
			point.z -= mCornerRadius;
			break;
		case 2:	//Move into stringer from left side (leave 1mm)
			point.y += mDiameter/2.0 + 0.1;
			point.z -= mCornerRadius;
			break;
		case 3:	//Lift
			point.y += mDiameter/2.0 + 0.1;
			point.z += (brd.getThicknessAtPos(brd.getLength()-1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		case 4:	//Go to above on right side of stringer
			point.y -= mStringerWidth + mDiameter;
			point.z += (brd.getThicknessAtPos(brd.getLength()-1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		case 5:	//Lower right side of stringer
			point.y -= mStringerWidth + mDiameter;
			point.z -= mCornerRadius;
			break;
		case 6:	//Move into stringer from right side  (leave 1mm)
			point.y -= mDiameter/2.0 + 0.1;
			point.z -= mCornerRadius;
			break;
		case 7:	//Lift to above
		default:
			point.y -= mDiameter/2.0 + 1.0;
			point.z += (brd.getThicknessAtPos(brd.getLength()-1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		}
		
		
		return point;
	}
	
	public Vector3d getNoseCutOffNormal(int step, AbstractBoard board, boolean deckSide)
	{
		return new Vector3d(1.0, 0.0, 0.0);
	}

	public boolean isNoseCutOffFinished(int step)
	{
		return step >=8;
	}
	
	public Point3d getTailCutOffPoint(int step, AbstractBoard board, boolean deckSide)
	{
		BezierBoard brd = (BezierBoard)board;	//Only Brd supported yet

		Point3d point = null;
		if(deckSide)
		{
			point = brd.getSurfacePoint(1.0, 0.0);
		}
		else
		{
			point = brd.getSurfacePoint(1.0, 360.0, 90.0, 0, 1, false);

			//Flip on bottom
			point.z = -point.z;
		}
		

		switch(step)
		{
		case 0:	//Go to above on left side of stringer
			point.y += mStringerWidth + mDiameter;
			point.z += (brd.getThicknessAtPos(1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		case 1:	//Lower left side of stringer
			point.y += mStringerWidth + mDiameter;
			point.z -= mCornerRadius;
			break;
		case 2:	//Move into stringer from left side (leave 1mm)
			point.y += mDiameter/2.0 + 0.1;
			point.z -= mCornerRadius;
			break;
		case 3:	//Lift
			point.y += mDiameter/2.0 + 0.1;
			point.z += (brd.getThicknessAtPos(1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		case 4:	//Go to above on right side of stringer
			point.y -= mStringerWidth + mDiameter;
			point.z += (brd.getThicknessAtPos(1.0) + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		case 5:	//Lower right side of stringer
			point.y -= mStringerWidth + mDiameter;
			point.z -= mCornerRadius;
			break;
		case 6:	//Move into stringer from right side  (leave 1mm)
			point.y -= mDiameter/2.0 + 0.1;
			point.z -= mCornerRadius;
			break;
		case 7:	//Lift to above
		default:
			point.y -= mDiameter/2.0 - 0.1;
			point.z += (brd.getThicknessAtPos(1.0) + mCornerRadius + mLiftOverHeight)*(deckSide?1.0:-1.0);
			break;
		}
		
		return point;
	}
	
	public Vector3d getTailCutOffNormal(int step, AbstractBoard board, boolean deckSide)
	{
		return new Vector3d(-1.0, 0.0, 0.0);
	}

	public boolean isTailCutOffFinished(int step)
	{
		return step >= 8;
	}
	
	public boolean checkCollision(Point3d pos, AbstractBoard board)
	{
		return false;
	}
	
	public boolean checkCollision(Point3d pos, AxisAlignedBoundingBox collisionBox)
	{
		return collisionBox.isIntersectingVerticalCylinder(pos, mDiameter*UnitUtils.MILLIMETER_PR_CENTIMETER/2.0, mHeight);
	}
	
	@Override
	public AxisAlignedBoundingBox getBoundingBox(Point3d pos) {
		double r = mDiameter*UnitUtils.MILLIMETER_PR_CENTIMETER/2.0;
		return new AxisAlignedBoundingBox(new Point3d(pos.x - r, pos.y - r, pos.z), new Point3d(pos.x + r, pos.y + r, pos.z + mHeight*UnitUtils.MILLIMETER_PR_CENTIMETER));
	}

	public String toString()
	{
		return String.format("%s %s:%s, %s:%s, %s:%s, ",	LanguageResource.getString("BULLNOSECUTTER_STR"),
													LanguageResource.getString("CUTTERDIAMETER_STR"), UnitUtils.convertLengthToCurrentUnit(mDiameter, false), 
													LanguageResource.getString("CUTTERCORNERRADIUS_STR"), UnitUtils.convertLengthToCurrentUnit(mCornerRadius, false), 
													LanguageResource.getString("CUTTERHEIGHT_STR"), UnitUtils.convertLengthToCurrentUnit(mHeight, false));	
	}
}
