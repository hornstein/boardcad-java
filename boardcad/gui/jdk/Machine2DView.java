package boardcad.gui.jdk;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cadcore.UnitUtils;

import board.BezierBoard;
import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;
import boardcam.holdingsystems.AbstractBlankHoldingSystem;

public class Machine2DView extends  BoardEdit{

	static final double fixedHeightBorder = 0;
	boolean mShowDeck = true;
	double BORDER = 10;
	BrdCommand mBrdPosCommand;
	
	MachineConfig mConfig;
	
	Point3d mCutterOffset = new Point3d(0.0,0.0,0.0);
	

	Machine2DView(MachineConfig config)
	{
		super();
		
		mConfig = config;
		System.out.printf("Machine2DView constructor mConfig:%s this:%s\n", mConfig.toString(), this.toString());

		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JPopupMenu menu = new JPopupMenu();

		final AbstractAction toggleDeckBottom = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TOGGLEDECKBOTTOM_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				mShowDeck = !mShowDeck;
				repaint();
			}
		};
		menu.add(toggleDeckBottom);
		add(menu);
		
		setPreferredSize(new Dimension(400,100));

		setLayout(new BorderLayout());
		
		mBrdPosCommand = new BrdPositionCommand();
		

	};

	public void fit_all()
	{
		Dimension dim = getSize();
		double len = 0.0;
		BezierBoard brd = mConfig.getBoard();
		if(brd != null)
		{	
			len = brd.getLength();
		}
		if(mConfig.getBlankHoldingSystem() != null)
		{
			len += mConfig.getBlankHoldingSystem().getBlankDeckOffsetPos().x;
		}

		mScale = (dim.width-((BORDER*dim.width/100)*2))/len;

		mOffsetX = (BORDER*dim.width/100);

		mOffsetY = dim.height*1/2;

		mLastWidth = dim.width;
		mLastHeight = dim.height;
		
		calcViewOffset();

		repaint();
	}

	public void adjustScaleAndOffset()
	{
		double currentWidth = getWidth();
		double currentHeight = getHeight();

		double widthChange = (currentWidth/mLastWidth);
		double heightChange = (currentHeight/mLastHeight);

		mScale *= widthChange;

		mOffsetX *= widthChange;
		mOffsetY *= widthChange;

		mLastWidth = currentWidth;
		mLastHeight = currentHeight;

	}

	public void setCutterOffset(Point3d pos){
		mCutterOffset = pos;
		repaint();
	}
	
	public void paintComponent(Graphics g) {

		adjustScaleAndOffset();


		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();



//		Turn on antialiasing, so painting is smooth.
		g2d.setRenderingHint(

				RenderingHints.KEY_ANTIALIASING,

				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
//		Paint the background.
		Color bkgColor = Color.LIGHT_GRAY;

		g2d.setColor(bkgColor);
		
		g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

		//Draw the blank holding system
		AbstractBlankHoldingSystem holdingSystem = mConfig.getBlankHoldingSystem();
		if(holdingSystem == null)
		{
			return;
		}

		holdingSystem.draw(g2d, mOffsetX, mOffsetY, mScale, mShowDeck);
		
		//Draw the blank and the board
		Vector3d boardOffset = mShowDeck?holdingSystem.getBoardDeckOffsetPos():holdingSystem.getBoardBottomOffsetPos();
		double boardAngle = mShowDeck?holdingSystem.getBoardDeckOffsetAngle():holdingSystem.getBoardBottomOffsetAngle();
				
		Vector3d blankOffset = mShowDeck?holdingSystem.getBlankDeckOffsetPos():holdingSystem.getBlankBottomOffsetPos();
		double blankAngle = mShowDeck?holdingSystem.getBlankDeckOffsetAngle():holdingSystem.getBlankBottomOffsetAngle();
	
		JavaDraw jd = new JavaDraw(g2d);
		
	//	System.out.printf("OffsetX: %f, OffsetY: %f, boardOffset: %f,%f boardAngle: %f, blankOffset: %f,%f blankAngle: %f\n", mOffsetX, mOffsetY, boardOffset.x, boardOffset.z, boardAngle, blankOffset.x, blankOffset.z, blankAngle);
	
		Stroke stroke = new BasicStroke((float)(1.0/mScale));
		g2d.setStroke(stroke);
		
		BezierBoard brd = mConfig.getBoard();
		if(brd != null)
		{
			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX+(boardOffset.x*mScale), mOffsetY-(boardOffset.z*mScale), boardAngle, mScale, BoardCAD.getInstance().getBrdColor(), stroke, brd.getDeck(), (mShowDeck?BezierBoardDrawUtil.FlipY:0), false);
			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX+(boardOffset.x*mScale), mOffsetY-(boardOffset.z*mScale), boardAngle, mScale, BoardCAD.getInstance().getBrdColor(), stroke, brd.getBottom(), mShowDeck?BezierBoardDrawUtil.FlipY:0, false);
		}

		//Draw blank
		BezierBoard blank = mConfig.getBlank();
		if(blank != null && !blank.isEmpty())
		{
			Color blankColor = BoardCAD.getInstance().getBlankColor();

			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX+(blankOffset.x*mScale), mOffsetY-(blankOffset.z*mScale), blankAngle, mScale, blankColor, stroke, blank.getDeck(), mShowDeck?BezierBoardDrawUtil.FlipY:0, false);
			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX+(blankOffset.x*mScale), mOffsetY-(blankOffset.z*mScale), blankAngle, mScale, blankColor, stroke, blank.getBottom(), mShowDeck?BezierBoardDrawUtil.FlipY:0, false);
		}
		
		//Setup for drawing
		AffineTransform savedTransform = g2d.getTransform();
		
		AffineTransform at = new AffineTransform();
		
		at.setToTranslation(mOffsetX, mOffsetY);
	
		g2d.transform(at);
	
		at.setToScale(mScale, mScale);
	
		g2d.transform(at);
		
		//Draw cutter offset
		g2d.setColor(Color.BLUE);
		
		final double halfCrossWidth = 5.0;
		Line2D line = new Line2D.Double();
		line.setLine(-halfCrossWidth + mCutterOffset.x, -mCutterOffset.z, halfCrossWidth + mCutterOffset.x, -mCutterOffset.z);
		g2d.draw(line);
		line.setLine(mCutterOffset.x, -halfCrossWidth - mCutterOffset.z, mCutterOffset.x, halfCrossWidth - mCutterOffset.z);
		g2d.draw(line);

		g2d.setTransform(savedTransform);	
	}

	public void drawSlidingInfo(Graphics2D g, Color color, Stroke stroke, BezierBoard brd) 
	{ 
//		drawProfileSlidingInfo(this, g, color, stroke, brd);
	}
	
	public BrdCommand getCurrentCommand()
	{
		return mBrdPosCommand;
	}

	void calcViewOffset()
	{
		//mOffsetY = getCurrentMachineConfig().getDouble(MachineConfig.SUPPORT_1_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
	}

	class BrdPositionCommand extends BrdInputCommand
	{
		double mOriginalOffsetX;
		double mOriginalOffsetY;
		Point mPressedPos;
		boolean mButtonPressed = true;
		
		BrdPanCommand mPanCommand = new BrdPanCommand();
		BrdZoomCommand mZoomCommand = new BrdZoomCommand();
		boolean mIsPaning = false;
		
		BrdPositionCommand()
		{
			mCanUndo = false;
		}

		public void onLeftMouseButtonPressed(BoardEdit source, MouseEvent event)
		{
			if(!mShowDeck)
				return;
			
			Point pos = event.getPoint();
			Vector3d deckOffset = mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
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
			
			if(!mShowDeck)
				return;
						
			if(!mButtonPressed)
				return;
			
			Point pos = event.getPoint();
			
			double offsetX = mOriginalOffsetX + (((pos.x - mPressedPos.x))*(event.isAltDown()?0.1:1.0)/mScale);
			double offsetY = mOriginalOffsetY - (((pos.y - mPressedPos.y))*(event.isAltDown()?0.1:1.0)/mScale);
//			System.out.printf("offsetX: %f, offsetY: %f\n",offsetX, offsetY);
			mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(offsetX, 0.0, offsetY));

			//System.out.printf("onMouseDragged mConfig:%s this:%s\n", mConfig.toString(), this.toString());
			
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
			
//			System.out.printf("onKeyEvent mConfig:%s this:%s\n", mConfig.toString(), this.toString());

			if(key == KeyEvent.VK_LEFT)
			{
				if(!mShowDeck)
					return false;

				Vector3d deckOffset = mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
				double offsetX = (deckOffset.x-((event.isAltDown()?0.1:1.0f)/source.getScale()));
				mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(offsetX, 0.0, deckOffset.z));
				source.repaint();
				return true;
			}
			else if(key == KeyEvent.VK_RIGHT)
			{
				if(!mShowDeck)
					return false;

				Vector3d deckOffset = mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
				double offsetX = (deckOffset.x+((event.isAltDown()?0.1:1.0f)/source.getScale()));
				mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(offsetX, 0.0, deckOffset.z));
				source.repaint();
				return true;
			}
			else if(key == KeyEvent.VK_UP)
			{
				if(!mShowDeck)
					return false;

				Vector3d deckOffset = mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
				double offsetY = (deckOffset.z+((event.isAltDown()?0.1:1.0f)/source.getScale()));
				mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(deckOffset.x, 0.0, offsetY));
				source.repaint();
				return true;
			}
			else if(key == KeyEvent.VK_DOWN)
			{
				if(!mShowDeck)
					return false;

				Vector3d deckOffset = mConfig.getBlankHoldingSystem().getBoardDeckOffsetPos();
				double offsetY = (deckOffset.z-((event.isAltDown()?0.1:1.0f)/source.getScale()));
				mConfig.getBlankHoldingSystem().setBoardDeckOffsetPos(new Vector3d(deckOffset.x, 0.0, offsetY));
				source.repaint();
				return true;
			}
			if(key == KeyEvent.VK_Q)
			{
				if(!mShowDeck)
					return false;

				double rot = mConfig.getBlankHoldingSystem().getBoardDeckOffsetAngle();
				rot = rot-(event.isAltDown()?0.0004:0.004f);
				mConfig.getBlankHoldingSystem().setBoardDeckOffsetAngle(rot);
				source.repaint();
				return true;
			}
			else if(key == KeyEvent.VK_W)
			{
				if(!mShowDeck)
					return false;

				double rot = mConfig.getBlankHoldingSystem().getBoardDeckOffsetAngle();
				rot = rot+(event.isAltDown()?0.0004:0.004f);
				mConfig.getBlankHoldingSystem().setBoardDeckOffsetAngle(rot);
				source.repaint();
				return true;
			}
			else if(key == KeyEvent.VK_SPACE)
			{
				mShowDeck = !mShowDeck;
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
}