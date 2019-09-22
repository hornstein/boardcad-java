package boardcam.toolpathgenerators;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import cadcore.BezierSpline;
import board.AbstractBoard;
import board.BezierBoard;
import boardcad.FileTools;
import boardcad.gui.jdk.MachineView;
import boardcad.i18n.LanguageResource;
import boardcad.settings.Settings;
import boardcam.MachineConfig;
import boardcam.cutters.AbstractCutter;
import boardcam.toolpathgenerators.ext.SandwichCompensation;
import boardcam.writers.AbstractMachineWriter;

public class SurfaceSplitsToolpathGenerator extends AbstractToolpathGenerator
		implements Cloneable {

	public class State {
		public static final int NO_STATE = 0;
		public static final int DECK_STRINGER_CUT = 1;
		public static final int DECK_NOSE_CUTOFF = 2;
		public static final int DECK_RIGHT_CUT = 3;
		public static final int DECK_LEFT_CUT = 4;
		public static final int DECK_LEFT_RAIL_CUT = 5;
		public static final int DECK_RIGHT_RAIL_CUT = 6;
		public static final int DECK_LEFT_CLEANUP_CUT = 7;
		public static final int DECK_RIGHT_CLEANUP_CUT = 8;
		public static final int DECK_SIDE_CHANGE = 9;
		public static final int DECK_LEFT_OUTLINE_CUT = 10;
		public static final int DECK_RIGHT_OUTLINE_CUT = 11;
		public static final int DECK_FINISHED = 12;

		public static final int BOTTOM_STRINGER_CUT = 13;
		public static final int BOTTOM_TAIL_CUTOFF = 14;
		public static final int BOTTOM_RIGHT_CUT = 15;
		public static final int BOTTOM_LEFT_CUT = 16;
		public static final int BOTTOM_LEFT_RAIL_CUT = 17;
		public static final int BOTTOM_RIGHT_RAIL_CUT = 18;
		public static final int BOTTOM_LEFT_CLEANUP_CUT = 19;
		public static final int BOTTOM_RIGHT_CLEANUP_CUT = 20;
		public static final int BOTTOM_SIDE_CHANGE = 21;
		public static final int BOTTOM_LEFT_OUTLINE_CUT = 22;
		public static final int BOTTOM_RIGHT_OUTLINE_CUT = 23;
		public static final int BOTTOM_FINISHED = 24;
	};

	static double RAD = Math.PI / 180.0;
	static double DECK_EXTREME_ANGLE = 0.0;
	static double BOTTOM_EXTREME_ANGLE = 360.0;

	static double NINTY_ANGLE = 90.0;

	double mLengthwiseResolution = 1.0;

	boolean mDeckStringerCut = true;
	boolean mBottomStringerCut = true;
	boolean mStayAwayFromStringer = false;

	boolean mCutOffNose = true;
	boolean mCutOffTail = true;

	protected int mNrOfStringers = 1;
	protected int mDeckCuts = 0;
	protected int mDeckRailCuts = 0;
	protected int mBottomRailCuts = 0;
	protected int mBottomCuts = 0;
	protected boolean mDeckCutOutline = false;
	protected int mDeckCutOutlineDepth = 0;
	protected boolean mBottomCutOutline = false;
	protected int mDeckCleanUpCuts = 0;
	protected int mBottomCleanUpCuts = 0;

	protected double mStringerSpacing = 0;
	protected double mStringerWidth = 0;
	protected double mDeckAngle = 0;
	protected double mDeckRailAngle = 0;
	protected double mBottomAngle = 0;
	protected double mBottomRailAngle = 0;

	private double mNormalSpeed = 0;
	private double mStringerSpeed = 0;
	private double mRailSpeed = 0;
	private double mOutlineSpeed = 0;

	private boolean mStartDeckCutAtTail = true;
	private int mBottomStartPos = 0;

	private boolean mHasCutBottomStringer = false;

	private int mTotalCuts = 0;

	protected double mLength = 0;
	protected int mNrOfLengthSplits = 0;

	protected int mMoveToNextToolpathSteps = 5;

	protected int mBottomSideChangeSteps = 10;
	protected int mDeckSideChangeSteps = 10;

	protected boolean mFirstCoordinate = false;

	protected MachineConfig mConfig;

	protected MachineView mMachineView; // TODO: Bad dependency

	protected SandwichCompensation mSandwichCompensation;

	protected boolean mNoseToTail = false;
	protected int mCurrentState = State.NO_STATE;
	protected int mCutNumber = 0;
	protected double y = 0;
	protected int i = 0;
	protected int j = 0;
	protected Vector3d mNormal;
	protected Point3d mPoint;
	protected boolean mIsCuttingStringer = false;
	protected boolean mIsCuttingRail = false;
	protected boolean mIsCuttingOutline = false;
	
	static int mDeckProgress = 0;
	static int mBottomProgress = 0;


	protected BezierBoard mBrd;

	protected SurfaceSplitsToolpathGenerator mDeckGenerator = null;
	protected SurfaceSplitsToolpathGenerator mBottomGenerator = null;

	public SurfaceSplitsToolpathGenerator(MachineConfig config,
			AbstractMachineWriter writer, MachineView view) {
		super(writer, view);

		mConfig = config;
		mMachineView = view;

		mSandwichCompensation = new SandwichCompensation(mConfig);
	}

	protected void initDeck() {
		y = 0;
		i = 0;
		j = 0;
		mLastSurfacePoint = null;
		mLastToolpathPoint = null;
		mFirstCoordinate = true;

		mPoint = new Point3d(0.0, 0.0, 0.0);
		mNormal = new Vector3d(0.0, 0.0, 0.0);

		mNoseToTail = mStartDeckCutAtTail ? false : true;

		mCurrentState = mStartDeckCutAtTail ? State.DECK_STRINGER_CUT
				: State.DECK_NOSE_CUTOFF;
		mIsCuttingStringer = true;
		mIsCuttingRail = false;
		mIsCuttingOutline = false;

		setOffsetAndRotation(
				mCurrentBlankHoldingSystem.getBoardDeckOffsetPos(),
				mCurrentBlankHoldingSystem.getBoardDeckOffsetAngle(), 10.0);

		// Go to first relevant state
		while (isCurrentStateFinished()) {
			nextState();
		}

		try {
			Locale.setDefault(Locale.US);

			mFile = new File(FileTools.setExtension(FileTools.append(mFilename,
					LanguageResource.getString("TOOLPATHFILEAPPENDDECK_STR")),
					"nc"));

			mStream = new PrintStream(mFile);

		} catch (Exception e) {
			System.out
					.println("Exception in AbstractToolpathGenerator::initDeck(): "
							+ e.toString());
		}
	}

	protected void initBottom() {
		y = 0;
		i = 0;
		j = 0;
		mLastSurfacePoint = new Point3d(0.0, 0.0, 0.0);
		mLastToolpathPoint = new Point3d(0.0, 0.0, 0.0);
		mFirstCoordinate = true;

		mPoint = new Point3d(0.0, 0.0, 0.0);
		mNormal = new Vector3d(0.0, 0.0, 0.0);

		mHasCutBottomStringer = false;

		switch (mBottomStartPos) {
		case 0: // Nose
			mNoseToTail = true;
			break;
		case 1: // Tail
			mNoseToTail = false;
			break;
		case 2: // Nothing
			break;

		}

		mCurrentState = mNoseToTail ? State.BOTTOM_STRINGER_CUT
				: State.BOTTOM_TAIL_CUTOFF;
		mIsCuttingStringer = true;
		mIsCuttingRail = false;
		mIsCuttingOutline = false;

		/*
		 * mCurrentCutter.update3DModel();
		 * mMachineView.get3DView().setBottomActive();
		 * mMachineView.get3DView().setCutterModel(mCurrentCutter.get3DModel());
		 * mMachineView.get3DView().setCutterpos(new Point3d(0.0, 0.0, 0.0));
		 * 
		 * setCutterOffset(mConfig.getCutterOffset());
		 */
		setOffsetAndRotation(
				mCurrentBlankHoldingSystem.getBoardBottomOffsetPos(),
				mCurrentBlankHoldingSystem.getBoardBottomOffsetAngle(), 10.0);

		// Go to first relevant state
		while (isCurrentStateFinished()) {
			nextState();
		}

		try {
			Locale.setDefault(Locale.US);

			mFile = new File(FileTools.setExtension(
					FileTools.append(mFilename, LanguageResource
							.getString("TOOLPATHFILEAPPENDBOTTOM_STR")), "nc"));

			mStream = new PrintStream(mFile);

		} catch (Exception e) {
			System.out
					.println("Exception in AbstractToolpathGenerator::initBottom(): "
							+ e.toString());
		}
	}

	protected void writeToolpathBegin() {
		mCurrentWriter.writeComment(mStream, "Generated with BoardCAD v3.1");
		// TODO: Write time and date
		mCurrentWriter.writeComment(mStream,
				isDeckCut() ? LanguageResource.getString("DECKCUT_STR")
						: LanguageResource.getString("BOTTOMCUT_STR"));
		mCurrentWriter.writeComment(
				mStream,
				LanguageResource.getString("BOARD_STR").concat(
						mConfig.getBoard().toString()));
		mCurrentWriter.writeComment(
				mStream,
				LanguageResource.getString("BLANK_STR").concat(
						mConfig.getBlank().toString()));
		mCurrentWriter.writeComment(
				mStream,
				LanguageResource.getString("CUTTER_STR").concat(
						mConfig.getCutter().toString()));
		mCurrentWriter.writeComment(
				mStream,
				LanguageResource.getString("BLANKHOLDINGSYSTEM_STR").concat(
						mConfig.getBlankHoldingSystem().toString(isDeckCut())));

		// TODO: Add cut and speed settings information in comments

		Settings controlSettings = mConfig.getCategory(LanguageResource
				.getString("CONTROLCATEGORY_STR"));
		int enu = controlSettings
				.getEnumeration(isDeckCut() ? MachineConfig.BEFORE_DECK_CUT
						: MachineConfig.BEFORE_BOTTOM_CUT);
		switch (enu) {
		case 0: // nothing
			break;
		case 1: // home
			mCurrentWriter.writeHome(mStream, null);
			break;
		case 2: // Home reference
			mCurrentWriter.writeHomeRef(mStream, null);
			break;
		case 3: // Lift clear
			double clearHeight = controlSettings
					.getMeasurement(MachineConfig.CLEAR_HEIGHT);
			double x = mNoseToTail ? mLength : 0.1;
			double z = isDeckCut() ? mBoard.getDeckAt(x, BezierSpline.ZERO)
					: mBoard.getBottomAt(x, BezierSpline.ZERO);
			mCurrentWriter.writeBeginGoTo(mStream);
			mCurrentWriter.writeZCoordinate(mStream, z + clearHeight);
			break;
		case 4: // Script
			String filename = controlSettings
					.getFileName(isDeckCut() ? MachineConfig.BEFORE_DECK_CUT_SCRIPT
							: MachineConfig.BEFORE_BOTTOM_CUT_SCRIPT);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						filename));

				String script = new String();
				String line = reader.readLine();
				while (line != null) {
					script.concat(line);
					line = reader.readLine();
				}

				mCurrentWriter.writeScript(mStream, script);
			} catch (Exception e) {
				String str = new String();
				str.concat("Failed to read pre cut script ");
				str.concat(filename);
				str.concat(", Error: ");
				str.concat(e.getMessage());
				mCurrentWriter.writeComment(mStream, str);
			}
		}

		mCurrentWriter.writeMetric(mStream);
		mCurrentWriter.writeAbsoluteCoordinateMode(mStream);
		mCurrentWriter.writeToolOn(mStream);
		mCurrentWriter.writePause(mStream, 2);

		mCurrentWriter.writeBeginGoTo(mStream);

		// Do a little trick to start just above where we start cutting and
		// start by going down slowly into the board
		getCurrentStateCut();

		Point3d coord = getToolpathCoordinate();
		Vector3d normal = getToolpathNormalVector();

		// Move to two inches above startingPoint
		Point3d above = new Point3d(coord);
		above.z += 2.54 * 2;
		processCoordinate(above, normal);

		// Move slowly down
		double normalSpeed = calcSpeed(coord, normal, mBoard, isAtStringer());
		double reducedSpeed = normalSpeed * 0.05;
		updateSpeed((int) reducedSpeed);
		processCoordinate(coord, normal);

		// Ready for next pos
		next();
	}

	protected void writeToolpathEnd() {
		mCurrentWriter.writeToolOff(mStream);

		Settings controlSettings = mConfig.getCategory(LanguageResource
				.getString("CONTROLCATEGORY_STR"));
		int enu = controlSettings
				.getEnumeration(isDeckCut() ? MachineConfig.AFTER_DECK_CUT
						: MachineConfig.AFTER_BOTTOM_CUT);
		switch (enu) {
		case 0: // nothing
			break;
		case 1: // home
			mCurrentWriter.writeHome(mStream, null);
			break;
		case 2: // Home reference
			mCurrentWriter.writeHomeRef(mStream, null);
			break;
		case 3: // Lift clear
			double clearHeight = controlSettings
					.getMeasurement(MachineConfig.CLEAR_HEIGHT);
			mCurrentWriter.writeBeginGoTo(mStream);
			mCurrentWriter.writeZCoordinate(mStream, mPoint.z + clearHeight);
			break;
		case 4: // Script
			String filename = controlSettings
					.getFileName(isDeckCut() ? MachineConfig.AFTER_DECK_CUT_SCRIPT
							: MachineConfig.AFTER_BOTTOM_CUT_SCRIPT);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						filename));

				String script = new String();
				String line = reader.readLine();
				while (line != null) {
					script.concat(line);
					line = reader.readLine();
				}

				mCurrentWriter.writeScript(mStream, script);
			} catch (Exception e) {
				String str = new String();
				str.concat("Failed to read post cut script ");
				str.concat(filename);
				str.concat(", Error: ");
				str.concat(e.getMessage());
				mCurrentWriter.writeComment(mStream, str);
			}

		}

		mCurrentWriter.writeEnd(mStream);
	}

	public void writeToolpath(String filename, AbstractBoard board,
			AbstractBoard blank) {
		mFilename = filename;
		mBoard = board;
		mBlank = blank;

		BezierBoard brd = (BezierBoard) mBoard;
		mBrd = (BezierBoard) brd.clone();

		try{
			mCurrentCutter = (AbstractCutter)mConfig.getCutter().clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCurrentBlankHoldingSystem = mConfig.getBlankHoldingSystem();

		mProgress.setProgress(0);
		mDeckProgress = 0;
		mBottomProgress = 0;

		mCutNumber = 0;

		mMachineView.get3DView().reset();

		getSettings(mConfig);

		mLength = mBoard.getLength() - 0.1;// BezierPatch.ZERO;
		mNrOfLengthSplits = ((int) (mLength / mLengthwiseResolution)) + 1;

		mCurrentCutter.init();

		mCurrentCutter.update3DModel();
		mMachineView.get3DView().setDeckActive();

		mCurrentCutter.setStayAwayFromStringer(mStayAwayFromStringer);
		mCurrentCutter.setStringerWidth(mStringerWidth);
		mMachineView.get3DView().setCutterPos(new Point3d(0.0, 0.0, 0.0));

		setCutterOffset(mConfig.getCutterOffset());

		try {
			mDeckGenerator = (SurfaceSplitsToolpathGenerator) this.clone();
			mBottomGenerator = (SurfaceSplitsToolpathGenerator) this.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SwingWorker<Void, Void> deckWorker = getDeckWorker();
		deckWorker.execute();

		SwingWorker<Void, Void> bottomWorker = getBottomWorker();
		bottomWorker.execute();
	}

	protected SwingWorker<Void, Void> getDeckWorker() {
		return new SwingWorker<Void, Void>() {

			protected Void doInBackground() throws Exception {

				try {

					mDeckGenerator.initDeck();
					mDeckGenerator.writeToolpath();

					mProgress.close();
				} catch (Exception e) {
					System.out
							.println("Exception in SwingWorker::doInBackground(): "
									+ e.toString());

					mProgress.close();

					JOptionPane.showMessageDialog(
							mConfig.getMachineView(),
							"An error occured when writing g-code, error:"
									+ e.toString(),
							"Error when writing G-Code file",
							JOptionPane.ERROR_MESSAGE);

					return null;
				}

				return null;
			}
		};

	}

	protected SwingWorker<Void, Void> getBottomWorker() {
		return new SwingWorker<Void, Void>() {

			protected Void doInBackground() throws Exception {

				try {

					mBottomGenerator.initBottom();
					mBottomGenerator.writeToolpath();

					mProgress.close();
				} catch (Exception e) {
					System.out
							.println("Exception in SwingWorker::doInBackground(): "
									+ e.toString());

					mProgress.close();

					JOptionPane.showMessageDialog(
							mConfig.getMachineView(),
							"An error occured when writing g-code, error:"
									+ e.toString(),
							"Error when writing G-Code file",
							JOptionPane.ERROR_MESSAGE);

					return null;
				}

				return null;
			}
		};

	}

	protected void getSettings(MachineConfig config) {
		Settings cutsSettings = config.getCategory(LanguageResource
				.getString("CUTSCATEGORY_STR"));

		mLengthwiseResolution = cutsSettings
				.getMeasurement(MachineConfig.LENGTHWISE_RESOLUTION);

		mDeckStringerCut = cutsSettings
				.getBoolean(MachineConfig.DECK_STRINGER_CUT);
		mBottomStringerCut = cutsSettings
				.getBoolean(MachineConfig.BOTTOM_STRINGER_CUT);

		mStayAwayFromStringer = cutsSettings
				.getBoolean(MachineConfig.STAY_AWAY_FROM_STRINGER);
		mStringerWidth = cutsSettings
				.getMeasurement(MachineConfig.STRINGER_WIDTH);

		mDeckCutOutline = cutsSettings
				.getBoolean(MachineConfig.DECK_OUTLINE_CUT);
		mDeckCutOutlineDepth = cutsSettings
				.getEnumeration(MachineConfig.DECK_OUTLINE_CUT_DEPTH);
		mBottomCutOutline = cutsSettings
				.getBoolean(MachineConfig.BOTTOM_OUTLINE_CUT);
		mCutOffNose = cutsSettings.getBoolean(MachineConfig.CUT_OFF_NOSE);
		mCutOffTail = cutsSettings.getBoolean(MachineConfig.CUT_OFF_TAIL);

		mDeckCuts = cutsSettings.getInt(MachineConfig.DECK_CUTS);
		mDeckRailCuts = cutsSettings.getInt(MachineConfig.DECK_RAIL_CUTS);
		mBottomCuts = cutsSettings.getInt(MachineConfig.BOTTOM_CUTS);
		mBottomRailCuts = cutsSettings.getInt(MachineConfig.BOTTOM_RAIL_CUTS);

		mDeckCleanUpCuts = cutsSettings
				.getInt(MachineConfig.DECK_CLEAN_UP_CUTS);
		mBottomCleanUpCuts = cutsSettings
				.getInt(MachineConfig.BOTTOM_CLEAN_UP_CUTS);

		mDeckAngle = cutsSettings.getDouble(MachineConfig.DECK_ANGLE);
		mDeckRailAngle = cutsSettings.getDouble(MachineConfig.DECK_RAIL_ANGLE);
		mBottomAngle = cutsSettings.getDouble(MachineConfig.BOTTOM_ANGLE);
		mBottomRailAngle = cutsSettings
				.getDouble(MachineConfig.BOTTOM_RAIL_ANGLE);

		Settings speedSettings = config.getCategory(LanguageResource
				.getString("SPEEDCATEGORY_STR"));
		mNormalSpeed = speedSettings.getDouble(MachineConfig.CUTTING_SPEED);
		mStringerSpeed = speedSettings
				.getDouble(MachineConfig.CUTTING_SPEED_STRINGER);
		mRailSpeed = speedSettings.getDouble(MachineConfig.CUTTING_SPEED_RAIL);
		mOutlineSpeed = speedSettings
				.getDouble(MachineConfig.CUTTING_SPEED_OUTLINE);

		mTotalCuts = mNrOfStringers + (mDeckCuts * 2) + (mDeckRailCuts * 2)
				+ (mDeckCleanUpCuts * 2) + 1 + mNrOfStringers
				+ (mBottomCuts * 2) + (mBottomRailCuts * 2)
				+ (mBottomCleanUpCuts * 2) + 1;

		Settings controlSettings = config.getCategory(LanguageResource
				.getString("CONTROLCATEGORY_STR"));
		mStartDeckCutAtTail = controlSettings
				.getEnumeration(MachineConfig.DECK_START_POS) == 1;
		mBottomStartPos = controlSettings
				.getEnumeration(MachineConfig.BOTTOM_START_POS);
	}

	protected boolean isDeckCut() {
		return (mCurrentState == State.DECK_STRINGER_CUT
				|| mCurrentState == State.DECK_NOSE_CUTOFF
				|| mCurrentState == State.DECK_RIGHT_CUT
				|| mCurrentState == State.DECK_RIGHT_RAIL_CUT
				|| mCurrentState == State.DECK_RIGHT_OUTLINE_CUT
				|| mCurrentState == State.DECK_RIGHT_CLEANUP_CUT
				|| mCurrentState == State.DECK_SIDE_CHANGE
				|| mCurrentState == State.DECK_LEFT_CUT
				|| mCurrentState == State.DECK_LEFT_RAIL_CUT
				|| mCurrentState == State.DECK_LEFT_OUTLINE_CUT
				|| mCurrentState == State.DECK_LEFT_CLEANUP_CUT || mCurrentState == State.DECK_FINISHED);
	}

	protected boolean isBottomCut() {
		return (mCurrentState == State.BOTTOM_STRINGER_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_RAIL_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_OUTLINE_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_CLEANUP_CUT
				|| mCurrentState == State.BOTTOM_SIDE_CHANGE
				|| mCurrentState == State.BOTTOM_LEFT_CUT
				|| mCurrentState == State.BOTTOM_LEFT_RAIL_CUT
				|| mCurrentState == State.BOTTOM_LEFT_OUTLINE_CUT
				|| mCurrentState == State.BOTTOM_LEFT_CLEANUP_CUT || mCurrentState == State.BOTTOM_FINISHED);
	}

	protected boolean isOutlineCut() {
		return (mCurrentState == State.DECK_RIGHT_OUTLINE_CUT
				|| mCurrentState == State.DECK_RIGHT_CLEANUP_CUT
				|| mCurrentState == State.DECK_LEFT_OUTLINE_CUT
				|| mCurrentState == State.DECK_LEFT_CLEANUP_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_OUTLINE_CUT || mCurrentState == State.BOTTOM_LEFT_OUTLINE_CUT);
	}

	protected boolean isRightCut() {
		return (mCurrentState == State.DECK_RIGHT_CUT
				|| mCurrentState == State.DECK_RIGHT_RAIL_CUT
				|| mCurrentState == State.DECK_RIGHT_OUTLINE_CUT
				|| mCurrentState == State.DECK_RIGHT_CLEANUP_CUT
				|| mCurrentState == State.DECK_SIDE_CHANGE
				|| mCurrentState == State.BOTTOM_RIGHT_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_RAIL_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_OUTLINE_CUT
				|| mCurrentState == State.BOTTOM_RIGHT_CLEANUP_CUT || mCurrentState == State.BOTTOM_FINISHED);
	}

	protected boolean isSideChange() {
		return (mCurrentState == State.DECK_SIDE_CHANGE || mCurrentState == State.BOTTOM_SIDE_CHANGE);
	}

	public Point3d getToolpathCoordinate() {
		getCurrentStateCut();

		if (mPoint != null) {
			if (mSandwichCompensation.compensateOutlineCuts() && isOutlineCut()
					&& !isSideChange()) {
				mPoint = mSandwichCompensation.compensateOutlineCut(mPoint,
						mNormal);
			} else if (mSandwichCompensation.compensateDeckCuts()
					&& isDeckCut() && !isSideChange()) {
				mPoint = mSandwichCompensation.compensateDeckCut(mPoint,
						mNormal);
			} else if (mSandwichCompensation.compensateBottomCuts()
					&& isBottomCut() && !isSideChange()) {
				mPoint = mSandwichCompensation.compensateBottomCut(mPoint,
						mNormal);
			}
		}

		return mPoint;
	}

	public void next() {
		if (isCurrentToolpathFinished()) {
			nextToolpath();
		}

		while (isCurrentStateFinished()) {
			nextState();
		}
	}

	protected boolean isCurrentToolpathFinished() {
		switch (mCurrentState) {
		case State.DECK_RIGHT_CUT:
		case State.DECK_RIGHT_RAIL_CUT:
		case State.DECK_LEFT_CUT:
		case State.DECK_LEFT_RAIL_CUT:
		case State.BOTTOM_RIGHT_CUT:
		case State.BOTTOM_RIGHT_RAIL_CUT:
		case State.BOTTOM_LEFT_CUT:
		case State.BOTTOM_LEFT_RAIL_CUT:
			return j++ >= mNrOfLengthSplits + mMoveToNextToolpathSteps;

		case State.DECK_STRINGER_CUT:
		case State.DECK_SIDE_CHANGE:
		case State.DECK_LEFT_OUTLINE_CUT:
		case State.DECK_LEFT_CLEANUP_CUT:
		case State.DECK_RIGHT_OUTLINE_CUT:
		case State.DECK_RIGHT_CLEANUP_CUT:
		case State.BOTTOM_STRINGER_CUT:
		case State.BOTTOM_SIDE_CHANGE:
		case State.BOTTOM_LEFT_OUTLINE_CUT:
		case State.BOTTOM_LEFT_CLEANUP_CUT:
		case State.BOTTOM_RIGHT_OUTLINE_CUT:
		case State.BOTTOM_RIGHT_CLEANUP_CUT:
		default:
			return j++ > mNrOfLengthSplits;

		case State.DECK_NOSE_CUTOFF:
			return mCurrentCutter.isNoseCutOffFinished(j++);

		case State.BOTTOM_TAIL_CUTOFF:
			return mCurrentCutter.isTailCutOffFinished(j++);
		}
	}

	protected void nextToolpath() {
		mNoseToTail = !mNoseToTail;

		j = 0;
		i++;

		mCutNumber++;

		updateProgress();
	}

	protected void updateProgress() {
		
		if(isBottomCut())
		{
			mBottomProgress = (int) (((double) mCutNumber / (double) mTotalCuts) * 100.0);
		}
		else
		{
			mDeckProgress = (int) (((double) mCutNumber / (double) mTotalCuts) * 100.0);
		}
		
		int progress = mDeckProgress + mBottomProgress;
		
		setProgressDone(progress);
	}

	protected void getCurrentStateCut() {
		double x = getX(i, j); // DEBUG
		// System.out.printf("SurfaceSplitToolpathGenerator.getCurrentStateCut() state:%s, i:%d, j:%d, x:%f\n",getStateString(mCurrentState),
		// i, j, x);

		switch (mCurrentState) {
		case State.DECK_STRINGER_CUT:
			getDeckStringerCut();
			break;

		case State.DECK_NOSE_CUTOFF:
			getDeckNoseCutOff();
			break;

		case State.DECK_RIGHT_CUT:
			getDeckRightCut();
			break;

		case State.DECK_RIGHT_RAIL_CUT:
			getDeckRailRightCut();
			break;

		case State.DECK_RIGHT_OUTLINE_CUT:
			getDeckOutlineRightCut();
			break;

		case State.DECK_RIGHT_CLEANUP_CUT:
			getDeckRailCleanupRightCut();
			break;

		case State.DECK_SIDE_CHANGE:
			getDeckSideChange();
			break;

		case State.DECK_LEFT_CUT:
			getDeckLeftCut();
			break;

		case State.DECK_LEFT_RAIL_CUT:
			getDeckRailLeftCut();
			break;

		case State.DECK_LEFT_OUTLINE_CUT:
			getDeckOutlineLeftCut();
			break;

		case State.DECK_LEFT_CLEANUP_CUT:
			getDeckRailCleanupLeftCut();
			break;

		case State.BOTTOM_STRINGER_CUT:
			getBottomStringerCut();
			break;

		case State.BOTTOM_TAIL_CUTOFF:
			getBottomTailCutOff();
			break;

		case State.BOTTOM_RIGHT_CUT:
			getBottomRightCut();
			break;

		case State.BOTTOM_RIGHT_RAIL_CUT:
			getBottomRailRightCut();
			break;

		case State.BOTTOM_RIGHT_OUTLINE_CUT:
			getBottomOutlineRightCut();
			break;

		case State.BOTTOM_RIGHT_CLEANUP_CUT:
			getBottomRailCleanupRightCut();
			break;

		case State.BOTTOM_SIDE_CHANGE:
			getBottomSideChange();
			break;

		case State.BOTTOM_LEFT_CUT:
			getBottomLeftCut();
			break;

		case State.BOTTOM_LEFT_RAIL_CUT:
			getBottomRailLeftCut();
			break;

		case State.BOTTOM_LEFT_OUTLINE_CUT:
			getBottomOutlineLeftCut();
			break;

		case State.BOTTOM_LEFT_CLEANUP_CUT:
			getBottomRailCleanupLeftCut();
			break;

		default:
			mPoint = null;
			break;
		}
	}

	protected boolean isCurrentStateFinished() {
		switch (mCurrentState) {
		case State.DECK_STRINGER_CUT:
			return (i >= mNrOfStringers || mDeckStringerCut == false);

		case State.DECK_NOSE_CUTOFF:
			return (mCutOffNose == false || mCurrentCutter
					.isNoseCutOffFinished(j));

		case State.DECK_RIGHT_CUT:
			return (i >= mDeckCuts);

		case State.DECK_RIGHT_RAIL_CUT:
			return (i >= mDeckRailCuts);

		case State.DECK_RIGHT_OUTLINE_CUT:
			return (mDeckCutOutline == false || i >= 1);

		case State.DECK_RIGHT_CLEANUP_CUT:
			return (i >= mDeckCleanUpCuts);

		case State.DECK_SIDE_CHANGE:
			return ((mDeckCuts == 0 && mDeckRailCuts == 0
					&& mDeckCleanUpCuts == 0 && mDeckCutOutline == false) || j > mDeckSideChangeSteps);

		case State.DECK_LEFT_CUT:
			return (i >= mDeckCuts);

		case State.DECK_LEFT_RAIL_CUT:
			return (i >= mDeckRailCuts);

		case State.DECK_LEFT_OUTLINE_CUT:
			return (mDeckCutOutline == false || i >= 1);

		case State.DECK_LEFT_CLEANUP_CUT:
			return (i >= mDeckCleanUpCuts);

		case State.BOTTOM_STRINGER_CUT:
			return (i >= mNrOfStringers || mBottomStringerCut == false);

		case State.BOTTOM_TAIL_CUTOFF:
			return (mCutOffTail == false || mCurrentCutter
					.isTailCutOffFinished(j));

		case State.BOTTOM_RIGHT_CUT:
			return (i >= mBottomCuts);

		case State.BOTTOM_RIGHT_RAIL_CUT:
			return (i >= mBottomRailCuts);

		case State.BOTTOM_RIGHT_OUTLINE_CUT:
			return (mBottomCutOutline == false || i >= 1);

		case State.BOTTOM_RIGHT_CLEANUP_CUT:
			return (i >= mBottomCleanUpCuts);

		case State.BOTTOM_SIDE_CHANGE:
			return ((mBottomCuts == 0 && mBottomRailCuts == 0
					&& mBottomCleanUpCuts == 0 && mBottomCutOutline == false) || j > mBottomSideChangeSteps);

		case State.BOTTOM_LEFT_CUT:
			return (i >= mBottomCuts);

		case State.BOTTOM_LEFT_RAIL_CUT:
			return (i >= mBottomRailCuts);

		case State.BOTTOM_LEFT_OUTLINE_CUT:
			return (mBottomCutOutline == false || i >= 1);

		case State.BOTTOM_LEFT_CLEANUP_CUT:
			return (i >= mBottomCleanUpCuts);

		default:
			return false; // No state so it cannot really be finished, can it?
		}
	}

	protected void nextState() {
		switch (mCurrentState) {
		case State.DECK_STRINGER_CUT:
			if (mStartDeckCutAtTail) {
				mCurrentState = State.DECK_NOSE_CUTOFF;
				mIsCuttingStringer = true;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			} else {
				mCurrentState = State.DECK_RIGHT_CUT;
				mIsCuttingStringer = false;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			}
			break;

		case State.DECK_NOSE_CUTOFF:
			if (mStartDeckCutAtTail) {
				mCurrentState = State.DECK_RIGHT_CUT;
				mIsCuttingStringer = false;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
				if (mCutOffNose)
					mNoseToTail = true; // Always cutting nose to tail after
										// cutting
										// off nose
			} else {
				mCurrentState = State.DECK_STRINGER_CUT;
				mIsCuttingStringer = true;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			}
			break;

		case State.DECK_RIGHT_CUT:
			mCurrentState = State.DECK_RIGHT_RAIL_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = true;
			mIsCuttingOutline = false;
			break;

		case State.DECK_RIGHT_RAIL_CUT:
			mCurrentState = State.DECK_RIGHT_OUTLINE_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = true;
			break;

		case State.DECK_RIGHT_OUTLINE_CUT:
			mCurrentState = State.DECK_RIGHT_CLEANUP_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.DECK_RIGHT_CLEANUP_CUT:
			mCurrentState = State.DECK_SIDE_CHANGE;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.DECK_SIDE_CHANGE:
			mCurrentState = State.DECK_LEFT_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.DECK_LEFT_CUT:
			mCurrentState = State.DECK_LEFT_RAIL_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = true;
			mIsCuttingOutline = false;
			break;

		case State.DECK_LEFT_RAIL_CUT:
			mCurrentState = State.DECK_LEFT_OUTLINE_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = true;
			break;

		case State.DECK_LEFT_OUTLINE_CUT:
			mCurrentState = State.DECK_LEFT_CLEANUP_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.DECK_LEFT_CLEANUP_CUT:
			mCurrentState = State.DECK_FINISHED; // Ended
			break;

		// BOTTOM
		case State.BOTTOM_STRINGER_CUT:
			mHasCutBottomStringer = true;
			if (mNoseToTail) {
				mCurrentState = State.BOTTOM_TAIL_CUTOFF;
				mIsCuttingStringer = true;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			} else {
				mCurrentState = State.BOTTOM_RIGHT_CUT;
				mIsCuttingStringer = false;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			}
			break;

		case State.BOTTOM_TAIL_CUTOFF:
			if (mHasCutBottomStringer) {
				mCurrentState = State.BOTTOM_RIGHT_CUT;
				mIsCuttingStringer = false;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			} else {
				mCurrentState = State.BOTTOM_STRINGER_CUT;
				mIsCuttingStringer = true;
				mIsCuttingRail = false;
				mIsCuttingOutline = false;
			}
			if (mCutOffTail)
				mNoseToTail = false; // Always cutting tail to nose after
										// cutting off tail
			break;

		case State.BOTTOM_RIGHT_CUT:
			mCurrentState = State.BOTTOM_RIGHT_RAIL_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = true;
			mIsCuttingOutline = false;
			break;

		case State.BOTTOM_RIGHT_RAIL_CUT:
			mCurrentState = State.BOTTOM_RIGHT_OUTLINE_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = true;
			break;

		case State.BOTTOM_RIGHT_OUTLINE_CUT:
			mCurrentState = State.BOTTOM_RIGHT_CLEANUP_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.BOTTOM_RIGHT_CLEANUP_CUT:
			mCurrentState = State.BOTTOM_SIDE_CHANGE;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.BOTTOM_SIDE_CHANGE:
			mCurrentState = State.BOTTOM_LEFT_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.BOTTOM_LEFT_CUT:
			mCurrentState = State.BOTTOM_LEFT_RAIL_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = true;
			mIsCuttingOutline = false;
			break;

		case State.BOTTOM_LEFT_RAIL_CUT:
			mCurrentState = State.BOTTOM_LEFT_OUTLINE_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = true;
			break;

		case State.BOTTOM_LEFT_OUTLINE_CUT:
			mCurrentState = State.BOTTOM_LEFT_CLEANUP_CUT;
			mIsCuttingStringer = false;
			mIsCuttingRail = false;
			mIsCuttingOutline = false;
			break;

		case State.BOTTOM_LEFT_CLEANUP_CUT:
		default:
			mCurrentState = State.BOTTOM_FINISHED; // Ended
			break;
		}

		// System.out.printf("Changed state to %s\n",
		// getStateString(mCurrentState));

		i = 0;
		j = 0;
	}

	protected String getStateString(int state) {
		switch (state) {
		case State.DECK_STRINGER_CUT:
			return "DECK_STRINGER_CUT";

		case State.DECK_NOSE_CUTOFF:
			return "DECK_NOSE_CUTOFF";

		case State.DECK_RIGHT_CUT:
			return "DECK_RIGHT_CUT";

		case State.DECK_RIGHT_RAIL_CUT:
			return "DECK_RIGHT_RAIL_CUT";

		case State.DECK_RIGHT_OUTLINE_CUT:
			return "DECK_RIGHT_OUTLINE_CUT";

		case State.DECK_RIGHT_CLEANUP_CUT:
			return "DECK_RIGHT_CLEANUP_CUT";

		case State.DECK_SIDE_CHANGE:
			return "DECK_SIDE_CHANGE";

		case State.DECK_LEFT_CUT:
			return "DECK_LEFT_CUT";

		case State.DECK_LEFT_RAIL_CUT:
			return "DECK_LEFT_RAIL_CUT";

		case State.DECK_LEFT_OUTLINE_CUT:
			return "DECK_LEFT_OUTLINE_CUT";

		case State.DECK_LEFT_CLEANUP_CUT:
			return "DECK_LEFT_CLEANUP_CUT";

		case State.BOTTOM_STRINGER_CUT:
			return "BOTTOM_STRINGER_CUT";

		case State.BOTTOM_TAIL_CUTOFF:
			return "BOTTOM_TAIL_CUTOFF";

		case State.BOTTOM_RIGHT_CUT:
			return "BOTTOM_RIGHT_CUT";

		case State.BOTTOM_RIGHT_RAIL_CUT:
			return "BOTTOM_RIGHT_RAIL_CUT";

		case State.BOTTOM_RIGHT_OUTLINE_CUT:
			return "BOTTOM_RIGHT_OUTLINE_CUT";

		case State.BOTTOM_RIGHT_CLEANUP_CUT:
			return "BOTTOM_RIGHT_CLEANUP_CUT";

		case State.BOTTOM_SIDE_CHANGE:
			return "BOTTOM_SIDE_CHANGE";

		case State.BOTTOM_LEFT_CUT:
			return "BOTTOM_LEFT_CUT";

		case State.BOTTOM_LEFT_RAIL_CUT:
			return "BOTTOM_LEFT_RAIL_CUT";

		case State.BOTTOM_LEFT_OUTLINE_CUT:
			return "BOTTOM_LEFT_OUTLINE_CUT";

		case State.BOTTOM_LEFT_CLEANUP_CUT:
			return "BOTTOM_LEFT_CLEANUP_CUT";

		case State.NO_STATE:
			return "NO_STATE";

		default:
			return "ERROR, UNKNOWN STATE";
		}
	}

	protected double getX(double i, double j) {
		// Do the Deck one side
		double x;

		if (mNoseToTail == false) {
			x = j * mLengthwiseResolution;
		} else {
			x = ((int) (mLength / mLengthwiseResolution) + 1)
					* mLengthwiseResolution - (j * mLengthwiseResolution);
		}

		if (x > mLength) // Note: length is compensated with BezierPatch.ZERO
		{
			x = mLength;
		}
		if (x < 0.1) {
			x = 0.1; // Min at 1mm from tail
		}
		return x;
	}

	protected void getDeckStringerCut() {
		double x = getX(i, j);

		// //system.out.printf("getDeckStringerCut() Cut: %d x: %f\n", i+1, x);
		// mPoint = getSurfacePoint(x, DECK_EXTREME_ANGLE, mDeckAngle, 0, 1);
		// mPoint = getSurfacePoint(x, 0.0);
		// mNormal = getSurfaceNormal(x, DECK_EXTREME_ANGLE, mDeckAngle, 0, 1);
		double y = mBrd.getDeck().getValueAt(x);
		Point2D.Double normal2d = mBrd.getDeck().getNormalAt(x);

		mPoint.x = x;
		mPoint.y = 0.0;
		mPoint.z = y;
		mNormal.x = normal2d.x;
		mNormal.y = 0.0;
		mNormal.z = normal2d.y;

	}

	protected void getDeckRightCut() {
		// if(i == 0 && j == 0 && !mCutOffNose && !mDeckStringerCut) //Don't
		// move between cuts at start of deck cut if not cutting stringer or cut
		// of nose
		// {
		// j = mMoveToNextToolpathSteps;
		// }
		if (j < mMoveToNextToolpathSteps) {
			double x = getX(i, 0);
			// system.out.printf("getDeckRightCut() Move to next cut: %d step: %d\n",
			// i+1, j-mNrOfLengthSplits);
			getMoveBetweenCuts(x, DECK_EXTREME_ANGLE, mDeckAngle, i
					* mMoveToNextToolpathSteps + j, mDeckCuts
					* mMoveToNextToolpathSteps, false, false, true);
		} else {
			double x = getX(i + 1, j - mMoveToNextToolpathSteps);
			// system.out.printf("getDeckRightCut() Cut: %d x: %f\n", i+1, x);
			mPoint = getSurfacePoint(x, DECK_EXTREME_ANGLE, mDeckAngle, i + 1,
					mDeckCuts);
			mNormal = getSurfaceNormal(x, DECK_EXTREME_ANGLE, mDeckAngle,
					i + 1, mDeckCuts);
		}

	}

	protected void getDeckRailRightCut() {
		if (j < mMoveToNextToolpathSteps) {
			double x = getX(i, 0);
			// system.out.printf("getDeckRightCut() Move to next cut: %d step: %d\n",
			// i+1, j-mNrOfLengthSplits);
			getMoveBetweenCuts(x, mDeckAngle, mDeckRailAngle, i
					* mMoveToNextToolpathSteps + j, mDeckRailCuts
					* mMoveToNextToolpathSteps, false, false, true);
		} else {
			double x = getX(i + 1, j - mMoveToNextToolpathSteps);
			// system.out.printf("getDeckRailRightCut() Cut: %d x: %f\n", i+1,
			// x);
			mPoint = getSurfacePoint(x, mDeckAngle, mDeckRailAngle, i + 1,
					mDeckRailCuts);
			mNormal = getSurfaceNormal(x, mDeckAngle, mDeckRailAngle, i + 1,
					mDeckRailCuts);
		}
	}

	protected void getDeckOutlineRightCut() {
		double x = getX(i, j);
		// system.out.printf("getDeckOutlineRightCut() Cut: %d x: %f\n", i+1,
		// x);
		Point3d bottomPoint = getSurfacePoint(x, mBottomAngle, mBottomAngle, 0,
				1); // Find z value for tuck under
		Point3d apexPoint = getSurfacePoint(x, NINTY_ANGLE, NINTY_ANGLE, 0, 1); // Find
																				// z
																				// value
																				// for
																				// apex
		double bottomZ = mBrd.getBottom().getValueAt(x);
		// System.out.printf("getDeckOutlineRightCut: apex z:%f bottom z:%f bottomPoint.z:%f",
		// apexPoint.z, bottomZ, bottomPoint.z);
		// Point3d railPoint = getSurfacePoint(x, DECK_EXTREME_ANGLE,90.0,1,1);
		// //Find rail point
		double railPoint = ((BezierBoard) mBoard).getOutline().getValueAt(x); // Find
																				// rail
																				// point
		mPoint = new Point3d(x, railPoint,
				mDeckCutOutlineDepth == 0 ? apexPoint.z : bottomPoint.z);

		Point2D.Double normal = ((BezierBoard) mBoard).getOutline()
				.getNormalAt(x); // Find rail point
		mNormal = new Vector3d(normal.x, normal.y, 0.0);
		// mNormal = getSurfaceNormal(x, DECK_EXTREME_ANGLE,90.0,1,1); //Normal
		// perpendicular to rail line
	}

	protected void getDeckRailCleanupRightCut() {
		double x = getX(i, j);
		// system.out.printf("getDeckRailCleanupRightCut() Cut: %d x: %f\n",
		// i+1, x);
		mPoint = getSurfacePoint(x, NINTY_ANGLE, NINTY_ANGLE, 0, 1);
		mNormal = getSurfaceNormal(x, NINTY_ANGLE, NINTY_ANGLE, 0, 1);

		double rocker = mBoard.getBottomAt(x, 0.0);
		mPoint.z = rocker + (1.0 * i);
	}

	protected void getDeckLeftCut() {
		if (j < mMoveToNextToolpathSteps) {
			double x = getX(i, 0);
			// System.out.printf("getDeckRightCut() Move to next cut: %d step: %d\n",
			// i+1, j);
			getMoveBetweenCuts(x, DECK_EXTREME_ANGLE, mDeckAngle, i
					* mMoveToNextToolpathSteps + j, mDeckCuts
					* mMoveToNextToolpathSteps, false, false, true);
		} else {
			double x = getX(i, j - mMoveToNextToolpathSteps);
			System.out.printf("getDeckLeftCut() Cut: %d x: %f\n", i + 1, x);
			mPoint = getSurfacePoint(x, DECK_EXTREME_ANGLE, mDeckAngle, i + 1,
					mDeckCuts);
			mNormal = getSurfaceNormal(x, DECK_EXTREME_ANGLE, mDeckAngle,
					i + 1, mDeckCuts);
		}

		mirrorY();
	}

	protected void getDeckRailLeftCut() {
		if (j < mMoveToNextToolpathSteps) {
			double x = getX(i, 0);
			// System.out.printf("getDeckRailLeftCut() Move to next cut: %d step: %d\n",
			// i+1, j-mNrOfLengthSplits);
			getMoveBetweenCuts(x, mDeckAngle, mDeckRailAngle, i
					* mMoveToNextToolpathSteps + j, mDeckRailCuts
					* mMoveToNextToolpathSteps, false, false, true);
		} else {
			double x = getX(i, j - mMoveToNextToolpathSteps);
			// system.out.printf("getDeckRailLeftCut() Cut: %d x: %f\n", i+1,
			// x);
			mPoint = getSurfacePoint(x, mDeckAngle, mDeckRailAngle, i + 1,
					mDeckRailCuts);
			mNormal = getSurfaceNormal(x, mDeckAngle, mDeckRailAngle, i + 1,
					mDeckRailCuts);
		}

		mirrorY();
	}

	protected void getDeckOutlineLeftCut() {
		getDeckOutlineRightCut();

		mirrorY();
	}

	protected void getDeckRailCleanupLeftCut() {
		double x = getX(i, j);
		// system.out.printf("getDeckRailCleanupLeftCut() Cut: %d x: %f\n", i+1,
		// x);
		mPoint = getSurfacePoint(x, NINTY_ANGLE, NINTY_ANGLE, 0, 1);
		mNormal = getSurfaceNormal(x, NINTY_ANGLE, NINTY_ANGLE, 0, 1);

		double rocker = mBoard.getBottomAt(x, 0.0);
		mPoint.z = rocker + (1.0 * i);

		mirrorY();
	}

	protected void getDeckNoseCutOff() {
		mPoint = mCurrentCutter.getNoseCutOffPoint(j, mBoard, true);
		mNormal = mCurrentCutter.getNoseCutOffNormal(j, mBoard, true);
	}

	protected void getDeckSideChange() {
		double x = mNoseToTail ? mLength - 0.1 : 0.1;

		if (mStayAwayFromStringer) {
			int stepsLeft = mDeckSideChangeSteps - j;

			if (stepsLeft >= 2) {
				double startAngle = (mDeckRailCuts > 0) ? mDeckRailAngle
						: mDeckAngle;
				if (startAngle > 90) { // Move if undercut
					getMoveBetweenCuts(x, 90.0, startAngle,
							mDeckSideChangeSteps - j, mDeckSideChangeSteps,
							false, false, true);
				} else {
					// Do nothing, just wait to go over stringer
				}
			} else {
				mNormal = new Vector3d(mNoseToTail ? 1.0 : -1.0, 0.0, 0.0);
				mPoint = new Point3d(mPoint);
				if (stepsLeft == 1) {
					mPoint.z = mPoint.z + 5.0;
				} else {
					mPoint.y = -mStringerWidth * 5 / 2;
				}

			}

		} else {
			getMoveBetweenCuts(x, DECK_EXTREME_ANGLE,
					((mDeckRailCuts > 0) ? mDeckRailAngle : mDeckAngle),
					mDeckSideChangeSteps - j, mDeckSideChangeSteps, false,
					false, true);
		}

	}

	protected void getBottomStringerCut() {
		double x = getX(i, j);
		// system.out.printf("getBottomStringerCut() Cut: %d x: %f\n", i+1, x);
		// mPoint = getSurfacePoint(x, BezierSpline.ONE);
		// mPoint = new Point3d(0.0,0.0,mBrd.getRockerAtPos(x));
		// mPoint = getSurfacePoint(x, BOTTOM_EXTREME_ANGLE, mBottomAngle, 0, 1,
		// false);
		double y = mBrd.getBottom().getValueAt(x);
		Point2D.Double normal = mBrd.getBottom().getNormalAt(x);

		mPoint = new Point3d(x, 0.0, y);
		mNormal = new Vector3d(normal.x, 0.0, normal.y);
		// Does not use same function as others so don't use flip();
		mPoint.z = -mPoint.z;
		mNormal.x = -mNormal.x;
	}

	protected void getBottomRightCut() {
		// System.out.printf("-------------------------------------------------------\n");
		if (j < mMoveToNextToolpathSteps) {
			// System.out.printf("#### Move between paths\n");
			double x = getX(i, 0);
			// system.out.printf("getBottomRightCut() Move to next cut: %d step: %d\n",
			// i+1, j-mNrOfLengthSplits);
			getMoveBetweenCuts(x, BOTTOM_EXTREME_ANGLE, mBottomAngle, i
					* mMoveToNextToolpathSteps + j, mBottomCuts
					* mMoveToNextToolpathSteps, false, false, true);
		} else {
			// System.out.printf("**** Cut\n");
			double x = getX(i, j - mMoveToNextToolpathSteps);
			// system.out.printf("getBottomRightCut() Cut: %d x: %f\n", i+1, x);
			mPoint = getSurfacePoint(x, BOTTOM_EXTREME_ANGLE, mBottomAngle,
					i + 1, mBottomCuts, true);
			mNormal = getSurfaceNormal(x, BOTTOM_EXTREME_ANGLE, mBottomAngle,
					i + 1, mBottomCuts, true);
			// System.out.printf("getBottomRightCut() Step: %d/%d x:%f, minAngle:%f, maxAngle:%f,\n",
			// i + 1, mBottomCuts, x, BOTTOM_EXTREME_ANGLE, mBottomAngle);
			// System.out.printf("getBottomRightCut() mPoint: %f, %f, %f mNormal: %f, %f, %f\n",
			// mPoint.x, mPoint.y, mPoint.z, mNormal.x, mNormal.y, mNormal.z);
		}
		// System.out.printf("getBottomRightCut() i:%d j:%d mPoint: %f, %f, %f\n",i,j,
		// mPoint.x, mPoint.y, mPoint.z);
		flip();
	}

	protected void getBottomRailRightCut() {
		if (j < mMoveToNextToolpathSteps) {
			double x = getX(i, 0);
			// system.out.printf("getBottomRailRightCut() Move to next cut: %d step: %d\n",
			// i+1, j-mNrOfLengthSplits);
			getMoveBetweenCuts(x, mBottomAngle, mBottomRailAngle, i
					* mMoveToNextToolpathSteps + j, mBottomRailCuts
					* mMoveToNextToolpathSteps, false, false, true);
		} else {
			double x = getX(i, j - mMoveToNextToolpathSteps);
			// system.out.printf("getBottomRailRightCut() Cut: %d x: %f\n", i+1,
			// x);
			mPoint = getSurfacePoint(x, mBottomAngle, mBottomRailAngle, i + 1,
					mBottomRailCuts);
			mNormal = getSurfaceNormal(x, mBottomAngle, mBottomRailAngle,
					i + 1, mBottomRailCuts);
		}
		flip();
	}

	protected void getBottomOutlineRightCut() {
		getDeckOutlineRightCut();

		mPoint.z = -mPoint.z;
	}

	protected void getBottomRailCleanupRightCut() {
		double x = getX(i, j);
		// system.out.printf("getBottomRailRightCut() Cut: %d x: %f\n", i+1, x);
		mPoint = getSurfacePoint(x, mBottomAngle, mBottomRailAngle, 1, 1);
		mNormal = getSurfaceNormal(x, mBottomAngle, mBottomRailAngle, 1, 1);

		mPoint.z = -mPoint.z;

		mPoint.y += 1.0 * (i + 1);
	}

	protected void getBottomLeftCut() {
		// system.out.printf("getBottomLeftCut() Cut: %d x: %f\n", i+1, x);
		getBottomRightCut();

		mirrorY();
	}

	protected void getBottomRailLeftCut() {
		// system.out.printf("getBottomRailLeftCut() Cut: %d x: %f\n", i+1, x);
		getBottomRailRightCut();

		mirrorY();
	}

	protected void getBottomOutlineLeftCut() {
		getDeckOutlineLeftCut();

		mPoint.z = -mPoint.z;
	}

	protected void getBottomRailCleanupLeftCut() {
		// system.out.printf("getBottomRailCleanupLeftCut() Cut: %d x: %f\n",
		// i+1, x);
		getBottomRailCleanupRightCut();

		mirrorY();
	}

	protected void getBottomTailCutOff() {
		mPoint = mCurrentCutter.getTailCutOffPoint(j, mBoard, false);
		mNormal = mCurrentCutter.getTailCutOffNormal(j, mBoard, false);
	}

	protected void getBottomSideChange() {
		double x = mNoseToTail ? mLength - 0.1 : 0.1;

		if (mStayAwayFromStringer) {
			int stepsLeft = mBottomSideChangeSteps - j;

			if (stepsLeft >= 2) {
				double startAngle = (mBottomRailCuts > 0) ? mBottomRailAngle
						: mBottomAngle;
				if (startAngle < 90) {
					getMoveBetweenCuts(x, 90.0, startAngle,
							mBottomSideChangeSteps - j, mBottomSideChangeSteps,
							true, false, (mBottomRailCuts > 0) ? true : false);
				}
			} else {
				mNormal = new Vector3d(mNoseToTail ? 1.0 : -1.0, 0.0, 0.0);
				mPoint = new Point3d(mPoint);
				if (stepsLeft == 1) {
					mPoint.z = mPoint.z + 5.0;
				} else {
					mPoint.y = -mStringerWidth / 2;
				}

			}

		} else {
			getMoveBetweenCuts(x, BOTTOM_EXTREME_ANGLE,
					(mBottomRailCuts > 0) ? mBottomRailAngle : mBottomAngle,
					mBottomSideChangeSteps - j, mBottomSideChangeSteps, true,
					false, (mBottomRailCuts > 0) ? true : false);
		}
	}

	protected void getMoveBetweenCuts(double x, double minAngle,
			double maxAngle, int i, int cuts, boolean flip, boolean mirror,
			boolean useMinimumAngleOnSharpCorners) {
		System.out
				.printf("getMoveBetweenCuts() Step: %d/%d x:%f, minAngle:%f, maxAngle:%f,\n",
						i, cuts, x, minAngle, maxAngle);

		if (i > cuts)
			i = cuts;

		mPoint = getSurfacePoint(x, minAngle, maxAngle, i, cuts,
				useMinimumAngleOnSharpCorners);
		mNormal = getSurfaceNormal(x, minAngle, maxAngle, i, cuts,
				useMinimumAngleOnSharpCorners);

		System.out
				.printf("getMoveBetweenCuts() mPoint: %f, %f, %f mNormal: %f, %f, %f\n",
						mPoint.x, mPoint.y, mPoint.z, mNormal.x, mNormal.y,
						mNormal.z);

		// mPoint.add(mNormal); //Lift

		if (flip)
			flip();

		if (mirror)
			mirrorY();
	}

	protected void mirrorY() {
		mPoint.y = -mPoint.y;
		mNormal.y = -mNormal.y;
	}

	protected void flip() {
		mPoint.z = -mPoint.z;
		// mNormal.x = -mNormal.x;
		// mNormal.y = -mNormal.y;
		mNormal.z = -mNormal.z;
	}

	protected Point3d getSurfacePoint(double x, double minAngle,
			double maxAngle, int currentSplit, int totalSplits) {
		return mBrd.getSurfacePoint(x, minAngle, maxAngle, currentSplit,
				totalSplits);

	}

	protected Point3d getSurfacePoint(double x, double minAngle,
			double maxAngle, int currentSplit, int totalSplits,
			boolean useMinimumAngleOnSharpCorners) {
		// System.out.printf("getSurfacePoint(x:%f, minAngle:%f, maxAngle:%f, currentSplit:%d,  totalSplits:%d, useMinimumAngleOnSharpCorners:%s)\n",x,minAngle,maxAngle,currentSplit,totalSplits,
		// useMinimumAngleOnSharpCorners?"true":"false");
		return mBrd.getSurfacePoint(x, minAngle, maxAngle, currentSplit,
				totalSplits, useMinimumAngleOnSharpCorners);

	}

	protected Point3d getSurfacePoint(double x, double s) {
		return mBrd.getSurfacePoint(x, s);

	}

	protected Vector3d getSurfaceNormal(double x, double minAngle,
			double maxAngle, int currentSplit, int totalSplits) {

		// System.out.printf("getSurfaceNormal(double x:%f, double minAngle:%f, double maxAngle:%f, int currentSplit:%d, int totalSplits:%d)\n",
		// x, minAngle, maxAngle, currentSplit, totalSplits);

		if (x < 1.0)
			x = 1.0;
		if (x > mLength - 1.0)
			x = mLength - 1.0;

		return mBrd.getSurfaceNormal(x, minAngle, maxAngle, currentSplit,
				totalSplits);
	}

	protected Vector3d getSurfaceNormal(double x, double minAngle,
			double maxAngle, int currentSplit, int totalSplits,
			boolean useMinimumAngleOnSharpCorners) {
		if (x < 0.1)
			x = 0.1;
		if (x > mLength - 0.1)
			x = mLength - 0.1;

		System.out
				.printf("getSurfaceNormal(double x:%f, double minAngle:%f, double maxAngle:%f, int currentSplit:%d, int totalSplits:%d, boolean useMinimumAngleOnSharpCorners:%s)\n",
						x, minAngle, maxAngle, currentSplit, totalSplits,
						useMinimumAngleOnSharpCorners ? "true" : "false");

		return mBrd.getSurfaceNormal(x, minAngle, maxAngle, currentSplit,
				totalSplits, useMinimumAngleOnSharpCorners);
	}

	protected Vector3d getSurfaceNormal(double x, double s) {
		if (x < 1.0)
			x = 1.0;
		if (x > mLength - 1.0)
			x = mLength - 1.0;

		return mBrd.getSurfaceNormal(x, s);
	}

	/*
	 * public Point3d getNextBottomToolpathCoordinate() { /* //Check if all
	 * splits done if(i >= (bottomCuts+bottomRailCuts+bottomCleanUpCuts)*2+1) {
	 * //Reset values mLength = 0; mNrOfLengthSplits = 0; y = 0; i = 0; j = 0;
	 * 
	 * return null; }
	 * 
	 * if(mLength == 0) mLength = mBoard.getLength() - BezierPatch.ZERO;
	 * if(mNrOfLengthSplits == 0) mNrOfLengthSplits = (mLength/x_res);
	 * 
	 * //Do the bottom one side double x=BezierPatch.ZERO;
	 * 
	 * if(i%2 == 0) { x = j*x_res; } else { x = mLength - (j*x_res); }
	 * 
	 * if(x > mLength) { x = mLength; } if(x < BezierPatch.ZERO) { x =
	 * BezierPatch.ZERO; }
	 * 
	 * if(i < nrOfStringers) //Stringer { getPointAndVector(x,0.0,0.0,1,1); }
	 * else if((i-1%2) < bottomCuts) //bottom cuts {
	 * getPointAndVector(x,0.0,bottomAngle,i-1,bottomCuts); } else
	 * if((i-1%2)-bottomCuts < bottomRailCuts) {
	 * getPointAndVector(x,bottomAngle,
	 * bottomRailAngle,i-1-bottomCuts,bottomRailCuts); } else
	 * if((i-1%2)-bottomCuts-bottomRailCuts < bottomCleanUpCuts) {
	 * getPointAndVector(x,bottomAngle,bottomAngle,1,1); //Get the point at edge
	 * of the bottom definition mPoint.y = mPoint.y +
	 * i-1-bottomCuts-bottomRailCuts; //Adjust the cut to the bottom + 1 cm for
	 * each additional pass } else { return null; }
	 * 
	 * if(++j >= mNrOfLengthSplits) { j=0; i++; }
	 * 
	 * return mPoint;
	 * 
	 * return null; }
	 */
	@Override
	public Vector3d getToolpathNormalVector() {
		return mNormal;
	}

	public boolean isAtStringer() {
		return mIsCuttingStringer;
	}

	boolean isCuttingRail() {
		return mIsCuttingRail;
	}

	boolean isCuttingOutline() {
		return mIsCuttingOutline;
	}

	@Override
	public double calcSpeed(Point3d pos, Vector3d normal, AbstractBoard board,
			boolean isAtStringer) {
		double currentSpeed = mNormalSpeed;

		if (isAtStringer) {
			currentSpeed = mStringerSpeed;
		} else if (isCuttingRail()) {
			currentSpeed = mRailSpeed;
		} else if (isCuttingOutline()) {
			currentSpeed = mOutlineSpeed;
		}

		// Removed as suggested
		// if (pos.x < (mTailSpeedReductionDistance / 10.0)) {
		// double i = (pos.x / (mTailSpeedReductionDistance / 10.0));
		//
		// //
		// System.out.printf("SurfaceSplitToolpathGenerator::calcSpeed() at tail, i: %f",
		// // i);
		//
		// double tailSpeed = currentSpeed * mTailSpeedReduction;
		//
		// currentSpeed = currentSpeed * i + tailSpeed * (1.0 - i);
		//
		// } else if (pos.x > board.getLength()
		// - (mNoseSpeedReductionDistance / 10.0)) {
		// double i = (pos.x - (board.getLength() - (mNoseSpeedReductionDistance
		// / 10.0)))
		// / (mNoseSpeedReductionDistance / 10.0);
		//
		// //
		// System.out.printf("SurfaceSplitToolpathGenerator::calcSpeed() at nose, i: %f",
		// // i);
		//
		// double noseSpeed = currentSpeed * mNoseSpeedReduction;
		//
		// currentSpeed = currentSpeed * (1.0 - i) + noseSpeed * i;
		// }

		// System.out.printf("SurfaceSplitToolpathGenerator::calcSpeed() pos: %f, speed: %f\n",
		// pos.x, currentSpeed);

		return currentSpeed * 60; // Convert from mm/sec to mm/min
	}

	protected void addSurfaceLine(Point3d point) {
		if (isDeckCut()) {
			mMachineView.get3DView().addDeckSurfaceLine(point);
		} else {
			mMachineView.get3DView().addBottomSurfaceLine(point);
		}
	}

	protected void setSurfaceStart(Point3d point) {
		if (isDeckCut()) {
			mMachineView.get3DView().setDeckSurfaceStart(point);
		} else {
			mMachineView.get3DView().setBottomSurfaceStart(point);
		}
	}

	protected void addNormal(Point3d point, Vector3d vector) {
		if (isDeckCut()) {
			mMachineView.get3DView().addDeckNormal(point, vector);
		} else {
			mMachineView.get3DView().addBottomNormal(point, vector);
		}
	}

	protected void addToolpathLine(Point3d point) {
		if (isDeckCut()) {
			mMachineView.get3DView().addDeckToolpathLine(point);
		} else {
			mMachineView.get3DView().addBottomToolpathLine(point);
		}
	}

	protected void setToolpathStart(Point3d point) {
		if (isDeckCut()) {
			mMachineView.get3DView().setDeckToolpathStart(point);
		} else {
			mMachineView.get3DView().setBottomToolpathStart(point);
		}
	}

	protected boolean checkCollision(Point3d pos, AbstractBoard board) {

		// TODO: pseudo-implementation, implement the missing function to
		// provide a simpler way to avoid hitting the stringer
		// if(mStayAwayFromStringer &&
		// mCurrentCutter.checkCollisionWithStringer(pos))
		// {
		// double blankZ = isDeckCut()?mBlankholdingSystem.getBlankDeckAt(pos.x,
		// pos.y):mBlankholdingSystem.getBlankBottomAt(pos.x, pos.y);
		// double liftZ = blankZ + 25.0f; //One inch above blank;
		//
		// if(mLastToolpathPoint != null)
		// {
		// Point3d lift = new Point3d(mLastToolpathPoint);
		// if(lift.z < liftZ)
		// {
		// lift.z = liftZ;
		//
		// addToolpathLine(lift);
		// writeCoordinate(new double[]{lift.x, lift.y, lift.z});
		// }
		// }
		//
		// Point3d aboveStringerPos = new Point3d(pos);
		// aboveStringerPos.z = liftZ;
		// aboveStringerPos.sub(mCutterOffset);
		//
		// if (mLastToolpathPoint == null)
		// {
		// setToolpathStart(aboveStringerPos);
		// }
		// else
		// {
		// addToolpathLine(aboveStringerPos);
		// }
		//
		// writeCoordinate(new double[]{aboveStringerPos.x, aboveStringerPos.y,
		// aboveStringerPos.z});
		//
		// mLastToolpathPoint = aboveStringerPos;
		//
		// mPreviousCollisionWithStringer = true;
		//
		// }
		// else if(mPreviousCollisionWithStringer)
		// {
		// double blankZ = isDeckCut()?mBlankholdingSystem.getBlankDeckAt(pos.x,
		// pos.y):mBlankholdingSystem.getBlankBottomAt(pos.x, pos.y);
		// double liftZ = blankZ + 25.0f; //One inch above blank;
		//
		// Point3d aboveStringerPos = new Point3d(pos);
		// aboveStringerPos.z = liftZ;
		// aboveStringerPos.sub(mCutterOffset);
		//
		// if (mLastToolpathPoint == null)
		// {
		// setToolpathStart(aboveStringerPos);
		// }
		// else
		// {
		// addToolpathLine(aboveStringerPos);
		// }
		//
		// writeCoordinate(new double[]{aboveStringerPos.x, aboveStringerPos.y,
		// aboveStringerPos.z});
		//
		// mLastToolpathPoint = aboveStringerPos;
		//
		// mPreviousCollisionWithStringer = false;
		// }

		return mCurrentCutter.checkCollision(pos, board);
	}

	public Object clone() throws CloneNotSupportedException {

		SurfaceSplitsToolpathGenerator gen = (SurfaceSplitsToolpathGenerator) super
				.clone();

		gen.mBrd = (BezierBoard) mBrd.clone();

		return gen;
	}
}