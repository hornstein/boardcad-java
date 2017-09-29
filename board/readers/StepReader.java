package board.readers;

import cadcore.*;
import cadcore.BezierBoardCrossSection;
import board.*;

import java.awt.*;
import java.awt.geom.*;

import java.io.*;


public class StepReader {
	
	static public NurbsSurface bottom;
	static public NurbsSurface deck;
	static public NurbsPoint x_axis_direction;
	static public NurbsPoint y_axis_direction;
	static public NurbsPoint local_origin;

	/**
	 * Creates a new board from a step-file
	 * 
	 * @param dataIn - stream to the step file
	 * @param brd - a BezierBoard where outline, rocker, and cross sections will be stored
	 */
	static public void loadFile(DataInputStream dataIn, BezierBoard brd)
	{
		loadFile(dataIn, brd, false);
	}

	/**
	 * Loads a 2D view of the board for preview
	 * 
	 * @param brd - a BezierBoard where outline, rocker, and cross sections will be stored
	 * @param filename - the name of the file to preview
	 */
	static public void loadPreview(BezierBoard brd, String filename)
	{

		try
		{
			DataInputStream dataIn = new DataInputStream(new FileInputStream(filename));
			loadFile(dataIn, brd, true);

		}
		catch(IOException excep1)
		{
			//here we should open a dialog
			System.out.println("Problem finding file" + excep1.toString());
		}
	}

	static private void loadFile(DataInputStream dataIn, BezierBoard brd, boolean preview)
	{
		int i, j;
		int[] x;
		int[][] by, dy;
		int[][] bz, dz;
		int version=0;
		int nr_of_segments=0;
		int data_points_deck=0;
		int data_points_bottom=0;
		int x_axis_index=0;
		int y_axis_index=0;
		int origin_index=0;

		boolean is_boardcad_file=false;
		double file_version=0.0;
		
		//read step-file
		NurbsPoint[] cartesian_points=new NurbsPoint[10000];
		
		//save all cartesian points
		String cartpoint, mystring, surface_string, deck_string, bottom_string;
		String outline_bezier_string, bottom_bezier_string, deck_bezier_string;
		String[] section_bezier_string=new String[100];
		int pos, pos2, pos3;
		double x_value, y_value, z_value;
		
		int index;
		
		int nr_of_cross_sections=0;
		
		cartpoint=new String("");
		deck_string=new String("");
		bottom_string=new String("");
		outline_bezier_string=new String("");
		bottom_bezier_string=new String("");
		deck_bezier_string=new String("");
		section_bezier_string[nr_of_cross_sections]=new String("");
		
		do
		{
			try
			{
				cartpoint=dataIn.readLine();
				pos=cartpoint.indexOf("CARTESIAN_POINT");
				if(pos<=0)
				{
					pos=cartpoint.indexOf("DIRECTION");				
				}
				if(pos>0)
				{
//					System.out.println("" + cartpoint);
					pos=cartpoint.indexOf("'");
					pos2=cartpoint.indexOf("'", pos+1);
					String point_name=cartpoint.substring(pos+1, pos2);

					pos=cartpoint.indexOf("#");
					pos2=cartpoint.indexOf("=");
					
					index=Integer.parseInt((cartpoint.substring(pos+1,pos2)).trim());
					cartpoint=cartpoint.substring(pos2+1);

					pos=cartpoint.indexOf(",");
					cartpoint=cartpoint.substring(pos+1);
					
					pos=cartpoint.indexOf("(");
					pos2=cartpoint.indexOf(",");
					
					x_value=Double.parseDouble((cartpoint.substring(pos+1,pos2)).trim());

					pos=cartpoint.indexOf(",");
					cartpoint=cartpoint.substring(pos+1);
					pos=cartpoint.indexOf(",");

					y_value=Double.parseDouble((cartpoint.substring(0,pos)).trim());
					
					cartpoint=cartpoint.substring(pos+1);
					pos=cartpoint.indexOf(")");
					
					z_value=Double.parseDouble((cartpoint.substring(0,pos)).trim());
					
					cartesian_points[index]=new NurbsPoint(x_value, y_value, z_value);
					cartesian_points[index].name=new String(point_name);
					
				}
				
				
				pos=cartpoint.indexOf("FILE_NAME");
				if(pos>=0)
				{
					pos=cartpoint.indexOf("BoardCAD");
					if(pos>=0)
						is_boardcad_file=true;
					
					pos=cartpoint.indexOf("BCstep 1.0");
					if(pos>=0)
						file_version=1.0;
				}
				
				
				pos=cartpoint.indexOf("BOUNDED_SURFACE");
				if(pos>=0)
				{
					surface_string=new String(cartpoint);
					do
					{
						surface_string=surface_string.concat(cartpoint);
						cartpoint=dataIn.readLine();
					}while(cartpoint.indexOf(";")<0);
					
					System.out.println("" + surface_string);
					
					
					if(surface_string.indexOf("deck")>0)
					{
						deck_string=new String(surface_string);
					}

					if(surface_string.indexOf("bottom")>0)
					{
						bottom_string=new String(surface_string);
					}					
					

				}
				
				pos=cartpoint.indexOf("CARTESIAN_TRANSFORMATION_OPERATOR");
				if(pos>=0)
				{
					
					System.out.println("found cartesian transformation operator");

					pos=cartpoint.indexOf("#");
					
					cartpoint=cartpoint.substring(pos+1);
					pos=cartpoint.indexOf("#");
					pos2=cartpoint.indexOf(",",pos);
					x_axis_index=Integer.parseInt((cartpoint.substring(pos+1,pos2)).trim());
					
					cartpoint=cartpoint.substring(pos+1);
					pos=cartpoint.indexOf("#");
					pos2=cartpoint.indexOf(",",pos);
					y_axis_index=Integer.parseInt((cartpoint.substring(pos+1,pos2)).trim());

					cartpoint=cartpoint.substring(pos+1);
					pos=cartpoint.indexOf("#");
					pos2=cartpoint.indexOf(",",pos);
					origin_index=Integer.parseInt((cartpoint.substring(pos+1,pos2)).trim());
				}

				pos=cartpoint.indexOf("BEZIER_CURVE");
				if(pos>=0)
				{
					
					System.out.println("found bezier curve");
					
					surface_string=new String(cartpoint);
					while(cartpoint.indexOf(";")<0)
					{
						surface_string=surface_string.concat(cartpoint);
						cartpoint=dataIn.readLine();
					}
					
					System.out.println("" + surface_string);
					
					if(surface_string.indexOf("outline")>0)
					{
						outline_bezier_string=new String(surface_string);
					}
					if(surface_string.indexOf("bottom")>0)
					{
						bottom_bezier_string=new String(surface_string);
					}
					if(surface_string.indexOf("deck")>0)
					{
						deck_bezier_string=new String(surface_string);
					}
					if(surface_string.indexOf("section")>0)
					{
						section_bezier_string[nr_of_cross_sections]=new String(surface_string);
						nr_of_cross_sections++;
					}

				}
				
				
				
				
			}
			catch(IOException e)
			{
				System.out.println("Problem reading file" + e.toString());
			}				
		}while(cartpoint.indexOf("END-ISO-10303-21")<0);


		if(!preview)
		{
			//create deck
			System.out.println("Creating deck");
		
			deck=new NurbsSurface(deck_string, cartesian_points);
		
			//create bottom
			System.out.println("Creating bottom");
		
			bottom=new NurbsSurface(bottom_string, cartesian_points);
		
		
			//fix tail

			NurbsPoint p1,p2;
		

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
				deck.set_control_point(i,deck.get_nr_of_points()-1,p1);
				deck.set_control_point(i,deck.get_nr_of_points()-1,p1);
				deck.set_control_point(i,deck.get_nr_of_points()-1,p1);

				p2=new NurbsPoint(p1.x, p1.y, -p1.z);
			
				bottom.set_control_point(i,bottom.get_nr_of_points()-3,p2);
				bottom.set_control_point(i,bottom.get_nr_of_points()-2,p2);
				bottom.set_control_point(i,bottom.get_nr_of_points()-1,p2);
				deck.set_control_point(i,0,p2);
				deck.set_control_point(i,1,p2);
				deck.set_control_point(i,2,p2);

			}		
		
			//evaluate surfaces
		
		
			deck.evaluate_surface();
			bottom.evaluate_surface();

			if(file_version>0.0)
			{
				deck.flipNormal();
				bottom.flipNormal();
			}

		
			//creating cartesian transformation between nurbs and bezier boards
		
			if(x_axis_index>0)
			{
				x_axis_direction=cartesian_points[x_axis_index];
				y_axis_direction=cartesian_points[y_axis_index];
				local_origin=cartesian_points[origin_index];
			}
		}
		
		if(outline_bezier_string.length()>0)
		{
			brd.reset();
			BezierSpline bs;
		
			//create bezier outline
			bs=brd.getOutline();
			
			create_bezier_curve(bs, outline_bezier_string, cartesian_points, 0);

			//create outline guide points
			
			for(i=0; i<10000; i++)
			{
				if(cartesian_points[i]!=null)
				{
					if(cartesian_points[i].name.equalsIgnoreCase("outline guide point"))
					{
						brd.getOutlineGuidePoints().add(new Point2D.Double(cartesian_points[i].x/10, cartesian_points[i].z/10));
					}
				}
			
			}
		
			//create bezier bottom
			bs=brd.getBottom();
			create_bezier_curve(bs, bottom_bezier_string, cartesian_points, 1);
		
			//create bottom guide points
			
			for(i=0; i<10000; i++)
			{
				if(cartesian_points[i]!=null)
				{
					if(cartesian_points[i].name.equalsIgnoreCase("bottom guide point"))
					{
						brd.getBottomGuidePoints().add(new Point2D.Double(cartesian_points[i].x/10, cartesian_points[i].y/10));
					}
				}
			
			}


			//create bezier deck
			bs=brd.getDeck();
			System.out.println(deck_bezier_string);
			create_bezier_curve(bs, deck_bezier_string, cartesian_points, 1);
	
			//create deck guide points
			
			for(i=0; i<10000; i++)
			{
				if(cartesian_points[i]!=null)
				{
					if(cartesian_points[i].name.equalsIgnoreCase("deck guide point"))
					{
						brd.getDeckGuidePoints().add(new Point2D.Double(cartesian_points[i].x/10, cartesian_points[i].y/10));
					}
				}
			
			}

			//create bezier section
			String s1;
			int index1;
		
			for(i=0;i<nr_of_cross_sections;i++)
			{
				BezierBoardCrossSection crossSection = new BezierBoardCrossSection();
				brd.getCrossSections().add(crossSection);
				
				//find x position
				pos=section_bezier_string[i].indexOf("(");
				s1=section_bezier_string[i].substring(pos+1);
				pos=s1.indexOf("(");
				s1=s1.substring(pos+1);
				pos=s1.indexOf("#");
				pos2=s1.indexOf(",");
				pos3=s1.indexOf(")");
				pos2=Math.min(pos2,pos3);
				index1=Integer.parseInt((s1.substring(pos+1,pos2)).trim());		
				
				
				crossSection.setPosition(cartesian_points[index1].x/10);
				bs=crossSection.getBezierSpline();
				create_bezier_curve(bs, section_bezier_string[i], cartesian_points, 2);

				//create cross section guide points
			
				for(j=0; j<10000; j++)
				{
					if(cartesian_points[j]!=null)
					{
						if(cartesian_points[j].name.equalsIgnoreCase("cross section " + i + " guide point"))
						{
							brd.getCrossSections().get(i).getGuidePoints().add(new Point2D.Double(cartesian_points[j].z/10, cartesian_points[j].y/10));
						}
					}
				}
			

			}
	
			
		}
	}	
	
	/**
	 * Creates an iternal bezier spline from the step model
	 * 
	 * @param bs - the iternal bezier spline
	 * @param stepstring - a string describing a bezier curve on step format
	 * @param cartesian_points - an array of cartesian points
	 * @param transform - defines how the 3D coordinates should be transformed to 2D coordinates 
	 */
	private static void create_bezier_curve(BezierSpline bs, String stepstring, NurbsPoint[] cartesian_points, int transform)
	{
	
		String s1;
		int points2=0;
		int index1=0;
		int index2=0;
		int index3=0;
		int pos, pos2;
		int i,j;
		
		BezierKnot bcp;
		
		//find number of points
		pos=stepstring.indexOf("(");
		s1=stepstring.substring(pos+1);
		pos=s1.indexOf("(");
		pos2=s1.indexOf(")");
		s1=s1.substring(pos+1, pos2);		
		pos=s1.indexOf(",");
		
		if(pos<0) //special case when curve only has one point
		{
			//read first point
			
			pos=stepstring.indexOf("(");
			s1=stepstring.substring(pos+1);
			pos=s1.indexOf("(");
			s1=s1.substring(pos+1);
			pos=s1.indexOf("#");
			pos2=s1.indexOf(")");
			index1=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
			
			bcp=new BezierKnot();
			
			bcp.getEndPoint().setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
			bcp.getTangentToPrev().setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
			bcp.getTangentToNext().setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
			
			add_meta_data(bcp, cartesian_points[index1].name);

			bs.append(bcp);
			
		}
		else
		{
			do
			{
				points2++;
				s1=s1.substring(pos+1);
				pos=s1.indexOf(",");
			}while(pos>=0);
			
			points2=points2-3;
			
			//read first point
			
			pos=stepstring.indexOf("(");
			s1=stepstring.substring(pos+1);
			pos=s1.indexOf("(");
			s1=s1.substring(pos+1);
			pos=s1.indexOf("#");
			pos2=s1.indexOf(",");
			index1=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
			s1=s1.substring(pos2+1);
			pos=s1.indexOf("#");
			pos2=s1.indexOf(",");
			index2=Integer.parseInt((s1.substring(pos+1,pos2)).trim());	
	
			bcp=new BezierKnot();
			Point2D.Double[] points = bcp.getPoints();
			if(transform==0)
			{
				points[1].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].z/10);
				points[0].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].z/10);
				points[2].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].z/10);
			}
			else if(transform==1) //rocker
			{
				points[1].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].y/10);
				points[0].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].y/10);
				points[2].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].y/10);
			}
			else //cross section
			{
				points[1].setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
				points[0].setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
				points[2].setLocation(cartesian_points[index2].z/10, cartesian_points[index2].y/10);
			}	

			add_meta_data(bcp, cartesian_points[index1].name);
	
			bs.append(bcp);
	
			//read center points
			for(i=0;i<points2;i=i+3)
			{
				s1=s1.substring(pos2+1);
				pos=s1.indexOf("#");
				pos2=s1.indexOf(",");
				index1=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
					
				s1=s1.substring(pos2+1);
				pos=s1.indexOf("#");
				pos2=s1.indexOf(",");
				index2=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
	
				s1=s1.substring(pos2+1);
				pos=s1.indexOf("#");
				pos2=s1.indexOf(",");
				index3=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
	
				bcp=new BezierKnot();
				if(transform==0) //outline
				{
					bcp.getPoints()[1].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].z/10);
					bcp.getPoints()[0].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].z/10);
					bcp.getPoints()[2].setLocation(cartesian_points[index3].x/10, cartesian_points[index3].z/10);
				}
				else if(transform==1) //rocker
				{
					bcp.getPoints()[1].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].y/10);
					bcp.getPoints()[0].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].y/10);
					bcp.getPoints()[2].setLocation(cartesian_points[index3].x/10, cartesian_points[index3].y/10);
				}
				else //cross section
				{
					bcp.getPoints()[1].setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
					bcp.getPoints()[0].setLocation(cartesian_points[index2].z/10, cartesian_points[index2].y/10);
					bcp.getPoints()[2].setLocation(cartesian_points[index3].z/10, cartesian_points[index3].y/10);
				}
				
				add_meta_data(bcp, cartesian_points[index2].name);
				
				bs.append(bcp);
			}
	
			//read end point
	
			s1=s1.substring(pos2+1);
			pos=s1.indexOf("#");
			pos2=s1.indexOf(",");
			index1=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
			s1=s1.substring(pos2+1);
			pos=s1.indexOf("#");
			pos2=s1.indexOf(")");
			index2=Integer.parseInt((s1.substring(pos+1,pos2)).trim());
			
			bcp=new BezierKnot();
			if(transform==0)
			{
				bcp.getPoints()[1].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].z/10);
				bcp.getPoints()[0].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].z/10);
				bcp.getPoints()[2].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].z/10);
			}
			else if(transform==1) //rocker
			{
				bcp.getPoints()[1].setLocation(cartesian_points[index1].x/10, cartesian_points[index1].y/10);
				bcp.getPoints()[0].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].y/10);
				bcp.getPoints()[2].setLocation(cartesian_points[index2].x/10, cartesian_points[index2].y/10);
			}
			else //cross section
			{
				bcp.getPoints()[1].setLocation(cartesian_points[index1].z/10, cartesian_points[index1].y/10);
				bcp.getPoints()[0].setLocation(cartesian_points[index2].z/10, cartesian_points[index2].y/10);
				bcp.getPoints()[2].setLocation(cartesian_points[index2].z/10, cartesian_points[index2].y/10);
			}
			
			add_meta_data(bcp, cartesian_points[index2].name);
		
			bs.append(bcp);
			
		}
	}

	/**
	 * Adds meta data for the bezier control points
	 * This is a hack where the name of the cartesian point is used to store
	 * information about tangent continuity between bezier segments. Not sure
	 * if there is a better way to store this information in the step file
	 * 
	 * @param bcp - the bezier control point
	 * @param name - the name of the cartesian point
	 */
	private static void add_meta_data(BezierKnot bcp, String name)
	{
	
		if(name.indexOf("false false")>=0)
		{
			bcp.setContinous(false);
			bcp.setOther(false);
		}
		else if(name.indexOf("false true")>=0)
		{
			bcp.setContinous(false);
			bcp.setOther(true);
		}
		else if(name.indexOf("true false")>=0)
		{
			bcp.setContinous(true);
			bcp.setOther(false);
		}
		else
		{
			bcp.setContinous(true);
			bcp.setOther(true);
		}			
	}
	

}
