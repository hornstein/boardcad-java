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

public class BrdImportOutlineCommand extends BrdCommand
{
	BezierSpline mOldOutline = null;
	BezierSpline mNewOutline = null;


	public BrdImportOutlineCommand(BoardEdit source)
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

		mOldOutline = brd.getOutline();
		
		mNewOutline = new BezierSpline();
		brd.setOutline(mNewOutline);
		
		try
		{
			if(BrdReader.importOutline(brd, new BufferedReader(new FileReader(filename))) < 0)
			{
				throw new FileNotFoundException();
			}
		}
		catch(Exception e)
		{
			String str = e.toString();
	        JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str, LanguageResource.getString("IMPORTOUTLINEFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

	        return;
		}
		
		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		brd.setOutline(mOldOutline);
		
		super.undo();
	}

	public void redo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		brd.setOutline(mNewOutline);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("PASTECROSSECTIONCMD_STR");
	}
}