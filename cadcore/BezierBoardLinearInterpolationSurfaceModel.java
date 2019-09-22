package cadcore;

import board.BezierBoard;

import javax.vecmath.Point3d;


class BezierBoardLinearInterpolationSurfaceModel extends AbstractBezierBoardSurfaceModel
{
	public Point3d getDeckAt(BezierBoard brd, final double x, final double y)
	{		
		//Calculate scales
		double widthAtPos = brd.getWidthAtPos(x);
		double thicknessAtPos = brd.getThicknessAtPos(x);
		
		//Get the position from function since we cheat with the crosssections at tip and tail
		double pos1 = brd.getPreviousCrossSectionPos(x);
		double pos2 = brd.getNextCrossSectionPos(x);

		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		BezierBoardCrossSection c1 = brd.getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = brd.getNextCrossSection(x);

		//Get scales and values
		double Scale1Y = c1.getWidth() / widthAtPos;
		double Scale1Z = c1.getCenterThickness()/ thicknessAtPos;

		double Scale2Y = c2.getWidth() / widthAtPos;
		double Scale2Z = c2.getCenterThickness()/ thicknessAtPos;

		double v1 = c1.getDeckAtPos(y*Scale1Y);
		double v2 = c2.getDeckAtPos(y*Scale2Y);

		v1 /= Scale1Z;
		v2 /= Scale2Z;

		//Get blended point
		double p = (x - pos1)/(pos2 - pos1);

		double z  = ((1-p)*v1) + (p*v2);

		return new Point3d(x,y,z);
	}
	

	public Point3d getBottomAt(final BezierBoard brd, final double x, final double y)
	{
		//Calculate scales
		double widthAtPos = brd.getWidthAtPos(x);
		double thicknessAtPos = brd.getThicknessAtPos(x);

		//Get the position first since we cheat with the crosssections at tip and tail
		double pos1 = brd.getPreviousCrossSectionPos(x);
		double pos2 = brd.getNextCrossSectionPos(x);

		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		BezierBoardCrossSection c1 = brd.getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = brd.getNextCrossSection(x);

		double Scale1Y = c1.getWidth() / widthAtPos;
		double Scale1Z = c1.getCenterThickness()/ thicknessAtPos;

		double Scale2Y = c2.getWidth() / widthAtPos;
		double Scale2Z = c2.getCenterThickness()/ thicknessAtPos;

		double v1 = c1.getBottomAtPos(y*Scale1Y);
		double v2 = c2.getBottomAtPos(y*Scale2Y);

		v1 /= Scale1Z;
		v2 /= Scale2Z;

		double p = (x - pos1)/(pos2 - pos1);

		double z  = ((1-p)*v1) + (p*v2);

		return new Point3d(x,y,z);
	}
	
	public Point3d getPointAt(BezierBoard brd, final double x, final double s, final double minAngle, final double maxAngle, boolean useMinimumAngleOnSharpCorners)
	{
		return null;
	}

	public double getCrosssectionAreaAt(final BezierBoard brd, final double x, int splits)
	{
		double a = 0.01;
		double b = (brd.getWidthAtPos(x)/2) - 0.01;
	
	    final MathUtils.Function deckFunc = new MathUtils.Function(){public double f(double y){return getDeckAt(brd, x,y).z;}};
		
		double deckIntegral =  MathUtils.Integral.getIntegral(deckFunc, a, b, splits);
	
		final MathUtils.Function bottomFunc = new MathUtils.Function(){public double f(double y){return getBottomAt(brd, x,y).z;}};
		
		double bottomIntegral =  MathUtils.Integral.getIntegral(bottomFunc, a, b, splits);
		
		double area = deckIntegral-bottomIntegral;
		area *= 2.0f;
		
		if(area < 0)
			area = 0.0;

//		System.out.printf("getCrosssectionAreaAt() x:%f area:%f\n", x, area);		
		
		return area;
	}

}
