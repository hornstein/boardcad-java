package cadcore;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import cadcore.BezierKnot;

public class BezierCurve implements Cloneable, BezierKnotChangeListener {
	BezierKnot mStartKnot;
	BezierKnot mEndKnot;

	protected double coeff0 = 0.0;
	protected double coeff1 = 0.0;
	protected double coeff2 = 0.0;
	protected double coeff3 = 0.0;
	protected double coeff4 = 0.0;
	protected double coeff5 = 0.0;
	protected double coeff6 = 0.0;
	protected double coeff7 = 0.0;

	boolean mCoeffDirty = true;
	boolean mLengthDirty = true;

	double mLength = 0.0;

	public BezierCurve(double P0X, double P0Y, double P1X, double P1Y,
			double P2X, double P2Y, double P3X, double P3Y) {
		mStartKnot = new BezierKnot(P0X, P0Y, 0.0, 0.0, P1X, P1Y);
		mEndKnot = new BezierKnot(P3X, P3Y, P2X, P2Y, 0.0, 0.0);
	}

	public BezierCurve(BezierKnot startKnot, BezierKnot endKnot) {
		mStartKnot = startKnot;
		mEndKnot = endKnot;

		if (mStartKnot != null)
			mStartKnot.addChangeListener(this);
		if (mEndKnot != null)
			mEndKnot.addChangeListener(this);
	}

	public BezierKnot getStartKnot() {
		return mStartKnot;
	}

	public BezierKnot getEndKnot() {
		return mEndKnot;
	}

	void setStartKnot(BezierKnot startKnot) {
		mStartKnot = startKnot;
		if (mStartKnot != null)
			mStartKnot.addChangeListener(this);
		setDirty();
	}

	void setEndKnot(BezierKnot endKnot) {
		mEndKnot = endKnot;
		if (mEndKnot != null)
			mEndKnot.addChangeListener(this);
		setDirty();
	}

	private void calculateCoeff() {
		if (!mCoeffDirty)
			return;

		// MORE ON CUBIC SPLINE MATH
		// =========================
		// by Don Lancaster

		// In graph space, the cubic spline is defined by eight points. A pair
		// of
		// initial points x0 and y0. A pair of end points x3 and y3. A pair of
		// first influence points x1 and y1. And a pair of second influence
		// points
		// x2 and y2.

		// A cubic spline consists of two parametric equations in t (or time)
		// space...

		// x = At^3 + Bt^2 + Ct + D
		// y = Dt^3 + Et^2 + Ft + G

		// Cubing can be a real pain, so the above equations can be rewritten in
		// a
		// "cubeless" form that calculates quickly...

		// x = ((At + B)t + C)t + D
		// y = ((Dt + E)t + F)t + G
		/*
		 * double A = p3.x - (3*t2.x) + (3*t1.x) - p0.x; //A = x3 - 3x2 + 3x1 -
		 * x0 double B = (3*t2.x) - (6*t1.x) + (3*p0.x); //B = 3x2 - 6x1 + 3x0
		 * double C = (3*t1.x) - (3*p0.x); //C = 3x1 - 3x0 double D = p0.x; //D
		 * = x0
		 * 
		 * double E = p3.y - (3*t2.y) + (3*t1.y) - p0.y; //E = y3 - 3y2 + 3y1 -
		 * y0 double F = (3*t2.y) - (6*t1.y) + (3*p0.y); //F = 3y2 - 6y1 + 3y0
		 * double G = (3*t1.y) - (3*p0.y); //G = 3y1 - 3y0 double H = p0.y; //H
		 * = y0
		 * 
		 * coeff0 = A; coeff1 = B; coeff2 = C; coeff3 = D; coeff4 = E; coeff5 =
		 * F; coeff6 = G; coeff7 = H;
		 */
		final Point2D.Double p0 = mStartKnot.getEndPoint();
		final Point2D.Double t1 = mStartKnot.getTangentToNext();
		final Point2D.Double t2 = mEndKnot.getTangentToPrev();
		final Point2D.Double p3 = mEndKnot.getEndPoint();

		coeff0 = p3.x + (3 * (-t2.x + t1.x)) - p0.x; // A = x3 - 3x2 + 3x1 - x0
		coeff1 = 3 * (t2.x - (2 * t1.x) + p0.x); // B = 3x2 - 6x1 + 3x0
		coeff2 = 3 * (t1.x - p0.x); // C = 3x1 - 3x0
		coeff3 = p0.x; // D = x0

		coeff4 = p3.y + (3 * (-t2.y + t1.y)) - p0.y; // E = y3 - 3y2 + 3y1 - y0
		coeff5 = 3 * (t2.y - (2 * t1.y) + p0.y); // F = 3y2 - 6y1 + 3y0
		coeff6 = 3 * (t1.y - p0.y); // G = 3y1 - 3y0
		coeff7 = p0.y; // H = y0

		mCoeffDirty = false;
		/*
		 * if(coeff0 != A) return; if(coeff1 != B) return; if(coeff2 != C)
		 * return; if(coeff3 != D) return; if(coeff4 != E) return; if(coeff5 !=
		 * F) return; if(coeff6 != G) return; if(coeff7 != H) return;
		 */
		// Debug copyCoeff();
	}

	/*
	 * Debug void copyCoeff() { for(int i = 0; i < 8; i++) { coeffCopy[i] =
	 * coeff[i]; } }
	 * 
	 * void compareCoeff() { for(int i = 0; i < 8; i++) { double val2 =
	 * coeffCopy[i]; double val1 = coeff[i];
	 * 
	 * double diff = Math.abs(val1 - val2); if(diff > 0.1) {
	 * System.out.println("Coeff changed i:" + i + " diff:" + diff + " values:"
	 * + val1 + " " + val2); } } }
	 */
	public double getTForX(final double x) {
		double t = (x - getEndKnot().getEndPoint().x)
				/ (getStartKnot().getEndPoint().x - getEndKnot().getEndPoint().x);

		return getTForX(x, t);
	}

	public double getTForX(final double x, final double start_t) {
		calculateCoeff();

		return getTForXInternal(x, start_t);
	}

	private double getTForXInternal(final double x, final double start_t) {
		// I don't know how to find an exact and closed solution to finding y
		// given
		// x. You first have to use x to solve for t and then you solve t for y.

		// One useful way to do this is to take a guess for a t value. See what
		// x
		// you get. Note the error. Reduce the error and try again. Keep this up
		// till you have a root to acceptable accuracy.
		//
		// A good first guess is to normalize x so it ranges from 0 to 1 and
		// then
		// simply guess that x = t. This will be fairly close for curves that
		// aren't
		// bent very much. And a useful guess for ALL spline curves.

		// Guess initial t
		double tn = start_t;

		// Now, on any triangle...
		//
		// rise = run x (rise/run)
		//
		// This gives us a very good improvement for our next approximation. It
		// turns out that the "adjust for slope" method converges very rapidly.
		// Three passes are usually good enough.
		//
		// If our curve has an equation of...
		//
		// x = At^3 + Bt^2 + Ct + D
		//
		// ...its slope will be...
		//
		// x' = 3At^2 +2Bt + C
		//
		// And the dt/dx slope will be its inverse or 1/(3At^2 + 2Bt +C)
		//
		// This is easily calculated. We'll have code and an example in just a
		// bit.
		//
		// The next guess will be...
		//
		// nextguess = currentt + (curentx - x)(currentslope)
		double xn = getXValue(tn);

		double error = x - xn;
		// double lasterror = error;

		int n = 0;
		while (Math.abs(error) > BezierSpline.POS_TOLERANCE
				&& n++ < BezierSpline.POS_MAX_ITERATIONS) {
			double currentSlope = 1 / getXDerivate(tn);

			tn = tn + (error * currentSlope);

			xn = getXValue(tn);

			error = x - xn;
			/*
			 * if(Math.abs(error) > Math.abs(lasterror))
			 * System.out.println("getTForX(): increasing error: " + error +
			 * " last error:" + lasterror + " slope:" + currentSlope + " tn:" +
			 * tn);
			 * 
			 * lasterror = error;
			 */}

		// Sanity check
		if (tn < 0 || tn > 1 || Double.isNaN(tn)
				|| n >= BezierSpline.POS_MAX_ITERATIONS
				|| Math.abs(error) > BezierSpline.POS_TOLERANCE) {
			// System.out.printf("getTForX(): converge failed, error: %f t:%f\n",
			// Math.abs(xn-x), tn);
			tn = getTForX(x, 0, 1, BezierSpline.MIN_MAX_SPLITS);
			xn = getXValue(tn);
			/*
			 * Debug if(Math.abs(xn-x) < POS_TOLERANCE) { st++; } else { tf++;
			 * System
			 * .out.printf("getTForX() failed, error:%f t:%f ct:%f: st:%f tf:%f\n"
			 * , Math.abs(xn-x), tn , ct , st , tf); }
			 */
		}
		/*
		 * Debug else { ct++; }
		 */
		return tn;
	}

	private double getTForX(final double x, double t0, double t1, int nrOfSplits) {
		double best_t = 0;
		double best_error = 1000000000;
		double best_value = 0;

		double current_t;
		double current_value;
		double seg = (t1 - t0) / nrOfSplits;
		double error = 0;
		for (int i = 1; i < nrOfSplits; i++) {
			current_t = seg * i + t0;
			if (current_t < 0 || current_t > 1)
				continue;

			current_value = getXValue(current_t);

			error = Math.abs(x - current_value);

			if (error < best_error) {
				best_error = error;
				best_t = current_t;
				best_value = current_value;
			}

		}
		if (best_error < BezierSpline.POS_TOLERANCE)
			return best_t;
		else if (Math.abs(best_t - (t1 - t0) / 2) < BezierSpline.MIN_MAX_TOLERANCE)
			return best_t;
		else if (nrOfSplits <= 2)
			return best_t;
		else
			return getTForX(x, best_t - seg, best_t + seg, nrOfSplits / 2);
	}

	private double getTForTangent(final double angle, double t0, double t1,
			int nrOfSplits) {
		double best_t = 0;
		double best_error = 1000000000;

		double current_t = 0;
		double current_value = 0;
		double seg = (t1 - t0) / nrOfSplits;
		double error = 0;
		for (int i = 1; i <= nrOfSplits; i++) {
			current_t = seg * i + t0;
			if (current_t < 0 || current_t > 1)
				continue;

			current_value = getTangent(current_t);

			error = Math.abs(angle - current_value);

			if (error < best_error) {
				best_error = error;
				best_t = current_t;
			}

		}
		error = current_t;
		if (best_error < BezierSpline.ANGLE_TOLERANCE) {
			return best_t;
		} else if (Math.abs(best_t - (t1 - t0) / 2) < BezierSpline.ANGLE_T_TOLERANCE) {
			return best_t;
		} else if (nrOfSplits <= 2) {
			return best_t;
		} else
			return getTForTangent(angle, best_t - seg, best_t + seg,
					nrOfSplits / 2);
	}

	double getTForTangent2(double target_angle, double current_t, double last_t) {
		// Search for the angle using secant method
		double current_angle = getTangent(current_t);
		double last_angle = getTangent(last_t);

		double current_error = target_angle - current_angle;

		// System.out.printf("getTForTangent2(): target_angle:%f, current_t:%f, last_t:%f\n",
		// target_angle/BezierBoard.DEG_TO_RAD, current_t, last_t);

		int n = 0;
		while (Math.abs(current_error) > BezierSpline.ANGLE_TOLERANCE
				&& n++ < BezierSpline.ANGLE_MAX_ITERATIONS
				&& current_t > BezierSpline.ZERO
				&& current_t < BezierSpline.ONE) {
			double current_slope = (current_angle - last_angle)
					/ (current_t - last_t);

			last_t = current_t;
			current_t = current_t + (current_error * current_slope);

			last_angle = current_angle;
			current_angle = getTangent(current_t);

			current_error = target_angle - current_angle;

			// System.out.printf("getTForTangent2(): current_slope:%f, current_error:%f, current_t:%f last_angle:%f current_angle:%f target_angle:%f\n",
			// current_slope, current_error/BezierBoard.DEG_TO_RAD, current_t,
			// last_angle/BezierBoard.DEG_TO_RAD,
			// current_angle/BezierBoard.DEG_TO_RAD,
			// target_angle/BezierBoard.DEG_TO_RAD);

		}

		// Sanity check
		if (Math.abs(getTangent(current_t) - target_angle) > BezierSpline.ANGLE_TOLERANCE
				|| (current_t < BezierSpline.ZERO || current_t > BezierSpline.ONE)) {
			// System.out.printf("getTForTangent(): converge failed, error: %f\n",
			// current_error/BezierBoard.DEG_TO_RAD);

			n = 0;
			double lt = 0.0;
			double ht = 1.0;
			while (Math.abs(current_error) > BezierSpline.ANGLE_TOLERANCE
					&& n++ < BezierSpline.ANGLE_MAX_ITERATIONS
					&& ht - lt > 0.00001) {
				current_t = lt + ((ht - lt) / 2.0);

				current_angle = getTangent(current_t);
				current_error = target_angle - current_angle;

				if (current_error < 0.0)
					lt = current_t;
				else
					ht = current_t;

				// System.out.printf("getTForTangent2(): current_error:%f, current_t:%f lt:%f ht:%f current_angle:%f target_angle:%f \n",
				// current_error, current_t, lt, ht, current_angle,
				// target_angle);
			}

		}

		return current_t;
	}

	double getTForLength(double lengthLeft) {
		return getTForLength(BezierSpline.ZERO, BezierSpline.ONE, lengthLeft);
	}

	double getTForLength(double t0, double t1, double lengthLeft) {
		calculateCoeff();

		// Get t split point
		double ts = (t1 - t0) / 2 + t0;
		double sl = getLength(t0, ts);

		try {
			if (Math.abs(t0 - t1) < 0.00001) {
				return t0;
			}
			if (Math.abs(sl - lengthLeft) > BezierSpline.LENGTH_TOLERANCE) {
				if (sl > lengthLeft) {
					return getTForLength(t0, ts, lengthLeft);
				} else {
					return getTForLength(ts, t1, lengthLeft - sl);
				}
			} else {
				return ts;
			}
		} catch (Exception e) {
			System.out.println("Exception in BezierSpline::getTForLength(): "
					+ e.toString());
			return 0.0;
		}
	}

	public double getXValue(final double t) {
		calculateCoeff();

		// compareCoeff();
		// A cubic spline consists of two parametric equations in t (or time)
		// space...

		// x = At^3 + Bt^2 + Ct + D
		// y = Dt^3 + Et^2 + Ft + G

		// Cubing can be a real pain, so the above equations can be rewritten in
		// a
		// "cubeless" form that calculates quickly...

		// x = ((At + B)t + C)t + D
		double value = (((((coeff0 * t) + coeff1) * t) + coeff2) * t) + coeff3;

		// compareCoeff();

		return value;
	}

	public double getYValue(final double t) {
		calculateCoeff();

		// compareCoeff();
		// A cubic spline consists of two parametric equations in t (or time)
		// space...

		// x = At^3 + Bt^2 + Ct + D
		// y = Dt^3 + Et^2 + Ft + G

		// Cubing can be a real pain, so the above equations can be rewritten in
		// a
		// "cubeless" form that calculates quickly...

		// y = ((Dt + E)t + F)t + G
		double value = (((((coeff4 * t) + coeff5) * t) + coeff6) * t) + coeff7;

		// compareCoeff();
		return value;
	}

	public Point2D.Double getValue(final double t) {
		calculateCoeff();

		return new Point2D.Double(getXValue(t), getYValue(t));
	}

	private double getXDerivate(final double t) {
		// compareCoeff();
		// If our curve has an equation of...
		//
		// x = At^3 + Bt^2 + Ct + D
		//
		// ...its slope will be...
		//
		// x' = 3At^2 + 2Bt + C
		// x' = (3At + 2B)t + C
		double value = ((((3 * coeff0) * t) + (2 * coeff1)) * t) + coeff2;
		// compareCoeff();

		return value;
	}

	private double getYDerivate(final double t) {
		// If our curve has an equation of...
		//
		// y = At^3 + Bt^2 + Ct + D
		//
		// ...its slope will be...
		//
		// y' = 3Et^2 +2Ft + G
		// y' = (3Et + 2F)t + G
		double value = ((((3 * coeff4) * t) + (2 * coeff5)) * t) + coeff6;

		return value;
	}

	double getXSecondDerivate(final double t) {
		double value = (6 * coeff0 * t) + (2 * coeff1);

		return value;
	}

	double getYSecondDerivate(final double t) {
		double value = (6 * coeff4 * t) + (2 * coeff5);

		return value;
	}

	double getYForX(final double x) {
		calculateCoeff();

		// I don't know how to find an exact and closed solution to finding y
		// given
		// x. You first have to use x to solve for t and then you solve t for y.

		// One useful way to do this is to take a guess for a t value. See what
		// x
		// you get. Note the error. Reduce the error and try again. Keep this up
		// till you have a root to acceptable accuracy.
		//
		// A good first guess is to normalize x so it ranges from 0 to 1 and
		// then
		// simply guess that x = t. This will be fairly close for curves that
		// aren't
		// bent very much. And a useful guess for ALL spline curves.

		// Guess initial t
		double t = (x - mStartKnot.getEndPoint().x)
				/ (mEndKnot.getEndPoint().x - mStartKnot.getEndPoint().x);

		t = getTForXInternal(x, t);

		double value = getYValue(t);

		/*
		 * double xvalue = getXValue(t);
		 * 
		 * if(Math.abs(xvalue - x) > POS_TOLERANCE) {
		 * System.out.println("find x: " + x + " real x: " + xvalue + " t:" + t
		 * + " p0 x:" + p0.x + " p3 x: " + p3.x); xvalue = getXValue(t); }
		 */
		return value;
	}

	public double getMinX() {
		return getMinMaxNumerical(BezierSpline.X, BezierSpline.MIN);
	}

	public double getMinY() {
		return getMinMaxNumerical(BezierSpline.Y, BezierSpline.MIN);
	}

	public double getMaxX() {
		return getMinMaxNumerical(BezierSpline.X, BezierSpline.MAX);
	}

	public double getMaxY() {
		return getMinMaxNumerical(BezierSpline.Y, BezierSpline.MAX);
	}

	public double getMinMaxNumerical(int XorY, int MinOrMax) {
		calculateCoeff();

		return getMinMaxNumerical(0, 1, BezierSpline.MIN_MAX_SPLITS, XorY,
				MinOrMax);
	}

	private double getMinMaxNumerical(int XorY, int MinOrMax,
			double t0, double t1) {
		calculateCoeff();

		return getMinMaxNumerical(t0, t1, BezierSpline.MIN_MAX_SPLITS, XorY,
				MinOrMax);
	}

	public double getTForMinMaxNumerical(int XorY, int MinOrMax) {
		return getTForMinMaxNumerical(XorY, MinOrMax, 0.0, 1.0);
	}

	private  double getTForMinMaxNumerical(int XorY, int MinOrMax,
			double t0, double t1) {
		calculateCoeff();

		return getTForMinMaxNumerical(t0, t1, BezierSpline.MIN_MAX_SPLITS,
				XorY, MinOrMax);
	}

	public double getMinMaxNumerical(double t0, double t1, int nrOfSplits,
			int XorY, int MinOrMax) {
		double best_t = 0;
		double best_value = (MinOrMax == BezierSpline.MAX) ? -10000000
				: 10000000;

		double current_t;
		double current_value;
		double seg = ((t1 - t0) / nrOfSplits);
		for (int i = 0; i < nrOfSplits; i++) {
			current_t = seg * i + t0;
			if (current_t < 0 || current_t > 1)
				continue;

			current_value = (XorY == BezierSpline.X) ? getXValue(current_t)
					: getYValue(current_t);

			if (MinOrMax == BezierSpline.MAX) {
				if (current_value >= best_value) {
					best_value = current_value;
					best_t = current_t;
				}
			} else {
				if (current_value <= best_value) {
					best_value = current_value;
					best_t = current_t;
				}
			}
		}
		if ((best_t - ((t1 - t0) / 2)) < BezierSpline.MIN_MAX_TOLERANCE)
			return best_value;
		else if (nrOfSplits <= 2)
			return best_value;
		else
			return getMinMaxNumerical(best_t - seg, best_t + seg,
					nrOfSplits / 2, XorY, MinOrMax);
	}

	double getTForMinMaxNumerical(double t0, double t1, int nrOfSplits,
			int XorY, int MinOrMax) {
		double best_t = 0;
		double best_value = (MinOrMax == BezierSpline.MAX) ? -10000000
				: 10000000;

		double current_t;
		double current_value;
		double seg = ((t1 - t0) / nrOfSplits);
		for (int i = 0; i < nrOfSplits; i++) {
			current_t = seg * i + t0;
			if (current_t < 0 || current_t > 1)
				continue;

			current_value = (XorY == BezierSpline.X) ? getXValue(current_t)
					: getYValue(current_t);

			if (MinOrMax == BezierSpline.MAX) {
				if (current_value >= best_value) {
					best_value = current_value;
					best_t = current_t;
				}
			} else {
				if (current_value <= best_value) {
					best_value = current_value;
					best_t = current_t;
				}
			}
		}
		if ((best_t - ((t1 - t0) / 2)) < BezierSpline.MIN_MAX_TOLERANCE)
			return best_t;
		else if (nrOfSplits <= 2)
			return best_t;
		else
			return getTForMinMaxNumerical(best_t - seg, best_t + seg,
					nrOfSplits / 2, XorY, MinOrMax);
	}

	public double getTangent(double t) {

		calculateCoeff();

		double dx = getXDerivate(t);
		double dy = getYDerivate(t);

		double angle = Math.atan2(dx, dy);

		return angle;
	}

	public double getNormal(double t) {
		return getTangent(t) + (Math.PI / 2.0);
	}

	public Point2D.Double getTangentVector(double t) {

		double angle = getTangent(t);

		return new Point2D.Double(Math.cos(angle), Math.sin(angle));
	}

	public Point2D.Double getNormalVector(double t) {
		double angle = getTangent(t) + (Math.PI / 2.0);

		return new Point2D.Double(Math.cos(angle), Math.sin(angle));
	}

	public double getLength() {
		calculateCoeff();

		if (mLengthDirty) {
			mLength = getLength(BezierSpline.ZERO, BezierSpline.ONE);
			mLengthDirty = false;
		}
		return mLength;
	}

	public double getLength(double t0, double t1) {
		calculateCoeff();

		// Get endpoints
		double x0 = getXValue(t0);
		double y0 = getYValue(t0);
		double x1 = getXValue(t1);
		double y1 = getYValue(t1);

		// Get t split point
		double ts = (t1 - t0) / 2 + t0;
		double sx = getXValue(ts);
		double sy = getYValue(ts);

		// Distance between centerpoint and real split curvepoint
		double length = VecMath.getVecLength(x0, y0, sx, sy)
				+ VecMath.getVecLength(sx, sy, x1, y1);
		double chord = VecMath.getVecLength(x0, y0, x1, y1);

		if (length - chord > BezierSpline.LENGTH_TOLERANCE && t1 - t0 > 0.001) {
			return getLength(t0, ts) + getLength(ts, t1);
		} else {
			return length;
		}

	}

	public double getTForDistance(Point2D.Double fromPoint,
			double distance) {
		calculateCoeff();

		return getTForDistance(0, 1, BezierSpline.MIN_MAX_SPLITS, fromPoint,
				distance);
	}

	private double getTForDistance(double t0, double t1, int nrOfSplits,
			Point2D.Double fromPoint, double distance) {
		double best_t = 0;
		double best_error = 100000000;

		double current_t;
		Point2D.Double current_point;
		double current_distance;
		double current_error;
		double seg = ((t1 - t0) / nrOfSplits);
		for (int i = 0; i < nrOfSplits; i++) {
			current_t = seg * i + t0;
			if (current_t < 0 || current_t > 1)
				continue;

			current_point = getValue(current_t);

			current_distance = VecMath.getVecLength(fromPoint, current_point);

			current_error = Math.abs(current_distance - distance);
			if (current_error < best_error) {
				best_error = current_error;
				best_t = current_t;
			}
		}
		if ((best_t - ((t1 - t0) / 2)) < BezierSpline.DISTANCE_TOLERANCE)
			return best_t;
		else if (nrOfSplits <= 2)
			return best_t;
		else
			return getTForDistance(best_t - seg, best_t + seg, nrOfSplits / 2,
					fromPoint, distance);
	}

	public double getClosestT(Point2D.Double point) {
		calculateCoeff();

		return getClosestT(0, 1, 32, point.x, point.y);
	}

	double getClosestT(double t0, double t1, int nrOfSplits, double x, double y) {
		double best_t = 0;
		double min_dist = 1000000000;

		double current_t;
		double current_x;
		double current_y;
		double current_dist;
		double seg = ((t1 - t0) / nrOfSplits);
		for (int i = 0; i < nrOfSplits; i++) {
			current_t = seg * i + t0;
			if (current_t < 0 || current_t > 1)
				continue;

			current_x = getXValue(current_t);
			current_y = getYValue(current_t);

			current_dist = VecMath.getVecLength(current_x, current_y, x, y);

			if (current_dist <= min_dist) {
				min_dist = current_dist;
				best_t = current_t;
			}

		}
		if ((best_t - ((t1 - t0) / 2)) < 0.001)
			return best_t;
		else if (nrOfSplits <= 2)
			return best_t;
		else
			return getClosestT(best_t - seg, best_t + seg, nrOfSplits / 2, x, y);
	}

	public double getCurvatureAt(double pos) {
		calculateCoeff();

		return getCurvature(getTForX(pos));
	}

	public double getCurvature(double t) {
		calculateCoeff();
		// K(t) = (x'y" - y'x") / (x'^2 + y'^2)^(3/2)

		double dx = getXDerivate(t);
		double dy = getYDerivate(t);

		double ddx = getXSecondDerivate(t);
		double ddy = getYSecondDerivate(t);

		return ((dx * ddy) - (dy * ddx)) / Math.pow((dx * dx) + (dy * dy), 1.5);
	}

	BezierKnot getSplitControlPoint(double t) {
		// Split using de Casteljau's algorithm
		Point2D.Double q1 = new Point2D.Double();
		Point2D.Double q2 = new Point2D.Double();
		Point2D.Double q3 = new Point2D.Double();

		VecMath.subVector(getStartKnot().getEndPoint(), getStartKnot()
				.getTangentToNext(), q1);
		VecMath.scaleVector(q1, t);
		VecMath.addVector(getStartKnot().getEndPoint(), q1, q1);

		VecMath.subVector(getStartKnot().getTangentToNext(), getEndKnot()
				.getTangentToPrev(), q2);
		VecMath.scaleVector(q2, t);
		VecMath.addVector(getStartKnot().getTangentToNext(), q2, q2);

		VecMath.subVector(getEndKnot().getTangentToPrev(), getEndKnot()
				.getEndPoint(), q3);
		VecMath.scaleVector(q3, t);
		VecMath.addVector(getEndKnot().getTangentToPrev(), q3, q3);

		Point2D.Double r1 = new Point2D.Double();
		Point2D.Double r2 = new Point2D.Double();
		Point2D.Double r3 = new Point2D.Double();

		VecMath.subVector(q1, q2, r2);
		VecMath.scaleVector(r2, t);
		VecMath.addVector(q1, r2, r2);

		VecMath.subVector(q2, q3, r3);
		VecMath.scaleVector(r3, t);
		VecMath.addVector(q2, r3, r3);

		VecMath.subVector(r2, r3, r1);
		VecMath.scaleVector(r1, t);
		VecMath.addVector(r2, r1, r1);

		BezierKnot ret = new BezierKnot();
		ret.getPoints()[0].setLocation(r1);
		ret.getPoints()[1].setLocation(r2);
		ret.getPoints()[2].setLocation(r3);

		return ret;
	}

	double getTForLength(Point2D.Double p0, Point2D.Double t1,
			Point2D.Double t2, Point2D.Double p3, double length) {
		calculateCoeff();

		return getTForLength(0, 1, length);
	}

	private double getTangent2(double t) {
		BezierKnot cp = getSplitControlPoint(t);

		Point2D.Double u = new Point2D.Double(0, 1);
		Point2D.Double v = new Point2D.Double();
		VecMath.subVector(cp.getEndPoint(), cp.getTangentToNext(), v);
		double angle = VecMath.getVecAngle(u, v);
		return angle;
	}

	public double getTangentAt(double pos) {
		calculateCoeff();

		double t = getTForX(pos);

		return getTangent(t);
	}

	public void onChange(BezierKnot knot) {
		setDirty();
	}

	void setDirty() {
		mCoeffDirty = true;
		mLengthDirty = true;

	}
	
}
