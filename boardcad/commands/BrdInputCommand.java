package boardcad.commands;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import boardcad.gui.jdk.BoardEdit;

public abstract class BrdInputCommand extends BrdCommand
{
	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
	}

	public void onLeftMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
	}

	public void onRightMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
	}

	public void onRightMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
	}

	public void onMouseWheelButtonPressed(BoardEdit source, MouseEvent event)
	{
	}

	public void onMouseWheelButtonReleased(BoardEdit source, MouseEvent event)
	{
	}

	public void onMouseMove(BoardEdit source, MouseEvent event)
	{
	}

	public void onMouseDragged(BoardEdit source, MouseEvent event)
	{
	}


	public boolean onKeyEvent(BoardEdit source, KeyEvent event)
	{
		return false;
	}

	public void onMouseWheelMoved(BoardEdit source, MouseWheelEvent e)
	{
	}
}