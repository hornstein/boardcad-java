package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import boardcad.settings.*;
import boardcad.settings.CategorizedSettings.CategorySettingsChangeListener;


public class CategorizedSettingsComponent extends JComponent {
	final static long serialVersionUID = 1L;
	private CategorizedSettings mCategorizedSettings = null;

	private JTabbedPane mTabbedPane = null;

	/**
	 * This method initializes 
	 * 
	 */
	public CategorizedSettingsComponent(final CategorizedSettings settings)
	{
		super();
		mCategorizedSettings = settings;
		initialize();

		mCategorizedSettings.addCategorySettingsChangeListener(new CategorySettingsChangeListener(){
			public void onCategoryRemoved(String categoryName)
			{
				for(int i = 0; i < mTabbedPane.getTabCount(); i++)
				{
					if(mTabbedPane.getTitleAt(i) == categoryName)
					{
						mTabbedPane.remove(i);
						break;
					}
				}				
			}
			public void onCategoryAdded(String categoryName)
			{
				final Settings settings = mCategorizedSettings.getCategory(categoryName);
				SettingsComponent component = new SettingsComponent(settings);
				mTabbedPane.add(categoryName, component);
			}
		});
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() 
	{
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setHgap(10);
		this.setLayout(borderLayout);
		this.setSize(new Dimension(300, 100));
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		this.add(getTabbedPane(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes scrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (mTabbedPane == null) 
		{
			mTabbedPane = new JTabbedPane();

			final Set<String> categories = mCategorizedSettings.getCategories();
			final String[] categoriesArray = categories.toArray(new String[categories.size()]);

			for(int i = 0; i < categories.size(); i++)
			{	
				final Settings settings = mCategorizedSettings.getCategory(categoriesArray[i]);
				
				SettingsComponent component = new SettingsComponent(settings);
				mTabbedPane.add(categoriesArray[i],component);
			}
		}
		return mTabbedPane;
	}
	
}
