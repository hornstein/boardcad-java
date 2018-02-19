package boardcam.cutters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.io.PrintStream;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.PickSegment;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.picking.Intersect;

import cadcore.NurbsSurface;
import cadcore.NurbsPoint;

import board.AbstractBoard;
import boardcam.MachineConfig;
import boardcad.settings.Settings;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcad.i18n.LanguageResource;


/**
 * This class reads the cutter geometry from an STL-file
 * allowing for general cutters to be designed in an
 * external CAD-program and used in BoardCAD.
 */

public class STLCutter extends AbstractCutter
{
	private final String CUTTER_SCALE_X = "CutterScaleX";
	private final String CUTTER_SCALE_Y = "CutterScaleY";
	private final String CUTTER_SCALE_Z = "CutterScaleZ";
	
	private final String CUTTER_STL_FILENAME = "STLFilename";

	private Point3d[] cutting_point;
	private Vector3d[] cutting_normal;
	private Point3d[] transformed_cutting_point;
	private Vector3d[] transformed_cutting_normal;

	private Point3d[] collision_point;
	private Point3d[] transformed_collision_point;
	
	private double offset_x;
	private double offset_y;
	private double offset_z;	
	private int n;
	private int cn;

//	private Point3d[][] triangle;
	private Point3d[][][][]	triangle;
	private int triangle_n;
	private int triangle_n2;

	public STLCutter()
	{	
	}
	
	public STLCutter(MachineConfig config)
	{		
		final Settings cutterSettings = config.addCategory(LanguageResource.getString("CUTTERCATEGORY_STR"));
		cutterSettings.clear();
		SettingChangedCallback scaleChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{		
				scale(cutterSettings.getDouble(CUTTER_SCALE_X), cutterSettings.getDouble(CUTTER_SCALE_Y), cutterSettings.getDouble(CUTTER_SCALE_Z));
				update3DModel();
			}	
		};
		cutterSettings.addObject(CUTTER_SCALE_X,  new Double(1.0), LanguageResource.getString("CUTTERSCALEX_STR"), scaleChanged);
		cutterSettings.addObject(CUTTER_SCALE_Y,  new Double(1.0), LanguageResource.getString("CUTTERSCALEY_STR"), scaleChanged);
		cutterSettings.addObject(CUTTER_SCALE_Z,  new Double(1.0), LanguageResource.getString("CUTTERSCALEZ_STR"), scaleChanged);

		SettingChangedCallback filenameChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object object)
			{		
				try{
					loadCutter(cutterSettings.getFileName(CUTTER_STL_FILENAME));
					update3DModel();
				}
				catch(Exception e)
				{
					
				}
			}	
		};
		cutterSettings.addFileName(CUTTER_STL_FILENAME, "", LanguageResource.getString("STL_FILENAME_STR"), filenameChanged);
	}

	public void init(String toolname)
	{

/*		try {
			createFlatCutter(new PrintStream(new File("flat_cutter.stl")), 10.0, 25.3);
		} catch (Exception e) {
		}
*/		
		loadCutter(toolname);
		
		transformed_cutting_point=new Point3d[n];
		transformed_cutting_normal=new Vector3d[n];
				
		for(int i=0;i<n-1;i++)
		{
			transformed_cutting_point[i]=new Point3d(cutting_point[i].x, cutting_point[i].y, cutting_point[i].z);
			transformed_cutting_normal[i]=new Vector3d(cutting_normal[i].x, cutting_normal[i].y, cutting_normal[i].z);
		}
		
	}
	

	public void createCollisionCutter()
	{

		cn=n;
		
		collision_point=new Point3d[cn];
		transformed_collision_point=new Point3d[cn];
		
		double myscale=0.9;
		double myztrans=1;
				
		for(int i=0;i<cn-1;i++)
		{
			collision_point[i]=new Point3d(cutting_point[i].x*myscale, cutting_point[i].y*myscale, cutting_point[i].z+myztrans);
			transformed_collision_point[i]=new Point3d(cutting_point[i].x*myscale, cutting_point[i].y*myscale, cutting_point[i].z+myztrans);
		}
		
	}
	
	public void createCollisionCutter(String toolname, double scale_x, double scale_y, double scale_z, double myztrans)
	{

		loadCollisionCutter(toolname);

		transformed_collision_point=new Point3d[cn];
		
		for(int i=0;i<cn-1;i++)
		{
			collision_point[i]=new Point3d(collision_point[i].x*scale_x, collision_point[i].y*scale_y, collision_point[i].z*scale_z+myztrans);
			transformed_collision_point[i]=new Point3d(collision_point[i].x*scale_x, collision_point[i].y*scale_y, collision_point[i].z*scale_z+myztrans);
		}
		
	}


	
	public void scale(double xscale, double yscale, double zscale)
	{		
		for(int i=0;i<n-1;i++)
		{
			cutting_point[i].x=cutting_point[i].x*xscale;
			cutting_point[i].y=cutting_point[i].y*yscale;
			cutting_point[i].z=cutting_point[i].z*zscale;

			transformed_cutting_point[i].x=transformed_cutting_point[i].x*xscale;
			transformed_cutting_point[i].y=transformed_cutting_point[i].y*yscale;
			transformed_cutting_point[i].z=transformed_cutting_point[i].z*zscale;
		}
	}

	public void setRotationCenter(double x, double y, double z)
	{
		offset_x=x;
		offset_y=y;
		offset_z=z;
	}
	
	public void setRotation(double angle)
	{
		for(int i=0;i<n-1;i++)
		{
			transformed_cutting_point[i]=new Point3d(cutting_point[i].x, cutting_point[i].y, cutting_point[i].z);
			transformed_cutting_normal[i]=new Vector3d(cutting_normal[i].x, cutting_normal[i].y, cutting_normal[i].z);
		}
		
		for(int i=0;i<cn-1;i++)
		{
			transformed_collision_point[i]=new Point3d(collision_point[i].x, collision_point[i].y, collision_point[i].z);
		}
		
		translate(-offset_x, -offset_y, -offset_z);
		rotate4(angle);
		translate(offset_x, offset_y, offset_z);
	}

	public void setRotation(double angle4, double angle5)
	{
		for(int i=0;i<n-1;i++)
		{
			transformed_cutting_point[i]=new Point3d(cutting_point[i].x, cutting_point[i].y, cutting_point[i].z);
			transformed_cutting_normal[i]=new Vector3d(cutting_normal[i].x, cutting_normal[i].y, cutting_normal[i].z);
		}
		
		for(int i=0;i<cn-1;i++)
		{
			transformed_collision_point[i]=new Point3d(collision_point[i].x, collision_point[i].y, collision_point[i].z);
		}
		
		translate(-offset_x, -offset_y, -offset_z);
		rotate4(angle4);
		rotate5(angle5);
		translate(offset_x, offset_y, offset_z);
	}
	
	
	/**
	* Rotates the tool around fourth (x) axis 
	*
	* @param theta	The rotation angle in degrees
	*/	
	private void rotate4(double theta)
	{

		//rotate board
		
		double[][] m = {{1.0, 0.0, 0.0},
				{0.0, Math.cos(-theta*3.1415/180.0), -Math.sin(-theta*3.1415/180.0)},
		                {0.0, Math.sin(-theta*3.1415/180.0), Math.cos(-theta*3.1415/180.0)}}; 



		double[] t={0.0 , 0.0, 0.0};

		
		transform(m,t);
	}
	
	/**
	* Rotates the tool around fifth (z) axis
	*
	* @param theta	The rotation angle in degrees
	*/	
	private void rotate5(double theta)
	{

		//rotate board
		
		double[][] m = {{Math.cos(theta*3.1415/180.0), Math.sin(theta*3.1415/180.0), 0.0},
				{-Math.sin(theta*3.1415/180.0), Math.cos(theta*3.1415/180.0), 0.0},
		                {0.0, 0.0, 1.0}}; 

		double[] t={0.0 , 0.0, 0.0};

		
		transform(m,t);
	}

	private void translate(double dx, double dy, double dz)
	{
	
		double[][] m = {{1.0, 0.0, 0.0},
		                {0.0, 1.0, 0.0},
		                {0.0, 0.0, 1.0}}; 

		double[] t={dx, dy, dz};

		transform(m,t);
	}
	
	private void transform(double[][] m, double[] t)
	{
		
//		transformed_cutting_point=new Point3d[n];
//		transformed_cutting_normal=new Vector3d[n];
		double tx,ty,tz;				
		
		for(int i=0;i<n-1;i++)
		{
//			transformed_cutting_point[i]=new Point3d(0.0, 0.0, 0.0);
			tx=m[0][0]*transformed_cutting_point[i].x+m[0][1]*transformed_cutting_point[i].y+m[0][2]*transformed_cutting_point[i].z+t[0];
			ty=m[1][0]*transformed_cutting_point[i].x+m[1][1]*transformed_cutting_point[i].y+m[1][2]*transformed_cutting_point[i].z+t[1];
			tz=m[2][0]*transformed_cutting_point[i].x+m[2][1]*transformed_cutting_point[i].y+m[2][2]*transformed_cutting_point[i].z+t[2];
			transformed_cutting_point[i]=new Point3d(tx, ty, tz);

//			transformed_cutting_normal[i]=new Vector3d(0.0, 0.0, 0.0);
			tx=m[0][0]*transformed_cutting_normal[i].x+m[0][1]*transformed_cutting_normal[i].y+m[0][2]*transformed_cutting_normal[i].z;
			ty=m[1][0]*transformed_cutting_normal[i].x+m[1][1]*transformed_cutting_normal[i].y+m[1][2]*transformed_cutting_normal[i].z;
			tz=m[2][0]*transformed_cutting_normal[i].x+m[2][1]*transformed_cutting_normal[i].y+m[2][2]*transformed_cutting_normal[i].z;
			transformed_cutting_normal[i]=new Vector3d(tx, ty, tz);

		}


		for(int i=0;i<cn-1;i++)
		{
			tx=m[0][0]*transformed_collision_point[i].x+m[0][1]*transformed_collision_point[i].y+m[0][2]*transformed_collision_point[i].z+t[0];
			ty=m[1][0]*transformed_collision_point[i].x+m[1][1]*transformed_collision_point[i].y+m[1][2]*transformed_collision_point[i].z+t[1];
			tz=m[2][0]*transformed_collision_point[i].x+m[2][1]*transformed_collision_point[i].y+m[2][2]*transformed_collision_point[i].z+t[2];
			transformed_collision_point[i]=new Point3d(tx, ty, tz);
		}
		
	}

	
		
	public double[] calcOffset(Point3d pos, Vector3d normal, AbstractBoard board)
	{	
		// find the cutting point with normal pointing against the board normal
		// if ambiguous choose cutting point closest to tool tip (0,0,0)
		
		double min_normal=1000;
		double min_distance=1000;
		double norm, dist;
		int selected_point=-1;
		for(int i=0;i<n-1;i++)
		{
			norm=Math.sqrt( (normal.x+transformed_cutting_normal[i].x)*(normal.x+transformed_cutting_normal[i].x)+
					(normal.y+transformed_cutting_normal[i].y)*(normal.y+transformed_cutting_normal[i].y)+
					(normal.z+transformed_cutting_normal[i].z)*(normal.z+transformed_cutting_normal[i].z));
			
			dist=Math.sqrt( transformed_cutting_point[i].x*transformed_cutting_point[i].x +
					transformed_cutting_point[i].y*transformed_cutting_point[i].y +
					transformed_cutting_point[i].z*transformed_cutting_point[i].z);

			if(norm<min_normal || (norm==min_normal && dist<min_distance) )
			{
				min_normal=norm;
				min_distance=dist;
				selected_point=i;
			}
		}
		
		Point3d offsetPoint = new Point3d(pos);
		//TODO: Add feature for mould milling? IE, sub or add...
//		offsetPoint.add(transformed_cutting_point[selected_point]);		
		offsetPoint.sub(transformed_cutting_point[selected_point]);		
		
		double[] ret = new double[]{offsetPoint.x, offsetPoint.y, offsetPoint.z};
		return ret;	

	}
	
	public double[] calcOffset_WS(Point3d pos, Vector3d normal, AbstractBoard board)
	{	
		// find the cutting point with normal pointing against the board normal
		// if ambiguous choose cutting point closest to tool tip (0,0,0)
		// 2016-03-04
		// Updated for WaveShapes MachineConfig
		// If lateral angle is less than 30 deg, then tool is offset upwards, 
		// otherwise normal behaviour applies
		
		double min_normal=1000;
		double min_distance=1000;
		double norm;
		double dist = 0;
		int selected_point=-1;
		
		double max_lat_ang;   //10/180*3.14159; // 30 degrees in radians
		double max_neg_distance = 0; // maximum normal interference of tool
		
		// calculate lateral angle
		//System.out.println(normal.x + ", " + normal.y + ", " + normal.z);
		//System.out.println("max_lat_ang = " + max_lat_ang);
		//System.out.println("TAN max_lat_ang x Y = " + Math.abs(normal.y));
		if ((Math.abs(normal.x) >= 100.577*Math.abs(normal.z)) 
			|| (Math.abs(normal.y) >= 100.577*Math.abs(normal.z)))
		{
			for(int i=0;i<n-1;i++)
			{
				norm=Math.sqrt( (normal.x+transformed_cutting_normal[i].x)*(normal.x+transformed_cutting_normal[i].x)+
						(normal.y+transformed_cutting_normal[i].y)*(normal.y+transformed_cutting_normal[i].y)+
						(normal.z+transformed_cutting_normal[i].z)*(normal.z+transformed_cutting_normal[i].z));
				
				dist=Math.sqrt( transformed_cutting_point[i].x*transformed_cutting_point[i].x +
						transformed_cutting_point[i].y*transformed_cutting_point[i].y +
						transformed_cutting_point[i].z*transformed_cutting_point[i].z);

				if(norm<min_normal || (norm==min_normal && dist<min_distance) )
				{
					min_normal=norm;
					min_distance=dist;
					selected_point=i;
				}
			}
			
			Point3d offsetPoint = new Point3d(pos);
			//TODO: Add feature for mould milling? IE, sub or add...
	//		offsetPoint.add(transformed_cutting_point[selected_point]);		
			offsetPoint.sub(transformed_cutting_point[selected_point]);		
			
			double[] ret = new double[]{offsetPoint.x, offsetPoint.y, offsetPoint.z};
			return ret;	
		}
		else // lateral does not exceed limit - offset tool upwards only
		{
			//System.out.println("Just Checking In Here");
			for(int i=0;i<n-1;i++) // each point on tool mesh
			{
				//normal distance
				//dist = ((normal.x*transformed_cutting_point[i].x 
				//	+ normal.y*transformed_cutting_point[i].y 
				//	+ normal.z*transformed_cutting_point[i].z)
				//	/Math.sqrt(normal.x*normal.x + normal.y*normal.y + normal.z*normal.z));
				
				//vertical distance
				dist = ((normal.x*transformed_cutting_point[i].x 
					+ normal.y*transformed_cutting_point[i].y) / (-normal.z))
					- transformed_cutting_point[i].z;
				
				if (dist > max_neg_distance)
				{
					max_neg_distance = dist;
				}
			}
			//System.out.println("Dist = " + max_neg_distance);
			//double[] ret = new double[]{pos.x, pos.y , pos.z + (Math.abs(normal.z)/normal.z)*max_neg_distance};
			double[] ret = new double[]{pos.x, pos.y , pos.z + max_neg_distance};
			
			//double[] ret = new double[]{pos.x, pos.y , pos.z 
			//	+ max_neg_distance};
			return ret;	
		}
	}

public double[] calcOffset_WS_bottom(Point3d pos, Vector3d normal, AbstractBoard board)
	{	
		// find the cutting point with normal pointing against the board normal
		// if ambiguous choose cutting point closest to tool tip (0,0,0)
		// 2016-03-04
		// Updated for WaveShapes MachineConfig
		// If lateral angle is less than 30 deg, then tool is offset upwards, 
		// otherwise normal behaviour applies
		
		double min_normal=1000;
		double min_distance=1000;
		double norm;
		double dist = 0;
		int selected_point=-1;
		
		double max_lat_ang;   //10/180*3.14159; // 30 degrees in radians
		double max_neg_distance = 0; // maximum normal interference of tool
		
		// calculate lateral angle
		//System.out.println(normal.x + ", " + normal.y + ", " + normal.z);
		//System.out.println("max_lat_ang = " + max_lat_ang);
		//System.out.println("TAN max_lat_ang x Y = " + Math.abs(normal.y));
		if ((Math.abs(normal.x) >= 100.577*Math.abs(normal.z)) 
			|| (Math.abs(normal.y) >= 100.577*Math.abs(normal.z)))
		{
			for(int i=0;i<n-1;i++)
			{
				norm=Math.sqrt( (normal.x+transformed_cutting_normal[i].x)*(normal.x+transformed_cutting_normal[i].x)+
						(normal.y+transformed_cutting_normal[i].y)*(normal.y+transformed_cutting_normal[i].y)+
						(normal.z+transformed_cutting_normal[i].z)*(normal.z+transformed_cutting_normal[i].z));
				
				dist=Math.sqrt( transformed_cutting_point[i].x*transformed_cutting_point[i].x +
						transformed_cutting_point[i].y*transformed_cutting_point[i].y +
						transformed_cutting_point[i].z*transformed_cutting_point[i].z);

				if(norm<min_normal || (norm==min_normal && dist<min_distance) )
				{
					min_normal=norm;
					min_distance=dist;
					selected_point=i;
				}
			}
			
			Point3d offsetPoint = new Point3d(pos);
			//TODO: Add feature for mould milling? IE, sub or add...
	//		offsetPoint.add(transformed_cutting_point[selected_point]);		
			offsetPoint.sub(transformed_cutting_point[selected_point]);		
			
			double[] ret = new double[]{offsetPoint.x, offsetPoint.y, offsetPoint.z};
			return ret;	
		}
		else // lateral does not exceed limit - offset tool upwards only
		{
			//System.out.println("Just Checking In Here");
			for(int i=0;i<n-1;i++) // each point on tool mesh
			{
				//normal distance
				//dist = ((normal.x*transformed_cutting_point[i].x 
				//	+ normal.y*transformed_cutting_point[i].y 
				//	+ normal.z*transformed_cutting_point[i].z)
				//	/Math.sqrt(normal.x*normal.x + normal.y*normal.y + normal.z*normal.z));
				
				//vertical distance
				dist = ((normal.x*transformed_cutting_point[i].x 
					+ normal.y*transformed_cutting_point[i].y) / (-normal.z))
					- transformed_cutting_point[i].z;
				
				if (dist < max_neg_distance)
				{
					max_neg_distance = dist;
				}
			}
			//System.out.println("Dist = " + max_neg_distance);
			//double[] ret = new double[]{pos.x, pos.y , pos.z + (Math.abs(normal.z)/normal.z)*max_neg_distance};
			double[] ret = new double[]{pos.x, pos.y , pos.z + max_neg_distance};
			
			//double[] ret = new double[]{pos.x, pos.y , pos.z 
			//	+ max_neg_distance};
			return ret;	
		}
	}
	
	public NurbsPoint calcOffset_WS(NurbsPoint p1, NurbsPoint mynormal)
	{	
//		Vector3d mynormal2=new Vector3d(mynormal.x, mynormal.y, mynormal.z);
		Vector3d mynormal2=new Vector3d(mynormal.x, mynormal.z, mynormal.y);
		double[] res=new double[3];
		res=calcOffset_WS(new Point3d(p1.x, p1.z, p1.y), mynormal2, null);
								
		return new NurbsPoint(res[0],res[2],res[1]);
	}
	
	public NurbsPoint calcOffset_WS_bottom(NurbsPoint p1, NurbsPoint mynormal)
	{	
//		Vector3d mynormal2=new Vector3d(mynormal.x, mynormal.y, mynormal.z);
		Vector3d mynormal2=new Vector3d(mynormal.x, mynormal.z, mynormal.y);
		double[] res=new double[3];
		res=calcOffset_WS_bottom(new Point3d(p1.x, p1.z, p1.y), mynormal2, null);
								
		return new NurbsPoint(res[0],res[2],res[1]);
	}
	
	public NurbsPoint calcOffset(NurbsPoint p1, NurbsPoint mynormal)
	{	
//		Vector3d mynormal2=new Vector3d(mynormal.x, mynormal.y, mynormal.z);
		Vector3d mynormal2=new Vector3d(mynormal.x, mynormal.z, mynormal.y);
		double[] res=new double[3];
		res=calcOffset(new Point3d(p1.x, p1.z, p1.y), mynormal2, null);
								
		return new NurbsPoint(res[0],res[2],res[1]);
	}
	public void setRotationAngle(double angle)
	{
	}
	
	public void setRotationMatrix(double[][] m)
	{
	}
	
	public void setTranslationVector(double[] t)
	{
	}

	public void calculateTriangles(NurbsSurface srf)
	{

		double step=0.5;
//		triangle=new Point3d[(int)(2*srf.get_nr_of_segments()/step*srf.get_nr_of_points()/step)][3];
		triangle=new Point3d[(int)(2*srf.get_nr_of_segments()/step)][(int)(2*srf.get_nr_of_points()/step)][2][3];
		
		NurbsPoint p1, p2, p3;

		int k=0;
		int k2=0;
		
		
		for(int i=0; i<srf.get_nr_of_segments(); i++)
		{
			for(double u=0.0; u<1.0-step/2; u=u+step)
			{
				
				k2=0;
				
				for(int j=0; j<srf.get_nr_of_points(); j++)
				{
				
					for(double v=0.0; v<1.0-step/2; v=v+step)
					{
						p1=srf.get_point_on_surface(i,j,u,v);
						p2=srf.get_point_on_surface(i,j,u+step,v);
						p3=srf.get_point_on_surface(i,j,u+step,v+step);
						
						triangle[k][k2][0][0]=new Point3d(p1.x,p1.y,p1.z);
						triangle[k][k2][0][1]=new Point3d(p2.x,p2.y,p2.z);
						triangle[k][k2][0][2]=new Point3d(p3.x,p3.y,p3.z);
										
						p1=srf.get_point_on_surface(i,j,u,v);
						p2=srf.get_point_on_surface(i,j,u+step,v+step);
						p3=srf.get_point_on_surface(i,j,u+step,v);
						
						triangle[k][k2][1][0]=new Point3d(p1.x,p1.y,p1.z);
						triangle[k][k2][1][1]=new Point3d(p2.x,p2.y,p2.z);
						triangle[k][k2][1][2]=new Point3d(p3.x,p3.y,p3.z);
						k2++;
//						System.out.println("k=" + k + " k2=" + k2);
						
					}
			
				}
				
				k++;
			
			}
		}
		
		triangle_n=k;
		triangle_n2=k2;		
	
	}

	public boolean checkCollision(NurbsPoint cutterpos)
	{
		Intersect intersect=new Intersect();
		PickSegment segment;
		
		Point3d start, end;
//		Point3d[] triangle=new Point3d[3];
		int index=0;
		double[] dist=new double[3];
		boolean collision=false;
		NurbsPoint p1, p2, p3;
		double step=0.5;
		
		int start_i, end_i;
		int start_j, end_j;
		
		start_i=(int)((cutterpos.i-1)/step);
		end_i=(int)((cutterpos.i+2)/step);
		start_j=(int)((cutterpos.j-1)/step);
		end_j=(int)((cutterpos.j+2)/step);
		
		System.out.println("step=" + step);
		System.out.println("cutterpos.i=" + cutterpos.i);
		System.out.println("cutterpos.j=" + cutterpos.j);
		System.out.println("n=" + n);		
		for(int k=0;k<cn-2;k=k+2)
		{
			//create line segment from tool
			
//			start=new Point3d(transformed_cutting_point[k].x + cutterpos.x,
//					transformed_cutting_point[k].y + cutterpos.y,
//					transformed_cutting_point[k].z + cutterpos.z);

//			end=new Point3d(transformed_cutting_point[k+1].x + cutterpos.x,
//					transformed_cutting_point[k+1].y + cutterpos.y,
//					transformed_cutting_point[k+1].z + cutterpos.z	);
					

			start=new Point3d(transformed_collision_point[k].x + cutterpos.x,
					transformed_collision_point[k].y + cutterpos.y,
					transformed_collision_point[k].z + cutterpos.z);

			end=new Point3d(transformed_collision_point[k+1].x + cutterpos.x,
					transformed_collision_point[k+1].y + cutterpos.y,
					transformed_collision_point[k+1].z + cutterpos.z);


			segment=new PickSegment(start, end);
	
			//check intersection with each surface triangle
				
			System.out.println("checking for collision");


			for(int i=start_i;i<end_i;i++)
			{
				for(int j=start_j;j<end_j;j++)
				{
					System.out.println("checking 1");
					if(intersect.segmentAndTriangle(segment,triangle[i][j][0],index,dist))
						collision=true;
					System.out.println("checking 2");
					if(intersect.segmentAndTriangle(segment,triangle[i][j][1],index,dist))
						collision=true;
				}
			}		

			System.out.println("checked for collision");
			
			/*			
			for(int i=3; i<srf.get_nr_of_segments()-5; i++)
			{
				for(double u=0.0; u<1.0; u=u+step)
				{
				
					for(int j=3; j<srf.get_nr_of_points()-5; j++)
					{
					
						for(double v=0.0; v<1.0; v=v+step)
						{
							p1=srf.get_point_on_surface(i,j,u,v);
							p2=srf.get_point_on_surface(i,j,u+step,v);
							p3=srf.get_point_on_surface(i,j,u+step,v+step);
							
							triangle[0]=new Point3d(p1.x,p1.y,p1.z);
							triangle[1]=new Point3d(p2.x,p2.y,p2.z);
							triangle[2]=new Point3d(p3.x,p3.y,p3.z);
							
							if(intersect.segmentAndTriangle(segment,triangle,index,dist))
								collision=true;
							
							p1=srf.get_point_on_surface(i,j,u,v);
							p2=srf.get_point_on_surface(i,j,u+step,v+step);
							p3=srf.get_point_on_surface(i,j,u+step,v);
							
							triangle[0]=new Point3d(p1.x,p1.y,p1.z);
							triangle[1]=new Point3d(p2.x,p2.y,p2.z);
							triangle[2]=new Point3d(p3.x,p3.y,p3.z);
							
							if(intersect.segmentAndTriangle(segment,triangle,index,dist))
								collision=true;
						}
				
					}
				
				}
			}
*/

		
		}

		System.err.println("INHERE 3");	
		if(collision)
		{
			System.out.println("collision detected at i=" + cutterpos.i + " j=" + cutterpos.j);
			
		}
		
		
		return collision;
	}

	public boolean checkCollision(Point3d pos, AbstractBoard board)
	{
		return false;
	}
	
	public BranchGroup get3DModel()
	{
		return null;
	}

	public void update3DModel()
	{
	}
	
	public void loadCutter(String filename)
	{
	
		n=0;
		cutting_point=new Point3d[10000];
		cutting_normal=new Vector3d[10000];
				
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
					cutting_normal[n]=new Vector3d(0.0, 0.0, 0.0);
					line=line.substring(pos+6);	// remove part before normal coordinates
					line=line.trim();		// remove leading spaces
					pos=line.indexOf(' ');		// find space after normal_x coordinate
					cutting_normal[n].x=Double.parseDouble(line.substring(0,pos));	// read normal_x
					line=line.substring(pos);	// remove normal_x
					line=line.trim();		// remove leading spaces
					pos=line.indexOf(' ');		// find space after normal_y coordinate
					cutting_normal[n].y=Double.parseDouble(line.substring(0,pos));	// read normal_y
					line=line.substring(pos);	// remove normal_y
					line=line.trim();		// remove leading spaces
					cutting_normal[n].z=Double.parseDouble(line);	// read normal_z
					for(int i=0;i<3;i++)
					{
					
						//read vertex
						do{
							line=buf_reader.readLine();
							line.toLowerCase();
							pos=line.indexOf("vertex");	// a new vertex should start with "vertex"
						}while(pos==-1);

						cutting_point[n]=new Point3d(0.0, 0.0, 0.0);
						line=line.substring(pos+6);	// remove part before vertex coordinates
						line=line.trim();		// remove leading spaces
					
						pos=line.indexOf(' ');		// find space after vertex_x coordinate
						cutting_point[n].x=Double.parseDouble(line.substring(0,pos));	// read vertex_x
					
						line=line.substring(pos);	// remove vertex_x
						line=line.trim();		// remove leading spaces
						pos=line.indexOf(' ');		// find space after vertex_y coordinate
						cutting_point[n].y=Double.parseDouble(line.substring(0,pos));	// read vertex_y
						
						line=line.substring(pos);	// remove vertex_y
						line=line.trim();		// remove leading spaces
						cutting_point[n].z=Double.parseDouble(line);	// read vertex_z
						
						n=n+1;
						cutting_normal[n]=new Vector3d(cutting_normal[n-1].x, cutting_normal[n-1].y,cutting_normal[n-1].z);
					
					}
				}
				
			}while(pos2==-1);
		
			buf_reader.close ();
			
		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}

	}


	public void loadCollisionCutter(String filename)
	{
	
		cn=0;
		collision_point=new Point3d[10000];
				
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
/*					cutting_normal[n]=new Vector3d(0.0, 0.0, 0.0);
					line=line.substring(pos+6);	// remove part before normal coordinates
					line=line.trim();		// remove leading spaces
					pos=line.indexOf(' ');		// find space after normal_x coordinate
					cutting_normal[n].x=Double.parseDouble(line.substring(0,pos));	// read normal_x
					line=line.substring(pos);	// remove normal_x
					line=line.trim();		// remove leading spaces
					pos=line.indexOf(' ');		// find space after normal_y coordinate
					cutting_normal[n].y=Double.parseDouble(line.substring(0,pos));	// read normal_y
					line=line.substring(pos);	// remove normal_y
					line=line.trim();		// remove leading spaces
					cutting_normal[n].z=Double.parseDouble(line);	// read normal_z
*/

					for(int i=0;i<3;i++)
					{
					
						//read vertex
						do{
							line=buf_reader.readLine();
							line.toLowerCase();
							pos=line.indexOf("vertex");	// a new vertex should start with "vertex"
						}while(pos==-1);

						collision_point[cn]=new Point3d(0.0, 0.0, 0.0);
						line=line.substring(pos+6);	// remove part before vertex coordinates
						line=line.trim();		// remove leading spaces
					
						pos=line.indexOf(' ');		// find space after vertex_x coordinate
						collision_point[cn].x=Double.parseDouble(line.substring(0,pos));	// read vertex_x
					
						line=line.substring(pos);	// remove vertex_x
						line=line.trim();		// remove leading spaces
						pos=line.indexOf(' ');		// find space after vertex_y coordinate
						collision_point[cn].y=Double.parseDouble(line.substring(0,pos));	// read vertex_y
						
						line=line.substring(pos);	// remove vertex_y
						line=line.trim();		// remove leading spaces
						collision_point[cn].z=Double.parseDouble(line);	// read vertex_z
						
						cn=cn+1;
//						cutting_normal[n]=new Vector3d(cutting_normal[n-1].x, cutting_normal[n-1].y,cutting_normal[n-1].z);
					
					}
				}
				
			}while(pos2==-1);
		
			buf_reader.close ();
			
		}
		catch (IOException e2) {
			System.out.println ("IO exception =" + e2 );
		}

	}

	
	//create a flat cutter and save as STL-file

	public void createFlatCutter(String filename, double tool_radius, int nr_triangles)
	{
		Point3d p1,p2,p3;
		Vector3d normal = new Vector3d();
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();

		PrintStream dataOut;

		try
		{
			dataOut=new PrintStream(new File(filename));

			dataOut.println("solid cutter");
	
			double step_angle=360.0/(nr_triangles/2.0);

			for(double i=0;i<360.0;i=i+step_angle)
			{

				p1=new Point3d((tool_radius-0.1)*Math.cos((i+step_angle)*Math.PI/180.0), (tool_radius-0.1)*Math.sin((i+step_angle)*Math.PI/180.0), 0.0);
				p2=new Point3d(tool_radius*Math.cos((i+step_angle)*Math.PI/180.0), tool_radius*Math.sin((i+step_angle)*Math.PI/180.0), 0.1);
				p3=new Point3d(tool_radius*Math.cos(i*Math.PI/180.0), tool_radius*Math.sin(i*Math.PI/180.0), 0.1);

				v1.sub(p1, p2);
				v2.sub(p1, p3);
				normal.cross(v1, v2);
				normal.normalize();
			
				if(!(Double.toString(normal.x).equals("NaN")))
				{
					dataOut.println("facet normal " + Double.toString(normal.x) + " " + Double.toString(normal.y)+ " " + Double.toString(normal.z) + "");
					dataOut.println(" outer loop");
					dataOut.println("  vertex " + Double.toString(p1.x) + " " + Double.toString(p1.y) + " " + Double.toString(p1.z) + "");
					dataOut.println("  vertex " + Double.toString(p2.x) + " " + Double.toString(p2.y) + " " + Double.toString(p2.z) + "");
					dataOut.println("  vertex " + Double.toString(p3.x) + " " + Double.toString(p3.y) + " " + Double.toString(p3.z) + "");
					dataOut.println(" endloop");
					dataOut.println("endfacet");
				}


				p1=new Point3d((tool_radius-0.1)*Math.cos((i+step_angle)*Math.PI/180.0), (tool_radius-0.1)*Math.sin((i+step_angle)*Math.PI/180.0), 0.0);
				p2=new Point3d(tool_radius*Math.cos(i*Math.PI/180.0), tool_radius*Math.sin(i*Math.PI/180.0), 0.1);
				p3=new Point3d((tool_radius-0.1)*Math.cos(i*Math.PI/180.0), (tool_radius-0.1)*Math.sin(i*Math.PI/180.0), 0.0);

				v1.sub(p1, p2);
				v2.sub(p1, p3);
				normal.cross(v1, v2);
				normal.normalize();
			
				if(!(Double.toString(normal.x).equals("NaN")))
				{
					dataOut.println("facet normal " + Double.toString(normal.x) + " " + Double.toString(normal.y)+ " " + Double.toString(normal.z) + "");
					dataOut.println(" outer loop");
					dataOut.println("  vertex " + Double.toString(p1.x) + " " + Double.toString(p1.y) + " " + Double.toString(p1.z) + "");
					dataOut.println("  vertex " + Double.toString(p2.x) + " " + Double.toString(p2.y) + " " + Double.toString(p2.z) + "");
					dataOut.println("  vertex " + Double.toString(p3.x) + " " + Double.toString(p3.y) + " " + Double.toString(p3.z) + "");
					dataOut.println(" endloop");
					dataOut.println("endfacet");
				}



			}

			dataOut.println("endsolid cutter");


			}
			catch(IOException excep2)
			{
				System.out.println("Problem creating file");
			}



	}
	
			
}

