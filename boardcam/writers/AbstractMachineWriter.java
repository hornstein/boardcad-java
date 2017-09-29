package boardcam.writers;

import java.io.PrintStream;

import javax.vecmath.Vector3d;

public abstract class AbstractMachineWriter {
	abstract public void writeMetric(PrintStream stream);
	abstract public void writeAbsoluteCoordinateMode(PrintStream stream);
	abstract public void writePause(PrintStream stream, double seconds);
	abstract public void writeComment(PrintStream stream, String comment);
	abstract public void writeToolOn(PrintStream stream);
	abstract public void writeEnd(PrintStream stream);
	abstract public void writeToolOff(PrintStream stream);
	abstract public void writeSpeed(PrintStream stream, int speed);
	abstract public void writeBeginGoTo(PrintStream stream);
	abstract public void writeBeginJogTo(PrintStream stream);
	abstract public void writeCoordinate(PrintStream stream, double x, double y, double z);
	abstract public void writeCoordinate(PrintStream stream, Vector3d coordinate);
	abstract public void writeCoordinate(PrintStream stream, double[] coordinate);

	public void writeZCoordinate(PrintStream stream, double z){};
	
	public void writeHome(PrintStream stream, double[] coordinate){}
	public void writeHomeRef(PrintStream stream, double[] coordinate){}

	public void writeScript(PrintStream stream, String script)
	{
		stream.append(script);
	}
}
