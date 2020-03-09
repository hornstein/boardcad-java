package boardcad.commands;

import board.BezierBoard;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;

public class BrdRemoveCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mRemovedCrossSection = null;


	public BrdRemoveCrossSectionCommand(BoardEdit source, BezierBoardCrossSection crossSection)
	{
		mSource = source;

		mRemovedCrossSection = crossSection;
	}

	public void execute()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.removeCrossSection(mRemovedCrossSection);

		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.addCrossSection(mRemovedCrossSection);

		super.undo();

	}

	public void redo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.removeCrossSection(mRemovedCrossSection);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("REMOVECROSSECTIONCMD_STR");
	}
}