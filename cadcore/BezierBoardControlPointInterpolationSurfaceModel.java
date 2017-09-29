package cadcore;

import board.BezierBoard;

import java.awt.geom.Point2D;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cadcore.MathUtils.Function;

class BezierBoardControlPointInterpolationSurfaceModel extends
		AbstractBezierBoardSurfaceModel {

	synchronized public Point3d getDeckAt(final BezierBoard brd, final double x,
			final double y) {
		Function func = new Function() {
			public double f(double s) {
				return getPointAt(brd, x, s, -90.0, 90.0, true).y;
			};
		};
		double s = MathUtils.RootFinder.getRoot(func, y);

		Point3d point = getPointAt(brd, x, s, -90.0, 90.0, true);

		// System.out.printf("getDeckAt() s:%f point: %f,%f,%f\n", s,
		// point.x,point.y,point.z);

		return point;
	}

	synchronized public Point3d getBottomAt(final BezierBoard brd, final double x,
			final double y) {
		Function func = new Function() {
			public double f(double s) {
				return getPointAt(brd, x, s, 90.0, 270.0, true).y;
			};
		};
		double s = MathUtils.RootFinder.getRoot(func, y);

		return getPointAt(brd, x, s, 90.0, 270.0, true);
	}

	synchronized public Point3d getPointAt(final BezierBoard brd, double x, double s,
			double minAngle, double maxAngle,
			boolean useMinimumAngleOnSharpCorners) {
		if (x < 0.1)
			x = 0.1;

		if (x > brd.getLength() - 0.1)
			x = brd.getLength() - 0.1;

		BezierBoardCrossSection crossSection = brd
				.getInterpolatedCrossSection(x);
		if (crossSection == null)
			return new Point3d(0.0, 0.0, 0.0);

		double minS = BezierSpline.ONE;
		double maxS = BezierSpline.ZERO;

		if (minAngle > 0.0) {
			minS = crossSection.getBezierSpline().getSByNormalReverse(
					minAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (maxAngle < 270.0) {
			maxS = crossSection.getBezierSpline().getSByNormalReverse(
					maxAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (minS > BezierSpline.ONE)
			minS = BezierSpline.ONE;
		if (maxS < BezierSpline.ZERO)
			maxS = BezierSpline.ZERO;

		double currentS = ((maxS - minS) * s) + minS;
		Point2D.Double point2D = crossSection.getPointAtS(currentS);

		Point3d point = new Point3d(x, point2D.x, point2D.y);
		point.z += brd.getRockerAtPos(x);

		return point;
	}

	synchronized public Vector3d getNormalAt(final BezierBoard brd, double x, double s,
			double minAngle, double maxAngle,
			boolean useMinimumAngleOnSharpCorners) {
		if (x < 0.1)
			x = 0.1;

		if (x > brd.getLength() - 0.1)
			x = brd.getLength() - 0.1;

		final double X_OFFSET = 0.1;
		final double S_OFFSET = 0.01;

		boolean flipNormal = false;

		if (x < 1.0) {
			x = 1.0;
		}
		if (x > brd.getLength() - 1.0) {
			x = brd.getLength() - 1.0;
		}

		BezierBoardCrossSection crossSection = (BezierBoardCrossSection) (brd
				.getInterpolatedCrossSection(x).clone());
		if (crossSection == null)
			return new Vector3d(0.0, 0.0, 0.0);

		double minS = BezierSpline.ONE;
		double maxS = BezierSpline.ZERO;

		if (minAngle > 0.0) 
		{
			minS = crossSection.getBezierSpline().getSByNormalReverse(
					minAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (maxAngle < 270.0) 
		{
			maxS = crossSection.getBezierSpline().getSByNormalReverse(
					maxAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (minS > BezierSpline.ONE)
		{
			minS = BezierSpline.ONE;
		}
		
		if (maxS < BezierSpline.ZERO)
		{
			maxS = BezierSpline.ZERO;
		}

		double currentS = ((maxS - minS) * s) + minS;

		double so = currentS + S_OFFSET;
		if (so > 1.0) {
			so = currentS - S_OFFSET;
			flipNormal = true;
		}

		double xo = x + X_OFFSET;
		BezierBoardCrossSection crossSectionXO = (BezierBoardCrossSection) (brd.getInterpolatedCrossSection(xo).clone());
		if (crossSectionXO == null)return new Vector3d(0.0, 0.0, 0.0);

		double rockerX = brd.getBottom().getValueAt(x);
		double rockerXO = brd.getBottom().getValueAt(xo);

		Point2D.Double p = crossSection.getPointAtS(currentS);
		Point2D.Double pso = crossSection.getPointAtS(so);
		Point2D.Double pxo = crossSectionXO.getPointAtS(currentS);

		// System.out.printf("x:%f, xo: %f crossection width: %f crossectionXO width: %f\n",
		// x, xo, crossSection.getWidth(), crossSectionXO.getWidth());

		Vector3d vc = new Vector3d(0, p.x - pso.x, p.y - pso.y); // Vector
																	// across
		vc.normalize();

		Vector3d vl = new Vector3d(xo - x, pxo.x - p.x, pxo.y - p.y + rockerXO
				- rockerX); // Vector lengthwise
		vl.normalize();

		Vector3d normalVec = new Vector3d();
		normalVec.cross(vl, vc);
		normalVec.normalize();

		if (flipNormal == true) {
			
			normalVec.scale(-1.0);
		}

		//DEBUG 
//		if(cv.angle(lv)*180.0/Math.PI < 15)
//		{ 
//			System.out.printf( "getSurfaceNormalAtPos() Low angle between vectors: %f\n", cv.angle(lv)*180.0/Math.PI); 
//		}
//		 
//		if(last != null && last.angle(normalVec)*180.0/Math.PI > 15) 
//		{
//			System.out.printf( "getSurfaceNormalAtPos() large angle between last and current normal:%f\n" , last.angle(normalVec)*180.0/Math.PI); 
//		}
//		 
//		last = new Vector3d(normalVec);
//		 
//		System.out.printf( "getNormalAtPos() x:%f, xo:%f, sa:%f, sao:%f, sb:%f, sbo:%f\n", x, xo, sa, sao, sb, sbo); //
//		System.out.printf("getNormalAtPos() %f, %f, %f\n", normalVec.x,normalVec.y,normalVec.z);

		return normalVec;
	}

	synchronized public double getCrosssectionAreaAt(final BezierBoard brd, final double x,
			int splits) {
		final BezierBoardCrossSection crossSection = brd
				.getInterpolatedCrossSection(x);
		if (crossSection == null)
			return 0.0;

		double ttAtRail = crossSection.getBezierSpline().getTTByNormal(
				90.0 * MathUtils.DEG_TO_RAD);

		MathUtils.FunctionXY func = new MathUtils.FunctionXY() {
			public Point2D.Double f(double tt) {
				return crossSection.getBezierSpline().getPointByTT(tt);
			}
		};

		double deckIntegral = MathUtils.Integral.getIntegral(func, ttAtRail,
				1.0, splits);

		double bottomIntegral = MathUtils.Integral.getIntegral(func, 0.0,
				ttAtRail, splits);

		double area = deckIntegral - bottomIntegral;
		area *= 2.0;

		if (area < 0)
			area = 0.0;

		// System.out.printf("getCrosssectionAreaAt() x:%f area:%f\n", x, area);

		return area;
	}
}
