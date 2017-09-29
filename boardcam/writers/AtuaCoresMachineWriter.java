package boardcam.writers;

import java.io.PrintStream;
import javax.vecmath.Vector3d;

import boardcam.writers.AbstractMachineWriter;

public class AtuaCoresMachineWriter extends AbstractMachineWriter {

	@Override
	public void writeAbsoluteCoordinateMode(PrintStream stream) {
		//NOT USED
	}

	@Override
	public void writeBeginGoTo(PrintStream stream) {
		//NOT USED
	}

	@Override
	public void writeBeginJogTo(PrintStream stream) {
		//NOT USED
	}

	@Override
	public void writeComment(PrintStream stream, String comment) {
		stream.printf("%s\n", comment);		
	}

	@Override
	public void writeCoordinate(PrintStream stream, double x, double y, double z) {
		stream.printf("%.8f                  %.8f\n", x, z);
	}

	@Override
	public void writeCoordinate(PrintStream stream, Vector3d coordinate) {
		stream.printf("%.8f                  %.8f\n", coordinate.x, coordinate.z);
	}

	@Override
	public void writeCoordinate(PrintStream stream, double[] coordinate) {
		stream.printf("%.8f                  %.8f\n", coordinate[0], coordinate[2]);
	}

	@Override
	public void writeEnd(PrintStream stream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeMetric(PrintStream stream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writePause(PrintStream stream, double seconds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeSpeed(PrintStream stream, int speed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeToolOff(PrintStream stream) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeToolOn(PrintStream stream) {
		// TODO Auto-generated method stub

	}
}
