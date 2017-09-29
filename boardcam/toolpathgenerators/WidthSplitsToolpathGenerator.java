package boardcam.toolpathgenerators;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.AbstractBoard;
import boardcam.cutters.AbstractCutter;
import boardcam.holdingsystems.AbstractBlankHoldingSystem;
import boardcam.writers.AbstractMachineWriter;
import boardcam.MachineConfig;
import boardcad.gui.jdk.BoardCAD;	//TODO: Bad dependency
import boardcad.FileTools;
import boardcad.settings.Settings;	//TODO: Bad dependency
import boardcad.i18n.LanguageResource;

public class WidthSplitsToolpathGenerator extends AbstractToolpathGenerator {

	enum State {STATE_DECK, STATE_BOTTOM};
	
	State mCurrentState;

	double x_res = 1;
	static int nrOfWidthSplits = 30; 
	double length = 0;
	double nrOfLengthSplits = 0;
	double y = 0;
	int i = 0;
	int j = 0;
	Vector3d mNormalVec;

	private double mNormalSpeed = 0;
	private double mStringerSpeed = 0;
	private double mRailSpeed = 0;
	private double mOutlineSpeed = 0;
	
	MachineConfig mConfig;

	public WidthSplitsToolpathGenerator(AbstractCutter cutter, AbstractBlankHoldingSystem holdingSystem, AbstractMachineWriter writer, MachineConfig config)
	{
		super(writer, BoardCAD.getInstance().getFrame());
		
		mConfig = config;
		
		setCutter(cutter);
		setBlankHoldingSystem(holdingSystem);

		Settings speedSettings = mConfig.getCategory("Speed");
		mNormalSpeed = speedSettings.getDouble(MachineConfig.CUTTING_SPEED);
		mStringerSpeed = speedSettings.getDouble(MachineConfig.CUTTING_SPEED_STRINGER);
		mRailSpeed = speedSettings.getDouble(MachineConfig.CUTTING_SPEED_RAIL);
		mOutlineSpeed = speedSettings.getDouble(MachineConfig.CUTTING_SPEED_OUTLINE);
	}

	public void writeToolpath(String filename, AbstractBoard board)
	{
		String topFileName = FileTools.append(filename, LanguageResource.getString("APPENDTOPTOFILENAME_STR"));
		mCurrentState = State.STATE_DECK;
		super.writeToolpath(topFileName, board, null);

		String bottomFileName = FileTools.append(filename, LanguageResource.getString("APPENDBOTTOMTOFILENAME_STR"));
		mCurrentState = State.STATE_BOTTOM;
		super.writeToolpath(bottomFileName, board, null);
	}


	public Point3d getToolpathCoordinate()
	{
		if(mCurrentState == State.STATE_DECK)
		{
			//Check if all splits done
			if(i >= nrOfWidthSplits*2)
			{
				//Reset values
				length = 0;
				nrOfLengthSplits = 0;
				y = 0;
				i = 0;
				j = 0;
	
				return null;
			}
	
			if(length == 0)
				length = mBoard.getLength();
			if(nrOfLengthSplits == 0)
				nrOfLengthSplits = (length/x_res);
	
			//Do the Deck one side
			double x=0,y=0,z=0;
	
			if(i%2 == 0)
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
			y = (mBoard.getWidthAt(x)/2.0)*((double)(i%nrOfWidthSplits)/(double)nrOfWidthSplits)*((i>nrOfWidthSplits)?-1.0:1.0);
			z = mBoard.getDeckAt(x,y);
	
			mNormalVec = mBoard.getDeckNormalAt(x, y);
	
			if(++j >= nrOfLengthSplits)
			{
				j=0;
				i++;
			}
	
			return new Point3d(x,y,z);
		}
		else if(mCurrentState == State.STATE_BOTTOM)
		{
			//Check if all splits done
			if(i >= nrOfWidthSplits*2)
			{
				//Reset values
				length = 0;
				nrOfLengthSplits = 0;
				y = 0;
				i = 0;
				j = 0;
	
				return null;
			}
	
			if(length == 0)
				length = mBoard.getLength();
			if(nrOfLengthSplits == 0)
				nrOfLengthSplits = (length/x_res);
	
			//Do the Deck one side
			double x=0,y=0,z=0;
	
			if(i%2 == 0)
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
			y = (mBoard.getWidthAt(x)/2.0)*((double)(i%nrOfWidthSplits)/(double)nrOfWidthSplits)*((i>nrOfWidthSplits)?-1.0:1.0);
			z = mBoard.getBottomAt(x,y);
	
			mNormalVec = mBoard.getBottomNormalAt(x, y);
	
			if(++j >= nrOfLengthSplits)
			{
				j=0;
				i++;
			}
	
			return new Point3d(x,y,z);
				
		}

		return null;
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
		return (i==0);
	}

	@Override
	public double calcSpeed(Point3d pos, Vector3d normal, AbstractBoard board, boolean isAtStringer) 
	{
		double currentSpeed = mNormalSpeed;
		
		if(isAtStringer)
		{
			currentSpeed = mStringerSpeed;
		}
			
//		if(pos.x < mTailSpeedReductionDistance)
//		{
//			
//			double i = (pos.x/mTailSpeedReductionDistance);
//			
//			double level = ((1-mTailSpeedReduction)*(1-i) + mTailSpeedReduction);
//			
//			currentSpeed = currentSpeed * level;
//			
//		}
//		else if(pos.x > board.getLength()-mNoseSpeedReductionDistance)
//		{
//			double i = (pos.x/mNoseSpeedReductionDistance);
//			
//			double level = ((1-mNoseSpeedReduction)*(1-i) + mNoseSpeedReduction);
//			
//			currentSpeed = currentSpeed * level;
//		}

		return currentSpeed;
	}
}
