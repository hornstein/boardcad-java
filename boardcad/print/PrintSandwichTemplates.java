package boardcad.print;



/**

 * @author Håvard

 *

 * To change the template for this generated type comment go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import cadcore.UnitUtils;

import board.BezierBoard;
import boardcad.i18n.LanguageResource;

import boardcad.gui.jdk.BezierBoardDrawUtil;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.JavaDraw;

public class PrintSandwichTemplates extends JComponent implements Printable {

	static final long serialVersionUID=1L;

	 private PageFormat myPageFormat;
	 private PrinterJob myPrintJob;

	protected enum PrintState {NO_STATE, 
		PRINT_DECKSKIN_TEMPLATE,				
		PRINT_BOTTOMSKIN_TEMPLATE,			
		PRINT_RAIL_TEMPLATE,
		PRINT_PROFILE_TEMPLATE,
		PRINT_CROSSSECTION_TEMPLATE
	};

	public PrintState mCurrentPrintState;
	
	private Font mPrintFontSmall = new Font("Dialog", Font.PLAIN, 10);

	private double mSkinThickness = 0.8;
	private double mDistanceToRail = 3.0;
	private double mTailOffset = 3.0;
	private double mNoseOffset = 4.0;
	private double mCrosssectionPos = 3.0;
	private boolean mFlatten = false;
	private double mProfileOffset = 0.0;
	

	/** Creates and initializes the ClickMe component. */

	public PrintSandwichTemplates() {

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

	 public void printProfileTemplate(double skinThickness, boolean flatten, double offset) {

		 initPrint();

			try {

				mSkinThickness = skinThickness;
				mFlatten = flatten;
				mProfileOffset = offset;
				mCurrentPrintState = PrintState.PRINT_PROFILE_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}

	 public void printRailTemplate(double distanceFromRail, double skinThickness, double tailOffset, double noseOffset, boolean flatten) 
	 {
		 initPrint();

			try {

				mDistanceToRail = distanceFromRail;
				mSkinThickness = skinThickness;
				mTailOffset = tailOffset;
				mNoseOffset = noseOffset;
				mFlatten = flatten;
				mCurrentPrintState = PrintState.PRINT_RAIL_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}

	 	public void printDeckSkinTemplate(double distanceFromRail) {

			 initPrint();

			try {

				mDistanceToRail = distanceFromRail;
				mCurrentPrintState = PrintState.PRINT_DECKSKIN_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}

	 	public void printBottomSkinTemplate(double distanceFromRail) {

			 initPrint();

			try {

				mDistanceToRail = distanceFromRail;
				mCurrentPrintState = PrintState.PRINT_BOTTOMSKIN_TEMPLATE;

				myPrintJob.print();

			} catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}
	 	
		 public void printCrosssectionTemplate(double distanceFromRail, double skinThickness, double pos) {

			 initPrint();

				try {

					mDistanceToRail = distanceFromRail;
					mSkinThickness = skinThickness;
					mCrosssectionPos = pos;
					mCurrentPrintState = PrintState.PRINT_CROSSSECTION_TEMPLATE;

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
		BezierBoardDrawUtil.printProfile(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, false,BoardCAD.getInstance().getCurrentBrd(), 0.0, 0.0, false, 0.0, 0.0);
		BezierBoardDrawUtil.printProfile(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, true,BoardCAD.getInstance().getCurrentBrd(), 5.0, 0.0, false, 0.0, 0.0);
		BezierBoardDrawUtil.printProfile(jd, border, dim.height*2.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, true,BoardCAD.getInstance().getCurrentBrd(), 20.0, 0.0, false, 0.0, 0.0);

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
		
			case PRINT_PROFILE_TEMPLATE:
			{
				if(printProfileTemplate(pageFormat, pageIndex, g) == 0)
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
			case PRINT_CROSSSECTION_TEMPLATE:
			{
				if(printCrosssectionTemplate(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;		
		
				break;
			}
		}
	
		return NO_SUCH_PAGE;
	
	}
	
	int printProfileTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLength()
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 2;
		
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
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("PROFILE_STR");
			String mRowString = LanguageResource.getString("ROW_STR")+ ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
			BezierBoardDrawUtil.printProfile(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), mProfileOffset, mSkinThickness, mFlatten, 0.0, 0.0);
			
			return 0;
		}
		
		return -1;
	}

	int printRailTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLength()
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 2;
		
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
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("PROFILE_STR");
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
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail, mSkinThickness, mTailOffset, mNoseOffset, mFlatten);
			
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
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail);
			
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
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), mDistanceToRail);
			
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
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
			BezierBoardDrawUtil.printCrossSection(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos, mDistanceToRail, mSkinThickness);
		
			BezierBoardDrawUtil.printCrossSection(new JavaDraw(g2d),
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, true, BoardCAD.getInstance().getCurrentBrd(), mCrosssectionPos);

			return 0;
		}
		
		return -1;
	}
}
