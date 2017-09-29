package boardcad.settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


public class CategorizedSettings {
	
	public interface CategorySettingsChangeListener
	{
			void onCategoryRemoved(String categoryName);
			void onCategoryAdded(String categoryName);
	};
	
	private final Map<String, Settings>  mCategorizedSettings = new HashMap<String, Settings>();
	
	Settings.SettingChangedCallback mDefaultCallback = null;
	
	Vector<CategorySettingsChangeListener> mChangeListeners = new Vector<CategorySettingsChangeListener>();
	
	public Settings getCategory(final String category)
	{
		return mCategorizedSettings.get(category);
	}

	public Set<String> getCategories()
	{
		return mCategorizedSettings.keySet();
	}
	
	public void removeCategory(final String category)
	{
		mCategorizedSettings.remove(category);
		
		onCategoryRemoved(category);
	}

	public Settings addCategory(final String category)
	{
		Settings settings = null;
		
		if(mCategorizedSettings.containsKey(category))
		{
			settings = mCategorizedSettings.get(category);
		}
		else
		{
			settings = new Settings();
			mCategorizedSettings.put(category, settings);
			onCategoryAdded(category);			
		}
		
		if(mDefaultCallback != null)
		{
			settings.setDefaultCallback(mDefaultCallback);
		}
		
		return settings;
	}
	
	public void getPreferences() {
		//System.out.println("CategorizedSettings.getPreferences()");
		
		final Set<Map.Entry<String, Settings>> categorizedSettingsSet = mCategorizedSettings.entrySet();
		final Iterator<Map.Entry<String, Settings>> categoriesIterator = categorizedSettingsSet.iterator();

		while(categoriesIterator.hasNext())
		{
			final Map.Entry<String, Settings> e = categoriesIterator.next();
			//System.out.printf("Get preferences for %s", e.getKey());
			final Settings current = e.getValue();
			current.getPreferences();
		}
	}

	public void putPreferences() {
		//System.out.println("CategorizedSettings.putPreferences()");
		
		final Set<Map.Entry<String, Settings>> categorizedSettingsSet = mCategorizedSettings.entrySet();
		final Iterator<Map.Entry<String, Settings>> categoriesIterator = categorizedSettingsSet.iterator();

		while(categoriesIterator.hasNext())
		{
			final Map.Entry<String, Settings> e = categoriesIterator.next();
			//System.out.printf("Put preferences for %s", e.getKey());
			final Settings current = e.getValue();
			current.putPreferences();
		}
	}
	

	public void setDefaultCallback(final Settings.SettingChangedCallback defaultCallback) {
		mDefaultCallback = defaultCallback;
		
		final Set<Map.Entry<String, Settings>> categorizedSettingsSet = mCategorizedSettings.entrySet();
		final Iterator<Map.Entry<String, Settings>> categoriesIterator = categorizedSettingsSet.iterator();

		while(categoriesIterator.hasNext())
		{
			final Map.Entry<String, Settings> e = categoriesIterator.next();
			final Settings current = e.getValue();
			current.setDefaultCallback(mDefaultCallback);
		}
	}
	
	public void addCategorySettingsChangeListener(CategorySettingsChangeListener listener)
	{
		mChangeListeners.add(listener);
	}
	
	public void onCategoryAdded(String category)
	{
		for(CategorySettingsChangeListener listener : mChangeListeners)
		{
			listener.onCategoryAdded(category);
		}
	}
	public void onCategoryRemoved(String category)
	{
		for(CategorySettingsChangeListener listener : mChangeListeners)
		{
			listener.onCategoryRemoved(category);
		}		
	}
}
