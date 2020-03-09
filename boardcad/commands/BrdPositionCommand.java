package boardcad.commands;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.vecmath.Vector3d;

import boardcad.gui.jdk.BoardEdit;
import boardcad.gui.jdk.Machine2DView;

public class BrdPositionCommand extends BrdInputCommand
{
	double mOriginalOffsetX;
	double mOriginalOffsetY;
	Point mPressedPos;
	boolean mButtonPressed = true;
	
	BrdPanCommand mPanCommand = new BrdPanCommand();
	BrdZoomCommand mZoomCommand = new BrdZoomCommand();
	boolean mIsPaning = false;
	
	Machine2DView mView = null;
	
	public BrdPositionCommand(Machine2DView view)
	{
		mView = view;
		mCanUndo = false;
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		if(!mView.mShowDeck)
			return;
		
		Point pos = event.getPoint();
		Vector3d deckOffset = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
		mOriginalOffsetX = deckOffset.x;
		mOriginalOffsetY = deckOffset.z;
		mPressedPos = (Point)pos.clone();
		mButtonPressed = true;
	}


	public void onMouseDragged(BoardEdit source, MouseEvent event)
	{
		if(mIsPaning)
		{
			mPanCommand.onMouseDragged(source, event);
			return;
		}
		
		if(!mView.mShowDeck)
			return;
					
		if(!mButtonPressed)
			return;
		
		Point pos = event.getPoint();
		
		double offsetX = mOriginalOffsetX + (((pos.x - mPressedPos.x))*(event.isAltDown()?0.1:1.0)/mView.mScale);
		double offsetY = mOriginalOffsetY - (((pos.y - mPressedPos.y))*(event.isAltDown()?0.1:1.0)/mView.mScale);
//		System.out.printf("offsetX: %f, offsetY: %f\n",offsetX, offsetY);
		mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(offsetX, 0.0, offsetY));

		//System.out.printf("onMouseDragged mView.mConfig:%s this:%s\n", mView.mConfig.toString(), this.toString());
		
		source.repaint();
	}

	public void onLeftMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
		mButtonPressed = false;
	}
	
	public void onMouseWheelMoved(BoardEdit source, MouseWheelEvent event)
	{
		int scroll = event.getWheelRotation();

		int steps = scroll*((scroll>0)?1:-1);
		for(int i = 0; i < steps; i++)
		{
			if(scroll < 0)
			{
				mZoomCommand.zoomInStep(source, event.isAltDown());
			}
			else
			{
				mZoomCommand.zoomOutStep(source, event.isAltDown());				
			}

		}
		event.consume();
	}

	public void onMouseWheelButtonPressed(BoardEdit source, MouseEvent event)
	{
		mPanCommand.onLeftMouseButtonPressed(source, event);
		mIsPaning = true;
	}

	public void onMouseWheelButtonReleased(BoardEdit source, MouseEvent event)
	{
		mPanCommand.onLeftMouseButtonReleased(source, event);
		mIsPaning = false;
	}
	
	public boolean onKeyEvent(BoardEdit source, KeyEvent event)
	{

		if(event.getID() != KeyEvent.KEY_PRESSED)
			return false;

		int key = event.getKeyCode();
		
//		System.out.printf("onKeyEvent mView.mConfig:%s this:%s\n", mView.mConfig.toString(), this.toString());

		if(key == KeyEvent.VK_LEFT)
		{
			if(!mView.mShowDeck)
				return false;

			Vector3d deckOffset = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
			double offsetX = (deckOffset.x-((event.isAltDown()?0.1:1.0f)/source.getScale()));
			mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(offsetX, 0.0, deckOffset.z));
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_RIGHT)
		{
			if(!mView.mShowDeck)
				return false;

			Vector3d deckOffset = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
			double offsetX = (deckOffset.x+((event.isAltDown()?0.1:1.0f)/source.getScale()));
			mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(offsetX, 0.0, deckOffset.z));
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_UP)
		{
			if(!mView.mShowDeck)
				return false;

			Vector3d deckOffset = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
			double offsetY = (deckOffset.z+((event.isAltDown()?0.1:1.0f)/source.getScale()));
			mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(deckOffset.x, 0.0, offsetY));
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_DOWN)
		{
			if(!mView.mShowDeck)
				return false;

			Vector3d deckOffset = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
			double offsetY = (deckOffset.z-((event.isAltDown()?0.1:1.0f)/source.getScale()));
			mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(deckOffset.x, 0.0, offsetY));
			source.repaint();
			return true;
		}
		if(key == KeyEvent.VK_Q)
		{
			if(!mView.mShowDeck)
				return false;

			double rot = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetAngle();
			rot = rot-(event.isAltDown()?0.0004:0.004f);
			mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetAngle(rot);
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_W)
		{
			if(!mView.mShowDeck)
				return false;

			double rot = mView.mConfig.getBlankHoldingSystem().getBoardDeckOffsetAngle();
			rot = rot+(event.isAltDown()?0.0004:0.004f);
			mView.mConfig.getBlankHoldingSystem().setBoardDeckOffsetAngle(rot);
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_SPACE)
		{
			mView.mShowDeck = !mView.mShowDeck;
			source.repaint();
			return true;
		}
		return false;
	}

	public String getCommandString()
	{
		return "Position";
	}

}
