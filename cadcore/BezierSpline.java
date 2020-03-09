package cadcore;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class BezierSpline implements Cloneable {
	public static final double ZERO = 0.0000000001;
	public static final double ONE = 0.9999999999;

	static final int X = 0;
	static final int Y = 1;
	static final int MIN = 0;
	static final int MAX = 1;

	static final double ANGLE_TOLERANCE = 0.05 * MathUtils.DEG_TO_RAD; // 0.1degrees
	static final double ANGLE_T_TOLERANCE = 0.000002;
	static final int ANGLE_MAX_ITERATIONS = 50;
	static final int ANGLE_SPLITS = 112;

	static final double POS_TOLERANCE = 0.003; // 0.03mm
	static final int POS_MAX_ITERATIONS = 30;

	static final double LENGTH_TOLERANCE = 0.001;

	static final double MIN_MAX_TOLERANCE = 0.0001;
	static final int MIN_MAX_SPLITS = 96;

	static final double DISTANCE_TOLERANCE = 0.0001;

	protected ArrayList<BezierCurve> mCurves = new ArrayList<BezierCurve>();

	public BezierSpline() {

	}

	public void set(BezierSpline spline) {
		// Remove extra control points
		while (getNrOfControlPoints() > spline.getNrOfControlPoints()) {
			remove(0);
		}
		// Add extra control points
		while (getNrOfControlPoints() < spline.getNrOfControlPoints()) {
			append(new BezierKnot());
		}

		int nrControlPoints = getNrOfControlPoints();
		for (int i = 0; i < nrControlPoints; i++) {
			getControlPoint(i).set(spline.getControlPoint(i));
		}

	}

	public void append(BezierKnot controlPoint) {
		if (mCurves.size() == 0) {
			mCurves.add(new BezierCurve(controlPoint, null));
		} else if (mCurves.size() == 1 && mCurves.get(0).getEndKnot() == null) {
			mCurves.get(0).setEndKnot(controlPoint);
		} else {
			mCurves.add(new BezierCurve(mCurves.get(mCurves.size() - 1)
					.getEndKnot(), controlPoint));
		}
	}

	public void insert(int i, BezierKnot controlPoint) {
		BezierKnot next = null;
		if (i < getNrOfControlPoints()) {
			next = getControlPoint(i);
		}

		if (i > 0 && i - 1 < mCurves.size()) {
			BezierCurve prevCurve = mCurves.get(i - 1);
			prevCurve.setEndKnot(controlPoint);
		}

		BezierCurve newCurve = new BezierCurve(controlPoint, next);

		mCurves.add(i, newCurve);
	}

	public void remove(BezierKnot controlPoint) {
		int i = indexOf(controlPoint);

		remove(i);
	}

	public void remove(int i) {
		BezierCurve removeCurve = null;
		if (i < mCurves.size()) {
			removeCurve = mCurves.get(i);
		}

		if (i > 0 && i - 1 < mCurves.size()) {
			BezierCurve prevCurve = mCurves.get(i - 1);
			prevCurve.setEndKnot(removeCurve != null ? removeCurve.getEndKnot()
					: null);
		}

		mCurves.remove(removeCurve);
	}

	public BezierKnot getControlPoint(int i) {
		if (mCurves.size() == 0 || mCurves.size() < i - 1) {
			return null;
		}

		if (i == 0) {
			return mCurves.get(0).getStartKnot();
		} else {
			return mCurves.get(i - 1).getEndKnot();
		}
	}

	public BezierCurve getCurve(int i) {
		return mCurves.get(i);
	}

	public boolean isLastControlPointNull() {
		if (mCurves.size() == 0) {
			return true;
		}

		return (mCurves.get(mCurves.size() - 1).getEndKnot() == null);
	}

	public int indexOf(BezierKnot controlPoint) {
		for (int i = 0; i < mCurves.size() + 1; i++) {
			if (controlPoint == getControlPoint(i))
				return i;
		}
		return -1;
	}

	public int getNrOfControlPoints() {
		return mCurves.size() + (isLastControlPointNull() ? 0 : 1);
	}

	public int getNrOfCurves() {
		return mCurves.size();
	}

	public void clear() {
		mCurves.clear();
	}

	public double getValueAt(double pos) {
		int index = findMatchingBezierSegment(pos);

		if (index == -1) {
			return 0.0;
		}

		double value = mCurves.get(index).getYForX(pos);
		return value;
	}

	public double getValueAtReverse(double pos) {
		int index = findMatchingBezierSegmentReverse(pos);
		if (index != -1) {
			BezierCurve curve = mCurves.get(index);

			return curve.getYForX(pos);
		}

		return 0.0;
	}

	public double getCurvatureAt(double pos) {
		int index = findMatchingBezierSegment(pos);

		if (index == -1) // Not within patch
			return 0.0;

		BezierCurve curve = mCurves.get(index);

		return curve.getCurvatureAt(pos);

	}

	public double getMaxX() {
		double max = -100000;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double current = curve.getMaxX();

			if (current > max)
				max = current;
		}

		return max;
	}

	public Point2D.Double getPointByTT(double tt) {
		int index = (int) (tt * getNrOfCurves());
		double t = (tt * getNrOfCurves()) - index;
		if (tt >= 1) {
			index = getNrOfCurves() - 1;
			t = ONE;
		}

		BezierCurve curve = mCurves.get(index);

		return curve.getValue(t);
	}

	public double getNormalByTT(double tt) {
		int index = (int) (tt * getNrOfCurves());
		double t = (tt * getNrOfCurves()) - index;
		if (tt >= 1) {
			index = getNrOfCurves() - 1;
			t = ONE;
		}

		// System.out.printf("getNormalByTT() tt:%f index:%d t:%f\n", tt, index,
		// t);

		BezierCurve curve = mCurves.get(index);

		return curve.getTangent(t) + (Math.PI / 2.0);
	}

	public double getTTByLineIntersect(final Point2D.Double a,
			final Point2D.Double b) {
		final double slope = (b.y - a.y) / (b.x - a.x);
		final double p = a.y - (slope * a.x);

		MathUtils.Function func = new MathUtils.Function() {
			public double f(double tt) {

				// Bezier
				Point2D.Double bezierPos = getPointByTT(tt);

				// Line
				double lineY = (slope * bezierPos.x) + p;

				return bezierPos.y - lineY;
			}
		};

		double aTT = getTTByX(a.x);
		double bTT = getTTByX(b.x);

		if (aTT == bTT) {
			return bTT;
		}

		double tt = MathUtils.RootFinder.getRoot(func, 0.0, Math.min(aTT, bTT),
				Math.max(aTT, bTT));

		return tt;
	}

	public double getTTByX(double x) {
		MathUtils.Function func = new MathUtils.Function() {
			public double f(double tt) {
				return getPointByTT(tt).x;
			}
		};

		double tt = MathUtils.RootFinder.getRoot(func, x);

		return tt;
	}

	public double getTTByNormal(double angle) {
		MathUtils.Function func = new MathUtils.Function() {
			public double f(double tt) {
				return getNormalByTT(tt);
			}
		};

		double tt = MathUtils.RootFinder.getRoot(func, angle);

		return tt;
	}

	public double getLengthByTT(double tt) {
		int index = (int) (tt * getNrOfControlPoints());
		if (tt >= 1) {
			index = getNrOfControlPoints() - 1;
		}

		double length = 0.0;

		for (int i = 0; i < index; i++) {
			BezierCurve curve = mCurves.get(i);

			length += curve.getLength();
		}

		BezierCurve curve = mCurves.get(index);

		double t = tt - index;
		length += curve.getLength(0, t);

		return length;
	}

	public double getSByTT(double tt) {
		return getLengthByTT(tt) / getLength();
	}

	public double getMinX() {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < mCurves.size() - 1; i++) {
			BezierCurve curve = mCurves.get(i);

			double current = curve.getMinX();

			if (current < min)
				min = current;
		}

		return min;
	}

	public double getMaxY() {
		double max = -Double.MAX_VALUE;

		for (int i = 0; i < mCurves.size() - 1; i++) {
			BezierCurve curve = mCurves.get(i);

			double current = curve.getMaxY();

			if (current > max)
				max = current;
		}

		return max;
	}

	public double getMaxYInRange(double x0, double x1) {
		double max = -100000;

		double current = 0.0;
		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double minX = curve.getMinMaxNumerical(0.0, 1.0, MIN_MAX_SPLITS, X,
					MIN);
			double maxX = curve.getMinMaxNumerical(0.0, 1.0, MIN_MAX_SPLITS, X,
					MAX);

			if (minX > x1)
				continue;

			if (maxX < x0)
				continue;

			double t0 = curve.getTForX(x0, 0.1);
			double t1 = curve.getTForX(x1, 0.9);

			current = curve.getMinMaxNumerical(t0, t1, MIN_MAX_SPLITS, Y, MAX);

			if (current > max)
				max = current;

		}

		return max;
	}

	public double getXForMaxY() {
		double max = -100000;
		BezierCurve maxCurve = null;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double current = curve.getMinMaxNumerical(Y, MAX);

			if (current > max) {
				max = current;
				maxCurve = curve;
			}
		}

		double tMax = maxCurve.getTForMinMaxNumerical(0, 1, MIN_MAX_SPLITS, Y,
				MAX);
		double x = maxCurve.getXValue(tMax);
		return x;
	}

	public double getXForMaxYInRange(double x0, double x1) {
		double max = -100000;
		BezierCurve maxCurve = null;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double minX = curve.getMinMaxNumerical(0.0, 1.0, MIN_MAX_SPLITS, X,
					MIN);
			double maxX = curve.getMinMaxNumerical(0.0, 1.0, MIN_MAX_SPLITS, X,
					MAX);

			if (minX > x1)
				continue;

			if (maxX < x0)
				continue;

			double t0 = curve.getTForX(x0, 0.1);
			double t1 = curve.getTForX(x1, 0.9);

			double current = curve.getMinMaxNumerical(t0, t1, MIN_MAX_SPLITS,
					Y, MAX);

			if (current > max) {
				maxCurve = curve;
				max = current;
			}

		}

		double tMax = maxCurve.getTForMinMaxNumerical(0, 1, MIN_MAX_SPLITS, Y,
				MAX);

		double x = maxCurve.getXValue(tMax);

		return x;

	}

	public double getMinY() {
		double min = 100000;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double current = curve.getMinY();

			if (current < min)
				min = current;
		}

		return min;
	}

	public double getLengthByX(double pos) {
		double length = 0;
		int index = findMatchingBezierSegment(pos);

		if (index == -1) // Not within patch
		{
			return 0.0;
		}

		BezierCurve curve = mCurves.get(index);

		double t = curve.getTForX(pos, ONE);

		length += curve.getLength(ZERO, t);

		for (int i = 0; i < index; i++) {
			curve = mCurves.get(i);

			double currentLength = curve.getLength();

			length += currentLength;
		}

		return length;
	}

	public double getSByX(double pos) {
		return getLengthByX(pos) / getLength();
	}

	public Point2D.Double getPointByS(double s) {
		return getPointByCurveLength(s * getLength());
	}

	public Point2D.Double getPointByCurveLength(double curveLength) {
		double l = curveLength;
		double t = -1;

		BezierCurve curve = null;

		// Return endpoints if input curvelength is out of range
		if (curveLength <= 0.0) {
			curve = mCurves.get(0);

			return new Point2D.Double(curve.getXValue(ZERO),
					curve.getYValue(ZERO));
		}
		if (curveLength >= getLength()) {
			curve = mCurves.get(mCurves.size() - 1);

			return new Point2D.Double(curve.getXValue(ONE),
					curve.getYValue(ONE));
		}

		for (int i = 0; i < mCurves.size(); i++) {
			curve = mCurves.get(i);

			double currentLength = curve.getLength();

			if (l < currentLength) {
				t = curve.getTForLength(l);
				break;
			}

			l -= currentLength;
		}

		double x = curve.getXValue(t);
		double y = curve.getYValue(t);

		return new Point2D.Double(x, y);
	}

	// Returns the first point found that is the given distance from
	public Point2D.Double getPointByDistance(Point2D.Double point,
			double distance) {
		Point2D.Double best = null;
		double best_error = 10000;
		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double t = curve.getTForDistance(point, distance);

			Point2D.Double current = curve.getValue(t);
			double error = Math.abs(VecMath.getVecLength(point, current));

			if (error < best_error) {
				best = current;
				best_error = error;
			}
		}

		return best;
	}

	public Point2D.Double getPointByLineIntersection(Point2D.Double a,
			Point2D.Double b) {
		return getPointByTT(getTTByLineIntersect(a, b));
	}

	public double getSByNormal(double angle) {
		return getSByTangent(angle - Math.PI / 2.0);
	}

	public double getSByTangent(double angle) {
		return getLengthByTangent(angle) / getLength();
	}

	public double getLengthByNormal(double angle) {
		return getLengthByTangent(angle - Math.PI / 2.0) / getLength();
	}

	public double getLengthByTangent(double angle) {
		double length = 0;
		double t = 0;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double endAngle = curve.getTangent(ZERO);
			double otherEndAngle = curve.getTangent(ONE);

			double initial_t = (angle - endAngle) / (otherEndAngle - endAngle);
			if (initial_t < 0.0)
				initial_t = 0.0;
			if (initial_t > 1.0)
				initial_t = 1.0;
			double last_t = initial_t + 0.1;
			if (last_t > 1.0)
				last_t -= 0.2;

			t = curve.getTForTangent2(angle, initial_t, last_t);

			double tAngle = curve.getTangent(t);
			if (Math.abs(tAngle - angle) < ANGLE_TOLERANCE) {
				length += curve.getLength(ZERO, t);
				break;
			}

			length += curve.getLength();

		}
		return length;
	}

	public double getSByNormalReverse(double angle) {
		return getSByTangentReverse(angle - Math.PI / 2.0);
	}

	public double getSByNormalReverse(double angle,
			boolean useMinimumAngleOnSharpCorners) {
		return getSByTangentReverse(angle - Math.PI / 2.0,
				useMinimumAngleOnSharpCorners);
	}

	public double getSByTangentReverse(double angle) {
		double s = getLengthByTangentReverse(angle) / getLength();
		s = MathUtils.clamp(s, ZERO, ONE);
		return s;
	}

	public double getSByTangentReverse(double angle,
			boolean useMinimumAngleOnSharpCorners) {
		double s = getLengthByTangentReverse(angle,
				useMinimumAngleOnSharpCorners) / getLength();
		s = MathUtils.clamp(s, ZERO, ONE);
		return s;
	}

	public double getLengthByNormalReverse(double angle) {
		return getLengthByTangentReverse(angle - Math.PI / 2.0) / getLength();
	}

	public double getLengthByNormalReverse(double angle,
			boolean useMinimumAngleOnSharpCorners) {
		return getLengthByTangentReverse(angle - Math.PI / 2.0,
				useMinimumAngleOnSharpCorners) / getLength();
	}

	public double getLengthByTangentReverse(double angle) {
		return getLengthByTangentReverse(angle, true);
	}

	public double getLengthByTangentReverse(double targetAngle,
			boolean useMinimumAngleOnSharpCorners) {
		// System.out.printf("getLengthByTangentReverseScaled() targetAngle:%f\n",
		// targetAngle/BezierBoard.DEG_TO_RAD);

		double length = 0;
		double t = 0;

		double minAngleError = 10000;
		double minErrorT = -1;
		int minAngleErrorSection = -1;

		boolean targetFound = false;

		int i;
		for (i = mCurves.size() - 1; i >= 0; i--) {
			BezierCurve curve = mCurves.get(i);

			double startAngle = curve.getTangent(ZERO);
			double endAngle = curve.getTangent(ONE);
			// System.out.printf("getLengthByTangentReverseScaled() StartAngle:%f EndAngle:%f TargetAngle:%f\n",
			// startAngle/BezierBoard.DEG_TO_RAD +90.0,
			// endAngle/BezierBoard.DEG_TO_RAD + 90.0,
			// targetAngle/BezierBoard.DEG_TO_RAD +90.0);
			if (startAngle >= targetAngle && endAngle <= targetAngle) {
				// System.out.printf("getLengthByTangentReverseScaled() Value should be in this span, StartAngle:%f EndAngle:%f TargetAngle:%f\n",
				// startAngle/BezierBoard.DEG_TO_RAD +90.0,
				// endAngle/BezierBoard.DEG_TO_RAD + 90.0,
				// targetAngle/BezierBoard.DEG_TO_RAD +90.0);
			}

			// if(k1.mContinous == false)
			// {
			if (endAngle > targetAngle) {
				// System.out.printf("getLengthByTangentReverseScaled() i:%d, endAngle > targetAngle endAngle:%f targetAngle:%f\n",i,
				// endAngle/BezierBoard.DEG_TO_RAD,
				// targetAngle/BezierBoard.DEG_TO_RAD);
				if (useMinimumAngleOnSharpCorners == true) {
					i += 1;
					// System.out.printf("getLengthByTangentReverseScaled() Target found by endAngle(%f) > targetAngle(%f) with use minimun angle on shape corner\n",
					// endAngle/BezierBoard.DEG_TO_RAD+90.0,
					// targetAngle/BezierBoard.DEG_TO_RAD+90.0);
				} else {
					length = curve.getLength(ZERO, ONE - .05);
					// System.out.printf("getLengthByTangentReverseScaled() Target found by endAngle(%f) > targetAngle(%f)\n",
					// endAngle/BezierBoard.DEG_TO_RAD+90.0,
					// targetAngle/BezierBoard.DEG_TO_RAD+90.0);
				}
				targetFound = true;
				break;
			}
			// }

			// t = getTForTangent(angle,ZERO,ONE, ANGLE_SPLITS);
			double initial_t = (targetAngle - startAngle)
					/ (endAngle - startAngle);
			if (initial_t < 0.0)
				initial_t = 0.0;
			if (initial_t > 1.0)
				initial_t = 1.0;
			double last_t = initial_t + 0.1;
			if (last_t > 1.0)
				last_t -= 0.2;

			// System.out.printf("getLengthByTangentReverseScaled() i:%d startAngle:%f end_angle:%f initial_t:%f last_t:%f\n",i,
			// startAngle/BezierBoard.DEG_TO_RAD,
			// endAngle/BezierBoard.DEG_TO_RAD, initial_t, last_t);

			t = curve.getTForTangent2(targetAngle, initial_t, last_t);

			double tAngle = curve.getTangent(t);
			double angleError = Math.abs(tAngle - targetAngle);
			if (minAngleError > angleError) {
				minAngleError = angleError;
				minErrorT = t;
				minAngleErrorSection = i;
			}
			if (angleError <= ANGLE_TOLERANCE) {
				length = curve.getLength(ZERO, t);
				targetFound = true;
				// System.out.printf("getLengthByTangentReverseScaled() Target found by matching angle: %f, error: %f\n",
				// tAngle/BezierBoard.DEG_TO_RAD+90.0,
				// angleError/BezierBoard.DEG_TO_RAD);
				break;
			} else if (startAngle >= targetAngle && endAngle <= targetAngle) {
				// System.out.printf("getLengthByTangentReverseScaled() Value should be in this span but error is too large, StartAngle:%f EndAngle:%f Target: %f Returned: %f t:%f\n",
				// startAngle/BezierBoard.DEG_TO_RAD +90.0,
				// endAngle/BezierBoard.DEG_TO_RAD +90.0,
				// targetAngle/BezierBoard.DEG_TO_RAD +90.0,
				// tAngle/BezierBoard.DEG_TO_RAD+90.0, t);
				t = curve.getTForTangent2(targetAngle, initial_t, last_t);
			}

		}

		if (targetFound == false && minAngleErrorSection != -1) {
			// Use the nearest target
			BezierCurve curve = mCurves.get(minAngleErrorSection);

			length = curve.getLength(ZERO, minErrorT);
		}

		length += getLengthByControlPointIndex(0, i);

		// DEBUG sanity check
		double tangent = getTangentByCurveLength(length);
		if (Math.abs(tangent - targetAngle) > 0.5 && targetFound) {
			// System.out.printf("getLengthByTangentReverseScaled()  getTangentByCurveLength() returned different angle. Target: %f Returned: %f  length:%f curveLength:%f\n",
			// targetAngle/BezierBoard.DEG_TO_RAD +90.0,
			// tangent/BezierBoard.DEG_TO_RAD + 90.0, length,
			// lengthScaled(scaleX, scaleY));
			targetFound = !targetFound;
		}

		return length;
	}

	public double getLengthByControlPointIndex(int startIndex, int endIndex) {
		double length = 0;

		for (int i = startIndex; i < endIndex; i++) {
			BezierCurve curve = mCurves.get(i);

			length += curve.getLength();

		}
		return length;
	}

	public double getNormalAngle(double pos) {
		return getTangentAt(pos) - Math.PI / 2.0;
	}

	public Point2D.Double getNormalAt(double pos) {
		double angle = getNormalAngle(pos);

		double x = Math.sin(angle);
		double y = Math.cos(angle);

		return new Point2D.Double(x, y);
	}

	public double getNormalByS(double s) {
		return getNormalByCurveLength(s * getLength());
	}

	public double getNormalByCurveLength(double curveLength) {
		return getTangentByCurveLength(curveLength) + Math.PI / 2.0;
	}

	public double getCurvatureByS(double s) {
		return getCurvatureByCurveLength(s * getLength());
	}

	public double getCurvatureByCurveLength(double curveLength) {
		double left = curveLength;
		double t = -1;

		if (curveLength <= 0.0) {
			return 0.0;
		}

		if (curveLength >= getLength()) {
			return 0.0;
		}

		BezierCurve curve = null;
		for (int i = 0; i < mCurves.size(); i++) {
			curve = mCurves.get(i);

			double currentLength = curve.getLength();

			if (left < currentLength) {
				break;
			}

			left -= currentLength;
		}

		t = curve.getTForLength(left);

		double curvature = curve.getCurvature(t);

		return curvature;
	}

	public double getTangentAt(double pos) {
		int index = findMatchingBezierSegment(pos);
		if (index == -1) {
			return 0.0;
		}

		BezierCurve curve = mCurves.get(index);
		return curve.getTangentAt(pos);
	}

	public double getTangentByS(double s) {
		return getTangentByCurveLength(s * getLength());
	}

	public double getTangentByCurveLength(double curveLength) {
		double l = curveLength;
		double t = -1;

		BezierCurve curve = null;

		for (int i = 0; i < mCurves.size(); i++) {
			curve = mCurves.get(i);

			double currentLength = curve.getLength();

			if (l < currentLength) {
				t = curve.getTForLength(l);
				break;
			}

			l -= currentLength;
		}

		return curve.getTangent(t);
	}

	public double getIntegral(double a, double b, int splits) // Numerical
																// integration
																// using
																// composite
																// simpsons rule
	{

		MathUtils.Function deckFunc = new MathUtils.Function() {
			public double f(double x) {
				return getValueAt(x);
			}
		};

		double result = MathUtils.Integral.getIntegral(deckFunc, a, b, splits);

		return result;
	}

	public double getLength() {
		double length = 0;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			length += curve.getLength();
		}

		return length;
	}

	public void scale(double verticalScale, double horizontalScale) {
		for (int i = 0; i < mCurves.size(); i++) {
			if (i == 0) {
				BezierKnot startKnot = mCurves.get(i).getStartKnot();
				startKnot.scale(horizontalScale, verticalScale);
			}
			BezierKnot endKnot = mCurves.get(i).getEndKnot();
			if (endKnot != null) {
				endKnot.scale(horizontalScale, verticalScale);
			}
		}
	}

	public int getSplitControlPoint(Point2D.Double nearPoint,
			BezierKnot returned) {
		double nearestDist = 100000000;
		int index = 0;
		double t = 0;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double tc = curve.getClosestT(nearPoint);

			double x = curve.getXValue(tc);
			double y = curve.getYValue(tc);

			double dist = VecMath.getVecLength(x, y, nearPoint.x, nearPoint.y);
			if (nearestDist > dist) {
				nearestDist = dist;
				index = i;
				t = tc;
			}

		}

		BezierCurve curve = mCurves.get(index);
		returned.set(curve.getSplitControlPoint(t));

		return index + 1; // Insertion point

	}

	public double getDistanceToPoint(Point2D.Double point) {
		double nearestDist = 100000000;
		// int index = 0;
		// double t = 0;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double tc = curve.getClosestT(point);

			double x = curve.getXValue(tc);
			double y = curve.getYValue(tc);

			double dist = VecMath.getVecLength(x, y, point.x, point.y);
			if (nearestDist > dist) {
				nearestDist = dist;
				// index = i;
				// t = tc;
			}

		}

		return nearestDist; // Insertion point

	}

	int findMatchingBezierSegment(double pos) {
		int result = findMatchingBezierSegmentSimple(pos);
		if (result < 0) {
			result = findMatchingBezierSegmentMinMax(pos);
		}

		return result;
	}

	int findMatchingBezierSegmentSimple(double pos) {
		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);
			double lx = curve.getStartKnot().getEndPoint().x;
			double ux = curve.getEndKnot().getEndPoint().x;
			if (lx <= pos && ux >= pos) {
				return i;
			}
		}
		return -1;
	}

	int findMatchingBezierSegmentMinMax(double pos) {
		// Didn't find any matching segment

		// Iterate through all segments and see if we're within max/min x value
		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			double lx = curve.getMinMaxNumerical(X, MIN);
			double ux = curve.getMinMaxNumerical(X, MAX);

			if ((lx <= pos && ux >= pos) || (ux <= pos && lx >= pos)) {
				return i;
			}

		}
		return -1;
	}

	int findMatchingBezierSegmentReverse(double pos) {
		int result = findMatchingBezierSegmentSimpleReverse(pos);
		if (result < 0) {
			result = findMatchingBezierSegmentMinMaxReverse(pos);
		}
		return result;
	}

	int findMatchingBezierSegmentSimpleReverse(double pos) {
		int index = -1;

		for (int i = mCurves.size() - 1; i >= 0; i--) {
			BezierCurve curve = mCurves.get(i);
			double lx = curve.getStartKnot().getEndPoint().x;
			double ux = curve.getEndKnot().getEndPoint().x;
			if ((lx <= pos && ux >= pos) || (ux <= pos && lx >= pos)) {
				return i;
			}
		}
		return -1;
	}

	int findMatchingBezierSegmentMinMaxReverse(double pos) {
		// Iterate through all segments and see if we're within max/min x value
		for (int i = mCurves.size() - 1; i >= 0; i--) {
			BezierCurve curve = mCurves.get(i);

			double lx = curve.getMinMaxNumerical(X, MIN);
			double ux = curve.getMinMaxNumerical(X, MAX);

			if (lx <= pos && ux >= pos) {
				return i;
			}

		}
		return -1;
	}

	public BezierKnot findBestMatch(Point2D.Double brdPos) {
		BezierKnot bestMatch = null;
		int bestMatchPointIndex = -1;
		double bestMatchDistance = Double.MAX_VALUE;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			BezierKnot startKnot = curve.getStartKnot();
			BezierKnot endKnot = curve.getEndKnot();

			if (endKnot == null) // Incomplete curve, must be at end so break
									// out
				break;

			// Check tangent to next for startknot
			double distance = (double) brdPos.distance(startKnot
					.getTangentToNext());
			if (distance < bestMatchDistance) {
				bestMatch = startKnot;
				bestMatchPointIndex = BezierKnot.NEXT_TANGENT;
				bestMatchDistance = distance;
			}

			// Check tangent to prev for endknot
			distance = (double) brdPos.distance(endKnot.getTangentToPrev());
			if (distance < bestMatchDistance) {
				bestMatch = endKnot;
				bestMatchPointIndex = BezierKnot.PREVIOUS_TANGENT;
				bestMatchDistance = distance;
			}

			// Check endpoint for startknot
			distance = (double) brdPos.distance(startKnot.getEndPoint());
			if (distance < bestMatchDistance) {
				bestMatch = startKnot;
				bestMatchPointIndex = BezierKnot.END_POINT;
				bestMatchDistance = distance;
			}

			// Check tangent to prev for endknot
			distance = (double) brdPos.distance(endKnot.getEndPoint());
			if (distance < bestMatchDistance) {
				bestMatch = endKnot;
				bestMatchPointIndex = BezierKnot.END_POINT;
				bestMatchDistance = distance;
			}

		}

		return bestMatch;
	}

	public int getBestMatchWhich(Point2D.Double brdPos) {
		BezierKnot bestMatch = null;
		int bestMatchPointIndex = -1;
		double bestMatchDistance = Double.MAX_VALUE;

		for (int i = 0; i < mCurves.size(); i++) {
			BezierCurve curve = mCurves.get(i);

			BezierKnot startKnot = curve.getStartKnot();
			BezierKnot endKnot = curve.getEndKnot();

			if (endKnot == null) // Incomplete curve, must be at end so break
									// out
				break;

			// Check tangent to next for startknot
			double distance = (double) brdPos.distance(startKnot
					.getTangentToNext());
			if (distance < bestMatchDistance) {
				bestMatch = startKnot;
				bestMatchPointIndex = BezierKnot.NEXT_TANGENT;
				bestMatchDistance = distance;
			}

			// Check tangent to prev for endknot
			distance = (double) brdPos.distance(endKnot.getTangentToPrev());
			if (distance < bestMatchDistance) {
				bestMatch = endKnot;
				bestMatchPointIndex = BezierKnot.PREVIOUS_TANGENT;
				bestMatchDistance = distance;
			}

			// Check endpoint for startknot
			distance = (double) brdPos.distance(startKnot.getEndPoint());
			if (distance < bestMatchDistance) {
				bestMatch = startKnot;
				bestMatchPointIndex = BezierKnot.END_POINT;
				bestMatchDistance = distance;
			}

			// Check tangent to prev for endknot
			distance = (double) brdPos.distance(endKnot.getEndPoint());
			if (distance < bestMatchDistance) {
				bestMatch = endKnot;
				bestMatchPointIndex = BezierKnot.END_POINT;
				bestMatchDistance = distance;
			}

		}

		return bestMatchPointIndex;
	}

	public Object clone() {
		BezierSpline spline = null;
		try {
			spline = (BezierSpline) super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Exception in BezierSpline::clone(): "
					+ e.toString());
			throw new Error("CloneNotSupportedException in BrdCommand");
		}

		spline.mCurves = new ArrayList<BezierCurve>();
		for (int i = 0; i < this.getNrOfControlPoints(); i++) {
			spline.append((BezierKnot) this.getControlPoint(i).clone());
		}
		return spline;
	}

	public String toString() {
		String string = new String();
		for (int i = 0; i < this.getNrOfControlPoints(); i++) {
			if (string.length() > 0)
				string.concat(";");
			string.concat(this.getControlPoint(i).toString());
		}

		return string;
	}

	public void fromString(String string) {
		String[] values = string.split(";");
		for (int i = 0; i < values.length; i++) {
			BezierKnot point = new BezierKnot();
			point.fromString(values[i]);
			this.append(point);
		}

	}
}
