package boardcam;

//=========================================================
/*
 * Scan.java: creates a board from scanned points
 */
//=========================================================

import cadcore.*;
import board.BezierBoard;
import boardcad.gui.jdk.BoardCAD;

import java.io.*;
import java.util.*;
import java.awt.geom.*;
import javax.swing.*;


public class Scan
{
	private BezierBoard brd;
	public double min_x;
	public double max_x;
    
    
	//This is a duplication of the machine data in Boards.java
	//should be moved to a separate class
    
	
	private double[][] machineCoordinates;
	private double[] endSupportPosition;
	private NurbsPoint support1;
	private double support1_radius;
	private NurbsPoint support2;
	private double support2_radius;
	private NurbsPoint scan_coord;
	//private double scan_comp;
	private String scan_path;
    
    
	public NurbsPoint help_points[];
	public int nr_of_help_points;
    
    /*	private double zMaxHeight;
     private double cutDownOffset;
     private boolean includeComments;
     private String commentStart;
     private String commentEnd;
     private String stopSign;
     private double cross_step;
     private double length_step;
     private double speed;
     private double stringer_speed;
     private String toolname;
     private double toolscale_x;
     private double toolscale_y;
     private double toolscale_z;
     private double toolradie;
     private double sandwich;
     private double perimeter_size;
     private NurbsPoint support1;
     private double support1_radius;
     private NurbsPoint support2;
     private double support2_radius;
     private boolean cut_stringer;
     private boolean cut_rail;
     private NurbsPoint scan_coord;
     private double scan_comp;
     private String scan_path;
     */
	public NurbsPoint[][] stl_point;
	public NurbsPoint[] stl_normal;
	public int n;
	
	public NurbsPoint[] stl_cs;
	public int n_cs;
	public double cs_outline;
	public double cs_bottom;
	public double cs_deck;
	
	
	
	//----------------------------------------------------
    
    
    
	public Scan()
	{
		help_points=new NurbsPoint[1000];
		nr_of_help_points=0;
        
		brd=BoardCAD.getInstance().getCurrentBrd();
		min_x=10000;
		max_x=0;
		read_machine_data();
	}
	
	public void virtualScan(String filename)
	{
		// Load STL-file
		loadSTL(filename);
		
		// Get length
		for(int i=0; i<n; i++)
		{
			for(int j=0; j<3; j++)
			{
				if(stl_point[i][j].x<min_x)
					min_x=stl_point[i][j].x;
				if(stl_point[i][j].x>max_x)
					max_x=stl_point[i][j].x;
			}
		}
		
		// Translate to x=0
		for(int i=0; i<n; i++)
		{
			for(int j=0; j<3; j++)
			{
				stl_point[i][j].x=stl_point[i][j].x-min_x;
			}
		}
		max_x=(max_x-min_x)/10.0;
		min_x=0;
		
		// Scale board and place cross sections
		brd.scale(max_x, brd.getMaxWidth(), brd.getMaxThickness());
		brd.getCrossSections().get(1).setPosition(30.48);
		brd.getCrossSections().get(2).setPosition(max_x/2);
		brd.getCrossSections().get(3).setPosition(max_x-30.48);
        
		// Get guide points for outline and rocker
		ArrayList<Point2D.Double> outline_gp=brd.getOutlineGuidePoints();
		ArrayList<Point2D.Double> bottom_gp=brd.getBottomGuidePoints();
		ArrayList<Point2D.Double> deck_gp=brd.getDeckGuidePoints();
		
		for(double x=0; x<max_x*10; x=x+10)
		{
			scanCrossSectionSTL(x);
			outline_gp.add(new Point2D.Double(x/10.0, cs_outline/10.0));
			bottom_gp.add(new Point2D.Double(x/10.0, cs_bottom/10.0));
			deck_gp.add(new Point2D.Double(x/10.0, cs_deck/10.0));
		}
		
		approximateCurve(brd.getOutline(), brd.getOutlineGuidePoints());
		approximateCurve(brd.getBottom(), brd.getBottomGuidePoints());
		approximateCurve(brd.getDeck(), brd.getDeckGuidePoints());
		
		// Scan cross sections
		BezierBoardCrossSection cs;
		ArrayList<Point2D.Double> csgp;
		
		for(int i=1; i<=3; i++)
		{
			cs = brd.getCrossSections().get(i);
			csgp = cs.getGuidePoints();
			scanCrossSectionSTL(cs.getPosition()*10.0);
			for(int j=0;j<n_cs;j++)
			{
				if(stl_cs[j].z>0)
					csgp.add(new Point2D.Double(stl_cs[j].z/10.0, (stl_cs[j].y-cs_bottom)/10.0));
			}
            
			approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
		}
		
		// Make sure the board is placed at x=0
		repair();
	}
	
	public void scanCrossSectionSTL(double x)
	{
		stl_cs=new NurbsPoint[10000];
		n_cs=0;
		double d,d1,d2;
		double y;
		double z;
		cs_outline=0.0;
		cs_deck=-10000;
		cs_bottom=10000;
		if(x<0.1)
			x=0.1;
		
		for(int i=0; i<n; i++)
		{
			for(int j=0; j<2; j++)
			{
				if(stl_point[i][j].x<x && stl_point[i][j+1].x>x)
				{
					// Interpolate between points
					d=stl_point[i][j+1].x-stl_point[i][j].x;
					d1=x-stl_point[i][j].x;
					d2=stl_point[i][j+1].x-x;
					y=(stl_point[i][j].y*d2+stl_point[i][j+1].y*d1)/d;
					z=(stl_point[i][j].z*d2+stl_point[i][j+1].z*d1)/d;
					stl_cs[n_cs]=new NurbsPoint(x, y, z);
					n_cs=n_cs+1;
					
					if(z>cs_outline)
						cs_outline=z;
					if(y>cs_deck)
						cs_deck=y;
					if(y<cs_bottom)
						cs_bottom=y;
				}
				if(stl_point[i][j+1].x<x && stl_point[i][j].x>x)
				{
					// Interpolate between points
					d=stl_point[i][j].x-stl_point[i][j+1].x;
					d1=x-stl_point[i][j+1].x;
					d2=stl_point[i][j].x-x;
					y=(stl_point[i][j+1].y*d2+stl_point[i][j].y*d1)/d;
					z=(stl_point[i][j+1].z*d2+stl_point[i][j].z*d1)/d;
					stl_cs[n_cs]=new NurbsPoint(x, y, z);
					n_cs=n_cs+1;
					
					if(z>cs_outline)
						cs_outline=z;
					if(y>cs_deck)
						cs_deck=y;
					if(y<cs_bottom)
						cs_bottom=y;
				}
			}
		}
		
	}
    
	private void loadSTL(String filename)
	{
        
		n=0;
		stl_point=new NurbsPoint[100000][3];
		stl_normal=new NurbsPoint[100000];
        
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			// Read cutter data
            
			String line;
			int pos=0;
			int pos2=0;
            
			do{
                
				//search for normal, i.e. start of new facet
                
				do{
					line=buf_reader.readLine();
					line.toLowerCase();
					pos=line.indexOf("normal");	// a new facet should start with "facet normal"
					pos2=line.indexOf("endsolid");	// the file should end with "endsolid"
				}while(pos==-1 && pos2==-1);
                
				
				if(pos>0)
				{
                    
					
					//read normal
					stl_normal[n]=new NurbsPoint(0.0, 0.0, 0.0);
					line=line.substring(pos+6);	// remove part before normal coordinates
					line=line.trim();		// remove leading spaces
					pos=line.indexOf(' ');		// find space after normal_x coordinate
					stl_normal[n].x=Double.parseDouble(line.substring(0,pos));	// read normal_x
					line=line.substring(pos);	// remove normal_x
					line=line.trim();		// remove leading spaces
					pos=line.indexOf(' ');		// find space after normal_y coordinate
					stl_normal[n].y=Double.parseDouble(line.substring(0,pos));	// read normal_y
					line=line.substring(pos);	// remove normal_y
					line=line.trim();		// remove leading spaces
					stl_normal[n].z=Double.parseDouble(line);	// read normal_z
					for(int i=0;i<3;i++)
					{
                        
						//read vertex
						do{
							line=buf_reader.readLine();
							line.toLowerCase();
							pos=line.indexOf("vertex");	// a new vertex should start with "vertex"
						}while(pos==-1);
                        
						stl_point[n][i]=new NurbsPoint(0.0, 0.0, 0.0);
						line=line.substring(pos+6);	// remove part before vertex coordinates
						line=line.trim();		// remove leading spaces
                        
						pos=line.indexOf(' ');		// find space after vertex_x coordinate
						stl_point[n][i].x=Double.parseDouble(line.substring(0,pos));	// read vertex_x
                        
						line=line.substring(pos);	// remove vertex_x
						line=line.trim();		// remove leading spaces
						pos=line.indexOf(' ');		// find space after vertex_y coordinate
						stl_point[n][i].y=Double.parseDouble(line.substring(0,pos));	// read vertex_y
						
						line=line.substring(pos);	// remove vertex_y
						line=line.trim();		// remove leading spaces
						stl_point[n][i].z=Double.parseDouble(line);	// read vertex_z
					}
					n=n+1;
				}
				
			}while(pos2==-1);
            
			buf_reader.close ();
			
		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}
        
	}
    
	
	public void readScan()
	{
		
		readScanOutline(brd.getOutlineGuidePoints());
		brd.scale(max_x, brd.getMaxWidth(), brd.getMaxThickness());
		brd.getCrossSections().get(1).setPosition(30.48);
		brd.getCrossSections().get(2).setPosition(max_x/2);
		brd.getCrossSections().get(3).setPosition(max_x-30.48);
		approximateCurve(brd.getOutline(), brd.getOutlineGuidePoints());
		
		// Read scanned guide points and approximate bottom
		
		readScanBottom(brd.getBottomGuidePoints());
		approximateCurve(brd.getBottom(), brd.getBottomGuidePoints());
        
		BezierBoardCrossSection cs;
		String filename=scan_path + "bottomsection1.txt";
		readScanBottomCrossSection(brd.getCrossSections().get(1).getGuidePoints(), brd.getCrossSections().get(1).getPosition(), filename);
		cs = brd.getCrossSections().get(1);
		approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
        
		filename=scan_path + "bottomsection2.txt";
		readScanBottomCrossSection(brd.getCrossSections().get(2).getGuidePoints(), brd.getCrossSections().get(2).getPosition(), filename);
		cs = brd.getCrossSections().get(2);
		approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
        
		filename=scan_path + "bottomsection3.txt";
		readScanBottomCrossSection(brd.getCrossSections().get(3).getGuidePoints(), brd.getCrossSections().get(3).getPosition(), filename);
		cs = brd.getCrossSections().get(3);
		approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
		
		// Read scanned guide points and approximate deck
		
        //		do
        //		{
        brd.getDeckGuidePoints().clear();
        //for(int i=0;i<10;i++)
            placeBoard();
        readScanDeck(brd.getDeckGuidePoints());
        support2.z-=1;
        //		}while(brd.getDeckGuidePoints().get(brd.getDeckGuidePoints().size()-1).y-brd.getBottom().get(4).mPoints[0].y<0.5);
		approximateCurve(brd.getDeck(), brd.getDeckGuidePoints());
        
		filename=scan_path + "decksection1.txt";
		readScanDeckCrossSection(brd.getCrossSections().get(1).getGuidePoints(), brd.getCrossSections().get(1).getPosition(), filename);
		cs = brd.getCrossSections().get(1);
		approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
		
		filename=scan_path + "decksection2.txt";
		readScanDeckCrossSection(brd.getCrossSections().get(2).getGuidePoints(), brd.getCrossSections().get(2).getPosition(), filename);
		cs = brd.getCrossSections().get(2);
		approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
        
		filename=scan_path + "decksection3.txt";
		readScanDeckCrossSection(brd.getCrossSections().get(3).getGuidePoints(), brd.getCrossSections().get(3).getPosition(), filename);
		cs = brd.getCrossSections().get(3);
		approximateCrossSection(cs.getBezierSpline(), cs.getGuidePoints());
		
		repair();
        
	}
	
	public void readGuidePointsBottom(ArrayList<Point2D.Double> guidepointArray)
	{
        
		//Read points
		
		String filename=scan_path + "bottom.txt";
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			while(buf_reader.ready())
			{
                
				String line = buf_reader.readLine ();
                
				int pos=line.indexOf(' ');
				String subline=line.substring(0,pos);
				double helpx=Double.parseDouble(subline);
                //				System.out.println("Read x");
				
				subline=line.substring(pos);
				subline=subline.trim();
				pos=subline.indexOf(' ');
				String subline2=subline.substring(0,pos);
				String subline3=subline.substring(pos);
				double helpy=Double.parseDouble(subline2);
                //				System.out.println("Read y");
				
				subline=subline3.trim();
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
                //				System.out.println("Read z");
                
				//transform point to BoardCAD coordinates
				NurbsPoint p=machine2boardcad_coordinate(new NurbsPoint(helpx,helpy,helpz));
                
				//create guide point
				guidepointArray.add(new Point2D.Double(p.x/10, p.y/10));
			}
		}
		catch (IOException e)
		{
			System.out.println("shapebot.properties error: " + e);
		}
        
        
	}
	
	
	
	public void readScanOutline(ArrayList<Point2D.Double> guidepointArray)
	{
        
		//Read outline points
		String filename=scan_path + "outline.txt";
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			while(buf_reader.ready())
			{
                
				String line = buf_reader.readLine ();
                
				int pos=line.indexOf(' ');
				String subline=line.substring(0,pos);
				double helpx=Double.parseDouble(subline);
                //				System.out.println("Read x");
				
				subline=line.substring(pos);
				subline=subline.trim();
				pos=subline.indexOf(' ');
				String subline2=subline.substring(0,pos);
				String subline3=subline.substring(pos);
				double helpy=Double.parseDouble(subline2);
                //				System.out.println("Read y");
				
				subline=subline3.trim();
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
                //				System.out.println("Read z");
                
				//transform point to BoardCAD coordinates
				NurbsPoint p=machine2boardcad_coordinate(new NurbsPoint(helpx,helpy,helpz));
                
				//create guide point
				guidepointArray.add(new Point2D.Double(p.x/10, -p.z/10));
			}
		}
		catch (IOException e)
		{
			System.out.println("shapebot.properties error: " + e);
		}
		
		//look for smallest and largest x-value
        
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( guidepointArray.get(i).x  < min_x )
			{
				min_x=guidepointArray.get(i).x;
			}
            
			if( guidepointArray.get(i).x  > max_x )
			{
				max_x=guidepointArray.get(i).x;
			}
            
		}
        
		//transform x to 0
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x-=min_x;
		}
		
		max_x=max_x-min_x;
        
	}
    
	public void readScanBottom(ArrayList<Point2D.Double> guidepointArray)
	{
        
		//Read points
		String filename=scan_path + "bottom.txt";
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			while(buf_reader.ready())
			{
                
				String line = buf_reader.readLine ();
                
				int pos=line.indexOf(' ');
				String subline=line.substring(0,pos);
				double helpx=Double.parseDouble(subline);
                //				System.out.println("Read x");
				
				subline=line.substring(pos);
				subline=subline.trim();
				pos=subline.indexOf(' ');
				String subline2=subline.substring(0,pos);
				String subline3=subline.substring(pos);
				double helpy=Double.parseDouble(subline2);
                //				System.out.println("Read y");
				
				subline=subline3.trim();
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
                //				System.out.println("Read z");
                
				//transform point to BoardCAD coordinates
				NurbsPoint p=machine2boardcad_coordinate(new NurbsPoint(helpx,helpy,helpz));
                
				//create guide point
				guidepointArray.add(new Point2D.Double(p.x/10, -p.y/10));
			}
		}
		catch (IOException e)
		{
			System.out.println("shapebot.properties error: " + e);
		}
        
		//find smallest y
		double min_y=10000;
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( guidepointArray.get(i).y  < min_y )
			{
				min_y=guidepointArray.get(i).y;
			}
		}
        
		//transform to 0
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x-=min_x;
			guidepointArray.get(i).y-=min_y;
		}
        
        
	}
	
	public void readScanBottomCrossSection(ArrayList<Point2D.Double> guidepointArray, double position, String filename)
	{
        
		//Read outline points
		
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			while(buf_reader.ready())
			{
                
				String line = buf_reader.readLine ();
                
				int pos=line.indexOf(' ');
				String subline=line.substring(0,pos);
				double helpx=Double.parseDouble(subline);
                //				System.out.println("Read x");
				
				subline=line.substring(pos);
				subline=subline.trim();
				pos=subline.indexOf(' ');
				String subline2=subline.substring(0,pos);
				String subline3=subline.substring(pos);
				double helpy=Double.parseDouble(subline2);
                //				System.out.println("Read y");
				
				subline=subline3.trim();
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
                //				System.out.println("Read z");
                
				//transform point to BoardCAD coordinates
				NurbsPoint p=machine2boardcad_coordinate(new NurbsPoint(helpx,helpy,helpz));
                
				//create guide point
				guidepointArray.add(new Point2D.Double(-p.z/10, -p.y/10));
			}
		}
		catch (IOException e)
		{
			System.out.println("shapebot.properties error: " + e);
		}
        
		//find smallest x
		double min_x2=10000;
		double max_x2=0;
		double min_y=10000;
		int index2=0;
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( guidepointArray.get(i).x  < min_x2 )
			{
				min_x2=guidepointArray.get(i).x;
				min_y=guidepointArray.get(i).y;
				index2=i;
			}
			if( guidepointArray.get(i).x  > max_x2 )
			{
				max_x2=guidepointArray.get(i).x;
			}
		}
        
		//transform to 0 and scale to outline
        
		double width=brd.getWidthAtPos(position)/2;
        
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x*=width/max_x2;
			guidepointArray.get(i).y-=min_y;
		}
        
		
		
        
        
	}
	
	
	public void readScanDeckCrossSection(ArrayList<Point2D.Double> guidepointArray, double position, String filename)
	{
        
		//Read outline points
		
		int apex=guidepointArray.size();
		
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			while(buf_reader.ready())
			{
                
				String line = buf_reader.readLine ();
                
				int pos=line.indexOf(' ');
				String subline=line.substring(0,pos);
				double helpx=Double.parseDouble(subline);
                //				System.out.println("Read x");
				
				subline=line.substring(pos);
				subline=subline.trim();
				pos=subline.indexOf(' ');
				String subline2=subline.substring(0,pos);
				String subline3=subline.substring(pos);
				double helpy=Double.parseDouble(subline2);
                //				System.out.println("Read y");
				
				subline=subline3.trim();
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
                //				System.out.println("Read z");
                
				//transform point to BoardCAD coordinates
				NurbsPoint p=machine2boardcad_coordinate(new NurbsPoint(helpx,helpy,helpz));
                
				//create guide point
				guidepointArray.add(new Point2D.Double(-p.z/10, p.y/10));
			}
		}
		catch (IOException e)
		{
			System.out.println("shapebot.properties error: " + e);
		}
        
        
		//find smallest x
		double max_x2=0;
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
            
			if( guidepointArray.get(i).x  > max_x2 )
			{
				max_x2=guidepointArray.get(i).x;
			}
		}
        
		//transform to thickness and scale to width
		NurbsPoint s1=machine2boardcad_coordinate(support1);
		double width=brd.getWidthAtPos(position)/2;
		
		for(int i = apex; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x*=width/max_x2;
			guidepointArray.get(i).y-=(brd.getRockerAtPos(position)+s1.y/10);
		}
        
	}
	
	
    
	public void readScanDeck(ArrayList<Point2D.Double> guidepointArray)
	{
		//Read points
		String filename=scan_path + "deck.txt";
		File file = new File (filename);
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
            
			while(buf_reader.ready())
			{
                
				String line = buf_reader.readLine ();
                
				int pos=line.indexOf(' ');
				String subline=line.substring(0,pos);
				double helpx=Double.parseDouble(subline);
                //				System.out.println("Read x");
				
				subline=line.substring(pos);
				subline=subline.trim();
				pos=subline.indexOf(' ');
				String subline2=subline.substring(0,pos);
				String subline3=subline.substring(pos);
				double helpy=Double.parseDouble(subline2);
                //				System.out.println("Read y");
				
				subline=subline3.trim();
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
                //				System.out.println("Read z");
                
				//transform point to BoardCAD coordinates
				NurbsPoint p=machine2boardcad_coordinate(new NurbsPoint(helpx,helpy,helpz));
                
				//create guide point
				guidepointArray.add(new Point2D.Double(p.x/10, p.y/10));
			}
		}
		catch (IOException e)
		{
			System.out.println("shapebot.properties error: " + e);
		}
        
        
		NurbsPoint s1=machine2boardcad_coordinate(support1);
		//transform to 0
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x-=min_x;
			guidepointArray.get(i).y-=s1.y/10;
		}
        
        
        
	}
	
	
	
	public void placeBoard()
	{
        
		// Convert supports from machine to BoardCAD coordinates
		NurbsPoint s1=machine2boardcad_coordinate(support1);
		NurbsPoint s2=machine2boardcad_coordinate(support2);
        
		s1.x=s1.x-10*min_x;
		s2.x=s2.x-10*min_x;
        
		// find coordinates
		NurbsPoint p1, p3, p4, p_min;
        
		double y_dist = 10000;
		double y_dist2 = 10000;
		
		p_min = new NurbsPoint(0, 0, 0);
		
		double py;
		double x, y, z;
		NurbsPoint p;
		
        
		for(int angle=0; angle<360; angle=angle+10)
		{
			x=(s1.x+support1_radius*Math.cos(angle*3.14/180));
			z=-(s1.z+support1_radius*Math.sin(angle*3.14/180));
            
			if(z/10<brd.getWidthAtPos(x/10)/2)
			{
                //				y=brd.getBottomAtPos(x/10, z/10)*10;
				p=brd.getBottom3D(x,z);
				y=p.y;
                
				if( (y-s1.y) < y_dist)
				{
					y_dist=y-s1.y;
					p_min=new NurbsPoint(x,y,z);
				}
			}
		}
        
		p3=new NurbsPoint(p_min.x, p_min.y, p_min.z);
        
		for(int angle=0; angle<360; angle=angle+10)
		{
			x=(s2.x+support2_radius*Math.cos(angle*3.14/180));
			z=-(s2.z+support2_radius*Math.sin(angle*3.14/180));
			
			if(z/10<brd.getWidthAtPos(x/10)/2)
			{
                //				y=brd.getBottomAtPos(x/10, z/10)*10;
				p=brd.getBottom3D(x,z);
				y=p.y;
                
				if( (y-s2.y) < y_dist2)
				{
					y_dist2=y-s2.y;
					p_min=new NurbsPoint(x,y,z);
				}
			}
		}
		
		p4=new NurbsPoint(p_min.x, p_min.y, p_min.z);
        
		double theta=Math.atan2((p4.y-p3.y),(p4.x-p3.x));
		double theta2=Math.atan2((s2.y-s1.y),(s2.x-s1.x));
		
		theta=theta-theta2;
        
        //		System.out.println("p3.x=" + p3.x + " p4.x=" + p4.x + "p3.y=" + p3.y + " p4.y=" + p4.y +" theta=" + theta);
		
        
		//Create transformed surfaces
		
		
		BezierSpline knotArray=brd.getBottom();
		ArrayList<Point2D.Double> guidepointArray=brd.getBottomGuidePoints();
		
		double tempx, tempy;
		BezierKnot knot;
		
		//translate knots
		for(int i = 0; i < knotArray.getNrOfControlPoints(); i++)
		{
			knot=knotArray.getControlPoint(i);
			for(int j = 0; j < 3; j++)
			{
				knot.getPoints()[j].x-=p3.x/10;
				knot.getPoints()[j].y-=p3.y/10;
                knot.onChange();
			}
		}
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x-=p3.x/10;
			guidepointArray.get(i).y-=p3.y/10;
		}
        
		//rotate knots
		
		for(int i = 0; i < knotArray.getNrOfControlPoints(); i++)
		{
			knot=knotArray.getControlPoint(i);
            
			for(int j = 0; j < 3; j++)
			{
				tempx=knot.getPoints()[j].x*Math.cos(-theta)+knot.getPoints()[j].y*(-Math.sin(-theta));
				tempy=knot.getPoints()[j].x*Math.sin(-theta)+knot.getPoints()[j].y*Math.cos(-theta);
				knot.getPoints()[j].x=tempx;
				knot.getPoints()[j].y=tempy;
                knot.onChange();
			}
		}
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			tempx=guidepointArray.get(i).x*Math.cos(-theta)+guidepointArray.get(i).y*(-Math.sin(-theta));
			tempy=guidepointArray.get(i).x*Math.sin(-theta)+guidepointArray.get(i).y*Math.cos(-theta);
			guidepointArray.get(i).x=tempx;
			guidepointArray.get(i).y=tempy;
		}
        
		//translate knots
		for(int i = 0; i < knotArray.getNrOfControlPoints(); i++)
		{
			knot=knotArray.getControlPoint(i);
            
			for(int j = 0; j < 3; j++)
			{
				knot.getPoints()[j].x+=p3.x/10;
                knot.onChange();
				//knot.mPoints[j].y+=s1.y/10;
			}
		}
        
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			guidepointArray.get(i).x+=p3.x/10;
		}
        
        
        System.out.println("Finished placing board");
		
	}
    
    
	public void approximateCurve(BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	{
		double err=0.0;
        
		
		//find guide point closest to knot and move knot to that point.
		
		double min_dist, y_diff, x_diff;
		int closest_point;
		
		for(int n=0;n<=4;n++)
		{
			min_dist=10000;
			closest_point=0;
			
			for(int i = 0; i < guidepointArray.size(); i++)
			{
				if( Math.abs( knotArray.getControlPoint(n).getPoints()[0].x - guidepointArray.get(i).x ) < min_dist )
				{
					min_dist=Math.abs( knotArray.getControlPoint(n).getPoints()[0].x - guidepointArray.get(i).x );
					closest_point=i;
				}
			}
            
			y_diff=guidepointArray.get(closest_point).y-knotArray.getControlPoint(n).getPoints()[0].y;
			x_diff=guidepointArray.get(closest_point).x-knotArray.getControlPoint(n).getPoints()[0].x;
            
			for(int i=0;i<3;i++)
			{
				knotArray.getControlPoint(n).getPoints()[i].y+=y_diff;
				knotArray.getControlPoint(n).getPoints()[i].x+=x_diff;
			}
            knotArray.getControlPoint(n).onChange();
		}
		
		for(int n=0; n<100;n++)
		{
			for(int i=0; i<10;i++)
				minimizeErrorY(knotArray, guidepointArray);
			
			for(int i=0; i<1;i++)
				minimizeErrorX(knotArray, guidepointArray);
		}

        
	}
	
	private void minimizeErrorY(BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	{
        
		double err=0.0;
        
		//find guide points between knot 1 and 2 and minimize error
		
		ArrayList<Point2D.Double> gpArray=new ArrayList<Point2D.Double>();
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( knotArray.getControlPoint(1).getPoints()[0].x < guidepointArray.get(i).x  && knotArray.getControlPoint(2).getPoints()[1].x > guidepointArray.get(i).x )
			{
				gpArray.add(guidepointArray.get(i));
                System.out.println("Adding guide point: (" + guidepointArray.get(i).x + ", " + guidepointArray.get(i).y + "  y=" + knotArray.getValueAt(guidepointArray.get(i).x));
			}
		}
        
        
		//find average distance between curve and guide points
		err=0.0;
        
		for(int i = 0; i < gpArray.size(); i++)
		{
			err += knotArray.getValueAt(gpArray.get(i).x) - gpArray.get(i).y;
			
		}
        System.out.println("Average error 1-2: " + (err/gpArray.size()));
        
		knotArray.getControlPoint(1).getPoints()[2].y-=err/gpArray.size();
        knotArray.getControlPoint(1).onChange();
//		knotArray.getControlPoint(1).setTangentToNext(knotArray.getControlPoint(1).getPoints()[2].x,knotArray.getControlPoint(1).getPoints()[2].y-err/gpArray.size());
        
        
		//find guide points between knot 2 and 3 and minimize error
		
		gpArray=new ArrayList<Point2D.Double>();
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( knotArray.getControlPoint(2).getPoints()[2].x < guidepointArray.get(i).x  && knotArray.getControlPoint(3).getPoints()[0].x > guidepointArray.get(i).x )
			{
				gpArray.add(guidepointArray.get(i));
			}
		}
        
        
		//find average distance between curve and guide points
		err=0.0;
        
		for(int i = 0; i < gpArray.size(); i++)
		{
			err += knotArray.getValueAt(gpArray.get(i).x) - gpArray.get(i).y;
		}
        
        System.out.println("Average error 2-3: " + (err/gpArray.size()));
		
		knotArray.getControlPoint(3).getPoints()[1].y-=err/gpArray.size();
        knotArray.getControlPoint(3).onChange();
//		knotArray.getControlPoint(3).setTangentToPrev(knotArray.getControlPoint(3).getPoints()[1].x,knotArray.getControlPoint(3).getPoints()[1].y-err/gpArray.size());
        
	}
    
    
	private void minimizeErrorX(BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	{
        
		double err=0.0;
        
		//find guide points between knot 1 and 2 and minimize error
		
		ArrayList<Point2D.Double> gpArray=new ArrayList<Point2D.Double>();
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( knotArray.getControlPoint(1).getPoints()[0].x < guidepointArray.get(i).x  && knotArray.getControlPoint(1).getPoints()[2].x > guidepointArray.get(i).x )
			{
				gpArray.add(guidepointArray.get(i));
			}
		}
        
		if(gpArray.size()<3)
			return;
        
		//find average distance between curve and guide points
		err=0.0;
        
		for(int i = 0; i < gpArray.size(); i++)
		{
			err += knotArray.getValueAt(gpArray.get(i).x) - gpArray.get(i).y;
		}
        
		if(knotArray.getControlPoint(1).getPoints()[0].y<knotArray.getControlPoint(2).getPoints()[0].y)
		{
			knotArray.getControlPoint(1).getPoints()[2].x+=err/gpArray.size();
            knotArray.getControlPoint(1).onChange();
//            knotArray.getControlPoint(1).setTangentToNext(knotArray.getControlPoint(1).getPoints()[2].x+err/gpArray.size(),knotArray.getControlPoint(1).getPoints()[2].y);
		}
		else
		{
			knotArray.getControlPoint(1).getPoints()[2].x-=err/gpArray.size();
            knotArray.getControlPoint(1).onChange();
//            knotArray.getControlPoint(1).setTangentToNext(knotArray.getControlPoint(1).getPoints()[2].x-err/gpArray.size(),knotArray.getControlPoint(1).getPoints()[2].y);
		}
		
        
        
		//find guide points between knot 2 and 3 and minimize error
		
		gpArray=new ArrayList<Point2D.Double>();
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( knotArray.getControlPoint(3).getPoints()[1].x < guidepointArray.get(i).x  && knotArray.getControlPoint(3).getPoints()[0].x > guidepointArray.get(i).x )
			{
				gpArray.add(guidepointArray.get(i));
			}
		}
        
		if(gpArray.size()<2)
			return;
        
		//find average distance between curve and guide points
		err=0.0;
        
		for(int i = 0; i < gpArray.size(); i++)
		{
			err += knotArray.getValueAt(gpArray.get(i).x) - gpArray.get(i).y;
		}
        
        
		if(knotArray.getControlPoint(3).getPoints()[0].y<knotArray.getControlPoint(2).getPoints()[0].y)
		{
			knotArray.getControlPoint(3).getPoints()[1].x-=err/gpArray.size();
            knotArray.getControlPoint(3).onChange();
//            knotArray.getControlPoint(3).setTangentToNext(knotArray.getControlPoint(3).getPoints()[1].x-err/gpArray.size(),knotArray.getControlPoint(3).getPoints()[1].y);
		}
		else
		{
			knotArray.getControlPoint(3).getPoints()[1].x+=err/gpArray.size();
            knotArray.getControlPoint(3).onChange();
//            knotArray.getControlPoint(3).setTangentToNext(knotArray.getControlPoint(3).getPoints()[1].x+err/gpArray.size(),knotArray.getControlPoint(3).getPoints()[1].y);
		}
        
	}
	
	
	public void approximateCrossSection(BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	{
		double err=0.0;
		int index_max_x=0;
		double max_x=0;
		double max_x2=0;
		double index_y=0;
		double index_y2=0;
		
		//read guide points for cross section
		
        //		System.out.println("Reading guide points...");
		
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if(guidepointArray.get(i).x>max_x)
			{
				max_x2=max_x;
				index_y2=index_y;
				max_x=guidepointArray.get(i).x;
				index_y=guidepointArray.get(i).y;
				index_max_x=i;
			}
			else if(guidepointArray.get(i).x>max_x2)
			{
				max_x2=guidepointArray.get(i).x;
				index_y2=guidepointArray.get(i).y;
			}
			
		}
        System.out.println("index_y= " + index_y + " index_y2= " + index_y2);
		//separate guide points for deck and bottom
		
		ArrayList<Point2D.Double> gpDeckArray=new ArrayList<Point2D.Double>();
		ArrayList<Point2D.Double> gpBottomArray=new ArrayList<Point2D.Double>();
        
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if(guidepointArray.get(i).y>index_y)
			{
				gpDeckArray.add(guidepointArray.get(i));
			}
			else
			{
				gpBottomArray.add(guidepointArray.get(i));
			}
		}
        
		double min_dist, y_diff;
		int closest_point;
		
		//set outline point and scale the rest...
		
		double scale_x=max_x/knotArray.getControlPoint(2).getPoints()[0].x;
        
		y_diff=(index_y+index_y2)/2-knotArray.getControlPoint(2).getPoints()[0].y;
		
		for(int i=0;i<3;i++)
		{
			knotArray.getControlPoint(2).getPoints()[i].y+=y_diff;
            knotArray.getControlPoint(2).onChange();
		}
		
		
		for(int n=0;n<knotArray.getNrOfControlPoints();n++)
		{
			for(int i=0;i<3;i++)
			{
				knotArray.getControlPoint(n).getPoints()[i].x*=scale_x;
                knotArray.getControlPoint(n).onChange();
			}
		}
		//set bottom points
		
		for(int n=0;n<=1;n++)
		{
			min_dist=10000;
			closest_point=0;
			
			for(int i = 0; i < gpBottomArray.size(); i++)
			{
				if( Math.abs( knotArray.getControlPoint(n).getPoints()[0].x - gpBottomArray.get(i).x ) < min_dist )
				{
					min_dist=Math.abs( knotArray.getControlPoint(n).getPoints()[0].x - gpBottomArray.get(i).x );
					closest_point=i;
				}
			}
            
			y_diff=gpBottomArray.get(closest_point).y-knotArray.getControlPoint(n).getPoints()[0].y;
            
			for(int i=0;i<3;i++)
			{
				knotArray.getControlPoint(n).getPoints()[i].y+=y_diff;
                knotArray.getControlPoint(n).onChange();
			}
		}
        
		//set deck points
		if(gpDeckArray.size()>0)
		{
			for(int n=3;n<=4;n++)
			{
				min_dist=10000;
				closest_point=0;
                
				for(int i = 0; i < gpDeckArray.size(); i++)
				{
					if( Math.abs( knotArray.getControlPoint(n).getPoints()[0].x - gpDeckArray.get(i).x ) < min_dist )
					{
						min_dist=Math.abs( knotArray.getControlPoint(n).getPoints()[0].x - gpDeckArray.get(i).x );
						closest_point=i;
					}
				}
                
				y_diff=gpDeckArray.get(closest_point).y-knotArray.getControlPoint(n).getPoints()[0].y;
                
				for(int i=0;i<3;i++)
				{
					knotArray.getControlPoint(n).getPoints()[i].y+=y_diff;
                    knotArray.getControlPoint(n).onChange();
				}
			}
            
            
			for(int n=0; n<100;n++)
			{
				for(int i=0; i<10;i++)
					minimizeErrorYCrossSectionDeck(knotArray, gpDeckArray);
                
				for(int i=0; i<1;i++)
					minimizeErrorXCrossSectionDeck(knotArray, gpDeckArray);
			}
            
			
		}
		
	}
	
    
	private void minimizeErrorYCrossSectionDeck(BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	{
        
		double err=0.0;
        
		//find guide points between knot 3 and 4 and minimize error
		
		ArrayList<Point2D.Double> gpArray=new ArrayList<Point2D.Double>();
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( knotArray.getControlPoint(3).getPoints()[0].x < guidepointArray.get(i).x  && knotArray.getControlPoint(4).getPoints()[1].x > guidepointArray.get(i).x )
			{
				gpArray.add(guidepointArray.get(i));
			}
		}
        
        
		//find average distance between curve and guide points
		err=0.0;
        
		for(int i = 0; i < gpArray.size(); i++)
		{
			err += knotArray.getValueAt(gpArray.get(i).x) - gpArray.get(i).y;
			
		}
		
		
		if(gpArray.size()>0)
        {
			knotArray.getControlPoint(3).getPoints()[2].y-=err/gpArray.size();
            knotArray.getControlPoint(3).onChange();
		}
        
        
	}
    
	private void minimizeErrorXCrossSectionDeck(BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	{
        
		double err=0.0;
        
		//find guide points between knot 3 and 4 and minimize error
		
		ArrayList<Point2D.Double> gpArray=new ArrayList<Point2D.Double>();
		
		for(int i = 0; i < guidepointArray.size(); i++)
		{
			if( knotArray.getControlPoint(3).getPoints()[0].x < guidepointArray.get(i).x  && knotArray.getControlPoint(4).getPoints()[2].x > guidepointArray.get(i).x )
			{
				gpArray.add(guidepointArray.get(i));
			}
		}
        
		if(gpArray.size()<3)
			return;
        
		//find average distance between curve and guide points
		err=0.0;
        
		for(int i = 0; i < gpArray.size(); i++)
		{
			err += knotArray.getValueAt(gpArray.get(i).x) - gpArray.get(i).y;
		}
        
		knotArray.getControlPoint(1).getPoints()[2].x+=err/gpArray.size();
        knotArray.getControlPoint(1).onChange();
	}
	
	public void approximateCurrentView()
	{
		ArrayList<Point2D.Double> gpArray=BoardCAD.getInstance().getSelectedEdit().getGuidePoints();
		BezierSpline[] bs=BoardCAD.getInstance().getSelectedEdit().getActiveBezierSplines(brd);
        
		final JTabbedPane mTabbedPane = BoardCAD.getInstance().getmTabbedPane();
		if (mTabbedPane.getSelectedIndex() == 5 || (mTabbedPane.getSelectedIndex() == 2 && BoardCAD.getInstance().getFourViewName() == "QuadViewCrossSection" ))
		{
			approximateCrossSection(bs[0], gpArray);
		}
		else
		{
			approximateCurve(bs[0], gpArray);
		}
        
	}
    
    
    
	/**
	 * Removes the guide points
	 */
	public void close_guide_points()
	{
		nr_of_help_points=0;
	}
    
	/**
	 * Reads a number of guide points from the file "scan.txt"
	 *
	 * Note: This method should be moved to the scan interface
	 */
	public void read_guide_points()
	{
		int ns=17;
		int np=15;
		NurbsPoint[][] bottom_points;
		NurbsPoint[][] deck_points;
		NurbsPoint p,p2;
        
        //		BoardMachine board_machine=new BoardMachine();
		//read_machine_data();
        //		String scan_path=board_machine.scan_path;
        //		NurbsPoint scan_coord=board_machine.scan_coord;
		
		String filename=scan_path+"scan.txt";
		File file = new File (filename);
		System.out.println("Opening file");
        
		try {
            
            
			// Create a FileReader and then wrap it with BufferedReader.
            
			FileReader file_reader = new FileReader (file);
			BufferedReader buf_reader = new BufferedReader (file_reader);
			
			do
			{
                
				System.out.println("Reading point");
				String line = buf_reader.readLine ();
                
				if (line == null) break;
                
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
				pos=subline.indexOf(' ');
				double helpz=Double.parseDouble(subline.substring(0,pos));
				
				//transformera
				helpx=helpx-scan_coord.x; //(scan_coord.x-(support1.x-endSupportPosition[0]));
				helpy=(-scan_coord.y)+helpy;
				helpz=-scan_coord.z+helpz;
                
                
				help_points[nr_of_help_points]=new NurbsPoint(helpx, helpz, -helpy);
				nr_of_help_points++;
                
			}while(true);
			
            
		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}
        
        
	}
    
    
	
	/**
	 * Reads the config file for the machine.
	 */
	private void read_machine_data()
	{
		BoardMachine.read_machine_data();
        
		machineCoordinates=BoardMachine.machineCoordinates;
		endSupportPosition=BoardMachine.endSupportPosition;
		support1=BoardMachine.support1;
		support1_radius=BoardMachine.support1_radius;
		support2=BoardMachine.support2;
		support2_radius=BoardMachine.support2_radius;	
		scan_coord=BoardMachine.scan_coord;
		//scan_comp=BoardMachine.scan_comp;	
		scan_path=BoardMachine.scan_path;
		
		support1.x=support1.x-endSupportPosition[0]+scan_coord.x;
		support1.y=support1.y-endSupportPosition[1]+scan_coord.y;
		support1.z=support1.z-endSupportPosition[2]+scan_coord.z;
		
		support2.x=support2.x-endSupportPosition[0]+scan_coord.x;
		support2.y=support2.y-endSupportPosition[1]+scan_coord.y;
		support2.z=support2.z-endSupportPosition[2]+scan_coord.z;
        
		endSupportPosition=new double[3];
		endSupportPosition[0]=scan_coord.x;
		endSupportPosition[1]=scan_coord.y;
		endSupportPosition[2]=scan_coord.z;
		
		
	}
    
    
	/**
	 * Returns an array with the values for the transform array
	 * 
	 * @param axis
	 * @return array with transform values for the given axis
	 */
	private double[] getAxisArray(String axis) {
		double[] array = { 0, 0, 0 };
		if (axis.equals("X"))
			array[0] = 1;
		else if (axis.equals("-X"))
			array[0] = -1;
		else if (axis.equals("Y"))
			array[1] = 1;
		else if (axis.equals("-Y"))
			array[1] = -1;
		else if (axis.equals("Z"))
			array[2] = 1;
		else if (axis.equals("-Z"))
			array[2] = -1;
		return array;
        
	}
	
	private NurbsPoint machine2boardcad_coordinate(NurbsPoint p)
	{
        
		return new NurbsPoint(machineCoordinates[0][0]*(p.x-endSupportPosition[0])+machineCoordinates[1][0]*(p.y-endSupportPosition[1])+machineCoordinates[2][0]*(p.z-endSupportPosition[2]),
                              machineCoordinates[0][1]*(p.x-endSupportPosition[0])+machineCoordinates[1][1]*(p.y-endSupportPosition[1])+machineCoordinates[2][1]*(p.z-endSupportPosition[2]),
                              machineCoordinates[0][2]*(p.x-endSupportPosition[0])+machineCoordinates[1][2]*(p.y-endSupportPosition[1])+machineCoordinates[2][2]*(p.z-endSupportPosition[2]));
	}
	
	private void repair()
	{
        
		//translate rocker and guide points to 0
		
		double y=brd.getBottom().getControlPoint(2).getPoints()[0].y;
		
		for(int i=0; i<brd.getBottom().getNrOfControlPoints();i++)
		{
			for(int j=0;j<3;j++)
			{
				brd.getBottom().getControlPoint(i).getPoints()[j].y-=y;
			}
		}		
        
		for(int i=0; i<brd.getBottomGuidePoints().size();i++)
		{
			brd.getBottomGuidePoints().get(i).y-=y;
		}
        
        
		for(int i=0; i<brd.getDeck().getNrOfControlPoints();i++)
		{
			for(int j=0;j<3;j++)
			{
				brd.getDeck().getControlPoint(i).getPoints()[j].y-=y;
			}
		}		
        
		for(int i=0; i<brd.getDeckGuidePoints().size();i++)
		{
			brd.getDeckGuidePoints().get(i).y-=y;
		}
        
		//make sure board starts at x=0
        
		brd.getBottom().getControlPoint(0).getPoints()[0].x=0.0;
        
        
	}
    
}
