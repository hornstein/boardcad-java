package cadcore;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import boardcad.gui.jdk.BezierBoardDrawUtil;

import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.MathUtils;

public class BezierBoardCrossSection implements Cloneable, Comparable {
	double mPosition;

	private BezierSpline mCrossSectionSpline;
	private ArrayList<Point2D.Double> mCrossSectionGuidePoints;

	public BezierBoardCrossSection() {
		mCrossSectionSpline = new BezierSpline();
		mCrossSectionGuidePoints = new ArrayList<Point2D.Double>();
	}

	public void setPosition(double pos) {
		mPosition = pos;
	}

	public double getPosition() {
		return mPosition;
	}

	public double getDeckAtPos(double pos) {
		double deck = mCrossSectionSpline.getValueAtReverse(Math.abs(pos));

		return deck;
	}

	public double getBottomAtPos(double pos) {
		double bottom = mCrossSectionSpline.getValueAt(Math.abs(pos));

		return bottom;
	}

	public double getThicknessAtPos(double pos) {
		double thickness = getDeckAtPos(pos) - getBottomAtPos(pos);

		return thickness;
	}

	public Point2D.Double getPointAtS(double s) {
		Point2D.Double point = mCrossSectionSpline.getPointByS(s);

		return point;
	}

	public Point2D.Double getNormalAtS(double s) {
		double angle = mCrossSectionSpline.getNormalByS(s);

		return new Point2D.Double(Math.cos(angle), Math.sin(angle));
	}


	public double getCenterThickness() {
		return (mCrossSectionSpline.getControlPoint(
				mCrossSectionSpline.getNrOfControlPoints() - 1).getPoints()[0].y - mCrossSectionSpline
				.getControlPoint(0).getPoints()[0].y);
	}

	public double getWidth() {
		return mCrossSectionSpline.getMaxX() * 2;
	}

	public double getReleaseAngle() {
		Point2D.Double pos = getPointAtS(mCrossSectionSpline
				.getSByNormalReverse(BezierBoardDrawUtil.TUCK_UNDER_DEFINITION_ANGLE
						* MathUtils.DEG_TO_RAD)); // TODO: Bad dependency

		BezierKnot point = mCrossSectionSpline.findBestMatch(pos);
		if (point == null) {
			return 0.0;
		}
		if (point.getEndPoint().distance(pos) > 0.9) {
			return 0.0;
		}

		return point.getAngleBetweenTangents();
	}

	public double getTuckRadius() {
		double sForApex = mCrossSectionSpline
				.getSByNormalReverse(BezierBoardDrawUtil.APEX_DEFINITION_ANGLE
						* MathUtils.DEG_TO_RAD); // TODO: Bad dependency
		double sForTuck = mCrossSectionSpline
				.getSByNormalReverse(BezierBoardDrawUtil.TUCK_UNDER_DEFINITION_ANGLE
						* MathUtils.DEG_TO_RAD); // TODO: Bad dependency

		Point2D.Double apexPos = getPointAtS(sForApex);
		Point2D.Double tuckPos = getPointAtS(sForTuck);

		if ((apexPos.x - tuckPos.x) < 0.18) {
			return 0.0f;
		}

		int steps = 5;
		double step = (sForTuck - sForApex) / steps;
		double curvatureSum = 0.0;
		for (int i = 0; i < steps; i++) {
			double curvatureAtStep = mCrossSectionSpline
					.getCurvatureByS(sForApex + i * step);
			curvatureSum += curvatureAtStep;
		}
		double averageCurvature = curvatureSum / steps;

		double radius = 1.0 / averageCurvature;

		return radius;
	}

	public BezierSpline getBezierSpline() {
		return mCrossSectionSpline;
	}

	public void setBezierSpline(BezierSpline spline) {
		mCrossSectionSpline = spline;
	}

	public ArrayList<Point2D.Double> getGuidePoints() {
		return mCrossSectionGuidePoints;
	}

	public void scale(double newThickness, double newWidth) {
		double oldWidth = getWidth();
		double oldThickness = getCenterThickness();

		if (oldWidth < 0.1)
			oldWidth = 0.1;

		if (oldThickness < 0.1)
			oldThickness = 0.1;

		double newThicknessScale = Math.abs(newThickness / oldThickness);
		double newWidtScale = Math.abs(newWidth / oldWidth);

		if ((oldThickness * newThicknessScale) <= 0.1)
			return;

		if ((oldWidth * newWidtScale) <= 0.1)
			return;

		mCrossSectionSpline.scale(newThicknessScale, newWidtScale);
	}

	public int compareTo(Object other) {
		BezierBoardCrossSection otherCrossSection = (BezierBoardCrossSection) other;

		if (mPosition == otherCrossSection.mPosition)
			return 0;

		return (mPosition < otherCrossSection.mPosition) ? -1 : 1;
	}

	public boolean equals(Object other) {
		BezierBoardCrossSection otherCrossSection = (BezierBoardCrossSection) other;

		if (mPosition != otherCrossSection.mPosition)
			return false;

		if (mCrossSectionSpline.getNrOfControlPoints() != otherCrossSection.mCrossSectionSpline
				.getNrOfControlPoints()) {
			return false;
		}

		for (int i = 0; i < mCrossSectionSpline.getNrOfControlPoints(); i++) {
			if (!mCrossSectionSpline.getControlPoint(i).equals(
					otherCrossSection.mCrossSectionSpline.getControlPoint(i))) {
				return false;
			}
		}

		return true;

	}

	public synchronized BezierBoardCrossSection interpolate(
			BezierBoardCrossSection target, double t) {
		try {
			// boolean hasChanged = false;

			BezierBoardCrossSection interpolationClone = new BezierBoardCrossSection();
			BezierBoardCrossSection interpolationTargetClone = new BezierBoardCrossSection();

			// if(!interpolationClone.equals(this))
			// {
			interpolationClone.set(this);
			// hasChanged = true;
			// }

			// if(!interpolationTargetClone.equals(target))
			// {
			// interpolationTargetClone.set(target);
			//
			// hasChanged = true;
			//
			// }

			BezierBoardCrossSection interpolationSandboxCopy = new BezierBoardCrossSection();
			BezierBoardCrossSection interpolationTargetSandboxCopy = new BezierBoardCrossSection();

			// Create sandboxes to play in
			interpolationSandboxCopy.set(this);
			interpolationTargetSandboxCopy.set(target);

			// Scale to same width and thickness
			interpolationTargetSandboxCopy.scale(getCenterThickness(),
					getWidth());

			if (interpolationSandboxCopy.mCrossSectionSpline.getNrOfControlPoints() != interpolationTargetSandboxCopy.mCrossSectionSpline
					.getNrOfControlPoints()) {

				//
				// To match a ControlPoint there are two cases that should match
				//
				// 1)The ControlPoint has moved
				// The ControlPoint has roughly the same tangent angles and
				// length but are located at a different position
				// Properties to check(With priority)
				// *Sequence of ControlPoints (index in ControlPointlist)
				// *The ControlPoint has same continouancy
				// *The tangent angle is similar
				// *The tangent lengths are similar
				// *Previous/next ControlPoint is similar (What if the
				// previous/next ControlPoint has morphed?)
				//
				// 2)The ControlPoint has morphed
				// The ControlPoint has roughly the same position, but the
				// ControlPoints angle and length has changed
				// Properties to check(With priority)
				// *Sequence of ControlPoints (index in ControlPointlist)
				// *Location is similar
				// *Arc position is similar
				// *Distance to previous/next ControlPoint is similar
				// *Arc Distance to previous/next ControlPoint is similar
				// *Previous/next ControlPoint is similar (What if the
				// previous/next ControlPoint has morphed?)

				BezierSpline mostControlPointsBezier;
				BezierSpline otherBezier;

				if (interpolationSandboxCopy.mCrossSectionSpline.getNrOfControlPoints() > interpolationTargetSandboxCopy.mCrossSectionSpline
						.getNrOfControlPoints()) {
					mostControlPointsBezier = interpolationSandboxCopy.mCrossSectionSpline;
					otherBezier = interpolationTargetSandboxCopy.mCrossSectionSpline;
				} else {
					mostControlPointsBezier = interpolationTargetSandboxCopy.mCrossSectionSpline;
					otherBezier = interpolationSandboxCopy.mCrossSectionSpline;
				}

				double scaleX = otherBezier.getMaxX()
						/ mostControlPointsBezier.getMaxX();
				double scaleY = otherBezier.getMaxY()
						/ mostControlPointsBezier.getMaxY();

				while (mostControlPointsBezier.getNrOfControlPoints() > otherBezier.getNrOfControlPoints()) {
					BezierKnot worstMatchControlPoint = null;
					double worstMatch = 0;
					int worstMatchIndex = -1;

					for (int i = 1; i < mostControlPointsBezier.getNrOfControlPoints() - 1; i++) {
						BezierKnot currentControlPoint = mostControlPointsBezier
								.getControlPoint(i);
						double bestMatch = 10000000;
						for (int j = 1; j < otherBezier.getNrOfControlPoints() - 1; j++) {
							// Scaleing crosssection points by moving so cross
							// sections have same size to better make use of the
							// distance bewtween points
							// Note that the tangents stay the same, scaleing
							// them would change the angle which may introduce
							// poor matching
							BezierKnot otherControlPoint = (BezierKnot) otherBezier
									.getControlPoint(j).clone();
							otherControlPoint.setControlPointLocation(
									otherControlPoint.getEndPoint().x * scaleX,
									otherControlPoint.getEndPoint().y * scaleY);

							double currentMatch = currentControlPoint
									.compareTo(otherControlPoint);
							if (currentMatch < bestMatch) {
								bestMatch = currentMatch;
							}
						}
						// DEBUG System.out.println("index: " + i + " match: " +
						// bestMatch);
						if (bestMatch > worstMatch) {
							worstMatchControlPoint = currentControlPoint;
							worstMatch = bestMatch;
							worstMatchIndex = i;
						}
					}

					// DEBUG System.out.println("Worst match index: " +
					// worstMatchIndex);

					BezierKnot newControlPoint = new BezierKnot();
					int index = otherBezier.getSplitControlPoint(
							worstMatchControlPoint.getPoints()[0],
							newControlPoint);

					// DEBUG System.out.println("Split ControlPoint: " + index);
					if (index > 0) {

						otherBezier.insert(index, newControlPoint);

						Point2D.Double tmp = new Point2D.Double();

						BezierKnot prev = otherBezier
								.getControlPoint(index - 1);
						BezierKnot next = otherBezier
								.getControlPoint(index + 1);
						
						BezierCurve tmpCurve = new BezierCurve(prev, next);

						double ct = tmpCurve.getClosestT(worstMatchControlPoint.getPoints()[0]);

						VecMath.subVector(prev.getPoints()[0],
								prev.getPoints()[2], tmp);
						VecMath.scaleVector(tmp, ct);
						VecMath.addVector(prev.getPoints()[0], tmp,
								prev.getPoints()[2]);

						VecMath.subVector(next.getPoints()[1],
								next.getPoints()[0], tmp);
						VecMath.scaleVector(tmp, ct - 1);
						VecMath.addVector(next.getPoints()[0], tmp,
								next.getPoints()[1]);

					} else {
						return this;
					}
				}
			}

			BezierBoardCrossSection interpolated = new BezierBoardCrossSection();
			interpolated.set(interpolationTargetSandboxCopy);

			// }

			for (int i = 0; i < interpolationTargetSandboxCopy.mCrossSectionSpline
					.getNrOfControlPoints(); i++) {
				BezierKnot a = interpolationSandboxCopy.mCrossSectionSpline
						.getControlPoint(i);
				BezierKnot b = interpolationTargetSandboxCopy.mCrossSectionSpline
						.getControlPoint(i);
				BezierKnot v = interpolated.mCrossSectionSpline
						.getControlPoint(i);

				// Calculate vectors between ControlPoints
				for (int j = 0; j < 3; j++) {
					VecMath.subVector(a.getPoints()[j], b.getPoints()[j],
							v.getPoints()[j]);
					VecMath.scaleVector(v.getPoints()[j], t);
					VecMath.addVector(a.getPoints()[j], v.getPoints()[j],
							v.getPoints()[j]);
				}

			}

			return interpolated;
		} catch (Exception e) {
			System.out.println("Error occured in Brd::interpolate() "
					+ e.toString());
			return null;
		}
	}

	public void set(BezierBoardCrossSection crossSection) {
		// Remove extra control points
		while (mCrossSectionSpline.getNrOfControlPoints() > crossSection.mCrossSectionSpline
				.getNrOfControlPoints()) {
			mCrossSectionSpline.remove(0);
		}
		// Add extra control points
		while (mCrossSectionSpline.getNrOfControlPoints() < crossSection.mCrossSectionSpline
				.getNrOfControlPoints()) {
			mCrossSectionSpline.append(new BezierKnot());
		}

		int nrControlPoints = mCrossSectionSpline.getNrOfControlPoints();
		for (int i = 0; i < nrControlPoints; i++) {
			mCrossSectionSpline.getControlPoint(i).set(
					crossSection.mCrossSectionSpline.getControlPoint(i));
		}

	}

	public Object clone() {
		BezierBoardCrossSection crossSection = null;
		try {
			crossSection = (BezierBoardCrossSection) super.clone();
		} catch (CloneNotSupportedException e) {
			System.out
					.println("Exception in BezierBoardCrossSection::clone(): "
							+ e.toString());
			throw new Error("CloneNotSupportedException in CrossSection");
		}

		crossSection.mCrossSectionSpline = (BezierSpline) this.mCrossSectionSpline
				.clone();

		crossSection.mCrossSectionGuidePoints = new ArrayList<Point2D.Double>();
		for (int i = 0; i < this.mCrossSectionGuidePoints.size(); i++) {
			crossSection.mCrossSectionGuidePoints
					.add((Point2D.Double) this.mCrossSectionGuidePoints.get(i)
							.clone());
		}

		return crossSection;
	}

}
