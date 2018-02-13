package board;

import cadcore.*;
//import boardcad.BezierBoardCrossSection;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.io.*;

/**
 * Defines the 3D model of the board.
 *
 * The 3D model consists of two nurbs surfaces, one for the deck and one for the bottom.
 * These surfaces can be generated from the Bezier curves for the outline, rocker, and cross sections
 * using either a smooth approximation using the method approximate_bezier, or a more exact
 * approximation using create_bezier_patch.
 * 
 * This class also provides methods for manipulating the surfaces directly. This is especially
 * useful in order to create swallow tails that cannot be described directly by the 2D curves.
 */
 
public class NurbsBoard extends AbstractBoard implements Cloneable
{
	protected NurbsSurface deck;
	protected NurbsSurface bottom;
    
    public Triangle[] triangle_array;
    public int triangle_count;

	public NurbsSurface marked_surface;
	public int marked_segment;
	public int marked_point;
	public boolean is_marked;
	protected boolean is_new;

	protected String name;
	protected String[] bottom_points, deck_points;
	protected double step;
	protected int tail;
	protected double alfa, beta;
	protected int transformation_segments;
	protected int transformation_points;

	protected boolean single_point_editing;

	protected Point3f[] vertises;
	
	protected NurbsPoint x_axis_direction;
	protected NurbsPoint y_axis_direction;
	protected NurbsPoint local_origin;
	
	/**
	 * Creates a new board
	 */
	public NurbsBoard()
	{
		transformation_segments=17;
		transformation_points=15;

		x_axis_direction=new NurbsPoint(1.0,0.0,0.0);
		y_axis_direction=new NurbsPoint(0.0,1.0,0.0);
		local_origin=new NurbsPoint(0.0,0.0,0.0);
		
		create_board(1800);
	}


	/**
	 * Creates a new board of a given size.
	 * 
	 * @param length - desired length of the board.
	 */
	public NurbsBoard(double length)
	{
		transformation_segments=17;
		transformation_points=15;

		x_axis_direction=new NurbsPoint(1.0,0.0,0.0);
		y_axis_direction=new NurbsPoint(0.0,1.0,0.0);
		local_origin=new NurbsPoint(0.0,0.0,0.0);
		
		create_board(length);
	}

	
	/**
	 * Initializes the surfaces for a board of a given size.
	 * 
	 * @param length - desired length of the board.
	 */
	public void create_board(double length)
	{
		int i, j;
		double[] x;
		double[][] by, dy;
		double[][] bz, dz;

		int nr_of_segments=transformation_segments;
		int nr_of_points=transformation_points;
		
		tail=2;
		alfa=0;
		beta=0;

		vertises=new Point3f[3];

		deck=new NurbsSurface(nr_of_segments,nr_of_points);
		deck_points=new String[nr_of_points];
		bottom=new NurbsSurface(nr_of_segments,nr_of_points);
		bottom_points=new String[nr_of_points];

		is_marked=false;
		is_new=true;
		marked_segment=5;
		name="unnamed";
		step=0.2;

		deck_points[0]="tucked under";
		deck_points[1]="tucked under";
		deck_points[2]="tucked under";
		deck_points[3]="bottom edge";
		deck_points[4]="outline";
		for(i=5;i<deck.get_nr_of_points()/2;i++)
		{
			deck_points[i]="deck " + (i-4);
			deck_points[deck.get_nr_of_points()-1-i]="deck " + (i-4);
		}
		deck_points[deck.get_nr_of_points()/2]="top";
		deck_points[deck.get_nr_of_points()-5]="outline";
		deck_points[deck.get_nr_of_points()-4]="bottom edge";
		deck_points[deck.get_nr_of_points()-3]="tucked under";
		deck_points[deck.get_nr_of_points()-2]="tucked under";
		deck_points[deck.get_nr_of_points()-1]="tucked under";

		bottom_points[0]="tucked under";
		bottom_points[1]="tucked under";
		bottom_points[2]="tucked under";
		bottom_points[3]="tucked under";
		bottom_points[4]="tucked under";
		for(i=5;i<bottom.get_nr_of_points()/2-1;i++)
		{
			bottom_points[i]="bottom " + (i-2);
			bottom_points[bottom.get_nr_of_points()-1-i]="bottom " + (i-2);
		}
		bottom_points[bottom.get_nr_of_points()/2-1]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()/2]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()/2+1]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()-5]="tucked under";
		bottom_points[bottom.get_nr_of_points()-4]="tucked under";
		bottom_points[bottom.get_nr_of_points()-3]="tucked under";
		bottom_points[bottom.get_nr_of_points()-2]="tucked under";
		bottom_points[bottom.get_nr_of_points()-1]="tucked under";


		x=new double[nr_of_segments];
		x[0]=0;
		x[1]=0;
		x[2]=0;
		x[3]=0;
		x[nr_of_segments-4]=length;
		x[nr_of_segments-3]=length;
		x[nr_of_segments-2]=length;
		x[nr_of_segments-1]=length;
		
		x[4]=20.0;
		x[nr_of_segments-5]=length-20.0;
		
		
		double dist=length/(nr_of_segments-12+1);
		if(dist<240)
		{
			x[5]=dist/2;
			x[nr_of_segments-6]=length-(dist/2);
		}
		else
		{
			x[5]=120.0;
			x[nr_of_segments-6]=length-120.0;		
		}
		
		for(i=6;i<nr_of_segments-6;i++)
		{
			x[i]=length*(i-5)/(nr_of_segments-12+1);
		}
		

		for(i=0; i<bottom.get_nr_of_segments();i++)
		{
			for(j=0;j<bottom.get_nr_of_points();j++)
				bottom.set_control_point(i, j, new NurbsPoint(x[i],0,0));

			for(j=0;j<deck.get_nr_of_points();j++)
				deck.set_control_point(i, deck.get_nr_of_points()-1-j, new NurbsPoint(x[i],0,0));
		}
		
		deck.evaluate_surface();
		bottom.evaluate_surface();
		
	}
	
	/**
	 * Creates a new board from two surfaces
	 * 
	 * @param deck_surface - the deck surface
	 * @param bottom_surface - the bottom surface
	 */
	public NurbsBoard(NurbsSurface deck_surface, NurbsSurface bottom_surface)
	{

		int i, j;

		deck=deck_surface;
		bottom=bottom_surface;

		//adding meta data
		
		is_marked=false;
		is_new=false;
		marked_segment=5;
		tail=2;
		name="unnamed";
		step=0.2;
		
		deck_points=new String[deck.get_nr_of_points()];
		
		deck_points[0]="tucked under";
		deck_points[1]="tucked under";
		deck_points[2]="tucked under";
		deck_points[3]="bottom edge";
		deck_points[4]="outline";
		for(i=5;i<deck.get_nr_of_points()/2;i++)
		{
			deck_points[i]="deck " + (i-4);
			deck_points[deck.get_nr_of_points()-1-i]="deck " + (i-4);
		}
		deck_points[deck.get_nr_of_points()/2]="top";
		deck_points[deck.get_nr_of_points()-5]="outline";
		deck_points[deck.get_nr_of_points()-4]="bottom edge";
		deck_points[deck.get_nr_of_points()-3]="tucked under";
		deck_points[deck.get_nr_of_points()-2]="tucked under";
		deck_points[deck.get_nr_of_points()-1]="tucked under";
		
		
		bottom_points=new String[bottom.get_nr_of_points()];
		bottom_points[0]="tucked under";
		bottom_points[1]="tucked under";
		bottom_points[2]="tucked under";
		for(i=3;i<bottom.get_nr_of_points()/2-1;i++)
		{
			bottom_points[i]="bottom " + (i-2);
			bottom_points[bottom.get_nr_of_points()-1-i]="bottom " + (i-2);
		}
		bottom_points[bottom.get_nr_of_points()/2-1]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()/2]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()/2+1]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()-3]="tucked under";
		bottom_points[bottom.get_nr_of_points()-2]="tucked under";
		bottom_points[bottom.get_nr_of_points()-1]="tucked under";


	}

	/**
	 * Creates a new board from the old .cad file format
	 * 
	 * @param dataIn - stream to the .cad file
	 */
	public NurbsBoard(DataInputStream dataIn)
	{
		int i, j;
		int[] x;
		int[][] by, dy;
		int[][] bz, dz;
		int version=0;
		int nr_of_segments=0;
		int data_points_deck=0;
		int data_points_bottom=0;

		//file information for future backward compatibility
		try
		{
			version = dataIn.readInt();
			nr_of_segments = dataIn.readInt();
			data_points_deck = dataIn.readInt();
			data_points_bottom = dataIn.readInt();
		}
		catch(IOException e)
		{
			System.out.println("Problem reading file");
		}

		deck=new NurbsSurface(nr_of_segments,data_points_deck);
		deck_points=new String[data_points_deck];
		bottom=new NurbsSurface(nr_of_segments,data_points_bottom);
		is_marked=false;
		is_new=false;
		marked_segment=5;
		name="unnamed";
		step=0.2;

		deck_points[0]="tucked under";
		deck_points[1]="tucked under";
		deck_points[2]="tucked under";
		deck_points[3]="bottom edge";
		deck_points[4]="outline";
		for(i=5;i<deck.get_nr_of_points()/2;i++)
		{
			deck_points[i]="deck " + (i-4);
			deck_points[deck.get_nr_of_points()-1-i]="deck " + (i-4);
		}
		deck_points[deck.get_nr_of_points()/2]="top";
		deck_points[deck.get_nr_of_points()-5]="outline";
		deck_points[deck.get_nr_of_points()-4]="bottom edge";
		deck_points[deck.get_nr_of_points()-3]="tucked under";
		deck_points[deck.get_nr_of_points()-2]="tucked under";
		deck_points[deck.get_nr_of_points()-1]="tucked under";


		for(i=0; i<bottom.get_nr_of_segments();i++)
		{
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				try
				{
					bottom.set_control_point(i, j, new NurbsPoint(dataIn.readDouble(),dataIn.readDouble(),(dataIn.readDouble())));
				}
				catch(IOException e)
				{
					System.out.println("Problem creating file " + e.toString());
				}
			}
		}
		for(i=0; i<bottom.get_nr_of_segments();i++)
		{
			for(j=0;j<deck.get_nr_of_points();j++)
			{
				try
				{
					deck.set_control_point(i, j, new NurbsPoint(dataIn.readDouble(),dataIn.readDouble(),-dataIn.readDouble()));
				}
				catch(IOException e)
				{
					System.out.println("Problem creating file " + e.toString());
				}
			}
		}
		if(version>1)
		{
			try
			{
				tail= dataIn.readInt();
				alfa = dataIn.readDouble();
				beta = dataIn.readDouble();
			}
			catch(IOException e)
			{
				System.out.println("Problem reading file " + e.toString());
			}
		}

		
		if(data_points_deck>data_points_bottom)
		{
			NurbsSurface bottom2=new NurbsSurface(nr_of_segments,data_points_deck);
			for(i=0; i<bottom.get_nr_of_segments();i++)
			{
				bottom2.set_control_point(i,0, bottom.get_control_point(i,0));
				bottom2.set_control_point(i,1, bottom.get_control_point(i,0));
				bottom2.set_control_point(i,bottom2.get_nr_of_points()-2, bottom.get_control_point(i,bottom.get_nr_of_points()-1));
				bottom2.set_control_point(i,bottom2.get_nr_of_points()-1, bottom.get_control_point(i,bottom.get_nr_of_points()-1));
								
				for(j=0;j<bottom.get_nr_of_points();j++)
				{
					bottom2.set_control_point(i, j+2, bottom.get_control_point(i,j));
				}
			}
			bottom=bottom2;
		}
		
		bottom_points=new String[bottom.get_nr_of_points()];
		bottom_points[0]="tucked under";
		bottom_points[1]="tucked under";
		bottom_points[2]="tucked under";
		for(i=3;i<bottom.get_nr_of_points()/2-1;i++)
		{
			bottom_points[i]="bottom " + (i-2);
			bottom_points[bottom.get_nr_of_points()-1-i]="bottom " + (i-2);
		}
		bottom_points[bottom.get_nr_of_points()/2-1]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()/2]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()/2+1]="scoop/rocker";
		bottom_points[bottom.get_nr_of_points()-3]="tucked under";
		bottom_points[bottom.get_nr_of_points()-2]="tucked under";
		bottom_points[bottom.get_nr_of_points()-1]="tucked under";
		
		deck.evaluate_surface();
		bottom.evaluate_surface();

		x_axis_direction=new NurbsPoint(1.0,0.0,0.0);
		y_axis_direction=new NurbsPoint(0.0,1.0,0.0);
		local_origin=new NurbsPoint(0.0,0.0,0.0);


	}

	/**
	 * Saves a board using the old .cad file format
	 *
	 * @param dataOut - output stream for writing data
	 */
	public void save(DataOutputStream dataOut)
	{
		NurbsPoint p;
	
		try
		{
			dataOut.writeInt(2);
			dataOut.writeInt(deck.get_nr_of_segments());
			dataOut.writeInt(deck.get_nr_of_points());
			dataOut.writeInt(bottom.get_nr_of_points());
		}
		catch(IOException e)
		{
			System.out.println("Problem writing to file " + e.toString());
		}

		bottom.save(dataOut);
		
		for(int i=0;i<deck.get_nr_of_segments();i++)
		{
			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				p=deck.get_control_point(i,j);
				
				try
				{
					dataOut.writeDouble( p.x);
					dataOut.writeDouble( p.y);
					dataOut.writeDouble( -p.z);
				}
				catch(IOException e)
				{
					System.out.println("Problem creating file");
				}

			}
		}
						
//		deck.save(dataOut);

		try
		{
			dataOut.writeInt(tail);
			dataOut.writeDouble(alfa);
			dataOut.writeDouble(beta);
		}
		catch(IOException e)
		{
			System.out.println("Problem writing to file " + e.toString());
		}

		is_new=false;

	}

	/**
	 * Checks if a board is new.
	 *
	 * @return boolean
	 */
	public boolean is_new_board()
	{
		return is_new;
	}

	/**
	 * Sets the name of the board.
	 *
	 * @param new_name - the name of the board
	 */
	public void set_name(String new_name)
	{
		name=new_name;
	}

	/**
	 * Returns the name of the board
	 *
	 * @return String - the name of the board
	 */
	public String get_name()
	{
		return name;
	}

	/**
	 * Returns the name of the currently marked point
	 *
	 * @return String - the name of the point
	 */
	public String get_point_name()
	{
		if(marked_surface==bottom)
			return bottom_points[marked_point];
		else if(marked_surface==deck)
			return deck_points[marked_point];
		return "";
	}

	/**
	 * Returns the length of the board
	 *
	 * @return int - Length of board
	 */
	public int get_length()
	{
		return (int)((bottom.get_control_point(bottom.get_nr_of_segments()-1,bottom.get_nr_of_points()-1)).x-
				(bottom.get_control_point(0,bottom.get_nr_of_points()-1)).x);
	}

	/**
	 * Returns the width of the marked segment
	 *
	 * @return double - Length of board
	 */
	public double get_segment_width()
	{
		return((deck.get_control_point(marked_segment, deck.get_nr_of_points()-5)).z-
				(deck.get_control_point(marked_segment, 4)).z);
	}

	/**
	 * Returns the rocker value at the marked segment
	 *
	 * @return double - Length of board
	 */
	public double get_bottom_coord()
	{
		return (bottom.get_control_point(marked_segment, bottom.get_nr_of_points()/2)).y;
	}


	/**
	 * Returns a copy of the marked point
	 *
	 * @return NurbsPoint - The marked point
	 */
	public NurbsPoint get_marked_point()
	{
		return new NurbsPoint(
				(marked_surface.get_control_point(marked_segment, marked_point)).x,
				(marked_surface.get_control_point(marked_segment, marked_point)).y,
				(marked_surface.get_control_point(marked_segment, marked_point)).z);
	}

	/**
	 * Marks a point from the side view with priority for points along the rocker
	 * Only x and y values are checked while z value is ignored.
	 *
	 * @param value - the point we want check
	 * @param scale - the current scale at view
	 * @return boolean - true is a point is marked
	 */
	public boolean mark_rocker_point(NurbsPoint value, double scale)
	{
		if(is_marked)
		{
			if(Math.abs((marked_surface.get_control_point(marked_segment, marked_point)).x-value.x)*scale>3)
			{
				is_marked=false;
			}
				
			if(Math.abs((marked_surface.get_control_point(marked_segment, marked_point)).y-value.y)*scale>3)
			{
				is_marked=false;
			}
		}

		int i, j;

		//check if we mark a point on top or bottom
		if(!is_marked)
		{
			for(i=2;i<deck.get_nr_of_segments()-2;i++)
			{
				if(Math.abs((deck.get_control_point(i, deck.get_nr_of_points()/2)).x-value.x)*scale<3)
				{
					if(Math.abs((deck.get_control_point(i, deck.get_nr_of_points()/2)).y-value.y)*scale<3)
					{
						is_marked=true;
						marked_surface=deck;
						marked_segment=i;
						marked_point=deck.get_nr_of_points()/2;
					}
				}
			}

			for(i=2;i<bottom.get_nr_of_segments()-2;i++)
			{
				if(Math.abs((bottom.get_control_point(i, bottom.get_nr_of_points()/2)).x-value.x)*scale<3)
				{
					if(Math.abs((bottom.get_control_point(i, bottom.get_nr_of_points()/2)).y-value.y)*scale<3)
					{
						is_marked=true;
						marked_surface=bottom;
						marked_segment=i;
						marked_point=bottom.get_nr_of_points()/2;
					}
				}
			}
		}

		//check all points	
		if(!is_marked)
		{
			for(i=0;i<bottom.get_nr_of_segments();i++)
			{
				for(j=2;j<bottom.get_nr_of_points()-2;j++)
				{
					if(Math.abs((bottom.get_control_point(i, j)).x-value.x)*scale<3)
					{
						if(Math.abs((bottom.get_control_point(i, j)).y-value.y)*scale<3)
						{
							is_marked=true;
							marked_surface=bottom;
							marked_segment=i;
							marked_point=j;
						}
					}
				}
			}

			for(i=0;i<deck.get_nr_of_segments();i++)
			{
				for(j=2;j<deck.get_nr_of_points()-2;j++)
				{
					if(Math.abs((deck.get_control_point(i, j)).x-value.x)*scale<3)
					{
						if(Math.abs((deck.get_control_point(i, j)).y-value.y)*scale<3)
						{
							is_marked=true;
							marked_surface=deck;
							marked_segment=i;
							marked_point=j;
						}
					}
				}
			}
		}
		
		return is_marked;
	}

	/**
	 * Marks a point from the top or 3D view
	 *
	 * @param value - the point we want check
	 * @param scale - the current scale at view
	 * @param rotation_matrix - a 3x3 matris describing the rotation of the view
	 * @return boolean - true is a point is marked
	 */
	public boolean mark_outline_point(NurbsPoint value, double scale, double[][] rotation_matrix)
	{

		NurbsSurface tdeck=deck;
		NurbsSurface tbottom=bottom;
		
		int marked_surface_nr=0;

		if(rotation_matrix!=null)
		{
			rotate(rotation_matrix);
		}
		
		//check if we mark an already marked point	
		if(is_marked)
		{
			if(Math.abs((marked_surface.get_control_point(marked_segment, marked_point)).x-value.x)*scale>3)
			{
				is_marked=false;
			}
				
			if(Math.abs((marked_surface.get_control_point(marked_segment, marked_point)).z-value.z)*scale>3)
			{
				is_marked=false;
			}
		}
					
		int i, j;

		//check if we mark a point on the outline	
		if(!is_marked)
	     	{
			for(i=2;i<deck.get_nr_of_segments()-2;i++)
			{
				if(Math.abs((deck.get_control_point(i, 0)).x-value.x)*scale<3)
				{
					if(Math.abs((deck.get_control_point(i, deck.get_nr_of_points()-5)).z-value.z)*scale<3)
					{
						is_marked=true;
						marked_surface=deck;
						marked_segment=i;
						marked_point=deck.get_nr_of_points()-5;
						marked_surface_nr=1;
					}
					if(Math.abs((deck.get_control_point(i, 4)).z-value.z)*scale<3)
					{
						is_marked=true;
						marked_surface=deck;	    
						marked_segment=i;
						marked_point=4;
						marked_surface_nr=1;
					}
				}
			}
	      	}

		//check all points	
		if(!is_marked)
		{
			for(i=0;i<bottom.get_nr_of_segments();i++)
			{
				for(j=2;j<bottom.get_nr_of_points()-2;j++)
				{
					if(Math.abs((bottom.get_control_point(i, j)).x-value.x)*scale<3)
					{
						if(Math.abs((bottom.get_control_point(i, j)).z-value.z)*scale<3)
						{
							is_marked=true;
							marked_surface=bottom;
							marked_segment=i;
							marked_point=j;
							marked_surface_nr=2;
						}
					}
				}
			}

			for(i=0;i<deck.get_nr_of_segments();i++)
			{
				for(j=2;j<deck.get_nr_of_points()-2;j++)
				{
					if(Math.abs((deck.get_control_point(i, j)).x-value.x)*scale<3)
					{
						if(Math.abs((deck.get_control_point(i, j)).z-value.z)*scale<3)
						{
							is_marked=true;
							marked_surface=deck;
							marked_segment=i;
							marked_point=j;
							marked_surface_nr=1;
						}
					}
				}
			}
		}

		if(marked_surface_nr==1)
		{
			marked_surface=tdeck;
		}
		else if(marked_surface_nr==2)
		{
			marked_surface=tbottom;
		}
		
		deck=tdeck;
		bottom=tbottom;

		return is_marked;
	}

	/**
	 * Marks a point on the selected segment.
	 *
	 * @param value - the point we want check
	 * @param scale - the current scale at view
	 * @return boolean - true is a point is marked
	 */
	public boolean mark_edge_point(NurbsPoint value, double scale)
	{
		//check if we mark an already marked point	
		if(is_marked)
		{
			if(Math.abs((marked_surface.get_control_point(marked_segment, marked_point)).y-value.y)*scale>3)
			{
				is_marked=false;
			}
				
			if(Math.abs((marked_surface.get_control_point(marked_segment, marked_point)).z-value.z)*scale>3)
			{
				is_marked=false;
			}
		}
					
		int i, j;

		i=marked_segment;
		j=bottom.get_nr_of_points()/2;
		
		//check if we select rocker point	
		if(!is_marked)
		{
			if(Math.abs((bottom.get_control_point(i, j)).y-value.y)*scale<3 &&
					Math.abs((bottom.get_control_point(i, j)).z-value.z)*scale<3)
			{
				is_marked=true;
				marked_surface=bottom;
				marked_segment=i;
				marked_point=j;
			}
		}
		
		//check all points	
		if(!is_marked)
		{
		
			for(j=2;j<bottom.get_nr_of_points()-2;j++)
			{
				if(Math.abs((bottom.get_control_point(i, j)).y-value.y)*scale<3 &&
						Math.abs((bottom.get_control_point(i, j)).z-value.z)*scale<3)
				{
					is_marked=true;
					marked_surface=bottom;
					marked_segment=i;
					marked_point=j;
				}
			}
			for(j=2;j<deck.get_nr_of_points()-2;j++)
			{
				if(Math.abs((deck.get_control_point(i, j)).y-value.y)*scale<3 &&
						Math.abs((deck.get_control_point(i, j)).z-value.z)*scale<3)
				{
					is_marked=true;
					marked_surface=deck;
					marked_segment=i;
					marked_point=j;
				}
			}
		}
		
		return is_marked;
	}

	/**
	 * Sets the new position for the currently marked point
	 *
	 * @param value - the new 3D position of the point
	 * @param rotation_matrix - a 3x3 matris describing the rotation of the view
	 */
	public void set_point(NurbsPoint value, double[][] rotation_matrix)
	{
		double temp_z=value.z;

		NurbsSurface temp_surface=marked_surface;

		NurbsSurface tdeck=deck;
		NurbsSurface tbottom=bottom;

		if(rotation_matrix!=null)
		{
			rotate(rotation_matrix);
		}
		
		if(is_marked)
		{
			if(marked_segment>=marked_surface.get_nr_of_segments()-3)
			{
				update_point(marked_surface.get_nr_of_segments()-3, value);
				update_point(marked_surface.get_nr_of_segments()-2, value);
				update_point(marked_surface.get_nr_of_segments()-1, value);
				
				value=new NurbsPoint(value.x, value.y, -value.z);
				
				if(marked_surface==deck)
					marked_surface=bottom;
				else
					marked_surface=deck;

				update_point(marked_surface.get_nr_of_segments()-3, value);
				update_point(marked_surface.get_nr_of_segments()-2, value);
				update_point(marked_surface.get_nr_of_segments()-1, value);

				marked_surface=temp_surface;

			}
			else if(marked_segment<=2)
			{				
				update_point(0, value);
				update_point(1, value);
				update_point(2, value);

				value=new NurbsPoint(value.x, value.y, -value.z);
				
				if(marked_surface==deck)
					marked_surface=bottom;
				else
					marked_surface=deck;

				update_point(0, value);
				update_point(1, value);
				update_point(2, value);
				marked_surface=temp_surface;
			}
			else
			{
				update_point(marked_segment, value);
			}
			
			deck.evaluate_surface();
			bottom.evaluate_surface();
		}

		value.z=temp_z;

		deck=tdeck;
		bottom=tbottom;

	}
	
	
	/**
	 * Sets the new position for the currently marked point at a given segment
	 *
	 * @param segment - the segment where we want to update the position of the point
	 * @param value - the new 3D position of the point
	 */
	private void update_point(int segment, NurbsPoint value)
	{
		NurbsPoint temp;
		double temp_z=value.z;
		int i;

		if(single_point_editing)
		{
			if(marked_point<=2 || marked_point>=marked_surface.get_nr_of_points()-3)
			{
				set_tucked_under(segment, value);			
			}
			else
			{
				marked_surface.set_control_point(segment, marked_point, value);
				if(Math.abs(marked_point-marked_surface.get_nr_of_points()/2) >= 1)
				{
					value.z=-value.z;	
					marked_surface.set_control_point(segment, marked_surface.get_nr_of_points()-1-marked_point, value);
					value.z=temp_z;
				}
			}
		}
		else
		{

			//If we are not in the tail we move all points in that segment to the same x-value...
			if(segment>2)
			{
				if(value.x!=(marked_surface.get_control_point(marked_segment, marked_point)).x)
				{
					for(i=0;i<bottom.get_nr_of_points();i++)
					{
						temp=bottom.get_control_point(segment, i);
						temp.x=value.x;
						bottom.set_control_point(segment, i, temp);
					}
					for(i=0;i<deck.get_nr_of_points();i++)
					{
						temp=deck.get_control_point(segment, i);
						temp.x=value.x;
						deck.set_control_point(segment, i, temp);
					}
				}
			}
	
			//tail or nose
			if(segment>=marked_surface.get_nr_of_segments()-3 || segment<=2)
			{
				if(marked_point==(int)(marked_surface.get_nr_of_points()/2))
				{
					value.z=0.0;
					set_bottom(segment, value);
				}
				else if(marked_point<=2 || marked_point>=marked_surface.get_nr_of_points()-3)
				{
					set_outline(segment, value);
				}
				else if(marked_surface==deck && (marked_point<=4 || marked_point>=deck.get_nr_of_points()-5))
				{
					set_outline(segment, value);
				}
				else
				{
					set_end_segment(segment, value);
				}
			}
			else if(marked_point==marked_surface.get_nr_of_points()-3 || marked_point==2)
			{
				set_tucked_under(segment, value);
				value.z=temp_z;
			}
			else if(marked_point==(int)(marked_surface.get_nr_of_points()/2))
			{
				value.z=0.0;
				if(marked_surface==deck)
				{
					set_top(segment, value);
				}
				else
				{
					set_bottom(segment, value);
				}
			}
			else if(marked_surface==deck && (marked_point==4 || marked_point==deck.get_nr_of_points()-5))
			{
				set_outline(segment, value);
			}
			else
			{
				marked_surface.set_control_point(segment, marked_point, value);
				value.z=-value.z;
				marked_surface.set_control_point(segment, marked_surface.get_nr_of_points()-1-marked_point, value);
				value.z=temp_z;
			}
		}
	}

	/**
	 * Sets the position of the top point of a given segment
	 *
	 * @param segment - the segment where we want to update the position of the point
	 * @param value - the new 3D position of the point
	 */
	private void set_top(int segment, NurbsPoint value)
	{
		deck.set_control_point(segment, deck.get_nr_of_points()/2, value);
	}

	/**
	 * Sets the position of the bottom point of a given segment
	 *
	 * @param segment - the segment where we want to update the position of the point
	 * @param value - the new 3D position of the point
	 */
	private void set_bottom(int segment, NurbsPoint value)
	{
		int i;
		NurbsPoint temp;
		double diff;

		temp=bottom.get_control_point(segment, bottom.get_nr_of_points()/2);
		diff=value.y-temp.y;

		for(i=0;i<bottom.get_nr_of_points();i++)
		{
			temp=bottom.get_control_point(segment, i);
			temp.y=temp.y+diff;
			bottom.set_control_point(segment, i, temp);
		}
		for(i=0;i<deck.get_nr_of_points();i++)
		{
			temp=deck.get_control_point(segment, i);
			temp.y=temp.y+diff;
			deck.set_control_point(segment, i, temp);
		}
	}

	/**
	 * Sets the position of the outline point of a given segment
	 *
	 * @param segment - the segment where we want to update the position of the point
	 * @param value - the new 3D position of the point
	 */
	private void set_outline(int segment, NurbsPoint value)
	{
		int i;
		NurbsPoint temp;
		double diff, width;

		temp=deck.get_control_point(segment, marked_point);
		width=Math.abs(temp.z);

		if(width!=0)
		{
			if(marked_point==4)
				diff=-value.z+temp.z;
			else
				diff=value.z-temp.z;

			for(i=0;i<deck.get_nr_of_points()/2;i++)
			{
				temp=deck.get_control_point(segment, i);
				temp.z=temp.z-diff*Math.abs(temp.z)/width;
				deck.set_control_point(segment, i, temp);

				temp=deck.get_control_point(segment, deck.get_nr_of_points()-1-i);
				temp.z=temp.z+diff*Math.abs(temp.z)/width;
				deck.set_control_point(segment, deck.get_nr_of_points()-1-i, temp);
			}

			for(i=0;i<bottom.get_nr_of_points()/2-1;i++)
			{
				temp=bottom.get_control_point(segment, i);
				temp.z=temp.z+diff*Math.abs(temp.z)/width;
				bottom.set_control_point(segment, i, temp);

				temp=bottom.get_control_point(segment, bottom.get_nr_of_points()-1-i);
				temp.z=temp.z-diff*Math.abs(temp.z)/width;
				bottom.set_control_point(segment, bottom.get_nr_of_points()-1-i, temp);
			}
		}

		marked_surface.set_control_point(segment, marked_point, value);
		value.z=-value.z;
		marked_surface.set_control_point(segment, marked_surface.get_nr_of_points()-1-marked_point, value);
		value.z=-value.z;
	}

	/**
	 * Sets the position of the marked point at a given segment
	 *
	 * @param segment - the segment where we want to update the position of the point
	 * @param value - the new 3D position of the point
	 */
	private void set_end_segment(int segment, NurbsPoint value)
	{
		int i;
		NurbsPoint temp, temp2;
		double diff;

		if(marked_point==(int)(marked_surface.get_nr_of_points()/2))
		{
			value.z=0.0;
		}


		if(marked_surface==deck)
		{
			
			temp=new NurbsPoint(value.x,value.y,-value.z);
			deck.set_control_point(segment, marked_point, value);
			bottom.set_control_point(segment, marked_point, value);
			deck.set_control_point(segment, deck.get_nr_of_points()-1-marked_point, temp);		
			bottom.set_control_point(segment, bottom.get_nr_of_points()-1-marked_point, temp);		


		}

	}

	/**
	 * Sets the position of the tucked under point at a given segment
	 *
	 * @param segment - the segment where we want to update the position of the point
	 * @param value - the new 3D position of the point
	 */
	private void set_tucked_under(int segment, NurbsPoint value)
	{
		int i;
		NurbsPoint temp;
		double diff_z, diff_y, width;
		double epsilon=0.000001;

		temp=deck.get_control_point(segment, marked_point);
		width=Math.abs(temp.z);

		if(width<epsilon)
			width=epsilon;
			
		if(width!=0)
		{
			diff_y=value.y-temp.y;
			if(marked_point==2)
				diff_z=-value.z+temp.z;
			else
				diff_z=value.z-temp.z;

			for(i=0;i<3;i++)
			{
				temp=deck.get_control_point(segment, i);
				temp.z=temp.z-diff_z*Math.abs(temp.z)/width;
				temp.y=temp.y+diff_y*Math.abs(temp.z)/width;
				deck.set_control_point(segment, i, temp);

				temp=deck.get_control_point(segment, deck.get_nr_of_points()-1-i);
				temp.z=temp.z+diff_z*Math.abs(temp.z)/width;
				temp.y=temp.y+diff_y*Math.abs(temp.z)/width;
				deck.set_control_point(segment, deck.get_nr_of_points()-1-i, temp);
			}

			for(i=0;i<bottom.get_nr_of_points()/2-1;i++)
			{
				temp=bottom.get_control_point(segment, i);
				temp.z=temp.z+diff_z*Math.abs(temp.z)/width;
				temp.y=temp.y+diff_y*Math.abs(temp.z)/width;
				bottom.set_control_point(segment, i, temp);

				temp=bottom.get_control_point(segment, bottom.get_nr_of_points()-1-i);
				temp.z=temp.z-diff_z*Math.abs(temp.z)/width;
				temp.y=temp.y+diff_y*Math.abs(temp.z)/width;
				bottom.set_control_point(segment, bottom.get_nr_of_points()-1-i, temp);
			}
		}
	}

	/**
	 * Creates a 3D mesh of the board
	 *
	 * @param board_shape - the 3D mesh that we want to set
	 */
	 public void set_shape(Shape3D board_shape)
	 {
		int i,j;
		double u,v;
		int count=0;
		NurbsPoint p1;
		Point3f p2;
		Vector3f n;
		int deck_segments=deck.get_nr_of_segments();
		int deck_points=deck.get_nr_of_points();
		int bottom_segments=bottom.get_nr_of_segments();
		int bottom_points=bottom.get_nr_of_points();
		float board_length=(float)get_length();

		Point3f[] verts;
		if(tail>=2)
			verts = new Point3f[(int)((deck_segments-3)/step*(deck_points-3)/step*6+(bottom_segments-3)/step*(bottom_points-3)/step*6)];
		else
			verts = new Point3f[(int)((deck_segments-5)/step*(deck_points-3)/step*6+(bottom_segments-5)/step*(bottom_points-3)/step*6)];

		TriangleArray board_coord;
		if(tail>=2)
			board_coord = new TriangleArray((int)((deck_segments-3)/step*(deck_points-3)/step*6+(bottom_segments-3)/step*(bottom_points-3)/step*6), TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.TEXTURE_COORDINATE_2);
		else
			board_coord = new TriangleArray((int)((deck_segments-3)/step*(deck_points-3)/step*6+(bottom_segments-3)/step*(bottom_points-3)/step*6), TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.TEXTURE_COORDINATE_2);


		int start_point=1;

		for(i=start_point;i<deck_segments-2;i++)
		{
			for(u=0.0;u<1.0;u=u+step)
			{
				for(j=1;j<deck_points-2;j++)
				{
					for(v=0.0;v<1.0;v=v+step)
					{
						p1=deck.get_point_on_surface(i,j,u,v);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)-p1.z/board_length);
						board_coord.setTextureCoordinate(count, new Point2f((float)(j+v)/(deck_points), (float)(i+u)/(deck_segments)));
						n=calculate_normal(deck, i, j, u, v);
						board_coord.setNormal(count, new Vector3f(n.x, n.y, -n.z));
						verts[count++]=p2;
						p1=deck.get_point_on_surface(i,j,u,v+step);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)-p1.z/board_length);
						board_coord.setTextureCoordinate(count, new Point2f((float)(j+v+step)/(deck_points), (float)(i+u)/(deck_segments)));
						n=calculate_normal(deck, i, j, u, v+step);
						board_coord.setNormal(count, new Vector3f(n.x, n.y, -n.z));
						verts[count++]=p2;
						p1=deck.get_point_on_surface(i,j,u+step,v);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)-p1.z/board_length);
						board_coord.setTextureCoordinate(count, new Point2f((float)(j+v)/(deck_points), (float)(i+u+step)/(deck_segments)));
						n=calculate_normal(deck, i, j, u+step, v);
						board_coord.setNormal(count, new Vector3f(n.x, n.y, -n.z));
						verts[count++]=p2;

						p1=deck.get_point_on_surface(i,j,u,v+step);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)-p1.z/board_length);
						board_coord.setTextureCoordinate(count, new Point2f((float)(j+v+step)/(deck_points), (float)(i+u)/(deck_segments)));
						n=calculate_normal(deck, i, j, u, v+step);
						board_coord.setNormal(count, new Vector3f(n.x, n.y, -n.z));
						verts[count++]=p2;
						p1=deck.get_point_on_surface(i,j,u+step,v+step);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)-p1.z/board_length);
						board_coord.setTextureCoordinate(count, new Point2f((float)(j+v+step)/(deck_points), (float)(i+u+step)/(deck_segments)));
						n=calculate_normal(deck, i, j, u+step, v+step);
						board_coord.setNormal(count, new Vector3f(n.x, n.y, -n.z));
						verts[count++]=p2;
						p1=deck.get_point_on_surface(i,j,u+step,v);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)-p1.z/board_length);
						board_coord.setTextureCoordinate(count, new Point2f((float)(j+v)/(deck_points), (float)(i+u+step)/(deck_segments)));
						n=calculate_normal(deck, i, j, u+step, v);
						board_coord.setNormal(count, new Vector3f(n.x, n.y, -n.z));
						verts[count++]=p2;
					}
				}
			}
		}


		for(i=start_point;i<bottom_segments-2;i++)
		{
			for(u=0.0;u<1.0;u=u+step)
			{
				for(j=1;j<bottom_points-2;j++)
				{
					for(v=0.0;v<1.0;v=v+step)
					{
						p1=bottom.get_point_on_surface(i,j,u,v);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)p1.z/board_length);
						board_coord.setNormal(count, calculate_normal(bottom, i, j, u, v));
						verts[count++]=p2;
						p1=bottom.get_point_on_surface(i,j,u+step,v);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)p1.z/board_length);
						board_coord.setNormal(count, calculate_normal(bottom, i, j, u+step, v));
						verts[count++]=p2;
						p1=bottom.get_point_on_surface(i,j,u,v+step);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)p1.z/board_length);
						board_coord.setNormal(count, calculate_normal(bottom, i, j, u, v+step));
						verts[count++]=p2;

						p1=bottom.get_point_on_surface(i,j,u,v+step);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)p1.z/board_length);
						board_coord.setNormal(count, calculate_normal(bottom, i, j, u, v+step));
						verts[count++]=p2;
						p1=bottom.get_point_on_surface(i,j,u+step,v);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)p1.z/board_length);
						board_coord.setNormal(count, calculate_normal(bottom, i, j, u+step, v));
						verts[count++]=p2;
						p1=bottom.get_point_on_surface(i,j,u+step,v+step);
						p2 = new Point3f((float)(p1.x-board_length/2)/board_length, (float)p1.y/board_length, (float)p1.z/board_length);
						board_coord.setNormal(count, calculate_normal(bottom, i, j, u+step, v+step));
						verts[count++]=p2;
					}
				}
			}
		}

		board_coord.setCoordinates(0, verts);
		board_shape.setGeometry(board_coord);

		vertises=verts;

	 }

	/**
	 * Calculates the surface normal in a given point.
	 * This function should be removed and replaced with
	 * the getNormal method in class NurbsSurface
	 * 
	 * @param surf
	 * @param i
	 * @param j
	 * @param u
	 * @param v
	 */
	 private Vector3f calculate_normal(NurbsSurface surf, int i, int j, double u, double v)
	 {
//	 	return surf.calculate_normal(i, j, u, v, true);

		double step2=0.1; //0.1
		double my_u1,my_v1;
		double my_u2,my_v2;
		int my_i1, my_j1;
		int my_i2, my_j2;
		NurbsPoint p,p1,p2,p3,p4;
		
		float board_length=(float)get_length();
	
		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Point3f [] pts = new Point3f[3];
	
	
		for (int i2 = 0; i2 < 3; i2++) 
		    pts[i2] = new Point3f();
	
		if(v<0)
			v=0.0;
			
		if(u<0)
			u=0.0;
		
		my_u1=u;
		my_v1=v;
		my_i1=i;
		my_j1=j;
		my_u2=u;
		my_v2=v;
		my_i2=i;
		my_j2=j;
	
		p=surf.get_point_on_surface(i,j,u,v);
	
		boolean ok;
	
		do
		{
		
			my_u1=my_u1+step2;
			if(my_u1>=1)
			{
				my_i1++;
				my_u1=step2;//0.0;
			}	
			my_v1=my_v1+step2;
			if(my_v1>=1)
			{
				my_j1++;
				my_v1=step2;//0.0;
			}	
	
			my_u2=my_u2-step2;
			if(my_u2<0)
			{
				my_i2--;
				my_u2=1-step2;
			}	
			my_v2=my_v2-step2;
			if(my_v2<0)
			{
				my_j2--;
				my_v2=1-step2;
			}	
	
			p1=surf.get_point_on_surface(my_i1,my_j2,my_u1,my_v2);
			p2=surf.get_point_on_surface(my_i1,my_j1,my_u1,my_v1);
			p3=surf.get_point_on_surface(my_i2,my_j2,my_u2,my_v2);
			p4=surf.get_point_on_surface(my_i2,my_j1,my_u2,my_v1);
	
			pts[0] = new Point3f((float)p3.x, (float)p3.y, (float)p3.z);
			pts[1] = new Point3f((float)p1.x, (float)p1.y, (float)p1.z);
			pts[2] = new Point3f((float)p4.x, (float)p4.y, (float)p4.z);
		
			v1.sub(pts[0], pts[1]);
			v2.sub(pts[0], pts[2]);
	
			v1.normalize();
			v2.normalize();
	
	//		if(surf==bottom)
			    normal.cross(v1, v2);
	//		else
	//		    normal.cross(v2, v1);
	
			normal.normalize();
	
			ok=(Math.sqrt(normal.x*normal.x+normal.y*normal.y+normal.z*normal.z)>0.1);
			
			if(!ok)
			{
				pts[0] = new Point3f((float)p2.x, (float)p2.y, (float)p2.z);
				pts[1] = new Point3f((float)p4.x, (float)p4.y, (float)p4.z);
				pts[2] = new Point3f((float)p1.x, (float)p1.y, (float)p1.z);
	
				v1.sub(pts[0], pts[1]);
				v2.sub(pts[0], pts[2]);
	
				v1.normalize();
				v2.normalize();
					
	//			if(surf==bottom)
				    normal.cross(v1, v2);
	//			else
	//			    normal.cross(v2, v1);
	
				normal.normalize();
	
				ok=(Math.sqrt(normal.x*normal.x+normal.y*normal.y+normal.z*normal.z)>0.1);
			}
	
		}while(!ok);
	
			
		return normal;
		

	 }


	/**
	 * Exports the board on stl format
	 * 
	 * @param dataOut - stream to the output
	 * @param filename - this parameter is not used...
	 */
	 public void export_stl(PrintStream dataOut, String filename)
	 {
	 	int deck_segments=deck.get_nr_of_segments();
		int deck_points=deck.get_nr_of_points();
		int bottom_segments=bottom.get_nr_of_segments();
		int bottom_points=bottom.get_nr_of_points();
		int face; 
		int nr_of_vertises;
		nr_of_vertises=(int)( ( (deck_segments-3)/step*(deck_points-3)/step*6 + (bottom_segments-3)/step*(bottom_points-3)/step*6 ) /3 );

		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		double board_length=get_length();

		dataOut.println("solid board");

		for (face = 0; face < nr_of_vertises; face++) 
		{
			if(!vertises[face*3].equals(vertises[face*3+1]) && !vertises[face*3].equals(vertises[face*3+2]) && !vertises[face*3+1].equals(vertises[face*3+2]))
			{
				v1.sub(vertises[face*3+1], vertises[face*3]);
				v2.sub(vertises[face*3+2], vertises[face*3]);
				normal.cross(v1, v2);
				normal.normalize();
				if(!(Double.toString(normal.x).equals("NaN")))
				{
					dataOut.println("facet normal " + Double.toString(normal.x) + " " + Double.toString(normal.y)+ " " + Double.toString(normal.z) + "");
					dataOut.println(" outer loop");
					dataOut.println("  vertex " + Double.toString(vertises[face*3+2].x*board_length) + " " + Double.toString(vertises[face*3+2].y*board_length) + " " + Double.toString(vertises[face*3+2].z*board_length) + "");
					dataOut.println("  vertex " + Double.toString(vertises[face*3+1].x*board_length) + " " + Double.toString(vertises[face*3+1].y*board_length) + " " + Double.toString(vertises[face*3+1].z*board_length) + "");
					dataOut.println("  vertex " + Double.toString(vertises[face*3].x*board_length) + " " + Double.toString(vertises[face*3].y*board_length) + " " + Double.toString(vertises[face*3].z*board_length) + "");
					dataOut.println(" endloop");
					dataOut.println("endfacet");
				}
			}	
		}

		dataOut.println("endsolid board");


	}

	/**
	 * Exports the board on dxf format
	 * 
	 * @param dataOut - stream to the output
	 * @param filename - this parameter is not used...
	 */
	public void export_dxf(PrintStream dataOut, String filename)
	{
		int deck_segments=deck.get_nr_of_segments();
		int deck_points=deck.get_nr_of_points();
		int bottom_segments=bottom.get_nr_of_segments();
		int bottom_points=bottom.get_nr_of_points();
		int face; 
		int nr_of_vertises;
		nr_of_vertises=(int)( ( (deck_segments-3)/step*(deck_points-3)/step*6 + (bottom_segments-3)/step*(bottom_points-3)/step*6 ) /3 );

		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		double board_length=get_length();

		dataOut.println("0");
		dataOut.println("SECTION");
		dataOut.println("2");
		dataOut.println("ENTITIES");
		dataOut.println("0");

		for (face = 0; face < nr_of_vertises; face++) 
		{
			if(!vertises[face*3].equals(vertises[face*3+1]) && !vertises[face*3].equals(vertises[face*3+2]) && !vertises[face*3+1].equals(vertises[face*3+2]))
			{
				v1.sub(vertises[face*3+1], vertises[face*3]);
				v2.sub(vertises[face*3+2], vertises[face*3]);
				normal.cross(v1, v2);
				normal.normalize();
				if(!(Double.toString(normal.x).equals("NaN")))
				{
					dataOut.println("3DFACE");
					dataOut.println("8");
					dataOut.println("0");
					dataOut.println("10");
					dataOut.println(""+Double.toString(vertises[face*3+2].x*board_length)+"");
					dataOut.println("20");
					dataOut.println(""+Double.toString(vertises[face*3+2].y*board_length)+"");
					dataOut.println("30");
					dataOut.println(""+Double.toString(vertises[face*3+2].z*board_length)+"");
					dataOut.println("11");
					dataOut.println(""+Double.toString(vertises[face*3+1].x*board_length)+"");
					dataOut.println("21");
					dataOut.println(""+Double.toString(vertises[face*3+1].y*board_length)+"");
					dataOut.println("31");
					dataOut.println(""+Double.toString(vertises[face*3+1].z*board_length)+"");
					dataOut.println("12");
					dataOut.println(""+Double.toString(vertises[face*3].x*board_length)+"");
					dataOut.println("22");
					dataOut.println(""+Double.toString(vertises[face*3].y*board_length)+"");
					dataOut.println("32");
					dataOut.println(""+Double.toString(vertises[face*3].z*board_length)+"");
					dataOut.println("13");
					dataOut.println(""+Double.toString(vertises[face*3+2].x*board_length)+"");
					dataOut.println("23");
					dataOut.println(""+Double.toString(vertises[face*3+2].y*board_length)+"");
					dataOut.println("33");
					dataOut.println(""+Double.toString(vertises[face*3+2].z*board_length)+"");
					dataOut.println("0");
				}
			}	
		}

		dataOut.println("ENDSEC");
		dataOut.println("0");
		dataOut.println("EOF");


	}


	//transform board with rotation m and translation t
	
	/**
	 * Transforms the board a rotation matrix and a translation vector
	 *
	 * @param m - 3x3 rotation matrix
	 * @param t - translation vector
	 */	 
	public void transform(double[][] m, double[] t)
	{
		int i=0;
		int j=0;
		double u=0.0;
		double v=0.0;
		NurbsPoint p1, p2, p3, p4;
		double tx,ty,tz;
		
		//Create transformed surfaces
		
		deck.transform(m,t);
		bottom.transform(m,t);
		
		//update transformation operator
		
		tx=m[0][0]*local_origin.x+m[0][1]*local_origin.y+m[0][2]*local_origin.z+t[0];
		ty=m[1][0]*local_origin.x+m[1][1]*local_origin.y+m[1][2]*local_origin.z+t[1];
		tz=m[2][0]*local_origin.x+m[2][1]*local_origin.y+m[2][2]*local_origin.z+t[2];		
		local_origin=new NurbsPoint(tx,ty,tz);
						
		tx=m[0][0]*x_axis_direction.x+m[0][1]*x_axis_direction.y+m[0][2]*x_axis_direction.z;
		ty=m[1][0]*x_axis_direction.x+m[1][1]*x_axis_direction.y+m[1][2]*x_axis_direction.z;
		tz=m[2][0]*x_axis_direction.x+m[2][1]*x_axis_direction.y+m[2][2]*x_axis_direction.z;		
		x_axis_direction=new NurbsPoint(tx,ty,tz);
		
		tx=m[0][0]*y_axis_direction.x+m[0][1]*y_axis_direction.y+m[0][2]*y_axis_direction.z;
		ty=m[1][0]*y_axis_direction.x+m[1][1]*y_axis_direction.y+m[1][2]*y_axis_direction.z;
		tz=m[2][0]*y_axis_direction.x+m[2][1]*y_axis_direction.y+m[2][2]*y_axis_direction.z;		
		y_axis_direction=new NurbsPoint(tx,ty,tz);
		
	}


	/**
	 * Creates a swallow tail
	 *
	 * @param beta - the depth of the swallow tail
	 */	 
	public void set_tail(double beta)
	{
		NurbsPoint value, value2;
		double u=0.0;
		int seg=2;

		tail=2;

		if(tail==2) //swallow tail
		{
			do
			{
				u=u+0.01;
				if(u>=1)
				{
					u=0.0;
					seg++;
				}
				//value=bottom.get_point_on_surface(seg, bottom.get_nr_of_points()/2,u,0);
				value=bottom.get_point_on_surface(seg, 1,u,0);

			}while (value.x<beta);

			value=new NurbsPoint(value.x, value.y, 0);
			//we now want to set the endpoints to the position of value...

			bottom.set_control_point(3, bottom.get_nr_of_points()/2, value);
			bottom.set_control_point(3, bottom.get_nr_of_points()/2-1, value);
			bottom.set_control_point(3, bottom.get_nr_of_points()/2+1, value);
			
			value2=deck.get_control_point(3, deck.get_nr_of_points()/2);
			value2.x=value.x;
			deck.set_control_point(3, deck.get_nr_of_points()/2, value2);
			
			value2=deck.get_control_point(3, deck.get_nr_of_points()/2-1);
			value2.x=value.x;
			deck.set_control_point(3, deck.get_nr_of_points()/2-1, value2);
			
			value2=deck.get_control_point(3, deck.get_nr_of_points()/2+1);
			value2.x=value.x;
			deck.set_control_point(3, deck.get_nr_of_points()/2+1, value2);

			for(int i=0;i<bottom.get_nr_of_points();i++)
			{
				bottom.set_control_point(0, i, bottom.get_control_point(3,i));
				bottom.set_control_point(1, i, bottom.get_control_point(3,i));
				bottom.set_control_point(2, i, bottom.get_control_point(3,i));
			}

			for(int i=0;i<deck.get_nr_of_points();i++)
			{
				deck.set_control_point(0,i,bottom.get_control_point(3,bottom.get_nr_of_points()-i-1));
				deck.set_control_point(1,i,bottom.get_control_point(3,bottom.get_nr_of_points()-i-1));
				deck.set_control_point(2,i,bottom.get_control_point(3,bottom.get_nr_of_points()-i-1));				
			}

		}

		deck.evaluate_surface();
		bottom.evaluate_surface();
	}


	/**
	 * Sets the resolution of the 3D mesh
	 *
	 * @param resolution
	 */	 
	public void set_resolution(int resolution)
	{
		if(resolution==0)
			step=0.5;
		else if(resolution==1)
			step=0.2;
		else if(resolution==2)
			step=0.05;
	}

	/**
	 * Fits the deck surface to a number of desired points
	 *
	 * This method uses and iterative algorithm [1] to fit a Bspline surface to a given
	 * ordered point set without solving a linear system. It starts by creating an initial
	 * non-uniform B-spline surface which takes the given point setas its control point set.
	 * Then by adjusting its control points gradually with iterative formula, it produce
	 * a non-uniform B-spline surfaces with gradually highervprecision. 
	 * 
	 * [1] LIN Hongwei, WANG Guojin & DONG Chenshi, "Constructing iterative non-uniform B-spline
	 * curve and surface to fit data points", Science in China Ser. F Information Sciences, 
	 * Vol.47 No.3 pp. 315-331, 2004
	 *
	 * @param segments - nr of segments in the surface
	 * @param nr_of_points - nr of points in the surface
	 * @param desired_points - the points to which we want to fit the surface
	 */	 
	public void create_deck(int segments, int nr_of_points, NurbsPoint[][] desired_points)
	{
		deck=new NurbsSurface(segments,nr_of_points);

		int i;
		int j;

		NurbsPoint[][] points2=new NurbsPoint[segments][nr_of_points];

		NurbsPoint p, p2;
		int i2,j2;


		//place controlpoints at desired positions
		for(i=0; i<segments;i++)
		{
			for(j=0; j<nr_of_points;j++)
			{
				deck.set_control_point(i, j, desired_points[i][j]);
			}
		}

		//init
		for(i=0; i<segments;i++)
		{
			for(j=0; j<nr_of_points;j++)
			{
				points2[i][j]=new NurbsPoint(desired_points[i][j].x, desired_points[i][j].y, desired_points[i][j].z);
			}
		}

		//iterate until knot points interpolate desired positions		

		double max_err;
		int max_err_i=0;

		for(int loop=0;loop<10;loop++)
		{
		
			max_err=0.0;
		
			for(i=3; i<segments-3;i++)
			{
				for(j=0; j<nr_of_points;j++)
				{
					i2=i;
					j2=j;

					p=deck.get_point_on_surface_old(i2, j2, 0, 0);
					p2=deck.get_control_point(i2,j2);
					points2[i][j]=new NurbsPoint(p2.x+desired_points[i2][j2].x-p.x,p2.y+desired_points[i2][j2].y-p.y,p2.z+desired_points[i2][j2].z-p.z);

					if(j==4 && Math.abs(desired_points[i2][j2].z-p.z)>max_err)
					{
						max_err_i=i;
						max_err=Math.abs(desired_points[i2][j2].z-p.z);
					}
				}
			}

			System.out.println("max error (at " + max_err_i + " of " + segments + ") = " + max_err);

			//avoid passing board center

			for(i=3; i<segments-3;i++)
			{		
				for(j=0; j<nr_of_points;j++)
				{			
					if(j<nr_of_points/2)
					{
						if(points2[i][j].z>0)
							points2[i][j].z=0;
					}
					
					if(j>nr_of_points/2)
					{
						if(points2[i][j].z<0)
							points2[i][j].z=0;
					}
				}
			}
								
			//fix for convexity-preserving

			for(int loopa=0;loopa<10;loopa++)
			{

				for(i=4; i<segments-4;i++)
				{
					for(j=4;j<nr_of_points-3;j++)
					{
	
					    i2=i;
					    j2=j;
					    if(i<3)
						i2=1;
					    if(i>segments-4)
						i2=segments-2;
					    if(j<3)
						j2=1;
					    if(j>nr_of_points-4)
						j2=nr_of_points-2;
	
					    //Calculate angle between desired points
					    
					    double v1xa=desired_points[i][j].x-desired_points[i-1][j].x;
					    double v1ya=desired_points[i][j].y-desired_points[i-1][j].y;
					    double v2xa=desired_points[i+1][j].x-desired_points[i][j].x;
					    double v2ya=desired_points[i+1][j].y-desired_points[i][j].y;
	
					    double alfa_desired=Math.atan2(v1ya, v1xa)-Math.atan2(v2ya, v2xa);
	
					    double v1xb=points2[i][j].x-points2[i-1][j].x;
					    double v1yb=points2[i][j].y-points2[i-1][j].y;
					    double v2xb=points2[i+1][j].x-points2[i][j].x;
					    double v2yb=points2[i+1][j].y-points2[i][j].y;
	
					    double alfa_control=Math.atan2(v1yb, v1xb)-Math.atan2(v2yb, v2xb);
					    
					    //if the angles have the angles have different signs 
					    //the convexity is broken and has to be fixed
	
					    if((alfa_desired>0 && alfa_control<0) || (alfa_desired<0 && alfa_control>0))
					    {
						//adjust position of point to fix convexity
	
						//find the minimum distance between the point and the line
	
						double u=(points2[i][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i][j].y-points2[i-1][j].y)*(points2[i+1][j].y-points2[i-1][j].y);
				
						u=u/((points2[i+1][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i+1][j].y-points2[i-1][j].y)*(points2[i+1][j].y-points2[i-1][j].y));
	
						double new_x=points2[i-1][j].x+u*(points2[i+1][j].x-points2[i-1][j].x);
						double new_y=points2[i-1][j].y+u*(points2[i+1][j].y-points2[i-1][j].y);
	
						//points2[i][j].x=new_x;
						points2[i][j].y=new_y;
					    }
	
	
					    //Do the same in z-direction: 
					    //Calculate angle between desired points
	
					    v1xa=desired_points[i][j].x-desired_points[i-1][j].x;
					    double v1za=desired_points[i][j].z-desired_points[i-1][j].z;
					    v2xa=desired_points[i+1][j].x-desired_points[i][j].x;
					    double v2za=desired_points[i+1][j].z-desired_points[i][j].z;
	
					    alfa_desired=Math.atan2(v1za, v1xa)-Math.atan2(v2za, v2xa);
	
					    v1xb=points2[i][j].x-points2[i-1][j].x;
					    double v1zb=points2[i][j].z-points2[i-1][j].z;
					    v2xb=points2[i+1][j].x-points2[i][j].x;
					    double v2zb=points2[i+1][j].z-points2[i][j].z;
	
					    alfa_control=Math.atan2(v1zb, v1xb)-Math.atan2(v2zb, v2xb);
					    
					    //if the angles have different signs 
					    //the convexity is broken and has to be fixed
	
					    if((alfa_desired>0 && alfa_control<0) || (alfa_desired<0 && alfa_control>0))
					    {
						//adjust position of point to fix convexity
	
						//find the minimum distance between the point and the line
	
						double u=(points2[i][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i][j].z-points2[i-1][j].z)*(points2[i+1][j].z-points2[i-1][j].z);
				
						u=u/((points2[i+1][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i+1][j].z-points2[i-1][j].z)*(points2[i+1][j].z-points2[i-1][j].z));
	
						double new_x=points2[i-1][j].x+u*(points2[i+1][j].x-points2[i-1][j].x);
						double new_z=points2[i-1][j].z+u*(points2[i+1][j].z-points2[i-1][j].z);
	
						//points2[i][j].x=new_x;
						points2[i][j].z=new_z;
					 
									
					    }
	
					    
	
					}
				}				
	
					    
				//Fix convexity in yz-plane
				for(i=3; i<segments-3;i++)
				{
					for(j=2;j<nr_of_points-2;j++)
					{
	
					    i2=i;
					    j2=j;
					    if(i<3)
						i2=1;
					    if(i>segments-4)
						i2=segments-2;
					    if(j<3)
						j2=1;
					    if(j>nr_of_points-4)
						j2=nr_of_points-2;
	
	
					    //Calculate angle between desired points
					    //change x for z
					    
					    double v1xa=desired_points[i][j].z-desired_points[i][j-1].z;
					    double v1ya=desired_points[i][j].y-desired_points[i][j-1].y;
					    double v2xa=desired_points[i][j+1].z-desired_points[i][j].z;
					    double v2ya=desired_points[i][j+1].y-desired_points[i][j].y;
	
					    double alfa_desired=Math.atan2(v1ya, v1xa)-Math.atan2(v2ya, v2xa);
	
					    double v1xb=points2[i][j].z-points2[i][j-1].z;
					    double v1yb=points2[i][j].y-points2[i][j-1].y;
					    double v2xb=points2[i][j+1].z-points2[i][j].z;
					    double v2yb=points2[i][j+1].y-points2[i][j].y;
	
					    double alfa_control=Math.atan2(v1yb, v1xb)-Math.atan2(v2yb, v2xb);
					    
					    //if the angles have the angles have different signs 
					    //the convexity is broken and has to be fixed
	
					    if((alfa_desired>=0 && alfa_control<0) || (alfa_desired<=0 && alfa_control>0))
					    {
						//adjust position of point to fix convexity
	
						//find the minimum distance between the point and the line
	
						double u=(points2[i][j].z-points2[i][j-1].z)*(points2[i][j+1].z-points2[i][j-1].z)+(points2[i][j].y-points2[i][j-1].y)*(points2[i][j+1].y-points2[i][j-1].y);
				
						u=u/((points2[i][j+1].z-points2[i][j-1].z)*(points2[i][j+1].z-points2[i][j-1].z)+(points2[i][j+1].y-points2[i][j-1].y)*(points2[i][j+1].y-points2[i][j-1].y));
	
						double new_x=points2[i][j-1].z+u*(points2[i][j+1].z-points2[i][j-1].z);
						double new_y=points2[i][j-1].y+u*(points2[i][j+1].y-points2[i][j-1].y);
	
						points2[i][j].z=new_x;
						points2[i][j].y=new_y;
					 
	
										
					    }
					}
				}
			}

			//set the points

			for(i=3; i<segments-3;i++)
			{
				for(j=0; j<nr_of_points;j++)
				{
					deck.set_control_point(i, j, points2[i][j]);
				}
			}
		}

		for(i=0; i<segments;i++)
		{
			for(j=0; j<nr_of_points;j++)
			{
				deck.set_control_point(i, deck.get_nr_of_points()-1-j, desired_points[i][j]);
			}
		}
		for(i=3; i<segments-3;i++)
		{
			for(j=0; j<nr_of_points;j++)
			{
				deck.set_control_point(i, deck.get_nr_of_points()-1-j, points2[i][j]);
			}
		}

		deck.set_as_nurbs();				

	}
	

	/**
	 * Fits the bottom surface to a number of desired points
	 *
	 * This method uses and iterative algorithm [1] to fit a Bspline surface to a given
	 * ordered point set without solving a linear system. It starts by creating an initial
	 * non-uniform B-spline surface which takes the given point setas its control point set.
	 * Then by adjusting its control points gradually with iterative formula, it produce
	 * a non-uniform B-spline surfaces with gradually highervprecision. 
	 * 
	 * [1] LIN Hongwei, WANG Guojin & DONG Chenshi, "Constructing iterative non-uniform B-spline
	 * curve and surface to fit data points", Science in China Ser. F Information Sciences, 
	 * Vol.47 No.3 pp. 315-331, 2004
	 *
	 * @param segments - nr of segments in the surface
	 * @param nr_of_points - nr of points in the surface
	 * @param desired_points - the points to which we want to fit the surface
	 */	 
	public void create_bottom(int segments, int nr_of_points, NurbsPoint[][] desired_points)
	{
	
		bottom=new NurbsSurface(segments,nr_of_points);

		int i;
		int j;

		NurbsPoint[][] points2=new NurbsPoint[segments][nr_of_points];

		NurbsPoint p, p2;
		int i2,j2;

		//place controlpoints at desired positions

		for(i=0; i<segments;i++)
		{
			for(j=0; j<nr_of_points;j++)
			{
				bottom.set_control_point(i, j, desired_points[i][j]);
			}
		}

		//iterate until knot points interpolate desired positions		

		for(int loop=0;loop<10;loop++)
		{
			for(i=0; i<segments;i++)
			{
				for(j=0; j<nr_of_points;j++)
				{
					i2=i;
					j2=j;
					if(i<3)
						i2=1;
					if(i>segments-4)
						i2=segments-2;
					if(j<5)
						j2=1;
					if(j>nr_of_points-6)
						j2=nr_of_points-2;

					p=bottom.get_point_on_surface_old(i2, j2, 0, 0);
					p2=bottom.get_control_point(i2,j2);
					points2[i][j]=new NurbsPoint(p2.x+desired_points[i2][j2].x-p.x,p2.y+desired_points[i2][j2].y-p.y,p2.z+desired_points[i2][j2].z-p.z);
				}
			}


			//avoid passing board center

			for(i=3; i<segments-3;i++)
			{
			
				for(j=0; j<nr_of_points;j++)
				{
			
					if(j<nr_of_points/2)
					{
						if(points2[i][j].z>0)
							points2[i][j].z=0;
					}
					
					if(j>nr_of_points/2)
					{
						if(points2[i][j].z<0)
							points2[i][j].z=0;
					}
				}
			}

			//fix vee

			for(i=3; i<segments-3;i++)
			{
				points2[i][(nr_of_points-1)/2-1].y=points2[i][(nr_of_points-1)/2].y;
				points2[i][(nr_of_points-1)/2+1].y=points2[i][(nr_of_points-1)/2].y;
			}

			//fix for tail and nose
			for(i=3; i<segments-3;i++)
			{
				if(points2[i][0].x<bottom.get_control_point(0, 0).x)
				{
					for(j=0;j<bottom.get_nr_of_points();j++)
					{
						points2[i][j].x=bottom.get_control_point(0, 0).x;
					}
				}
				if(points2[i][0].x>bottom.get_control_point(bottom.get_nr_of_segments()-1, 0).x)
				{
					for(j=0;j<bottom.get_nr_of_points();j++)
					{
						points2[i][j].x=bottom.get_control_point(bottom.get_nr_of_segments()-1, 0).x;
					}
				}				
			}			

			//fix for convexity-preserving

			for(int loopa=0;loopa<10;loopa++)
			{

				for(i=4; i<segments-4;i++)
				{
	//				for(j=4;j<nr_of_points-3;j++)
					for(j=5;j<nr_of_points-5;j++)
					{
					    
					    i2=i;
					    j2=j;
					    //Calculate angle between desired points
	
					    double v1xa=desired_points[i][j].x-desired_points[i-1][j].x;
					    double v1ya=desired_points[i][j].y-desired_points[i-1][j].y;
					    double v2xa=desired_points[i+1][j].x-desired_points[i][j].x;
					    double v2ya=desired_points[i+1][j].y-desired_points[i][j].y;
	
					    double alfa_desired=Math.atan2(v1ya, v1xa)-Math.atan2(v2ya, v2xa);
	
					    double v1xb=points2[i][j].x-points2[i-1][j].x;
					    double v1yb=points2[i][j].y-points2[i-1][j].y;
					    double v2xb=points2[i+1][j].x-points2[i][j].x;
					    double v2yb=points2[i+1][j].y-points2[i][j].y;
	
					    double alfa_control=Math.atan2(v1yb, v1xb)-Math.atan2(v2yb, v2xb);
	
					    //if the angles have different signs 
					    //the convexity is broken and has to be fixed
	
					    if((alfa_desired>0 && alfa_control<0) || (alfa_desired<0 && alfa_control>0))
					    {
						//adjust position of point to fix convexity
	
						//find the minimum distance between the point and the line
	
						double u=(points2[i][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i][j].y-points2[i-1][j].y)*(points2[i+1][j].y-points2[i-1][j].y);
				
						u=u/((points2[i+1][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i+1][j].y-points2[i-1][j].y)*(points2[i+1][j].y-points2[i-1][j].y));
	
						double new_x=points2[i-1][j].x+u*(points2[i+1][j].x-points2[i-1][j].x);
						double new_y=points2[i-1][j].y+u*(points2[i+1][j].y-points2[i-1][j].y);
	
	//					points2[i][j].x=new_x;
						points2[i][j].y=new_y;
					 
										
					    }
	
	
					    //Do the same in z-direction: 
					    //Calculate angle between desired points
	
					    v1xa=desired_points[i][j].x-desired_points[i-1][j].x;
					    double v1za=desired_points[i][j].z-desired_points[i-1][j].z;
					    v2xa=desired_points[i+1][j].x-desired_points[i][j].x;
					    double v2za=desired_points[i+1][j].z-desired_points[i][j].z;
	
					    alfa_desired=Math.atan2(v1za, v1xa)-Math.atan2(v2za, v2xa);
	
					    v1xb=points2[i][j].x-points2[i-1][j].x;
					    double v1zb=points2[i][j].z-points2[i-1][j].z;
					    v2xb=points2[i+1][j].x-points2[i][j].x;
					    double v2zb=points2[i+1][j].z-points2[i][j].z;
	
					    alfa_control=Math.atan2(v1zb, v1xb)-Math.atan2(v2zb, v2xb);
					    
					    //if the angles have the angles have different signs 
					    //the convexity is broken and has to be fixed
	
					    if((alfa_desired>0 && alfa_control<0) || (alfa_desired<0 && alfa_control>0))
					    {
						//adjust position of point to fix convexity
	
						//find the minimum distance between the point and the line
	
						double u=(points2[i][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i][j].z-points2[i-1][j].z)*(points2[i+1][j].z-points2[i-1][j].z);
				
						u=u/((points2[i+1][j].x-points2[i-1][j].x)*(points2[i+1][j].x-points2[i-1][j].x)+(points2[i+1][j].z-points2[i-1][j].z)*(points2[i+1][j].z-points2[i-1][j].z));
	
						double new_x=points2[i-1][j].x+u*(points2[i+1][j].x-points2[i-1][j].x);
						double new_z=points2[i-1][j].z+u*(points2[i+1][j].z-points2[i-1][j].z);
	
	//					points2[i][j].x=new_x;
						points2[i][j].z=new_z;
					 
	
					    }
	
					}
					
				}
				
				//Fix convexity in yz-plane
				for(i=4; i<segments-4;i++)
				{
					for(j=5;j<nr_of_points-5;j++)
					{
	
					    i2=i;
					    j2=j;
					    if(i<3)
						i2=1;
					    if(i>segments-4)
						i2=segments-2;
					    if(j<5)
						j2=1;
					    if(j>=nr_of_points-5)
						j2=nr_of_points-2;
	
	
					    //Calculate angle between desired points
					    //change x for z
					    
					    double v1xa=desired_points[i][j].z-desired_points[i][j-1].z;
					    double v1ya=desired_points[i][j].y-desired_points[i][j-1].y;
					    double v2xa=desired_points[i][j+1].z-desired_points[i][j].z;
					    double v2ya=desired_points[i][j+1].y-desired_points[i][j].y;
	
					    double alfa_desired=Math.atan2(v1ya, v1xa)-Math.atan2(v2ya, v2xa);
	
					    double v1xb=points2[i][j].z-points2[i][j-1].z;
					    double v1yb=points2[i][j].y-points2[i][j-1].y;
					    double v2xb=points2[i][j+1].z-points2[i][j].z;
					    double v2yb=points2[i][j+1].y-points2[i][j].y;
	
					    double alfa_control=Math.atan2(v1yb, v1xb)-Math.atan2(v2yb, v2xb);
					    
					    //if the angles have the angles have different signs 
					    //the convexity is broken and has to be fixed
	
					    if((alfa_desired>0 && alfa_control<0) || (alfa_desired<0 && alfa_control>0))
					    {
						//adjust position of point to fix convexity
	
						//find the minimum distance between the point and the line
	
						double u=(points2[i][j].z-points2[i][j-1].z)*(points2[i][j+1].z-points2[i][j-1].z)+(points2[i][j].y-points2[i][j-1].y)*(points2[i][j+1].y-points2[i][j-1].y);
				
						u=u/((points2[i][j+1].z-points2[i][j-1].z)*(points2[i][j+1].z-points2[i][j-1].z)+(points2[i][j+1].y-points2[i][j-1].y)*(points2[i][j+1].y-points2[i][j-1].y));
	
						double new_x=points2[i][j-1].z+u*(points2[i][j+1].z-points2[i][j-1].z);
						double new_y=points2[i][j-1].y+u*(points2[i][j+1].y-points2[i][j-1].y);
	
						points2[i][j].z=new_x;
						points2[i][j].y=new_y;
					 
	
										
					    }
					    
					}
				}
	
			}			
				

			//set the points

			for(i=3; i<segments-3;i++)
			{
				for(j=0; j<nr_of_points;j++)
				{
					bottom.set_control_point(i, j, points2[i][j]);
				}
			}
			
		}

		bottom.set_as_nurbs();

	}






	
	
	/**
	 * Creates a 3D model of the board by fitting a bspline surface
	 *
	 * This method selects a number of points from the 2D model that we want the
	 * 3D model to interpolate, and creates deck and bottom surfaces that fits those.
	 *
	 * @param bezier_board - the bezier model that we want to fit
	 * @param closed_model - True if we want the surfaces to be closed in the nose and tail
	 */	 
	public void approximate_bezier(BezierBoard bezier_board, boolean closed_model)
	{
		NurbsPoint[][] bottom_points=new NurbsPoint[bottom.get_nr_of_segments()][bottom.get_nr_of_points()];
		NurbsPoint[][] deck_points=new NurbsPoint[deck.get_nr_of_segments()][deck.get_nr_of_points()];

		//First translate the board to 0 and make sure that deck points up.
		
		NurbsPoint temp_local_origin=new NurbsPoint(local_origin.x, local_origin.y, local_origin.z);
		NurbsPoint temp_x_axis_direction=new NurbsPoint(x_axis_direction.x, x_axis_direction.y, x_axis_direction.z);
		NurbsPoint temp_y_axis_direction=new NurbsPoint(y_axis_direction.x, y_axis_direction.y, y_axis_direction.z);
		
		if(local_origin.x!=0)
			translate(-bottom.get_point_on_surface_old(0,0,0,0).x, 0, 0);

		if(y_axis_direction.y<0)
			flip();
				
		local_origin=temp_local_origin;
		x_axis_direction=temp_x_axis_direction;
		y_axis_direction=temp_y_axis_direction;
	
	
		double xval, zval;
		NurbsPoint op, tup, p;

		NurbsPoint end_point=bezier_board.getBottom3D(get_length(), 0);

		for(int i=deck.get_nr_of_segments()-3;i<deck.get_nr_of_segments();i++)
		{
			if(closed_model)
			{
			
				for(int j=0;j<deck.get_nr_of_points();j++)
				{
					deck_points[i][j]=end_point;
				}

				for(int j=0;j<bottom.get_nr_of_points();j++)
				{
					bottom_points[i][j]=end_point;
				}
			}
			else
			{
				p=bottom.get_point_on_surface_old(i,0,0,0);
				xval=p.x-0.1;
			
				op=bezier_board.getOutline3D(xval);
				tup=bezier_board.getTuckedUnder3D(xval);
			
				//set bottom surface
			
				//set tucked under

				for(int j=0;j<5;j++)
				{
					p=new NurbsPoint(tup.x, tup.y, -tup.z);
					bottom_points[i][j]=p;
				}

				for(int j=bottom.get_nr_of_points()-5;j<bottom.get_nr_of_points();j++)
				{
					p=new NurbsPoint(tup.x, tup.y, tup.z);
					bottom_points[i][j]=p;
				}

				//set concave points
			
				double temp_n=0;
			
				for(int j=5;j<bottom.get_nr_of_points()/2-1;j++)
				{
					temp_n++;
				
					p=bezier_board.getBottom3D(tup.x, tup.z-tup.z/ ((bottom.get_nr_of_points()+1)/2-6)*temp_n);
					bottom_points[i][j]=new NurbsPoint(p.x,p.y,-p.z);
					bottom_points[i][bottom.get_nr_of_points()-j-1]=new NurbsPoint(p.x,p.y,p.z);
				}
			
				//set mid point
			
				p=bezier_board.getBottom3D(tup.x, 0);
			
				bottom_points[i][bottom.get_nr_of_points()/2-1]=new NurbsPoint(p.x,p.y,0);
				bottom_points[i][bottom.get_nr_of_points()/2]=new NurbsPoint(p.x,p.y,0);
				bottom_points[i][bottom.get_nr_of_points()/2+1]=new NurbsPoint(p.x,p.y,0);			
						
			
				//set deck surface

				//set tucked under

				for(int j=0;j<3;j++)
				{
					p=new NurbsPoint(tup.x, tup.y, -tup.z);
					deck_points[i][j]=p;
				}

				for(int j=deck.get_nr_of_points()-3;j<deck.get_nr_of_points();j++)
				{
					p=new NurbsPoint(tup.x, tup.y, tup.z);
					deck_points[i][j]=p;
				}

				//set rail under

				p=bezier_board.getBottom3D(tup.x, (tup.z+(op.z-tup.z)*0.6));
				deck_points[i][3]=new NurbsPoint(p.x,p.y,-p.z);
				deck_points[i][deck.get_nr_of_points()-4]=new NurbsPoint(p.x,p.y,p.z);
			
				//set outline
			
				deck_points[i][4]=new NurbsPoint(op.x,op.y,-op.z);
				deck_points[i][deck.get_nr_of_points()-5]=new NurbsPoint(op.x,op.y,op.z);
			
				//set mid top

				p=bezier_board.getTop3D(tup.x, 0);
				deck_points[i][deck.get_nr_of_points()/2]=new NurbsPoint(p.x,p.y,p.z);

				//set rest of top
			
				temp_n=0;
			
				for(int j=6;j<deck.get_nr_of_points()/2;j++)
				{
					temp_n++;
				
					p=bezier_board.getTop3D(tup.x, op.z-op.z/ ((deck.get_nr_of_points()+1)/2-6)*temp_n);
					deck_points[i][j]=new NurbsPoint(p.x,p.y,-p.z);
					deck_points[i][bottom.get_nr_of_points()-j-1]=new NurbsPoint(p.x,p.y,p.z);
				}
				
				//set rail top
			
				p=bezier_board.getTop3D(tup.x, op.z-(op.z+deck_points[i][6].z)/100);
				deck_points[i][5]=new NurbsPoint(p.x,p.y,-p.z);
				deck_points[i][deck.get_nr_of_points()-6]=new NurbsPoint(p.x,p.y,p.z);
					
			}
		}

		NurbsPoint start_point=bezier_board.getBottom3D(0.1, 0);
		start_point.z=0;

		
		for(int i=0;i<3;i++)
		{
		
			if(closed_model)
			{
			
		
				for(int j=0;j<deck.get_nr_of_points();j++)
				{
					deck_points[i][j]=start_point;
				}

				for(int j=0;j<bottom.get_nr_of_points();j++)
				{
					bottom_points[i][j]=start_point;
				}

			}
			else
			{

				p=bottom.get_point_on_surface_old(i,0,0,0);
				xval=p.x;
			
				if(xval<0.1)
					xval=0.1;

				op=bezier_board.getOutline3D(xval);
				tup=bezier_board.getTuckedUnder3D(xval);
			
				//set bottom surface
			
				//set tucked under

				for(int j=0;j<5;j++)
				{
					p=new NurbsPoint(tup.x, tup.y, -tup.z);
					bottom_points[i][j]=p;
				}

				for(int j=bottom.get_nr_of_points()-5;j<bottom.get_nr_of_points();j++)
				{
					p=new NurbsPoint(tup.x, tup.y, tup.z);
					bottom_points[i][j]=p;
				}

				//set concave points
			
				double temp_n=0;
			
				for(int j=5;j<bottom.get_nr_of_points()/2-1;j++)
				{
					temp_n++;
				
					p=bezier_board.getBottom3D(tup.x, tup.z-tup.z/ ((bottom.get_nr_of_points()+1)/2-6)*temp_n);
					bottom_points[i][j]=new NurbsPoint(p.x,p.y,-p.z);
					bottom_points[i][bottom.get_nr_of_points()-j-1]=new NurbsPoint(p.x,p.y,p.z);
				}
			
				//set mid point
			
				p=bezier_board.getBottom3D(tup.x, 0);
			
				bottom_points[i][bottom.get_nr_of_points()/2-1]=new NurbsPoint(p.x,p.y,0);
				bottom_points[i][bottom.get_nr_of_points()/2]=new NurbsPoint(p.x,p.y,0);
				bottom_points[i][bottom.get_nr_of_points()/2+1]=new NurbsPoint(p.x,p.y,0);			
						
			
				//set deck surface

				//set tucked under

				for(int j=0;j<3;j++)
				{
					p=new NurbsPoint(tup.x, tup.y, -tup.z);
					deck_points[i][j]=p;
				}

				for(int j=deck.get_nr_of_points()-3;j<deck.get_nr_of_points();j++)
				{
					p=new NurbsPoint(tup.x, tup.y, tup.z);
					deck_points[i][j]=p;
				}

				//set rail under

				p=bezier_board.getBottom3D(tup.x, (tup.z+(op.z-tup.z)*0.6));
				deck_points[i][3]=new NurbsPoint(p.x,p.y,-p.z);
				deck_points[i][deck.get_nr_of_points()-4]=new NurbsPoint(p.x,p.y,p.z);
			
				//set outline
			
				deck_points[i][4]=new NurbsPoint(op.x,op.y,-op.z);
				deck_points[i][deck.get_nr_of_points()-5]=new NurbsPoint(op.x,op.y,op.z);
			
				//set mid top

				p=bezier_board.getTop3D(tup.x, 0);
				deck_points[i][deck.get_nr_of_points()/2]=new NurbsPoint(p.x,p.y,p.z);

				//set rest of top
			
				temp_n=0;
			
				for(int j=6;j<deck.get_nr_of_points()/2;j++)
				{
					temp_n++;
				
					p=bezier_board.getTop3D(tup.x, op.z-op.z/ ((deck.get_nr_of_points()+1)/2-6)*temp_n);
					deck_points[i][j]=new NurbsPoint(p.x,p.y,-p.z);
					deck_points[i][bottom.get_nr_of_points()-j-1]=new NurbsPoint(p.x,p.y,p.z);
				}
				
				//set rail top
			
				p=bezier_board.getTop3D(tup.x, op.z-(op.z+deck_points[i][6].z)/100);
				deck_points[i][5]=new NurbsPoint(p.x,p.y,-p.z);
				deck_points[i][deck.get_nr_of_points()-6]=new NurbsPoint(p.x,p.y,p.z);
					
			}
		}


		//set rest of the segments

		
		for(int i=3;i<deck.get_nr_of_segments()-3;i++)
		{
			p=bottom.get_point_on_surface_old(i,0,0,0);
			xval=p.x;
			
			if(xval<0.1)
				xval=0.1;

			op=bezier_board.getOutline3D(xval);
			tup=bezier_board.getTuckedUnder3D(xval);
			
			//set bottom surface
			
			//set tucked under

			for(int j=0;j<5;j++)
			{
				p=new NurbsPoint(tup.x, tup.y, -tup.z);
				bottom_points[i][j]=p;
			}

			for(int j=bottom.get_nr_of_points()-5;j<bottom.get_nr_of_points();j++)
			{
				p=new NurbsPoint(tup.x, tup.y, tup.z);
				bottom_points[i][j]=p;
			}

			//set concave points
			
			double temp_n=0;
			
			for(int j=5;j<bottom.get_nr_of_points()/2-1;j++)
			{
				temp_n++;
				
				p=bezier_board.getBottom3D(tup.x, tup.z-tup.z/ ((bottom.get_nr_of_points()+1)/2-6)*temp_n);
				bottom_points[i][j]=new NurbsPoint(p.x,p.y,-p.z);
				bottom_points[i][bottom.get_nr_of_points()-j-1]=new NurbsPoint(p.x,p.y,p.z);
			}
			
			//set mid point
			
			p=bezier_board.getBottom3D(tup.x, 0);
			
			bottom_points[i][bottom.get_nr_of_points()/2-1]=new NurbsPoint(p.x,p.y,bottom_points[i][bottom.get_nr_of_points()/2-2].z/6);
			bottom_points[i][bottom.get_nr_of_points()/2]=new NurbsPoint(p.x,p.y,0);
			bottom_points[i][bottom.get_nr_of_points()/2+1]=new NurbsPoint(p.x,p.y,bottom_points[i][bottom.get_nr_of_points()/2+2].z/6);			
						
			
			//set deck surface

			//set tucked under

			for(int j=0;j<3;j++)
			{
				p=new NurbsPoint(tup.x, tup.y, -tup.z);
				deck_points[i][j]=p;
			}

			for(int j=deck.get_nr_of_points()-3;j<deck.get_nr_of_points();j++)
			{
				p=new NurbsPoint(tup.x, tup.y, tup.z);
				deck_points[i][j]=p;
			}

			//set rail under

			p=bezier_board.getBottom3D(tup.x, (tup.z+(op.z-tup.z)*0.6));
			deck_points[i][3]=new NurbsPoint(p.x,p.y,-p.z);
			deck_points[i][deck.get_nr_of_points()-4]=new NurbsPoint(p.x,p.y,p.z);
			
			//set outline
			
			deck_points[i][4]=new NurbsPoint(op.x,op.y,-op.z);
			deck_points[i][deck.get_nr_of_points()-5]=new NurbsPoint(op.x,op.y,op.z);
			
			//set mid top

			p=bezier_board.getTop3D(tup.x, 0);
			deck_points[i][deck.get_nr_of_points()/2]=new NurbsPoint(p.x,p.y,p.z);

			//set rest of top
			
			temp_n=0;
			
			for(int j=6;j<deck.get_nr_of_points()/2;j++)
			{
				temp_n++;
				
				p=bezier_board.getTop3D(tup.x, op.z-op.z/ ((deck.get_nr_of_points()+1)/2-6)*temp_n);
//				p=bezier_board.getBottom3D(tup.x, tup.z/ ((bottom.get_nr_of_points()+1)/2-6)*temp_n);
				deck_points[i][j]=new NurbsPoint(p.x,p.y,-p.z);
				deck_points[i][bottom.get_nr_of_points()-j-1]=new NurbsPoint(p.x,p.y,p.z);
			}
				
			//set rail top
			
			p=bezier_board.getTop3D(tup.x, op.z-(op.z+deck_points[i][6].z)/5.25);
			deck_points[i][5]=new NurbsPoint(p.x,p.y,-p.z);
			deck_points[i][deck.get_nr_of_points()-6]=new NurbsPoint(p.x,p.y,p.z);
					
						
		}

		create_deck(deck.get_nr_of_segments(),deck.get_nr_of_points(),deck_points);
		create_bottom(bottom.get_nr_of_segments(),bottom.get_nr_of_points(),bottom_points);
		
		//fix end points etc.
		repair();

		approximate_bezier2(bezier_board);

		transform_with_operator();		
	}

	
	/**
	 * Fits the outline and the rocker of the current 3D model to the bezier curves.
	 *
	 * @param bezier_board - the bezier model that we want to fit
	 */	 
	 public void approximate_bezier2(BezierBoard bezier_board)
	{

		double xval, zval;
		NurbsPoint op, tup, p;

		//approximate outline
		for(int i=0;i<deck.get_nr_of_segments();i++)
		{
			//find outline-point on nurbs-board
			p=deck.get_point_on_surface(i,4,0,0);
			
			//find outline point on bezier-board
			if(p.x<0.1)
				p.x=0.1;
				
			op=bezier_board.getOutline3D(p.x);
			
			//scale nurbs segment
			if(Math.abs(p.z)>0.1)
			{
				double scale_factor=op.z/Math.abs(p.z);
				for(int j=0;j<deck.get_nr_of_points();j++)
				{
					p=deck.get_control_point(i,j);
					deck.set_control_point(i,j,new NurbsPoint(p.x,p.y,p.z*scale_factor));
					
					p=bottom.get_control_point(i,j);
					bottom.set_control_point(i,j,new NurbsPoint(p.x,p.y,p.z*scale_factor));
				}
			}
		}

		//approximate rocker
	
		for(int i=0;i<bottom.get_nr_of_segments();i++)
		{
			//find bottom-point on nurbs-board
			p=bottom.get_point_on_surface(i,bottom.get_nr_of_points()/2,0,0);
			
			//find bottom point on bezier-board
			op=bezier_board.getBottom3D(p.x,0);
				
			//scale nurbs segment
			double diff=op.y-p.y;
			for(int j=0;j<bottom.get_nr_of_points();j++)
			{
				p=deck.get_control_point(i,j);
				deck.set_control_point(i,j,new NurbsPoint(p.x,p.y+diff,p.z));
				
				p=bottom.get_control_point(i,j);
				bottom.set_control_point(i,j,new NurbsPoint(p.x,p.y+diff,p.z));
			}
		}
				
		deck.evaluate_surface();
		bottom.evaluate_surface();
	}



	/**
	 * Creates a 3D model of the boards by fitting Bezier patches to the 2D curves.
	 *
	 * This produces a surface that fits the 2D curves much closer than the approximated
	 * b-spline surfaces above. On the other hand the surfaces becomes less intuitive 
	 * to edit compared to the bspline-surfaces.
	 *
	 * @param bezier_board - the bezier model that we want to fit
	 * @return int -1 if error
	 */	 
	public int create_bezier_patch(BezierBoard bezier_board)
	{
		BezierBoardCrossSection cross_section, cross_section2, cross_section3;
		BezierSpline bezier_spline, bezier_spline2, bezier_spline3;
		BezierKnot control_point, control_point2, control_point3;
		
		double xval, zval, dist, dist2;
		NurbsPoint op, tup, p, tangent1, tangent2;

		java.awt.geom.Point2D p2d, p2d2, p2d3;

		NurbsSurface old_deck=deck;
		NurbsSurface old_bottom=bottom;

/*		
		//All bezier splines need to have exactly 4 or 5 control points
		boolean four_control_points=true;
		boolean five_control_points=true;
		for(int i = 0; i < bezier_board.mCrossSections.size(); i++)
		{
			if(bezier_board.mCrossSections.get(i).getBezierSpline().size()!=4)
				four_control_points=false;
			if(bezier_board.mCrossSections.get(i).getBezierSpline().size()!=5)
				five_control_points=false;
			System.out.println("loopar: " + i);
		}
			System.out.println("slutloopat... ");
		
		if( (!four_control_points) && (!five_control_points) )
		{
			System.out.println("error");
			return -1;
		}
*/		
		
		//use smart placement of knots
		double array1[]=new double[100];
		double array2[]=new double[100];
		double array3[]=new double[100];
		int n1,n2,n3;
		
		System.out.println("Placing knots");
		
		//read knots from outline
		n1=0;
		BezierSpline knotArray=bezier_board.getOutline();
		for(int i=0;i<knotArray.getNrOfControlPoints();i++)
		{
			if(knotArray.getControlPoint(i).getPoints()[0].x>1 && knotArray.getControlPoint(i).getPoints()[0].x<bezier_board.getLength()-1)
			{
				array1[n1]=knotArray.getControlPoint(i).getPoints()[0].x;
				n1++;
			}
		}
		
		System.out.println("Number of knots in outline = " + n1);
		
		//read knots from nurbs surface
		boolean needed;
		n2=0;
		array2[n2]=0.0;
		n2++;
		array2[n2]=0.1;
		n2++;

		for(int i=3;i<old_deck.get_nr_of_segments()-3;i++)
		{
			xval=old_bottom.get_point_on_surface(i,0,0.0,0.0).x/10;
			needed=true;
			
			for(int j=0;j<n1;j++)
			{
				if(Math.abs(array1[j]-xval)<8)
					needed=false;
			}
			
			if(needed)
			{
			 	array2[n2]=xval;
			 	n2++;
			}
		}
		array2[n2]=bezier_board.getLength()-0.1;
		n2++;
		array2[n2]=bezier_board.getLength();
		n2++;

		
		System.out.println("Number of extra knots from nurbs board = " + n2);
			
		
		//joint knots in new array
		array3[0]=array2[0];
		n3=1;
		for(int i=1; i<n2; i++)
		{
			for(int j=0; j<n1; j++)
			{
				if(array1[j]>array3[n3-1] && array1[j]<array2[i])
				{
					array3[n3]=array1[j];
					n3++;
				}
			}
			array3[n3]=array2[i];
			n3++;
		}

		System.out.println("Total number of knots = " + n3);
		for(int i=0;i<n3;i++)
			System.out.println("xval = " + array3[i]);
		
		cross_section=new BezierBoardCrossSection();
		cross_section=(BezierBoardCrossSection)(bezier_board.getCurrentCrossSection()).clone();
		bezier_spline=cross_section.getBezierSpline();
		int spline_size=bezier_spline.getNrOfControlPoints();
		
		System.out.println("Spline size = " + spline_size);

		int segments=3*(n3-2)+2;
		System.out.println("Number of segments = " + segments);
		
		if(spline_size==5)
		{
			deck=new NurbsSurface(segments,23);
			for(int i=0;i<segments;i++)
				for(int j=0;j<23;j++)
					deck.set_control_point(i,j,new NurbsPoint(0,0,0));
			
			bottom=new NurbsSurface(segments,11);
			for(int i=0;i<segments;i++)
				for(int j=0;j<11;j++)
					bottom.set_control_point(i,j,new NurbsPoint(0,0,0));
		}
		// PW Insert for Channel Correction - Typical Cross section = 5, so use standard Deck profile, but add more bottom segments?
		// 2 midspan bottom control points makes it the same as the deck...
		else if(spline_size >= 6)
		{
			System.out.println("-------------------------");
			System.out.println("Channel Interp - Num of Additional Bottom Points = " + (spline_size - 5));
			System.out.println("-------------------------");

			deck=new NurbsSurface(segments,23);
			for(int i=0;i<segments;i++)
				for(int j=0;j<23;j++)
					deck.set_control_point(i,j,new NurbsPoint(0,0,0));
			
			bottom=new NurbsSurface(segments,(spline_size-3)*6-1); //was 23 for ss = 7 
			for(int i=0;i<segments;i++)
				for(int j=0;j<(spline_size-3)*6-1;j++)
					bottom.set_control_point(i,j,new NurbsPoint(0,0,0));
		}
		// PW Insert for Channel Correction 		
		else
		{
			deck=new NurbsSurface(segments,17);
			for(int i=0;i<segments;i++)
				for(int j=0;j<17;j++)
					deck.set_control_point(i,j,new NurbsPoint(0,0,0));
			
			bottom=new NurbsSurface(segments,11);
			for(int i=0;i<segments;i++)
				for(int j=0;j<11;j++)
					bottom.set_control_point(i,j,new NurbsPoint(0,0,0));
		
		}	
		
	
				
		System.out.println("Creating Surfaces");
	
		NurbsPoint[][] bottom_points=new NurbsPoint[bottom.get_nr_of_segments()][bottom.get_nr_of_points()];
		NurbsPoint[][] deck_points=new NurbsPoint[deck.get_nr_of_segments()][deck.get_nr_of_points()];	
	
		int mysegment=0;
		double distfwd, distbwd;
		
		for(int i=2;i<deck.get_nr_of_segments()-1;i=i+3)
		{
			System.out.println("Calculating position of cross section i=" + i);
			
			mysegment++;
			xval=array3[mysegment];
			distbwd=(-array3[mysegment-1]+xval)/3;
			distfwd=(array3[mysegment+1]-xval)/3;
			
			dist=1.0;

			if(xval<0.1)
				xval=0.1;
				
			if(xval>bezier_board.getLength()-0.1)
				xval=bezier_board.getLength()-0.1;
				
			System.out.println("xval=" + xval + " board length=" + bezier_board.getLength());
			cross_section=new BezierBoardCrossSection();
			cross_section=(BezierBoardCrossSection)(bezier_board.getInterpolatedCrossSection(xval)).clone();
			bezier_spline=cross_section.getBezierSpline();
			if(bezier_spline.getNrOfControlPoints()!=spline_size)
				return -1;
			
			cross_section2=new BezierBoardCrossSection();
			cross_section2=(BezierBoardCrossSection)(bezier_board.getInterpolatedCrossSection(xval+0.1)).clone();
			bezier_spline2=cross_section2.getBezierSpline();
			if(bezier_spline2.getNrOfControlPoints()!=spline_size)
				return -1;

			cross_section3=new BezierBoardCrossSection();
			cross_section3=(BezierBoardCrossSection)(bezier_board.getInterpolatedCrossSection(xval-0.1)).clone();
			bezier_spline3=cross_section3.getBezierSpline();
			if(bezier_spline3.getNrOfControlPoints()!=spline_size)
				return -1;
			
			//**************tucked under
			// PW Insert for Channel Correction - Assumes all additional points are on bottom, no good for deck grooves etc...
			if (spline_size > 5)
			{
				control_point=bezier_spline.getControlPoint(spline_size - 4);
				control_point2=bezier_spline2.getControlPoint(spline_size - 4);
				control_point3=bezier_spline3.getControlPoint(spline_size - 4);
			}
			// PW Insert for Channel Correction 
			else
			{
				control_point=bezier_spline.getControlPoint(1);
				control_point2=bezier_spline2.getControlPoint(1);
				control_point3=bezier_spline3.getControlPoint(1);
			}
			//center point

			p2d=control_point.getEndPoint();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			
			deck_points[i][2]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			deck_points[i][deck.get_nr_of_points()-3]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
			dist=distfwd;
			p2d2=control_point2.getEndPoint();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));

			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			deck_points[i+1][2]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
			deck_points[i+1][deck.get_nr_of_points()-3]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), 10*(p2d.getX()+tangent1.z*dist));

			System.out.println("Tangent 1 x=" + tangent1.x + " y=" + tangent1.y + " z="+ tangent1.z);

			dist=distbwd;
			
			p2d3=control_point3.getEndPoint();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			deck_points[i-1][2]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
			deck_points[i-1][deck.get_nr_of_points()-3]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), 10*(p2d.getX()+tangent2.z*dist));
			
			//tangent to next

			p2d=control_point.getTangentToNext();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			deck_points[i][3]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			deck_points[i][deck.get_nr_of_points()-4]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

			dist=distfwd;
			p2d2=control_point2.getTangentToNext();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			deck_points[i+1][3]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//			deck_points[i+1][3]=new NurbsPoint(deck_points[i][3].x+deck_points[i+1][2].x-deck_points[i][2].x, deck_points[i][3].y+deck_points[i+1][2].y-deck_points[i][2].y, deck_points[i][3].z+deck_points[i+1][2].z-deck_points[i][2].z);
			deck_points[i+1][deck.get_nr_of_points()-4]=new NurbsPoint(deck_points[i+1][3].x,deck_points[i+1][3].y,-deck_points[i+1][3].z);

			dist=distbwd;
			p2d3=control_point3.getTangentToNext();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			deck_points[i-1][3]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//			deck_points[i-1][3]=new NurbsPoint(deck_points[i][3].x+deck_points[i-1][2].x-deck_points[i][2].x, deck_points[i][3].y+deck_points[i-1][2].y-deck_points[i][2].y, deck_points[i][3].z+deck_points[i-1][2].z-deck_points[i][2].z);
			deck_points[i-1][deck.get_nr_of_points()-4]=new NurbsPoint(deck_points[i-1][3].x,deck_points[i-1][3].y,-deck_points[i-1][3].z);
			
			//***********************rail
			// PW Insert for Channel Correction - Assumes all additional points are on bottom, no good for deck grooves etc...
			if (spline_size > 5)
			{
				control_point=bezier_spline.getControlPoint(spline_size - 3);
				control_point2=bezier_spline2.getControlPoint(spline_size - 3);
				control_point3=bezier_spline3.getControlPoint(spline_size - 3);
			}
			// PW Insert for Channel Correction 
			else
			{
				control_point=bezier_spline.getControlPoint(2);
				control_point2=bezier_spline2.getControlPoint(2);
				control_point3=bezier_spline3.getControlPoint(2);	
			}
			//center point

			p2d=control_point.getEndPoint();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			deck_points[i][5]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			deck_points[i][deck.get_nr_of_points()-6]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
			
			dist=distfwd;
			p2d2=control_point2.getEndPoint();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			deck_points[i+1][5]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
			deck_points[i+1][deck.get_nr_of_points()-6]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), 10*(p2d.getX()+tangent1.z*dist));
						
			dist=distbwd;
			p2d3=control_point3.getEndPoint();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			deck_points[i-1][5]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
			deck_points[i-1][deck.get_nr_of_points()-6]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), 10*(p2d.getX()+tangent2.z*dist));

			//tangent to prev

			p2d=control_point.getTangentToPrev();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			deck_points[i][4]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			deck_points[i][deck.get_nr_of_points()-5]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

			dist=distfwd;
			p2d2=control_point2.getTangentToPrev();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			deck_points[i+1][4]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//			deck_points[i+1][4]=new NurbsPoint(deck_points[i][4].x+deck_points[i+1][5].x-deck_points[i][5].x, deck_points[i][4].y+deck_points[i+1][5].y-deck_points[i][5].y, deck_points[i][4].z+deck_points[i+1][5].z-deck_points[i][5].z);
			deck_points[i+1][deck.get_nr_of_points()-5]=new NurbsPoint(deck_points[i+1][4].x,deck_points[i+1][4].y,-deck_points[i+1][4].z);

			dist=distbwd;
			p2d3=control_point3.getTangentToPrev();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			deck_points[i-1][4]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//			deck_points[i-1][4]=new NurbsPoint(deck_points[i][4].x+deck_points[i-1][5].x-deck_points[i][5].x, deck_points[i][4].y+deck_points[i-1][5].y-deck_points[i][5].y, deck_points[i][4].z+deck_points[i-1][5].z-deck_points[i][5].z);
			deck_points[i-1][deck.get_nr_of_points()-5]=new NurbsPoint(deck_points[i-1][4].x,deck_points[i-1][4].y,-deck_points[i-1][4].z);

			//tangent to next

			p2d=control_point.getTangentToNext();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			deck_points[i][6]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			deck_points[i][deck.get_nr_of_points()-7]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

			dist=distfwd;
			p2d2=control_point2.getTangentToNext();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			deck_points[i+1][6]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//			deck_points[i+1][6]=new NurbsPoint(deck_points[i][6].x+deck_points[i+1][5].x-deck_points[i][5].x, deck_points[i][6].y+deck_points[i+1][5].y-deck_points[i][5].y, deck_points[i][6].z+deck_points[i+1][5].z-deck_points[i][5].z);
			deck_points[i+1][deck.get_nr_of_points()-7]=new NurbsPoint(deck_points[i+1][6].x,deck_points[i+1][6].y,-deck_points[i+1][6].z);

			dist=distbwd;
			p2d3=control_point3.getTangentToNext();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			deck_points[i-1][6]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//			deck_points[i-1][6]=new NurbsPoint(deck_points[i][6].x+deck_points[i-1][5].x-deck_points[i][5].x, deck_points[i][6].y+deck_points[i-1][5].y-deck_points[i][5].y, deck_points[i][6].z+deck_points[i-1][5].z-deck_points[i][5].z);
			deck_points[i-1][deck.get_nr_of_points()-7]=new NurbsPoint(deck_points[i-1][6].x,deck_points[i-1][6].y,-deck_points[i-1][6].z);

			// PW modified for Channels - Deck assumed un modified (ie same as 5 point option. >= was  ==
			if(spline_size>=5)
			{			
				
				//***************deck 1
				// PW Insert for Channel Correction 
				if (spline_size > 5)
				{
					control_point=bezier_spline.getControlPoint(spline_size - 2);
					control_point2=bezier_spline2.getControlPoint(spline_size - 2);
					control_point3=bezier_spline3.getControlPoint(spline_size - 2);
				}
				// PW Insert for Channel Correction 
				else
				{
					control_point=bezier_spline.getControlPoint(3);
					control_point2=bezier_spline2.getControlPoint(3);
					control_point3=bezier_spline3.getControlPoint(3);
				}
				//center point
	
				p2d=control_point.getEndPoint();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][8]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
				deck_points[i][deck.get_nr_of_points()-9]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getEndPoint();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][8]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
				deck_points[i+1][deck.get_nr_of_points()-9]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), 10*(p2d.getX()+tangent1.z*dist));
							
				dist=distbwd;
				p2d3=control_point3.getEndPoint();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][8]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
				deck_points[i-1][deck.get_nr_of_points()-9]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), 10*(p2d.getX()+tangent2.z*dist));
	
				//tangent to prev
	
				p2d=control_point.getTangentToPrev();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][7]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
				deck_points[i][deck.get_nr_of_points()-8]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getTangentToPrev();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][7]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//				deck_points[i+1][7]=new NurbsPoint(deck_points[i][7].x+deck_points[i+1][8].x-deck_points[i][8].x, deck_points[i][7].y+deck_points[i+1][8].y-deck_points[i][8].y, deck_points[i][7].z+deck_points[i+1][8].z-deck_points[i][8].z);
				deck_points[i+1][deck.get_nr_of_points()-8]=new NurbsPoint(deck_points[i+1][7].x,deck_points[i+1][7].y,-deck_points[i+1][7].z);
	
				dist=distbwd;
				p2d3=control_point3.getTangentToPrev();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][7]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//				deck_points[i-1][7]=new NurbsPoint(deck_points[i][7].x+deck_points[i-1][8].x-deck_points[i][8].x, deck_points[i][7].y+deck_points[i-1][8].y-deck_points[i][8].y, deck_points[i][7].z+deck_points[i-1][8].z-deck_points[i][8].z);
				deck_points[i-1][deck.get_nr_of_points()-8]=new NurbsPoint(deck_points[i-1][7].x,deck_points[i-1][7].y,-deck_points[i-1][7].z);
	
				//tangent to next
				
				p2d=control_point.getTangentToNext();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][9]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
				deck_points[i][deck.get_nr_of_points()-10]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getTangentToNext();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][9]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//				deck_points[i+1][9]=new NurbsPoint(deck_points[i][9].x+deck_points[i+1][8].x-deck_points[i][8].x, deck_points[i][9].y+deck_points[i+1][8].y-deck_points[i][8].y, deck_points[i][9].z+deck_points[i+1][8].z-deck_points[i][8].z);
				deck_points[i+1][deck.get_nr_of_points()-10]=new NurbsPoint(deck_points[i+1][9].x,deck_points[i+1][9].y,-deck_points[i+1][9].z);
	
				dist=distbwd;
				p2d3=control_point3.getTangentToNext();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][9]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//				deck_points[i-1][9]=new NurbsPoint(deck_points[i][9].x+deck_points[i-1][8].x-deck_points[i][8].x, deck_points[i][9].y+deck_points[i-1][8].y-deck_points[i][8].y, deck_points[i][9].z+deck_points[i-1][8].z-deck_points[i][8].z);
				deck_points[i-1][deck.get_nr_of_points()-10]=new NurbsPoint(deck_points[i-1][9].x,deck_points[i-1][9].y,-deck_points[i-1][9].z);
	
	
				//***************deck top
				// PW Insert for Channel Correction 
				if (spline_size > 5)
				{
					control_point=bezier_spline.getControlPoint(spline_size - 1);
					control_point2=bezier_spline2.getControlPoint(spline_size - 1);
					control_point3=bezier_spline3.getControlPoint(spline_size - 1);
				}
				// PW Insert for Channel Correction 
				else
				{
					control_point=bezier_spline.getControlPoint(4);
					control_point2=bezier_spline2.getControlPoint(4);
					control_point3=bezier_spline3.getControlPoint(4);
				}
				//center point
	
				p2d=control_point.getEndPoint();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][11]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getEndPoint();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][11]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
							
				dist=distbwd;
				p2d3=control_point3.getEndPoint();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][11]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
	
				//tangent to prev
	
				p2d=control_point.getTangentToPrev();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][10]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
				deck_points[i][deck.get_nr_of_points()-11]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getTangentToPrev();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][10]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//				deck_points[i+1][10]=new NurbsPoint(deck_points[i][10].x+deck_points[i+1][11].x-deck_points[i][11].x, deck_points[i][10].y+deck_points[i+1][11].y-deck_points[i][11].y, deck_points[i][10].z+deck_points[i+1][11].z-deck_points[i][11].z);
				deck_points[i+1][deck.get_nr_of_points()-11]=new NurbsPoint(deck_points[i+1][10].x,deck_points[i+1][10].y,-deck_points[i+1][10].z);
	
				dist=distbwd;
				p2d3=control_point3.getTangentToPrev();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][10]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//				deck_points[i-1][10]=new NurbsPoint(deck_points[i][10].x+deck_points[i-1][11].x-deck_points[i][11].x, deck_points[i][10].y+deck_points[i-1][11].y-deck_points[i][11].y, deck_points[i][10].z+deck_points[i-1][11].z-deck_points[i][11].z);
				deck_points[i-1][deck.get_nr_of_points()-11]=new NurbsPoint(deck_points[i-1][10].x,deck_points[i-1][10].y,-deck_points[i-1][10].z);
	
			}
			else
			{

				control_point=bezier_spline.getControlPoint(3);
				control_point2=bezier_spline2.getControlPoint(3);
				control_point3=bezier_spline3.getControlPoint(3);
	
				//center point
	
				p2d=control_point.getEndPoint();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][8]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getEndPoint();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][8]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
							
				dist=distbwd;
				p2d3=control_point3.getEndPoint();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][8]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
	
				//tangent to prev
	
				p2d=control_point.getTangentToPrev();
				p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
				deck_points[i][7]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
				deck_points[i][deck.get_nr_of_points()-8]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
	
				dist=distfwd;
				p2d2=control_point2.getTangentToPrev();
				p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
				tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
				deck_points[i+1][7]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//				deck_points[i+1][7]=new NurbsPoint(deck_points[i][7].x+deck_points[i+1][8].x-deck_points[i][8].x, deck_points[i][7].y+deck_points[i+1][8].y-deck_points[i][8].y, deck_points[i][7].z+deck_points[i+1][8].z-deck_points[i][8].z);
				deck_points[i+1][deck.get_nr_of_points()-8]=new NurbsPoint(deck_points[i+1][7].x,deck_points[i+1][7].y,-deck_points[i+1][7].z);
	
				dist=distbwd;
				p2d3=control_point3.getTangentToPrev();
				p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
				dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
				tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
				deck_points[i-1][7]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//				deck_points[i-1][7]=new NurbsPoint(deck_points[i][7].x+deck_points[i-1][8].x-deck_points[i][8].x, deck_points[i][7].y+deck_points[i-1][8].y-deck_points[i][8].y, deck_points[i][7].z+deck_points[i-1][8].z-deck_points[i][8].z);
				deck_points[i-1][deck.get_nr_of_points()-8]=new NurbsPoint(deck_points[i-1][7].x,deck_points[i-1][7].y,-deck_points[i-1][7].z);
	
	
			}


		}


		mysegment=0;
		
		for(int i=2;i<bottom.get_nr_of_segments()-1;i=i+3)
		{
		
			mysegment++;
			dist=1.0;

			xval=array3[mysegment];
			distbwd=(-array3[mysegment-1]+xval)/3;
			distfwd=(array3[mysegment+1]-xval)/3;



			if(xval<0.1)
				xval=0.1;
				
			if(xval>bezier_board.getLength()-0.1)
				xval=bezier_board.getLength()-0.1;
		
		
			cross_section=new BezierBoardCrossSection();
			cross_section=(BezierBoardCrossSection)(bezier_board.getInterpolatedCrossSection(xval)).clone();
			bezier_spline=cross_section.getBezierSpline();

			cross_section2=new BezierBoardCrossSection();
			cross_section2=(BezierBoardCrossSection)(bezier_board.getInterpolatedCrossSection(xval+0.1)).clone();
			bezier_spline2=cross_section2.getBezierSpline();

			cross_section3=new BezierBoardCrossSection();
			cross_section3=(BezierBoardCrossSection)(bezier_board.getInterpolatedCrossSection(xval-0.1)).clone();
			bezier_spline3=cross_section3.getBezierSpline();
			
			//*************************tucked under
						// PW Insert for Channel Correction - Assumes all additional points are on bottom, no good for deck grooves etc...
			if (spline_size > 5)
			{
				control_point=bezier_spline.getControlPoint(spline_size - 4);
				control_point2=bezier_spline2.getControlPoint(spline_size - 4);
				control_point3=bezier_spline3.getControlPoint(spline_size - 4);
			}
			// PW Insert for Channel Correction 
			else
			{
				control_point=bezier_spline.getControlPoint(1);
				control_point2=bezier_spline2.getControlPoint(1);
				control_point3=bezier_spline3.getControlPoint(1);
			}

			//center point
			System.out.println("setting bottom rail point");

			dist=distfwd;
			p2d=control_point.getEndPoint();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			bottom_points[i][2]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			bottom_points[i][bottom.get_nr_of_points()-3]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

			p2d2=control_point2.getEndPoint();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			bottom_points[i+1][2]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
			bottom_points[i+1][bottom.get_nr_of_points()-3]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), 10*(p2d.getX()+tangent1.z*dist));
					
			dist=distbwd;
			p2d3=control_point3.getEndPoint();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			bottom_points[i-1][2]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
			bottom_points[i-1][bottom.get_nr_of_points()-3]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), 10*(p2d.getX()+tangent2.z*dist));
			
			//tangent to prev
			System.out.println("setting bottom tangent to prev point");

			p2d=control_point.getTangentToPrev();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			bottom_points[i][3]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			bottom_points[i][bottom.get_nr_of_points()-4]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

			dist=distfwd;
			p2d2=control_point2.getTangentToPrev();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			bottom_points[i+1][3]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//			bottom_points[i+1][3]=new NurbsPoint(bottom_points[i][3].x+bottom_points[i+1][2].x-bottom_points[i][2].x, bottom_points[i][3].y+bottom_points[i+1][2].y-bottom_points[i][2].y, bottom_points[i][3].z+bottom_points[i+1][2].z-bottom_points[i][2].z);
			bottom_points[i+1][bottom.get_nr_of_points()-4]=new NurbsPoint(bottom_points[i+1][3].x,bottom_points[i+1][3].y,-bottom_points[i+1][3].z);

			dist=distbwd;
			p2d3=control_point3.getTangentToPrev();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			bottom_points[i-1][3]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//			bottom_points[i-1][3]=new NurbsPoint(bottom_points[i][3].x+bottom_points[i-1][2].x-bottom_points[i][2].x, bottom_points[i][3].y+bottom_points[i-1][2].y-bottom_points[i][2].y, bottom_points[i][3].z+bottom_points[i-1][2].z-bottom_points[i][2].z);
			bottom_points[i-1][bottom.get_nr_of_points()-4]=new NurbsPoint(bottom_points[i-1][3].x,bottom_points[i-1][3].y,-bottom_points[i-1][3].z);

			//PW - need to do something in here for all internal control points?
			if (spline_size > 5)
			{
				for(int k = 0;k < (spline_size - 5);k++) 
				// for each intermediate control point - generate Nurbs Pt - should reference Board Property - num bottom points
				{
					control_point=bezier_spline.getControlPoint(spline_size - 5 - k);
					control_point2=bezier_spline2.getControlPoint(spline_size - 5 - k);
					control_point3=bezier_spline3.getControlPoint(spline_size - 5 - k);
				
					p2d=control_point.getEndPoint();
					p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
					bottom_points[i][2+((k+1)*3)]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
					bottom_points[i][bottom.get_nr_of_points()-(3+((k+1)*3))]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());
					
					dist=distfwd;
					p2d2=control_point2.getEndPoint();
					p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
					dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
					tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
					bottom_points[i+1][2+((k+1)*3)]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
					bottom_points[i+1][bottom.get_nr_of_points()-(3+((k+1)*3))]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), 10*(p2d.getX()+tangent1.z*dist));
								
					dist=distbwd;
					p2d3=control_point3.getEndPoint();
					p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
					dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
					tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
					bottom_points[i-1][2+((k+1)*3)]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
					bottom_points[i-1][bottom.get_nr_of_points()-(3+((k+1)*3))]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), 10*(p2d.getX()+tangent2.z*dist));

					//tangent to prev

					p2d=control_point.getTangentToNext();
					p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
					bottom_points[i][1+((k+1)*3)]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
					bottom_points[i][bottom.get_nr_of_points()-(2+((k+1)*3))]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

					dist=distfwd;
					p2d2=control_point2.getTangentToNext();
					p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
					dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
					tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
					bottom_points[i+1][1+((k+1)*3)]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
		//			bottom_points[i+1][4]=new NurbsPoint(bottom_points[i][4].x+bottom_points[i+1][5].x-bottom_points[i][5].x, bottom_points[i][4].y+bottom_points[i+1][5].y-bottom_points[i][5].y, bottom_points[i][4].z+bottom_points[i+1][5].z-bottom_points[i][5].z);
					bottom_points[i+1][bottom.get_nr_of_points()-(2+((k+1)*3))]=new NurbsPoint(bottom_points[i+1][1+((k+1)*3)].x,bottom_points[i+1][1+((k+1)*3)].y,-bottom_points[i+1][1+((k+1)*3)].z);

					dist=distbwd;
					p2d3=control_point3.getTangentToNext();
					p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
					dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
					tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
					bottom_points[i-1][1+((k+1)*3)]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
		//			bottom_points[i-1][4]=new NurbsPoint(bottom_points[i][4].x+bottom_points[i-1][5].x-bottom_points[i][5].x, bottom_points[i][4].y+bottom_points[i-1][5].y-bottom_points[i][5].y, bottom_points[i][4].z+bottom_points[i-1][5].z-bottom_points[i][5].z);
					bottom_points[i-1][bottom.get_nr_of_points()-(2+((k+1)*3))]=new NurbsPoint(bottom_points[i-1][1+((k+1)*3)].x,bottom_points[i-1][1+((k+1)*3)].y,-bottom_points[i-1][1+((k+1)*3)].z);

					//tangent to next

					p2d=control_point.getTangentToPrev();
					p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
					bottom_points[i][3+((k+1)*3)]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
					bottom_points[i][bottom.get_nr_of_points()-(4+((k+1)*3))]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

					dist=distfwd;
					p2d2=control_point2.getTangentToPrev();
					p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
					dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
					tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
					bottom_points[i+1][3+((k+1)*3)]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
		//			bottom_points[i+1][6]=new NurbsPoint(bottom_points[i][6].x+bottom_points[i+1][5].x-bottom_points[i][5].x, bottom_points[i][6].y+bottom_points[i+1][5].y-bottom_points[i][5].y, bottom_points[i][6].z+bottom_points[i+1][5].z-bottom_points[i][5].z);
					bottom_points[i+1][bottom.get_nr_of_points()-(4+((k+1)*3))]=new NurbsPoint(bottom_points[i+1][3+((k+1)*3)].x,bottom_points[i+1][3+((k+1)*3)].y,-bottom_points[i+1][3+((k+1)*3)].z);

					dist=distbwd;
					p2d3=control_point3.getTangentToPrev();
					p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
					dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
					tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
					bottom_points[i-1][3+((k+1)*3)]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
		//			bottom_points[i-1][6]=new NurbsPoint(bottom_points[i][6].x+bottom_points[i-1][5].x-bottom_points[i][5].x, bottom_points[i][6].y+bottom_points[i-1][5].y-bottom_points[i][5].y, bottom_points[i][6].z+bottom_points[i-1][5].z-bottom_points[i][5].z);
					bottom_points[i-1][bottom.get_nr_of_points()-(4+((k+1)*3))]=new NurbsPoint(bottom_points[i-1][3+((k+1)*3)].x,bottom_points[i-1][3+((k+1)*3)].y,-bottom_points[i-1][3+((k+1)*3)].z);
//_______________________________________________________________________________
				
				}
			}
			// PW Insert for Channel Correction 


			//bottom center

			//*************** bottom

			control_point=bezier_spline.getControlPoint(0);
			control_point2=bezier_spline2.getControlPoint(0);
			control_point3=bezier_spline3.getControlPoint(0);
			
			//center point
			System.out.println("setting bottom center point");

			p2d=control_point.getEndPoint();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			bottom_points[i][(bottom.get_nr_of_points()-1)/2]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());

			dist=distfwd;
			p2d2=control_point2.getEndPoint();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			bottom_points[i+1][(bottom.get_nr_of_points()-1)/2]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
						
			dist=distbwd;
			p2d3=control_point3.getEndPoint();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			bottom_points[i-1][(bottom.get_nr_of_points()-1)/2]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));

			//tangent to next
			System.out.println("setting bottom tangent to next");

			p2d=control_point.getTangentToNext();
			p2d.setLocation(p2d.getX(),p2d.getY()+bezier_board.getRockerAtPos(xval));
			bottom_points[i][(bottom.get_nr_of_points()-1)/2-1]=new NurbsPoint(10*xval, 10*p2d.getY(), -10*p2d.getX());
			bottom_points[i][(bottom.get_nr_of_points()-1)/2+1]=new NurbsPoint(10*xval, 10*p2d.getY(), 10*p2d.getX());

			dist=distfwd;
			p2d2=control_point2.getTangentToNext();
			p2d2.setLocation(p2d2.getX(),p2d2.getY()+bezier_board.getRockerAtPos(xval+0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d2.getY()-p2d.getY())*(p2d2.getY()-p2d.getY())+(p2d2.getX()-p2d.getX())*(p2d2.getX()-p2d.getX()));
			tangent1=new NurbsPoint(0.1/dist2, (p2d2.getY()-p2d.getY())/dist2, (p2d2.getX()-p2d.getX())/dist2);
			bottom_points[i+1][(bottom.get_nr_of_points()-1)/2-1]=new NurbsPoint(10*(xval+tangent1.x*dist), 10*(p2d.getY()+tangent1.y*dist), -10*(p2d.getX()+tangent1.z*dist));
//			bottom_points[i+1][4]=new NurbsPoint(bottom_points[i][4].x+bottom_points[i+1][5].x-bottom_points[i][5].x, bottom_points[i][4].y+bottom_points[i+1][5].y-bottom_points[i][5].y, bottom_points[i][4].z+bottom_points[i+1][5].z-bottom_points[i][5].z);
			bottom_points[i+1][(bottom.get_nr_of_points()-1)/2+1]=new NurbsPoint(bottom_points[i+1][(bottom.get_nr_of_points()-1)/2-1].x,bottom_points[i+1][(bottom.get_nr_of_points()-1)/2-1].y,-bottom_points[i+1][(bottom.get_nr_of_points()-1)/2-1].z);

			dist=distbwd;
			p2d3=control_point3.getTangentToNext();
			p2d3.setLocation(p2d3.getX(),p2d3.getY()+bezier_board.getRockerAtPos(xval-0.1));
			dist2=Math.sqrt(0.1*0.1+(p2d3.getY()-p2d.getY())*(p2d3.getY()-p2d.getY())+(p2d3.getX()-p2d.getX())*(p2d3.getX()-p2d.getX()));
			tangent2=new NurbsPoint(0.1/dist2, (p2d3.getY()-p2d.getY())/dist2, (p2d3.getX()-p2d.getX())/dist2);
			bottom_points[i-1][(bottom.get_nr_of_points()-1)/2-1]=new NurbsPoint(10*(xval-tangent2.x*dist), 10*(p2d.getY()+tangent2.y*dist), -10*(p2d.getX()+tangent2.z*dist));
//			bottom_points[i-1][4]=new NurbsPoint(bottom_points[i][4].x+bottom_points[i-1][5].x-bottom_points[i][5].x, bottom_points[i][4].y+bottom_points[i-1][5].y-bottom_points[i][5].y, bottom_points[i][4].z+bottom_points[i-1][5].z-bottom_points[i][5].z);
			//System.out.println("mirror tangent: " + ((bottom.get_nr_of_points()-1)/2+1));
			
			bottom_points[i-1][(bottom.get_nr_of_points()-1)/2+1]=new NurbsPoint(bottom_points[i-1][(bottom.get_nr_of_points()-1)/2-1].x,bottom_points[i-1][(bottom.get_nr_of_points()-1)/2-1].y,-bottom_points[i-1][(bottom.get_nr_of_points()-1)/2-1].z);
			


		}

		//System.out.println("Setting control points - deck");

		//create surfaces
		
		for(int i=2;i<deck.get_nr_of_segments()-2;i++)
		{
			for(int j=2;j<deck.get_nr_of_points()-2;j++)
			{
				deck.set_control_point(i,j,deck_points[i][j]);
			}
		}
		
		//System.out.println("Setting control points - bottom: seg num total = " + bottom.get_nr_of_segments() + " point num total = " + bottom.get_nr_of_points());

		
		for(int i=2;i<bottom.get_nr_of_segments()-2;i++)
		{
			for(int j=2;j<bottom.get_nr_of_points()-2;j++)
			{
				//System.out.println("seg num = " + i + " point num = " + j);
				//System.out.println(bottom_points[i][j]);
				bottom.set_control_point(i,j,bottom_points[i][j]);
			}
		}
		
		System.out.println("Surfaces created");

		//fix tail

		NurbsPoint p1,p2;
		int i,j;

		for(i=0;i<3;i++)
		{				
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				p1=bottom.get_control_point(2,j);
				bottom.set_control_point(i,j,p1);
			}
			for(j=0;j<deck.get_nr_of_points();j++)
			{
				p1=deck.get_control_point(2,j);
				deck.set_control_point(i,j,p1);
			}
		}
		
		//fix nose
		for(i=bottom.get_nr_of_segments()-3;i<bottom.get_nr_of_segments();i++)
		{				
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				p1=bottom.get_control_point(bottom.get_nr_of_segments()-3,j);
				bottom.set_control_point(i,j,p1);
			}
			for(j=0;j<deck.get_nr_of_points();j++)
			{
				p1=deck.get_control_point(deck.get_nr_of_segments()-3,j);
				deck.set_control_point(i,j,p1);
			}
		}
		
		//fix tucked under
		
		for(i=0;i<bottom.get_nr_of_segments();i++)
		{
		
			p1=bottom.get_control_point(i,2);
						
			bottom.set_control_point(i,0,p1);
			bottom.set_control_point(i,1,p1);
			bottom.set_control_point(i,2,p1);
			deck.set_control_point(i,0,p1);
			deck.set_control_point(i,1,p1);
			deck.set_control_point(i,2,p1);

			p2=new NurbsPoint(p1.x, p1.y, -p1.z);
			
			bottom.set_control_point(i,bottom.get_nr_of_points()-3,p2);
			bottom.set_control_point(i,bottom.get_nr_of_points()-2,p2);
			bottom.set_control_point(i,bottom.get_nr_of_points()-1,p2);
			deck.set_control_point(i,deck.get_nr_of_points()-1,p2);
			deck.set_control_point(i,deck.get_nr_of_points()-2,p2);
			deck.set_control_point(i,deck.get_nr_of_points()-3,p2);

		}		
		
		deck.set_knots_bezier();
		deck.set_as_bezier();
		deck.evaluate_surface();
		bottom.set_knots_bezier();
		bottom.set_as_bezier();
		bottom.evaluate_surface();
		
		deck.flipNormal();

		//set names of points
		this.bottom_points=new String[bottom.get_nr_of_points()];
		for(i=0;i<bottom.get_nr_of_points();i++)
		{
			this.bottom_points[i]="bottom " + (i);
		}
		this.deck_points=new String[deck.get_nr_of_points()];
		for(i=0;i<deck.get_nr_of_points();i++)
		{
			this.deck_points[i]="deck " + (i);
		}

		
		return spline_size;
	
	}
		
	
	/**
	 * Move the board to the correct position using a stored transformation
	 */	 
	private void transform_with_operator()
	{
		//transform with operator
		
		if(y_axis_direction.y>0)
		{
			double theta=Math.atan2(x_axis_direction.y,x_axis_direction.x);
			double[][] m = {{ Math.cos(theta),-Math.sin(theta), 0.0 },
				{ Math.sin(theta),Math.cos(theta), 0.0 },
				{ 0.0, 0.0, 1.0 } };			

			double[] t={local_origin.x ,local_origin.y, local_origin.z};	
			System.out.println("t.x= " + t[0]);
			local_origin=new NurbsPoint(0.0,0.0,0.0);
			x_axis_direction=new NurbsPoint(1.0,0.0,0.0);
			y_axis_direction=new NurbsPoint(0.0,1.0,0.0);
			
			
			transform(m,t);			
		}
		else
		{
			double theta=Math.atan2(x_axis_direction.y,x_axis_direction.x);
			double[][] m = {{ Math.cos(theta),-Math.sin(theta), 0.0 },
				{ Math.sin(theta),-Math.cos(theta), 0.0 },
				{ 0.0, 0.0, -1.0 } };			
	
			double[] t={local_origin.x ,local_origin.y, local_origin.z};			
			System.out.println("t.x= " + t[0]);
			local_origin=new NurbsPoint(0.0,0.0,0.0);
			x_axis_direction=new NurbsPoint(1.0,0.0,0.0);
			y_axis_direction=new NurbsPoint(0.0,1.0,0.0);
			transform(m,t);			
		}	
		
	}

		
	
	/**
	 * Adds a surface segment at a given position
	 *
	 * @param x - the position where we want the new segment
	 */	 
	public void add_segment(double x)
	{
		NurbsPoint[][] bottom_points=new NurbsPoint[bottom.get_nr_of_segments()+1][bottom.get_nr_of_points()];
		NurbsPoint[][] deck_points=new NurbsPoint[deck.get_nr_of_segments()+1][deck.get_nr_of_points()];

		NurbsPoint p1,p2;
		
		int k=0;
		do
		{
			k++;
			p1=bottom.get_control_point(k, 0);
		}while(p1.x<=x && k<bottom.get_nr_of_segments());
		
		for(int i=0;i<=bottom.get_nr_of_segments();i++)
		{
			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				if(i<=k)
					p1=deck.get_control_point(i, j);
				else
					p1=deck.get_control_point(i-1, j);
					
				p2=new NurbsPoint(p1.x,p1.y,p1.z);
				
				if(i==k)
					p2.x=x;
				
				deck_points[i][j]=p2;
			}

			for(int j=0;j<bottom.get_nr_of_points();j++)
			{
				if(i<=k)
					p1=bottom.get_control_point(i, j);
				else
					p1=bottom.get_control_point(i-1, j);
					
				p2=new NurbsPoint(p1.x,p1.y,p1.z);				

				if(i==k)
					p2.x=x;
				
				bottom_points[i][j]=p2;
			}
		}

		//create new surfaces
		
		NurbsSurface temp_bottom=new NurbsSurface(bottom.get_nr_of_segments()+1, bottom.get_nr_of_points());
		for(int i=0; i<temp_bottom.get_nr_of_segments(); i++)
		{
			for(int j=0; j<temp_bottom.get_nr_of_points();j++)
			{
				temp_bottom.set_control_point(i,j,bottom_points[i][j]);
			}
		}
		
		double temp_x=0;
		double mystep=0.01;
		double u=0.0;
		int seg=0;
		
		bottom.evaluate_always(true);
		
		for(int i=3;i<=bottom.get_nr_of_segments()-3;i++)
		{
			do
			{
				u=u+mystep;
				if(u>=1)
				{
					u=0.0;
					seg++;
				}
				p1=bottom.get_point_on_surface(seg, 0,u,0);
				p2=temp_bottom.get_point_on_surface(i,0,0,0);
				System.out.println("p1.x=" + p1.x + " bottom_points.x=" + bottom_points[i][0].x + " i=" + i);
			}while (p1.x<=p2.x);

			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				deck_points[i][j]=deck.get_point_on_surface(seg, j, u, 0);
			}

			for(int j=0;j<bottom.get_nr_of_points();j++)
			{
				bottom_points[i][j]=bottom.get_point_on_surface(seg, j, u, 0);
			}
			
		}

		create_deck(deck.get_nr_of_segments()+1,deck.get_nr_of_points(),deck_points);
		create_bottom(bottom.get_nr_of_segments()+1,bottom.get_nr_of_points(),bottom_points);
		
		repair();
		
		deck.evaluate_surface();
		bottom.evaluate_surface();
	}
	

	/**
	 * Sets the editing mode
	 *
	 * @param mode - True if we want to edit single points
	 */	 
	public void set_single_point_editing(boolean mode)
	{
		single_point_editing=mode;
	}
		
	/**
	 * Sets the number of segments of the surfaces
	 *
	 * @param n - nr of segments
	 */	 
	public void set_nr_of_segments(int n)
	{
		transformation_segments=n+8;
	}
	
	/**
	 * Sets the number of points of the surfaces
	 *
	 * @param n - nr of points
	 */	 
	public void set_nr_of_points(int n)
	{
		transformation_points=2*n+13;
	}
	

	
	/**
	 * Rotates the board around the z axis
	 * 
	 * If a control point is marked, the function rotates around this point
	 * otherwise the rotation is made around origo.
	 *
	 * @param theta - the angle in degrees
	 */	 
	public void rotate(double theta) {

		// rotate board

		double[][] m = {{ Math.cos(-theta * 3.1415 / 180.0),-Math.sin(-theta * 3.1415 / 180.0), 0.0 },
				{ Math.sin(-theta * 3.1415 / 180.0),Math.cos(-theta * 3.1415 / 180.0), 0.0 },
				{ 0.0, 0.0, 1.0 } };

		//If a control point is marked, then rotate around this point
		//else rotate around (0.0, 0.0, 0.0)
		
		double[] t=new double[3];
		
		if(is_marked)
		{
			NurbsPoint p=marked_surface.get_control_point(marked_segment, marked_point);
			
			translate(-p.x, -p.y, -p.z);
			t[0] = p.x;
			t[1] = p.y;
			t[2] = p.z;

		}
		else
		{
			t[0] = 0.0;
			t[1] = 0.0;
			t[2] = 0.0;
		}
		
		
		transform(m,t);
		

	}

	/**
	 * Rotates a single point using a rotation matrix
	 * 
	 * @param p1 - the point we want to transform
	 * @param m - the rotation matrix (3x3)
	 * @return NurbsPoint - the new position of the point
	 */	 
	public NurbsPoint rotate(NurbsPoint p1, double[][] m)
	{
		NurbsPoint value=new NurbsPoint(0.0, 0.0, 0.0);
		
		value.x=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z;
		value.y=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z;
		value.z=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z;
		
		return value;
	}

	/**
	 * Rotates the board using a rotation matrix
	 * 
	 * @param m - the rotation matrix (3x3)
	 */	 
	public void rotate(double[][] m)
	{

		//rotate board
		double[] t={0.0 , 0.0, 0.0};

		NurbsPoint p1, p2;
		int i,j;
		double tx,ty,tz;
				
		NurbsSurface tdeck=new NurbsSurface(deck.get_nr_of_segments(), deck.get_nr_of_points());
		NurbsSurface tbottom=new NurbsSurface(bottom.get_nr_of_segments(), bottom.get_nr_of_points());
		
		for(i=0;i<deck.get_nr_of_segments();i++)
		{
			for(j=0;j<deck.get_nr_of_points();j++)
			{
				p1=deck.get_control_point(i,j);
				
				//transform p2
		
				tx=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
				ty=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
				tz=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];
				
				p2=new NurbsPoint(tx,ty,tz);

				tdeck.set_control_point(i,j,p2);
			}
		}
		
		for(i=0;i<bottom.get_nr_of_segments();i++)
		{
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				p1=bottom.get_control_point(i,j);
				
				//transform p2
		
				tx=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
				ty=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
				tz=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];

				p2=new NurbsPoint(tx,ty,tz);

				tbottom.set_control_point(i,j,p2);
			}
		}

		tdeck.evaluate_surface();
		tbottom.evaluate_surface();
		
		deck=tdeck;
		bottom=tbottom;

	}



	/**
	 * Rotates the board around the x axis
	 * 
	 * @param theta - the rotation angle in degrees
	 */	 
	public void rotate_yz(double theta)
	{

		//rotate board
		
		double[][] m = {{1.0, 0.0, 0.0},
				{0.0, Math.cos(-theta*3.1415/180.0), -Math.sin(-theta*3.1415/180.0)},
		                {0.0, Math.sin(-theta*3.1415/180.0), Math.cos(-theta*3.1415/180.0)}}; 



		double[] t={0.0 , 0.0, 0.0};

		transform(m,t);
	}

	/**
	 * Rotates the board around the y axis
	 * 
	 * @param theta - the rotation angle in degrees
	 */	 
	public void rotate_xz(double theta)
	{

		//rotate board
		
		double[][] m = {{Math.cos(-theta*3.1415/180.0), 0.0, -Math.sin(-theta*3.1415/180.0)},
				{0.0, 1.0, 0.0},
		                {Math.sin(-theta*3.1415/180.0), 0.0, Math.cos(-theta*3.1415/180.0)}}; 



		double[] t={0.0 , 0.0, 0.0};
		
		transform(m,t);	
	}

	
	/**
	 * Rotates the board 180 degrees around the x axis
	 */	 
	public void flip()
	{

		//flip board
		double[][] m = {{1.0, 0.0, 0.0},
				{0.0, -1.0, 0.0},
		                {0.0, 0.0, -1.0}}; 

		double[] t={0.0 , 0.0, 0.0};

		transform(m,t);

	}

	/**
	 * Translates the board
	 * 
	 * @param dx - the x translation
	 * @param dy - the y translation
	 * @param dz - the z translation
	 */	 
	public void translate(double dx, double dy, double dz)
	{
	
		double[][] m = {{1.0, 0.0, 0.0},
		                {0.0, 1.0, 0.0},
		                {0.0, 0.0, 1.0}}; 

		double[] t={dx , dy, dz};

		NurbsPoint p1, p2;
		int i,j;
		double tx,ty,tz;
		
		
		transform(m,t);

	}
	
	
	/**
	 * Reads some points from a file "knutpunkter.txt" and fits the board to these.
	 * Use the scan interface instead.
	 */	 
	@Deprecated public void open_measurements()
	{
		int ns, np;
		NurbsPoint[][] bottom_points;
		NurbsPoint[][] deck_points;

		File file = new File ("knutpunkter.txt");

		try {
			// Create a FileReader and then wrap it with BufferedReader.

			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);

			// Read number of segments and points
			
			String segment_string=buf_reader.readLine ();
			int mysegments=Integer.parseInt(segment_string);

			String point_string=buf_reader.readLine ();
			int mypoints=Integer.parseInt(point_string);


			bottom_points=new NurbsPoint[mysegments+4][mypoints+4];


			ns=mysegments+4;
			np=mypoints+4;
			
			for(int i=2;i<ns-2;i++)
			{
				for(int j=2;j<np-2;j++)
				{
				
					String line = buf_reader.readLine ();

					int pos=line.indexOf(' ');
					String subline=line.substring(0,pos);
					double helpx=Double.parseDouble(subline);

					subline=line.substring(pos);
					subline=subline.trim();
					pos=subline.indexOf(' ');
					String subline2=subline.substring(0,pos);
					String subline3=subline.substring(pos);
					double helpy=Double.parseDouble(subline2);

					subline=subline3.trim();
					double helpz=Double.parseDouble(subline);
				
					bottom_points[i][j]=new NurbsPoint(helpx, helpy, helpz);
					
					if(j==2)
					{
						bottom_points[i][1]=new NurbsPoint(helpx, helpy, helpz);
						bottom_points[i][0]=new NurbsPoint(helpx, helpy, helpz);	
					}						
										
					if(j==np-3)
					{
						bottom_points[i][np-2]=new NurbsPoint(helpx, helpy, helpz);
						bottom_points[i][np-1]=new NurbsPoint(helpx, helpy, helpz);	
					}						
					
				}
			}

			for(int i=0;i<2;i++)
			{
				for(int j=0;j<np;j++)
				{
					bottom_points[i][j]=new NurbsPoint(bottom_points[2][j].x,bottom_points[2][j].y,bottom_points[2][j].z);
				}
			}	

			for(int i=ns-2;i<ns;i++)
			{
				for(int j=0;j<np;j++)
				{
					bottom_points[i][j]=new NurbsPoint(bottom_points[ns-3][j].x,bottom_points[ns-3][j].y,bottom_points[ns-3][j].z);
				}
			}	



			segment_string=buf_reader.readLine ();
			mysegments=Integer.parseInt(segment_string);

			point_string=buf_reader.readLine ();
			mypoints=Integer.parseInt(point_string);


			deck_points=new NurbsPoint[mysegments+4][mypoints+4];


			for(int i=2;i<ns-2;i++)
			{
				for(int j=2;j<np-2;j++)
				{
				
					String line = buf_reader.readLine ();

					int pos=line.indexOf(' ');
					String subline=line.substring(0,pos);
					double helpx=Double.parseDouble(subline);

					subline=line.substring(pos);
					subline=subline.trim();
					pos=subline.indexOf(' ');
					String subline2=subline.substring(0,pos);
					String subline3=subline.substring(pos);
					double helpy=Double.parseDouble(subline2);

					subline=subline3.trim();
					double helpz=Double.parseDouble(subline);
				
					deck_points[i][j]=new NurbsPoint(helpx, helpy, helpz);
					
					if(j==2)
					{
						deck_points[i][1]=new NurbsPoint(helpx, helpy, helpz);
						deck_points[i][0]=new NurbsPoint(helpx, helpy, helpz);	
					}						
										
					if(j==np-3)
					{
						deck_points[i][np-2]=new NurbsPoint(helpx, helpy, helpz);
						deck_points[i][np-1]=new NurbsPoint(helpx, helpy, helpz);	
					}						
					
				}
			}

			for(int i=0;i<2;i++)
			{
				for(int j=0;j<np;j++)
				{
					deck_points[i][j]=new NurbsPoint(deck_points[2][j].x,deck_points[2][j].y,deck_points[2][j].z);
				}
			}	

			for(int i=ns-2;i<ns;i++)
			{
				for(int j=0;j<np;j++)
				{
					deck_points[i][j]=new NurbsPoint(deck_points[ns-3][j].x,deck_points[ns-3][j].y,deck_points[ns-3][j].z);
				}
			}	

			buf_reader.close ();
			
			create_deck(ns,np,deck_points);
			create_bottom(ns,np,bottom_points);
		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}
	}


	/**
	 * Reads some points from a file "bottom.txt" and fits the bottom surface to these.
	 * Use the scan interface instead.
	 */	 
	@Deprecated public void open_bottom_measurements()
	{
		int ns=17;
		int np=15;
		NurbsPoint[][] bottom_points;
		NurbsPoint[][] deck_points;

		File file = new File ("bottom.txt");

		try {
		
		
			// Create a FileReader and then wrap it with BufferedReader.

			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);

			// Read number of segments and points
			bottom_points=new NurbsPoint[ns][np];

			
			for(int i=2;i<ns-2;i++)
			{
			System.out.println("Reading segment: " + i);
				for(int j=4;j<(np+1)/2;j++)
				{
			System.out.println("Reading point: " + j);				

					String line = buf_reader.readLine ();

					int pos=line.indexOf(' ');
					String subline=line.substring(0,pos);
					double helpx=Double.parseDouble(subline);
					System.out.println("Read x");
					subline=line.substring(pos);
					subline=subline.trim();
					pos=subline.indexOf(' ');
					String subline2=subline.substring(0,pos);
					String subline3=subline.substring(pos);
					double helpy=Double.parseDouble(subline2);
					System.out.println("Read y");
					subline=subline3.trim();
					pos=subline.indexOf(' ');					
					double helpz=Double.parseDouble(subline.substring(0,pos));
					System.out.println("Read z");				
					
					//transformera
					helpx=helpx-(780-300);
					helpy=278+helpy;
					helpz=169-helpz;					

					bottom_points[i][j]=new NurbsPoint(helpx, helpz, -helpy);
					bottom_points[i][np-j-1]=new NurbsPoint(helpx, helpz, helpy);					
				}
			}
			System.out.println("Fixing rail");
			for(int i=2;i<ns-2;i++)
			{
				bottom_points[i][0]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,bottom_points[i][4].z);
				bottom_points[i][1]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,bottom_points[i][4].z);				
				bottom_points[i][2]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,bottom_points[i][4].z);
				bottom_points[i][3]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,bottom_points[i][4].z);
				bottom_points[i][np-4]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,-bottom_points[i][4].z);
				bottom_points[i][np-3]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,-bottom_points[i][4].z);
				bottom_points[i][np-2]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,-bottom_points[i][4].z);
				bottom_points[i][np-1]=new NurbsPoint(bottom_points[i][4].x,bottom_points[i][4].y,-bottom_points[i][4].z);
			}
			
			System.out.println("Fixing tail");
			for(int i=0;i<2;i++)
			{
				for(int j=0;j<np;j++)
				{
					bottom_points[i][j]=new NurbsPoint(bottom_points[2][j].x,bottom_points[2][j].y,bottom_points[2][j].z);
				}
			}	
			System.out.println("Fixing nose");
			for(int i=ns-2;i<ns;i++)
			{
				for(int j=0;j<np;j++)
				{
					bottom_points[i][j]=new NurbsPoint(bottom_points[ns-3][j].x,bottom_points[ns-3][j].y,bottom_points[ns-3][j].z);
				}
			}	

			buf_reader.close ();
			System.out.println("Creating bottom");				
			
			create_bottom(ns,np,bottom_points);
			System.out.println("Finished");				

		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}

		
		
	}

	/**
	 * Reads some points from a file "deck.txt" and fits the deck surface to these.
	 * Use the scan interface instead.
	 */	 
	@Deprecated public void open_deck_measurements()
	{
		int ns=17;
		int np=15;
		NurbsPoint[][] bottom_points;
		NurbsPoint[][] deck_points;

		File file = new File ("deck.txt");

		try {
		
		
			// Create a FileReader and then wrap it with BufferedReader.

			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);

			// Read number of segments and points
			bottom_points=new NurbsPoint[ns][np];

			
			for(int i=2;i<ns-2;i++)
			{
			System.out.println("Reading segment: " + i);
				for(int j=4;j<(np+1)/2;j++)
				{
			System.out.println("Reading point: " + j);				

					String line = buf_reader.readLine ();

					int pos=line.indexOf(' ');
					String subline=line.substring(0,pos);
					double helpx=Double.parseDouble(subline);
					System.out.println("Read x");
					subline=line.substring(pos);
					subline=subline.trim();
					pos=subline.indexOf(' ');
					String subline2=subline.substring(0,pos);
					String subline3=subline.substring(pos);
					double helpy=Double.parseDouble(subline2);
					System.out.println("Read y");
					subline=subline3.trim();
					pos=subline.indexOf(' ');					
					double helpz=Double.parseDouble(subline.substring(0,pos));
					System.out.println("Read z");				
					
					//transformera
					helpx=helpx-(780-300);
					helpy=278+helpy;
					helpz=-169+helpz;

					bottom_points[i][j]=new NurbsPoint(helpx, helpz, -helpy);
					bottom_points[i][np-j-1]=new NurbsPoint(helpx, helpz, helpy);					
				}
			}
			System.out.println("Fixing rail");

			NurbsPoint p;
			
			for(int i=2;i<ns-2;i++)
			{
				p=bottom.get_control_point(i,0);
				
				bottom_points[i][0]=new NurbsPoint(p.x, p.y, p.z);
				bottom_points[i][1]=new NurbsPoint(p.x, p.y, p.z);				
				bottom_points[i][2]=new NurbsPoint(p.x, p.y, p.z);
				bottom_points[i][3]=new NurbsPoint(bottom_points[i][4].x,(bottom_points[i][4].y+p.y)/2,(bottom_points[i][4].z+p.z)/2);
				bottom_points[i][np-4]=new NurbsPoint(bottom_points[i][4].x,(bottom_points[i][4].y+p.y)/2,-(bottom_points[i][4].z+p.z)/2);
				bottom_points[i][np-3]=new NurbsPoint(p.x, p.y, -p.z);				
				bottom_points[i][np-2]=new NurbsPoint(p.x, p.y, -p.z);
				bottom_points[i][np-1]=new NurbsPoint(p.x, p.y, -p.z);

			}
			
			System.out.println("Fixing tail");
			for(int i=0;i<2;i++)
			{
				for(int j=0;j<np;j++)
				{
					bottom_points[i][j]=new NurbsPoint(bottom_points[2][j].x,bottom_points[2][j].y,bottom_points[2][j].z);
				}
			}	
			System.out.println("Fixing nose");
			for(int i=ns-2;i<ns;i++)
			{
				for(int j=0;j<np;j++)
				{
					bottom_points[i][j]=new NurbsPoint(bottom_points[ns-3][j].x,bottom_points[ns-3][j].y,bottom_points[ns-3][j].z);
				}
			}	

			buf_reader.close ();
			System.out.println("Creating bottom");				
			
			create_deck(ns,np,bottom_points);
			System.out.println("Finished");				

		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}

		
		
	}



	/**
	 * Scales the length of the board
	 *
	 * @param factor - the scale factor
	 */	 	
	public void scale_length(double factor)
	{
	
		NurbsPoint p;
		
		for(int i=0;i<deck.get_nr_of_segments();i++)
		{
			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				p=deck.get_control_point(i, j);
				
				p.x=p.x*factor;
			}
		}	

		for(int i=0;i<bottom.get_nr_of_segments();i++)
		{
			for(int j=0;j<bottom.get_nr_of_points();j++)
			{
				p=bottom.get_control_point(i, j);
				
				p.x=p.x*factor;
			}
		}
		deck.evaluate_surface();
		bottom.evaluate_surface();	
	}

	/**
	 * Scales the rocker of the board
	 *
	 * @param factor - the scale factor
	 */	 	
	public void scale_rocker(double factor)
	{
	
		NurbsPoint p;
		
		for(int i=0;i<deck.get_nr_of_segments();i++)
		{
			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				p=deck.get_control_point(i, j);
				p.y=p.y*factor;
				
			}
		}	

		for(int i=0;i<bottom.get_nr_of_segments();i++)
		{
			for(int j=0;j<bottom.get_nr_of_points();j++)
			{
				p=bottom.get_control_point(i, j);
				p.y=p.y*factor;
				
			}
		}	
		
		scale_thickness(1.0/factor);
	}

	/**
	 * Scales the width of the board
	 *
	 * @param factor - the scale factor
	 */	 	
	public void scale_width(double factor)
	{
	
		NurbsPoint p;
		
		for(int i=0;i<deck.get_nr_of_segments();i++)
		{
			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				p=deck.get_control_point(i, j);
				
				p.z=p.z*factor;
				
			}
		}	

		for(int i=0;i<bottom.get_nr_of_segments();i++)
		{
			for(int j=0;j<bottom.get_nr_of_points();j++)
			{
				p=bottom.get_control_point(i, j);
				
				p.z=p.z*factor;
				
			}
		}	
		deck.evaluate_surface();
		bottom.evaluate_surface();	
	}

	/**
	 * Scales the thickness of the board
	 *
	 * @param factor - the scale factor
	 */	 	
	public void scale_thickness(double factor)
	{	
		NurbsPoint p,p_tu;;
		
		for(int i=0;i<deck.get_nr_of_segments();i++)
		{
			p_tu=deck.get_control_point(i,0);
			for(int j=0;j<deck.get_nr_of_points();j++)
			{
				p=deck.get_control_point(i, j);

				p.y=p_tu.y+(p.y-p_tu.y)*factor;
				
			}
		}	
		deck.evaluate_surface();
		bottom.evaluate_surface();	
	}


	/**
	 * Makes sure that the surface models are correct and that
	 * the deck and bottom surfaces are connected.
	 */	 	
	public void repair()
	{

		NurbsPoint p1, p2, p3, p4;
		int i,j;


		//fix tail
		for(i=0;i<3;i++)
		{				
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				p1=bottom.get_control_point(2,j);
				bottom.set_control_point(i,j,p1);
			}
		}

		for(i=0;i<3;i++)
		{				
			for(j=0;j<deck.get_nr_of_points();j++)
			{
				p1=deck.get_control_point(2,j);
				deck.set_control_point(i,j,p1);
			}
		}
		
		//fix nose
		for(i=bottom.get_nr_of_segments()-3;i<bottom.get_nr_of_segments();i++)
		{				
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				p1=bottom.get_control_point(bottom.get_nr_of_segments()-1,j);
				bottom.set_control_point(i,j,p1);
			}
		}

		for(i=deck.get_nr_of_segments()-3;i<deck.get_nr_of_segments();i++)
		{				
			for(j=0;j<deck.get_nr_of_points();j++)
			{
				p1=deck.get_control_point(bottom.get_nr_of_segments()-1,j);
				deck.set_control_point(i,j,p1);
			}
		}


		//avoid passing board center

		for(i=0;i<bottom.get_nr_of_segments();i++)
		{				
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
		
				if(j<bottom.get_nr_of_points()/2)
				{
					p1=bottom.get_control_point(i,j);
					if(p1.z>0)
					{
						p1.z=0;
						bottom.set_control_point(i,j,p1);
					}
				}
				
				if(j>bottom.get_nr_of_points()/2)
				{
					p1=bottom.get_control_point(i,j);
					if(p1.z<0)
					{
						p1.z=0;
						bottom.set_control_point(i,j,p1);
					}
				}
			}

			for(j=0;j<deck.get_nr_of_points();j++)
			{
		
				if(j<deck.get_nr_of_points()/2)
				{
					p1=deck.get_control_point(i,j);
					if(p1.z<0)
					{
						p1.z=0;
						deck.set_control_point(i,j,p1);
					}
				}
				
				if(j>deck.get_nr_of_points()/2)
				{
					p1=deck.get_control_point(i,j);
					if(p1.z>0)
					{
						p1.z=0;
						deck.set_control_point(i,j,p1);
					}
				}
			}
	
		
		}


		//fix vee
		for(i=0;i<bottom.get_nr_of_segments();i++)
		{				
			for(j=0;j<bottom.get_nr_of_points();j++)
			{
				p1=bottom.get_control_point(i,j);
				if(Math.abs(p1.z)<2)
				{
					p1.z=0;
				}
				bottom.set_control_point(i,j,p1);
			}
		}

	
		//fix for tail+1 and noise-1
		for(j=0; j<bottom.get_nr_of_points();j++)
		{
			p1=bottom.get_control_point(3,j);
			p2=bottom.get_control_point(2,j);
				
			if(p1.y>p2.y-0.1)
			{
				p1.y=p2.y-0.1;
				bottom.set_control_point(3,j,p1);
			}

			if(p1.x<p2.x+0.1)
			{
				p1.x=p2.x+0.1;
				bottom.set_control_point(3,j,p1);
			}
			
			p1=bottom.get_control_point(bottom.get_nr_of_segments()-4,j);
			p2=bottom.get_control_point(bottom.get_nr_of_segments()-3,j);
				
			if(p1.y>p2.y-0.1)
			{
				p1.y=p2.y-0.1;
				bottom.set_control_point(bottom.get_nr_of_segments()-4,j,p1);
			}

			if(p1.x>p2.x-0.1)
			{
				p1.x=p2.x-0.1;
				bottom.set_control_point(bottom.get_nr_of_segments()-4,j,p1);
			}

		}
	
		for(j=0; j<deck.get_nr_of_points();j++)
		{
			p1=deck.get_control_point(3,j);
			p2=deck.get_control_point(2,j);
				
			if(p1.x<p2.x+0.1)
			{
				p1.x=p2.x+0.1;
				deck.set_control_point(3,j,p1);
			}
			
			p1=deck.get_control_point(deck.get_nr_of_segments()-4,j);
			p2=deck.get_control_point(deck.get_nr_of_segments()-3,j);
				
			if(p1.x>p2.x-0.1)
			{
				p1.x=p2.x-0.1;
				deck.set_control_point(deck.get_nr_of_segments()-4,j,p1);
			}

		}
	
		//fix tucked under
		
		for(i=0;i<bottom.get_nr_of_segments();i++)
		{
		
			p1=bottom.get_control_point(i,0);
						
			bottom.set_control_point(i,0,p1);
			bottom.set_control_point(i,1,p1);
			bottom.set_control_point(i,2,p1);
			bottom.set_control_point(i,3,p1);
			bottom.set_control_point(i,4,p1);
			deck.set_control_point(i,deck.get_nr_of_points()-1,p1);
			deck.set_control_point(i,deck.get_nr_of_points()-2,p1);
			deck.set_control_point(i,deck.get_nr_of_points()-3,p1);

			p2=new NurbsPoint(p1.x, p1.y, -p1.z);
			bottom.set_control_point(i,bottom.get_nr_of_points()-5,p2);
			bottom.set_control_point(i,bottom.get_nr_of_points()-4,p2);
			bottom.set_control_point(i,bottom.get_nr_of_points()-3,p2);
			bottom.set_control_point(i,bottom.get_nr_of_points()-2,p2);
			bottom.set_control_point(i,bottom.get_nr_of_points()-1,p2);
			deck.set_control_point(i,0,p2);
			deck.set_control_point(i,1,p2);
			deck.set_control_point(i,2,p2);


			p3=deck.get_control_point(i,3);

			p3.x=p2.x;

			if(Math.abs(p3.y)<Math.abs(p2.y))
				p3.y=p2.y;

			if(Math.abs(p3.z)<Math.abs(p2.z))
				p3.z=p2.z;

			deck.set_control_point(i,3,p3);
			p2=new NurbsPoint(p3.x, p3.y, -p3.z);
			deck.set_control_point(i,deck.get_nr_of_points()-4,p2);


			p4=deck.get_control_point(i,4);

			p4.x=p2.x;

			if(Math.abs(p4.y)<Math.abs(p3.y))
				p4.y=p3.y;

			if(Math.abs(p4.z)<Math.abs(p1.z))
				p4.z=p3.z;

			deck.set_control_point(i,4,p4);
			p2=new NurbsPoint(p4.x, p4.y, -p4.z);
			deck.set_control_point(i,deck.get_nr_of_points()-5,p2);



		}
		
		deck.evaluate_surface();
		bottom.evaluate_surface();

	}

	
	/**
	 * Sets the cartesian transformation operator which is used describe the differences
	 * in position and orientation of the 3D model and the bezier board
	 *
	 * @param origin - the origin of the 3D model
	 * @param x_axis - the direction of the x axis
	 * @param y_axis - the direction of the y axis
	 */	 	
	public void set_cartesian_transformation_operator(NurbsPoint origin, NurbsPoint x_axis, NurbsPoint y_axis)
	{
		local_origin=new NurbsPoint(origin.x, origin.y, origin.z);
		x_axis_direction=new NurbsPoint(x_axis.x, x_axis.y, x_axis.z);
		y_axis_direction=new NurbsPoint(y_axis.x, y_axis.y, y_axis.z);
	}

	/**
	 * Gets the cartesian transformation operator which is used describe the differences
	 * in position and orientation of the 3D model and the bezier board
	 *
	 * @param origin - the origin of the 3D model
	 * @param x_axis - the direction of the x axis
	 * @param y_axis - the direction of the y axis
	 */	 	
	public void get_cartesian_transformation_operator(NurbsPoint origin, NurbsPoint x_axis, NurbsPoint y_axis)
	{
		origin=new NurbsPoint(local_origin.x, local_origin.y, local_origin.z);
		x_axis=new NurbsPoint(x_axis_direction.x, x_axis_direction.y, x_axis_direction.z);
		y_axis=new NurbsPoint(y_axis_direction.x, y_axis_direction.y, y_axis_direction.z);
	}
	
	/**
	 * Gets the deck surface
	 *
	 * @return NurbsSurface - deck surface
	 */	 	
	public NurbsSurface getDeck()
	{
		return deck;
	}
	
	/**
	 * Sets the deck surface
	 *
	 * @param s - deck surface
	 */	 	
	public void setDeck(NurbsSurface s)
	{
		deck=s;
	}

	/**
	 * Gets the bottom surface
	 *
	 * @return NurbsSurface - bottom surface
	 */	 	
	public NurbsSurface getBottom()
	{
		return bottom;
	}
	
	/**
	 * Sets the bottom surface
	 *
	 * @param s - bottom surface
	 */	 	
	public void setBottom(NurbsSurface s)
	{
		bottom=s;
	}
	
    
    public void triangulate()
    {
        triangle_array=new Triangle[100*100*2];
        double s, t;
        int count=0;
        
        double step_s=(deck.getMaxS()-deck.getMinS())/50.0;
        double step_t=(deck.getMaxT()-deck.getMinT())/50.0;
        
        for(s=deck.getMinS();s<deck.getMaxS()-step_s;s=s+step_s)
        {
            for(t=deck.getMinT();t<deck.getMaxT()-step_t;t=t+step_t)
            {
                triangle_array[count]=new Triangle();
                triangle_array[count].vertices[0]=deck.getPoint(s,t);
                triangle_array[count].vertices[1]=deck.getPoint(s,t+step_t);
                triangle_array[count].vertices[2]=deck.getPoint(s+step_s,t);
                triangle_array[count].normal=deck.getNormal(s,t);
                count++;
                
                triangle_array[count]=new Triangle();
                triangle_array[count].vertices[0]=deck.getPoint(s,t+step_t);
                triangle_array[count].vertices[1]=deck.getPoint(s+step_s,t+step_t);
                triangle_array[count].vertices[2]=deck.getPoint(s+step_s,t);
                triangle_array[count].normal=deck.getNormal(s,t);
                count++;
            }
        }

        for(s=bottom.getMinS();s<bottom.getMaxS()-step_s;s=s+step_s)
        {
            for(t=bottom.getMinT();t<bottom.getMaxT()-step_t;t=t+step_t)
            {
                triangle_array[count]=new Triangle();
                triangle_array[count].vertices[0]=bottom.getPoint(s,t);
                triangle_array[count].vertices[1]=bottom.getPoint(s,t+step_t);
                triangle_array[count].vertices[2]=bottom.getPoint(s+step_s,t);
                triangle_array[count].normal=bottom.getNormal(s,t);
                count++;
                
                triangle_array[count]=new Triangle();
                triangle_array[count].vertices[0]=bottom.getPoint(s,t+step_t);
                triangle_array[count].vertices[1]=bottom.getPoint(s+step_s,t+step_t);
                triangle_array[count].vertices[2]=bottom.getPoint(s+step_s,t);
                triangle_array[count].normal=bottom.getNormal(s,t);
                count++;
            }
        }

        triangle_count=count;
    }
	
	//*********************************************************************
	// Inherited function from abstract board - not sure if they make sense
	//*********************************************************************
	
	/**
	 * Gets the length of the board
	 *
	 * Note: The unit of the 3D board is mm while the bezier board returns cm
	 *
	 * @return double - the length of the board in mm
	 */	 
	public double getLength()
	{
		return get_length();
	}

	/**
	 * Gets the deck value at a given position
	 *
	 * Not implemented, returns 0.0!
	 * The differences in coordinate system and units between the bezier board 
	 * and the 3D model make this function very confusing
	 *
	 * @param x - the x value of the 3D model
	 * @param y - the z value of the 3D model
	 * @return double - 0.0
	 */	 
	public double getDeckAt(double x, double y)
	{
		//TODO deck.get_point_on_surface()
		return 0.0;
	}

	/**
	 * Gets the bottom value at a given position
	 *
	 * Not implemented, returns 0.0!
	 * The differences in coordinate system and units between the bezier board 
	 * and the 3D model make this function very confusing
	 *
	 * @param x - the x value of the 3D model
	 * @param y - the z value of the 3D model
	 * @return double - 0.0
	 */	 
	public double getBottomAt(double x, double y)
	{
		//TODO bottom.get_point_on_surface()
		return 0.0;
	}

	/**
	 * Gets the width at a given position
	 *
	 * Not implemented, returns 0.0!
	 *
	 * @param x - the x value of the 3D model
	 * @return double - 0.0
	 */	 
	public double getWidthAt(double x)
	{
		//TODO bottom.get_point_on_surface()
		return 0.0;
	}

	/**
	 * Gets the deck normal at a given position
	 *
	 * Not implemented, returns (0,0,0)
	 *
	 * @param x - the x value of the 3D model
	 * @param y - the z value of the 3D model
	 * @return Vector3d - (0,0,0)
	 */	 
	public Vector3d getDeckNormalAt(double x, double y)
	{
		//TODO bottom.get_point_on_surface() and calc
		return new Vector3d(0,0,0);
	}
	
	/**
	 * Gets the bottom normal at a given position
	 *
	 * Not implemented, returns (0,0,0)
	 *
	 * @param x - the x value of the 3D model
	 * @param y - the z value of the 3D model
	 * @return Vector3d - (0,0,0)
	 */	 
	public Vector3d getBottomNormalAt(double x, double y)
	{
		//TODO bottom.get_point_on_surface() and calc
		return new Vector3d(0,0,0);		
	}
	
	
	/**
	 * Clone the board
	 *
	 * @return Object a cloned board
	 */	 	
	public Object clone()
	{
		int i;
		
		NurbsBoard mbrd=new NurbsBoard();
	
		mbrd.deck=(NurbsSurface)deck.clone();
		mbrd.bottom=(NurbsSurface)bottom.clone();
		
		if(marked_surface==deck)
			mbrd.marked_surface=mbrd.deck;
		else if(marked_surface==bottom)
			mbrd.marked_surface=mbrd.bottom;
		else
			mbrd.marked_surface=null;
		
		
		mbrd.marked_segment=marked_segment;
		mbrd.marked_point=marked_point;
		mbrd.is_marked=is_marked;
		mbrd.is_new=is_new;
	
		mbrd.name=new String(name);
		
		mbrd.deck_points=new String[deck.get_nr_of_points()];
		for(i=0;i<deck.get_nr_of_points();i++)
		{
			mbrd.deck_points[i]=new String(deck_points[i]);
//			mbrd.deck_points[i]=new String("");

		}
		
		mbrd.bottom_points=new String[deck.get_nr_of_points()];
		for(i=0;i<bottom.get_nr_of_points();i++)
		{
			mbrd.bottom_points[i]=new String(bottom_points[i]);
//			mbrd.bottom_points[i]=new String("");
		}
	
		mbrd.step=step;
		mbrd.tail=tail;
		mbrd.alfa=alfa;
		mbrd.beta=beta;
		mbrd.transformation_segments=transformation_segments;
		mbrd.transformation_points=transformation_points;
	
		mbrd.single_point_editing=single_point_editing;
	
		mbrd.vertises=vertises.clone();
	
		mbrd.x_axis_direction=(NurbsPoint)x_axis_direction.clone();
		mbrd.y_axis_direction=(NurbsPoint)y_axis_direction.clone();
		mbrd.local_origin=(NurbsPoint)local_origin.clone();
	
		return mbrd;
	}
	

}

