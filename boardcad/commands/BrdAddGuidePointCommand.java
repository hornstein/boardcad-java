package boardcad.commands;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;

public class BrdAddGuidePointCommand extends BrdInputCommand
{
	public BrdAddGuidePointCommand()
	{
		mCanUndo = false;

	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();
		Point2D.Double guidePoint = source.screenCoordinateToBrdCoordinate(pos);

		source.getGuidePoints().add(guidePoint);
		source.repaint();
		BoardCAD.getInstance().getGuidePointsDialog().update();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADDGUIDEPOINTCMD_STR");
	}
}