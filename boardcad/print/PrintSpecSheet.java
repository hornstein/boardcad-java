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

import cadcore.UnitUtils;

import board.BezierBoard;
import boardcad.gui.jdk.BezierBoardDrawUtil;
import boardcad.gui.jdk.BoardCAD;
import boardcad.gui.jdk.JavaDraw;
import boardcad.i18n.LanguageResource;



public class PrintSpecSheet extends JComponent implements Printable {

	static final long serialVersionUID=1L;

	private PageFormat myPageFormat;

	private Font mPrintFontNormal = new Font("Dialog", Font.PLAIN, 10);
	private Font mPrintFontLarge = new Font("Dialog", Font.PLAIN, 30);

	private String mLogoFileName = "";
	private boolean mBoardVertical = false;
	private boolean mOverlayCrossSections = false;
	private boolean mUseDetailedMeasurements = false;
	
	private BezierBoard mBrd = null;
	
	private float mImagableHeight = 0;
	private float mImagableWidth = 0;
	private float mImagableX = 0;
	private float mImagableY = 0;
	
	FontMetrics mLargeFontMetrics;
	FontMetrics mNormalFontMetrics;
	int mLargeFontHeight;
	int mNormalFontHeight;
	
	float mScale = 1.0f;

	/** Creates and initializes the ClickMe component. */

	public PrintSpecSheet() {

//		Hint at good sizes for this component.

		setPreferredSize(new Dimension(800, 600));

		setMinimumSize(new Dimension(600, 480));

//		Request a black line around this component.

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

	}


	public BezierBoard getBrd() {

		return BoardCAD.getInstance().getCurrentBrd();

	}


	public void printSpecSheet() {

		PrinterJob printJob = PrinterJob.getPrinterJob();

		myPageFormat = printJob.pageDialog(printJob.defaultPage());
		if(myPageFormat == printJob.defaultPage())
			return;

		printJob.setPrintable(this,myPageFormat);

		try {

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
		
		mBrd = BoardCAD.getInstance().getCurrentBrd();

		JavaDraw jd = new JavaDraw(g2d);

		PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(dim.width, dim.height);
		paper.setImageableArea(0,0,dim.width, dim.height);
		format.setPaper(paper);
//		printDetailedSpecSheet(format, 0, g2d);
		printNewSpecSheet(format, 0, g2d);
//		BezierBoardDrawUtil.printOutline(jd, border, dim.height*1.0/5.0, (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),0.0,  true, BoardCAD.getInstance().getCurrentBrd(), false, false);
//		BezierBoardDrawUtil.printOutlineOverCurve(jd, border, dim.height*1.0/5.0, (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, true, BoardCAD.getInstance().getCurrentBrd(), false, false, false, false);

//		BezierBoardDrawUtil.printSpinTemplate(jd, border, dim.height*2.0/5.0,  (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()), 0.0, true, BoardCAD.getInstance().getCurrentBrd(), false, false);
//		BezierBoardDrawUtil.printSpinTemplateOverCurve(jd, border, dim.height*2.0/5.0, (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),0.0,  true, BoardCAD.getInstance().getCurrentBrd(), false, false);
		/*	
		BezierBoardDrawUtil.printProfile(jd, border, dim.height*3.0/5.0, (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),0.0,  true,BoardCAD.getInstance().getCurrentBrd(),false);
		BezierBoardDrawUtil.printRailTemplate(jd, border, dim.height*3.0/5.0, (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),0.0,  true,BoardCAD.getInstance().getCurrentBrd(), 3.0);

		for(int i = 1; i < BoardCAD.getInstance().getCurrentBrd().getCrossSections().size()-1; i++)
		{
			BezierBoardDrawUtil.printSlice(jd, border, dim.height*4.0/5.0, (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength()),0.0,  true,BoardCAD.getInstance().getCurrentBrd(), i,false);
		}

		final BezierSpline p = BoardCAD.getInstance().getCurrentBrd().getInterpolatedCrossSection(BoardCAD.getInstance().getCurrentBrd().getLength()/2.0).getBezierSpline();

	    MathUtils.Function func = new MathUtils.Function(){public double f(double tt){return  p.getNormalByTT(tt);}};

	    double mScale = (dim.mImagableWidth-(border*2))/(BoardCAD.getInstance().getCurrentBrd().getLength());

		BezierBoardDrawUtil.paintFunction(jd, border, dim.height/2.0, mScale,0.0,  new Color(0,0,0), new BasicStroke((float)(2.0/mScale)), func, 0.0, 1.0, 200.0, 10.0);
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
		if(printSpecSheet(pageFormat, pageIndex, g) == 0)
			return PAGE_EXISTS;		
		else
			return NO_SUCH_PAGE;

	}


	int printSpecSheet(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		if(pageIndex > 0)
			return -1;

		mBrd = BoardCAD.getInstance().getCurrentBrd();

		double pageWidth = pageFormat.getImageableWidth();
		double pageHeight = pageFormat.getImageableHeight();

		int xm = (int)pageFormat.getImageableX();
		int currentY = (int)pageFormat.getImageableY();

		int mImagableWidth = (int)pageFormat.getImageableWidth();
		int height = (int)pageFormat.getImageableHeight();

		mScale = (mImagableWidth-10)/(float)mBrd.getLength();

		Graphics2D g2d = (Graphics2D)g;

		FontMetrics metrics = g2d.getFontMetrics(mPrintFontLarge);
		// get the height of a line of text in this font and render context
		int hgt = metrics.getHeight();

		String fileString = LanguageResource.getString("FILE_STR") + mBrd.getFilename();
		String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getThickness(), true);
		String volumeString = LanguageResource.getString("VOLUME_STR") + UnitUtils.convertVolumeToCurrentUnit(mBrd.getVolume());
		String designerString = LanguageResource.getString("DESIGNER_STR") + mBrd.getDesigner();
		String modelString = LanguageResource.getString("MODEL_STR") + mBrd.getModel();
		String surferString = LanguageResource.getString("SURFER_STR") + mBrd.getSurfer();
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
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
		g.drawString(dateString, xm+(mImagableWidth/2), currentY);
		currentY += hgt+2;
		g.drawString(surferString, xm, currentY);
		g.drawString(modelString, xm+(mImagableWidth/2), currentY);
		currentY += hgt+2;
		g.drawString(measurementsString, xm, currentY);
		g.drawString(volumeString, xm+(mImagableWidth/2), currentY);
		currentY += hgt+2;
		g.drawString(fileString, xm, currentY);

		currentY += hgt+2;

		currentY += (mBrd.getCenterWidth()/2)*mScale;

		Stroke linestroke = new BasicStroke((float)(1.0/mScale));
		Line2D.Double line = new Line2D.Double();

		JavaDraw jd = new JavaDraw(g2d);
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, mScale, 0.0);	

		g2d.setStroke(linestroke);
		g2d.setColor(Color.GRAY);
		double tailpos = 12*UnitUtils.INCH;
		double tailbrdwidth = mBrd.getWidthAtPos(tailpos);
		line.setLine(tailpos, (-tailbrdwidth/2), tailpos, (tailbrdwidth/2));
		g2d.draw(line);
		double centerpos = mBrd.getLength()/2;
		double centerbrdwidth = mBrd.getWidthAtPos(centerpos);
		line.setLine(centerpos, (-centerbrdwidth/2), centerpos, (centerbrdwidth/2));
		g2d.draw(line);
		double nosepos = mBrd.getLength() - (12*UnitUtils.INCH);
		double nosebrdwidth = mBrd.getWidthAtPos(nosepos);
		line.setLine(nosepos, (-nosebrdwidth/2), nosepos, (nosebrdwidth/2));
		g2d.draw(line);

		//Stringer
		line.setLine(mBrd.getLength(), 0, 0, 0);
		g2d.draw(line);

		//Fins. x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of sidefin, splay angle
		/*			g2d.setColor(Color.LIGHT_GRAY);
		final Line2D tmp = new Line2D.Double();
		tmp.setLine(mBrd.getFins()[0],mBrd.getFins()[1],mBrd.getFins()[2],mBrd.getFins()[3]);
		g2d.draw(tmp);
		tmp.setLine(mBrd.getFins()[0],-mBrd.getFins()[1],mBrd.getFins()[2],-mBrd.getFins()[3]);
		g2d.draw(tmp);
		tmp.setLine(mBrd.getFins()[5],0,mBrd.getFins()[4],0);
		g2d.draw(tmp);
		 */

		g2d.setTransform(savedTransform);

		Stroke stroke = new BasicStroke((float)(1.5/mScale));
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getOutline(), BezierBoardDrawUtil.MirrorY, false);
		BezierBoardDrawUtil.paintFins(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getFins(), false,false);

		currentY += (mBrd.getCenterWidth()/2)*mScale + hgt + 5;

		g2d.setColor(Color.GRAY);
		String tailWidthString = UnitUtils.convertLengthToCurrentUnit(tailbrdwidth, false);
		double tailstringlen = metrics.stringWidth(tailWidthString);
		g.drawString(tailWidthString, (int)(xm+5 +(tailpos*mScale - tailstringlen/2)), currentY);

		String centerWidthString = UnitUtils.convertLengthToCurrentUnit(centerbrdwidth, false);
		double centerstringlen = metrics.stringWidth(centerWidthString);
		g.drawString(centerWidthString, (int)(xm+5 +(centerpos*mScale - centerstringlen/2)), currentY);

		String noseWidthString = UnitUtils.convertLengthToCurrentUnit(nosebrdwidth, false);
		double nosestringlen = metrics.stringWidth(noseWidthString);
		g.drawString(noseWidthString, (int)(xm+5 +(nosepos*mScale - nosestringlen/2)), currentY);

		double tailThickness = mBrd.getThicknessAtPos(tailpos);
		double centerThickness = mBrd.getThicknessAtPos(centerpos);
		double noseThickness = mBrd.getThicknessAtPos(nosepos);

		double tailRocker = mBrd.getRockerAtPos(0);
		double noseRocker = mBrd.getRockerAtPos(mBrd.getLength());
		double tail1Rocker = mBrd.getRockerAtPos(tailpos);
		double nose1Rocker = mBrd.getRockerAtPos(nosepos);

		currentY += mBrd.getMaxRocker()*mScale +10;

		savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, mScale, 0.0);	

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
		line.setLine(mBrd.getLength(), -(noseRocker), mBrd.getLength(), 0);
		g2d.draw(line);

		g2d.setTransform(savedTransform);

		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getDeck(), BezierBoardDrawUtil.FlipY, false);
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getBottom(), BezierBoardDrawUtil.FlipY, false);

		currentY += hgt + 5;

		g2d.setColor(Color.LIGHT_GRAY);

		String tailThicknessString = UnitUtils.convertLengthToCurrentUnit(tailThickness, false);
		double tailthicknessstringlen = metrics.stringWidth(tailThicknessString);
		g.drawString(tailThicknessString, (int)(xm+5 +(tailpos*mScale - tailthicknessstringlen/2)), currentY);

		String CenterThicknessString = UnitUtils.convertLengthToCurrentUnit(centerThickness, false);
		double Centerthicknessstringlen = metrics.stringWidth(CenterThicknessString);
		g.drawString(CenterThicknessString, (int)(xm+5 +(centerpos*mScale - Centerthicknessstringlen/2)), currentY);

		String NoseThicknessString = UnitUtils.convertLengthToCurrentUnit(noseThickness, false);
		double Nosethicknessstringlen = metrics.stringWidth(NoseThicknessString);
		g.drawString(NoseThicknessString, (int)(xm+5 +(nosepos*mScale - Nosethicknessstringlen/2)), currentY);

		currentY += hgt + 5;

		g2d.setColor(Color.GRAY);
		String tailRockerString = UnitUtils.convertLengthToCurrentUnit(tailRocker, false);
		double tailRockerstringlen = metrics.stringWidth(tailRockerString);
		g.drawString(tailRockerString, xm+5, currentY);

		String tail1RockerString = UnitUtils.convertLengthToCurrentUnit(tail1Rocker, false);
		double tail1Rockerstringlen = metrics.stringWidth(tail1RockerString);
		g.drawString(tail1RockerString, (int)(xm+5 +(tailpos*mScale - tail1Rockerstringlen/2)), currentY);

		String nose1RockerString = UnitUtils.convertLengthToCurrentUnit(nose1Rocker, false);
		double nose1Rockerstringlen = metrics.stringWidth(nose1RockerString);
		g.drawString(nose1RockerString, (int)(xm+5 +(nosepos*mScale - nose1Rockerstringlen/2)), currentY);

		String noseRockerString = UnitUtils.convertLengthToCurrentUnit(noseRocker, false);
		double noseRockerstringlen = metrics.stringWidth(noseRockerString);
		g.drawString(noseRockerString, (int)(xm+5 +(mBrd.getLength()*mScale - noseRockerstringlen)), currentY);

		currentY += mBrd.getThickness()*mScale + 5;

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+mImagableWidth/2,	currentY, mScale, 0.0, Color.BLACK, linestroke, false, true, mBrd.getLength()/2, mBrd);

		currentY += hgt;

		String centerString = LanguageResource.getString("CENTER_STR");
		double centerStringLen = metrics.stringWidth(centerString);
		g.drawString(centerString, (int)(xm+5 + mImagableWidth/2 - centerStringLen/2), currentY);

		currentY += hgt+10;

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((mImagableWidth/3)*1),	currentY, mScale, 0.0, Color.BLACK, linestroke, false, true, UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, mBrd);

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((mImagableWidth/3)*2),	currentY, mScale, 0.0, Color.BLACK, linestroke, false, true, mBrd.getLength() - UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, mBrd);

		currentY += hgt;
		String tailString = LanguageResource.getString("TAIL_STR");
		double tailStringLen = metrics.stringWidth(tailString);
		g.drawString(tailString, (int)(xm+5 + ((mImagableWidth/3)*1) - tailStringLen/2), currentY);

		String noseString = LanguageResource.getString("NOSE_STR");
		double noseStringLen = metrics.stringWidth(noseString);
		g.drawString(noseString, (int)(xm+5 + ((mImagableWidth/3)*2) - noseStringLen/2), currentY);

		currentY += hgt+10;

		if(mBrd.getFins()[2] != 0 || mBrd.getFins()[5] != 0 || mBrd.getFinType() != "")
		{
			g.drawString(LanguageResource.getString("FINS_STR"), xm, currentY);
			currentY += hgt+5;

			if(mBrd.getFinType() != "")
			{
				g.drawString(LanguageResource.getString("FINTYPE_STR") + mBrd.getFinType(), xm, currentY);
				currentY += hgt+5;					
			}

			if(mBrd.getFins()[5] != 0)
			{
				g.drawString(LanguageResource.getString("CENTERFIN_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[5], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[4], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[6], false), xm, currentY);
				currentY += hgt+5;					
			}

			if(mBrd.getFins()[2] != 0)
			{
				g.drawString(LanguageResource.getString("SIDEFINS_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[2], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[0], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[7],false) + LanguageResource.getString("TOEIN_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[3]-mBrd.getFins()[1],false) + LanguageResource.getString("SPLAY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[8],false), xm, currentY);
				currentY += hgt+5;					
			}

			currentY += hgt+10;
		}

		g.drawString("Comments:", xm, currentY);
		currentY += hgt+2;
		int currentX = xm;

		String[] lineStrings = mBrd.getComments().split("\n");

		double spacelen = metrics.stringWidth(" ");

		for(int i = 0; i < lineStrings.length; i++)
		{
			String[] commentsStrings = lineStrings[i].split(" ");

			for(int j = 0; j < commentsStrings.length; j++)
			{

				double strlen = metrics.stringWidth(commentsStrings[j]);
				if(currentX + strlen > xm+mImagableWidth)
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

		int mImagableWidth = (int)pageFormat.getImageableWidth();
		int height = (int)pageFormat.getImageableHeight();

		double mScale = (mImagableWidth-10)/mBrd.getLength();

		Graphics2D g2d = (Graphics2D)g;

		FontMetrics metrics = g2d.getFontMetrics(mPrintFontLarge);
		// get the height of a line of text in this font and render context
		int hgt = metrics.getHeight();

		String fileString = LanguageResource.getString("FILE_STR") + mBrd.getFilename();
		String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getThickness(), true);
		String volumeString = LanguageResource.getString("VOLUME_STR") + UnitUtils.convertVolumeToCurrentUnit(mBrd.getVolume());
		String designerString = LanguageResource.getString("DESIGNER_STR") + mBrd.getDesigner();
		String modelString = LanguageResource.getString("MODEL_STR") + mBrd.getModel();
		String surferString = LanguageResource.getString("SURFER_STR") + mBrd.getSurfer();
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
		g.drawString(dateString, xm+(mImagableWidth/2), currentY);
		currentY += hgt+2;
		g.drawString(surferString, xm, currentY);
		g.drawString(modelString, xm+(mImagableWidth/2), currentY);
		currentY += hgt+2;
		g.drawString(measurementsString, xm, currentY);
		g.drawString(volumeString, xm+(mImagableWidth/2), currentY);
		currentY += hgt+2;
		g.drawString(fileString, xm, currentY);

		currentY += hgt+2;

		currentY += (mBrd.getCenterWidth()/2)*mScale;

		Stroke linestroke = new BasicStroke((float)(1.0/mScale));
		Line2D.Double line = new Line2D.Double();

		JavaDraw jd = new JavaDraw(g2d);
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, mScale, 0.0);	

		//Outline Crossection lines
		g2d.setStroke(linestroke);
		g2d.setColor(Color.RED);
		double tailpos = 12*UnitUtils.INCH;
		double tailbrdwidth = mBrd.getWidthAtPos(tailpos);
		line.setLine(tailpos, (-tailbrdwidth/2), tailpos, (tailbrdwidth/2));
		g2d.draw(line);
		double centerpos = mBrd.getLength()/2;
		double centerbrdwidth = mBrd.getWidthAtPos(centerpos);
		line.setLine(centerpos, (-centerbrdwidth/2), centerpos, (centerbrdwidth/2));
		g2d.draw(line);
		double nosepos = mBrd.getLength() - (12*UnitUtils.INCH);
		double nosebrdwidth = mBrd.getWidthAtPos(nosepos);
		line.setLine(nosepos, (-nosebrdwidth/2), nosepos, (nosebrdwidth/2));
		g2d.draw(line);

		//Stringer
		line.setLine(mBrd.getLength(), 0, 0, 0);
		g2d.draw(line);

		//Fins. x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of sidefin, splay angle
		/*			g2d.setColor(Color.LIGHT_GRAY);
	final Line2D tmp = new Line2D.Double();
	tmp.setLine(mBrd.getFins()[0],mBrd.getFins()[1],mBrd.getFins()[2],mBrd.getFins()[3]);
	g2d.draw(tmp);
	tmp.setLine(mBrd.getFins()[0],-mBrd.getFins()[1],mBrd.getFins()[2],-mBrd.getFins()[3]);
	g2d.draw(tmp);
	tmp.setLine(mBrd.getFins()[5],0,mBrd.getFins()[4],0);
	g2d.draw(tmp);
		 */

		g2d.setTransform(savedTransform);

		Stroke stroke = new BasicStroke((float)(1.5/mScale));
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getOutline(), BezierBoardDrawUtil.MirrorY, false);
		BezierBoardDrawUtil.paintFins(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getFins(), false,false);

		currentY += (mBrd.getCenterWidth()/2)*mScale + hgt + 5;

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
				pos = mBrd.getMaxWidthPos();
			}
			else
			{
				pos = mBrd.getLength() + (i-6)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
			}

			double currentWidth = mBrd.getWidthAt(pos);

			String widthString = UnitUtils.convertLengthToCurrentUnit(currentWidth, false);
			double widthStringLength = metrics.stringWidth(widthString);
			g.drawString(widthString, (int)(xm+5 +(pos*mScale - widthStringLength/2)), currentY);

		}

		double tailThickness = mBrd.getThicknessAtPos(tailpos);
		double centerThickness = mBrd.getThicknessAtPos(centerpos);
		double noseThickness = mBrd.getThicknessAtPos(nosepos);

		double tailRocker = mBrd.getRockerAtPos(0);
		double noseRocker = mBrd.getRockerAtPos(mBrd.getLength());
		double tail1Rocker = mBrd.getRockerAtPos(tailpos);
		double nose1Rocker = mBrd.getRockerAtPos(nosepos);

		currentY += mBrd.getMaxRocker()*mScale +10;

		savedTransform = BezierBoardDrawUtil.setTransform(jd, xm+5, currentY, mScale, 0.0);	

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
		line.setLine(mBrd.getLength(), -(noseRocker), mBrd.getLength(), 0);
		g2d.draw(line);

		g2d.setTransform(savedTransform);

		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getDeck(), BezierBoardDrawUtil.FlipY, false);
		BezierBoardDrawUtil.paintBezierSpline(jd, xm+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getBottom(), BezierBoardDrawUtil.FlipY, false);

		currentY += hgt + 5;

		g2d.setColor(Color.PINK);

		//Draw thickness labels
		String tailThicknessString = UnitUtils.convertLengthToCurrentUnit(tailThickness, false);
		double tailthicknessstringlen = metrics.stringWidth(tailThicknessString);
		g.drawString(tailThicknessString, (int)(xm+5 +(tailpos*mScale - tailthicknessstringlen/2)), currentY);

		String CenterThicknessString = UnitUtils.convertLengthToCurrentUnit(centerThickness, false);
		double Centerthicknessstringlen = metrics.stringWidth(CenterThicknessString);
		g.drawString(CenterThicknessString, (int)(xm+5 +(centerpos*mScale - Centerthicknessstringlen/2)), currentY);

		String NoseThicknessString = UnitUtils.convertLengthToCurrentUnit(noseThickness, false);
		double Nosethicknessstringlen = metrics.stringWidth(NoseThicknessString);
		g.drawString(NoseThicknessString, (int)(xm+5 +(nosepos*mScale - Nosethicknessstringlen/2)), currentY);

		currentY += hgt + 5;

		//Draw rocker labels
		g2d.setColor(Color.CYAN);
		String tailRockerString = UnitUtils.convertLengthToCurrentUnit(tailRocker, false);
		double tailRockerstringlen = metrics.stringWidth(tailRockerString);
		g.drawString(tailRockerString, xm+5, currentY);

		String tail1RockerString = UnitUtils.convertLengthToCurrentUnit(tail1Rocker, false);
		double tail1Rockerstringlen = metrics.stringWidth(tail1RockerString);
		g.drawString(tail1RockerString, (int)(xm+5 +(tailpos*mScale - tail1Rockerstringlen/2)), currentY);

		String nose1RockerString = UnitUtils.convertLengthToCurrentUnit(nose1Rocker, false);
		double nose1Rockerstringlen = metrics.stringWidth(nose1RockerString);
		g.drawString(nose1RockerString, (int)(xm+5 +(nosepos*mScale - nose1Rockerstringlen/2)), currentY);

		String noseRockerString = UnitUtils.convertLengthToCurrentUnit(noseRocker, false);
		double noseRockerstringlen = metrics.stringWidth(noseRockerString);
		g.drawString(noseRockerString, (int)(xm+5 +(mBrd.getLength()*mScale - noseRockerstringlen)), currentY);

		currentY += mBrd.getThickness()*mScale + 5;

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+mImagableWidth/2,	currentY, Math.PI/2.0, mScale, Color.BLACK, linestroke, false, true, mBrd.getLength()/2, brd);

		currentY += hgt;

		String centerString = LanguageResource.getString("CENTER_STR");
		double centerStringLen = metrics.stringWidth(centerString);
		g.drawString(centerString, (int)(xm+5 + mImagableWidth/2 - centerStringLen/2), currentY);

		currentY += hgt+10;

		//Draw
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((mImagableWidth/3)*1),	currentY, mScale, Math.PI/2.0, Color.BLACK, linestroke, false, true, UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, brd);

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, xm+5+((mImagableWidth/3)*2),	currentY, mScale, Math.PI/2.0, Color.BLACK, linestroke, false, true, mBrd.getLength() - UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, brd);

		currentY += hgt;
		String tailString = LanguageResource.getString("TAIL_STR");
		double tailStringLen = metrics.stringWidth(tailString);
		g.drawString(tailString, (int)(xm+5 + ((mImagableWidth/3)*1) - tailStringLen/2), currentY);

		String noseString = LanguageResource.getString("NOSE_STR");
		double noseStringLen = metrics.stringWidth(noseString);
		g.drawString(noseString, (int)(xm+5 + ((mImagableWidth/3)*2) - noseStringLen/2), currentY);

		currentY += hgt+10;

		if(mBrd.getFins()[2] != 0 || mBrd.getFins()[5] != 0 || mBrd.getFinType() != "")
		{
			g.drawString(LanguageResource.getString("FINS_STR"), xm, currentY);
			currentY += hgt+5;

			if(mBrd.getFinType() != "")
			{
				g.drawString(LanguageResource.getString("FINTYPE_STR") + mBrd.getFinType(), xm, currentY);
				currentY += hgt+5;					
			}

			if(mBrd.getFins()[5] != 0)
			{
				g.drawString(LanguageResource.getString("CENTERFIN_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[5], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[4], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[6], false), xm, currentY);
				currentY += hgt+5;					
			}

			if(mBrd.getFins()[2] != 0)
			{
				g.drawString(LanguageResource.getString("SIDEFINS_STR") + LanguageResource.getString("FRONT_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[2], false) + LanguageResource.getString("REAR_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[0], false) + LanguageResource.getString("DEPTH_STR")  + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[7],false) + LanguageResource.getString("TOEIN_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[3]-mBrd.getFins()[1],false) + LanguageResource.getString("SPLAY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getFins()[8],false), xm, currentY);
				currentY += hgt+5;					
			}

			currentY += hgt+10;
		}

		g.drawString("Comments:", xm, currentY);
		currentY += hgt+2;
		int currentX = xm;

		String[] lineStrings = mBrd.getComments().split("\n");

		double spacelen = metrics.stringWidth(" ");

		for(int i = 0; i < lineStrings.length; i++)
		{
			String[] commentsStrings = lineStrings[i].split(" ");

			for(int j = 0; j < commentsStrings.length; j++)
			{

				double strlen = metrics.stringWidth(commentsStrings[j]);
				if(currentX + strlen > xm+mImagableWidth)
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
	
	//New stuff
	int printNewSpecSheet(PageFormat pageFormat, int pageIndex, Graphics g)
	{
		if(pageIndex > 0)
			return -1;
	
		Graphics2D g2d = (Graphics2D)g;
		
		mImagableHeight = (float)pageFormat.getImageableHeight();
		mImagableWidth = (float)pageFormat.getImageableWidth();
		mImagableX = (float)pageFormat.getImageableX();
		mImagableY = (float)pageFormat.getImageableY();
		
		mLargeFontMetrics = g2d.getFontMetrics(mPrintFontLarge);
		mLargeFontHeight = mLargeFontMetrics.getHeight();
		mNormalFontMetrics = g2d.getFontMetrics(mPrintFontNormal);
		mNormalFontHeight = mNormalFontMetrics.getHeight();

		mScale = (mImagableWidth-10)/(float)mBrd.getLength();
		
		if(mLogoFileName != "")
		{
			printLogo(g2d);
		}
		
		float specsBottom = 0.0f;
		float boardBottom = 0.0f;
		if(mBoardVertical){
			specsBottom = printBoardSpecsVertical(g2d, mImagableY);
			boardBottom = printBoardVertical(g2d, specsBottom);
			printBoardMeasurementsVertical(g2d, specsBottom);
		}
		else{
			specsBottom = printBoardSpecsHorizontal(g2d, mImagableY);
			boardBottom = printBoardHorizontal(g2d, specsBottom);
			printBoardMeasurementsHorizontal(g2d, specsBottom);	
		}
		
		if(mOverlayCrossSections){
			printOverlayedCrossSections(g2d, specsBottom);			
		}
		else{
			printCrossSections(g2d, boardBottom);
		}
		
		printComment(g2d);
		
		return 0;
	}

	void printLogo(Graphics2D g2d){
		
	}
	
	float printBoardVertical(Graphics2D g2d, float currentY){
		
		return currentY;
	}
	
	float printBoardHorizontal(Graphics2D g2d, float currentY)
	{
		JavaDraw jd = new JavaDraw(g2d);
		
		currentY += (mBrd.getCenterWidth()/2)*mScale;

		//Outline
		Stroke stroke = new BasicStroke((float)(1.5/mScale));
		BezierBoardDrawUtil.paintBezierSpline(jd, mImagableX+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getOutline(), BezierBoardDrawUtil.MirrorY, false);
		BezierBoardDrawUtil.paintFins(jd, mImagableX+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getFins(), false, false);

		//Stringer
		Line2D.Double line = new Line2D.Double();
		line.setLine(mImagableX+5, currentY, mImagableX+5 + mBrd.getLength()*mScale, currentY);
		g2d.draw(line);

		currentY += (mBrd.getCenterWidth()/2)*mScale + mBrd.getMaxRocker()*mScale;
		
		//Profile
		BezierBoardDrawUtil.paintBezierSpline(jd, mImagableX+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getDeck(), BezierBoardDrawUtil.FlipY, false);
		BezierBoardDrawUtil.paintBezierSpline(jd, mImagableX+5,	currentY, mScale, 0.0, Color.BLACK, stroke, mBrd.getBottom(), BezierBoardDrawUtil.FlipY, false);

		return currentY;
	}

	float printBoardMeasurementsVertical(Graphics2D g2d, float currentY){
		return currentY;
	}
	
	float printBoardMeasurementsHorizontal(Graphics2D g2d, float currentY){

		//Outline texts		
		currentY += (mBrd.getCenterWidth()/2)*mScale;

		g2d.setFont(mPrintFontNormal);
		
		g2d.setColor(Color.BLUE);

		Line2D.Double line = new Line2D.Double();
		for(int i = 0; i < (mUseDetailedMeasurements?7:3); i++)
		{
			double pos = 0.0;
			if(mUseDetailedMeasurements)
			{
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
					pos = mBrd.getMaxWidthPos();
				}
				else
				{
					pos = mBrd.getLength() + (i-6)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
				}
			}
			else
			{
				if(i == 0)
				{
					pos = UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
				}
				else if(i == 1)
				{
					pos = mBrd.getMaxWidthPos();
				}
				else
				{
					pos = mBrd.getLength() - UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
				}
				
			}

			double currentWidth = mBrd.getWidthAt(pos);

			String widthString = UnitUtils.convertLengthToCurrentUnit(currentWidth, false);
			double widthStringLength = mNormalFontMetrics.stringWidth(widthString);
			
			float textX = 0.0f; 
			if(mUseDetailedMeasurements)
			{
				if(i == 0)
				{
					textX = (float)(mImagableX+5 +(pos*mScale));
				}
				else if(i == 6)
				{
					textX = (float)(mImagableX+5 +(pos*mScale - widthStringLength));
				}
				else{
					textX = (float)(mImagableX+5 +(pos*mScale - widthStringLength/2));					
				}
			}
			else{
				textX = (float)(mImagableX+5 +(pos*mScale - widthStringLength/2));
			}
			g2d.drawString(widthString, textX, (float)(currentY + (mBrd.getCenterWidth()/2)*mScale + 15));

			Stroke linestroke = new BasicStroke((float)(3.0/mScale));
			g2d.setStroke(linestroke);

			line.setLine(pos*mScale, currentY + (-currentWidth/2)*mScale, pos*mScale, currentY + (currentWidth/2)*mScale);
			g2d.draw(line);

		}
		
		return currentY;
				
	}

	float printBoardSpecsVertical(Graphics2D g2d, double currentY){
		return 0.0f;
	}
	
	float printBoardSpecsHorizontal(Graphics2D g2d, float currentY){
		
		String fileString = LanguageResource.getString("FILE_STR") + mBrd.getFilename();
		String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(mBrd.getThickness(), true);
		String volumeString = LanguageResource.getString("VOLUME_STR") + UnitUtils.convertVolumeToCurrentUnit(mBrd.getVolume());
		String designerString = LanguageResource.getString("DESIGNER_STR") + mBrd.getDesigner();
		String modelString = LanguageResource.getString("MODEL_STR") + mBrd.getModel();
		String surferString = LanguageResource.getString("SURFER_STR") + mBrd.getSurfer();
		SimpleDateFormat format = new SimpleDateFormat("dd.mm.yyyy");
		String dateString = LanguageResource.getString("DATE_STR") + format.format(new Date());

		g2d.setColor(Color.BLACK);
		//g2d.setStroke(new BasicStroke((float)(1.0/mScale)));

		g2d.setFont(mPrintFontNormal);

		currentY += 20;	//Top margin
		
		g2d.drawString(designerString, mImagableX+(mImagableWidth/2), currentY);
		currentY += mNormalFontHeight+2;
		g2d.drawString(modelString, mImagableX+(mImagableWidth/2), currentY);
		currentY += mNormalFontHeight+2;
		g2d.drawString(surferString, mImagableX+(mImagableWidth/2), currentY);
		currentY += mNormalFontHeight+2;
		g2d.drawString(dateString, mImagableX+(mImagableWidth/2), currentY);
		currentY += mNormalFontHeight+2;
		g2d.drawString(measurementsString, mImagableX+(mImagableWidth/2), currentY);
		currentY += mNormalFontHeight+2;
		g2d.drawString(fileString, mImagableX+(mImagableWidth/2), currentY);

		currentY += mNormalFontHeight+2;
		
//		g2d.drawString(volumeString, mImagableX+(mImagableWidth/2), currentY);
//		currentY += mNormalFontHeight+2;

		return currentY;
	}
	
	float printCrossSections(Graphics2D g2d, float currentY)
	{		
		currentY += 80; //margin

		JavaDraw jd = new JavaDraw(g2d);

		Stroke linestroke = new BasicStroke((float)(1.5/mScale));
		
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, mImagableX+5+mImagableWidth/2,	currentY,  mScale, 0, Color.BLACK, linestroke, false, true, mBrd.getLength()/2, mBrd);
		
		g2d.setFont(mPrintFontNormal);

		currentY += mNormalFontHeight;

		String centerString = LanguageResource.getString("CENTER_STR");
		double centerStringLen = mNormalFontMetrics.stringWidth(centerString);
		g2d.drawString(centerString, (float)(mImagableX+5 + mImagableWidth/2 - centerStringLen/2), currentY);

		currentY += mBrd.getMaxThickness()*mScale;

		//Draw
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, mImagableX+5+((mImagableWidth/3)*1),	currentY, mScale, 0.0,  Color.BLACK, linestroke, false, true, UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, mBrd);

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, mImagableX+5+((mImagableWidth/3)*2),	currentY, mScale, 0.0,  Color.BLACK, linestroke, false, true, mBrd.getLength() - UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, mBrd);

		currentY += mNormalFontHeight;
		String tailString = LanguageResource.getString("TAIL_STR");
		double tailStringLen = mNormalFontMetrics.stringWidth(tailString);
		g2d.drawString(tailString, (int)(mImagableX+5 + ((mImagableWidth/3)*1) - tailStringLen/2), currentY);

		String noseString = LanguageResource.getString("NOSE_STR");
		double noseStringLen = mNormalFontMetrics.stringWidth(noseString);
		g2d.drawString(noseString, (int)(mImagableX+5 + ((mImagableWidth/3)*2) - noseStringLen/2), currentY);
	
		return currentY;
	}
	
	float printOverlayedCrossSectionsVertical(Graphics2D g2d, float currentY){
		
		return 0.0f;
	}
	
	float printOverlayedCrossSections(Graphics2D g2d, float currentY)
	{
		JavaDraw jd = new JavaDraw(g2d);

		Stroke linestroke = new BasicStroke((float)(2.0/mScale));
		
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, mImagableX+5+mImagableWidth/2,	currentY, mScale, Math.PI/2.0, Color.BLACK, linestroke, false, true, mBrd.getLength()/2, mBrd);
		
		g2d.setFont(mPrintFontNormal);

		currentY += mNormalFontHeight;

		String centerString = LanguageResource.getString("CENTER_STR");
		double centerStringLen = mNormalFontMetrics.stringWidth(centerString);
		g2d.drawString(centerString, (int)(mImagableX+5 + mImagableWidth/2 - centerStringLen/2), currentY);

		currentY += mNormalFontHeight+10;

		//Draw
		BezierBoardDrawUtil.paintSlidingCrossSection(jd, mImagableX+5+((mImagableWidth/3)*1),	currentY, mScale, Math.PI/2.0, Color.BLACK, linestroke, false, true, UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, mBrd);

		BezierBoardDrawUtil.paintSlidingCrossSection(jd, mImagableX+5+((mImagableWidth/3)*2),	currentY, mScale, Math.PI/2.0, Color.BLACK, linestroke, false, true, mBrd.getLength() - UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH, mBrd);

		currentY += mNormalFontHeight;
		String tailString = LanguageResource.getString("TAIL_STR");
		double tailStringLen = mNormalFontMetrics.stringWidth(tailString);
		g2d.drawString(tailString, (int)(mImagableX+5 + ((mImagableWidth/3)*1) - tailStringLen/2), currentY);

		String noseString = LanguageResource.getString("NOSE_STR");
		double noseStringLen = mNormalFontMetrics.stringWidth(noseString);
		g2d.drawString(noseString, (int)(mImagableX+5 + ((mImagableWidth/3)*2) - noseStringLen/2), currentY);

		currentY += mNormalFontHeight+10;
		
		return currentY;
	}

	void printComment(Graphics2D g2d)
	{
		float currentY = mImagableY;
		currentY += 20;	//Top margin
		currentY += (mNormalFontHeight+2)*6;	//sPECS
		currentY += 20; //margin 

		currentY += mBrd.getCenterWidth()*mScale + mBrd.getMaxRocker()*mScale;

		currentY += mBrd.getMaxThickness()*3*mScale;
		
		currentY += 80; //margin

		currentY += mNormalFontHeight*2;
		
		currentY += 30;

		g2d.setFont(mPrintFontNormal);
		g2d.drawString("Comments:", mImagableX+5, currentY);
		currentY += mNormalFontHeight+2;
		float currentX = mImagableX +5;

		String[] lineStrings = mBrd.getComments().split("\n");

		double spacelen = mNormalFontMetrics.stringWidth(" ");

		for(int i = 0; i < lineStrings.length; i++)
		{
			String[] commentsStrings = lineStrings[i].split(" ");

			for(int j = 0; j < commentsStrings.length; j++)
			{

				double strlen = mNormalFontMetrics.stringWidth(commentsStrings[j]);
				if(currentX + strlen > mImagableX+mImagableWidth-10)
				{
					currentY += mNormalFontHeight+2;
					currentX = mImagableX + 5;
				}
				g2d.drawString(commentsStrings[j]+" ", currentX, currentY);

				currentX += strlen + spacelen;
			}
			currentY += mNormalFontHeight+2;										
			currentX = mImagableX + 5;
		}

	}
	
}
