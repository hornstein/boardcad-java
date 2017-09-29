package boardcam.toolpathgenerators;

import java.awt.Component;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.ProgressMonitor; //TODO: Bad dependency
import javax.swing.SwingWorker;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cadcore.UnitUtils;

import board.AbstractBoard;
import boardcam.cutters.AbstractCutter;
import boardcam.holdingsystems.AbstractBlankHoldingSystem;
import boardcam.writers.AbstractMachineWriter;
import boardcad.gui.jdk.Machine3DView; //TODO: Bad dependency
import boardcad.i18n.LanguageResource;

public abstract class AbstractToolpathGenerator {
	protected AbstractCutter mCurrentCutter;
	protected Point3d mCutterOffset = new Point3d(0.0, 0.0, 0.0);
	protected AbstractBlankHoldingSystem mCurrentBlankHoldingSystem;
	protected AbstractMachineWriter mCurrentWriter;
	protected AbstractBoard mBoard;
	protected AbstractBoard mBlank;

	protected Matrix4d mRotationOffsetMatrix;
	protected Matrix4d mOffsetMatrix;
	protected Matrix4d mInverseOffsetMatrix;
	protected Matrix4d mInverseRotationOffsetMatrix;

	protected String mFilename;

	protected ProgressMonitor mProgress;

	protected Point3d mLastSurfacePoint = null;
	protected Point3d mLastToolpathPoint = null;

	protected int mCurrentSpeed = 0;

	protected boolean mCancelled = false;

	PrintStream mStream = null;
	File mFile = null;

	AbstractToolpathGenerator(AbstractMachineWriter writer,
			Component parentComponent) {
		setWriter(writer);

		mProgress = new ProgressMonitor(parentComponent,
				LanguageResource.getString("GENERATINGTOOLPATH_STR"), "", 0,
				100);
	}

	void setCutter(AbstractCutter cutter) {
		mCurrentCutter = cutter;
	}

	void setCutterOffset(Point3d cutterOffset) {
		mCutterOffset = new Point3d(cutterOffset);
		mCutterOffset.scale(UnitUtils.MILLIMETER_PR_CENTIMETER);
	}

	AbstractCutter getCutter() {
		return mCurrentCutter;
	}

	void setBlankHoldingSystem(AbstractBlankHoldingSystem holdingSystem) {
		mCurrentBlankHoldingSystem = holdingSystem;
	}

	AbstractBlankHoldingSystem getBlankHoldingSystem() {
		return mCurrentBlankHoldingSystem;
	}

	void setWriter(AbstractMachineWriter writer) {
		mCurrentWriter = writer;
	}

	void setOffsetAndRotation(Vector3d offset, double rotation, double scale) {
		Matrix4d translationOffsetMatrix = new Matrix4d();
		translationOffsetMatrix.setIdentity();
		translationOffsetMatrix.setTranslation(offset);

		mRotationOffsetMatrix = new Matrix4d();
		mRotationOffsetMatrix.setIdentity();
		mRotationOffsetMatrix.rotY(rotation);

		Matrix4d scaleMatrix = new Matrix4d();
		scaleMatrix.setIdentity();
		scaleMatrix.setScale(scale);

		mOffsetMatrix = new Matrix4d();
		mOffsetMatrix.setIdentity();

		mOffsetMatrix.mul(scaleMatrix);
		mOffsetMatrix.mul(translationOffsetMatrix);
		mOffsetMatrix.mul(mRotationOffsetMatrix);

		mInverseOffsetMatrix = new Matrix4d(mOffsetMatrix);
		mInverseOffsetMatrix.invert();

		mInverseRotationOffsetMatrix = new Matrix4d(mRotationOffsetMatrix);
		mInverseRotationOffsetMatrix.invert();
	}

	protected void init() {
		try {
			Locale.setDefault(Locale.US);

			mFile = new File(mFilename);

			mStream = new PrintStream(mFile);

			mLastSurfacePoint = null;
			mLastToolpathPoint = null;
			mCurrentSpeed = 0;
		} catch (Exception e) {
			System.out
					.println("Exception in AbstractToolpathGenerator::init(): "
							+ e.toString());
		}
	}

	protected SwingWorker<Void, Void> getNewWorker() {
		return new SwingWorker<Void, Void>() {

			protected Void doInBackground() throws Exception {
				reset3DView();

				mProgress.setProgress(0);

				init();
				writeToolpath();

				mProgress.close();

				return null;
			}
		};

	}

	public void writeToolpath(String filename, AbstractBoard board,
			AbstractBoard blank) {
		mFilename = filename;
		mBoard = board;
		mBlank = blank;

		SwingWorker<Void, Void> worker = getNewWorker();
		worker.execute();
	}

	protected void writeToolpath() {

		// system.out.printf("\n\nwriteToolpath() Starting...\n");
		//
		// try {
		writeToolpathBegin();

		Point3d retCoord;
		Vector3d retVector;

		do {
			if (mProgress.isCanceled() || mCancelled) {
				mStream.close();
				mFile.delete();
				return;
			}

			retCoord = getToolpathCoordinate();
			if (retCoord == null)
				break;

			retVector = getToolpathNormalVector();

			double newSpeed = calcSpeed(retCoord, retVector, mBoard,
					isAtStringer());
			updateSpeed((int) newSpeed);

			processCoordinate(retCoord, retVector);

			next();

		} while (true);

		writeToolpathEnd();

		mStream.close();

		// } catch (Exception e) {
		// System.out
		// .println("Exception in AbstractToolpathGenerator::writeToolpath(): "
		// + e.toString());
		// }

		// System.out.printf("writeToolpath() Ended...\n\n\n");
	}

	public void updateSpeed(int newSpeed) {
		if ((int) newSpeed != (int) mCurrentSpeed) // The speed is output as an
													// integer, we don't want to
		// write several lines with the same speed
		{
			mCurrentSpeed = newSpeed;
			mCurrentWriter.writeSpeed(mStream, (int) mCurrentSpeed);
		}
	}

	public void processCoordinate(Point3d coordinate, Vector3d normal) //throws Throwable TODO: fixme
	{
		double[] finalCoord;

		// DEBUG
		//System.out.printf("coordinate: %f,%f,%f  normal: %f,%f,%f\n",coordinate.x,coordinate.y,coordinate.z,normal.x,normal.y,normal.z);
	
		Point3d transformedCoord = transformPoint(coordinate);
		Vector3d transformedVector = transformVector(normal);
	
		// DEBUG
		// DEBUG
		// //system.out.printf("transformedCoord: %f,%f,%f  transformedVector: %f,%f,%f\n",
		// transformedCoord.x,transformedCoord.y,transformedCoord.z,
		// transformedVector.x,transformedVector.y,transformedVector.z);
	
		finalCoord = mCurrentCutter.calcOffset(transformedCoord,
				transformedVector, mBoard);
		
		Point3d finalCoordRaw = new Point3d(finalCoord[0], finalCoord[1],
				finalCoord[2]);
	
		Point3d finalCoordTransformed = new Point3d(finalCoord[0], finalCoord[1],
				finalCoord[2]);
		
		finalCoordTransformed.sub(mCutterOffset);
	
		// DEBUG
		//System.out.printf("finalCoord: %f,%f,%f\n", finalCoordTransformed.x,finalCoordTransformed.y,finalCoordTransformed.z);
	
		if (mLastSurfacePoint == null) 
		{
			setSurfaceStart(transformedCoord);
		} 
		else 
		{
			addSurfaceLine(transformedCoord);
		}
		mLastSurfacePoint = transformedCoord;
	
		addNormal(mLastSurfacePoint, transformedVector);
	
		//Check collision between board and cutter and handle it
		if (checkCollision(finalCoordRaw, mBoard) == true) 
		{
			handleCollision(finalCoordRaw, mCurrentCutter, mBoard);
		}
		//Check collision between cutter and blank holding system, and handle it
		else if (mCurrentBlankHoldingSystem != null && mCurrentBlankHoldingSystem.checkCollision(finalCoordRaw, mCurrentCutter) == true) 
		{
			ArrayList<double[]> collisionHandlingToolpath = mCurrentBlankHoldingSystem.handleCollision(finalCoordRaw, mCurrentCutter, mBoard);
			if(collisionHandlingToolpath == null)
			{
				//throw new Throwable(""); TODO: Fixme
				System.out.println("Unhandled collision!!!");
			}
			
			for(int i = 0; i < collisionHandlingToolpath.size(); i++)
			{
				finalCoord = collisionHandlingToolpath.get(i);
				finalCoordTransformed = new Point3d(finalCoord[0], finalCoord[1], finalCoord[2]);
				finalCoordTransformed.sub(mCutterOffset);
				
				//System.out.printf("Collision coordinate transformed: %f, %f, %f\n", finalCoordTransformed.x, finalCoordTransformed.y, finalCoordTransformed.z);
				
				if (mLastToolpathPoint == null) 
				{
					setToolpathStart(finalCoordTransformed);
				} 
				else 
				{
					addToolpathLine(finalCoordTransformed);
				}
	
				writeCoordinate(new double[]{finalCoordTransformed.x, finalCoordTransformed.y, finalCoordTransformed.z});
	
				mLastToolpathPoint = finalCoordTransformed;					
			}
		} 
		else 
		{
			if (mLastToolpathPoint == null)
			{
				setToolpathStart(finalCoordTransformed);
				writeCoordinate(new double[]{finalCoordTransformed.x, finalCoordTransformed.y, finalCoordTransformed.z});				
				mLastToolpathPoint = finalCoordTransformed;
			} 
			else
			if(!mLastToolpathPoint.equals(finalCoordTransformed))
			{
				addToolpathLine(finalCoordTransformed);

				writeCoordinate(new double[]{finalCoordTransformed.x, finalCoordTransformed.y, finalCoordTransformed.z});
		
				mLastToolpathPoint = finalCoordTransformed;
			}
			// System.out.printf("mLastToolpathPoint: %f, %f, %f\n",
			// mLastToolpathPoint.x, mLastToolpathPoint.y,
			// mLastToolpathPoint.z);
				
		}
	}

	protected void writeToolpathBegin() {
		mCurrentWriter.writeComment(mStream, "Generated with BoardCAD");

		mCurrentWriter.writeMetric(mStream);
		mCurrentWriter.writeAbsoluteCoordinateMode(mStream);
		mCurrentWriter.writeToolOn(mStream);

		mCurrentWriter.writeBeginGoTo(mStream);
	}

	protected void writeCoordinate(double[] coordinate) {
		mCurrentWriter.writeCoordinate(mStream, coordinate);
	}

	protected void writeToolpathEnd() {
		mCurrentWriter.writeToolOff(mStream);

		mCurrentWriter.writeEnd(mStream);
	}

	protected Point3d transformPoint(Point3d point) {
		Point3d transformedCoord = new Point3d(point);
		mOffsetMatrix.transform(transformedCoord);

		return transformedCoord;
	}

	protected Vector3d transformVector(Vector3d vector) {
		Vector3d transformedVector = new Vector3d(vector);
		mRotationOffsetMatrix.transform(transformedVector);

		return transformedVector;
	}

	protected Point3d transformPointInverse(Point3d point) {
		Point3d transformedCoord = new Point3d(point);
		mInverseOffsetMatrix.transform(transformedCoord);

		return transformedCoord;
	}

	protected Vector3d transformVectorInverse(Vector3d vector) {
		Vector3d transformedVector = new Vector3d(vector);
		mInverseRotationOffsetMatrix.transform(transformedVector);

		return transformedVector;
	}

	protected void setProgressDone(int percentDone) {
		String progressString = String
				.format(LanguageResource.getString("TOOLPATHPROGRESS_STR"),
						percentDone);
		mProgress.setNote(progressString);
		mProgress.setProgress(percentDone);
	}

	protected void cancel() {
		mCancelled = true;
	}

	abstract protected Point3d getToolpathCoordinate();

	abstract protected Vector3d getToolpathNormalVector();

	abstract protected void next();

	abstract protected boolean isAtStringer();

	abstract protected double calcSpeed(Point3d pos, Vector3d normal,
			AbstractBoard board, boolean isAtStringer);

	protected boolean checkCollision(Point3d pos, AbstractBoard board) {
		return mCurrentCutter.checkCollision(pos, board);
	}

	protected void handleCollision(Point3d point, AbstractCutter cutter,
			AbstractBoard board) {

	}

	protected Machine3DView getMachine3DView() {
		// MachineView view = BoardCAD.getInstance().getMachineView();
		//
		// if(view == null)
		// return null;
		//
		// return view.get3DView();

		return null;
	}

	protected void reset3DView() {
		if (getMachine3DView() != null)
			getMachine3DView().reset();
	}

	protected void setSurfaceStart(Point3d point) {
		if (getMachine3DView() != null)
			getMachine3DView().setDeckSurfaceStart(point);
	}

	protected void addSurfaceLine(Point3d point) {
		if (getMachine3DView() != null)
			getMachine3DView().addDeckSurfaceLine(point);
	}

	protected void addNormal(Point3d point, Vector3d vector) {
		if (getMachine3DView() != null)
			getMachine3DView().addDeckNormal(point, vector);
	}

	protected void addToolpathLine(Point3d point) {
		if (getMachine3DView() != null)
			getMachine3DView().addDeckToolpathLine(point);
	}

	protected void setToolpathStart(Point3d point) {
		if (getMachine3DView() != null)
			getMachine3DView().setDeckToolpathStart(point);
	}

}
