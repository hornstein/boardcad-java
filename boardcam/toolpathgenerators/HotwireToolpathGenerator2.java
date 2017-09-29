package boardcam.toolpathgenerators;

import java.awt.geom.Point2D;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cadcore.BezierSpline;

import board.BezierBoard;
import boardcam.cutters.AbstractCutter;
import boardcam.writers.AbstractMachineWriter;


public class HotwireToolpathGenerator2 extends HotwireToolpathGenerator {
	
	BezierBoard mBrd = null;
	
	double mExpand = 0.0;
	
	public HotwireToolpathGenerator2(AbstractCutter cutter, AbstractMachineWriter writer, double speed)
	{
		super(cutter, writer, speed);
		
		mExpand = 0.0;
	}
	
	public HotwireToolpathGenerator2(AbstractCutter cutter, AbstractMachineWriter writer, double speed, double expand)
	{
		super(cutter, writer, speed);
		
		mExpand = expand;
	}
	
	public void init()
	{
		super.init();

		mBeginAtNose = true;
		nrOfLengthSplits = 5000;
		mBrd = (BezierBoard)mBoard;
	}

	//Actually the profile
	public Point3d getToolpathCoordinate()
	{
		
		BezierSpline currentPatch = null;
		double tt = 0;
		
		if(mCurrentState == State.STATE_PROFILE)
		{
			if(i == 0)
			{
				currentPatch = mBrd.getDeck();
			}
			else if(i == 1)
			{
				currentPatch = mBrd.getBottom();				
			}
			else if(i == 2)
			{
				currentPatch = mBrd.getDeck();				
			}
			else
				return null;
					
		}
		else if(mCurrentState == State.STATE_OUTLINE)
		{
			currentPatch = mBrd.getOutline();							
		}
		
		if(mCurrentState == State.STATE_OUTLINE)
		{
			if(i >=2)
				return null;
			
			if(i == (mBeginAtNose?1:0) )
			{
				tt = (j/nrOfLengthSplits);
			}
			else
			{
				tt = (1.0 - (j/nrOfLengthSplits));					
			}


		}
		else if(mCurrentState == State.STATE_PROFILE)
		{
			if(mBeginAtNose)
			{
				if(i == 0)
				{
					tt =  (1.0 - (j/nrOfLengthSplits));
					if(tt < 0)
					{
						tt = 0.0;
						j=0;
						i++;
					}
				}
				else if(i == 1)
				{
					tt = (j/nrOfLengthSplits);
				}
				else if(i == 2)
				{
					tt =  (1.0 - (j/nrOfLengthSplits));					
					if(tt < (double)currentPatch.getNrOfControlPoints()-1.0/(double)currentPatch.getNrOfControlPoints() )
					{
						return null;
					}
				}
				else 
					return null;
				
			}
			else
			{
				if(i == 0)
				{
					tt = (j/nrOfLengthSplits);
				}
				else if(i == 1)
				{
					tt =  (1.0 - (j/nrOfLengthSplits));					
				}
				else 
					return null;
			}
			
		}
		
		Point2D.Double point = currentPatch.getPointByTT(tt);
		
		
		if(mCurrentState == State.STATE_PROFILE)
		{
			double angle = currentPatch.getNormalByTT(tt);

			mNormalVec = new Vector3d(Math.cos(angle), 0.0, Math.sin(angle));
			
			if(i == 0)
			{
				point.y += mExpand;				
			}
		}
		else if(mCurrentState == State.STATE_OUTLINE)
		{
			
			double angle = currentPatch.getNormalByTT(tt);

			mNormalVec = new Vector3d(Math.cos(angle), 0.0, Math.sin(angle));

			if(i == 1)
			{
				point.y = -point.y;		//Mirror
				mNormalVec.z = -mNormalVec.z;
				
				point.y -= mExpand/2.0;
			}
			else
			{
				point.y += mExpand/2.0;
			}
			
		}

		if(++j > nrOfLengthSplits)
		{
			j=0;
			i++;
		}

		//DEBUG
//		System.out.printf("i:%f j:%f tt:%f x:%f y:%f z:%f\n", i, j, tt, point.x, 0.0, point.y);

		return new Point3d(point.x,0.0,point.y);
	}

}

