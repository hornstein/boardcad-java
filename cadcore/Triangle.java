package cadcore;
//=========================================================Triangle
/*
 * Implements a triangle with normal
 */

public class Triangle implements Cloneable
{
	public NurbsPoint[] vertices;
    public NurbsPoint normal;

	public Triangle()
	{
        vertices=new NurbsPoint[3];
        for(int i=0; i<2; i++)
        {
            vertices[i]=new NurbsPoint(0.0, 0.0, 0.0);
        }
        
        normal=new NurbsPoint(0.0, 0.0, 0.0);
        
	}
	   
	/**
	 * Clone the point
	 *
	 * @return Object a cloned point
	 */	 	
	public Object clone()
	{
        Triangle mtriangle=new Triangle();
        mtriangle.vertices[0]=(NurbsPoint)vertices[0].clone();
        mtriangle.vertices[1]=(NurbsPoint)vertices[1].clone();
        mtriangle.vertices[2]=(NurbsPoint)vertices[2].clone();
        mtriangle.normal=(NurbsPoint)normal.clone();
        
		return mtriangle;
	}
	
}
