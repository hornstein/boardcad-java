package boardcad.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LanguageResource {

	private static ResourceBundle RESOURCE_BUNDLE = null;

	private LanguageResource() 
	{

	}
	
	public static void init(Object userNode)
	{
		final Preferences prefs = Preferences.userNodeForPackage(userNode.getClass());
		String languageStr = prefs.get("Language", "en");
	
	    Locale l = new Locale (languageStr,"");
	    RESOURCE_BUNDLE = ResourceBundle.getBundle("boardcad.i18n.LanguageResource", l);
	    System.out.printf("Resource language %s\n", RESOURCE_BUNDLE.getLocale().getLanguage());
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
//			System.out.println("LanguageResource.getString() exception: " + e.toString());			
			return '!' + key + '!';
		} catch (NullPointerException e) {
//			System.out.println("LanguageResource.getString() exception: " + e.toString());			
			return '!' + key + '!';
		}
	}
}
