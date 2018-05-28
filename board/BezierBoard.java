package board;
/*

 * Created on Sep 18, 2005

 *

 * To change the template for this generated file go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */



/**

 * @author Håvard

 *

 * To change the template for this generated type comment go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */

import cadcore.AbstractBezierBoardSurfaceModel;
import cadcore.BezierBoardCrossSection;
import boardcad.gui.jdk.BoardCAD;	//TODO: BAD DEPENDENCY
import boardcad.i18n.LanguageResource;
import cadcore.UnitUtils;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.BezierUtil;
import cadcore.MathUtils;
import cadcore.NurbsPoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class BezierBoard extends AbstractBoard implements Cloneable {

	Vector3d last;	//DEBUG

	static int VOLUME_X_SPLITS = 10;
	static int VOLUME_Y_SPLITS = 30;
	static int MASS_X_SPLITS = 10;
	static int MASS_Y_SPLIS = 10;
	public static int AREA_SPLITS = 10;

	protected String mVersion = new String();
	protected String mName = new String();
	protected String mAuthor = new String();
	protected String mDesigner = new String();
	protected String mBlankFile = new String();
	protected int mTopCuts = 0;
	protected int mBottomCuts = 0;
	protected int mRailCuts = 0;
	protected double mCutterDiam = 0;
	protected double mBlankPivot = 0;
	protected double mBoardPivot = 0;
	protected double mMaxAngle = 0;
	protected double mNoseMargin = 0;
	protected double mTailMargin = 0;
	protected double mNoseLength = 0;
	protected double mTailLength = 0;
	protected double mDeltaXNose = 0;
	protected double mDeltaXTail = 0;
	protected double mDeltaXMiddle = 0;
	protected int mToTailSpeed = 0;
	protected int mStringerSpeed = 0;
	protected int mRegularSpeed = 0;
	protected double[] mStrut1 = new double[3];
	protected double[] mStrut2 = new double[3];
	protected double[] mCutterStartPos = new double[3];
	protected double[] mBlankTailPos = new double[3];
	protected double[] mBoardStartPos = new double[3];
	protected int mCurrentUnits = 0;
	protected double mNoseRockerOneFoot = 0;
	protected double mTailRockerOneFoot = 0;
	protected boolean mShowOriginalBoard = true;
	protected int mStringerSpeedBottom = 0;
	protected String mMachineFolder = new String();
	protected double mTopShoulderAngle = 0;
	protected int mTopShoulderCuts = 0;
	protected int mBottomRailCuts = 0;
	protected String mSurfer = new String();
	protected String mComments = new String();
	protected double[] mFins = new double[9];	//x, y for back of fin, x,y for front of fin, bac of center, front of center, depth of center, depth of sidefin, splay angle
	protected String mFinType = new String();
	protected String  mDescription = new String();
	protected int mSecurityLevel = 0;
	protected String  mModel = new String();
	protected String  mAux1 = new String();
	protected String  mAux2 = new String();
	protected String  mAux3 = new String();

	protected BezierSpline mOutlineSpline = new BezierSpline();

	protected BezierSpline mDeckSpline = new BezierSpline();

	protected BezierSpline mBottomSpline = new BezierSpline();

	protected ArrayList<Point2D.Double> mOutlineGuidePoints = new ArrayList<Point2D.Double>();

	protected ArrayList<Point2D.Double> mDeckGuidePoints = new ArrayList<Point2D.Double>();

	protected ArrayList<Point2D.Double> mBottomGuidePoints = new ArrayList<Point2D.Double>();

	protected ArrayList<BezierBoardCrossSection> mCrossSections = new ArrayList<BezierBoardCrossSection>();

	private int mCurrentCrossSection = 1;

	private String mFilename = new String();
	
	private double mCenterOfMass = 0;
	
	private AbstractBezierBoardSurfaceModel.ModelType mInterpolationType = AbstractBezierBoardSurfaceModel.ModelType.ControlPointInterpolation;

	private Shape3D m3DModel = null;
	
	private int nr_of_bottom_ctrl_pts = 0;           // number of additional bottom control points for beizer patch creation of channels
	private boolean curved_channel_iterpolation = false; // interpolate channels relative to outline (TRUE) or linearly (FALSE)

	
	public BezierBoard()
	{
		reset();
	}
	
	public boolean isEmpty()
	{
		return (mOutlineSpline.getNrOfControlPoints() == 0);
	}
	

	public void set(BezierBoard brd)
	{
		mOutlineSpline = (BezierSpline)brd.mOutlineSpline.clone();

		mDeckSpline = (BezierSpline)brd.mDeckSpline.clone();

		mBottomSpline = (BezierSpline)brd.mBottomSpline.clone();

		mOutlineGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < brd.mOutlineGuidePoints.size(); i++)
		{
			mOutlineGuidePoints.add((Point2D.Double)brd.mOutlineGuidePoints.get(i).clone());
		}

		mDeckGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < brd.mDeckGuidePoints.size(); i++)
		{
			mDeckGuidePoints.add((Point2D.Double)brd.mDeckGuidePoints.get(i).clone());
		}

		mBottomGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < brd.mBottomGuidePoints.size(); i++)
		{
			mBottomGuidePoints.add((Point2D.Double)brd.mBottomGuidePoints.get(i).clone());
		}

		mCrossSections = new ArrayList<BezierBoardCrossSection>();
		for(int i = 0; i < brd.mCrossSections.size(); i++)
		{
			mCrossSections.add((BezierBoardCrossSection)brd.mCrossSections.get(i).clone());
		}

	}

	public void reset()
	{
		mOutlineSpline.clear();

		mDeckSpline.clear();

		mBottomSpline.clear();

		mOutlineGuidePoints.clear();

		mDeckGuidePoints.clear();

		mBottomGuidePoints.clear();

		mCrossSections.clear();

		mCurrentCrossSection = 1;

		mFilename = new String();


		mVersion = new String("V4.4");
		mName = new String();
		mAuthor = new String();
		mDesigner = new String();
		mBlankFile = new String();
		mTopCuts = 0;
		mBottomCuts = 0;
		mRailCuts = 0;
		mCutterDiam = 0;
		mBlankPivot = 0;
		mBoardPivot = 0;
		mMaxAngle = 0;
		mNoseMargin = 0;
		mTailMargin = 0;
		mNoseLength = 0;
		mTailLength = 0;
		mDeltaXNose = 0;
		mDeltaXTail = 0;
		mDeltaXMiddle = 0;
		mToTailSpeed = 0;
		mStringerSpeed = 0;
		mRegularSpeed = 0;
		mStrut1 = new double[3];
		mStrut2 = new double[3];
		mCutterStartPos = new double[3];
		mBlankTailPos = new double[3];
		mBoardStartPos = new double[3];
		mCurrentUnits = 0;
		mNoseRockerOneFoot = 0;
		mTailRockerOneFoot = 0;
		mShowOriginalBoard = true;
		mStringerSpeedBottom = 0;
		mMachineFolder = new String("c:\\machine");
		mTopShoulderAngle = 0;
		mTopShoulderCuts = 0;
		mBottomRailCuts = 0;
		mSurfer = new String();
		mComments = new String();
		mFins = new double[9];	//x, y for back of fin, x,y for front of fin, bac of center, front of center, depth of center, depth of sidefin, splay angle
		mFinType = new String();
		mDescription = new String();
		mSecurityLevel = 0;
		mModel = new String();
		mAux1 = new String();
		mAux2 = new String();
		mAux3 = new String();
		
		mCenterOfMass = 0;
		nr_of_bottom_ctrl_pts = 0;           // number of additional bottom control points for beizer patch creation of channels
		curved_channel_iterpolation = false; // interpolate channels relative to outline (TRUE) or linearly (FALSE)

	}
	
	
	
	public int getRailCuts() {
		return mRailCuts;
	}

	public void setRailCuts(int mRailCuts) {
		this.mRailCuts = mRailCuts;
	}

	public double getMaxAngle() {
		return mMaxAngle;
	}

	public void setMaxAngle(double mMaxAngle) {
		this.mMaxAngle = mMaxAngle;
	}

	public double getNoseMargin() {
		return mNoseMargin;
	}

	public void setNoseMargin(double mNoseMargin) {
		this.mNoseMargin = mNoseMargin;
	}

	public double getTailMargin() {
		return mTailMargin;
	}

	public void setTailMargin(double mTailMargin) {
		this.mTailMargin = mTailMargin;
	}

	public double getNoseLength() {
		return mNoseLength;
	}

	public void setNoseLength(double mNoseLength) {
		this.mNoseLength = mNoseLength;
	}

	public double getTailLength() {
		return mTailLength;
	}

	public void setTailLength(double mTailLength) {
		this.mTailLength = mTailLength;
	}

	public int getStringerSpeed() {
		return mStringerSpeed;
	}

	public void setStringerSpeed(int mStringerSpeed) {
		this.mStringerSpeed = mStringerSpeed;
	}

	public int getRegularSpeed() {
		return mRegularSpeed;
	}

	public void setRegularSpeed(int mRegularSpeed) {
		this.mRegularSpeed = mRegularSpeed;
	}

	public int getCurrentUnits() {
		return mCurrentUnits;
	}

	public void setCurrentUnits(int mCurrentUnits) {
		this.mCurrentUnits = mCurrentUnits;
	}

	public boolean getShowOriginalBoard() {
		return mShowOriginalBoard;
	}

	public void setShowOriginalBoard(boolean mShowOriginalBoard) {
		this.mShowOriginalBoard = mShowOriginalBoard;
	}

	public int getStringerSpeedBottom() {
		return mStringerSpeedBottom;
	}

	public void setStringerSpeedBottom(int mStringerSpeedBottom) {
		this.mStringerSpeedBottom = mStringerSpeedBottom;
	}

	public int getSecurityLevel() {
		return mSecurityLevel;
	}

	public void setSecurityLevel(int mSecurityLevel) {
		this.mSecurityLevel = mSecurityLevel;
	}

	public double getNoseRockerOneFoot() {
		return mNoseRockerOneFoot;
	}

	public void setNoseRockerOneFoot(double mNoseRockerOneFoot) {
		this.mNoseRockerOneFoot = mNoseRockerOneFoot;
	}

	public double getTailRockerOneFoot() {
		return mTailRockerOneFoot;
	}

	public void setTailRockerOneFoot(double mTailRockerOneFoot) {
		this.mTailRockerOneFoot = mTailRockerOneFoot;
	}

	public double[] getFins() {
		return mFins;
	}

	public void setFins(double[] mFins) {
		this.mFins = mFins;
	}
	
	public String getAuthor() {
		return mAuthor;
	}

	public void setSurfer(String surfer) {
		this.mSurfer = surfer;
	}

	public void setAuthor(String author) {
		this.mAuthor = author;
	}

	public void setInterpolationType(AbstractBezierBoardSurfaceModel.ModelType type)
	{
		mInterpolationType = type;
	}
	
	public AbstractBezierBoardSurfaceModel.ModelType getInterpolationType()
	{
		return mInterpolationType;
	}
	
	public String getVersion() {
		return mVersion;
	}

	public void setVersion(String mVersion) {
		this.mVersion = mVersion;
	}

	public String getDesigner() {
		return mDesigner;
	}

	public void setDesigner(String mDesigner) {
		this.mDesigner = mDesigner;
	}

	public String getBlankFile() {
		return mBlankFile;
	}

	public void setBlankFile(String mBlankFile) {
		this.mBlankFile = mBlankFile;
	}

	public int getTopCuts() {
		return mTopCuts;
	}

	public void setTopCuts(int mTopCuts) {
		this.mTopCuts = mTopCuts;
	}

	public int getBottomCuts() {
		return mBottomCuts;
	}

	public void setBottomCuts(int mBottomCuts) {
		this.mBottomCuts = mBottomCuts;
	}

	public double getCutterDiam() {
		return mCutterDiam;
	}

	public void setCutterDiam(double mCutterDiam) {
		this.mCutterDiam = mCutterDiam;
	}

	public double getBlankPivot() {
		return mBlankPivot;
	}

	public void setBlankPivot(double mBlankPivot) {
		this.mBlankPivot = mBlankPivot;
	}

	public double getBoardPivot() {
		return mBoardPivot;
	}

	public void setBoardPivot(double mBoardPivot) {
		this.mBoardPivot = mBoardPivot;
	}

	public double getDeltaXNose() {
		return mDeltaXNose;
	}

	public void setDeltaXNose(double mDeltaXNose) {
		this.mDeltaXNose = mDeltaXNose;
	}

	public double getDeltaXTail() {
		return mDeltaXTail;
	}

	public void setDeltaXTail(double mDeltaXTail) {
		this.mDeltaXTail = mDeltaXTail;
	}

	public double getDeltaXMiddle() {
		return mDeltaXMiddle;
	}

	public void setDeltaXMiddle(double mDeltaXMiddle) {
		this.mDeltaXMiddle = mDeltaXMiddle;
	}

	public int getToTailSpeed() {
		return mToTailSpeed;
	}

	public void setToTailSpeed(int mToTailSpeed) {
		this.mToTailSpeed = mToTailSpeed;
	}

	public double[] getStrut1() {
		return mStrut1;
	}

	public void setStrut1(double[] mStrut1) {
		this.mStrut1 = mStrut1;
	}

	public double[] getStrut2() {
		return mStrut2;
	}

	public void setStrut2(double[] mStrut2) {
		this.mStrut2 = mStrut2;
	}

	public double[] getCutterStartPos() {
		return mCutterStartPos;
	}

	public void setCutterStartPos(double[] mCutterStartPos) {
		this.mCutterStartPos = mCutterStartPos;
	}

	public double[] getBlankTailPos() {
		return mBlankTailPos;
	}

	public void setBlankTailPos(double[] mBlankTailPos) {
		this.mBlankTailPos = mBlankTailPos;
	}

	public double[] getBoardStartPos() {
		return mBoardStartPos;
	}

	public void setBoardStartPos(double[] mBoardStartPos) {
		this.mBoardStartPos = mBoardStartPos;
	}

	public String getMachineFolder() {
		return mMachineFolder;
	}

	public void setMachineFolder(String mMachineFolder) {
		this.mMachineFolder = mMachineFolder;
	}

	public double getTopShoulderAngle() {
		return mTopShoulderAngle;
	}

	public void setTopShoulderAngle(double mTopShoulderAngle) {
		this.mTopShoulderAngle = mTopShoulderAngle;
	}

	public int getTopShoulderCuts() {
		return mTopShoulderCuts;
	}

	public void setTopShoulderCuts(int mTopShoulderCuts) {
		this.mTopShoulderCuts = mTopShoulderCuts;
	}

	public int getBottomRailCuts() {
		return mBottomRailCuts;
	}

	public void setBottomRailCuts(int mBottomRailCuts) {
		this.mBottomRailCuts = mBottomRailCuts;
	}

	public String getComments() {
		return mComments;
	}

	public void setComments(String mComments) {
		this.mComments = mComments;
	}

	public String getFinType() {
		return mFinType;
	}

	public void setFinType(String mFinType) {
		this.mFinType = mFinType;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}

	public String getAux1() {
		return mAux1;
	}

	public void setAux1(String mAux1) {
		this.mAux1 = mAux1;
	}

	public String getAux2() {
		return mAux2;
	}

	public void setAux2(String mAux2) {
		this.mAux2 = mAux2;
	}

	public String getAux3() {
		return mAux3;
	}

	public void setAux3(String mAux3) {
		this.mAux3 = mAux3;
	}

	public void setDeckGuidePoints(ArrayList<Point2D.Double> mDeckGuidePoints) {
		this.mDeckGuidePoints = mDeckGuidePoints;
	}


	public void setCenterOfMass(double mCenterOfMass) {
		this.mCenterOfMass = mCenterOfMass;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	

	public BezierSpline getOutline()
	{
		return mOutlineSpline;
	}
	
	public void setOutline(BezierSpline outline)
	{
		mOutlineSpline = outline;
	}

	public BezierSpline getDeck()
	{
		return mDeckSpline;
	}
	
	public void setDeck(BezierSpline deck)
	{
		mDeckSpline = deck;
	}
	
	public BezierSpline getBottom()
	{
		return mBottomSpline;
	}

	public void setBottom(BezierSpline bottom)
	{
		mBottomSpline = bottom;
	}

	public ArrayList<Point2D.Double> getOutlineGuidePoints()
	{
		return mOutlineGuidePoints;
	}

	public ArrayList<Point2D.Double> getDeckGuidePoints()
	{
		return mDeckGuidePoints;
	}

	public ArrayList<Point2D.Double> getBottomGuidePoints()
	{
		return mBottomGuidePoints;
	}

	public ArrayList<BezierBoardCrossSection> getCrossSections()
	{
		return mCrossSections;
	}

	public void addCrossSection(BezierBoardCrossSection crossSection)
	{
		mCrossSections.add(crossSection);
		sortCrossSections();
	}

	public void removeCrossSection(BezierBoardCrossSection crossSection)
	{
		mCrossSections.remove(crossSection);
		sortCrossSections();
		while(mCurrentCrossSection >= mCrossSections.size()-1 && mCurrentCrossSection > 1)
		{
			mCurrentCrossSection--;
		}
	}

	public void sortCrossSections()
	{
		Collections.sort(mCrossSections);
	}

	public BezierBoardCrossSection getCurrentCrossSection()
	{
		if(mCurrentCrossSection < 1 || mCurrentCrossSection > mCrossSections.size() -2)
			return null;

		return mCrossSections.get(mCurrentCrossSection);
	}

	public int getCurrentCrossSectionIndex()
	{
		return mCurrentCrossSection;
	}

	public void setCurrentCrossSection(int crossSectionNr)
	{
		mCurrentCrossSection = crossSectionNr;
	}

	public void nextCrossSection()
	{
		if(++mCurrentCrossSection >= mCrossSections.size()-1)
			mCurrentCrossSection = 1;
	}

	public void previousCrossSection()
	{

		if(--mCurrentCrossSection <= 0)
			mCurrentCrossSection = mCrossSections.size()-2;
	}

	public BezierBoardCrossSection getNearestCrossSection(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);
		if(index == -1)
		{
			return null;
		}
		return mCrossSections.get(index);
	}

	public BezierBoardCrossSection getPreviousCrossSection(double pos)
	{
		return mCrossSections.get(getPreviousCrossSectionIndex(pos));
	}

	public BezierBoardCrossSection getNextCrossSection(double pos)
	{
		return mCrossSections.get(getNextCrossSectionIndex(pos));
	}

	public double getPreviousCrossSectionPos(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);

		if(getCrossSections().get(index).getPosition() >= pos)
		{
			index -= 1;
		}
		
		return mCrossSections.get(index).getPosition();
	}

	public double getNextCrossSectionPos(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);

		if(getCrossSections().get(index).getPosition() < pos)
		{
			index += 1;
		}
		
		return mCrossSections.get(index).getPosition();
	}

	public int getNearestCrossSectionIndex(double pos)
	{
		int nearest = -1;
		double nearestPos = -300000.0;

		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection current = mCrossSections.get(i);

			if(nearest == -1 || Math.abs(nearestPos - pos) > Math.abs(current.getPosition() - pos))
			{
				nearest = i;
				nearestPos = current.getPosition();
			}

		}

		return nearest;

	}

	public int getPreviousCrossSectionIndex(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);
		
		if(getCrossSections().get(index).getPosition() >= pos)
		{
			index -= 1;
		}


		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		if(index==0)
			index = 1;
		
		if(index > getCrossSections().size() - 2)
			index = getCrossSections().size();

		return index;
	}

	public int getNextCrossSectionIndex(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);
		
		if(getCrossSections().get(index).getPosition() < pos)
		{
			index += 1;
		} 


		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		if(index==0)
			index = 1;
		
		if(index > getCrossSections().size() - 2)
			index = getCrossSections().size() - 2;

		return index;
	}

	public double getLength() {

		double length = 0;

		for(int i = 0; i < mOutlineSpline.getNrOfControlPoints(); i++) {

			double x = mOutlineSpline.getControlPoint(i).getEndPoint().x;

			if(x > length) {

				length = x;

			}

		}

		return length;

	}

	public double getLengthOverCurve() {

		return mBottomSpline.getLength();

	}


	public double getCenterWidth() {

		return getWidthAtPos(getLength()/2);

	}

	public double getMaxWidth() {

		return mOutlineSpline.getMaxY()*2.0;

	}
	public double getMaxWidthPos() {

		return mOutlineSpline.getXForMaxY();

	}

	public double getThickness()
	{
		return getThicknessAtPos(getLength()/2.0);
	}
	
	public double getMaxThickness()
	{
		double max = -100000;
		double maxPos = -100000;
		for(int i = 0; i < Math.floor(getLength()*10); i++)  //in order to have it computed every millimeter
		{
			double posi = (double)i/10;
			double current = getThicknessAtPos(posi);
			if(current > max){
				maxPos = posi;
				max = current;
			}
		}
		return max;
	}
	
	public double getMaxThicknessPos() 
	{
		double max = -100000;
		double maxPos = -100000;
		for(int i = 0; i < Math.floor(getLength()*10); i++)  //in order to have it computed every millimeter
		{
			double posi = (double)i/10;
			double current = getThicknessAtPos(posi);
			if(current > max){
				maxPos = posi;
				max = current;
			}
		}
		return maxPos;
	}
	
	public double getMaxRocker()
	{
		return mBottomSpline.getMaxY();
	}
	
	public Point2d getMaxDeckAtTailPos()
	{
		BezierSpline deck = getDeck();
		
		double x = deck.getXForMaxYInRange(0.0, UnitUtils.FOOT);
		double y = deck.getMaxYInRange(0.0, UnitUtils.FOOT);		
		
		return new Point2d(x,y);		
	}
	
	public Point2d getMaxDeckAtNosePos()
	{
		BezierSpline deck = getDeck();
		
		double x = deck.getXForMaxYInRange(getLength() - UnitUtils.FOOT, getLength());
		double y = deck.getMaxYInRange(getLength() - UnitUtils.FOOT, getLength());		
		
		return new Point2d(x,y);				
	}

	public double getWidthAtPos(double pos)
	{
		return mOutlineSpline.getValueAt(pos)*2;
	}

	public double getRockerAtPos(double pos)
	{
		return mBottomSpline.getValueAt(pos);
	}

	public double getDeckAtPos(double pos)
	{
		return mDeckSpline.getValueAt(pos);
	}

	public double getThicknessAtPos(double pos)
	{
		return getDeckAtPos(pos) - getRockerAtPos(pos);
	}
	
	public double getThicknessAtPos(double x, double y)
	{
		return getDeckAt(x,y) - getBottomAt(x,y);
	}
	

	public double getFromTailOverBottomCurveAtPos(double pos)
	{
		return mBottomSpline.getLengthByX(pos);
	}
		
	public double getFromNoseOverBottomCurveAtPos(double pos)
	{
		return mBottomSpline.getLength() - mBottomSpline.getLengthByX(pos);
	}

	public double getXFromTailByOverBottomCurveLength(double length)
	{
		return mBottomSpline.getPointByCurveLength(length).x;
	}

	public double getDeckAtPos(double x, double y)
	{
		
/*
		//Calculate scales
		double widthAtPos = getWidthAtPos(x);
		double thicknessAtPos = getThicknessAtPos(x);
		
		//Get the position from function since we cheat with the crosssections at tip and tail
		double pos1 = getPreviousCrossSectionPos(x);
		double pos2 = getNextCrossSectionPos(x);

		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		BezierBoardCrossSection c1 = getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = getNextCrossSection(x);

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

		return z;
*/
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getDeckAt(this,x, y).getZ();
	}

	public double getBottomAtPos(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getBottomAt(this,x, y).getZ();

	}

	//Point on crosssection without rocker added
	public Point2D.Double getPointAtPos(double x, double s)
	{
		Point2D.Double pos = getSurfacePointAtPos(x,s);
		
		return new Point2D.Double(pos.x,pos.y-getRockerAtPos(x));
	}

	public Point2D.Double getSurfacePointAtPos(double x, double s)
	{
		Point3d pos = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getPointAt(this, x, s, -90.0, 360.0, true);
		
		return new Point2D.Double(pos.y,pos.z);
		
	}

	
	public Vector3d getDeckNormalAt(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getBottomNormalAt(this,x, y);
	}
	
	public Vector3d getBottomNormalAt(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getDeckNormalAt(this,x, y);

	}

	public Vector3d getNormalAtPos(double x, double s)
	{
		final double OFFSET = 0.001;
		final double S_OFFSET = 0.001;
		Point2D.Double o = getSurfacePointAtPos(x,s);
		
		Point2D.Double cp = getSurfacePointAtPos(x,s-S_OFFSET);
		Vector3d cv = new Vector3d(x, cp.x-o.x, cp.y-o.y);
		
		Point2D.Double lp = getSurfacePointAtPos(x-OFFSET,s);
		Vector3d lv = new Vector3d(x, lp.x-o.x, lp.y-o.y);
		
		Vector3d normalVec = new Vector3d();
		normalVec.cross(cv,lv);
		normalVec.normalize();
		
		return normalVec;
	}

	public Vector3d getSurfaceNormalAtPos(double x, double s)
	{
		final double OFFSET = 0.001;
		final double S_OFFSET = 0.001;
		Point2D.Double o = getSurfacePointAtPos(x,s);
		
		Point2D.Double cp = getSurfacePointAtPos(x,s-S_OFFSET);
		Vector3d cv = new Vector3d(x, cp.x-o.x, cp.y-o.y);
		
		Point2D.Double lp = getSurfacePointAtPos(x-OFFSET,s);
		Vector3d lv = new Vector3d(x, lp.x-o.x, lp.y-o.y);
		
		Vector3d normalVec = new Vector3d();
		normalVec.cross(cv,lv);
		normalVec.normalize();
		
		return normalVec;
	}
	
	public Point3d getSurfacePoint(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits)
	{
		return getSurfacePoint(x, minAngle, maxAngle, currentSplit, totalSplits, true);
	}

	public Point3d getSurfacePoint(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits, boolean useMinimumAngleOnSharpCorners)
	{
		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getPointAt(this,x, (double)currentSplit/(double)totalSplits, minAngle, maxAngle, useMinimumAngleOnSharpCorners);
		return point;
	}

	public Point3d getSurfacePoint(double x, double s)
	{
		Point3d point = null;

		point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getPointAt(this,x, s, -360.0, 360.0, true);
		return point;
		
	}

	public Vector3d getSurfaceNormal(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits)
	{		
		return 	getSurfaceNormal(x, minAngle, maxAngle, currentSplit, totalSplits, true);

	}
	
	public Vector3d getSurfaceNormal(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits, boolean useMinimumAngleOnSharpCorners )
	{		
		
		Vector3d normal = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getNormalAt(this,x, (double)currentSplit/(double)totalSplits, minAngle, maxAngle, useMinimumAngleOnSharpCorners);
		return normal;

	}

	public Vector3d getSurfaceNormal(double x, double s)
	{		
		Vector3d normal = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getNormalAt(this,x, s, -360.0, 360.0, true);
		return normal;
	}

	
	public Vector3d getTangentAtPos(double x, double sa, double sb)
	{
		//Get the position first since we cheat with the crosssections at tip and tail
		double pos1 = getPreviousCrossSectionPos(x);
		double pos2 = getNextCrossSectionPos(x);

		//Get crosssections but use the first and last real crosssections position if we're at the dummy crosssections at nose and tail
		BezierBoardCrossSection c1 = getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = getNextCrossSection(x);


		double a1 = c1.getBezierSpline().getTangentByS(sa);
		double a2 = c2.getBezierSpline().getTangentByS(sb);

		//Get blended point
		double p = (x - pos1)/(pos2 - pos1);

		double a  = ((1-p)*a1) + (p*a2);

		Vector3d ret = new Vector3d(0,Math.sin(a),Math.cos(a)); 
		
		return ret;
	}

	public BezierBoardCrossSection getInterpolatedCrossSection(double x)
	{
		if(getCrossSections().size() == 0)
			return null;
		
		if(x < 0)
			return null;
		
		if(x > getLength())
			return null;
		
		int index = getNearestCrossSectionIndex(x);
		if(getCrossSections().get(index).getPosition() > x)
		{
			index -= 1;
		}			
		int nextIndex = index+1;
		
		double firstCrossSectionPos = getCrossSections().get(index).getPosition();
		double secondCrossSectionPos = getCrossSections().get(nextIndex).getPosition();
				
		//Calculate t
		double t = (x - firstCrossSectionPos) / (secondCrossSectionPos - firstCrossSectionPos);
		if(Double.isInfinite(t) || Double.isNaN(t))
		{
			t = 0.0;
		}
		
		if(index < 1)
		{
			index = 1;
		}
		if(nextIndex > mCrossSections.size()-2)
		{
			index = mCrossSections.size()-2;
			nextIndex = index;
		}

		BezierBoardCrossSection c1 = getCrossSections().get(index);
		BezierBoardCrossSection c2 = getCrossSections().get(nextIndex);

		BezierBoardCrossSection i = c1.interpolate(c2, t);
		if(i != null)
		{
			//Calculate scale
			double thickness = getThicknessAtPos(x); 
			if(thickness < 0.5)
				thickness = 0.5;
				
			double width = getWidthAtPos(x);
			if(width < 0.5)
				width = 0.5;
			
			i.scale(thickness, width);
			
			i.setPosition(x);
			// Perform Concave Adjustment Here (if curved interpolation requested)
			int NumberOfConcavePts = 0;

			int lowXSInd = 0;
			int highXSInd = 0;
			double m_slope = 0;
			double C_intercept = 0;

			int j = 1;
			if (!curved_channel_iterpolation){
				NumberOfConcavePts=nr_of_bottom_ctrl_pts;
			}
			while (j <= NumberOfConcavePts)
			{
				//System.out.println("CC Pt "+j);
				BezierSpline ConcavePtPathSpline = new BezierSpline();
				Point2D.Double[] ConcavePtArray = new Point2D.Double[mCrossSections.size()-2];
				//create array of points for temp bezier spline
				//
				//System.out.println("NumXsects "+ mCrossSections.size());
				for(int k = 0; k < mCrossSections.size() - 2; k++)
				{
					ConcavePtArray[k] = new Point2D.Double();
					ConcavePtArray[k].x = mCrossSections.get(k+1).getPosition();
					ConcavePtArray[k].y = mCrossSections.get(k+1).getBezierSpline().getControlPoint(j).getPoints()[0].x;
					//System.out.println(mCrossSections.get(k).getPosition()+", "+mCrossSections.get(k).getBezierSpline().getControlPoint(j).getPoints()[0].x);
				}
			
			if (x <= ConcavePtArray[0].x) // cross section between tail and first cross section
			{
				lowXSInd = 0;
				highXSInd = 1;
			}
			else if (x >= ConcavePtArray[mCrossSections.size() - 3].x) // cross section between nose and last cross section
			{
				lowXSInd = mCrossSections.size() - 4;
				highXSInd = mCrossSections.size() - 3;	
			}
			else // cross section is within user defined cross sections
			{
				// determine which cross sections the interpolated point is between
				int k = 0;
				while (x > ConcavePtArray[k].x)
				{
					k++;
				}
				lowXSInd = k-1;
				highXSInd = k;
			}
			m_slope = ((ConcavePtArray[highXSInd].y - ConcavePtArray[lowXSInd].y) / (ConcavePtArray[highXSInd].x - ConcavePtArray[lowXSInd].x));
			C_intercept = ConcavePtArray[highXSInd].y - m_slope * ConcavePtArray[highXSInd].x;
			if (x <= ConcavePtArray[mCrossSections.size() - 3].x)
			{
				i.getBezierSpline().getControlPoint(j).setControlPointLocation(m_slope*x+C_intercept, i.getBezierSpline().getControlPoint(j).getPoints()[0].y);
			}
			j++;
			}
			
		}
		
		return i;
	}
	
	public double getArea()
	{
	    final MathUtils.Function f = new MathUtils.Function(){public double f(double x){return getWidthAtPos(x);}};
		
		double newInt =  MathUtils.Integral.getIntegral(f, BezierSpline.ZERO, getLength()-BezierSpline.ZERO, AREA_SPLITS);
				
		return newInt;
	}

	public double getVolume()
	{
		if(getCrossSections().size() < 3)
			return 0;
		
		double a = 0.01;
		
		double b = getLength() - 0.01;

		MathUtils.Function crsSecAreaFunc = new MathUtils.Function(){public double f(double x){return getCrossSectionAreaAt(x,VOLUME_X_SPLITS);}};
		
		double volume =  MathUtils.Integral.getIntegral(crsSecAreaFunc, a, b, VOLUME_Y_SPLITS);
				
		return volume;
	}
	
	public double getMomentOfInertia(double x, double y)
	{
		if(getCrossSections().size() < 3)
			return 0;
		
		double a = 0.01;
		
		double b = getLength() - 0.01;

		MathUtils.Function crsSecAreaFunc = new MathUtils.Function(){public double f(double x){return getCrossSectionAreaAt(x,VOLUME_X_SPLITS);}};
		
		//System.out.println("--------------");
		
		double pos = a;
		double pos_step = (b-a)/VOLUME_Y_SPLITS;
		double density = 3.0/30;	//3kg board of 30 liters is a good estimate for a modern performance board
		
		double momentOfInertia = 0;
		for(int i = 0; i < VOLUME_Y_SPLITS; i++)
		{
			double volume =  MathUtils.Integral.getIntegral(crsSecAreaFunc, pos, pos+pos_step, 1);
			volume /= UnitUtils.CUBICCENTIMETER_PR_LITRE;
			
			double dx = pos+(pos_step/2) - x;
			double dy = y;
			double r = Math.sqrt((dx*dx) + (dy*dy));
			r/=UnitUtils.CENTIMETER_PR_METER;	//To get the unit right kgm2
			
			momentOfInertia += volume*density*r*r;

			//System.out.printf("x:%f y:%f dx:%f, dy:%f r: %f volume:%f\n",x,y,dx,dy,r, volume);
			
			pos += pos_step;
		}

		return momentOfInertia;
	}
	
	
	public double getCenterOfMass()
	{
		if(mCenterOfMass != 0)
			return mCenterOfMass;
		
		double momentSum = 0.0;
		double weightSum = 0.0;
		
		if(getCrossSections().size() < 3)
			return 0.0;
		
		
		double a = 0.01;
		
		double b = getLength() - 0.01;
		
		double step = (b-a)/MASS_Y_SPLIS;
		
		double an = a;
		
		double x0 = getCrossSectionAreaAt(an, MASS_X_SPLITS);

		for(int i = 0; i < MASS_Y_SPLIS; i++)
		{

			double x1 = getCrossSectionAreaAt(an+(step/2), MASS_X_SPLITS);
			double x2 = getCrossSectionAreaAt(an+step, MASS_X_SPLITS);
			
			if(Double.isNaN(x0))
			{
				x0 = 0;
			}
			if(Double.isNaN(x1))
			{
				x1 = 0;
			}
			if(Double.isNaN(x2))
			{
				x2 = 0;
			}

			double integral = (step/6)*(x0 + (4*x1) + x2);

			momentSum += (an+(step/2))*integral;
			weightSum += integral;

			an += step;	
			
			x0 = x2;
		}
		mCenterOfMass = momentSum/weightSum;
		return mCenterOfMass;

	}
	
	public double getCrossSectionAreaAt(final double pos, final int splits)
	{
		double area = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getCrosssectionAreaAt(this,pos, splits);
		
		return area;
	}
	
	public double getWidthAt(double x)
	{
		return getWidthAtPos(x);
	}
	
	public double getDeckAt(double x, double y)
	{
		if(getWidthAt(x)/2.0 < y)
			return 0.0;
		
		if(y == 0.0)
		{
			double z = getDeck().getValueAt(x);
			return z;
		}
		
		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getDeckAt(this,x, y);
		return point.z;

	}
	
	public double getBottomAt(double x, double y)
	{
		if(getWidthAt(x)/2.0 < y)
			return 0.0;

		if(y == 0.0)
		{
			double z = getBottom().getValueAt(x);
			return z;
		}

		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getBottomAt(this,x, y);
		return point.z;

	}

	public void onRockerChanged()
	{
		mCenterOfMass = 0;
		
		if(BoardCAD.getInstance().isUsingRockerStickAdjustment())
		{
			adjustRockerToCenterTangent();			
		}
		
		adjustRockerToZero();

		adjustCrosssectionsToThicknessAndWidth();
	}

	public void onOutlineChanged()
	{
		mCenterOfMass = 0;
		adjustCrosssectionsToThicknessAndWidth();
	}

	public void onCrossSectionChanged()
	{
		mCenterOfMass = 0;
		adjustCrosssectionsToThicknessAndWidth();
	}

	void adjustCrosssectionsToThicknessAndWidth()
	{
		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection current = mCrossSections.get(i);
			double currentPos = current.getPosition();
			current.scale(getThicknessAtPos(currentPos), getWidthAtPos(currentPos));
		}
	}

	void adjustRockerToCenterTangent()
	{
		double tangentAngle = mBottomSpline.getTangentByS(.5);	//Get tangent at center

		double sin = Math.sin(Math.PI - tangentAngle);
		double cos = Math.cos(Math.PI - tangentAngle);
		
//DEBUG		System.out.printf("Brd.adjustRockerToCenterTangent() angle: %f sin: %f cos: %f\n", tangentAngle*180/Math.PI, sin, cos);

		double x=0;
		double y=0;
		BezierKnot current = null;
		
		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			current = mBottomSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				x = points[j].x;
				y = points[j].y;
				
				points[j].x = x*sin + y*cos;
				points[j].y = x*cos + y*sin;
			}
		}

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			current = mDeckSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				x = points[j].x;
				y = points[j].y;
				
				points[j].x = x*sin + y*cos;
				points[j].y = x*cos + y*sin;
			}
		}
		
		//Tail may be a bit off the end so move the rocker
		double fromZero = mBottomSpline.getControlPoint(0).getEndPoint().x;
		
		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			current = mBottomSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].x = points[j].x-fromZero;
			}
		}

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			current = mDeckSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].x = points[j].x-fromZero;
			}
		}
		
		//Scale so length of rocker matches outline
		double rockerLength = mBottomSpline.getMaxX();
		double brdLength = getLength();
		
		mBottomSpline.scale(1.0, brdLength/rockerLength);
		mDeckSpline.scale(1.0, brdLength/rockerLength);
		
	}

	void adjustRockerToZero()
	{
		double min = mBottomSpline.getMinY();

		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			BezierKnot current = mBottomSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].y -= min;
			}
		}

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			BezierKnot current = mDeckSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].y -= min;
			}
		}
	}

	public void scale(double newLength, double newWidth, double newThickness)
	{
		double lengthScale = newLength/getLength();
		double widthScale = newWidth/getMaxWidth();
		double thicknessScale = newThickness/getMaxThickness();

		mOutlineSpline.scale(widthScale, lengthScale);
		mDeckSpline.scale(thicknessScale, lengthScale);		
		//problem...from here the value of BoardCAD.getInstance().getCurrentBrd().getMaxThickness() is not what the user asked
		
		mBottomSpline.scale(thicknessScale, lengthScale);

		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection cs = mCrossSections.get(i);
			cs.setPosition(cs.getPosition()*lengthScale);
		}
		mCrossSections.get(mCrossSections.size()-1).setPosition(newLength);

		adjustCrosssectionsToThicknessAndWidth();
	}

	public void scaleAccordingly(double newLength, double newWidth, double newThickness)
	{
		double lengthScale = newLength/getLength();
		double widthScale = newWidth/getMaxWidth();
		double thicknessScale = newThickness/getMaxThickness();
		double thicknessDiff = newThickness - getMaxThickness();
		
		double maxThicknessPos = getMaxThicknessPos();

		ArrayList<Double> thicknesses = new ArrayList<Double>();
		for(int i = 1; i < mDeckSpline.getNrOfControlPoints()-1; i++)
		{
			BezierKnot point = mDeckSpline.getControlPoint(i);
			
			double x = point.getEndPoint().x;
			if(x < BezierSpline.ZERO)
			{
				x = BezierSpline.ZERO;
			}
			if(x > getLength() - BezierSpline.ZERO)
			{
				x = getLength() - BezierSpline.ZERO;
			}
			double thickness = point.getEndPoint().y - mBottomSpline.getValueAt(x);
			System.out.printf("x:%f, Thickness: %f\n", point.getEndPoint().x, thickness);
			
			thicknesses.add(thickness);
		}
		
		mOutlineSpline.scale(widthScale, lengthScale);
		mDeckSpline.scale(1.0, lengthScale);		
//problem...from here the value of BoardCAD.getInstance().getCurrentBrd().getMaxThickness() is not what the user asked
		
		mBottomSpline.scale(lengthScale, lengthScale);

		double angle = Math.atan2(thicknessDiff, maxThicknessPos);
	//	System.out.printf("Angle:%f\n", angle);

		for(int i = 1; i < mDeckSpline.getNrOfControlPoints()-1; i++)
		{
			BezierKnot point = mDeckSpline.getControlPoint(i);
			
			double x = point.getEndPoint().x;
			if(x < BezierSpline.ZERO)
			{
				x = BezierSpline.ZERO;
			}
			if(x > getLength() - BezierSpline.ZERO)
			{
				x = getLength() - BezierSpline.ZERO;
			}

			double thickness = thicknesses.get(i-1);
			
			double targetThickness = thickness*thicknessScale;
			
			double actualThickness = point.getEndPoint().y - mBottomSpline.getValueAt(x);
			
			double dy = targetThickness - actualThickness;

//			System.out.printf("x:%f, Thickness:%f, thicknessScale:%f, targetThickness:%f, actualThickness:%f, dy:%f\n", x, thickness, thicknessScale, targetThickness, actualThickness, dy);

			point.setControlPointLocation(point.getEndPoint().x, point.getEndPoint().y + dy);
			
			
			double usedAngle = angle*((maxThicknessPos-x)/maxThicknessPos);
			
//			System.out.printf("usedAngle:%f\n", usedAngle);

			point.setTangentToNextAngle(point.getTangentToNextAngle() + usedAngle);
			point.setTangentToPrevAngle(point.getTangentToPrevAngle() + usedAngle);
		}

		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection cs = mCrossSections.get(i);
			cs.setPosition(cs.getPosition()*lengthScale);
		}
		mCrossSections.get(mCrossSections.size()-1).setPosition(newLength);

		adjustCrosssectionsToThicknessAndWidth();
	}

	public void finScaling(double straightXRatio, double YRatio)
	{
		mFins[0]=mFins[0]*straightXRatio;
		mFins[2]=mFins[2]*straightXRatio;
		mFins[4]=mFins[4]*straightXRatio;
		mFins[5]=mFins[5]*straightXRatio;
		
		mFins[1]=mFins[1]*YRatio;    
		mFins[3]=mFins[3]*YRatio;
	}
	
	
	public void setLocks()
	{
		if(mOutlineSpline.getNrOfControlPoints() < 2)
			return;

//		Set masks
		mOutlineSpline.getControlPoint(0).setMask(0,0);
		mOutlineSpline.getControlPoint(mOutlineSpline.getNrOfControlPoints()-1).setMask(0,0);

		mDeckSpline.getControlPoint(0).setMask(0,1.0f);
		mDeckSpline.getControlPoint(mDeckSpline.getNrOfControlPoints()-1).setMask(0,1.0f);

		mBottomSpline.getControlPoint(0).setMask(0,0);

		mBottomSpline.getControlPoint(0).setMask(0,1.0f);
		mBottomSpline.getControlPoint(mBottomSpline.getNrOfControlPoints()-1).setMask(0,1.0f);

		for(int i = 0; i < mCrossSections.size(); i++)
		{
			mCrossSections.get(i).getBezierSpline().getControlPoint(0).setMask(0,0);
			mCrossSections.get(i).getBezierSpline().getControlPoint(mCrossSections.get(i).getBezierSpline().getNrOfControlPoints()-1).setMask(0,0);
		}

//		Set slaves
		mDeckSpline.getControlPoint(0).setSlave(mBottomSpline.getControlPoint(0));
		mDeckSpline.getControlPoint(mDeckSpline.getNrOfControlPoints()-1).setSlave(mBottomSpline.getControlPoint(mBottomSpline.getNrOfControlPoints()-1));

		mBottomSpline.getControlPoint(0).setSlave(mDeckSpline.getControlPoint(0));
		mBottomSpline.getControlPoint(mBottomSpline.getNrOfControlPoints()-1).setSlave(mDeckSpline.getControlPoint(mDeckSpline.getNrOfControlPoints()-1));

//		Set locks
		for(int i = 0; i < mOutlineSpline.getNrOfControlPoints(); i++)
		{
			mOutlineSpline.getControlPoint(i).setTangentToPrevLocks(BezierKnot.LOCK_X_LESS);
			mOutlineSpline.getControlPoint(i).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
		}
		mOutlineSpline.getControlPoint(0).addTangentToNextLocks(BezierKnot.LOCK_Y_MORE);
		mOutlineSpline.getControlPoint(mOutlineSpline.getNrOfControlPoints()-1).addTangentToPrevLocks(BezierKnot.LOCK_Y_MORE);

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			mDeckSpline.getControlPoint(i).setTangentToPrevLocks(BezierKnot.LOCK_X_LESS);
			mDeckSpline.getControlPoint(i).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
		}

		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			mBottomSpline.getControlPoint(i).setTangentToPrevLocks(BezierKnot.LOCK_X_LESS);
			mBottomSpline.getControlPoint(i).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
		}

		for(int i = 0; i < mCrossSections.size(); i++)
		{
			mCrossSections.get(i).getBezierSpline().getControlPoint(0).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
			mCrossSections.get(i).getBezierSpline().getControlPoint(mCrossSections.get(i).getBezierSpline().getNrOfControlPoints()-1).setTangentToPrevLocks(BezierKnot.LOCK_X_MORE);
		}
	}
	
	public void checkAndFixContinousy(boolean fixShouldBeCont, boolean fixShouldNotBeCont)
	{
		checkAndFixContinousy(getOutline(), fixShouldBeCont, fixShouldNotBeCont);
		checkAndFixContinousy(getBottom(), fixShouldBeCont, fixShouldNotBeCont);
		checkAndFixContinousy(getDeck(), fixShouldBeCont, fixShouldNotBeCont);
		
		for(int i = 0; i < getCrossSections().size(); i++)
		{
			checkAndFixContinousy(getCrossSections().get(i).getBezierSpline(), fixShouldBeCont, fixShouldNotBeCont);			
		}
		
	}

	public void checkAndFixContinousy(BezierSpline patch, boolean fixShouldBeCont, boolean fixShouldNotBeCont)
	{
		for(int i = 0; i < patch.getNrOfControlPoints(); i++)
		{
			BezierKnot ControlPoint = patch.getControlPoint(i);
			
			double pta = ControlPoint.getTangentToPrevAngle();
			double nta = ControlPoint.getTangentToNextAngle();

			boolean cont = (Math.abs(Math.abs(Math.PI -pta)-nta) < 0.02)?true:false;	//0.02 is roughly one degree
			
			if(cont && fixShouldBeCont)
			{
				ControlPoint.setContinous(cont);
			}
			
			if(!cont && fixShouldNotBeCont)
			{
				ControlPoint.setContinous(cont);
			}
		}
	}

	public String getFilename()
	{
		return mFilename;
	}
	
	public void setModel(String model)
	{
		 mModel = model;
	}
	
	public String getModel()
	{
		return mModel;
	}

	public String getSurfer()
	{
		return mSurfer;
	}

	public void setFilename(String filename)
	{
		mFilename = filename;
	}

	boolean deckCollisionTest(Point3d aabbCenter, double width, double depth, double height)
	{
		if(aabbCenter.z - (height/2.0) > getMaxRocker())
			return false;

		if(aabbCenter.y - (width/2.0) > getMaxWidth())
				return false;
		
		if(aabbCenter.x - (depth/2.0) > getLength())
			return false;
		
		int depthSplits = 3;	//Numbers of coordinates, 3 mean corners point plus one at each center 
		int widthSplits = 3;	//9 checks total
		
		double x = aabbCenter.x - (depth/2.0);
		
		for(int i = 0; i <= depthSplits; i++)
		{
			double y = aabbCenter.y - (width/2.0);

			for(int j = 0; i <= widthSplits; i++)
			{
				double z = getDeckAt(x, y);
				z += getRockerAtPos(x);
				
				if(z < aabbCenter.y - (width/2.0))
					return true;
				
				y += width / (double)(widthSplits-1);
			}
			x += depth / (double)(depthSplits-1);
		}

		return false;
	}

	public NurbsPoint getOutline3D(double x)
	{
		x=x/10;
		if(x>getLength())
			x=getLength();

		double width=mOutlineSpline.getValueAt(x);
		double rocker=mBottomSpline.getValueAt(x);
		double deck=mDeckSpline.getValueAt(x);

		//find closest cross sections before and after x

		int before_index=0;
		double before_x=0.0;
		double before_y=0.0;
		double before_z=0.0;
		int after_index=0;
		double after_x=0.0;	
		double after_y=0.0;
		double after_z=0.0;

		before_index=1;
		BezierBoardCrossSection current = mCrossSections.get(before_index);
		before_x=current.getPosition();

		for(int i = 1; i < mCrossSections.size(); i++)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()<x)
			{
				before_index=i;
				before_x=current.getPosition();
			}
		}

		after_index=mCrossSections.size()-2;
		current = mCrossSections.get(after_index);
		after_x=current.getPosition();

		for(int i = mCrossSections.size()-2; i>=1; i--)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()>x)
			{
				after_index=i;
				after_x=current.getPosition();
			}
		}
		/*
		int ni = getNearestCrossSectionIndex(x);
		double nearestPos = mCrossSections.get(ni).getPosition();
		if(nearestPos < x)
		{
			before_index=ni;
			before_x=nearestPos;
			after_index=ni+1;
			after_x= mCrossSections.get(ni+1).getPosition();
		}
		else
		{
			before_index=ni-1;
			before_x=mCrossSections.get(ni-1).getPosition();
			after_index=ni;
			after_x= nearestPos;

		}
		 */
		//find outline ControlPoint for each cross section

//		int before_outline_index=0;
//		int after_outline_index=0;

		double before_outline=0;		
		BezierBoardCrossSection before_cross_section=mCrossSections.get(before_index);
		BezierSpline before_ControlPoints=before_cross_section.getBezierSpline();
		for(int i=1; i<before_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=before_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(before_outline<=ControlPoint_point.getX())
			{
				before_outline=ControlPoint_point.getX();
				before_z=before_outline;
//				before_y=ControlPoint_point.getY();
				before_y=myControlPoint.getEndPoint().y;
//				before_outline_index=i;
			}
		}



		double after_outline=0;		
		BezierBoardCrossSection after_cross_section=mCrossSections.get(after_index);
		BezierSpline after_ControlPoints=after_cross_section.getBezierSpline();
		for(int i=1; i<after_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=after_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(after_outline<=ControlPoint_point.getX())
			{
				after_outline=ControlPoint_point.getX();
				after_z=after_outline;
//				after_y=ControlPoint_point.getY();
				after_y=myControlPoint.getEndPoint().y;
//				after_outline_index=i;
			}
		}

		//calculate the linear combination

		double y1top=mDeckSpline.getValueAt(before_x)-mBottomSpline.getValueAt(before_x);
		double y1=before_y/y1top*(deck-rocker);
		double y2top=mDeckSpline.getValueAt(after_x)-mBottomSpline.getValueAt(after_x);
		double y2=after_y/y2top*(deck-rocker);

		double y_value=0.0;

		if(before_x==after_x)
		{
			y_value=y1+rocker;
		}
		else
		{
			y_value=y1+(y2-y1)/(after_x-before_x)*(x-before_x)+rocker;
		}

		NurbsPoint outline_point=new NurbsPoint(10*x,10*y_value,10*width);				

		return outline_point;
	}

	public NurbsPoint getTuckedUnder3D(double x)
	{
		x=x/10;
		if(x>getLength())
			x=getLength();

		double width=mOutlineSpline.getValueAt(x);
		double rocker=mBottomSpline.getValueAt(x);
		double deck=mDeckSpline.getValueAt(x);

		//find closest cross sections before and after x

		int before_index=0;
		double before_x=0.0;
		double before_y=0.0;
		double before_z=0.0;
		int after_index=0;
		double after_x=0.0;	
		double after_y=0.0;
		double after_z=0.0;


		before_index=1;
		BezierBoardCrossSection current = mCrossSections.get(before_index);
		before_x=current.getPosition();

		for(int i = 1; i < mCrossSections.size(); i++)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()<x)
			{
				before_index=i;
				before_x=current.getPosition();
			}
		}

		after_index=mCrossSections.size()-2;
		current = mCrossSections.get(after_index);
		after_x=current.getPosition();

		for(int i = mCrossSections.size()-2; i>=1; i--)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()>x)
			{
				after_index=i;
				after_x=current.getPosition();
			}
		}

		/*
		int ni = getNearestCrossSectionIndex(x);
		double nearestPos = mCrossSections.get(ni).getPosition();
		if(nearestPos < x)
		{
			before_index=ni;
			before_x=nearestPos;
			after_index=ni+1;
			after_x= mCrossSections.get(ni+1).getPosition();
		}
		else
		{
			before_index=ni-1;
			before_x=mCrossSections.get(ni-1).getPosition();
			after_index=ni;
			after_x= nearestPos;

		}
		 */

		//find outline ControlPoint for each cross section

		int before_outline_index=0;
		int after_outline_index=0;

		double before_outline=0;		
		BezierBoardCrossSection before_cross_section=mCrossSections.get(before_index);
		BezierSpline before_ControlPoints=before_cross_section.getBezierSpline();
		for(int i=1; i<before_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=before_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(before_outline<=ControlPoint_point.getX())
			{
				before_outline=ControlPoint_point.getX();
				before_z=before_outline;
				before_y=ControlPoint_point.getY();
				before_outline_index=i;
			}
		}

		double after_outline=0;		
		BezierBoardCrossSection after_cross_section=mCrossSections.get(after_index);
		BezierSpline after_ControlPoints=after_cross_section.getBezierSpline();
		for(int i=1; i<after_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=after_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(after_outline<=ControlPoint_point.getX())
			{
				after_outline=ControlPoint_point.getX();
				after_z=after_outline;
				after_y=ControlPoint_point.getY();
				after_outline_index=i;
			}
		}

		//get point before outline and check if it is tucked under, otherwise use outline
		BezierKnot myControlPoint;
		Point2D.Double ControlPoint_point;
		if(before_outline_index > 0)//to prevent java.lang.ArrayIndexOutOfBoundsException
		{
			myControlPoint=before_ControlPoints.getControlPoint(before_outline_index-1);
			ControlPoint_point=myControlPoint.getEndPoint();
			if(ControlPoint_point.getX()>before_outline/2)
			{
				before_z=ControlPoint_point.getX();
				before_y=ControlPoint_point.getY();
			}
		}

		if(after_outline_index > 0)	//to prevent java.lang.ArrayIndexOutOfBoundsException
		{
			myControlPoint=after_ControlPoints.getControlPoint(after_outline_index-1);
			ControlPoint_point=myControlPoint.getEndPoint();
			if(ControlPoint_point.getX()>after_outline/2)
			{
				after_z=ControlPoint_point.getX();
				after_y=ControlPoint_point.getY();
			}
		}

		//calculate the linear combination

		double y1top=mDeckSpline.getValueAt(before_x)-mBottomSpline.getValueAt(before_x);
		double y1=before_y/y1top*(deck-rocker);
		double y2top=mDeckSpline.getValueAt(after_x)-mBottomSpline.getValueAt(after_x);
		double y2=after_y/y2top*(deck-rocker);

		double y_value=0.0;

		if(before_x==after_x)
		{
			y_value=y1+rocker;
		}
		else
		{
			y_value=y1+(y2-y1)/(after_x-before_x)*(x-before_x)+rocker;
		}

		double z1top=mOutlineSpline.getValueAt(before_x);
		double z1=before_z/z1top*width;
		double z2top=mOutlineSpline.getValueAt(after_x);
		double z2=after_z/z2top*width;

		double z_value=0.0;

		if(before_x==after_x)
		{
			z_value=z1;
		}
		else
		{
			z_value=z1+(z2-z1)/(after_x-before_x)*(x-before_x);
		}

		NurbsPoint tucked_under_point=new NurbsPoint(10*x,10*y_value,10*z_value);				
		return tucked_under_point;


	}

	public NurbsPoint getTop3D(double x, double z)
	{
		x=x/10;
		if(x>getLength())
			x=getLength();

		z=z/10;
		double width=mOutlineSpline.getValueAt(x);
		double rocker=mBottomSpline.getValueAt(x);
		double deck=mDeckSpline.getValueAt(x);

		//find closest cross sections before and after x

		int before_index=0;
		double before_x=0.0;
		int after_index=0;
		double after_x=0.0;		

		before_index=1;
		BezierBoardCrossSection current = mCrossSections.get(before_index);
		before_x=current.getPosition();

		for(int i = 1; i < mCrossSections.size(); i++)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()<x)
			{
				before_index=i;
				before_x=current.getPosition();
			}
		}

		after_index=mCrossSections.size()-2;
		current = mCrossSections.get(after_index);
		after_x=current.getPosition();

		for(int i = mCrossSections.size()-2; i>=1; i--)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()>x)
			{
				after_index=i;
				after_x=current.getPosition();
			}
		}
		/*
		int ni = getNearestCrossSectionIndex(x);
		double nearestPos = mCrossSections.get(ni).getPosition();
		if(nearestPos < x)
		{
			before_index=ni;
			before_x=nearestPos;
			after_index=ni+1;
			after_x= mCrossSections.get(ni+1).getPosition();
		}
		else
		{
			before_index=ni-1;
			before_x=mCrossSections.get(ni-1).getPosition();
			after_index=ni;
			after_x= nearestPos;

		}

		 */
		//find outline ControlPoint for each cross section

		int before_outline_index=0;
		int after_outline_index=0;

		double before_outline=0;		
		BezierBoardCrossSection before_cross_section=mCrossSections.get(before_index);
		BezierSpline before_ControlPoints=before_cross_section.getBezierSpline();
		for(int i=1; i<before_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=before_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(before_outline<ControlPoint_point.getX())
			{
				before_outline=ControlPoint_point.getX();
				before_outline_index=i;
			}
		}

		double after_outline=0;		
		BezierBoardCrossSection after_cross_section=mCrossSections.get(after_index);
		BezierSpline after_ControlPoints=after_cross_section.getBezierSpline();
		for(int i=1; i<after_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=after_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(after_outline<ControlPoint_point.getX())
			{
				after_outline=ControlPoint_point.getX();
				after_outline_index=i;
			}
		}

		//find y-value at related z for each cross section
		/*
		ArrayList<BezierControlPoint> top_before_ControlPoints = new ArrayList<BezierControlPoint>();
		for(int i = before_ControlPoints.size()-1; i>=before_outline_index; i--)
		{
			ControlPoint myControlPoint=(BezierControlPoint)before_ControlPoints.get(i).clone();
			myControlPoint.switch_tangents();
			top_before_ControlPoints.add(myControlPoint);
		}
		 */
//		double y1=BezierUtil.value(top_before_ControlPoints, z*before_outline/width);
//		double y1top=BezierUtil.value(top_before_ControlPoints, 0);
		double y1=before_ControlPoints.getValueAtReverse(z*before_outline/width);
		double y1top=before_ControlPoints.getValueAtReverse(0);
		/*
		ArrayList<BezierControlPoint> top_after_ControlPoints = new ArrayList<BezierControlPoint>();
		for(int i = after_ControlPoints.size()-1; i>=after_outline_index; i--)
		{
			ControlPoint myControlPoint=(BezierControlPoint)after_ControlPoints.get(i).clone();
			myControlPoint.switch_tangents();
			top_after_ControlPoints.add(myControlPoint);
		}
		 */
//		double y2=BezierUtil.value(top_after_ControlPoints, z*after_outline/width);
//		double y2top=BezierUtil.value(top_after_ControlPoints, 0);
		double y2=after_ControlPoints.getValueAtReverse(z*after_outline/width);
		double y2top=after_ControlPoints.getValueAtReverse(0);


		//calculate the linear combination

		y1=y1/y1top*(deck-rocker);
		y2=y2/y2top*(deck-rocker);

		double y_value=0.0;

		if(before_x==after_x)
		{
			y_value=y1+rocker;
		}
		else
		{
			y_value=y1+(y2-y1)/(after_x-before_x)*(x-before_x)+rocker;
		}

		NurbsPoint top_point=new NurbsPoint(10*x,10*y_value,10*z);				
		return top_point;
	}

	public NurbsPoint getBottom3D(double x, double z)
	{
		x=x/10;
		if(x>getLength())
			x=getLength();

		z=z/10;
		double width=mOutlineSpline.getValueAt(x);
		double rocker=mBottomSpline.getValueAt(x);
		double deck=mDeckSpline.getValueAt(x);

		//find closest cross sections before and after x

		int before_index=0;
		double before_x=0.0;
		int after_index=0;
		double after_x=0.0;		

		before_index=1;
		BezierBoardCrossSection current = mCrossSections.get(before_index);
		before_x=current.getPosition();

		for(int i = 1; i < mCrossSections.size(); i++)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()<x)
			{
				before_index=i;
				before_x=current.getPosition();
			}
		}

		after_index=mCrossSections.size()-2;
		current = mCrossSections.get(after_index);
		after_x=current.getPosition();

		for(int i = mCrossSections.size()-2; i>=1; i--)
		{
			current = mCrossSections.get(i);
			if(current.getPosition()>x)
			{
				after_index=i;
				after_x=current.getPosition();
			}
		}


		//find outline ControlPoint for each cross section

		int before_outline_index=0;
		int after_outline_index=0;

		double before_outline=0;		
		BezierBoardCrossSection before_cross_section=mCrossSections.get(before_index);
		BezierSpline before_ControlPoints=before_cross_section.getBezierSpline();
		for(int i=1; i<before_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=before_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(before_outline<ControlPoint_point.getX())
			{
				before_outline=ControlPoint_point.getX();
				before_outline_index=i;
			}
		}

		double after_outline=0;		
		BezierBoardCrossSection after_cross_section=mCrossSections.get(after_index);
		BezierSpline after_ControlPoints=after_cross_section.getBezierSpline();
		for(int i=1; i<after_ControlPoints.getNrOfControlPoints()-1; i++)
		{
			BezierKnot myControlPoint=after_ControlPoints.getControlPoint(i);
			Point2D.Double ControlPoint_point=myControlPoint.getEndPoint();

			if(after_outline<ControlPoint_point.getX())
			{
				after_outline=ControlPoint_point.getX();
				after_outline_index=i;
			}
		}

		//find y-value at related z for each cross section

		ArrayList<BezierKnot> top_before_ControlPoints = new ArrayList<BezierKnot>();
		for(int i = 0; i<=before_outline_index; i++)
		{
			BezierKnot myControlPoint=(BezierKnot)before_ControlPoints.getControlPoint(i).clone();
			top_before_ControlPoints.add(myControlPoint);
		}

		double y1=BezierUtil.value(top_before_ControlPoints, z*before_outline/width);
//		double y1top=mDeckSpline.getValueAt(before_x);
		double y1top=mDeckSpline.getValueAt(before_x)-mBottomSpline.getValueAt(before_x);


		ArrayList<BezierKnot> top_after_ControlPoints = new ArrayList<BezierKnot>();
		for(int i = 0; i<=after_outline_index; i++)
		{
			BezierKnot myControlPoint=(BezierKnot)after_ControlPoints.getControlPoint(i).clone();
			top_after_ControlPoints.add(myControlPoint);
		}

		double y2=BezierUtil.value(top_after_ControlPoints, z*after_outline/width);
//		double y2top=mDeckSpline.getValueAt(after_x);
		double y2top=mDeckSpline.getValueAt(after_x)-mBottomSpline.getValueAt(after_x);


		//calculate the linear combination

		y1=y1/y1top*(deck-rocker);
		y2=y2/y2top*(deck-rocker);

		double y_value=0.0;

		if(before_x==after_x)
		{
			y_value=y1+rocker;
		}
		else
		{
			y_value=y1+(y2-y1)/(after_x-before_x)*(x-before_x)+rocker;
		}

		NurbsPoint bottom_point=new NurbsPoint(10*x,10*y_value,10*z);				
		return bottom_point;
	}



	public Object clone()
	{
		BezierBoard brd = null;
		try {
			brd = (BezierBoard)super.clone();
		} catch(CloneNotSupportedException e) {
			System.out.println("Exception in BezierBoard::clone(): " + e.toString());			
			throw new Error("CloneNotSupportedException in Brd");
		}

		brd.mOutlineSpline = (BezierSpline)mOutlineSpline.clone();

		brd.mDeckSpline = (BezierSpline)mDeckSpline.clone();

		brd.mBottomSpline = (BezierSpline)mBottomSpline.clone();

		brd.mOutlineGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < this.mOutlineGuidePoints.size(); i++)
		{
			brd.mOutlineGuidePoints.add((Point2D.Double)this.mOutlineGuidePoints.get(i).clone());
		}

		brd.mDeckGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < this.mDeckGuidePoints.size(); i++)
		{
			brd.mDeckGuidePoints.add((Point2D.Double)this.mDeckGuidePoints.get(i).clone());
		}

		brd.mBottomGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < this.mBottomGuidePoints.size(); i++)
		{
			brd.mBottomGuidePoints.add((Point2D.Double)this.mBottomGuidePoints.get(i).clone());
		}

		brd.mCrossSections = new ArrayList<BezierBoardCrossSection>();
		for(int i = 0; i < this.mCrossSections.size(); i++)
		{
			brd.mCrossSections.add((BezierBoardCrossSection)this.mCrossSections.get(i).clone());
		}

		return brd;
	}
	

	public void update3DModel(Shape3D model)
	{				
		double lengthAccuracy = 1.0;
		double deckWidthAccuracy = 1.0;
		double bottomWidthAccuracy = 1.0;
		
		double length = getLength();
		double width = getCenterWidth();
		
		int lengthSteps = (int)(length/lengthAccuracy) +1; 
//		int lengthSteps = 200; 
		int deckSteps = (int)((width/2.0)/deckWidthAccuracy) +1; 
		int railSteps = 0; 
		int bottomSteps = (int)(width/2.0/bottomWidthAccuracy) +1; 

		double lengthStep = length/lengthSteps; 

		int nrOfCoords = (lengthSteps)*(deckSteps+railSteps+bottomSteps)*4*2;
		
		QuadArray quads;
		quads = new QuadArray(nrOfCoords, IndexedQuadArray.COORDINATES | IndexedQuadArray.NORMALS);

		Point3d[] vertices = new Point3d[4];
		Vector3f[] normals = new Vector3f[4];
		Point3d[] mirrorVertices = new Point3d[4];
		Vector3f[] mirrorNormals = new Vector3f[4];
		for(int i = 0; i < 4; i++)
		{
			vertices[i] = new Point3d();
			normals[i] = new Vector3f();
			mirrorVertices[i] = new Point3d();
			mirrorNormals[i] = new Vector3f();
		}
		
		int nrOfQuads = 0;
		double xPos = 0.0;

		double minAngle = -45.0; 
		double maxAngle = 45.0; 
		for(int i = 0; i < deckSteps; i++)
		{	
			xPos = 0.0;

			//TODO: temporary fix because the control point surface model doesn't work well near stringer. Remove when surface model works better
			if( i == 0)
			{
				vertices[0].set(getSurfacePoint(xPos, 0.0));
				Point2D.Double normal = getDeck().getNormalAt(xPos);				
				normals[0].set(new Vector3d(normal.x, 0.0, normal.y));
			}
			else
			{
				//first coords in lengthwise strip
				vertices[0].set(getSurfacePoint(xPos, minAngle, maxAngle, i, deckSteps));
				normals[0].set(getSurfaceNormal(xPos, minAngle, maxAngle, i, deckSteps));
			}

			vertices[3].set(getSurfacePoint(xPos, minAngle, maxAngle, i+1, deckSteps));
			normals[3].set(getSurfaceNormal(xPos, minAngle, maxAngle, i+1, deckSteps));

//			normals[0].scale(-1.0f);
//			normals[3].scale(-1.0f);
			
			xPos += lengthStep;

			for(int j = 1; j <= lengthSteps; j++)
			{	
				//TODO: temporary fix because the control point surface model doesn't work well near stringer. Remove when surface model works better
				if( i == 0)
				{
					vertices[1].set(getSurfacePoint(xPos, 0.0));
					Point2D.Double normal = getDeck().getNormalAt(xPos);				
					normals[1].set(new Vector3d(normal.x, 0.0, normal.y));
				}
				else
				{
					//Two next coords
					vertices[1].set(getSurfacePoint(xPos, minAngle, maxAngle, i, deckSteps));
					normals[1].set(getSurfaceNormal(xPos, minAngle, maxAngle, i, deckSteps));
				}
	
				vertices[2].set(getSurfacePoint(xPos, minAngle, maxAngle, i+1, deckSteps));
				normals[2].set(getSurfaceNormal(xPos, minAngle, maxAngle, i+1, deckSteps));
			
//				normals[1].scale(-1.0f);
//				normals[2].scale(-1.0f);

//				if(j == lengthSteps/2)
//				{
//					for(int h = 0; h<1; h++)
//					{
//						System.out.printf("update3DModel() on deck: normals[%d] %f,%f,%f\n", h, normals[h].x, normals[h].y, normals[h].z);
//					}
//				}

				//Build one quad
				quads.setCoordinates(nrOfQuads*4, vertices);
				quads.setNormals(nrOfQuads*4, normals);
				nrOfQuads++;
				
				//Mirror
				for(int n = 0; n < 4; n++)
				{
					mirrorVertices[n].set(vertices[3-n]);
					mirrorNormals[n].set(normals[3-n]);
					
					mirrorVertices[n].y = -mirrorVertices[n].y; 
					mirrorNormals[n].y = -mirrorNormals[n].y;
				}
				
				//Build mirrored quad
				quads.setCoordinates(nrOfQuads*4, mirrorVertices);
				quads.setNormals(nrOfQuads*4, mirrorNormals);
				nrOfQuads++;

				//Get ready for next step
				vertices[0].set(vertices[1]);
				normals[0].set(normals[1]);

				vertices[3].set(vertices[2]);
				normals[3].set(normals[2]);

				xPos += lengthStep;
				
			}
		}		
/*
		minAngle = maxAngle; 
		maxAngle = 174.0; 
		for(int i = 0; i < railSteps; i++)
		{	
			xPos = 0.0;
			
			//first coords in lengthwise strip
			vertices[0].set(getSurfacePoint(xPos, minAngle, maxAngle, i, railSteps));
			normals[0].set(getSurfaceNormal(xPos, minAngle, maxAngle, i, railSteps));

			vertices[3].set(getSurfacePoint(xPos, minAngle, maxAngle, i+1, railSteps));
			normals[3].set(getSurfaceNormal(xPos, minAngle, maxAngle, i+1, railSteps));

			xPos += lengthStep;

			for(int j = 1; j <= lengthSteps; j++)
			{	
				//Two next coords
				vertices[1].set(getSurfacePoint(xPos, minAngle, maxAngle, i, railSteps));
				normals[1].set(getSurfaceNormal(xPos, minAngle, maxAngle, i, railSteps));

				vertices[2].set(getSurfacePoint(xPos, minAngle, maxAngle, i+1, railSteps));
				normals[2].set(getSurfaceNormal(xPos, minAngle, maxAngle, i+1, railSteps));

				if(j == lengthSteps/2)
				{
					for(int h = 0; h<1; h++)
					{
						System.out.printf("update3DModel() on rails: normals[%d] %f,%f,%f\n", h, normals[h].x, normals[h].y, normals[h].z);
					}
				}
				
				//Build one quad
				quads.setCoordinates(nrOfQuads*4, vertices);
				quads.setNormals(nrOfQuads*4, normals);
				nrOfQuads++;
				
				//Mirror
				for(int n = 0; n < 4; n++)
				{
					mirrorVertices[n].set(vertices[3-n]);
					mirrorNormals[n].set(normals[3-n]);
					
					mirrorVertices[n].y = -mirrorVertices[n].y; 
					mirrorNormals[n].y = -mirrorNormals[n].y;
				}
				
				//Build mirrored quad
				quads.setCoordinates(nrOfQuads*4, mirrorVertices);
				quads.setNormals(nrOfQuads*4, mirrorNormals);
				nrOfQuads++;

				//Get ready for next step
				vertices[0].set(vertices[1]);
				normals[0].set(normals[1]);

				vertices[3].set(vertices[2]);
				normals[3].set(normals[2]);

				xPos += lengthStep;
				
			}
		}		
*/
		minAngle = maxAngle; 
		maxAngle = 360.0; 
		for(int i = 0; i < bottomSteps; i++)
		{	
			xPos = 0.0;
			
			//first coords in lengthwise strip
			vertices[0].set(getSurfacePoint(xPos, minAngle, maxAngle, i, bottomSteps));
			normals[0].set(getSurfaceNormal(xPos, minAngle, maxAngle, i, bottomSteps));

			vertices[3].set(getSurfacePoint(xPos, minAngle, maxAngle, i+1, bottomSteps));
			normals[3].set(getSurfaceNormal(xPos, minAngle, maxAngle, i+1, bottomSteps));
			

			xPos += lengthStep;

			for(int j = 1; j <= lengthSteps; j++)
			{	
				//Two next coords
				vertices[1].set(getSurfacePoint(xPos, minAngle, maxAngle, i, bottomSteps));
				normals[1].set(getSurfaceNormal(xPos, minAngle, maxAngle, i, bottomSteps));

				vertices[2].set(getSurfacePoint(xPos, minAngle, maxAngle, i+1, bottomSteps));
				normals[2].set(getSurfaceNormal(xPos, minAngle, maxAngle, i+1, bottomSteps));

//				if(j == lengthSteps/2)
//				{
//					for(int h = 0; h<1; h++)
//					{
//						System.out.printf("update3DModel() on bottom: normals[%d] %f,%f,%f\n", h, normals[h].x, normals[h].y, normals[h].z);
//					}
//				}
				
				//Build one quad
				quads.setCoordinates(nrOfQuads*4, vertices);
				quads.setNormals(nrOfQuads*4, normals);
				nrOfQuads++;
				
				//Mirror
				for(int n = 0; n < 4; n++)
				{
					mirrorVertices[n].set(vertices[3-n]);
					mirrorNormals[n].set(normals[3-n]);
					
					mirrorVertices[n].y = -mirrorVertices[n].y; 
					mirrorNormals[n].y = -mirrorNormals[n].y;
				}
				
				//Build mirrored quad
				quads.setCoordinates(nrOfQuads*4, mirrorVertices);
				quads.setNormals(nrOfQuads*4, mirrorNormals);
				nrOfQuads++;

				//Get ready for next step
				vertices[0].set(vertices[1]);
				normals[0].set(normals[1]);

				vertices[3].set(vertices[2]);
				normals[3].set(normals[2]);

				xPos += lengthStep;
				
			}
		}		

		model.setGeometry(quads);

	}
	
	public String toString()
	{
		String str = new String();
		str.concat(getFilename());
		str.concat(" ");
		String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(getThickness(), true);
		str.concat(measurementsString);
		if(getModel() != "")
		{
			String modelString = LanguageResource.getString("MODEL_STR") + getModel();
			str.concat(" ");
			str.concat(modelString);
		}
		if(getDesigner() != "")
		{
			String designerString = LanguageResource.getString("DESIGNER_STR") + getDesigner();
			str.concat(" ");
			str.concat(designerString);
		}
		if(getSurfer() != "")
		{
			String surferString = LanguageResource.getString("SURFER_STR") + getSurfer();
			str.concat(" ");
			str.concat(surferString);
		}

		
		return str;
	}
	
	//PW additions for channels
	public void set_nr_of_bottom_ctrl_pts(int numPts)
	{
		nr_of_bottom_ctrl_pts = numPts;
	}
	
	public int get_nr_of_bottom_ctrl_pts()
	{
		return nr_of_bottom_ctrl_pts;
	}
	
	public void set_curved_channel_iterpolation(boolean curved_chan)
	{
		curved_channel_iterpolation = curved_chan;
	}
	
	public boolean get_curved_channel_iterpolation()
	{
		return curved_channel_iterpolation;
	}
}
