package boardcad.gui.jdk;

import boardcad.settings.*;

class BoardCADSettings extends CategorizedSettings
{

	static private BoardCADSettings mInstance = null;

	public static BoardCADSettings getInstance()
	{
		if(mInstance == null) {
			mInstance = new BoardCADSettings();
		}
		return mInstance;
	}

	protected BoardCADSettings() 
	{
		super();
		Settings.SettingChangedCallback defaultCallback = new Settings.SettingChangedCallback()
		{

			public void onSettingChanged(final Object obj) {
				BoardCAD.getInstance().onSettingsChanged();
			}

		};
		setDefaultCallback(defaultCallback);

	}

};
