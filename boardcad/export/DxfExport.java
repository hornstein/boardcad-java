package boardcad.export;

import boardcad.FileTools;
import cadcore.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.PrintStream;


public class DxfExport {
	
	static final long serialVersionUID=1L;
	static public final double SCALE_DXF_TO_SI = 10; //was metres (0.01), output to mm.

	static int q = 100;

	static public void exportBezierSplines(String filename, BezierSpline[] beziers)
	{
		try
		{

			File file = new File(FileTools.setExtension(filename, "dxf"));

			PrintStream stream = new PrintStream(file);

			writeHeader(stream);
			
			for(int i = 0; i < beziers.length; i++)
			{
				writeBezierSpline(stream, beziers[i]);
			}

			writeEndSequence(stream);

			writeEndOfFile(stream);

			stream.close();

		}catch(Exception e)
		{
			System.out.println("Exception in DxfExport::exportBezierSplines(): " + e.toString());
		}
	}

	static public void exportPolyline(String filename, Point2D.Double[] points)
	{
		try
		{

			File file = new File(FileTools.setExtension(filename, "dxf"));

			PrintStream stream = new PrintStream(file);

			writeHeader(stream);
			
			writePolylineBegin(stream, points.length);
			
			for(int i = 0; i < points.length; i++)
			{
				writeVertex(stream, points[i]);
			}

			writeEndOfFile(stream);

			stream.close();

		}catch(Exception e)
		{
			System.out.println("Exception in DxfExport::exportBezierSplines(): " + e.toString());
		}
	}

	static public void exportPolylineFromSplines(String filename,  BezierSpline[] beziers, int splitsPrSpline)
	{
		Point2D.Double point = new Point2D.Double();
		
		try
		{

			File file = new File(FileTools.setExtension(filename, "dxf"));

			PrintStream stream = new PrintStream(file);

			writeHeader(stream);
			
			writePolylineBegin(stream, splitsPrSpline*beziers.length);
			
			for(int i = 0; i < beziers.length; i++)
			{
				BezierSpline current = beziers[i];

				for(int j = 0; j < current.getNrOfControlPoints()-1; j++)
				{
					BezierCurve curve = current.getCurve(j);

					for(int k = 0; k < splitsPrSpline; k++)
					{
						double t = (double)k/(double)splitsPrSpline;
						point.x = curve.getXValue(t);
						point.y = curve.getYValue(t);
						writeVertex(stream, point);
					}
				}
			}
			
			writePolylineEnd(stream);

			writeEndOfFile(stream);

			stream.close();

		}
			catch(Exception e)
		{
			System.out.println("Exception in DxfExport::exportBezierSplines(): " + e.toString());
		}
	}

	static public void writeComment(PrintStream stream, String comment)
	{
		stream.println("999");
		stream.println(comment);
	}

	static public void writeHeader(PrintStream stream)
	{
		stream.println("999");
		stream.println("Dxf export from BoardCAD");
		stream.println("0");
		stream.println("SECTION");
		stream.println("2");

/* dxf R14 header version 
		stream.println("HEADER");
		stream.println("9");
		stream.println("$ACADVER");
		stream.println("  1");
		stream.println("AC1014");
		stream.println("0");
		stream.println("ENDSEC");
		stream.println("0");
		stream.println("SECTION");
		stream.println("2");
*/
		stream.println("ENTITIES");
		stream.println("0");

	}

	static public void writePolylineBegin(PrintStream stream, int vertexCount)
	{
/* dxf R14 version buggy
		stream.println("LWPOLYLINE");
		stream.println(" 5");
		stream.println(Integer.toString(q++).toUpperCase());
		stream.println(" 100");
		stream.println("AcDbEntity");
		stream.println(" 100");
		stream.println("AcDbPolyline");
		stream.println(" 90");
		stream.println(Integer.toString(vertexCount));
		stream.println(" 8");
		stream.println("0");	//Layer
		stream.println(" 6");
		stream.println("CONTINUOUS");
		stream.println(" 62");
		stream.println("9");	//Color number
*/		

		
// dxf R12 version
		stream.println("POLYLINE");
		stream.println(" 62");
		stream.println("9");
		stream.println(" 66");
		stream.println("1");
		stream.println(" 8");
		stream.println("0");
		stream.println(" 6");
		stream.println("CONTINUOUS");
		stream.println(" 0");

	}

	static public void writePolylineEnd(PrintStream stream)
	{
// dxf r14		stream.println(" 0");
		stream.println("SEQEND");
		stream.println(" 0");
		stream.println("ENDSEC");
	}
	
	static public void writeVertex(PrintStream stream, Point2D.Double point)
	{
/* dxf r14 version
		stream.println("10");
		stream.println(Double.toString(point.x*SCALE_DXF_TO_SI));
		stream.println("20");
		stream.println(Double.toString(point.y*SCALE_DXF_TO_SI));
*/
		
// dxf R12 version
		stream.println("VERTEX");
		stream.println(" 62");
		stream.println("9");
		stream.println(" 8");
		stream.println("0");
		stream.println(" 6");
		stream.println("CONTINUOUS");
		stream.println("10");
		stream.println(Double.toString(point.x*SCALE_DXF_TO_SI));
		stream.println("20");
		stream.println(Double.toString(point.y*SCALE_DXF_TO_SI));
		stream.println("0");

	}

	static public void writeEndSequence(PrintStream stream)
	{
		stream.println("ENDSEC");
	}


	static public void writeBezierSpline(PrintStream stream, BezierSpline beziers)
	{
		for(int i = 0; i < beziers.getNrOfControlPoints()-1; i++)
		{
			stream.println("SPLINE");
			stream.println(" 5");
			stream.println(Integer.toString(q++).toUpperCase());
			stream.println(" 8");
			stream.println("0");
/* dxf R14 ??			stream.println("100");
			stream.println("AcDbEntity");
			stream.println("100");
			stream.println("AcDbSpline");
*/			stream.println(" 62");
			stream.println("9");	//Color number
			stream.println(" 70");
			stream.println("8");
			stream.println(" 71");
			stream.println("3");
			stream.println(" 72");
			stream.println("8");
			stream.println(" 73");
			stream.println("4");
			stream.println(" 74");
			stream.println("0");
			stream.println(" 40");
			stream.println("0");
			stream.println(" 40");
			stream.println("0");
			stream.println(" 40");
			stream.println("0");
			stream.println(" 40");
			stream.println("0");
			stream.println(" 40");
			stream.println("1");
			stream.println(" 40");
			stream.println("1");
			stream.println(" 40");
			stream.println("1");
			stream.println(" 40");
			stream.println("1");

			BezierKnot startpoint = beziers.getControlPoint(i);
			stream.println(" 10");
			stream.println(Double.toString(startpoint.getEndPoint().x*SCALE_DXF_TO_SI));
			stream.println(" 20");
			stream.println(Double.toString(startpoint.getEndPoint().y*SCALE_DXF_TO_SI));
			stream.println(" 30");
			stream.println(Double.toString(0));

			stream.println(" 10");
			stream.println(Double.toString(startpoint.getTangentToNext().x*SCALE_DXF_TO_SI));
			stream.println(" 20");
			stream.println(Double.toString(startpoint.getTangentToNext().y*SCALE_DXF_TO_SI));
			stream.println(" 30");
			stream.println(Double.toString(0));

			BezierKnot endpoint = beziers.getControlPoint(i+1);

			stream.println(" 10");
			stream.println(Double.toString(endpoint.getTangentToPrev().x*SCALE_DXF_TO_SI));
			stream.println(" 20");
			stream.println(Double.toString(endpoint.getTangentToPrev().y*SCALE_DXF_TO_SI));
			stream.println(" 30");
			stream.println(Double.toString(0));

			stream.println(" 10");
			stream.println(Double.toString(endpoint.getEndPoint().x*SCALE_DXF_TO_SI));
			stream.println(" 20");
			stream.println(Double.toString(endpoint.getEndPoint().y*SCALE_DXF_TO_SI));
			stream.println(" 30");
			stream.println(Double.toString(0));

			stream.println(" 0");
		}


	}

	static public void writeEndOfFile(PrintStream stream)
	{
		stream.append("0\n");
		stream.append("EOF\n");

	}
}
