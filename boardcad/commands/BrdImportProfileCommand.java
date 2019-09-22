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
import cadcore.BezierSpline;

public class BrdImportProfileCommand extends BrdCommand
{
	BezierSpline mOldDeck = null;
	BezierSpline mOldBottom = null;
	BezierSpline mNewDeck = null;
	BezierSpline mNewBottom = null;

	public BrdImportProfileCommand(BoardEdit source)
	{
		mSource = source;
	}

	public void execute()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		final JFileChooser fc = new JFileChooser();

		fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

		int returnVal = fc.showOpenDialog(BoardCAD.getInstance().getFrame());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		String filename = file.getPath();    // Load and display selection
		if(filename == null)
			return;

		mOldDeck = brd.getDeck();
		mOldBottom = brd.getBottom();
		
		mNewDeck = new BezierSpline();
		brd.setDeck(mNewDeck);
		mNewBottom = new BezierSpline();
		brd.setBottom(mNewBottom);
		
		try
		{
			if(BrdReader.importProfile(brd, new BufferedReader(new FileReader(filename))) < 0)
			{
				throw new FileNotFoundException();
			}
		}
		catch(Exception e)
		{
			String str = e.toString();
	        JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str, LanguageResource.getString("IMPORTPROFILEFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

	        return;
		}
		
		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		brd.setDeck(mOldDeck);
		brd.setBottom(mOldBottom);
		
		super.undo();
	}

	public void redo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		brd.setDeck(mNewDeck);
		brd.setBottom(mNewBottom);
		
		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("PASTECROSSECTIONCMD_STR");
	}
}