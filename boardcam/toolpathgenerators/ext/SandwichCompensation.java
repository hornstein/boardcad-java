package boardcam.toolpathgenerators.ext;

import java.awt.geom.Point2D;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import board.BezierBoard;
import boardcad.settings.Settings;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;

public class SandwichCompensation
{
	public static String SANDWICH_DECK_COMPENSATION_ON     = "SandwichDeckCompensationOn";
	public static String SANDWICH_DECK_THICKNESS     = "SandwichDeckThickness";
	public static String SANDWICH_DECK_RAIL_LIMIT     = "SandwichDeckRailLimit";
	public static String SANDWICH_BOTTOM_COMPENSATION_ON     = "SandwichBottomCompensationOn";
	public static String SANDWICH_BOTTOM_THICKNESS     = "SandwichBottomThickness";
	public static String SANDWICH_BOTTOM_RAIL_LIMIT     = "SandwichBottomRailLimit";
	public static String SANDWICH_OUTLINE_COMPENSATION_ON     = "SandwichOutlineCompensationOn";
	public static String SANDWICH_OUTLINE_COMPENSATION     = "SandwichOutlineCompensation";
	
	private boolean mUseDeckSandwichCompensation = false;
	private double mDeckSandwichThickness = 0.0;
	private double mDeckRailLimit = 0.0;

	private boolean mUseBottomSandwichCompensation = false;
	private double mBottomSandwichThickness = 0.0;
	private double mBottomRailLimit = 0.0;
	
	private boolean mUseSandwichOutlineCompensation = false;
	private double mOutlineCompensation = 0.0;
	
	MachineConfig mConfig;
	
	public SandwichCompensation(MachineConfig config) 
	{
		mConfig = config;
		
		final Settings sandwichCompensationSettings = mConfig.addCategory(LanguageResource.getString("SANDWICHCOMPENSATIONCATEGORY_STR"));

		SettingChangedCallback deckOnCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mUseDeckSandwichCompensation = sandwichCompensationSettings.getBoolean(SANDWICH_DECK_COMPENSATION_ON);
				sandwichCompensationSettings.getSetting(SANDWICH_DECK_THICKNESS).setDisabled(!mUseDeckSandwichCompensation);
			}	
		};
		sandwichCompensationSettings.addBoolean(SANDWICH_DECK_COMPENSATION_ON,  false, LanguageResource.getString("SANDWICH_DECK_COMPENSATION_ENABLED_STR"), deckOnCb);
		SettingChangedCallback deckThicknessCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mDeckSandwichThickness = sandwichCompensationSettings.getMeasurement(SANDWICH_DECK_THICKNESS);
			}	
		};
		sandwichCompensationSettings.addMeasurement(SANDWICH_DECK_THICKNESS,  new Double(0.3), LanguageResource.getString("SANDWICH_DECK_COMPENSATION_STR"), deckThicknessCb);
		SettingChangedCallback deckRailLimitCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mBottomRailLimit = sandwichCompensationSettings.getMeasurement(SANDWICH_DECK_RAIL_LIMIT);
			}	
		};
		sandwichCompensationSettings.addMeasurement(SANDWICH_DECK_RAIL_LIMIT,  new Double(0.0), LanguageResource.getString("SANDWICH_DECK_RAIL_LIMIT_STR"), deckRailLimitCb);

		SettingChangedCallback bottomOnCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mUseBottomSandwichCompensation = sandwichCompensationSettings.getBoolean(SANDWICH_BOTTOM_COMPENSATION_ON);
				sandwichCompensationSettings.getSetting(SANDWICH_BOTTOM_THICKNESS).setDisabled(!mUseBottomSandwichCompensation);
			}	
		};
		sandwichCompensationSettings.addBoolean(SANDWICH_BOTTOM_COMPENSATION_ON,  false, LanguageResource.getString("SANDWICH_BOTTOM_COMPENSATION_ENABLED_STR"), bottomOnCb);
		SettingChangedCallback bottomThicknessCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mBottomSandwichThickness = sandwichCompensationSettings.getMeasurement(SANDWICH_BOTTOM_THICKNESS);
			}	
		};
		sandwichCompensationSettings.addMeasurement(SANDWICH_BOTTOM_THICKNESS,  new Double(0.3), LanguageResource.getString("SANDWICH_BOTTOM_COMPENSATION_STR"), bottomThicknessCb);
		SettingChangedCallback bottomRailLimitCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mBottomRailLimit = sandwichCompensationSettings.getMeasurement(SANDWICH_BOTTOM_RAIL_LIMIT);
			}	
		};
		sandwichCompensationSettings.addMeasurement(SANDWICH_BOTTOM_RAIL_LIMIT,  new Double(0.0), LanguageResource.getString("SANDWICH_BOTTOM_RAIL_LIMIT_STR"), bottomRailLimitCb);

		SettingChangedCallback outlineOnCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mUseSandwichOutlineCompensation = sandwichCompensationSettings.getBoolean(SANDWICH_OUTLINE_COMPENSATION_ON);
				sandwichCompensationSettings.getSetting(SANDWICH_OUTLINE_COMPENSATION).setDisabled(!mUseSandwichOutlineCompensation);
			}	
		};
		sandwichCompensationSettings.addBoolean(SANDWICH_OUTLINE_COMPENSATION_ON,  false, LanguageResource.getString("SANDWICH_OUTLINE_COMPENSATION_ENABLED_STR"), outlineOnCb);
		SettingChangedCallback outlineCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mOutlineCompensation = sandwichCompensationSettings.getMeasurement(SANDWICH_OUTLINE_COMPENSATION);
			}	
		};
		sandwichCompensationSettings.addMeasurement(SANDWICH_OUTLINE_COMPENSATION,  new Double(0.3), LanguageResource.getString("SANDWICH_OUTLINE_COMPENSATION_STR"), outlineCb);
	}
	
	public boolean compensateDeckCuts()
	{
		return mUseDeckSandwichCompensation;
	}

	public boolean compensateBottomCuts()
	{
		return mUseBottomSandwichCompensation;
	}
	
	public boolean compensateOutlineCuts()
	{
		return mUseSandwichOutlineCompensation;
	}
	
	public Point3d compensateDeckCut(Point3d pos, Vector3d normal)
	{
		if(mUseDeckSandwichCompensation == false)
		{
			return pos;
		}
		
		if(mDeckRailLimit > 0.0)
		{
			BezierBoard brd = mConfig.getBoard();
			
			double distanceFromRail = brd.getOutline().getDistanceToPoint(new Point2D.Double(pos.x,pos.y));
			
			if(distanceFromRail < mDeckRailLimit)
			{
				return pos;
			}
		}
		
		Vector3d tmp = new Vector3d();
		tmp.set(normal);
		tmp.normalize();
		tmp.scale(mDeckSandwichThickness);
		
		Point3d result = new Point3d();
		result.set(pos);
		result.sub(tmp);
		
		System.out.printf("SandwichCompensation.compensateDeckCut()\n");


		return result;
	}

	public Point3d compensateBottomCut(Point3d pos, Vector3d normal)
	{
		if(mUseBottomSandwichCompensation == false)
		{
			return pos;
		}
		
		if(mBottomRailLimit > 0.0)
		{
			BezierBoard brd = mConfig.getBoard();
			
			double distanceFromRail = brd.getOutline().getDistanceToPoint(new Point2D.Double(pos.x,pos.y));
			
			if(distanceFromRail < mBottomRailLimit)
			{
				return pos;
			}
		}
		
		Vector3d tmp = new Vector3d();
		tmp.set(normal);
		tmp.normalize();
		tmp.scale(mBottomSandwichThickness);
		
		Point3d result = new Point3d();
		result.set(pos);
		result.sub(tmp);
		
		System.out.printf("SandwichCompensation.compensateBottomCut()\n");

		return result;
	}

	public Point3d compensateOutlineCut(Point3d pos, Vector3d normal)
	{
		if(mUseSandwichOutlineCompensation == false)
		{
			return pos;
		}
		
		Vector3d tmp = new Vector3d();
		tmp.set(normal);
		tmp.normalize();
		tmp.scale(mOutlineCompensation);
		
		Point3d result = new Point3d();
		result.set(pos);
		result.sub(tmp);
		
		System.out.printf("SandwichCompensation.compensateOutlineCut()\n");
		
		return result;
	}
	
	

}
