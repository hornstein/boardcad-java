package boardcam.toolpathgenerators.ext;

import boardcad.settings.Settings;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcad.i18n.LanguageResource;
import boardcam.MachineConfig;

public class CoordinateScaling {

	static String SCALE_X     = "ScaleX";
	static String SCALE_Y     = "ScaleY";
	static String SCALE_Z     = "ScaleZ";
	static String SCALE_A     = "ScaleA";
	static String SCALE_F     = "ScaleF";
	
	private double[] mScales = new double[5];
	
	MachineConfig mConfig;
	
	public CoordinateScaling(MachineConfig config) 
	{
		mConfig = config;
		
		final Settings scalingSettings = mConfig.addCategory(LanguageResource.getString("SCALINGCATEGORY_STR"));
		
		SettingChangedCallback scaleChangeCb = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{
				mScales[0] = scalingSettings.getDouble(SCALE_X);
				mScales[1] = scalingSettings.getDouble(SCALE_Y);
				mScales[2] = scalingSettings.getDouble(SCALE_Z);
				mScales[3] = scalingSettings.getDouble(SCALE_A);
				mScales[4] = scalingSettings.getDouble(SCALE_F);
			}	
		};
		
		scalingSettings.addDouble(SCALE_X,  new Double(1.0), LanguageResource.getString("SCALE_X_STR"), scaleChangeCb);
		scalingSettings.addDouble(SCALE_Y,  new Double(1.0), LanguageResource.getString("SCALE_Y_STR"), scaleChangeCb);
		scalingSettings.addDouble(SCALE_Z,  new Double(1.0), LanguageResource.getString("SCALE_Z_STR"), scaleChangeCb);
		scalingSettings.addDouble(SCALE_A,  new Double(1.0), LanguageResource.getString("SCALE_A_STR"), scaleChangeCb);
		scalingSettings.addDouble(SCALE_F,  new Double(1.0), LanguageResource.getString("SCALE_F_STR"), scaleChangeCb);

	}
	
	public double getXScale(){return mScales[0];}
	public double getYScale(){return mScales[1];}
	public double getZScale(){return mScales[2];}
	public double getAScale(){return mScales[3];}
	public double getFScale(){return mScales[4];}
	public double[] getScales(){return mScales;}
}