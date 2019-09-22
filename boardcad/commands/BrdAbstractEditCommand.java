package boardcad.commands;

import java.util.ArrayList;

import board.BezierBoard;
import cadcore.BezierKnot;
import cadcore.BezierSpline;

//Class with functions for storing changes to a brd for undo/redo functionality
//This is to pick up on changes from scaling and translation for fitting rocker to zero 
//and fitting cross sections to width and thickness
abstract class BrdAbstractEditCommand extends BrdInputCommand implements Cloneable
{
	class ControlPointChange implements Cloneable
	{
		BezierKnot mChangedPoint;
		BezierKnot mBefore;
		BezierKnot mAfter;

		ControlPointChange(BezierKnot point, BezierKnot before, BezierKnot after)
		{
			mChangedPoint = point;
			mBefore = before;
			mAfter = after;
		}

		void undo()
		{
			mChangedPoint.set(mBefore);
		}

		void redo()
		{
			mChangedPoint.set(mAfter);
		}

		public Object clone()
		{
			ControlPointChange controlPointChange = null;
			try {
				controlPointChange =  (ControlPointChange)super.clone();
			} catch(CloneNotSupportedException e) {
				System.out.println("BrdAbstractEditCommand.clone() Exception: " + e.toString());				
				throw new Error("CloneNotSupportedException in BrdAbstractEditCommand");
			}

			controlPointChange.mChangedPoint = (BezierKnot)mChangedPoint;
			controlPointChange.mBefore = (BezierKnot)mBefore.clone();
			controlPointChange.mAfter = (BezierKnot)mAfter.clone();

			return controlPointChange;
		}


	}

	static private ArrayList<BezierKnot> mPoints = new ArrayList<BezierKnot>();
	static private ArrayList<BezierKnot> mBeforeChangePoints = new ArrayList<BezierKnot>();

	private ArrayList<ControlPointChange> mChanges;

	void saveBeforeChange(BezierBoard brd)
	{
		mPoints.clear();
		mBeforeChangePoints.clear();
		mChanges = new ArrayList<ControlPointChange>();

		BezierSpline currentBezier = null;
		BezierKnot point = null;
		for(int i=0;i<brd.getCrossSections().size()+3;i++)
		{
			switch(i)
			{
			case 0:
				currentBezier = brd.getOutline();
				break;
			case 1:
				currentBezier = brd.getDeck();
				break;
			case 2:
				currentBezier = brd.getBottom();
				break;
			default:
				currentBezier = brd.getCrossSections().get(i-3).getBezierSpline();			
			break;
			}

			for(int j = 0; j < currentBezier.getNrOfControlPoints(); j++)
			{
				point = currentBezier.getControlPoint(j);
				addPoint(point);
			}
		}
	}

	void saveChanges()
	{
		BezierKnot point = null;
		BezierKnot before = null;
		for(int i = 0; i < mPoints.size(); i++)
		{
			point = mPoints.get(i);
			before = mBeforeChangePoints.get(i);
			if(!point.equals(before))
			{
				mChanges.add(new ControlPointChange(point, before, (BezierKnot)point.clone()));
			}
		}
	}


	protected void addPoint(BezierKnot point)
	{
		mPoints.add(point);			
		mBeforeChangePoints.add((BezierKnot)point.clone());					
	}

	protected void removePoint(BezierKnot point)
	{
		int index = mPoints.indexOf(point);
		mPoints.remove(index);					
		mBeforeChangePoints.remove(index);
	}

	public void execute()
	{
		saveChanges();

		super.execute();
	}

	public void undo()
	{
		for(int i = 0; i < mChanges.size(); i++)
		{
			mChanges.get(i).undo();
		}		

		super.undo();
	}

	public void redo()
	{
		for(int i = 0; i < mChanges.size(); i++)
		{
			mChanges.get(i).redo();
		}		

		super.redo();
	}

	public Object clone()
	{
		BrdAbstractEditCommand abstractEditCommand = (BrdAbstractEditCommand)super.clone();

		abstractEditCommand.mChanges = new ArrayList<ControlPointChange>();
		for(int i = 0; i < this.mChanges.size(); i++)
		{
			abstractEditCommand.mChanges.add((ControlPointChange)this.mChanges.get(i).clone());
		}

		return abstractEditCommand;
	}
}