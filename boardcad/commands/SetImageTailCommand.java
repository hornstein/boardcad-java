package boardcad.commands;

import java.awt.Point;
import java.awt.event.MouseEvent;

import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;

public class SetImageTailCommand extends BrdInputCommand
{

	public SetImageTailCommand()
	{
		mCanUndo = false;
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();

		source.adjustBackgroundImageTail(pos);

	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADJUSTIMAGETOTAILCMD_STR");
	}

}