package boardcam.toolpathgenerators;


import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.AbstractBoard;
import board.BezierBoard;
import boardcad.gui.jdk.BoardCAD;	//TODO: Bad dependency
import boardcad.FileTools;
import boardcam.cutters.AbstractCutter;
import boardcam.writers.AbstractMachineWriter;


public class HotwireToolpathGenerator extends AbstractToolpathGenerator {
	enum State {STATE_PROFILE, STATE_OUTLINE};
	
	State mCurrentState;

	double x_res = 0.1;
	static int passes = 2; 
	double length = 0;
	double nrOfLengthSplits = 0;
	double y = 0;
	double i = 0;
	double j = 0;
	Vector3d mNormalVec;
	boolean mBeginAtNose;

	private double mSpeed = 0;

	HotwireToolpathGenerator(AbstractCutter cutter, AbstractMachineWriter writer, double speed)
	{
		super(writer, BoardCAD.getInstance().getFrame());
		
		setCutter(cutter);

		mSpeed = speed;
	}

	public void writeProfile(String filename, AbstractBoard board)
	{
		filename =  FileTools.append(filename, "_profile");
		mCurrentState = State.STATE_PROFILE;
		
		BezierBoard brd = (BezierBoard)board;
		
		Point2d tailMaxPos = brd.getMaxDeckAtTailPos();	
		Point2d noseMaxPos = brd.getMaxDeckAtNosePos();
		
		double tailBottom = brd.getBottomAtPos(0.0, 0.0);
		
		double profileRotAngle = Math.atan2(noseMaxPos.y-tailMaxPos.y, noseMaxPos.x-tailMaxPos.x);
		
//DEBUG		System.out.printf("Diff in Nose: %f Length: %f Toolpath rot angle: %f\n",nose-tail, board.getLength()-BezierPatch.ZERO, profileRotAngle);

		setOffsetAndRotation(new Vector3d(-tailBottom*Math.sin(profileRotAngle),0,-(tailMaxPos.y*Math.cos(profileRotAngle)) ), profileRotAngle, 10.0);
		
		writeToolpath(filename, board, null);	
	}

	public void writeOutline(String filename, AbstractBoard board)
	{
		filename =  FileTools.append(filename, "_outline");		
		mCurrentState = State.STATE_OUTLINE;

		setOffsetAndRotation(new Vector3d(0,0,0), 0, 10.0);

		writeToolpath(filename, board, null); 
	}
	
	public void init()
	{
		super.init();

		//Reset values
		length = mBoard.getLength();
		nrOfLengthSplits = (length/x_res);
		y = 0;
		i = 0;
		j = 0;
	}

	//Actually the profile
	public Point3d getToolpathCoordinate()
	{
		if(i >= passes)
		{
			return null;
		}

		double x=0,y=0,z=0;
		
		if(i == (mBeginAtNose?1:0) )
		{
			x = j*x_res;
		}
		else
		{
			x = length - (j*x_res);					
		}

		if(x > length)
		{
			x = length;
		}
		if(x < 0)
		{
			x = 0;
		}

		y = 0;

		if(mCurrentState == State.STATE_PROFILE)
		{
	
			//Do the Deck one side
			
			if(i == 0)
			{
				z = mBoard.getDeckAt(x,y);
	
				mNormalVec = mBoard.getDeckNormalAt(x, y);
			}
			else if (i == 1)
			{
				z = mBoard.getBottomAt(x,y);
				
				mNormalVec = mBoard.getBottomNormalAt(x, y);				
			}
		
		}
		else if(mCurrentState == State.STATE_OUTLINE)
		{
			z = (mBoard.getWidthAt(x)/2.0)*((i==0)?1.0:-1.0);
	
			mNormalVec = mBoard.getDeckNormalAt(x, z);		
		}

		if(++j > nrOfLengthSplits)
		{
			j=0;
			i++;
		}

		//DEBUG
//		System.out.printf("i:%d x:%f y:%f z:%f\n", i, x, y, z);

		return new Point3d(x,y,z);
	}


	@Override
	public Vector3d getToolpathNormalVector() {
		return mNormalVec;
	}
	
	@Override
	public void next() {
	}

	public boolean isAtStringer()
	{
		return false;
	}
	
	
	@Override
	public double calcSpeed(Point3d pos, Vector3d normal, AbstractBoard board, boolean isAtStringer) 
	{
		return mSpeed;
	}

}
