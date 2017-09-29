package boardcam.holdingsystems;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import cadcore.BezierSpline;
import cadcore.UnitUtils;
import cadcore.VecMath;

import board.BezierBoard;
import boardcad.gui.jdk.BoardCAD;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcad.settings.Settings;
import boardcad.i18n.LanguageResource;

import boardcam.MachineConfig;

public class SupportsBlankHoldingSystem extends AbstractBlankHoldingSystem {

	public static String SUPPORT_1_POS     = "Support1Pos";
	public static String SUPPORT_2_POS     = "Support2Pos";
	public static String SUPPORT_1_HEIGHT  = "Support1Height";
	public static String SUPPORT_2_HEIGHT  = "Support2Height";
	public static String TAILSTOP_POS      = "TailstopPos";
	
	MachineConfig mConfig;
	
	protected double refLineLength = 300;
	
	boolean invalidated = true;

	Shape3D mSupportStructure;
	LineArray mSupportStructureArray;

	public SupportsBlankHoldingSystem(MachineConfig config)
	{
		super.init();
		mConfig = config;
				
		Settings supportsSettings = mConfig.addCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));
		supportsSettings.clear();
		SettingChangedCallback cb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				calcBlankDeckOffset();
				calcApproxBoardDeckOffset();
				calcBoardAndBlankBottomOffset();
				update3DModel();
				if(mConfig.getMachineView() != null)
				{
					mConfig.getMachineView().update();
				}
			}	
		};

		supportsSettings.addObject(TAILSTOP_POS,  new Double(1000), LanguageResource.getString("TAILSTOPPOS_STR"), cb);
		supportsSettings.addObject(SUPPORT_1_POS, new Double(600), LanguageResource.getString("SUPPORT1FROMTAILSTOP_STR"), cb);
		supportsSettings.addObject(SUPPORT_2_POS, new Double(2600), LanguageResource.getString("SUPPORT2FROMTAILSTOP_STR"), cb);
	
		supportsSettings.addObject(SUPPORT_1_HEIGHT, new Double(260), LanguageResource.getString("SUPPORT1HEIGHT_STR"), cb);
		supportsSettings.addObject(SUPPORT_2_HEIGHT, new Double(300), LanguageResource.getString("SUPPORT2HEIGHT_STR_STR"), cb);
	
		init3DModel();
	}
	
	public void setBoardDeckOffsetPos(Vector3d offsetPos)
	{
//		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		
//		double tailstop = mConfig.getCategory(holdingSystemStr).getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;

		mBoardDeckOffset.set(offsetPos.x, 0.0, offsetPos.z);
		
		invalidated = true;
	}
	
	public void setBoardDeckOffsetAngle(double angle)
	{
		super.setBoardDeckOffsetAngle(angle);
		invalidated = true;
	}

	public Vector3d getBoardDeckOffsetPos()
	{
		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		
		double tailstop = mConfig.getCategory(holdingSystemStr).getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;

//		System.out.printf("getBoardDeckOffsetPos() x:%f y:%f z:%f\n",mBoardDeckOffset.x, mBoardDeckOffset.y, mBoardDeckOffset.z);

		return new Vector3d(mBoardDeckOffset.x, 0.0, mBoardDeckOffset.z);
	}

	public double getBoardBottomOffsetAngle()
	{
		if(invalidated)
		{
			calcBoardAndBlankBottomOffset();
			invalidated = false;
		}
		
		return mBoardBottomRotation;
	}

	public Vector3d getBoardBottomOffsetPos()
	{
		if(invalidated)
		{
			calcBoardAndBlankBottomOffset();
			invalidated = false;
		}

		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		
		double tailstop = mConfig.getCategory(holdingSystemStr).getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;

		return new Vector3d(mBoardBottomOffset.x, 0.0, mBoardBottomOffset.z);
	}
	
	public Vector3d getBlankBottomOffsetPos()
	{
		if(invalidated)
		{
			calcBoardAndBlankBottomOffset();
			invalidated = false;
		}

		return super.getBlankBottomOffsetPos();
	}
	
	public double getBlankBottomOffsetAngle()
	{
		if(invalidated)
		{
			calcBoardAndBlankBottomOffset();
			invalidated = false;
		}

		return super.getBlankBottomOffsetAngle();
	}
	
	void calcBlankDeckOffset()
	{
		BezierBoard blank = mConfig.getBlank();
		if(blank == null || blank.isEmpty())
			return;
	
		//Get vertical machinespace positions for supports and calcualte the angle for the supports
		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		MachineConfig config = mConfig;
		Settings holdingSystemSettings = config.getCategory(holdingSystemStr);
		double distanceToSupport1 = holdingSystemSettings.getDouble(SUPPORT_1_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double distanceToSupport2 = holdingSystemSettings.getDouble(SUPPORT_2_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER - holdingSystemSettings.getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;

		double support1Height = holdingSystemSettings.getDouble(SUPPORT_1_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double support2Height = holdingSystemSettings.getDouble(SUPPORT_2_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		Vector2d supportsVector = new Vector2d(distanceToSupport2-distanceToSupport1, support2Height-support1Height);
		double supportsAngle = supportsVector.angle(new Vector2d(1.0,0.0))*((support1Height >= support2Height)?1.0:-1.0);;
		
		//Initial values
		double Ox = 0.0;
		double Oy = support1Height;
		double totalAngle = supportsAngle;
		
		double blankTailYBottomPos = blank.getBottomAt(0.1, 0.0);
		double blankTailYDeckPos = blank.getDeckAt(0.1, 0.0);
		
//		System.out.printf("calcBlankDeckOffset() tailXDeckPos: %f tailXBottomPos: %f\n", blankTailYDeckPos, blankTailYBottomPos);

		
		//Recalculate using last values for improved accuracy		
		for(int i = 0; i < 10; i++)
		{	
			double blankTailX = Math.min(blankTailYDeckPos*Math.sin(totalAngle), blankTailYBottomPos*Math.sin(totalAngle));
			
			//Transform the machine distances to blank space horizontal distances
			double blankDistanceToSupport1 = (distanceToSupport1+blankTailX)/Math.cos(totalAngle);
			double blankDistanceToSupport2 = (distanceToSupport2+blankTailX)/Math.cos(totalAngle);
//			double blankSupport1Y = blank.getBottomAt(blankDistanceToSupport1, 0.5);
//			double blankSupport2Y = blank.getBottomAt(blankDistanceToSupport2, 0.5);
//			Vector2d blankVector = new Vector2d(blankDistanceToSupport2-blankDistanceToSupport1, blankSupport2Y-blankSupport1Y);
//			double blankAngle = blankVector.angle(new Vector2d(1,0))*((blankSupport1Y >= blankSupport2Y)?1.0:-1.0);
					
			BezierSpline bottomSpline = blank.getBottom();
			double lineXOffset = blank.getMaxRocker()*Math.sin(totalAngle);
			double lineYOffset = blank.getMaxRocker()*Math.cos(totalAngle);
//			System.out.printf("calcBlankDeckOffset() iteration: %d  lineXOffset: %f lineYOffset: %f\n", i, lineXOffset, lineYOffset);
			Point2D.Double blankSupport1Pos = bottomSpline.getPointByLineIntersection(new Point2D.Double(blankDistanceToSupport1, 0), new Point2D.Double(blankDistanceToSupport1 - lineXOffset, lineYOffset));
			Point2D.Double blankSupport2Pos = bottomSpline.getPointByLineIntersection(new Point2D.Double(blankDistanceToSupport2, 0), new Point2D.Double(blankDistanceToSupport2 - lineXOffset, lineYOffset));
			Vector2d blankVector2 = new Vector2d(blankSupport2Pos.x-blankSupport1Pos.x, blankSupport2Pos.y-blankSupport1Pos.y);
			double blankAngle2 = blankVector2.angle(new Vector2d(1,0))*((blankSupport1Pos.y >= blankSupport2Pos.y)?1.0:-1.0);

//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankDistanceToSupport1: %f blankDistanceToSupport2: %f\n", i, blankDistanceToSupport1, blankDistanceToSupport2);
//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankSupport1Y: %f blankSupport2Y: %f\n", i, blankSupport1Y, blankSupport2Y);
//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankAngle: %f\n", i, blankAngle);
//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankSupport1Pos.x: %f blankSupport1Pos.y: %f\n", i, blankSupport1Pos.x, blankSupport1Pos.y);
//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankSupport2Pos.x: %f blankSupport2Pos.y: %f\n", i, blankSupport2Pos.x, blankSupport2Pos.y);
//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankAngle2: %f\n", i, blankAngle2);
		
			totalAngle = supportsAngle - blankAngle2;
			
			Ox = - blankTailX;
			Oy = -support1Height - ((blankDistanceToSupport1)*Math.sin(totalAngle)) + (blankSupport1Pos.y/Math.cos(totalAngle));
			
//			System.out.printf("calcBlankDeckOffset() iteration: %d  blankTailX: %f Ox: %f Oy: %f Angle:%f\n", i, blankTailX, Ox, Oy, totalAngle*180.0/Math.PI);
		}
		
//		System.out.printf("calcBlankDeckOffset() Ox: %f Oy: %f Angle:%f\n", Ox, Oy, totalAngle*180.0/Math.PI);

		mBlankDeckOffset.x = Ox + holdingSystemSettings.getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		mBlankDeckOffset.z = -Oy;
		mBlankDeckRotation = totalAngle;

	}

	void calcApproxBoardDeckOffset()
	{
		BezierBoard mBoard = mConfig.getBoard();
		if(mBoard == null || mBoard.isEmpty())
			return;

		BezierBoard blank = mConfig.getBlank();
		if(blank == null || blank.isEmpty())
			return;

		double tailPickPos = 1.0;
		double nosePickPos = mBoard.getLength()-0.5;

		double blankTailPoint = blank.getDeck().getValueAt(tailPickPos); 
		double blankTailPointYAfterRot = Math.cos(mBlankDeckRotation)*blankTailPoint - Math.sin(mBlankDeckRotation)*tailPickPos; 
		
		double blankNosePoint = blank.getDeck().getValueAt(nosePickPos); 
		double blankNosePointYAfterRot = Math.cos(mBlankDeckRotation)*blankNosePoint - Math.sin(mBlankDeckRotation)*nosePickPos; 
		Point2D.Double blankVec = new Point2D.Double(nosePickPos-tailPickPos, blankNosePointYAfterRot-blankTailPointYAfterRot);
		
		double mBoardTailPoint = mBoard.getDeck().getValueAt(tailPickPos); 
		double mBoardNosePoint = mBoard.getDeck().getValueAt(nosePickPos); 
		Point2D.Double mBoardVec = new Point2D.Double(nosePickPos-tailPickPos, mBoardNosePoint-mBoardTailPoint);
		
		mBoardDeckRotation = VecMath.getVecAngle(mBoardVec, blankVec)*((mBoardVec.y > blankVec.y)?1.0:-1.0);

		double boardTailPointYAfterRot = Math.cos(mBoardDeckRotation)*mBoardTailPoint - Math.sin(mBoardDeckRotation)*tailPickPos; 

		mBoardDeckOffset.x = mBlankDeckOffset.x;
//		mBoardDeckOffset.z = -(mBoardTailPointYAfterRot - blankTailPointYAfterRot + mBlankDeckOffset.z);
		mBoardDeckOffset.z = mBlankDeckOffset.z + (blankTailPointYAfterRot - boardTailPointYAfterRot);
	}

	
	void calcBoardAndBlankBottomOffset()
	{
//		System.out.printf("\n\ncalcBoardBottomOffset()\n");
		
		BezierBoard board = BoardCAD.getInstance().getCurrentBrd();
		if(board == null || board.isEmpty())
			return;

		BezierBoard blank = mConfig.getBlank();
		if(blank == null || blank.isEmpty())
			return;

//		System.out.printf("mBlankDeckOffset.x: %f mBlankDeckOffset.z: %f mBoardDeckOffset.x: %f mBoardDeckOffset.z: %f\n", mBlankDeckOffset.x, mBlankDeckOffset.z, mBoardDeckOffset.x, mBoardDeckOffset.z);

		//Blank against endstop, so offset
		double deckDiffX = mBlankDeckOffset.x-mBoardDeckOffset.x;
		double deckDiffY = mBlankDeckOffset.z-mBoardDeckOffset.z;
		double deckDiffRotation =  mBlankDeckRotation-mBoardDeckRotation;
		
//		System.out.printf("deckDiffX: %f deckDiffY: %f deckDiffRotation: %f\n", deckDiffX, deckDiffY, deckDiffRotation);
		
		//Convert the deck difference between blank and board into horizontal board space for the bottom calculations
		double boardSpaceDeckDiffX = deckDiffX*Math.cos(-mBoardDeckRotation) - deckDiffY*Math.sin(-mBoardDeckRotation); 
		double boardSpaceDeckDiffY = -deckDiffX*Math.sin(-mBoardDeckRotation) - deckDiffY*Math.cos(-mBoardDeckRotation); 
	
//		System.out.printf("boardSpaceDeckDiffX: %f boardSpaceDeckDiffY: %f\n", boardSpaceDeckDiffX, boardSpaceDeckDiffY);

		//Get vertical machinespace positions for supports and calculate the angle for the supports
		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		MachineConfig config = mConfig;
		Settings holdingSystemSettings = config.getCategory(holdingSystemStr);
		double distanceToSupport1 = holdingSystemSettings.getDouble(SUPPORT_1_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double distanceToSupport2 = holdingSystemSettings.getDouble(SUPPORT_2_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER - holdingSystemSettings.getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
//		System.out.printf("distanceToSupport1: %f distanceToSupport2: %f\n", distanceToSupport1, distanceToSupport2);

		double support1Height = holdingSystemSettings.getDouble(SUPPORT_1_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double support2Height = holdingSystemSettings.getDouble(SUPPORT_2_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		Vector2d supportsVector = new Vector2d(distanceToSupport2-distanceToSupport1, support2Height-support1Height);
		double supportsAngle = supportsVector.angle(new Vector2d(1,0))*((support1Height >= support2Height)?1.0:-1.0);;
		
		//Initial values
		double Ox = -boardSpaceDeckDiffX;					
		double Oy = support1Height;
		double totalAngle = supportsAngle;
		
		double blankTailYDeckPos = blank.getDeckAt(0.1, 0.0);
		double blankTailYBottomPos = blank.getBottomAt(0.1, 0.0);
		
		double boardSpaceBlankTailDeckOffsetX = blankTailYDeckPos*Math.sin(deckDiffRotation);
		double boardSpaceBlankTailBottomOffsetX = blankTailYBottomPos*Math.sin(deckDiffRotation);
		
		double boardSpaceBlankTailDeckOffsetY = blankTailYDeckPos*Math.cos(deckDiffRotation);
		double boardSpaceBlankTailBottomOffsetY = blankTailYBottomPos*Math.cos(deckDiffRotation);

//		System.out.printf("boardSpaceBlankTailDeckOffsetX: %f boardSpaceBlankTailBottomOffsetX: %f\n", boardSpaceBlankTailDeckOffsetX, boardSpaceBlankTailBottomOffsetX);
//		System.out.printf("boardSpaceBlankTailDeckOffsetY: %f boardSpaceBlankTailBottomOffsetY: %f\n", boardSpaceBlankTailDeckOffsetY, boardSpaceBlankTailBottomOffsetY);

		for(int i = 0; i < 5; i++)
		{
			double blankTailX = Math.min(
					(boardSpaceDeckDiffX+boardSpaceBlankTailDeckOffsetX)*Math.cos(totalAngle) + (boardSpaceDeckDiffY-boardSpaceBlankTailDeckOffsetY)*Math.sin(totalAngle), 
					(boardSpaceDeckDiffX+boardSpaceBlankTailBottomOffsetX)*Math.cos(totalAngle) + (boardSpaceDeckDiffY-boardSpaceBlankTailBottomOffsetY)*Math.sin(totalAngle));
			
//			System.out.printf("blankTailX: %f, totalAngle:%f\n",blankTailX, totalAngle);

			//Transform the machine distances to blank space horizontal distances
			double boardDistanceToSupport1 = (distanceToSupport1+blankTailX)/Math.cos(totalAngle);
			double boardDistanceToSupport2 = (distanceToSupport2+blankTailX)/Math.cos(totalAngle);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  mBoardDistanceToSupport1:%f mBoardDistanceToSupport2: %f\n", i, boardDistanceToSupport1, boardDistanceToSupport2);

//			double boardSupport1Y = board.getDeckAt(boardDistanceToSupport1, 0.5);
//			double boardSupport2Y = board.getDeckAt(boardDistanceToSupport2, 0.5);
//			
//			Vector2d mBoardVector = new Vector2d(boardDistanceToSupport2-boardDistanceToSupport1, boardSupport2Y-boardSupport1Y);
//			double mBoardAngle = mBoardVector.angle(new Vector2d(1,0))*((boardSupport1Y >= boardSupport2Y)?1.0:-1.0);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardSupport1Y:%f boardSupport2Y: %f mBoardAngle: %f\n", i, boardSupport1Y, boardSupport2Y, mBoardAngle*180.0/Math.PI);
						
			BezierSpline deckSpline = board.getDeck();
			double lineXOffset = board.getMaxRocker()*Math.sin(totalAngle);
			double lineYOffset = board.getMaxRocker()*Math.cos(totalAngle);
			Point2D.Double boardSupport1Pos = deckSpline.getPointByLineIntersection(new Point2D.Double(boardDistanceToSupport1, 0), new Point2D.Double(boardDistanceToSupport1 + lineXOffset, lineYOffset));
			Point2D.Double boardSupport2Pos = deckSpline.getPointByLineIntersection(new Point2D.Double(boardDistanceToSupport2, 0), new Point2D.Double(boardDistanceToSupport2 + lineXOffset, lineYOffset));
			Vector2d boardVector2 = new Vector2d(boardSupport2Pos.x-boardSupport1Pos.x, boardSupport2Pos.y-boardSupport1Pos.y);
			double boardAngle2 = boardVector2.angle(new Vector2d(1,0))*((boardSupport1Pos.y >= boardSupport2Pos.y)?1.0:-1.0);

//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardDistanceToSupport1: %f boardDistanceToSupport2: %f\n", i, boardDistanceToSupport1, boardDistanceToSupport2);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardSupport1Y: %f boardSupport2Y: %f\n", i, boardSupport1Y, boardSupport2Y);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardAngle: %f\n", i, mBoardAngle);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardSupport1Pos.x: %f boardSupport1Pos.y: %f\n", i, boardSupport1Pos.x, boardSupport1Pos.y);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardSupport2Pos.x: %f boardSupport2Pos.y: %f\n", i, boardSupport2Pos.x, boardSupport2Pos.y);
//			System.out.printf("calcBoardAndBlankBottomOffset() iteration: %d  boardAngle2: %f\n", i, boardAngle2);
			
			totalAngle = supportsAngle + boardAngle2;
//			System.out.printf("supportsAngle: %f - mBoardAngle: %f = totalAngle %f\n", supportsAngle*180.0/Math.PI, mBoardAngle*180.0/Math.PI, totalAngle*180.0/Math.PI);
			
			Ox = -blankTailX;
			Oy = -support1Height  - (boardDistanceToSupport1*Math.sin(totalAngle)) - (boardSupport1Pos.y/Math.cos(totalAngle));		//OK
//			System.out.printf("calcBoardBottomOffset() iteration: %d  Ox: %f Oy: %f Angle:%f\n", i, Ox, Oy, totalAngle*180.0/Math.PI);
		}
		
		Ox += holdingSystemSettings.getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
				
		mBoardBottomOffset.x = Ox;
		mBoardBottomOffset.z = -Oy;
		mBoardBottomRotation = totalAngle;

		mBlankBottomOffset.x = Ox + (boardSpaceDeckDiffX*Math.cos(totalAngle)) + (boardSpaceDeckDiffY*Math.sin(totalAngle));	//OK
		mBlankBottomOffset.z = -Oy + (boardSpaceDeckDiffX*Math.sin(totalAngle)) + (boardSpaceDeckDiffY*Math.cos(totalAngle));	//OK
		mBlankBottomRotation = mBoardBottomRotation - deckDiffRotation; 

//		System.out.printf("BOTTOM BRD x:%f y:%f rot:%f BLANK x:%f y:%f rot:%f\n", mBoardBottooffsetX, mBoardBottooffsetY, mBoardBottomRotation, mBlankBottooffsetX, mBlankBottooffsetY, mBlankBottomRotation);
	}


	public void draw(Graphics2D g2d, double offsetX, double offsetY, double scale, boolean showDeck)
	{
		super.draw(g2d, offsetX, offsetY, scale, showDeck);
		
		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		Settings holdingSystemSettings = mConfig.getCategory(holdingSystemStr);
		double tailstop = holdingSystemSettings.getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double s1pos = holdingSystemSettings.getDouble(SUPPORT_1_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double s2pos = holdingSystemSettings.getDouble(SUPPORT_2_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double s1height = holdingSystemSettings.getDouble(SUPPORT_1_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;
		double s2height = holdingSystemSettings.getDouble(SUPPORT_2_HEIGHT)/UnitUtils.MILLIMETER_PR_CENTIMETER;

		AffineTransform savedTransform = g2d.getTransform();
	
		g2d.setColor(BoardCAD.getInstance().getBrdColor());
	
		Stroke stroke = new BasicStroke((float)(1.0/scale));
		g2d.setStroke(stroke);
	
		AffineTransform at = new AffineTransform();
	
		at.setToTranslation(offsetX, offsetY);
	
		g2d.transform(at);
	
		at.setToScale(scale, scale);
	
		g2d.transform(at);
	
		double mulX = 1;
		double mulY = -1;	//Flip y
	
		Line2D line = new Line2D.Double();
	
		//Draw reference line
		line.setLine(0, 0, refLineLength, 0);
		g2d.draw(line);
	
		//Draw supports
	
		line.setLine(tailstop,0,tailstop,-s1height-10);
		g2d.draw(line);
		line.setLine(tailstop + s1pos,0,tailstop + s1pos,-s1height);
		g2d.draw(line);
		line.setLine(s2pos,0,s2pos,-s2height);
		g2d.draw(line);
	
		g2d.setTransform(savedTransform);
		
//		DEBUG
//		JavaDraw jd = new JavaDraw(g2d);
//		if(showDeck)
//		{
//			jd.draw(new Line2D.Double(offsetX+(mBoardDeckOffset.x*scale), offsetY+(mBoardDeckOffset.z*scale), offsetX+(mBoardDeckOffset.x*scale) + 250.0*Math.cos(mBoardDeckRotation)*scale, offsetY+(mBoardDeckOffset.z*scale) + 250.0*Math.sin(mBoardDeckRotation)*scale));
//				
//			jd.draw(new Line2D.Double(offsetX+(mBlankDeckOffset.x*scale), offsetY+(mBlankDeckOffset.z*scale), offsetX+(mBlankDeckOffset.x*scale) + 250.0*Math.cos(mBlankDeckRotation)*scale, offsetY+(mBlankDeckOffset.z*scale) + 250.0*Math.sin(mBlankDeckRotation)*scale));
//		}
//		else{
//			jd.draw(new Line2D.Double(offsetX+(mBoardBottomOffset.x*scale), offsetY+(mBoardBottomOffset.z*scale), offsetX+(mBoardBottomOffset.x*scale) + 250.0*Math.cos(mBoardBottomRotation)*scale, offsetY+(mBoardBottomOffset.z*scale) + 250.0*Math.sin(mBoardBottomRotation)*scale));
//			
//			jd.draw(new Line2D.Double(offsetX+(mBlankBottomOffset.x*scale), offsetY+(mBlankBottomOffset.z*scale), offsetX+(mBlankBottomOffset.x*scale) + 250.0*Math.cos(mBlankBottomRotation)*scale, offsetY+(mBlankBottomOffset.z*scale) + 250.0*Math.sin(mBlankBottomRotation)*scale));
//			
//		}
	}

	void init3DModel()
	{
		mSupportStructure = new Shape3D();
		mSupportStructureArray = new LineArray(100, LineArray.COORDINATES);
		mSupportStructureArray.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		mSupportStructureArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		mSupportStructureArray.setCoordinates(0, new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
		mSupportStructureArray.setValidVertexCount(8);
		mSupportStructure.setGeometry(mSupportStructureArray);
		mModelRoot.addChild(mSupportStructure);
//		mModelRoot.addChild(new Box(100.0f, 100.0f, 100.0f, new Appearance()));

		Appearance supportStructureApperance = new Appearance();
		ColoringAttributes supportStructureColor = new ColoringAttributes();
		supportStructureColor.setColor (0.1f, 0.1f, 0.5f);
		supportStructureApperance.setColoringAttributes(supportStructureColor);			
		mSupportStructure.setAppearance(supportStructureApperance);
		
//		System.out.printf("init3DModel()\n");
		
	}
	
	public void update3DModel()
	{
		setSupportBaseLine(refLineLength*10);
		
		String holdingSystemStr = LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR");
		
		double tailstop = mConfig.getCategory(holdingSystemStr).getDouble(TAILSTOP_POS);//*0.0005;
		setSupportTailStop(tailstop);
		
		double support1Height = mConfig.getCategory(holdingSystemStr).getDouble(SUPPORT_1_HEIGHT);//*0.0005;
		double support1Pos = mConfig.getCategory(holdingSystemStr).getDouble(SUPPORT_1_POS);//*0.0005;
		setSupport1(support1Pos+tailstop, support1Height);
		
		double support2Height = mConfig.getCategory(holdingSystemStr).getDouble(SUPPORT_2_HEIGHT);//*0.0005;
		double support2Pos = mConfig.getCategory(holdingSystemStr).getDouble(SUPPORT_2_POS);//*0.0005;
		setSupport2(support2Pos, support2Height);
		
//		double scale = 0.0005;
		//	Vector3d deckOffset = BoardCAD.getInstance().getMachineView().getBoardDeckOffsetPos();
//			Vector3d deckTranslation = new Vector3d();
		//	deckTranslation.sub(deckOffset);
		
//		deckTranslation.x -= (BoardCAD.getInstance().getCurrentBrd().getLength() + (mConfig.getCategory(holdingSystemStr).getDouble(TAILSTOP_POS)/UnitUtils.MILLIMETER_PR_CENTIMETER))/2.0;		
//		deckTranslation.scale(scale*10);	//Since the board coordinates are in cm, scaled to millimeters used by g-code
//		mTransform.set(scale, deckTranslation);
//		mScale.setTransform(mTransform);
		
//		System.out.printf("update3DModel()\n");
	}
	
	public void setSupportBaseLine(double length)
	{
		mSupportStructureArray.setCoordinates(0, new Point3d[]{new Point3d(0,0,0), new Point3d(length,0,0)});		
	}

	public void setSupport1(double pos, double height)
	{
		mSupportStructureArray.setCoordinates(2, new Point3d[]{new Point3d(pos,0,0), new Point3d(pos,0,height)});
		
		//Height of tailstop should be height + 10 cm
		Point3d point = new Point3d();
		mSupportStructureArray.getCoordinate(7, point);
		point.z = height + 100;
		mSupportStructureArray.setCoordinates(7, new Point3d[]{point});		
	}

	public void setSupport2(double pos, double height)
	{
		mSupportStructureArray.setCoordinates(4, new Point3d[]{new Point3d(pos,0,0), new Point3d(pos,0,height)});				
	}

	public void setSupportTailStop(double pos)
	{
		Point3d point = new Point3d();
		mSupportStructureArray.getCoordinate(3, point);
		point.x = pos;
		point.z += 100;

		mSupportStructureArray.setCoordinates(6, new Point3d[]{new Point3d(pos,0,0), point});				
	}

}
