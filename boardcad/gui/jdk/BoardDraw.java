package boardcad.gui.jdk;

import cadcore.*;

import java.awt.*;
import java.awt.geom.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.io.*;
import java.util.*;

/**
 * Draws the 3D model of the board
 */
 
public class BoardDraw
{
	public NurbsSurface deck;
	public NurbsSurface bottom;
	public double step;

	public NurbsSurface marked_surface;
	public int marked_segment;
	public int marked_point;
	public boolean is_marked;
	private boolean is_new;
	public NurbsPoint active;

	public NurbsPoint[] bottom_cut;
	public boolean[] bottom_collision;
	public NurbsPoint[] deck_cut;
	public boolean[] deck_collision;
	public int nr_of_cuts_bottom;
	public int nr_of_cuts_deck;

	private NurbsPoint help_points[];
	private int nr_of_help_points;		

	public boolean single_point_editing;
    
    public Triangle[] triangle_array;
    public int triangle_count;


	public BoardDraw()
	{
		step=0.2;
	}

	/**
	 * Draw the side view of the board
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param scale
	 * @param offset_x
	 * @param offset_y
	 * @param draw_vee
	 * @param draw_bottom_cut
	 * @param draw_blank
	 * @param color
	 */
	public void draw_rocker(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_vee, boolean draw_bottom_cut, boolean draw_deck_cut, boolean draw_blank, java.awt.Color color)
	{
		NurbsPoint value;
		int segment, point;
		int a,b,c,d;
		double u, v;

		g.setStroke(new BasicStroke((float)1.1));
		
		g.setColor(Color.black);

		g.drawString("y", offset_x-10, offset_y-20);
		g.drawString("x", offset_x+20, offset_y+10);
		g.drawLine(offset_x, offset_y, offset_x+20, offset_y);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y-3);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y+3);
		g.drawLine(offset_x, offset_y, offset_x, offset_y-20);
		g.drawLine(offset_x, offset_y-20, offset_x+3, offset_y-15);
		g.drawLine(offset_x, offset_y-20, offset_x-3, offset_y-15);


		NurbsPoint p1, p2, p3;
		Vector3f normal;
		Polygon pol=new Polygon();
        NurbsPoint normal2;
		
		if(!draw_blank)
		{
            if(triangle_array!=null)
            {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                for(int i=0;i<triangle_count;i++)
                {
                    pol.reset();
                    
                    p1=triangle_array[i].vertices[0];
                    pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(-p1.y*scale)+offset_y );
                    
                    normal2=triangle_array[i].normal;
                    
                    p2=triangle_array[i].vertices[1];
                    pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(-p2.y*scale)+offset_y );
                    
                    p3=triangle_array[i].vertices[2];;
                    pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(-p3.y*scale)+offset_y );
                    
                    if(normal2.z>0)
                    {
                        g.setColor(new Color((int)(250*Math.abs(normal2.z)), (int)(250*Math.abs(normal2.z)), (int)(250*Math.abs(normal2.z))));
                        g.fillPolygon(pol);
                    }
                    
                }
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                
            }
            else
            {
                
            
			for(int i=1;i<deck.get_nr_of_segments()-2;i++)
			{
				for(u=0.0;u<1.0;u=u+step)
				{
					for(int j=1;j<deck.get_nr_of_points()-2;j++)
					{
						for(v=0.0;v<1.0;v=v+step)
						{
							pol.reset();
							
							p1=deck.get_point_on_surface(i,j,u,v);
							pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(-p1.y*scale)+offset_y ); 
							
							normal=calculate_normal(deck, i, j, u+step/2, v+step/2);

							p2=deck.get_point_on_surface(i,j,u,v+step);
							pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(-p2.y*scale)+offset_y ); 

							p3=deck.get_point_on_surface(i,j,u+step,v);
							pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(-p3.y*scale)+offset_y ); 

							g.setColor(new Color((int)(250*Math.abs(normal.z)), (int)(250*Math.abs(normal.z)), (int)(250*Math.abs(normal.z))));
							g.fillPolygon(pol);
							
							
							pol.reset();
							
							p1=deck.get_point_on_surface(i,j,u,v+step);
	//						normal=calculate_normal(deck, i, j, u, v+step);
							pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(-p1.y*scale)+offset_y ); 
													
							p2=deck.get_point_on_surface(i,j,u+step,v+step);
							pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(-p2.y*scale)+offset_y ); 

							p3=deck.get_point_on_surface(i,j,u+step,v);
							pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(-p3.y*scale)+offset_y ); 

							g.setColor(new Color((int)(250*Math.abs(normal.z)), (int)(250*Math.abs(normal.z)), (int)(250*Math.abs(normal.z))));
							g.fillPolygon(pol);
						}
					}
				}
			}
            }

		}


		if(single_point_editing)
		{
			g.setColor(color);
//			g.setColor(Color.black);
			

			//draw bottom			
			for(point=1;point<bottom.get_nr_of_points()-2;point++)
			{
				value=bottom.get_point_on_surface(1, point,0,0);
				a=(int)(value.x*scale);
				b=(int)(-value.y*scale);
				for(segment=1;segment<bottom.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=bottom.get_point_on_surface(segment, point,u,0);
						c=(int)(value.x*scale);
						d=(int)(-value.y*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
					value=bottom.get_control_point(segment, point);
					g.drawOval((int)(value.x*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
				}
			}	

			for(segment=1;segment<bottom.get_nr_of_segments()-2;segment++)
			{
				value=bottom.get_point_on_surface(segment, 1,0,0);
				a=(int)(value.x*scale);
				b=(int)(-value.y*scale);
				for(point=1;point<bottom.get_nr_of_points()-2;point++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=bottom.get_point_on_surface(segment, point,0,u);
						c=(int)(value.x*scale);
						d=(int)(-value.y*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}	

			//draw deck			
			for(point=1;point<deck.get_nr_of_points()-2;point++)
			{
				value=deck.get_point_on_surface(1, point,0,0);
				a=(int)(value.x*scale);
				b=(int)(-value.y*scale);
				for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, point,u,0);
						c=(int)(value.x*scale);
						d=(int)(-value.y*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
					value=deck.get_control_point(segment, point);
					g.drawOval((int)(value.x*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
				}
			}	

			for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
			{
				value=deck.get_point_on_surface(segment, 1,0,0);
				a=(int)(value.x*scale);
				b=(int)(-value.y*scale);
				for(point=1;point<deck.get_nr_of_points()-2;point++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, point,0,u);
						c=(int)(value.x*scale);
						d=(int)(-value.y*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}	

		}
		else
		{
			value=deck.get_control_point(0, deck.get_nr_of_points()/2);
			a=(int)(value.x*scale);
			b=(int)(-value.y*scale);
			for(segment=2;segment<4;segment++)
			{
				for(u=0;u<=1;u=u+step)
				{
					value=deck.get_point_on_surface(segment, deck.get_nr_of_points()/2,u,0);
					c=(int)(value.x*scale);
					d=(int)(-value.y*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=deck.get_control_point(segment, deck.get_nr_of_points()/2);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
			}

			g.setColor(color);
	//		g.setColor(Color.black);

			for(segment=4;segment<deck.get_nr_of_segments()-2;segment++)
			{
				for(u=0;u<=1;u=u+step)
				{
					value=deck.get_point_on_surface(segment, deck.get_nr_of_points()/2,u,0);
					c=(int)(value.x*scale);
					d=(int)(-value.y*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=deck.get_control_point(segment, deck.get_nr_of_points()/2);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
			}
	

			value=bottom.get_control_point(0, bottom.get_nr_of_points()/2);
			a=(int)(value.x*scale);
			b=(int)(-value.y*scale);
			for(segment=2;segment<3;segment++)
			{
				for(u=0;u<=1;u=u+step)
				{
					value=bottom.get_point_on_surface(segment, bottom.get_nr_of_points()/2,u,0);
					c=(int)(value.x*scale);
					d=(int)(-value.y*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=bottom.get_control_point(segment, bottom.get_nr_of_points()/2);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
			}

			g.setColor(color);
	//		g.setColor(Color.black);

			for(segment=3;segment<bottom.get_nr_of_segments()-2;segment++)
			{
				for(u=0;u<=1;u=u+step)
				{
					value=bottom.get_point_on_surface(segment, bottom.get_nr_of_points()/2,u,0);
					c=(int)(value.x*scale);
					d=(int)(-value.y*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=bottom.get_control_point(segment, bottom.get_nr_of_points()/2);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
			}

			if(draw_vee)
			{
				value=deck.get_control_point(0, 1);
				a=(int)(value.x*scale);
				b=(int)(-value.y*scale);
				for(segment=2;segment<deck.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, 1,u,0);
						c=(int)(value.x*scale);
						d=(int)(-value.y*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
				value=deck.get_control_point(0, deck.get_nr_of_points()-3);
				a=(int)(value.x*scale);
				b=(int)(-value.y*scale);
				for(segment=2;segment<deck.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, deck.get_nr_of_points()-3,u,1);
						c=(int)(value.x*scale);
						d=(int)(-value.y*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}
		}

		if(is_marked)
		{
			value=active;//marked_surface.get_control_point(marked_segment, marked_point);			
			a=(int)(value.x*scale);
			b=(int)(-value.y*scale);
			g.setColor(Color.green);
			g.drawOval(a+offset_x-2,b+offset_y-2,5,5);
		}
		
		
		//draw deck cut
		if (nr_of_cuts_deck>0 && draw_deck_cut)
		{
		
			g.setColor(Color.green);
			value=deck_cut[0];
			a=(int)(value.x*scale);
			b=(int)(-value.y*scale);

			for(int i=1;i<nr_of_cuts_deck;i++)
			{
				if(deck_collision[i])
					g.setColor(Color.red);
				else
					g.setColor(Color.green);
				
				value=deck_cut[i];
				c=(int)(value.x*scale);
				d=(int)(-value.y*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
		}

		//draw bottom cut
		if (nr_of_cuts_bottom>0  && draw_bottom_cut)
		{
		
			g.setColor(Color.green);
			value=bottom_cut[0];
			a=(int)(value.x*scale);
			b=(int)(-value.y*scale);

			for(int i=1;i<nr_of_cuts_bottom;i++)
			{
				if(bottom_collision[i])
					g.setColor(Color.red);
				else
					g.setColor(Color.green);
				value=bottom_cut[i];
				c=(int)(value.x*scale);
				d=(int)(-value.y*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
		}
		
		//draw help points
		
		for(int i=0;i<nr_of_help_points;i++)
		{
			g.setColor(Color.red);
			g.drawLine((int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].y*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].y*scale)+offset_y+2);
			g.drawLine((int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].y*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].y*scale)+offset_y+2);
		}

						
				
	
	}

	/**
	 * Draw tail view of the board
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param scale
	 * @param offset_x
	 * @param offset_y
	 */
	public void draw_edge(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y)
	{
		NurbsPoint value;
		int point;
		int a,b,c,d;
		double v;

		g.setStroke(new BasicStroke((float)1.1));
		g.setColor(Color.black);
		g.drawString("y", offset_x-10, -20+offset_y);
		g.drawString("z", offset_x+20, +10+offset_y);
		g.drawLine(offset_x, offset_y, offset_x+20, offset_y);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y-3);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y+3);
		g.drawLine(offset_x, offset_y, offset_x, offset_y-20);
		g.drawLine(offset_x, offset_y-20, offset_x+3, offset_y-15);
		g.drawLine(offset_x, offset_y-20, offset_x-3, offset_y-15);

		value=deck.get_point_on_surface(marked_segment, 1,0,0);
		a=(int)(value.z*scale);
		b=(int)(-value.y*scale);
		for(point=1;point<deck.get_nr_of_points()-2;point++)
		{
			for(v=0;v<=1;v=v+step)
			{
				value=deck.get_point_on_surface(marked_segment, point,0,v);
				c=(int)(value.z*scale);
				d=(int)(-value.y*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
			value=deck.get_control_point(marked_segment, point);
			g.drawOval((int)(value.z*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
		}
		value=bottom.get_point_on_surface(marked_segment, 1,0,0);
		a=(int)(value.z*scale);
		b=(int)(-value.y*scale);
		for(point=1;point<bottom.get_nr_of_points()-2;point++)
		{
			for(v=0;v<=1;v=v+step)
			{
				value=bottom.get_point_on_surface(marked_segment, point,0,v);
				c=(int)(value.z*scale);
				d=(int)(-value.y*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
			value=bottom.get_control_point(marked_segment, point);
			g.drawOval((int)(value.z*scale)+offset_x-2,(int)(-value.y*scale)+offset_y-2,5,5);
		}

		if(is_marked)
		{
			value=active;//marked_surface.get_control_point(marked_segment, marked_point);			
			a=(int)(value.z*scale);
			b=(int)(-value.y*scale);
			g.setColor(Color.green);
			g.drawOval(a+offset_x-2,b+offset_y-2,5,5);
		}
		
		
		//draw help points

		for(int i=0;i<nr_of_help_points;i++)
		{
			if(Math.abs(help_points[i].x-(bottom.get_point_on_surface(marked_segment, 1,0,0)).x)<10)
			{
				g.setColor(Color.red);
				g.drawLine((int)(help_points[i].z*scale)+offset_x-2,(int)(-help_points[i].y*scale)+offset_y-2,(int)(help_points[i].z*scale)+offset_x+2,(int)(-help_points[i].y*scale)+offset_y+2);
				g.drawLine((int)(help_points[i].z*scale)+offset_x+2,(int)(-help_points[i].y*scale)+offset_y-2,(int)(help_points[i].z*scale)+offset_x-2,(int)(-help_points[i].y*scale)+offset_y+2);
			}
		}
		
		

	}

	/**
	 * Draw top view of the board
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param scale
	 * @param offset_x
	 * @param offset_y
	 * @param draw_tucked_under
	 * @param draw_bottom_cut
	 * @param draw_deck_cut
	 * @param draw_blank
	 * @param color
	 */
	public void draw_outline(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_tucked_under, boolean draw_bottom_cut, boolean draw_deck_cut, boolean draw_blank, java.awt.Color color)
	{
		NurbsPoint value;
		int segment, point;
		int a,b,c,d;
		double u,v;
		int i,j;

		g.setStroke(new BasicStroke((float)1.1));
		g.setColor(Color.black);
		g.drawString("z", offset_x-10, offset_y+20);
		g.drawString("x", offset_x+20, offset_y+10);
		g.drawLine(offset_x, offset_y, offset_x+20, offset_y);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y-3);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y+3);
		g.drawLine(offset_x, offset_y, offset_x, offset_y+20);
		g.drawLine(offset_x, offset_y+20, offset_x+3, offset_y+15);
		g.drawLine(offset_x, offset_y+20, offset_x-3, offset_y+15);

		NurbsPoint p1, p2, p3, p4;
		Vector3f normal;
		Polygon pol=new Polygon();
		NurbsPoint normal2;
        
		if(!draw_blank)
		{
            
            if(triangle_array!=null)
            {
                for(i=0;i<triangle_count;i++)
                {
                    pol.reset();
                    
                    p1=triangle_array[i].vertices[0];
                    pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(p1.z*scale)+offset_y );
                    
                    normal2=triangle_array[i].normal;
                    
                    p2=triangle_array[i].vertices[1];
                    pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(p2.z*scale)+offset_y );
                    
                    p3=triangle_array[i].vertices[2];;
                    pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(p3.z*scale)+offset_y );
                    
                    if(normal2.y>0)
                    {
                        g.setColor(new Color((int)(250*Math.abs(normal2.y)), (int)(250*Math.abs(normal2.y)), (int)(250*Math.abs(normal2.y))));
                        g.fillPolygon(pol);
                    }
                    
                }
                
            }
            else
            {
                
			for(i=1;i<deck.get_nr_of_segments()-2;i++)
			{
				for(u=0.0;u<1.0;u=u+step)
				{
					for(j=1;j<deck.get_nr_of_points()-2;j++)
					{
						for(v=0.0;v<1.0;v=v+step)
						{
							pol.reset();
							
							p1=deck.get_point_on_surface(i,j,u,v);
							pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(p1.z*scale)+offset_y ); 
							
							normal=calculate_normal(deck, i, j, u, v);

							p2=deck.get_point_on_surface(i,j,u,v+step);
							pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(p2.z*scale)+offset_y ); 

							p3=deck.get_point_on_surface(i,j,u+step,v+step);
							pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(p3.z*scale)+offset_y ); 

							p4=deck.get_point_on_surface(i,j,u+step,v);
							pol.addPoint( (int)(p4.x*scale)+offset_x, (int)(p4.z*scale)+offset_y ); 

							if(normal.y>0)
							{
								g.setColor(new Color((int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y))));
								g.fillPolygon(pol);
							}
							
						}
					}
				}
			}
            
			for(i=1;i<bottom.get_nr_of_segments()-2;i++)
			{
				for(u=0.0;u<1.0;u=u+step)
				{
					for(j=1;j<bottom.get_nr_of_points()-2;j++)
					{
						for(v=0.0;v<1.0;v=v+step)
						{
							pol.reset();
					
							p1=bottom.get_point_on_surface(i,j,u,v);
							pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(p1.z*scale)+offset_y ); 
						
							normal=calculate_normal(bottom, i, j, u, v);

							p2=bottom.get_point_on_surface(i,j,u,v+step);
							pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(p2.z*scale)+offset_y ); 

							p3=bottom.get_point_on_surface(i,j,u+step,v+step);
							pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(p3.z*scale)+offset_y ); 

							p4=bottom.get_point_on_surface(i,j,u+step,v);
							pol.addPoint( (int)(p4.x*scale)+offset_x, (int)(p4.z*scale)+offset_y ); 

							if(normal.y>0)
							{
								g.setColor(new Color((int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y))));
								g.fillPolygon(pol);
							}
							
						}
					}
				}
			}
            }

		}



		if(single_point_editing)
		{
			g.setColor(color);
//			g.setColor(new Color(0,0,0));
//			g.setColor(Color.black);
			
			//draw bottom			
			for(point=1;point<bottom.get_nr_of_points()-2;point++)
			{
				value=bottom.get_point_on_surface(1, point,0,0);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(segment=1;segment<bottom.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=bottom.get_point_on_surface(segment, point,u,0);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
					value=bottom.get_control_point(segment, point);
					g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
				}
			}	

			for(segment=1;segment<bottom.get_nr_of_segments()-2;segment++)
			{
				value=bottom.get_point_on_surface(segment, 1,0,0);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(point=1;point<bottom.get_nr_of_points()-2;point++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=bottom.get_point_on_surface(segment, point,0,u);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}	

			//draw deck			
			for(point=1;point<deck.get_nr_of_points()-2;point++)
			{
				value=deck.get_point_on_surface(1, point,0,0);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, point,u,0);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
					value=deck.get_control_point(segment, point);
					g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
				}
			}	

			for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
			{
				value=deck.get_point_on_surface(segment, 1,0,0);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(point=1;point<deck.get_nr_of_points()-2;point++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, point,0,u);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}	

		}
		else 
		{
		 
			g.setColor(color);
//			g.setColor(Color.black);
//			g.setColor(new Color(0,0,0));
			j=4;
			if(deck.get_surface_type()==1)
				j=5;
	
			
			value=deck.get_point_on_surface(1, 4,0,0);
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);
			for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
			{
				for(u=0;u<=1;u=u+step)
				{
					value=deck.get_point_on_surface(segment, j,u,0);
					c=(int)(value.x*scale);
					d=(int)(value.z*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=deck.get_control_point(segment, j);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
			}
	
			//draw rest of outline
			g.setColor(color);
//			g.setColor(Color.black);
//			g.setColor(new Color(0,0,0));
				
			j=deck.get_nr_of_points()-5;
			if(deck.get_surface_type()==1)
				j=deck.get_nr_of_points()-6;
				
			value=deck.get_point_on_surface(1, j,0,0);
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);
			for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
			{
				for(u=0;u<=1;u=u+step)
				{
					value=deck.get_point_on_surface(segment, j,u,0);
					c=(int)(value.x*scale);
					d=(int)(value.z*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=deck.get_control_point(segment, j);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
			}
			
			j=4;
			if(deck.get_surface_type()==1)
				j=5;
				
			value=deck.get_control_point(deck.get_nr_of_segments()-1, j);
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);
			
			j=deck.get_nr_of_points()-5;
			if(deck.get_surface_type()==1)
				j=deck.get_nr_of_points()-6;
			
			value=deck.get_control_point(deck.get_nr_of_segments()-1, j);
			c=(int)(value.x*scale);
			d=(int)(value.z*scale);
			g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
	
			//draw tail
//			g.setColor(Color.black);
//			g.setColor(new Color(0,0,0));
			g.setColor(color);

			value=deck.get_point_on_surface(1, 2,0,0);
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);
			for(point=2;point<deck.get_nr_of_points()-2;point++)
			{
				for(v=0;v<=1;v=v+step)
				{
					value=deck.get_point_on_surface(1, point,0,v);
					c=(int)(value.x*scale);
					d=(int)(value.z*scale);
					g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
					a=c;
					b=d;
				}
				value=deck.get_control_point(1, point);
				g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
			}	
	
	
			if(draw_tucked_under)
			{
				value=deck.get_control_point(0, 1);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, 1,u,0);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
				value=deck.get_control_point(0, deck.get_nr_of_points()-3);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(segment=2;segment<deck.get_nr_of_segments()-2;segment++)
				{	
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, deck.get_nr_of_points()-3,u,1);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}
		}
			
		
		if(is_marked)
		{
			value=active;//marked_surface.get_control_point(marked_segment, marked_point);			
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);
			g.setColor(Color.green);
			g.drawOval(a+offset_x-2,b+offset_y-2,5,5);
		}

		//draw deck cut
		if (nr_of_cuts_deck>0 && draw_deck_cut)
		{
		
			g.setColor(Color.green);
			value=deck_cut[0];
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);

			for(i=1;i<nr_of_cuts_deck;i++)
			{
				if(deck_collision[i])
					g.setColor(Color.red);
				else
					g.setColor(Color.green);
			
				value=deck_cut[i];
				c=(int)(value.x*scale);
				d=(int)(value.z*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
		}

		//draw bottom cut
		if (nr_of_cuts_bottom>0  && draw_bottom_cut)
		{
			g.setColor(Color.green);
			value=bottom_cut[0];
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);

			for(i=1;i<nr_of_cuts_bottom;i++)
			{
				if(bottom_collision[i])
					g.setColor(Color.red);
				else
					g.setColor(Color.green);
		
				value=bottom_cut[i];
				c=(int)(value.x*scale);
				d=(int)(value.z*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
		}


		for(i=0;i<nr_of_help_points;i++)
		{
			g.setColor(Color.red);
			g.drawLine((int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].z*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].z*scale)+offset_y+2);
			g.drawLine((int)(help_points[i].x*scale)+offset_x+2,(int)(-help_points[i].z*scale)+offset_y-2,(int)(help_points[i].x*scale)+offset_x-2,(int)(-help_points[i].z*scale)+offset_y+2);
		}





	}
	
	/**
	 * Draw a 3D view of the board
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param scale
	 * @param offset_x
	 * @param offset_y
	 * @param draw_tucked_under
	 * @param draw_bottom_cut
	 * @param draw_deck_cut
	 * @param color
	 * @param m
	 */
	public void draw_outline3D(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y, boolean draw_tucked_under, boolean draw_bottom_cut, boolean draw_deck_cut, java.awt.Color color, double[][] m)
	{
		NurbsPoint value;
		int segment, point;
		int a,b,c,d;
		double u,v;



		NurbsSurface tdeck=deck;
		NurbsSurface tbottom=bottom;
		
//		rotate(m);		

		//We need to transform this

		g.setStroke(new BasicStroke((float)1.1));
		g.setColor(Color.black);
		g.drawString("z", offset_x-10, offset_y+20);
		g.drawString("x", offset_x+20, offset_y+10);
		g.drawLine(offset_x, offset_y, offset_x+20, offset_y);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y-3);
		g.drawLine(offset_x+20, offset_y, offset_x+15, offset_y+3);
		g.drawLine(offset_x, offset_y, offset_x, offset_y+20);
		g.drawLine(offset_x, offset_y+20, offset_x+3, offset_y+15);
		g.drawLine(offset_x, offset_y+20, offset_x-3, offset_y+15);


		//We should do a depth sorting and start drawing from the back, but
		//simply checking the normal works for most models so fix it later...

		NurbsPoint p1, p2, p3, p4, pt;
		Vector3f normal;
		Polygon pol=new Polygon();
        NurbsPoint normal2;

        if(triangle_array!=null)
        {
            for(int i=0;i<triangle_count;i++)
            {
                pol.reset();
                
                p1=(NurbsPoint)triangle_array[i].vertices[0].clone();
                p1=rotate(p1,m);
                pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(p1.z*scale)+offset_y );
                
                normal2=(NurbsPoint)triangle_array[i].normal.clone();
                normal2=rotate(normal2,m);
                
                p2=(NurbsPoint)triangle_array[i].vertices[1].clone();
                p2=rotate(p2,m);
                pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(p2.z*scale)+offset_y );
                
                p3=(NurbsPoint)triangle_array[i].vertices[2].clone();
                p3=rotate(p3,m);
                pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(p3.z*scale)+offset_y );
                
                if(normal2.y>0)
                {
                    g.setColor(new Color((int)(250*Math.abs(normal2.y)), (int)(250*Math.abs(normal2.y)), (int)(250*Math.abs(normal2.y))));
                    //                        g.setColor(new Color(0, 0, 150));
                    g.fillPolygon(pol);
                }
                
            }
            
        }
        else
        {
            
		
        
		for(int i=1;i<deck.get_nr_of_segments()-2;i++)
		{
			for(u=0.0;u<1.0;u=u+step)
			{
				for(int j=1;j<deck.get_nr_of_points()-2;j++)
				{
					for(v=0.0;v<1.0;v=v+step)
					{
						pol.reset();
					
						p1=deck.get_point_on_surface(i,j,u,v);
						p1=rotate(p1,m);
						pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(p1.z*scale)+offset_y ); 
						
						normal=calculate_normal(deck, i, j, u, v);
						pt=new NurbsPoint(normal.x, normal.y, normal.z);
						pt=rotate(pt,m);
						
						p2=deck.get_point_on_surface(i,j,u,v+step);
						p2=rotate(p2,m);
						pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(p2.z*scale)+offset_y ); 

						p3=deck.get_point_on_surface(i,j,u+step,v+step);
						p3=rotate(p3,m);
						pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(p3.z*scale)+offset_y ); 

						p4=deck.get_point_on_surface(i,j,u+step,v);
						p4=rotate(p4,m);
						pol.addPoint( (int)(p4.x*scale)+offset_x, (int)(p4.z*scale)+offset_y ); 

						if(pt.y>0)
						{
							g.setColor(new Color((int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y))));
							g.fillPolygon(pol);
						}
							
					}
				}
			}
		}

		for(int i=1;i<bottom.get_nr_of_segments()-2;i++)
		{
			for(u=0.0;u<1.0;u=u+step)
			{
				for(int j=1;j<bottom.get_nr_of_points()-2;j++)
				{
					for(v=0.0;v<1.0;v=v+step)
					{
						pol.reset();
					
						p1=bottom.get_point_on_surface(i,j,u,v);
						p1=rotate(p1,m);						
						pol.addPoint( (int)(p1.x*scale)+offset_x, (int)(p1.z*scale)+offset_y ); 
						
						normal=calculate_normal(bottom, i, j, u, v);
						pt=new NurbsPoint(normal.x, normal.y, normal.z);
						pt=rotate(pt,m);

						p2=bottom.get_point_on_surface(i,j,u,v+step);
						p2=rotate(p2,m);
						pol.addPoint( (int)(p2.x*scale)+offset_x, (int)(p2.z*scale)+offset_y ); 

						p3=bottom.get_point_on_surface(i,j,u+step,v+step);
						p3=rotate(p3,m);
						pol.addPoint( (int)(p3.x*scale)+offset_x, (int)(p3.z*scale)+offset_y ); 

						p4=bottom.get_point_on_surface(i,j,u+step,v);
						p4=rotate(p4,m);
						pol.addPoint( (int)(p4.x*scale)+offset_x, (int)(p4.z*scale)+offset_y ); 

						if(pt.y>0)
						{
							g.setColor(new Color((int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y)), (int)(250*Math.abs(normal.y))));
							g.fillPolygon(pol);
						}
							
					}
				}
			}
		}
        }

		if(single_point_editing)
		{


			g.setColor(color);
//			g.setColor(Color.black);
			
			//draw bottom			
			for(point=1;point<bottom.get_nr_of_points()-2;point++)
			{
				value=bottom.get_point_on_surface(1, point,0,0);
				value=rotate(value,m);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(segment=1;segment<bottom.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=bottom.get_point_on_surface(segment, point,u,0);
						value=rotate(value,m);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
					value=bottom.get_control_point(segment, point);
					value=rotate(value,m);
					g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
				}
			}	

			for(segment=1;segment<bottom.get_nr_of_segments()-2;segment++)
			{
				value=bottom.get_point_on_surface(segment, 1,0,0);
				value=rotate(value,m);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(point=1;point<bottom.get_nr_of_points()-2;point++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=bottom.get_point_on_surface(segment, point,0,u);
						value=rotate(value,m);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}	

			//draw deck			
			for(point=1;point<deck.get_nr_of_points()-2;point++)
			{
				value=deck.get_point_on_surface(1, point,0,0);
				value=rotate(value,m);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, point,u,0);
						value=rotate(value,m);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
					value=deck.get_control_point(segment, point);
					value=rotate(value,m);
					g.drawOval((int)(value.x*scale)+offset_x-2,(int)(value.z*scale)+offset_y-2,5,5);
				}
			}	

			for(segment=1;segment<deck.get_nr_of_segments()-2;segment++)
			{
				value=deck.get_point_on_surface(segment, 1,0,0);
				value=rotate(value,m);
				a=(int)(value.x*scale);
				b=(int)(value.z*scale);
				for(point=1;point<deck.get_nr_of_points()-2;point++)
				{
					for(u=0;u<=1;u=u+step)
					{
						value=deck.get_point_on_surface(segment, point,0,u);
						value=rotate(value,m);
						c=(int)(value.x*scale);
						d=(int)(value.z*scale);
						g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
						a=c;
						b=d;
					}
				}
			}	

		}
		
		if(is_marked)
		{
			value=active;
			value=rotate(value,m);
			
			a=(int)(value.x*scale);
			b=(int)(value.z*scale);
			g.setColor(Color.green);
			g.drawOval(a+offset_x-2,b+offset_y-2,5,5);
		}



		double[] t={0.0 , 0.0, 0.0};
		value=new NurbsPoint(0.0, 0.0, 0.0);
		
		//draw deck cut
		if (nr_of_cuts_deck>0 && draw_deck_cut)
		{
		
			g.setColor(Color.green);
			p1=deck_cut[0];

			value.x=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
			value.y=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
			value.z=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];

			a=(int)(value.x*scale);
			b=(int)(value.z*scale);

			for(int i=1;i<nr_of_cuts_deck;i++)
			{
				if(deck_collision[i])
					g.setColor(Color.red);
				else
					g.setColor(Color.green);
			
				p1=deck_cut[i];

				value.x=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
				value.y=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
				value.z=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];

				c=(int)(value.x*scale);
				d=(int)(value.z*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
		}

		//draw bottom cut
		
		if (nr_of_cuts_bottom>0  && draw_bottom_cut)
		{
		
			g.setColor(Color.green);
			p1=bottom_cut[0];

			value.x=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
			value.y=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
			value.z=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];

			a=(int)(value.x*scale);
			b=(int)(value.z*scale);

			for(int i=1;i<nr_of_cuts_bottom;i++)
			{
				if(bottom_collision[i])
					g.setColor(Color.red);
				else
					g.setColor(Color.green);
			
				p1=bottom_cut[i];

				value.x=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
				value.y=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
				value.z=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];

				c=(int)(value.x*scale);
				d=(int)(value.z*scale);
				g.drawLine(a+offset_x,b+offset_y,c+offset_x,d+offset_y);
				a=c;
				b=d;
			}
		}


		deck=tdeck;
		bottom=tbottom;


	}	
	
	/**
	 * Draws the rocker curvature. This function is currently not used
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param scale
	 * @param offset_x
	 * @param offset_y
	 */
	public void draw_rocker_curvature(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y)
	{
		NurbsPoint value;
		int segment, point;
		int a,b,c,d;
		double u;
		double[] curvature=new double[(int)(bottom.get_nr_of_segments()/step)];
		int[] x_value=new int[(int)(bottom.get_nr_of_segments()/step)];
		double max_curv=0.0;
		double min_curv=0.0;
		int i=0, j=0;
		double y_scale;
		int y_offset;

		for(segment=3;segment<bottom.get_nr_of_segments()-4;segment++)
		{
			for(u=0;u<1;u=u+step)
			{
				value=bottom.get_point_on_surface(segment, bottom.get_nr_of_points()/2,u+step/2,0);
				x_value[i]=(int)(value.x*scale)+offset_x;
				curvature[i]=bottom.get_curvature_xy(segment, bottom.get_nr_of_points()/2, u+step/2, 0);
				if(curvature[i]>max_curv && x_value[i]>0 && x_value[i]<width)
					max_curv=curvature[i];
				if(curvature[i]<min_curv && x_value[i]>0 && x_value[i]<width)
					min_curv=curvature[i];
				i++;
			}
		}

		y_scale=(height-40)/(max_curv-min_curv);
		y_offset=(int)(-min_curv*y_scale)+20;

		g.setStroke(new BasicStroke((float)1.1));
		g.setColor(Color.black);
		g.drawLine(x_value[0],height-y_offset,x_value[i-1],height-y_offset);
		g.setColor(Color.blue);
		a=x_value[j];
		b=(int)(height-(curvature[j]*y_scale+y_offset));
		for(j=1;j<i;j++)
		{
			c=x_value[j];
			d=(int)(height-(curvature[j]*y_scale+y_offset));
			g.drawLine(a,b,c,d);
			a=c;
			b=d;
		}
	}

	/**
	 * Draws the outline curvature. This function is currently not used
	 * 
	 * @param g
	 * @param width
	 * @param height
	 * @param scale
	 * @param offset_x
	 * @param offset_y
	 */
	public void draw_outline_curvature(Graphics2D g, int width, int height, double scale, int offset_x, int offset_y)
	{
		NurbsPoint value;
		int segment, point;
		int a,b,c,d;
		double u;
		double[] curvature=new double[(int)(bottom.get_nr_of_segments()/step)];
		int[] x_value=new int[(int)(bottom.get_nr_of_segments()/step)];
		double max_curv=0.0;
		double min_curv=0.0;
		int i=0, j=0;
		double y_scale;
		int y_offset;

		for(segment=3;segment<deck.get_nr_of_segments()-4;segment++)
		{
			for(u=0;u<1;u=u+step)
			{
				value=deck.get_point_on_surface(segment, 1,u+step/2,0);
				x_value[i]=(int)(value.x*scale)+offset_x;
				curvature[i]=bottom.get_curvature_xz(segment, 1, u+step/2, 0);
				if(curvature[i]>max_curv && x_value[i]>0 && x_value[i]<width)
					max_curv=curvature[i];
				if(curvature[i]<min_curv && x_value[i]>0 && x_value[i]<width)
					min_curv=curvature[i];
				i++;
			}
		}

		y_scale=(height-40)/(max_curv-min_curv);
		y_offset=(int)(-min_curv*y_scale)+20;

		g.setStroke(new BasicStroke((float)1.1));
		g.setColor(Color.black);
		g.drawLine(x_value[0],height-y_offset,x_value[i-1],height-y_offset);
		g.setColor(Color.blue);
		a=x_value[j];
		b=(int)(height-(curvature[j]*y_scale+y_offset));
		for(j=1;j<i;j++)
		{
			c=x_value[j];
			d=(int)(height-(curvature[j]*y_scale+y_offset));
			g.drawLine(a,b,c,d);
			a=c;
			b=d;
		}
	}
	
	/**
	 * Rotates a point with a given rotation matrix
	 * 
	 * @param p1 - the point to rotate
	 * @param m - the rotation matrix
	 */
	private NurbsPoint rotate(NurbsPoint p1, double[][] m)
	{
		NurbsPoint value=new NurbsPoint(0.0, 0.0, 0.0);
		
		value.x=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z;
		value.y=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z;
		value.z=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z;
		
		return value;
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

		double step2=0.1; //0.1
		double my_u1,my_v1;
		double my_u2,my_v2;
		int my_i1, my_j1;
		int my_i2, my_j2;
		NurbsPoint p,p1,p2,p3,p4;
	
	//	return surf.calculate_normal(i, j, u, v, (surf==bottom));

		//float board_length=(float)get_length();

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


}


