package boardcad;

//TODO: This class uses java.awt.* which potentially does not work with Android SDK or GWT. Change interface to take generic parameters.
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;



/*
 * Abstraction of drawing rutines
 * Useful for 'printing' to other formats, like g-code, dxf, pdf, etc.
 * This way the same base functions can be used to print to several formats,
 * making it possible to reuse significan portions of code for less code to maintain
 * This is a minimum subset of Graphics2D for drawing a board
 * An alternative would be to extend Graphics2D for these operations,
 * however Graphics2D is quite extensive. By using a small subset of
 * Graphics2D the amount of code to maintain is kept at a minimum. 
 */
public abstract class AbstractDraw
{
	public abstract void close();
	
	public abstract void setColor(Color color);
	public abstract void setStroke(Stroke stroke);
	public abstract void transform(AffineTransform transform);
	public abstract void setTransform(AffineTransform transform);
	public abstract AffineTransform getTransform();

	public abstract void fill(Ellipse2D line);
	public abstract void fill(GeneralPath path);

	public abstract void draw(Line2D line);
	public abstract void draw(Ellipse2D line);
	public abstract void draw(CubicCurve2D curve);

	public void draw(GeneralPath path)
	{
		Point2D lastPoint = null;
		Point2D lastMoveToPoint = null;
		
		for(PathIterator it = path.getPathIterator(getTransform()); it.isDone(); it.next())
		{
			double[] coords = null; 
			int segmentType = it.currentSegment(coords);
			
			switch(segmentType)
			{
			    // The segment type constant that specifies that the preceding subpath should be closed by appending a line segment back to the point corresponding to the most recent SEG_MOVETO.
				case PathIterator.SEG_CLOSE:
				{
					Line2D line = new Line2D.Double(lastMoveToPoint, lastPoint);
					this.draw(line);
				}
				break;
					
			    // The segment type constant for the set of 3 points that specify a cubic parametric curve to be drawn from the most recently specified point.
				case PathIterator.SEG_CUBICTO:
				{
					CubicCurve2D bezier= new CubicCurve2D.Double(lastPoint.getX(), lastPoint.getY(), coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					this.draw(bezier);					
				}
				break;
			    
				// The segment type constant for a point that specifies the end point of a line to be drawn from the most recently specified point.
				case PathIterator.SEG_LINETO:
				{
					Point2D currentPoint = new Point2D.Double(coords[0], coords[1]);
					Line2D line = new Line2D.Double(lastPoint, currentPoint );
					this.draw(line);
					lastPoint = currentPoint;
				}
				break;
			
			    // The segment type constant for a point that specifies the starting location for a new subpath.
				case PathIterator.SEG_MOVETO:
				{
					lastPoint = new Point2D.Double(coords[0], coords[1]);
					lastMoveToPoint = new Point2D.Double(coords[0], coords[1]);
				}
				break;
			
			    // The segment type constant for the pair of points that specify a quadratic parametric curve to be drawn from the most recently specified point.			}
				case PathIterator.SEG_QUADTO:
				{
				}
				break;
			}
			
		}
				
	}

	public abstract void moveTo(Point2D point);
}

