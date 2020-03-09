package cadcore;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

public class BezierKnot extends Object implements Cloneable
{
	public static int LOCK_X_MORE = 0x0001;
	public static int LOCK_X_LESS = 0x0010;
	public static int LOCK_Y_MORE = 0x0100;
	public static int LOCK_Y_LESS = 0x1000;
	
	public static int END_POINT = 0;
	public static int PREVIOUS_TANGENT = 1;
	public static int NEXT_TANGENT = 2;
	
	public static double COMPARE_POS_WEIGHT = 4.0;
	public static double COMPARE_ANGLE_WEIGHT = 15.0;
	public static double COMPARE_TANGENT_LENGTH_WEIGHT = .3;
	
	private Point2D.Double mPoints[];
	protected BezierKnot mSlave;
	protected boolean mContinous = false;
	private boolean mOther = false;
	protected double X_mask = 1.0f;
	protected double Y_mask = 1.0f;
	protected int mTangent1Locks = 0;
	protected int mTangent2Locks = 0;
 
	List<BezierKnotChangeListener> mChangeListeners = new Vector<BezierKnotChangeListener>();

	public BezierKnot(){
		setPoints(new Point2D.Double[3]);
		for(int i = 0; i < 3; i++)
		{
			getPoints()[i] = new Point2D.Double();
		}	
	}

	public BezierKnot(double cx, double cy, double px, double py, double nx, double ny){
		setPoints(new Point2D.Double[3]);
		getPoints()[0] = new Point2D.Double(cx, cy);
		getPoints()[1] = new Point2D.Double(px, py);
		getPoints()[2] = new Point2D.Double(nx, ny);
	}

	public Point2D.Double getEndPoint()
	{
		return getPoints()[0];
	}

	public Point2D.Double getTangentToPrev()
	{
		return getPoints()[1];
	}

	public double getTangentToPrevLength()
	{
		double x = getPoints()[1].x - getPoints()[0].x;
		double y = getPoints()[1].y - getPoints()[0].y;
		
		return Math.sqrt(x*x+y*y);
	}

	public Point2D.Double getTangentToNext()
	{
		return getPoints()[2];
	}

	public double getTangentToNextLength()
	{
		double x = getPoints()[2].x - getPoints()[0].x;
		double y = getPoints()[2].y - getPoints()[0].y;
		
		return Math.sqrt(x*x+y*y);
	}

	public void setContinous(boolean continuous)
	{
		mContinous = continuous;
	}

	public boolean isContinous()
	{
		return mContinous;
	}

	public void setControlPointLocation(double x, double y)
	{
		double x_diff = (x - getPoints()[0].x)*X_mask;
		double y_diff = (y - getPoints()[0].y)*Y_mask;

		getPoints()[0].setLocation(getPoints()[0].x+x_diff, getPoints()[0].y+y_diff);
		getPoints()[1].setLocation(getPoints()[1].x+x_diff, getPoints()[1].y+y_diff);
		getPoints()[2].setLocation(getPoints()[2].x+x_diff, getPoints()[2].y+y_diff);

		if(mSlave != null)
		{
//			Set the points of the slave directly to avoid recursion
			updateSlave(x_diff, y_diff);
		}
		onChange();
	}
	
	public void updateSlave(double x_diff, double y_diff)
	{
		mSlave.getPoints()[0].setLocation(getPoints()[0].x, getPoints()[0].y);
		mSlave.getPoints()[1].setLocation(mSlave.getPoints()[1].x+x_diff, mSlave.getPoints()[1].y+y_diff);
		mSlave.getPoints()[2].setLocation(mSlave.getPoints()[2].x+x_diff, mSlave.getPoints()[2].y+y_diff);
		mSlave.onChange();
	}

	public void setEndPoint(double x, double y)
	{
		getPoints()[0].setLocation(x*X_mask, y*Y_mask);
		onChange();
	}

	public void setTangentToPrev(double x, double y)
	{
		getPoints()[1].setLocation(x, y);
		handleLocks(getPoints()[1], mTangent1Locks);
		onChange();
	}

	public void setTangentToNext(double x, double y)
	{
		getPoints()[2].setLocation(x, y);
		handleLocks(getPoints()[2], mTangent2Locks);
		onChange();
	}

	public void setLocation(int index, double x, double y)
	{
		switch(index)
		{
		case 0:
			setEndPoint(x, y);
			break;
		case 1:
			setTangentToPrev(x, y);
			break;
		case 2:
			setTangentToNext(x, y);
			break;
		}
		onChange();
	}

	public void scale(double scaleX, double scaleY)
	{
		for(int i = 0; i < 3; i++)
		{
			getPoints()[i].setLocation(getPoints()[i].x*scaleX, getPoints()[i].y*scaleY);
		}
		onChange();
	}

	public void scaleTangentToPrev(double scale)
	{
		Point2D.Double vec = new Point2D.Double(); 
		VecMath.subVector(getEndPoint(), getTangentToPrev(), vec);
		VecMath.scaleVector(vec, scale);
		VecMath.addVector(vec, getEndPoint(), vec);
		setTangentToPrev(vec.x,vec.y);
		onChange();
	}

	public void scaleTangentToNext(double scale)
	{
		Point2D.Double vec = new Point2D.Double(); 
		VecMath.subVector(getEndPoint(), getTangentToNext(), vec);
		VecMath.scaleVector(vec, scale);
		VecMath.addVector(vec, getEndPoint(), vec);
		setTangentToNext(vec.x,vec.y);
		onChange();
	}

	public double getTangentToPrevAngle()
	{
		Point2D.Double u = new Point2D.Double(0,1); 
		Point2D.Double vec = new Point2D.Double(); 
		VecMath.subVector(getEndPoint(), getTangentToPrev(), vec);
		
		return VecMath.getVecAngle(u, vec);
	}

	public void setTangentToPrevAngle(double angle)
	{
		Point2D.Double next = getTangentToPrev();
		double sx = next.x - getEndPoint().x;
		double sy = next.y - getEndPoint().y;

		double pointAngle = getTangentToPrevAngle();
//		System.out.printf("pointAngle:%f ", pointAngle);

		double rotAngle = angle - pointAngle;
//		System.out.printf("rotAngle:%f ", rotAngle);

		double x_diff = (double)((Math.cos(rotAngle)*sx - Math.sin(rotAngle)*sy) - sx);
		double y_diff = (double)((Math.sin(rotAngle)*sx + Math.cos(rotAngle)*sy) - sy);

//		System.out.printf("x_diff:%f y_diff:%f\n", x_diff, y_diff);

		setTangentToPrev(next.x+x_diff, next.y+y_diff);
		onChange();
	}

	public double getTangentToNextAngle()
	{
		Point2D.Double u = new Point2D.Double(0,1); 
		Point2D.Double vec = new Point2D.Double(); 
		VecMath.subVector(getEndPoint(), getTangentToNext(), vec);

		return VecMath.getVecAngle(u, vec);
	}

	public void setTangentToNextAngle(double angle)
	{
		Point2D.Double next = getTangentToNext();
		double sx = next.x - getEndPoint().x;
		double sy = next.y - getEndPoint().y;

		double pointAngle = getTangentToNextAngle();
//		System.out.printf("pointAngle:%f ", pointAngle);

		double rotAngle = angle - pointAngle;
//		System.out.printf("rotAngle:%f ", rotAngle);

		double x_diff = (double)((Math.cos(rotAngle)*sx - Math.sin(rotAngle)*sy) - sx);
		double y_diff = (double)((Math.sin(rotAngle)*sx + Math.cos(rotAngle)*sy) - sy);

//		System.out.printf("x_diff:%f y_diff:%f\n", x_diff, y_diff);

		setTangentToNext(next.x+x_diff, next.y+y_diff);
		onChange();
	}

	public double getAngleBetweenTangents()
	{
		Point2D.Double vec1 = new Point2D.Double(); 
		Point2D.Double vec2 = new Point2D.Double(); 
		VecMath.subVector(getEndPoint(), getTangentToPrev(), vec1);
		VecMath.subVector(getEndPoint(), getTangentToNext(), vec2);

		return VecMath.getVecAngle(vec1, vec2);
	}

	public void setMask(double x, double y)
	{
		X_mask = x;
		Y_mask = y;
	}

	public void setTangentToPrevLocks(int locks)
	{
		mTangent1Locks = locks;
	}

	public void setTangentToNextLocks(int locks)
	{
		mTangent2Locks = locks;
	}

	public void addTangentToPrevLocks(int locks)
	{
		mTangent1Locks |= locks;
	}

	public void addTangentToNextLocks(int locks)
	{
		mTangent2Locks |= locks;
	}

	public void setSlave(BezierKnot slave)
	{
		mSlave = slave;
		
		double x_diff = mSlave.getPoints()[0].x - getPoints()[0].x;
		double y_diff = mSlave.getPoints()[0].y - getPoints()[0].y;
		
		updateSlave(x_diff, y_diff);
	}

	public void handleLocks(Point2D.Double point, int locks)
	{
		if((locks&LOCK_X_MORE) != 0)
		{
			if(getPoints()[0].x > point.x)
			{
				point.x = getPoints()[0].x;
			}
		}
		if((locks&LOCK_X_LESS) != 0)
		{
			if(getPoints()[0].x < point.x)
			{
				point.x = getPoints()[0].x;
			}
		}
		if((locks&LOCK_Y_MORE) != 0)
		{
			if(getPoints()[0].y > point.y)
			{
				point.y = getPoints()[0].y;
			}
		}
		if((locks&LOCK_Y_LESS) != 0)
		{
			if(getPoints()[0].y < point.y)
			{
				point.y = getPoints()[0].y;
			}
		}
		onChange();
	}

	public void switch_tangents()
	{
		Point2D.Double temp=getPoints()[1];
		getPoints()[1]=getPoints()[2];
		getPoints()[2]=temp;
		onChange();
	}

	public int compareTo(Object other)
	{
		BezierKnot otherControlPoint = (BezierKnot)other;

		if(equals(otherControlPoint))
			return 0;

		double retVal = 0;

		// To match a ControlPoint there are two cases that should match
		//
		//	1)The ControlPoint has moved
		//		The ControlPoint has roughly the same tangent angles and length but are located at a different position
		//		Properties to check(With priority)
		//			*Sequence of ControlPoints (index in ControlPointlist)
		//			*The ControlPoint has same continouancy
		//			*The tangent angle is similar
		//			*The tangent lengths are similar
		//			*Previous/next ControlPoint is similar (What if the previous/next ControlPoint has morphed?)
		//
		//	2)The ControlPoint has morphed
		//		The ControlPoint has roughly the same position, but the ControlPoints angle and length has changed
		//		Properties to check(With priority)
		//			*Sequence of ControlPoints (index in ControlPointlist)
		//			*Location is similar
		//			*Arc position is similar			
		//			*Distance to previous/next ControlPoint is similar
		//			*Arc Distance to previous/next ControlPoint is similar
		//			*Previous/next ControlPoint is similar (What if the previous/next ControlPoint has morphed?)

		//continouancy
		retVal += ((mContinous == otherControlPoint.mContinous)?0:1)*1.0; //Weigth of continouancy

		//Position
		double posDiff = VecMath.getVecLength(getPoints()[0],otherControlPoint.getPoints()[0]);
		posDiff /= 0.5;	//N cm off or less is a good match, bigger will square to a large number
		retVal += posDiff*posDiff*COMPARE_POS_WEIGHT;	//Weight of position difference

		//Build tangents
		Point2D.Double tt1 = new Point2D.Double();
		Point2D.Double tt2 = new Point2D.Double();
		Point2D.Double ot1 = new Point2D.Double();
		Point2D.Double ot2 = new Point2D.Double();

		VecMath.subVector(getPoints()[0],getPoints()[1],tt1);	
		VecMath.subVector(getPoints()[0],getPoints()[2],tt2);

		VecMath.subVector(otherControlPoint.getPoints()[0],otherControlPoint.getPoints()[1],ot1);	
		VecMath.subVector(otherControlPoint.getPoints()[0],otherControlPoint.getPoints()[2],ot2);	


		//Tangent angles
		double angleDiff = VecMath.getVecAngle(tt1, ot1)*180.0/Math.PI;		//In degrees
		double angleDiff2 = VecMath.getVecAngle(tt2, ot2)*180.0/Math.PI;	//In degrees
		
		angleDiff /=5.0f;	//15 degrees equals one meaning less will square to a lower number and larger will square up
		angleDiff2 /=5.0f;
		
		retVal += (angleDiff*angleDiff)*COMPARE_ANGLE_WEIGHT;//Weigth of angle
		retVal += (angleDiff2*angleDiff2)*COMPARE_ANGLE_WEIGHT;//Weigth of angle

		//Tangent lengths
		double tanLength1Diff = Math.abs(VecMath.getVecLength(tt1) - VecMath.getVecLength(ot1));
		double tanLength2Diff = Math.abs(VecMath.getVecLength(tt2) - VecMath.getVecLength(ot2));

		retVal += tanLength1Diff*tanLength1Diff*COMPARE_TANGENT_LENGTH_WEIGHT; //Weigth of tan length diff
		retVal += tanLength2Diff*tanLength2Diff*COMPARE_TANGENT_LENGTH_WEIGHT; //Weigth of tan length diff

		return (int)retVal;
	}	

	public boolean equals(Object other)
	{
		BezierKnot otherControlPoint = (BezierKnot)other;
		for(int i = 0; i < 3; i++)
		{
			if(getPoints()[i].x != otherControlPoint.getPoints()[i].x)
				return false;

			if(getPoints()[i].y != otherControlPoint.getPoints()[i].y)
				return false;
		}

		if(mContinous != otherControlPoint.mContinous)
			return false;

		if(getOther() != otherControlPoint.getOther())
			return false;

		return true;

	}

	public void set(BezierKnot controlPoint)
	{
		mContinous = controlPoint.mContinous;
		setOther(controlPoint.getOther());
		mSlave = controlPoint.mSlave;
		mTangent1Locks = controlPoint.mTangent1Locks;
		mTangent2Locks = controlPoint.mTangent2Locks;

		for(int i = 0; i < 3; i++)
		{
			getPoints()[i].x = controlPoint.getPoints()[i].x;
			getPoints()[i].y = controlPoint.getPoints()[i].y;
		}
		onChange();
	}

	public Object clone(){
		BezierKnot controlPoint = null;
		try {
			controlPoint =  (BezierKnot)super.clone();
		} catch(CloneNotSupportedException e) {
			System.out.println("Exception in BezierControlPoint::clone(): " + e.toString());
			throw new Error("CloneNotSupportedException in BrdCommand");
		}

		controlPoint.initPoints();
		for(int i = 0; i < 3; i++)
		{
			controlPoint.getPoints()[i] = (Point2D.Double)this.getPoints()[i].clone();
		}
		return controlPoint;
	}

	public String toString()
	{		
		String string = String.format("%f,%f,%f,%f,%f,%f,%s,%s,%d,%d,%d,%d", getPoints()[0].x, getPoints()[0].y, getPoints()[1].x, getPoints()[1].y, getPoints()[2].x, getPoints()[2].y, mContinous?"true":"false", getOther()?"true":"false", X_mask,Y_mask,mTangent1Locks, mTangent2Locks);
		
		return string;
	}
	
	public void fromString(String string)
	{
		String[] values = string.split(",");
		for(int i = 0; i < 3; i++)
		{
			double x = Double.valueOf(values[i*2]);
			double y = Double.valueOf(values[(i*2)+1]);
	
			getPoints()[i].setLocation(x,y);
	
		}
	
		mContinous = Boolean.valueOf(values[6]);
	
		setOther(Boolean.valueOf(values[7]));
	
		X_mask = Integer.valueOf(values[8]);
		Y_mask = Integer.valueOf(values[9]);
		mTangent1Locks = Integer.valueOf(values[10]);
		mTangent2Locks = Integer.valueOf(values[11]);
		onChange();
	}

	public void initPoints() {
		this.mPoints = new Point2D.Double[3];
	}
	
	public void setPoints(Point2D.Double mPoints[]) {
		this.mPoints = mPoints;
		onChange();
	}

	public Point2D.Double[] getPoints() {
		return mPoints;
	}

	public void setOther(boolean mOther) {
		this.mOther = mOther;
	}

	public boolean getOther() {
		return mOther;
	}

	void addChangeListener(BezierKnotChangeListener listener)
	{
		mChangeListeners.add(listener);
	}

	public void onChange()	//TODO: THIS FUNCTION SHOULD NOT BE PUBLIC, FUNCTIONS WHICH CALL onChange() SHOULD BE USED FOR SETTING KNOT VALUES
	{
		for(BezierKnotChangeListener listener : mChangeListeners)
		{
			listener.onChange(this);
		}
	}
}
