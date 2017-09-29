package boardcad.gui.jdk;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import board.*;

import cadcore.*;
import boardcad.i18n.LanguageResource;
import board.readers.*;

class BrdCommandHistory
{
	LinkedList<BrdCommand> mCommandHistory = new LinkedList<BrdCommand>();
	int mCurrentCommand = -1;
	private static BrdCommandHistory mInstance = null;

	public static BrdCommandHistory getInstance()
	{
		if(mInstance == null) {
			mInstance = new BrdCommandHistory();
		}
		return mInstance;
	}

	public void clear()
	{
		mCommandHistory.clear();
		mCurrentCommand = -1;
	}

	public void addCommand(BrdCommand command)
	{
		if(mCurrentCommand>=0 && mCommandHistory.size() > mCurrentCommand+1)
		{
			/*			Sometimes get java.util.ConcurrentModificationException with this code
			 * 			int size = mCommandHistory.size();	
			java.util.List<BrdCommand> toBeRemoved = mCommandHistory.subList(mCurrentCommand+1, size);
			mCommandHistory.removeAll(toBeRemoved);
			 */
			while(mCommandHistory.size() > mCurrentCommand+1)
			{
				mCommandHistory.removeLast();
			}
		}
		mCommandHistory.add(command);
		mCurrentCommand = mCommandHistory.size()-1;
	}

	public void undo()
	{
		if(mCurrentCommand >= 0)
		{
			BrdCommand command = mCommandHistory.get(mCurrentCommand--);
			if(command == null)
				return;
			command.undo();
		}
	}

	public void redo()
	{
		if(mCurrentCommand < mCommandHistory.size()-1 && mCommandHistory.size() > 0)
		{
			BrdCommand command = mCommandHistory.get(++mCurrentCommand);
			if(command == null)
				return;
			command.redo();
		}
	}
}

abstract class BrdCommand extends Object implements Cloneable{
	boolean mCanUndo = true;
	BoardEdit mSource = null;
	BrdCommand mPreviousCommand = null;

	void setPreviousCommand(BrdCommand previousCommand)
	{
		mPreviousCommand = previousCommand;
	}

	public void doAction()
	{

	}

	public void execute()
	{
		if(canUndo())
		{
			BrdCommandHistory.getInstance().addCommand((BrdCommand)this.clone());
		}

		if(mPreviousCommand != null)
		{
			BoardCAD.getInstance().setCurrentCommand(mPreviousCommand);
		}

	}; 	//Do command including user interactions

	public void setSource(BoardEdit source)
	{
		mSource = source;
	}

	public void redo()
	{
		if(mSource != null)
		{
			BoardCAD.getInstance().setSelectedEdit(mSource);
			mSource.onBrdChanged();
			BoardCAD.getInstance().onControlPointChanged();
		}

	}

	public void undo()
	{
		if(mSource != null)
		{
			BoardCAD.getInstance().setSelectedEdit(mSource);
			mSource.onBrdChanged();
			BoardCAD.getInstance().onControlPointChanged();
		}
	}
	public boolean canUndo(){return mCanUndo;};

	public Object clone(){
		try {
			return super.clone();
		} catch(CloneNotSupportedException e) {
			System.out.println("BoardComand.clone() Exception: " + e.toString());
			throw new Error("CloneNotSupportedException in BrdCommand");
		}
	}

	public void onSetCurrent()
	{

	}

	public void onCurrentChanged()
	{

	}

	public abstract String getCommandString();
}

class BrdMacroCommand extends BrdCommand {

	LinkedList<BrdCommand> mBrdCommand;

	BrdMacroCommand()
	{
		mBrdCommand = new LinkedList<BrdCommand>();
	}

	public void add(BrdCommand cmd)
	{
		mBrdCommand.add(cmd);
	}

	public void execute()
	{
		for(Iterator i = mBrdCommand.iterator(); i.hasNext();)
		{
			((BrdCommand)i.next()).doAction();
		}
		super.execute();
	}
	public void redo()
	{
		for(Iterator i = mBrdCommand.iterator(); i.hasNext();)
			((BrdCommand)i.next()).redo();

		super.redo();
	}

	public void undo()
	{
//		Java 6		for(Iterator i = mBrdCommand.descendingIterator(); i.hasNext();)
//		((BrdCommand)i.next()).undo();

		for(ListIterator i = mBrdCommand.listIterator(mBrdCommand.size()); i.hasPrevious();)
		{
			((BrdCommand)i.previous()).undo();
		}

		super.undo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MACROCMD_STR");
	}

}



class PointEditCommand extends BrdCommand
{
	Point2D.Double mEditedPoint;
	Point2D.Double mOriginalPos;
	Point2D.Double mNewPos;

	PointEditCommand(Point2D.Double point)
	{
		mEditedPoint = point;
		mOriginalPos = (Point2D.Double)point.clone();
	}

	public void execute(Point2D.Double newPos)
	{
		mNewPos = (Point2D.Double)newPos.clone();
		redo();
	}

	public void redo()
	{
		mEditedPoint.setLocation(mNewPos);
	}

	public void undo()
	{
		mEditedPoint.setLocation(mOriginalPos);
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MACROCMD_STR");
	}
}


class NurbsEditCommand extends BrdCommand
{
	NurbsBoard oldBoard;
	NurbsBoard newBoard;
	BoardHandler board_handler;
	
	NurbsEditCommand()
	{
		NurbsBoard board;
		board_handler=BoardCAD.getInstance().getBoardHandler();
		board=board_handler.getActiveBoard();
		if(board!=null)
			oldBoard = (NurbsBoard)board.clone();
	}

	public void execute()
	{
		NurbsBoard board;
		board=board_handler.getActiveBoard();
		if(board!=null)
			newBoard = (NurbsBoard)board.clone();
		super.execute();
	}

	public void redo()
	{
		board_handler.setActiveBoard(newBoard);
	}

	public void undo()
	{
		board_handler.setActiveBoard(oldBoard);
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MACROCMD_STR");
	}
}



abstract class BrdInputCommand extends BrdCommand
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


class BrdPanCommand extends BrdInputCommand
{
	double mOriginalOffsetX;
	double mOriginalOffsetY;
	Point mPressedPos;
	boolean mButtonPressed = true;

	BrdPanCommand()
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




class BrdZoomCommand extends BrdInputCommand
{
	Point mClickedPos;
	Point mZoomRectCorner;
	double mNewScale;
	boolean mButtonPressed = false;

	BrdZoomCommand()
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


class BrdRotateViewCommand extends BrdInputCommand
{
	Point mClickedPos;
	Point mZoomRectCorner;
	double mNewScale;
	boolean mButtonPressed = false;
	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;

	BrdRotateViewCommand()
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
			double[][] m1 = {{Math.cos(-theta2*3.1415/180.0), 0.0, -Math.sin(-theta2*3.1415/180.0)},
					{0.0, 1.0, 0.0},
			                {Math.sin(-theta2*3.1415/180.0), 0.0, Math.cos(-theta2*3.1415/180.0)}}; 
			double zeta2=source.zeta+dragged_y-clicked_y;
			double[][] m2 = {{1.0, 0.0, 0.0},
					{0.0, Math.cos(-zeta2*3.1415/180.0), -Math.sin(-zeta2*3.1415/180.0)},
			                {0.0, Math.sin(-zeta2*3.1415/180.0), Math.cos(-zeta2*3.1415/180.0)}}; 
			source.mRotationMatrix=cross_product(m1,m2);
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

	private double[][] cross_product(double[][] m1, double[][] m2)
	{
		double[][] m=new double[3][3];
		
		m[0][0]=m1[0][0]*m2[0][0]+m1[1][0]*m2[0][1]+m1[2][0]*m2[0][2];
		m[1][0]=m1[0][0]*m2[1][0]+m1[1][0]*m2[1][1]+m1[2][0]*m2[1][2];
		m[2][0]=m1[0][0]*m2[2][0]+m1[1][0]*m2[2][1]+m1[2][0]*m2[2][2];

		m[0][1]=m1[0][1]*m2[0][0]+m1[1][1]*m2[0][1]+m1[2][1]*m2[0][2];
		m[1][1]=m1[0][1]*m2[1][0]+m1[1][1]*m2[1][1]+m1[2][1]*m2[1][2];
		m[2][1]=m1[0][1]*m2[2][0]+m1[1][1]*m2[2][1]+m1[2][1]*m2[2][2];

		m[0][2]=m1[0][2]*m2[0][0]+m1[1][2]*m2[0][1]+m1[2][2]*m2[0][2];
		m[1][2]=m1[0][2]*m2[1][0]+m1[1][2]*m2[1][1]+m1[2][2]*m2[1][2];
		m[2][2]=m1[0][2]*m2[2][0]+m1[1][2]*m2[2][1]+m1[2][2]*m2[2][2];
		
		return m;
	
	}
	

}


class BrdAddGuidePointCommand extends BrdInputCommand
{
	BrdAddGuidePointCommand()
	{
		mCanUndo = false;

	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();
		Point2D.Double guidePoint = source.screenCoordinateToBrdCoordinate(pos);

		source.getGuidePoints().add(guidePoint);
		source.repaint();
		BoardCAD.getInstance().getGuidePointsDialog().update();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADDGUIDEPOINTCMD_STR");
	}
}

//Class with functions for storing changes to a brd for undo/redo functionality
//This is to pick up on changes from scaling and translation for fitting rocker to zero 
//and fitting cross sections to width and thickness
abstract class BrdAbstractEditCommand extends BrdInputCommand implements Cloneable
{
	class ControlPointChange implements Cloneable
	{
		BezierKnot mChangedPoint;
		BezierKnot mBefore;
		BezierKnot mAfter;

		ControlPointChange(BezierKnot point, BezierKnot before, BezierKnot after)
		{
			mChangedPoint = point;
			mBefore = before;
			mAfter = after;
		}

		void undo()
		{
			mChangedPoint.set(mBefore);
		}

		void redo()
		{
			mChangedPoint.set(mAfter);
		}

		public Object clone()
		{
			ControlPointChange controlPointChange = null;
			try {
				controlPointChange =  (ControlPointChange)super.clone();
			} catch(CloneNotSupportedException e) {
				System.out.println("BrdAbstractEditCommand.clone() Exception: " + e.toString());				
				throw new Error("CloneNotSupportedException in BrdAbstractEditCommand");
			}

			controlPointChange.mChangedPoint = (BezierKnot)mChangedPoint;
			controlPointChange.mBefore = (BezierKnot)mBefore.clone();
			controlPointChange.mAfter = (BezierKnot)mAfter.clone();

			return controlPointChange;
		}


	}

	static private ArrayList<BezierKnot> mPoints = new ArrayList<BezierKnot>();
	static private ArrayList<BezierKnot> mBeforeChangePoints = new ArrayList<BezierKnot>();

	private ArrayList<ControlPointChange> mChanges;

	void saveBeforeChange(BezierBoard brd)
	{
		mPoints.clear();
		mBeforeChangePoints.clear();
		mChanges = new ArrayList<ControlPointChange>();

		BezierSpline currentBezier = null;
		BezierKnot point = null;
		for(int i=0;i<brd.getCrossSections().size()+3;i++)
		{
			switch(i)
			{
			case 0:
				currentBezier = brd.getOutline();
				break;
			case 1:
				currentBezier = brd.getDeck();
				break;
			case 2:
				currentBezier = brd.getBottom();
				break;
			default:
				currentBezier = brd.getCrossSections().get(i-3).getBezierSpline();			
			break;
			}

			for(int j = 0; j < currentBezier.getNrOfControlPoints(); j++)
			{
				point = currentBezier.getControlPoint(j);
				addPoint(point);
			}
		}
	}

	void saveChanges()
	{
		BezierKnot point = null;
		BezierKnot before = null;
		for(int i = 0; i < mPoints.size(); i++)
		{
			point = mPoints.get(i);
			before = mBeforeChangePoints.get(i);
			if(!point.equals(before))
			{
				mChanges.add(new ControlPointChange(point, before, (BezierKnot)point.clone()));
			}
		}
	}


	protected void addPoint(BezierKnot point)
	{
		mPoints.add(point);			
		mBeforeChangePoints.add((BezierKnot)point.clone());					
	}

	protected void removePoint(BezierKnot point)
	{
		int index = mPoints.indexOf(point);
		mPoints.remove(index);					
		mBeforeChangePoints.remove(index);
	}

	public void execute()
	{
		saveChanges();

		super.execute();
	}

	public void undo()
	{
		for(int i = 0; i < mChanges.size(); i++)
		{
			mChanges.get(i).undo();
		}		

		super.undo();
	}

	public void redo()
	{
		for(int i = 0; i < mChanges.size(); i++)
		{
			mChanges.get(i).redo();
		}		

		super.redo();
	}

	public Object clone()
	{
		BrdAbstractEditCommand abstractEditCommand = (BrdAbstractEditCommand)super.clone();

		abstractEditCommand.mChanges = new ArrayList<ControlPointChange>();
		for(int i = 0; i < this.mChanges.size(); i++)
		{
			abstractEditCommand.mChanges.add((ControlPointChange)this.mChanges.get(i).clone());
		}

		return abstractEditCommand;
	}
}

class BrdEditCommand extends BrdAbstractEditCommand
{
	static double KEY_MOVE_AMOUNT = 1.0f;

//	ArrayList<ControlPoint> mSelectedControlPointsCopy;
	ArrayList<BezierKnot> mControlPointsBeforeChange;
//	ArrayList<ControlPoint> mControlPointsAfterChange;
	Point2D.Double mDragStartPos;
	Point2D.Double mDragOffset;
	Point mBoxSelectStartPos;
	int mWhich = 0;
	boolean mIsDragging = false;
	boolean mIsKeyEditing = false;
	boolean mIsBoxSelecting = false;
	int mCurrentKeyCode = 0;
	int mRepeat = 1;

	BrdPanCommand mPanCommand = new BrdPanCommand();
	BrdZoomCommand mZoomCommand = new BrdZoomCommand();
	NurbsEditCommand mNurbsCommand = new NurbsEditCommand();
	
	boolean mIsPaning = false;

	final double MAX_OFF = 4.0f;

	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;
	protected static boolean is_marked;

	private NurbsPoint p;
	
	
	BrdEditCommand()
	{
	}

	public void execute()
	{
		/*		mControlPointsAfterChange = new ArrayList<ControlPoint>();
		for(int i = 0; i < mSelectedControlPointsCopy.size(); i++)
		{
			mControlPointsAfterChange.add((ControlPoint)mSelectedControlPointsCopy.get(i).clone());
		}
		 */
		mSource.onBrdChanged();	//adjust rocker to zero and cross sections to width and thickness
		super.execute();
		mIsDragging = false;
		mIsKeyEditing = false;
		mCurrentKeyCode = 0;
//		mSelectedControlPointsCopy = null;
		mControlPointsBeforeChange = null;
//		mControlPointsAfterChange = null;
		mDragStartPos = null;
		mDragOffset = null;
		mSource = null;
		mRepeat = 1;
	}

	public void onSetCurrent()
	{
		BoardCAD.getInstance().getControlPointInfo().setEnabled(true);
	}

	public void onCurrentChanged()
	{
		BoardCAD.getInstance().getControlPointInfo().setEnabled(false);
	}

	void saveControlPointsBeforeChange(BoardEdit source)
	{
		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();

//		Save the original points
		mControlPointsBeforeChange = new ArrayList<BezierKnot>();
//		mSelectedControlPointsCopy = new ArrayList<ControlPoint>();
		for(int i = 0; i < selectedControlPoints.size(); i++)
		{
			mControlPointsBeforeChange.add((BezierKnot)selectedControlPoints.get(i).clone());
//			mSelectedControlPointsCopy.add(selectedControlPoints.get(i));
		}
//		mControlPointsAfterChange = null;
		super.saveBeforeChange(source.getCurrentBrd());
	}

	public void moveControlPoints(double x_diff, double y_diff, int which)
	{

		ArrayList<BezierKnot> selectedControlPoints = mSource.getSelectedControlPoints();
		if(selectedControlPoints.size() > 1 || which == 0)
		{
			for(int i = 0; i < selectedControlPoints.size(); i++)
			{
				BezierKnot sel = selectedControlPoints.get(i);
				BezierKnot org = mControlPointsBeforeChange.get(i);

				sel.setControlPointLocation(org.getEndPoint().x+x_diff, org.getEndPoint().y+y_diff);

			}
		}
		else{
//			We know we only have a single point, so just use it directly
			selectedControlPoints.get(0).setLocation(mWhich, mControlPointsBeforeChange.get(0).getPoints()[mWhich].x+x_diff, mControlPointsBeforeChange.get(0).getPoints()[mWhich].y+y_diff);

			if(selectedControlPoints.get(0).isContinous())
			{

				int other = (which ==1)?2:1;

//				Calculate the length of the other end vector
				double ox = (double)mControlPointsBeforeChange.get(0).getPoints()[other].x - mControlPointsBeforeChange.get(0).getPoints()[0].x;
				double oy = (double)mControlPointsBeforeChange.get(0).getPoints()[other].y - mControlPointsBeforeChange.get(0).getPoints()[0].y;

				double ol = Math.sqrt(ox*ox+oy*oy);
				if(ol == 0)
					return;	//Avoid multiply by zero

//				Length of current
				double sx = (double)selectedControlPoints.get(0).getPoints()[which].x - mControlPointsBeforeChange.get(0).getPoints()[0].x;
				double sy = (double)selectedControlPoints.get(0).getPoints()[which].y - mControlPointsBeforeChange.get(0).getPoints()[0].y;

				double sl = Math.sqrt(sx*sx+sy*sy);
				if(sl == 0)
					return; //Avoid division by zero

//				Normalize
				sx /= sl;
				sy /= sl;

				selectedControlPoints.get(0).setLocation(other, (double)(-sx*ol) + selectedControlPoints.get(0).getPoints()[0].x,
						(double)(-sy*ol) + selectedControlPoints.get(0).getPoints()[0].y);
			}

			/*Debug			
	        System.out.println("Tangent to Prev angle: " + mSelectedControlPointsCopy.get(0).getTangentToPrevAngle() + " Tangent to Next angle: " + mSelectedControlPointsCopy.get(0).getTangentToNextAngle());
	        double a = mSelectedControlPointsCopy.get(0).getTangentToPrevAngle();
	        a = Math.abs(Math.PI - a);
	        double b = mSelectedControlPointsCopy.get(0).getTangentToNextAngle();	        
			boolean cont = (Math.abs(a-b) < 0.02)?true:false;
	        System.out.println("a: " + a + " b: " + b + " cont:" + cont);
			 */
		}
		BoardCAD.getInstance().onBrdChanged();
		BoardCAD.getInstance().onControlPointChanged();
		mSource.repaint();
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
	
		//marking nurbs points
		
		clicked_x=event.getX();
		clicked_y=event.getY();

		BoardHandler board_handler=BoardCAD.getInstance().getBoardHandler();				

		if(source.mDrawControl == 0)
		{
			clicked_x=event.getX();
			clicked_y=event.getY();
			board_handler.set_x((clicked_x-source.mOffsetX)/(source.mScale/10));
			board_handler.set_z((clicked_y-source.mOffsetY)/(source.mScale/10));
			is_marked=board_handler.outline_mark(source.mScale/10, source.mRotationMatrix);
			if(is_marked)
			{
				p=new NurbsPoint(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
			}		
		}		
		else if(source.mDrawControl == BezierBoardDrawUtil.MirrorY)
		{
			board_handler.set_x((clicked_x-source.mOffsetX)/(source.mScale/10));
			board_handler.set_z((clicked_y-source.mOffsetY)/(source.mScale/10));
			is_marked=board_handler.outline_mark(source.mScale/10);
		}
		else if(source.mDrawControl == BezierBoardDrawUtil.FlipY)
		{
			board_handler.set_x((clicked_x-source.mOffsetX)/(source.mScale/10));
			board_handler.set_y((-clicked_y+source.mOffsetY)/(source.mScale/10));
			is_marked=board_handler.rocker_mark(source.mScale/10);
		}
		else if(source.mDrawControl == (BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY))
		{
			board_handler.set_z((clicked_x-source.mOffsetX)/(source.mScale/10));
			board_handler.set_y((-clicked_y+source.mOffsetY)/(source.mScale/10));
			is_marked=board_handler.edge_mark(source.mScale/10);
		}
		
		if(is_marked)
		{
			BoardCAD.getInstance().status_panel.set_point_name(board_handler.get_point_name());
			BoardCAD.getInstance().status_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
			mNurbsCommand = new NurbsEditCommand();
		}
			
		
		//marking bezier point
	
		Point pos = event.getPoint();
		Point2D.Double brdPos = source.screenCoordinateToBrdCoordinate(pos);

		BezierSpline[] splines = source.getActiveBezierSplines(BoardCAD.getInstance().getCurrentBrd());
		if(splines == null)
			return;

		BezierKnot bestMatch = null;
		boolean hitControlPoint = false;
		for(int i = 0; i < splines.length; i++)
		{
			bestMatch = splines[i].findBestMatch(brdPos);
			if(bestMatch != null)
			{
				mWhich = splines[0].getBestMatchWhich();
				hitControlPoint = (double)brdPos.distance(bestMatch.getPoints()[mWhich]) < (MAX_OFF/source.getScale());
				if(hitControlPoint)
					break;
			}
		}
		
		if(bestMatch == null)
		{
			mSource = source;
			mIsBoxSelecting = true;
			mBoxSelectStartPos = pos;
			return;
		}

		if(!hitControlPoint)
		{
			mSource = source;
			mIsBoxSelecting = true;
			mBoxSelectStartPos = pos;

			if(!(event.isShiftDown() || event.isControlDown()))
			{
				source.clearSelectedControlPoints();	//If shift is held, don't clear
			}

		}
		else
		{
			boolean alreadySelected = source.mSelectedControlPoints.contains(bestMatch);

			if(!alreadySelected && (!(event.isShiftDown() || event.isControlDown())) )
				source.clearSelectedControlPoints();	//If shift or control is held or the ControlPoint is already selected, don't clear

			if(event.isControlDown())
				source.toggleSelectedControlPoint(bestMatch);
			else
				source.addSelectedControlPoint(bestMatch);
		}

		if(hitControlPoint && source.mSelectedControlPoints.contains(bestMatch))
		{
			mDragStartPos = source.screenCoordinateToBrdCoordinate(pos);

			mDragOffset = new Point2D.Double(bestMatch.getPoints()[mWhich].x-mDragStartPos.x,bestMatch.getPoints()[mWhich].y-mDragStartPos.y);

		}

		BoardCAD.getInstance().onControlPointChanged();
		source.repaint();
		BoardCAD.getInstance().redraw();

	}


	public void onMouseDragged(BoardEdit source, MouseEvent event)
	{


		//dragging nurbs points
		
//		clicked_x=event.getX();
//		clicked_y=event.getY();

		BoardHandler board_handler=BoardCAD.getInstance().getBoardHandler();				
		
		if(is_marked)
		{		
			if(source.mDrawControl == 0)
			{

				double[][] m=invert(source.mRotationMatrix);
				double myy=(source.mRotationMatrix[1][0]*p.x+source.mRotationMatrix[1][1]*p.y+source.mRotationMatrix[1][2]*p.z);

				board_handler.set_x( m[0][0]*(event.getX()-source.mOffsetX)/(source.mScale/10) + m[0][1]*myy + m[0][2]*(event.getY()-source.mOffsetY)/(source.mScale/10) );// + m[0][2]*100);
				board_handler.set_y( m[1][0]*(event.getX()-source.mOffsetX)/(source.mScale/10) + m[1][1]*myy + m[1][2]*(event.getY()-source.mOffsetY)/(source.mScale/10) );// + m[1][2]*100);
				board_handler.set_z( m[2][0]*(event.getX()-source.mOffsetX)/(source.mScale/10) + m[2][1]*myy + m[2][2]*(event.getY()-source.mOffsetY)/(source.mScale/10) );// + m[2][2]*100);					

				board_handler.set_point(source.mRotationMatrix);
			}
			else if(source.mDrawControl == BezierBoardDrawUtil.MirrorY)
			{
				if(!BoardCAD.getInstance().mIsLockedX.getState())
					board_handler.set_x((event.getX()-source.mOffsetX)/(source.mScale/10));
				if(!BoardCAD.getInstance().mIsLockedZ.getState())
					board_handler.set_z((event.getY()-source.mOffsetY)/(source.mScale/10));
				board_handler.set_point();
			}
			else if(source.mDrawControl == BezierBoardDrawUtil.FlipY)
			{
	
				if(!BoardCAD.getInstance().mIsLockedX.getState())
					board_handler.set_x((event.getX()-source.mOffsetX)/(source.mScale/10));
				if(!BoardCAD.getInstance().mIsLockedY.getState())
					board_handler.set_y((source.mOffsetY-event.getY())/(source.mScale/10));
				board_handler.set_point();
			}
			else if(source.mDrawControl == (BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY))
			{
				if(!BoardCAD.getInstance().mIsLockedY.getState())
					board_handler.set_y((-event.getY()+source.mOffsetY)/(source.mScale/10));
				if(!BoardCAD.getInstance().mIsLockedZ.getState())
					board_handler.set_z((event.getX()-source.mOffsetX)/(source.mScale/10));
				board_handler.set_point();			
			}
						
			BoardCAD.getInstance().redraw();
		}
		

		//dragging bezier points


		if(mIsKeyEditing)
			return;

		if(mIsPaning)
		{
			mPanCommand.onMouseDragged(source, event);
			return;
		}

		if(mIsBoxSelecting)
		{
			mSource.setDrawZoomRectangle(mBoxSelectStartPos, event.getPoint());

			return;
		}

		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(selectedControlPoints.size() == 0)
			return;

		Point pos = event.getPoint();
		Point2D.Double brdPos = source.screenCoordinateToBrdCoordinate(pos);

		if(mIsDragging == false)
		{
			/*			BezierPatch ControlPoints = source.getBezierControlPoints(BoardCAD.getInstance().getCurrentBrd());
			ControlPoint bestMatch = ControlPoints.findBestMatch(brdPos);
			mWhich = ControlPoints.getBestMatchWhich();

			double distance = (double)brdPos.distance(bestMatch.mPoints[mWhich]);

			if(distance > (MAX_OFF/source.getScale()) || bestMatch == null)
				return;	//trying to drag an unselected point

			if(!selectedControlPoints.contains(bestMatch))
				return;	//trying to drag an unselected point

			mDragStartPos = source.screenCoordinateToBrdCoordinate(pos);

			mDragOffset = new Point2D.Double(bestMatch.mPoints[mWhich].x-mDragStartPos.x,bestMatch.mPoints[mWhich].y-mDragStartPos.y);
			 */
			saveControlPointsBeforeChange(source);

			mIsDragging = true;
			mSource = source;
		}

		brdPos.x += mDragOffset.x;
		brdPos.y += mDragOffset.y;

		double x_diff = (brdPos.x - mDragStartPos.x)*(event.isAltDown()?.1f:1f);
		double y_diff = (brdPos.y - mDragStartPos.y)*(event.isAltDown()?.1f:1f);

		moveControlPoints(x_diff, y_diff, mWhich);


	}

	public void onLeftMouseButtonReleased(BoardEdit source, MouseEvent event)
	{
		if(is_marked)
		{
			mNurbsCommand.execute();
			is_marked=false;
		}
		
		if(mIsBoxSelecting)
		{
			mSource.disableDrawZoomRectangle();

			Point pos = event.getPoint();
			Point2D.Double boxStartPos = source.screenCoordinateToBrdCoordinate(mBoxSelectStartPos);
			Point2D.Double boxEndPos = source.screenCoordinateToBrdCoordinate(pos);

			if(boxStartPos.x > boxEndPos.x)
			{
				double x = boxStartPos.x;
				boxStartPos.x = boxEndPos.x;
				boxEndPos.x = x;
			}

			if(boxStartPos.y > boxEndPos.y)
			{
				double y = boxStartPos.y;
				boxStartPos.y = boxEndPos.y;
				boxEndPos.y = y;
			}

			BezierSpline[] splines = source.getActiveBezierSplines(BoardCAD.getInstance().getCurrentBrd());
			if(splines == null)
				return;


			for(int k = 0; k < splines.length; k++)
			{
				for(int i = 0; i < splines[k].getNrOfControlPoints(); i++)
				{
					BezierKnot point = splines[k].getControlPoint(i);
					for(int j = 0; j < 3; j++)
					{
						Point2D.Double p = point.getPoints()[j];
	
						if(p.x > boxStartPos.x && p.x < boxEndPos.x && p.y > boxStartPos.y && p.y < boxEndPos.y )	//Check within box
						{
							mWhich = j;
	
							if(event.isControlDown())
								source.toggleSelectedControlPoint(point);
							else
								source.addSelectedControlPoint(point);
	
							break;
						}
					}
				}
			}

			BoardCAD.getInstance().onControlPointChanged();
			source.repaint();
			mIsBoxSelecting = false;			
		}

		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(selectedControlPoints.size() == 0)
			return;

		if(mIsDragging == true)
		{
			execute();
		}
	}

	public void onMouseWheelMoved(BoardEdit source, MouseWheelEvent event)
	{
		int scroll = event.getWheelRotation();

		int steps = Math.abs(scroll);
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
		if(mIsDragging)
			return false;

		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(selectedControlPoints.size() == 0)
			return false;

		double mulX = ((source.mDrawControl&(BezierBoardDrawUtil.FlipX)) != 0)?-1.0f:1.0f;
		double mulY = ((source.mDrawControl&(BezierBoardDrawUtil.FlipY)) != 0)?-1.0f:1.0f;

		if(event.getID() == KeyEvent.KEY_PRESSED)
		{
			int key = event.getKeyCode();

			double x_diff = 0;
			double y_diff = 0;
			double movement = (KEY_MOVE_AMOUNT/source.getScale())*(event.isAltDown()?.1f:1f);

//			Length of current
			double sx;
			double sy;
			if(mControlPointsBeforeChange == null || mControlPointsBeforeChange.size() == 0)
			{
				sx = (double)selectedControlPoints.get(0).getPoints()[mWhich].x - selectedControlPoints.get(0).getPoints()[0].x;
				sy = (double)selectedControlPoints.get(0).getPoints()[mWhich].y - selectedControlPoints.get(0).getPoints()[0].y;
			}
			else
			{
				sx = (double)mControlPointsBeforeChange.get(0).getPoints()[mWhich].x - mControlPointsBeforeChange.get(0).getPoints()[0].x;
				sy = (double)mControlPointsBeforeChange.get(0).getPoints()[mWhich].y - mControlPointsBeforeChange.get(0).getPoints()[0].y;
			}

			double sl = Math.sqrt(sx*sx+sy*sy);

//			Normalize
			double snx = sx/sl;
			double sny = sy/sl;

			switch(key)
			{
			case KeyEvent.VK_LESS:
				if(selectedControlPoints.size() > 1 || mIsKeyEditing == true)
					return false;

				--mWhich;
				if(mWhich < 0)
					mWhich = 2;

				source.repaint();			
				break;

			case KeyEvent.VK_C:
				if(selectedControlPoints.size() > 1 || mIsKeyEditing == true)
					return false;

				BezierSpline[] splines = source.getActiveBezierSplines(BoardCAD.getInstance().getCurrentBrd());
				if(splines == null)
					return false;

				for(int i = 0; i < splines.length; i++)
				{
					int currentIndex = splines[i].indexOf(selectedControlPoints.get(0));
					if(currentIndex == -1)
						continue;
	
					int newIndex = ++currentIndex%splines[i].getNrOfControlPoints();
	
					selectedControlPoints.clear();
	
					selectedControlPoints.add(splines[i].getControlPoint(newIndex));

					source.repaint();			
				}
				break;

			case KeyEvent.VK_LEFT:
				x_diff = -movement*mulX*mRepeat;
				break;

			case KeyEvent.VK_RIGHT:
				x_diff = movement*mulX*mRepeat;
				break;

			case KeyEvent.VK_UP:
				y_diff = -movement*mulY*mRepeat;
				break;

			case KeyEvent.VK_DOWN:
				y_diff = movement*mulY*mRepeat;
				break;

			case KeyEvent.VK_E:
			case KeyEvent.VK_R:
				if(mWhich == 0 || selectedControlPoints.size() > 1)
					return false;

				x_diff = (double)(snx*movement*((key==KeyEvent.VK_R)?-1.0f:1.0f))*mRepeat;
				y_diff = (double)(sny*movement*((key==KeyEvent.VK_R)?-1.0f:1.0f))*mRepeat;
				break;

			case KeyEvent.VK_Q:
			case KeyEvent.VK_W:
				if(mWhich == 0 || selectedControlPoints.size() > 1)
					return false;

				double angle = (((double)Math.PI/180.0f)*mRepeat)*((key==KeyEvent.VK_Q)?-1.0f:1.0f)*(event.isAltDown()?.1f:1f);
				x_diff = (double)((Math.cos(angle)*sx - Math.sin(angle)*sy) - sx);
				y_diff = (double)((Math.sin(angle)*sx + Math.cos(angle)*sy) - sy);
				break;

			default:
				return false;
			}

			if(mIsKeyEditing == false)
			{
				saveControlPointsBeforeChange(source);

				mIsKeyEditing = true;
				mCurrentKeyCode = key;

				mRepeat = 1;

				mSource = source;
			}

			mRepeat++;

			moveControlPoints(x_diff, y_diff, mWhich);

			BoardCAD.getInstance().onControlPointChanged();
			source.repaint();
			return true;
		}
		else if(event.getID() == KeyEvent.KEY_RELEASED)
		{
			if(mIsKeyEditing == true)
			{
				if(mCurrentKeyCode == event.getKeyCode())
				{
					execute();
				}

			}

		}
		return false;
	}

	public void setContinous(BoardEdit source, boolean continous)
	{
		if(source.getSelectedControlPoints().size() == 0)
			return;
		
		mSource = source;

		saveControlPointsBeforeChange(mSource);

		mSource.getSelectedControlPoints().get(0).setContinous(continous);

		moveControlPoints(0, 0, (mWhich == 0)?1:mWhich);	//If endpoint selected, select tangent

		execute();

	}

	public void setControlPoint(BoardEdit source, Point2D.Double pos)
	{
		mSource = source;

		saveControlPointsBeforeChange(mSource);

		double dx = pos.x - mControlPointsBeforeChange.get(0).getPoints()[mWhich].x;
		double dy = pos.y - mControlPointsBeforeChange.get(0).getPoints()[mWhich].y;

		moveControlPoints(dx, dy, mWhich);

		execute();

	}

	public void rotateControlPoint(BoardEdit source, double targetAngle)
	{
		mSource = source;

		saveControlPointsBeforeChange(mSource);

		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(mWhich == 0 || selectedControlPoints.size() > 1)
			return;

		double sx = (double)selectedControlPoints.get(0).getPoints()[mWhich].x - selectedControlPoints.get(0).getPoints()[0].x;
		double sy = (double)selectedControlPoints.get(0).getPoints()[mWhich].y - selectedControlPoints.get(0).getPoints()[0].y;

		Point2D.Double horAxis = new Point2D.Double(1.0,0.0);
		Point2D.Double pointVec = new Point2D.Double(sx,sy);

		double pointAngle = VecMath.getVecAngle(horAxis, pointVec);

		double rotAngle = targetAngle - pointAngle;

		double x_diff = (double)((Math.cos(rotAngle)*sx - Math.sin(rotAngle)*sy) - sx);
		double y_diff = (double)((Math.sin(rotAngle)*sx + Math.cos(rotAngle)*sy) - sy);

		moveControlPoints(x_diff, y_diff, mWhich);

		BoardCAD.getInstance().onControlPointChanged();
		execute();
	}

	public void rotateControlPointToHorizontal(BoardEdit source)
	{
		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(selectedControlPoints.size() != 1)
			return;

		mSource = source;

		saveControlPointsBeforeChange(mSource);

		BezierKnot current = selectedControlPoints.get(0);

		double nextLength = current.getTangentToNextLength();
		double prevLength = current.getTangentToPrevLength();
		double x = current.getEndPoint().x;
		double y = current.getEndPoint().y;

		double tanPrevSign = (current.getTangentToPrev().x-x>0)?1:-1;
		double tanNextSign = (current.getTangentToNext().x-x>=0)?1:-1;

		if(mWhich == 0 || mWhich == 1)
		{
			current.setTangentToPrev((prevLength*tanPrevSign)+x, y);
			if(current.isContinous())
			{
				current.setTangentToNext((-nextLength*tanPrevSign)+x, y);
			}
		}
		else if(mWhich == 2 || current.isContinous())
		{
			current.setTangentToNext((nextLength*tanNextSign)+x, y);
			if(current.isContinous())
			{
				current.setTangentToPrev((-prevLength*tanNextSign)+x, y);
			}
		}

		BoardCAD.getInstance().onControlPointChanged();
		execute();
	}

	public void rotateControlPointToVertical(BoardEdit source)
	{
		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(selectedControlPoints.size() != 1)
			return;

		mSource = source;

		saveControlPointsBeforeChange(mSource);

		BezierKnot current =  selectedControlPoints.get(0);

		double nextLength = current.getTangentToNextLength();
		double prevLength = current.getTangentToPrevLength();
		double x = current.getEndPoint().x;
		double y = current.getEndPoint().y;

		double tanPrevSign = (current.getTangentToPrev().y-y>0)?1:-1;
		double tanNextSign = (current.getTangentToNext().y-y>=0)?1:-1;

		if(mWhich == 0 || mWhich == 1)
		{
			current.setTangentToPrev(x, (prevLength*tanPrevSign)+y);
			if(current.isContinous())
			{
				current.setTangentToNext(x, (-nextLength*tanPrevSign)+y);
			}
		}
		if(mWhich == 0 || mWhich == 2)
		{
			current.setTangentToNext(x, (nextLength*tanNextSign)+y);
			if(current.isContinous())
			{
				current.setTangentToPrev(x, (-prevLength*tanNextSign)+y);

			}
		}

		BoardCAD.getInstance().onControlPointChanged();
		execute();
	}

	public void redo()
	{
		/*		for(int i = 0; i < mSelectedControlPointsCopy.size(); i++)
		{
			mSelectedControlPointsCopy.get(i).set(mControlPointsAfterChange.get(i));
		}
		 */		super.redo();
	}

	public void undo()
	{
		/*		for(int i = 0; i < mSelectedControlPointsCopy.size(); i++)
		{
			mSelectedControlPointsCopy.get(i).set(mControlPointsBeforeChange.get(i));
		}
		 */		super.undo();
	}

	public Object clone(){
		BrdEditCommand cmd = null;

		cmd =  (BrdEditCommand)super.clone();

//		The arrays are instansiated in this class so there is no need to copy them

		return cmd;
	}

	public String getCommandString()
	{
		return LanguageResource.getString("EDITCMD_STR");
	}
	
	
	private double[][] invert(double[][] a)
	{
		/*	
		
		| a11 a12 a13 |-1                |   a33a22-a32a23  -(a33a12-a32a13)   a23a12-a22a13   |
		| a21 a22 a23 |    =  1/DET(A) * | -(a33a21-a31a23)   a33a11-a31a13  -(a23a11-a21a13) |
		| a31 a32 a33 |                  |   a32a21-a31a22  -(a32a11-a31a12)   a22a11-a21a12   |

		DET(A)  =  a11(a33a22-a32a23)-a21(a33a12-a32a13)+a31(a23a12-a22a13)	

		*/
	
		double det=a[0][0]*(a[2][2]*a[1][1]-a[2][1]*a[1][2])-a[1][0]*(a[2][2]*a[0][1]-a[2][1]*a[0][2])+a[2][0]*(a[1][2]*a[0][1]-a[1][1]*a[0][2]);

		double[][] b={ {(a[2][2]*a[1][1]-a[2][1]*a[1][2])/det, -(a[2][2]*a[0][1]-a[2][1]*a[0][2])/det, (a[1][2]*a[0][1]-a[1][1]*a[0][2])/det },
		               {-(a[2][2]*a[1][0]-a[2][0]*a[1][2])/det, (a[2][2]*a[0][0]-a[2][0]*a[0][2])/det, -(a[1][2]*a[0][0]-a[1][0]*a[0][2])/det },
		               {(a[2][1]*a[1][0]-a[2][0]*a[1][1])/det, -(a[2][1]*a[0][0]-a[2][0]*a[0][1])/det, (a[1][1]*a[0][0]-a[1][0]*a[0][1])/det }};
		               
		               
		return b;
		
				
	}
	
	private double[][] cross_product(double[][] m1, double[][] m2)
	{
		double[][] m=new double[3][3];
		
		m[0][0]=m1[0][0]*m2[0][0]+m1[1][0]*m2[0][1]+m1[2][0]*m2[0][2];
		m[1][0]=m1[0][0]*m2[1][0]+m1[1][0]*m2[1][1]+m1[2][0]*m2[1][2];
		m[2][0]=m1[0][0]*m2[2][0]+m1[1][0]*m2[2][1]+m1[2][0]*m2[2][2];

		m[0][1]=m1[0][1]*m2[0][0]+m1[1][1]*m2[0][1]+m1[2][1]*m2[0][2];
		m[1][1]=m1[0][1]*m2[1][0]+m1[1][1]*m2[1][1]+m1[2][1]*m2[1][2];
		m[2][1]=m1[0][1]*m2[2][0]+m1[1][1]*m2[2][1]+m1[2][1]*m2[2][2];

		m[0][2]=m1[0][2]*m2[0][0]+m1[1][2]*m2[0][1]+m1[2][2]*m2[0][2];
		m[1][2]=m1[0][2]*m2[1][0]+m1[1][2]*m2[1][1]+m1[2][2]*m2[1][2];
		m[2][2]=m1[0][2]*m2[2][0]+m1[1][2]*m2[2][1]+m1[2][2]*m2[2][2];
		
		return m;
	
	}

}

class BrdAddControlPointCommand extends BrdAbstractEditCommand
{
	static double K = 1.0f;

	private int mIndex = 0;
	private BezierKnot mNewControlPoint = null;
	private BezierSpline mSpline;


	BrdAddControlPointCommand()
	{
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		super.saveBeforeChange(source.getCurrentBrd());

		Point pos = event.getPoint();
		Point2D.Double brdPos = source.screenCoordinateToBrdCoordinate(pos);

		BezierSpline[] splines = source.getActiveBezierSplines(source.getCurrentBrd());
		mNewControlPoint = new BezierKnot();
		
		for(int i = 0; i < splines.length; i++)
		{
			mIndex = splines[i].getSplitControlPoint(brdPos, mNewControlPoint);

			if(mIndex < 0)
				continue;
			
			mSpline = splines[i];
	
			mSpline.insert(mIndex, mNewControlPoint);
	
			Point2D.Double tmp = new Point2D.Double();
	
			BezierKnot prev = mSpline.getControlPoint(mIndex-1);
			BezierKnot next = mSpline.getControlPoint(mIndex+1);
			
			BezierCurve tmpCurve = new BezierCurve(prev, next);
	
			double t = tmpCurve.getClosestT(brdPos);
	
			VecMath.subVector(prev.getPoints()[0],prev.getPoints()[2],tmp);
			VecMath.scaleVector(tmp, t);
			VecMath.addVector(prev.getPoints()[0],tmp,prev.getPoints()[2]);
	
			VecMath.subVector(next.getPoints()[1],next.getPoints()[0],tmp);
			VecMath.scaleVector(tmp, t-1);
			VecMath.addVector(next.getPoints()[0],tmp,next.getPoints()[1]);
	
			source.onBrdChanged();
	
			execute();
	
			source.repaint();
		}

	}

	public void redo()
	{
		mSpline.insert(mIndex, mNewControlPoint);

		super.redo();
	}
	
	public void undo()
	{
		mSpline.remove(mIndex);

		super.undo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADDCONTROLPOINTCMD_STR");
	}
}

class BrdDeleteControlPointCommand extends BrdAbstractEditCommand
{
	BezierKnot mDeletedControlPoint;
	BezierSpline mControlPoints;
	int mIndex;

	BrdDeleteControlPointCommand(BoardEdit source, BezierKnot ControlPoint, BezierSpline ControlPoints)
	{
		mSource = source;

		mDeletedControlPoint = ControlPoint;

		mControlPoints = ControlPoints;
	}

	public void doAction()
	{
		super.saveBeforeChange(mSource.getCurrentBrd());

		mSource.mSelectedControlPoints.remove(mDeletedControlPoint);

		super.removePoint(mDeletedControlPoint);

		mIndex = mControlPoints.indexOf(mDeletedControlPoint);
		BezierKnot prev =  mControlPoints.getControlPoint(mIndex-1);
		BezierKnot next =  mControlPoints.getControlPoint(mIndex+1);

		BezierCurve tmpCurve = new BezierCurve(prev, next);
		
		double t = tmpCurve.getClosestT(mDeletedControlPoint.getEndPoint());
		if(t != 0 && t != 1)	//To prevent divide by zero
		{
			Point2D.Double tmp = new Point2D.Double();

			VecMath.subVector(prev.getPoints()[0],prev.getPoints()[2],tmp);
			VecMath.scaleVector(tmp, 1/(t));
			VecMath.addVector(prev.getPoints()[0],tmp,prev.getPoints()[2]);

			VecMath.subVector(next.getPoints()[0],next.getPoints()[1],tmp);
			VecMath.scaleVector(tmp, 1/(1-t));
			VecMath.addVector(next.getPoints()[0],tmp,next.getPoints()[1]);
		}


		mControlPoints.remove(mDeletedControlPoint);		

		super.saveChanges();
	}

	public void execute()
	{		
		doAction();

		mSource.onBrdChanged();

		super.execute();
	}

	public void undo()
	{
		mControlPoints.insert(mIndex, mDeletedControlPoint);

		super.undo();
	}

	public void redo()
	{
		mControlPoints.remove(mDeletedControlPoint);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("DELETECONTROLPOINTCMD_STR");

	}
}


class BrdAddCrossSectionCommand extends BrdCommand
{
	double mPos;
	BezierBoardCrossSection mNewCrossSection = null;


	BrdAddCrossSectionCommand(BoardEdit source, double pos)
	{
		mSource = source;

		mPos = pos;
	}

	public void execute()
	{
		BezierBoard brd = mSource.getCurrentBrd();

//		mNewCrossSection = (BezierBoardCrossSection)brd.getNearestCrossSection(mPos).clone();
		mNewCrossSection = (BezierBoardCrossSection)brd.getInterpolatedCrossSection(mPos).clone();

		mNewCrossSection.setPosition(mPos);

		mNewCrossSection.scale(brd.getThicknessAtPos(mPos), brd.getWidthAtPos(mPos));

		brd.addCrossSection(mNewCrossSection);

		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.removeCrossSection(mNewCrossSection);

		super.undo();

	}

	public void redo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.addCrossSection(mNewCrossSection);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADDCROSSECTIONCMD_STR");
	}
}

class BrdRemoveCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mRemovedCrossSection = null;


	BrdRemoveCrossSectionCommand(BoardEdit source, BezierBoardCrossSection crossSection)
	{
		mSource = source;

		mRemovedCrossSection = crossSection;
	}

	public void execute()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.removeCrossSection(mRemovedCrossSection);

		super.execute();
	}

	public void undo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.addCrossSection(mRemovedCrossSection);

		super.undo();

	}

	public void redo()
	{
		BezierBoard brd = mSource.getCurrentBrd();

		brd.removeCrossSection(mRemovedCrossSection);

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("REMOVECROSSECTIONCMD_STR");
	}
}

class BrdMoveCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mMovedCrossSection = null;
	double mOldPos;
	double mNewPos;


	BrdMoveCrossSectionCommand(BoardEdit source, BezierBoardCrossSection crossSection, double newPos)
	{
		mSource = source;
		mMovedCrossSection = crossSection;
		mNewPos = newPos;
	}

	public void execute()
	{
		BezierBoard brd = mSource.getCurrentBrd();
		mMovedCrossSection.setPosition(mNewPos);
		brd.sortCrossSections();

		super.execute();

	}

	public void undo()
	{
		BezierBoard brd = mSource.getCurrentBrd();
		mMovedCrossSection.setPosition(mOldPos);
		brd.sortCrossSections();

		super.undo();

	}

	public void redo()
	{
		BezierBoard brd = mSource.getCurrentBrd();
		mMovedCrossSection.setPosition(mNewPos);
		brd.sortCrossSections();

		super.redo();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("MOVECROSSECTIONCMD_STR");
	}
}

class BrdPasteCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mCrossSection = null;
	BezierSpline mOldCrossSectionBezier = null;
	BezierSpline mNewCrossSectionBezier = null;


	BrdPasteCrossSectionCommand(BoardEdit source, BezierBoardCrossSection currentCrossSection, BezierBoardCrossSection copyCrossSection)
	{
		mSource = source;
		mCrossSection = currentCrossSection;
		mOldCrossSectionBezier = currentCrossSection.getBezierSpline();
		mNewCrossSectionBezier = (BezierSpline)copyCrossSection.getBezierSpline().clone();
	}

	public void execute()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		double pos = mCrossSection.getPosition();

		mCrossSection.setBezierSpline(mNewCrossSectionBezier);

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
		return LanguageResource.getString("PASTECROSSECTIONCMD_STR");
	}
}

class BrdImportCrossSectionCommand extends BrdCommand
{
	BezierBoardCrossSection mCrossSection = null;
	BezierSpline mOldCrossSectionBezier = null;
	BezierSpline mNewCrossSectionBezier = null;


	BrdImportCrossSectionCommand(BoardEdit source)
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

class BrdImportOutlineCommand extends BrdCommand
{
	BezierSpline mOldOutline = null;
	BezierSpline mNewOutline = null;


	BrdImportOutlineCommand(BoardEdit source)
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

class BrdImportProfileCommand extends BrdCommand
{
	BezierSpline mOldDeck = null;
	BezierSpline mOldBottom = null;
	BezierSpline mNewDeck = null;
	BezierSpline mNewBottom = null;

	BrdImportProfileCommand(BoardEdit source)
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

class BrdScaleCommand extends BrdCommand
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

	BrdScaleCommand(BoardEdit source)
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

class BrdSpotCheckCommand extends BrdCommand
{
	double mOldOffsetX;
	double mOldOffsetY;
	double mOldScale;


	BrdSpotCheckCommand()
	{
	}

	public void spotCheck()
	{
		mSource = BoardCAD.getInstance().getSelectedEdit();

		mOldOffsetX = mSource.getOffsetX();
		mOldOffsetY = mSource.getOffsetY();
		mOldScale = mSource.getScale();

		mSource.fitBrd();
		mSource.repaint();
	}

	public void restore()
	{
		mSource.setOffsetX(mOldOffsetX);
		mSource.setOffsetY(mOldOffsetY);
		mSource.setScale(mOldScale);
		mSource.repaint();
	}

	public String getCommandString()
	{
		return LanguageResource.getString("SPOTCHECKCMD_STR");
	}

}

class SetImageTailCommand extends BrdInputCommand
{

	SetImageTailCommand()
	{
		mCanUndo = false;
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();

		source.adjustBackgroundImageTail(pos);

	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADJUSTIMAGETOTAILCMD_STR");
	}

}


class SetImageNoseCommand extends BrdInputCommand
{

	SetImageNoseCommand()
	{
		mCanUndo = false;
	}

	public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
	{
		Point pos = event.getPoint();

		source.adjustBackgroundImageNose(pos);

	}

	public String getCommandString()
	{
		return LanguageResource.getString("ADJUSTIMAGETONOSECMD_STR");
	}
}


class GhostCommand extends BrdInputCommand
{

	GhostCommand()
	{
		mCanUndo = false;
	}

	public String getCommandString()
	{
		return LanguageResource.getString("GHOSTCMD_STR");
	}
}

