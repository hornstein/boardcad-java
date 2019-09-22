package boardcad.commands;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.i18n.LanguageResource;

public class BrdZoomCommand extends BrdInputCommand
{
	Point mClickedPos;
	Point mZoomRectCorner;
	double mNewScale;
	boolean mButtonPressed = false;

	public BrdZoomCommand()
	{
		mCanUndo = false;

		mClickedPos = new Point();
		mZoomRectCorner = new Point();
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();
		mClickedPos.setLocation(pos);
		mButtonPressed = true;
		mNewScale = source.getScale();
	}

	public void onMouseDragged(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();

		zoomSetDims(source, pos);
	}

	public void onLeftMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
		source.setScale(source.getScale()/mNewScale);

		source.setOffsetX((source.getOffsetX()-mClickedPos.x)/mNewScale);
		source.setOffsetY((source.getOffsetY()-mClickedPos.y)/mNewScale);

		source.disableDrawZoomRectangle();
		source.repaint();

		BoardCAD.getInstance().mLifeSizeButton.getModel().setSelected(false);

		execute();
	}

	public void zoomSetDims(BoardEdit source, Point pos)
	{
		Dimension dim = source.getSize();

		double scaleX = (double)Math.abs(pos.x - mClickedPos.x)/(double)dim.width;
		double scaleY = (double)Math.abs(pos.y - mClickedPos.y)/(double)dim.height;
		double usedScale = (scaleX > scaleY)?scaleX:scaleY;

		mZoomRectCorner.setLocation(mClickedPos.x+(dim.width*usedScale), mClickedPos.y+(dim.height*usedScale));

		mNewScale = usedScale;

		source.setDrawZoomRectangle(mClickedPos, mZoomRectCorner);
		source.repaint();
	}

	public void zoom(BoardEdit source)
	{
		source.setScale(source.getScale()/mNewScale);

		source.setOffsetX((source.getOffsetX()-mClickedPos.x)/mNewScale);
		source.setOffsetY((source.getOffsetY()-mClickedPos.y)/mNewScale);

		source.disableDrawZoomRectangle();
		source.repaint();

//		BoardCAD.getInstance().mLifeSizeButton.getModel().setSelected(false);

		execute();
	}

	public void zoomInStep(BoardEdit source, boolean fine)
	{
		Dimension dim = source.getSize();
		Dimension scaledDim = source.getSize();
		scaledDim.width *= (fine?0.99:0.9);
		scaledDim.height *= (fine?0.99:0.9);

		Point pos = new Point();
		mClickedPos.x = (dim.width - scaledDim.width)/2;
		mClickedPos.y = (dim.height - scaledDim.height)/2;

		pos.x = mClickedPos.x + scaledDim.width;
		pos.y = mClickedPos.y + scaledDim.height;

		zoomSetDims(source, pos);

		zoom(source);
	}

	public void zoomOutStep(BoardEdit source, boolean fine)
	{
		Dimension dim = source.getSize();
		Dimension scaledDim = source.getSize();
		scaledDim.width *= (fine?1.01:1.1);
		scaledDim.height *= (fine?1.01:1.1);

		Point pos = new Point();
		mClickedPos.x = (dim.width - scaledDim.width)/2;
		mClickedPos.y = (dim.height - scaledDim.height)/2;

		pos.x = mClickedPos.x + scaledDim.width;
		pos.y = mClickedPos.y + scaledDim.height;

		zoomSetDims(source, pos);

		zoom(source);
	}

	public boolean onKeyEvent(BoardEdit source, KeyEvent event)
	{
		if(event.getID() != KeyEvent.KEY_PRESSED)
			return false;

		int key = event.getKeyChar();

		if(key == '+')
		{
			zoomInStep(source, event.isAltDown());

			return true;
		}
		else if(key == '-')
		{
			zoomOutStep(source, event.isAltDown());

			return true;
		}
		return false;
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ZOOMCMD_STR");
	}

}