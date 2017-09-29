package boardcad.gui.jdk;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import board.BezierBoard;
import boardcad.FileTools;
import boardcad.i18n.LanguageResource;
import board.readers.*;

public class BoardLoadAction extends AbstractAction {
	static final long serialVersionUID=1L;

	BezierBoard mBrd = null;
	BezierBoard mCloneBrd = null;
	JFrame mFrame = null;

	BoardLoadAction()
	{
		
	};

	public BoardLoadAction(BezierBoard brd)
	{
		mFrame = BoardCAD.getInstance().getFrame();
		mBrd = brd;
	}

	public BoardLoadAction(BezierBoard brd, BezierBoard cloneBrd)
	{
		mFrame = BoardCAD.getInstance().getFrame();
		mBrd = brd;
		mCloneBrd = cloneBrd;
	}

	public BoardLoadAction(JFrame frame, BezierBoard brd)
	{
		mFrame = frame;
		mBrd = brd;
	}


	public void actionPerformed(ActionEvent event)
	{
//		Create a file dialog box to prompt for a new file to display
		FileFilter filter = new FileFilter()
		{

//			Accept all directories and brd and s3d files.
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				String extension = FileTools.getExtension(f);
				if (extension != null && (extension.equals("brd") || extension.equals("s3d") || extension.equals("srf") || extension.equals("cad") || extension.equals("stp") || extension.equals("step")))
				{
					return true;
				}

				return false;
			}

//			The description of this filter
			public String getDescription() {
				return "Board files";
			}



		};

		final JFileChooser fc = new JFileChooser();
		fc.setFileView(new BoardFileView());
		fc.setAccessory(new BoardPreview(fc));
		fc.addChoosableFileFilter(filter);

		fc.setAcceptAllFileFilterUsed(false);

		fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

		int returnVal = fc.showOpenDialog(mFrame);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		String filename = file.getPath();    // Load and display selection
		if(filename == null)
			return;

		load(filename);

	}
	
	public void load(String filename)
	{
		BoardCAD.defaultDirectory = filename;

		String ext = FileTools.getExtension(filename);

		int ret = 0;
		String errorStr="";
		if(ext.compareToIgnoreCase("s3d")==0)
		{
			ret = S3dReader.loadFile(mBrd, filename);
			
			if(ret == 1)	//Show warning dialog
			{
				JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), LanguageResource.getString("S3DTHICKNESSCURVENOTSUPPOSTEDMSG_STR"), LanguageResource.getString("S3DTHICKNESSCURVENOTSUPPOSTEDTITLE_STR"), JOptionPane.WARNING_MESSAGE);
			}
			
			if(ret < 0)
			{
				errorStr = S3dReader.getErrorStr();
			}
		}
		else if(ext.compareToIgnoreCase("srf")==0)
		{
			ret = SrfReader.loadFile(mBrd, filename);
			if(ret < 0)
			{
				errorStr = SrfReader.getErrorStr();
			}
		}
		else if(ext.compareToIgnoreCase("cad")==0)
		{
			BoardCAD.getInstance().board_handler.open_board(filename);	
			BoardCAD.getInstance().design_panel.view_all();
			BoardCAD.getInstance().design_panel.fit_all();
			BoardCAD.getInstance().design_panel.update_3d();
		}
		else if(ext.compareToIgnoreCase("stp")==0 || ext.compareToIgnoreCase("step")==0)
		{
			BoardCAD.getInstance().board_handler.open_board(filename,mBrd);
			BoardCAD.getInstance().design_panel.view_all();
			BoardCAD.getInstance().design_panel.fit_all();
			BoardCAD.getInstance().design_panel.update_3d();
			mBrd.setFilename(filename);
		}			
		else
		{
			ret = BrdReader.loadFile(mBrd, filename);
			if(ret < 0)
			{
				errorStr = BrdReader.getErrorStr();
			}
		}
		
		if(ret < 0)
		{
	        JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), errorStr, LanguageResource.getString("READBRDFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);			
		}

		if(mCloneBrd != null)
		{
			mCloneBrd.set(mBrd);
		}

	}
}

