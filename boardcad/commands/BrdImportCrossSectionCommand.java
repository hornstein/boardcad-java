package boardcad.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import board.BezierBoard;
import board.readers.BrdReader;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;
import cadcore.BezierSpline;

public class BrdImportCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mCrossSection = null;
	BezierSpline mOldCrossSectionBezier = null;
	BezierSpline mNewCrossSectionBezier = null;


	public BrdImportCrossSectionCommand(BoardEdit source)
	{
		mSource = source;

	}

	public void execute()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();

		mCrossSection = brd.getCurrentCrossSection();

		mOldCrossSectionBezier = mCrossSection.getBezierSpline();

		double pos = mCrossSection.getPosition();

		final JFileChooser fc = new JFileChooser();

		fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

		int returnVal = fc.showOpenDialog(BoardCAD.getInstance().getFrame());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		String filename = file.getPath();    // Load and display selection
		if(filename == null)
			return;

		try
		{
			if(BrdReader.importCrossection(brd, new BufferedReader(new FileReader(filename))) < 0)
			{
				throw new FileNotFoundException();
			}
		}
		catch(Exception e)
		{
			String str = e.toString();
	        JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str, LanguageResource.getString("IMPORTCROSSECTIONFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

	        return;
		}
		
		
		mNewCrossSectionBezier = (BezierSpline)brd.getCurrentCrossSection().clone();

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
		return LanguageResource.getString("IMPORTCROSSECTIONCMD_STR");
	}
}