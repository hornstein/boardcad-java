package board.readers;

import java.awt.geom.Point2D;
//import java.io.*;//File.*;
import java.nio.file.*;//File.*;
import java.nio.charset.*;
import java.io.StringReader;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
//import org.xml.sax.SAXException;

import board.BezierBoard;

import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;

public class S3dReader {

	static final int XY = 0;
	static final int XZ = 1;
	static final int YZ = 2;
	
	static String mErrorStr;
	
	static public int loadFile(BezierBoard brd, String aFilename)
	{
		System.out.printf("Loading %s\n", aFilename);
		
		int ret = 0;
		
		brd.reset();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		try {
			// rectify bad XML in v8 s3d file (PW - 12/04/2018)
			// this does not modify the file, rather changes the problematic issues as read by BC.
			Path path = Paths.get(aFilename);
			Charset charset = StandardCharsets.UTF_8;
		
			String s3Dcontent = new String(Files.readAllBytes(path));//, charset);
			s3Dcontent = s3Dcontent.replaceAll("Ref. point", "RefPoint");
		
			// finish rectifying bad XML
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			//Document document = builder.parse( new File(aFilename) );
			Document document = builder.parse( new InputSource(new StringReader(s3Dcontent)) );
			
			Element shape3Dnode = (Element)document.getElementsByTagName("Shape3d_design").item(0);
			Element boardNode = (Element)shape3Dnode.getElementsByTagName("Board").item(0);

			Element bottom = (Element)boardNode.getElementsByTagName("Bottom").item(0);
			Element deck = (Element)boardNode.getElementsByTagName("Deck").item(0);
			Element outline = (Element)boardNode.getElementsByTagName("Outline").item(0);
			
			readBezierAndGuidePoints(outline, brd.getOutline(), brd.getOutlineGuidePoints(), XY);

			BezierKnot controlPoint;
			//Add outline controlPoints at tail and nose
			if(brd.getOutline().getControlPoint(0).getPoints()[0].y > 1.0)
			{
				brd.getOutline().getControlPoint(0).setContinous(false);
				brd.getOutline().getControlPoint(0).getPoints()[1].y = brd.getOutline().getControlPoint(0).getPoints()[0].y*2/3;
			
				controlPoint = new BezierKnot();
				controlPoint.getPoints()[2].y = brd.getOutline().getControlPoint(0).getPoints()[0].y/3;
				brd.getOutline().insert(0, controlPoint);
			}
			
			if(brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[0].y > 1.0)
			{
				brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).setContinous(false);
				brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[2].x = brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[0].x;
				brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[2].y = brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[0].y*2/3;
	
				controlPoint = new BezierKnot();
				controlPoint.getPoints()[0].x = brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[0].x;
				controlPoint.getPoints()[1].x = controlPoint.getPoints()[0].x;
				controlPoint.getPoints()[1].y = brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[0].y/3;
				controlPoint.getPoints()[2].x = controlPoint.getPoints()[0].x;
				brd.getOutline().append(controlPoint);
			}
			
			readBezierAndGuidePoints(bottom, brd.getBottom(), brd.getBottomGuidePoints(), XZ);
			if(deck != null)	//Thickness curve instead?
			{
				readBezierAndGuidePoints(deck, brd.getDeck(), brd.getDeckGuidePoints(), XZ);
			}
			else
			{
				//TODO: Add generation of deck curve from thickness curve when curve fitting is implemented
				
				ret = 1;	//Warning code, deck curve is no good
				
				//Dummy deck curve
				controlPoint = (BezierKnot)brd.getBottom().getControlPoint(0).clone();
				controlPoint.setContinous(false);
				controlPoint.getPoints()[0].y += 1.5;
				controlPoint.getPoints()[2].x = 20;
				controlPoint.getPoints()[2].y = controlPoint.getPoints()[0].y - 0.5;				
				brd.getDeck().append(controlPoint);
				
				double thickness = ((( brd.getLength()/(12*UnitUtils.INCH)) - 5 )/4 * 1) + 2.125*UnitUtils.INCH;
				controlPoint = new BezierKnot();
				controlPoint.getPoints()[0].x = brd.getLength()/2;
				controlPoint.getPoints()[0].y = thickness;
				controlPoint.getPoints()[1].x = controlPoint.getPoints()[0].x - 50;
				controlPoint.getPoints()[1].y = thickness;
				controlPoint.getPoints()[2].x = controlPoint.getPoints()[0].x + 50;
				controlPoint.getPoints()[2].y = thickness;
				brd.getDeck().append(controlPoint);
				
				controlPoint = (BezierKnot)brd.getBottom().getControlPoint(brd.getBottom().getNrOfControlPoints()-1).clone();
				controlPoint.setContinous(false);
				controlPoint.getPoints()[0].y += 1.5;
				controlPoint.getPoints()[1].y += controlPoint.getPoints()[0].y/2;				
				controlPoint.getPoints()[2].x = controlPoint.getPoints()[0].x;
				controlPoint.getPoints()[2].y += 0.8;
				brd.getDeck().append(controlPoint);

			}
			
			//Add bottom end controlPoints to deck
			controlPoint = (BezierKnot)brd.getBottom().getControlPoint(0).clone();
			controlPoint.getPoints()[2].x = 0;
			controlPoint.getPoints()[2].y = (brd.getDeck().getControlPoint(0).getPoints()[0].y - controlPoint.getPoints()[0].y)/2 + controlPoint.getPoints()[0].y;
			brd.getDeck().insert(0, controlPoint);
			
			controlPoint = (BezierKnot)brd.getBottom().getControlPoint(brd.getBottom().getNrOfControlPoints()-1).clone();
			controlPoint.getPoints()[1].x = controlPoint.getPoints()[0].x;
			controlPoint.getPoints()[1].y = (brd.getDeck().getControlPoint(brd.getDeck().getNrOfControlPoints()-1).getPoints()[0].y - controlPoint.getPoints()[0].y)/2 + controlPoint.getPoints()[0].y;
			brd.getDeck().append(controlPoint);
			
			BezierBoardCrossSection crossSection;
			for(int i = 0; ;i++)
			{
				String ident = "Couples_".concat(Integer.toString(i));
				NodeList list = boardNode.getElementsByTagName(ident);
				if(list.getLength() == 0)
					break;

				Element slice = (Element)list.item(0);
				
				crossSection = new BezierBoardCrossSection();

				brd.getCrossSections().add(crossSection);
		
				Element bezierNode = (Element)slice.getElementsByTagName("Bezier3d").item(0);
				
				Element controlPointsNode = (Element)bezierNode.getElementsByTagName("Control_points").item(0);
				Element controlPointsPolygone3dNode = (Element)controlPointsNode.getElementsByTagName("Polygone3d").item(0);
				NodeList controlPointsList = controlPointsPolygone3dNode.getElementsByTagName("Point3d");
				String value = ((Element)controlPointsList.item(1)).getElementsByTagName("x").item(0).getTextContent();
			
				double pos = Double.valueOf(value);

				crossSection.setPosition(pos);
				
				BezierSpline spline = crossSection.getBezierSpline();

				readBezierAndGuidePoints(slice, spline, crossSection.getGuidePoints(), YZ);
				
				//Subtract however high the crosssection is above zero
				double height = spline.getControlPoint(0).getPoints()[0].y;
				
				for(int j = 0; j < spline.getNrOfControlPoints(); j++)
				{
					spline.getControlPoint(j).getPoints()[0].y -= height;
					spline.getControlPoint(j).getPoints()[1].y -= height;
					spline.getControlPoint(j).getPoints()[2].y -= height;
				}
				
			}
			
			//Move first and last crossSection as these are something special for shape3D
			brd.getCrossSections().get(0).setPosition(0.2);
			brd.getCrossSections().get(brd.getCrossSections().size()-1).setPosition(brd.getLength()-0.2);
			
			
			//Add crossection at tail like .brd
			crossSection = new BezierBoardCrossSection();
			crossSection.getBezierSpline().append(new BezierKnot(0,0,0,0,0,0));
			crossSection.setPosition(0.0);
			brd.getCrossSections().add(0, crossSection);
		
			//Add crossection at tip like .brd
			crossSection = new BezierBoardCrossSection();
			crossSection.getBezierSpline().append(new BezierKnot(0,0,0,0,0,0));
			crossSection.setPosition(brd.getLength());
			brd.getCrossSections().add(crossSection);
			
			//Read some board info
			brd.setModel(boardNode.getElementsByTagName("Name").item(0).getTextContent());
			brd.setDesigner(boardNode.getElementsByTagName("Author").item(0).getTextContent());
			brd.setComments(boardNode.getElementsByTagName("Comment").item(0).getTextContent());
			
			//TODO: add reading of fins

		    brd.setFilename(aFilename);

		    brd.checkAndFixContinousy(false, true);

		    brd.setLocks();
		}
	    catch (IOException ioe) {
		       // I/O error
		       ioe.printStackTrace();
		       
			   setErrorStr(LanguageResource.getString("S3DREADERRORMSG_STR")  + ioe.getMessage());
		       return -1;
		} 
		catch (SAXException sxe) {
	       // Error generated during parsing
	       Exception  x = sxe;
	       if (sxe.getException() != null)
	           x = sxe.getException();
	       setErrorStr(LanguageResource.getString("S3DREADERRORMSG_STR")  + x.getMessage());
			return -1;
	              
	    } catch (ParserConfigurationException pce) {
	       // Parser with specified options can't be built
	       pce.printStackTrace();

	       setErrorStr(LanguageResource.getString("S3DREADERRORMSG_STR")  + pce.getMessage());
	       
	       return -1;
	    }
    
		return ret;
	}
	
	static boolean readBezierAndGuidePoints(Element parent, BezierSpline bezier, ArrayList<Point2D.Double> guidePoints, int whichPlane)
	{
		Element bezierNode = (Element)parent.getElementsByTagName("Bezier3d").item(0);
		
		Element controlPointsNode = (Element)bezierNode.getElementsByTagName("Control_points").item(0);
		Element tangents1Node = (Element)bezierNode.getElementsByTagName("Tangents_1").item(0);
		Element tangents2Node = (Element)bezierNode.getElementsByTagName("Tangents_2").item(0);
		
		Element controlPointsPolygone3dNode = (Element)controlPointsNode.getElementsByTagName("Polygone3d").item(0);
		Element tangents1Polygone3dNode = (Element)tangents1Node.getElementsByTagName("Polygone3d").item(0);
		Element tangents2Polygone3dNode = (Element)tangents2Node.getElementsByTagName("Polygone3d").item(0);
		
		String numberOfPointsStr = controlPointsPolygone3dNode.getElementsByTagName("Nb_of_points").item(0).getTextContent();
		
		NodeList controlPointsList = controlPointsPolygone3dNode.getElementsByTagName("Point3d");
		NodeList tangents1PointsList = tangents1Polygone3dNode.getElementsByTagName("Point3d");
		NodeList tangents2PointsList = tangents2Polygone3dNode.getElementsByTagName("Point3d");

		int nrOfPoints = Integer.parseInt(numberOfPointsStr);
		
		//Disregard first as it's the symetry point and we don't care about that(whatever that is)
		for(int i = 1; i < nrOfPoints+1; i++)
		{
			System.out.printf("loop: i=%d\n", i);
			BezierKnot newControlPoint = new BezierKnot();
			
			String cx = ((Element)controlPointsList.item(i)).getElementsByTagName("x").item(0).getTextContent();
			String cy = ((Element)controlPointsList.item(i)).getElementsByTagName("y").item(0).getTextContent();
			String cz = ((Element)controlPointsList.item(i)).getElementsByTagName("z").item(0).getTextContent();

			String t1x = ((Element)tangents1PointsList.item(i)).getElementsByTagName("x").item(0).getTextContent();
			String t1y = ((Element)tangents1PointsList.item(i)).getElementsByTagName("y").item(0).getTextContent();
			String t1z = ((Element)tangents1PointsList.item(i)).getElementsByTagName("z").item(0).getTextContent();
			
			String t2x = ((Element)tangents2PointsList.item(i)).getElementsByTagName("x").item(0).getTextContent();
			String t2y = ((Element)tangents2PointsList.item(i)).getElementsByTagName("y").item(0).getTextContent();
			String t2z = ((Element)tangents2PointsList.item(i)).getElementsByTagName("z").item(0).getTextContent();
			
			String p1x, p1y, p2x, p2y, p3x, p3y;
			
			switch(whichPlane)
			{
			default:
			case XY:
				p1x = cx;
				p1y = cy;
				p2x = t1x;
				p2y = t1y;
				p3x = t2x;
				p3y = t2y;
				break;

			case XZ:
				p1x = cx;
				p1y = cz;
				p2x = t1x;
				p2y = t1z;
				p3x = t2x;
				p3y = t2z;
				break;
				
			case YZ:
				p1x = cy;
				p1y = cz;
				p2x = t1y;
				p2y = t1z;
				p3x = t2y;
				p3y = t2z;
				break;
			}
			
			newControlPoint.getPoints()[0].setLocation(Double.parseDouble(p1x), Double.parseDouble(p1y));
			newControlPoint.getPoints()[1].setLocation(Double.parseDouble(p2x), Double.parseDouble(p2y));
			newControlPoint.getPoints()[2].setLocation(Double.parseDouble(p3x), Double.parseDouble(p3y));
			
			String typeIdent = "Tangent_type_point_".concat(Integer.toString(i-1));
			String typeStr = bezierNode.getElementsByTagName(typeIdent).item(0).getTextContent().trim();
			int type = Integer.parseInt(typeStr);
			
			switch(type)
			{
			case 0:
				newControlPoint.setContinous(false);
				break;
			default:
				newControlPoint.setContinous(true);
				break;
			}
			
			bezier.append(newControlPoint);
		}
		
		System.out.printf("here\n");
		
		//Read guidepoints
		int nrOfGuidePoints = 0;
		NodeList guidePointsElements = bezierNode.getElementsByTagName("Number_of_guides");
		if(guidePointsElements.getLength() > 0)
		{
			String numberOfGuidePointsStr = guidePointsElements.item(0).getTextContent();
			nrOfGuidePoints = Integer.parseInt(numberOfGuidePointsStr);
		}
		
		NodeList guidePointsList = bezierNode.getElementsByTagName("Guide");

		for(int i = 0; i < nrOfGuidePoints; i++)
		{
			Point2D.Double gp = new Point2D.Double();
			
			Element guideNode = (Element)guidePointsList.item(i);
			Element guidePointNode = (Element)guideNode.getElementsByTagName("Point3d").item(0);
			
			String gx = guidePointNode.getElementsByTagName("x").item(0).getTextContent();
			String gy = guidePointNode.getElementsByTagName("y").item(0).getTextContent();
			String gz = guidePointNode.getElementsByTagName("z").item(0).getTextContent();
	
			String gpx, gpy;
			
			switch(whichPlane)
			{
			default:
			case XY:
				gpx = gx;
				gpy = gy;
				break;

			case XZ:
				gpx = gx;
				gpy = gz;
				break;
				
			case YZ:
				gpx = gy;
				gpy = gz;
				break;
			}
			
			gp.setLocation(Double.parseDouble(gpx), Double.parseDouble(gpy));
			
			guidePoints.add(gp);
		}

		return true;
	}
	
	static void setErrorStr(String errorStr)
	{
		mErrorStr = errorStr;
	}
	
	static public String getErrorStr()
	{
		return mErrorStr;
	}
}