package boardcad.commands;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;

public class BrdPanCommand extends BrdInputCommand
{
	double mOriginalOffsetX;
	double mOriginalOffsetY;
	Point mPressedPos;
	boolean mButtonPressed = true;

	public BrdPanCommand()
	{
		mCanUndo = false;
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();
		mOriginalOffsetX = source.getOffsetX();
		mOriginalOffsetY = source.getOffsetY();
		mPressedPos = (Point)pos.clone();
		mButtonPressed = true;
	}


	public void onMouseDragged(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();
		source.setOffsetX(mOriginalOffsetX + (pos.x - mPressedPos.x));
		source.setOffsetY(mOriginalOffsetY + (pos.y - mPressedPos.y));
		source.repaint();
	}

	public void onLeftMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
		mButtonPressed = false;
	}

	public boolean onKeyEvent(BoardEdit source, KeyEvent event)
	{
		if(event.getID() != KeyEvent.KEY_PRESSED)
			return false;

		int key = event.getKeyCode();

		if(key == KeyEvent.VK_LEFT)
		{
			source.setOffsetX(source.getOffsetX()-(1.0f*source.getScale()));
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_RIGHT)
		{
			source.setOffsetX(source.getOffsetX()+(1.0f*source.getScale()));
			source.repaint();
			return true;
		}
		if(key == KeyEvent.VK_UP)
		{
			source.setOffsetY(source.getOffsetY()-(1.0f*source.getScale()));
			source.repaint();
			return true;
		}
		else if(key == KeyEvent.VK_DOWN)
		{
			source.setOffsetY(source.getOffsetY()+(1.0f*source.getScale()));
			source.repaint();
			return true;
		}
		return false;
	}

	public String getCommandString()
	{
		return LanguageResource.getString("PANCMD_STR");
	}

}