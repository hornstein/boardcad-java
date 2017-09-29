package board;

import javax.vecmath.Vector3d;
import cadcore.*;

public class MeshBoard extends AbstractBoard 
{

	public Triangle[] triangle_array;
	public int triangle_count;

	public NurbsPoint point;
	public NurbsPoint normal;

	/**
	 * Creates a new board
	 */
	public MeshBoard()
	{
		triangle_count=0;
	}

	public double getMinX()
	{
		double min_x=99999;
		NurbsPoint p;

		for(int i=0; i<triangle_count; i++)
		{
			for(int j=0; j<3; j++)
			{
	                	p=triangle_array[i].vertices[j];
				if(p.x<min_x)
					min_x=p.x;
			}   			
		}
		return min_x;
	}

	public double getMaxX()
	{
		double max_x=0.0;
		NurbsPoint p;

		for(int i=0; i<triangle_count; i++)
		{
			for(int j=0; j<3; j++)
			{
	                	p=triangle_array[i].vertices[j];
				if(p.x>max_x)
					max_x=p.x;
			}   			
		}
		return max_x;
	}

	public void calculatePointAndNormal(double x, double z)
	{

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
		return getMaxX()-getMinX();
	}

	public double getWidthAt(double x) 
	{
		double width=0;
		double z;
		NurbsPoint p1, p2, p3;

                for(int i=0;i<triangle_count;i++)
                {
 	                for(int j=0;j<3;j++)
	                {
				p1=triangle_array[i].vertices[j];
				p2=triangle_array[i].vertices[(j+1)%3];
				
				if(p1.x>p2.x)
				{
					p3=p1;
					p1=p2;
					p2=p3;
				}
				
				if(p1.x==x)
				{
					if(p1.z>width)
						width=p1.z;
				}

				if(p2.x==x)
				{
					if(p2.z>width)
						width=p2.z;
				}
				
				if(p1.x<x && p2.x>x)
				{
					z=( (p2.x-x)*p1.z + (x-p1.x)*p2.z ) / (p2.x-p1.x);
					if(z>width)
						width=z;
				}
			}

		}

		return width;
	}

	@Override
	public double getDeckAt(double x, double y) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getBottomAt(double x, double y) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Vector3d getDeckNormalAt(double x, double y) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3d getBottomNormalAt(double x, double y) 
	{
		// TODO Auto-generated method stub
		return null;
	}

}
