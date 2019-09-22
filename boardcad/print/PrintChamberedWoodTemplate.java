package boardcad.print;
/*

 * Created on Sep 18, 2005

 *

 * To change the template for this generated file go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */



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
import java.awt.geom.Line2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.vecmath.Point3d;

import cadcore.UnitUtils;

import board.BezierBoard;
import boardcad.AbstractDraw;
import boardcad.i18n.LanguageResource;
import boardcad.gui.jdk.*;


public class PrintChamberedWoodTemplate extends JComponent implements Printable {

	static final long serialVersionUID=1L;

	private PageFormat myPageFormat;
	
	private Font mPrintFontNormal = new Font("Dialog", Font.PLAIN, 10);

	private boolean mPaintGrid = true;
	private double mOffsetFromCenter = 20.0;
	private double mPlankThickness = 2.54;
	private double mDeckAndBottomMinimumThickness = 2.54/4;
	private boolean mDrawChambering = true;
	private boolean mDrawAlignmentMarks = true;
	

	/** Creates and initializes the ClickMe component. */

	public PrintChamberedWoodTemplate() {

//		Hint at good sizes for this component.

		setPreferredSize(new Dimension(800, 600));

		setMinimumSize(new Dimension(600, 480));

//		Request a black line around this component.

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

	}

	 public BezierBoard getBrd() {

		return BoardCAD.getInstance().getCurrentBrd();

	}


	 public void printTemplate(boolean paintGrid, double offsetFromCenter, double endOffsetFromCenter, double plankThickness, double deckAndBottomMinimumThickness, boolean drawChambering, boolean drawAlignmentMarks, boolean printMultiple) 
	 {
		mPaintGrid = paintGrid;
		mPlankThickness = plankThickness;
		mDeckAndBottomMinimumThickness = deckAndBottomMinimumThickness;
		mDrawChambering = drawChambering;
		mDrawAlignmentMarks = drawAlignmentMarks;
		mOffsetFromCenter = offsetFromCenter;
		 
		PrinterJob printJob = PrinterJob.getPrinterJob();

		myPageFormat = PrintBrd.getPageFormat(this, printJob, BoardCAD.getInstance().getCurrentBrd().getMaxRocker());
		if(myPageFormat == null)
			return;

		do
		{
			try {
				printJob.print();
	
			} catch(PrinterException pe) {
	
				System.out.println("Error printing: " + pe);
	
			}
			
			mOffsetFromCenter += plankThickness;
			
		}while(printMultiple && mOffsetFromCenter < endOffsetFromCenter);

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

		PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(dim.width, dim.height);
		paper.setImageableArea(0,0,dim.width, dim.height);
		format.setPaper(paper);

		BezierBoardDrawUtil.printProfile(jd, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, mPaintGrid,BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter, 0.0, false, 0.0, 0.0);
		printChambering(jd, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter, mPlankThickness, mDeckAndBottomMinimumThickness);
		BezierBoardDrawUtil.printProfile(jd, new Color(200,200,200), 1.0, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, false, BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter + mPlankThickness, 0.0, false, 0.0, 0.0);
		printAlignmentMarks(jd, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter, mPlankThickness, mDeckAndBottomMinimumThickness);

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
		
		if(printTemplate(pageFormat, pageIndex, g) == 0)
			return PAGE_EXISTS;
	
		return NO_SUCH_PAGE;
	
	}
	

	int printTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLength()
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 1;
		
		int heightInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getMaxRocker()
				/ ((pageFormat.getImageableHeight()/72)*2.54)) + 1;
		
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
		
		if (pageIndex < widthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontNormal);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontNormal);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("PROFILE_STR");
			String mRowString = LanguageResource.getString("ROW_STR") + ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("BOARDFILE_STR")+ ((pageIndex/widthInPages)+1) + "/" + heightInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				
			JavaDraw jd = new JavaDraw(g2d);
			
			BezierBoardDrawUtil.printProfile(jd,
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, mPaintGrid,BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter, 0.0, false, 0.0, 0.0);
			
			if(mDrawChambering)
			{
				printChambering(jd, 
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter, mPlankThickness, mDeckAndBottomMinimumThickness);
				BezierBoardDrawUtil.printProfile(jd, new Color(200,200,200), 1.0,
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, false, BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter + mPlankThickness, 0.0, false, 0.0, 0.0);
			}
			
			if(mDrawAlignmentMarks)
			{
				printAlignmentMarks(jd, 
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, 0.0, BoardCAD.getInstance().getCurrentBrd(), mOffsetFromCenter, mPlankThickness, mDeckAndBottomMinimumThickness);
			}
			
			return 0;
		}
		
		return -1;
	}
	
	public static void printChambering(AbstractDraw d, double offsetX, double offsetY, double scale, double rotation, BezierBoard brd, double centerOffset, double plankThickness, double deckAndBottomMinimumThickness)
	{	
		if(brd.isEmpty()) {
			return;	
		}
		
		System.out.printf("\nCHAMBERING\n");

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, rotation);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));
			
		GeneralPath path = new GeneralPath();
				
		double span = (UnitUtils.FOOT/2.0) - 2.0;
		int nrOfSteps = 20;
		double step = span/nrOfSteps;
		
		int nrOfHoles = 2*(int)((brd.getLength()/UnitUtils.FOOT) );
		
		double y = centerOffset + plankThickness;
		
		//For each foot of board
		double bottom;
		double deck;

		for(int i = 0; i != nrOfHoles; i++)
		{
			double x = (UnitUtils.FOOT/2.0) + (i*UnitUtils.FOOT/2.0) + 1.0;

			boolean first = true;
			int n = 0;
			for(; n < nrOfSteps+1; n++)
			{
				deck = brd.getDeckAt(x, y);
				deck -= deckAndBottomMinimumThickness;
				bottom = brd.getBottomAt(x, y);
				bottom += deckAndBottomMinimumThickness;
				
				if(bottom > deck)
				{
					if(first){
						x += step;
						continue;												
					}
					else
					{
						break;
					}
				}
				if(first)
				{
					path.moveTo(x,deck);
					first = false;
				}
				else{
					path.lineTo(x,deck);
				}
				x += step;
			}
		
			if(first == true)
			{
				continue;
			}
			
			for(; n > 0; n--)
			{
				x -= step;
				deck = brd.getDeckAt(x, y);
				deck -= deckAndBottomMinimumThickness;
				bottom = brd.getBottomAt(x, y);
				bottom += deckAndBottomMinimumThickness;
					if(bottom > deck)
				{
					break;
				}
				path.lineTo(x,bottom);
			}
			path.closePath();
			d.draw(path);
			
			path.reset();
		}

		d.setTransform(savedTransform);		
	}

	public static void printAlignmentMarks(AbstractDraw d, double offsetX, double offsetY, double scale, double rotation, BezierBoard brd, double centerOffset, double plankThickness, double deckAndBottomMinimumThickness)
	{	
		if(brd.isEmpty()) {
			return;	
		}
		
		System.out.printf("\nALIGNMENT MARKS\n");

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale, rotation);	
		Stroke stroke = new BasicStroke((float)(1.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(255,20,20));
		
		double y = centerOffset;
		
		int lengthInFeet = (int)(brd.getLength()/UnitUtils.FOOT);

		Line2D.Double line = new Line2D.Double();

		//For each foot of board, except far ends
		for(int i = 1; i < lengthInFeet; i++)
		{
			double x = i*UnitUtils.FOOT;
			
			//Check if there is some template to draw upon
			double deck = BezierBoardDrawUtil.getDeckWithSkinCompensation(brd, x, y, deckAndBottomMinimumThickness);
			double bottom = BezierBoardDrawUtil.getBottomWithSkinCompensation(brd, x, y, deckAndBottomMinimumThickness);
			
			if(deck < bottom)
			{
				continue;
			}
			
			if(deck-bottom < 1.0)
			{
				continue;
			}
			
			//Get the rail apex height at this position
			Point3d point = brd.getSurfacePoint(x, -45.0, 88.0, 1,1);
			
			if(deck - point.z < 0.5)	//Too close to deck
			{
				continue;
			}

			if(point.z - bottom < 0.5)	//Too close to bottom
			{
				continue;
			}

			//Draw a cross
			line.setLine(x-0.5, point.z, x+0.5, point.z);
			d.draw(line);			
			line.setLine(x, point.z-0.5, x, point.z+0.5);
			d.draw(line);

		}

		d.setTransform(savedTransform);		
	}
}
