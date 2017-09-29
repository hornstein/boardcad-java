package boardcam.toolpathgenerators;

import cadcore.*;
import board.AbstractBoard;
import board.BezierBoard;
import boardcam.cutters.AbstractCutter;
import boardcam.writers.AtuaCoresMachineWriter;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import boardcad.FileTools;
import boardcad.i18n.LanguageResource;

public class AtuaCoresToolpathGenerator extends HotwireToolpathGenerator2 {
	
	double mRotAngle;

	public AtuaCoresToolpathGenerator()
	{
		super(new AbstractCutter() {
			@Override
			public double[] calcOffset(Point3d point, Vector3d normal,
					AbstractBoard board) {
				return new double[] { point.x, point.y, point.z };
			}

		},
		new AtuaCoresMachineWriter(), 0, 0);	//Speed is irrelevant
	}
	
	public void writeProfile(String filename, AbstractBoard board, boolean noRotation)
	{
		filename =  FileTools.append(filename, LanguageResource.getString("ATUAPPENDPROFILE_STR"));
		mCurrentState = State.STATE_PROFILE;
		
		BezierBoard brd = (BezierBoard)board;
		
		mRotAngle = 0.0;
		double rotatedLength = board.getLength();
		
		double noseBottom = board.getBottomAt(board.getLength()-BezierSpline.ZERO, 0.0);
		double tailBottom = brd.getBottomAtPos(0.1, 0.0);

		Point2d tailMaxPos = brd.getMaxDeckAtTailPos();	
		Point2d noseMaxPos = brd.getMaxDeckAtNosePos();
			
		if(!noRotation)
		{
			mRotAngle = Math.atan2(noseMaxPos.y-tailMaxPos.y, noseMaxPos.x-tailMaxPos.x);
			
			rotatedLength = (Math.sin(mRotAngle)*(noseBottom-tailBottom)) + (Math.cos(mRotAngle)*board.getLength());	
	
			System.out.printf("Diff in Nose: %f Length: %f Toolpath rot angle: %f\n",noseMaxPos.y-tailMaxPos.y, noseMaxPos.x-tailMaxPos.x, mRotAngle*180.0/Math.PI);

			setOffsetAndRotation(new Vector3d(-tailBottom*Math.sin(mRotAngle),0,-(tailMaxPos.y*Math.cos(mRotAngle)) ), mRotAngle, 1.0/rotatedLength);
		}
		else
		{
			setOffsetAndRotation(new Vector3d(0.0, 0.0,-noseMaxPos.y), 0.0, 1.0/rotatedLength);			
		}
		
		
		mBeginAtNose = true;

		writeToolpath(filename, board, null);	

	}

	public void writeOutline(String filename, AbstractBoard board)
	{
		filename =  FileTools.append(filename, LanguageResource.getString("ATUAPPENDOUTLINE_STR"));		
		mCurrentState = State.STATE_OUTLINE;
		
		setOffsetAndRotation(new Vector3d(0,0,0), 0.0, 1.0/board.getLength());

		mBeginAtNose = true;

		writeToolpath(filename, board, null); 
	}

	public void init()
	{
		super.init();		
	}

	protected void writeToolpathBegin()
	{
		BezierBoard brd = (BezierBoard)mBoard;
		
		mCurrentWriter.writeComment(mStream, "Type: " + ((mCurrentState == State.STATE_PROFILE )?"Rocker":"Outline") );
		mCurrentWriter.writeComment(mStream, "" );
		mCurrentWriter.writeComment(mStream, "Code: " + brd.getFilename());
		mCurrentWriter.writeComment(mStream, "" );
		if(mCurrentState == State.STATE_PROFILE)
		{
			mCurrentWriter.writeComment(mStream, "Longueur template BoardCAD: " + UnitUtils.convertLengthToUnit(brd.getLength(), false, UnitUtils.MILLIMETERS)); 
			double tail = brd.getBottomAt(BezierSpline.ZERO, 0.0);
			double nose = brd.getBottomAt(brd.getLength()-BezierSpline.ZERO, 0.0);
			double rotatedLength = Math.sqrt(Math.pow(nose-tail,2)+ Math.pow(brd.getLength(), 2));	
			mCurrentWriter.writeComment(mStream, "Longueur template avec rotation: " + UnitUtils.convertLengthToUnit(rotatedLength, false, UnitUtils.MILLIMETERS));
			double maxDeck = 0.0;
			double maxBottom = 0.0;
			for(int i=0; i < brd.getLength(); i++)
			{
				//Get values
				double deckY = brd.getDeckAt(i, 0.0);
				double bottomY = brd.getBottomAt(i, 0.0);
				
				//Transform values
				Point3d deckPoint = new Point3d(i, deckY, 0.0);
				Point3d bottomPoint = new Point3d(i, bottomY, 0.0);
				
				Point3d deckTransformed = transformPoint(deckPoint);
				Point3d bottomTransformed = transformPoint(bottomPoint);
				
				if(deckTransformed.y > maxDeck)
				{
					maxDeck = deckTransformed.y;
				}
				
				if(bottomTransformed.y > maxBottom)
				{
					maxBottom = bottomTransformed.y;
				}
			}
			mCurrentWriter.writeComment(mStream, "Required thickness: " + UnitUtils.convertLengthToUnit(maxDeck-maxBottom + 12, false, UnitUtils.MILLIMETERS)); 
		}
		else if(mCurrentState == State.STATE_OUTLINE)
		{
			mCurrentWriter.writeComment(mStream, "Longueur: " + UnitUtils.convertLengthToUnit(brd.getLength(), false, UnitUtils.MILLIMETERS)); 			
			mCurrentWriter.writeComment(mStream, "" );
		}

		mCurrentWriter.writeComment(mStream, "x                              y" );

	}
	
	protected void writeToolpathEnd()
	{
		//Do nothing
	}
}
