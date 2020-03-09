package boardcad.print;

/**

 * @author Håvard

 *

 * To change the template for this generated type comment go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.ujmp.core.util.MathUtil;

import board.BezierBoard;
import cadcore.BezierSpline;
import cadcore.MathUtils;
import cadcore.UnitUtils;
import boardcad.AbstractDraw;
import boardcad.i18n.LanguageResource;
import boardcad.gui.jdk.*;

public class PrintHollowWoodTemplates extends JComponent implements Printable {

	static final long serialVersionUID = 1L;

	private PageFormat myPageFormat;
	private PrinterJob myPrintJob;

	protected enum PrintState {
		NO_STATE, PRINT_DECKSKIN_TEMPLATE, PRINT_BOTTOMSKIN_TEMPLATE, PRINT_RAIL_TEMPLATE, PRINT_STRINGER_TEMPLATE, PRINT_CROSSSECTION_TEMPLATES,

		PRINT_NOSE_SECTION_TEMPLATE, PRINT_TAIL_SECTION_TEMPLATE
	};

	public PrintState mCurrentPrintState;

	private Font mPrintFontSmall = new Font("Dialog", Font.PLAIN, 10);

	private double mSkinThickness = 0.5;
	private double mFrameThickness = 0.5;
	private double mWebbing = 1.5;
	private double mDistanceToRail = 5.0;
	private double mTailOffset = 4.0;
	private double mNoseOffset = 4.0;
	private double mCrosssectionPos = 3.0;

	/** Creates and initializes the ClickMe component. */

	public PrintHollowWoodTemplates() {

		// Hint at good sizes for this component.

		setPreferredSize(new Dimension(800, 600));

		setMinimumSize(new Dimension(600, 480));

		// Request a black line around this component.

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

	}

	/**
	 * 
	 * Paints the PrintBrd component. This method is
	 * 
	 * invoked by the Swing component-painting system.
	 */

	public void paintComponent(Graphics g) {

		/**
		 * 
		 * Copy the graphics context so we can change it.
		 * 
		 * Cast it to Graphics2D so we can use antialiasing.
		 */

		Graphics2D g2d = (Graphics2D) g.create();

		// Turn on antialiasing, so painting is smooth.

		g2d.setRenderingHint(

		RenderingHints.KEY_ANTIALIASING,

		RenderingHints.VALUE_ANTIALIAS_ON);

		// Paint the background.

		g2d.setColor(Color.WHITE);

		g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

		Dimension dim = getSize();

		double border = 30;

		if (BoardCAD.getInstance().getCurrentBrd() == null
				|| BoardCAD.getInstance().getCurrentBrd().isEmpty())
			return;

		JavaDraw jd = new JavaDraw(g2d);

		// BezierBoardDrawUtil.printRailTemplate(jd, border, dim.height*offset2,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 5.0, 0.8, 8.0, 15.0,
		// trues);
		// BezierBoardDrawUtil.printDeckSkinTemplate(jd, border,
		// dim.height*offset3,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 3.0);
		// BezierBoardDrawUtil.printBottomSkinTemplate(jd, border,
		// dim.height*4.0/5.0,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 3.0);
		// BezierBoardDrawUtil.printCrossSection(jd, border, dim.height*offset2,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 50.0, 3.0, 0.8);
		// BezierBoardDrawUtil.printCrossSection(jd, border, dim.height*offset2,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 50.0);
		// BezierBoardDrawUtil.printProfile(jd, border, dim.height*offset2,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 0.0, 0.0, false);
		// BezierBoardDrawUtil.printProfile(jd, border, dim.height*offset2,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 5.0, 0.0, false);
		// BezierBoardDrawUtil.printProfile(jd, border, dim.height*offset2,
		// (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),
		// true,BoardCAD.getInstance().getCurrentBrd(), 20.0, 0.0, false);

		double offset1 = 1.0 / 5.0;
		double offset2 = 2.0 / 5.0;
		double offset3 = 3.0 / 5.0;

		double scale = (dim.width - (border * 2))
				/ (BoardCAD.getInstance().getCurrentBrd().getLength());

		
		 //Print stringer 
		printStringerDebug(jd, border, dim.height * offset3,
				scale, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), 0.0,
		 mSkinThickness, false, mTailOffset, mNoseOffset);
		 

		/*
		 * //Print crosssections printCrossSections(jd, border + 200,
		 * dim.height*offset2, scale, 0.0,
		 * BoardCAD.getInstance().getCurrentBrd(), UnitUtils.INCH,
		 * mSkinThickness, mFrameThickness, mWebbing, false);
		 */

		// Print rails
		BezierBoardDrawUtil.printRailTemplate(jd, border, dim.height * offset3,
				scale, 0.0, true, BoardCAD.getInstance().getCurrentBrd(),
				mDistanceToRail, mSkinThickness, mTailOffset, mNoseOffset,
				false);

		printRailWebbing(jd, border, dim.height * offset3, scale, 0.0,
				BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
				mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
				mNoseOffset);

		printRailNotching(jd, border, dim.height * offset3, scale, 0.0,
				BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
				mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
				mNoseOffset);

		// TODO: Needs fixing
		printRailTailPieceNotches(jd, border, dim.height * offset3, scale,
				0.0, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
				mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
				mNoseOffset);
		/*
		 * printRailNosePieceNotches(jd, border, dim.height * offset3, scale,
		 * 0.0, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);
		 */

		/*
		 * //Print tail piece printTailPiece(jd, border, dim.height * offset3,
		 * scale, 0.0, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * false);
		 * 
		 * printTailPiece(jd, border, dim.height * offset3, scale, 0.0,
		 * BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * true);
		 * 
		 * printTailPieceWebbing(jd, border, dim.height * offset3, scale, 0.0,
		 * BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * false);
		 * 
		 * printTailPieceWebbing(jd, border, dim.height * offset3, scale, 0.0,
		 * BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * true);
		 */

		/*
		 * //Print nose piece printNosePiece(jd, border+300, dim.height *
		 * offset3, scale, 0.0, BoardCAD.getInstance().getCurrentBrd(),
		 * mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing,
		 * mTailOffset, mNoseOffset, false);
		 * 
		 * printNosePiece(jd, border+300, dim.height * offset3, scale, 0.0,
		 * BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * true);
		 * 
		 * printNosePieceWebbing(jd, border+300, dim.height * offset3, scale,
		 * 0.0, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * false);
		 * 
		 * printNosePieceWebbing(jd, border+300, dim.height * offset3, scale,
		 * 0.0, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
		 * mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset,
		 * true);
		 */
	}

	void printStringerDebug(AbstractDraw d, double offsetX, double offsetY,
			double scale, double rotation, boolean paintGrid, BezierBoard brd,
			double offset, double skinThickness, boolean flatten,
			double tailOffset, double noseOffset) {

		// Print stringer
		BezierBoardDrawUtil.printProfile(d, offsetX, offsetY, scale, rotation,
				paintGrid, brd, offset, skinThickness, flatten, tailOffset,
				noseOffset);
		/*
		 * BezierBoardDrawUtil.printProfile(jd, border, dim.height*offset2,
		 * (dim.
		 * width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength
		 * ()), 0.0, true, BoardCAD.getInstance().getCurrentBrd(), 0.0,
		 * mSkinThickness, false, 0.0, 0.0);
		 * 
		 * BezierBoardDrawUtil.printProfile(jd, border, dim.height*offset2,
		 * (dim.
		 * width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength
		 * ()), 0.0, true, BoardCAD.getInstance().getCurrentBrd(), 0.0, 0.0,
		 * false, 0.0, 0.0);
		 */
		printStringerWebbing(d, offsetX, offsetY, scale, rotation, brd,
				mSkinThickness, mFrameThickness, mWebbing);

		printStringerTailPieceCutOut(d, offsetX, offsetY, scale, rotation, brd,
				mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing,
				mTailOffset, mNoseOffset);

		printStringerNosePieceCutOut(d, offsetX, offsetY, scale, rotation, brd,
				mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing,
				mTailOffset, mNoseOffset);

	}

	void printCrossSectionsDebug(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double railDistance, double skinThickness, double frameThickness,
			double webbing, boolean mirror) {
		double crossSectionPos = 1 * UnitUtils.FOOT;

		while (crossSectionPos < brd.getLength()) {
			printCrossSection(d, offsetX, offsetY, scale, rotation, brd,
					crossSectionPos, railDistance, skinThickness,
					frameThickness, webbing, mirror);

			printCrossSection(d, offsetX, offsetY, scale, rotation, brd,
					crossSectionPos, railDistance, skinThickness,
					frameThickness, webbing, mirror);

			printCrossSectionWebbing(d, offsetX, offsetY, scale, rotation, brd,
					crossSectionPos, railDistance, skinThickness,
					frameThickness, webbing, mirror);

			printCrossSectionWebbing(d, offsetX, offsetY, scale, rotation, brd,
					crossSectionPos, railDistance, skinThickness,
					frameThickness, webbing, mirror);

			crossSectionPos += UnitUtils.FOOT;
			offsetY += scale * 10;
		}
	}

	public BezierBoard getBrd() {

		return BoardCAD.getInstance().getCurrentBrd();

	}

	void initPrint() {
		myPrintJob = PrinterJob.getPrinterJob();

		myPageFormat = PrintBrd.getPageFormat(this, myPrintJob, BoardCAD
				.getInstance().getCurrentBrd().getMaxRocker());
		if (myPageFormat == null)
			return;

		myPrintJob.setPrintable(this, myPageFormat);
	}

	public void printStringerTemplate(double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {

		initPrint();

		try {

			mSkinThickness = skinThickness;
			mFrameThickness = frameThickness;
			mWebbing = webbing;
			mCurrentPrintState = PrintState.PRINT_STRINGER_TEMPLATE;

			myPrintJob.print();

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public void printRailTemplate(double distanceFromRail,
			double skinThickness, double frameThickness, double webbing,
			double tailOffset, double noseOffset) {
		initPrint();

		try {

			mDistanceToRail = distanceFromRail;
			mSkinThickness = skinThickness;
			mFrameThickness = frameThickness;
			mWebbing = webbing;
			mTailOffset = tailOffset;
			mNoseOffset = noseOffset;
			mCurrentPrintState = PrintState.PRINT_RAIL_TEMPLATE;

			myPrintJob.print();

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public void printDeckSkinTemplate(double distanceFromRail,
			double tailOffset, double noseOffset) {

		initPrint();

		try {

			mDistanceToRail = distanceFromRail;
			mTailOffset = tailOffset;
			mNoseOffset = noseOffset;
			mCurrentPrintState = PrintState.PRINT_DECKSKIN_TEMPLATE;

			myPrintJob.print();

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public void printBottomSkinTemplate(double distanceFromRail,
			double tailOffset, double noseOffset) {

		initPrint();

		try {

			mDistanceToRail = distanceFromRail;
			mTailOffset = tailOffset;
			mNoseOffset = noseOffset;
			mCurrentPrintState = PrintState.PRINT_BOTTOMSKIN_TEMPLATE;

			myPrintJob.print();

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public void printCrosssectionTemplates(double distanceFromRail,
			double skinThickness, double frameThickness, double webbing) {

		initPrint();

		try {

			mDistanceToRail = distanceFromRail;
			mSkinThickness = skinThickness;
			mFrameThickness = frameThickness;
			mWebbing = webbing;
			mCurrentPrintState = PrintState.PRINT_CROSSSECTION_TEMPLATES;

			int nrOfCrossSections = (int) ((BoardCAD.getInstance()
					.getCurrentBrd().getLength() - 9.0 * UnitUtils.INCH) / UnitUtils.FOOT);

			for (int i = 0; i < nrOfCrossSections; i++) {
				mCrosssectionPos = (i + 1) * UnitUtils.FOOT;
				myPrintJob.print();
			}

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public void printNoseTemplate(double distanceFromRail,
			double skinThickness, double frameThickness, double webbing,
			double tailOffset, double noseOffset) {

		initPrint();

		try {

			mDistanceToRail = distanceFromRail;
			mSkinThickness = skinThickness;
			mFrameThickness = frameThickness;
			mWebbing = webbing;
			mTailOffset = tailOffset;
			mNoseOffset = noseOffset;
			mCurrentPrintState = PrintState.PRINT_NOSE_SECTION_TEMPLATE;

			myPrintJob.print();

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public void printTailTemplate(double distanceFromRail,
			double skinThickness, double frameThickness, double webbing,
			double tailOffset, double noseOffset) {

		initPrint();

		try {

			mDistanceToRail = distanceFromRail;
			mSkinThickness = skinThickness;
			mFrameThickness = frameThickness;
			mWebbing = webbing;
			mTailOffset = tailOffset;
			mNoseOffset = noseOffset;
			mCurrentPrintState = PrintState.PRINT_TAIL_SECTION_TEMPLATE;

			myPrintJob.print();

		} catch (PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {

		/*
		 * DEBUG!!!
		 * 
		 * if(pageIndex >=2)
		 * 
		 * {
		 * 
		 * isPrintingProfile = false;
		 * 
		 * isPrintingOutline = false;
		 * 
		 * return NO_SUCH_PAGE;
		 * 
		 * }
		 */

		switch (mCurrentPrintState) {

		case PRINT_STRINGER_TEMPLATE: {
			if (printStringerTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}

		case PRINT_RAIL_TEMPLATE: {
			if (printRailTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}

		case PRINT_DECKSKIN_TEMPLATE: {
			if (printDeckSkinTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}

		case PRINT_BOTTOMSKIN_TEMPLATE: {
			if (printBottomSkinTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}
		case PRINT_CROSSSECTION_TEMPLATES: {
			if (printCrosssectionTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}
		case PRINT_NOSE_SECTION_TEMPLATE: {
			if (printNosePieceTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}
		case PRINT_TAIL_SECTION_TEMPLATE: {
			if (printTailPieceTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;

			break;
		}
		}

		return NO_SUCH_PAGE;

	}

	int printStringerTemplate(PageFormat pageFormat, int pageIndex, Graphics g) {
		System.out
				.printf("printStringerTemplate() Page width: %f, page height: %f, orientation: %s, Margins x:%f, y: %f\n",
						pageFormat.getImageableWidth(),
						pageFormat.getImageableHeight(),
						pageFormat.getOrientation() == PageFormat.LANDSCAPE ? "Landscape"
								: "Portrait", pageFormat.getImageableX(),
						pageFormat.getImageableY());

		double width = pageFormat.getImageableWidth();
		double height = pageFormat.getImageableHeight();

		int widthInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getLength() / ((width / 72) * 2.54)) + 2;

		int heightInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getMaxRocker() / ((height / 72) * 2.54)) + 1;

		int xm = 0;
		int ym = 0;

		xm = (int) pageFormat.getImageableX();
		ym = (int) pageFormat.getImageableY();

		System.out.printf("Width: %f, Height: %f\n", width, height);

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ LanguageResource.getString("STRINGER_STR");
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 2);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 3);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			BezierBoardDrawUtil.printProfile(new JavaDraw(g2d), -width
					* (pageIndex % widthInPages), -height
					* (pageIndex / widthInPages), 72 / 2.54, 0.0, true,
					BoardCAD.getInstance().getCurrentBrd(), 0.0,
					mSkinThickness, false, mTailOffset, mNoseOffset);

			printStringerWebbing(new JavaDraw(g2d), -width
					* (pageIndex % widthInPages), -height
					* (pageIndex / widthInPages), 72 / 2.54, 0.0, BoardCAD
					.getInstance().getCurrentBrd(), mSkinThickness,
					mFrameThickness, mWebbing);

			printStringerTailPieceCutOut(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset);

			printStringerNosePieceCutOut(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset);

			return 0;
		}

		return -1;
	}

	int printRailTemplate(PageFormat pageFormat, int pageIndex, Graphics g) {
		int widthInPages = (int) ((BoardCAD.getInstance().getCurrentBrd()
				.getLength()
				- (mNoseOffset + mTailOffset) + 7.5) / ((pageFormat
				.getImageableWidth() / 72) * 2.54)) + 1;

		int heightInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getMaxRocker() / ((pageFormat.getImageableHeight() / 72) * 2.54)) + 1;

		int xm = (int) pageFormat.getImageableX();
		int ym = (int) pageFormat.getImageableY();

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ " " + LanguageResource.getString("PRINTHWSRAIL_STR");
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 2);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 3);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			BezierBoardDrawUtil.printRailTemplate(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0, true,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mTailOffset, mNoseOffset, false);

			printRailWebbing(new JavaDraw(g2d), -pageFormat.getImageableWidth()
					* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset);

			printRailNotching(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset);

			printRailNosePieceNotches(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset);

			printRailTailPieceNotches(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset);

			return 0;
		}

		return -1;
	}

	int printDeckSkinTemplate(PageFormat pageFormat, int pageIndex, Graphics g) {
		int widthInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getLength() / ((pageFormat.getImageableWidth() / 72) * 2.54)) + 2;

		int heightInPages = (int) ((BoardCAD.getInstance().getCurrentBrd()
				.getMaxWidth() / 2.0) / ((pageFormat.getImageableHeight() / 72) * 2.54)) + 1;

		int xm = (int) pageFormat.getImageableX();
		int ym = (int) pageFormat.getImageableY();

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ LanguageResource.getString("DECKSKINTEMPLATE_STR");
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 2);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 3);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			BezierBoardDrawUtil.printDeckSkinTemplate(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0, true,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail);

			return 0;
		}

		return -1;
	}

	int printBottomSkinTemplate(PageFormat pageFormat, int pageIndex, Graphics g) {
		int widthInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getLength() / ((pageFormat.getImageableWidth() / 72) * 2.54)) + 2;

		int heightInPages = (int) ((BoardCAD.getInstance().getCurrentBrd()
				.getMaxWidth() / 2.0) / ((pageFormat.getImageableHeight() / 72) * 2.54)) + 1;

		int xm = (int) pageFormat.getImageableX();
		int ym = (int) pageFormat.getImageableY();

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ LanguageResource.getString("BOTTOMSKIN_STR");
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 2);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 3);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			BezierBoardDrawUtil.printBottomSkinTemplate(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0, true,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail);

			return 0;
		}

		return -1;
	}

	int printCrosssectionTemplate(PageFormat pageFormat, int pageIndex,
			Graphics g) {
		int widthInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getWidthAt(mCrosssectionPos) / 2.0f / ((pageFormat
				.getImageableWidth() / 72) * 2.54)) + 1;

		int heightInPages = (int) ((BoardCAD.getInstance().getCurrentBrd()
				.getMaxWidth() / 2.0) / ((pageFormat.getImageableHeight() / 72) * 2.54)) + 1;

		int xm = (int) pageFormat.getImageableX();
		int ym = (int) pageFormat.getImageableY();

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ " " + LanguageResource.getString("CROSSECTION_STR");
			String mPosString = LanguageResource
					.getString("CROSSECTIONPOSITION_STR")
					+ " "
					+ UnitUtils.convertLengthToCurrentUnit(mCrosssectionPos,
							true);
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mPosString, xm, ym + (hgt + 2) * 2);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 3);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 4);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY() + UnitUtils.INCH * 72);

			printCrossSection(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos,
					mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing,
					false);

			BezierBoardDrawUtil.printCrossSection(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0, true,
					BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos);

			printCrossSectionWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos,
					mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing,
					false);

			return 0;
		}

		return -1;
	}

	int printNosePieceTemplate(PageFormat pageFormat, int pageIndex, Graphics g) {
		int widthInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getWidthAt(9.0 * UnitUtils.INCH - mNoseOffset) / 2.0f / ((pageFormat
				.getImageableWidth() / 72) * 2.54)) + 1;

		int heightInPages = (int) ((9.0 * UnitUtils.INCH - mNoseOffset / 2.0) / ((pageFormat
				.getImageableHeight() / 72) * 2.54)) + 1;

		int xm = (int) pageFormat.getImageableX();
		int ym = (int) pageFormat.getImageableY();

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ " " + LanguageResource.getString("NOSEPIECE_STR");
			String mPosString = LanguageResource
					.getString("NOSEPIECEPOSITION_STR")
					+ " "
					+ UnitUtils.convertLengthToCurrentUnit(mNoseOffset, true);
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mPosString, xm, ym + (hgt + 2) * 2);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 3);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 4);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY() + UnitUtils.INCH * 72);

			printNosePiece(new JavaDraw(g2d), -pageFormat.getImageableWidth()
					* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, false);

			printNosePiece(new JavaDraw(g2d), -pageFormat.getImageableWidth()
					* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, true);

			printNosePieceWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, false);

			printNosePieceWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, true);

			return 0;
		}

		return -1;
	}

	int printTailPieceTemplate(PageFormat pageFormat, int pageIndex, Graphics g) {

		int widthInPages = (int) (BoardCAD.getInstance().getCurrentBrd()
				.getWidthAt(9.0 * UnitUtils.INCH - mTailOffset) / 2.0f / ((pageFormat
				.getImageableWidth() / 72) * 2.54)) + 1;

		int heightInPages = (int) ((9.0 * UnitUtils.INCH - mTailOffset / 2.0) / ((pageFormat
				.getImageableHeight() / 72) * 2.54)) + 1;

		int xm = (int) pageFormat.getImageableX();
		int ym = (int) pageFormat.getImageableY();

		if (pageIndex < widthInPages * heightInPages) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(mPrintFontSmall);

			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();

			String mModelString = LanguageResource.getString("BOARDFILE_STR")
					+ BoardCAD.getInstance().getCurrentBrd().getFilename()
					+ " " + LanguageResource.getString("TAILPIECE_STR");
			String mPosString = LanguageResource
					.getString("TAILPIECEPOSITION_STR")
					+ " "
					+ UnitUtils.convertLengthToCurrentUnit(mTailOffset, true);
			String mRowString = LanguageResource.getString("ROW_STR")
					+ ((pageIndex % widthInPages) + 1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR")
					+ ((pageIndex / widthInPages) + 1) + "/" + heightInPages;

			g2d.setColor(Color.BLACK);
			// g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym + (hgt + 2) * 1);
			g.drawString(mPosString, xm, ym + (hgt + 2) * 2);
			g.drawString(mRowString, xm, ym + (hgt + 2) * 3);
			g.drawString(mColumnString, xm, ym + (hgt + 2) * 4);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY() + UnitUtils.INCH * 72);

			printTailPiece(new JavaDraw(g2d), -pageFormat.getImageableWidth()
					* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, false);

			printTailPiece(new JavaDraw(g2d), -pageFormat.getImageableWidth()
					* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, true);

			printTailPieceWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, false);

			printTailPieceWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()
							* (pageIndex % widthInPages),
					-pageFormat.getImageableHeight()
							* (pageIndex / widthInPages), 72 / 2.54, 0.0,
					BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail,
					mSkinThickness, mFrameThickness, mWebbing, mTailOffset,
					mNoseOffset, true);

			return 0;
		}

		return -1;
	}

	public static void printStringerWebbing(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double skinThickness, double frameThickness, double webbing) {
		if (brd.isEmpty()) {
			return;
		}

		System.out.printf("\nSTRINGER WEBBING\n");

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(0, 0, 0));

		GeneralPath path = new GeneralPath();

		double span = (UnitUtils.FOOT / 2.0) - webbing;
		int nrOfSteps = 3;// 20;
		double step = span / nrOfSteps;

		int nrOfHoles = 2 * (int) ((brd.getLength() / UnitUtils.FOOT)) - 1;
		if (nrOfHoles <= 0)
			return;

		// For each foot of board
		for (int i = 0; i != nrOfHoles; i++) {
			double x = UnitUtils.FOOT
					+ /* (UnitUtils.FOOT/2.0) */+(i * UnitUtils.FOOT / 2.0)
					- span / 2.0;

			double bottom;
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					x, 0.0, skinThickness);

			path.moveTo(x, deck - webbing);

			boolean first = true;
			int n = 0;
			for (; n < nrOfSteps - 1; n++) {
				x += step;
				deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x,
						0.0, skinThickness) - webbing;
				bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd,
						x, 0.0, skinThickness) + webbing;
				if (bottom > deck) {
					if (first) {
						path.moveTo(x, deck);
						continue;
					} else {
						x -= step;
						break;
					}
				}
				first = false;
				path.lineTo(x, deck);
			}

			for (; n >= 0; n--) {
				bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd,
						x, 0.0, skinThickness) + webbing;
				deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x,
						0.0, skinThickness) - webbing;
				if (bottom > deck) {
					break;
				}
				path.lineTo(x, bottom);
				x -= step;
			}
			if (path.getCurrentPoint() != null) // Test for empty path
			{
				path.closePath();
				d.draw(path);
			}

			path.reset();
		}

		d.setTransform(savedTransform);
	}

	public static void printCrossSection(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double position, double railDistance, double skinThickness,
			double frameThickness, double webbing, boolean mirror) {
		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation, mirror, false);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(0, 0, 0));

		System.out.printf("\nHWS CROSSSECTION at %f\n", position);

		GeneralPath path = new GeneralPath();

		double span = brd.getWidthAt(position) / 2.0f;

		double outlineAngle = Math.abs(brd.getOutline().getTangentAt(position));

		double sinOutline = Math.sin(outlineAngle);

		span -= railDistance / sinOutline;

		span -= frameThickness * 1.5;

		final int steps = 150;

		double x = position;

		// First the notch at stringer
		double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x,
				frameThickness / 2.0, skinThickness);

		path.moveTo(0.0, deck - webbing);
		path.lineTo(frameThickness / 2.0, deck - webbing);

		double y = 0;
		// Deck
		for (int i = 0; i <= steps; i++) {
			y = i * span / steps + (frameThickness / 2.0);

			// Find thickness and rocker at pos
			deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x, y,
					skinThickness);

			// System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n",
			// ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			// Draw lines
			path.lineTo(y, deck);
		}
		double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd,
				x, y, skinThickness);

		double thickness = deck - bottom;
		double notch = thickness / 4.0;
		if (notch < 0.5) {
			notch = 0.5;
		}
		double cutout = (thickness - notch) / 2.0;

		// Rail notch
		path.lineTo(y, deck - cutout);
		path.lineTo(y + frameThickness, deck - cutout);
		path.lineTo(y + frameThickness, deck - cutout - notch);
		path.lineTo(y, deck - cutout - notch);
		path.lineTo(y, bottom);

		// Bottom
		for (int i = 1; i <= steps; i++) {
			y = span - (i * span / steps) + (frameThickness / 2.0);

			// Find thickness and rocker at pos
			bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd, x,
					y, skinThickness);

			// System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n",
			// ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			// Draw lines
			path.lineTo(y, bottom);

		}

		bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd, x,
				frameThickness / 2.0, skinThickness);

		path.lineTo(frameThickness / 2.0, bottom + webbing);
		path.lineTo(0.0, bottom + webbing);

		d.draw(path);

		d.setTransform(savedTransform);

	}

	public static void printCrossSectionWebbing(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double position, double railDistance, double skinThickness,
			double frameThickness, double webbing, boolean mirror) {
		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation, mirror, false);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(0, 0, 0));

		System.out.printf("\nHWS CROSSSECTION WEBBING\n");

		GeneralPath path = new GeneralPath();

		double span = brd.getWidthAt(position) / 2.0f;

		double outlineAngle = Math.abs(brd.getOutline().getTangentAt(position));

		double sinOutline = Math.sin(outlineAngle);

		span -= railDistance / sinOutline;

		span -= frameThickness * 1.5;

		span -= webbing * 3.0;

		span /= 2.0;

		final int steps = 20;

		double step = span / steps;

		double x = position;

		for (int i = 0; i < 2; i++) {
			double y = (frameThickness / 2.0) + webbing
					+ (i * (span + webbing));

			double bottom;
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					x, y, skinThickness);

			path.moveTo(y, deck - webbing);

			boolean first = true;
			int n = 0;
			for (; n < steps - 1; n++) {
				y += step;
				deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x,
						y, skinThickness) - webbing;
				bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd,
						x, y, skinThickness) + webbing;
				if (bottom > deck) {
					if (first) {
						path.moveTo(y, deck);
						continue;
					} else {
						y -= step;
						break;
					}
				}
				first = false;
				path.lineTo(y, deck);
			}

			for (; n >= 0; n--) {
				bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd,
						x, y, skinThickness) + webbing;
				deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x,
						y, skinThickness) - webbing;
				if (bottom > deck) {
					break;
				}
				path.lineTo(y, bottom);
				y -= step;
			}
			path.closePath();
			d.draw(path);

			path.reset();

		}

		d.setTransform(savedTransform);

	}

	public static void printRailWebbing(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		/* d.setColor(new Color(0,0,255)); */

		// paintBezierControlPoints(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints,
		// false, false);

		// paintBezierControlPoints(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke,
		// brd.mBottomControlPoints, false, false);

		// paintBezierPath(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke, deck);

		// paintBezierPath(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke, bottom);

		BezierSpline outline = brd.getOutline();

		System.out.printf("\nRAIL WEBBING\n");

		boolean first = true;
		double lastPos = tailOffset;
		double lastX = 0;
		double lastY = 0;
		double lastDeck = 0;
		GeneralPath path = new GeneralPath();

		double ox = tailOffset;

		int steps;
		double span;
		double step;
		boolean noDraw = true;

		boolean end = false;

		// Calculate how many sections to do
		int sections = (((int) (brd.getLength() / UnitUtils.FOOT) - 1) * 5) + 2;

		for (int n = 0; n < sections; n++) {
			/*
			 * System.out.printf("section: %d pos: %s", n,
			 * UnitUtils.convertLengthToUnit(ox, true, UnitUtils.INCHES));
			 */

			if (n == 0) {
				// No cutouts in first section
				span = UnitUtils.FOOT - tailOffset;
				noDraw = true;
				System.out.printf(" first\n");
			} else {
				if (((n - 1) % 5) % 2 == 0) {
					span = webbing;
					noDraw = true;
					/* System.out.printf(" webbing\n"); */
				} else {
					span = UnitUtils.FOOT / 2.0 - webbing * 1.5;
					noDraw = false;
					/* System.out.printf(" hole\n"); */
				}
			}

			steps = (int) (span / 0.5);
			step = span / steps;

			boolean firstInSection = true;
			ArrayList<Point2D.Double> bottomCoordinates = new ArrayList<Point2D.Double>();
			for (int i = 0; i < steps; i++) {
				// Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);

				// Find the target point at distance from outline
				double x = ox - (distanceFromRail * Math.sin(normalAngle));
				double y = oy - (distanceFromRail * Math.cos(normalAngle));

				if (first && y < 0) {
					ox += step;
					continue;
				}

				if (!first && y < 0)
					break;

				// Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(
						brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil
						.getBottomWithSkinCompensation(brd, x, y, skinThickness);

				if (first && deck < bottom) {
					ox += step;
					continue;
				}

				if (!first && deck < bottom)
					break;

				if (first) {
					first = false;
				} else {

					deck -= webbing;
					bottom += webbing;

					// Find the 2D length from the last point on deck and bottom
					double xd = x - lastX;
					double yd = y - lastY;
					double stepLength = Math.sqrt((xd * xd) + (yd * yd));

					double newPos = lastPos + stepLength;

					if (deck < bottom) {
						if (!firstInSection) {
							end = true;
						}

					} else {
						if (!noDraw) {
							if (firstInSection) {
								path.moveTo(lastPos, lastDeck);
								bottomCoordinates.add(new Point2D.Double(
										lastPos, bottom));
								firstInSection = false;
							}

							path.lineTo(newPos, deck);
							bottomCoordinates.add(new Point2D.Double(newPos,
									bottom));
						}
					}

					// Update last pos
					lastPos = newPos;
				}

				lastX = x;
				lastY = y;
				lastDeck = deck;

				ox += step;
			}

			for (int i = bottomCoordinates.size() - 1; i >= 0; i--) {
				Point2D.Double point = bottomCoordinates.get(i);
				path.lineTo(point.x, point.y);
			}

			if (!noDraw && !first && !firstInSection) {
				path.closePath();

				d.draw(path);
			}

			if (end) {
				break;
			}
		}
		d.setTransform(savedTransform);

	}

	public static void printRailNotching(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		// d.setColor(new Color(255,0,0));

		// paintBezierControlPoints(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints,
		// false, false);

		// paintBezierControlPoints(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke,
		// brd.mBottomControlPoints, false, false);

		// paintBezierPath(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke, deck);

		// paintBezierPath(d,offsetX, offsetY, scale,
		// BoardCAD.getInstance().getBrdColor(), stroke, bottom);

		BezierSpline outline = brd.getOutline();

		System.out.printf("\nRAIL NOTCHING\n");

		boolean first = true;
		double lastPos = tailOffset;
		double lastX = 0;
		double lastY = 0;
		double lastDeck = 0;
		double lastBottom = 0;
		GeneralPath path = new GeneralPath();

		double ox = tailOffset;

		int steps;
		double span;
		double step;

		boolean end = false;

		// Calculate how many sections to do
		int sections = (int) (brd.getLength() / UnitUtils.FOOT) - 1;

		for (int n = 0; n < sections; n++) {
			if (n == 0) {
				// No cutouts in first section
				span = UnitUtils.FOOT - tailOffset;
			} else {
				span = UnitUtils.FOOT;
			}

			steps = (int) (span / 0.5);
			step = span / steps;

			for (int i = 0; i < steps; i++) {
				// Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);

				// Find the target point at distance from outline
				double x = ox - (distanceFromRail * Math.sin(normalAngle));
				double y = oy - (distanceFromRail * Math.cos(normalAngle));

				if (first && y < 0) {
					ox += step;
					continue;
				}

				if (!first && y < 0)
					break;

				// Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(
						brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil
						.getBottomWithSkinCompensation(brd, x, y, skinThickness);

				if (first && deck < bottom) {
					ox += step;
					continue;
				}

				if (!first && deck < bottom)
					break;

				if (first) {
					first = false;
				} else {
					// Find the 2D length from the last point on deck and bottom
					double xd = x - lastX;
					double yd = y - lastY;
					double stepLength = Math.sqrt((xd * xd) + (yd * yd));

					double newPos = lastPos + stepLength;

					// Update last pos
					lastPos = newPos;
				}

				lastX = x;
				lastY = y;
				lastDeck = deck;
				lastBottom = bottom;

				ox += step;
			}

			double thickness = lastDeck - lastBottom;
			double notch = thickness / 4.0;
			if (notch < 0.5) {
				notch = 0.5;
			}
			double cutout = (thickness - notch) / 2.0;

			path.moveTo(lastPos - frameThickness / 2.0, lastDeck - cutout);
			path.lineTo(lastPos + frameThickness / 2.0, lastDeck - cutout);
			path.lineTo(lastPos + frameThickness / 2.0, lastBottom + cutout);
			path.lineTo(lastPos - frameThickness / 2.0, lastBottom + cutout);

			path.closePath();

			d.draw(path);

			if (end) {
				break;
			}
		}
		d.setTransform(savedTransform);

	}

	public static void printTailPiece(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset, boolean mirror) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation, false, mirror);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(255, 0, 0));

		BezierSpline outline = brd.getOutline();

		System.out.printf("\nTAIL PIECE\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();

		double ox = tailOffset;
		double oy = 0.0;

		double tailPieceLength = 9.0 * UnitUtils.INCH - tailOffset;

		double span = tailPieceLength;

		double angle = getTailPieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);
		double x = 0.0;
		double y = 0.0;

		double normalAngle = 0.0;

		int steps = (int) (span / 0.25);
		double step = span / steps;

		for (int i = 0; i < steps; i++) {
			// Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);

			// Find the point at distance from outline
			double offsetFromRail = distanceFromRail + frameThickness
					+ ((i * 5 / steps) % 2 == 1 ? 0.0 : frameThickness);
			x = ox - offsetFromRail * Math.sin(normalAngle);
			y = oy - offsetFromRail * Math.cos(normalAngle);

			if (first && y < 0) {
				ox += step;
				continue;
			}

			// Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					x, y, skinThickness);
			double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(
					brd, x, y, skinThickness);

			if (first && deck < bottom) {
				ox += step;
				continue;
			}

			if (first) {
				first = false;
				path.moveTo(x / Math.cos(angle), 0.0);
			}
			path.lineTo(x / Math.cos(angle), y);

			ox += step;
		}

		path.lineTo(x / Math.cos(angle), frameThickness / 2.0);
		path.lineTo(x / Math.cos(angle) - 2.0 * UnitUtils.INCH,
				frameThickness / 2.0);
		path.lineTo(x / Math.cos(angle) - 2.0 * UnitUtils.INCH, 0.0);

		d.draw(path);

		System.out.printf("\nTAIL PIECE DONE\n");

		d.setTransform(savedTransform);
	}

	public static void printTailPieceWebbing(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset, boolean mirror) {
		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation, false, mirror);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(255, 0, 0));

		BezierSpline outline = brd.getOutline();

		System.out.printf("\nTAIL WEBBING\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();

		double tailPieceLength = 9.0 * UnitUtils.INCH - tailOffset;

		double span = (tailPieceLength - webbing * 3.0) / 2.0;

		double angle = getTailPieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);
		Point2D.Double point = null;

		int steps = (int) (span / 0.5);
		double step = span / steps;

		double comp = (webbing)
				* Math.sin(outline.getNormalAngle(9.0 * UnitUtils.INCH));

		double ox = tailOffset + webbing + comp;

		for (int n = 0; n < 2; n++) {
			boolean firstInSection = true;

			for (int i = 0; i < steps; i++) {
				// Use point from tail piece for end calculation
				point = BezierBoardDrawUtil.getOutline(brd, ox,
						distanceFromRail);

				if (first && point.y < 0) {
					ox += step;
					continue;
				}

				if (!first && point.y < 0)
					break;

				// Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(
						brd, point.x, point.y, skinThickness);
				double bottom = BezierBoardDrawUtil
						.getBottomWithSkinCompensation(brd, point.x, point.y,
								skinThickness);

				if (first && deck < bottom) {
					ox += step;
					continue;
				}

				if (!first && deck < bottom)
					break;

				if (first) {
					first = false;

				}

				// Get the webbing point
				point = BezierBoardDrawUtil.getOutline(brd, ox,
						distanceFromRail + webbing + frameThickness);

				if (firstInSection) {
					firstInSection = false;
					path.moveTo((point.x) / Math.cos(angle), webbing);
				}

				path.lineTo((point.x) / Math.cos(angle), point.y);

				ox += step;
			}

			if (!firstInSection) {
				path.lineTo((point.x) / Math.cos(angle), webbing);
				path.closePath();

				d.draw(path);
			}

			path.reset();

			ox += webbing;
		}

		d.setTransform(savedTransform);
	}

	public static void printStringerTailPieceCutOut(AbstractDraw d,
			double offsetX, double offsetY, double scale, double rotation,
			BezierBoard brd, double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {
		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);

		System.out.printf("\nSTRINGER TAIL PIECE CUTOUT\n");

		BezierSpline outline = brd.getOutline();

		GeneralPath path = new GeneralPath();

		double tailPieceLength = 9.0f * UnitUtils.INCH - tailOffset;

		double angle = getTailPieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);

		double thicknessXOffset = Math.sin(angle) * frameThickness / 2.0;
		double thicknessYOffset = Math.cos(angle) * frameThickness / 2.0;

		// Get the tail pos
		double tailX = getRailMinPos(brd, distanceFromRail, skinThickness,
				frameThickness, tailOffset, noseOffset);
		double tailDeck = BezierBoardDrawUtil.getRailDeck(brd, tailX,
				distanceFromRail, skinThickness);
		double tailBottom = BezierBoardDrawUtil.getRailBottom(brd, tailX,
				distanceFromRail, skinThickness);
		double tailZ = (tailDeck + tailBottom) / 2.0;
		double tailNormalAngle = outline.getNormalAngle(tailX);
		// Find the point at distance from outline
		tailX = tailX - (distanceFromRail * Math.sin(tailNormalAngle));

		// Get the front tail pos
		double tailPieceFrontX = 9.0 * UnitUtils.INCH;
		double tailPieceFrontDeck = BezierBoardDrawUtil.getRailDeck(brd,
				tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontBottom = BezierBoardDrawUtil.getRailBottom(brd,
				tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontZ = (tailPieceFrontDeck + tailPieceFrontBottom) / 2.0;
		double tailPieceFrontNormalAngle = outline
				.getNormalAngle(tailPieceFrontX);

		// Find the point at distance from outline
		tailPieceFrontX = tailPieceFrontX
				- (distanceFromRail * Math.sin(tailPieceFrontNormalAngle));

		double tailPieceCutoutX = tailPieceFrontX - (2.0 * UnitUtils.INCH);
		double tailPieceCutoutZ = tailPieceFrontZ - (2.0 * UnitUtils.INCH)
				/ Math.cos(angle) * Math.sin(angle);

		double tailEndX = tailOffset;
		double tailEndZ = tailPieceFrontZ - (tailPieceFrontX - tailEndX)
				* Math.sin(angle);

		path.moveTo(tailPieceCutoutX - thicknessXOffset, tailPieceCutoutZ
				+ thicknessYOffset);
		path.lineTo(tailEndX - thicknessXOffset, tailEndZ + thicknessYOffset);
		path.lineTo(tailEndX + thicknessXOffset, tailEndZ - thicknessYOffset);
		path.lineTo(tailPieceCutoutX + thicknessXOffset, tailPieceCutoutZ
				- thicknessYOffset);
		path.closePath();

		d.draw(path);
		path.reset();

		d.setTransform(savedTransform);
	}

	public static void printRailTailPieceNotches(AbstractDraw d,
			double offsetX, double offsetY, double scale, double rotation,
			BezierBoard brd, double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(255, 0, 0));

		GeneralPath path = new GeneralPath();

		double tailPieceLength = 9.0 * UnitUtils.INCH - tailOffset;

		double angle = getTailPieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);

		double thicknessXOffset = Math.sin(angle) * frameThickness / 2.0;
		double thicknessYOffset = Math.cos(angle) * frameThickness / 2.0;

		// Use same Y start pos reference as stringer cutout
		double tailPieceFrontX = 9.0 * UnitUtils.INCH;
		// Find the point at distance from outline
		double tailPieceFrontNormalAngle = brd.getOutline().getNormalAngle(
				tailPieceFrontX);
		tailPieceFrontX = tailPieceFrontX
				- (distanceFromRail * Math.sin(tailPieceFrontNormalAngle));
		double tailPieceFrontDeck = BezierBoardDrawUtil.getRailDeck(brd,
				tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontBottom = BezierBoardDrawUtil.getRailBottom(brd,
				tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontZ = (tailPieceFrontDeck + tailPieceFrontBottom) / 2.0;

		// Positions along board
		double firstStartX = tailOffset + (tailPieceLength / 5);
		double firstEndX = tailOffset + (tailPieceLength / 5) * 2;

		// Calculate Y for start and end
		double firstStartZ = tailPieceFrontZ - (tailPieceFrontX - firstStartX)
				* Math.tan(angle);
		double firstEndZ = tailPieceFrontZ - (tailPieceFrontX - firstEndX)
				* Math.tan(angle);

		// Calculate X along rail for start and end
		double firstStartRailX = getRailLength(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset, tailOffset,
				firstStartX);
		double firstEndRailX = getRailLength(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset, tailOffset,
				firstEndX);
		
		System.out.printf("tailOffset:%f firstStartX::%f firstStartRailX:%f", tailOffset, firstStartX, firstStartRailX);

		// Draw
		path.moveTo(firstStartRailX - thicknessXOffset + tailOffset, firstStartZ
				+ thicknessYOffset);
		path.lineTo(firstEndRailX - thicknessXOffset + tailOffset, firstEndZ
				+ thicknessYOffset);
		path.lineTo(firstEndRailX + thicknessXOffset + tailOffset, firstEndZ
				- thicknessYOffset);
		path.lineTo(firstStartRailX + thicknessXOffset + tailOffset, firstStartZ
				- thicknessYOffset);
		path.closePath();

		d.draw(path);

		path.reset();

		// Positions along board
		double secondStartX = tailOffset + (tailPieceLength / 5) * 3;
		double secondEndX = tailOffset + (tailPieceLength / 5) * 4;

		// Calculate Y for start and end
		double secondStartZ = tailPieceFrontZ
				- (tailPieceFrontX - secondStartX) * Math.tan(angle);
		double secondEndZ = tailPieceFrontZ - (tailPieceFrontX - secondEndX)
				* Math.tan(angle);

		// Calculate X along rail for start and end
		double secondStartRailX = getRailLength(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset, tailOffset,
				secondStartX);
		double secondEndRailX = getRailLength(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset, tailOffset,
				secondEndX);

		// Draw
		path.moveTo(secondStartRailX - thicknessXOffset + tailOffset, secondStartZ
				+ thicknessYOffset);
		path.lineTo(secondEndRailX - thicknessXOffset + tailOffset, secondEndZ
				+ thicknessYOffset);
		path.lineTo(secondEndRailX + thicknessXOffset + tailOffset, secondEndZ
				- thicknessYOffset);
		path.lineTo(secondStartRailX + thicknessXOffset + tailOffset, secondStartZ
				- thicknessYOffset);
		path.closePath();

		d.draw(path);

		d.setTransform(savedTransform);
	}

	public static void printNosePiece(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset, boolean mirror) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation, false, mirror);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		// d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();

		System.out.printf("\nNose PIECE\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();

		double nosePieceLength = 9.0 * UnitUtils.INCH - noseOffset;

		double startX = brd.getLength() - 9.0 * UnitUtils.INCH;
		double ox = startX;
		double oy = 0.0;

		double span = nosePieceLength;

		double angle = getNosePieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);

		double x = 0.0;
		double y = 0.0;

		double normalAngle = 0.0;

		int steps = (int) (span / 0.25);
		double step = span / steps;

		for (int i = 0; i < steps; i++) {
			// Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);

			// Find the point at distance from outline
			double offsetFromRail = distanceFromRail + frameThickness
					+ ((i * 5 / steps) % 2 == 1 ? 0.0 : frameThickness);
			x = ox - offsetFromRail * Math.sin(normalAngle);
			y = oy - offsetFromRail * Math.cos(normalAngle);

			System.out.printf(
					"i:%d ox:%f oy:%f offsetFromRail:%f x:%f y:%f \n", i, ox,
					oy, offsetFromRail, x, y);

			// Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					x, y, skinThickness);
			double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(
					brd, x, y, skinThickness);

			if (!first && deck < bottom) {
				break;
			}

			if (y < 0) {
				break;
			}

			if (first) {
				first = false;
				path.moveTo((x - startX) / Math.cos(angle) + 2.0
						* UnitUtils.INCH, 0.0);
				path.lineTo((x - startX) / Math.cos(angle) + 2.0
						* UnitUtils.INCH, frameThickness / 2.0);
				path.lineTo((x - startX) / Math.cos(angle),
						frameThickness / 2.0);
			}
			path.lineTo((x - startX) / Math.cos(angle), y);

			ox += step;
		}

		path.lineTo((x - startX) / Math.cos(angle), 0.0);

		d.draw(path);

		System.out.printf("\nNose PIECE DONE\n");

		d.setTransform(savedTransform);
	}

	public static void printNosePieceWebbing(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset, boolean mirror) {
		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation, false, mirror);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);

		System.out.printf("\nNose WEBBING\n");

		double nosePieceLength = 9.0 * UnitUtils.INCH - noseOffset;

		double span = (nosePieceLength - webbing * 3.0) / 2.0;

		double angle = getNosePieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);

		double positionOffset = brd.getLength() - 9.0 * UnitUtils.INCH;

		for (int n = 0; n < 2; n++) {

			double webbingStartPos = brd.getLength() - 9.0 * UnitUtils.INCH
					+ (webbing * (n + 1)) + (span * n);
			double webbingEndPos = webbingStartPos + span;

			printPieceWebbing(d, 0.0, 0.0, 1.0, 0.0, brd, distanceFromRail,
					skinThickness, frameThickness, webbing, tailOffset,
					noseOffset, mirror, positionOffset, webbingStartPos,
					webbingEndPos, angle);
		}

		d.setTransform(savedTransform);
	}

	public static void printPieceWebbing(AbstractDraw d, double offsetX,
			double offsetY, double scale, double rotation, BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset, boolean mirror, double positionOffset,
			double webbingStartPos, double webbingEndPos, double angle) {
		if (brd.isEmpty()) {
			return;
		}

		System.out.printf("\nWEBBING\n");

		GeneralPath path = new GeneralPath();

		double span = webbingEndPos - webbingStartPos;

		int steps = (int) (span / 0.5);
		double step = span / steps;

		double ox = webbingStartPos;

		double railOffset = distanceFromRail + frameThickness + webbing;

		Point2D.Double point = null;
		boolean first = true;
		for (int i = 0; i < steps; i++) {
			// Use point from nose piece for end calculation
			point = BezierBoardDrawUtil.getOutline(brd, ox, railOffset);

			System.out.printf("point(%f, %f)\n", point.x, point.y);

			if (point.y < webbing) {
				if (first)
					return; // Break out if there is somewhere without enough
							// width
				else
					break;
			}

			// Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					point.x, point.y, skinThickness);

			double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(
					brd, point.x, point.y, skinThickness);

			if (deck < bottom) {
				System.out.printf("Too thin, deck:%f, bottom:%f\n", deck,
						bottom);
				return; // Break out if there is somewhere without enough
						// thickness
			}

			if (first) {
				first = false;
				path.moveTo((point.x - positionOffset) / Math.cos(angle),
						webbing);
			}

			path.lineTo((point.x - positionOffset) / Math.cos(angle), point.y);

			ox += step;
		}

		path.lineTo((point.x - positionOffset) / Math.cos(angle), webbing);
		path.closePath();

		d.draw(path);

	}

	public static void printStringerNosePieceCutOut(AbstractDraw d,
			double offsetX, double offsetY, double scale, double rotation,
			BezierBoard brd, double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		BezierSpline outline = brd.getOutline();

		System.out.printf("\nSTRINGER NOSE PIECE CUTOUT\n");

		GeneralPath path = new GeneralPath();

		double nosePieceLength = 9.0 * UnitUtils.INCH - noseOffset;

		double angle = getNosePieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);

		double thicknessXOffset = Math.sin(angle) * frameThickness / 2.0;
		double thicknessYOffset = Math.cos(angle) * frameThickness / 2.0;

		// Get the nose pos
		double noseX = getRailMaxPos(brd, distanceFromRail, skinThickness,
				frameThickness, tailOffset, noseOffset);
		double noseDeck = BezierBoardDrawUtil.getRailDeck(brd, noseX,
				distanceFromRail, skinThickness);
		double noseBottom = BezierBoardDrawUtil.getRailBottom(brd, noseX,
				distanceFromRail, skinThickness);
		double noseZ = (noseDeck + noseBottom) / 2.0;
		double noseNormalAngle = outline.getNormalAngle(noseX);
		// Find the point at distance from outline
		noseX = noseX - (distanceFromRail * Math.sin(noseNormalAngle));

		// Get the nose piece rear end pos
		double nosePieceRearX = brd.getLength() - 9.0 * UnitUtils.INCH;
		double nosePieceRearDeck = BezierBoardDrawUtil.getRailDeck(brd,
				nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearBottom = BezierBoardDrawUtil.getRailBottom(brd,
				nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearZ = (nosePieceRearDeck + nosePieceRearBottom) / 2.0;
		double nosePieceRearNormalAngle = outline
				.getNormalAngle(nosePieceRearX);
		// Find the point at distance from outline
		nosePieceRearX = nosePieceRearX
				- (distanceFromRail * Math.sin(nosePieceRearNormalAngle));

		double nosePieceCutoutX = nosePieceRearX + (2.0 * UnitUtils.INCH);
		double nosePieceCutoutZ = nosePieceRearZ + (2.0 * UnitUtils.INCH)
				/ Math.cos(angle) * Math.sin(angle);

		double noseEndX = brd.getLength() - noseOffset;
		double noseEndZ = nosePieceRearZ + (noseEndX - nosePieceRearX)
				* Math.sin(angle);

		path.moveTo(nosePieceCutoutX - thicknessXOffset, nosePieceCutoutZ
				+ thicknessYOffset);
		path.lineTo(noseEndX - thicknessXOffset, noseEndZ + thicknessYOffset);
		path.lineTo(noseEndX + thicknessXOffset, noseEndZ - thicknessYOffset);
		path.lineTo(nosePieceCutoutX + thicknessXOffset, nosePieceCutoutZ
				- thicknessYOffset);
		path.closePath();

		d.draw(path);

		d.setTransform(savedTransform);
	}

	public static void printRailNosePieceNotches(AbstractDraw d,
			double offsetX, double offsetY, double scale, double rotation,
			BezierBoard brd, double distanceFromRail, double skinThickness,
			double frameThickness, double webbing, double tailOffset,
			double noseOffset) {

		if (brd.isEmpty()) {
			return;
		}

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d,
				offsetX, offsetY, scale, rotation);
		Stroke stroke = new BasicStroke((float) (2.0 / scale));
		d.setStroke(stroke);
		d.setColor(new Color(0, 0, 255));

		BezierSpline outline = brd.getOutline();

		System.out.printf("\nRAIL NOSE PIECE NOTCHING\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();

		double ox = tailOffset;

		double nosePieceLength = 9.0 * UnitUtils.INCH - noseOffset;

		double span;

		double angle = getNosePieceRailAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);
		double x = 0.0;

		double endPointX = 0.0;
		double endPointZ = 0.0;
		double endPointPos = 0.0;

		double lastX = 0.0;
		double lastY = 0.0;
		double lastPos = 0.0;

		double notchX = 0.0;
		double notchZ = 0.0;

		double thicknessXOffset = 0.0;
		double thicknessYOffset = 0.0;

		double deck = 0.0;
		boolean endFound = false;
		for (int n = 0; n < 6; n++) {
			if (nosePieceLength > 5.0 * UnitUtils.INCH) {
				switch (n) {
				case 0:
					span = brd.getLength() - 9.0 * UnitUtils.INCH - noseOffset;
					break;
				case 1:
				case 2:
				case 4:
				case 5:
					span = 1.0 * UnitUtils.INCH;
					break;
				default:
					span = nosePieceLength - 4.0 * UnitUtils.INCH;
					break;
				}
			} else {
				if (n == 0) {
					span = brd.getLength() - 9.0 * UnitUtils.INCH - noseOffset;
				} else {
					span = (n + 1) / 5.0 * nosePieceLength;
				}
			}

			int steps = (int) (span / 0.5);
			double step = span / steps;

			/*
			 * System.out.printf("n: %d, span: %f, steps: %d, step: %f\n", n,
			 * span, steps, step);
			 */

			double bottom = 0.0;
			for (int i = 0; i < steps; i++) {
				// Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);

				// Find the point at distance from outline
				x = ox - (distanceFromRail * Math.sin(normalAngle));
				double y = oy + (distanceFromRail * Math.cos(normalAngle));

				if (first && y < 0) {
					ox += step;
					continue;
				}

				if (!first && y < 0)
					break;

				// Find thickness and rocker at pos
				deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x,
						y, skinThickness);
				bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd,
						x, y, skinThickness);

				if (deck < bottom) {
					ox += step;
					continue;
				}

				if (deck < bottom) {
					break;
				}

				if (n > 0) {
					// Find stringer thickness and rocker at pos
					double deckAtStringer = BezierBoardDrawUtil
							.getDeckWithSkinCompensation(brd, ox, 0.0,
									skinThickness);
					double bottomAtStringer = BezierBoardDrawUtil
							.getBottomWithSkinCompensation(brd, ox, 0.0,
									skinThickness);

					if (deckAtStringer < bottomAtStringer) {
						// break;
					}
				}

				if (!endFound && n > 0) {
					double z1 = (bottom + deck) / 2.0;

					endPointX = ox;
					endPointZ = z1;
					endPointPos = lastPos;

					first = false;
					/*
					 * Line2D.Double point = new Line2D.Double();
					 * point.setLine(lastPos, endPointZ,lastPos, endPointZ);
					 * d.draw(point);
					 */
					thicknessXOffset = Math.sin(angle) * frameThickness / 2.0;
					thicknessYOffset = Math.cos(angle) * frameThickness / 2.0;

					endFound = true;
				}

				if (!first) {
					// Find the 2D length from the last point on deck and bottom
					double xd = x - lastX;
					double yd = y - lastY;
					double stepLength = Math.sqrt((xd * xd) + (yd * yd));

					double newPos = lastPos + stepLength;
					/*
					 * Line2D.Double line = new Line2D.Double();
					 * line.setLine(lastPos, bottom, newPos, bottom);
					 * d.draw(line);
					 */
					// Update last pos
					lastPos = newPos;
				} else {
					first = false;
				}
				/* Debug */

				lastX = x;
				lastY = y;

				ox += step;
			}

			/* System.out.printf("-------------------spanned: %f\n", ox-temp); */
			/*
			 * Line2D.Double line = new Line2D.Double(); line.setLine(lastPos,
			 * deck, lastPos, deck + 8.0); d.draw(line);
			 */
			System.out.printf("ox: %f, x: %f, lastPos: %f deck:%f bottom:%f\n",
					ox, x, lastPos, deck, bottom);

			// Check if we should store pos
			if (n % 2 == 1) {
				// Store for notching
				notchX = lastPos;
				notchZ = endPointZ
						+ (((ox - endPointX) / Math.cos(angle)) * Math
								.sin(angle));
				/*
				 * System.out.printf("notchX: %f, notchY: %f\n", notchX,
				 * notchZ);
				 */

				/*
				 * Line2D.Double line = new Line2D.Double();
				 * line.setLine(notchX, notchZ, notchX, notchZ); d.draw(line);
				 */} else if (n % 2 == 0 && n > 0) {
				double otherEndX = lastPos;
				double otherEndZ = endPointZ
						+ (((ox - endPointX) / Math.cos(angle)) * Math
								.sin(angle));
				/*
				 * System.out.printf("otherEndX: %f, otherEndZ: %f\n",
				 * otherEndX, otherEndZ);
				 */
				/*
				 * Line2D.Double line = new Line2D.Double();
				 * line.setLine(otherEndX, otherEndZ, otherEndX, otherEndZ);
				 * d.draw(line); path.moveTo(notchX, notchZ);
				 * path.lineTo(otherEndX, otherEndZ);
				 */
				d.setColor(new Color(255, 0, 0));
				path.moveTo(notchX - thicknessXOffset, notchZ
						+ thicknessYOffset);
				path.lineTo(otherEndX - thicknessXOffset, otherEndZ
						+ thicknessYOffset);
				path.lineTo(endPointX + otherEndX + thicknessXOffset
						- endPointX, otherEndZ - thicknessYOffset);
				path.lineTo(endPointX + notchX + thicknessXOffset - endPointX,
						notchZ - thicknessYOffset);

				System.out.printf(
						"notchX:%f notchZ:%f otherEndX:%f otherEndZ:%f\n",
						notchX, notchZ, otherEndX, otherEndZ);
				path.closePath();
				d.draw(path);

				path.reset();
			}

		}
		/*
		 * Line2D.Double line = new Line2D.Double(); d.setColor(new
		 * Color(0,255,0)); line.setLine(endPointPos, endPointZ, lastPos,
		 * deck+bottom/2); d.draw(line);
		 * 
		 * d.setColor(new Color(0,0,255)); line.setLine(endPointPos, endPointZ,
		 * lastPos, endPointZ +
		 * (((ox-step-endPointX)/Math.cos(angle))*Math.sin(angle)));
		 * d.draw(line);
		 */
		d.setTransform(savedTransform);
	}

	// helper functions
	static double getTailPieceStringerAngle(BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double tailOffset, double noseOffset) {

		BezierSpline outline = brd.getOutline();

		// Get the tail pos
		double railMin = getRailMinPos(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		System.out.printf("getTailPieceStringerAngle() railMin:%f\n", railMin);
		double tailDeck = BezierBoardDrawUtil.getRailDeck(brd, railMin, distanceFromRail, skinThickness);
		double tailBottom = BezierBoardDrawUtil.getRailBottom(brd, railMin, distanceFromRail, skinThickness);
		double tailZ = (tailDeck + tailBottom) / 2.0;
		double tailNormalAngle = outline.getNormalAngle(railMin);
		System.out.printf("getTailPieceStringerAngle() distanceFromRail:%f tailNormalAngle:%f\n", distanceFromRail, Math.toDegrees(tailNormalAngle));
		double tailX = railMin - (distanceFromRail * Math.sin(tailNormalAngle));
		System.out.printf("getTailPieceStringerAngle() tailDeck:%f tailBottom:%f\n", tailDeck, tailBottom);
		System.out.printf("getTailPieceStringerAngle() tailX:%f tailZ:%f\n", tailX, tailZ);
		
		// Get the second tail pos
		double tailPieceFrontX = 9.0 * UnitUtils.INCH;
		System.out.printf( "getTailPieceStringerAngle() tailPieceFrontX:%f\n", tailPieceFrontX);
		double tailPieceFrontDeck = BezierBoardDrawUtil.getRailDeck(brd, tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontBottom = BezierBoardDrawUtil.getRailBottom(brd, tailPieceFrontX, distanceFromRail, skinThickness);
		System.out.printf("getTailPieceStringerAngle() tailPieceFrontDeck:%f tailPieceFrontBottom:%f\n", tailPieceFrontDeck, tailPieceFrontBottom);

		
		double tailPieceFrontZ = (tailPieceFrontDeck + tailPieceFrontBottom) / 2.0;
		double tailPieceFrontNormalAngle = outline.getNormalAngle(tailPieceFrontX);
		// Find the point at distance from outline
		System.out.printf( "getTailPieceStringerAngle() tailPieceFrontNormalAngle:%f\n", Math.toDegrees(tailPieceFrontNormalAngle));
		tailPieceFrontX = tailPieceFrontX - (distanceFromRail * Math.sin(tailPieceFrontNormalAngle));
		System.out.printf( "getTailPieceStringerAngle() tailPieceFrontX:%f tailPieceFrontZ:%f\n", tailPieceFrontX, tailPieceFrontZ);

		// Calculate angle
		double h = tailPieceFrontZ - tailZ;
		double w = tailPieceFrontX - tailX;
		System.out.printf("getTailPieceStringerAngle() h:%f w:%f\n", h, w);
//		double angle = Math.atan2(tailPieceFrontZ - tailZ, tailPieceFrontX - tailX);
		double angle = Math.atan2(h, w);
		System.out.printf("getTailPieceStringerAngle() angle:%f\n", Math.toDegrees(angle));

		return angle;
	}

	static double getNosePieceStringerAngle(BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double tailOffset, double noseOffset) {

		BezierSpline outline = brd.getOutline();
		// Get the nose pos
		double noseX = getRailMaxPos(brd, distanceFromRail, skinThickness,
				frameThickness, tailOffset, noseOffset);
		double noseDeck = BezierBoardDrawUtil.getRailDeck(brd, noseX,
				distanceFromRail, skinThickness);
		double noseBottom = BezierBoardDrawUtil.getRailBottom(brd, noseX,
				distanceFromRail, skinThickness);
		double noseZ = (noseDeck + noseBottom) / 2.0;
		double noseNormalAngle = outline.getNormalAngle(noseX);
		noseX = noseX - (distanceFromRail * Math.sin(noseNormalAngle));

		// Get the nose piece rear end pos
		double nosePieceRearX = brd.getLength() - 9.0 * UnitUtils.INCH;
		double nosePieceRearDeck = BezierBoardDrawUtil.getRailDeck(brd,
				nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearBottom = BezierBoardDrawUtil.getRailBottom(brd,
				nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearZ = (nosePieceRearDeck + nosePieceRearBottom) / 2.0;
		double nosePieceRearNormalAngle = outline
				.getNormalAngle(nosePieceRearX);
		// Find the point at distance from outline
		nosePieceRearX = nosePieceRearX
				- (distanceFromRail * Math.sin(nosePieceRearNormalAngle));

		// Calculate angle
		double angle = Math.atan2(noseZ - nosePieceRearZ, noseX
				- nosePieceRearX);
		System.out.printf("\noseZ:%f\n", noseZ);
		System.out.printf("nosePieceRearZ:%f\n", nosePieceRearZ);
		System.out.printf("noseX:%f\n", noseX);
		System.out.printf("nosePieceRearX:%f\n", nosePieceRearX);
		System.out.printf("angle:%f\n", angle);

		return angle;

	}

	static double getNosePieceRailAngle(BezierBoard brd,
			double distanceFromRail, double skinThickness,
			double frameThickness, double tailOffset, double noseOffset) {
		double angle = getNosePieceStringerAngle(brd, distanceFromRail,
				skinThickness, frameThickness, tailOffset, noseOffset);

		System.out.printf("getNosePieceRailAngle() Angle: %f\n", angle * 180.0
				/ Math.PI);
		/*
		 * double endz = nosePieceRearZ +
		 * (((nosePieceRailLength)/Math.cos(angle))*Math.sin(angle));
		 * System.out.printf("getNosePieceRailAngle() endz: %f\n", endz);
		 */
		return angle;
	}

	static double getRailMinPos(BezierBoard brd, double distanceFromRail,
			double skinThickness, double frameThickness, double tailOffset,
			double noseOffset) {

		double ox = tailOffset;
		double oy = 0.0;

		double x = 0.0;
		double y = 0.0;

		double normalAngle = 0.0;

		double span = brd.getLength();

		int steps = (int) (span / 0.5);
		double step = span / steps;

		BezierSpline outline = brd.getOutline();

		for (int i = 0; i < steps; i++) {
			// Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);

			// Find the point at distance from outline
			x = ox - (distanceFromRail * Math.sin(normalAngle));
			y = oy - (distanceFromRail * Math.cos(normalAngle));

			if (y < 0) {
				ox += step;
				continue;
			}

			// Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					x, y, skinThickness);
			double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(
					brd, x, y, skinThickness);

			if (deck < bottom) {
				ox += step;
				continue;
			}

			break;
		}
		return ox;
	}

	static double getRailMaxPos(BezierBoard brd, double distanceFromRail,
			double skinThickness, double frameThickness, double tailOffset,
			double noseOffset) {
		double ox = brd.getLength() - noseOffset;
		double oy = 0.0;

		double x = 0.0;
		double y = 0.0;

		double normalAngle = 0.0;

		double span = brd.getLength() - noseOffset - tailOffset;

		int steps = (int) (span / 0.5);
		double step = span / steps;

		BezierSpline outline = brd.getOutline();

		for (int i = 0; i < steps; i++) {
			// Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);

			// Find the point at distance from outline
			x = ox - (distanceFromRail * Math.sin(normalAngle));
			y = oy - (distanceFromRail * Math.cos(normalAngle));

			if (y < 0) {
				ox -= step;
				continue;
			}

			// Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd,
					x, y, skinThickness);
			double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(
					brd, x, y, skinThickness);

			if (deck > bottom) {
				break;
			}

			ox -= step;
		}
		return ox;
	}

	static double getRailLength(BezierBoard brd, double distanceFromRail,
			double skinThickness, double frameThickness, double tailOffset,
			double noseOffset, double from, double to) {
		double ox = from;
		double oy = 0.0;

		double x = 0.0;
		double y = 0.0;

		double normalAngle = 0.0;

		double span = to - from;

		int steps = (int) (span / 0.5);
		double step = span / steps;

		BezierSpline outline = brd.getOutline();

		boolean first = true;
		double lastX = 0.0;
		double lastY = 0.0;
		double length = 0.0;
		for (int i = 0; i < steps; i++) {
			// Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);

			// Find the point at distance from outline
			x = ox - (distanceFromRail * Math.sin(normalAngle));
			y = oy - (distanceFromRail * Math.cos(normalAngle));

			if (!first) {
				double xd = x - lastX;
				double yd = y - lastY;
				double stepLength = Math.sqrt((xd * xd) + (yd * yd));

				double newLength = length + stepLength;

				// Update last pos
				length = newLength;
			} else {
				first = false;
			}

			lastX = x;
			lastY = y;

			ox += step;
		}
		return length;
	}

}
