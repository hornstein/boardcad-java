package cadcore;

import board.BezierBoard;

import java.awt.geom.Point2D;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cadcore.MathUtils.Function;

class BezierBoardSLinearInterpolationSurfaceModel extends
		AbstractBezierBoardSurfaceModel {
	public Point3d getDeckAt(final BezierBoard brd, final double x,
			final double y) {
		final Function func = new Function() {
			public double f(double s) {
				return getPointAt(brd, x, s, -90.0, 90.0, true).y;
			};
		};
		double s = MathUtils.RootFinder.getRoot(func, y);

		return getPointAt(brd, x, s, -90.0, 90.0, true);
	}

	public Point3d getBottomAt(final BezierBoard brd, final double x,
			final double y) {
		final Function func = new Function() {
			public double f(double s) {
				return getPointAt(brd, x, s, 90.0, 270.0, true).y;
			};
		};
		double s = MathUtils.RootFinder.getRoot(func, y);

		return getPointAt(brd, x, s, 90.0, 270.0, true);
	}

	public Point3d getPointAt(final BezierBoard brd, double x, double s,
			double minAngle, double maxAngle,
			boolean useMinimumAngleOnSharpCorners) {
		if (x < 0.1)
			x = 0.1;

		if (x > brd.getLength() - 0.1)
			x = brd.getLength() - 0.1;

		BezierBoardCrossSection c1 = brd.getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = brd.getNextCrossSection(x);

		// Scaling
		double targetWidth = brd.getWidthAt(x);
		double targetThickness = brd.getThicknessAtPos(x);

		double c1Width = c1.getWidth();
		double c1Thickness = c1.getThicknessAtPos(BezierSpline.ZERO);

		double c2Width = c2.getWidth();
		double c2Thickness = c2.getThicknessAtPos(BezierSpline.ZERO);

		double c1ThicknessScale = targetThickness / c1Thickness;
		double c1WidthScale = targetWidth / c1Width;

		double c2ThicknessScale = targetThickness / c2Thickness;
		double c2WidthScale = targetWidth / c2Width;

		BezierSpline c1Spline = new BezierSpline();
		BezierSpline c2Spline = new BezierSpline();

		c1Spline.set(c1.getBezierSpline());
		c1Spline.scale(c1ThicknessScale, c1WidthScale);

		c2Spline.set(c2.getBezierSpline());
		c2Spline.scale(c2ThicknessScale, c2WidthScale);

		// double c1SplineWidth = c1Spline.getMaxX();
		// double c2SplineWidth = c2Spline.getMaxX();
		//
		// double c1SplineThick = c1Spline.getMaxY();
		// double c2SplineThick = c2Spline.getMaxY();

		// System.out.printf("targetWidth: %f targetThickness: %f c1SplineWidth x2: %f c2SplineWidth x2: %f c1SplineThick: %f c2SplineThick: %f\n",
		// targetWidth, targetThickness, c1SplineWidth*2.0, c2SplineWidth*2.0,
		// c1SplineThick, c2SplineThick);

		// System.out.printf("getSurfacePoint()\n");
		// System.out.printf("Target width: %f thickness: %f\n", targetWidth,
		// targetThickness);
		// System.out.printf("C1 width: %f thickness: %f\n", c1Width,
		// c1Thickness);
		// System.out.printf("C2 width: %f thickness: %f\n", c2Width,
		// c2Thickness);
		// System.out.printf("C1 width scale: %f thickness scale: %f\n",
		// c1WidthScale, c1ThicknessScale);
		// System.out.printf("C2 width scale: %f thickness scale: %f\n",
		// c2WidthScale, c2ThicknessScale);

		double s1min = BezierSpline.ONE;
		double s2min = BezierSpline.ONE;
		double s1max = BezierSpline.ZERO;
		double s2max = BezierSpline.ZERO;

		if (minAngle > 0.0) {
			s1min = c1Spline.getSByNormalReverse(minAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
			s2min = c2Spline.getSByNormalReverse(minAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
		}

		if (maxAngle < 270.0) {
			s1max = c1Spline.getSByNormalReverse(maxAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
			s2max = c2Spline.getSByNormalReverse(maxAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
		}

		double current1S = ((s1max - s1min) * s) + s1min;
		double current2S = ((s2max - s2min) * s) + s2min;

		// Get the position first since we cheat with the crosssections at tip
		// and tail
		double pos1 = brd.getPreviousCrossSectionPos(x);
		double pos2 = brd.getNextCrossSectionPos(x);

		// New
		Point2D.Double v1 = c1Spline.getPointByS(current1S);
		Point2D.Double v2 = c2Spline.getPointByS(current2S);

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getPointAt() v1:%f, %f\n",
		// v1.x, v1.y);
		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getPointAt() v2:%f, %f\n",
		// v2.x, v2.y);

		// Get blended point
		double d = (x - pos1) / (pos2 - pos1);

		Point2D.Double ret = new Point2D.Double();
		ret.x = ((1 - d) * v1.x) + (d * v2.x);
		ret.y = ((1 - d) * v1.y) + (d * v2.y);

		double rockerAtPos = brd.getRockerAtPos(x);

		Point3d point = new Point3d(x, ret.x, ret.y + rockerAtPos);

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getPointAt() x:%f, result: %f, %f, %f\n",
		// x, point.x, point.y, point.z);

		return point;
	}

	public Vector3d getNormalAt(final BezierBoard brd, double x, double s,
			double minAngle, double maxAngle,
			boolean useMinimumAngleOnSharpCorners) {
		if (x < 0.1)
			x = 0.1;

		if (x > brd.getLength() - 0.1)
			x = brd.getLength() - 0.1;

		final double X_OFFSET = 0.1;
		final double S_OFFSET = 0.01;

		boolean flipNormal = false;

		// Get first blended point
		Point3d pointS = getPointAt(brd, x, s, minAngle, maxAngle,
				useMinimumAngleOnSharpCorners);

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getNormalAt() first point x:%f, result: %f, %f, %f\n",
		// x, pointS.x, pointS.y, pointS.z);

		// Get second blended point
		BezierBoardCrossSection c1 = brd.getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = brd.getNextCrossSection(x);

		// Scaling
		double targetWidth = brd.getWidthAt(x);
		double targetThickness = brd.getThicknessAtPos(x);

		double c1Width = c1.getWidth();
		double c1Thickness = c1.getThicknessAtPos(BezierSpline.ZERO);

		double c2Width = c2.getWidth();
		double c2Thickness = c2.getThicknessAtPos(BezierSpline.ZERO);

		double c1ThicknessScale = targetThickness / c1Thickness;
		double c1WidthScale = targetWidth / c1Width;

		double c2ThicknessScale = targetThickness / c2Thickness;
		double c2WidthScale = targetWidth / c2Width;

		BezierSpline c1Spline = new BezierSpline();
		BezierSpline c2Spline = new BezierSpline();

		c1Spline.set(c1.getBezierSpline());
		c1Spline.scale(c1ThicknessScale, c1WidthScale);

		c2Spline.set(c2.getBezierSpline());
		c2Spline.scale(c2ThicknessScale, c2WidthScale);

		// System.out.printf("getNormalAt()\n");
		// System.out.printf("Target width: %f thickness: %f\n", targetWidth,
		// targetThickness);
		// System.out.printf("C1 width: %f thickness: %f\n", c1Width,
		// c1Thickness);
		// System.out.printf("C2 width: %f thickness: %f\n", c2Width,
		// c2Thickness);
		// System.out.printf("C1 width scale: %f thickness scale: %f\n",
		// c1WidthScale, c1ThicknessScale);
		// System.out.printf("C2 width scale: %f thickness scale: %f\n",
		// c2WidthScale, c2ThicknessScale);

		double s1min = BezierSpline.ONE;
		double s2min = BezierSpline.ONE;
		double s1max = BezierSpline.ZERO;
		double s2max = BezierSpline.ZERO;

		if (minAngle > 0.0) {
			s1min = c1Spline.getSByNormalReverse(minAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
			s2min = c2Spline.getSByNormalReverse(minAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
		}

		if (maxAngle < 270.0) {
			s1max = c1Spline.getSByNormalReverse(maxAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
			s2max = c2Spline.getSByNormalReverse(maxAngle
					* MathUtils.DEG_TO_RAD, useMinimumAngleOnSharpCorners);
		}

		double current1S = ((s1max - s1min) * s) + s1min;
		double current2S = ((s2max - s2min) * s) + s2min;

		double current1SO = current1S + S_OFFSET;
		double current2SO = current2S + S_OFFSET;
		if (current1SO > 1.0 || current2SO > 1.0
				|| useMinimumAngleOnSharpCorners == false) {
			current1SO = current1S - S_OFFSET;
			current2SO = current2S - S_OFFSET;
			flipNormal = true;
		}

		// Get the position first since we cheat with the crosssections at tip
		// and tail
		double pos1 = brd.getPreviousCrossSectionPos(x);
		double pos2 = brd.getNextCrossSectionPos(x);

		Point2D.Double v1so = c1Spline.getPointByS(current1SO);
		Point2D.Double v2so = c2Spline.getPointByS(current2SO);

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getPointAt() v1:%f, %f\n",
		// v1.x, v1.y);
		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getPointAt() v2:%f, %f\n",
		// v2.x, v2.y);

		double pSO = (x - pos1) / (pos2 - pos1);

		Point2D.Double retSO = new Point2D.Double();
		retSO.x = ((1 - pSO) * v1so.x) + (pSO * v2so.x);
		retSO.y = ((1 - pSO) * v1so.y) + (pSO * v2so.y);

		double rockerAtX = brd.getRockerAtPos(x);

		Point3d pointSO = new Point3d(x, retSO.x, retSO.y + rockerAtX);

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getNormalAt() second point x:%f, result: %f, %f, %f\n",
		// x, pointSO.x, pointSO.y, pointSO.z);

		// Get third blended point
		double xo = x + X_OFFSET;

		Point3d pointXO = getPointAt(brd, xo, s, minAngle, maxAngle,
				useMinimumAngleOnSharpCorners);

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getNormalAt() third point x:%f, result: %f, %f, %f\n",
		// x, pointXO.x, pointXO.y, pointXO.z);

		// Calculate normal
		Vector3d vc = new Vector3d(0, pointS.y - pointSO.y, pointS.z
				- pointSO.z); // Vector across
		// vc.normalize();

		Vector3d vl = new Vector3d(xo - x, pointXO.y - pointS.y, pointXO.z
				- pointS.z); // Vector lengthwise
		// vl.normalize();

		Vector3d normalVec = new Vector3d();
		normalVec.cross(vl, vc);
		normalVec.normalize();

		if (flipNormal == true) {
			normalVec.scale(-1.0);
		}

		// System.out.printf("BezierBoardSLinearInterpolationSurfaceModel.getNormalAt() third point x:%f, result: %f, %f, %f\n",
		// x, pointXO.x, pointXO.y, pointXO.z);

		return normalVec;

	}

	public double getCrosssectionAreaAt(final BezierBoard brd, final double x,
			int splits) {
		MathUtils.FunctionXY deckFunc = new MathUtils.FunctionXY() {
			public Point2D.Double f(double s) {
				Point3d point = getPointAt(brd, x, s, -90.0, 90.0, true);
				return new Point2D.Double(point.y, point.z);
			}
		};
		double deckIntegral = MathUtils.Integral.getIntegral(deckFunc, 0.0,
				1.0, BezierBoard.AREA_SPLITS);

		MathUtils.FunctionXY bottomFunc = new MathUtils.FunctionXY() {
			public Point2D.Double f(double s) {
				Point3d point = getPointAt(brd, x, s, 90.0, 360.0, true);
				return new Point2D.Double(point.y, point.z);
			}
		};
		double bottomIntegral = MathUtils.Integral.getIntegral(bottomFunc, 0.0,
				1.0, BezierBoard.AREA_SPLITS);

		double area = deckIntegral - bottomIntegral;
		area *= 2.0;

		if (area < 0)
			area = 0.0;

		// System.out.printf("getCrosssectionAreaAt() x:%f area:%f\n", x, area);

		return area;
	}

}
