package boardcam.cutters;

import java.util.HashMap;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import board.AbstractBoard;
import boardcad.gui.jdk.Machine3DView;
import boardcad.settings.Settings;
import boardcad.settings.Settings.Enumeration;
import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;

public class RotatingBoardCutter extends AbstractCutter
{
	public static String ROTATINGBOARDCUTTER_TYPE     = "RotatingBoardCutterType";

	private AbstractCutter mCutter;
	private Vector2d mUpVector = new Vector2d(0.0, 1.0);
	private Matrix3d mRotMatrix = new Matrix3d();
	private Machine3DView mView;	//TODO: bad dependency, GUI inside cutter?!
	private MachineConfig mConfig;
	
	public RotatingBoardCutter(MachineConfig config, Machine3DView view)
	{
		mView = view;
		mConfig = config;
		
		Settings.SettingChangedCallback onCutterTypeChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				System.out.printf("RotatingBoardCutter Setting changed\n");
				Settings cutterSettings = mConfig.getCategory(LanguageResource.getString("CUTTERCATEGORY_STR"));
				cutterSettings.putPreferences();
				cutterSettings.clear();

				Enumeration enu = (Enumeration)obj;
				
				switch(enu.getValue())
				{
				case 0:
					mCutter = new SimpleBullnoseCutter(mConfig);
					break;
				case 1:
					mCutter = new STLCutter(mConfig);
					break;
				}
				
				cutterSettings.getPreferences();

				if(mView != null)
				{
					mView.setCutterModel(mCutter.get3DModel());
					mView.update();
				}
			}
		};
		
		Settings generalSettings = mConfig.addCategory(LanguageResource.getString("GENERALCATEGORY_STR"));
		
		HashMap<Integer, String> cutterTypes = new HashMap<Integer, String>();
		cutterTypes.put(0, LanguageResource.getString("BULLNOSECUTTER_STR"));
		cutterTypes.put(1, LanguageResource.getString("STLCUTTER_STR"));
		generalSettings.addEnumeration(ROTATINGBOARDCUTTER_TYPE, 0, cutterTypes, LanguageResource.getString("ROTATINGBOARDCUTTERTYPE_STR"), onCutterTypeChanged);
		
		mCutter = new SimpleBullnoseCutter(mConfig);
	}
	
	public void init()
	{
		
	}

	@Override
	public double[] calcOffset(Point3d pos, Vector3d normal, AbstractBoard board) 
	{
		//Find the angle to rotate the board
		Vector2d normal2D = new Vector2d(normal.y,normal.z);
		double angle = normal2D.angle(mUpVector);
		angle *= (normal2D.x > 0.0) ? 1.0 : -1.0;
		
		//System.out.printf("Angle:%f\n", angle);
		
		mRotMatrix.rotX(angle);
		
		Point3d rotatedPos = new Point3d();
		mRotMatrix.transform(pos, rotatedPos);
		
		Vector3d rotatedNormal = new Vector3d();
		mRotMatrix.transform(normal, rotatedNormal);
		
		mView.setModelRotation(angle);
		
		double[] offset =  mCutter.calcOffset(rotatedPos, rotatedNormal, board);
		
		return new double[]{offset[0], offset[1], offset[2], angle};
	}
	

	public void setStayAwayFromStringer(boolean stayAwayFromStringer)
	{
		mCutter.setStayAwayFromStringer(stayAwayFromStringer);
	}
	
	public void setStringerWidth(double stringerWidth)
	{
		mCutter.setStringerWidth(stringerWidth);
	}

	public boolean checkCollision(Point3d pos, AbstractBoard board)
	{
		return mCutter.checkCollision(pos, board);
	}
	
	public BranchGroup get3DModel()
	{
		return mCutter.get3DModel();
	}

	public void update3DModel()
	{
		mCutter.update3DModel();
	}
	
	public Point3d getNoseCutOffPoint(int step, AbstractBoard board, boolean deckSide)
	{
		return mCutter.getNoseCutOffPoint(step, board, deckSide);
	}
	
	public Vector3d getNoseCutOffNormal(int step, AbstractBoard board, boolean deckSide)
	{
		return mCutter.getNoseCutOffNormal(step, board, deckSide);
	}

	public boolean isNoseCutOffFinished(int step)
	{
		return mCutter.isNoseCutOffFinished(step);
	}
	
	public Point3d getTailCutOffPoint(int step, AbstractBoard board, boolean deckSide)
	{
		return mCutter.getTailCutOffPoint(step, board, deckSide);
	}
	
	public Vector3d getTailCutOffNormal(int step, AbstractBoard board, boolean deckSide)
	{
		return mCutter.getTailCutOffNormal(step, board, deckSide);
	}

	public boolean isTailCutOffFinished(int step)
	{
		return mCutter.isTailCutOffFinished(step);
	}
}
