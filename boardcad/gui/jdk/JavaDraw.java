package boardcad.gui.jdk;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import boardcad.AbstractDraw;


/*
 * Basic implementation for drawing in java
 */
public class JavaDraw extends AbstractDraw
{
	private Graphics2D g;
	
	public JavaDraw(Graphics2D graphics)
	{
		g = graphics;
	}

	public void close()
	{
		
	}
	
	Graphics2D getGraphics2D()
	{
		return g;
	}
	
	public void setColor(Color color)
	{
		g.setColor(color);
	}
	
	public void setStroke(Stroke stroke)
	{
		g.setStroke(stroke);
	}
	
	public void transform(AffineTransform transform)
	{
		g.transform(transform);
	}

	public void setTransform(AffineTransform transform)
	{
		g.setTransform(transform);
	}

	public AffineTransform getTransform()
	{
		return g.getTransform();
	}

	public void draw(Line2D line)
	{
		g.draw(line);
	}
	
	public void draw(Ellipse2D line)
	{
		g.draw(line);
	}
	
	public void draw(CubicCurve2D curve)
	{
		g.draw(curve);		
	}
	
	public void draw(GeneralPath path)
	{
		g.draw(path);		
	}
	
	public void fill(Ellipse2D ellipse)
	{
		g.fill(ellipse);		
	}

	public void fill(GeneralPath path)
	{
		g.fill(path);		
	}
	
	
	public void moveTo(Point2D point)
	{
	}
}
