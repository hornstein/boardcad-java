package cadcore;
//=========================================================myPoint
/*
 * Our implementation of a 3D point
 */

public class NurbsPoint implements Cloneable
{
	public double x; 
	public double y;
	public double z;
	public double w; //weight
	public int i;
	public int j;
	public double u;
	public double v;
	public String name;

	public NurbsPoint(double x_value, double y_value, double z_value)
	{
		x=x_value;
		y=y_value;
		z=z_value;
		w=1.0;
		name=new String("");
	}
	
	public NurbsPoint(double x_value, double y_value, double z_value, double w_value)
	{
		x=x_value;
		y=y_value;
		z=z_value;
		w=w_value;
		name=new String("");
	}
	
	/**
	 * Clone the point
	 *
	 * @return Object a cloned point
	 */	 	
	public Object clone()
	{
		NurbsPoint mpoint=new NurbsPoint(x,y,z,w);
		mpoint.i=i;
		mpoint.j=j;
		mpoint.u=u;
		mpoint.v=v;
		mpoint.name=new String(name);
		
		return mpoint;
	}
	
}
