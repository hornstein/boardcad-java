package boardcad.gui.jdk;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

import javax.vecmath.*;

import board.*;
import cadcore.*;
import cadcore.MathUtils.Function;
import boardcad.*;

public class BezierBoardDrawUtil {
	static final long serialVersionUID=1L;
	
	static public int FlipX = 0x01;
	static public int FlipY = 0x02;
	static public int MirrorX = 0x010;
	static public int MirrorY = 0x100;
	static public int Vertical = 0x1000;

	static Line2D.Double ls = new Line2D.Double();
	static public final double CURVATURE_TOLERANCE = 0.0005;
	static public final double CURVATURE_T_TOLERANCE = 0.005;
	static public final double CURVATURE_MIN_LINE_LENGTH = 0.2;
	static public final double CURVATURE_MAX_LINE_LENGTH = 1.0;
	static public final double SLIDING_CROSS_SECTION_TOLERANCE = 0.0000001;
	static public final double SLIDING_CROSS_SECTION_MIN_LINE_LENGTH = 0.05;
	static public final double SLIDING_CROSS_SECTION_MAX_LINE_LENGTH = 1.0;
	static public final double SLIDING_CROSS_MIN_SPLIT = 0.00001;
	static public final double S_BLEND_MIN_ANGLE = 2*(Math.PI/180.0) ;
	static public final double S_BLEND_MAX_LENGTH = 1.0;
	static public final double S_BLEND_MIN_LENGTH = 0.1;
	static public final double TUCK_UNDER_DEFINITION_ANGLE = 175.0;
	static public final double APEX_DEFINITION_ANGLE = 90;

	 static final Vector3d upVector = new Vector3d(0.0, 0.0, 1.0);
		
	 //Shared variables so we don't need to pass them all the time
	static double mMulX;
	static double mMulY;
	static BezierBoard mBrd;


	public static GeneralPath makeBezierPathFromControlPoints(BezierSpline spline, boolean flipX, boolean flipY, boolean vertical, boolean reverse)
	{
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

		if(spline == null)
			return path;

		if(spline.getNrOfControlPoints() == 0)
			return path;

		double sx;

		double sy;

		double scx;

		double scy;

		double ecx;

		double ecy;

		double ex;

		double ey;
		
		double tmp;

		double mulX = flipX?-1:1;
		double mulY= flipY?-1:1;

		if(reverse)
		{

	//		Start
			sx = spline.getControlPoint(spline.getNrOfControlPoints() - 1).getEndPoint().x;
			sy = spline.getControlPoint(spline.getNrOfControlPoints() - 1).getEndPoint().y;
					
			if(vertical)
			{
				tmp = sx;
				sx = sy;
				sy = tmp;			
			}
	
			sx *= mulX;
			sy *= mulY;
	
	
			path.moveTo((float)(sx),(float)(sy));
	
			for(int i = spline.getNrOfControlPoints() - 1; i > 0; i--) {
	
	//			Start control point
				scx = spline.getControlPoint(i).getTangentToPrev().x;
				scy = spline.getControlPoint(i).getTangentToPrev().y;
	
	//			End control point
				ecx = spline.getControlPoint(i-1).getTangentToNext().x;
				ecy = spline.getControlPoint(i-1).getTangentToNext().y;
	
	//			End point
				ex = spline.getControlPoint(i-1).getEndPoint().x;
				ey = spline.getControlPoint(i-1).getEndPoint().y;
				
				if(vertical)
				{
					tmp = scx;
					scx = scy;
					scy = tmp;
	
					tmp = ecx;
					ecx = ecy;
					ecy = tmp;
	
					tmp = ex;
					ex = ey;
					ey = tmp;
				}
	
				path.curveTo((float)(scx*mulX), (float)(scy*mulY), (float)(ecx*mulX), (float)(ecy*mulY), (float)(ex*mulX), (float)(ey*mulY));
	
			}
		}
		else
		{

			//		Start
					sx = spline.getControlPoint(0).getEndPoint().x;
					sy = spline.getControlPoint(0).getEndPoint().y;
							
					if(vertical)
					{
						tmp = sx;
						sx = sy;
						sy = tmp;			
					}
			
					sx *= mulX;
					sy *= mulY;
			
			
					path.moveTo((float)(sx),(float)(sy));
			
					for(int i = 0; i < spline.getNrOfControlPoints()-1; i++) {
			
			//			Start control point
						scx = spline.getControlPoint(i).getTangentToNext().x;
						scy = spline.getControlPoint(i).getTangentToNext().y;
			
			
			
			//			End control point
						ecx = spline.getControlPoint(i+1).getTangentToPrev().x;
						ecy = spline.getControlPoint(i+1).getTangentToPrev().y;
			
			
			
			//			End point
						ex = spline.getControlPoint(i+1).getEndPoint().x;
						ey = spline.getControlPoint(i+1).getEndPoint().y;
						
						if(vertical)
						{
							tmp = scx;
							scx = scy;
							scy = tmp;
			
							tmp = ecx;
							ecx = ecy;
							ecy = tmp;
			
							tmp = ex;
							ex = ey;
							ey = tmp;
						}
			
						path.curveTo((float)(scx*mulX), (float)(scy*mulY), (float)(ecx*mulX), (float)(ecy*mulY), (float)(ex*mulX), (float)(ey*mulY));
			
					}
		}

		return path;
	}

	public static void paintPath(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, GeneralPath path, boolean fill)
	{
		if(path == null)
			return;

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	

		if(fill)
		{
			try{
				GeneralPath fillPath = (GeneralPath)path.clone();
				fillPath.closePath();
				d.setColor(Color.white);
				d.fill(fillPath);
			}
			catch(java.awt.geom.IllegalPathStateException e)
			{
			}
		}

		d.setColor(color);

		d.setStroke(stroke);

		d.draw(path);

		d.setTransform(savedTransform);
	}

	public static void paintPath(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, GeneralPath path, boolean fill)
	{
		if(path == null)
			return;

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);
		
		//Draw fill
		if(fill)
		{
			try
			{
				GeneralPath closedPath = new GeneralPath(path);
				closedPath.closePath();
				d.setColor(Color.white);
				d.fill(closedPath);
			}
			catch(java.awt.geom.IllegalPathStateException e)
			{
				//Ignore empty path
			}
		}
		
		//Draw path
		d.setColor(color);
		d.setStroke(stroke);
		d.draw(path);
				
		d.setTransform(savedTransform);
	}

	public static void fillPath(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, GeneralPath path)
	{
		if(path == null)
			return;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);	

		d.fill(path);
		
		d.setTransform(savedTransform);
	}

	public static void paintBezierSpline(AbstractDraw d, double offsetX,

			double offsetY, double scale, Color color, Stroke stroke, BezierSpline mPoints, int DrawControl, boolean fill)
	{
		if(mPoints == null)
			return;

		GeneralPath path = makeBezierPathFromControlPoints(mPoints, (DrawControl&BezierBoardDrawUtil.FlipX)!=0, (DrawControl&BezierBoardDrawUtil.FlipY)!=0, (DrawControl&BezierBoardDrawUtil.Vertical)!=0, false);
		paintPath(d, offsetX, offsetY, scale, color, stroke, path, fill);

		if((DrawControl&BezierBoardDrawUtil.MirrorX)!=0)
		{
			path = makeBezierPathFromControlPoints(mPoints, !((DrawControl&BezierBoardDrawUtil.FlipX)!=0), (DrawControl&BezierBoardDrawUtil.FlipY)!=0, (DrawControl&BezierBoardDrawUtil.Vertical)!=0, false);
			paintPath(d, offsetX, offsetY, scale, color, stroke, path, fill);
		}

		if((DrawControl&BezierBoardDrawUtil.MirrorY)!=0)
		{
			path = makeBezierPathFromControlPoints(mPoints, (DrawControl&BezierBoardDrawUtil.FlipX)!=0, !((DrawControl&BezierBoardDrawUtil.FlipY)!=0), (DrawControl&BezierBoardDrawUtil.Vertical)!=0, false);
			paintPath(d, offsetX, offsetY, scale, color, stroke, path, fill);
		}
	}

	public static void paintBezierSplines(AbstractDraw d, double offsetX,

			double offsetY, double scale, Color color, Stroke stroke, BezierSpline[] splines, int DrawControl, boolean fill)
	{
		if(splines == null)
			return;
		
		GeneralPath path = new GeneralPath();
		for(int i = 0; i < splines.length; i++)
		{
			GeneralPath tmp = makeBezierPathFromControlPoints(splines[i], (DrawControl&BezierBoardDrawUtil.FlipX)!=0, (DrawControl&BezierBoardDrawUtil.FlipY)!=0, (DrawControl&BezierBoardDrawUtil.Vertical)!=0, i%2!=0);
			path.append(tmp.getPathIterator(null), true);
		}
		paintPath(d, offsetX, offsetY, 0.0, scale, color, stroke, path, fill);
		
		if((DrawControl&BezierBoardDrawUtil.MirrorX)!=0)
		{
			path.reset();
			
			for(int i = 0; i < splines.length; i++)
			{
				GeneralPath tmp = makeBezierPathFromControlPoints(splines[i], !((DrawControl&BezierBoardDrawUtil.FlipX)!=0), (DrawControl&BezierBoardDrawUtil.FlipY)!=0, (DrawControl&BezierBoardDrawUtil.Vertical)!=0, i%2!=0);
				path.append(tmp.getPathIterator(null), true);
			}
			paintPath(d, offsetX, offsetY, 0.0, scale, color, stroke, path, fill);
		}

		if((DrawControl&BezierBoardDrawUtil.MirrorY)!=0)
		{
			path.reset();
			for(int i = 0; i < splines.length; i++)
			{
				GeneralPath tmp = makeBezierPathFromControlPoints(splines[i], (DrawControl&BezierBoardDrawUtil.FlipX)!=0, !((DrawControl&BezierBoardDrawUtil.FlipY)!=0), (DrawControl&BezierBoardDrawUtil.Vertical)!=0, i%2!=0);
				path.append(tmp.getPathIterator(null), true);
			}
			paintPath(d, offsetX, offsetY, 0.0, scale, color, stroke, path, fill);
		}
	}

	public static void paintBezierSpline(AbstractDraw d, double offsetX,

			double offsetY, double rotation, double scale, Color color, Stroke stroke, BezierSpline mPoints, int DrawControl, boolean fill)
	{
		if(mPoints == null)
			return;

		d.setColor(color);

		d.setStroke(stroke);

		//DEBUG
//AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);	
//d.draw(new Line2D.Double(0,0,0,0));
//d.setTransform(savedTransform);

		GeneralPath path = makeBezierPathFromControlPoints(mPoints, (DrawControl&BezierBoardDrawUtil.FlipX)!=0, (DrawControl&BezierBoardDrawUtil.FlipY)!=0, (DrawControl&BezierBoardDrawUtil.Vertical)!=0, false);
		paintPath(d, offsetX, offsetY, rotation, scale, color, stroke, path, fill);

		if((DrawControl&BezierBoardDrawUtil.MirrorX)!=0)
		{
			path = makeBezierPathFromControlPoints(mPoints, !((DrawControl&BezierBoardDrawUtil.FlipX)!=0), (DrawControl&BezierBoardDrawUtil.FlipY)!=0, (DrawControl&BezierBoardDrawUtil.Vertical)!=0, false);
			paintPath(d, offsetX, offsetY, rotation, scale, color, stroke, path, fill);
		}

		if((DrawControl&BezierBoardDrawUtil.MirrorY)!=0)
		{
			path = makeBezierPathFromControlPoints(mPoints, (DrawControl&BezierBoardDrawUtil.FlipX)!=0, !((DrawControl&BezierBoardDrawUtil.FlipY)!=0), (DrawControl&BezierBoardDrawUtil.Vertical)!=0, false);
			paintPath(d, offsetX, offsetY, rotation, scale, color, stroke, path, fill);
		}
	}

	public static void paintBezierControlPoints(AbstractDraw d, double offsetX,

			double offsetY, double scale, Stroke stroke, BezierSpline mPoints, ArrayList<BezierKnot> mSelectedControlPoints, boolean flipX, boolean flipY)
	{
		if(mPoints == null)
			return;

		double selectedCircleSize = BoardCAD.getInstance().getSelectedControlPointSize();
		double unselectedCircleSize = BoardCAD.getInstance().getUnselectedControlPointSize();

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	

		double cpx,cpy,kx,ky,cnx,cny;

		Line2D line = new Line2D.Double();
		Ellipse2D circle = new Ellipse2D.Double();

		Color selControlPointColor = BoardCAD.getInstance().getSelectedCenterControlPointColor();
		Color selTan1Color = (mSelectedControlPoints != null && mSelectedControlPoints.size() == 1)?BoardCAD.getInstance().getSelectedTangent1ControlPointColor():selControlPointColor;
		Color selTan2Color = (mSelectedControlPoints != null && mSelectedControlPoints.size() == 1)?BoardCAD.getInstance().getSelectedTangent2ControlPointColor():selControlPointColor;
		Color selOutControlPointColor = BoardCAD.getInstance().getSelectedOutlineCenterControlPointColor();
		Color selOutTan1Color = (mSelectedControlPoints != null && mSelectedControlPoints.size() == 1)?BoardCAD.getInstance().getSelectedOutlineTangent1ControlPointColor():selControlPointColor;
		Color selOutTan2Color = (mSelectedControlPoints != null && mSelectedControlPoints.size() == 1)?BoardCAD.getInstance().getSelectedOutlineTangent2ControlPointColor():selControlPointColor;

		Color unselControlPointColor = BoardCAD.getInstance().getUnselectedCenterControlPointColor();
		Color unselTan1Color = BoardCAD.getInstance().getUnselectedTangent1ControlPointColor();
		Color unselTan2Color = BoardCAD.getInstance().getUnselectedTangent2ControlPointColor();
		Color unselOutControlPointColor = BoardCAD.getInstance().getUnselectedOutlineCenterControlPointColor();
		Color unselOutTan1Color = BoardCAD.getInstance().getUnselectedOutlineTangent1ControlPointColor();
		Color unselOutTan2Color = BoardCAD.getInstance().getUnselectedOutlineTangent2ControlPointColor();

		Color selectedTangentColor = BoardCAD.getInstance().getSelectedTangentColor();
		Color unselectedTangentColor = BoardCAD.getInstance().getUnselectedTangentColor();

		boolean isControlPointSelected = false;
		int whichControlPointSelected = -1;

		for(int i = 0; i < mPoints.getNrOfControlPoints(); i++) {

			BezierKnot currentControlPoint = mPoints.getControlPoint(i);

			isControlPointSelected = mSelectedControlPoints != null && mSelectedControlPoints.contains(currentControlPoint);
			if(isControlPointSelected &&  mSelectedControlPoints.size() == 1 && BoardCAD.getInstance().getCurrentCommand().getClass().getName().compareTo(BrdEditCommand.class.getName())==0)
			{
				whichControlPointSelected = ((BrdEditCommand)BoardCAD.getInstance().getCurrentCommand()).mWhich;
			}
			else
			{
				whichControlPointSelected = -1;
			}
			
//			ControlPoint

			kx = currentControlPoint.getEndPoint().x;

			ky = currentControlPoint.getEndPoint().y;

//			control to previous

			cpx = currentControlPoint.getTangentToPrev().x;

			cpy = currentControlPoint.getTangentToPrev().y;

//			next control point

			cnx = currentControlPoint.getTangentToNext().x;

			cny = currentControlPoint.getTangentToNext().y;

//			ControlPoint
			d.setStroke(stroke);
			if(whichControlPointSelected==0 || (mSelectedControlPoints != null && mSelectedControlPoints.size() > 1 && mSelectedControlPoints.contains(currentControlPoint)))
			{
				circle.setFrameFromCenter(kx*mulX, ky*mulY, (kx*mulX)+(selectedCircleSize/scale), (ky*mulY)+(selectedCircleSize/scale));
				d.setColor(selControlPointColor);
				d.fill(circle);
				d.setColor(selOutControlPointColor);
				d.draw(circle);				
			}
			else
			{
				circle.setFrameFromCenter(kx*mulX, ky*mulY, (kx*mulX)+(unselectedCircleSize/scale), (ky*mulY)+(unselectedCircleSize/scale));
				d.setColor(unselControlPointColor);
				d.fill(circle);
				d.setColor(unselOutControlPointColor);
				d.draw(circle);				
			}

			//			Previous
			if(i !=0)
			{
				d.setColor(isControlPointSelected?selectedTangentColor:unselectedTangentColor);
				d.setStroke(stroke);
				line.setLine(cpx*mulX, cpy*mulY, kx*mulX, ky*mulY);
				d.draw(line);

				if(whichControlPointSelected==1)
				{
					circle.setFrameFromCenter(cpx*mulX, cpy*mulY, (cpx*mulX)+(selectedCircleSize/scale), (cpy*mulY)+(selectedCircleSize/scale));
					d.setColor(selTan1Color);
					d.fill(circle);
					d.setColor(selOutTan1Color);
					d.draw(circle);				
				}
				else
				{
					circle.setFrameFromCenter(cpx*mulX, cpy*mulY, (cpx*mulX)+(unselectedCircleSize/scale), (cpy*mulY)+(unselectedCircleSize/scale));
					d.setColor(unselTan1Color);
					d.fill(circle);
					d.setColor(unselOutTan1Color);
					d.draw(circle);				
				}

			}

			if(i != mPoints.getNrOfControlPoints()-1)
			{
				d.setColor(isControlPointSelected?selectedTangentColor:unselectedTangentColor);
				d.setStroke(stroke);
				line.setLine(kx*mulX, ky*mulY, cnx*mulX, cny*mulY);
				d.draw(line);

				if(whichControlPointSelected==2)
				{
					circle.setFrameFromCenter(cnx*mulX, cny*mulY, (cnx*mulX)+(selectedCircleSize/scale), (cny*mulY)+(selectedCircleSize/scale));
					d.setColor(selTan2Color);
					d.fill(circle);
					d.setColor(selOutTan2Color);
					d.draw(circle);				
				}
				else
				{
					circle.setFrameFromCenter(cnx*mulX, cny*mulY, (cnx*mulX)+(unselectedCircleSize/scale), (cny*mulY)+(unselectedCircleSize/scale));
					d.setColor(unselTan2Color);
					d.fill(circle);
					d.setColor(unselOutTan2Color);
					d.draw(circle);				
				}

			}
		}


		d.setTransform(savedTransform);
	}

	public static void paintBezierControlPoints(AbstractDraw d, double offsetX,

			double offsetY, double scale, Stroke stroke, BezierSpline[] splines, ArrayList<BezierKnot> mSelectedControlPoints, boolean flipX, boolean flipY)
	{
		if(splines == null)
			return;
		
		for(int i = 0; i < splines.length; i++)
		{
			paintBezierControlPoints( d, offsetX, offsetY, scale, stroke, splines[i], mSelectedControlPoints, flipX, flipY);
		}
		
	}

	public static void paintGuidePoints(AbstractDraw d, double offsetX,

			double offsetY, double scale, Color color, Stroke stroke, ArrayList<Point2D.Double> guidePoints, boolean flipX, boolean flipY)
	{

		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	

		d.setColor(BoardCAD.getInstance().getGuidePointColor());

		d.setStroke(new BasicStroke((float)(BoardCAD.getInstance().getGuidePointThickness()/scale)));

		double crossSize = 4.0f/scale;

		double mulX = (flipX?-1.0:1.0);
		double mulY = (flipY?-1.0:1.0);

		Line2D.Double line = new Line2D.Double();

		Point2D.Double pos = new Point2D.Double();;
		for(int i = 0; i < guidePoints.size(); i++)
		{
			pos.setLocation(guidePoints.get(i));
			line.setLine((pos.x-crossSize)*mulX, (pos.y)*mulY, (pos.x+crossSize)*mulX, (pos.y)*mulY);
			d.draw(line);
			line.setLine((pos.x)*mulX, (pos.y-crossSize)*mulY, (pos.x)*mulX, (pos.y+crossSize)*mulY);
			d.draw(line);
		}
		d.setTransform(savedTransform);	
	}

	public static void paintFins(AbstractDraw d, double offsetX,

			double offsetY, double scale, Color color, Stroke stroke, double[] finCoords, boolean flipX, boolean flipY)
	{
		AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(d, offsetX, offsetY, scale);	

		double mulX = (flipX)?-1:1;
		double mulY = (flipY)?-1:1;

		d.setColor(color);
		d.setStroke(stroke);

//		x, y for back of fin, x,y for front of fin, bac of center, front of center, depth of center, depth of sidefin, splay angle
		final Line2D tmp = new Line2D.Double();
		tmp.setLine(finCoords[0]*mulX,finCoords[1]*mulY,finCoords[2]*mulX,finCoords[3]*mulY);
		d.draw(tmp);
		tmp.setLine(finCoords[0]*mulX,-finCoords[1]*mulY,finCoords[2]*mulX,-finCoords[3]*mulY);
		d.draw(tmp);
		tmp.setLine(finCoords[5]*mulX,0,finCoords[4]*mulX,0);
		d.draw(tmp);

		d.setTransform(savedTransform);	
	}

	public static void paintCircle(AbstractDraw d, double offsetX,

			double offsetY, double scale, Color color, Stroke stroke, Point2D.Double pos, boolean flipX, boolean flipY)
	{
		double circleSize = 3.0;

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	

		Ellipse2D circle = new Ellipse2D.Double();

		d.setColor(color);
		d.setStroke(stroke);
		circle.setFrameFromCenter(pos.x*mulX, pos.y*mulY, (pos.x*mulX)+(circleSize/scale), (pos.y*mulY)+(circleSize/scale));
		d.draw(circle);

		d.setTransform(savedTransform);
	}

	public static void paintGrid(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, double verticalToCover, double horizontalToCover, boolean flipX, boolean flipY)
	{

		switch(UnitUtils.getCurrentUnit())
		{
		case UnitUtils.CENTIMETERS:
		case UnitUtils.MILLIMETERS:
			paintGridMetric(d, offsetX, offsetY, scale, color, verticalToCover, horizontalToCover, flipX, flipY);
			break;
		case UnitUtils.INCHES:
		case UnitUtils.INCHES_DECIMAL:
			paintGridImperial(d, offsetX, offsetY, scale, color, verticalToCover, horizontalToCover, flipX, flipY);
			break;
		}
	}

	private static void paintGridMetric(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, double verticalToCover, double horizontalToCover, boolean flipX, boolean flipY) 
	{

		int verticalLines = (int)verticalToCover + 1;
		verticalLines += 10 - verticalLines%10;
		int horizontalLines = (int)horizontalToCover + 1;
		horizontalLines += 10 - horizontalLines%10;

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	

		Line2D line = new Line2D.Double();
		for(int i = 0; i < verticalLines+1; i++)
		{

			setMetricGridLineStrokeAndColor(d, color, i, scale);

			line.setLine(i*1.0*mulX, 0*mulY, i*1.0*mulX, horizontalLines*1.0*mulY);
			d.draw(line);
		}

		for(int i = 0; i < horizontalLines+1; i++)
		{
			setMetricGridLineStrokeAndColor(d, color, i, scale);

			line.setLine(0*mulX, i*1.0*mulY, verticalLines*1.0*mulX, i*1.0*mulY);
			d.draw(line);
		}

		d.setTransform(savedTransform);
	}

	private static void paintGridImperial(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, double verticalToCover, double horizontalToCover, boolean flipX, boolean flipY) {

		int verticalLines = (int)(verticalToCover/UnitUtils.INCH) + 1;
		verticalLines += 6 - verticalLines%6;
		int horizontalLines = (int)(horizontalToCover/UnitUtils.INCH) + 1;
		horizontalLines += 6 - horizontalLines%6;

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	

		Line2D line = new Line2D.Double();
		for(int i = 0; i < verticalLines+1; i++)
		{

			setImperialGridLineStrokeAndColor(d, color, i, scale);

			line.setLine(i*2.54*mulX, 0*mulY, i*2.54*mulX, horizontalLines*2.54*mulY);
			d.draw(line);
		}

		for(int i = 0; i < horizontalLines+1; i++)
		{
			setImperialGridLineStrokeAndColor(d, color, i, scale);

			line.setLine(0*mulX, i*2.54*mulY, verticalLines*2.54*mulX, i*2.54*mulY);
			d.draw(line);
		}

		d.setTransform(savedTransform);
	}

	public static void paintCurvature(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierSpline bezierSpline, boolean flipX, boolean flipY, double curvature_scale) 
	{
		if(bezierSpline == null)
			return;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);

		for(int i = 0; i < bezierSpline.getNrOfCurves(); i++)
		{
			paintCurvatureByT(d, bezierSpline.getCurve(i),  0,1, flipX, flipY, curvature_scale);
		}


		
//		paintCurvatureByPos( 0,bezierPath.getMaxX(), flipX, flipY,curvature_scale);


		resetTransform(d,savedTransform);	  
	}

	public static void paintCurvature(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierSpline[] splines, boolean flipX, boolean flipY, double curvature_scale) 
	{
		if(splines == null)
			return;
		
		for(int i = 0; i < splines.length; i++)
		{
			paintCurvature(d, offsetX, offsetY, scale, color, stroke, splines[i], flipX, flipY, curvature_scale) ;
		}
		
	}

		
	public static void paintVolumeDistribution(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY, double graph_scale) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);
	
		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;
		int segments = 20;
		double step = brd.getLength()/segments;
		double lastVol = brd.getCrossSectionAreaAt(0, 8);
		for(int i = 1; i <= segments; i++)
		{
			double currentVol = brd.getCrossSectionAreaAt(i*step, 8);

			ls.setLine((i-1)*step*mulX, lastVol*mulY*graph_scale, i*step*mulX, currentVol*mulY*graph_scale);
			d.draw(ls);
			
			lastVol = currentVol;
		}


		resetTransform(d,savedTransform);	  
	}
	
	public static void paintOutlineFlowLines(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;
		int segments = 10;
		
		GeneralPath path = new GeneralPath();
		for(int j = 0; j < 3; j++)
		{
			d.setColor(color);

			path.reset();

			double angle = 10.0;
			if(j == 1)
				angle = 27.5;
			else if(j == 2)
				angle = 45.0;
						
			Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
			if(pos == null)
				continue;
			
			path.moveTo(pos.x*mulX, pos.y*mulY);
			ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
			for(int i = 0; i < crossSections.size(); i++)
			{
				double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
				double currentCrsPos = crossSections.get(i).getPosition();
				
				double step = (currentCrsPos-previousCrsPos)/segments;
				for(int k = 1; k <= segments; k++)
				{
					pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
					
					path.lineTo(pos.x*mulX, pos.y*mulY);				
				}
			}
			d.draw(path);
			
			color = color.brighter();
		}
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintOutlineTuckUnderLine(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);
	
		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;
		int segments = 1000;
		
		GeneralPath path = new GeneralPath();
		d.setColor(color);

		path.reset();

		double angle = 175.0;
		mulY = -mulY;
					
		Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
		if(pos == null)
			return;
		
		path.moveTo(pos.x*mulX, pos.y*mulY);
		ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
		for(int i = 0; i < crossSections.size(); i++)
		{
			double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
			double currentCrsPos = crossSections.get(i).getPosition();
			
			double step = (currentCrsPos-previousCrsPos)/segments;
			for(int k = 1; k <= segments; k++)
			{
				pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
				if(pos == null)
					return;
				
				path.lineTo(pos.x*mulX, pos.y*mulY);				
			}
		}
		d.draw(path);
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintProfileFlowLines(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);
		
		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;
		int segments = 10;
		
		GeneralPath path = new GeneralPath();
		for(int j = 0; j < 3; j++)
		{
			d.setColor(color);
			path.reset();

			double angle = 10.0;
			if(j == 1)
				angle = 27.5;
			else if(j == 2)
				angle = 45.0;
						
			Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
			if(pos == null)
				continue;
			
			path.moveTo(pos.x*mulX, pos.z*mulY);
			ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
			for(int i = 0; i < crossSections.size(); i++)
			{
				double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
				double currentCrsPos = crossSections.get(i).getPosition();
				
				double step = (currentCrsPos-previousCrsPos)/segments;
				for(int k = 1; k <= segments; k++)
				{
					pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
				
					path.lineTo(pos.x*mulX, pos.z*mulY);				
				}
			}
			d.draw(path);
			color = color.brighter();
		}
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintProfileApexline(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		paintProfileApexLine(d, offsetX, offsetY, 0.0, scale, color, stroke, brd, flipX, flipY);
	}

	public static void paintProfileApexLine(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);	
	
		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;
		int segments = 10;
		
		GeneralPath path = new GeneralPath();
		d.setColor(color);

		double	angle = APEX_DEFINITION_ANGLE;
						
		Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
		
		path.moveTo(pos.x*mulX, pos.z*mulY);
		ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
		for(int i = 0; i < crossSections.size(); i++)
		{
			double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
			double currentCrsPos = crossSections.get(i).getPosition();
			
			double step = (currentCrsPos-previousCrsPos)/segments;
			for(int k = 1; k <= segments; k++)
			{
				pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
			
				path.lineTo(pos.x*mulX, pos.z*mulY);				
			}
		}
		d.draw(path);
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintProfileTuckUnderLine(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		paintProfileTuckUnderLine(d, offsetX, offsetY, 0.0, scale, color, stroke, brd, flipX, flipY) ;
	}
	
	public static void paintProfileTuckUnderLine(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipX, boolean flipY) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);	

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;
		int segments = 10;
		
		GeneralPath path = new GeneralPath();
		d.setColor(color);

		double	angle = TUCK_UNDER_DEFINITION_ANGLE;
						
		Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
		
		path.moveTo(pos.x*mulX, pos.z*mulY);
		ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
		for(int i = 0; i < crossSections.size(); i++)
		{
			double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
			double currentCrsPos = crossSections.get(i).getPosition();
			
			double step = (currentCrsPos-previousCrsPos)/segments;
			for(int k = 1; k <= segments; k++)
			{
				pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
			
				path.lineTo(pos.x*mulX, pos.z*mulY);				
			}
		}
		d.draw(path);
		
		resetTransform(d,savedTransform);	  
	}


	public static void paintCrossSectionCenterline(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipY, boolean flattenRocker) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);
		
		double mulY = flipY?-1:1;
		int segments = 10;


		GeneralPath pathDeck = new GeneralPath();
		GeneralPath pathBottom = new GeneralPath();
			
		d.setColor(color);
					
		Point2D.Double deckPos = new Point2D.Double(0.01, brd.getDeck().getValueAt(0.01));
		Point2D.Double bottomPos = new Point2D.Double(0.01, brd.getBottom().getValueAt(0.01));
		
		if(flattenRocker)
		{
			deckPos.y -= bottomPos.y;
			bottomPos.y = 0.0;
		}
		
		pathDeck.moveTo(0, deckPos.y);
		pathBottom.moveTo(0, bottomPos.y);
		ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
		for(int i = 0; i <= 20; i++)
		{
			double x = brd.getLength()/20*i;
			deckPos = new Point2D.Double(x, brd.getDeck().getValueAt(0.01));
			bottomPos = new Point2D.Double(x, brd.getBottom().getValueAt(0.01));
		
			if(flattenRocker)
			{
				deckPos.y -= bottomPos.y;
				bottomPos.y = 0.0;
			}

			pathDeck.moveTo(0, deckPos.y);
			pathBottom.moveTo(0, bottomPos.y);
		}
		d.draw(pathDeck);
		d.draw(pathBottom);
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintCrossSectionFlowLines(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipY, boolean flattenRocker) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);

		double mulY = flipY?-1:1;
		int segments = 10;


		GeneralPath pathLeft = new GeneralPath();
		GeneralPath pathRight = new GeneralPath();
			
		for(int j = 0; j < 3; j++)
		{
			d.setColor(color);
			pathLeft.reset();
			pathRight.reset();

			double angle = 10.0;
			if(j == 1)
				angle = 27.5;
			else if(j == 2)
				angle = 45.0;
						
			Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
			if(pos == null)
				continue;
			
			if(flattenRocker)
			{
				double rocker = brd.getRockerAtPos(pos.x);
				pos.z -= rocker;
			}
			
			pathLeft.moveTo(pos.y*-1.0, pos.z*mulY);
			pathRight.moveTo(pos.y, pos.z*mulY);
			ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
			for(int i = 0; i < crossSections.size(); i++)
			{
				double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
				double currentCrsPos = crossSections.get(i).getPosition();
				
				double step = (currentCrsPos-previousCrsPos)/segments;
				for(int k = 1; k <= segments; k++)
				{
					pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
				
					if(flattenRocker)
					{
						double rocker = brd.getRockerAtPos(pos.x);
						pos.z -= rocker;
					}

					pathLeft.lineTo(pos.y*-1.0, pos.z*mulY);				
					pathRight.lineTo(pos.y, pos.z*mulY);				
				}
			}
			d.draw(pathLeft);
			d.draw(pathRight);
			color = color.brighter();
		}
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintCrossSectionApexline(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipY, boolean flattenRocker) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);

		double mulY = flipY?-1:1;
		int segments = 10;


		GeneralPath pathLeft = new GeneralPath();
		GeneralPath pathRight = new GeneralPath();
			
		d.setColor(color);
		pathLeft.reset();
		pathRight.reset();

		double angle = 88.0;
					
		Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
		if(pos == null)
			return;
		
		if(flattenRocker)
		{
			double rocker = brd.getRockerAtPos(pos.x);
			pos.z -= rocker;
		}
		
		pathLeft.moveTo(pos.y*-1.0, pos.z*mulY);
		pathRight.moveTo(pos.y, pos.z*mulY);
		ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
		for(int i = 0; i < crossSections.size(); i++)
		{
			double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
			double currentCrsPos = crossSections.get(i).getPosition();
			
			double step = (currentCrsPos-previousCrsPos)/segments;
			for(int k = 1; k <= segments; k++)
			{
				pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
			
				if(flattenRocker)
				{
					double rocker = brd.getRockerAtPos(pos.x);
					pos.z -= rocker;
				}

				pathLeft.lineTo(pos.y*-1.0, pos.z*mulY);				
				pathRight.lineTo(pos.y, pos.z*mulY);				
			}
		}
		d.draw(pathLeft);
		d.draw(pathRight);
		color = color.brighter();
		
		resetTransform(d,savedTransform);	  
	}

	public static void paintCrossSectionTuckUnderLine(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierBoard brd, boolean flipY, boolean flattenRocker) 
	{
		if(brd == null)
			return;
		
		if(brd.isEmpty())
			return;

		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);
		
		double mulY = flipY?-1:1;
		int segments = 10;
		
		GeneralPath pathLeft = new GeneralPath();
		GeneralPath pathRight = new GeneralPath();

		d.setColor(color);

		double	angle = TUCK_UNDER_DEFINITION_ANGLE;
						
		Point3d pos = brd.getSurfacePoint(0.01, -45.0, angle, 1,1);
		
		if(flattenRocker)
		{
			double rocker = brd.getRockerAtPos(pos.x);
			pos.z -= rocker;
		}

		pathLeft.moveTo(pos.y*-1.0, pos.z*mulY);
		pathRight.moveTo(pos.y, pos.z*mulY);
		ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
		for(int i = 0; i < crossSections.size(); i++)
		{
			double previousCrsPos = (i==0)?0.0:crossSections.get(i-1).getPosition();
			double currentCrsPos = crossSections.get(i).getPosition();
			
			double step = (currentCrsPos-previousCrsPos)/segments;
			for(int k = 1; k <= segments; k++)
			{
				pos = brd.getSurfacePoint(previousCrsPos+(k*step), -45.0, angle, 1,1);
			
				if(flattenRocker)
				{
					double rocker = brd.getRockerAtPos(pos.x);
					pos.z -= rocker;
				}

				pathLeft.lineTo(pos.y*-1.0, pos.z*mulY);				
				pathRight.lineTo(pos.y, pos.z*mulY);				
			}
		}
		d.draw(pathLeft);
		d.draw(pathRight);
		
		resetTransform(d,savedTransform);	  
	}

	static void paintCurvatureByT(AbstractDraw d, BezierCurve curve, double t0, double t1, boolean flipX, boolean flipY, double scale)
	{

//		Get t split point
		double ts = (t1-t0)/2 + t0;

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;

//		Get endpoints
		double x0 = curve.getXValue(t0);
		double y0 = curve.getCurvature(t0);
		double x1 = curve.getXValue(t1);
		double y1 = curve.getCurvature(t1);

		double xs = curve.getXValue(ts);
		double ys = curve.getCurvature(ts);

//		Distance between centerpoint and real split curvepoint
		double length = VecMath.getVecLength(x0,y0,xs,ys) + VecMath.getVecLength(xs,ys,x1,y1);
		double chord  = VecMath.getVecLength(x0,y0,x1,y1);

		if(Double.isInfinite(chord)  || Double.isInfinite(length) ||
				Double.isNaN(chord)  || Double.isNaN(length)	)
			return;

		if( (chord > CURVATURE_MAX_LINE_LENGTH || Math.abs(length - chord) > CURVATURE_TOLERANCE) && chord > CURVATURE_MIN_LINE_LENGTH && Math.abs(t1-t0) > CURVATURE_T_TOLERANCE)
		{
			paintCurvatureByT(d, curve, t0,ts, flipX, flipY, scale);
			paintCurvatureByT(d, curve, ts,t1, flipX, flipY, scale);
		}
		else
		{
			ls.setLine(x0*mulX, y0*mulY*scale, xs*mulX, ys*mulY*scale);
			d.draw(ls);
			ls.setLine(xs*mulX, ys*mulY*scale, x1*mulX, y1*mulY*scale);
			d.draw(ls);
		}


	}

	static void paintCurvatureByPos(AbstractDraw d, BezierCurve curve, double x0, double x1, boolean flipX, boolean flipY, double scale)
	{

//		Get t split point
		double xs = (x1-x0)/2 + x0;

		double mulX = flipX?-1:1;
		double mulY = flipY?-1:1;

//		Get endpoints
		double y0 = curve.getCurvatureAt(x0);
		double y1 = curve.getCurvatureAt(x1);

		double ys = curve.getCurvatureAt(xs);

//		Distance between centerpoint and real split curvepoint
		double length = VecMath.getVecLength(x0,y0,xs,ys) + VecMath.getVecLength(xs,ys,x1,y1);
		double chord  = VecMath.getVecLength(x0,y0,x1,y1);

		if(Double.isInfinite(chord)  || Double.isInfinite(length) ||
				Double.isNaN(chord)  || Double.isNaN(length)	)
			return;

		if( (chord > CURVATURE_MAX_LINE_LENGTH || Math.abs(length - chord) > CURVATURE_TOLERANCE) && chord > CURVATURE_MIN_LINE_LENGTH)
		{
			paintCurvatureByPos(d, curve, x0,xs, flipX, flipY, scale);
			paintCurvatureByPos(d, curve, xs,x1, flipX, flipY, scale);
		}
		else
		{
			ls.setLine(x0*mulX, y0*mulY*scale, xs*mulX, ys*mulY*scale);
			d.draw(ls);
			ls.setLine(xs*mulX, ys*mulY*scale, x1*mulX, y1*mulY*scale);
			d.draw(ls);
		}


	}
	
	
	public static void paintSlidingCrossSection(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, boolean flipX, boolean flipY, double pos, BezierBoard brd) 
	{
		if(brd.getCrossSections().size() <= 2)
			return;

//DEBUG
//paintSlidingCrossSectionNormals(d, offsetX, offsetY, scale, color, stroke, flipX, flipY, pos, brd);
		
		switch(BoardCAD.getInstance().getCrossSectionInterpolationType())
		{
		case LinearInterpolation:
			paintSlidingCrossSectionBlendInterpolation(d, offsetX, offsetY, rotation, scale, color, stroke, flipX, flipY, pos, brd);			
			break;
		case ControlPointInterpolation:
			paintSlidingCrossSectionControlPointInterpolation(d, offsetX, offsetY, rotation, scale, color, stroke, flipX, flipY, pos, brd);
			break;
		case SLinearInterpolation:
//			paintSlidingCrossSectionBlendFromSInterpolation(d, offsetX, offsetY, scale, color, stroke, flipX, flipY, pos, brd);			
			paintSlidingCrossSectionBlendFromSInterpolationSep(d, offsetX, offsetY, rotation, scale, color, stroke, flipX, flipY, pos, brd);
			break;
		default:	//DEBUG
			paintSlidingCrossSectionBlendInterpolation(d, offsetX, offsetY, rotation, scale, new Color(255,0,0), stroke, flipX, flipY, pos, brd);			
			paintSlidingCrossSectionControlPointInterpolation(d, offsetX, offsetY, rotation, scale,  new Color(0,255,0), stroke, flipX, flipY, pos, brd);
			paintSlidingCrossSectionBlendFromSInterpolationSep(d, offsetX, offsetY, rotation, scale,  new Color(0,0,255), stroke, flipX, flipY, pos, brd);			
			break;
			
		}
		
	}	

	public static void paintSlidingCrossSectionNormals(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, boolean flipX, boolean flipY, double pos, BezierBoard brd)
	{
		//
		mMulX = flipX?-1:1;
		mMulY = flipY?-1:1;

		mBrd = brd;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);

		double splitAngle = TUCK_UNDER_DEFINITION_ANGLE;

		double start = -90.0;
		double shoulder = 25.0f;
		double end = 360.0;
		double a0, a1 = 0;
		
		double rocker = brd.getRockerAtPos(pos);
		
		for(int w = 0; w < 3; w++)
		{
			
			if(w == 0)
			{
				d.setColor(new Color(0,0,255));
				a0 = start;
				a1 = shoulder;			
			}
			else if(w == 1)
			{
				d.setColor(new Color(255,0,255));
				a0 = shoulder;
				a1 = splitAngle;			
			}
			else 
			{
				d.setColor(new Color(0,250,0));
				a0 = splitAngle;			
				a1 = end;
			}
			

			Line2D line = new Line2D.Double();
			Point3d point;
			Vector3d normal;
			double steps = 20; 
			for(int i = 1; i <= steps; i++)
			{
				point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(BoardCAD.getInstance().getCrossSectionInterpolationType()).getPointAt(mBrd, pos, (double)i/steps, a0, a1, true);
				if(point == null)
					continue;
				point.z -= rocker;

				normal = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(BoardCAD.getInstance().getCrossSectionInterpolationType()).getNormalAt(mBrd, pos, (double)i/steps, a0, a1, true);				
				normal.scale(0.1f*scale);
				
				Point3d otherEnd = new Point3d(point);
				otherEnd.add(normal);
				
				line.setLine(new Point2D.Double(point.y*mMulX, point.z*mMulY), new Point2D.Double(otherEnd.y*mMulX, otherEnd.z*mMulY));  
				d.draw(line);
			}

		}
		
		resetTransform(d,savedTransform);
		
	}

	/*	public static void paintSlidingCrossSectionBlendFromSInterpolation(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, boolean flipX, boolean flipY, double pos, BezierBoard brd) 
	{
		//
		mMulX = flipX?-1:1;
		mMulY = flipY?-1:1;

		mBrd = brd;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);

		ds = d;

		paintSlidingCrossSectionBlendFromSInterpolation(pos, 0.00001,0.99999);

		resetTransform(d,savedTransform);


	}
	*/

	public static void paintSlidingCrossSectionBlendFromSInterpolationSep(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, boolean flipX, boolean flipY, double pos, BezierBoard brd) 
	{
		//
		mMulX = flipX?-1:1;
		mMulY = flipY?-1:1;

		mBrd = brd;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);

		double splitAngle = TUCK_UNDER_DEFINITION_ANGLE;
/*		
		BezierBoardCrossSection c1 = brd.getPreviousCrossSection(pos);
		BezierBoardCrossSection c2 = brd.getNextCrossSection(pos);
		
		//Scaling 
		double targetWidth = mBrd.getWidthAt(pos);
		double targetThickness = mBrd.getThicknessAtPos(pos);

		double c1Width = c1.getWidth();
		double c1Thickness = c1.getThicknessAtPos(BezierSpline.ZERO);
		
		double c2Width = c2.getWidth();
		double c2Thickness = c2.getThicknessAtPos(BezierSpline.ZERO);
		
		double c1ThicknessScale = targetThickness/c1Thickness;
		double c1WidthScale = targetWidth/c1Width;
		
		double c2ThicknessScale = targetThickness/c2Thickness;
		double c2WidthScale = targetWidth/c2Width;

/*
		System.out.printf("getSurfacePoint()\n");
		System.out.printf("Target width: %f thickness: %f\n", targetWidth, targetThickness);
		System.out.printf("C1 width: %f thickness: %f\n", c1Width, c1Thickness);
		System.out.printf("C2 width: %f thickness: %f\n", c2Width, c2Thickness);
		System.out.printf("C1 width scale: %f thickness scale: %f\n", c1WidthScale, c1ThicknessScale);
		System.out.printf("C2 width scale: %f thickness scale: %f\n", c2WidthScale, c2ThicknessScale);
*/

/*
		double s1 = c1.getBezierSpline().getSByNormalReverseScaled(splitAngle, true, c1WidthScale, c1ThicknessScale);
		double s2 = c2.getBezierSpline().getSByNormalReverseScaled(splitAngle, true, c2WidthScale, c2ThicknessScale);

//		d.setColor(new Color(0,0,255));
		paintSlidingCrossSectionBlendFromSInterpolation(pos, BezierSpline.ZERO, s1, BezierSpline.ZERO, s2);
//		d.setColor(new Color(0,250,0));
		paintSlidingCrossSectionBlendFromSInterpolation(pos, s1, BezierSpline.ONE, s2, BezierSpline.ONE);
*/

		double start = -90.0;
		double shoulder = 25.0f;
		double end = 360.0;
		double a0, a1 = 0;
		
		double rocker = brd.getRockerAtPos(pos);
		
		for(int w = 0; w < 3; w++)
		{
			
			if(w == 0)
			{
//				d.setColor(new Color(0,0,255));
				a0 = start;
				a1 = shoulder;			
			}
			else if(w == 1)
			{
//				d.setColor(new Color(255,0,255));
				a0 = shoulder;
				a1 = splitAngle;			
			}
			else 
			{
//				d.setColor(new Color(0,250,0));
				a0 = splitAngle;			
				a1 = end;
			}
			
			
			GeneralPath path = new GeneralPath();
			Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(AbstractBezierBoardSurfaceModel.ModelType.SLinearInterpolation).getPointAt(mBrd, pos, 0.0, a0, a1, true);
			
			point.z -= rocker;
			
			path.moveTo(point.y*mMulX,point.z*mMulY);  
			double steps = 20; 
			for(int i = 1; i <= steps; i++)
			{
				point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(AbstractBezierBoardSurfaceModel.ModelType.SLinearInterpolation).getPointAt(mBrd, pos, (double)i/steps, a0, a1, true);
				point.z -= rocker;
				path.lineTo(point.y*mMulX,point.z*mMulY);  
	
			}
			d.draw(path);
			AffineTransform trans = new AffineTransform();
			trans.setToScale(-1.0, 1.0);
			path.transform(trans);
			d.draw(path);
		}
		
		
		resetTransform(d,savedTransform);


	}
	public static void paintSlidingCrossSectionBlendFromSInterpolation(double pos, double a0, double a1)
	{

	}
	
/*
	public static void paintSlidingCrossSectionBlendFromSInterpolation(double pos, double s0, double s1 )
	{		
//		Get endpoints
		Point2D.Double p0 = mBrd.getPointAtPos(pos, s0);
		Point2D.Double p1 = mBrd.getPointAtPos(pos, s1);

//		Get x split point
		double ss = (s1-s0)/2 + s0;
		Point2D.Double ps = mBrd.getPointAtPos(pos, ss);

//		Angle between vectors formed by
		Point2D.Double v0 = new Point2D.Double();
		Point2D.Double v1 = new Point2D.Double();
		
		BezierSpline.subVector(p0, ps, v0);
		BezierSpline.subVector(ps, p1, v1);
		
		double length = BezierSpline.getVecLength(v0) + BezierSpline.getVecLength(v1);  
		
		double angle = BezierSpline.getVecAngle(v0,v1);

		if( length > S_BLEND_MAX_LENGTH || (Math.abs(angle) > S_BLEND_MIN_ANGLE &&  length > S_BLEND_MIN_LENGTH) )
		{
			paintSlidingCrossSectionBlendFromSInterpolation(pos, s0, ss);
			paintSlidingCrossSectionBlendFromSInterpolation(pos, ss, s1);
		}
		else
		{
/*Draw Normal for debug
			Vector3d vec = mBrd.getNormalAtPos(pos, ss);	
			ls.setLine(ps.x*mMulX, ps.y*mMulY,(ps.x+vec.y)*mMulX, (ps.y+vec.z)*mMulY);
			ds.draw(ls);
*/
	/*
			ls.setLine(p0.x*mMulX, p0.y*mMulY, ps.x*mMulX, ps.y*mMulY);
			ds.draw(ls);
			ls.setLine(ps.x*mMulX, ps.y*mMulY, p1.x*mMulX, p1.y*mMulY);
			ds.draw(ls);
			ls.setLine(-p0.x*mMulX, p0.y*mMulY, -ps.x*mMulX, ps.y*mMulY);
			ds.draw(ls);
			ls.setLine(-ps.x*mMulX, ps.y*mMulY, -p1.x*mMulX, p1.y*mMulY);
			ds.draw(ls);
		}

	}
	*/
/*
	public static void paintSlidingCrossSectionBlendFromSInterpolation(double pos, double a0, double a1, double b0, double b1 )
	{		
//		Get endpoints
		Point2D.Double p0 = mBrd.getPointAtPos(pos, a0, b0);
		Point2D.Double p1 = mBrd.getPointAtPos(pos, a1, b1);

//		Get x split point
		double as = (a1-a0)/2 + a0;
		double bs = (b1-b0)/2 + b0;
		Point2D.Double ps = mBrd.getPointAtPos(pos, as, bs);

//		Angle between vectors formed by
		Point2D.Double v0 = new Point2D.Double();
		Point2D.Double v1 = new Point2D.Double();
		
		BezierSpline.subVector(p0, ps, v0);
		BezierSpline.subVector(ps, p1, v1);
		
		double length = BezierSpline.getVecLength(v0) + BezierSpline.getVecLength(v1);  
		
		double angle = BezierSpline.getVecAngle(v0,v1);

		if( length > S_BLEND_MAX_LENGTH || (Math.abs(angle) > S_BLEND_MIN_ANGLE &&  length > S_BLEND_MIN_LENGTH) )
		{
			paintSlidingCrossSectionBlendFromSInterpolation(pos, a0, as, b0, bs);
			paintSlidingCrossSectionBlendFromSInterpolation(pos, as, a1, bs, b1);
		}
		else
		{			
/*	Draw normal for debug
 * 			Vector3d vec = mBrd.getNormalAtPos(pos, as, bs);	
			ls.setLine(ps.x*mMulX, ps.y*mMulY,(ps.x+vec.y)*mMulX, (ps.y+vec.z)*mMulY);
			ds.draw(ls);

	Draw tangent for debug
			vec = mBrd.getTangentAtPos(pos, as, bs);	
			ls.setLine(ps.x*mMulX, ps.y*mMulY,(ps.x+vec.y)*mMulX, (ps.y+vec.z)*mMulY);
			ds.setColor(new Color(0,0,250));
			ds.draw(ls);
			ds.setColor(new Color(255,255,0));
*/
/*
			ls.setLine(p0.x*mMulX, p0.y*mMulY, ps.x*mMulX, ps.y*mMulY);
			ds.draw(ls);
			ls.setLine(ps.x*mMulX, ps.y*mMulY, p1.x*mMulX, p1.y*mMulY);
			ds.draw(ls);
			ls.setLine(-p0.x*mMulX, p0.y*mMulY, -ps.x*mMulX, ps.y*mMulY);
			ds.draw(ls);
			ls.setLine(-ps.x*mMulX, ps.y*mMulY, -p1.x*mMulX, p1.y*mMulY);
			ds.draw(ls);
		}

	}
*/
	public static void paintSlidingCrossSectionControlPointInterpolation(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, boolean flipX, boolean flipY, double pos, BezierBoard brd) 
	{
		BezierBoardCrossSection interpol = brd.getInterpolatedCrossSection(pos);
		if(interpol != null)
		{	
			BezierBoardDrawUtil.paintBezierSpline(d,offsetX, offsetY, rotation, scale, color, stroke, interpol.getBezierSpline(), ((flipX==true)?BezierBoardDrawUtil.FlipX:0)|((flipY==true)?BezierBoardDrawUtil.FlipY:0), true);
			BezierBoardDrawUtil.paintBezierSpline(d,offsetX, offsetY, rotation, scale, color, stroke, interpol.getBezierSpline(), ((flipX==true)?0:BezierBoardDrawUtil.FlipX)|((flipY==true)?BezierBoardDrawUtil.FlipY:0), true);
//DEBUG			BrdDrawUtil.paintBezierControlPoints(d,offsetX, offsetY, scale, stroke, interpol.mCrossSectionControlPoints, new ArrayList<ControlPoint>(), flipX, flipY);	//DEBUG!
		}

	}

	public static void paintSlidingCrossSectionBlendInterpolation(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale, Color color, Stroke stroke, boolean flipX, boolean flipY, double pos, BezierBoard brd) 
	{


		//
		mMulX = flipX?-1:1;
		mMulY = flipY?-1:1;

		mBrd = brd;

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, rotation, scale);

		//Calculate scales
		double halfWidth = mBrd.getWidthAtPos(pos)/2;
		halfWidth -= .00001;

		paintSlidingCrossSectionBlendInterpolation(d, pos,0, halfWidth/2);
		paintSlidingCrossSectionBlendInterpolation(d, pos,halfWidth/2, halfWidth);
		paintSlidingCrossSectionBlendInterpolationReverse(d, pos, 0, halfWidth/2);
		paintSlidingCrossSectionBlendInterpolationReverse(d, pos, halfWidth/2, halfWidth);


		double z0 = mBrd.getDeckAtPos(pos, halfWidth);
		double z1 = mBrd.getBottomAtPos(pos, halfWidth);

		ls.setLine(halfWidth*mMulX, z0*mMulY, halfWidth*mMulX, z1*mMulY);
		d.draw(ls);
		ls.setLine(-halfWidth*mMulX, z0*mMulY, -halfWidth*mMulX, z1*mMulY);
		d.draw(ls);

		resetTransform(d,savedTransform);

	}
	/*	
	public static void paintSlidingCrossSectionS(double x, double s0, double s1 )
	{

//		Get endpoints
		double z0 = mBrd.getDeckAtPos(x, y0);
		double z1 = mBrd.getDeckAtPos(x, y1);

//		Get x split point
		double ys = (y1-y0)/2 + y0;
		double zs = mBrd.getDeckAtPos(x, ys);

//		Distance between centerpoint and real split curvepoint
		double length = BezierPatch.getVecLength(y0,z0,ys,zs) + BezierPatch.getVecLength(ys,zs,y1,z1);
		double chord  = BezierPatch.getVecLength(y0,z0,y1,z1);

		if(Double.isInfinite(chord)  || Double.isInfinite(length) ||
				Double.isNaN(chord)  || Double.isNaN(length)	)
			return;

		if(Math.abs(length - chord) > SLIDING_CROSS_SECTION_TOLERANCE && chord > SLIDING_CROSS_SECTION_MIN_LINE_LENGTH )
		{
			paintSlidingCrossSection(x, y0, ys);
			paintSlidingCrossSection(x, ys, y1);
		}
		else
		{
			ls.setLine(y0*mMulX, z0*mMulY, ys*mMulX, zs*mMulY);
			ds.draw(ls);
			ls.setLine(ys*mMulX, zs*mMulY, y1*mMulX, z1*mMulY);
			ds.draw(ls);
		}

	}
	 */
	public static void paintSlidingCrossSectionBlendInterpolation(AbstractDraw d, double x, double y0, double y1 )
	{		
//		Get endpoints
		double z0 = mBrd.getDeckAtPos(x, y0);
		double z1 = mBrd.getDeckAtPos(x, y1);

//		Get x split point
		double ys = (y1-y0)/2 + y0;
		double zs = mBrd.getDeckAtPos(x, ys);

//		Distance between centerpoint and real split curvepoint
		double length = VecMath.getVecLength(y0,z0,ys,zs) + VecMath.getVecLength(ys,zs,y1,z1);
		double chord  = VecMath.getVecLength(y0,z0,y1,z1);

		if(Double.isInfinite(chord)  || Double.isInfinite(length) ||
				Double.isNaN(chord)  || Double.isNaN(length)	)
			return;

		if(Math.abs(y1 - y0) > SLIDING_CROSS_MIN_SPLIT && (chord > SLIDING_CROSS_SECTION_MAX_LINE_LENGTH  || (Math.abs(length - chord) > SLIDING_CROSS_SECTION_TOLERANCE && chord > SLIDING_CROSS_SECTION_MIN_LINE_LENGTH )))
		{
			paintSlidingCrossSectionBlendInterpolation(d, x, y0, ys);
			paintSlidingCrossSectionBlendInterpolation(d, x, ys, y1);
		}
		else
		{
			ls.setLine(y0*mMulX, z0*mMulY, ys*mMulX, zs*mMulY);
			d.draw(ls);
			ls.setLine(ys*mMulX, zs*mMulY, y1*mMulX, z1*mMulY);
			d.draw(ls);
			ls.setLine(-y0*mMulX, z0*mMulY, -ys*mMulX, zs*mMulY);
			d.draw(ls);
			ls.setLine(-ys*mMulX, zs*mMulY, -y1*mMulX, z1*mMulY);
			d.draw(ls);
		}

	}

	public static void paintSlidingCrossSectionBlendInterpolationReverse(AbstractDraw d, double x, double y0, double y1 )
	{		
//		Get endpoints
		double z0 = mBrd.getBottomAtPos(x, y0);
		double z1 = mBrd.getBottomAtPos(x, y1);

//		Get x split point
		double ys = (y1-y0)/2 + y0;
		double zs = mBrd.getBottomAtPos(x, ys);

//		Distance between centerpoint and real split curvepoint
		double length = VecMath.getVecLength(y0,z0,ys,zs) + VecMath.getVecLength(ys,zs,y1,z1);
		double chord  = VecMath.getVecLength(y0,z0,y1,z1);

		if(Double.isInfinite(chord)  || Double.isInfinite(length) ||
				Double.isNaN(chord)  || Double.isNaN(length)	)
			return;

		if(Math.abs(y1 - y0) > SLIDING_CROSS_MIN_SPLIT && (chord > SLIDING_CROSS_SECTION_MAX_LINE_LENGTH || (Math.abs(length - chord) > SLIDING_CROSS_SECTION_TOLERANCE && chord > SLIDING_CROSS_SECTION_MIN_LINE_LENGTH )) )
		{
			paintSlidingCrossSectionBlendInterpolationReverse(d, x, y0, ys);
			paintSlidingCrossSectionBlendInterpolationReverse(d, x, ys, y1);
		}
		else
		{
			ls.setLine(y0*mMulX, z0*mMulY, ys*mMulX, zs*mMulY);
			d.draw(ls);
			ls.setLine(ys*mMulX, zs*mMulY, y1*mMulX, z1*mMulY);
			d.draw(ls);
			ls.setLine(-y0*mMulX, z0*mMulY, -ys*mMulX, zs*mMulY);
			d.draw(ls);
			ls.setLine(-ys*mMulX, zs*mMulY, -y1*mMulX, z1*mMulY);
			d.draw(ls);		
		}		
	}

	public static void paintDetails(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierSpline[] beziers, boolean flipX, boolean flipY, boolean mirrorX, boolean mirrorY, double posOffsetX, double posOffsetY, double measurementOffsetX, double measurementOffsetY) 
	{
		for(int i = 0; i < beziers.length; i++ )
		{
			paintBezierSpline(d,offsetX, offsetY, scale, color, stroke, beziers[i],((flipX==true)?BezierBoardDrawUtil.FlipX:0)|((flipY==true)?BezierBoardDrawUtil.FlipY:0)|((mirrorX==true)?BezierBoardDrawUtil.MirrorX:0)|((mirrorY==true)?BezierBoardDrawUtil.MirrorY:0), true);
		}	
	}

	public static void paintDetailsVertical(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, BezierSpline[] beziers, boolean flipX, boolean flipY, boolean mirrorX, boolean mirrorY, double posOffsetX, double posOffsetY, double measurementOffsetX, double measurementOffsetY) 
	{
		for(int i = 0; i < beziers.length; i++ )
		{
			paintBezierSpline(d,offsetX, offsetY, Math.PI/4.0, scale, color, stroke, beziers[i],((flipX==true)?BezierBoardDrawUtil.FlipX:0)|((flipY==true)?BezierBoardDrawUtil.FlipY:0)|((mirrorX==true)?BezierBoardDrawUtil.MirrorX:0)|((mirrorY==true)?BezierBoardDrawUtil.MirrorY:0), true);
		}
	}

	public static AffineTransform setTransform(AbstractDraw d, double offsetX, double offsetY, double scale)
	{

		AffineTransform savedTransform = d.getTransform();

		final AffineTransform at = new AffineTransform();

		at.setToTranslation(offsetX, offsetY);

		d.transform(at);

		at.setToScale(scale, scale);

		d.transform(at);

		return savedTransform;
	}

	public static AffineTransform setTransform(AbstractDraw d, double offsetX, double offsetY, double scale, boolean mirrorX, boolean mirrorY)
	{

		AffineTransform savedTransform = d.getTransform();

		final AffineTransform at = new AffineTransform();

		at.setToTranslation(offsetX, offsetY);

		d.transform(at);

		at.setToScale(scale*(mirrorX?-1.0:1.0), scale*(mirrorY?-1.0:1.0));

		d.transform(at);

		return savedTransform;
	}

	static AffineTransform setTransform(AbstractDraw d, double offsetX, double offsetY, double rotation, double scale)
	{

		AffineTransform savedTransform = d.getTransform();

		final AffineTransform at = new AffineTransform();


		at.setToTranslation(offsetX, offsetY);

		d.transform(at);

		at.setToScale(scale, scale);

		d.transform(at);

		at.setToRotation(rotation);

		d.transform(at);

		return savedTransform;
	}

	
	static void resetTransform(AbstractDraw d, AffineTransform savedTransform)
	{
		d.setTransform(savedTransform);
	}


//	Old functions orginating from printing
	private static void setImperialGridLineStrokeAndColor(AbstractDraw d, Color color, int i, double scale)
	{

		if (i%12 == 0)
		{
			d.setColor(color.darker().darker());

			d.setStroke(new BasicStroke((float)(1.5/scale)));
		}
		else if (i%6 == 0)
		{
			d.setColor(color.darker());

			d.setStroke(new BasicStroke((float)(1.2/scale)));
		}
		else
		{
			d.setColor(color);

			d.setStroke(new BasicStroke((float)(1.0/scale)));
		}
	}

	
//	Old functions orginating from printing
	private static void setMetricGridLineStrokeAndColor(AbstractDraw d, Color color, int i, double scale)
	{

		if (i%100 == 0)
		{
			d.setColor(color.darker().darker().darker());

			d.setStroke(new BasicStroke((float)(1.5/scale)));
		}
		else
		if (i%50 == 0)
		{
			d.setColor(color.darker().darker());

			d.setStroke(new BasicStroke((float)(1.3/scale)));
		}
		else if (i%10 == 0)
		{
			d.setColor(color.darker());

			d.setStroke(new BasicStroke((float)(1.1/scale)));
		}
		else
		{
			d.setColor(color);

			d.setStroke(new BasicStroke((float)(1.0/scale)));
		}
	}

	
	public static void printOutline(AbstractDraw d, double offsetX,

			double offsetY, double rotation, double scale, boolean paintGrid, BezierBoard brd, boolean printGuidePoints, boolean printFins) {

		if(brd.isEmpty()) {
			
			return;
	
		}

		if(brd.getOutline().getNrOfControlPoints() < 2) {

			return;

		}

		paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), brd.getLength(), brd.getCenterWidth()/2.0, false, false);

		GeneralPath outlineLower = makeBezierPathFromControlPoints(brd.getOutline(),false,false, false, false);

		GeneralPath outlineUpper = makeBezierPathFromControlPoints(brd.getOutline(),false,true, false, false);

		Stroke stroke = new BasicStroke((float)(2.0/scale));

//		paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mOutlineControlPoints, null, false, false);

			
		paintPath(d,offsetX, offsetY, rotation, scale, BoardCAD.getInstance().getBrdColor(), stroke, outlineLower, false);
	
		paintPath(d,offsetX, offsetY, rotation, scale,  BoardCAD.getInstance().getBrdColor(), stroke, outlineUpper, false);

		if(printGuidePoints == true)
		{
			BezierBoardDrawUtil.paintGuidePoints(d, offsetX, offsetY, scale, Color.GRAY, stroke, brd.getOutlineGuidePoints(), false, false);
		}
		
		if(printFins == true)
		{
			BezierBoardDrawUtil.paintFins(d, offsetX, offsetY, scale, Color.GRAY, stroke, brd.getFins(), false, false);
		}
	}

	public static void printOutlineOverCurve(AbstractDraw d, double offsetX,

			double offsetY, double rotation, double scale, boolean paintGrid, BezierBoard brd, boolean printGuidePoints, boolean printFins, boolean mirrorX, boolean mirrorY) {

		if(brd.isEmpty()) {
			
			return;
	
		}

		if(brd.getOutline().getNrOfControlPoints() < 2) {

			return;

		}

		System.out.printf("\nOUTLINE OVER CURVE\n");

		if(paintGrid)
		{
			paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), brd.getLength(), brd.getMaxWidth()/2.0, false, false);
		}
	
		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));

	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);
	
	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom);
	
		double step = 0.3;
		BezierSpline outline = brd.getOutline();
		BezierSpline bottom = brd.getBottom();
		
		GeneralPath path = new GeneralPath();
		path.moveTo(0.0,0.0);
		for(double ox = 0; ox <= brd.getLength(); ox += step)
		{
			//Get the outline point and the angle
			double oy = outline.getValueAt(ox)*(mirrorY?-1.0:1.0);
								
			//Find the length over deck curve
			double length = bottom.getLengthByX(ox)*(mirrorX?-1.0:1.0);
			
//			System.out.printf("OC: %f, width %f\n", length, oy*2.0);
				
			//Draw lines
			path.lineTo(length, oy);
		}
		d.draw(path);
		
		d.setTransform(savedTransform);
		
		if(printGuidePoints == true)
		{
			BezierBoardDrawUtil.paintGuidePoints(d, offsetX, offsetY, scale, Color.GRAY, stroke, brd.getOutlineGuidePoints(), false, false);
		}
		
		if(printFins == true)
		{
			BezierBoardDrawUtil.paintFins(d, offsetX, offsetY, scale, Color.GRAY, stroke, brd.getFins(), false, false);
		}
	}

	public static void printSpinTemplate(AbstractDraw d, double offsetX,

			double offsetY, double rotation, double scale, boolean paintGrid, BezierBoard brd, boolean printGuidePoints, boolean printFins) {

		if(brd.isEmpty()) {
			
			return;
	
		}
		if(brd.getOutline().getNrOfControlPoints() < 2) {

			return;

		}


		paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), brd.getLength(), brd.getMaxWidth()/2, false, false);

		GeneralPath outlineLower = makeBezierPathFromControlPoints(brd.getOutline(),false,false, false,false);

		GeneralPath outlineUpper = makeBezierPathFromControlPoints(brd.getOutline(),false,true, false,false);

		Stroke stroke = new BasicStroke((float)(2.0/scale));

//		paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mOutlineControlPoints, null, false, false);

		paintPath(d,offsetX, offsetY, rotation, scale, BoardCAD.getInstance().getBrdColor(), stroke, outlineLower, false);

		//Calculate offset
		double xOffset = brd.getLength()/2.0; 
		double yOffset = brd.getWidthAt(xOffset)/2.0; 
		
		xOffset *= scale;
		yOffset *= scale;
		
		double rotationOffset = 0.0;

		paintPath(d,offsetX-xOffset, offsetY+yOffset, rotation+rotationOffset, scale,  BoardCAD.getInstance().getBrdColor(), stroke, outlineUpper, false);

	}

	public static void printSpinTemplateOverCurve(AbstractDraw d, double offsetX,

			double offsetY, double rotation, double scale, boolean paintGrid, BezierBoard brd, boolean printGuidePoints, boolean printFins) {

		if(brd.getOutline().getNrOfControlPoints() < 2) {

			return;

		}

		paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), brd.getLength(), brd.getMaxWidth()/2.0, false, false);

		printOutlineOverCurve(d,offsetX, offsetY, rotation, scale, false, brd, printGuidePoints, printFins, false, false);
		
		//Calculate offset
		double xOffset = brd.getBottom().getLengthByX(brd.getLength()/2.0);
		double yOffset = brd.getWidthAt(xOffset)/2.0; 
		
		xOffset *= scale;
		yOffset *= scale;
		
		double rotationOffset = 0.0;

		printOutlineOverCurve(d,offsetX-xOffset, offsetY+yOffset, rotation+rotationOffset,  scale, false, brd, printGuidePoints, printFins, false, true);

	}


//	Old functions orginating from printing
	public static void printProfile(AbstractDraw d, double offsetX,

			double offsetY, double scale, boolean paintGrid, BezierBoard brd, Boolean printGuidePoints) {

		if(brd.getDeck().getNrOfControlPoints() < 2) {

			return;

		}

		if(paintGrid)
			paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), brd.getLength(), brd.getMaxRocker()/2.0, false, false);

		GeneralPath deck = makeBezierPathFromControlPoints(brd.getDeck(), false, false, false, false);

		GeneralPath bottom = makeBezierPathFromControlPoints(brd.getBottom(), false, false, false, false);

		Stroke stroke = new BasicStroke((float)(2.0/scale));

//		paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);

//		paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);

		paintPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck, false);

		paintPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom, false);

		if(printGuidePoints == true)
		{
			BezierBoardDrawUtil.paintGuidePoints(d, offsetX, offsetY, scale, Color.GRAY, stroke, brd.getDeckGuidePoints(), false, false);
			BezierBoardDrawUtil.paintGuidePoints(d, offsetX, offsetY, scale, Color.GRAY, stroke, brd.getBottomGuidePoints(), false, false);
		}
	}




//	Old functions orginating from printing
	public static void printSlice(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, int i, boolean printGuidePoints) {

		BezierBoardCrossSection crs = brd.getCrossSections().get(i);

		if(crs.getBezierSpline().getNrOfControlPoints() < 2) {

			return;

		}

		if(paintGrid)
			paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), crs.getWidth(), crs.getCenterThickness(), false, false);

		GeneralPath crsPath = makeBezierPathFromControlPoints(crs.getBezierSpline(), false, false, false, false);

		Stroke stroke = new BasicStroke((float)(2.0/scale));

//		paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);

//		paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);

		paintPath(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, crsPath, false);

		if(printGuidePoints == true)
		{
			BezierBoardDrawUtil.paintGuidePoints(d, offsetX, offsetY, scale, Color.GRAY, stroke, crs.getGuidePoints(), false, false);
		}
	}

	
	//Old functions orginating from printing
	public static void printRailTemplate(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double distanceFromRail, double skinThickness, double tailOffset, double noseOffset,  boolean flatten) 
	{
	
		if(brd.isEmpty()) {
			return;
		}
	
		int  verticalLines = 0;
		int  horizontalLines = 0;
		while(brd.getLength()/2.54 > verticalLines)verticalLines+=6;
		while((brd.getMaxRocker()/2)/2.54 > horizontalLines)horizontalLines+=6;
		
		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));

	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);
	
	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom);
	
		BezierSpline outline = brd.getOutline();
		
		System.out.printf("\nRAIL\n");

		boolean first = true;
		double lastPos = 0;
		double lastX = 0;
		double lastY = 0;
		double lastDeck = 0;
		double lastBottom = 0;
		Line2D line = new Line2D.Double();
		
		double length = brd.getLength()-tailOffset-noseOffset;
		int steps = (int)(length/0.5);
		
		double step = length/steps;

		GeneralPath path = new GeneralPath();
		int i = 0;
		double x = 0.0;
		double y = 0.0;
		double ox = 0;
		for(; i <= steps; i++)
		{

			ox = tailOffset + i*step;
			
			//Get the outline point and the angle
			double oy = outline.getValueAt(ox);
			double normalAngle = outline.getNormalAngle(ox);
			
			//Find the target point at distance from outline
			x = ox - (distanceFromRail*Math.sin(normalAngle));
			y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(first && y < 0)
				continue;

			if(!first && y < 0)
				break;

			//Find thickness and rocker at pos
			double deck = getDeck(brd, x, y, skinThickness);
			double bottom = getBottom(brd, x, y, skinThickness);
					
			if(first && deck < bottom)
				continue;

			if(!first && deck < bottom)
				break;
			
			if(flatten)
			{
				double deckRockerCompensation = brd.getDeck().getValueAt(x) - brd.getThickness(); 
				deck -= deckRockerCompensation;
				bottom -= deckRockerCompensation;
			}

//			System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			if(!first)
			{
				//Find the 2D length from the last point on deck and bottom
				double xd = x-lastX;
				double yd = y-lastY;
				double span = Math.sqrt((xd*xd)+(yd*yd));
				
				double newPos = lastPos + span;
				
				//Draw lines
				path.lineTo(newPos,deck);
				
				//Update last pos
				lastPos = newPos;
			}
			else
			{
				first = false;
				path.moveTo(lastPos, deck);
			}

			lastX = x;
			lastY = y;
			lastDeck = deck;
			lastBottom = bottom;
			
		}

		System.out.printf("Rail tip of nose pos at %f, deck:%f bottom:%f", ox, lastDeck, lastBottom );

		first = true;
		for(; i > 0; i--)
		{

			ox = tailOffset + i*step;
			
			//Get the outline point and the angle
			double oy = outline.getValueAt(ox);
			double normalAngle = outline.getNormalAngle(ox);
			
			//Find the target point at distance from outline
			x = ox - (distanceFromRail*Math.sin(normalAngle));
			y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(first && y < 0)
				continue;

			if(!first && y < 0)
				break;

			//Find thickness and rocker at pos
			double deck = getDeck(brd, x, y, skinThickness);
			double bottom = getBottom(brd, x, y, skinThickness);
					
			if(first && deck < bottom)
				continue;

			if(!first && deck < bottom)
				break;
			
			if(flatten)
			{
				double deckRockerCompensation = brd.getDeck().getValueAt(x) - brd.getThickness(); 
				deck -= deckRockerCompensation;
				bottom -= deckRockerCompensation;
			}

//			System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			if(!first)
			{
				//Find the 2D length from the last point on deck and bottom
				double xd = x-lastX;
				double yd = y-lastY;
				double span = Math.sqrt((xd*xd)+(yd*yd));
				
				double newPos = lastPos - span;
				
				//Draw lines
				path.lineTo(newPos, bottom);
				
				//Update last pos
				lastPos = newPos;
			}
			else
			{
				first = false;
			}

			lastX = x;
			lastY = y;
			lastDeck = deck;
			lastBottom = bottom;
			
		}
		path.closePath();
		d.draw(path);

		d.setTransform(savedTransform);
		
	}
	
	//Old functions orginating from printing
	public static void printDeckSkinTemplate(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double distanceFromRail) {
	
		if(brd.isEmpty()) {
	
			return;
	
		}
	
		System.out.printf("\nDECK\n");

		int  verticalLines = 0;
		int  horizontalLines = 0;
		while(brd.getLength()/2.54 > verticalLines)verticalLines+=6;
		while((brd.getMaxWidth()/2)/2.54 > horizontalLines)horizontalLines+=6;
	
//		paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), verticalLines, horizontalLines, false, false);
	
		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));

	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);
	
	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom);
	
		double step = 1.0;
		BezierSpline outline = brd.getOutline();
		BezierSpline deck = brd.getDeck();
		
		boolean first = true;
		double lastPos = 0;
		double lastLength = 0;
		double lastWidth = 0;
		Line2D line = new Line2D.Double();
		for(double ox = 0; ox < brd.getLength(); ox += step)
		{
			//Get the outline point and the angle
			double oy = outline.getValueAt(ox);
			double normalAngle = outline.getNormalAngle(ox);
			
			//Find the target point at distance from outline
			double x = ox - (distanceFromRail*Math.sin(normalAngle));
			double y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(first && y < 0)
				continue;

			if(!first && y < 0)
				break;
					
//			System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y);

			//Find the length over deck curve
			double length = deck.getLengthByX(x);
			
			//Find the width of the template at this point accounting for over curve
			double width = 0;
			double lastz = brd.getDeckAt(x, 0.0);
			double splits = 10;
			double step_y = y/splits;
			for(int i = 0; i < splits; i++)
			{
				double z = brd.getDeckAt(x, i*step_y);
				
				double dz = z - lastz;
				double span = Math.sqrt((step_y*step_y)+(dz*dz));

//				System.out.printf("z %f\n", z);

				lastz = z;
				width += span;
			}
					
//			System.out.printf("width %f\n", width);

			if(!first)
			{	
				double newPos = lastPos + (length - lastLength); 
				
				//Draw lines
				line.setLine(lastPos, lastWidth, newPos, width);
				d.draw(line);
				
				lastPos = newPos;
			}
			else
			{
				first = false;
			}

			lastLength = length;
			lastWidth = width;
		}

		d.setTransform(savedTransform);
		
	}

	//Old functions orginating from printing
	public static void printBottomSkinTemplate(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double distanceFromRail)
	{	
		if(brd.isEmpty()) {
	
			return;
	
		}
	
		System.out.printf("\nBOTTOM\n");

		int  verticalLines = 0;
		int  horizontalLines = 0;
		while(brd.getLength()/2.54 > verticalLines)verticalLines+=6;
		while((brd.getMaxWidth()/2)/2.54 > horizontalLines)horizontalLines+=6;
	
//		paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), verticalLines, horizontalLines, false, false);
	
		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));

	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mDeckControlPoints, false, false);
	
	//	paintBezierControlPoints(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, brd.mBottomControlPoints, false, false);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, deck);
	
	//	paintBezierSpline(d,offsetX, offsetY, scale, BoardCAD.getInstance().getBrdColor(), stroke, bottom);
	
		double step = 1.0;
		BezierSpline outline = brd.getOutline();
		BezierSpline bottom = brd.getBottom();
		
		boolean first = true;
		double lastPos = 0;
		double lastLength = 0;
		double lastWidth = 0;
		Line2D line = new Line2D.Double();
		for(double ox = 0; ox < brd.getLength(); ox += step)
		{
			//Get the outline point and the angle
			double oy = outline.getValueAt(ox);
			double normalAngle = outline.getNormalAngle(ox);
			
			//Find the target point at distance from outline
			double x = ox - (distanceFromRail*Math.sin(normalAngle));
			double y = oy - (distanceFromRail*Math.cos(normalAngle));

			if(first && y < 0)
				continue;

			if(!first && y < 0)
				break;
					
//			System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y);

			//Find the length over deck curve
			double length = bottom.getLengthByX(x);
			
			//Find the width of the template at this point accounting for over curve
			double width = 0;
			double lastz = brd.getBottomAt(x, 0.0);
			double splits = 10;
			double step_y = y/splits;
			for(int i = 0; i < splits; i++)
			{
				double z = brd.getBottomAt(x, i*step_y);
				
				double dz = z - lastz;
				double span = Math.sqrt((step_y*step_y)+(dz*dz));

				lastz = z;
				width += span;
			}
					
//			System.out.printf("width %f\n", width);

			if(!first)
			{	
				double newPos = lastPos + (length - lastLength); 
				
				//Draw lines
				line.setLine(lastPos, lastWidth, newPos, width);
				d.draw(line);
				
				lastPos = newPos;
			}
			else
			{
				first = false;
			}

			lastLength = length;
			lastWidth = width;
		}

		d.setTransform(savedTransform);
		
	}

	public static void printCrossSection(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double position)
	{	
		if(brd.isEmpty()) {
	
			return;
	
		}
		
		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));
			
		System.out.printf("\nCROSSSECTION\n");

		int steps = 300;
				
		double x = position;
		
		GeneralPath path = new GeneralPath();
		
		double s = 0.0;

		//Find thickness and rocker at pos
		Point2D.Double current = brd.getSurfacePointAtPos(x, s);
		path.moveTo(current.x, current.y);

		for(int i = 1; i <= steps; i++)
		{
			s = (double)i/steps;
			
			//Find thickness and rocker at pos
			current = brd.getSurfacePointAtPos(x, s);
				
			//System.out.printf("Outline x: %f s: %f current: %f %f \n", x, s, current.x, current.y);

			path.lineTo(current.x, current.y);
			
		}
		path.closePath();
		d.draw(path);

		d.setTransform(savedTransform);
		
	}

	//Old functions orginating from printing
	public static void printCrossSection(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double position, double railOffset, double skinThickness)
	{	
		if(brd.isEmpty()) {
	
			return;
	
		}
		
		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);	
		Stroke stroke = new BasicStroke((float)(2.0/scale));
		d.setStroke(stroke);
		d.setColor(new Color(0,0,0));
			
		System.out.printf("\nCROSSSECTION\n");

		GeneralPath path = new GeneralPath();


		double halfWidth = brd.getWidthAt(position)/2.0f;
		
		double outlineAngle = Math.abs(brd.getOutline().getTangentAt(position));
		
		double sinOutline = Math.sin(outlineAngle);
		
		halfWidth -= railOffset/sinOutline;

		Vector3d upVector = new Vector3d(0.0, 0.0, 1.0);

		int steps = 150;
		
		double x = position;
		
		boolean first = true;

		//Deck
		for(int i = 0; i <= steps; i++)
		{
			double y = i* halfWidth / steps;
			
			//Find thickness and rocker at pos
			double deck = brd.getDeckAt(x, y);
				
			if(skinThickness != 0.0)
			{
				Vector3d deckNormal = brd.getDeckNormalAt(x, y);
			
				double upAngle = upVector.angle(deckNormal);

				double deckSkinCompensation = skinThickness/Math.cos(upAngle);
				
				deck += deckSkinCompensation;
			}

	//		System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			if(!first)
			{				
				//Draw lines
				path.lineTo(y, deck);
			}
			else
			{
				path.moveTo(y, deck);
				first = false;
			}

			
		}
		
		//Bottom
		for(int i = 0; i <= steps; i++)
		{
			double y = halfWidth - (i* halfWidth / steps);
			
			//Find thickness and rocker at pos
			double bottom = brd.getBottomAt(x, y);
				
			if(skinThickness != 0.0)
			{
				Vector3d bottomNormal = brd.getBottomNormalAt(x, y);
			
				double downAngle = upVector.angle(bottomNormal);

				double bottomSkinCompensation = skinThickness/Math.cos(downAngle);
				
				bottom -= bottomSkinCompensation;
			}

	//		System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);

			//Draw lines
			path.lineTo(y, bottom);

			
		}

		path.closePath();
		d.draw(path);

		if(railOffset > 0.0)
		{
			//Find thickness and rocker at pos
			double deck = brd.getDeckAt(x, halfWidth);
			double bottom = brd.getBottomAt(x, halfWidth);		
			d.draw(new Line2D.Double(halfWidth, deck, halfWidth, bottom));
		}
		
		d.setTransform(savedTransform);
		
	}
	
	//Old functions orginating from printing
	public static void printProfile(AbstractDraw d, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double offset, double skinThickness, boolean flatten, double tailOffset, double noseOffset)
	{
		printProfile(d, new Color(0,0,0), 2.0, offsetX, offsetY, scale, paintGrid, brd, offset, skinThickness, flatten, tailOffset, noseOffset);
	}
	
	//Old functions orginating from printing
	public static void printProfile(AbstractDraw d, Color color, double lineWidth, double offsetX, double offsetY, double scale, boolean paintGrid, BezierBoard brd, double offset, double skinThickness, boolean flatten, double tailOffset, double noseOffset)
	{
		if(brd.isEmpty()){
			return;
		}
		
		System.out.printf("\nPROFILE\n");

		if(paintGrid){
			paintGrid(d, offsetX, offsetY, scale, new Color(128,128,128), brd.getLength(), brd.getMaxRocker(), false, false);
		}

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);
		Stroke stroke = new BasicStroke((float)(lineWidth/scale));
		d.setStroke(stroke);
		d.setColor(color);

		boolean first = true;
		
		double length = brd.getLength() - (tailOffset + noseOffset);
		int steps = (int)(length/0.3); 
		double step = length/steps;
		GeneralPath path = new GeneralPath();
				
		double y = offset;

		//Deck
		double x = tailOffset;
		for(int i = 0; i <= steps; i++)
		{
			x = i*step + tailOffset;
			
			//Find thickness and rocker at pos
			double deck = (skinThickness!=0.0)?getDeck(brd, x, y, skinThickness):brd.getDeckAt(x, y);
			
			double bottom = (skinThickness!=0.0)?getBottom(brd, x, y, skinThickness):brd.getBottomAt(x, y);
			if(flatten)
			{
				double deckRockerCompensation = brd.getDeck().getValueAt(x) - brd.getThickness(); 
				deck -= deckRockerCompensation;
				bottom -= deckRockerCompensation;
			}

//			System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f Deck: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, deck, bottom);
			if(bottom >= deck)
			{
				if(first)
				{
					path.moveTo(x,deck);
				}
				else
				{
					break;
				}
			}
			else{
				if(first)
				{
					path.moveTo(x,deck);
					first = false;
					continue;
				}
				path.lineTo(x, deck);
			}
		}
		
		System.out.printf("At end of profile x:%f\n", x);

		//Bottom
		first = true;
		for(int i = 0; i <= steps; i++)
		{
			x = length + tailOffset - (i*step);
			
			//Find thickness and rocker at pos
			double bottom = (skinThickness!=0.0)? getBottom(brd, x, y, skinThickness):brd.getBottomAt(x, y);
								
			double deck = (skinThickness!=0.0)?getDeck(brd, x, y, skinThickness):brd.getDeckAt(x,y);
			if(flatten)
			{
				double bottomRockerCompensation = brd.getDeck().getValueAt(x) - brd.getThickness(); 
				bottom -= bottomRockerCompensation;
				deck -= bottomRockerCompensation;
			}


//			System.out.printf("Outline x: %f y: %f Normal: %f Template x: %f y: %f bottom: %f Bottom: %f\n", ox,oy,normalAngle/BezierBoard.DEG_TO_RAD, x,y, bottom, bottom);

			
			if(bottom >= deck)
			{
				continue;
//				path.moveTo(x,bottom);
			}
			else{
				if(first)
				{
//					path.moveTo(x,bottom);
					first = false;
//					continue;
				}
				path.lineTo(x, bottom);
			}
		}
	
		path.closePath();
		d.draw(path);

		d.setTransform(savedTransform);
		
	}

	public static void paintFunction(AbstractDraw d, double offsetX, double offsetY, double scale, Color color, Stroke stroke, Function func, double minLimit, double maxLimit, double horizontalScale, double verticalScale) 
	{

		d.setColor(color);
		d.setStroke(stroke);

		AffineTransform savedTransform = setTransform(d, offsetX, offsetY, scale);


		int splits = 100;
		double step = (maxLimit-minLimit)/splits;
		
		double x = minLimit;
		double lastValue = func.f(x);
		
		Line2D.Double l = new Line2D.Double();
		for(int i = 1; i < splits; i++)
		{
			double currentValue = func.f(x+step);
			
			l.setLine(x*horizontalScale, lastValue*verticalScale, (x+step)*horizontalScale, currentValue*verticalScale);
			d.draw(l);
			
			x += step;
			lastValue = currentValue;
		}


		resetTransform(d,savedTransform);	  
	}

	public static double getDeck(AbstractBoard board, double x, double y, double skinThickness)
	{
		//Find thickness and rocker at pos
		double deck = board.getDeckAt(x, y);
				
		Vector3d deckNormal = board.getDeckNormalAt(x, y);
	
		double upAngle = upVector.angle(deckNormal);
	
		double deckSkinCompensation = skinThickness/Math.cos(upAngle);
		
		deck += deckSkinCompensation;

		return deck;
	}
	
	public static double getBottom(AbstractBoard board, double x, double y, double skinThickness)
	{

		//Find thickness and rocker at pos
		double bottom = board.getBottomAt(x, y);

		Vector3d bottomNormal = board.getBottomNormalAt(x, y);
		
		double upAngle = upVector.angle(bottomNormal);

		double bottomSkinCompensation = skinThickness/Math.cos(upAngle);
		
		bottom -= bottomSkinCompensation;
		
		return bottom;
	}

	public static Point2D.Double getOutline(AbstractBoard board, double x, double distanceFromRail)
	{
		BezierSpline outline = ((BezierBoard)board).getOutline();
		
		double oy = outline.getValueAt(x);
		double normalAngle = outline.getNormalAngle(x);
		
		Point2D.Double point = new Point2D.Double();
		
		//Find the point at distance from outline
		point.x = x - ( (distanceFromRail )*Math.sin(normalAngle));
		point.y = oy - ( (distanceFromRail )*Math.cos(normalAngle));
		
		return point;
	}

	public static double getRailDeck(AbstractBoard board, double pos, double distanceFromRail, double skinThickness)
	{
		BezierSpline outline = ((BezierBoard)board).getOutline();

		double oy = outline.getValueAt(pos);
		double normalAngle = outline.getNormalAngle(pos);
	
		//Find the target point at distance from outline
		double x = pos - (distanceFromRail*Math.sin(normalAngle));
		double y = oy - (distanceFromRail*Math.cos(normalAngle));
	
		return getDeck(board, x, y, skinThickness);
	}
	
	public static double getRailBottom(AbstractBoard board, double pos, double distanceFromRail, double skinThickness)
	{
		BezierSpline outline = ((BezierBoard)board).getOutline();

		double oy = outline.getValueAt(pos);
		double normalAngle = outline.getNormalAngle(pos);
	
		//Find the target point at distance from outline
		double x = pos - (distanceFromRail*Math.sin(normalAngle));
		double y = oy - (distanceFromRail*Math.cos(normalAngle));
	
		return getBottom(board, x, y, skinThickness);
	}
}
