package boardcad.export;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import boardcad.*;


/*
 * Basic implementation for drawing in java
 */
class DxfDraw extends AbstractDraw
{	
	PrintStream mStream = null;
	AffineTransform mTransform = new AffineTransform();
	
	Vector<Point2D.Double> mVerticies = new Vector<Point2D.Double>();
	
	DxfDraw(String filename)
	{
		mTransform.setToIdentity();
		
		File file = new File(FileTools.setExtension(filename, "dxf"));

		try{
			mStream = new PrintStream(file);
		}
		catch(Exception e)
		{
			return;
		}

		DxfExport.writeHeader(mStream);
		
	}

	public void close()
	{
		writePolyLine();
	
		DxfExport.writeEndSequence(mStream);

		DxfExport.writeEndOfFile(mStream);
	
		mStream.close();
	
	}

	public void setColor(Color color)
	{
		//
	}
	
	public void setStroke(Stroke stroke)
	{
		//
	}
	
	public void transform(AffineTransform transform)
	{
		writePolyLine();

		mTransform.concatenate(transform);
	}
	
	public void setTransform(AffineTransform transform)
	{
		writePolyLine();

		mTransform = transform;
	}

	public AffineTransform getTransform()
	{
		return mTransform;
	}

	public void draw(Line2D line)
	{
		Point2D p1 = line.getP1();
		Point2D p2 = line.getP2();
		
		Point2D t1 = null;
		Point2D t2 = null;

		mTransform.transform(p1, t1);
		mTransform.transform(p2, t2);
		
		if(mVerticies.size() == 0 || !t1.equals(mVerticies.lastElement()))
			mVerticies.add(new Point2D.Double(t1.getX(), t1.getY()));

		mVerticies.add(new Point2D.Double(t2.getX(), t2.getY()));
		
	}
	
	public void draw(Ellipse2D line)
	{
		//TODO
	}
	
	public void draw(CubicCurve2D curve)
	{
		//TODO		
	}

	public void fill(Ellipse2D ellipse)
	{
	}

	public void moveTo(Point2D point)
	{
		writePolyLine();
		
		Point2D transformedPoint = null;

		mTransform.transform(point, transformedPoint);

		mVerticies.add(new Point2D.Double(transformedPoint.getX(), transformedPoint.getY()));
	}

	void writePolyLine()
	{
		if(mVerticies.size() == 0)
			return;
		
		DxfExport.writePolylineBegin(mStream, mVerticies.size());
		
		for(int i = 0; i < mVerticies.size(); i++)
		{
			DxfExport.writeVertex(mStream, mVerticies.get(i));
		}
	
		DxfExport.writePolylineEnd(mStream);
	
		mVerticies.clear();
	}

	@Override
	public void fill(GeneralPath path) {
		// TODO Auto-generated method stub
		
	}

}