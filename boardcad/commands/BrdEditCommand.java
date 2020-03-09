package boardcad.commands;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import boardcad.gui.jdk.BezierBoardDrawUtil;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.BoardEdit;
import boardcad.gui.jdk.BoardHandler;
import boardcad.i18n.LanguageResource;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.MathUtils;
import cadcore.NurbsPoint;
import cadcore.VecMath;

public class BrdEditCommand extends BrdAbstractEditCommand
{
	static double KEY_MOVE_AMOUNT = 1.0f;

//	ArrayList<ControlPoint> mSelectedControlPointsCopy;
	ArrayList<BezierKnot> mControlPointsBeforeChange;
//	ArrayList<ControlPoint> mControlPointsAfterChange;
	Point2D.Double mDragStartPos;
	Point2D.Double mDragOffset;
	Point mBoxSelectStartPos;
	private int mWhich = 0;
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
	protected static boolean is_marked=false;

	private NurbsPoint p;
	
	
	public BrdEditCommand()
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
			selectedControlPoints.get(0).setLocation(getWhich(), mControlPointsBeforeChange.get(0).getPoints()[getWhich()].x+x_diff, mControlPointsBeforeChange.get(0).getPoints()[getWhich()].y+y_diff);

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
				setWhich(splines[0].getBestMatchWhich(brdPos));
				hitControlPoint = (double)brdPos.distance(bestMatch.getPoints()[getWhich()]) < (MAX_OFF/source.getScale());
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

			mDragOffset = new Point2D.Double(bestMatch.getPoints()[getWhich()].x-mDragStartPos.x,bestMatch.getPoints()[getWhich()].y-mDragStartPos.y);

		}

		BoardCAD.getInstance().onControlPointChanged();
//		source.repaint();
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

				double[][] m=MathUtils.invert(source.mRotationMatrix);
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

		moveControlPoints(x_diff, y_diff, getWhich());


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
							setWhich(j);
	
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
		{
			return mPanCommand.onKeyEvent(source, event);
		}

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
				sx = (double)selectedControlPoints.get(0).getPoints()[getWhich()].x - selectedControlPoints.get(0).getPoints()[0].x;
				sy = (double)selectedControlPoints.get(0).getPoints()[getWhich()].y - selectedControlPoints.get(0).getPoints()[0].y;
			}
			else
			{
				sx = (double)mControlPointsBeforeChange.get(0).getPoints()[getWhich()].x - mControlPointsBeforeChange.get(0).getPoints()[0].x;
				sy = (double)mControlPointsBeforeChange.get(0).getPoints()[getWhich()].y - mControlPointsBeforeChange.get(0).getPoints()[0].y;
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

				setWhich(getWhich() - 1);
				if(getWhich() < 0)
					setWhich(2);

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
				if(getWhich() == 0 || selectedControlPoints.size() > 1)
					return false;

				x_diff = (double)(snx*movement*((key==KeyEvent.VK_R)?-1.0f:1.0f))*mRepeat;
				y_diff = (double)(sny*movement*((key==KeyEvent.VK_R)?-1.0f:1.0f))*mRepeat;
				break;

			case KeyEvent.VK_Q:
			case KeyEvent.VK_W:
				if(getWhich() == 0 || selectedControlPoints.size() > 1)
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

			moveControlPoints(x_diff, y_diff, getWhich());

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

		moveControlPoints(0, 0, (getWhich() == 0)?1:getWhich());	//If endpoint selected, select tangent

		execute();

	}

	public void setControlPoint(BoardEdit source, Point2D.Double pos)
	{
		mSource = source;

		saveControlPointsBeforeChange(mSource);

		double dx = pos.x - mControlPointsBeforeChange.get(0).getPoints()[getWhich()].x;
		double dy = pos.y - mControlPointsBeforeChange.get(0).getPoints()[getWhich()].y;

		moveControlPoints(dx, dy, getWhich());

		execute();

	}

	public void rotateControlPoint(BoardEdit source, double targetAngle)
	{
		mSource = source;

		saveControlPointsBeforeChange(mSource);

		ArrayList<BezierKnot> selectedControlPoints = source.getSelectedControlPoints();
		if(getWhich() == 0 || selectedControlPoints.size() > 1)
			return;

		double sx = (double)selectedControlPoints.get(0).getPoints()[getWhich()].x - selectedControlPoints.get(0).getPoints()[0].x;
		double sy = (double)selectedControlPoints.get(0).getPoints()[getWhich()].y - selectedControlPoints.get(0).getPoints()[0].y;

		Point2D.Double horAxis = new Point2D.Double(1.0,0.0);
		Point2D.Double pointVec = new Point2D.Double(sx,sy);

		double pointAngle = VecMath.getVecAngle(horAxis, pointVec);

		double rotAngle = targetAngle - pointAngle;

		double x_diff = (double)((Math.cos(rotAngle)*sx - Math.sin(rotAngle)*sy) - sx);
		double y_diff = (double)((Math.sin(rotAngle)*sx + Math.cos(rotAngle)*sy) - sy);

		moveControlPoints(x_diff, y_diff, getWhich());

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

		if(getWhich() == 0 || getWhich() == 1)
		{
			current.setTangentToPrev((prevLength*tanPrevSign)+x, y);
			if(current.isContinous())
			{
				current.setTangentToNext((-nextLength*tanPrevSign)+x, y);
			}
		}
		else if(getWhich() == 2 || current.isContinous())
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

		if(getWhich() == 0 || getWhich() == 1)
		{
			current.setTangentToPrev(x, (prevLength*tanPrevSign)+y);
			if(current.isContinous())
			{
				current.setTangentToNext(x, (-nextLength*tanPrevSign)+y);
			}
		}
		if(getWhich() == 0 || getWhich() == 2)
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

//		The arrays are instantiated in this class so there is no need to copy them

		return cmd;
	}

	public String getCommandString()
	{
		return LanguageResource.getString("EDITCMD_STR");
	}	

	public int getWhich() {
		return mWhich;
	}

	public void setWhich(int mWhich) {
		this.mWhich = mWhich;
	}

}