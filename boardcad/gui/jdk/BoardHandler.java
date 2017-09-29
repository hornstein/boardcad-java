package boardcad.gui.jdk;

import cadcore.*;
import boardcam.*;
import board.*;
import board.readers.*;
import board.writers.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;

import java.awt.event.*;
import java.awt.geom.*;


import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;

import java.io.*;

import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class BoardHandler
{
	private NurbsBoard active_board;
	private NurbsBoard boards[];
	private NurbsBoard blank;
	private int nr_of_boards;
	private int new_boards;
	private NurbsPoint active;
	private NurbsPoint help_points[];
	private int nr_of_help_points;
	private int max_iterations;
	private double tolerance;
	private boolean viewblank;
	private boolean marked;

	public BoardDraw board_draw;

	public BoardHandler()
	{
		nr_of_boards=0;
		new_boards=0;
		boards=new NurbsBoard[10];
		active=new NurbsPoint(0,0,0);
		help_points=new NurbsPoint[1000];
		nr_of_help_points=0;
		max_iterations=10;
		tolerance=50.0;
		viewblank=true;
		marked=true;

		board_draw=new BoardDraw();

	}

	public void set_x(double x)
	{
		active.x=x;
	}

	public void set_y(double y)
	{
		active.y=y;
	}

	public void set_z(double z)
	{
		active.z=z;
	}

	public double get_x()
	{
		return active.x;
	}

	public double get_y()
	{
		return active.y;
	}

	public double get_z()
	{
		return active.z;
	}

	public int get_nr_of_boards()
	{
		return nr_of_boards;
	}

	public String get_board_name()
	{
		if(active_board!=null)
			return active_board.get_name();
		return "";
	}

	public String get_point_name()
	{
		if(active_board!=null)
			return active_board.get_point_name();
		return "";
	}

	public void new_board()
	{
		new_boards++;
		boards[nr_of_boards]=new NurbsBoard();
		boards[nr_of_boards].set_name("board"+new_boards);
		active_board=boards[nr_of_boards];
		nr_of_boards++;
	}
	
	public void new_board(double length)
	{
		new_boards++;
		boards[nr_of_boards]=new NurbsBoard(length);
		boards[nr_of_boards].set_name("board"+new_boards);
		active_board=boards[nr_of_boards];
		nr_of_boards++;
	}

/*	public void new_board(double length, double width, double thickness, double scoop, double rocker)
	{
		new_boards++;
		boards[nr_of_boards]=new NurbsBoard(length, width, thickness, scoop, rocker);
		boards[nr_of_boards].set_name("board"+new_boards);
		active_board=boards[nr_of_boards];
		nr_of_boards++;		
	}
*/

	public boolean is_new_board()
	{
		return active_board.is_new_board();
	}

	public void open_board(String filename)
	{
		try
		{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(filename));
			new_boards++;
			boards[nr_of_boards]=new NurbsBoard(dataIn);
			boards[nr_of_boards].set_name(filename);
			active_board=boards[nr_of_boards];
			nr_of_boards++;
			dataIn.close();
		}
		catch(IOException excep1)
		{
			//here we should open a dialog
			System.out.println("Problem finding file " + excep1.toString());
		}

	}

	public void open_board(String filename, BezierBoard brd)
	{
		try
		{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(filename));
			new_boards++;

			StepReader.loadFile(dataIn, brd);
			boards[nr_of_boards]=new NurbsBoard(StepReader.deck, StepReader.bottom);
			boards[nr_of_boards].set_cartesian_transformation_operator(StepReader.local_origin, StepReader.x_axis_direction, StepReader.y_axis_direction);

//			boards[nr_of_boards]=new NurbsBoard(dataIn, brd);
			boards[nr_of_boards].set_name(filename);
			active_board=boards[nr_of_boards];
			nr_of_boards++;
			dataIn.close();
		}
		catch(IOException excep1)
		{
			//here we should open a dialog
			System.out.println("Problem finding file" + excep1.toString());
		}

	}
	public void close_board()
	{
	}

	public void save_board()
	{
		save_board_as(active_board.get_name());
	}

	public void save_board_as(String filename)
	{
		try
		{
			DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(filename));
			active_board.save(dataOut);
			active_board.set_name(filename);
			dataOut.close();
		}
		catch(IOException excep2)
		{
			System.out.println("Problem creating file " + excep2.toString());
		}

	}

	public void export_board(PrintStream dataOut, String filename)
	{
		if(active_board==null)
			approximate_bezier(BoardCAD.getInstance().getCurrentBrd(), true);
		
		//active_board.export(dataOut, filename);
		BezierBoard brd=BoardCAD.getInstance().getCurrentBrd();
		StepWriter.saveFile(dataOut, filename, active_board, brd);


	}

	public void export_board_stl(PrintStream dataOut, String filename)
	{
		active_board.export_stl(dataOut, filename);
	}

	public void export_board_dxf(PrintStream dataOut, String filename)
	{
		active_board.export_dxf(dataOut, filename);
	}
	
	public void generateGCodeDeck(String filename)
	{
		//active_board.export_gcode(dataOut, filename, blank);
		//BoardMachine boardmachine=new BoardMachine();
		BoardMachine.deck=active_board.getDeck();
		BoardMachine.bottom=active_board.getBottom();
		BoardMachine.generateGCodeDeck(filename);

		board_draw.bottom_cut=BoardMachine.bottom_cut;
		board_draw.bottom_collision=BoardMachine.bottom_collision;
		board_draw.deck_cut=BoardMachine.deck_cut;
		board_draw.deck_collision=BoardMachine.deck_collision;
		board_draw.nr_of_cuts_bottom=BoardMachine.nr_of_cuts_bottom;
		board_draw.nr_of_cuts_deck=BoardMachine.nr_of_cuts_deck;
	}

	public void generateGCodeBottom(String filename)
	{
		//active_board.export_gcode_bottom(dataOut, filename, blank);
		//BoardMachine boardmachine=new BoardMachine();
		BoardMachine.deck=active_board.getDeck();
		BoardMachine.bottom=active_board.getBottom();
		BoardMachine.generateGCodeBottom(filename);

		board_draw.bottom_cut=BoardMachine.bottom_cut;
		board_draw.bottom_collision=BoardMachine.bottom_collision;
		board_draw.deck_cut=BoardMachine.deck_cut;
		board_draw.deck_collision=BoardMachine.deck_collision;
		board_draw.nr_of_cuts_bottom=BoardMachine.nr_of_cuts_bottom;
		board_draw.nr_of_cuts_deck=BoardMachine.nr_of_cuts_deck;
	}

	public void print_board(Graphics2D g2d)
	{
//		g2d.drawString("in board handler", 250, 350);
/*		if(active_board!=null)
		{
			active_board.draw_outline(g2d, 5000, 3000, 0.05*2.8436, (int)(0.05*2.8436+30), 200, false, false, false, Color.black); //old scale 0.17
			active_board.draw_rocker(g2d, 5000, 3000, 0.05*2.8436, (int)(0.05*2.8436+30), 400, false, false, false, Color.black); //old scale 0.17

		}
*/
	}

	public void print_rocker_template(Graphics2D g2d, int page)
	{
/*		if(active_board!=null)
		{
			active_board.draw_rocker(g2d, 5000, 3000, 2.8436, (int)(-page*210*2.8436+30), 400, false, false, false, Color.black);
		}
*/
	}

	public void print_outline_template(Graphics2D g2d, int page, int index)
	{
/*		if(active_board!=null)
		{
			active_board.set_xcompensation(true);
			active_board.draw_outline(g2d, 5000, 3000, 2.8436, (int)(-page*210*2.8436+30),  (int)(-index*297*2.8436+725), false, false, false, Color.black);
			active_board.set_xcompensation(false);
		}
*/	}



	public int get_board_length()
	{
		if(active_board!=null)
			return (active_board.get_length());
		return -1;
	}

	public double get_segment_width()
	{
		if(active_board!=null)
			return active_board.get_segment_width();
		return -1.0;
	}

	public double get_edge_offset()
	{
		if(active_board!=null)
			return active_board.get_bottom_coord();
		return -1.0;
	}

	public void set_point()
	{
		if(active_board!=null)
			active_board.set_point(active, null);
	}

	public void set_point(double[][] rotation_matrix)
	{
		if(active_board!=null)
			active_board.set_point(active, rotation_matrix);
	}


	public boolean rocker_mark(double scale)
	{
		if(active_board!=null)
		{
			if(active_board.mark_rocker_point(active, scale))
			{
				active=active_board.get_marked_point();
				marked=true;
			}
			else
			{
				marked=false;
			}
		}


		return marked;
	}

	public boolean edge_mark(double scale)
	{
		if(active_board!=null)
		{
			if(active_board.mark_edge_point(active, scale))
			{
				active=active_board.get_marked_point();
				marked=true;
			}
			else
			{
				marked=false;
			}
		}
		return marked;
	}

	public boolean outline_mark(double scale)
	{
		if(active_board!=null)
		{
			if(active_board.mark_outline_point(active, scale, null))
			{
				active=active_board.get_marked_point();
				marked=true;
			}
			else
			{
				marked=false;
			}
		}
		return marked;
	}

	public boolean outline_mark(double scale, double[][] rotation_matrix)
	{
		if(active_board!=null)
		{
			if(active_board.mark_outline_point(active, scale, rotation_matrix))
			{
				active=active_board.get_marked_point();
				marked=true;
			}
			else
			{
				marked=false;
			}
		}
		return marked;
	}

	public void draw_rocker(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_vee, boolean draw_bottom_cut, boolean draw_deck_cut, boolean draw_blank)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(active_board!=null)
		{
			board_draw.deck=active_board.getDeck();
			board_draw.bottom=active_board.getBottom();
			board_draw.draw_rocker(g, width, height, scale, offset_x, offset_y, draw_vee, draw_bottom_cut, draw_deck_cut, draw_blank, Color.black);
			//active_board.draw_rocker(g, width, height, scale, offset_x, offset_y, draw_vee, draw_bottom_cut, draw_deck_cut, draw_blank, Color.black);
		}

		for(int i=0;i<nr_of_help_points;i++)
		{
			g.setColor(Color.red);
			g.drawLine((int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].y*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].y*scale)+offset_y+2);
			g.drawLine((int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].y*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].y*scale)+offset_y+2);
		}

		if(blank!=null && draw_blank==true)
		{
			board_draw.deck=blank.getDeck();
			board_draw.bottom=blank.getBottom();
			board_draw.draw_rocker(g, width, height, scale, offset_x, offset_y, draw_vee, draw_bottom_cut, draw_deck_cut, draw_blank, Color.blue);
		}

	}

	public void draw_blank_rocker(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_vee)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(blank!=null)
		{
			board_draw.deck=blank.getDeck();
			board_draw.bottom=blank.getBottom();
			board_draw.draw_rocker(g, width, height, scale, offset_x, offset_y, draw_vee, false, false, false, Color.blue);
		}
	}


	public void draw_edge(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(active_board!=null)
		{
			board_draw.marked_segment=active_board.marked_segment;
			board_draw.deck=active_board.getDeck();
			board_draw.bottom=active_board.getBottom();
			board_draw.draw_edge(g, width, height, scale, offset_x, offset_y);
		}

	}

	public void draw_outline(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_tucked_under, boolean draw_bottom_cut, boolean draw_deck_cut, boolean draw_blank)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(active_board!=null)
		{
            if(active_board.triangle_array!=null)
            {
                board_draw.triangle_array=active_board.triangle_array;
                board_draw.triangle_count=active_board.triangle_count;
            }
			board_draw.deck=active_board.getDeck();
			board_draw.bottom=active_board.getBottom();
			board_draw.draw_outline(g, width, height, scale, offset_x, offset_y, draw_tucked_under, draw_bottom_cut, draw_deck_cut, draw_blank, Color.BLACK);
		}

		for(int i=0;i<nr_of_help_points;i++)
		{
			g.setColor(Color.red);
			g.drawLine((int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].z*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].z*scale)+offset_y+2);
			g.drawLine((int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].z*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].z*scale)+offset_y+2);
		}
		
		if(blank!=null && draw_blank==true)
		{
			board_draw.deck=blank.getDeck();
			board_draw.bottom=blank.getBottom();
			board_draw.draw_outline(g, width, height, scale, offset_x, offset_y, draw_tucked_under, draw_bottom_cut, draw_deck_cut, true, Color.blue);

		}
	}
	
	public void draw_outline3D(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_tucked_under, boolean draw_bottom_cut, boolean draw_deck_cut, double[][] m)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(active_board!=null)
		{
			board_draw.deck=active_board.getDeck();
			board_draw.bottom=active_board.getBottom();
			board_draw.draw_outline3D(g, width, height, scale, offset_x, offset_y, draw_tucked_under, draw_bottom_cut, draw_deck_cut, Color.black, m);
		}

/*		for(int i=0;i<nr_of_help_points;i++)
		{
			g.setColor(Color.red);
			g.drawLine((int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].z*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].z*scale)+offset_y+2);
			g.drawLine((int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].z*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].z*scale)+offset_y+2);
		}
*/		
//		if(blank!=null && viewblank==true)
//			blank.draw_outline(g, width, height, scale, offset_x, offset_y, draw_tucked_under, Color.blue);

	}

	public void draw_blank_outline(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_tucked_under)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(blank!=null)
		{
			board_draw.deck=blank.getDeck();
			board_draw.bottom=blank.getBottom();
			board_draw.draw_outline(g, width, height, scale, offset_x, offset_y, draw_tucked_under, false, false, true, Color.blue);
		}
	}
	
	public void draw_rocker_curvature(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(active_board!=null)
		{
			board_draw.deck=active_board.getDeck();
			board_draw.bottom=active_board.getBottom();
			board_draw.draw_rocker_curvature(g, width, height, scale, offset_x, offset_y);
		}
	}

	public void draw_outline_curvature(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y)
	{
		board_draw.is_marked=marked;
		board_draw.active=active;

		if(active_board!=null)
		{
			board_draw.deck=active_board.getDeck();
			board_draw.bottom=active_board.getBottom();
			board_draw.draw_outline_curvature(g, width, height, scale, offset_x, offset_y);
		}
	}

	public void set_shape(Shape3D shape)
	{
		if(active_board!=null)
			active_board.set_shape(shape);
		else
			shape.setGeometry(new TriangleArray(3, TriangleArray.COORDINATES | TriangleArray.NORMALS));
	}

	public void add_help_point(double x, double y, double z)
	{
		help_points[nr_of_help_points]=new NurbsPoint(x,y,z);
		nr_of_help_points++;
	}

	public void set_tail(double swallow_tail)
	{
		if(active_board!=null)
			active_board.set_tail(swallow_tail);
	}

	public void close_help_points()
	{
		nr_of_help_points=0;
	}

	public void set_active_board(int nr)
	{
		active_board=boards[nr];
	}

	public void set_resolution(int resolution)
	{
		active_board.set_resolution(resolution);
	}

/*	public void approximate_rocker()
	{
		active_board.approximate_rocker(help_points);
	}
*/

/*	public void approximate_board()
	{
		active_board.approximate_board();
	}
*/
	public void approximate_bezier(BezierBoard b, boolean closed_model)
	{
		if(active_board==null)
		{
			new_board(10*b.getLength());
			//active_board.create_board2(10*b.getLength());
		}
		else
		{
			active_board.scale_length(10*b.getLength()/active_board.get_length());
		}
			
		active_board.approximate_bezier(b, closed_model);
//		repair();


	}
	
	public void approximate_bezier2(BezierBoard b, boolean reparameterize)
	{
		if(active_board==null)
		{
			new_board(10*b.getLength());
			//active_board.create_board2(10*b.getLength());
		}
		else
		{
			active_board.scale_length(10*b.getLength()/active_board.get_length());
		}
			
		active_board.approximate_bezier2(b);
//		repair();
	}	

	public int approximate_bezier_patch(BezierBoard b, boolean reparameterize)
	{
		NurbsBoard tempBoard=new NurbsBoard(10*b.getLength());
		
		int splineSize=tempBoard.create_bezier_patch(b);
		
		if (splineSize>0)
			active_board=tempBoard;
	
		return splineSize;		
		
	}

	
	public void set_nr_of_segments(int n)
	{
		if(active_board!=null)
		{
			BezierBoard b=BoardCAD.getInstance().getCurrentBrd();
			
			active_board.scale_length(10*b.getLength()/active_board.get_length());

			active_board.set_nr_of_segments(n);

			active_board.create_board(active_board.get_length());

	

		}
	}		
	
	public void set_nr_of_points(int n)
	{
		if(active_board!=null)
		{
			BezierBoard b=BoardCAD.getInstance().getCurrentBrd();
			
			active_board.scale_length(10*b.getLength()/active_board.get_length());

			active_board.set_nr_of_points(n);

			active_board.create_board(active_board.get_length());
		}
	}	
	
	public void add_segment(double x)
	{
		if(active_board!=null)
			active_board.add_segment(x);
	}
	
	public void set_tolerance(double tol)
	{
		tolerance=tol;
	}
	
	public void set_iterations(int m)
	{
		max_iterations=m;
	}
	
	public void set_single_point_editing(boolean mode)
	{
		board_draw.single_point_editing=mode;

		if(active_board!=null)
			active_board.set_single_point_editing(mode);
	
	}

	public void set_as_blank()
	{
		blank=active_board;
		active_board=null;
	}
	
	public void rotate_blank(double angle)
	{
		if(blank!=null)
			blank.rotate(angle);
	}

	public void translate_blank(double x, double y, double z)
	{
		if(blank!=null)
			blank.translate(x,y,z);
	}

	public void rotate(double angle)
	{
		if(active_board!=null)
			active_board.rotate(angle);
	}

	public void rotate_xz(double angle)
	{
		if(active_board!=null)
			active_board.rotate_xz(angle);
	}

	public void rotate_yz(double angle)
	{
		if(active_board!=null)
			active_board.rotate_yz(angle);
	}

	public void translate(double x, double y, double z)
	{
		if(active_board!=null)
			active_board.translate(x,y,z);
	}

	public void flip()
	{
		if(active_board!=null)
			active_board.flip();
	}

	public void place_board()
	{
		if(active_board!=null)
		{
			place(active_board, null);
		}

	}
	
	public void place_blank()
	{
		if(blank!=null && active_board!=null)
		{
			place(blank, active_board);
		}

	}


	private void place(NurbsBoard blank, NurbsBoard board)
	{
		NurbsPoint p1, p2;
		NurbsSurface deck=blank.getDeck();
		NurbsSurface bottom=blank.getBottom();

		p1=deck.get_point_on_surface(deck.get_nr_of_segments()/2,deck.get_nr_of_points()/2,0.0,0.0);
		p2=bottom.get_point_on_surface(bottom.get_nr_of_segments()/2,bottom.get_nr_of_points()/2,0.0,0.0);
		
		if(p1.y<p2.y)
		{
			for(int i=0;i<9;i++)
				place(deck, blank, board, false);

			place(deck, blank, board, true);
		}
		else
		{
			for(int i=0;i<9;i++)
				place(bottom, blank, board, false);

			place(bottom, blank, board, true);
		}
		
	}


	private void place(NurbsSurface surf, NurbsBoard blank, NurbsBoard board, boolean do_evaluate) 
	{

		//BoardMachine board_machine=new BoardMachine();
		BoardMachine.read_machine_data();

		NurbsPoint s1=BoardMachine.machine2boardcad_coordinate(BoardMachine.support1);
		NurbsPoint s2=BoardMachine.machine2boardcad_coordinate(BoardMachine.support2);
		
		double support1_radius=BoardMachine.support1_radius;
		double support2_radius=BoardMachine.support2_radius;
		
		// find coordinates
		NurbsPoint p1, p3, p4, p_min;

		double y_dist = 10000;
		double y_dist2 = 10000;
		
		p_min = new NurbsPoint(0, 0, 0);

		surf.evaluate_always(do_evaluate);

		for(int angle=0; angle<360; angle=angle+10)
		{
			p1=surf.getYforXZ(s1.x+support1_radius*Math.cos(angle*3.14/180), s1.z+support1_radius*Math.sin(angle*3.14/180));

			if(p1.i<surf.get_nr_of_segments()-1 && p1.j<surf.get_nr_of_points())
			{
				if( (p1.y-s1.y) < y_dist)
				{
					y_dist=p1.y-s1.y;
					p_min=p1;
				}
			}
		}

		p3=new NurbsPoint(p_min.x, p_min.y, p_min.z);		

		for(int angle=0; angle<360; angle=angle+10)
		{
			p1=surf.getYforXZ(s2.x+support2_radius*Math.cos(angle*3.14/180), s2.z+support2_radius*Math.sin(angle*3.14/180));

			if(p1.i<surf.get_nr_of_segments()-1 && p1.j<surf.get_nr_of_points())
			{
				if( (p1.y-s2.y) < y_dist2)
				{
					y_dist2=p1.y-s2.y;
					p_min=p1;
				}
			}
		}
		
		p4=new NurbsPoint(p_min.x, p_min.y, p_min.z);

		double theta=Math.atan2((p4.y-p3.y),(p4.x-p3.x));
		double theta2=Math.atan2((s2.y-s1.y),(s2.x-s1.x));
		
		theta=theta-theta2;
		
		surf.evaluate_always(false);

		
		//System.out.println("p3.x=" + p3.x + " p4.x=" + p4.x + "p3.y=" + p3.y + " p4.y=" + p4.y +" theta=" + theta);
		
		//translate board to origin
		
		double[][] m = {{1.0, 0.0, 0.0},
		                {0.0, 1.0, 0.0},
		                {0.0, 0.0, 1.0}}; 

		double[] t={-p3.x , -p3.y, 0};

		//Create transformed surfaces
		
		blank.transform(m,t);		//transform blank

		if(board!=null)
			board.transform(m,t);	//transform board
		
		

		//rotate board
		
		double[][] m2 = {{Math.cos(-theta), -Math.sin(-theta), 0.0},
		                {Math.sin(-theta), Math.cos(-theta), 0.0},
		                {0.0, 0.0, 1.0}}; 

//		double[] t2={p3.x , 0.0, 0.0};

		
		double[] t2={p3.x , s1.y, 0.0};

		m=m2;
		t=t2;
		
		//Create transformed surfaces

		blank. transform(m,t);		//transform blank

		if(board!=null)
			board.transform(m,t);	//transform board
		
	}

	public void read_guide_points()
	{
		Scan scanner=new Scan();
		scanner.read_guide_points();
		help_points=scanner.help_points;
		nr_of_help_points=scanner.nr_of_help_points;
	}
	
	public void close_guide_points()
	{
		nr_of_help_points=0;
	}	
	
	public void scale_length(double factor)
	{
		if(active_board!=null)
			active_board.scale_length(factor);
	}
	
	public void scale_rocker(double factor)
	{
		if(active_board!=null)
			active_board.scale_rocker(factor);
	}

	public void scale_width(double factor)
	{
		if(active_board!=null)
			active_board.scale_width(factor);
	}

	public void scale_thickness(double factor)
	{
		if(active_board!=null)
			active_board.scale_thickness(factor);
	}

	public void repair()
	{
		if(active_board!=null)
			active_board.repair();
	}
	
	public boolean is_empty()
	{
		return (active_board==null);
	}

	public boolean is_marked()
	{
		return marked;
	}

	public void view_blank(boolean status)
	{
		viewblank=status;
	}


	public void clearBezier() 
	{
    		active_board = null;
	}
    

	public NurbsBoard getActiveBoard()
	{
		return active_board;
	}
	
	public void setActiveBoard(NurbsBoard b)
	{
		active_board=b;
	}

}


