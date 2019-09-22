package boardcad.commands;

import boardcad.gui.jdk.BoardCAD;
import boardcad.i18n.LanguageResource;

public class BrdSpotCheckCommand extends BrdCommand
{
	double mOldOffsetX;
	double mOldOffsetY;
	double mOldScale;


	public BrdSpotCheckCommand()
	{
	}

	public void spotCheck()
	{
		mSource = BoardCAD.getInstance().getSelectedEdit();

		mOldOffsetX = mSource.getOffsetX();
		mOldOffsetY = mSource.getOffsetY();
		mOldScale = mSource.getScale();

		mSource.fitBrd();
		mSource.repaint();
	}

	public void restore()
	{
		mSource.setOffsetX(mOldOffsetX);
		mSource.setOffsetY(mOldOffsetY);
		mSource.setScale(mOldScale);
		mSource.repaint();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("SPOTCHECKCMD_STR");
	}

}