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

	@Override
	public void close()
	{
		
	}
	
	Graphics2D getGraphics2D()
	{
		return g;
	}
	
	@Override
	public void setColor(Color color)
	{
		g.setColor(color);
	}
	
	@Override
	public void setStroke(Stroke stroke)
	{
		g.setStroke(stroke);
	}
	
	@Override
	public void transform(AffineTransform transform)
	{
		g.transform(transform);
	}

	@Override
	public void setTransform(AffineTransform transform)
	{
		g.setTransform(transform);
	}

	@Override
	public AffineTransform getTransform()
	{
		return g.getTransform();
	}

	@Override
	public void draw(Line2D line)
	{
		g.draw(line);
	}
	
	@Override
	public void draw(Ellipse2D line)
	{
		g.draw(line);
	}
	
	@Override
	public void draw(CubicCurve2D curve)
	{
		g.draw(curve);		
	}
	
	@Override
	public void draw(GeneralPath path)
	{
		g.draw(path);		
	}
	
	@Override
	public void fill(Ellipse2D ellipse)
	{
		g.fill(ellipse);		
	}

	@Override
	public void fill(GeneralPath path)
	{
		g.fill(path);		
	}
	
	
	@Override
	public void moveTo(Point2D point)
	{
	}
}
