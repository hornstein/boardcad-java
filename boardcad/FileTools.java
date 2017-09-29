package boardcad;

import java.io.File;

public class FileTools
{
	public static String getExtension(String filename) {
		String ext = "";
		int i = filename.lastIndexOf('.');

		if (i > 0 &&  i < filename.length() - 1) {
			ext = filename.substring(i+1).toLowerCase();
		}
		return ext;
	}

	public static String getExtension(File f) {
		String s = f.getName();

		return getExtension(s);
	}

	public static String setExtension(String filename, String setExt) {
		String tmp = filename;
		int i = tmp.lastIndexOf('.');

		if (i > 0 &&  i < tmp.length() - 1) {
			tmp = tmp.substring(0,i+1);
		}else if(i == -1)
		{
			tmp = tmp.concat(".");
		}

		tmp = tmp.concat(setExt);

		return tmp;
	}

	//Append without affecting the extension
	public static String append(String filename, String append) {
		String ext = getExtension(filename);
		int i = filename.lastIndexOf('.');
		String tmp = filename.substring(0,i);

		tmp = tmp.concat(append);
		tmp = tmp.concat(".");
		tmp = tmp.concat(ext);

		return tmp;
	}

	public static String getFilename(String filename) {
		String name = null;
		int i = filename.lastIndexOf('/');
		if(i == -1)
		{
			i = filename.lastIndexOf('\\');
		}
		int j = filename.lastIndexOf('.');
		if(j == -1)
		{
			j = filename.length();
		}

		if (i < filename.length() - 1 && j > i && i < filename.length() - 1) {
			name = filename.substring(i,j).toLowerCase();
		}
		return name;
	}
}
