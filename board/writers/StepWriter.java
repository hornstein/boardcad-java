package board.writers;

import cadcore.*;
import cadcore.BezierBoardCrossSection;
import board.*;

import java.io.*;
import java.util.*;


public class StepWriter {

	/**
	 * Saves a board using the new step format
	 *
	 * @param dataOut - output stream for writing data
	 * @param filename - the name of the file
	 */
	static public void saveFile(PrintStream dataOut, String filename, NurbsBoard board, BezierBoard brd)
	{
		int deck_start;
		int bottom_start;

		NurbsSurface deck=board.getDeck();
		NurbsSurface bottom=board.getBottom();
		
		bottom.flipNormal();
		deck.flipNormal();
		
		NurbsPoint x_axis_direction=new NurbsPoint(0.0, 0.0, 0.0);
		NurbsPoint y_axis_direction=new NurbsPoint(0.0, 0.0, 0.0);
		NurbsPoint local_origin=new NurbsPoint(0.0, 0.0, 0.0);

		board.get_cartesian_transformation_operator(local_origin, x_axis_direction, y_axis_direction);

		Calendar now=Calendar.getInstance();
		
		//BezierBoard brd=BoardCAD.getInstance().getCurrentBrd();
	
		dataOut.println("ISO-10303-21;");
		dataOut.println("HEADER;");
		dataOut.println("FILE_DESCRIPTION (( 'STEP AP203' ), '1' );");
		dataOut.println("FILE_NAME ('" + filename + "','" + now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" +now.get(Calendar.DAY_OF_MONTH) + "T" + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + "',( '' ),( '' ),'BCstep 1.0','BoardCAD','');");
		dataOut.println("FILE_SCHEMA (( 'CONFIG_CONTROL_DESIGN' ));");
		dataOut.println("ENDSEC;");
		dataOut.println("DATA;");


		//write deck surface

		int points=deck.get_nr_of_points();
		int segments=deck.get_nr_of_segments();

		int k=200;
		deck_start=k;
		
		dataOut.println("#" + k +"=(");
		dataOut.println("BOUNDED_SURFACE()");
		dataOut.println("B_SPLINE_SURFACE(3,3,(");
		

		for(int i=1;i<=segments-4;i++)
		{
			dataOut.print("(");
			for(int j=2;j<points-3;j++)    // points-4;j>=2;j--)
			{
				dataOut.print("#" + (k+(i-1)*(points-4)+j-1) + ", ");
			}
			if(i==segments-4)
			{
				dataOut.println("#" + (k+(i-1)*(points-4)+points-4) + ")), ");
			}
			else
			{
				dataOut.println("#" + (k+(i-1)*(points-4)+points-4) + "), ");
			}
		}
				
		
		dataOut.println(".UNSPECIFIED.,.F.,.F.,.F.)");
		dataOut.println("B_SPLINE_SURFACE_WITH_KNOTS(");

		dataOut.print("( 4,");
		for(int i=0;i<segments-8;i++)
			dataOut.print(" 1,");
		dataOut.println(" 4),");

		dataOut.print("( 4,");
		for(int i=0;i<points-8;i++)
			dataOut.print(" 1,");
		dataOut.println(" 4),");

		dataOut.print("( ");
		for(int i=0;i<deck.get_nr_of_segments()-7;i++)
		{
			dataOut.print("" + deck.get_knot_at_segment(i+3) + ", ");
		}
		dataOut.println(""+ deck.get_knot_at_segment(deck.get_nr_of_segments()-3) + " ),");
		
		dataOut.print("( ");
		for(int i=0;i<deck.get_nr_of_points()-7;i++)
		{
			dataOut.print("" + deck.get_knot_at_point(i+3) + ", ");
		}
		dataOut.println(""+ deck.get_knot_at_point(deck.get_nr_of_points()-3) + " ),");

		dataOut.println(".UNSPECIFIED.)");
		dataOut.println("GEOMETRIC_REPRESENTATION_ITEM()");
		dataOut.println("RATIONAL_B_SPLINE_SURFACE((");

		for(int i=1;i<=segments-4;i++)
		{
			dataOut.print("(");
			for(int j=points-4;j>=2;j--)
			{
				dataOut.print("1.0, ");
			}
			if(i==segments-4)
			{
				dataOut.println("1.0))) ");
			}
			else
			{
				dataOut.println("1.0), ");
			}
		}
		dataOut.println("REPRESENTATION_ITEM('deck')");
		dataOut.println("SURFACE()");
		dataOut.println(");");



		k=201;
		for(int i=2;i<deck.get_nr_of_segments()-2;i++)
		{
			for(int j=2;j<deck.get_nr_of_points()-2;j++)
			{
				NurbsPoint value=deck.get_control_point(i,j);
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(value.x) + ", " + Double.toString(value.y) + ", " + Double.toString(value.z) + ") );");
				k=k+1;
			}
		}


		//write bottom
		
		
		points=bottom.get_nr_of_points();
		segments=bottom.get_nr_of_segments();

		k++;
		int k2=k; 

		bottom_start=k;

		
		dataOut.println("#" + k +"=(");
		dataOut.println("BOUNDED_SURFACE()");
		dataOut.println("B_SPLINE_SURFACE(3,3,(");
		for(int i=1;i<=segments-4;i++)
		{
			dataOut.print("(");
			for(int j=2;j<points-3;j++)    // points-4;j>=2;j--)
			{
				dataOut.print("#" + (k2+(i-1)*(points-4)+j-1) + ", ");
			}
			if(i==segments-4)
			{
				dataOut.println("#" + (k2+(i-1)*(points-4)+points-4) + ")), ");
			}
			else
			{
				dataOut.println("#" + (k2+(i-1)*(points-4)+points-4) + "), ");
			}
		}

		dataOut.println(".UNSPECIFIED.,.F.,.F.,.F.)");
		dataOut.println("B_SPLINE_SURFACE_WITH_KNOTS(");
		
		dataOut.print("( 4,");
		for(int i=0;i<segments-8;i++)
			dataOut.print(" 1,");
		dataOut.println(" 4),");

		dataOut.print("( 4,");
		for(int i=0;i<points-8;i++)
			dataOut.print(" 1,");
		dataOut.println(" 4),");

		dataOut.print("( ");
		for(int i=0;i<bottom.get_nr_of_segments()-7;i++)
		{
			dataOut.print("" + bottom.get_knot_at_segment(i+3) + ", ");
		}
		dataOut.println(""+ bottom.get_knot_at_segment(bottom.get_nr_of_segments()-3) + " ),");

		dataOut.print("( ");
		for(int i=0;i<bottom.get_nr_of_points()-7;i++)
		{
			dataOut.print("" + bottom.get_knot_at_point(i+3) + ", ");
		}
		dataOut.println(""+ bottom.get_knot_at_point(bottom.get_nr_of_points()-3) + " ),");

		dataOut.println(".UNSPECIFIED.)");
		dataOut.println("GEOMETRIC_REPRESENTATION_ITEM()");
		dataOut.println("RATIONAL_B_SPLINE_SURFACE((");

		for(int i=1;i<=segments-4;i++)
		{
			dataOut.print("(");
			for(int j=points-4;j>=2;j--)
			{
				dataOut.print("1.0, ");
			}
			if(i==segments-4)
			{
				dataOut.println("1.0))) ");
			}
			else
			{
				dataOut.println("1.0), ");
			}
		}
		dataOut.println("REPRESENTATION_ITEM('bottom')");
		dataOut.println("SURFACE()");
		dataOut.println(");");		


		k++;
		for(int i=2;i<bottom.get_nr_of_segments()-2;i++)
		{
			for(int j=2;j<bottom.get_nr_of_points()-2;j++)
			{
				NurbsPoint value=bottom.get_control_point(i,j);
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(value.x) + ", " + Double.toString(value.y) + ", " + Double.toString(value.z) + ") );");
				k=k+1;
			}
		}


		bottom.flipNormal();
		deck.flipNormal();
		
		//writing transformation operator
		
		dataOut.println("#" + k + " = CARTESIAN_TRANSFORMATION_OPERATOR( '','','',#" + (k+1) + ", #" + (k+2) + ", #" + (k+3) +",1.0);");
		k++;
		dataOut.println("#" + k + " = DIRECTION ( 'x axis',  ( " + Double.toString(x_axis_direction.x) + ", " + Double.toString(x_axis_direction.y) + ", " + Double.toString(x_axis_direction.z) + ") );");
		k++;
		dataOut.println("#" + k + " = DIRECTION ( 'y axis',  ( " + Double.toString(y_axis_direction.x) + ", " + Double.toString(y_axis_direction.y) + ", " + Double.toString(y_axis_direction.z) + ") );");
		k++;
		dataOut.println("#" + k + " = CARTESIAN_POINT ( 'local origin',  ( " + Double.toString(local_origin.x) + ", " + Double.toString(local_origin.y) + ", " + Double.toString(local_origin.z) + ") );");
		k++;
		

		//writing bezier curves
		
		//outline
		
		BezierSpline outline=brd.getOutline();
		
		dataOut.print("#" + k + " = BEZIER_CURVE( 'outline',3,(");
		
		for(int i=0;i<3*outline.getNrOfControlPoints()-2;i++)
		{ 
			dataOut.print("#" + (k+i+1));
			
			if(i<3*outline.getNrOfControlPoints()-3)
			{
				dataOut.print(",");
			}
			else
			{
				dataOut.println("),.UNSPECIFIED.,.F.,.F.);");
			}
		}
		
		BezierKnot knot;
		
		for(int i=0;i<outline.getNrOfControlPoints();i++)
		{ 
			knot=outline.getControlPoint(i);
			
			if(i>0)
			{
				k=k+1;
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(10*knot.getPoints()[1].x) + ", " + Double.toString(0.0) + ", " + Double.toString(10*knot.getPoints()[1].y) + ") );");
			}
			
			k=k+1;
			dataOut.println("#" + k + " = CARTESIAN_POINT ( '" + knot.isContinous() + " " + knot.getOther() + "',  ( " + Double.toString(10*knot.getPoints()[0].x) + ", " + Double.toString(0.0) + ", " + Double.toString(10*knot.getPoints()[0].y) + ") );");

			k=k+1;
			if(i<outline.getNrOfControlPoints()-1)
			{
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(10*knot.getPoints()[2].x) + ", " + Double.toString(0.0) + ", " + Double.toString(10*knot.getPoints()[2].y) + ") );");
			}
		}
	
		for(int i=0;i<brd.getOutlineGuidePoints().size();i++)
		{ 
			k=k+1;
			dataOut.println("#" + k + " = CARTESIAN_POINT ( 'outline guide point',  ( " + Double.toString(10*brd.getOutlineGuidePoints().get(i).x) + ", " + Double.toString(0.0) + ", " + Double.toString(10*brd.getOutlineGuidePoints().get(i).y) + ") );");
		}
		
		//bottom
		
		k=k+1;
		
		BezierSpline bottom2=brd.getBottom();
		
		dataOut.print("#" + k + " = BEZIER_CURVE( 'bottom',3,(");
		
		for(int i=0;i<3*bottom2.getNrOfControlPoints()-2;i++)
		{ 
			dataOut.print("#" + (k+i+1));
			
			if(i<3*bottom2.getNrOfControlPoints()-3)
			{
				dataOut.print(",");
			}
			else
			{
				dataOut.println("),.UNSPECIFIED.,.F.,.F.);");
			}
		}
		
		for(int i=0;i<bottom2.getNrOfControlPoints();i++)
		{ 
			knot=bottom2.getControlPoint(i);
			
			if(i>0)
			{
				k=k+1;
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(10*knot.getPoints()[1].x) + ", " + Double.toString(10*knot.getPoints()[1].y) + ", " + Double.toString(0.0) + ") );");
			}
			
			k=k+1;
			dataOut.println("#" + k + " = CARTESIAN_POINT ( '" + knot.isContinous() + " " + knot.getOther() + "',  ( " + Double.toString(10*knot.getPoints()[0].x) + ", " + Double.toString(10*knot.getPoints()[0].y) + ", " + Double.toString(0.0) + ") );");

			k=k+1;
			if(i<bottom2.getNrOfControlPoints()-1)
			{
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(10*knot.getPoints()[2].x) + ", "  + Double.toString(10*knot.getPoints()[2].y) + ", " + Double.toString(0.0) + ") );");
			}
		}
		
		for(int i=0;i<brd.getBottomGuidePoints().size();i++)
		{ 
			k=k+1;
			dataOut.println("#" + k + " = CARTESIAN_POINT ( 'bottom guide point',  ( " + Double.toString(10*brd.getBottomGuidePoints().get(i).x) + ", " + Double.toString(10*brd.getBottomGuidePoints().get(i).y) + ", " + Double.toString(0.0) + ") );");
		}


		//deck
		
		k=k+1;
		
		BezierSpline deck2=brd.getDeck();
		
		dataOut.print("#" + k + " = BEZIER_CURVE( 'deck',3,(");
		
		for(int i=0;i<3*deck2.getNrOfControlPoints()-2;i++)
		{ 
			dataOut.print("#" + (k+i+1));
			
			if(i<3*deck2.getNrOfControlPoints()-3)
			{
				dataOut.print(",");
			}
			else
			{
				dataOut.println("),.UNSPECIFIED.,.F.,.F.);");
			}
		}
		
		for(int i=0;i<deck2.getNrOfControlPoints();i++)
		{ 
			knot=deck2.getControlPoint(i);
			
			if(i>0)
			{
				k=k+1;
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(10*knot.getPoints()[1].x) + ", " + Double.toString(10*knot.getPoints()[1].y) + ", " + Double.toString(0.0) + ") );");
			}
			
			k=k+1;
			dataOut.println("#" + k + " = CARTESIAN_POINT ( '" + knot.isContinous() + " " + knot.getOther() + "',  ( " + Double.toString(10*knot.getPoints()[0].x) + ", " + Double.toString(10*knot.getPoints()[0].y) + ", " + Double.toString(0.0) + ") );");

			k=k+1;
			if(i<deck2.getNrOfControlPoints()-1)
			{
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.toString(10*knot.getPoints()[2].x) + ", " + Double.toString(10*knot.getPoints()[2].y) + ", " + Double.toString(0.0) + ") );");
			}
		}
		
		for(int i=0;i<brd.getDeckGuidePoints().size();i++)
		{ 
			k=k+1;
			dataOut.println("#" + k + " = CARTESIAN_POINT ( 'deck guide point',  ( " + Double.toString(10*brd.getDeckGuidePoints().get(i).x) + ", " + Double.toString(10*brd.getDeckGuidePoints().get(i).y) + ", " + Double.toString(0.0) + ") );");
		}
		
		//cross sections
		
		ArrayList<BezierBoardCrossSection> crossSectionArray=brd.getCrossSections();
		
		for(int n = 0; n < crossSectionArray.size(); n++)
		{
			BezierBoardCrossSection current = crossSectionArray.get(n);
			
			BezierSpline section=current.getBezierSpline();

			k=k+1;	
			dataOut.print("#" + k + " = BEZIER_CURVE( 'section',3,(");
			
			for(int i=0;i<3*section.getNrOfControlPoints()-2;i++)
			{ 
				dataOut.print("#" + (k+i+1));
				
				if(i<3*section.getNrOfControlPoints()-3)
				{
					dataOut.print(",");
				}
				else
				{
					dataOut.println("),.UNSPECIFIED.,.F.,.F.);");
				}
			}
		
			for(int i=0;i<section.getNrOfControlPoints();i++)
			{ 
				knot=section.getControlPoint(i);
				
				if(i>0)
				{
					k=k+1;
					dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.valueOf(10*current.getPosition()) + ", " + Double.toString(10*knot.getPoints()[1].y) + ", " + Double.toString(10*knot.getPoints()[1].x) + ") );");
				}
				
				k=k+1;
				dataOut.println("#" + k + " = CARTESIAN_POINT ( '" + knot.isContinous() + " " + knot.getOther() + "',  ( " + Double.valueOf(10*current.getPosition()) + ", " + Double.toString(10*knot.getPoints()[0].y) + ", " + Double.toString(10*knot.getPoints()[0].x) + ") );");
	
				k=k+1;
				if(i<section.getNrOfControlPoints()-1)
				{
					dataOut.println("#" + k + " = CARTESIAN_POINT ( 'NONE',  ( " + Double.valueOf(10*current.getPosition())  + ", " + Double.toString(10*knot.getPoints()[2].y) + ", " + Double.toString(10*knot.getPoints()[2].x) + ") );");
				}
			}
			
			for(int i=0;i<brd.getCrossSections().get(n).getGuidePoints().size();i++)
			{ 
				k=k+1;
				dataOut.println("#" + k + " = CARTESIAN_POINT ( 'cross section " + n + " guide point',  ( " + Double.toString(0.0) + ", " + Double.toString(10*brd.getCrossSections().get(n).getGuidePoints().get(i).y) + ", " + Double.toString(10*brd.getCrossSections().get(n).getGuidePoints().get(i).x) + ") );");
			}			
						
		}
		
		//create brep product model

		k++;

		//Product definition

		dataOut.println("#" + k + " = PRODUCT_DEFINITION_SHAPE('','',#" + (k+1) + ");");
		k++;
		dataOut.println("#" + k + " = PRODUCT_DEFINITION('design','',#" + (k+1) + ");");
		k++;
		dataOut.println("#" + k + " = PRODUCT_DEFINITION_FORMATION('','',#" + (k+1) +");");
		k++;
		dataOut.println("#" + k + " = PRODUCT('Board name', 'Generated from BoardCAD','');");
		k++;
		dataOut.println("#" + k + " =SHAPE_DEFINITION_REPRESENTATION(#" + (k-4) + ",#" + (k+22) + ");");
		k++;

		//Deck surface as b-spline-surface-with-knots

		int surface1=k;
		dataOut.println("#" + k + " = ADVANCED_FACE('',(#" + (k+1) + "),#" + deck_start + ",.F.);");
		k++;
		dataOut.println("#" + k + " = FACE_BOUND('',#" + (k+1) + ",.T.);");
		k++;
		dataOut.println("#" + k + " = VERTEX_LOOP('',#" + (k+1) + ");");
		k++;
		dataOut.println("#" + k + " = VERTEX_POINT('',#" + (k+1) + ");");
		k++;
		dataOut.println("#" + k + " = CARTESIAN_POINT('',(0.0,0.0,0.0));");
		k++;

		//Bottom surface as b-spline-surface-with-knots

		int surface2=k;
		dataOut.println("#" + k + " = ADVANCED_FACE('',(#" + (k+1) +"),#" + bottom_start + ",.F.);");
		k++;
		dataOut.println("#" + k + " = FACE_BOUND('',#" + (k+1) + ",.T.);");
		k++;
		dataOut.println("#" + k + " = VERTEX_LOOP('',#" + (k+1) +");");
		k++;
		dataOut.println("#" + k + " = VERTEX_POINT('',#" + (k+1) + ");");
		k++;
		dataOut.println("#" + k + " = CARTESIAN_POINT('',(0.0,0.0,0.0));");
		k++;


		dataOut.println("#" + k + "=CLOSED_SHELL('',(#" + surface1 + ",#" + surface2 + "));");
		k++;
		int brep=k;
		dataOut.println("#" + k + "=MANIFOLD_SOLID_BREP('',#" + (k-1) + ");");
		k++;
		dataOut.println("#" + k + "=DRAUGHTING_PRE_DEFINED_COLOUR('white');");
		k++;
		dataOut.println("#" + k + "=FILL_AREA_STYLE_COLOUR('Plastic (White)',#" + (k-1)+ ");");
		k++;
		dataOut.println("#" + k + "=FILL_AREA_STYLE('Plastic (White)',(#" + (k-1) + "));");
		k++;
		dataOut.println("#" + k + "=SURFACE_STYLE_FILL_AREA(#" + (k-1) + ");");
		k++;
		dataOut.println("#" + k + "=SURFACE_SIDE_STYLE('Plastic (White)',(#" + (k-1) + "));");
		k++;
		dataOut.println("#" + k + "=SURFACE_STYLE_USAGE(.BOTH.,#" + (k-1) + ");");
		k++;
		dataOut.println("#" + k + "=PRESENTATION_STYLE_ASSIGNMENT((#" + (k-1) + "));");
		k++;
		dataOut.println("#" + k + "=STYLED_ITEM('',(#" + (k-1) + "),#" + brep + ");");
		k++;
		dataOut.println("#" + k + "=MECHANICAL_DESIGN_GEOMETRIC_PRESENTATION_REPRESENTATION('',(#" + (k-1) + "),#" + (k+2) +");");
		k++;
		dataOut.println("#" + k + "=ADVANCED_BREP_SHAPE_REPRESENTATION('ABSR',(#" + brep + "),#" + (k+1) + ");");
		k++;

		dataOut.println("#" + k + " = ( GEOMETRIC_REPRESENTATION_CONTEXT(3)GLOBAL_UNCERTAINTY_ASSIGNED_CONTEXT((#" + (k+4) + ")) GLOBAL_UNIT_ASSIGNED_CONTEXT((#" + (k+1) + ",#" + (k+2) + ",#" + (k+3) + ")) REPRESENTATION_CONTEXT('Context #1', '3D Context with UNIT and UNCERTAINTY') );");
		k++;
		dataOut.println("#" + k + " = ( LENGTH_UNIT() NAMED_UNIT(*) SI_UNIT(.MILLI.,.METRE.) );");
		k++;
		dataOut.println("#" + k + " = ( NAMED_UNIT(*) PLANE_ANGLE_UNIT() SI_UNIT($,.RADIAN.) );");
		k++;
		dataOut.println("#" + k + " = ( NAMED_UNIT(*) SI_UNIT($,.STERADIAN.) SOLID_ANGLE_UNIT() );");
		k++;
		dataOut.println("#" + k + " = UNCERTAINTY_MEASURE_WITH_UNIT(LENGTH_MEASURE(0.01),#" + (k-3) + ",'distance_accuracy_value','confusion accuracy');");
		k++;

		dataOut.println("ENDSEC;");
		dataOut.println("END-ISO-10303-21;");


	}

}
