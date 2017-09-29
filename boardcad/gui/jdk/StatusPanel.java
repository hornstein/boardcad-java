package boardcad.gui.jdk;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import boardcad.i18n.LanguageResource;

//=========================================================Status Panel
class StatusPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
//	private UserInterface user_interface;
	private JLabel status_text;
	private String point_name, mode;
	private int x,y,z;

	public StatusPanel()
	{
//		user_interface=ui;
		setLayout(new FlowLayout(FlowLayout.LEFT,2,0));
		status_text=new JLabel(LanguageResource.getString("NURBSSURFACE_STR"));
		point_name="";
		mode="";
		x=0;
		y=0;
		z=0;
		add(status_text);
	}

	public void set_mode(String text)
	{
		mode=text;
		update_status();
	}
	public void set_point_name(String text)
	{
		point_name=text;
		update_status();
	}

	public void set_coordinates(double x_coord, double y_coord, double z_coord)
	{
		x=(int)x_coord;
		y=(int)y_coord;
		z=(int)z_coord;
		update_status();
	}

	private void update_status()
	{
		String text;
		text=mode + ":" + point_name+": x=" + x + " y=" + y + " z=" + z;
		status_text.setText(text);
	}
}

