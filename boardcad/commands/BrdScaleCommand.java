package boardcad.commands;

import javax.swing.JOptionPane;

import board.BezierBoard;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.gui.jdk.ScaleBoardInputDialog;
import boardcad.i18n.LanguageResource;

public class BrdScaleCommand extends BrdCommand
{
	double mOldWidth;
	double mOldLength;
	double mOldThickness;
	double mNewWidth;
	double mNewLength;
	double mNewThickness;
	//double[] mFinsOld;
	boolean mScaleFins;
	boolean mScaleFinsFactor;
	boolean mScaleBottomRocker;

	public BrdScaleCommand(BoardEdit source)
	{
		mSource = source;
	}

	public void execute()
	{

		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();

		ScaleBoardInputDialog dialog = new ScaleBoardInputDialog(BoardCAD.getInstance().getFrame());
		dialog.setModal(true);
		//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(LanguageResource.getString("SCALEBOARDTITLE_STR"));
		dialog.setBoardLength(brd.getLength());
		dialog.setBoardWidth(brd.getMaxWidth());
		dialog.setBoardThick(brd.getMaxThickness());

		dialog.setVisible(true);

		if(dialog.wasCancelled())
		{
			dialog.dispose();
			return;
		}
		
		//mFinsOld=brd.mFins;
		mOldLength= brd.getLength();
		mOldWidth = brd.getMaxWidth();
		mOldThickness = brd.getMaxThickness();
		
		if(!dialog.scaleThroughFactor())
		{
			mNewLength = dialog.getBoardLength();
			mNewWidth = dialog.getBoardWidth();
			mNewThickness = dialog.getBoardThick();
			
			mScaleFins = dialog.scaleFins();
			
			mScaleBottomRocker = dialog.scaleBottomRocker();
			
			boolean overCurve = dialog.useOverCurve();
		
			dialog.dispose();
	
			if(mNewLength <= 0 || mNewWidth <= 0 || mNewThickness <= 0)
			{
				JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), LanguageResource.getString("SCALEBOARDINVALIDINPUTERRORMSG_STR"), LanguageResource.getString("SCALEBOARDINVALIDINPUTERRORTITLE_STR"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(!overCurve)
			{
				if(mScaleBottomRocker)
				{
					brd.scaleAccordingly(mNewLength,mNewWidth,mNewThickness);
				}
				else {
					brd.scale(mNewLength,mNewWidth,mNewThickness);
				}
				if(mScaleFins)
				{				
					brd.finScaling(mNewLength/mOldLength,mNewWidth/mOldWidth);
				}
			}
			else
			{
				double newLengthOverCurve = mNewLength;
				double guestimatedNewLength = 0;
				for(int i = 0; i < 5; i++)
				{
					guestimatedNewLength = brd.getLength() * (newLengthOverCurve/brd.getLengthOverCurve()) + 0.01;	//cheat by adding 0.1 mm
					if(mScaleBottomRocker)
					{
						brd.scaleAccordingly(guestimatedNewLength, mNewWidth, mNewThickness);					
					}
					else
					{
						brd.scale(guestimatedNewLength, mNewWidth, mNewThickness);											
					}
				}
				if(mScaleFins)
				{					
					brd.finScaling(guestimatedNewLength/mOldLength,mNewWidth/mOldWidth);
				}
	
				//Get the actual new length, used for redo
				mNewLength = brd.getLength();
//...why is the actual length different from the length the user asked for, particularly when "constraint proportions is on"??				
			}
	
			super.execute();				

			BoardCAD.getInstance().onBrdChanged();
			BoardCAD.getInstance().fitAll();
		}else  
		{ //scaleTroughFactor:

			mNewLength = brd.getLength() * dialog.getFactor();
			mNewWidth = brd.getMaxWidth() * dialog.getFactor();
			mNewThickness = brd.getMaxThickness() * dialog.getFactor();
		
			mScaleFinsFactor = dialog.scaleFinsFactor();
			
			mScaleBottomRocker = true;
			
			dialog.dispose();
	
			if(mNewLength <= 0 || mNewWidth <= 0 || mNewThickness <= 0 || dialog.getFactor()<=0)
			{
				JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), LanguageResource.getString("SCALEBOARDINVALIDINPUTERRORMSG_STR"), LanguageResource.getString("SCALEBOARDINVALIDINPUTERRORTITLE_STR"), JOptionPane.ERROR_MESSAGE);
				return;
			}
	
			brd.scale(mNewLength,mNewWidth,mNewThickness);
			if(mScaleFinsFactor)
			{
				brd.finScaling(dialog.getFactor(),dialog.getFactor());
			}
			
			super.execute();

			BoardCAD.getInstance().onBrdChanged();
			BoardCAD.getInstance().fitAll();			
		}
	}

	public void undo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(mScaleBottomRocker)
		{
			brd.scaleAccordingly(mOldLength,mOldWidth,mOldThickness);					
		}
		else
		{
			brd.scale(mOldLength,mOldWidth,mOldThickness);											
		}
		if(mScaleFins || mScaleFinsFactor)
		{
			brd.finScaling(mOldLength/mNewLength,mOldWidth/mNewWidth);
		}
		
		super.undo();
		
		BoardCAD.getInstance().fitAll();
	}

	public void redo()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(mScaleBottomRocker)
		{
			brd.scaleAccordingly(mNewLength, mNewWidth, mNewThickness);					
		}
		else
		{
			brd.scale(mNewLength, mNewWidth, mNewThickness);											
		}
		if(mScaleFins || mScaleFinsFactor)
		{
			brd.finScaling(mNewLength/mOldLength,mNewWidth/mOldWidth);
		}

		super.redo();

		BoardCAD.getInstance().fitAll();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("SCALEBOARDCMD_STR");
	}
}