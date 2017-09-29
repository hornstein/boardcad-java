package boardcam.writers;

import java.io.PrintStream;

import javax.vecmath.Vector3d;

import boardcam.MachineConfig;
import boardcam.toolpathgenerators.ext.CoordinateScaling;

public class GCodeWriter extends AbstractMachineWriter{	

	private static String  mAxis = "XYZAF";
	
	public GCodeWriter()
	{
	}
	
	public void writeMetric(PrintStream stream)
	{
		stream.printf("G21\n");
	}

	public void writeAbsoluteCoordinateMode(PrintStream stream)
	{
		stream.printf("G90\n");
	}

	public void writePause(PrintStream stream, double seconds)
	{
		stream.printf("G04 P%f\n", seconds);
	}

	public void writeComment(PrintStream stream, String comment)
	{
		String[] list = comment.split("\n");
		
		for(int i = 0; i < list.length; i++)
		{
			stream.printf("(%s)\n", list[i]);
		}
	}

	public void writeToolOn(PrintStream stream)
	{
		stream.printf("M3\n");
	}

	public void writeEnd(PrintStream stream)
	{
		stream.printf("M2\n");
	}

	public void writeToolOff(PrintStream stream)
	{
		stream.printf("M5\n");
	}

	public void writeSpeed(PrintStream stream, int speed)
	{
		stream.printf("F%d\n", speed); 
	}

	public void writeBeginGoTo(PrintStream stream)
	{
		stream.printf("G01 ");
	}

	public void writeBeginJogTo(PrintStream stream)
	{
		stream.printf("G00 ");
	}

	public void writeCoordinate(PrintStream stream, double x, double y, double z)
	{
		stream.printf("X%4.4f Y%4.4f Z%4.4f\n", x, y, z);
	}

	public void writeCoordinate(PrintStream stream, Vector3d coordinate)
	{
		writeCoordinate(stream, coordinate.x,coordinate.y,coordinate.z);
	}

	public void writeCoordinate(PrintStream stream, double[] coordinate)
	{
		if(coordinate.length == 3)	
		{
			this.writeCoordinate(stream, coordinate[0],coordinate[1],coordinate[2]);	//Better performance in case of 3 axis
		}
		else
		{
			for(int i = 0; i < coordinate.length && i < mAxis.length(); i++)	//Flexible but slow
			{
				stream.printf("%c%4.4f%s", mAxis.charAt(i), coordinate[i], i==coordinate.length-1?"":" ");
			}
			stream.printf("\n");
		}
	}
	
	public void writeZCoordinate(PrintStream stream, double z)
	{
		stream.printf("Z%4.4f\n", z);
	}

	public void writeHome(PrintStream stream, double[] coordinate)
	{
		stream.printf("G28 ");
		if(coordinate == null)
		{
			stream.printf("\n");			
		}
		else
		{
			writeCoordinate(stream, coordinate);
		}
	}
	
	public void writeHomeRef(PrintStream stream, double[] coordinate)
	{
		stream.printf("G28.1 ");
		if(coordinate == null)
		{
			stream.printf("\n");			
		}
		else
		{
			writeCoordinate(stream, coordinate);
		}		
	}
}
