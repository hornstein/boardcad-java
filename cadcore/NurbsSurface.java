package cadcore;

import java.io.*;
import javax.vecmath.*;

//=========================================================NurbsSurface
/**
 * This class implements a NURBS surface, which is used for creating a 3D
 * model of the board. The class can also represent special cases of
 * nurbs surfaces such as bezier patches and b-spline surfaces.  
 */

 
public class NurbsSurface implements Cloneable
{
	protected int nr_of_segments;
	protected int points_per_segment;
	protected double[] u;			//knot vector
	protected double[] v;			//knot vector
	protected int k=3; 			//degree
	protected NurbsPoint[][] points;
	protected NurbsPoint[][] evaluated_points;
	protected double step;
	protected int surface_type;		//1=bezier, 2=bspline, 3=nurbs
	protected boolean always_evaluate;
	

	/**
	 * Creates a nurbs surface with a given number of segments and points
	 *
	 * @param segments nr of segments
	 * @param nr_of_points nr of points
	 */	 
	public NurbsSurface(int segments, int nr_of_points)
	{
		always_evaluate=false;
		nr_of_segments=segments;
		points_per_segment=nr_of_points;
		surface_type=2;
		u=new double[100];
		v=new double[100];
		k=3;
		int i,j;

		step=0.2;		
		evaluated_points=new NurbsPoint[(int)((double)nr_of_segments/step)][(int)((double)points_per_segment/step)];
		
		for(i=0;i<(int)((double)nr_of_segments/step);i++)
		{
			for(j=0;j<(int)((double)points_per_segment/step);j++)
			{
				evaluated_points[i][j]=new NurbsPoint(0,0,0);	
			}
		}
		
		u[0]=0;
		u[1]=0;
		u[2]=0;
		u[3]=0;
		v[0]=0;
		v[1]=0;
		v[2]=0;
		v[3]=0;

		for(i=4;i<segments-4;i++)
		{
			u[i]=i-3;
		}
		
		for(i=4;i<nr_of_points-4;i++)
		{
			v[i]=i-3;
		}


		u[segments-4]=segments-7;
		u[segments-3]=segments-7;
		u[segments-2]=segments-7;
		u[segments-1]=segments-7;
		
		v[nr_of_points-4]=nr_of_points-7;
		v[nr_of_points-3]=nr_of_points-7;
		v[nr_of_points-2]=nr_of_points-7;
		v[nr_of_points-1]=nr_of_points-7;
		
		points=new NurbsPoint[nr_of_segments][points_per_segment];
	}

	/**
	 * Creates a nurbs surface from a step model
	 *
	 * @param stepstring a string describing a nurbs surface on step format
	 * @param cp an array of cartesian points
	 */	 
	public NurbsSurface(String stepstring, NurbsPoint[] cp)
	{
		always_evaluate=false;
	
		String s1, s2;
		int pos, pos2;
		int segments=0;
		int points2=0;
		surface_type=3;
		
		//find number of segments
		
		pos=stepstring.indexOf("KNOTS");
		s1=stepstring.substring(pos);
		pos=s1.indexOf("(");
		s1=s1.substring(pos+1);
		pos=s1.indexOf("(");
		pos2=s1.indexOf(")");
		s1=s1.substring(pos+1, pos2);		

		pos=s1.indexOf(",");
		do
		{
			segments++;
			s1=s1.substring(pos+1);
			pos=s1.indexOf(",");
		}while(pos>=0);
		
		segments=segments+7;
		
		System.out.println("nr of segment = " + segments);	
			
		//find number of points
	
		pos=stepstring.indexOf("KNOTS");
		s1=stepstring.substring(pos);
		pos=s1.indexOf(")");
		s1=s1.substring(pos+1);
		pos=s1.indexOf("(");
		pos2=s1.indexOf(")");
		s1=s1.substring(pos+1, pos2);		
		pos=s1.indexOf(",");
		do
		{
			points2++;
			s1=s1.substring(pos+1);
			pos=s1.indexOf(",");
		}while(pos>=0);
		
		points2=points2+7;
		
		System.out.println("nr of points = " + points2);
		
		//read knot vectors
		
		pos=stepstring.indexOf("KNOTS");
		s1=stepstring.substring(pos);
		pos=s1.indexOf(")");
		s1=s1.substring(pos+1);
		pos=s1.indexOf(")");
		s1=s1.substring(pos+1);
		pos=s1.indexOf("(");
		s1=s1.substring(pos+1);
		
		
		u=new double[100];
		u[0]=0;
		u[1]=0;
		u[2]=0;
		
		for(int i=3; i<segments-3;i++)
		{
			pos=Math.min(s1.indexOf(","),s1.indexOf(")"));
			u[i]=Double.parseDouble( (s1.substring(0,pos)).trim() );
			s1=s1.substring(pos+1);
		}
		
		u[segments-3]=u[segments-4];
		u[segments-2]=u[segments-4];
		u[segments-1]=u[segments-4];
		
		for(int i=0;i<segments;i++)
		{
			System.out.println("u[" + i + "] = " + u[i]);
		}
			
		pos=stepstring.indexOf("KNOTS");
		s1=stepstring.substring(pos);
		pos=s1.indexOf(")");
		s1=s1.substring(pos+1);
		pos=s1.indexOf(")");
		s1=s1.substring(pos+1);
		pos=s1.indexOf(")");
		s1=s1.substring(pos+1);
		pos=s1.indexOf("(");
		s1=s1.substring(pos+1);	
		
		v=new double[100];
		v[0]=0;
		v[1]=0;
		v[2]=0;
		
		for(int i=3; i<points2-3;i++)
		{
			pos=Math.min(s1.indexOf(","),s1.indexOf(")"));
			v[i]=Double.parseDouble( (s1.substring(0,pos)).trim() );
			s1=s1.substring(pos+1);
		}
		
		v[points2-3]=u[points2-4];
		v[points2-2]=u[points2-4];
		v[points2-1]=u[points2-4];

		for(int i=0;i<points2;i++)
		{
			System.out.println("v[" + i + "] = " + v[i]);
		}

		//read surface points
		
		nr_of_segments=segments;
		points_per_segment=points2;
		
		points=new NurbsPoint[nr_of_segments][points_per_segment];
		
		
		for(int i=0;i<nr_of_segments;i++)
			for(int j=0;j<points_per_segment;j++)
				points[i][j]=new NurbsPoint(0,0,0);
				
		
		pos=stepstring.indexOf("B_SPLINE_SURFACE");
		s1=stepstring.substring(pos);
		pos=s1.indexOf("(");
		s1=s1.substring(pos+1);	
		pos=s1.indexOf("(");
		s1=s1.substring(pos+1);	
		
		int isdeck=stepstring.indexOf("deck");
		
		for(int i=2;i<nr_of_segments-2;i++)
		{
			pos=s1.indexOf("(");
			s1=s1.substring(pos+1);
			
			for(int j=2;j<points_per_segment-2;j++)
			{
				pos=s1.indexOf("#");
				pos2=Math.min(s1.indexOf(","),s1.indexOf(")"));
				
				points[i][j] = cp[Integer.parseInt( (s1.substring(pos+1,pos2)).trim() )];
				
				System.out.println("points[" + i + "][" + j + "] = " + points[i][j].x);
					
				s1=s1.substring(pos2+1);
			}
		}
		
		//set global variables
		if(u[4]==u[5])
			surface_type=1;
	
		k=3;
		step=0.2;		
		evaluated_points=new NurbsPoint[(int)((double)nr_of_segments/step)][(int)((double)points_per_segment/step)];
	}



	/**
	 * Set the knot values to bspline representation, i.e. uniform distances
	 * between each knot value.
	 */	 
	public void set_knots_bspline()
	{
		int segments=nr_of_segments;
		int nr_of_points=points_per_segment;
		int i;
		surface_type=2;
		
		u[0]=0;
		u[1]=0;
		u[2]=0;
		u[3]=0;
		v[0]=0;
		v[1]=0;
		v[2]=0;
		v[3]=0;

		for(i=4;i<segments-4;i++)
		{
			u[i]=i-3;
		}
		
		for(i=4;i<nr_of_points-4;i++)
		{
			v[i]=i-3;
		}

		u[segments-4]=segments-7;
		u[segments-3]=segments-7;
		u[segments-2]=segments-7;
		u[segments-1]=segments-7;
		
		v[nr_of_points-4]=nr_of_points-7;
		v[nr_of_points-3]=nr_of_points-7;
		v[nr_of_points-2]=nr_of_points-7;
		v[nr_of_points-1]=nr_of_points-7;
	}

	/**
	 * Set the knot values to bezier representation, i.e. having three
	 * points with the same value
	 */	 
	public void set_knots_bezier()
	{
		int segments=nr_of_segments;
		int nr_of_points=points_per_segment;
		int i;
		surface_type=1;
		
		u[0]=0;
		u[1]=0;
		u[2]=0;
		u[3]=0;
		v[0]=0;
		v[1]=0;
		v[2]=0;
		v[3]=0;

		for(i=4;i<segments-4;i=i+3)
		{
			u[i]=(i-1)/3;
			u[i+1]=(i-1)/3;
			u[i+2]=(i-1)/3;
		}
		
		for(i=4;i<nr_of_points-4;i=i+3)
		{
			v[i]=(i-1)/3;
			v[i+1]=(i-1)/3;
			v[i+2]=(i-1)/3;
		}

		u[segments-4]=(segments-5)/3;
		u[segments-3]=(segments-5)/3;
		u[segments-2]=(segments-5)/3;
		u[segments-1]=(segments-5)/3;
		
		v[nr_of_points-4]=(nr_of_points-5)/3;
		v[nr_of_points-3]=(nr_of_points-5)/3;
		v[nr_of_points-2]=(nr_of_points-5)/3;
		v[nr_of_points-1]=(nr_of_points-5)/3;

	}
	
	/**
	 * Define the surface as bezier
	 */	 
	public void set_as_bezier()
	{
		surface_type=1;
	}

	/**
	 * Define the surface as bspline
	 */	 
	public void set_as_bspline()
	{
		surface_type=2;
	}
	
	/**
	 * Define the surface as full nurbs
	 */	 
	public void set_as_nurbs()
	{
		surface_type=3;
	}
	
	/**
	 * Get the surface type
	 *
	 * @return int number representing the surface type (1=bezier, 2=bspline, 3=nurbs)
	 */	 
	public int get_surface_type()
	{
		return surface_type;
	}

	/**
	 * Set the surface type
	 *
	 * @param st number representing the surface type (1=bezier, 2=bspline, 3=nurbs)
	 */	 
	public void set_surface_type(int st)
	{
		surface_type=st;
	}
	
	/**
	 * Decides if the surface will be evaluated at each point or approximated
	 * from a triangulated surface. Evaluating the surface is slow, but more accurate.
	 *
	 * @param value True is we want evaluate the surface in each point
	 */	 
	public void evaluate_always(boolean value)
	{
		always_evaluate=value;
	}

	/**
	 * Saves the surface on the old .cad format
	 *
	 * @param dataOut Output stream for writing the surface
	 */	 
	public void save(DataOutputStream dataOut)
	{
		int i;
		int j;
		for(i=0;i<nr_of_segments;i++)
		{
			for(j=0;j<points_per_segment;j++)
			{
				try
				{
					dataOut.writeDouble( points[i][j].x);
					dataOut.writeDouble( points[i][j].y);
					dataOut.writeDouble( points[i][j].z);
				}
				catch(IOException e)
				{
					System.out.println("Problem creating file");
				}

			}
		}
	}


	/**
	 * Get number of segments in the surface
	 *
	 * @return int number of segments
	 */	 
	public int get_nr_of_segments()
	{
		return nr_of_segments;
	}

	/**
	 * Get number of points in the surface
	 *
	 * @return int number of segments
	 */	 
	public int get_nr_of_points()
	{
		return points_per_segment;
	}

	/**
	 * Sets the position of the control point at a given segment and point
	 *
	 * @param segment the segment we want to set
	 * @param point the point we want to set
	 * @param value the 3D position of the control point
	 */	 
	public void set_control_point(int segment, int point, NurbsPoint value)
	{
		points[segment][point]=new NurbsPoint(value.x,value.y,value.z);
	}

	/**
	 * Gets the position of the control point at a given segment and point
	 *
	 * @param segment the segment we want to set
	 * @param point the point we want to set
	 * @return  NurbsPoint the 3D position of the control point
	 */	 
	public NurbsPoint get_control_point(int segment, int point)
	{
		return points[segment][point];
	}


	/**
	 * Evaluates the surface and creates a 3D mesh.
	 */	 
	public void evaluate_surface()
	{
		if(surface_type!=2)
		{
			double epsilon=0.001;
			
			for(int i=0;i<nr_of_segments;i++)
			{
				for(double u=0.0;u<1.0-epsilon;u=u+step)
				{
					for(int j=0;j<points_per_segment;j++)
					{
						for(double v=0.0;v<1.0-epsilon;v=v+step)
						{
							evaluated_points[(int)(((i+u)/step)+epsilon)][(int)(((j+v)/step)+epsilon)] = evaluate_point_on_surface(i, j, u, v);
						}
					}
				}
			}
		}
	
	}

	/**
	 * Gets a point on the surface. For a bspline surface the position is
	 * always calculated directly by evaluating the surface function. For bezier and
	 * full nurbs surfaces the point is either estimated from the mesh or evaluated
	 * directly depending on the whether always_evaluate is set.
	 *
	 * @deprecated  This function is deprecated and is replaced by {@link #get_point_on_surface(double t, double s)}
	 *
	 * @param segment
	 * @param point
	 * @param u
	 * @param v
	 * @return NurbsPoint the point on the surface
	 */	 
	@Deprecated public NurbsPoint get_point_on_surface(int segment, int point, double u, double v)
	{
	
		if(surface_type==2 || always_evaluate)
		{
			return evaluate_point_on_surface(segment, point, u, v);
		}
		
		
		double epsilon=0.001;
			
		if(segment<1)
		{
			segment=1;
			u=0;
		}
		if(segment>nr_of_segments-3)
		{
			segment=nr_of_segments-3;
			u=1-step;
		}
		if(point<1)
		{
			point=1;
			v=0;
		}
		if(point>points_per_segment-3)
		{
			point=points_per_segment-3;
			v=1-step;
		}
		
		NurbsPoint ep;

		ep=evaluated_points[(int)(((segment+u)/step)+epsilon)][(int)(((point+v)/step)+epsilon)];
		
		if(ep==null)
		{
		System.out.println("null error");
		System.out.println("matrix1=" + (int)(((segment+u)/step)+epsilon) + " matrix2=" + (int)(((point+v)/step)+epsilon));
			return evaluate_point_on_surface(segment, point, u, v);
		}
		
		return ep;
	
	}
	
	/**
	 * Evaluates a point on the surface. 
	 *
	 * @param segment
	 * @param point
	 * @param u2
	 * @param v2
	 * @return NurbsPoint the point on the surface
	 */	 	
	@Deprecated private NurbsPoint evaluate_point_on_surface(int segment, int point, double u2, double v2)
	{

		if(segment<1)
		{
			segment=1;
			u2=0;
		}
		if(segment>nr_of_segments-3)
		{
			segment=nr_of_segments-3;
			u2=1;
		}
		if(point<1)
		{
			point=1;
			v2=0;
		}
		if(point>points_per_segment-3)
		{
			point=points_per_segment-3;
			v2=1;
		}

		if(surface_type==2)
			return get_point_on_surface_old(segment, point, u2, v2);
			

		double s=(segment-3.0+u2)/(nr_of_segments-7.0);
		double t=(point-3.0+v2)/(points_per_segment-7.0);

		
		if(t<=0)
			t=0.00001;
		if(t>=1)
			t=0.99999;

		if(s<=0)
			s=0.00001;
		if(s>=1)
			s=0.99999;
		
		s=u[0]+(u[nr_of_segments-1]-u[0])*s;
		t=v[0]+(v[points_per_segment-1]-v[0])*t;
		
		return get_point_on_surface(s, t);

	}


		
	/**
	 * Gets a point on the surface. The surface is parameterized using it's knot
	 * vectors. The parameters t and s defines a position on the surface. Use the
	 * functions {@link #getMinT()}, {@link #getMaxT()}, {@link #getMinS()}, and
	 * {@link #getMaxS()} to find the parameter space.
	 *
	 * @param s
	 * @param t 
	 * @return NurbsPoint the point on the surface
	 */	 
	public NurbsPoint get_point_on_surface(double s, double t)
	{

		double x=0.0,y=0.0,z=0.0;
		int i,j;
		double sum2=0.0;
		double temp;
		
		for(i=2;i<nr_of_segments-2;i++)
		{
			for(j=2;j<points_per_segment-2;j++)
			{			

				temp=n(i-2,3,s)*n2(j-2,3,t)*(points[i][j]).w;

				x += (points[i][j]).x*temp;
				y += (points[i][j]).y*temp;
				z += (points[i][j]).z*temp;
				sum2=sum2+temp;
			}
		}
		
		return new NurbsPoint(x/sum2,y/sum2,z/sum2);

	}
	
	private double n(int i, int k, double t)
	{
		if(k==0)
		{
			if(u[i]<=t && t<u[i+1] && u[i]<u[i+1])
				return 1.0;
			else
				return 0.0;
		}

		double a=0.0;
		double b=0.0;
		
		if(u[i+k]>u[i])
			a=(t-u[i])/(u[i+k]-u[i]);
		 
		if(u[i+k+1]>u[i+1])
			b=(u[i+k+1]-t)/(u[i+k+1]-u[i+1]);
		

		return ( a*n(i,k-1,t) + b*n(i+1,k-1,t) );
	}

	private double n2(int i, int k, double t)
	{

		if(k==0)
		{
			if(v[i]<=t && t<v[i+1] && v[i]<v[i+1])
				return 1.0;
			else
				return 0.0;
		}


		double a=0.0;
		double b=0.0;
		
		if(v[i+k]>v[i])
			a=(t-v[i])/(v[i+k]-v[i]);
		 
		if(v[i+k+1]>v[i+1])
			b=(v[i+k+1]-t)/(v[i+k+1]-v[i+1]);


		return ( a*n2(i,k-1,t) + b*n2(i+1,k-1,t) );


	}
	
	
	/**
	 * Gets a point on the surface using the old algorithms for bspline surfaces.
	 * These are very quick, but does not work for other surface types.
	 *
	 * @param segment
	 * @param point
	 * @param u
	 * @param v
	 * @return NurbsPoint the point on the surface
	 */	 	
	 @Deprecated public NurbsPoint get_point_on_surface_old(int segment, int point, double u, double v)
	 {

		double x=0.0,y=0.0,z=0.0;
		int i,j;
		double sum2=0.0;
		final double weight=1.0;
		
		
		if(segment<1)
		{
			segment=1;
			u=0;
		}
		if(segment>nr_of_segments-3)
		{
			segment=nr_of_segments-3;
			u=1;
		}
		if(point<1)
		{
			point=1;
			v=0;
		}
		if(point>points_per_segment-3)
		{
			point=points_per_segment-3;
			v=1;
		}

		for(i=segment-1;i<segment+3;i++)
		{
			for(j=point-1;j<point+3;j++)
			{

				double k = segment-i+u;
				double l = point-j+v;
				x += (points[i][j]).x*b(k)*b(l);
				y += (points[i][j]).y*b(k)*b(l);
				z += (points[i][j]).z*b(k)*b(l);
				sum2 += b(k)*b(l);
			}
		}
		x /= sum2;
		y /= sum2;
		z /= sum2;

		return new NurbsPoint(x,y,z);
	}

	private double b(double t)
	{
		double at = Math.abs(t);
		
		if(at >= 2)
			return 0.0;
		else if(at >= 1)
		{
			double two_sub_at = 2.0-at;
			return 0.16666666666666666666666666666667*two_sub_at*two_sub_at*two_sub_at;
		}

		double one_sub_at = 1.0-at;
		return 0.16666666666666666666666666666667 + 0.5*(-one_sub_at*(one_sub_at*one_sub_at - one_sub_at - 1));	//optimized slightly more
	}



	/**
	 * Gets the curvate at a given point on the surface. This function should
	 * updated to use the new surface parameterization...
	 *
	 * @param segment
	 * @param point
	 * @param u
	 * @param v
	 * @return NurbsPoint the point on the surface
	 */	 	
	public double get_curvature_xy(int segment, int point, double u, double v)
	{
		NurbsPoint p1, p2, p3;
		double xprim1, xprim2, yprim1, yprim2, xbis, ybis;
		double dt=0.01;

		p1=get_point_on_surface(segment, point, u, v);
		p2=get_point_on_surface(segment, point, u+dt, v);
		p3=get_point_on_surface(segment, point, u+2*dt, v);

		xprim1=(p2.x-p1.x)/dt;
		xprim2=(p3.x-p2.x)/dt;
		yprim1=(p2.y-p1.y)/dt;
		yprim2=(p3.y-p2.y)/dt;

		xbis=(xprim2-xprim1)/dt;
		ybis=(yprim2-yprim1)/dt;

		return (xprim1*ybis-xbis*yprim1)/Math.sqrt(Math.pow(xprim1*xprim1+yprim1*yprim1,3));
	}

	/**
	 * Gets the curvate at a given point on the surface. This function should
	 * updated to use the new surface parameterization...
	 *
	 * @param segment
	 * @param point
	 * @param u
	 * @param v
	 * @return NurbsPoint the point on the surface
	 */	 	
	public double get_curvature_xz(int segment, int point, double u, double v)
	{
		NurbsPoint p1, p2, p3;
		double xprim1, xprim2, zprim1, zprim2, xbis, zbis;
		double dt=0.01;

		p1=get_point_on_surface(segment, point, u, v);
		p2=get_point_on_surface(segment, point, u+dt, v);
		p3=get_point_on_surface(segment, point, u+2*dt, v);

		xprim1=(p2.x-p1.x)/dt;
		xprim2=(p3.x-p2.x)/dt;
		zprim1=(p2.z-p1.z)/dt;
		zprim2=(p3.z-p2.z)/dt;

		xbis=(xprim2-xprim1)/dt;
		zbis=(zprim2-zprim1)/dt;

		return (xprim1*zbis-xbis*zprim1)/Math.sqrt(Math.pow(xprim1*xprim1+zprim1*zprim1,3));
	}
	
	/**
	 * Gets the Y position of the surface, given the X and Z values.
	 *
	 * @param x
	 * @param z
	 * @return NurbsPoint the point on the surface
	 */	 	
	public NurbsPoint getYforXZ(double x, double z)
	{

		double u=0.0;
		double v=0.0;
		double mystep=0.01;
		NurbsPoint p;
		int i=0;
		int j=points_per_segment/2;
			
		//this works only for a rectangular mesh, fix later...
		do
		{
			u=u+mystep;
			if(u>=1.0)
			{
				u=0.0;
				i=i+1;
			}
			
			p=get_point_on_surface(i,j,u,v);
			
		}while(p.x<=x && i<nr_of_segments-1);
		
		do
		{
			v=v+mystep;
			if(v>=1.0)
			{
				v=0.0;
				j=j+1;
			}
			
			p=get_point_on_surface(i,j,u,v);
			
		}while(Math.abs(p.z)<=Math.abs(z) && j<points_per_segment);
		
		p.i=i;
		p.j=j;
		p.u=u;
		p.v=v;
		
		return p;

	}
	
	/**
	 * Transforms the surface given the name of the axis and a translation vector.
	 *
	 * @param x the name of the new x axis
	 * @param y the name of the new y axis
	 * @param z the name of the new z axis
	 * @param t the transformation vector (dx,dy,dz)
	 */	 	
	public void transform(String x, String y, String z, double[] t)
	{
		                
		double[][] m = {getAxisArray(x), getAxisArray(y), getAxisArray(x)};
		
		transform(m,t);
	
	}
	
	private double[] getAxisArray(String axis) 
	{
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
	
	/**
	 * Transforms the surface given a rotation matrix and a translation vector.
	 *
	 * @param m the rotation matrix (3x3)
	 * @param t the transformation vector (dx,dy,dz)
	 */	 	
	public void transform(double[][] m, double[] t)
	{
		int i=0;
		int j=0;
		double u=0.0;
		double v=0.0;
		NurbsPoint p1, p2, p3, p4;
		double tx,ty,tz;
		
		//transform control points
				
		for(i=0;i<nr_of_segments;i++)
		{
			for(j=0;j<points_per_segment;j++)
			{
				p1=points[i][j];
		
				tx=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
				ty=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
				tz=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];
				
				points[i][j]=new NurbsPoint(tx,ty,tz);
			}
		}
		
		//transform evaluated points

		double epsilon=0.001;
			
		for(i=0;i<nr_of_segments;i++)
		{
			for(u=0.0;u<1.0-epsilon;u=u+step)
			{
				for(j=0;j<points_per_segment;j++)
				{
					for(v=0.0;v<1.0-epsilon;v=v+step)
					{
						p1=evaluated_points[(int)(((i+u)/step)+epsilon)][(int)(((j+v)/step)+epsilon)];
			
						tx=m[0][0]*p1.x+m[0][1]*p1.y+m[0][2]*p1.z+t[0];
						ty=m[1][0]*p1.x+m[1][1]*p1.y+m[1][2]*p1.z+t[1];
						tz=m[2][0]*p1.x+m[2][1]*p1.y+m[2][2]*p1.z+t[2];
				
						evaluated_points[(int)(((i+u)/step)+epsilon)][(int)(((j+v)/step)+epsilon)]=new NurbsPoint(tx,ty,tz);
					}
				}
			}
		}
			

	}
	
	/**
	 * Calculates the surface normal at a given position
	 *
	 * @param i
	 * @param j
	 * @param myu
	 * @param myv
	 * @param bottom
	 * @return Vector3f
	 */	 	
	@Deprecated public Vector3f calculate_normal(int i, int j, double myu, double myv, boolean bottom)
	{
		double step2=0.01; //0.1
		NurbsPoint p,p1,p2,p3;

		double s=get_knot_at_segment(i);
		double t=get_knot_at_point(j);

		double s1=s+myu-step2;
		double s2=s+myu+step2;
		double t1=t+myv-step2;
		double t2=t+myv+step2;		
		
		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Point3f [] pts = new Point3f[3];
		
				
		if(s1<u[0])
			s1=u[0];
		if(s2>u[nr_of_segments-1])
			s2=u[nr_of_segments-1];
		if(t1<v[0])
			t1=v[0];
		if(t2>v[points_per_segment-1])
			t2=v[points_per_segment-1];

		for (int i2 = 0; i2 < 3; i2++) 
		    pts[i2] = new Point3f();

		p1=get_point_on_surface(s1, t1);
		p2=get_point_on_surface(s2, t1);
		p3=get_point_on_surface(s1, t2);
		
		pts[0] = new Point3f((float)p1.x, (float)p1.y, (float)p1.z);
		pts[1] = new Point3f((float)p2.x, (float)p2.y, (float)p2.z);
		pts[2] = new Point3f((float)p3.x, (float)p3.y, (float)p3.z);

		v1.sub(pts[0], pts[1]);
		v2.sub(pts[0], pts[2]);

		v1.normalize();
		v2.normalize();
		
		if(bottom)
		    normal.cross(v1, v2);
		else
		    normal.cross(v2, v1);

		normal.normalize();
					
		return normal;
	}
	
	/**
	 * Calculates the surface normal at a position given by the parameters
	 * s and t
	 *
	 * @param s
	 * @param t
	 * @return NurbsPoint the surface normal
	 */	 	
	public NurbsPoint getNormal(double s, double t)
	{
		double step2=0.01; //0.1
		NurbsPoint p,p1,p2,p3;

		//---this is a fix to compensate for bad models
		//---that create deep concaves in the tail and nose
		double step3=0.1;
		if(s<u[0]+2*step3)
			s=u[0]+2*step3;
		if(s>u[nr_of_segments-1]-2*step3)
			s=u[nr_of_segments-1]-step3;
		if(t<v[0]+step3)
			t=v[0]+step3;
		if(t>v[points_per_segment-1]-step3)
			t=v[points_per_segment-1]-step3;
		//---end fix

		double t1=t-step2/2;
		double t2=t+step2/2;
		double s1=s-step2/2;
		double s2=s+step2/2;		
		
		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Point3f [] pts = new Point3f[3];
		

		for (int i2 = 0; i2 < 3; i2++) 
		    pts[i2] = new Point3f();

		p1=get_point_on_surface(s1, t1);
		p2=get_point_on_surface(s2, t1);
		p3=get_point_on_surface(s1, t2);
		
		pts[0] = new Point3f((float)p1.x, (float)p1.y, (float)p1.z);
		pts[1] = new Point3f((float)p2.x, (float)p2.y, (float)p2.z);
		pts[2] = new Point3f((float)p3.x, (float)p3.y, (float)p3.z);

		v1.sub(pts[0], pts[1]);
		v2.sub(pts[0], pts[2]);

		v1.normalize();
		v2.normalize();
		
		normal.cross(v1, v2);

		normal.normalize();
					
		return new NurbsPoint(normal.x, normal.y, normal.z);
	}

	/**
	 * Gets the surface point at a position given by the parameters
	 * t and s
	 *
	 * @param s
	 * @param t
	 * @return NurbsPoint the surface point
	 */	 	
	public NurbsPoint getPoint(double s, double t)
	{
		double step2=0.001;
	
		if(s<u[0]+step2)
			s=u[0]+step2;
		if(s>u[nr_of_segments-1]-step2)
			s=u[nr_of_segments-1]-step2;
		if(t<v[0]+step2)
			t=v[0]+step2;
		if(t>v[points_per_segment-1]-step2)
			t=v[points_per_segment-1]-step2;
	
	
		return get_point_on_surface(s, t);
	}
	
	/**
	 * Gets the knot value at a given segment
	 *
	 * @param i the segment
	 * @return double the knot value
	 */	 	
	public double get_knot_at_segment(int i)
	{
		return u[i];
	}

	/**
	 * Gets the knot value at a given point
	 *
	 * @param i the point
	 * @return double the knot value
	 */	 	
	public double get_knot_at_point(int i)
	{
		return v[i];
	}
	
	/**
	 * Gets the maximum knot value for parameter t
	 *
	 * @return double the maximum knot value
	 */	 	
	public double getMaxT()
	{
		return v[points_per_segment-1];
	}
	
	/**
	 * Gets the minimum knot value for parameter t
	 *
	 * @return double the minimum knot value
	 */	 	
	public double getMinT()
	{
		return v[0];
	}
	
	/**
	 * Gets the maximum knot value for parameter s
	 *
	 * @return double the maximum knot value
	 */	 	
	public double getMaxS()
	{
		return u[nr_of_segments-1];
	}
	
	/**
	 * Gets the minimum knot value for parameter s
	 *
	 * @return double the minimum knot value
	 */	 	
	public double getMinS()
	{
		return u[0];
	}
	
	
	/**
	 * Inverts the surface normal
	 */	 	
	public void flipNormal()
	{
		int i,j;
		
		for(i=0; i<nr_of_segments; i++)
		{
			for(j=0; j<points_per_segment; j++)
			{
				points[i][j].z=-points[i][j].z;
			}
		}
		
		for(i=0;i<(int)((double)nr_of_segments/step);i++)
		{
			for(j=0;j<(int)((double)points_per_segment/step);j++)
			{
				evaluated_points[i][j].z=-evaluated_points[i][j].z;	
			}
		}
		
   	}
   	
	
	/**
	 * Clone the surface
	 *
	 * @return Object a cloned surface
	 */	 	
	public Object clone()
	{
		int i,j;
		
		NurbsSurface srf=new NurbsSurface(nr_of_segments, points_per_segment);

		for(i=0; i<nr_of_segments; i++)
		{
			for(j=0; j<points_per_segment; j++)
			{
				srf.set_control_point(i,j, get_control_point(i,j));
			}
		}
		
		srf.set_surface_type(surface_type);
		srf.evaluate_always(always_evaluate);	

		srf.u=u.clone();			
		srf.v=v.clone();			
		srf.k=k; 			
	
		for(i=0;i<(int)((double)nr_of_segments/step);i++)
		{
			for(j=0;j<(int)((double)points_per_segment/step);j++)
			{
				srf.evaluated_points[i][j]=evaluated_points[i][j];	
			}
		}
	
		srf.step=step;
		
		return srf;
			
	}

}

