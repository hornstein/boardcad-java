package board.readers;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import board.BezierBoard;

import cadcore.BezierKnot;
import cadcore.BezierSpline;
import boardcad.i18n.LanguageResource;
import cadcore.BezierBoardCrossSection;

public class BrdReader {
	
	static String mErrorStr;

	static public int loadFile(BezierBoard brd, String aFilename)
	{
		BufferedReader cFile;

		try
		{
			cFile = new BufferedReader(new FileReader(aFilename) );
			cFile.mark(100000);

			String strLine = cFile.readLine();
			if(strLine.startsWith("%BRD-1.02"))
			{
				return loadEncryptedFile(brd, aFilename, "deltaXTaildeltaXMiddle");
			}
			else if(strLine.startsWith("%BRD-1.01"))
			{
				return loadEncryptedFile(brd, aFilename, "deltaXTail");
			}
			cFile.reset();

			int retVal = loadFile(brd, cFile);
			brd.setFilename(aFilename);

			return retVal;

		}
		catch(Exception e)
		{
			setErrorStr("exception occured during load: " + e.toString());
			return -1;
		}

	}

//	Load from an array in memory
	static public int loadFile(BezierBoard brd, char[] brdArray, String aFilename)
	{
		BufferedReader cFile;

		try
		{
			cFile = new BufferedReader(new CharArrayReader(brdArray) );

			int retVal = loadFile(brd, cFile);
			brd.setFilename(aFilename);

			return retVal;

		}
		catch(Exception e)
		{
			setErrorStr("exception occured during load %s" + e.toString());
			return -1;
		}

	}

	static public int loadEncryptedFile(BezierBoard brd, String aFilename, String key)
	{
		BufferedReader cFile;

		try
		{

			char[] ac = new char[key.length()];
			key.getChars(0, key.length(), ac, 0);
			PBEKeySpec pbekeyspec = new PBEKeySpec(ac);
			SecretKeyFactory secretkeyfactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey secretkey = secretkeyfactory.generateSecret(pbekeyspec);
			Cipher cipher1 = Cipher.getInstance("PBEWithMD5AndDES");
			PBEParameterSpec pbeParamSpec = new PBEParameterSpec(new byte[] {
					-57, 115, 33, -116, 126, -56, -18, -103
			}, 20);

			cipher1.init(2, secretkey, pbeParamSpec);	//Decrypt mode

			FileInputStream inputstream = new FileInputStream (new File(aFilename));

			for(int i = 0; i < 12; i++)
				inputstream.read();

			cFile = new BufferedReader(new InputStreamReader(new CipherInputStream(inputstream, cipher1)));
			
			int retVal = loadFile(brd, cFile);
			brd.setFilename(aFilename);

			return retVal;

		}
		catch(Exception e)
		{
			setErrorStr("exception occured during load: " + e.toString());
			return -1;
		}

	}


	static public int loadFile(BezierBoard brd, BufferedReader cFile)

	{
		brd.reset();

		try
		{

			String strLine = cFile.readLine();

			while(strLine != null)
			{
				System.out.println(strLine);
				

				if(strLine.length() < 3)
				{
					strLine = cFile.readLine();
					continue;
				}
				
				if(strLine.contains(":") == false)
				{
					strLine = cFile.readLine();
					continue;
				}

				if(strLine.startsWith("p") == false)	//so new id "noseAngelWings" doesn't mess things up
				{
					strLine = cFile.readLine();
					continue;
				}

				int id = Integer.valueOf(strLine.substring(1, 3).trim());
				
				String val = strLine.substring(6, strLine.length()).trim();

				switch(id)
				{
				case 1:  	  //Length
				{
//					Don't read values we can get from the actual model
				}
				break;
				case 2:  //Length over curve
				{
//					Don't read values we can get from the actual model
				}
				break;
				case 3:  //Thickness
				{
//					Don't read values we can get from the actual model
				}
				break;
				case 4:  //width
				{
//					Don't read values we can get from the actual model
				}
				break;
				case 5:  //noseRocker
				{
//					Don't read values we can get from the actual model
				}
				break;
				case 6:  //tailRocker
				{
				}
				break;
				case 7:  //version
				{
					                   
					brd.setVersion(val);
				}
				break;
				case 8:  //name
				{					
					brd.setName(val);
				}
				break;
				case 9:  //author
				{					
					brd.setAuthor(val);
				}
				break;
				case 10:  //blankFile
				{					
					brd.setBlankFile(val);
				}
				break;
				case 11:  //topCuts
				{
					brd.setTopCuts(Integer.valueOf(val));
				}
				break;
				case 12:  //bottomCuts
				{
					brd.setBottomCuts(Integer.valueOf(val));
				}
				break;
				case 13:  //railCuts
				{
					brd.setRailCuts(Integer.valueOf(val));
				}
				break;
				case 14:  //cutterDiam
				{
					brd.setCutterDiam(Double.valueOf(val));
				}
				break;
				case 15:  //blankPivot
				{
					brd.setBlankPivot(Double.valueOf(val));
				}
				break;
				case 16:  //boardPivot
				{
					brd.setBoardPivot(Double.valueOf(val));
				}
				break;
				case 17:  //maxAngle
				{
					brd.setMaxAngle(Double.valueOf(val));
				}
				break;
				case 18:  //noseMargin
				{
					brd.setNoseMargin(Double.valueOf(val));
				}
				break;
				case 19:  //noseLength
				{
					brd.setNoseLength(Double.valueOf(val));
				}
				break;
				case 20:  //tailLength
				{
					brd.setTailLength(Double.valueOf(val));
				}
				break;
				case 21:  //deltaXNose
				{
					brd.setDeltaXNose(Double.valueOf(val));
				}
				break;
				case 22:  //deltaXTail
				{
					brd.setDeltaXTail(Double.valueOf(val));
				}
				break;
				case 23:  //deltaXMiddle
				{
					brd.setDeltaXMiddle(Double.valueOf(val));
				}
				break;
				case 24:  //toTailSpeed
				{
					brd.setToTailSpeed(Integer.valueOf(val));
				}
				break;
				case 25:  //stringerSpeed
				{
					brd.setStringerSpeed(Integer.valueOf(val));
				}
				break;
				case 26:  //regularSpeed
				{
					brd.setRegularSpeed(Integer.valueOf(val));
				}
				break;
				case 27:  //strut1
				{
					readDoubleArray(val, brd.getStrut1());
				}
				break;
				case 28:  //strut2
				{
					readDoubleArray(val, brd.getStrut2());
				}
				break;
				case 29:  //cutterStartPos
				{
					readDoubleArray(val, brd.getCutterStartPos());
				}
				break;
				case 30:  //blankTailPos
				{
					readDoubleArray(val, brd.getBlankTailPos());
				}
				break;
				case 31:  //boardStartPos
				{
					readDoubleArray(val, brd.getBoardStartPos());
				}
				break;
				case 32:  //Outline
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getOutline(),brd.getOutlineGuidePoints());
				}
				break;
				case 33:  //Bottom
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getBottom(), brd.getBottomGuidePoints());
				}
				break;
				case 34:  //Deck
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getDeck(), brd.getDeckGuidePoints());
				}
				break;
				case 35:  //Slices
				{
					strLine = cFile.readLine();

					while(strLine.startsWith("(p36"))
					{
                        System.out.println(strLine);
						BezierBoardCrossSection crossSection = new BezierBoardCrossSection();

						brd.getCrossSections().add(crossSection);

						int a = strLine.indexOf(' ', 0);
                        int b = strLine.indexOf(' ', a+1);
                        if (b<0)
                            b=strLine.length();
						//String value = strLine.substring(a, strLine.length());
                        String value = strLine.substring(a, b);

						double pos = Double.valueOf(value).doubleValue();

						crossSection.setPosition(pos);

						readArrayOfControlPointsAndGuidepoints(cFile, crossSection.getBezierSpline(), crossSection.getGuidePoints());

						strLine = cFile.readLine();
					}
				}
				break;
				case 38:  //currentUnits
				{
					brd.setCurrentUnits(Integer.valueOf(val));
				}
				break;
				case 39:  //noseRockerOneFoot
				{
					brd.setNoseRockerOneFoot(Double.valueOf(val));
				}
				break;
				case 40:  //tailRockerOneFoot
				{
					brd.setTailRockerOneFoot(Double.valueOf(val));
				}
				break;
				case 41:  //showOriginalBoard
				{
					brd.setShowOriginalBoard(Boolean.valueOf(val));
				}
				break;
				case 42:  //stringerSpeedBottom
				{
					brd.setStringerSpeedBottom(Integer.valueOf(val));
				}
				break;
				case 43:  //machOutputFolder
				{
					brd.setMachineFolder(val);
				}
				break;
				case 44:  //topShoulderAngle
				{
					brd.setTopShoulderAngle(Double.valueOf(val));
				}
				break;
				case 45:  //Designer
				{
					brd.setDesigner(val);
				}
				break;
				case 46:  //topShoulderCuts
				{
					brd.setTopShoulderCuts(Integer.valueOf(val));
				}
				break;
				case 47:  //bottomRailCuts
				{
					brd.setBottomRailCuts(Integer.valueOf(val));
				}
				break;
				case 48:  //surfer
				{
					brd.setSurfer(val);
				}
				break;
				case 49:  //comments
				{
					brd.setComments(val.replaceAll("\\\\n", "\n"));
				}
				break;
				case 50:  //fins
				{
					readDoubleArray(val, brd.getFins());
				}
				break;
				case 51:  //finType
				{
					brd.setFinType(val);
				}
				break;
				case 52:  //description
				{
					brd.setDescription(val);
				}
				break;
				case 53:  //securityLevel
				{
					brd.setSecurityLevel(Integer.valueOf(val));
				}
				break;
				case 54:  //mModel
				{
					brd.setModel(val);
				}
				break;
				case 55:  //akubirdBoardID
				{
					brd.setAux1(val);
				}
				break;
				case 56:  //akubirdModelID
				{
					brd.setAux2(val);
				}
				break;
				case 57:  //akubirdLastUpdate
				{
					brd.setAux3(val);
				}
				break;

				case 99:  //tailMargin
				{
					brd.setTailMargin(Double.valueOf(val));
				}
				break;
				}
				strLine = cFile.readLine();
			}

		    brd.checkAndFixContinousy(false, true);

			brd.setLocks();
		}

		catch(Exception e){
			setErrorStr(LanguageResource.getString("READBRDFAILEDMSG_STR") + e.getMessage());
			return -1;
		}

		return 0;

	}

	static public int importOutline(BezierBoard brd, BufferedReader cFile)
	{
		try
		{

			String strLine = cFile.readLine();

			while(strLine != null)
			{
				int id = Integer.valueOf(strLine.substring(1, 3).trim());
					
				String val = strLine.substring(6, strLine.length()).trim();
	
				switch(id)
				{
					case 32:  //Outline
					{
						readArrayOfControlPointsAndGuidepoints(cFile, brd.getOutline(),brd.getOutlineGuidePoints());
					}
					break;
				}
			}

		    brd.checkAndFixContinousy(false, true);

			brd.setLocks();
		}

		catch(Exception e){
			setErrorStr(LanguageResource.getString("READBRDFAILEDMSG_STR") + e.getMessage());
			return -1;
		}

		return 0;

	}

	static public int importProfile(BezierBoard brd, BufferedReader cFile)
	{
		try
		{

			String strLine = cFile.readLine();

			int id = Integer.valueOf(strLine.substring(1, 3).trim());
				
			String val = strLine.substring(6, strLine.length()).trim();

			switch(id)
			{
				case 33:  //Bottom
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getBottom(), brd.getBottomGuidePoints());
				}
				break;
				case 34:  //Deck
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getDeck(), brd.getDeckGuidePoints());
				}
				break;
			}

		    brd.checkAndFixContinousy(false, true);

			brd.setLocks();
		}

		catch(Exception e){
			setErrorStr(LanguageResource.getString("READBRDFAILEDMSG_STR") + e.getMessage());
			return -1;
		}

		return 0;

	}
	
	static public int importCrossection(BezierBoard brd, BufferedReader cFile)
	{
		try
		{

			String strLine = cFile.readLine();

			int id = Integer.valueOf(strLine.substring(1, 3).trim());
				
			String val = strLine.substring(6, strLine.length()).trim();

			switch(id)
			{
				case 33:  //Bottom
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getBottom(), brd.getBottomGuidePoints());
				}
				break;
				case 34:  //Deck
				{
					readArrayOfControlPointsAndGuidepoints(cFile, brd.getDeck(), brd.getDeckGuidePoints());
				}
				break;
				case 35:  //Slices
				{
					strLine = cFile.readLine();
	
					while(strLine.startsWith("(p36"))
					{
						BezierBoardCrossSection crossSection = new BezierBoardCrossSection();
	
						brd.getCrossSections().add(crossSection);
	
						int a = strLine.indexOf(' ', 0);
						String value = strLine.substring(a, strLine.length());
	
						double pos = Double.valueOf(value).doubleValue();
	
						crossSection.setPosition(pos);
	
						readArrayOfControlPointsAndGuidepoints(cFile, crossSection.getBezierSpline(), crossSection.getGuidePoints());
	
						strLine = cFile.readLine();
					}
				}
				break;
			}

		    brd.checkAndFixContinousy(false, true);

			brd.setLocks();
		}

		catch(Exception e){
			setErrorStr(LanguageResource.getString("READBRDFAILEDMSG_STR") + e.getMessage());
			return -1;
		}

		return 0;

	}

	static void readArrayOfControlPointsAndGuidepoints(BufferedReader cFile, BezierSpline spline, ArrayList<Point2D.Double> guidepointArray)
	{
		try{
			if(spline != null)
			{
				String strLine = cFile.readLine();

				while(strLine.startsWith("(cp"))
				{
					spline.append(readControlPoint(strLine));

					strLine = cFile.readLine();
				}
				if(strLine.startsWith("gps"))
				{
					if(guidepointArray != null)
					{
						strLine = cFile.readLine();

						while(strLine.startsWith("(gp"))
						{
							String[] splitString = strLine.split(" ");

							String  valuesStr = splitString[1].substring(1,splitString[1].length()-2);
							String[] values = valuesStr.split(",");
							guidepointArray.add(new Point2D.Double(Double.valueOf(values[0]).doubleValue(), Double.valueOf(values[1]).doubleValue()));

							strLine = cFile.readLine();
						}
						cFile.readLine();
					}
				}
			}
		}
		catch(Exception e)
		{
			String str = e.toString();
			System.out.printf("exception occured during load %s",  str);
/*java 1.6			System.console().printf("exception occured during load %s",  str);*/
		}
	}


	static BezierKnot readControlPoint(String string)
	{
		BezierKnot controlPoint = new BezierKnot();

		if(string.startsWith("(cp")== false)
			return null;

		String[] splitString = string.substring(1,string.length()-1).split(" ");

		String  valuesStr = splitString[1].substring(1,splitString[1].length()-1);
		String[] values = valuesStr.split(",");
		for(int i = 0; i < 3; i++)
		{
			double x = Double.valueOf(values[i*2]).doubleValue();
			double y = Double.valueOf(values[(i*2)+1]).doubleValue();

			controlPoint.getPoints()[i].setLocation(x,y);

		}

		controlPoint.setContinous(Boolean.valueOf(splitString[2]));

		controlPoint.setOther(Boolean.valueOf(splitString[3]));

		return controlPoint;
	}

	static void readDoubleArray(String input, double[] returnValues)
	{
		String valStr = input.substring(1,input.length()-1);
		String[] values = valStr.split(",");

		for(int i = 0; i < values.length; i++)
		{
			returnValues[i] = Double.valueOf(values[i]);
		}
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