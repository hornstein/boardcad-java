package cadcore;

import java.awt.geom.Point2D;

import javax.vecmath.Point3d;

public class MathUtils {
	
	public static double DEG_TO_RAD = Math.PI/180.0;


	public interface Function
	{
		double f(double x);
	}
	
	public interface FunctionXY
	{
		Point2D.Double f(double x);
	}

	public interface FunctionXYZ
	{
		Point3d f(double x);
	}

	public interface DerivableFunction extends Function
	{
		double f(double x);
		double fd(double x);
	}
	
	public interface TwoParamFunction
	{
		double f(double x, double y);
	}
	
	public interface TwoParamFunctionXY
	{
		Point2D.Double f(double x, double y);
	}

	public interface TwoParamFunctionXYZ
	{
		Point3d f(double x, double y);
	}

	public interface TwoParamDerivableFunction extends Function
	{
		double f(double x, double y);
		double fd(double x, double y);
	}

	public static double clamp(double value, double minLimit, double maxLimit)
	{
		if(value < minLimit)
			value = minLimit;
		
		if(value > maxLimit)
			value = maxLimit;
		
		return value;
	}

	public static class RootFinder
	{
		static public double ROOTFINER_VALUE_TOLERANCE = 0.001;
		
		static public double getRoot(Function function, double targetValue)
		{
			return getRoot(function, targetValue, 0.0, 1.0);
		}

		static public double getRoot(Function function, double targetValue, double minLimit, double maxLimit)
		{
			double x = SecantRootFinder.getRoot(function, targetValue, minLimit, maxLimit);

			//Sanity  check
			if(Math.abs(function.f(x)-targetValue) > ROOTFINER_VALUE_TOLERANCE)
			{
//				System.out.printf("getRoot(): SecantRootFinder failed, x:%f, value:%f, targetValue:%f, error: %f\n", x, function.f(x), targetValue, function.f(x)-targetValue);				
				
				x = BisectRootFinder.getRoot(function, targetValue, minLimit, maxLimit);
				
				if(Math.abs(function.f(x)-targetValue) > ROOTFINER_VALUE_TOLERANCE)
				{
//					System.out.printf("getRoot(): BisectRootFinder failed, x:%f, value:%f, targetValue:%f, error: %f\n", x, function.f(x), targetValue, function.f(x)-targetValue);				
				}
			}
			
			return x;
		}

		static public double getRoot(DerivableFunction function, double targetValue)
		{
			return getRoot(function, targetValue, 0.0, 1.0);
		}


		static public double getRoot(DerivableFunction function, double targetValue, double minLimit, double maxLimit)
		{
			double x = NewtonRaphsonRootFinder.getRoot(function, targetValue, minLimit, maxLimit);
			
			//Sanity  check
			if(Math.abs(function.f(x)-targetValue) > ROOTFINER_VALUE_TOLERANCE)
			{
//				System.out.printf("getRoot(): SecantRootFinder failed, error: %f\n", function.f(x)-targetValue);				
				
				x = BisectRootFinder.getRoot(function, targetValue, minLimit, maxLimit);
				
			}
			
			return x;
			
		}


		public static class NewtonRaphsonRootFinder
		{
			static private int NEWTONRAPHSON_MAX_ITERATIONS = 50;
			
			static public double getRoot(DerivableFunction function, double target_value, double minLimit, double maxLimit)
			{
				//Guess initial value 
				double valueAtMin = function.f(minLimit);
				double valueAtMax = function.f(maxLimit);

				double x = (target_value-valueAtMin)/(valueAtMax-valueAtMin);

				x = clamp(x,minLimit,maxLimit);
		
				double value = function.f(x);
		
				double error = target_value-value;
		//		double lasterror = error;
		
				int n = 0;
				while(Math.abs(error) > ROOTFINER_VALUE_TOLERANCE && n++ < NEWTONRAPHSON_MAX_ITERATIONS)
				{
					double currentSlope = 1/function.fd(x);
		
					x = x + (error*currentSlope);
		
					value = function.f(x);
		
					error = target_value-value;
				}
		
				return x;		
			}
		}
		
		public static class SecantRootFinder
		{
			static private int SECANT_MAX_ITERATIONS = 50;
			
			static public double getRoot(Function function, double target_value, double minLimit, double maxLimit)
			{
				double valueAtMin = function.f(minLimit);
				double valueAtMax = function.f(maxLimit);

				double x = (target_value-valueAtMin)/(valueAtMax-valueAtMin);
				x = clamp(x,minLimit,maxLimit);

				double last_x = x + (maxLimit-minLimit)/10.0;
				if(last_x > maxLimit)
					last_x = x - (maxLimit-minLimit)/10.0;
						
				//Search for the angle using secant method
				double current_value = function.f(x);
				double last_value = function.f(last_x);
			
				double current_error = target_value-current_value;
			
//				System.out.printf("SecantRootFinder.getRoot(): START current_error:%f, x:%f last_value:%f current_value:%f target_value:%f\n", current_error, x, last_value, current_value, target_value);
				
				int n = 0;
				while(Math.abs(current_error) > ROOTFINER_VALUE_TOLERANCE && n++ < SECANT_MAX_ITERATIONS && current_value != last_value)
				{					
					double d = ((x-last_x)/(current_value-last_value))*current_error;
			
					last_x = x;
					x = x + d;
			
					x = clamp(x,minLimit,maxLimit);

					last_value = current_value;
					current_value = function.f(x);
			
					current_error = target_value-current_value;
			
//					System.out.printf("SecantRootFinder.getRoot(): d:%f, current_error:%f, x:%f last_value:%f current_value:%f target_value:%f\n", d, current_error, x, last_value, current_value, target_value);
			
				}
//				System.out.printf("SecantRootFinder.getRoot(): target_value:%f, current_value:%f current_error:%f\n", target_value, current_error);
						
				return x;		
			}
				
		}
	
		public static class BisectRootFinder
		{
			static private int BISECT_MAX_ITERATIONS = 50;
			
			static public double getRoot(Function function, double target_value, double minLimit, double maxLimit)
			{

				int n = 0;
				double lt = minLimit;
				double ht = maxLimit;

				double l_error = function.f(lt)-target_value;
				double h_error = function.f(ht)-target_value;
				
//				if(l_error*h_error > 0)
//					return 0.0;	//NO ROOT WITHIN LIMITS? FUNCTION NOT SUITABLE FOR BISECT METHOD
				
				double current_error = 100000000;
				double x = 0;
				while(Math.abs(current_error) > ROOTFINER_VALUE_TOLERANCE && n++ < BISECT_MAX_ITERATIONS && ht-lt > 0.0001)
				{
					x = (ht+lt)/2.0;
		
					double current_value = function.f(x);
					current_error = current_value-target_value;
					
					if(current_error*l_error < 0.0)	//if these two positions have the opposite sign, then the root must be within the this span
					{
//						System.out.printf("low increased\n");
						ht = x;
						h_error = current_error;
					}
					else
					{
//						System.out.printf("high decreased\n");
						lt = x;
						l_error = current_error;
					}
					
//					System.out.printf("BisectRootFinder.getRoot(): current_error:%f, x:%f lt:%f ht:%f current_value:%f target_value:%f \n", current_error, x, lt, ht, current_value, target_value);
				}
//				System.out.printf("BisectRootFinder.getRoot(): n:%d\n", n);
							
				return x;		
			}
				
		}
	}
	
	public static class Integral
	{
		static public double getIntegral(Function function, double min, double max, int splits)
		{
			return SimpsonsRuleIntegral.getIntegral(function, min, max, splits);
		}

		static public double getIntegral(FunctionXY function, double min, double max, int splits)
		{
			return TrapezoidRuleIntegral.getIntegral(function, min, max, splits);
		}
		
	
		public static class SimpsonsRuleIntegral
		{
			public static double getIntegral(Function func, double min, double max, double splits)
			{
				double result = 0;
		
				double m = (max-min)/splits;
		
				double an = min;
		
				double x0 = func.f(an);

				for(int n = 0; n < splits; n++)
				{	
					double am = an+m;
					
					double x1 = func.f((an+am)/2);
					double x2 = func.f(am);
			
					if(Double.isNaN(x0))
					{
						x0 = 0;
					}
					if(Double.isNaN(x1))
					{
						x1 = 0;
					}
					if(Double.isNaN(x2))
					{
						x2 = 0;
					}

					double sum = ((am-an)/6)*(x0 + (4*x1) + x2);
					
					result += sum;
		
					an += m;
					x0 = x2;
				}
		
				return result;
			}
		
		}
		
		public static class TrapezoidRuleIntegral
		{
			static double getIntegral(FunctionXY func, double min, double max, int splits)
			{
				double result = 0;
		
				double m = (max-min)/splits;
		
				double an = min;
		
				Point2D.Double x0 = func.f(an);

				for(int n = 0; n < splits; n++)
				{	
					an = an+m;
					
					Point2D.Double x1 = func.f(an);
					
					result += ((x0.y + x1.y)/2.0) * Math.abs(x1.x-x0.x);

					x0 = x1;
				}
		
				return result;
			}
		
		}

	}
	
	public static class CurveLength
	{
		static public double getCurveLength(FunctionXY function, double min, double max, int splits)
		{
			float length = 0;
			Point2D.Double last = function.f(min);
			double step = (max-min)/splits;
			for(int i = 1; i < splits+1; i++)
			{
				double x = min+(i*step);
				Point2D.Double current = function.f(x);
				length += VecMath.getVecLength(last.x, last.y, current.x, current.y);
				last = current;
			}
			
			return length;
		}
		
		static public double getCurveLength(FunctionXY function, double min, double max)
		{
			return getCurveLength(function, min, max, 10);
		}
	}
}
