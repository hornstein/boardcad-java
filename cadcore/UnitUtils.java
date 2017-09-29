package cadcore;

import boardcad.i18n.LanguageResource;

public class UnitUtils {

	static public final int CENTIMETERS = 0;
	static public final int INCHES = 1;
	static public final int MILLIMETERS = 2;
	static public final int INCHES_DECIMAL = 3;
	static public final int METERS = 4;

	static public final double INCH = 2.54;
	static public final double INCHES_PR_FOOT = 12;
	static public final double FOOT = (INCH*INCHES_PR_FOOT);


	public static final double CENTIMETER_PR_METER = 100;
	public static final double MILLIMETER_PR_CENTIMETER = 10;
	public static final double METER = CENTIMETER_PR_METER;
	public static final double CENTIMETER = 1;
	public static final double MILLIMETER = 0.1;

	public static final double SQUARECENTIMETER_PR_METER = 10000;
	public static final double SQUAREMETER = SQUARECENTIMETER_PR_METER;
	
	public static final double CUBICCENTIMETER_PR_LITRE = 1000;
	public static final double CUBICCENTIMETER_PR_US_PINT = 473;

	static public final double FEET_PR_METER = CENTIMETER_PR_METER/(INCH*INCHES_PR_FOOT);

	public static final double POUNDS_PR_KG = 2.20462262;

	private static int mSelectedUnit = INCHES;
	
	private static int mFractionAccuracy = 16;
	private static int mNrOfDecimals = 2;

	public static void setCurrentUnit(int unit)
	{
		mSelectedUnit = unit;
	}
	
	public static int getCurrentUnit()
	{
		return mSelectedUnit;
	}
	public static void setFractionAccuracy(int accuracy)
	{
		mFractionAccuracy = accuracy;
	}
	
	public static void setNrOfDecimals(int nrOfDecimals)
	{
		mNrOfDecimals = nrOfDecimals;
	}
	
	public static double convertInputStringToInternalLengthUnit(String string)
	{
		double value = 0;

		try{
			string = string.replace(",", ".");
			if(string.contains("\'") || string.contains("\"") )
			{
				if(string.contains("\'"))
				{
					String[] sa = string.split("\'");
					if(sa.length > 1)
					{
						
						string = sa[1];
					}
					else
					{
						string = "";
					}

					value += convertCombinedStringToValue(sa[0])*12*INCH;
				}
				if(string.contains("\""))
				{
					string = string.substring(0, string.indexOf("\""));
				}			
				value += convertCombinedStringToValue(string)*INCH;

			}
			else if(string.contains(LanguageResource.getString("UNITMETER_STR") ) || string.contains("m"))
			{
				if(string.endsWith(LanguageResource.getString("UNITMILIMETER_STR") ) || string.endsWith("mm"))
				{
					string = string.substring(0, string.indexOf("m"));
					value += convertCombinedStringToValue(string)*0.1;
				}
				else if(string.endsWith(LanguageResource.getString("UNITCENTIMETER_STR") ) || string.endsWith("cm"))
				{
					string = string.substring(0, string.indexOf("c"));
					value += convertCombinedStringToValue(string)*1;
				}
				else if(string.endsWith(LanguageResource.getString("UNITMETER_STR") ) || string.endsWith("m"))
				{
					string = string.substring(0, string.indexOf("m"));
					value += convertCombinedStringToValue(string)*100;
				}
				else
				{
					value += convertCombinedStringToValue(string)*1;	//Default to centimeter
				}
			}
			else
			{
				double mul = 1;

				switch(mSelectedUnit)
				{
				case MILLIMETERS:
					mul = 0.1;
					break;
				case INCHES:
					mul = INCH;
					break;
				default:
				case CENTIMETERS:
					mul = 1.0;
					break;
				case METERS:
					mul = 100.0;
					break;
				}

				value += convertCombinedStringToValue(string)*mul;

			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in convertInputStringToInternalUnit(): " + e.toString()); 
		}

		return value;
	}
	
	public static double convertInputStringToInternalWeightUnit(String string)
	{
		double value = 0;
		double mul = 0;

		try{
			string = string.replace(",", ".");
			if(string.endsWith("kg") || string.endsWith("kilo") || string.endsWith("kilogram"))
			{
				mul = 1;	//Default to kg
			}
			else if(string.endsWith("g") || string.endsWith("gram"))
			{
				mul = 0.001;	//Gram to kg
			}
			else if(string.endsWith("lb") || string.endsWith("lbs") || string.endsWith("pounds"))
			{
				mul = 0.45359237;	//Pound to kg
			}
			else if(string.endsWith("oz") || string.endsWith("ounces"))
			{
				mul = 0.0283495231;	//Ounce to kg
			}
			else
			{
				//Unknown, use current logic
				switch(mSelectedUnit)
				{
				case INCHES:
					mul = 0.45359237;
					break;
				default:
					mul = 1;
					break;
				}
			}	
			value += convertCombinedStringToValue(string)*mul;	
			
		}
		catch(Exception e)
		{
			System.out.println("Exception in convertInputStringToInternalWeightUnit(): " + e.toString()); 
		}

		return value;
	}

	public static double convertInputStringToInternalDensityUnit(String string)
	{
		double weight = 0.0;
		double volume = 0.0;
		try{
			string = string.replace(",", ".");
			if(string.contains("/"))
			{
				String[] sa = string.split("/");

				weight = convertInputStringToInternalWeightUnit(sa[0]);	
				volume = convertInputStringToInternalVolumeUnit("1"+sa[1]);		
			}
			else
			{
				weight = convertInputStringToInternalWeightUnit(string);	

				//Guess the most natural unit to use
				if(string.endsWith("kg") || string.endsWith("kilo") || string.endsWith("kilogram"))
				{
					volume = 1000.0;	//Default to one cubic meter
				}
				else if(string.endsWith("g") || string.endsWith("gram"))
				{
					volume = 1.0;	//Gram pr. litre
				}
				else if(string.endsWith("lb") || string.endsWith("lbs") || string.endsWith("pounds"))
				{
					volume = 28.3168466;	//Pound pr. cubic foot
				}
				else if(string.endsWith("oz") || string.endsWith("ounces"))
				{
					volume = 0.016387064;	//Ounce pr. cubic inch
				}
				else
				{
					//Unknown, use current logic
					switch(mSelectedUnit)
					{
					case INCHES:
						volume = 28.3168466;
						break;
					default:
						volume = 1;
						break;
					}
				}
				
			}

		}
		catch(Exception e)
		{
			System.out.println("Exception in convertInputStringToInternalWeightUnit(): " + e.toString()); 
		}

		double value = weight/volume;
		return value;
	}

	public static double convertInputStringToInternalVolumeUnit(String string)
	{
		//Note: internal volume unit is liter
		double value = 0;
		double mul = 1;

		try{
			string = string.replace(",", ".");
			if(string.endsWith("l") || string.endsWith("liter") || string.endsWith("litre") || string.endsWith("dm^3") || string.endsWith("dm�") || string.endsWith("dm3"))
			{
				mul = 1;	//Default to cubic dm (liter)
			}
			else if(string.endsWith("m") || string.endsWith("m3") || string.endsWith("m^3") || string.endsWith("m�") || string.endsWith("cubicmeter") || string.endsWith("cubic") || string.endsWith("cubic meter") || string.endsWith("cubicmetre") || string.endsWith("cubic metre"))
			{
				mul = 1000.0;	// to cubic meter
			}
		    else if(string.endsWith("cubic feet") || string.endsWith("cubic foot") || string.endsWith("cubic ft") || string.endsWith("cubic ft")  || string.endsWith("cu feet") || string.endsWith("cu foot") || string.endsWith("cu ft") || string.endsWith("ft�") || string.endsWith("ft") || string.endsWith("ft3") || string.endsWith("feet�") || string.endsWith("foot�") || string.endsWith("feet^3") || string.endsWith("foot^3") || string.endsWith("ft^3"))
			{
				mul = 28.3168466;	//Pound pr. cubic foot
			}
			
			if(string.charAt(string.length()-1) == '3')
				string = string.substring(0, string.length()-1);

			value = convertCombinedStringToValue(string)*mul;	
		}
		catch(Exception e)
		{
			System.out.println("Exception in convertInputStringToInternalVolumeUnit(): " + e.toString()); 
		}

		return value;
	}

	public static double convertCombinedStringToValue(String string)
	{
		double value = 0;

		//to remove the first blanks
		string=string.replaceAll("^\\s+", "");   
		
		//Remove all nonnumerical characters at end of string
		int n = string.length();
		while(n > 0 && !Character.isDigit(string.charAt(n-1)) )
		{
			n--;
		}
		string = string.substring(0, n);

		if(string.contains(" ") && string.contains("/"))
		{
			String[] sa = string.split("\\s+");
			string = sa[0];

			value += convertFractionStringToValue(sa[1].trim());	//Fraction part
		}

		if(string.contains("/"))
		{
			value += convertFractionStringToValue(string.trim());	//Fraction part
		}
		else if(string.trim().length() > 0)
		{
			value += Double.parseDouble(string.trim());	//INCH part
		}

		return value;
	}


	public static double convertFractionStringToValue(String string)
	{
		String[] sa = string.split("/");

		try{
			return Double.parseDouble(sa[0].trim())/Double.parseDouble(sa[1].trim());
		}
		catch(Exception e)
		{
			System.out.println("Exception in UnitUtils.convertFractionStringToValue(): " + e.toString()); 
			return 0;
		}

	}

	public static String convertLengthToCurrentUnit(double value, boolean useLargeUnits)
	{
		return convertLengthToUnit(value, useLargeUnits, mSelectedUnit);
	}

	public static String convertLengthToUnit(double value, boolean useLargeUnits, int unit)
	{

		switch(unit)
		{
		case INCHES:
		{
			String format = "";
			
			if(value < 0)
			{
				format = format.concat("-");
			}
			value = Math.abs(value);

			int feet = 0, inches = 0, fraction = 0, divider = mFractionAccuracy;

			inches = (int)(value/INCH);

			if(useLargeUnits && inches > 3*INCHES_PR_FOOT)
			{
				feet = (int)(inches/INCHES_PR_FOOT);
				inches %= INCHES_PR_FOOT;

				format = format.concat("%1$d'");
			}
			if(inches >= 1)
			{
				if(format.length() > 0)
					format = format.concat(" ");
					
				format = format.concat("%2$d");
			}

			fraction = (int)(((value/INCH) - (double)(inches + (feet*12)))*divider);

			if(fraction > 0)
			{
				while(fraction%2 == 0)
				{
					fraction /=2;
					divider /=2;
				}

				if(format.length() > 0)
					format = format.concat(" ");
				
				format = format.concat("%3$d/%4$d");
			}
			
			if(format.length() == 0 || format.equalsIgnoreCase("-") || format.endsWith("'"))
			{
				format = format.concat("0");
			}
			format = format.concat("\"");

			return String.format(format, feet, inches, fraction, divider);
		}
		default:
		case CENTIMETERS:
		{
			if(useLargeUnits && value > CENTIMETER_PR_METER)
			{
				return  String.format("%1$.3f m", value/CENTIMETER_PR_METER);
			}
//			Default to cm
			return  String.format("%1$.2f cm", value);
		}
		case METERS:
		{
			return  String.format("%1$.3f m", value/CENTIMETER_PR_METER);
		}
		case MILLIMETERS:
		{
//			mm
			return  String.format("%1$.1f mm", value*MILLIMETER_PR_CENTIMETER);
		}
		case INCHES_DECIMAL:
		{
			String format = "";
			
			if(value < 0)
			{
				format = format.concat("-");
			}
			value = Math.abs(value);

			int feet = 0;
			float inches = 0;

			inches = (float)(value/INCH);

			if(useLargeUnits && inches > (3*INCHES_PR_FOOT))
			{
				feet = (int)(inches/INCHES_PR_FOOT);
				inches -= feet*INCHES_PR_FOOT;

				format = format.concat("%1$d'");
			}
			if(inches >= 0.01)
			{
				format = format.concat("%2$." + mNrOfDecimals + "f");
			}

			
			if(format.length() == 0 || format.equalsIgnoreCase("-"))
			{
				format = format.concat("0");
			}
			format = format.concat("\"");

			return String.format(format, feet, inches);
		}
		}

	}
	
	public static String convertAreaToCurrentUnit(double value)
	{

		switch(mSelectedUnit)
		{
		case INCHES:
		case INCHES_DECIMAL:
		{
			return  String.format("%.3f %s", value*0.00107639104, LanguageResource.getString("UNITSQUAREFEET_STR") );
		}
		
		default:
		case CENTIMETERS:
		case MILLIMETERS:
		case METERS:
		{
			return  String.format("%.3f %s", value/(CENTIMETER_PR_METER*CENTIMETER_PR_METER), LanguageResource.getString("UNITSQUAREMETERS_STR") );
		}
		
		}

	}
	
	public static String convertVolumeToCurrentUnit(double value)
	{

		switch(mSelectedUnit)
		{
		
		default:
		case INCHES:
		case INCHES_DECIMAL:
		case CENTIMETERS:
		case MILLIMETERS:
		case METERS:
		{
			return  String.format("%.3f %s", value/(CUBICCENTIMETER_PR_LITRE), LanguageResource.getString("UNITLITERS_STR"));
		}
		
		}

	}

	public static String convertWeightToCurrentUnit(double value, boolean useSmallUnits)
	{

		switch(mSelectedUnit)
		{
		case INCHES:
		case INCHES_DECIMAL:
		{
			if(useSmallUnits && value < 1.0)
				return  String.format("%.3f %s", value/0.0283495231, LanguageResource.getString("UNITOUNCE_STR") );				
			else
				return  String.format("%.3f %s", value/0.45359237, LanguageResource.getString("UNITPOUNDS_STR") );
		}
		
		
		default:
		case CENTIMETERS:
		case MILLIMETERS:
		case METERS:
		{
			if(useSmallUnits && value < 1.0)
				return  String.format("%.0f %s", value*1000, LanguageResource.getString("UNITGRAMS_STR") );
			else
				return  String.format("%.3f %s", value, LanguageResource.getString("UNITKILOGRAMS_STR") );
		}
		}
	}

	public static String convertDensityToCurrentUnit(double value)
	{

		switch(mSelectedUnit)
		{
		case INCHES:
		case INCHES_DECIMAL:
		{
			return  String.format("%.3f %s", value*62.4279606, LanguageResource.getString("UNITPOUNDSPRFOOT_STR") );
		}
		
		default:
		case CENTIMETERS:
		case MILLIMETERS:
		case METERS:
		{
			return  String.format("%.3f %s", value, LanguageResource.getString("UNITKILOGRAMSPRLITER_STR") );
		}
		}
	}
	
	public static String convertMomentOfInertiaToCurrentUnit(double value)
	{
		switch(mSelectedUnit)
		{
		case INCHES:
		case INCHES_DECIMAL:
		{
			return  String.format("%.3f %s", value*UnitUtils.POUNDS_PR_KG*UnitUtils.FEET_PR_METER*UnitUtils.FEET_PR_METER, LanguageResource.getString("UNITPOUNDSPRFOOTSQUARED_STR") );
		}
		
		default:
		case CENTIMETERS:
		case MILLIMETERS:
		case METERS:
		{
			return  String.format("%.3f %s", value, LanguageResource.getString("UNITKILOGRAMSPRMETERSQUARED_STR") );
		}
		}		
	}
		
}