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

import board.BezierBoard;

import cadcore.BezierSpline;
import cadcore.UnitUtils;
import boardcad.AbstractDraw;
import boardcad.i18n.LanguageResource;

import boardcad.gui.jdk.*;

public class PrintHollowWoodTemplates extends JComponent implements Printable {

	static final long serialVersionUID=1L;

	 private PageFormat myPageFormat;
	 private PrinterJob myPrintJob;

	protected enum PrintState {NO_STATE, 
		PRINT_DECKSKIN_TEMPLATE,				
		PRINT_BOTTOMSKIN_TEMPLATE,			
		PRINT_RAIL_TEMPLATE,
		PRINT_STRINGER_TEMPLATE,
		PRINT_CROSSSECTION_TEMPLATES,
		
		
		PRINT_NOSE_SECTION_TEMPLATE,
		PRINT_TAIL_SECTION_TEMPLATE
	};

	public PrintState mCurrentPrintState;
	
	private Font mPrintFontSmall = new Font("Dialog", Font.PLAIN, 10);

	private double mSkinThickness = 0.5;
	private double mFrameThickness = 0.5;
	private double mWebbing = 2.0;
	private double mDistanceToRail = 3.5;
	private double mTailOffset = 4.0;
	private double mNoseOffset = 4.0;
	private double mCrosssectionPos = 3.0;
	

	/** Creates and initializes the ClickMe component. */

	public PrintHollowWoodTemplates() {

//		Hint at good sizes for this component.

		setPreferredSize(new Dimension(800, 600));

		setMinimumSize(new Dimension(600, 480));

//		Request a black line around this component.

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

	}



	 public BezierBoard getBrd() {

		return BoardCAD.getInstance().getCurrentBrd();

	}
	 
	 void initPrint()
	 {
			myPrintJob = PrinterJob.getPrinterJob();

			myPageFormat = PrintBrd.getPageFormat(this, myPrintJob, BoardCAD.getInstance().getCurrentBrd().getMaxRocker());
			if(myPageFormat == null)
				return;
			
			
			myPrintJob.setPrintable(this,myPageFormat);		 
	 }

	 public void printStringerTemplate(double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) {

		 initPrint();

			try {

				mSkinThickness = skinThickness;
				mFrameThickness = frameThickness;
				mWebbing = webbing;
				mCurrentPrintState = PrintState.PRINT_STRINGER_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}

	 public void printRailTemplate(double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	 {
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

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}

	 	public void printDeckSkinTemplate(double distanceFromRail, double tailOffset, double noseOffset) {

			 initPrint();

			try {

				mDistanceToRail = distanceFromRail;
				mTailOffset = tailOffset;
				mNoseOffset = noseOffset;
				mCurrentPrintState = PrintState.PRINT_DECKSKIN_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}

	 	public void printBottomSkinTemplate(double distanceFromRail, double tailOffset, double noseOffset) {

			 initPrint();

			try {

				mDistanceToRail = distanceFromRail;
				mTailOffset = tailOffset;
				mNoseOffset = noseOffset;
				mCurrentPrintState = PrintState.PRINT_BOTTOMSKIN_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}
	 	
		public void printCrosssectionTemplates(double distanceFromRail, double skinThickness, double frameThickness, double webbing) {
		
			 initPrint();
		
				try {
		
					mDistanceToRail = distanceFromRail;
					mSkinThickness = skinThickness;
					mFrameThickness = frameThickness;
					mWebbing = webbing;
					mCurrentPrintState = PrintState.PRINT_CROSSSECTION_TEMPLATES;
		
					int nrOfCrossSections = (int)((BoardCAD.getInstance().getCurrentBrd().getLength() - 9.0*UnitUtils.INCH)/UnitUtils.FOOT);
					
					for(int i = 0; i < nrOfCrossSections; i++)
					{
						mCrosssectionPos = (i+1)* UnitUtils.FOOT;
						myPrintJob.print();
					}
	
				} catch(PrinterException pe) {
		
					System.out.println("Error printing: " + pe);
		
			}
		
		}

		public void printNoseTemplate(double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) {
				
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
		
				} catch(PrinterException pe) {
		
					System.out.println("Error printing: " + pe);
		
			}
		
		}

		public void printTailTemplate(double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) {
				
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
		
				} catch(PrinterException pe) {
		
					System.out.println("Error printing: " + pe);
		
			}
		
		}
		 /**

	 * Paints the PrintBrd component.  This method is

	 * invoked by the Swing component-painting system.

	 */

	public void paintComponent(Graphics g) {

		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();



//		Turn on antialiasing, so painting is smooth.

		g2d.setRenderingHint(

				RenderingHints.KEY_ANTIALIASING,

				RenderingHints.VALUE_ANTIALIAS_ON);



//		Paint the background.

		g2d.setColor(Color.WHITE);

		g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

		Dimension dim = getSize();

		double border = 10;

		if(BoardCAD.getInstance().getCurrentBrd() == null || BoardCAD.getInstance().getCurrentBrd().isEmpty())
			return;
		
		JavaDraw jd = new JavaDraw(g2d);

//		BezierBoardDrawUtil.printRailTemplate(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 5.0, 0.8, 8.0, 15.0, trues);
//		BezierBoardDrawUtil.printDeckSkinTemplate(jd, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 3.0);
//		BezierBoardDrawUtil.printBottomSkinTemplate(jd, border, dim.height*4.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 3.0);
//		BezierBoardDrawUtil.printCrossSection(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 50.0, 3.0, 0.8);
//		BezierBoardDrawUtil.printCrossSection(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 50.0);
//		BezierBoardDrawUtil.printProfile(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 0.0, 0.0, false);
//		BezierBoardDrawUtil.printProfile(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 5.0, 0.0, false);
//		BezierBoardDrawUtil.printProfile(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 20.0, 0.0, false);

		


	 	BezierBoardDrawUtil.printProfile(jd,
				border,
				dim.height*1.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), 0.0, mSkinThickness, false, mTailOffset, mNoseOffset);
/*
	 	BezierBoardDrawUtil.printProfile(jd,
				border,
				dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), 0.0, mSkinThickness, false, 0.0, 0.0);

	 	BezierBoardDrawUtil.printProfile(jd,
				border,
				dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), 0.0, 0.0, false, 0.0, 0.0);

	 	printStringerWebbing(jd,
				border,
				dim.height*1.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mSkinThickness, mFrameThickness, mWebbing);
	*/
		printStringerTailPieceCutOut(jd,
				border,
				dim.height*1.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

		printStringerNosePieceCutOut(jd,
				border,
				dim.height*1.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);
	
/*		printCrossSection(jd,
				border,
				dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), 3*UnitUtils.FOOT, 2*UnitUtils.INCH, mSkinThickness, mFrameThickness, mWebbing, false);
		
		printCrossSection(jd,
				border,
				dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), 3*UnitUtils.FOOT, 2*UnitUtils.INCH, mSkinThickness, mFrameThickness, mWebbing, true);

		printCrossSectionWebbing(jd,
				border,
				dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), 3*UnitUtils.FOOT, 2*UnitUtils.INCH, mSkinThickness, mFrameThickness, mWebbing, false);

		printCrossSectionWebbing(jd,
				border,
				dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), 3*UnitUtils.FOOT, 2*UnitUtils.INCH, mSkinThickness, mFrameThickness, mWebbing, true);


		
		BezierBoardDrawUtil.printRailTemplate(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mTailOffset, mNoseOffset, false);

		printRailWebbing(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);
		
		printRailNotching(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

		printRailTailPieceNotches(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

		printRailNosePieceNotches(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);
		
		
		printTailPiece(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, false);

		printTailPiece(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, true);
		
		printTailPieceWebbing(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, false);

		printTailPieceWebbing(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, true);
		
		
*/		
		printNosePiece(jd,
				border,
				dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset, false);
/*
		printNosePiece(jd,
				border,
				dim.height*3.0/5.0, 3*(dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mNoseOffset, true);

		printNosePieceWebbing(jd,
				border,
				dim.height*3.0/5.0, 3*(dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mNoseOffset, false);

		printNosePieceWebbing(jd,
				border,
				dim.height*3.0/5.0, 3*(dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mNoseOffset, true);
				*/
}



	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {


	
			/*DEBUG!!!
	
	if(pageIndex >=2)
	
	{
	
	isPrintingProfile = false;
	
	isPrintingOutline = false;
	
	return NO_SUCH_PAGE;
	
	}
	
			 */
		
		switch(mCurrentPrintState)
		{
		
			case PRINT_STRINGER_TEMPLATE:
			{
				if(printStringerTemplate(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;		
		
				break;
			}
			
			case PRINT_RAIL_TEMPLATE:
			{
				if(printRailTemplate(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;		
		
				break;
			}

			case PRINT_DECKSKIN_TEMPLATE:
			{
				if(printDeckSkinTemplate(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;		
		
				break;
			}

			case PRINT_BOTTOMSKIN_TEMPLATE:
			{
				if(printBottomSkinTemplate(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;		
		
				break;
			}
			case PRINT_CROSSSECTION_TEMPLATES:
			{
				if(printCrosssectionTemplate(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;		
		
				break;
			}
		}
	
		return NO_SUCH_PAGE;
	
	}
	
	int printStringerTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		System.out.printf("printStringerTemplate() Page width: %f, page height: %f, orientation: %s, Margins x:%f, y: %f\n", pageFormat.getImageableWidth(), pageFormat.getImageableHeight(), pageFormat.getOrientation() == PageFormat.LANDSCAPE?"Landscape":"Portrait", pageFormat.getImageableX(), pageFormat.getImageableY());

		double width = pageFormat.getImageableWidth();
		double height = pageFormat.getImageableHeight();
		
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLength()
				/ ((width/72)*2.54) ) + 2;
		
		int heightInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getMaxRocker()
				/ ((height/72)*2.54)) + 1;
		
		int xm = 0;
		int ym = 0;

		xm = (int)pageFormat.getImageableX();
		ym = (int)pageFormat.getImageableY();			

		System.out.printf("Width: %f, Height: %f\n", width, height);
		
		if (pageIndex < widthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontSmall);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("STRINGER_STR");
			String mRowString = LanguageResource.getString("ROW_STR")+ ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
			BezierBoardDrawUtil.printProfile(new JavaDraw(g2d),
					-width*(pageIndex%widthInPages),
					-height*(pageIndex/widthInPages), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(), 0.0, mSkinThickness, false, mTailOffset, mNoseOffset);

			printStringerWebbing(new JavaDraw(g2d),
					-width*(pageIndex%widthInPages),
					-height*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mSkinThickness, mFrameThickness, mWebbing);
			
			printStringerTailPieceCutOut(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

			printStringerNosePieceCutOut(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);
			
			return 0;
		}
		
		return -1;
	}

	int printRailTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)((BoardCAD.getInstance().getCurrentBrd().getLength() - (mNoseOffset + mTailOffset) + 7.5)
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 1;
		
		int heightInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getMaxRocker()
				/ ((pageFormat.getImageableHeight()/72)*2.54)) + 1;
		
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
		
		if (pageIndex < widthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontSmall);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + " "+ LanguageResource.getString("PRINTHWSRAIL_STR");
			String mRowString = LanguageResource.getString("ROW_STR") + ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				
			BezierBoardDrawUtil.printRailTemplate(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mTailOffset, mNoseOffset, false);

			printRailWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

			printRailNotching(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

			printRailNosePieceNotches(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

			printRailTailPieceNotches(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, mTailOffset, mNoseOffset);

			return 0;
		}
		
		return -1;
	}

	int printDeckSkinTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLength()
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 2;
		
		int heightInPages = (int)((BoardCAD.getInstance().getCurrentBrd().getMaxWidth()/2.0)
				/ ((pageFormat.getImageableHeight()/72)*2.54)) + 1;
		
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
		
		if (pageIndex < widthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontSmall);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString =LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("DECKSKINTEMPLATE_STR");
			String mRowString = LanguageResource.getString("ROW_STR") + ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
		
		
			BezierBoardDrawUtil.printDeckSkinTemplate(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail);
			
			return 0;
		}
		
		return -1;
	}
	
	int printBottomSkinTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLength()
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 2;
		
		int heightInPages = (int)((BoardCAD.getInstance().getCurrentBrd().getMaxWidth()/2.0)
				/ ((pageFormat.getImageableHeight()/72)*2.54)) + 1;
		
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
		
		if (pageIndex < widthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontSmall);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("BOTTOMSKIN_STR");
			String mRowString = LanguageResource.getString("ROW_STR")+ ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
		
		
			BezierBoardDrawUtil.printBottomSkinTemplate(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail);
			
			return 0;
		}
		
		return -1;
	}

	int printCrosssectionTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getWidthAt(mCrosssectionPos)/2.0f
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 1;
		
		int heightInPages = (int)((BoardCAD.getInstance().getCurrentBrd().getMaxWidth()/2.0)
				/ ((pageFormat.getImageableHeight()/72)*2.54)) + 1;
		
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
		
		if (pageIndex < widthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontSmall);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontSmall);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + " " +LanguageResource.getString("CROSSECTION_STR");
			String mPosString = LanguageResource.getString("CROSSECTIONPOSITION_STR") + " " + UnitUtils.convertLengthToCurrentUnit(mCrosssectionPos, true); 
			String mRowString = LanguageResource.getString("ROW_STR")+ ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mPosString, xm, ym+(hgt+2)*2);
			g.drawString(mRowString, xm, ym+(hgt+2)*3);
			g.drawString(mColumnString, xm, ym+(hgt+2)*4);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY() + UnitUtils.INCH*72);
		
			printCrossSection(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos, mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, false);
		
			BezierBoardDrawUtil.printCrossSection(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos);
			
			printCrossSectionWebbing(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos, mDistanceToRail, mSkinThickness, mFrameThickness, mWebbing, false);

			return 0;
		}
		
		return -1;
	}
	
	public static void printStringerWebbing(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double skinThickness, double frameThickness, double webbing)
	{	
		if(brd.isEmpty()) {
			return;	
		}
		
		System.out.printf("\nSTRINGER WEBBING\n");

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));
			
		GeneralPath path = new GeneralPath();
				
		double span = (UnitUtils.FOOT/2.0) - webbing;
		int nrOfSteps = 20;
		double step = span/nrOfSteps;
		
		int nrOfHoles = 2*(int)((brd.getLength()/UnitUtils.FOOT) );
		
		//For each foot of board
		for(int i = 0; i != nrOfHoles; i++)
		{
			double x = (UnitUtils.FOOT/2.0) + (i*UnitUtils.FOOT/2.0) - span/2.0;

			double bottom;
			double deck = BezierBoardDrawUtil.getDeck(brd, x, 0.0, skinThickness);

			path.moveTo(x, deck - webbing);

			boolean first = true;
			int n = 0;
			for(; n < nrOfSteps-1; n++)
			{
				x += step;
				deck = BezierBoardDrawUtil.getDeck(brd, x, 0.0, skinThickness) - webbing;
				bottom = BezierBoardDrawUtil.getBottom(brd, x, 0.0, skinThickness) + webbing;
				if(bottom > deck)
				{
					if(first){
						path.moveTo(x,deck);
						continue;												
					}
					else
					{
						x -= step;
						break;
					}
				}
				first = false;
				path.lineTo(x,deck);
			}
		
			for(; n >= 0; n--)
			{
				bottom = BezierBoardDrawUtil.getBottom(brd, x, 0.0, skinThickness) + webbing;
				deck = BezierBoardDrawUtil.getDeck(brd, x, 0.0, skinThickness) - webbing;
				if(bottom > deck)
				{
					break;
				}
				path.lineTo(x,bottom);
				x -= step;
			}
			path.closePath();
			d.draw(path);
			
			path.reset();
		}

		d.setTransform(savedTransform);		
	}

	public static void printCrossSection(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double position, double railDistance, double skinThickness, double frameThickness, double webbing, boolean mirror)
	{	
		if(brd.isEmpty()) {
			return;	
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, mirror, false);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));
			
		System.out.printf("\nHWS CROSSSECTION\n");

		GeneralPath path = new GeneralPath();

		double span = brd.getWidthAt(position)/2.0f;
		
		double outlineAngle = Math.abs(brd.getOutline().getTangentAt(position));
		
		double sinOutline = Math.sin(outlineAngle);
		
		span -= railDistance/sinOutline;
		
		span -= frameThickness*1.5;

		final int steps = 150;
		
		double x = position;
		
		//First the notch at stringer
		double deck = BezierBoardDrawUtil.getDeck(brd, x, frameThickness/2.0, skinThickness);
		
		path.moveTo(0.0, deck - webbing);
		path.lineTo(frameThickness/2.0, deck - webbing);
		
		double y = 0;
		//Deck
		for(int i = 0; i <= steps; i++)
		{
			y = i* span / steps + (frameThickness/2.0);
			
			//Find thickness and rocker at pos
			deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);

	//		System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			//Draw lines
			path.lineTo(y, deck);			
		}
		double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);
		
		double thickness = deck - bottom;
		double notch = thickness / 4.0;
		if(notch < 0.5)
		{
			notch = 0.5;
		}
		double cutout = (thickness - notch)/2.0;
		
		//Rail notch
		path.lineTo(y, deck - cutout);
		path.lineTo(y + frameThickness, deck - cutout);
		path.lineTo(y + frameThickness, deck - cutout - notch);
		path.lineTo(y, deck - cutout - notch);
		path.lineTo(y, bottom);
				
		//Bottom
		for(int i = 1; i <= steps; i++)
		{
			y = span - (i* span / steps) + (frameThickness/2.0);
			
			//Find thickness and rocker at pos
			bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);
				
	//		System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			//Draw lines
			path.lineTo(y, bottom);

		}

		bottom = BezierBoardDrawUtil.getBottom(brd, x, frameThickness/2.0, skinThickness);

		path.lineTo(frameThickness/2.0, bottom + webbing);
		path.lineTo(0.0, bottom + webbing);
		
		d.draw(path);
		
		d.setTransform(savedTransform);
		
	}

	public static void printCrossSectionWebbing(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double position, double railDistance, double skinThickness, double frameThickness, double webbing, boolean mirror)
	{	
		if(brd.isEmpty()) {
			return;	
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, mirror, false);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));
			
		System.out.printf("\nHWS CROSSSECTION WEBBING\n");

		GeneralPath path = new GeneralPath();

		double span = brd.getWidthAt(position)/2.0f;
		
		double outlineAngle = Math.abs(brd.getOutline().getTangentAt(position));
		
		double sinOutline = Math.sin(outlineAngle);
		
		span -= railDistance/sinOutline;
		
		span -= frameThickness*1.5;
		
		span -= webbing*3.0;
		
		span /= 2.0;

		final int steps = 20;
		
		double step = span/steps;

		
		
		double x = position;

		
		for(int i = 0; i < 2; i++)
		{
			double y = (frameThickness/2.0) + webbing + (i*(span + webbing));
		
			double bottom;
			double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);

			path.moveTo(y, deck - webbing);

			boolean first = true;
			int n = 0;
			for(; n < steps-1; n++)
			{
				y += step;
				deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness) - webbing;
				bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness) + webbing;
				if(bottom > deck)
				{
					if(first){
						path.moveTo(y, deck);
						continue;												
					}
					else
					{
						y -= step;
						break;
					}
				}
				first = false;
				path.lineTo(y,deck);
			}
		
			for(; n >= 0; n--)
			{
				bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness) + webbing;
				deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness) - webbing;
				if(bottom > deck)
				{
					break;
				}
				path.lineTo(y,bottom);
				y -= step;
			}
			path.closePath();
			d.draw(path);
			
			path.reset();
			
			
		}
		
		d.setTransform(savedTransform);
		
	}


	public static void printRailWebbing(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
/*		d.setColor(new Color(0,0,255));*/

	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);
	
	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);
	
	//	paintBezierPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck);
	
	//	paintBezierPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom);
	
		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nRAIL WEBBING\n");

		boolean first = true;
		double lastPos = 0;
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
		
		//Calculate how many sections to do
		int sections = (((int)(brd.getLength()/UnitUtils.FOOT)-1) * 5) + 2;
		
		for(int n = 0; n < sections; n++)
		{
/*			System.out.printf("section: %d pos: %s", n, UnitUtils.convertLengthToUnit(ox, true, UnitUtils.INCHES));*/
			
			if(n == 0)
			{
				//No cutouts in first section
				span = UnitUtils.FOOT - tailOffset;
				noDraw = true;
				System.out.printf(" first\n");
			}
			else
			{
				if(((n-1)%5)%2 == 0)
				{
					span = webbing;
					noDraw = true;
/*					System.out.printf(" webbing\n");*/
				}
				else
				{
					span = UnitUtils.FOOT/ 2.0 - webbing*1.5; 
					noDraw = false;
/*					System.out.printf(" hole\n");*/
				}
			}
		
			steps = (int)(span/0.5);
			step = span/steps;

			boolean firstInSection = true;
			ArrayList<Point2D.Double> bottomCoordinates = new ArrayList<Point2D.Double>();
			for(int i = 0; i < steps; i++)
			{
				//Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);
				
				//Find the target point at distance from outline
				double x = ox - (distanceFromRail*Math.sin(normalAngle));
				double y = oy - (distanceFromRail*Math.cos(normalAngle));

				if(first && y < 0)
				{
					ox += step;
					continue;
				}

				if(!first && y < 0)
					break;

				//Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);

				if(first && deck < bottom)
				{
					ox += step;
					continue;
				}

				if(!first && deck < bottom)
					break;


				if(first)
				{
					first = false;
				}
				else
				{

					deck -= webbing;
					bottom += webbing;
					
					//Find the 2D length from the last point on deck and bottom
					double xd = x-lastX;
					double yd = y-lastY;
					double stepLength = Math.sqrt((xd*xd)+(yd*yd));
					
					double newPos = lastPos + stepLength;
					
					if(deck < bottom)
					{
						if(!firstInSection)
						{
							end = true;
						}
						
					}
					else
					{
						if(!noDraw)
						{
							if(firstInSection)
							{
								path.moveTo(lastPos, lastDeck);
								bottomCoordinates.add(new Point2D.Double(lastPos, bottom));
								firstInSection = false;
							}
					
							path.lineTo(newPos, deck);
							bottomCoordinates.add(new Point2D.Double(newPos, bottom));
						}
					}
					
					//Update last pos
					lastPos = newPos;
				}

				lastX = x;
				lastY = y;
				lastDeck = deck;
				
				ox += step;
			}

			for(int i = bottomCoordinates.size()-1 ; i >= 0; i--)
			{
				Point2D.Double point = bottomCoordinates.get(i);
				path.lineTo(point.x, point.y);
			}

			if(!noDraw && !first && !firstInSection)
			{
				path.closePath();
				
				d.draw(path);
			}

			if(end)
			{
				break;
			}
		}
		d.setTransform(savedTransform);
		
	}


	public static void printRailNotching(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
	//	d.setColor(new Color(255,0,0));

	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);
	
	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);
	
	//	paintBezierPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck);
	
	//	paintBezierPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom);
	
		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nRAIL NOTCHING\n");

		boolean first = true;
		double lastPos = 0;
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
		
		//Calculate how many sections to do
		int sections = (int)(brd.getLength()/UnitUtils.FOOT)-1;
		
		for(int n = 0; n < sections; n++)
		{
			if(n == 0)
			{
				//No cutouts in first section
				span = UnitUtils.FOOT - tailOffset;
			}
			else
			{
				span = UnitUtils.FOOT; 
			}
		
			steps = (int)(span/0.5);
			step = span/steps;

			for(int i = 0; i < steps; i++)
			{
				//Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);
				
				//Find the target point at distance from outline
				double x = ox - (distanceFromRail*Math.sin(normalAngle));
				double y = oy - (distanceFromRail*Math.cos(normalAngle));

				if(first && y < 0)
				{
					ox += step;
					continue;
				}

				if(!first && y < 0)
					break;

				//Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);

				if(first && deck < bottom)
				{
					ox += step;
					continue;
				}

				if(!first && deck < bottom)
					break;

				if(first)
				{
					first = false;
				}
				else
				{					
					//Find the 2D length from the last point on deck and bottom
					double xd = x-lastX;
					double yd = y-lastY;
					double stepLength = Math.sqrt((xd*xd)+(yd*yd));
					
					double newPos = lastPos + stepLength;
										
					//Update last pos
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
			if(notch < 0.5)
			{
				notch = 0.5;
			}
			double cutout = (thickness - notch)/2.0;

			path.moveTo(lastPos - frameThickness/2.0, lastDeck-cutout);
			path.lineTo(lastPos + frameThickness/2.0, lastDeck-cutout);
			path.lineTo(lastPos + frameThickness/2.0, lastBottom+cutout);
			path.lineTo(lastPos - frameThickness/2.0, lastBottom+cutout);
			
			path.closePath();
			
			d.draw(path);

			if(end)
			{
				break;
			}
		}
		d.setTransform(savedTransform);
		
	}

	public static void printTailPiece(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset, boolean mirror) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, false, mirror);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nTAIL PIECE\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();
		
		double ox = tailOffset;
		double oy = 0.0;
		
		double tailPieceLength = 9.0*UnitUtils.INCH - tailOffset;						

		double span;						

		double angle = getTailPieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double x = 0.0;
		double y = 0.0;
		
		double normalAngle = 0.0;
		
		double xf = 0.0;
		
		for(int n = 0; n < 5; n++)
		{
			if(tailPieceLength > 5.0*UnitUtils.INCH)
			{
				switch(n)
				{
				case 0:
				case 1:
				case 3:
				case 4:
					span = 1.0*UnitUtils.INCH;
					break;
				default:
					span = tailPieceLength - 4.0*UnitUtils.INCH;
					break;
				}
			}
			else
			{
				span = (n+1)/5.0*tailPieceLength;
			}
			
			int	steps = (int)(span/0.5);
			double step = span/steps;

			for(int i = 0; i < steps; i++)
			{
				//Get the outline point and the angle
				oy = outline.getValueAt(ox);
				normalAngle = outline.getNormalAngle(ox);
				
				//Find the point at distance from outline
				x = ox - ((distanceFromRail - ((n%2==1)?frameThickness:0.0) )*Math.sin(normalAngle));
				y = oy - ((distanceFromRail - ((n%2==1)?frameThickness:0.0) )*Math.cos(normalAngle));

				if(first && y < 0)
				{
					ox += step;
					continue;
				}

				if(!first && y < 0)
					break;

				//Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);

				if(first && deck < bottom)
				{
					ox += step;
					continue;
				}

				if(!first && deck < bottom)
					break;

				if(first)
				{					
					first = false;

					xf = x;
					
					path.moveTo((x-xf)/Math.cos(angle), 0.0);
				}
				
				path.lineTo((x-xf)/Math.cos(angle), y);
				
				ox += step;
			}
			if(n<4)
			{
			ox -= step;
			x = ox - ((distanceFromRail - ((n%2==0)?frameThickness:0.0) )*Math.sin(normalAngle));
			y = oy - ((distanceFromRail - ((n%2==0)?frameThickness:0.0) )*Math.cos(normalAngle));
			path.lineTo((x-xf)/Math.cos(angle), y);			
			ox += step;
			}
		}
		
		path.lineTo((x-xf)/Math.cos(angle), frameThickness/2.0);
		path.lineTo((x-xf)/Math.cos(angle) - 2.0*UnitUtils.INCH, frameThickness/2.0);
		path.lineTo((x-xf)/Math.cos(angle) - 2.0*UnitUtils.INCH, 0.0);
		
		d.draw(path);

		System.out.printf("\nTAIL PIECE\n");

		
		d.setTransform(savedTransform);
	}
	
	public static void printTailPieceWebbing(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset, boolean mirror) 
	{
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, false, mirror);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nTAIL WEBBING\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();
				
		double tailPieceLength = 9.0*UnitUtils.INCH - tailOffset;						

		double span = (tailPieceLength - webbing*5.0)/2.0;						

		double angle = getTailPieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		Point2D.Double point = null;
		
		int	steps = (int)(span/0.5);
		double step = span/steps;

		double comp = (webbing)*Math.sin(outline.getNormalAngle(9.0*UnitUtils.INCH));

		double ox = tailOffset + webbing*2.0 + comp;
		
		double xf = 0.0;

		for(int n = 0; n < 2; n++)
		{			
			boolean firstInSection = true;

			for(int i = 0; i < steps; i++)
			{
				//Use point from tail piece for end calculation
				point = BezierBoardDrawUtil.getOutline(brd, ox, distanceFromRail);

				if(first && point.y < 0)
				{
					ox += step;
					continue;
				}

				if(!first && point.y < 0)
					break;

				//Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeck(brd, point.x, point.y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, point.x, point.y, skinThickness);

				if(first && deck < bottom)
				{
					ox += step;
					continue;
				}

				if(!first && deck < bottom)
					break;

				if(first)
				{
					first = false;
										
					xf = point.x - webbing*2.0 - comp;
					
				}
			
				//Get the webbing point
				point = BezierBoardDrawUtil.getOutline(brd, ox, distanceFromRail + webbing);

				if(firstInSection)
				{
					firstInSection = false;
					path.moveTo((point.x - xf)/Math.cos(angle), webbing);
				}
				
				path.lineTo((point.x - xf)/Math.cos(angle), point.y);
				
				ox += step;
			}

			path.lineTo((point.x - xf)/Math.cos(angle), webbing);
			path.closePath();
			
			d.draw(path);
			
			path.reset();
			
			ox += webbing;
		}
		
		
		d.setTransform(savedTransform);
	}

	public static void printStringerTailPieceCutOut(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	{
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));
		
		System.out.printf("\nSTRINGER TAIL PIECE CUTOUT\n");

		BezierSpline outline = brd.getOutline();
		
		GeneralPath path = new GeneralPath();
		
		double tailPieceLength = 9.0f*UnitUtils.INCH - tailOffset;
		
		double angle = getTailPieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);

		double thicknessXOffset = Math.sin(angle)*frameThickness/2.0;
		double thicknessYOffset = Math.cos(angle)*frameThickness/2.0;
		
		//Get the tail pos
		double tailX = getRailMinPos(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double tailDeck = BezierBoardDrawUtil.getRailDeck(brd, tailX, distanceFromRail, skinThickness);
		double tailBottom = BezierBoardDrawUtil.getRailBottom(brd, tailX, distanceFromRail, skinThickness);
		double tailZ = (tailDeck +tailBottom)/2.0;
		double tailNormalAngle = outline.getNormalAngle(tailX);		
		//Find the point at distance from outline
		tailX = tailX - (distanceFromRail *Math.sin(tailNormalAngle));
		
		//Get the second tail pos
		double tailPieceFrontX = 9.0*UnitUtils.INCH;
		double tailPieceFrontDeck = BezierBoardDrawUtil.getRailDeck(brd, tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontBottom = BezierBoardDrawUtil.getRailBottom(brd, tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontZ = (tailPieceFrontDeck +tailPieceFrontBottom)/2.0;
		double tailPieceFrontNormalAngle = outline.getNormalAngle(tailPieceFrontX);		
		//Find the point at distance from outline
		tailPieceFrontX = tailPieceFrontX - (distanceFromRail *Math.sin(tailPieceFrontNormalAngle));
			
		double tailPieceCutoutX = tailPieceFrontX - (2.0*UnitUtils.INCH);
		double tailPieceCutoutZ = tailPieceFrontZ - (2.0*UnitUtils.INCH)/Math.cos(angle)*Math.sin(angle);

		double tailEndX = tailPieceFrontX - tailPieceLength;
		double tailEndZ = tailPieceFrontZ - (tailPieceLength)*Math.sin(angle);

		path.moveTo(tailX-thicknessXOffset, tailZ+thicknessYOffset);
		path.lineTo(tailPieceFrontX-thicknessXOffset, tailPieceFrontZ+thicknessYOffset);
		path.lineTo(tailPieceFrontX+thicknessXOffset, tailPieceFrontZ-thicknessYOffset);
		path.lineTo(tailX+thicknessXOffset, tailZ-thicknessYOffset);
		path.closePath();
		
		
		d.draw(path);
		path.reset();
		
		d.setColor(new Color(0,255,0));
		path.moveTo(tailPieceCutoutX-thicknessXOffset, tailPieceCutoutZ+thicknessYOffset);
		path.lineTo(tailEndX-thicknessXOffset, tailEndZ+thicknessYOffset);
		path.lineTo(tailEndX+thicknessXOffset, tailEndZ-thicknessYOffset);
		path.lineTo(tailPieceCutoutX+thicknessXOffset, tailPieceCutoutZ-thicknessYOffset);
		path.closePath();

		d.draw(path);

		d.setTransform(savedTransform);
	}

	public static void printRailTailPieceNotches(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nRAIL TAIL PIECE NOTCHING\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();
		
		double ox = tailOffset;
		
		double tailPieceLength = 9.0*UnitUtils.INCH - tailOffset;						

		double span;						

		double angle = getTailPieceRailAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double x = 0.0;
		
		double endPointX = 0.0;
		double endPointZ = 0.0;
		
		double lastX = 0.0;
		double lastY = 0.0;
		double lastPos = 0.0;
		
		double notchX = 0.0;
		double notchZ = 0.0;
		
		double thicknessXOffset = 0.0;
		double thicknessYOffset = 0.0;
		
		double deck = 0.0;
		for(int n = 0; n < 5; n++)
		{
			if(tailPieceLength > 5.0*UnitUtils.INCH)
			{
				switch(n)
				{
				case 0:
				case 1:
				case 3:
				case 4:
					span = 1.0*UnitUtils.INCH;
					break;
				default:
					span = tailPieceLength - 4.0*UnitUtils.INCH;
					break;
				}
			}
			else
			{
				span = (n+1)/5.0*tailPieceLength;
			}
			
			int	steps = (int)(span/0.5);
			double step = span/steps;

			for(int i = 0; i < steps; i++)
			{
				//Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);
				
				//Find the point at distance from outline
				x = ox - (distanceFromRail *Math.sin(normalAngle));
				double y = oy - (distanceFromRail*Math.cos(normalAngle));
	
				if(first && y < 0)
				{
					ox += step;
					continue;
				}
	
				if(!first && y < 0)
					break;
	
				//Find thickness and rocker at pos
				deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);
	
				if(deck < bottom)
				{
					ox += step;
					continue;
				}
	
				if(deck < bottom)
					break;
	
				if(first)
				{
					//Calculate angle for tailpiece (tail piece sit halfway between deck and bottom on the rail template at both ends) 
					double z1 = (bottom+deck)/2.0;
									
					System.out.printf("angle: %f", angle*180.0/Math.PI);

					endPointX = ox;
					endPointZ = z1;
					first = false;

					thicknessXOffset = Math.sin(angle)*frameThickness/2.0;
					thicknessYOffset = Math.cos(angle)*frameThickness/2.0;
				}							
				else
				{					
					//Find the 2D length from the last point on deck and bottom
					double xd = x-lastX;
					double yd = y-lastY;
					double stepLength = Math.sqrt((xd*xd)+(yd*yd));
					
					double newPos = lastPos + stepLength;
										
					//Update last pos
					lastPos = newPos;
				}

				lastX = x;
				lastY = y;
				
				ox += step;
			}
/*
			Line2D.Double line = new Line2D.Double();
			line.setLine(lastPos, deck, lastPos, deck + 8.0);
			d.draw(line);
*/
			//Check if we should store pos
			if(n%2==0)
			{
				//Store for notching
				notchX = lastPos;
				notchZ = endPointZ + (((ox - endPointX)/Math.cos(angle))*Math.sin(angle));
			}
			else if(n%2==1)
			{
				double otherEndX = lastPos;
				double otherEndZ = endPointZ + (((ox - endPointX)/Math.cos(angle))*Math.sin(angle));

				path.moveTo(endPointX + notchX-thicknessXOffset - endPointX, notchZ+thicknessYOffset);
				path.lineTo(endPointX + otherEndX-thicknessXOffset - endPointX, otherEndZ+thicknessYOffset);
				path.lineTo(endPointX + otherEndX+thicknessXOffset - endPointX, otherEndZ-thicknessYOffset);
				path.lineTo(endPointX + notchX+thicknessXOffset - endPointX, notchZ-thicknessYOffset);
				path.closePath();
				d.draw(path);
				
				path.reset();
			}
			
		}		
		
		d.setTransform(savedTransform);
	}

	public static void printNosePiece(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset, boolean mirror) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, false, mirror);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nNose PIECE\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();
		
		
		double nosePieceLength = 9.0*UnitUtils.INCH - noseOffset;						

		double ox = brd.getLength() - 9.0*UnitUtils.INCH;
		double oy = 0.0;

		double span;						

		double angle = getNosePieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double x = 0.0;
		double y = 0.0;
		
		double normalAngle = 0.0;
		
		double xf = 0.0;
		
		for(int n = 0; n < 5; n++)
		{
			if(nosePieceLength > 5.0*UnitUtils.INCH)
			{
				switch(n)
				{
				case 0:
				case 1:
				case 3:
				case 4:
					span = 1.0*UnitUtils.INCH;
					break;
				default:
					span = nosePieceLength - 4.0*UnitUtils.INCH;
					break;
				}
			}
			else
			{
				span = (n+1)/5.0*nosePieceLength;
			}
			
			int	steps = (int)(span/0.5);
			double step = span/steps;

			for(int i = 0; i < steps; i++)
			{
				//Get the outline point and the angle
				oy = outline.getValueAt(ox);
				normalAngle = outline.getNormalAngle(ox);
				
				//Find the point at distance from outline
				x = ox - ((distanceFromRail - ((n%2==1)?frameThickness:0.0) )*Math.sin(normalAngle));
			    y = oy - ((distanceFromRail - ((n%2==1)?frameThickness:0.0) )*Math.cos(normalAngle));

				if(first && y < 0)
				{
					ox += step;
					continue;
				}

				if(!first && y < 0)
					break;

				//Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);

				if(first && deck < bottom)
				{
					ox += step;
					continue;
				}

				if(!first && deck < bottom)
				{
					path.lineTo(path.getCurrentPoint().getX(),0.0);
					d.draw(path);
					d.setTransform(savedTransform);
					return;
				}

				if(first)
				{
					first = false;

					xf = x;
					
					path.moveTo((x-xf + 2.0*UnitUtils.INCH)/Math.cos(angle), 0.0);
					path.lineTo((x-xf + 2.0*UnitUtils.INCH)/Math.cos(angle), frameThickness/2.0);
					path.lineTo((x-xf)/Math.cos(angle), frameThickness/2.0);
				}
				
				path.lineTo((x-xf)/Math.cos(angle), y);

				ox += step;
			}
			if(n < 4)
			{
				ox -= step;
				x = ox - ((distanceFromRail - ((n%2==0)?frameThickness:0.0) )*Math.sin(normalAngle));
				y = oy - ((distanceFromRail - ((n%2==0)?frameThickness:0.0) )*Math.cos(normalAngle));
				path.lineTo((x-xf)/Math.cos(angle), y);			
				ox += step;
			}
		}
		path.lineTo((x-xf)/Math.cos(angle), 0);
		
		
		d.draw(path);

		System.out.printf("\nNOSE PIECE\n");

		
		d.setTransform(savedTransform);
	}

	public static void printNosePieceWebbing(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset, boolean mirror) 
	{
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, false, mirror);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nNose WEBBING\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();
				
		double nosePieceLength = 9.0*UnitUtils.INCH - noseOffset;						

		double span = (nosePieceLength - webbing*5.0)/2.0;						

		double angle = getNosePieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		Point2D.Double point = null;
		
		int	steps = (int)(span/0.5);
		double step = span/steps;

		double ox = brd.getLength() - 9.0*UnitUtils.INCH - noseOffset + webbing*2.0;

		double comp = (webbing)*Math.sin(outline.getNormalAngle(ox));
		
		double xf = 0.0;

		for(int n = 0; n < 2; n++)
		{			
			boolean firstInSection = true;

			for(int i = 0; i < steps; i++)
			{
				//Use point from nose piece for end calculation
				point = BezierBoardDrawUtil.getOutline(brd, ox, distanceFromRail);

				if(first && point.y < 0)
				{
					ox += step;
					continue;
				}

				if(!first && point.y < 0)
					break;

				//Find thickness and rocker at pos
				double deck = BezierBoardDrawUtil.getDeck(brd, point.x, point.y, skinThickness);
				double bottom = BezierBoardDrawUtil.getBottom(brd, point.x, point.y, skinThickness);

				if(first && deck < bottom)
				{
					ox += step;
					continue;
				}

				if(!first && deck < bottom)
					break;

				if(first)
				{
					first = false;
					
					//Calculate angle for nosepiece (Nose piece sit halfway between deck and bottom on the rail template at both ends) 
					double z1 = (bottom+deck)/2.0;
				
					double tmp = brd.getLength()-noseOffset;
					double tmpDeck;
					double tmpBottom;
					Point2D.Double tmpPoint;
					do{
						tmpPoint = BezierBoardDrawUtil.getOutline(brd, tmp, distanceFromRail);
						
						tmpDeck = BezierBoardDrawUtil.getDeck(brd, tmpPoint.x, tmpPoint.y, skinThickness);
						tmpBottom = BezierBoardDrawUtil.getBottom(brd, tmpPoint.x, tmpPoint.y, skinThickness);
						
						tmp -= step;
					}while(tmpDeck < tmpBottom);

					double z2 = (tmpBottom+tmpDeck)/2.0;
					
					angle = Math.atan2(z2-z1, tmp - ox);
					
					System.out.printf("angle: %f", angle*180.0/Math.PI);
					
					xf = point.x - webbing*2.0 - comp;
					
				}
			
				//Get the webbing point
				point = BezierBoardDrawUtil.getOutline(brd, ox, distanceFromRail + webbing);

				if(firstInSection)
				{
					firstInSection = false;
					path.moveTo((point.x - xf)/Math.cos(angle), webbing);
				}
				
				path.lineTo((point.x - xf)/Math.cos(angle), point.y);
				
				ox += step;
			}

			path.lineTo((point.x - xf)/Math.cos(angle), webbing);
			path.closePath();
			
			d.draw(path);
			
			path.reset();
			
			ox += webbing;
		}
		
		
		d.setTransform(savedTransform);
	}

	public static void printStringerNosePieceCutOut(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nSTRINGER NOSE PIECE CUTOUT\n");

		GeneralPath path = new GeneralPath();
				
		double nosePieceLength = 9.0*UnitUtils.INCH - noseOffset;						

		double angle = getNosePieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		
		double thicknessXOffset = Math.sin(angle)*frameThickness/2.0;
		double thicknessYOffset = Math.cos(angle)*frameThickness/2.0;

		//Get the nose pos
		double noseX = getRailMaxPos(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double noseDeck = BezierBoardDrawUtil.getRailDeck(brd, noseX, distanceFromRail, skinThickness);
		double noseBottom = BezierBoardDrawUtil.getRailBottom(brd, noseX, distanceFromRail, skinThickness);
		double noseZ = (noseDeck + noseBottom)/2.0;
		double noseNormalAngle = outline.getNormalAngle(noseX);		
		//Find the point at distance from outline
		noseX = noseX - (distanceFromRail *Math.sin(noseNormalAngle));
		
		//Get the nose piece rear end pos
		double nosePieceRearX = brd.getLength() - 9.0*UnitUtils.INCH ;
		double nosePieceRearDeck = BezierBoardDrawUtil.getRailDeck(brd, nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearBottom = BezierBoardDrawUtil.getRailBottom(brd, nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearZ = (nosePieceRearDeck +nosePieceRearBottom)/2.0;
		double nosePieceRearNormalAngle = outline.getNormalAngle(nosePieceRearX);		
		//Find the point at distance from outline
		nosePieceRearX = nosePieceRearX - (distanceFromRail *Math.sin(nosePieceRearNormalAngle));
		
		double nosePieceCutoutX = nosePieceRearX + (2.0*UnitUtils.INCH);
		double nosePieceCutoutZ = nosePieceRearZ + (2.0*UnitUtils.INCH)/Math.cos(angle)*Math.sin(angle);

		double noseEndX = nosePieceRearX + nosePieceLength;
		double noseEndZ = nosePieceRearZ + (nosePieceLength)*Math.sin(angle);
		
		d.setColor(new Color(255,0,0));
		path.moveTo(nosePieceRearX-thicknessXOffset, nosePieceRearZ+thicknessYOffset);
		path.lineTo(noseX-thicknessXOffset, noseZ+thicknessYOffset);
		path.lineTo(noseX+thicknessXOffset, noseZ-thicknessYOffset);
		path.lineTo(nosePieceRearX+thicknessXOffset, nosePieceRearZ-thicknessYOffset);
		path.closePath();
		
		d.draw(path);
		path.reset();
		
		d.setColor(new Color(0,255,0));
		path.moveTo(nosePieceCutoutX-thicknessXOffset, nosePieceCutoutZ+thicknessYOffset);
		path.lineTo(noseEndX-thicknessXOffset, noseEndZ+thicknessYOffset);
		path.lineTo(noseEndX+thicknessXOffset, noseEndZ-thicknessYOffset);
		path.lineTo(nosePieceCutoutX+thicknessXOffset, nosePieceCutoutZ-thicknessYOffset);
		path.closePath();

		d.draw(path);
		
		d.setTransform(savedTransform);
	}

	public static void printRailNosePieceNotches(AbstractDraw d, double offsetX, double offsetY, double scale, BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double webbing, double tailOffset, double noseOffset) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
		
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
//		d.setColor(new Color(255,0,0));

		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nRAIL TAIL PIECE NOTCHING\n");

		boolean first = true;
		GeneralPath path = new GeneralPath();
		
		double ox = tailOffset;
		
		double nosePieceLength = 9.0*UnitUtils.INCH - noseOffset;						

		double span;						

		double angle = getNosePieceRailAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
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
		for(int n = 0; n < 6; n++)
		{
			if(nosePieceLength > 5.0*UnitUtils.INCH)
			{
				switch(n)
				{
				case 0:
					span = brd.getLength() - 9.0*UnitUtils.INCH; 
					break;
				case 1:
				case 2:					
				case 4:
				case 5:
					span = 1.0*UnitUtils.INCH;
					break;
				default:
					span = nosePieceLength - 4.0*UnitUtils.INCH;
					break;
				}
			}
			else
			{
				if(n == 0){
					span = brd.getLength() - 9.0*UnitUtils.INCH - noseOffset; 					
				}
				else
				{
					span = (n+1)/5.0*nosePieceLength;
				}
			}
			

			int	steps = (int)(span/0.5);
			double step = span/steps;

/*			System.out.printf("n: %d, span: %f, steps: %d, step: %f\n", n, span, steps, step);*/

			double bottom = 0.0;
			for(int i = 0; i < steps; i++)
			{
				//Get the outline point and the angle
				double oy = outline.getValueAt(ox);
				double normalAngle = outline.getNormalAngle(ox);
				
				//Find the point at distance from outline
				x = ox - (distanceFromRail *Math.sin(normalAngle));
				double y = oy - (distanceFromRail*Math.cos(normalAngle));
	
				if(first && y < 0)
				{
					ox += step;
					continue;
				}
	
				if(!first && y < 0)
					break;
	
				//Find thickness and rocker at pos
				deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
				bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);
	
				if(deck < bottom)
				{
					ox += step;
					continue;
				}
	
				if(deck < bottom)
				{
					break;
				}
	
				if(n > 0){
					//Find stringer thickness and rocker at pos
					double deckAtStringer = BezierBoardDrawUtil.getDeck(brd, ox, 0.0, skinThickness);
					double bottomAtStringer = BezierBoardDrawUtil.getBottom(brd, ox, 0.0, skinThickness);
	
					if(deckAtStringer < bottomAtStringer)
					{
	//					break;			
					}
				}

				if(!endFound && n>0)
				{
					double z1 = (bottom+deck)/2.0;
				
					endPointX = ox;
					endPointZ = z1;
					endPointPos = lastPos;

					first = false;
	/*
					Line2D.Double point = new Line2D.Double();
					point.setLine(lastPos, endPointZ,lastPos, endPointZ);
					d.draw(point);
*/
					thicknessXOffset = Math.sin(angle)*frameThickness/2.0;
					thicknessYOffset = Math.cos(angle)*frameThickness/2.0;
					
					endFound = true;
				}

				if(!first)
				{					
					//Find the 2D length from the last point on deck and bottom
					double xd = x-lastX;
					double yd = y-lastY;
					double stepLength = Math.sqrt((xd*xd)+(yd*yd));
					
					double newPos = lastPos + stepLength;
	/*									
					Line2D.Double line = new Line2D.Double();
					line.setLine(lastPos, bottom, newPos, bottom);
					d.draw(line);
*/
					//Update last pos
					lastPos = newPos;
				}
				else
				{
					first = false;
				}
				/*Debug*/
				
				lastX = x;
				lastY = y;
				
				ox += step;
			}
			
/*			System.out.printf("-------------------spanned: %f\n", ox-temp);*/
/*			
			Line2D.Double line = new Line2D.Double();
			line.setLine(lastPos, deck, lastPos, deck + 8.0);
			d.draw(line);
*/			
			System.out.printf("ox: %f, x: %f, lastPos: %f deck:%f bottom:%f\n", ox, x, lastPos,deck,bottom);

			//Check if we should store pos
			if(n%2==1)
			{
				//Store for notching
				notchX = lastPos;
				notchZ = endPointZ + (((ox - endPointX)/Math.cos(angle))*Math.sin(angle));
/*				System.out.printf("notchX: %f, notchY: %f\n", notchX, notchZ);*/

/*				Line2D.Double line = new Line2D.Double();
				line.setLine(notchX, notchZ, notchX, notchZ);
				d.draw(line);
*/			}
			else if(n%2==0 && n > 0)
			{
				double otherEndX = lastPos;
				double otherEndZ = endPointZ + (((ox - endPointX)/Math.cos(angle))*Math.sin(angle));
/*				System.out.printf("otherEndX: %f, otherEndZ: %f\n", otherEndX, otherEndZ);*/
/*
				Line2D.Double line = new Line2D.Double();
				line.setLine(otherEndX, otherEndZ, otherEndX, otherEndZ);
				d.draw(line);
	path.moveTo(notchX, notchZ);
	path.lineTo(otherEndX, otherEndZ);*/
				d.setColor(new Color(255,0,0));
				path.moveTo(notchX-thicknessXOffset, notchZ+thicknessYOffset);
				path.lineTo(otherEndX-thicknessXOffset, otherEndZ+thicknessYOffset);
				path.lineTo(endPointX + otherEndX+thicknessXOffset - endPointX, otherEndZ-thicknessYOffset);
				path.lineTo(endPointX + notchX+thicknessXOffset - endPointX, notchZ-thicknessYOffset);

				System.out.printf("notchX:%f notchZ:%f otherEndX:%f otherEndZ:%f\n", notchX, notchZ, otherEndX, otherEndZ);
				path.closePath();
				d.draw(path);
				
				path.reset();
			}


		}		
/*		Line2D.Double line = new Line2D.Double();
		d.setColor(new Color(0,255,0));
		line.setLine(endPointPos, endPointZ, lastPos, deck+bottom/2);
		d.draw(line);

		d.setColor(new Color(0,0,255));
		line.setLine(endPointPos, endPointZ, lastPos, endPointZ + (((ox-step-endPointX)/Math.cos(angle))*Math.sin(angle)));
		d.draw(line);
*/		
		d.setTransform(savedTransform);
	}
	
	//helper functions
	static double getTailPieceStringerAngle(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset){
		
		BezierSpline outline = brd.getOutline();
		
		//Get the tail pos
		double tailX = getRailMinPos(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double tailDeck = BezierBoardDrawUtil.getRailDeck(brd, tailX, distanceFromRail, skinThickness);
		double tailBottom = BezierBoardDrawUtil.getRailBottom(brd, tailX, distanceFromRail, skinThickness);
		double tailZ = (tailDeck +tailBottom)/2.0;
		double tailNormalAngle = outline.getNormalAngle(tailX);
		tailX = tailX - (distanceFromRail *Math.sin(tailNormalAngle));
		
		//Get the second tail pos
		double tailPieceFrontX = 9.0*UnitUtils.INCH;
		double tailPieceFrontDeck = BezierBoardDrawUtil.getRailDeck(brd, tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontBottom = BezierBoardDrawUtil.getRailBottom(brd, tailPieceFrontX, distanceFromRail, skinThickness);
		double tailPieceFrontZ = (tailPieceFrontDeck +tailPieceFrontBottom)/2.0;
		double tailPieceFrontNormalAngle = outline.getNormalAngle(tailPieceFrontX);		
		//Find the point at distance from outline
		tailPieceFrontX = tailPieceFrontX - (distanceFromRail *Math.sin(tailPieceFrontNormalAngle));
		
		//Calculate angle
		double angle = Math.atan2(tailPieceFrontZ-tailZ, tailPieceFrontX - tailX);
		
		return angle;
	}

	static double getTailPieceRailAngle(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset){
		
		//Calculate angle
		double angle = getTailPieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		
		return angle;
	}

	static double getNosePieceStringerAngle(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset){

		BezierSpline outline = brd.getOutline();
		//Get the nose pos
		double noseX = getRailMaxPos(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		double noseDeck = BezierBoardDrawUtil.getRailDeck(brd, noseX, distanceFromRail, skinThickness);
		double noseBottom = BezierBoardDrawUtil.getRailBottom(brd, noseX, distanceFromRail, skinThickness);
		double noseZ = (noseDeck +noseBottom)/2.0;
		double noseNormalAngle = outline.getNormalAngle(noseX);
		noseX = noseX - (distanceFromRail *Math.sin(noseNormalAngle));
		
		//Get the nose piece rear end pos
		double nosePieceRearX = brd.getLength() - 9.0*UnitUtils.INCH ;
		double nosePieceRearDeck = BezierBoardDrawUtil.getRailDeck(brd, nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearBottom = BezierBoardDrawUtil.getRailBottom(brd, nosePieceRearX, distanceFromRail, skinThickness);
		double nosePieceRearZ = (nosePieceRearDeck +nosePieceRearBottom)/2.0;
		double nosePieceRearNormalAngle = outline.getNormalAngle(nosePieceRearX);		
		//Find the point at distance from outline
		nosePieceRearX = nosePieceRearX - (distanceFromRail *Math.sin(nosePieceRearNormalAngle));
	
		//Calculate angle
		double angle = Math.atan2(noseZ-nosePieceRearZ, noseX - nosePieceRearX);
		
		return angle;
		
	}

	static double getNosePieceRailAngle(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset){
		double angle = getNosePieceStringerAngle(brd, distanceFromRail, skinThickness, frameThickness, tailOffset, noseOffset);
		
		System.out.printf("getNosePieceRailAngle() Angle: %f\n", angle*180.0/Math.PI);		
/*
		double endz = nosePieceRearZ + (((nosePieceRailLength)/Math.cos(angle))*Math.sin(angle));
		System.out.printf("getNosePieceRailAngle() endz: %f\n", endz);		
*/
		return angle;
	}
	
	static double getRailMinPos(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset){

		double ox = tailOffset;
		double oy = 0.0;
		
		double x = 0.0;
		double y = 0.0;
		
		double normalAngle = 0.0;
				
		double	span = brd.getLength();
		
		int	steps = (int)(span/0.5);
		double step = span/steps;

		BezierSpline outline = brd.getOutline();
		
		for(int i = 0; i < steps; i++)
		{
			//Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);
			
			//Find the point at distance from outline
			x = ox - (distanceFromRail*Math.sin(normalAngle));
			y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(y < 0)
			{
				ox += step;
				continue;
			}

			//Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
			double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);

			if(deck < bottom)
			{
				ox += step;
				continue;
			}
			
			break;
		}
		return ox;
	}

	static double getRailMaxPos(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset)
	{
		double ox = brd.getLength() - noseOffset;
		double oy = 0.0;
		
		double x = 0.0;
		double y = 0.0;
		
		double normalAngle = 0.0;
				
		double	span = brd.getLength() - noseOffset - tailOffset;
		
		int	steps = (int)(span/0.5);
		double step = span/steps;

		BezierSpline outline = brd.getOutline();
		
		for(int i = 0; i < steps; i++)
		{
			//Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);
			
			//Find the point at distance from outline
			x = ox - (distanceFromRail*Math.sin(normalAngle));
			y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(y < 0)
			{
				ox -= step;
				continue;
			}

			//Find thickness and rocker at pos
			double deck = BezierBoardDrawUtil.getDeck(brd, x, y, skinThickness);
			double bottom = BezierBoardDrawUtil.getBottom(brd, x, y, skinThickness);

			if(deck > bottom)
			{
				break;
			}

			ox -= step;
		}
		return ox;
	}

	static double getRailLength(BezierBoard brd, double distanceFromRail, double skinThickness, double frameThickness, double tailOffset, double noseOffset, double from, double to)
	{
		double ox = from;
		double oy = 0.0;
		
		double x = 0.0;
		double y = 0.0;
		
		double normalAngle = 0.0;
				
		double	span = to-from;
		
		int	steps = (int)(span/0.5);
		double step = span/steps;

		BezierSpline outline = brd.getOutline();

		boolean first = true;
		double lastX = 0.0;
		double lastY = 0.0;
		double lastPos = 0.0;
		for(int i = 0; i < steps; i++)
		{
			//Get the outline point and the angle
			oy = outline.getValueAt(ox);
			normalAngle = outline.getNormalAngle(ox);
			
			//Find the point at distance from outline
			x = ox - (distanceFromRail*Math.sin(normalAngle));
			y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(!first)
			{
				double xd = x-lastX;
				double yd = y-lastY;
				double stepLength = Math.sqrt((xd*xd)+(yd*yd));
				
				double newPos = lastPos + stepLength;

				//Update last pos
				lastPos = newPos;
			}
			else
			{
				first = false;
			}
		
			lastX = x;
			lastY = y;
			
			ox += step;
		}
		return lastPos;		
	}
	
}
