package boardcad.gui.jdk.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import boardcad.FileTools;
import boardcad.gui.jdk.BoardCAD;
import boardcad.i18n.LanguageResource;

abstract public class AbstractPluginHandler {
	
	public void loadPlugins(String directory)
	{
		File file = new File(directory);
		FilenameFilter filter = new FilenameFilter()
		{

//			Accept all directories and brd and s3d files.
			public boolean accept(File f, String str) {

				String extension = FileTools.getExtension(f);
				if (extension != null && extension.equals("jar"))
				{
					return true;
				}

				return false;
			}

		};
		String[] jarFiles = file.list(filter);
		if(jarFiles == null)
			return;
		
		for(int i = 0; i < jarFiles.length; i++)
		{
			try{
				URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file:///" + jarFiles[i])});
				Class c = loader.loadClass(FileTools.getFilename(jarFiles[i]));
				Method m = c.getMethod("getMenu", new Class[] { });
				m.setAccessible(true);
				int mods = m.getModifiers();
				if (m.getReturnType() != void.class || !Modifier.isPublic(mods)) {
					throw new NoSuchMethodException("getMenu");
				}
				JMenu menu = (JMenu)m.invoke(c.newInstance(), new Object[] {  });
				if(menu != null)
				{
					onNewPluginMenu(menu);
				}
	
				m = c.getMethod("getComponent", new Class[] { });
				m.setAccessible(true);
				mods = m.getModifiers();
				if (m.getReturnType() != void.class || !Modifier.isPublic(mods)) {
					throw new NoSuchMethodException("writeDeck");
				}
				JComponent component = (JComponent)m.invoke(c.newInstance(), new Object[] {  });
				if(component != null)
				{
					onNewPluginComponent(component);
				}
				
				//m.invoke(c.newInstance(), new Object[] { "filename", getCurrentBrd() });
	
			}catch (Exception e) {
				String str = LanguageResource.getString("PLUGINLOADERFAILEDMSG_STR") + e.toString();
				JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str, LanguageResource.getString("PLUGINLOADERFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);
			}
		}

	
	}

	abstract protected void onNewPluginMenu(JMenu menu); 
	abstract protected void onNewPluginComponent(JComponent menu); 
	
	
}
