package boardcad.commands;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierCurve;
import cadcore.BezierFit;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.VecMath;

public class BrdDeleteControlPointCommand extends BrdAbstractEditCommand
{
	BezierKnot mDeletedControlPoint;
	BezierSpline mControlPoints;
	int mIndex;

	public BrdDeleteControlPointCommand(BoardEdit source, BezierKnot ControlPoint, BezierSpline ControlPoints)
	{
		mSource = source;

		mDeletedControlPoint = ControlPoint;

		mControlPoints = ControlPoints;
	}

	public void doAction()
	{
		super.saveBeforeChange(mSource.getCurrentBrd());
		
		mIndex = mControlPoints.indexOf(mDeletedControlPoint);

		ArrayList<Point2D> points = new ArrayList<Point2D>();
				
		int steps = 10;
		
		BezierCurve prevCurve = mControlPoints.getCurve(mIndex-1);
		for(int i = 0; i <= steps; i++)
		{
			double t = (double)i/(double)steps;
			points.add(new Point2D.Double(prevCurve.getXValue(t),prevCurve.getYValue(t)) );
		}
		BezierCurve nextCurve = mControlPoints.getCurve(mIndex);
		for(int i = 0; i < steps; i++)
		{
			double t = (double)i/(double)steps;
			points.add(new Point2D.Double(nextCurve.getXValue(t),nextCurve.getYValue(t)) );
		}
		
		BezierKnot prev =  mControlPoints.getControlPoint(mIndex-1);
		BezierKnot next =  mControlPoints.getControlPoint(mIndex+1);

		mSource.mSelectedControlPoints.remove(mDeletedControlPoint);

		super.removePoint(mDeletedControlPoint);

		mControlPoints.remove(mDeletedControlPoint);
					
		//Pass to bezierFit
		BezierFit fitter = new BezierFit();
		Point2D[] ctrlPoints = fitter.bestFit(points);
			
		//Update bezier curve 
		prev.setContinous(false);
		prev.setEndPoint(ctrlPoints[0].getX(),ctrlPoints[0].getY());
		prev.setTangentToNext(ctrlPoints[1].getX(),ctrlPoints[1].getY());
		next.setTangentToPrev(ctrlPoints[2].getX(),ctrlPoints[2].getY());
		next.setEndPoint(ctrlPoints[3].getX(),ctrlPoints[3].getY());
		next.setContinous(false);	

		super.saveChanges();
	}

	public void execute()
	{		
		doAction();

		mSource.onBrdChanged();

		super.execute();
	}

	public void undo()
	{
		mControlPoints.insert(mIndex, mDeletedControlPoint);

		super.undo();
	}

	public void redo()
	{
		mControlPoints.remove(mDeletedControlPoint);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("DELETECONTROLPOINTCMD_STR");

	}
}