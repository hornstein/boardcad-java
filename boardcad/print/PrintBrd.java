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
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import cadcore.UnitUtils;

import board.BezierBoard;
import boardcad.i18n.LanguageResource;
import boardcad.gui.jdk.*;

public class PrintBrd extends JComponent implements Printable {

	static final long serialVersionUID=1L;

	 private PageFormat myPageFormat;

	protected enum PrintState {NO_STATE, 
		PRINT_OUTLINE, 
		PRINT_SPINTEMPLATE, 
		PRINT_PROFILE, 
		PRINT_SLICES	};

	public PrintState mCurrentPrintState;
	
	private Font mPrintFontSmall = new Font("Dialog", Font.PLAIN, 6);
	private Font mPrintFontNormal = new Font("Dialog", Font.PLAIN, 10);
	private Font mPrintFontLarge = new Font("Dialog", Font.PLAIN, 30);

	private boolean mPrintGrid = true;
	private boolean mOverCurve = false;

	/** Creates and initializes the ClickMe component. */

	public PrintBrd() {

//		Hint at good sizes for this component.

		setPreferredSize(new Dimension(800, 600));

		setMinimumSize(new Dimension(600, 480));

//		Request a black line around this component.

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

	}



	 public BezierBoard getBrd() {

		return BoardCAD.getInstance().getCurrentBrd();

	}

	static PageFormat getPageFormat(Printable printable, PrinterJob printJob, double targetWidth)
	{
		if(!printJob.printDialog())
			return null;
		
		PageFormat retPage;
		PageFormat page = printJob.defaultPage();
		Paper paper = page.getPaper();
		paper.setImageableArea(BoardCAD.mPrintMarginLeft, BoardCAD.mPrintMarginTop, paper.getWidth()-(BoardCAD.mPrintMarginLeft+BoardCAD.mPrintMarginRight), paper.getHeight()-(BoardCAD.mPrintMarginTop+BoardCAD.mPrintMarginBottom));
		page.setPaper(paper);

		retPage = printJob.pageDialog(page);
		if(retPage == page)
			return null;

		paper = retPage.getPaper();
		
		BoardCAD.mPrintMarginLeft = paper.getImageableX();
		BoardCAD.mPrintMarginTop = paper.getImageableY();
		BoardCAD.mPrintMarginRight = paper.getWidth() - (paper.getImageableWidth() + paper.getImageableX());
		BoardCAD.mPrintMarginBottom = paper.getHeight() - (paper.getImageableHeight() + paper.getImageableY());
		
		double pageHeight = (retPage.getImageableHeight()/72)*2.54;
		double pageWidth = (retPage.getImageableWidth()/72)*2.54;
	
		if (targetWidth < pageWidth) {

			retPage.setOrientation(PageFormat.LANDSCAPE);

		}

		else if(targetWidth < pageHeight) {
			
			retPage.setOrientation(PageFormat.PORTRAIT);
		}
		else 
		{

			int selection = JOptionPane.showConfirmDialog(BoardCAD.getInstance().getFrame(), "Brd too wide for single page. \nThe board will be printed in two or more strips of paper.\n\n", "Warning", JOptionPane.YES_NO_OPTION);

			if(selection == JOptionPane.NO_OPTION) 
			{
				return null;
			}


			if(pageHeight < pageWidth*2) 
			{
				retPage.setOrientation(PageFormat.LANDSCAPE);
			}
			else 
			{
				retPage.setOrientation(PageFormat.PORTRAIT);
			}
		}
		System.out.printf("Pre validation Page width: %f, page height: %f, orientation: %s, Margins x:%f, y:%f\n", retPage.getImageableWidth(), retPage.getImageableHeight(), retPage.getOrientation() == PageFormat.LANDSCAPE?"Landscape":"Portrait", retPage.getImageableX(), retPage.getImageableY());

		retPage = printJob.validatePage(retPage);
		
		System.out.printf("Page width: %f, page height: %f, orientation: %s, Margins x:%f, y:%f\n", retPage.getImageableWidth(), retPage.getImageableHeight(), retPage.getOrientation() == PageFormat.LANDSCAPE?"Landscape":"Portrait", retPage.getImageableX(), retPage.getImageableY());

		printJob.setPrintable(printable,retPage);

		return retPage;
	}

	 public void printOutline(boolean printGrid, boolean overCurve) {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		
		myPageFormat = getPageFormat(this, printJob, BoardCAD.getInstance().getCurrentBrd().getCenterWidth()/2);
		if(myPageFormat == null)
			return;

		try {

			mCurrentPrintState = PrintState.PRINT_OUTLINE;
			mPrintGrid = printGrid;
			mOverCurve = overCurve;
			
			printJob.print();

		}

		catch(PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}


	 public void printSpinTemplate(boolean printGrid, boolean overCurve) {
			PrinterJob printJob = PrinterJob.getPrinterJob();

			myPageFormat = getPageFormat(this, printJob, BoardCAD.getInstance().getCurrentBrd().getCenterWidth()/2);
			if(myPageFormat == null)
				return;

			try {

				mCurrentPrintState = PrintState.PRINT_SPINTEMPLATE;
				mPrintGrid = printGrid;
				mOverCurve = overCurve;
				
				printJob.print();

			}

			catch(PrinterException pe) {

				System.out.println("Error printing: " + pe);

			}

		}





	 public void printProfile() {
		PrinterJob printJob = PrinterJob.getPrinterJob();

		myPageFormat = getPageFormat(this, printJob, BoardCAD.getInstance().getCurrentBrd().getMaxRocker());
		if(myPageFormat == null)
			return;

		try {

			mCurrentPrintState = PrintState.PRINT_PROFILE;

			printJob.print();

		} catch(PrinterException pe) {

			System.out.println("Error printing: " + pe);

		}

	}



	 public void printSlices() {
		PrinterJob printJob = PrinterJob.getPrinterJob();

		myPageFormat = getPageFormat(this, printJob,BoardCAD.getInstance().getCurrentBrd().getMaxRocker());
		if(myPageFormat == null)
			return;

		try {

			mCurrentPrintState = PrintState.PRINT_SLICES;

			printJob.print();

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

		PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(dim.width, dim.height);
		paper.setImageableArea(0,0,dim.width, dim.height);
		format.setPaper(paper);
		printDetailedSpecSheet(format, 0, g2d);
//		BezierBoardDrawUtil.printOutline(jd, border, dim.height*1.0/5.0, 0.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), false, false);
//		BezierBoardDrawUtil.printOutlineOverCurve(jd, border, dim.height*1.0/5.0, 0.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), false, false, false, false);

//		BezierBoardDrawUtil.printSpinTemplate(jd, border, dim.height*2.0/5.0, 0.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), false, false);
//		BezierBoardDrawUtil.printSpinTemplateOverCurve(jd, border, dim.height*2.0/5.0, 0.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true, BoardCAD.getInstance().getCurrentBrd(), false, false);
/*	
		BezierBoardDrawUtil.printProfile(jd, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(),false);
		BezierBoardDrawUtil.printRailTemplate(jd, border, dim.height*3.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), 3.0);
		
		for(int i = 1; i < BoardCAD.getInstance().getCurrentBrd().getCrossSections().size()-1; i++)
		{
			BezierBoardDrawUtil.printSlice(jd, border, dim.height*4.0/5.0, (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), true,BoardCAD.getInstance().getCurrentBrd(), i,false);
		}

		final BezierSpline p = BoardCAD.getInstance().getCurrentBrd().getInterpolatedCrossSection(BoardCAD.getInstance().getCurrentBrd().getLength()/2.0).getBezierSpline();
		
	    MathUtils.Function func = new MathUtils.Function(){public double f(double tt){return  p.getNormalByTT(tt);}};
		
	    double scale = (dim.width-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength());
	    
		BezierBoardDrawUtil.paintFunction(jd, border, dim.height/2.0, scale, new Color(0,0,0), new BasicStroke((float)(2.0/scale)), func, 0.0, 1.0, 200.0, 10.0);
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
		case PRINT_OUTLINE:
		{
			if(printOutline(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;
	
			break;
		}
		case PRINT_SPINTEMPLATE:
		{
			if(printSpinTemplate(pageFormat, pageIndex, g) == 0)
				return PAGE_EXISTS;
	
			break;
		}
		
			case PRINT_PROFILE:
			{
				if(printProfile(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;
		
				break;
			}
		
			case PRINT_SLICES:
			{
				if(printSlices(pageFormat, pageIndex, g) == 0)
					return PAGE_EXISTS;
		
				break;
			}
		
		}
	
		return NO_SUCH_PAGE;
	
	}
	

	int printOutline(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		double pageHeight = ((pageFormat.getImageableHeight()/72)*2.54);
		double pageWidth = ((pageFormat.getImageableWidth()/72)*2.54);
	
		int widthInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getLengthOverCurve()
	
				/ pageWidth ) + 1;
	
		int heightInPages = (int)((BoardCAD.getInstance().getCurrentBrd().getCenterWidth()/2)
	
				/ pageHeight ) + 1;
	
	
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
	
		if (pageIndex < widthInPages*heightInPages) {
	
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontNormal);
	
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontNormal);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
	
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename() + LanguageResource.getString("OUTLINE_STR");
			String mRowString = LanguageResource.getString("ROW_STR") + ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
	
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
	
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	

			if(mOverCurve)
			{
				BezierBoardDrawUtil.printOutlineOverCurve(new JavaDraw(g2d),
		
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
		
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 0.0, 72/2.54, mPrintGrid ,BoardCAD.getInstance().getCurrentBrd(),BoardCAD.getInstance().isPrintingControlPoints(),BoardCAD.getInstance().isPrintingFins(), false, false);
			}
			else
			{
				BezierBoardDrawUtil.printOutline(new JavaDraw(g2d),
						
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
		
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 0.0, 72/2.54, mPrintGrid ,BoardCAD.getInstance().getCurrentBrd(),BoardCAD.getInstance().isPrintingControlPoints(),BoardCAD.getInstance().isPrintingFins());				
			}
			
			return 0;
		}
		
		return -1;
	
	}
	
	int printSpinTemplate(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		double pageHeight = ((pageFormat.getImageableHeight()/72)*2.54);
		double pageWidth = ((pageFormat.getImageableWidth()/72)*2.54);
	
		int widthInPages = (int)((brd.getLengthOverCurve()/2.0)
	
				/ pageWidth ) + 1;
	
		int heightInPages = (int)((brd.getCenterWidth()/2)
	
				/ pageHeight ) + 1;
	
	
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
	
		if (pageIndex < widthInPages*heightInPages) {
	
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontNormal);
	
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontNormal);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
	
			String mModelString = LanguageResource.getString("BOARDFILE_STR") + brd.getFilename() + LanguageResource.getString("OUTLINE_STR");
			String mRowString = LanguageResource.getString("ROW_STR") + ((pageIndex%widthInPages)+1) + "/" + widthInPages;
			String mColumnString = LanguageResource.getString("COLUMN_STR") + ((pageIndex/widthInPages)+1) + "/" + heightInPages;
	
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mRowString, xm, ym+(hgt+2)*2);
			g.drawString(mColumnString, xm, ym+(hgt+2)*3);
	
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	

			if(mOverCurve)
			{
				BezierBoardDrawUtil.printSpinTemplateOverCurve(new JavaDraw(g2d),
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 0.0, 72/2.54, mPrintGrid,brd,BoardCAD.getInstance().isPrintingControlPoints(),BoardCAD.getInstance().isPrintingFins());
			}
			else
			{
				BezierBoardDrawUtil.printSpinTemplate(new JavaDraw(g2d),
						-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
						-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 0.0, 72/2.54, mPrintGrid,brd,BoardCAD.getInstance().isPrintingControlPoints(),BoardCAD.getInstance().isPrintingFins());
			}
			
			return 0;
		}
		
		return -1;
	
	}

	int printProfile(PageFormat pageFormat, int pageIndex, Graphics g)
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
		
		
		
			BezierBoardDrawUtil.printProfile(new JavaDraw(g2d),
		
					-pageFormat.getImageableWidth()*(pageIndex%widthInPages),
		
					-pageFormat.getImageableHeight()*(pageIndex/widthInPages), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(),BoardCAD.getInstance().isPrintingControlPoints());
			
			return 0;
		}
		
		return -1;
	}
	
	int printSlices(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		int widthPrSliceInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getCenterWidth()/2
				
				/ ((pageFormat.getImageableWidth()/72)*2.54) ) + 1;
		
		int heightInPages = (int)(BoardCAD.getInstance().getCurrentBrd().getThickness()
		
				/ ((pageFormat.getImageableHeight()/72)*2.54)) + 1;
		
		int totalWidthInPages = widthPrSliceInPages*(BoardCAD.getInstance().getCurrentBrd().getCrossSections().size()-2);
		
		int xm = (int)pageFormat.getImageableX();
		int ym = (int)pageFormat.getImageableY();
		
		if (pageIndex < totalWidthInPages*heightInPages) {
		
			Graphics2D g2d = (Graphics2D)g;
			g2d.setFont(mPrintFontNormal);
		
			FontMetrics metrics = g2d.getFontMetrics(mPrintFontNormal);
			// get the height of a line of text in this font and render context
			int hgt = metrics.getHeight();
		
			String mModelString =  LanguageResource.getString("BOARDFILE_STR") + BoardCAD.getInstance().getCurrentBrd().getFilename();
			String mSliceString =  LanguageResource.getString("CROSSECTION_STR") + (pageIndex/widthPrSliceInPages + 1) + LanguageResource.getString("AT_STR") + UnitUtils.convertLengthToCurrentUnit(BoardCAD.getInstance().getCurrentBrd().getCrossSections().get((pageIndex/widthPrSliceInPages + 1)).getPosition(), false);
			String mRowString =  LanguageResource.getString("ROW_STR")+ ((pageIndex%widthPrSliceInPages)+1) + "/" + widthPrSliceInPages;
		
			g2d.setColor(Color.BLACK);
			//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
			g.drawString(mModelString, xm, ym+(hgt+2)*1);
			g.drawString(mSliceString, xm, ym+(hgt+2)*2);
			g.drawString(mRowString, xm, ym+(hgt+2)*3);
		
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
		
		
			BezierBoardDrawUtil.printSlice(new JavaDraw(g2d),
		
					-pageFormat.getImageableWidth()*(pageIndex%widthPrSliceInPages),
		
					-BoardCAD.getInstance().getCurrentBrd().getCrossSections().get((pageIndex/widthPrSliceInPages + 1)).getBezierSpline().getMinY()*(72/2.54), 72/2.54, true, BoardCAD.getInstance().getCurrentBrd(),
					pageIndex/widthPrSliceInPages + 1,BoardCAD.getInstance().isPrintingControlPoints());
			
			return 0;
		}
		
		return -1;
	}
	
	int printSpecSheet(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		if(pageIndex > 0)
			return -1;
		
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		double pageWidth = pageFormat.getImageableWidth();
		double pageHeight = pageFormat.getImageableHeight();
		
		int xm = (int)pageFormat.getImageableX();
		int currentY = (int)pageFormat.getImageableY();
		
		int width = (int)pageFormat.getImageableWidth();
		int height = (int)pageFormat.getImageableHeight();
		
		double scale = (width-10)/brd.getLength();
		
		Graphics2D g2d = (Graphics2D)g;
		
		FontMetrics metrics = g2d.getFontMetrics(mPrintFontLarge);
		// get the height of a line of text in this font and render context
		int hgt = metrics.getHeight();
				
		String fileString = LanguageResource.getString("FILE_STR") + brd.getFilename();
		String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getThickness(), true);
		String volumeString = LanguageResource.getString("VOLUME_STR") + UnitUtils.convertVolumeToCurrentUnit(brd.getVolume());
		String designerString = LanguageResource.getString("DESIGNER_STR") + brd.getDesigner();
		String modelString = LanguageResource.getString("MODEL_STR") + brd.getModel();
		String surferString = LanguageResource.getString("SURFER_STR") + brd.getSurfer();
		SimpleDateFormat format = new SimpleDateFormat("dd.mm.yyyy");
		String dateString = LanguageResource.getString("DATE_STR") + format.format(new Date());
		
		currentY += hgt;
		
		g2d.setColor(Color.BLACK);
		//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
		g2d.setFont(mPrintFontLarge);
		g2d.drawString("BoardCAD", xm, currentY);
					
		g2d.setFont(mPrintFontNormal);
		
		metrics = g2d.getFontMetrics(mPrintFontNormal);
		// get the height of a line of text in this font and render context
		hgt = metrics.getHeight();
		
		currentY += hgt+1;
		g.drawString(LanguageResource.getString("SPECSHEET_STR"), xm, currentY);
		
		currentY += hgt+5;
		
		g.drawString(designerString, xm, currentY);
		g.drawString(dateString, xm+(width/2), currentY);
		currentY += hgt+2;
		g.drawString(surferString, xm, currentY);
		g.drawString(modelString, xm+(width/2), currentY);
		currentY += hgt+2;
		g.drawString(measurementsString, xm, currentY);
		g.drawString(volumeString, xm+(width/2), currentY);
		currentY += hgt+2;
		g.drawString(fileString, xm, currentY);
		
		currentY += hgt+2;
		
		currentY += (brd.getCenterWidth()/2)*scale;
		
		Stroke linestroke = new BasicStroke((float)(1.0/scale));
		Line2D.Double line = new Line2D.Double();
		
		JavaDraw jd = new JavaDraw(g2d);
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, scale);	
		
		g2d.setStroke(linestroke);
		g2d.setColor(Color.GRAY);
		double tailpos = 12*UnitUtils.INCH;
		double tailbrdwidth = brd.getWidthAtPos(tailpos);
		line.setLine(tailpos, (-tailbrdwidth/2), tailpos, (tailbrdwidth/2));
		g2d.draw(line);
		double centerpos = brd.getLength()/2;
		double centerbrdwidth = brd.getWidthAtPos(centerpos);
		line.setLine(centerpos, (-centerbrdwidth/2), centerpos, (centerbrdwidth/2));
		g2d.draw(line);
		double nosepos = brd.getLength() - (12*UnitUtils.INCH);
		double nosebrdwidth = brd.getWidthAtPos(nosepos);
		line.setLine(nosepos, (-nosebrdwidth/2), nosepos, (nosebrdwidth/2));
		g2d.draw(line);
		
		//Stringer
		line.setLine(brd.getLength(), 0, 0, 0);
		g2d.draw(line);
		
		//Fins. x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of sidefin, splay angle
		/*			g2d.setColor(Color.LIGHT_GRAY);
		final Line2D tmp = new Line2D.Double();
		tmp.setLine(brd.getFins[0],brd.getFins[1],brd.getFins[2],brd.getFins[3]);
		g2d.draw(tmp);
		tmp.setLine(brd.getFins[0],-brd.getFins[1],brd.getFins[2],-brd.getFins[3]);
		g2d.draw(tmp);
		tmp.setLine(brd.getFins[5],0,brd.getFins[4],0);
		g2d.draw(tmp);
		*/
		
		g2d.setTransform(savedTransform);
		
		Stroke stroke = new BasicStroke((float)(1.5/scale));
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getOutline(), BezierBoardDrawUtil.MirrorY, false);
		BezierBoardDrawUtil.paintFins(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getFins(), false,false);
		
		currentY += (brd.getCenterWidth()/2)*scale + hgt + 5;
		
		g2d.setColor(Color.GRAY);
		String tailWidthString = UnitUtils.convertLengthToCurrentUnit(tailbrdwidth, false);
		double tailstringlen = metrics.stringWidth(tailWidthString);
		g.drawString(tailWidthString, (int)(xm+5 +(tailpos*scale - tailstringlen/2)), currentY);
		
		String centerWidthString = UnitUtils.convertLengthToCurrentUnit(centerbrdwidth, false);
		double centerstringlen = metrics.stringWidth(centerWidthString);
		g.drawString(centerWidthString, (int)(xm+5 +(centerpos*scale - centerstringlen/2)), currentY);
		
		String noseWidthString = UnitUtils.convertLengthToCurrentUnit(nosebrdwidth, false);
		double nosestringlen = metrics.stringWidth(noseWidthString);
		g.drawString(noseWidthString, (int)(xm+5 +(nosepos*scale - nosestringlen/2)), currentY);
		
		double tailThickness = brd.getThicknessAtPos(tailpos);
		double centerThickness = brd.getThicknessAtPos(centerpos);
		double noseThickness = brd.getThicknessAtPos(nosepos);
		
		double tailRocker = brd.getRockerAtPos(0);
		double noseRocker = brd.getRockerAtPos(brd.getLength());
		double tail1Rocker = brd.getRockerAtPos(tailpos);
		double nose1Rocker = brd.getRockerAtPos(nosepos);
		
		currentY += brd.getMaxRocker()*scale +10;
		
		savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, scale);	
		
		g2d.setStroke(linestroke);
		line.setLine(tailpos, -(tailThickness+tail1Rocker), tailpos, -tail1Rocker);
		g2d.draw(line);
		line.setLine(centerpos, -(centerThickness), centerpos, 0);
		g2d.draw(line);
		line.setLine(nosepos, -(noseThickness+nose1Rocker), nosepos, -nose1Rocker);
		g2d.draw(line);
		
		g2d.setColor(Color.GRAY);
		line.setLine(0, -(tailRocker), 0, 0);
		g2d.draw(line);
		line.setLine(tailpos, -(tail1Rocker), tailpos, 0);
		g2d.draw(line);
		line.setLine(nosepos, -(nose1Rocker), nosepos, 0);
		g2d.draw(line);
		line.setLine(brd.getLength(), -(noseRocker), brd.getLength(), 0);
		g2d.draw(line);
		
		g2d.setTransform(savedTransform);
		
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getDeck(), BezierBoardDrawUtil.FlipY, false);
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getBottom(), BezierBoardDrawUtil.FlipY, false);
		
		currentY += hgt + 5;
		
		g2d.setColor(Color.LIGHT_GRAY);
		
		String tailThicknessString = UnitUtils.convertLengthToCurrentUnit(tailThickness, false);
		double tailthicknessstringlen = metrics.stringWidth(tailThicknessString);
		g.drawString(tailThicknessString, (int)(xm+5 +(tailpos*scale - tailthicknessstringlen/2)), currentY);
		
		String CenterThicknessString = UnitUtils.convertLengthToCurrentUnit(centerThickness, false);
		double Centerthicknessstringlen = metrics.stringWidth(CenterThicknessString);
		g.drawString(CenterThicknessString, (int)(xm+5 +(centerpos*scale - Centerthicknessstringlen/2)), currentY);
		
		String NoseThicknessString = UnitUtils.convertLengthToCurrentUnit(noseThickness, false);
		double Nosethicknessstringlen = metrics.stringWidth(NoseThicknessString);
		g.drawString(NoseThicknessString, (int)(xm+5 +(nosepos*scale - Nosethicknessstringlen/2)), currentY);
		
		currentY += hgt + 5;
		
		g2d.setColor(Color.GRAY);
		String tailRockerString = UnitUtils.convertLengthToCurrentUnit(tailRocker, false);
		double tailRockerstringlen = metrics.stringWidth(tailRockerString);
		g.drawString(tailRockerString, xm+5, currentY);
		
		String tail1RockerString = UnitUtils.convertLengthToCurrentUnit(tail1Rocker, false);
		double tail1Rockerstringlen = metrics.stringWidth(tail1RockerString);
		g.drawString(tail1RockerString, (int)(xm+5 +(tailpos*scale - tail1Rockerstringlen/2)), currentY);
		
		String nose1RockerString = UnitUtils.convertLengthToCurrentUnit(nose1Rocker, false);
		double nose1Rockerstringlen = metrics.stringWidth(nose1RockerString);
		g.drawString(nose1RockerString, (int)(xm+5 +(nosepos*scale - nose1Rockerstringlen/2)), currentY);
		
		String noseRockerString = UnitUtils.convertLengthToCurrentUnit(noseRocker, false);
		double noseRockerstringlen = metrics.stringWidth(noseRockerString);
		g.drawString(noseRockerString, (int)(xm+5 +(brd.getLength()*scale - noseRockerstringlen)), currentY);
		
		currentY += brd.getThickness()*scale + 5;
		
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+width/2,	currentY, 0.0, scale, Color.BLACK, linestroke, false, true, brd.getLength()/2, brd);
		
		currentY += hgt;
		
		String centerString = LanguageResource.getString("CENTER_STR");
		double centerStringLen = metrics.stringWidth(centerString);
		g.drawString(centerString, (int)(xm+5 + width/2 - centerStringLen/2), currentY);
		
		currentY += hgt+10;
		
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((width/3)*1),	currentY, 0.0, scale, Color.BLACK, linestroke, false, true, UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, brd);
		
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((width/3)*2),	currentY, 0.0, scale, Color.BLACK, linestroke, false, true, brd.getLength() - UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, brd);
		
		currentY += hgt;
		String tailString = LanguageResource.getString("TAIL_STR");
		double tailStringLen = metrics.stringWidth(tailString);
		g.drawString(tailString, (int)(xm+5 + ((width/3)*1) - tailStringLen/2), currentY);
					
		String noseString = LanguageResource.getString("NOSE_STR");
		double noseStringLen = metrics.stringWidth(noseString);
		g.drawString(noseString, (int)(xm+5 + ((width/3)*2) - noseStringLen/2), currentY);
		
		currentY += hgt+10;
		
		if(brd.getFins()[2] != 0 || brd.getFins()[5] != 0 || brd.getFinType() != "")
		{
			g.drawString(LanguageResource.getString("FINS_STR"), xm, currentY);
			currentY += hgt+5;
			
			if(brd.getFinType() != "")
			{
				g.drawString(LanguageResource.getString("FINTYPE_STR") + brd.getFinType(), xm, currentY);
				currentY += hgt+5;					
			}
			
			if(brd.getFins()[5] != 0)
			{
				g.drawString(LanguageResource.getString("CENTERFIN_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[5], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[4], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[6], false), xm, currentY);
				currentY += hgt+5;					
			}
		
			if(brd.getFins()[2] != 0)
			{
				g.drawString(LanguageResource.getString("SIDEFINS_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[2], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[0], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[7],false) + LanguageResource.getString("TOEIN_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[3]-brd.getFins()[1],false) + LanguageResource.getString("SPLAY_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[8],false), xm, currentY);
				currentY += hgt+5;					
			}
		
			currentY += hgt+10;
		}
		
		g.drawString("Comments:", xm, currentY);
		currentY += hgt+2;
		int currentX = xm;
		
		String[] lineStrings = brd.getComments().split("\n");
		
		double spacelen = metrics.stringWidth(" ");
		
		for(int i = 0; i < lineStrings.length; i++)
		{
			String[] commentsStrings = lineStrings[i].split(" ");
			
			for(int j = 0; j < commentsStrings.length; j++)
			{
				
				double strlen = metrics.stringWidth(commentsStrings[j]);
				if(currentX + strlen > xm+width)
				{
					currentY += hgt+2;
					currentX = xm;
				}
				g.drawString(commentsStrings[j]+" ", currentX, currentY);
				
				currentX += strlen + spacelen;
			}
			currentY += hgt+2;										
			currentX = xm;
		}
		
		
		return 0;
	
	}

int printDetailedSpecSheet(PageFormat pageFormat, int pageIndex, Graphics g)
{
	if(pageIndex > 0)
		return -1;
	
	BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
	
	double pageWidth = pageFormat.getImageableWidth();
	double pageHeight = pageFormat.getImageableHeight();
	
	int xm = (int)pageFormat.getImageableX();
	int currentY = (int)pageFormat.getImageableY();
	
	int width = (int)pageFormat.getImageableWidth();
	int height = (int)pageFormat.getImageableHeight();
	
	double scale = (width-10)/brd.getLength();
	
	Graphics2D g2d = (Graphics2D)g;
	
	FontMetrics metrics = g2d.getFontMetrics(mPrintFontLarge);
	// get the height of a line of text in this font and render context
	int hgt = metrics.getHeight();
		
	String fileString = LanguageResource.getString("FILE_STR") + brd.getFilename();
	String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getThickness(), true);
	String volumeString = LanguageResource.getString("VOLUME_STR") + UnitUtils.convertVolumeToCurrentUnit(brd.getVolume());
	String designerString = LanguageResource.getString("DESIGNER_STR") + brd.getDesigner();
	String modelString = LanguageResource.getString("MODEL_STR") + brd.getModel();
	String surferString = LanguageResource.getString("SURFER_STR") + brd.getSurfer();
	SimpleDateFormat format = new SimpleDateFormat("dd.mm.yyyy");
	String dateString = LanguageResource.getString("DATE_STR") + format.format(new Date());
	
	currentY += hgt;
	
	g2d.setColor(Color.GREEN);
	//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));
	g2d.setFont(mPrintFontLarge);
	
    AttributedString as = new AttributedString("BoardCAD");
    AffineTransform vert = new AffineTransform();
    vert.setToRotation(Math.PI/2);
    as.addAttribute(TextAttribute.TRANSFORM, vert);
	g2d.drawString(as.getIterator(), xm, currentY);
				
	g2d.setFont(mPrintFontNormal);
	
	metrics = g2d.getFontMetrics(mPrintFontNormal);
	// get the height of a line of text in this font and render context
	hgt = metrics.getHeight();
	
	//Top text
	currentY += hgt+1;
	g.drawString(LanguageResource.getString("SPECSHEET_STR"), xm, currentY);
	
	currentY += hgt+5;
	
	g.drawString(designerString, xm, currentY);
	g.drawString(dateString, xm+(width/2), currentY);
	currentY += hgt+2;
	g.drawString(surferString, xm, currentY);
	g.drawString(modelString, xm+(width/2), currentY);
	currentY += hgt+2;
	g.drawString(measurementsString, xm, currentY);
	g.drawString(volumeString, xm+(width/2), currentY);
	currentY += hgt+2;
	g.drawString(fileString, xm, currentY);
	
	currentY += hgt+2;
	
	currentY += (brd.getCenterWidth()/2)*scale;
	
	Stroke linestroke = new BasicStroke((float)(1.0/scale));
	Line2D.Double line = new Line2D.Double();
	
	JavaDraw jd = new JavaDraw(g2d);
	AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, scale);	
	
	//Outline Crossection lines
	g2d.setStroke(linestroke);
	g2d.setColor(Color.RED);
	double tailpos = 12*UnitUtils.INCH;
	double tailbrdwidth = brd.getWidthAtPos(tailpos);
	line.setLine(tailpos, (-tailbrdwidth/2), tailpos, (tailbrdwidth/2));
	g2d.draw(line);
	double centerpos = brd.getLength()/2;
	double centerbrdwidth = brd.getWidthAtPos(centerpos);
	line.setLine(centerpos, (-centerbrdwidth/2), centerpos, (centerbrdwidth/2));
	g2d.draw(line);
	double nosepos = brd.getLength() - (12*UnitUtils.INCH);
	double nosebrdwidth = brd.getWidthAtPos(nosepos);
	line.setLine(nosepos, (-nosebrdwidth/2), nosepos, (nosebrdwidth/2));
	g2d.draw(line);
	
	//Stringer
	line.setLine(brd.getLength(), 0, 0, 0);
	g2d.draw(line);
	
	//Fins. x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of sidefin, splay angle
	/*			g2d.setColor(Color.LIGHT_GRAY);
	final Line2D tmp = new Line2D.Double();
	tmp.setLine(brd.getFins[0],brd.getFins[1],brd.getFins[2],brd.getFins[3]);
	g2d.draw(tmp);
	tmp.setLine(brd.getFins[0],-brd.getFins[1],brd.getFins[2],-brd.getFins[3]);
	g2d.draw(tmp);
	tmp.setLine(brd.getFins[5],0,brd.getFins[4],0);
	g2d.draw(tmp);
	*/
	
	g2d.setTransform(savedTransform);
	
	Stroke stroke = new BasicStroke((float)(1.5/scale));
	BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getOutline(), BezierBoardDrawUtil.MirrorY, false);
	BezierBoardDrawUtil.paintFins(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getFins(), false,false);
	
	currentY += (brd.getCenterWidth()/2)*scale + hgt + 5;
	
	//Outline texts
	g2d.setColor(Color.BLUE);
	
	for(int i = 0; i < 7; i++)
	{
		double pos = 0.0;
		if(i == 0)
		{
			pos = UnitUtils.INCH;
		}
		else if(i <= 2)
		{
			pos = i*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
		}
		else if(i == 3)
		{
			pos = brd.getMaxWidthPos();
		}
		else
		{
			pos = brd.getLength() + (i-6)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
		}
		
		double currentWidth = brd.getWidthAt(pos);
		
		String widthString = UnitUtils.convertLengthToCurrentUnit(currentWidth, false);
		double widthStringLength = metrics.stringWidth(widthString);
		g.drawString(widthString, (int)(xm+5 +(pos*scale - widthStringLength/2)), currentY);
		
	}
		
	double tailThickness = brd.getThicknessAtPos(tailpos);
	double centerThickness = brd.getThicknessAtPos(centerpos);
	double noseThickness = brd.getThicknessAtPos(nosepos);
	
	double tailRocker = brd.getRockerAtPos(0);
	double noseRocker = brd.getRockerAtPos(brd.getLength());
	double tail1Rocker = brd.getRockerAtPos(tailpos);
	double nose1Rocker = brd.getRockerAtPos(nosepos);
	
	currentY += brd.getMaxRocker()*scale +10;
	
	savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, scale);	
	
	//Draw lines from numbers
	g2d.setStroke(linestroke);
	line.setLine(tailpos, -(tailThickness+tail1Rocker), tailpos, -tail1Rocker);
	g2d.draw(line);
	line.setLine(centerpos, -(centerThickness), centerpos, 0);
	g2d.draw(line);
	line.setLine(nosepos, -(noseThickness+nose1Rocker), nosepos, -nose1Rocker);
	g2d.draw(line);
	
	g2d.setColor(Color.YELLOW);
	line.setLine(0, -(tailRocker), 0, 0);
	g2d.draw(line);
	line.setLine(tailpos, -(tail1Rocker), tailpos, 0);
	g2d.draw(line);
	line.setLine(nosepos, -(nose1Rocker), nosepos, 0);
	g2d.draw(line);
	line.setLine(brd.getLength(), -(noseRocker), brd.getLength(), 0);
	g2d.draw(line);
	
	g2d.setTransform(savedTransform);
	
	BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getDeck(), BezierBoardDrawUtil.FlipY, false);
	BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, scale, Color.BLACK, stroke, brd.getBottom(), BezierBoardDrawUtil.FlipY, false);
	
	currentY += hgt + 5;
	
	g2d.setColor(Color.PINK);
	
	//Draw thickness labels
	String tailThicknessString = UnitUtils.convertLengthToCurrentUnit(tailThickness, false);
	double tailthicknessstringlen = metrics.stringWidth(tailThicknessString);
	g.drawString(tailThicknessString, (int)(xm+5 +(tailpos*scale - tailthicknessstringlen/2)), currentY);
	
	String CenterThicknessString = UnitUtils.convertLengthToCurrentUnit(centerThickness, false);
	double Centerthicknessstringlen = metrics.stringWidth(CenterThicknessString);
	g.drawString(CenterThicknessString, (int)(xm+5 +(centerpos*scale - Centerthicknessstringlen/2)), currentY);
	
	String NoseThicknessString = UnitUtils.convertLengthToCurrentUnit(noseThickness, false);
	double Nosethicknessstringlen = metrics.stringWidth(NoseThicknessString);
	g.drawString(NoseThicknessString, (int)(xm+5 +(nosepos*scale - Nosethicknessstringlen/2)), currentY);
	
	currentY += hgt + 5;
	
	//Draw rocker labels
	g2d.setColor(Color.CYAN);
	String tailRockerString = UnitUtils.convertLengthToCurrentUnit(tailRocker, false);
	double tailRockerstringlen = metrics.stringWidth(tailRockerString);
	g.drawString(tailRockerString, xm+5, currentY);
	
	String tail1RockerString = UnitUtils.convertLengthToCurrentUnit(tail1Rocker, false);
	double tail1Rockerstringlen = metrics.stringWidth(tail1RockerString);
	g.drawString(tail1RockerString, (int)(xm+5 +(tailpos*scale - tail1Rockerstringlen/2)), currentY);
	
	String nose1RockerString = UnitUtils.convertLengthToCurrentUnit(nose1Rocker, false);
	double nose1Rockerstringlen = metrics.stringWidth(nose1RockerString);
	g.drawString(nose1RockerString, (int)(xm+5 +(nosepos*scale - nose1Rockerstringlen/2)), currentY);
	
	String noseRockerString = UnitUtils.convertLengthToCurrentUnit(noseRocker, false);
	double noseRockerstringlen = metrics.stringWidth(noseRockerString);
	g.drawString(noseRockerString, (int)(xm+5 +(brd.getLength()*scale - noseRockerstringlen)), currentY);
	
	currentY += brd.getThickness()*scale + 5;
	
	BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+width/2,	currentY, Math.PI/2.0, scale, Color.BLACK, linestroke, false, true, brd.getLength()/2, brd);
	
	currentY += hgt;
	
	String centerString = LanguageResource.getString("CENTER_STR");
	double centerStringLen = metrics.stringWidth(centerString);
	g.drawString(centerString, (int)(xm+5 + width/2 - centerStringLen/2), currentY);
	
	currentY += hgt+10;
	
	//Draw
	BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((width/3)*1),	currentY, Math.PI/2.0, scale, Color.BLACK, linestroke, false, true, UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, brd);
	
	BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((width/3)*2),	currentY, Math.PI/2.0, scale, Color.BLACK, linestroke, false, true, brd.getLength() - UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, brd);
	
	currentY += hgt;
	String tailString = LanguageResource.getString("TAIL_STR");
	double tailStringLen = metrics.stringWidth(tailString);
	g.drawString(tailString, (int)(xm+5 + ((width/3)*1) - tailStringLen/2), currentY);
				
	String noseString = LanguageResource.getString("NOSE_STR");
	double noseStringLen = metrics.stringWidth(noseString);
	g.drawString(noseString, (int)(xm+5 + ((width/3)*2) - noseStringLen/2), currentY);
	
	currentY += hgt+10;
	
	if(brd.getFins()[2] != 0 || brd.getFins()[5] != 0 || brd.getFinType() != "")
	{
		g.drawString(LanguageResource.getString("FINS_STR"), xm, currentY);
		currentY += hgt+5;
		
		if(brd.getFinType() != "")
		{
			g.drawString(LanguageResource.getString("FINTYPE_STR") + brd.getFinType(), xm, currentY);
			currentY += hgt+5;					
		}
		
		if(brd.getFins()[5] != 0)
		{
			g.drawString(LanguageResource.getString("CENTERFIN_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[5], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[4], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[6], false), xm, currentY);
			currentY += hgt+5;					
		}
	
		if(brd.getFins()[2] != 0)
		{
			g.drawString(LanguageResource.getString("SIDEFINS_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[2], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[0], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[7],false) + LanguageResource.getString("TOEIN_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[3]-brd.getFins()[1],false) + LanguageResource.getString("SPLAY_STR") + UnitUtils.convertLengthToCurrentUnit(brd.getFins()[8],false), xm, currentY);
			currentY += hgt+5;					
		}
	
		currentY += hgt+10;
	}
	
	g.drawString("Comments:", xm, currentY);
	currentY += hgt+2;
	int currentX = xm;
	
	String[] lineStrings = brd.getComments().split("\n");
	
	double spacelen = metrics.stringWidth(" ");
	
	for(int i = 0; i < lineStrings.length; i++)
	{
		String[] commentsStrings = lineStrings[i].split(" ");
		
		for(int j = 0; j < commentsStrings.length; j++)
		{
			
			double strlen = metrics.stringWidth(commentsStrings[j]);
			if(currentX + strlen > xm+width)
			{
				currentY += hgt+2;
				currentX = xm;
			}
			g.drawString(commentsStrings[j]+" ", currentX, currentY);
			
			currentX += strlen + spacelen;
		}
		currentY += hgt+2;										
		currentX = xm;
	}
	
	
	return 0;

}
}
