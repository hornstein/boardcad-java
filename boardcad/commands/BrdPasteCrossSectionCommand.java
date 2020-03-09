package boardcad.commands;

import board.BezierBoard;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;
import cadcore.BezierSpline;

public class BrdPasteCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mCrossSection = null;
	BezierSpline mOldCrossSectionBezier = null;
	BezierSpline mNewCrossSectionBezier = null;


	public BrdPasteCrossSectionCommand(BoardEdit source, BezierBoardCrossSection currentCrossSection, BezierBoardCrossSection copyCrossSection)
	{
		mSource = source;
		mCrossSection = currentCrossSection;
		mOldCrossSectionBezier = currentCrossSection.getBezierSpline();
		mNewCrossSectionBezier = (BezierSpline)copyCrossSection.getBezierSpline().clone();
	}

	public void execute()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		double pos = mCrossSection.getPosition();

		mCrossSection.setBezierSpline(mNewCrossSectionBezier);

		mCrossSection.scale(brd.getThicknessAtPos(pos), brd.getWidthAtPos(pos));

		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		double pos = mCrossSection.getPosition();

		mCrossSection.setBezierSpline(mOldCrossSectionBezier);

		mCrossSection.scale(brd.getThicknessAtPos(pos), brd.getWidthAtPos(pos));

		super.undo();
	}

	public void redo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		double pos = mCrossSection.getPosition();

		mCrossSection.setBezierSpline(mNewCrossSectionBezier);

		mCrossSection.scale(brd.getThicknessAtPos(pos), brd.getWidthAtPos(pos));

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("PASTECROSSECTIONCMD_STR");
	}
}