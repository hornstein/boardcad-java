package boardcad.settings;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import cadcore.UnitUtils;

public class Settings {

	private Map<String, Setting> mSettings = null;
	SettingChangedCallback mDefaultCallback = null;

	public interface SettingChangedCallback
	{
		void onSettingChanged(Object obj);
	}

	public Settings() 
	{
		mSettings = new LinkedHashMap<String,Setting>();
	}


	public int size()
	{
		return mSettings.size();
	}

	public void clear()
	{
		mSettings.clear();
	}
	
	public void removeObject(final String key)
	{
		mSettings.remove(key);
	}
	
	public void addObject(final String key, final Object value, final String description )
	{
		mSettings.put(key,new Setting(value, description));
	}

	public void addString(final String key, final String value, final String description )
	{
		addObject(key, value, description);
	}

	public void addBoolean(final String key, final boolean value, final String description )
	{
		addObject(key, new Boolean(value), description);
	}

	public void addInteger(final String key, final int value, final String description )
	{
		addObject(key, new Integer(value), description);
	}

	public void addDouble(final String key, final double value, final String description )
	{
		addObject(key, new Double(value), description);
	}

	public void addColor(final String key, final Color value, final String description )
	{
		addObject(key, value, description);
	}

	public void addFileName(final String key, final String value, final String description )
	{
		addObject(key, new FileName(value), description);
	}

	public void addMeasurement(final String key, final double value, final String description )
	{
		addObject(key, new Measurement(value), description);
	}

	public void addEnumeration(final String key, final int value, Map<Integer, String> alternatives, final String description )
	{
		addObject(key, new Enumeration(value, alternatives), description);
	}

	public void addObject(final String key, final Object value, final String description, final SettingChangedCallback cb )
	{
		mSettings.put(key,new Setting(value, description, cb));
	}

	public void addString(final String key, final String value, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, value, description, cb);
	}

	public void addBoolean(final String key, final boolean value, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new Boolean(value), description, cb);
	}

	public void addInteger(final String key, final int value, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new Integer(value), description, cb);
	}

	public void addDouble(final String key, final double value, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new Double(value), description, cb);
	}

	public void addColor(final String key, final Color value, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, value, description, cb);
	}

	public void addFileName(final String key, final String value, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new FileName(value), description, cb);
	}

	public void addMeasurement(final String key, final double measurement, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new Measurement(measurement), description, cb);
	}

	public void addAction(final String key, final String text, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new SettingsAction(text), description, cb);
	}

	public void addEnumeration(final String key, final int value, Map<Integer, String> alternatives, final String description, final SettingChangedCallback cb  )
	{
		addObject(key, new Enumeration(value, alternatives), description, cb);
	}

	public void setObject(final String key, final Object value)
	{
		mSettings.get(key).mValue = value;
	}

	public void setString(final String key, final String value)
	{
		mSettings.get(key).mValue = value;
	}

	public void setBoolean(final String key, final boolean value)
	{
		mSettings.get(key).mValue = new Boolean(value);
	}

	public void setInteger(final String key, final int value)
	{
		mSettings.get(key).mValue = new Integer(value);
	}

	public void setDouble(final String key, final double value)
	{
		mSettings.get(key).mValue = new Double(value);
	}

	public void setColor(final String key, final Color value)
	{
		mSettings.get(key).mValue = value;
	}

	public void setFileName(final String key, final String value)
	{
		mSettings.get(key).mValue = new FileName(value);
	}

	public void setMeasurement(final String key, final double value)
	{
		mSettings.get(key).mValue = new Measurement(value);
	}

	public void setEnumeration(final String key, final int value)
	{
		((Enumeration)mSettings.get(key).mValue).setValue(value);
	}

	public Setting getSetting(final String key)
	{
		return mSettings.get(key);
	}

	public Object getObject(final String key)
	{
		return mSettings.get(key).mValue;
	}

	public String getString(final String key)
	{
		return (String)getObject(key);
	}

	public boolean getBoolean(final String key)
	{
		return ((Boolean)getObject(key)).booleanValue();
	}

	public int getInt(final String key)
	{
		return ((Integer)getObject(key)).intValue();
	}

	public double getDouble(final String key)
	{
		return ((Double)getObject(key)).doubleValue();
	}

	public Color getColor(final String key)
	{
		return (Color)getObject(key);
	}

	public String getFileName(final String key)
	{
		return ((FileName)getObject(key)).toString();
	}

	public double getMeasurement(final String key)
	{
		return ((Measurement)getObject(key)).getValue();
	}

	public int getEnumeration(final String key)
	{
		return ((Enumeration)getObject(key)).getValue();
	}

	public String getKey(final int pos)
	{
		final Set<String> keySet = mSettings.keySet();

		return (String)keySet.toArray()[pos];		
	}

	public String getDescription(final int pos)
	{	
		final Setting setting = get(pos);

		return setting.mDescription;
	}

	public Object getValue(final int pos)
	{
		final Setting setting = get(pos);

		return setting.mValue;
	}
	
	public boolean isHidden(final int pos)
	{
		final Setting setting = get(pos);

		return setting.isHidden();
	}
	
	public boolean isDisabled(final int pos)
	{
		final Setting setting = get(pos);

		return setting.isDisabled();
	}
	
	public List<SettingChangedCallback> getCallbacks(final int pos)
	{

		final Setting setting = get(pos);

		return setting.mCallbacks;
	}

	public Setting get(final int pos)
	{
		final Set<String> keySet = mSettings.keySet();

		final String key = (String)keySet.toArray()[pos];	

		final Setting setting = mSettings.get(key);

		return setting;
	}

	public void set(final int pos, final Object value)
	{
		final Setting setting = get(pos);

		setting.mValue = value;
	}

	public void getPreferences()
	{
		if(mSettings.size() == 0)
		{
			return;
		}

		// Preference keys for this package	
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		final Set<Map.Entry<String, Setting>> settingsSet = mSettings.entrySet();
		final Iterator<Map.Entry<String, Setting>> settingsIterator = settingsSet.iterator();
		
		ArrayList<Setting> signalSettings = new ArrayList<Setting>();

		while(settingsIterator.hasNext())
		{
			final Map.Entry<String, Setting> e = settingsIterator.next();
			final Setting setting = e.getValue();

			final String className = setting.mValue.getClass().getName();

			if(className.compareTo(String.class.getName()) == 0)
			{
				setting.mValue = prefs.get(e.getKey(), (String)setting.mValue);
			}
			else if(className.compareTo(Boolean.class.getName()) == 0)
			{
				setting.mValue = Boolean.valueOf(prefs.getBoolean(e.getKey(), ((Boolean)setting.mValue).booleanValue()));
			}
			else if(className.compareTo(Integer.class.getName()) == 0)
			{
				setting.mValue = Integer.valueOf(prefs.getInt(e.getKey(), ((Integer)setting.mValue).intValue()));
			}
			else if(className.compareTo(Double.class.getName()) == 0)
			{
				setting.mValue = Double.valueOf(prefs.getDouble(e.getKey(), ((Double)setting.mValue).doubleValue()));
			}
			else if(className.compareTo(Color.class.getName()) == 0)
			{
				setting.mValue = new Color(Integer.valueOf(prefs.getInt(e.getKey(), ((Color)setting.mValue).getRGB())));
			}
			else if(className.compareTo(FileName.class.getName()) == 0)
			{
				setting.mValue = new FileName(prefs.get(e.getKey(), ((FileName)setting.mValue).toString()));
			}
			else if(className.compareTo(Measurement.class.getName()) == 0)
			{
				setting.mValue = new Measurement(prefs.get(e.getKey(), ((Measurement)setting.mValue).toString()));
			}
			else if(className.compareTo(Enumeration.class.getName()) == 0)
			{
				((Enumeration)setting.mValue).setValue(prefs.getInt(e.getKey(), ((Enumeration)setting.mValue).mValue));
			}
			else if(className.compareTo(SettingsAction.class.getName()) == 0)
			{
				continue;
			}
			else
			{

			}
						
//			System.out.printf("Settings::getPreferences() setting:%s\n", setting.toString());
			
			signalSettings.add(setting);
			
		}

		Iterator<Setting> signalIterator = signalSettings.iterator();
		while(signalIterator.hasNext())
		{
			signalIterator.next().signal();			
		}

	}

	public void putPreferences()
	{
		if(mSettings.size() == 0)
		{
			return;
		}

		// Preference keys for this package	
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());


		final Set<Map.Entry<String, Setting>> settingsSet = mSettings.entrySet();
		final Iterator<Map.Entry<String, Setting>> settingsIterator = settingsSet.iterator();

		while(settingsIterator.hasNext())
		{
			final Map.Entry<String, Setting> e = settingsIterator.next();
			final Setting setting = e.getValue();

			final String className = setting.mValue.getClass().getName();
			final String key = e.getKey();

			if(className.compareTo(String.class.getName()) == 0)
			{
				prefs.put(key, (String)setting.mValue);
			}
			else if(className.compareTo(Boolean.class.getName()) == 0)
			{
				prefs.putBoolean(key, ((Boolean)setting.mValue).booleanValue() );
			}
			else if(className.compareTo(Integer.class.getName()) == 0)
			{
				prefs.putInt(key, ((Integer)setting.mValue).intValue() );
			}
			else if(className.compareTo(Double.class.getName()) == 0)
			{
				prefs.putDouble(key, ((Double)setting.mValue).doubleValue() );
			}
			else if(className.compareTo(Color.class.getName()) == 0)
			{
				prefs.putInt(key, ((Color)setting.mValue).getRGB() );
			}
			else if(className.compareTo(FileName.class.getName()) == 0)
			{
				prefs.put(key, ((FileName)setting.mValue).toString() );
			}
			else if(className.compareTo(Measurement.class.getName()) == 0)
			{
				prefs.put(key, ((Measurement)setting.mValue).toString() );
			}
			else if(className.compareTo(SettingsAction.class.getName()) == 0)
			{
				continue;
			}
			else if(className.compareTo(Enumeration.class.getName()) == 0)
			{
				prefs.putInt(key, ((Enumeration)setting.mValue).getValue() );
			}
			else
			{

			}

//			System.out.printf("Settings::putPreferences() setting:%s\n", setting.toString());			
		}

	}

	public SettingChangedCallback getDefaultCallback()
	{
		return mDefaultCallback;
	}

	public void setDefaultCallback(final Settings.SettingChangedCallback defaultCallback)
	{
		mDefaultCallback = defaultCallback;
	}


	public class FileName
	{
		String mValue;

		public FileName(final String value)
		{
			mValue = value;
		}

		@Override
		public String toString()
		{
			return mValue;
		}

	}

	public class Measurement
	{
		double mValue;

		public Measurement(final double value)
		{
			mValue = value;
		}

		public Measurement(final String value)
		{
			mValue = UnitUtils.convertInputStringToInternalLengthUnit(value);
		}

		@Override
		public String toString()
		{
			return UnitUtils.convertLengthToCurrentUnit(mValue, true);
		}
		
		public double getValue()
		{
			return mValue;
		}
	}

	public class Enumeration
	{
		protected int mValue;
		Map<Integer, String> mAlternatives = null;

		public Enumeration(final int value, Map<Integer, String> alternatives)
		{
			mValue = value;
			mAlternatives = alternatives;
		}

		@Override
		public String toString()
		{
			return mAlternatives.get(mValue);
		}
		
		public void setValue(int value)
		{
			mValue = value;
		}

		public int getValue()
		{
			return mValue;
		}

		public Map<Integer, String> getAlternatives(){
			return mAlternatives;			
		}
	}


}
