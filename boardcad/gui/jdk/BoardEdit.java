package boardcad.gui.jdk;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputListener;

import board.BezierBoard;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.MathUtils;
import cadcore.UnitUtils;
import cadcore.VecMath;
import boardcad.commands.BrdCommand;
import boardcad.commands.BrdInputCommand;
import boardcad.i18n.LanguageResource;

public class BoardEdit extends JComponent implements AbstractEditor, MouseInputListener, MouseWheelListener  {
	static final long serialVersionUID=1L;
	public double mOffsetX;
	public double mOffsetY;
	public double mScale = 1.0f;
	public ArrayList<BezierKnot> mSelectedControlPoints = new ArrayList<BezierKnot>();
	public double mGhostOffsetX = 0;
	public double mGhostOffsetY = 0;
	public double mGhostRot = 0;
	public double mOriginalOffsetX = 0;
	public double mOriginalOffsetY = 0;

	public int mDrawControl = 0;
	public double theta=30;
	public double zeta=45;	

	public double[][] mRotationMatrix;

	double BORDER = 2;	//% of width

	double mLastWidth = 800;
	double mLastHeight = 600;

	Rectangle mZoomRectangle = new Rectangle(); 
	boolean mDrawZoomRectangle;

	boolean mHasMouse = false;

	Point mScreenCoord = new Point();  //  @jve:decl-index=0:

	Font mBrdCoordFont = new Font("Dialog", Font.PLAIN, 12);
	Point2D.Double mBrdCoord = new Point2D.Double();  //  @jve:decl-index=0:
	String mBrdCoordString = new String();  //  @jve:decl-index=0:

	Font mSlidingInfoFont = new Font("Dialog", Font.PLAIN, 12);
	String mSlidingInfoString = new String();  //  @jve:decl-index=0:
	Line2D.Double mSlidingInfoLine = new Line2D.Double();  //  @jve:decl-index=0:

	double mCurvatureScale = 500;
	double mVolumeDistributionScale = 0.1;
	
	Image mBackgroundImage = null;
//	AffineTransform mBackgroundImageTransform = new AffineTransform();
	double mBackgroundImageOffsetX = 0;
	double mBackgroundImageOffsetY = 0;
	double mBackgroundImageScale = 1;
	double mBackgroundImageRot = 0;

	BrdEditParentContainer mParentContainer = null;
	
	JPopupMenu mPopupMenu = null;
	
	boolean mIsLifeSize = false;
	static double mLifeSizeScale = 1.0;
	protected double mLifeSizeOffsetX=0.0;
	protected double mLifeSizeOffsetY=0.0;
	double mPreviousScale = 1.0;
	protected double mPreviousOffsetX=0.0;
	protected double mPreviousOffsetY=0.0;
	
	public boolean mIsCrossSectionEdit=false;
	
	public BoardEdit()
	{
		super();

		double[][] m1 = {{Math.cos(-theta*Math.PI/180.0), 0.0, -Math.sin(-theta*Math.PI/180.0)},
				{0.0, 1.0, 0.0},
		                {Math.sin(-theta*Math.PI/180.0), 0.0, Math.cos(-theta*Math.PI/180.0)}}; 
		double[][] m2 = {{1.0, 0.0, 0.0},
				{0.0, Math.cos(-zeta*Math.PI/180.0), -Math.sin(-zeta*Math.PI/180.0)},
		                {0.0, Math.sin(-zeta*Math.PI/180.0), Math.cos(-zeta*Math.PI/180.0)}}; 

		mRotationMatrix=MathUtils.cross_product(m1,m2);

//		Hint at good sizes for this component.

		setPreferredSize(new Dimension(800, 600));

		setMinimumSize(new Dimension(400, 300));

//		Request a black line around this component.

		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setFocusable(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

	}
	public void add(JPopupMenu menu)
	{
		mPopupMenu = menu;
	}
	
	public void setParentContainer(BrdEditParentContainer parent)
	{
		mParentContainer = parent;
	}

	public double getOffsetX()
	{
		return mOffsetX;
	}

	public void setOffsetX(double newOffsetX)
	{
		mOffsetX = newOffsetX;
	}

	public double getOffsetY()
	{
		return mOffsetY;
	}

	public void setOffsetY(double newOffsetY)
	{
		mOffsetY = newOffsetY;
	}

	public double getScale()
	{
		return mScale;
	}

	public void setScale(double scale)
	{
		mScale = scale;
	}

	public void setCurrentAsLifeSizeScale()
	{
		mLifeSizeScale = mScale;
		mLifeSizeOffsetX = mOffsetX;
		mLifeSizeOffsetY = mOffsetY;
	}
	
	public boolean isLifeSize()
	{
		return mIsLifeSize;
	}

	public void setLifeSize(boolean lifeSize)
	{
		if(mIsLifeSize == lifeSize)
			return;
		
		mIsLifeSize = lifeSize;

		if(mIsLifeSize)
		{
			mPreviousScale = mScale;
			mPreviousOffsetX = mOffsetX;
			mPreviousOffsetY = mOffsetY;

			mScale = mLifeSizeScale;
			mOffsetX = mLifeSizeOffsetX;
			mOffsetY = mLifeSizeOffsetY;
		}
		else
		{
			mLifeSizeOffsetX = mOffsetX;
			mLifeSizeOffsetY = mOffsetY;			
		}
	}
	
	public void resetToPreviousScale()
	{
		mScale = mPreviousScale;
		mOffsetX = mPreviousOffsetX;
		mOffsetY = mPreviousOffsetY;
	}

	public void setDrawZoomRectangle(Point corner1, Point corner2)
	{
		mZoomRectangle.setFrameFromDiagonal(corner1, corner2);
		mDrawZoomRectangle = true;
	}

	public void disableDrawZoomRectangle()
	{
		mDrawZoomRectangle = false;

	}
	
	public void setFlipped(boolean flipped)
	{
		if(flipped && !isFlipped())
		{
			mDrawControl |= BezierBoardDrawUtil.FlipX;
		}
		else if(!flipped && isFlipped())
		{
			mDrawControl &= ~BezierBoardDrawUtil.FlipX;
		}
	}
	
	public boolean isFlipped()
	{
		return (mDrawControl&BezierBoardDrawUtil.FlipX) != 0;
	
	}
	protected boolean isPaintingVolumeDistribution()
	{
		return BoardCAD.getInstance().isPaintingVolumeDistribution();
	}

	public BezierBoard getCurrentBrd()
	{
		return BoardCAD.getInstance().getCurrentBrd();
	}

	protected BezierBoard getOriginalBrd()
	{
		return BoardCAD.getInstance().getOriginalBrd();
	}

	protected BezierBoard getGhostBrd()
	{
		return BoardCAD.getInstance().getGhostBrd();
	}
	
	protected BrdCommand getCurrentCommand()
	{
		return BoardCAD.getInstance().getCurrentCommand();
	}
	
	protected BoardHandler getBoardHandler()
	{
		return BoardCAD.getInstance().getBoardHandler();
	}

	public void fitBrd()
	{
		Dimension dim = getSize();
		
		double maxlength = 200.0;

		BoardHandler bh=getBoardHandler();
		
		if(bh != null)
		{
			maxlength=bh.get_board_length()/10;
		}

		if(getCurrentBrd() != null && getCurrentBrd().getLength()>maxlength)
		{
			maxlength=getCurrentBrd().getLength();
		}
		
		mScale = (dim.width-((BORDER*dim.width/100)*2))/maxlength;

		boolean noseToTheLeft = (mDrawControl&BezierBoardDrawUtil.FlipX)!=0;

		mOffsetX = noseToTheLeft?getCurrentBrd().getLength()*mScale + (BORDER*dim.width/100):0 + (BORDER*dim.width/100);

		mOffsetY = dim.height*1/2;

		mLastWidth = dim.width;
		mLastHeight = dim.height;

	}

	public void adjustScaleAndOffset()
	{
		double currentWidth = getWidth();
		double currentHeight = getHeight();

		double widthChange = (currentWidth/mLastWidth);
		double heightChange = (currentHeight/mLastHeight);

		if(!mIsLifeSize)
		{
			mScale *= widthChange;
			mLifeSizeOffsetX *= widthChange;
			mLifeSizeOffsetY *= heightChange;
		}
		else
		{
			mPreviousScale *= widthChange;
			mPreviousOffsetX *= widthChange;
			mPreviousOffsetY *= heightChange;
		}

		mOffsetX *= widthChange;
		mOffsetY *= heightChange;

		mLastWidth = currentWidth;
		mLastHeight = currentHeight;
	}

	public BezierSpline[] getActiveBezierSplines(BezierBoard brd)
	{
		return null;
	}

	public ArrayList<Point2D.Double> getGuidePoints()
	{
		return null;
	}

	public ArrayList<BezierKnot> getSelectedControlPoints()
	{
		return mSelectedControlPoints;
	}

	public void clearSelectedControlPoints()
	{
		mSelectedControlPoints.clear();
	}

	public void addSelectedControlPoint(BezierKnot selected)
	{
		if(!mSelectedControlPoints.contains(selected))
			mSelectedControlPoints.add(selected);
	}

	public void toggleSelectedControlPoint(BezierKnot selected)
	{
		if(!mSelectedControlPoints.contains(selected))
			mSelectedControlPoints.add(selected);
		else
			mSelectedControlPoints.remove(selected);
	}

	public Point2D.Double screenCoordinateToBrdCoordinate(Point scrPoint)
	{
		Point2D.Double brdPoint = new Point2D.Double();

		brdPoint.x = (scrPoint.x - mOffsetX)/ mScale*((mDrawControl&BezierBoardDrawUtil.FlipX)!=0?-1.0f:1.0f);
		brdPoint.y = (scrPoint.y - mOffsetY)/ mScale*((mDrawControl&BezierBoardDrawUtil.FlipY)!=0?-1.0f:1.0f);

		return brdPoint;
	}
	
	public Point brdCoordinateToScreenCoordinateTo(Point2D.Double brdPoint)
	{
		Point scrPoint = new Point();

		scrPoint.x = (int)(mOffsetX + (brdPoint.x * mScale * ((mDrawControl&BezierBoardDrawUtil.FlipX)!=0?-1.0f:1.0f)));
		scrPoint.y = (int)(mOffsetY + (brdPoint.y * mScale * ((mDrawControl&BezierBoardDrawUtil.FlipY)!=0?-1.0f:1.0f)));

		return scrPoint;
	}

	public void loadBackgroundImage(String filename)
	{
		mBackgroundImage = Toolkit.getDefaultToolkit().getImage(filename);
		

		int height = mBackgroundImage.getHeight(this);
		while(height == -1)
		{
			try{
				wait(20);
			}
			catch(Exception e){
				//System.out.println("BoardEdit.loadBackgroundImage() Exception: " + e.toString());				
			};
			height = mBackgroundImage.getHeight(this);
		}
		
		int width = mBackgroundImage.getWidth(this);
		while(width == -1)
		{
			try{
				wait(20);
			}
			catch(Exception e){
				//System.out.println("BoardEdit.loadBackgroundImage() Exception: " + e.toString());								
			};
			width = mBackgroundImage.getWidth(this);
		}
		
		Point2D.Double tail = getTail();
		Point2D.Double nose = getNose();
		
		Point2D.Double brdVec = new Point2D.Double();
		VecMath.subVector(tail, nose, brdVec);

		double brdVecLen = VecMath.getVecLength(brdVec);
		
		mBackgroundImageScale = 1;
		mBackgroundImageOffsetX = 0;
		mBackgroundImageOffsetY = 0;
		mBackgroundImageRot = 0;
		
		if(width > height)
		{
			mBackgroundImageScale = (brdVecLen/width);
			mBackgroundImageOffsetY = -(height/2)*mBackgroundImageScale;
		}
		else
		{
			mBackgroundImageRot = -Math.PI/2;
			mBackgroundImageScale = (brdVecLen/height);
			mBackgroundImageOffsetY = (width/2)*mBackgroundImageScale;
		}	
		repaint();
	}
	
	public void adjustBackgroundImageTail(Point clickedScreenCoord)
	{
//		double mulX = ((mDrawControl&Brd.FlipX)!=0?-1.0f:1.0f);
		double mulY = ((mDrawControl&BezierBoardDrawUtil.FlipY)!=0?-1.0f:1.0f);
		
		Point2D.Double pos = screenCoordinateToBrdCoordinate(clickedScreenCoord);
		Point2D.Double tail = getTail();
		Point2D.Double nose = getNose();

		Point2D.Double brdVec = new Point2D.Double();
		VecMath.subVector(tail, nose, brdVec);
		
		Point2D.Double imgVec = new Point2D.Double();
		VecMath.subVector(pos, nose, imgVec);

		double brdVecLen = VecMath.getVecLength(brdVec);
		double imgVecLen = VecMath.getVecLength(imgVec);
		double rot = VecMath.getVecAngle(brdVec, imgVec)*(((brdVec.y > imgVec.y)?1:-1))*mulY;
		double newScale = (brdVecLen/imgVecLen);
		
		mBackgroundImageRot += rot;
		
		mBackgroundImageOffsetX = (mBackgroundImageOffsetX + (tail.x - pos.x))*newScale;
		mBackgroundImageOffsetY = (mBackgroundImageOffsetY + (tail.y - pos.y)*mulY)*newScale;
		
		mBackgroundImageScale *= newScale;
		
//		System.out.printf("adjustBackgroundImageTail() rot:%f mulY:%f mBackgroundImageRot: %f, mBackgroundImageOffsetX: %f, mBackgroundImageOffsetY: %f, mBackgroundImageScale: %f", rot, mulY, mBackgroundImageRot, mBackgroundImageOffsetX, mBackgroundImageOffsetY, mBackgroundImageScale);
		
		repaint();
	}
	
	public void adjustBackgroundImageNose(Point clickedScreenCoord)
	{
//		double mulX = ((mDrawControl&Brd.FlipX)!=0?-1.0f:1.0f);
		double mulY = ((mDrawControl&BezierBoardDrawUtil.FlipY)!=0?-1.0f:1.0f);

		Point2D.Double pos = screenCoordinateToBrdCoordinate(clickedScreenCoord);
		Point2D.Double tail = getTail();
		Point2D.Double nose =  getNose();
		
		Point2D.Double brdVec = new Point2D.Double();
		VecMath.subVector(tail, nose, brdVec);
		
		Point2D.Double imgVec = new Point2D.Double();
		VecMath.subVector(tail, pos, imgVec);

		double brdVecLen = VecMath.getVecLength(brdVec);
		double imgVecLen = VecMath.getVecLength(imgVec);
		double newScale = (brdVecLen/imgVecLen);		
		double rot = VecMath.getVecAngle(brdVec, imgVec)*(((brdVec.y > imgVec.y)?1:-1))*mulY;
		
		mBackgroundImageRot += rot;
				
		mBackgroundImageOffsetX *=newScale;
		mBackgroundImageOffsetY *=newScale;

		mBackgroundImageScale *= newScale;
	
//		System.out.printf("adjustBackgroundImageNose() rot:%f mulY:%f mBackgroundImageRot: %f, mBackgroundImageOffsetX: %f, mBackgroundImageOffsetY: %f, mBackgroundImageScale: %f\n", rot, mulY, mBackgroundImageRot, mBackgroundImageOffsetX, mBackgroundImageOffsetY, mBackgroundImageScale);

		repaint();
	}
	
	Point2D.Double getTail()
	{
		return (Point2D.Double)getActiveBezierSplines(getCurrentBrd())[0].getControlPoint(0).getEndPoint().clone();
	}
	
	Point2D.Double getNose()
	{
		return (Point2D.Double)getActiveBezierSplines(getCurrentBrd())[0].getControlPoint(getActiveBezierSplines(getCurrentBrd())[0].getNrOfControlPoints()-1).getEndPoint().clone();
	}
	

	
	@Override
	public void paintComponent(Graphics g) {

		adjustScaleAndOffset();

		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();
		
//		Turn on antialiasing, so painting is smooth.
		if(BoardCAD.getInstance().isAntialiasing())
		{
			g2d.setRenderingHint(

					RenderingHints.KEY_ANTIALIASING,

					RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

//		Paint the background.
		Color bkgColor = BoardCAD.getInstance().getBackgroundColor();
		if(mParentContainer != null && !mParentContainer.isActive(this))
		{
			bkgColor = BoardCAD.getInstance().getUnselectedBackgroundColor();
		}

		g2d.setColor(bkgColor);

		g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

		if(mDrawControl == 0)
		{
			//draw nurbs board
			
			BoardHandler bh=getBoardHandler();
			
			bh.draw_outline3D(g2d, 800, 600, mScale/10, (int)mOffsetX, (int)mOffsetY, false, BoardCAD.getInstance().mViewBottomCut.isSelected(), BoardCAD.getInstance().mViewDeckCut.isSelected(), mRotationMatrix);
			//bh.draw_outline(g2d, 800, 600, mScale/10, (int)mOffsetX, (int)mOffsetY, false, false, false);


			if(mDrawZoomRectangle == true)
			{
				g2d.setColor(Color.blue);
				g2d.drawRect(mZoomRectangle.x, mZoomRectangle.y, mZoomRectangle.width, mZoomRectangle.height);			
			}

			BoardCAD.getInstance().status_panel.set_mode(BoardCAD.getInstance().getCurrentCommand().getCommandString() + " " + LanguageResource.getString("MODE_STR"));

			//drawBrdCoordinate(g2d);

			return;
		}

	if(!BoardCAD.getInstance().mViewBlank.isSelected())
	{

		BezierBoard currentBrd = getCurrentBrd();
		if(currentBrd == null)
			return;

		if(BoardCAD.getInstance().isPaintingBackgroundImage())
		{
			drawBackgroundImage(g2d);
		}

		Stroke stroke;
		stroke = new BasicStroke((float)(BoardCAD.getInstance().getBezierThickness()/mScale));

		if(BoardCAD.getInstance().isPaintingGrid())
		{
			BezierBoardDrawUtil.paintGrid(new JavaDraw(g2d), mOffsetX, mOffsetY, mScale, 0.0, BoardCAD.getInstance().getGridColor(), currentBrd.getLength(), currentBrd.getMaxWidth()/2.0, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, (mDrawControl&BezierBoardDrawUtil.FlipY)!=0);
		}

		
		BezierBoard ghostBrd = getGhostBrd();
		BezierBoard orgBrd = getOriginalBrd();
		if(BoardCAD.getInstance().mGhostMode && ghostBrd.isEmpty() == false)
		{
			if(BoardCAD.getInstance().isPaintingOriginalBrd())
			{
				if(orgBrd != null && getActiveBezierSplines(orgBrd) != null)
				{
					AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g2d), mOriginalOffsetX*mScale, mOriginalOffsetY*mScale, 1.0, 0.0);	
					drawPart(g2d, BoardCAD.getInstance().getOriginalBrdColor(), stroke, orgBrd, BoardCAD.getInstance().useFill());
					g2d.setTransform(savedTransform);
				}
			}

			drawPart(g2d, BoardCAD.getInstance().getGhostBrdColor(), stroke, currentBrd, BoardCAD.getInstance().useFill());
			if(BoardCAD.getInstance().isPaintingCurvature())
			{
				drawCurvature(g2d, BoardCAD.getInstance().getGhostBrdColor(), new BasicStroke((float)(BoardCAD.getInstance().getCurvatureThickness()/mScale)), currentBrd);
			}
			
			if(isPaintingVolumeDistribution())
			{
				drawVolumeDistribution(g2d, BoardCAD.getInstance().getGhostBrdColor(), new BasicStroke((float)(BoardCAD.getInstance().getVolumeDistributionThickness()/mScale)), currentBrd);
			}
					 
			if(BoardCAD.getInstance().isPaintingCenterOfMass())
			{
				double x = ghostBrd.getCenterOfMass();
				drawCircle(g2d, BoardCAD.getInstance().getCenterOfMassColor(), new BasicStroke((float)(BoardCAD.getInstance().getSelectedControlPointOutlineThickness()/mScale)), new Point2D.Double(x*Math.cos(mGhostRot)+(mGhostOffsetX*mScale),x*Math.sin(mGhostRot)+(mGhostOffsetY*mScale)));				
			}
			if(ghostBrd != null && getActiveBezierSplines(ghostBrd) != null)
			{
				AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g2d), mGhostOffsetX*mScale, mGhostOffsetY*mScale, 1.0, mGhostRot);	
				
				drawPart(g2d, BoardCAD.getInstance().getBrdColor(), stroke, ghostBrd, BoardCAD.getInstance().useFill());
		
				if(BoardCAD.getInstance().isPaintingSlidingInfo())
				{
					drawSlidingInfo(g2d, BoardCAD.getInstance().getBrdColor(), stroke, ghostBrd);
				}
		
				if(BoardCAD.getInstance().isPaintingCurvature())
				{
					drawCurvature(g2d, BoardCAD.getInstance().getCurvatureColor(), new BasicStroke((float)(BoardCAD.getInstance().getCurvatureThickness()/mScale)), ghostBrd);
				}
				
				if(isPaintingVolumeDistribution())
				{
					drawVolumeDistribution(g2d, BoardCAD.getInstance().getVolumeDistributionColor(), new BasicStroke((float)(BoardCAD.getInstance().getVolumeDistributionThickness()/mScale)), ghostBrd);
				}
		
				if(BoardCAD.getInstance().isPaintingControlPoints())
				{
					drawControlPoints(g2d, new BasicStroke((float)(BoardCAD.getInstance().getUnselectedControlPointOutlineThickness()/mScale)), ghostBrd);
				}
				
				g2d.setTransform(savedTransform);
			}

		}
		else if(BoardCAD.getInstance().mOrgFocus && orgBrd.isEmpty() == false)
		{
			if(BoardCAD.getInstance().isPaintingGhostBrd())
			{
				if(ghostBrd != null && getActiveBezierSplines(ghostBrd) != null)
				{
					AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g2d), mGhostOffsetX*mScale, mGhostOffsetY*mScale, 1.0, mGhostRot);	
					drawPart(g2d, BoardCAD.getInstance().getGhostBrdColor(), stroke, ghostBrd, BoardCAD.getInstance().useFill());
					g2d.setTransform(savedTransform);
				}
			}

			drawPart(g2d, BoardCAD.getInstance().getOriginalBrdColor(), stroke, currentBrd, BoardCAD.getInstance().useFill());
			if(BoardCAD.getInstance().isPaintingCurvature())
			{
				drawCurvature(g2d, BoardCAD.getInstance().getOriginalBrdColor(), new BasicStroke((float)(BoardCAD.getInstance().getCurvatureThickness()/mScale)), currentBrd);
			}
			
			if(isPaintingVolumeDistribution())
			{
				drawVolumeDistribution(g2d, BoardCAD.getInstance().getOriginalBrdColor(), new BasicStroke((float)(BoardCAD.getInstance().getVolumeDistributionThickness()/mScale)), currentBrd);
			}
						
			if(BoardCAD.getInstance().isPaintingCenterOfMass())
			{
				double x = orgBrd.getCenterOfMass();
				drawCircle(g2d, BoardCAD.getInstance().getCenterOfMassColor(), new BasicStroke((float)(BoardCAD.getInstance().getUnselectedControlPointOutlineThickness()/mScale)), new Point2D.Double(x,0));				
			}

			if(orgBrd != null && getActiveBezierSplines(orgBrd) != null)
			{
				AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g2d), mOriginalOffsetX*mScale, mOriginalOffsetY*mScale, 1.0, 0.0);	
				
				drawPart(g2d, BoardCAD.getInstance().getBrdColor(), stroke, orgBrd, BoardCAD.getInstance().useFill());
		
				if(BoardCAD.getInstance().isPaintingSlidingInfo())
				{
					drawSlidingInfo(g2d, BoardCAD.getInstance().getBrdColor(), stroke, orgBrd);
				}
		
				if(BoardCAD.getInstance().isPaintingCurvature())
				{
					drawCurvature(g2d, BoardCAD.getInstance().getCurvatureColor(), new BasicStroke((float)(BoardCAD.getInstance().getCurvatureThickness()/mScale)), orgBrd);
				}
				if(isPaintingVolumeDistribution())
				{
					drawVolumeDistribution(g2d, BoardCAD.getInstance().getVolumeDistributionColor(), new BasicStroke((float)(BoardCAD.getInstance().getVolumeDistributionThickness()/mScale)), orgBrd);
				}

				if(BoardCAD.getInstance().isPaintingControlPoints())
				{
					drawControlPoints(g2d, new BasicStroke((float)(BoardCAD.getInstance().getUnselectedControlPointOutlineThickness()/mScale)), orgBrd);
				}
				
				g2d.setTransform(savedTransform);
			}

		}
		else
		{
			if(BoardCAD.getInstance().isPaintingOriginalBrd())
			{
				if(orgBrd != null && getActiveBezierSplines(orgBrd) != null)
				{
					AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g2d), mOriginalOffsetX*mScale, mOriginalOffsetY*mScale, 1.0, 0.0);	
					drawPart(g2d, BoardCAD.getInstance().getOriginalBrdColor(), stroke, orgBrd, BoardCAD.getInstance().useFill());
					g2d.setTransform(savedTransform);
				}
			}
	
			if(BoardCAD.getInstance().isPaintingGhostBrd())
			{
				if(ghostBrd != null && getActiveBezierSplines(ghostBrd) != null)
				{
					AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g2d), mGhostOffsetX*mScale, mGhostOffsetY*mScale, 1.0, mGhostRot);	
					drawPart(g2d, BoardCAD.getInstance().getGhostBrdColor(), stroke, ghostBrd, BoardCAD.getInstance().useFill());
					g2d.setTransform(savedTransform);
				}
			}
					
			drawPart(g2d, BoardCAD.getInstance().getBrdColor(), stroke, currentBrd, BoardCAD.getInstance().useFill());

			if(BoardCAD.getInstance().isPaintingSlidingInfo())
			{
				drawSlidingInfo(g2d, BoardCAD.getInstance().getBrdColor(), stroke, currentBrd);
			}
	
			if(BoardCAD.getInstance().isPaintingCurvature())
			{
				drawCurvature(g2d, BoardCAD.getInstance().getCurvatureColor(), new BasicStroke((float)(BoardCAD.getInstance().getCurvatureThickness()/mScale)), currentBrd);
			}
			
			if(isPaintingVolumeDistribution())
			{
				drawVolumeDistribution(g2d, BoardCAD.getInstance().getVolumeDistributionColor(), new BasicStroke((float)(BoardCAD.getInstance().getVolumeDistributionThickness()/mScale)), currentBrd);
			}

			if(BoardCAD.getInstance().isPaintingCenterOfMass() && currentBrd.isEmpty() == false)
			{
				double x = currentBrd.getCenterOfMass();
				drawCircle(g2d, BoardCAD.getInstance().getCenterOfMassColor(), new BasicStroke((float)(BoardCAD.getInstance().getUnselectedControlPointOutlineThickness()/mScale)), new Point2D.Double(x,0));				
			}
	
			if(BoardCAD.getInstance().isPaintingControlPoints())
			{
				drawControlPoints(g2d, new BasicStroke((float)(BoardCAD.getInstance().getUnselectedControlPointOutlineThickness()/mScale)), currentBrd);
			}
		}
}
		if(BoardCAD.getInstance().isPaintingGuidePoints())
		{
			drawGuidePoints(g2d);
		}


		if(mDrawZoomRectangle == true)
		{
			g2d.setColor(Color.blue);
			g2d.drawRect(mZoomRectangle.x, mZoomRectangle.y, mZoomRectangle.width, mZoomRectangle.height);			
		}

		drawBrdCoordinate(g2d);


		//draw nurbs board
		
		BoardHandler bh=getBoardHandler();
		if(mDrawControl == BezierBoardDrawUtil.MirrorY)
		{
			bh.draw_outline(g2d, 800, 600, mScale/10, (int)mOffsetX, (int)mOffsetY, false, BoardCAD.getInstance().mViewBottomCut.isSelected(), BoardCAD.getInstance().mViewDeckCut.isSelected(), BoardCAD.getInstance().mViewBlank.isSelected());
		}
		else if(mDrawControl == BezierBoardDrawUtil.FlipY)
		{
			bh.draw_rocker(g2d, 800, 600, mScale/10, (int)mOffsetX, (int)mOffsetY, false, BoardCAD.getInstance().mViewBottomCut.isSelected(), BoardCAD.getInstance().mViewDeckCut.isSelected(), BoardCAD.getInstance().mViewBlank.isSelected());
		}
		else if(mDrawControl == (BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY))
		{
			bh.draw_edge(g2d, 800, 600, mScale/10, (int)mOffsetX, (int)mOffsetY);
		}


	}    

	public void drawCircle(Graphics2D g, Color color, Stroke stroke, Point2D.Double pos) {

		BezierBoardDrawUtil.paintCircle(new JavaDraw(g),mOffsetX, mOffsetY, mScale, 0.0, color, stroke, pos, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, (mDrawControl&BezierBoardDrawUtil.FlipY)!=0);
	}

	public void drawPart(Graphics2D g, Color color, Stroke stroke, BezierBoard brd, boolean fill) 
	{
		BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g),mOffsetX, mOffsetY, mScale, 0.0, color, stroke, getActiveBezierSplines(brd), mDrawControl, fill);	  			
	}

	public void drawControlPoints(Graphics2D g, Stroke stroke, BezierBoard brd) 
	{
		BezierBoardDrawUtil.paintBezierControlPoints(new JavaDraw(g),mOffsetX, mOffsetY, mScale, 0.0, stroke, getActiveBezierSplines(brd), (mParentContainer==null || mParentContainer.isActive(this))? mSelectedControlPoints:null, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, (mDrawControl&BezierBoardDrawUtil.FlipY)!=0);
	}

	public void drawCurvature(Graphics2D g, Color color, Stroke stroke, BezierBoard brd) 
	{
		BezierBoardDrawUtil.paintCurvature(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, color, stroke, getActiveBezierSplines(brd), (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, (mDrawControl&BezierBoardDrawUtil.FlipY)!=0, mCurvatureScale );
	}
	
	public void drawVolumeDistribution(Graphics2D g, Color color, Stroke stroke, BezierBoard brd) 
	{
		BezierBoardDrawUtil.paintVolumeDistribution(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, color, stroke, brd, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true, mVolumeDistributionScale );
	}
		
	public void drawSlidingInfo(Graphics2D g, Color color, Stroke stroke, BezierBoard brd)
	{
	}
	
	public void drawFins(Graphics2D g, Color color, Stroke stroke, BezierBoard brd)
	{
		BezierBoardDrawUtil.paintFins(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, color, stroke, brd.getFins(), (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, (mDrawControl&BezierBoardDrawUtil.FlipY)!=0 );
	}
	
	public void drawStringer(Graphics2D g, Color color, Stroke stroke, BezierBoard brd)
	{
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0);	

		double mulX = ((mDrawControl&BezierBoardDrawUtil.FlipX)!=0)?-1:1;
//		double mulY = ((mDrawControl&Brd.FlipY)!=0)?-1:1;
	
		g.setColor(color);
		g.setStroke(stroke);

		final Line2D tmp = new Line2D.Double();
		tmp.setLine(0,0,brd.getLength()*mulX,0);
		g.draw(tmp);
	
		g.setTransform(savedTransform);			
	}

	public void drawCenterLine(Graphics2D g, Color color, Stroke stroke, double pos, double length)
	{
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0 );	

		double mulX = ((mDrawControl&BezierBoardDrawUtil.FlipX)!=0)?-1:1;
//		double mulY = ((mDrawControl&Brd.FlipY)!=0)?-1:1;
	
		g.setColor(color);
		g.setStroke(stroke);

		final Line2D tmp = new Line2D.Double();
		tmp.setLine(pos*mulX,-length/2.0,pos*mulX,length/2.0);
		g.draw(tmp);
	
		g.setTransform(savedTransform);			
	}

	public void drawBackgroundImage(Graphics2D g)
	{
		if(mBackgroundImage != null)
		{
			AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0);	

			final AffineTransform tmp = new AffineTransform();

			tmp.setToRotation(mBackgroundImageRot);

			g.transform(tmp);
		
			tmp.setToTranslation(mBackgroundImageOffsetX, mBackgroundImageOffsetY);

			g.transform(tmp);

			tmp.setToScale(mBackgroundImageScale, mBackgroundImageScale);

			g.transform(tmp);

			g.drawImage(mBackgroundImage,0,0,this);
		
			g.setTransform(savedTransform);	
		}
		
	}

	public void drawBrdCoordinate(Graphics2D g) 
	{
		g.setColor(Color.BLACK);

		//	 get metrics from the graphics
		FontMetrics metrics = g.getFontMetrics(mBrdCoordFont);
		
//		Dimension dim = getSize();
		
		// get the height of a line of text in this font and render context
		int hgt = metrics.getHeight();

		mBrdCoordString = "x: " + UnitUtils.convertLengthToCurrentUnit(mBrdCoord.x, true) ;

		g.drawString(mBrdCoordString, 10, hgt);

		mBrdCoordString = "y: " + UnitUtils.convertLengthToCurrentUnit(mBrdCoord.y, true);

		g.drawString(mBrdCoordString, 90, hgt);

		mBrdCoordString = getCurrentCommand().getCommandString() + " " + LanguageResource.getString("MODE_STR");
		
		// get the advance of my text in this font and render context
//		int width = metrics.stringWidth(mBrdCoordString);

		g.drawString(mBrdCoordString, 10, hgt*2);
	}

	public void drawGuidePoints(Graphics2D g)
	{
		ArrayList<Point2D.Double> guidePoints = getGuidePoints();
		if(guidePoints == null)
			return;

		g.setColor(BoardCAD.getInstance().getGuidePointColor());

		g.setStroke(new BasicStroke((float)(BoardCAD.getInstance().getGuidePointThickness()/mScale)));

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0);	

		double crossSize = 4.0f/mScale;

		double mulX = (mDrawControl&BezierBoardDrawUtil.FlipX)!=0?-1:1;
		double mulY= (mDrawControl&BezierBoardDrawUtil.FlipY)!=0?-1:1;

		Line2D.Double line = new Line2D.Double();

		Point2D.Double pos = new Point2D.Double();;
		for(int i = 0; i < guidePoints.size(); i++)
		{
			pos.setLocation(guidePoints.get(i));
			line.setLine((pos.x-crossSize)*mulX, (pos.y)*mulY, (pos.x+crossSize)*mulX, (pos.y)*mulY);
			g.draw(line);
			line.setLine((pos.x)*mulX, (pos.y-crossSize)*mulY, (pos.x)*mulX, (pos.y+crossSize)*mulY);
			g.draw(line);
		}
		g.setTransform(savedTransform);

	}

	public void onBrdChanged()
	{
		BoardCAD.getInstance().onBrdChanged();

		repaint();
	}


	@Override
	public void mousePressed(MouseEvent e) {
		
		if(mParentContainer != null)
		{
			mParentContainer.setActive(this);
		}
		
		if (mPopupMenu != null && e.isPopupTrigger()) {
            mPopupMenu.show(e.getComponent(),
                       e.getX(), e.getY());
            return;
        }
		
		BrdInputCommand cmd = (BrdInputCommand)getCurrentCommand();
		if(cmd == null)
			return;

		switch(e.getButton())
		{
			case MouseEvent.BUTTON1:
				cmd.onLeftMouseButtonPressed(this, e);
				break;
				
			case MouseEvent.BUTTON2:
				cmd.onMouseWheelButtonPressed(this, e);
				break;

			case MouseEvent.BUTTON3:
				cmd.onRightMouseButtonPressed(this, e);
				break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (mPopupMenu != null && e.isPopupTrigger()) {
            mPopupMenu.show(e.getComponent(),
                       e.getX(), e.getY());
            return;
        }

		BrdInputCommand cmd = (BrdInputCommand)getCurrentCommand();
		if(cmd == null)
			return;

		switch(e.getButton())
		{
			case MouseEvent.BUTTON1:
				cmd.onLeftMouseButtonReleased(this, e);
				break;
				
			case MouseEvent.BUTTON2:
				cmd.onMouseWheelButtonReleased(this, e);
				break;

			case MouseEvent.BUTTON3:
				cmd.onRightMouseButtonReleased(this, e);
				break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mHasMouse = true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mHasMouse = false;
	}

	public boolean hasMouse()
	{
		return mHasMouse;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		mScreenCoord = e.getPoint();
		mBrdCoord.setLocation(screenCoordinateToBrdCoordinate(mScreenCoord));
		
		BoardHandler bh=getBoardHandler();
		if(mDrawControl == BezierBoardDrawUtil.MirrorY)
		{
			if(!bh.is_marked())
			{
				BoardCAD.getInstance().status_panel.set_point_name("");
				BoardCAD.getInstance().status_panel.set_coordinates(mBrdCoord.x*10, 0, mBrdCoord.y*10);
			}
		}
		else if(mDrawControl == BezierBoardDrawUtil.FlipY)
		{
			if(!bh.is_marked())
			{
				BoardCAD.getInstance().status_panel.set_point_name("");
				BoardCAD.getInstance().status_panel.set_coordinates(mBrdCoord.x*10, mBrdCoord.y*10, 0);
			}

		}
		else if(mDrawControl == (BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY))
		{
			if(!bh.is_marked())
			{
				BoardCAD.getInstance().status_panel.set_point_name("");
				BoardCAD.getInstance().status_panel.set_coordinates(0, mBrdCoord.y*10, mBrdCoord.x*10);
			}
		}
		
		repaint();

		BrdInputCommand cmd = (BrdInputCommand)getCurrentCommand();
		if(cmd == null)
			return;

		cmd.onMouseMove(this, e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mScreenCoord = e.getPoint();
		mBrdCoord.setLocation(screenCoordinateToBrdCoordinate(mScreenCoord));
		repaint();

		BrdInputCommand cmd = (BrdInputCommand)getCurrentCommand();
		if(cmd == null)
			return;

		cmd.onMouseDragged(this, e);
		
	
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		BrdInputCommand cmd = (BrdInputCommand)getCurrentCommand();
		if(cmd == null)
			return;

		cmd.onMouseWheelMoved(this, e);
		
	}

	@Override
	public void fit_all()
	{
		fitBrd();
	}
}
