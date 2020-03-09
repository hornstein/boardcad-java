package boardcad.commands;

import java.awt.Point;
import java.awt.event.MouseEvent;

import cadcore.MathUtils;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.gui.jdk.BoardHandler;
import boardcad.i18n.LanguageResource;

public class BrdRotateViewCommand extends BrdInputCommand
{
	Point mClickedPos;
	Point mZoomRectCorner;
	double mNewScale;
	boolean mButtonPressed = false;
	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;

	public BrdRotateViewCommand()
	{
		mCanUndo = false;

		mClickedPos = new Point();
		mZoomRectCorner = new Point();
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		clicked_x=event.getX();
		clicked_y=event.getY();

	}

	public void onMouseDragged(BoardEdit source, MouseEvent event)
	{
		BoardHandler board_handler=BoardCAD.getInstance().getBoardHandler();				
		
		if(source.mDrawControl == 0)
		{
			dragged_x=event.getX();
			dragged_y=event.getY();
			double theta2=source.theta+dragged_x-clicked_x;
			double[][] m1 = {{Math.cos(-theta2*Math.PI/180.0), 0.0, -Math.sin(-theta2*Math.PI/180.0)},
					{0.0, 1.0, 0.0},
			                {Math.sin(-theta2*Math.PI/180.0), 0.0, Math.cos(-theta2*Math.PI/180.0)}}; 
			double zeta2=source.zeta+dragged_y-clicked_y;
			double[][] m2 = {{1.0, 0.0, 0.0},
					{0.0, Math.cos(-zeta2*Math.PI/180.0), -Math.sin(-zeta2*Math.PI/180.0)},
			                {0.0, Math.sin(-zeta2*Math.PI/180.0), Math.cos(-zeta2*Math.PI/180.0)}}; 
			source.mRotationMatrix=MathUtils.cross_product(m1,m2);
				//design_panel.redraw();
		}
	}

	public void onLeftMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
	
		source.theta=source.theta+dragged_x-clicked_x;
		source.zeta=source.zeta+dragged_y-clicked_y;

		source.repaint();

		execute();
	}



	public String getCommandString()
	{
		return LanguageResource.getString("ROTATEVIEWCMD_STR");
	}



}