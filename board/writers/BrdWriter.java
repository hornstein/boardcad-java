package board.writers;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import board.BezierBoard;
import boardcad.FileTools;

import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.BezierBoardCrossSection;

public class BrdWriter {
	static StringBuilder builder = new StringBuilder();

	static public boolean saveFile(BezierBoard brd, String aFilename)
	{
		try
		{
			aFilename = FileTools.setExtension(aFilename, "brd");

			File file = new File(aFilename);

			FileWriter writer = new FileWriter(file);

//			Write in same sequence as original .brd file(not that I believe it matters, but to make sure the files are identical)
			write(writer, 1, brd.getLength());
			write(writer, 2, brd.getLengthOverCurve());
			write(writer, 3, brd.getThickness());
			write(writer, 4, brd.getCenterWidth());
			write(writer, 7, brd.getVersion());
			write(writer, 8, brd.getName());
			write(writer, 45, brd.getDesigner());
			write(writer, 54, brd.getModel());
			write(writer, 55, brd.getAux1());
			write(writer, 56, brd.getAux2());
			write(writer, 57, brd.getAux3());
			write(writer, 43, brd.getMachineFolder());
			write(writer, 11, brd.getTopCuts());
			write(writer, 12, brd.getBottomCuts());
			write(writer, 13, brd.getRailCuts());
			write(writer, 14, brd.getCutterDiam());
			write(writer, 15, brd.getBlankPivot());
			write(writer, 16, brd.getBoardPivot());
			write(writer, 17, brd.getMaxAngle());
			write(writer, 18, brd.getNoseMargin());
			write(writer, 99, brd.getTailMargin());
			write(writer, 19, brd.getNoseLength());
			write(writer, 20, brd.getTailLength());
			write(writer, 21, brd.getDeltaXNose());
			write(writer, 22, brd.getDeltaXTail());
			write(writer, 23, brd.getDeltaXMiddle());
			write(writer, 24, brd.getToTailSpeed());
			write(writer, 25, brd.getStringerSpeed());
			write(writer, 42, brd.getStringerSpeedBottom());
			write(writer, 26, brd.getRegularSpeed());
			write(writer, 44, brd.getTopShoulderAngle());
			write(writer, 46, brd.getTopShoulderCuts());
			write(writer, 47, brd.getBottomRailCuts());
			write(writer, 38, brd.getCurrentUnits());
			write(writer, 53, brd.getSecurityLevel());
			write(writer, 41, brd.getShowOriginalBoard());
			write(writer, 48, brd.getSurfer());
			write(writer, 49, brd.getComments());
			write(writer, 51, brd.getFinType());
			write(writer, 50, brd.getFins());
			write(writer, 27, brd.getStrut1());
			write(writer, 28, brd.getStrut2());
			write(writer, 29, brd.getCutterStartPos());
			write(writer, 30, brd.getBlankTailPos());
			write(writer, 31, brd.getBoardStartPos());
			write(writer, "p32 : (\n", brd.getOutline(), brd.getOutlineGuidePoints());
			write(writer, "p33 : (\n", brd.getBottom(), brd.getBottomGuidePoints());
			write(writer, "p34 : (\n", brd.getDeck(), brd.getDeckGuidePoints());
			write(writer, 35, brd.getCrossSections());

			writer.close();

			brd.setFilename(aFilename);

		}
		catch(Exception e){
			String str = e.toString();
			System.out.printf("exception occured during save %s",  str);
			return false;
		}
		return true;

	}

	static public boolean exportOutline(BezierBoard brd, String aFilename)
	{
		try
		{
			aFilename = FileTools.setExtension(aFilename, "otl");

			File file = new File(aFilename);

			FileWriter writer = new FileWriter(file);

			write(writer, "p32 : (\n", brd.getOutline(), brd.getOutlineGuidePoints());

			writer.close();

			brd.setFilename(aFilename);

		}
		catch(Exception e){
			String str = e.toString();
			System.out.printf("exception occured during save %s",  str);
			return false;
		}
		return true;

	}

	static public boolean exportProfile(BezierBoard brd, String aFilename)
	{
		try
		{
			aFilename = FileTools.setExtension(aFilename, "pfl");

			File file = new File(aFilename);

			FileWriter writer = new FileWriter(file);

			write(writer, "p33 : (\n", brd.getBottom(), brd.getBottomGuidePoints());
			write(writer, "p34 : (\n", brd.getDeck(), brd.getDeckGuidePoints());

			writer.close();

			brd.setFilename(aFilename);

		}
		catch(Exception e){
			String str = e.toString();
			System.out.printf("exception occured during save %s",  str);
			return false;
		}
		return true;
	}

	static public boolean exportCrossection(BezierBoard brd, int index, String aFilename)
	{
		try
		{
			aFilename = FileTools.setExtension(aFilename, "crs");

			File file = new File(aFilename);

			FileWriter writer = new FileWriter(file);

			BezierBoardCrossSection current = brd.getCrossSections().get(index);
			write(writer, "(p36 " + Double.valueOf(current.getPosition()) + "\n", current.getBezierSpline(), current.getGuidePoints());

			writer.close();

			brd.setFilename(aFilename);

		}
		catch(Exception e){
			String str = e.toString();
			System.out.printf("exception occured during save %s",  str);
			return false;
		}
		return true;
	}

	static void resetBuilder()
	{
		builder.delete(0,builder.length());//Clean up
	}

	static void buildId(int id)
	{
		resetBuilder();
		builder.append("p");
		if(id < 10)
			builder.append("0");
		builder.append(Integer.valueOf(id));
		builder.append(" : ");

	}

	static void write(FileWriter writer, int id, String value)
	throws IOException
	{
		if(value == null || value.length() == 0)
			return;	//This seem to be the behaviour of aps3000/akushaper

		value = value.replaceAll("\n","\\\\n");

		buildId(id);

		builder.append(value);

		builder.append("\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, int id, int value)
	throws IOException
	{
		buildId(id);

		builder.append(Integer.valueOf(value));

		builder.append("\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, int id, double value)
	throws IOException
	{
		buildId(id);

		builder.append(Double.valueOf(value));

		builder.append("\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, int id, float value)
	throws IOException
	{
		buildId(id);

		builder.append(Double.valueOf(value));

		builder.append("\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, int id, boolean value)
	throws IOException
	{
		buildId(id);

		builder.append(Boolean.valueOf(value));

		builder.append("\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, int id, double[] values)
	throws IOException
	{
		buildId(id);

		builder.append("[");
		for(int i = 0; i < values.length; i++)
		{
			builder.append(Double.valueOf(values[i]));
			if(i != values.length-1)
				builder.append(",");
		}

		builder.append("]\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, BezierKnot knot)
	throws IOException
	{

		resetBuilder();
		builder.append("(cp [");

		for(int i = 0; i < 3; i++)
		{
			builder.append(Double.valueOf(knot.getPoints()[i].x));
			builder.append(",");
			builder.append(Double.valueOf(knot.getPoints()[i].y));
			if(i !=2)
				builder.append(",");
		}
		builder.append("] ");

		builder.append(knot.isContinous());

		builder.append(" ");

		builder.append(knot.getOther());

		builder.append(")\n");

		writer.append(builder);
	}

	static void write(FileWriter writer, Point2D.Double guidepoint)
	throws IOException
	{
		resetBuilder();
		builder.append("(gp [");

		builder.append(Double.valueOf(guidepoint.x));
		builder.append(",");
		builder.append(Double.valueOf(guidepoint.y));

		builder.append("])\n");

		writer.append(builder);
	}


	static void write(FileWriter writer, String id, BezierSpline knotArray, ArrayList<Point2D.Double> guidepointArray)
	throws IOException
	{
		writer.write(id);	//NOTE: A string is used here as this code is used
//		both for crossections and 'regular' bezier data
//		This is because the reegular have the form "p3x : ("
//		while a crosssection begins with "(p36 20.6"
//		Don't want to duplicate the rest of the code

		for(int i = 0; i < knotArray.getNrOfControlPoints(); i++)
		{
			write(writer, knotArray.getControlPoint(i));
		}

		if(guidepointArray.size() > 0)
		{

			writer.write("gps : (\n");

			for(int i = 0; i < guidepointArray.size(); i++)
			{
				write(writer, guidepointArray.get(i));
			}
			writer.write(")\n");

		}
		writer.write(")\n");

	}

	static void write(FileWriter writer, int id, ArrayList<BezierBoardCrossSection> crossSectionArray)
	throws IOException
	{
		buildId(id);

		builder.append("(\n");

		writer.append(builder);

		for(int i = 0; i < crossSectionArray.size(); i++)
		{
			BezierBoardCrossSection current = crossSectionArray.get(i);
			write(writer, "(p36 " + Double.valueOf(current.getPosition()) + "\n", current.getBezierSpline(), current.getGuidePoints());
		}

		writer.append(")\n");

	}

}