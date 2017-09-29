package board.readers;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.vecmath.Point3d;

import board.BezierBoard;

import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;

public class SrfReader {
	
	static String mErrorStr;

	static public int loadFile(BezierBoard brd, String aFilename)
	{
		brd.reset();

		try{
			File file = new File(aFilename);
			DataInputStream dataStream = new DataInputStream(new FileInputStream(file));
			long size = file.length();
			
			byte[] dataBytes = new byte[(int)size]; 
			
			dataStream.read(dataBytes,0,(int)size);
			
			ByteBuffer data = ByteBuffer.wrap(dataBytes);
			data.order(ByteOrder.LITTLE_ENDIAN);
			
			byte[] strBytes = new byte[500];
			
			//Load the info data string
			int i = 0;
			do{
				data.get(strBytes, i, 1);
				
			}while(strBytes[i++] != ' ');
			
//DEBUG			String version = new String(strBytes,0,i-2);
			
			i=0;
			do{
				data.get(strBytes, i, 1);
				
			}while(strBytes[i++] != '*');

			String name =  new String(strBytes,0,i-2);
			brd.setModel(name);

			i=0;
			do{
				data.get(strBytes, i, 1);
				
			}while(strBytes[i++] != '@');
			String comments =  new String(strBytes,0,i-1);						
			brd.setComments(comments);

			int pos = data.position();
			data.position(pos+11);
			
			//Load measurements
			float boardLength = data.getFloat();
					
			float[] initialOutline = new float[4];
			for(i = 0; i < 4; i++)
			{
				initialOutline[i] = data.getFloat();
			}
			
			float widepointPos = data.getFloat();
			
			float[] initialRocker = new float[4];
			for(i = 0; i < 4; i++)
			{
				initialRocker[i] = data.getFloat();
			}

			float[] initialRail = new float[4];
			for(i = 0; i < 4; i++)
			{
				initialRail[i] = data.getFloat();
			}

			float[] initialThickness = new float[4];
			for(i = 0; i < 4; i++)
			{
				initialThickness[i] = data.getFloat();
			}
			pos = data.position();
//			data.position(pos+115);
			data.position(pos + 113);

			short nrOfPointsOutline = data.getShort();
			
			float x,y,z;
			Point2D.Double point;
			Point2D.Double[] outline = new Point2D.Double[(nrOfPointsOutline)*3];
			for(i = 0; i < nrOfPointsOutline; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					x = data.getFloat();
					y = data.getFloat();
					z = data.getFloat();
	
					point = new Point2D.Double();
					point.setLocation(x, y);
					
					outline[(i*3)+j] = point;

					if(j==0)
					{
						pos = data.position();
						data.position(pos+12);
					}
				}
			
				pos = data.position();
				data.position(pos+28);

			}

			pos = data.position();
//			data.position(pos+3);		//First point(nose) on rocker
			data.position(pos+1);		
			short nrOfPointsRocker = data.getShort();

			Point2D.Double[] rocker = new Point2D.Double[nrOfPointsRocker*3];

			for(i = 0; i < nrOfPointsRocker; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					x = data.getFloat();
					y = data.getFloat();
					z = data.getFloat();
					
					rocker[(i*3)+j] = new Point2D.Double(x,y);
					
					if(j == 0)
					{
						pos = data.position();
						data.position(pos+12);
					}
				}
				pos = data.position();
				data.position(pos+28);		//Next
			}


			pos = data.position();
//			data.position(pos+3);		//First point(nose) on rail
			data.position(pos+1);		//First point(nose) on rail

			short nrOfPointsRail = data.getShort();

			Point2D.Double[] rail = new Point2D.Double[nrOfPointsRail*3];

			for(i = 0; i < nrOfPointsRail; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					x = data.getFloat();
					y = data.getFloat();
					z = data.getFloat();
					
					rail[(i*3)+j] = new Point2D.Double(x,y);
					
					if(j == 0)
					{
						pos = data.position();
						data.position(pos+12);
					}
				}
				pos = data.position();
				data.position(pos+28);		//Next
			}


			pos = data.position();
//			data.position(pos+3);		//First point(nose) on deck
			data.position(pos+1);		//First point(nose) on deck

			short nrOfPointsDeck = data.getShort();

			Point2D.Double[] deck = new Point2D.Double[nrOfPointsDeck*3];

			for(i = 0; i < nrOfPointsDeck; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					x = data.getFloat();
					y = data.getFloat();
					z = data.getFloat();
					
					deck[(i*3)+j] = new Point2D.Double(x,y);
					
					if(j == 0)
					{
						pos = data.position();
						data.position(pos+12);
					}
				}
				pos = data.position();
				data.position(pos+28);		//Next
			}


			pos = data.position();
//			data.position(pos+3);		//First point(nose) on concave
			data.position(pos+1);		//First point(nose) on bottom

			short nrOfPointsBottom = data.getShort();

			Point2D.Double[] bottom = new Point2D.Double[nrOfPointsBottom*3];

			for(i = 0; i < nrOfPointsBottom; i++)
			{
				for(int j = 0; j < 3; j++)
				{
					x = data.getFloat();
					y = data.getFloat();
					z = data.getFloat();
					
					bottom[(i*3)+j] = new Point2D.Double(x,y);
					
					if(j == 0)
					{
						pos = data.position();
						data.position(pos+12);
					}
				}
				pos = data.position();
				data.position(pos+28);		//Next
			}
			
			//
			pos = data.position();
//			data.position(pos+3);		//First point(nose) on concave
			data.position(pos+1);		//First point(nose) on bottom

			short nrOfDeckCaves = data.getShort();
			
			Point3d[] deckCavePoints = new Point3d[nrOfDeckCaves*4];

			//System.out.printf("\n\nReading caves\n");		
			int q = 0;
			for(i = 0; i < nrOfDeckCaves; i++)
			{
				pos = data.position();
				data.position(pos+6);	//First cave

//				System.out.printf("File pos before upper cave: %x\n", data.position());		

				//Upper cave endpoint 1
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("Upper cave endpoint1: %f, %f, %f\n", x,y,z);		

				deckCavePoints[q++] = new Point3d(x,y,z);
					
				pos = data.position();
				data.position(pos+12);
				
				//Upper cave tangent 1
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("Upper cave tangent 1: %f, %f, %f\n", x,y,z);		

				deckCavePoints[q++] = new Point3d(x,y,z);
					
				pos = data.position();
				data.position(pos+40);

				//Upper cave tangent 2
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("Upper cave tangent 2: %f, %f, %f\n", x,y,z);		

				deckCavePoints[q++] = new Point3d(x,y,z);
					
				pos = data.position();
				data.position(pos+24);
				
				//Upper cave endpoint 2
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("Upper cave endpoint 2: %f, %f, %f\n\n", x,y,z);		

				deckCavePoints[q++] = new Point3d(x,y,z);
					
				//System.out.printf("File pos after upper cave read: %x\n", data.position());		

				pos = data.position();
				data.position(pos+28);	//Next cave

				//System.out.printf("File pos after jump to next upper cave: %x\n", data.position());		
			}
			
			pos = data.position();
			data.position(pos+1);	//Lower caves

			short nrOfBottomCaves = data.getShort();

			Point3d[] bottomCavePoints = new Point3d[nrOfBottomCaves*4];

			q=0;
			for(i = 0; i < nrOfBottomCaves; i++)
			{
				pos = data.position();
				data.position(pos+6);	//First cave

				//			System.out.printf("File pos before lower cave: %x\n", data.position());		

				//lower cave endpoint 1
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("lower cave endpoint1: %f, %f, %f\n", x,y,z);		

				bottomCavePoints[q++] = new Point3d(x,y,z);
					
				pos = data.position();
				data.position(pos+12);
				
				//lower cave tangent 1
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("lower cave tangent 1: %f, %f, %f\n", x,y,z);		

				bottomCavePoints[q++] = new Point3d(x,y,z);
					
				pos = data.position();
				data.position(pos+40);

				//lower cave tangent 2
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("lower cave tangent 2: %f, %f, %f\n", x,y,z);		

				bottomCavePoints[q++] = new Point3d(x,y,z);
					
				pos = data.position();
				data.position(pos+24);
				
				//lower cave endpoint 2
				x = data.getFloat();
				y = data.getFloat();
				z = data.getFloat();
				
				//System.out.printf("lower cave endpoint 2: %f, %f, %f\n\n", x,y,z);		

				bottomCavePoints[q++] = new Point3d(x,y,z);
					
	//			System.out.printf("File pos after lower cave read: %x\n", data.position());		

				pos = data.position();
				data.position(pos+28);	//Next cave

	//			System.out.printf("File pos after jump to next lower cave: %x\n", data.position());		
			}

			//Done reading, now build board
			BezierKnot controlPoint;
			boardLength*=UnitUtils.CENTIMETER_PR_METER;
			
			BezierSpline bottomBezier = new BezierSpline();
			BezierSpline railBezier = new BezierSpline();

			//Build guidepoints
			for(i = 4; i >= 0; i--)
			{
				double x_pos; 

				switch(i)
				{
				default:
				case 0:
					x_pos = boardLength;
					break;
				case 1:
					x_pos = boardLength-UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH;
					break;
				case 2:
					x_pos = boardLength-widepointPos*UnitUtils.CENTIMETER_PR_METER;
					break;
				case 3:
					x_pos = UnitUtils.INCHES_PR_FOOT*UnitUtils.INCH;
					break;
				case 4:
					x_pos = 0;
					break;
				}
				
								
				point = new Point2D.Double();
				point.setLocation(x_pos, (i==0)?0:initialOutline[i-1]*100);				
				brd.getOutlineGuidePoints().add(point);
				
				point = new Point2D.Double();
				point.setLocation(x_pos, ((i!=2)?initialRocker[(i<2)?i:i-1]:0)*100);				
				brd.getBottomGuidePoints().add(point);
			}

			//Outline
			for(i=nrOfPointsOutline-1; i >=0; i--)
			{
				controlPoint = new BezierKnot();
				controlPoint.setEndPoint(boardLength-outline[i*3].x*UnitUtils.CENTIMETER_PR_METER,		outline[i*3].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToNext(boardLength-outline[(i*3)+2].x*UnitUtils.CENTIMETER_PR_METER,	outline[(i*3)+2].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToPrev(boardLength-outline[(i*3)+1].x*UnitUtils.CENTIMETER_PR_METER,	outline[(i*3)+1].y*UnitUtils.CENTIMETER_PR_METER);
				brd.getOutline().append(controlPoint);
			}
			
			//Add outline ControlPoints at tail and nose
			if(brd.getOutline().getControlPoint(0).getPoints()[0].y > 0.3)
			{
				BezierKnot firstControlPoint = brd.getOutline().getControlPoint(0); 

				firstControlPoint.setContinous(false);
				firstControlPoint.getPoints()[1].x = 0;
				firstControlPoint.getPoints()[1].y = firstControlPoint.getPoints()[0].y*2/3;
			
				controlPoint = new BezierKnot();
				controlPoint.getPoints()[2].y = firstControlPoint.getPoints()[0].y/3;
				brd.getOutline().insert(0, controlPoint);
			}
			
			if(brd.getOutline().getControlPoint(brd.getOutline().getNrOfControlPoints()-1).getPoints()[0].y > 0.3)
			{
				int index = brd.getOutline().getNrOfControlPoints()-1;

				BezierKnot lastControlPoint = brd.getOutline().getControlPoint(index);  
				
				lastControlPoint.setContinous(false);
				lastControlPoint.getPoints()[2].x = lastControlPoint.getPoints()[0].x;
				lastControlPoint.getPoints()[2].y = lastControlPoint.getPoints()[0].y*2/3;
	
				controlPoint = new BezierKnot();
				controlPoint.getPoints()[0].x = lastControlPoint.getPoints()[0].x;
				controlPoint.getPoints()[0].y = 0;
				controlPoint.getPoints()[1].x = lastControlPoint.getPoints()[0].x;
				controlPoint.getPoints()[1].y = lastControlPoint.getPoints()[0].y/3;
				controlPoint.getPoints()[2].x = lastControlPoint.getPoints()[0].x;
				controlPoint.getPoints()[2].y = 0;
				
				lastControlPoint.getPoints()[2].x = lastControlPoint.getPoints()[0].x;
				lastControlPoint.getPoints()[2].y = lastControlPoint.getPoints()[0].y*2/3;

				brd.getOutline().append(controlPoint);
			}
			

			//Rocker
			for(i=nrOfPointsRocker-1; i >=0; i--)
			{
				controlPoint = new BezierKnot();
				controlPoint.setEndPoint(boardLength-rocker[i*3].x*UnitUtils.CENTIMETER_PR_METER,		rocker[i*3].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToNext(boardLength-rocker[(i*3)+2].x*UnitUtils.CENTIMETER_PR_METER,	rocker[(i*3)+2].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToPrev(boardLength-rocker[(i*3)+1].x*UnitUtils.CENTIMETER_PR_METER,	rocker[(i*3)+1].y*UnitUtils.CENTIMETER_PR_METER);
				brd.getBottom().append(controlPoint);
			}

			//Deck
			for(i=nrOfPointsRocker-1; i >=0; i--)
			{
				controlPoint = new BezierKnot();
				controlPoint.setEndPoint(boardLength-deck[i*3].x*UnitUtils.CENTIMETER_PR_METER,		deck[i*3].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToNext(boardLength-deck[(i*3)+2].x*UnitUtils.CENTIMETER_PR_METER,	deck[(i*3)+2].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToPrev(boardLength-deck[(i*3)+1].x*UnitUtils.CENTIMETER_PR_METER,	deck[(i*3)+1].y*UnitUtils.CENTIMETER_PR_METER);
				brd.getDeck().append(controlPoint);
			}

			//Add bottom end ControlPoints to deck
			BezierKnot bottomTailControlPoint = brd.getBottom().getControlPoint(0);
			BezierKnot deckOldTailControlPoint = brd.getDeck().getControlPoint(0);
			controlPoint = (BezierKnot)bottomTailControlPoint.clone();
			controlPoint.setTangentToNext(deckOldTailControlPoint.getEndPoint().x, deckOldTailControlPoint.getEndPoint().y);
			controlPoint.scaleTangentToNext(0.3);			
			brd.getDeck().insert(0, controlPoint);
			deckOldTailControlPoint.setTangentToPrev(controlPoint.getEndPoint().x, controlPoint.getEndPoint().y);
			deckOldTailControlPoint.scaleTangentToPrev(0.3);			
			
			BezierKnot bottomNoseControlPoint = brd.getBottom().getControlPoint(brd.getBottom().getNrOfControlPoints()-1);
			BezierKnot deckOldNoseControlPoint = brd.getDeck().getControlPoint(brd.getDeck().getNrOfControlPoints()-1);
			controlPoint = (BezierKnot)bottomNoseControlPoint.clone();
			controlPoint.setTangentToPrev(deckOldNoseControlPoint.getEndPoint().x, deckOldNoseControlPoint.getEndPoint().y);
			controlPoint.scaleTangentToPrev(0.3);			
			brd.getDeck().append(controlPoint);
			deckOldNoseControlPoint.setTangentToNext(controlPoint.getEndPoint().x, controlPoint.getEndPoint().y);
			deckOldNoseControlPoint.scaleTangentToNext(0.3);

			//Rail
			for(i=nrOfPointsRail-1; i >=0; i--)
			{
				controlPoint = new BezierKnot();
				controlPoint.setEndPoint(boardLength-rail[i*3].x*UnitUtils.CENTIMETER_PR_METER,		rail[i*3].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToNext(boardLength-rail[(i*3)+2].x*UnitUtils.CENTIMETER_PR_METER,	rail[(i*3)+2].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToPrev(boardLength-rail[(i*3)+1].x*UnitUtils.CENTIMETER_PR_METER,	rail[(i*3)+1].y*UnitUtils.CENTIMETER_PR_METER);
				railBezier.append(controlPoint);
			}

			//'Bottom'
			for(i=nrOfPointsBottom-1; i >=0; i--)
			{
				controlPoint = new BezierKnot();
				controlPoint.setEndPoint(boardLength-bottom[i*3].x*UnitUtils.CENTIMETER_PR_METER,		bottom[i*3].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToNext(boardLength-bottom[(i*3)+2].x*UnitUtils.CENTIMETER_PR_METER,	bottom[(i*3)+2].y*UnitUtils.CENTIMETER_PR_METER);
				controlPoint.setTangentToPrev(boardLength-bottom[(i*3)+1].x*UnitUtils.CENTIMETER_PR_METER,	bottom[(i*3)+1].y*UnitUtils.CENTIMETER_PR_METER);
				bottomBezier.append(controlPoint);
			}

			
//			BoardCAD.getInstance().getGhostBrd().getDeck() = railBezier;
//			BoardCAD.getInstance().getGhostBrd().getBottom() = bottomBezier;

			
			//Add crossection at tail like .brd
			BezierBoardCrossSection crossSection = new BezierBoardCrossSection();
			crossSection.getBezierSpline().append(new BezierKnot(0,0,0,0,0,0));
			crossSection.setPosition(0.0);
			brd.getCrossSections().add(0, crossSection);
		
			//Add crossection at tip like .brd
			crossSection = new BezierBoardCrossSection();
			crossSection.getBezierSpline().append(new BezierKnot(0,0,0,0,0,0));
			crossSection.setPosition(brd.getLength());
			brd.getCrossSections().add(crossSection);
			
			//Add Deck cross sections
			for(i = 0; i < nrOfDeckCaves; i++)
			{
				BezierBoardCrossSection crs = new BezierBoardCrossSection();
				double crsPos = boardLength-deckCavePoints[i*4].z*UnitUtils.CENTIMETER_PR_METER;
				if(crsPos < 0.5)
					crsPos = 0.5;
				if(crsPos > boardLength-0.5)
					crsPos = boardLength-0.5;
				
				crs.setPosition(crsPos);
				
				//System.out.printf("Deck cave at %f\n", crsPos);
				
				double crsBottom = brd.getRockerAtPos(crs.getPosition());//deckCavePoints[i*4].y*UnitUtils.CENTIMETER_PR_METER;
				
				//See if we can find a cave that match on the bottom
				boolean found = false;
				for(int j = 0; j < nrOfBottomCaves; j++)
				{
					if(Math.abs( boardLength-bottomCavePoints[j*4].z*UnitUtils.CENTIMETER_PR_METER - crs.getPosition()) < 2.0)
					{
						//System.out.printf("Found matching bottom cave at %f\n", boardLength-bottomCavePoints[j*4].z*UnitUtils.CENTIMETER_PR_METER);

						//Lower part, center control point
						controlPoint = new BezierKnot();
						controlPoint.setContinous(false);
						controlPoint.setEndPoint(bottomCavePoints[j*4].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[j*4].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
						controlPoint.setTangentToNext(bottomCavePoints[(j*4)+1].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(j*4)+1].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
						
						crs.getBezierSpline().append(controlPoint);
						
						//Lower part, rail control point
						controlPoint = new BezierKnot();
						controlPoint.setContinous(false);
						controlPoint.setTangentToPrev(bottomCavePoints[(j*4)+3].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(j*4)+3].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
						controlPoint.setEndPoint(bottomCavePoints[(j*4)+2].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(j*4)+2].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
						controlPoint.setTangentToNext(bottomCavePoints[(j*4)+2].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(j*4)+2].y*UnitUtils.CENTIMETER_PR_METER - crsBottom + 0.2);
						
						crs.getBezierSpline().append(controlPoint);

						found = true;
						
						break;
					}
				}
					
				if(!found)
				{
					//System.out.printf("No matching bottom cave found, generating bottom cave");
					
					//Get the two bottom caves that span this point
					int caveBefore = 0;
					int caveAfter = 0;
					for(int j = nrOfBottomCaves-1; j >= 0; j--)
					{
						double cavePos = boardLength-bottomCavePoints[j*4].z*UnitUtils.CENTIMETER_PR_METER ;
						//System.out.printf("%d cavePos: %f\n",j,cavePos);
						if(cavePos < crsPos)
						{
							caveBefore = j;
							caveAfter = j;
						}
						
						if(cavePos >= crsPos)
						{
							caveAfter = j;
							break;
						}
					}

					//Get positions of the found caves
					double beforePos = boardLength-bottomCavePoints[caveBefore*4].z*UnitUtils.CENTIMETER_PR_METER;
					double afterPos = boardLength-bottomCavePoints[caveAfter*4].z*UnitUtils.CENTIMETER_PR_METER;

					double r = (crsPos - beforePos)/(afterPos-beforePos);
					if(Double.isInfinite(r) || Double.isNaN(r))
						r=1.0;

					//Get center contol points from bottom cave
					double beforeCenterTangentX = bottomCavePoints[(caveBefore*4)+1].x*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[caveBefore*4].x*UnitUtils.CENTIMETER_PR_METER;
					double beforeCenterTangentY = bottomCavePoints[(caveBefore*4)+1].y*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[caveBefore*4].y*UnitUtils.CENTIMETER_PR_METER;
					
					double afterCenterTangentX = bottomCavePoints[(caveAfter*4)+1].x*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[caveAfter*4].x*UnitUtils.CENTIMETER_PR_METER;
					double afterCenterTangentY = bottomCavePoints[(caveAfter*4)+1].y*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[caveAfter*4].y*UnitUtils.CENTIMETER_PR_METER;

					//Interpolate center control point tangent
					double centerTangentX = (1.0-r)*beforeCenterTangentX + r*afterCenterTangentX;
					double centerTangentY = (1.0-r)*beforeCenterTangentY + r*afterCenterTangentY;
						
					//Build new center controlpoint, use rocker at pos as y 
					controlPoint = new BezierKnot();
					controlPoint.setContinous(false);
					controlPoint.setEndPoint(0,0);
					controlPoint.setTangentToNext(centerTangentX, centerTangentY);
					
					crs.getBezierSpline().append(controlPoint);
					
					//Get rail contol points from bottom cave
					double beforeRailTangentX = bottomCavePoints[(caveBefore*4)+3].x*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[(caveBefore*4)+2].x*UnitUtils.CENTIMETER_PR_METER;
					double beforeRailTangentY = bottomCavePoints[(caveBefore*4)+3].y*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[(caveBefore*4)+2].y*UnitUtils.CENTIMETER_PR_METER;
					
					double afterRailTangentX = bottomCavePoints[(caveAfter*4)+3].x*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[(caveAfter*4)+2].x*UnitUtils.CENTIMETER_PR_METER;
					double afterRailTangentY = bottomCavePoints[(caveAfter*4)+3].y*UnitUtils.CENTIMETER_PR_METER - bottomCavePoints[(caveAfter*4)+2].y*UnitUtils.CENTIMETER_PR_METER;

					//Interpolate rail control point tangent
					double railTangentX = (1.0-r)*beforeRailTangentX + r*afterRailTangentX;
					double railTangentY = (1.0-r)*beforeRailTangentY + r*afterRailTangentY;
					
					//Build new center controlpoint, use rail bezier for position 
					controlPoint = new BezierKnot();
					controlPoint.setContinous(false);
					controlPoint.setEndPoint(brd.getWidthAtPos(crsPos)/2.0, bottomBezier.getValueAt(crsPos) - crsBottom);
					controlPoint.setTangentToPrev(railTangentX + controlPoint.getEndPoint().x, railTangentY + controlPoint.getEndPoint().y);
					controlPoint.setTangentToNext(controlPoint.getEndPoint().x, controlPoint.getEndPoint().y + 0.2);

					crs.getBezierSpline().append(controlPoint);
				}	

				//Upper cave
				controlPoint = new BezierKnot();
				controlPoint.setContinous(false);
				controlPoint.setEndPoint(deckCavePoints[(i*4)+2].x*UnitUtils.CENTIMETER_PR_METER, deckCavePoints[(i*4)+2].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				controlPoint.setTangentToPrev(deckCavePoints[(i*4)+2].x*UnitUtils.CENTIMETER_PR_METER, deckCavePoints[(i*4)+2].y*UnitUtils.CENTIMETER_PR_METER - crsBottom - 0.2);
				controlPoint.setTangentToNext(deckCavePoints[(i*4)+3].x*UnitUtils.CENTIMETER_PR_METER, deckCavePoints[(i*4)+3].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				
				crs.getBezierSpline().append(controlPoint);
								
				controlPoint = new BezierKnot();
				controlPoint.setContinous(false);
				controlPoint.setTangentToPrev(deckCavePoints[(i*4)+1].x*UnitUtils.CENTIMETER_PR_METER, deckCavePoints[(i*4)+1].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				controlPoint.setEndPoint(deckCavePoints[i*4].x*UnitUtils.CENTIMETER_PR_METER, deckCavePoints[i*4].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				
				crs.getBezierSpline().append(controlPoint);

				brd.addCrossSection(crs);
				
			}

			//Add bottom cross sections
			for(i = 0; i < nrOfBottomCaves; i++)
			{
				double crsPos = boardLength-bottomCavePoints[i*4].z*UnitUtils.CENTIMETER_PR_METER;
				if(crsPos < 0.5)
					crsPos = 0.5;
				if(crsPos > boardLength-0.5)
					crsPos = boardLength-0.5;

				//System.out.printf("Bottom cave at %f\n", crsPos);

				BezierBoardCrossSection near = brd.getNearestCrossSection(crsPos);

				//System.out.printf("Nearest cross-section at %f\n", near!=null?near.getPosition():-1.0);
				
				if(near!=null && Math.abs(near.getPosition()-crsPos) < 2.0)
				{
					//System.out.printf("Bottom cave already inserted into crossection at %f\n", near.getPosition());

					continue;	//Already exist
				}
				
				
				BezierBoardCrossSection crs = new BezierBoardCrossSection();
				crs.setPosition(crsPos);
				
				double crsBottom = brd.getRockerAtPos(crsPos);//deckCavePoints[i*4].y*UnitUtils.CENTIMETER_PR_METER;
				
				//Lower part
				controlPoint = new BezierKnot();
				controlPoint.setContinous(false);
				controlPoint.setEndPoint(bottomCavePoints[i*4].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[i*4].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				controlPoint.setTangentToNext(bottomCavePoints[(i*4)+1].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(i*4)+1].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				
				crs.getBezierSpline().append(controlPoint);
				
				controlPoint = new BezierKnot();
				controlPoint.setContinous(false);
				controlPoint.setTangentToPrev(bottomCavePoints[(i*4)+3].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(i*4)+3].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				controlPoint.setEndPoint(bottomCavePoints[(i*4)+2].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(i*4)+2].y*UnitUtils.CENTIMETER_PR_METER - crsBottom);
				controlPoint.setTangentToNext(bottomCavePoints[(i*4)+2].x*UnitUtils.CENTIMETER_PR_METER, bottomCavePoints[(i*4)+2].y*UnitUtils.CENTIMETER_PR_METER - crsBottom + 0.2);
				
				crs.getBezierSpline().append(controlPoint);

				//Get the two bottom caves that span this point
				int caveBefore = 0;
				int caveAfter = 0;
				for(int j = nrOfDeckCaves-1; j >= 0; j--)
				{
					double cavePos = boardLength-deckCavePoints[j*4].z*UnitUtils.CENTIMETER_PR_METER ;
					//System.out.printf("%d cavePos: %f\n",j,cavePos);
					if(cavePos < crsPos)
					{
						caveBefore = j;
						caveAfter = j;
					}
					
					if(cavePos >= crsPos)
					{
						caveAfter = j;
						break;
					}
				}

				//Get positions of the found caves
				double beforePos = boardLength-deckCavePoints[caveBefore*4].z*UnitUtils.CENTIMETER_PR_METER;
				double afterPos = boardLength-deckCavePoints[caveAfter*4].z*UnitUtils.CENTIMETER_PR_METER;

				double r = (crsPos - beforePos)/(afterPos-beforePos);
				if(Double.isInfinite(r) || Double.isNaN(r))
					r=1.0;

				//Get center contol points from bottom cave
				double beforeCenterTangentX = deckCavePoints[(caveBefore*4)+3].x*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveBefore*4)+2].x*UnitUtils.CENTIMETER_PR_METER;
				double beforeCenterTangentY = deckCavePoints[(caveBefore*4)+3].y*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveBefore*4)+2].y*UnitUtils.CENTIMETER_PR_METER;
				
				double afterCenterTangentX = deckCavePoints[(caveAfter*4)+3].x*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveAfter*4)+2].x*UnitUtils.CENTIMETER_PR_METER;
				double afterCenterTangentY = deckCavePoints[(caveAfter*4)+3].y*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveAfter*4)+2].y*UnitUtils.CENTIMETER_PR_METER;

				//Interpolate center control point tangent
				double centerTangentX = (1.0-r)*beforeCenterTangentX + r*afterCenterTangentX;
				double centerTangentY = (1.0-r)*beforeCenterTangentY + r*afterCenterTangentY;
					
				//Build new center controlpoint, use rocker at pos as y 
				controlPoint = new BezierKnot();
				controlPoint.setContinous(false);
				double railPos = crsPos;
				if(railPos < railBezier.getMinX())
				{
					railPos = railBezier.getMinX();
				}
				if(railPos > railBezier.getMaxX())
				{
					railPos = railBezier.getMaxX();
				}
				controlPoint.setEndPoint( crs.getBezierSpline().getControlPoint(1).getEndPoint().x, railBezier.getValueAt(railPos) - crsBottom);
				controlPoint.setTangentToPrev(controlPoint.getEndPoint().x, controlPoint.getEndPoint().y - 0.2);
				controlPoint.setTangentToNext(centerTangentX + controlPoint.getEndPoint().x, centerTangentY+ controlPoint.getEndPoint().y);
				
				crs.getBezierSpline().append(controlPoint);

				//Get rail contol points from bottom cave
				double beforeRailTangentX = deckCavePoints[(caveBefore*4)+1].x*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveBefore*4)].x*UnitUtils.CENTIMETER_PR_METER;
				double beforeRailTangentY = deckCavePoints[(caveBefore*4)+1].y*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveBefore*4)].y*UnitUtils.CENTIMETER_PR_METER;
				
				double afterRailTangentX = deckCavePoints[(caveAfter*4)+1].x*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveAfter*4)].x*UnitUtils.CENTIMETER_PR_METER;
				double afterRailTangentY = deckCavePoints[(caveAfter*4)+1].y*UnitUtils.CENTIMETER_PR_METER - deckCavePoints[(caveAfter*4)].y*UnitUtils.CENTIMETER_PR_METER;

				//Interpolate rail control point tangent
				double railTangentX = (1.0-r)*beforeRailTangentX + r*afterRailTangentX;
				double railTangentY = (1.0-r)*beforeRailTangentY + r*afterRailTangentY;
				
				//Build new center controlpoint, use rail bezier for position 
				controlPoint = new BezierKnot();
				controlPoint.setContinous(false);
				controlPoint.setEndPoint(0, brd.getDeckAtPos(railPos) - crsBottom);
				controlPoint.setTangentToPrev(railTangentX + controlPoint.getEndPoint().x, railTangentY + controlPoint.getEndPoint().y);

				crs.getBezierSpline().append(controlPoint);

				brd.addCrossSection(crs);
			}

		    brd.setFilename(aFilename);
		    
		    brd.checkAndFixContinousy(true, true);
		    
		    brd.setLocks();
		}
		catch(Exception e){
			setErrorStr(LanguageResource.getString("SRFREADERFAILEDMSG_STR") + e.toString());
			return -1;
		}

		return 0;
	}

	Point2D.Double[] getDeckCaveAtPos(double pos, double targetWidth, double targetHeight, double deckCaves, Point3d[] deckCavesPoints)
	{
		return null;	//TODO: NIY
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
