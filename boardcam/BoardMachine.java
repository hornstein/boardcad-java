package boardcam;

import cadcore.*;
import boardcad.ScriptLoader;

import javax.vecmath.*;
import java.io.*;
import java.util.*;

import boardcam.cutters.STLCutter;

/**
 * Machine interface for cutting boards (nurbs surfaces)
 * Works for 3 and 4 axis machines
 * Requires the file shapebot.properties to be located in BoardCAD's home dir
 * Also requires an stl-model of the cutter to be located in the same dir
 */
 
public class BoardMachine
{
	public static NurbsSurface deck;
	public static NurbsSurface bottom;

	public static String deckFileName=new String("deck.ngc");
	public static String bottomFileName=new String("deck.ngc");

	public static String deckScript;
	public static String bottomScript;

	public static int deckCuts;
	public static int deckRailCuts;
	public static int bottomCuts;
	public static int bottomRailCuts;
	public static double deckRailAngle;
	public static double bottomRailAngle;

	public static double speed;
	public static double stringerSpeed;
	public static double outlineSpeed;

	public static boolean cutStringer;
	public static double stringerOffset;
	public static double stringerCutoff;
	public static double outlineOffset;

	public static String toolName;
	public static double toolScaleX;
	public static double toolScaleY;
	public static double toolScaleZ;

	public static double zMaxHeight;

	public static double[][] machineCoordinates;
	public static double[] endSupportPosition;
	public static NurbsPoint support1;
	public static double support1_radius;
	public static NurbsPoint support2;
	public static double support2_radius;	

	public static NurbsPoint scan_coord;
	public static double scan_comp;
	public static String scan_path;
	public static NurbsPoint axis4_offset;
	public static double axis4_offsetRotation;
	public static double axis4_rail_start;
	public static double axis4_rail_stop;

//	public boolean check_collision;
//	public String collision_toolname;
//	public double collision_toolscale_x;
//	public double collision_toolscale_y;
//	public double collision_toolscale_z;
//	public double collision_offset_z;
	
	public static NurbsPoint[] bottom_cut=new NurbsPoint[100000];
	public static boolean[] bottom_collision=new boolean[100000];
	public static NurbsPoint[] deck_cut=new NurbsPoint[100000];
	public static boolean[] deck_collision=new boolean[100000];
	public static int nr_of_cuts_bottom=0;
	public static int nr_of_cuts_deck=0;

	
/*	public BoardMachine()
	{
		bottom_cut=new NurbsPoint[100000];
		bottom_collision=new boolean[100000];
		deck_cut=new NurbsPoint[100000];
		deck_collision=new boolean[100000];
		nr_of_cuts_bottom=0;
		nr_of_cuts_deck=0;
		deckFileName=new String("deck.ngc");
		bottomFileName=new String("bottom.ngc");
		
		read_machine_data();
	}
*/	
	
	/**
	 * Creates the gcode for the deck surface.
	 * 
	 * @param filename
	 */
	public static void generateGCodeDeck(String filename) 
	{
		read_machine_data();
		deckFileName=new String(filename);
		ScriptLoader.loadScript(deckScript);
	}




	/**
	 * Create gcode for the bottom
	 * 
	 * @param filename
	 */		
	public static void generateGCodeBottom(String filename)
	{
		read_machine_data();
		bottomFileName=new String(filename);
		ScriptLoader.loadScript(bottomScript);
	}

	/**
	 * Reads the config file for the machine.
	 */
	public static void read_machine_data() {
		// File file = new File ("shapebot.txt");
		Properties properties = new Properties();
		String setting = null;
		try {
			properties.load(new FileInputStream("shapebot.properties"));
		} catch (IOException e) {
			System.out.println("shapebot.properties error: " + e);
		}

		try {
			// This string is used to read the setting from the properties file.
			// If a String-to-double parse fails, an exception will occur and
			// if the string is set to correct value the exception message will
			// tell what string that failed.

			// Script files for toolpath generation
			deckScript = properties.getProperty("toolpathGenerator.deckScript");
			bottomScript = properties.getProperty("toolpathGenerator.bottomScript");

			// Define number of cuts
			setting = "Define number of cuts";
			deckCuts = Integer.parseInt(properties.getProperty("g.deckCuts"));
			deckRailCuts = Integer.parseInt(properties.getProperty("g.deckRailCuts"));
			bottomCuts = Integer.parseInt(properties.getProperty("g.bottomCuts"));
			bottomRailCuts = Integer.parseInt(properties.getProperty("g.bottomRailCuts"));
			deckRailAngle = Double.parseDouble(properties.getProperty("g.deckRailAngle"));
			bottomRailAngle = Double.parseDouble(properties.getProperty("g.bottomRailAngle"));

			// Speed for cutting the board
			setting = "Speed for cutting the board";
			speed = Double.parseDouble(properties.getProperty("g.speed"));
			stringerSpeed = Double.parseDouble(properties.getProperty("g.stringerSpeed"));
			outlineSpeed = Double.parseDouble(properties.getProperty("g.outlineSpeed"));

			// Define extra parameters
			setting = "Define extra parameters";
			cutStringer = (Integer.parseInt(properties.getProperty("g.cutStringer")) != 0);
			stringerOffset=Double.parseDouble(properties.getProperty("g.stringerOffset"));
			stringerCutoff=Double.parseDouble(properties.getProperty("g.stringerCutoff"));
			outlineOffset=Double.parseDouble(properties.getProperty("g.outlineOffset"));

			// Define cutter
			setting = "Define cutter";
			toolName = properties.getProperty("g.toolName");
			toolScaleX = Double.parseDouble(properties.getProperty("g.toolScaleX"));
			toolScaleY = Double.parseDouble(properties.getProperty("g.toolScaleY"));
			toolScaleZ = Double.parseDouble(properties.getProperty("g.toolScaleZ"));

			// Safe height for traversing, transport etc.
			setting = "Safe height for traversing, transport etc.";
			zMaxHeight = Double.parseDouble(properties.getProperty("g.zMaxHeight"));


			// End support
			setting = "End support";
			double[] t = {Double.parseDouble(properties.getProperty("machine.supportEndX")),
					Double.parseDouble(properties.getProperty("machine.supportEndY")),
					Double.parseDouble(properties.getProperty("machine.supportEndZ")) };
			endSupportPosition = t;
			// The Machine Coordinates to BoardCad coordinates matrix.
			setting = "Machine coordinates to Boardcad coordinates";
			double[][] m = {getAxisArray(properties.getProperty("machine.axisForBoardcadX")),
					getAxisArray(properties.getProperty("machine.axisForBoardcadY")),
					getAxisArray(properties.getProperty("machine.axisForBoardcadZ")) };
			machineCoordinates = m;


			// Machine settings
			setting = "Support 1 length and width";
			support1 = new NurbsPoint( Double.parseDouble(properties.getProperty("machine.support1.distance")), 
						Double.parseDouble(properties.getProperty("machine.support1.widthBetweenSupports"))/2+t[1], 
						Double.parseDouble(properties.getProperty("machine.support1.height")));
			setting = "support 1 radius";
			support1_radius = Double.parseDouble(properties.getProperty("machine.support1.radius"));
			setting = "Support 2 length and width";
			support2 = new NurbsPoint( Double.parseDouble(properties.getProperty("machine.support2.distance")), 
						Double.parseDouble(properties.getProperty("machine.support2.widthBetweenSupports"))/2+t[1], 
						Double.parseDouble(properties.getProperty("machine.support2.height")));
			setting = "support 2 radius";
			support2_radius = Double.parseDouble(properties.getProperty("machine.support2.radius"));


			setting = "scanner";
			scan_coord=new NurbsPoint( Double.parseDouble(properties.getProperty("scanner.supportEndX")),
						Double.parseDouble(properties.getProperty("scanner.supportEndY")),
						Double.parseDouble(properties.getProperty("scanner.supportEndZ")));

			scan_path = properties.getProperty("scanner.path");
			
			// 4-axis settings
			setting = "4 axis";

			axis4_offset=new NurbsPoint(Double.parseDouble(properties.getProperty("machine.axis4.offsetX")),
						Double.parseDouble(properties.getProperty("machine.axis4.offsetY")),
						Double.parseDouble(properties.getProperty("machine.axis4.offsetZ")));

			axis4_offsetRotation = Double.parseDouble(properties.getProperty("machine.axis4.offsetRotation"));
			
			
//			setting = "collision test";
//			check_collision = (Double.parseDouble(properties.getProperty("g.checkCollision")) != 0);
//			collision_toolname = properties.getProperty("g.collisionToolName");
//			collision_toolscale_x = Double.parseDouble(properties.getProperty("g.collisionToolScaleX"));
//			collision_toolscale_y = Double.parseDouble(properties.getProperty("g.collisionToolScaleY"));
//			collision_toolscale_z = Double.parseDouble(properties.getProperty("g.collisionToolScaleZ"));
//			collision_offset_z = Double.parseDouble(properties.getProperty("g.collisionOffsetZ"));



			// The exception can be either a null pointer or number format,
			// catch both with a new message telling which parameter that
			// failed.
		} catch (Exception e) {
			System.out.println(new StringBuilder(
					"shapebot.properties error at parameter ").append(setting)
					.append(": ").append(e).toString());
		}

	}


	/**
	 * Returns an array with the values for the transform array
	 * 
	 * @param axis
	 * @return array with transform values for the given axis
	 */
	private static double[] getAxisArray(String axis) {
		double[] array = { 0, 0, 0 };
		if (axis.equals("X"))
			array[0] = 1;
		else if (axis.equals("-X"))
			array[0] = -1;
		else if (axis.equals("Y"))
			array[1] = 1;
		else if (axis.equals("-Y"))
			array[1] = -1;
		else if (axis.equals("Z"))
			array[2] = 1;
		else if (axis.equals("-Z"))
			array[2] = -1;
		return array;

	}
		/**
	 * Transform point from machine coordinates to BoardCAD coordinates
	 * 
	 * @param p
	 * @return NurbsPoint
	 */		
	public static NurbsPoint machine2boardcad_coordinate(NurbsPoint p)
	{
	
		return new NurbsPoint(machineCoordinates[0][0]*(p.x-endSupportPosition[0])+machineCoordinates[1][0]*(p.y-endSupportPosition[1])+machineCoordinates[2][0]*(p.z-endSupportPosition[2]),
		                   machineCoordinates[0][1]*(p.x-endSupportPosition[0])+machineCoordinates[1][1]*(p.y-endSupportPosition[1])+machineCoordinates[2][1]*(p.z-endSupportPosition[2]),
		                   machineCoordinates[0][2]*(p.x-endSupportPosition[0])+machineCoordinates[1][2]*(p.y-endSupportPosition[1])+machineCoordinates[2][2]*(p.z-endSupportPosition[2]));

	}

	
}
	
