package boardcad.export;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Vector;

import cadcore.BezierCurve;
import cadcore.VecMath;
import boardcad.AbstractDraw;
import boardcad.FileTools;
import boardcam.writers.GCodeWriter;

public class GCodeDraw extends AbstractDraw {
	PrintStream mStream = null;
	AffineTransform mTransform = new AffineTransform();

	Vector<Point2D.Double> mVerticies = new Vector<Point2D.Double>();

	GCodeWriter mGCodeWriter = new GCodeWriter();

	double mToolDiameter;
	double mCuttingDepth;
	double mCuttingSpeed;
	double mJogHeight;
	double mJogSpeed;
	double mSinkSpeed;

	boolean mFlipNormal = false;

	public GCodeDraw(String filename, double toolDiameter, double cuttingDepth,
			double cuttingSpeed, double jogHeight, double jogSpeed,
			double sinkSpeed) {
		mTransform.setToIdentity();

		mToolDiameter = toolDiameter;
		mCuttingDepth = cuttingDepth;
		mCuttingSpeed = cuttingSpeed;
		mJogHeight = jogHeight;
		mJogSpeed = jogSpeed;
		mSinkSpeed = sinkSpeed;

		File file = new File(FileTools.setExtension(filename, "nc"));

		try {
			mStream = new PrintStream(file);
		} catch (Exception e) {
			return;
		}

		mGCodeWriter.writeComment(mStream, "G-Code Draw");

	}
	
	public void writeComment(String comment)
	{
		mGCodeWriter.writeComment(mStream, comment);
	}

	@Override
	public void close() {
		mGCodeWriter.writeEnd(mStream);
		mStream.close();
	}

	public void setFlipNormal(boolean flip) {
		mFlipNormal = flip;
	}
	
	public void setCutterDiameter(double diam) {
		mToolDiameter = diam;
	}

	@Override
	public void setColor(Color color) {
		// Ignore color

	}

	@Override
	public void setStroke(Stroke stroke) {
		// Ignore stroke

	}

	@Override
	public void transform(AffineTransform transform) {
		mTransform.preConcatenate(transform);
	}

	@Override
	public void setTransform(AffineTransform transform) {
		mTransform = transform;
	}

	@Override
	public AffineTransform getTransform() {
		return mTransform;
	}

	@Override
	public void fill(Ellipse2D line) {
		// Ignored
		System.out.printf("Path fill not supported by GCodeDraw");

	}

	@Override
	public void fill(GeneralPath path) {
		// Ignored
		System.out.printf("Path fill not supported by GCodeDraw");
	}

	public void draw(GeneralPath path) {
		Point2D.Double prevPoint = null;
		Point2D.Double lastPoint = null;
		Point2D.Double moveToPoint = null;
		Point2D.Double afterMovePoint = null;
		boolean isLastMoveTo = false;

		// System.out.printf("GCodeDraw draw Path\n");

		double[] coords = new double[8];
		int segmentType;

		for (PathIterator it = path.getPathIterator(getTransform()); it
				.isDone() == false; it.next()) {
			segmentType = it.currentSegment(coords);

			switch (segmentType) {

			// The segment type constant for a point that specifies the starting
			// location for a new subpath.
			case PathIterator.SEG_MOVETO: {
				// System.out.printf("Path SEG_MOVETO\n");

				lastPoint = new Point2D.Double(coords[0], coords[1]);
				moveToPoint = lastPoint;
				isLastMoveTo = true;

				// Assuming there is only one close in path
				prevPoint = getLastPointBeforeClose(path);

			}
				break;

			// The segment type constant for a point that specifies the end
			// point of a line to be drawn from the most recently specified
			// point.
			// NOTE: This will only work if the angle between lines is 90
			// degrees or more. Narrower angles will result in overshoot into
			// the next line segment
			case PathIterator.SEG_LINETO: {
				// System.out.printf("Path SEG_LINETO\n");

				Point2D.Double currentPoint = new Point2D.Double(coords[0],
						coords[1]);
				if (lastPoint.equals(currentPoint)) {
					continue;
				}

				// Assuming there is only one close in path
				Point2D.Double nextPoint = getNextPoint(path, it);
				if (currentPoint.equals(nextPoint)) {
					continue;
				}

				if (isLastMoveTo == true) // First cut after move
				{
					Point2D.Double startOffset = getCornerOffset(prevPoint,
							lastPoint, currentPoint, mToolDiameter, mFlipNormal);
					if (Double.isInfinite(startOffset.x)
							|| Double.isInfinite(startOffset.y))
						continue;
					if (Double.isNaN(startOffset.x) || Double.isNaN(startOffset.y))
						continue;

					mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog
																	// speed
					mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Jog
																		// height

					mGCodeWriter.writeCoordinate(mStream, lastPoint.getX()
							+ startOffset.x, lastPoint.getY() + startOffset.y,
							mJogHeight); // Move to
					mGCodeWriter.writeSpeed(mStream, mSinkSpeed); // Set sink
																	// speed
					mGCodeWriter.writeCoordinate(mStream, lastPoint.getX()
							+ startOffset.x, lastPoint.getY() + startOffset.y,
							mCuttingDepth); // Sink

					afterMovePoint = currentPoint;
				}

				Point2D.Double currentOffset = getCornerOffset(lastPoint,
						currentPoint, nextPoint, mToolDiameter, mFlipNormal);
				if (Double.isInfinite(currentOffset.x)
						|| Double.isInfinite(currentOffset.y))
					continue;
				if (Double.isNaN(currentOffset.x) || Double.isNaN(currentOffset.y))
					continue;
				
				Point2D.Double currentPointWithOffset = new Point2D.Double();
				VecMath.addVector(currentPoint, currentOffset, currentPointWithOffset);

				System.out.printf("pos x:%f y:%f final x:%f y:%f  Offset x:%f y:%f\n", currentPoint.x, currentPoint.y, currentPointWithOffset.x, currentPointWithOffset.y, currentOffset.x, currentOffset.y);

				mGCodeWriter.writeSpeed(mStream, mCuttingSpeed); // Set cut
																	// speed
				mGCodeWriter.writeCoordinate(mStream, currentPointWithOffset.x, currentPointWithOffset.y, mCuttingDepth); // Cut

				prevPoint = lastPoint;
				lastPoint = currentPoint;

				isLastMoveTo = false;
			}
				break;

			// The segment type constant for the set of 3 points that specify a
			// cubic parametric curve to be drawn from the most recently
			// specified point.
			case PathIterator.SEG_CUBICTO: {
				// System.out.printf("Path SEG_CUBICTO\n");

				CubicCurve2D curve = new CubicCurve2D.Double(lastPoint.getX(),
						lastPoint.getY(), coords[0], coords[1], coords[2],
						coords[3], coords[4], coords[5]);
				BezierCurve bezier = new BezierCurve(curve.getX1(),
						curve.getY1(), curve.getCtrlX1(), curve.getCtrlY1(),
						curve.getCtrlX2(), curve.getCtrlY2(), curve.getX2(),
						curve.getY2());

				Point2D.Double startNormal = getCubicCurveStartNormal(curve);

				Point2D.Double startPos = bezier.getValue(0.0);

				if (isLastMoveTo == true) // First cut after move
				{
					Point2D.Double startOffset = getCornerOffset(prevPoint,
							lastPoint,
							new Point2D.Double(coords[0], coords[1]),
							mToolDiameter, mFlipNormal);

					mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog
																	// speed
					mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Lift
																		// to
																		// Jog
																		// height

					mGCodeWriter.writeCoordinate(mStream, startPos.getX()
							+ startOffset.x, startPos.getY() + startOffset.y,
							mJogHeight); // Move to
					mGCodeWriter.writeSpeed(mStream, mSinkSpeed); // Set sink
																	// speed
					mGCodeWriter.writeCoordinate(mStream, startPos.getX()
							+ startOffset.x, startPos.getY() + startOffset.y,
							mCuttingDepth); // Sink

					mGCodeWriter.writeSpeed(mStream, mCuttingSpeed); // Set cut
																		// speed

					afterMovePoint = new Point2D.Double(coords[0], coords[1]);
				}

				double bezierLength = bezier.getLength() * 10.0; // convert to
																	// mm

				int steps = (int) bezierLength;

				for (int i = 0; i < steps; i++) {
					double t = (double) i / (double) steps;
					Point2D.Double pos = bezier.getValue(t);

					Point2D.Double perpendicularVec = bezier.getNormalVector(t);
					
					VecMath.scaleVector(perpendicularVec, mToolDiameter / 2.0);

					if (mFlipNormal) {
						VecMath.scaleVector(perpendicularVec, -1.0);
					}

					mGCodeWriter.writeCoordinate(mStream, pos.getX()
							+ perpendicularVec.x, pos.getY()
							+ perpendicularVec.y, mCuttingDepth); // Sink

				}

				prevPoint = new Point2D.Double(coords[2], coords[3]);
				lastPoint = new Point2D.Double(coords[4], coords[5]);

				isLastMoveTo = false;
			}
				break;

			// The segment type constant for the pair of points that specify a
			// quadratic parametric curve to be drawn from the most recently
			// specified point. }
			case PathIterator.SEG_QUADTO: {
				// Unsupported
				System.out.printf("Quad not supported by GCodeDraw\n");
			}
				break;

			// The segment type constant that specifies that the preceding
			// subpath should be closed by appending a line segment back to the
			// point corresponding to the most recent SEG_MOVETO.
			case PathIterator.SEG_CLOSE: {
				// System.out.printf("Path SEG_CLOSE\n");

				// Assuming there is only one move in the path

				Point2D.Double offset = getCornerOffset(lastPoint, moveToPoint,
						afterMovePoint, mToolDiameter, mFlipNormal);

				mGCodeWriter.writeCoordinate(mStream, moveToPoint.getX()
						+ offset.x, moveToPoint.getY() + offset.y,
						mCuttingDepth); // Cut
			}
				break;
			}

		}

		// System.out.printf("GCodeDraw draw Path Ended\n");
	}

	@Override
	public void draw(Line2D line) {
		System.out.printf("GCodeDraw Draw Line\n");

		Point2D startPoint = line.getP1();
		Point2D endPoint = line.getP2();

		// Calculate normal vec to line
		Point2D.Double perpendicularVec = getLineNormal(line);
		VecMath.scaleVector(perpendicularVec, mToolDiameter / 2.0);
		if (mFlipNormal) {
			VecMath.scaleVector(perpendicularVec, -1.0);
		}

		System.out.printf("Line start: %f, %f\n", startPoint.getX(),
				startPoint.getY());
		System.out.printf("Line end: %f, %f\n", endPoint.getX(),
				endPoint.getY());

		System.out.printf("Offset: %f, %f\n", perpendicularVec.getX(),
				perpendicularVec.getY());

		mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog speed
		mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Lift tool
															// straight up for
															// jog

		mGCodeWriter.writeCoordinate(mStream, startPoint.getX()
				+ perpendicularVec.x, startPoint.getY() + perpendicularVec.y,
				mJogHeight); // Move to
		mGCodeWriter.writeSpeed(mStream, mSinkSpeed); // Set sink speed
		mGCodeWriter.writeCoordinate(mStream, startPoint.getX()
				+ perpendicularVec.x, startPoint.getY() + perpendicularVec.y,
				mCuttingDepth); // Sink
		mGCodeWriter.writeSpeed(mStream, mCuttingSpeed); // Set cut speed
		mGCodeWriter.writeCoordinate(mStream, endPoint.getX()
				+ perpendicularVec.x, endPoint.getY() + perpendicularVec.y,
				mCuttingDepth); // Cut

		mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog speed
		mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Lift tool for jog
	}

	@Override
	public void draw(Ellipse2D elipse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(CubicCurve2D curve) {

		System.out.printf("GCodeDraw Draw Cubic Curve\n");

		BezierCurve bezier = new BezierCurve(curve.getX1(), curve.getY1(),
				curve.getCtrlX1(), curve.getCtrlY1(), curve.getCtrlX2(),
				curve.getCtrlY2(), curve.getX2(), curve.getY2());

		double bezierLength = bezier.getLength() * 10.0; // convert to mm

		int steps = (int) bezierLength;

		for (int i = 0; i < steps; i++) {
			double t = (double) i / (double) steps;
			Point2D.Double pos = bezier.getValue(t);
			Point2D.Double perpendicularVec = bezier.getNormalVector(t);
			VecMath.scaleVector(perpendicularVec, mToolDiameter / 2.0);

			if (mFlipNormal) {
				VecMath.scaleVector(perpendicularVec, -1.0);
			}

			if (i == 0) {
				mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog speed
				mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Lift tool
																	// straight
																	// up for
																	// jog

				mGCodeWriter.writeCoordinate(mStream, pos.getX()
						+ perpendicularVec.x, pos.getY() + perpendicularVec.y,
						mJogHeight); // Move to
				mGCodeWriter.writeSpeed(mStream, mSinkSpeed); // Set sink speed
			} else {
				mGCodeWriter.writeSpeed(mStream, mCuttingSpeed); // Set cut
																	// speed
			}

			mGCodeWriter.writeCoordinate(mStream, pos.getX()
					+ perpendicularVec.x, pos.getY() + perpendicularVec.y,
					mCuttingDepth); // Sink

		}

		mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog speed
		mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Lift tool for jog

	}

	@Override
	public void moveTo(Point2D point) {
		System.out.printf("GCodeDraw moveTO\n");

		mGCodeWriter.writeSpeed(mStream, mJogSpeed); // Set jog speed
		mGCodeWriter.writeZCoordinate(mStream, mJogHeight); // Lift tool
															// straight up for
															// jog

		mGCodeWriter.writeCoordinate(mStream, point.getX(), point.getY(),
				mJogHeight); // Move to
	}

	Point2D.Double getLineNormal(Line2D line) {
		Point2D startPoint = line.getP1();
		Point2D endPoint = line.getP2();

		// Calculate normal vec to line
		Point2D.Double perpendicularVec = new Point2D.Double(startPoint.getY()
				- endPoint.getY(), endPoint.getX() - startPoint.getX()); // (-y,x)
																			// is
																			// perpendicular
		VecMath.normalizeVector(perpendicularVec);

		return perpendicularVec;
	}

	Point2D.Double getCubicCurveStartNormal(CubicCurve2D curve) {
		Point2D startPoint = curve.getP1();
		Point2D endPoint = curve.getCtrlP1();

		// Calculate normal vec to line
		Point2D.Double perpendicularVec = new Point2D.Double(startPoint.getY()
				- endPoint.getY(), endPoint.getX() - startPoint.getX()); // (-y,x)
																			// is
																			// perpendicular
		VecMath.normalizeVector(perpendicularVec);

		return perpendicularVec;
	}

	Point2D.Double getCubicCurveEndNormal(CubicCurve2D curve) {
		Point2D startPoint = curve.getCtrlP2();
		Point2D endPoint = curve.getP2();

		// Calculate normal vec to line
		Point2D.Double perpendicularVec = new Point2D.Double(startPoint.getY()
				- endPoint.getY(), endPoint.getX() - startPoint.getX()); // (-y,x)
																			// is
																			// perpendicular
		VecMath.normalizeVector(perpendicularVec);

		return perpendicularVec;
	}

	Point2D.Double getLastPointBeforeClose(GeneralPath path) {
		Point2D.Double last = null;

		for (PathIterator it = path.getPathIterator(getTransform()); it
				.isDone() == false; it.next()) {

			double[] coords = new double[8];
			int segmentType = it.currentSegment(coords);

			switch (segmentType) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO: {
				last = new Point2D.Double(coords[0], coords[1]);
			}
				break;

			case PathIterator.SEG_CUBICTO: {
				last = new Point2D.Double(coords[4], coords[5]);
			}
				break;

			case PathIterator.SEG_CLOSE:
				return last;
			}
		}
		return null;
	}

	Point2D.Double getNextPoint(GeneralPath path, PathIterator it) {
		double[] coords = new double[8];
		int segmentType = it.currentSegment(coords);

		Point2D.Double lastMove = null;

		for (PathIterator tmp = path.getPathIterator(getTransform()); tmp
				.isDone() == false; tmp.next()) {
			double[] tmpCoords = new double[8];
			int tmpSegmentType = tmp.currentSegment(tmpCoords);
			{
				if (tmpSegmentType == PathIterator.SEG_MOVETO) {
					lastMove = new Point2D.Double(tmpCoords[0], tmpCoords[1]);
				}

				if (tmpSegmentType == segmentType
						&& Arrays.equals(coords, tmpCoords)) {
					tmp.next();
					tmpSegmentType = tmp.currentSegment(coords);

					switch (tmpSegmentType) {
					case PathIterator.SEG_MOVETO:
					case PathIterator.SEG_LINETO:
					case PathIterator.SEG_CUBICTO: {
						Point2D.Double next = new Point2D.Double(coords[0],
								coords[1]);
						return next;
					}

					case PathIterator.SEG_CLOSE:
						return lastMove;
					}
				}
			}
		}
		return null;
	}

	Point2D.Double getCornerOffset(Point2D.Double start, Point2D.Double mid,
			Point2D.Double end, double toolDiam, boolean flip) {
		if (start == null) {
			// Start of sequence, no previous point
			Point2D.Double normal = new Point2D.Double(end.x -mid.x, mid.y - end.y);
			VecMath.normalizeVector(normal);
			return normal;
		}

		//Point2D.Double a = new Point2D.Double(mid.x - start.x, mid.y - start.y);
		//Point2D.Double b = new Point2D.Double(mid.x - end.x, mid.y - end.y);
		
		Point2D.Double an = new Point2D.Double(start.y - mid.y, mid.x - start.x);
		Point2D.Double bn = new Point2D.Double(mid.y - end.y, end.x - mid.x );

		Point2D.Double offsetVector = new Point2D.Double();
		//VecMath.addVector(a, b, offset);
		VecMath.addVector(an, bn, offsetVector);
		if (offsetVector.x == 0.0 && offsetVector.y == 0.0)
			return offsetVector;

		VecMath.normalizeVector(offsetVector);
		if (flip) {
			VecMath.scaleVector(offsetVector, -1);
		}

		//double angle = VecMath.getVecAngle(a, b);
		//double offsetLength = toolDiam / Math.sin(angle / 2.0);
		double angle = VecMath.getVecAngle(an, bn);
		double offsetLength = toolDiam / Math.cos(angle / 2.0);
		System.out.printf("angle:%f offsetLength:%f\n", angle/Math.PI*180.0, offsetLength);

		VecMath.scaleVector(offsetVector, offsetLength);
		//System.out.printf("offsetVector %f,%f\n", offsetVector.x,offsetVector.y);

		return offsetVector;
	}

	private boolean isPathClosed(GeneralPath path) {

		double[] coords = new double[8];

		for (PathIterator it = path.getPathIterator(getTransform()); it
				.isDone() == false; it.next()) {
			int segmentType = it.currentSegment(coords);

			if (segmentType == PathIterator.SEG_CLOSE) {
				return true;
			}

		}
		return false;
	}

	private boolean isPathClosed(PathIterator it) {
		double[] coords = new double[8];

		for (; it.isDone() == false; it.next()) {
			int segmentType = it.currentSegment(coords);

			if (segmentType == PathIterator.SEG_MOVETO) {
				return false;
			}

			if (segmentType == PathIterator.SEG_CLOSE) {
				return true;
			}

		}
		return false;
	}

	private Point2D.Double getLastPointBeforeClose(PathIterator it) {
		double[] coords = new double[8];

		Point2D.Double lastPoint = new Point2D.Double();

		for (; it.isDone() == false; it.next()) {
			int segmentType = it.currentSegment(coords);

			switch (segmentType) {
			case PathIterator.SEG_LINETO: {
				lastPoint.x = coords[0];
				lastPoint.y = coords[1];
				break;
			}
			case PathIterator.SEG_CUBICTO: {
				lastPoint.x = coords[4];
				lastPoint.y = coords[5];
				break;
			}
			case PathIterator.SEG_CLOSE: {
				return lastPoint;
			}
			default: {
				break; // Not used
			}
			}
		}
		return null;
	}

	private boolean isNextClose(PathIterator it) {
		it.next();

		if (it.isDone()) {
			return false;
		}

		double[] coords = new double[8];

		int segmentType = it.currentSegment(coords);

		switch (segmentType) {
		case PathIterator.SEG_CLOSE: {
			return true;
		}
		default: {
			return false;
		}
		}
	}

	private Point2D.Double getNextPoint(PathIterator it) {
		it.next();

		if (it.isDone()) {
			return null;
		}

		double[] coords = new double[8];

		Point2D.Double nextPoint = new Point2D.Double();

		int segmentType = it.currentSegment(coords);

		switch (segmentType) {

		case PathIterator.SEG_LINETO: {
			nextPoint.x = coords[0];
			nextPoint.y = coords[1];
			break;
		}
		case PathIterator.SEG_CUBICTO: {
			nextPoint.x = coords[4];
			nextPoint.y = coords[5];
			break;
		}
		case PathIterator.SEG_CLOSE: {
			// //Iterate backwards to find last move to
			// for(; it.isDone() == false; it.previous())
			// {
			//
			// }

			return null;
		}
		default: {
			return null;
		}
		}
		return nextPoint;
	}
}
