package boardcad.gui.jdk;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.vecmath.*;

import board.BezierBoard;
import boardcad.commands.BrdCommand;
import boardcad.commands.BrdPositionCommand;
import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;
import boardcam.holdingsystems.AbstractBlankHoldingSystem;

public class Machine2DView extends BoardEdit {

	static final double fixedHeightBorder = 0;
	public boolean mShowDeck = true;
	double BORDER = 10;
	BrdCommand mBrdPosCommand;

	public MachineConfig mConfig;

	Point3d mCutterOffset = new Point3d(0.0, 0.0, 0.0);

	Machine2DView(MachineConfig config) {
		super();

		mConfig = config;
		System.out.printf("Machine2DView constructor mConfig:%s this:%s\n", mConfig.toString(), this.toString());

		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JPopupMenu menu = new JPopupMenu();

		final AbstractAction toggleDeckBottom = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TOGGLEDECKBOTTOM_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mShowDeck = !mShowDeck;
				repaint();
			}
		};
		menu.add(toggleDeckBottom);
		add(menu);

		setPreferredSize(new Dimension(400, 100));

		setLayout(new BorderLayout());

		mBrdPosCommand = new BrdPositionCommand(this);

	};

	@Override
	public void fit_all() {
		Dimension dim = getSize();
		double len = 0.0;
		BezierBoard brd = mConfig.getBoard();
		if (brd != null) {
			len = brd.getLength();
		}
		if (mConfig.getBlankHoldingSystem() != null) {
			len += mConfig.getBlankHoldingSystem().getBlankDeckOffsetPos().x;
		}

		mScale = (dim.width - ((BORDER * dim.width / 100) * 2)) / len;

		mOffsetX = (BORDER * dim.width / 100);

		mOffsetY = dim.height * 1 / 2;

		mLastWidth = dim.width;
		mLastHeight = dim.height;

		calcViewOffset();

		repaint();
	}

	@Override
	public void adjustScaleAndOffset() {
		double currentWidth = getWidth();
		double currentHeight = getHeight();

		double widthChange = (currentWidth / mLastWidth);
		double heightChange = (currentHeight / mLastHeight);

		mScale *= widthChange;

		mOffsetX *= widthChange;
		mOffsetY *= widthChange;

		mLastWidth = currentWidth;
		mLastHeight = currentHeight;

	}

	public void setCutterOffset(Point3d pos) {
		mCutterOffset = pos;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {

		adjustScaleAndOffset();

		/**
		 * 
		 * Copy the graphics context so we can change it.
		 * 
		 * Cast it to Graphics2D so we can use anti-aliasing.
		 */

		Graphics2D g2d = (Graphics2D) g.create();

		// Turn on anti-aliasing, so painting is smooth.
		g2d.setRenderingHint(

		RenderingHints.KEY_ANTIALIASING,

		RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Paint the background.
		Color bkgColor = Color.LIGHT_GRAY;

		g2d.setColor(bkgColor);

		g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

		// Draw the blank holding system
		AbstractBlankHoldingSystem holdingSystem = mConfig.getBlankHoldingSystem();
		if (holdingSystem == null) {
			return;
		}

		holdingSystem.draw(g2d, mOffsetX, mOffsetY, mScale, mShowDeck);

		// Draw the blank and the board
		Vector3d boardOffset = mShowDeck ? holdingSystem.getBoardDeckOffsetPos() : holdingSystem.getBoardBottomOffsetPos();
		double boardAngle = mShowDeck ? holdingSystem.getBoardDeckOffsetAngle() : holdingSystem.getBoardBottomOffsetAngle();

		Vector3d blankOffset = mShowDeck ? holdingSystem.getBlankDeckOffsetPos() : holdingSystem.getBlankBottomOffsetPos();
		double blankAngle = mShowDeck ? holdingSystem.getBlankDeckOffsetAngle() : holdingSystem.getBlankBottomOffsetAngle();

		JavaDraw jd = new JavaDraw(g2d);

		// System.out.printf("OffsetX: %f, OffsetY: %f, boardOffset: %f,%f boardAngle: %f, blankOffset: %f,%f blankAngle: %f\n",
		// mOffsetX, mOffsetY, boardOffset.x, boardOffset.z, boardAngle,
		// blankOffset.x, blankOffset.z, blankAngle);

		Stroke stroke = new BasicStroke((float) (1.2 / mScale));
		g2d.setStroke(stroke);

		BezierBoard brd = mConfig.getBoard();
		if (brd != null) {
			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX + (boardOffset.x * mScale), mOffsetY - (boardOffset.z * mScale), mScale, boardAngle, BoardCAD.getInstance().getBrdColor(), stroke, brd.getDeck(), (mShowDeck ? BezierBoardDrawUtil.FlipY : 0), false);
			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX + (boardOffset.x * mScale), mOffsetY - (boardOffset.z * mScale), mScale, boardAngle, BoardCAD.getInstance().getBrdColor(), stroke, brd.getBottom(), mShowDeck ? BezierBoardDrawUtil.FlipY : 0, false);
		}

		// Draw blank
		BezierBoard blank = mConfig.getBlank();
		if (blank != null && !blank.isEmpty()) {
			Color blankColor = BoardCAD.getInstance().getBlankColor();

			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX + (blankOffset.x * mScale), mOffsetY - (blankOffset.z * mScale), mScale, blankAngle, blankColor, stroke, blank.getDeck(), mShowDeck ? BezierBoardDrawUtil.FlipY : 0, false);
			BezierBoardDrawUtil.paintBezierSpline(jd, mOffsetX + (blankOffset.x * mScale), mOffsetY - (blankOffset.z * mScale), mScale, blankAngle, blankColor, stroke, blank.getBottom(), mShowDeck ? BezierBoardDrawUtil.FlipY : 0, false);
		}

		// Setup for drawing
		AffineTransform savedTransform = g2d.getTransform();

		AffineTransform at = new AffineTransform();

		at.setToTranslation(mOffsetX, mOffsetY);

		at.scale(mScale, mScale);

		g2d.transform(at);

		// Draw cutter offset
		g2d.setColor(Color.BLUE);

		final double halfCrossWidth = 5.0;
		Line2D line = new Line2D.Double();
		line.setLine(-halfCrossWidth + mCutterOffset.x, -mCutterOffset.z, halfCrossWidth + mCutterOffset.x, -mCutterOffset.z);
		g2d.draw(line);
		line.setLine(mCutterOffset.x, -halfCrossWidth - mCutterOffset.z, mCutterOffset.x, halfCrossWidth - mCutterOffset.z);
		g2d.draw(line);

		g2d.setTransform(savedTransform);

		// Draw sliding line on brd
		double brdX = (mBrdCoord.x / Math.cos(boardAngle)) - boardOffset.x;
		if (brdX < 0 || brdX > brd.getLength())
			return;

		g2d.setColor(Color.RED);
		savedTransform = g2d.getTransform();
		at = new AffineTransform();

		at.setToTranslation(mOffsetX, mOffsetY);
		g2d.transform(at);
		at.setToScale(mScale, mScale);
		g2d.transform(at);
		at.setToTranslation(boardOffset.x, -boardOffset.z);
		g2d.transform(at);
		at.setToRotation(boardAngle);
		g2d.transform(at);

		double deckY = -brd.getDeckAtPos(brdX);
		double bottomY = -brd.getRockerAtPos(brdX);
		line.setLine(brdX, deckY * (mShowDeck ? 1 : -1), brdX, bottomY * (mShowDeck ? 1 : -1));
		g2d.draw(line);

		g2d.setTransform(savedTransform);

		// TODO Draw sliding line on blank
		g2d.setColor(Color.BLUE);
		savedTransform = g2d.getTransform();
		at = new AffineTransform();

		at.setToTranslation(mOffsetX, mOffsetY);
		g2d.transform(at);
		at.setToScale(mScale, mScale);
		g2d.transform(at);
		at.setToTranslation(blankOffset.x, -blankOffset.z);
		g2d.transform(at);
		at.setToRotation(blankAngle);
		g2d.transform(at);

		double blankX = (mBrdCoord.x / Math.cos(blankAngle)) - blankOffset.x;
		double blankDeckY = -blank.getDeckAtPos(blankX);
		double blankBottomY = -blank.getRockerAtPos(blankX);
		line.setLine(blankX, blankDeckY * (mShowDeck ? 1 : -1), blankX, blankBottomY * (mShowDeck ? 1 : -1));
		g2d.draw(line);

		g2d.setTransform(savedTransform);

		// Draw cross section of brd
		double crsScale = mScale * 2;
		Stroke crsStroke = new BasicStroke((float) (1.3 / crsScale));
		double crsY = getHeight() / 6 + (-boardOffset.z + bottomY + brdX * Math.sin(boardAngle)) * crsScale;
		BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g2d), getWidth() / 2, crsY, crsScale, 0.0, Color.RED, crsStroke, false, mShowDeck, brdX, brd);

		// Draw cross section blank
		double blankCrsY = getHeight() / 6 + (-blankOffset.z + blankBottomY + blankX * Math.sin(blankAngle)) * crsScale;
		BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g2d), getWidth() / 2, blankCrsY, crsScale, 0.0, Color.BLUE, crsStroke, false, mShowDeck, blankX, blank);

	}

	@Override
	public void drawSlidingInfo(Graphics2D g, Color color, Stroke stroke, BezierBoard brd) {
		// drawProfileSlidingInfo(this, g, color, stroke, brd);
	}

	@Override
	public BrdCommand getCurrentCommand() {
		return mBrdPosCommand;
	}

	void calcViewOffset() {
		// mOffsetY =
		// getCurrentMachineConfig().getDouble(MachineConfig.SUPPORT_1_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
	}

}