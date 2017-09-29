package boardcam.writers;

import java.io.PrintStream;

import javax.vecmath.Vector3d;

import boardcam.MachineConfig;
import boardcam.toolpathgenerators.ext.CoordinateScaling;

public class GCodeWriterWithScaling extends GCodeWriter{
		
		protected CoordinateScaling mCoordinateScaling;

		public GCodeWriterWithScaling(MachineConfig config)
		{
			mCoordinateScaling = new CoordinateScaling(config);
		}
		
		public void writeCoordinate(PrintStream stream, double x, double y, double z)
		{
			super.writeCoordinate(stream, x*mCoordinateScaling.getXScale(), y*mCoordinateScaling.getYScale(), z*mCoordinateScaling.getZScale());
		}

		public void writeCoordinate(PrintStream stream, Vector3d coordinate)
		{
			this.writeCoordinate(stream, coordinate.x,coordinate.y,coordinate.z);
		}

		public void writeCoordinate(PrintStream stream, double[] coordinate)
		{
			double[] scales = mCoordinateScaling.getScales();
			double[] scaledCoordinate = new double[coordinate.length];
			
			for(int i = 0; i < coordinate.length; i++)
			{
				scaledCoordinate[i] = coordinate[i]*scales[i];
			}
			super.writeCoordinate(stream, scaledCoordinate);
		}
		
}
