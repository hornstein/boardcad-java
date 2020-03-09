package boardcad.commands;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierCurve;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.VecMath;

public class BrdAddControlPointCommand extends BrdAbstractEditCommand
{
	static double K = 1.0f;

	private int mIndex = 0;
	private BezierKnot mNewControlPoint = null;
	private BezierSpline mSpline;


	public BrdAddControlPointCommand()
	{
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		super.saveBeforeChange(source.getCurrentBrd());

		Point pos = event.getPoint();
		Point2D.Double brdPos = source.screenCoordinateToBrdCoordinate(pos);

		BezierSpline[] splines = source.getActiveBezierSplines(source.getCurrentBrd());
		mNewControlPoint = new BezierKnot();
		
		for(int i = 0; i < splines.length; i++)
		{
			mIndex = splines[i].getSplitControlPoint(brdPos, mNewControlPoint);

			if(mIndex < 0)
				continue;
			
			mSpline = splines[i];
	
			mSpline.insert(mIndex, mNewControlPoint);
	
			Point2D.Double tmp = new Point2D.Double();
	
			BezierKnot prev = mSpline.getControlPoint(mIndex-1);
			BezierKnot next = mSpline.getControlPoint(mIndex+1);
			
			BezierCurve tmpCurve = new BezierCurve(prev, next);
	
			double t = tmpCurve.getClosestT(brdPos);
	
			VecMath.subVector(prev.getPoints()[0],prev.getPoints()[2],tmp);
			VecMath.scaleVector(tmp, t);
			VecMath.addVector(prev.getPoints()[0],tmp,prev.getPoints()[2]);
	
			VecMath.subVector(next.getPoints()[1],next.getPoints()[0],tmp);
			VecMath.scaleVector(tmp, t-1);
			VecMath.addVector(next.getPoints()[0],tmp,next.getPoints()[1]);
	
			source.onBrdChanged();
	
			execute();
	
			source.repaint();
		}

	}

	public void redo()
	{
		mSpline.insert(mIndex, mNewControlPoint);

		super.redo();
	}
	
	public void undo()
	{
		mSpline.remove(mIndex);

		super.undo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADDCONTROLPOINTCMD_STR");
	}
}