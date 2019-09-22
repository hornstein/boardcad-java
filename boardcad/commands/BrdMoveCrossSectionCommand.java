package boardcad.commands;

import board.BezierBoard;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;

public class BrdMoveCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mMovedCrossSection = null;
	double mOldPos;
	double mNewPos;


	public BrdMoveCrossSectionCommand(BoardEdit source, BezierBoardCrossSection crossSection, double newPos)
	{
		mSource = source;
		mMovedCrossSection = crossSection;
		mNewPos = newPos;
	}

	public void execute()
	{
		BezierBoard brd = mSource.getCurrentBrd();
		mMovedCrossSection.setPosition(mNewPos);
		brd.sortCrossSections();

		super.execute();

	}

	public void undo()
	{
		BezierBoard brd = mSource.getCurrentBrd();
		mMovedCrossSection.setPosition(mOldPos);
		brd.sortCrossSections();

		super.undo();

	}

	public void redo()
	{
		BezierBoard brd = mSource.getCurrentBrd();
		mMovedCrossSection.setPosition(mNewPos);
		brd.sortCrossSections();

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MOVECROSSECTIONCMD_STR");
	}
}