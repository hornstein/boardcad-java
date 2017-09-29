package boardcad.settings;

import java.util.ArrayList;
import java.util.List;

import boardcad.settings.Settings.SettingChangedCallback;

public class Setting
{
	protected Object mValue;
	protected String mDescription;
	protected boolean mDisabled = false;
	protected boolean mHidden = false;
	
	public boolean isDisabled() {
		return mDisabled;
	}

	public void setDisabled(boolean disabled) {
		this.mDisabled = disabled;
	}

	public boolean isHidden() {
		return mHidden;
	}

	public void setHidden(boolean hidden) {
		this.mHidden = hidden;
	}

	public List<SettingChangedCallback> mCallbacks = new ArrayList<SettingChangedCallback>();

	public Setting(final Object value, final String desc)
	{
		mValue = value;
		mDescription = desc;
	}

	public Setting(final Object value, final String desc, final SettingChangedCallback cb)
	{
		mValue = value;
		mDescription = desc;
		mCallbacks.add(cb);
	}
	
	public String toString()
	{
		return mDescription + ":" + mValue.toString();
	}
	
	public void addCallback(final SettingChangedCallback cb)
	{
		mCallbacks.add(cb);
	}
	
	public void removeCallback(final SettingChangedCallback cb)
	{
		mCallbacks.remove(cb);
	}
	
	public void signal()
	{
		if(mCallbacks != null)
		{
			for (SettingChangedCallback cb : mCallbacks)
			{
				cb.onSettingChanged(mValue);					
			}
		}
	}
};
