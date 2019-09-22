package boardcad.commands;

import java.awt.Point;
import java.awt.event.MouseEvent;

import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;

public class SetImageNoseCommand extends BrdInputCommand
{

	public SetImageNoseCommand()
	{
		mCanUndo = false;
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();

		source.adjustBackgroundImageNose(pos);

	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADJUSTIMAGETONOSECMD_STR");
	}
}