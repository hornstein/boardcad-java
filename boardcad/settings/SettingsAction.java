package boardcad.settings;

public class SettingsAction
{
	String mValue;

	SettingsAction(final String value)
	{
		mValue = value;
	}

	@Override
	public String toString()
	{
		return mValue;
	}

}