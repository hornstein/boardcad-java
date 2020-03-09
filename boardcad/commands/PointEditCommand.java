package boardcad.commands;

import java.awt.geom.Point2D;

import boardcad.i18n.LanguageResource;

public class PointEditCommand extends BrdCommand
{
	Point2D.Double mEditedPoint;
	Point2D.Double mOriginalPos;
	Point2D.Double mNewPos;

	PointEditCommand(Point2D.Double point)
	{
		mEditedPoint = point;
		mOriginalPos = (Point2D.Double)point.clone();
	}

	public void execute(Point2D.Double newPos)
	{
		mNewPos = (Point2D.Double)newPos.clone();
		redo();
	}

	public void redo()
	{
		mEditedPoint.setLocation(mNewPos);
	}

	public void undo()
	{
		mEditedPoint.setLocation(mOriginalPos);
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MACROCMD_STR");
	}
}