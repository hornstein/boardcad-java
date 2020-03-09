package boardcad.commands;

import board.BezierBoard;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;

public class BrdAddCrossSectionCommand extends BrdCommand
{
	double mPos;
	BezierBoardCrossSection mNewCrossSection = null;


	public BrdAddCrossSectionCommand(BoardEdit source, double pos)
	{
		mSource = source;

		mPos = pos;
	}

	public void execute()
	{
		BezierBoard brd = mSource.getCurrentBrd();

//		mNewCrossSection = (BezierBoardCrossSection)brd.getNearestCrossSection(mPos).clone();
		mNewCrossSection = (BezierBoardCrossSection)brd.getInterpolatedCrossSection(mPos).clone();

		mNewCrossSection.setPosition(mPos);

		mNewCrossSection.scale(brd.getThicknessAtPos(mPos), brd.getWidthAtPos(mPos));

		brd.addCrossSection(mNewCrossSection);

		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.removeCrossSection(mNewCrossSection);

		super.undo();

	}

	public void redo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.addCrossSection(mNewCrossSection);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADDCROSSECTIONCMD_STR");
	}
}