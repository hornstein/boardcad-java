package boardcam.holdingsystems;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.swing.JOptionPane;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.AbstractBoard;
import board.BezierBoard;
import boardcad.gui.jdk.BoardCAD;	//TODO: bad dependency
import boardcad.i18n.LanguageResource;
import boardcam.cutters.AbstractCutter;

public class AbstractBlankHoldingSystem {
	
	protected BezierBoard mBoard;
	protected BezierBoard mBlank;
	
	protected Vector3d mBlankDeckOffset = new Vector3d();
	protected double mBlankDeckRotation=0;
	protected Vector3d mBlankBottomOffset = new Vector3d();
	double mBlankBottomRotation=0;
	
	protected Vector3d mBoardDeckOffset = new Vector3d();
	double mBoardDeckRotation=0;
	protected Vector3d mBoardBottomOffset = new Vector3d();
	double mBoardBottomRotation=0;
	
	BranchGroup mModelRoot = new BranchGroup();
	ChangeListener mChangeListener = null;

	public void setChangeListener(ChangeListener changeListener){
		mChangeListener = changeListener;
	}

	public void init()
	{
		mModelRoot.setCapability(BranchGroup.ALLOW_DETACH);
	}
	
	public void setBoard(BezierBoard board)
	{
		mBoard = board;
	}
	
	public void setBlank(BezierBoard blank)
	{
		mBlank = blank;
	}
	
	public Vector3d getBlankDeckOffsetPos()
	{
		return mBlankDeckOffset;
	}
		
	public double getBlankDeckOffsetAngle()
	{
		return mBlankDeckRotation;
	}
	
	public Vector3d getBlankBottomOffsetPos()
	{
		return mBlankBottomOffset;
	}
	
	public double getBlankBottomOffsetAngle()
	{
		return mBlankBottomRotation;
	}
			
	public void setBoardDeckOffsetPos(Vector3d offset)
	{
		mBoardDeckOffset.set(offset);
		System.out.printf("AbstractBlankHoldingSystem.setBoardDeckOffsetPos: %s, %f,%f,%f\n", toString(), mBoardDeckOffset.x, mBoardDeckOffset.y, mBoardDeckOffset.z);
		if(mChangeListener != null){
			mChangeListener.onChange();
		}
	}

	public void setBoardDeckOffsetAngle(double angle)
	{
		mBoardDeckRotation = angle;
		System.out.printf("AbstractBlankHoldingSystem.setBoardDeckOffsetAngle: %s %f\n", toString(), mBoardDeckRotation);
		if(mChangeListener != null){
			mChangeListener.onChange();
		}
	}

	public Vector3d getBoardDeckOffsetPos()
	{
		//System.out.printf("AbstractBlankHoldingSystem.getBoardDeckOffsetPos: %s, %f,%f,%f\n", toString(), mBoardDeckOffset.x, mBoardDeckOffset.y, mBoardDeckOffset.z);
		return mBoardDeckOffset;
	}
	
	public double getBoardDeckOffsetAngle()
	{
		//System.out.printf("AbstractBlankHoldingSystem.getBoardDeckOffsetAngle: %s, %f\n", toString(), mBoardDeckRotation);
		return mBoardDeckRotation;
	}

	public Vector3d getBoardBottomOffsetPos()
	{
		return mBoardBottomOffset;
	}
	
	public double getBoardBottomOffsetAngle()
	{
		return mBoardBottomRotation;
	}

	public boolean checkCollision(Point3d pos, AbstractCutter cutter)
	{
		return false;
	}
	
	public ArrayList<double[]> handleCollision(Point3d point, AbstractCutter cutter, AbstractBoard board)
	{
		JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(),
				LanguageResource.getString("TOOLPATHCOLLISIONTITLE_STR"),
				LanguageResource.getString("TOOLPATHCOLLISIONMSG_STR"),
				JOptionPane.ERROR_MESSAGE);
		
		return null;
	}
	
	public BranchGroup get3DModel()
	{
		return mModelRoot;
	}

	public void update3DModel()
	{
	}
	
	public void draw(Graphics2D g2d, double offsetX, double offsetY, double scale, boolean deck)
	{
		//Setup for drawing
		AffineTransform savedTransform = g2d.getTransform();
	
		g2d.setColor(Color.WHITE);
	
		Stroke stroke = new BasicStroke((float)(1.0/scale));
		g2d.setStroke(stroke);
	
		AffineTransform at = new AffineTransform();
	
		at.setToTranslation(offsetX, offsetY);
	
		g2d.transform(at);
	
		at.setToScale(scale, scale);
	
		g2d.transform(at);
		
		//Draw cross at zero (reference point)
		final double halfCrossWidth = 5.0;
		Line2D line = new Line2D.Double();
		line.setLine(-halfCrossWidth, 0.0, halfCrossWidth, 0.0);
		g2d.draw(line);
		line.setLine(0.0, -halfCrossWidth, 0.0, halfCrossWidth);
		g2d.draw(line);

		g2d.setTransform(savedTransform);	
	}
	
	public String toString(boolean isDeckCut)
	{
		Vector3d boardOffset = isDeckCut?mBoardDeckOffset:mBoardBottomOffset;
		String boardOffsetStr = String.format("Board offset: %f,%f,%f Angle:%f\n", boardOffset.x, boardOffset.y, boardOffset.z, isDeckCut?mBoardDeckRotation:mBoardBottomRotation);
		Vector3d blankOffset = isDeckCut?mBlankDeckOffset:mBlankBottomOffset;
		String blankOffsetStr = String.format("Blank offset: %f,%f,%f Angle:%f\n", blankOffset.x, blankOffset.y, blankOffset.z, isDeckCut?mBlankDeckRotation:mBlankBottomRotation);
		
		boardOffsetStr.concat(blankOffsetStr);
		
		return boardOffsetStr;
	}
	
}
