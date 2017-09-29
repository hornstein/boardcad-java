package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import cadcore.NurbsPoint;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;


interface AbstractEditor 
{
	void fit_all();
}

//=========================================================Design Panel
class DesignPanel extends Panel implements AbstractEditor
{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//	private UserInterface user_interface;
	private StatusPanel status_panel;
	private RockerView rocker_view;
	private EdgeView edge_view;
	private OutlineView outline_view;
	private ThreeDView rendered_view;	
	private ThreeDView2 threed_view;
	

	public DesignPanel(BoardHandler board_handler, StatusPanel sp)
	{
//		user_interface=ui;
		status_panel=sp;

        threed_view=new ThreeDView2(board_handler, this);
		rocker_view=new RockerView(board_handler, this);
		edge_view=new EdgeView(board_handler, this);
		outline_view=new OutlineView(board_handler, this);
		rendered_view=new ThreeDView(board_handler, this);

/*
		setLayout(new GridLayout(2,2,3,3));

		add(rocker_view);
		add(edge_view);
		add(outline_view);
		add(threed_view);
		
		doLayout();
		redraw();
//		threed_view.setSize(getPreferredSize());
		threed_view.doLayout();
*/
		setLayout(new GridLayout(1,1));
		add(threed_view);
		doLayout();
//		redraw();
//		threed_view.setSize(getPreferredSize());
//		threed_view.doLayout();

	}

	public void view_outline()
	{
		remove(rocker_view);
		remove(edge_view);
		remove(outline_view);
		remove(threed_view);
		setLayout(new GridLayout(1,1));
		add(outline_view);
		doLayout();
		redraw();
	}

	public void view_rocker()
	{
		remove(rocker_view);
		remove(edge_view);
		remove(outline_view);
		remove(threed_view);
		setLayout(new GridLayout(1,1));
		add(rocker_view);
		doLayout();
		redraw();
	}
	public void view_edge()
	{
		remove(rocker_view);
		remove(edge_view);
		remove(outline_view);
		remove(threed_view);
		setLayout(new GridLayout(1,1));
		add(edge_view);
		doLayout();
		redraw();
	}
	public void view_3d()
	{
		remove(rocker_view);
		remove(edge_view);
		remove(outline_view);
		remove(threed_view);
		setLayout(new GridLayout(1,1));
		add(threed_view);
		doLayout();
//		redraw();
//		threed_view.setSize(getPreferredSize());
//		threed_view.doLayout();
	}

	public void view_rendered()
	{
		remove(rocker_view);
		remove(edge_view);
		remove(outline_view);
		remove(threed_view);
		setLayout(new GridLayout(1,1));
		add(rendered_view);
		doLayout();
//		redraw();
		rendered_view.setSize(getPreferredSize());
		rendered_view.doLayout();
	}

	public void view_all()
	{
		remove(rocker_view);
		remove(edge_view);
		remove(outline_view);
		remove(threed_view);

		setLayout(new GridLayout(2,2,3,3));

		add(rocker_view);
		add(edge_view);
		add(outline_view);
		add(threed_view);

		doLayout();
		redraw();
	}

	public void fit_all()
	{
		rocker_view.fit();
		edge_view.fit();
		outline_view.fit();
		threed_view.fit();
	}

	public void set_point_name(String text)
	{
		status_panel.set_point_name(text);
		status_panel.doLayout();
	}

	public void set_coordinates(double x, double y, double z)
	{
		status_panel.set_coordinates(x,y,z);
		status_panel.doLayout();
	}

	public void update_3d()
	{
		rendered_view.update();


	}

	public void addModel(BranchGroup model)
	{
		rendered_view.addModel(model);
	}

	public Shape3D getShape()
	{
		return rendered_view.getShape();
	}

	public void set_airbrush(String filename)
	{
		rendered_view.set_airbrush(filename);
	}

	public void redraw()
	{
		rocker_view.repaint();
		edge_view.repaint();
		outline_view.repaint();
		threed_view.repaint();
		rendered_view.repaint();
//		rendered_view.doLayout();

	}
	
	public ThreeDView get3DView()
	{
		return rendered_view;
	}
}

//=========================================================Views

abstract class View extends Canvas
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected BoardHandler board_handler;
	protected PopupMenu view_menu;
	protected DesignPanel design_panel;

	protected static boolean is_marked;

	protected Dimension off_dimension;
	protected Image off_image;
	protected Graphics g;
	protected double scale;		//transforms between image and board coordinates
	protected int offset_x;		//position of origo in image coordinates
	protected int offset_y;

	protected void clear_graphics()
	{
		Dimension d=getSize();

		if (g==null || d.width!=off_dimension.width || d.height!=off_dimension.height) 
		{
			off_dimension = d;
			off_image = createImage(d.width, d.height);
			g = off_image.getGraphics();
		}

		g.setColor(BoardCAD.getInstance().getBackgroundColor());
		g.fillRect(0, 0, d.width, d.height);
//		g.setColor(getBackground());
//		g.fillRect(0, 0, d.width, d.height);
//		g.draw3DRect(0,0,d.width-1,d.height-1,true);
//		g.draw3DRect(3,3,d.width-7,d.height-7,false);

	}
}


class EdgeView extends View implements ItemListener, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MenuItem vee_item;
	private MenuItem concave_item;
	private CheckboxMenuItem y_locked_item;
	private CheckboxMenuItem z_locked_item;
	private MenuItem fit_item;

	private static boolean is_dragged;
	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;
	private static int zoom_x, zoom_y, zoom_width, zoom_height;

	private View the_view;

	public EdgeView(BoardHandler bh, DesignPanel dp)
	{
		board_handler=bh;
		design_panel=dp;
		view_menu=new PopupMenu();

		is_marked=false;
		is_dragged=false;
		scale=1;
		offset_x=0;
		offset_y=0;

		vee_item=new MenuItem("Set Vee");
		concave_item=new MenuItem("Set Concave");
		y_locked_item=new CheckboxMenuItem("y locked", false);
		z_locked_item=new CheckboxMenuItem("z locked", false);
		fit_item=new MenuItem("Fit");

		vee_item.addActionListener(this);
		concave_item.addActionListener(this);
		y_locked_item.addActionListener(this);
		z_locked_item.addActionListener(this);
		fit_item.addActionListener(this);

		view_menu.add(fit_item);
		view_menu.addSeparator();
		view_menu.add(y_locked_item);
		view_menu.add(z_locked_item);
		view_menu.addSeparator();
		view_menu.add(vee_item);
		view_menu.add(concave_item);

		add(view_menu);	
		the_view=this;
		addMouseListener(EdgeMouse);
		addMouseMotionListener(EdgeMouseMotion);
	}

	public void fit()
	{
		if(getSize().width == 0)
			return;
		
		if(off_dimension==null)
			return;
		
		double width=board_handler.get_segment_width();
		if (width<100)
			width=100;

		scale=(double)(off_dimension.width-40)/width;
		offset_y=(int)(board_handler.get_edge_offset()*scale+2*off_dimension.height/3);
		offset_x=off_dimension.width/2;
		repaint();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==vee_item)
		{
		}
		else if(e.getSource()==concave_item)
		{
		}
		else if(e.getSource()==fit_item)
		{
			fit();
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
	}

	public void paint(Graphics g2) 
	{
		update(g2);
	}

	public void update(Graphics g2) 
	{
		clear_graphics();		
		
		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();

//		Turn on antialiasing, so painting is smooth.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_TEXT_ANTIALIAS_ON:RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		if(is_dragged && !is_marked)
		{
			g2d.setColor(Color.black);
			g2d.drawRect(zoom_x, zoom_y, zoom_width, zoom_height);
		}
		board_handler.draw_edge(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y);
		g2.drawImage(off_image, 0, 0, this);
	}

	MouseMotionListener EdgeMouseMotion=new MouseMotionAdapter()
	{
		public void mouseDragged(MouseEvent e)
		{
			if(e.isMetaDown())
			{
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				is_dragged=true;
				if(is_marked)
				{
					if(!y_locked_item.getState())
						board_handler.set_y((-e.getY()+offset_y)/scale);
					if(!z_locked_item.getState())
						board_handler.set_z((e.getX()-offset_x)/scale);
					board_handler.set_point();
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
					design_panel.redraw();
				}
				else
				{
					dragged_x=e.getX();
					dragged_y=e.getY();
					if(dragged_y>clicked_y)
						dragged_y=clicked_y+(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					else
						dragged_y=clicked_y-(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					if(clicked_x<dragged_x)
						zoom_x=clicked_x;
					else
						zoom_x=dragged_x;
					if(clicked_y<dragged_y)
						zoom_y=clicked_y;
					else
						zoom_y=dragged_y;
					zoom_width=Math.abs(clicked_x-dragged_x);
					zoom_height=Math.abs(clicked_y-dragged_y);
					repaint();
				}
			}
		}

		public void mouseMoved(MouseEvent e)
		{
			if(!is_marked)
				design_panel.set_coordinates(board_handler.get_x(), (-e.getY()+offset_y)/scale, (e.getX()-offset_x)/scale);
		}
	};

	MouseListener EdgeMouse=new MouseAdapter()
	{
		public void mousePressed(MouseEvent e)
		{
			is_dragged=false;
			if(e.isMetaDown())
			{
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				clicked_x=e.getX();
				clicked_y=e.getY();
				board_handler.set_z((clicked_x-offset_x)/scale);
				board_handler.set_y((-clicked_y+offset_y)/scale);
				is_marked=board_handler.edge_mark(scale);
				if(is_marked)
				{
					design_panel.set_point_name(board_handler.get_point_name());
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
				}
				else
					design_panel.set_point_name("");
				design_panel.redraw();
			}
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isMetaDown())
			{
				view_menu.show(the_view, e.getX(), e.getY());
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				if(is_dragged)
				{
					is_dragged=false;
					if(is_marked)
					{
						if(!y_locked_item.getState())
							board_handler.set_y((-e.getY()+offset_y)/scale);
						if(!z_locked_item.getState())
							board_handler.set_z((e.getX()-offset_x)/scale);
						board_handler.set_point();
						is_marked=board_handler.edge_mark(scale);
						design_panel.redraw();
					}
					else
					{
						if(clicked_x<dragged_x)
							zoom_x=clicked_x;
						else
							zoom_x=dragged_x;
						if(clicked_y<dragged_y)
							zoom_y=clicked_y;
						else
							zoom_y=dragged_y;
						zoom_width=Math.abs(clicked_x-dragged_x);
						zoom_height=Math.abs(clicked_y-dragged_y);

						scale=(double)(off_dimension.width)/zoom_width*scale;
						offset_x=(int)((double)(off_dimension.width)/zoom_width*(offset_x-zoom_x));
						offset_y=(int)((double)(off_dimension.width)/zoom_width*(offset_y-zoom_y));
						repaint();
					}
				}
			}
		}
	};
}

class OutlineView extends View implements ItemListener, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CheckboxMenuItem curvature_item;
	private CheckboxMenuItem t_under_item;
	private CheckboxMenuItem x_locked_item;
	private CheckboxMenuItem z_locked_item;
	private CheckboxMenuItem blank_item;
	private CheckboxMenuItem bottom_cut_item;
	private CheckboxMenuItem deck_cut_item;
	private MenuItem fit_item;
	private MenuItem zoom_in_item;
	private MenuItem zoom_out_item;

	private static boolean is_dragged;
	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;
	private static int zoom_x, zoom_y, zoom_width, zoom_height;

	private View the_view;

	public OutlineView(BoardHandler bh, DesignPanel dp)
	{
		board_handler=bh;
		design_panel=dp;
		view_menu=new PopupMenu();

		is_marked=false;
		is_dragged=false;
		scale=1;
		offset_x=0;
		offset_y=0;

		curvature_item=new CheckboxMenuItem("Curvature", false);
		t_under_item=new CheckboxMenuItem("Tucked Under", false);
		x_locked_item=new CheckboxMenuItem("x locked", true);
		z_locked_item=new CheckboxMenuItem("z locked", false);
		blank_item=new CheckboxMenuItem("View blank", false);
		bottom_cut_item=new CheckboxMenuItem("View bottom cut", false);
		deck_cut_item=new CheckboxMenuItem("View deck cut", false);
		fit_item=new MenuItem("Fit");
		zoom_in_item=new MenuItem("Zoom in");
		zoom_out_item=new MenuItem("Zoom out");

		curvature_item.addItemListener(this);
		t_under_item.addItemListener(this);
		x_locked_item.addItemListener(this);
		z_locked_item.addItemListener(this);
		blank_item.addItemListener(this);
		bottom_cut_item.addItemListener(this);
		deck_cut_item.addItemListener(this);
		fit_item.addActionListener(this);
		zoom_in_item.addActionListener(this);
		zoom_out_item.addActionListener(this);

		view_menu.add(fit_item);
		view_menu.add(zoom_in_item);
		view_menu.add(zoom_out_item);
		view_menu.addSeparator();
		view_menu.add(x_locked_item);
		view_menu.add(z_locked_item);
		view_menu.addSeparator();
		view_menu.add(curvature_item);
		view_menu.add(t_under_item);
		view_menu.addSeparator();
		view_menu.add(blank_item);
		view_menu.add(bottom_cut_item);
		view_menu.add(deck_cut_item);

		add(view_menu);	
		the_view=this;
		addMouseListener(OutlineMouse);
		addMouseMotionListener(OutlineMouseMotion);
	}

	public void fit()
	{
		if(getSize().width == 0)
			return;
		
		if(off_dimension==null)
			return;
		
		scale=(double)(off_dimension.width-40)/(double)board_handler.get_board_length();
		offset_x=20;
		offset_y=(int)(off_dimension.height/2);
		repaint();
	}

	public void zoom_in()
	{
		scale=scale*1.5;
		repaint();
	}

	public void zoom_out()
	{
		scale=scale/1.5;
		repaint();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==fit_item)
		{
			fit();
		}
		else if(e.getSource()==zoom_in_item)
		{
			zoom_in();
		}
		else if(e.getSource()==zoom_out_item)
		{
			zoom_out();
		}


	}

	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource()==curvature_item)
		{
			repaint();
		}
		else if(e.getSource()==t_under_item)
		{
			repaint();
		}
		else if(e.getSource()==blank_item)
		{
			repaint();
		}
		else if(e.getSource()==bottom_cut_item)
		{
			repaint();
		}
		else if(e.getSource()==deck_cut_item)
		{
			repaint();
		}
	}

	public void paint(Graphics g2) 
	{
		update(g2);
	}

	public void update(Graphics g2) 
	{
		clear_graphics();
		if(is_dragged && !is_marked)
		{
			g.setColor(Color.black);
			g.drawRect(zoom_x, zoom_y, zoom_width, zoom_height);
		}
		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();

//		Turn on antialiasing, so painting is smooth.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_TEXT_ANTIALIAS_ON:RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		
		board_handler.draw_outline(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y, t_under_item.getState(),bottom_cut_item.getState(),deck_cut_item.getState(), false);

		if(blank_item.getState())
			board_handler.draw_blank_outline(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y, t_under_item.getState());


		if(curvature_item.getState())
			board_handler.draw_outline_curvature(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y);

		g2.drawImage(off_image, 0, 0, this);
	}

	MouseMotionListener OutlineMouseMotion=new MouseMotionAdapter()
	{
		public void mouseDragged(MouseEvent e)
		{
			if(e.isMetaDown())
			{
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				is_dragged=true;
				if(is_marked)
				{
					if(!x_locked_item.getState())
						board_handler.set_x((e.getX()-offset_x)/scale);
					if(!z_locked_item.getState())
						board_handler.set_z((e.getY()-offset_y)/scale);
					board_handler.set_point();
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
					design_panel.redraw();
				}
				else
				{
					dragged_x=e.getX();
					dragged_y=e.getY();
					if(dragged_y>clicked_y)
						dragged_y=clicked_y+(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					else
						dragged_y=clicked_y-(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					if(clicked_x<dragged_x)
						zoom_x=clicked_x;
					else
						zoom_x=dragged_x;
					if(clicked_y<dragged_y)
						zoom_y=clicked_y;
					else
						zoom_y=dragged_y;
					zoom_width=Math.abs(clicked_x-dragged_x);
					zoom_height=Math.abs(clicked_y-dragged_y);
					repaint();
				}
			}
		}

		public void mouseMoved(MouseEvent e)
		{
			if(!is_marked)
				design_panel.set_coordinates((e.getX()-offset_x)/scale, board_handler.get_y(), (e.getY()-offset_y)/scale);
		}
	};

	MouseListener OutlineMouse=new MouseAdapter()
	{
		public void mousePressed(MouseEvent e)
		{
			is_dragged=false;
			if(e.isMetaDown())
			{
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				clicked_x=e.getX();
				clicked_y=e.getY();
				board_handler.set_x((clicked_x-offset_x)/scale);
				board_handler.set_z((clicked_y-offset_y)/scale);
				is_marked=board_handler.outline_mark(scale);
				if(is_marked)
				{
					design_panel.set_point_name(board_handler.get_point_name());
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
				}
				else
					design_panel.set_point_name("");
				design_panel.redraw();

			}
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isMetaDown())
			{
				view_menu.show(the_view, e.getX(), e.getY());
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				if(is_dragged)
				{
					is_dragged=false;
					if(is_marked)
					{
						if(!x_locked_item.getState())
							board_handler.set_x((e.getX()-offset_x)/scale);
						if(!z_locked_item.getState())
							board_handler.set_z((e.getY()-offset_y)/scale);
						board_handler.set_point();
						is_marked=board_handler.outline_mark(scale);
						design_panel.redraw();
					}
					else
					{
						if(clicked_x<dragged_x)
							zoom_x=clicked_x;
						else
							zoom_x=dragged_x;
						if(clicked_y<dragged_y)
							zoom_y=clicked_y;
						else
							zoom_y=dragged_y;
						zoom_width=Math.abs(clicked_x-dragged_x);
						zoom_height=Math.abs(clicked_y-dragged_y);

						scale=(double)(off_dimension.width)/zoom_width*scale;
						offset_x=(int)((double)(off_dimension.width)/zoom_width*(offset_x-zoom_x));
						offset_y=(int)((double)(off_dimension.width)/zoom_width*(offset_y-zoom_y));
						repaint();
					}
				}
			}
		}
	};
}

class RockerView extends View implements ItemListener, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CheckboxMenuItem curvature_item;
	private CheckboxMenuItem vee_item;
	private CheckboxMenuItem x_locked_item;
	private CheckboxMenuItem y_locked_item;
	private CheckboxMenuItem blank_item;
	private MenuItem fit_item;
	private MenuItem zoom_in_item;
	private MenuItem zoom_out_item;
	private static boolean is_dragged;
	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;
	private static int zoom_x, zoom_y, zoom_width, zoom_height;

	private View the_view;

	public RockerView(BoardHandler bh, DesignPanel dp)
	{
		board_handler=bh;
		design_panel=dp;
		view_menu=new PopupMenu();

		is_marked=false;
		is_dragged=false;
		scale=1;
		offset_x=0;
		offset_y=0;

		curvature_item=new CheckboxMenuItem("Curvature", false);
		vee_item=new CheckboxMenuItem("Vee", false);
		x_locked_item=new CheckboxMenuItem("x locked", true);
		y_locked_item=new CheckboxMenuItem("y locked", false);
		blank_item=new CheckboxMenuItem("View blank", false);
		fit_item=new MenuItem("Fit");
		zoom_in_item=new MenuItem("Zoom in");
		zoom_out_item=new MenuItem("Zoom out");

		curvature_item.addItemListener(this);
		vee_item.addItemListener(this);
		x_locked_item.addItemListener(this);
		y_locked_item.addItemListener(this);
		blank_item.addItemListener(this);
		fit_item.addActionListener(this);
		zoom_in_item.addActionListener(this);
		zoom_out_item.addActionListener(this);

		view_menu.add(fit_item);
		view_menu.add(zoom_in_item);
		view_menu.add(zoom_out_item);
		view_menu.addSeparator();
		view_menu.add(x_locked_item);
		view_menu.add(y_locked_item);
		view_menu.addSeparator();
		view_menu.add(curvature_item);
		view_menu.add(vee_item);
		view_menu.addSeparator();
		view_menu.add(blank_item);

		add(view_menu);	
		the_view=this;
		addMouseListener(RockerMouse);
		addMouseMotionListener(RockerMouseMotion);

	}

	public void fit()
	{
		if(getSize().width == 0)
			return;
		
		if(off_dimension==null)	//Fix crash
			return;
		
		scale=(double)(off_dimension.width-40)/(double)board_handler.get_board_length();
		offset_x=20;
		offset_y=(int)(2*off_dimension.height/3);
		repaint();
	}

	public void zoom_in()
	{
		scale=scale*1.5;
		repaint();
	}

	public void zoom_out()
	{
		scale=scale/1.5;
		repaint();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==fit_item)
		{
			fit();
		}
		else if(e.getSource()==zoom_in_item)
		{
			zoom_in();
		}
		else if(e.getSource()==zoom_out_item)
		{
			zoom_out();
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource()==curvature_item)
		{
			repaint();
		}
		else if(e.getSource()==vee_item)
		{
			repaint();
		}
		else if(e.getSource()==blank_item)
		{
			repaint();
		}
	}

	public void paint(Graphics g2) 
	{
		update(g2);
	}

	public void update(Graphics g2) 
	{
		clear_graphics();
		if(is_dragged && !is_marked)
		{
			g.setColor(Color.black);
			g.drawRect(zoom_x, zoom_y, zoom_width, zoom_height);
		}
		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();

//		Turn on antialiasing, so painting is smooth.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_TEXT_ANTIALIAS_ON:RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		board_handler.draw_rocker(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y, vee_item.getState(), false, false, false);

		if(blank_item.getState())
			board_handler.draw_blank_rocker(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y, vee_item.getState());

		if(curvature_item.getState())
			board_handler.draw_rocker_curvature(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y);

		g2.drawImage(off_image, 0, 0, this);

	}

	MouseMotionListener RockerMouseMotion=new MouseMotionAdapter()
	{
		public void mouseDragged(MouseEvent e)
		{
			if(e.isMetaDown())
			{
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				is_dragged=true;
				if(is_marked)
				{
					if(!x_locked_item.getState())
						board_handler.set_x((e.getX()-offset_x)/scale);
					if(!y_locked_item.getState())
						board_handler.set_y((offset_y-e.getY())/scale);
					board_handler.set_point();
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
					design_panel.redraw();
				}
				else
				{
					dragged_x=e.getX();
					dragged_y=e.getY();
					if(dragged_y>clicked_y)
						dragged_y=clicked_y+(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					else
						dragged_y=clicked_y-(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					if(clicked_x<dragged_x)
						zoom_x=clicked_x;
					else
						zoom_x=dragged_x;
					if(clicked_y<dragged_y)
						zoom_y=clicked_y;
					else
						zoom_y=dragged_y;
					zoom_width=Math.abs(clicked_x-dragged_x);
					zoom_height=Math.abs(clicked_y-dragged_y);
					repaint();
				}
			}
		}

		public void mouseMoved(MouseEvent e)
		{
			if(!is_marked)
				design_panel.set_coordinates((e.getX()-offset_x)/scale, (offset_y-e.getY())/scale, board_handler.get_z());
		}
	};

	MouseListener RockerMouse=new MouseAdapter()
	{
		public void mousePressed(MouseEvent e)
		{
			is_dragged=false;
			if(e.isMetaDown())
			{
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				clicked_x=e.getX();
				clicked_y=e.getY();
				board_handler.set_x((clicked_x-offset_x)/scale);
				board_handler.set_y((-clicked_y+offset_y)/scale);
				is_marked=board_handler.rocker_mark(scale);
				if(is_marked)
				{
					design_panel.set_point_name(board_handler.get_point_name());
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
				}
				else
					design_panel.set_point_name("");
				design_panel.redraw();

			}
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isMetaDown())
			{
				view_menu.show(the_view, e.getX(), e.getY());
			}
			else if(e.isControlDown())
			{
			}
			else
			{
				if(is_dragged)
				{
					is_dragged=false;
					if(is_marked)
					{
						if(!x_locked_item.getState())
							board_handler.set_x((e.getX()-offset_x)/scale);
						if(!y_locked_item.getState())
							board_handler.set_y((offset_y-e.getY())/scale);
						board_handler.set_point();
						is_marked=board_handler.rocker_mark(scale);
						design_panel.redraw();
					}
					else
					{
						if(clicked_x<dragged_x)
							zoom_x=clicked_x;
						else
							zoom_x=dragged_x;
						if(clicked_y<dragged_y)
							zoom_y=clicked_y;
						else
							zoom_y=dragged_y;
						zoom_width=Math.abs(clicked_x-dragged_x);
						zoom_height=Math.abs(clicked_y-dragged_y);

						scale=(double)(off_dimension.width)/zoom_width*scale;
						offset_x=(int)((double)(off_dimension.width)/zoom_width*(offset_x-zoom_x));
						offset_y=(int)((double)(off_dimension.width)/zoom_width*(offset_y-zoom_y));
						repaint();
					}
				}
			}
		}
	};
}


class ThreeDView extends Panel implements ItemListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BoardHandler board_handler;
	private DesignPanel design_panel;
	private JPopupMenu view_menu;
	private JCheckBoxMenuItem shade_item;

	private SimpleUniverse u = null;
	public BranchGroup scene;
	private Shape3D shape;
	private Appearance look;
	private Background mBackgroundNode;
	
	private DirectionalLight mUpLight;
	private DirectionalLight mDownLight;
	private DirectionalLight mLeftLight;
	private DirectionalLight mRightLight;
	private DirectionalLight mHeadLight;
	private AmbientLight mAmbientLight;
	

	GraphicsConfiguration config;

    JCanvas3D jc;
    Canvas3D c;


	public ThreeDView(BoardHandler bh, DesignPanel dp)
	{
		board_handler=bh;
		design_panel=dp;
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		view_menu=new JPopupMenu();
		add(view_menu);	

		setLayout(new BorderLayout());
		config=SimpleUniverse.getPreferredConfiguration();

        //c = new JCanvas3D(config);
        jc = new JCanvas3D();
        jc.setResizeMode(JCanvas3D.RESIZE_IMMEDIATELY);
//        this.add("Center",jCanvas);
        Dimension dim = new Dimension(100,100);
        jc.setPreferredSize(dim);
        jc.setSize(dim);
        add("Center", jc);
        
        
        Canvas3D c = jc.getOffscreenCanvas3D();
//		add("Center", c);
		shape=new Shape3D();
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		c.addMouseListener(new ThreeDMouse(this));

		look = new Appearance();

		// Create a simple scene and attach it to the virtual universe
		scene = createSceneGraph();
		u = new SimpleUniverse(c,4);

		BoundingSphere bounds =
			new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		//Headlight
		ViewingPlatform viewingPlatform = u.getViewingPlatform();

	    PlatformGeometry pg = new PlatformGeometry();
	    
	    mHeadLight = new DirectionalLight(new Color3f(0.7f,0.7f,0.8f), new Vector3f(0.0f, 0.0f, -1.0f));
	    mHeadLight.setInfluencingBounds(bounds);
	    mHeadLight.setCapability(Light.ALLOW_STATE_WRITE);
	    mHeadLight.setEnable(true);
	    pg.addChild(mHeadLight);
	    viewingPlatform.setPlatformGeometry(pg);

	    //Svenne
		//u.setJ3DThreadPriority(Thread.MAX_PRIORITY);
		//u.getViewer().getView().setMinimumFrameCycleTime(20);
		//u.getViewer().getView().setSceneAntialiasingEnable(true);

	    /*		viewingPlatform.addRotateBehavior(0);
		viewingPlatform.addZoomBehavior(1);
		viewingPlatform.addTranslateBehavior(2);
		 */
		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		u.getViewingPlatform().setNominalViewingTransform();
		//Svenne-test
		//u.getViewingPlatform().clearCapabilityIsFrequent(ALLBITS);

		// add orbit behavior to the ViewingPlatform
       
		OrbitBehavior orbit = new OrbitBehavior(c);
		orbit.setSchedulingBounds(bounds);
		viewingPlatform.setViewPlatformBehavior(orbit);

		u.addBranchGraph(scene);
		
		
		shade_item=new JCheckBoxMenuItem("Shade", false);

		shade_item.addItemListener(this);

		view_menu.add(shade_item);

		JCheckBoxMenuItem toggleUpLight = new JCheckBoxMenuItem( "Toggle up light");
		toggleUpLight.setMnemonic(KeyEvent.VK_U);
		toggleUpLight.setSelected(mUpLight.getEnable());
		toggleUpLight.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event)
			{
				toggleUpLight();
			}
		});
		view_menu.add(toggleUpLight);
			
		JCheckBoxMenuItem toggleDownLight = new JCheckBoxMenuItem( "Toggle down light");
		toggleDownLight.setMnemonic(KeyEvent.VK_R);
		toggleDownLight.setSelected(mDownLight.getEnable());
		toggleDownLight.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event)
			{
				toggleDownLight();
			}
		});
		view_menu.add(toggleDownLight);

		JCheckBoxMenuItem toggleLeftLight = new JCheckBoxMenuItem( "Toggle left light");
		toggleLeftLight.setMnemonic(KeyEvent.VK_L);
		toggleLeftLight.setSelected(mLeftLight.getEnable());
		toggleLeftLight.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event)
			{
				toggleLeftLight();
			}
		});
		view_menu.add(toggleLeftLight);

		JCheckBoxMenuItem toggleRightLight = new JCheckBoxMenuItem( "Toggle right light");
		toggleRightLight.setMnemonic(KeyEvent.VK_R);
		toggleRightLight.setSelected(mRightLight.getEnable());
		toggleRightLight.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event)
			{
				toggleRightLight();
			}
		});
		view_menu.add(toggleRightLight);

		JCheckBoxMenuItem toggleHeadLight = new JCheckBoxMenuItem( "Toggle head light");
		toggleHeadLight.setMnemonic(KeyEvent.VK_R);
		toggleHeadLight.setSelected(mHeadLight.getEnable());
		toggleHeadLight.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event)
			{
				toggleHeadLight();
			}
		});
		view_menu.add(toggleHeadLight);

		JCheckBoxMenuItem toggleAmbientLight = new JCheckBoxMenuItem( "Toggle ambient light");
		toggleAmbientLight.setMnemonic(KeyEvent.VK_R);
		toggleAmbientLight.setSelected(mAmbientLight.getEnable());
		toggleAmbientLight.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent event)
			{
				toggleAmbientLight();
			}
		});
		view_menu.add(toggleAmbientLight);

	}

	public void update()
	{
		board_handler.set_shape(shape);
	}
	
	public Shape3D getShape()
	{
		return shape;
	}

	public void addModel(BranchGroup model)
	{
		scene.addChild(model);
	}

	public void set_airbrush(String filename)
	{
		look = new Appearance();

		java.net.URL texImage=null;
		try {
//			texImage = new java.net.URL("file:earth.jpg");
			texImage = new java.net.URL("file:"+filename);
		}
		catch (java.net.MalformedURLException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		// Set up the texture map
		TextureLoader tex = new TextureLoader(texImage, this);
		look.setTexture(tex.getTexture());

		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		look.setTextureAttributes(texAttr);


		// Create an Appearance.
		Color3f objColor = new Color3f(1.5f, 1.5f, 1.6f);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
		// Set up the material properties
		look.setMaterial(new Material(objColor, black, objColor, white, 100.0f));

		ColoringAttributes colorattrib=new ColoringAttributes(objColor, 2);
		look.setColoringAttributes(colorattrib);

		shape.setAppearance(look);

	}
	
	void toggleUpLight(){
		mUpLight.setEnable(!mUpLight.getEnable());
	}
	void toggleDownLight(){
		mDownLight.setEnable(!mDownLight.getEnable());
	}
	void toggleLeftLight(){
		mLeftLight.setEnable(!mLeftLight.getEnable());
	}
	void toggleRightLight(){
		mRightLight.setEnable(!mRightLight.getEnable());
	}
	void toggleHeadLight(){
		mHeadLight.setEnable(!mHeadLight.getEnable());
	}
	void toggleAmbientLight(){
		mAmbientLight.setEnable(!mAmbientLight.getEnable());
	}

	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource()==shade_item)
		{
		}
	}

	class ThreeDMouse extends MouseAdapter
	{
		private ThreeDView the_view;
		public ThreeDMouse(ThreeDView v)
		{
			the_view=v;
		}

		public void mouseClicked(MouseEvent e)
		{
			if(e.isMetaDown())
				view_menu.show(the_view, e.getX(), e.getY());
		}
	}

	private BranchGroup createSceneGraph() 
	{
		BranchGroup branchRoot = new BranchGroup();
		branchRoot.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		// Create a bounds for the background and lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

		// Set up the background
		Color3f bgColor = new Color3f(BoardCAD.getInstance().getBackgroundColor());
		mBackgroundNode = new Background(bgColor);
		mBackgroundNode.setApplicationBounds(bounds);
		mBackgroundNode.setCapability(Background.ALLOW_COLOR_WRITE);
		branchRoot.addChild(mBackgroundNode);

		// Set up the ambient light
//		Color3f ambientColor = new Color3f(0.2f, 0.2f, 0.2f);
		Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);
		mAmbientLight = new AmbientLight(ambientColor);
		mAmbientLight.setInfluencingBounds(bounds);
		mAmbientLight.setCapability(AmbientLight.ALLOW_STATE_WRITE);
		mAmbientLight.setEnable(true);
		branchRoot.addChild(mAmbientLight);

		// Set up the directional lights
		Color3f lightColor = new Color3f(0.8f, 0.8f, 0.8f);
		Vector3f up  = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f down  = new Vector3f(0.0f, -1.0f, 0.0f);
		Vector3f left  = new Vector3f(0.0f, 0.0f, 1.0f);
		Vector3f right  = new Vector3f(0.0f, 0.0f, -1.0f);

		mUpLight = new DirectionalLight(lightColor, up);
		mUpLight.setInfluencingBounds(bounds);
		mUpLight.setCapability(DirectionalLight.ALLOW_STATE_WRITE);
		mUpLight.setEnable(false);
		branchRoot.addChild(mUpLight);

		mDownLight = new DirectionalLight(lightColor, down);
		mDownLight.setInfluencingBounds(bounds);
		mDownLight.setCapability(DirectionalLight.ALLOW_STATE_WRITE);
		mDownLight.setEnable(false);
		branchRoot.addChild(mDownLight);

		mLeftLight = new DirectionalLight(lightColor, left);
		mLeftLight.setInfluencingBounds(bounds);
		mLeftLight.setCapability(DirectionalLight.ALLOW_STATE_WRITE);
		mLeftLight.setEnable(false);
		branchRoot.addChild(mLeftLight);

		mRightLight = new DirectionalLight(lightColor, right);
		mRightLight.setInfluencingBounds(bounds);
		mRightLight.setCapability(DirectionalLight.ALLOW_STATE_WRITE);
		mRightLight.setEnable(false);
		branchRoot.addChild(mRightLight);
				
		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		TransformGroup objScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(1.5);
		objScale.setTransform(t3d);
		branchRoot.addChild(objScale);

/*
		//add texture
		java.net.URL texImage=null;
		try {
			texImage = new java.net.URL("file:airbrush.jpg");
//			texImage = new java.net.URL("file:"+filename);
		}
		catch (java.net.MalformedURLException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
		// Set up the texture map
		TextureLoader tex = new TextureLoader(texImage, this);
		look.setTexture(tex.getTexture());

//		TextureAttributes texAttr = new TextureAttributes();
//		texAttr.setTextureMode(TextureAttributes.MODULATE);
//		look.setTextureAttributes(texAttr);

*/


		// Create an Appearance.
		Color3f objColor = new Color3f(0.5f, 0.5f, 0.6f);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		look.setTextureAttributes(texAttr);

		// Set up the material properties
		look.setMaterial(new Material(objColor, black, objColor, white, 100.0f));

		// Create the transform group node and initialize it to the
		// identity.  Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime.  Add it to the
		// root of the subgraph.
		TransformGroup board_tg = new TransformGroup();
		board_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		board_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objScale.addChild(board_tg);

		shape.setGeometry(new TriangleArray(3, TriangleArray.COORDINATES | TriangleArray.NORMALS));
		shape.setAppearance(look);
		board_tg.addChild(shape);

        MouseRotate mr = new MouseRotate(jc, board_tg);
        //mr.setSchedulingBounds(bounds);
        //mr.setSchedulingInterval(1);
        branchRoot.addChild(mr);
        
		// Have Java 3D perform optimizations on this scene graph.
		branchRoot.compile();

		return branchRoot;
	}

	public void setBackgroundColor(Color color)
	{
		float[] components = color.getRGBComponents(null);
		mBackgroundNode.setColor(new Color3f(components[0], components[1], components[2]));
	}

	//	public void destroy() 
//	{
//	u.removeAllLocales();
//	}
}

class ThreeDView2 extends View implements ItemListener, ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CheckboxMenuItem curvature_item;
	private CheckboxMenuItem t_under_item;
	private CheckboxMenuItem x_locked_item;
	private CheckboxMenuItem z_locked_item;
	private CheckboxMenuItem blank_item;
	private CheckboxMenuItem bottom_cut_item;
	private CheckboxMenuItem deck_cut_item;
	private MenuItem fit_item;
	private MenuItem zoom_in_item;
	private MenuItem zoom_out_item;

	private static boolean is_dragged;
	private static int clicked_x;
	private static int clicked_y;
	private static int dragged_x;
	private static int dragged_y;
	private static int zoom_x, zoom_y, zoom_width, zoom_height;
	private static double[][] rotation_matrix;

	private View the_view;
	private NurbsPoint p;
	private double theta;
	private double zeta;	

	public ThreeDView2(BoardHandler bh, DesignPanel dp)
	{
		board_handler=bh;
		design_panel=dp;
		view_menu=new PopupMenu();

		is_marked=false;
		is_dragged=false;
		scale=1;
		offset_x=0;
		offset_y=0;
		
		theta=30;
		double[][] m1 = {{Math.cos(-theta*3.1415/180.0), 0.0, -Math.sin(-theta*3.1415/180.0)},
				{0.0, 1.0, 0.0},
		                {Math.sin(-theta*3.1415/180.0), 0.0, Math.cos(-theta*3.1415/180.0)}}; 
		zeta=45;
		double[][] m2 = {{1.0, 0.0, 0.0},
				{0.0, Math.cos(-zeta*3.1415/180.0), -Math.sin(-zeta*3.1415/180.0)},
		                {0.0, Math.sin(-zeta*3.1415/180.0), Math.cos(-zeta*3.1415/180.0)}}; 



		rotation_matrix=cross_product(m1,m2);
		
		
		curvature_item=new CheckboxMenuItem("Curvature", false);
		t_under_item=new CheckboxMenuItem("Tucked Under", false);
		x_locked_item=new CheckboxMenuItem("x locked", true);
		z_locked_item=new CheckboxMenuItem("z locked", false);
		blank_item=new CheckboxMenuItem("View blank", false);
		bottom_cut_item=new CheckboxMenuItem("View bottom cut", false);
		deck_cut_item=new CheckboxMenuItem("View deck cut", false);
		fit_item=new MenuItem("Fit");
		zoom_in_item=new MenuItem("Zoom in");
		zoom_out_item=new MenuItem("Zoom out");

		curvature_item.addItemListener(this);
		t_under_item.addItemListener(this);
		x_locked_item.addItemListener(this);
		z_locked_item.addItemListener(this);
		
		blank_item.addItemListener(this);
		bottom_cut_item.addItemListener(this);
		deck_cut_item.addItemListener(this);
		fit_item.addActionListener(this);
		zoom_in_item.addActionListener(this);
		zoom_out_item.addActionListener(this);

		view_menu.add(fit_item);
		view_menu.add(zoom_in_item);
		view_menu.add(zoom_out_item);
		view_menu.addSeparator();
		view_menu.add(x_locked_item);
		view_menu.add(z_locked_item);
		view_menu.addSeparator();
		view_menu.add(curvature_item);
		view_menu.add(t_under_item);
		view_menu.addSeparator();
		view_menu.add(blank_item);
		view_menu.add(bottom_cut_item);
		view_menu.add(deck_cut_item);

		add(view_menu);	
		the_view=this;
		addMouseListener(OutlineMouse);
		addMouseMotionListener(OutlineMouseMotion);
	}
	
	private double[][] invert(double[][] a)
	{
		/*	
		
		| a11 a12 a13 |-1                |   a33a22-a32a23  -(a33a12-a32a13)   a23a12-a22a13   |
		| a21 a22 a23 |    =  1/DET(A) * | -(a33a21-a31a23)   a33a11-a31a13  -(a23a11-a21a13) |
		| a31 a32 a33 |                  |   a32a21-a31a22  -(a32a11-a31a12)   a22a11-a21a12   |

		DET(A)  =  a11(a33a22-a32a23)-a21(a33a12-a32a13)+a31(a23a12-a22a13)	

		*/
	
		double det=a[0][0]*(a[2][2]*a[1][1]-a[2][1]*a[1][2])-a[1][0]*(a[2][2]*a[0][1]-a[2][1]*a[0][2])+a[2][0]*(a[1][2]*a[0][1]-a[1][1]*a[0][2]);

		double[][] b={ {(a[2][2]*a[1][1]-a[2][1]*a[1][2])/det, -(a[2][2]*a[0][1]-a[2][1]*a[0][2])/det, (a[1][2]*a[0][1]-a[1][1]*a[0][2])/det },
		               {-(a[2][2]*a[1][0]-a[2][0]*a[1][2])/det, (a[2][2]*a[0][0]-a[2][0]*a[0][2])/det, -(a[1][2]*a[0][0]-a[1][0]*a[0][2])/det },
		               {(a[2][1]*a[1][0]-a[2][0]*a[1][1])/det, -(a[2][1]*a[0][0]-a[2][0]*a[0][1])/det, (a[1][1]*a[0][0]-a[1][0]*a[0][1])/det }};
		               
		               
		return b;
		
				
	}
	
	private double[][] cross_product(double[][] m1, double[][] m2)
	{
		double[][] m=new double[3][3];
		
		m[0][0]=m1[0][0]*m2[0][0]+m1[1][0]*m2[0][1]+m1[2][0]*m2[0][2];
		m[1][0]=m1[0][0]*m2[1][0]+m1[1][0]*m2[1][1]+m1[2][0]*m2[1][2];
		m[2][0]=m1[0][0]*m2[2][0]+m1[1][0]*m2[2][1]+m1[2][0]*m2[2][2];

		m[0][1]=m1[0][1]*m2[0][0]+m1[1][1]*m2[0][1]+m1[2][1]*m2[0][2];
		m[1][1]=m1[0][1]*m2[1][0]+m1[1][1]*m2[1][1]+m1[2][1]*m2[1][2];
		m[2][1]=m1[0][1]*m2[2][0]+m1[1][1]*m2[2][1]+m1[2][1]*m2[2][2];

		m[0][2]=m1[0][2]*m2[0][0]+m1[1][2]*m2[0][1]+m1[2][2]*m2[0][2];
		m[1][2]=m1[0][2]*m2[1][0]+m1[1][2]*m2[1][1]+m1[2][2]*m2[1][2];
		m[2][2]=m1[0][2]*m2[2][0]+m1[1][2]*m2[2][1]+m1[2][2]*m2[2][2];
		
		return m;
	
	}
	
	public void fit()
	{
		if(getSize().width == 0)
			return;
		
		if(off_dimension==null)
			return;
		
		scale=(double)0.9*(off_dimension.width-40)/(double)board_handler.get_board_length();
		offset_x=100;
		offset_y=(int)(off_dimension.height-20);
		repaint();
	}

	public void zoom_in()
	{
		scale=scale*1.5;
		repaint();
	}

	public void zoom_out()
	{
		scale=scale/1.5;
		repaint();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==fit_item)
		{
			fit();
		}
		else if(e.getSource()==zoom_in_item)
		{
			zoom_in();
		}
		else if(e.getSource()==zoom_out_item)
		{
			zoom_out();
		}


	}

	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource()==curvature_item)
		{
			repaint();
		}
		else if(e.getSource()==t_under_item)
		{
			repaint();
		}
		else if(e.getSource()==blank_item)
		{
			repaint();
		}
		else if(e.getSource()==bottom_cut_item)
		{
			repaint();
		}
		else if(e.getSource()==deck_cut_item)
		{
			repaint();
		}
	}

	public void paint(Graphics g2) 
	{
		update(g2);
	}

	public void update(Graphics g2) 
	{
		clear_graphics();
		if(is_dragged && !is_marked)
		{
			g.setColor(Color.black);
			g.drawRect(zoom_x, zoom_y, zoom_width, zoom_height);
		}
		/**

		 * Copy the graphics context so we can change it.

		 * Cast it to Graphics2D so we can use antialiasing.

		 */

		Graphics2D g2d = (Graphics2D)g.create();

//		Turn on antialiasing, so painting is smooth.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,BoardCAD.getInstance().isAntialiasing()?RenderingHints.VALUE_TEXT_ANTIALIAS_ON:RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		
		board_handler.draw_outline3D(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y, t_under_item.getState(),bottom_cut_item.getState(),deck_cut_item.getState(), rotation_matrix);

		if(blank_item.getState())
			board_handler.draw_blank_outline(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y, t_under_item.getState());


		if(curvature_item.getState())
			board_handler.draw_outline_curvature(g2d, off_dimension.width, off_dimension.height, scale, offset_x, offset_y);

		g2.drawImage(off_image, 0, 0, this);
	}

	MouseMotionListener OutlineMouseMotion=new MouseMotionAdapter()
	{
		public void mouseDragged(MouseEvent e)
		{
			if(e.isMetaDown())
			{
					dragged_x=e.getX();
					dragged_y=e.getY();
			}
			else if(e.isControlDown())
			{
				dragged_x=e.getX();
				dragged_y=e.getY();

				double theta2=theta+dragged_x-clicked_x;
				double[][] m1 = {{Math.cos(-theta2*3.1415/180.0), 0.0, -Math.sin(-theta2*3.1415/180.0)},
						{0.0, 1.0, 0.0},
				                {Math.sin(-theta2*3.1415/180.0), 0.0, Math.cos(-theta2*3.1415/180.0)}}; 
				double zeta2=zeta+dragged_y-clicked_y;
				double[][] m2 = {{1.0, 0.0, 0.0},
						{0.0, Math.cos(-zeta2*3.1415/180.0), -Math.sin(-zeta2*3.1415/180.0)},
				                {0.0, Math.sin(-zeta2*3.1415/180.0), Math.cos(-zeta2*3.1415/180.0)}}; 

				rotation_matrix=cross_product(m1,m2);
				design_panel.redraw();

			}
			else
			{
				is_dragged=true;
				if(is_marked)
				{
/*					if(!x_locked_item.getState())
						board_handler.set_x((e.getX()-offset_x)/scale);
					if(!z_locked_item.getState())
						board_handler.set_z((e.getY()-offset_y)/scale);
*/

					double[][] m=invert(rotation_matrix);

//					board_handler.set_x((e.getX()-offset_x)/scale);
					double myy=(rotation_matrix[1][0]*p.x+rotation_matrix[1][1]*p.y+rotation_matrix[1][2]*p.z);

					board_handler.set_x( m[0][0]*(e.getX()-offset_x)/scale + m[0][1]*myy + m[0][2]*(e.getY()-offset_y)/scale );// + m[0][2]*100);
					board_handler.set_y( m[1][0]*(e.getX()-offset_x)/scale + m[1][1]*myy + m[1][2]*(e.getY()-offset_y)/scale );// + m[1][2]*100);
					board_handler.set_z( m[2][0]*(e.getX()-offset_x)/scale + m[2][1]*myy + m[2][2]*(e.getY()-offset_y)/scale );// + m[2][2]*100);					

					board_handler.set_point(rotation_matrix);
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
					design_panel.redraw();
				}
				else
				{
					dragged_x=e.getX();
					dragged_y=e.getY();
					if(dragged_y>clicked_y)
						dragged_y=clicked_y+(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					else
						dragged_y=clicked_y-(int)(Math.abs(dragged_x-clicked_x)*(double)off_dimension.height/(double)off_dimension.width);
					if(clicked_x<dragged_x)
						zoom_x=clicked_x;
					else
						zoom_x=dragged_x;
					if(clicked_y<dragged_y)
						zoom_y=clicked_y;
					else
						zoom_y=dragged_y;
					zoom_width=Math.abs(clicked_x-dragged_x);
					zoom_height=Math.abs(clicked_y-dragged_y);
					repaint();
				}
			}
		}

		public void mouseMoved(MouseEvent e)
		{
			if(!is_marked)
				design_panel.set_coordinates((e.getX()-offset_x)/scale, board_handler.get_y(), (e.getY()-offset_y)/scale);
		}
	};

	MouseListener OutlineMouse=new MouseAdapter()
	{
		public void mousePressed(MouseEvent e)
		{
			is_dragged=false;
			if(e.isMetaDown())
			{
				clicked_x=e.getX();
				clicked_y=e.getY();
			}
			else if(e.isControlDown())
			{
				clicked_x=e.getX();
				clicked_y=e.getY();
			}
			else
			{
				clicked_x=e.getX();
				clicked_y=e.getY();
				board_handler.set_x((clicked_x-offset_x)/scale);
				board_handler.set_z((clicked_y-offset_y)/scale);
				is_marked=board_handler.outline_mark(scale, rotation_matrix);
				if(is_marked)
				{
					design_panel.set_point_name(board_handler.get_point_name());
					design_panel.set_coordinates(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
					p=new NurbsPoint(board_handler.get_x(), board_handler.get_y(), board_handler.get_z());
					
				}
				else
					design_panel.set_point_name("");
				design_panel.redraw();

			}
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isMetaDown())
			{
				view_menu.show(the_view, e.getX(), e.getY());
			}
			else if(e.isControlDown())
			{

				theta=theta+dragged_x-clicked_x;
				zeta=zeta+dragged_y-clicked_y;

			}
			else
			{
				if(is_dragged)
				{
					is_dragged=false;
					if(is_marked)
					{
/*						if(!x_locked_item.getState())
							board_handler.set_x((e.getX()-offset_x)/scale);
						if(!z_locked_item.getState())
							board_handler.set_z((e.getY()-offset_y)/scale);
						board_handler.set_point(rotation_matrix);
						is_marked=board_handler.outline_mark(scale, rotation_matrix);
						design_panel.redraw();
*/
					}
					else
					{
						if(clicked_x<dragged_x)
							zoom_x=clicked_x;
						else
							zoom_x=dragged_x;
						if(clicked_y<dragged_y)
							zoom_y=clicked_y;
						else
							zoom_y=dragged_y;
						zoom_width=Math.abs(clicked_x-dragged_x);
						zoom_height=Math.abs(clicked_y-dragged_y);

						scale=(double)(off_dimension.width)/zoom_width*scale;
						offset_x=(int)((double)(off_dimension.width)/zoom_width*(offset_x-zoom_x));
						offset_y=(int)((double)(off_dimension.width)/zoom_width*(offset_y-zoom_y));
						repaint();
					}
				}
			}
		}
	};
}


