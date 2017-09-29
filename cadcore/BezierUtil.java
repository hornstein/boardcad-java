package cadcore;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class BezierUtil {
	
	static double POS_TOLERANCE = 0.002;	//0.02mm
	static double LENGTH_TOLERANCE = 0.001;	
	static int X = 0;
	static int Y = 1;
	static int MIN = 0;
	static int MAX = 1;
	
	static double[] coeff = new double[8];
	static double t[] = new double[4];

	static double curvature(ArrayList<BezierKnot> bezierKnots, double pos)
	{
		int index = -1;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			double lx = bezierKnots.get(i).getEndPoint().x;
			double ux = bezierKnots.get(i+1).getEndPoint().x;
			if(lx <= pos && ux >= pos)
			{
				index = i;
				break;
			}
		}
		
		if(index == -1)
			return 0.0;
		
		BezierKnot k0 = bezierKnots.get(index);
		BezierKnot k1 = bezierKnots.get(index+1);
		
		return getCurvature(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), pos);
		
	}

	public static double value(ArrayList<BezierKnot> bezierKnots, double pos)
	{
		int index = -1;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			double lx = bezierKnots.get(i).getEndPoint().x;
			double ux = bezierKnots.get(i+1).getEndPoint().x;
			if(lx <= pos && ux >= pos)
			{
				index = i;
				break;
			}
		}
		
		if(index == -1)
			return 0.0;
		
		BezierKnot k0 = bezierKnots.get(index);
		BezierKnot k1 = bezierKnots.get(index+1);
		
		return getYForX(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), pos);
	}
	
	static double valueReverse(ArrayList<BezierKnot> bezierKnots, double pos)
	{
		int index = -1;
		
		for(int i = bezierKnots.size()-1; i > 0 ; i--)
		{
			double lx = bezierKnots.get(i).getEndPoint().x;
			double ux = bezierKnots.get(i-1).getEndPoint().x;
			if(lx <= pos && ux >= pos)
			{
				index = i;
				break;
			}
		}
		
		if(index == -1)
			return 0.0;
		
		BezierKnot k0 = bezierKnots.get(index-1);
		BezierKnot k1 = bezierKnots.get(index);
		
		calculateCoeff(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToNext(), k1.getEndPoint());

		//Guess initial t 
		double t = 0;
				
		t = getTForX(pos,t);
		
		return getYValue(t);		
//		return getYForX(k0.getEndPoint(), k0.getTangent2(), k1.getTangent1(), k1.getEndPoint(), pos);
	}
	
	static double maxX(ArrayList<BezierKnot> bezierKnots)
	{
		double max = -100000;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			BezierKnot k0 = bezierKnots.get(i);
			BezierKnot k1 = bezierKnots.get(i+1);

			double current = getMinMaxNumerical(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), X, MAX);
			
			if(current > max)
				max = current;
		}
		
		return max;
	}

	static double minX(ArrayList<BezierKnot> bezierKnots)
	{
		double min = 100000;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			BezierKnot k0 = bezierKnots.get(i);
			BezierKnot k1 = bezierKnots.get(i+1);

			double current = getMinMaxNumerical(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), X, MIN);
					
			if(current < min)
				min = current;
		}
		
		return min;
	}
	
	static double maxY(ArrayList<BezierKnot> bezierKnots)
	{
		double max = -100000;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			BezierKnot k0 = bezierKnots.get(i);
			BezierKnot k1 = bezierKnots.get(i+1);

			double current = getMinMaxNumerical(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), Y, MAX);
			
			if(current > max)
				max = current;
		}
		
		return max;
	}

	static double minY(ArrayList<BezierKnot> bezierKnots)
	{
		double min = 100000;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			BezierKnot k0 = bezierKnots.get(i);
			BezierKnot k1 = bezierKnots.get(i+1);

			double current = getMinMaxNumerical(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), Y, MIN);
			
			
			if(current < min)
				min = current;
		}
		
		return min;
	}
	
	static double length(ArrayList<BezierKnot> bezierKnots)
	{
		double length = 0;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			BezierKnot k0 = bezierKnots.get(i);
			BezierKnot k1 = bezierKnots.get(i+1);

			length += getLength(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint());
			
		}
		
		return length;
		
	}
	
	static int getSplitKnot(ArrayList<BezierKnot> bezierKnots, Point2D.Double nearPoint, BezierKnot returned)
	{
		double nearestDist = 100000000;
		int index = 0;
		double t = 0;
		
		for(int i = 0; i < bezierKnots.size()-1; i++)
		{
			BezierKnot k0 = bezierKnots.get(i);
			BezierKnot k1 = bezierKnots.get(i+1);

			double tc = getClosestT(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), nearPoint);

			double x = getXValue(tc);
			double y = getYValue(tc);
			
			double dist = getVecLength(x, y, nearPoint.x, nearPoint.y);
			if(nearestDist > dist)
			{
				nearestDist = dist;
				index = i;
				t = tc;
			}
			
		}
		
		BezierKnot k0 = bezierKnots.get(index);
		BezierKnot k1 = bezierKnots.get(index+1);
		getSplitKnot(k0.getEndPoint(), k0.getTangentToNext(), k1.getTangentToPrev(), k1.getEndPoint(), t, returned);
		
		return index+1;	//Insertion point
		
	}
	
	static void calculateCoeff(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3)
	{
		//	MORE ON CUBIC SPLINE MATH
		//  =========================
		//  by  Don Lancaster
		
		//	In graph space, the cubic spline is defined by eight points. A pair of
		//  initial points x0 and y0. A pair of end points x3 and y3. A pair of
		//  first influence points x1 and y1. And a pair of second influence points
		//  x2 and y2.
		
		//	A cubic spline consists of two parametric equations in t (or time) space...

		//                  x = At^3 + Bt^2 + Ct + D
		//                  y = Dt^3 + Et^2 + Ft + G 
		
		//	Cubing can be a real pain, so the above equations can be rewritten in a
		//  "cubeless" form that calculates quickly...

		//                  x = ((At + B)t + C)t + D
		//                  y = ((Dt + E)t + F)t + G
		
		double A = p3.x - (3*t2.x) + (3*t1.x) - p0.x;	//A =  x3 - 3x2 + 3x1 - x0    
	    double B = (3*t2.x) - (6*t1.x) + (3*p0.x);		//B = 3x2 - 6x1 + 3x0          
	    double C = (3*t1.x) - (3*p0.x);					//C = 3x1 - 3x0                
	    double D = p0.x;								//D =  x0                       
	  		 	    
	    double E = p3.y - (3*t2.y) + (3*t1.y) - p0.y;	//E =  y3 - 3y2 + 3y1 - y0   
	    double F = (3*t2.y) - (6*t1.y) + (3*p0.y);		//F = 3y2 - 6y1 + 3y0
	    double G = (3*t1.y) - (3*p0.y);					//G = 3y1 - 3y0
	    double H = p0.y;								//H =  y0

	    coeff[0] = A;
	    coeff[1] = B;
	    coeff[2] = C;
	    coeff[3] = D;
	    coeff[4] = E;
	    coeff[5] = F;
	    coeff[6] = G;
	    coeff[7] = H;    
	}
	
	static double getXValue(double t)
	{
		//                  x = At^3 + Bt^2 + Ct + D
		//                  x = ((At + B)t + C)t + D
		return (((((coeff[0]*t) + coeff[1])*t) + coeff[2])*t) + coeff[3];
	}
	
	static double getYValue(double t)
	{
		//                  y = Dt^3 + Et^2 + Ft + G 
		//                  y = ((Dt + E)t + F)t + G
		return (((((coeff[4]*t) + coeff[5])*t) + coeff[6])*t) + coeff[7];
	}
	
	static double getXDerivate(double t)
	{
		//If our curve has an equation of... 
		//
		//                   x = At^3 + Bt^2 + Ct + D
		//
		//                            ...its slope will be...
		//
		//                  x' = 3At^2 + 2Bt + C
		//                  x' = (3At + 2B)t + C
		return ((((3*coeff[0])*t) + (2*coeff[1]))*t) + coeff[2];
	}	
	
	static double getYDerivate(double t)
	{
		//If our curve has an equation of... 
		//
		//                   y = Et^3 + Ft^2 + Gt + H
		//
		//                            ...its slope will be...
		//
		//                  y' = 3Et^2 +2Ft + G
		//                  y' = (3Et + 2F)t + G
		return ((((3*coeff[4])*t) + (2*coeff[5]))*t) + coeff[6];
	}
	
	static double getXSecondDerivate(double t)
	{
		return (6*coeff[0]*t) + (2*coeff[1]);
	}	
	
	static double getYSecondDerivate(double t)
	{
		return (6*coeff[4]*t) + (2*coeff[5]);
	}	
	
	static double getYForX(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, double x)
	{	
		calculateCoeff(p0,t1,t2,p3);
		//  I don't know how to find an exact and closed solution to finding y given
		//  x. You first have to use x to solve for t and then you solve t for y.

		// One useful way to do this is to take a guess for a t value. See what x
		//  you get. Note the error. Reduce the error and try again. Keep this up
		//  till you have a root to acceptable accuracy.
		//
		//  A good first guess is to normalize x so it ranges from 0 to 1 and then
		//  simply guess that x = t. This will be fairly close for curves that aren't
		//  bent very much. And a useful guess for ALL spline curves.
		
		//Guess initial t 
		double t = (x-p0.x)/(p3.x-p0.x);
				
		t = getTForX(x,t);
		
		return getYValue(t);
	}
	
	static double getTForX(double x, double start_t)
	{
		//  I don't know how to find an exact and closed solution to finding y given
		//  x. You first have to use x to solve for t and then you solve t for y.

		// One useful way to do this is to take a guess for a t value. See what x
		//  you get. Note the error. Reduce the error and try again. Keep this up
		//  till you have a root to acceptable accuracy.
		//
		//  A good first guess is to normalize x so it ranges from 0 to 1 and then
		//  simply guess that x = t. This will be fairly close for curves that aren't
		//  bent very much. And a useful guess for ALL spline curves.
		
		//Guess initial t 
		double tn = start_t;
				
		// Now, on any triangle...
		//
		//                      rise = run x (rise/run)
		//
		//  This gives us a very good improvement for our next approximation. It
		//  turns out that the "adjust for slope" method converges very rapidly.
		//  Three passes are usually good enough.              
		//
		//  If our curve has an equation of... 
		//
		//                   x = At^3 + Bt^2 + Ct + D
		//
		//                            ...its slope will be...
		//
		//                  x' = 3At^2 +2Bt + C
		//
		//  And the dt/dx slope will be its inverse or 1/(3At^2 + 2Bt +C)
		//
		//  This is easily calculated. We'll have code and an example in just a bit. 
		//
		//  The next guess will be...
		//
		//             nextguess = currentt + (curentx - x)(currentslope)
		double xn = getXValue(tn);
		
		double error = x-xn;
		
	
		while(Math.abs(error) > POS_TOLERANCE)
		{
			double currentSlope = 1/getXDerivate(tn);

			tn = tn + (error*currentSlope);
			
			xn = getXValue(tn);
			
			error = x-xn;
		}
		
		return tn;		
	}
	
	//NOTE: This function does not work. While it returns the extreme values based on the derivate, 
	//it falls outside of the [0,1] area of the bezier so no good. 
	//In reality we should have the function as y(x) instead of y(t)
	//Solve the problem numerically instead
	static void getTForExtremeValues(int XorY)
	{
		int offset = (XorY == X)?0:4;
		//Max/Min when derivate is zero
		
		//Derivate is  y' = 3At^2 + 2Bt + C
		//Which is a second degree equation
		//			 f(x) = ax^2 + bx + c
		//so           x = (-b +/- sqrt(b^2 - 4ac))/2a
		
		double a = 3*coeff[0+offset];	//a = 3A or 3E (offset should be 0 for x or 4 for y)
		double b = 2*coeff[1+offset];	// b = 2B or 2F 
		double c = coeff[2+offset];	// c = C or G
		
		double sqrtPart = Math.sqrt((b*b) - (4*a*c));
		
		
		if(a != 0.0)
		{
			t[0] = (-b - sqrtPart)/2*a;
			t[1] = (-b + sqrtPart)/2*a;
		}
		else
		{
			t[0] = 0.0;
			t[1] = 0.0;
		}
		
	}

	//NOTE: This function does not work. While it returns the extreme values based on the derivate, 
	//it falls outside of the [0,1] area of the bezier so no good. 
	//In reality we should have the function as y(x) instead of y(t)
	//Solve the problem numerically instead
	static double getMaxValue(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, int XorY)
	{
		calculateCoeff(p0,t1,t2,p3);

		getTForExtremeValues(XorY);

		double max = 0.0;
		
		double value;
		
		t[2] = 0;
		t[3] = 1;
		
		for(int i = 0; i < 4; i++)
		{
			if(t[i] < 0 || t[i] > 1)
				continue;
			
			if(XorY == X)
			{
				value = getXValue(t[i]);
			}
			else
			{
				value = getYValue(t[i]);				
			}
			
			if(value > max)
				max = value;			
		}
		
		return max;		
	}
	
	//NOTE: This function does not work. While it returns the extreme values based on the derivate, 
	//it falls outside of the [0,1] area of the bezier so no good. 
	//In reality we should have the function as y(x) instead of y(t)
	//Solve the problem numerically instead
	static double getMinValue(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, int XorY)
	{
		calculateCoeff(p0,t1,t2,p3);

		getTForExtremeValues(XorY);

		double min = 1000000.0;
		
		double value;
		
		t[2] = 0;
		t[3] = 1;
		
		for(int i = 0; i < 4; i++)
		{
			if(t[i] < 0 || t[i] > 1)
				continue;

			if(XorY == X)
			{
				value = getXValue(t[i]);
			}
			else
			{
				value = getYValue(t[i]);				
			}
			
			if(value < min)
				min = value;			
		}
		
		return min;		
	}
	
	static double getMinMaxNumerical(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, int XorY, int MinOrMax)
	{
		calculateCoeff(p0,t1,t2,p3);

		return getMinMaxNumerical(0, 1, 32, XorY, MinOrMax);		
	}
	
	static double getMinMaxNumerical(double t0, double t1, int nrOfSplits,int XorY, int MinOrMax)
	{
		double best_t = 0;
		double best_value = (MinOrMax==MAX)?-10000000:10000000;
		
		double current_t;
		double current_value;
		double seg = ((t1-t0)/nrOfSplits);
		for(int i = 0; i < nrOfSplits; i++)
		{
			current_t = seg*i + t0;
			if(current_t < 0 || current_t > 1)
				continue;
			
			current_value = (XorY==X)?getXValue(current_t):getYValue(current_t);
			
			if(MinOrMax==MAX)
			{
				if(current_value >= best_value)
				{
					best_value = current_value;
					best_t = current_t;
				}
			}
			else
			{
				if(current_value <= best_value)
				{
					best_value = current_value;
					best_t = current_t;
				}	
			}
		}
		if((best_t - ((t1-t0)/2)) < 0.001)
			return best_value;
		else if(nrOfSplits <= 2)
			return best_value;
		else
			return getMinMaxNumerical(best_t-seg,best_t+seg, nrOfSplits/2,XorY,MinOrMax);
	}
	
	static double getClosestT(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, Point2D.Double point)
	{
		calculateCoeff(p0,t1,t2,p3);

		return getClosestT(0, 1, 32, point.x, point.y);		
	}
	
	static double getClosestT(double t0, double t1, int nrOfSplits, double x, double y)
	{
		double best_t = 0;
		double min_dist = 1000000000;
		
		double current_t;
		double current_x;
		double current_y;
		double current_dist;
		double seg = ((t1-t0)/nrOfSplits);
		for(int i = 0; i < nrOfSplits; i++)
		{
			current_t = seg*i + t0;
			if(current_t < 0 || current_t > 1)
				continue;
			
			current_x = getXValue(current_t);
			current_y = getYValue(current_t);
			
			current_dist = getVecLength(current_x, current_y, x, y);

			if(current_dist <= min_dist)
			{
				min_dist = current_dist;
				best_t = current_t;
			}

		}
		if((best_t - ((t1-t0)/2)) < 0.001)
			return best_t;
		else if(nrOfSplits <= 2)
			return best_t;
		else
			return getClosestT(best_t-seg,best_t+seg, nrOfSplits/2, x, y);
	}
	
	static double getLength(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3)
	{
		calculateCoeff(p0,t1,t2,p3);

		return getLength(0, 1);		
	}
	
	static double getLength(double t0, double t1)
	{
		//Get endpoints
		double x0 = getXValue(t0);
		double y0 = getYValue(t0);
		double x1 = getXValue(t1);
		double y1 = getYValue(t1);
			
		//Get t split point
//		double ts = getClosestT(t0,t1, 32, cx, cy);
		double ts = (t1-t0)/2 + t0;
		double sx = getXValue(ts);
		double sy = getYValue(ts);
		
		//Distance between centerpoint and real split curvepoint
		double length = getVecLength(x0,y0,sx,sy) + getVecLength(sx,sy,x1,y1);
		double chord  = getVecLength(x0,y0,x1,y1);
		
		if(length - chord > LENGTH_TOLERANCE)
		{
			return getLength(t0, ts) + getLength(ts, t1);
		}
		else
		{
			return Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));			
		}
		
		
	}
	
	static double getCurvature(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, double pos)
	{
		calculateCoeff(p0,t1,t2,p3);

		return getCurvature(getTForX(pos, (pos-p0.x)/(p3.x-p0.x)));		
	}
	static double getCurvature(double t)
	{
		//K(t) = (x'y" - y'x") / (x'^2 + y'^2)^(3/2)
		
		double dx = getXDerivate(t);
		double dy = getYDerivate(t);
		
		double ddx = getXSecondDerivate(t);
		double ddy = getYSecondDerivate(t);
		
		return ((dx*ddy) - (dy*ddx))/Math.pow((dx*dx) + (dy*dy), 1.5);
	}
	static void getSplitKnot(Point2D.Double p0, Point2D.Double t1, Point2D.Double t2, Point2D.Double p3, double t, BezierKnot ret)
	{
		//Split using de Casteljau's algorithm
		
		Point2D.Double q1 = new Point2D.Double();
		Point2D.Double q2 = new Point2D.Double();
		Point2D.Double q3 = new Point2D.Double();
		
		subVector(p0,t1,q1);	
		scaleVector(q1, t);
		addVector(p0,q1,q1);
		
		subVector(t1,t2,q2);	
		scaleVector(q2, t);
		addVector(t1,q2,q2);
		
		subVector(t2,p3,q3);	
		scaleVector(q3, t);
		addVector(t2,q3,q3);
		
		Point2D.Double r1 = new Point2D.Double();
		Point2D.Double r2 = new Point2D.Double();
		Point2D.Double r3 = new Point2D.Double();
		
		subVector(q1,q2,r2);	
		scaleVector(r2, t);
		addVector(q1,r2,r2);
		
		subVector(q2,q3,r3);	
		scaleVector(r3, t);
		addVector(q2,r3,r3);
		
		subVector(r2,r3,r1);	
		scaleVector(r1, t);
		addVector(r2,r1,r1);
		
		ret.getPoints()[0].setLocation(r1);
		ret.getPoints()[1].setLocation(r2);
		ret.getPoints()[2].setLocation(r3);
		
	}

	static double getVecLength(double x0, double y0, double x1, double y1)
	{
		double dx = x0-x1;
		double dy = y0-y1;
		
		return Math.sqrt((dx*dx)+(dy*dy));
	}
	
	static void subVector(Point2D.Double p0, Point2D.Double p1, Point2D.Double ret)
	{
		ret.setLocation(p1.x-p0.x, p1.y-p0.y);
	}
	
	static void addVector(Point2D.Double p0, Point2D.Double p1, Point2D.Double ret)
	{
		ret.setLocation(p1.x+p0.x, p1.y+p0.y);
	}
		
	static void scaleVector(Point2D.Double p0, double v)
	{
		p0.setLocation(p0.x*v,p0.y*v);
	}
	
}

