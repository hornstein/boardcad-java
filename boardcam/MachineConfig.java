package boardcam;


import java.util.HashMap;

import javax.vecmath.Point3d;

import cadcore.UnitUtils;


import board.BezierBoard;
import boardcad.settings.*;
import boardcad.settings.Settings.*;
import boardcam.cutters.*;
import boardcam.holdingsystems.AbstractBlankHoldingSystem;
import boardcam.toolpathgenerators.*;
import boardcam.holdingsystems.*;
import boardcad.i18n.LanguageResource;
import boardcad.FileTools;
import boardcam.writers.GCodeWriter;
import boardcam.writers.GCodeWriterWithScaling;
import boardcad.gui.jdk.MachineView;	//TODO: Bad dependency
import board.readers.*;

public class MachineConfig extends CategorizedSettings 
{
	public static String USE_BRD_SETTINGS     = "UseBrdSettings";

	public static String CUTTER_TYPE     = "CutterType";
	public static String BLANKHOLDINGSYSTEM_TYPE     = "BlankHoldingSystemType";
	
	public static String DECK_ANGLE        = "DeckAngle";
	public static String DECK_RAIL_ANGLE   = "DeckRailAngle";
	public static String BOTTOM_ANGLE      = "BottomAngle";
	public static String BOTTOM_RAIL_ANGLE = "BottomRailAngle";

	public static String LENGTHWISE_RESOLUTION = "LengthwiseResolution";
	
	public static String DECK_CUTS         = "DeckCuts";
	public static String DECK_RAIL_CUTS    = "DeckRailCuts";
	public static String BOTTOM_CUTS       = "BottomCuts";
	public static String BOTTOM_RAIL_CUTS  = "BottomRailCuts";
	public static String BLANK  			= "Blank";

	public static String DECK_CLEAN_UP_CUTS = "DeckCleanUpCuts";
	public static String BOTTOM_CLEAN_UP_CUTS = "BottomCleanUpCuts";

	public static String DECK_STRINGER_CUT = "DeckStringerCut";
	public static String BOTTOM_STRINGER_CUT = "BottomStringerCut";
	
	public static String STAY_AWAY_FROM_STRINGER = "StayAwayFromStringer";
	public static String STRINGER_WIDTH = "StringerWidth";

	public static String DECK_OUTLINE_CUT = "DeckOutlineCut";
	public static String DECK_OUTLINE_CUT_DEPTH = "DeckOutlineCutDepth";
	public static String BOTTOM_OUTLINE_CUT = "BottomOutlineCut";

	public static String CUT_OFF_NOSE = "Cut off nose";
	public static String CUT_OFF_TAIL = "Cut off tail";
	
	public static final String CUTTING_SPEED = "CuttingSpeed";
	public static final String CUTTING_SPEED_STRINGER = "StringerCuttingSpeed";
	public static final String CUTTING_SPEED_RAIL = "RailCuttingSpeed";
	public static final String CUTTING_SPEED_OUTLINE = "OutlineCuttingSpeed";

	public static final String CUTTER_X_OFFSET = "CutterXOffset";
	public static final String CUTTER_Y_OFFSET = "CutterYOffset";
	public static final String CUTTER_Z_OFFSET = "CutterZOffset";
	
	public static final String BEFORE_DECK_CUT = "BeforeDeckCut";
	public static final String BEFORE_DECK_CUT_SCRIPT = "BeforeDeckCutScript";
	public static final String DECK_START_POS = "DeckStartPos";
	public static final String AFTER_DECK_CUT = "AfterDeckCut";
	public static final String AFTER_DECK_CUT_SCRIPT = "AfterDeckCutScript";
	
	public static final String BEFORE_BOTTOM_CUT = "BeforeBottomCut";
	public static final String BEFORE_BOTTOM_CUT_SCRIPT = "BeforeBottomCutScript";
	public static final String BOTTOM_START_POS = "BottomStartPos";
	public static final String AFTER_BOTTOM_CUT = "AfterBottomCut";
	public static final String AFTER_BOTTOM_CUT_SCRIPT = "AfterBottomCutScript";
	public static final String CLEAR_HEIGHT = "ClearHeight";
	
	public static final String SAVE_G_CODE = "SaveGCode";
	
	protected AbstractCutter mCutter = null;
	protected AbstractBlankHoldingSystem mBlankHoldingSystem = null;
	protected AbstractToolpathGenerator mToolpathGenerator = null;
	
	protected BezierBoard mBrd;
	protected BezierBoard mBlank;
	
	protected MachineView mMachineView = null;
	
	Point3d mCutterOffset = new Point3d(0.0, 0.0, 0.0);
	
	public MachineConfig()
	{		
	}
	
	public void initialize()
	{		
		Settings.SettingChangedCallback onSupportSettingsChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				if(mMachineView != null)
				{
					mMachineView.update();
				}
			}
		};
		
		Settings.SettingChangedCallback onCutterTypeChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				System.out.printf("Cutter Setting changed\n");
				
				Settings cutterSettings = getCategory(LanguageResource.getString("CUTTERCATEGORY_STR"));
				Settings generalSettings = getCategory(LanguageResource.getString("GENERALCATEGORY_STR"));					
				cutterSettings.putPreferences();
				cutterSettings.clear();
						
				mMachineView.get3DView().setModelRotation(0.0);

				Enumeration enu = (Enumeration)obj;
				
				switch(enu.getValue())
				{
				case 0:
					mCutter = new SimpleBullnoseCutter(MachineConfig.this);
					generalSettings.removeObject(RotatingBoardCutter.ROTATINGBOARDCUTTER_TYPE);
					if(generalSettings.getEnumeration(BLANKHOLDINGSYSTEM_TYPE) == 2)
					{
						generalSettings.setEnumeration(BLANKHOLDINGSYSTEM_TYPE, 0);
					}
					break;
				case 1:
					mCutter = new STLCutter(MachineConfig.this);
					generalSettings.removeObject(RotatingBoardCutter.ROTATINGBOARDCUTTER_TYPE); 
					if(generalSettings.getEnumeration(BLANKHOLDINGSYSTEM_TYPE) == 2)
					{
						generalSettings.setEnumeration(BLANKHOLDINGSYSTEM_TYPE, 0);
					}
					break;
				case 2:
					mCutter = new RotatingBoardCutter(MachineConfig.this, mMachineView.get3DView());
					generalSettings.setEnumeration(BLANKHOLDINGSYSTEM_TYPE, 2);
					break;					
				}
				
				cutterSettings.getPreferences();

				if(mMachineView != null)
				{
					mMachineView.get3DView().setCutterModel(mCutter.get3DModel());
					mMachineView.update();
				}
			}
		};

		Settings.SettingChangedCallback onBlankHoldingSystemChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				System.out.printf("Blankholdingsystem Setting changed\n");

				MachineConfig.this.getCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR")).putPreferences();
				Settings generalSettings = getCategory(LanguageResource.getString("GENERALCATEGORY_STR"));					

				Enumeration enu = (Enumeration)obj;
				
				switch(enu.getValue())
				{
				case 0:
					mBlankHoldingSystem = new SupportsBlankHoldingSystem(MachineConfig.this);
					if(generalSettings.getEnumeration(CUTTER_TYPE) == 2)
					{
						generalSettings.setEnumeration(CUTTER_TYPE, 0);
					}
					break;
				case 1:
					mBlankHoldingSystem = new EndClampsBlankHoldingSystem(MachineConfig.this);
					if(generalSettings.getEnumeration(CUTTER_TYPE) == 2)
					{
						generalSettings.setEnumeration(CUTTER_TYPE, 0);
					}
					break;
				case 2:
					mBlankHoldingSystem = new RotatingBoardBlankHoldingSystem(MachineConfig.this);
					generalSettings.setEnumeration(CUTTER_TYPE, 2);
					generalSettings.getSetting(CUTTER_TYPE).signal();
					break;					
				}
								
				getCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR")).getPreferences();

				if(mMachineView != null)
				{
					mMachineView.get3DView().setBlankHoldingSystemModel(mBlankHoldingSystem.get3DModel());
					mMachineView.update();
				}
			}
		};

		Settings generalSettings = addCategory(LanguageResource.getString("GENERALCATEGORY_STR"));
		
		generalSettings.addObject(USE_BRD_SETTINGS, new Boolean(true), LanguageResource.getString("USEBRDSETTINGS_STR"), onSupportSettingsChanged);

		HashMap<Integer, String> cutterTypes = new HashMap<Integer, String>();
		cutterTypes.put(0, LanguageResource.getString("BULLNOSECUTTER_STR"));
		cutterTypes.put(1, LanguageResource.getString("STLCUTTER_STR"));
		cutterTypes.put(2, LanguageResource.getString("ROTATINGBOARDCUTTER_STR"));
		generalSettings.addEnumeration(CUTTER_TYPE, 0, cutterTypes, LanguageResource.getString("CUTTERTYPE_STR"), onCutterTypeChanged);
	
		HashMap<Integer, String> holdingSystemTypes = new HashMap<Integer, String>();
		holdingSystemTypes.put(0, LanguageResource.getString("SUPPORTS_STR"));
		holdingSystemTypes.put(1, LanguageResource.getString("ENDCLAMPS_STR"));
		holdingSystemTypes.put(2, LanguageResource.getString("ROTATINGBOARD_STR"));
		generalSettings.addEnumeration(BLANKHOLDINGSYSTEM_TYPE, 0,holdingSystemTypes, LanguageResource.getString("BLANKHOLDINGSYSTEMTYPE_STR"), onBlankHoldingSystemChanged);

		generalSettings.addFileName(BLANK, "", LanguageResource.getString("BLANK_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				String filename = obj.toString();
				if(filename == null || filename == "")
					return;
				
				String ext = FileTools.getExtension(filename);
				
				mBlank = new BezierBoard();

				if(ext.compareToIgnoreCase("s3d")==0)
				{
					S3dReader.loadFile(mBlank, filename);
				}
				else if(ext.compareToIgnoreCase("srf")==0)
				{
					SrfReader.loadFile(mBlank, filename);
				}
				else if(ext.compareToIgnoreCase("cad")==0)
				{
					//TODO: CANNOT USE THIS AS BLANK YET. Fix
				}
				else
				{
					BrdReader.loadFile(mBlank, filename);
				}
				if(mMachineView != null)
				{
					mMachineView.update();
				}
			}			
		}
		);
/* Example of an action inside settings
		generalSettings.addAction(SAVE_G_CODE, "...", "Save g-code", new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				SurfaceSplitsToolpathGenerator toolpathGenerator = new SurfaceSplitsToolpathGenerator(new BullnoseCutter(), BoardCAD.getInstance().getCurrentMachineConfig());

				try {
					toolpathGenerator.writeDeck(filename, BoardCAD.getInstance().getCurrentBrd());
				} catch (Exception e) {
					String str = "Failed to write g-code file :" + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, "Error when writing g-code file",
							JOptionPane.ERROR_MESSAGE);

				}
			}			
		}
		);
*/
		Settings cutsSettings = addCategory(LanguageResource.getString("CUTSCATEGORY_STR"));
		
		cutsSettings.addMeasurement(LENGTHWISE_RESOLUTION, 1.0, LanguageResource.getString("LENGTHWISERESOLUTION_STR"));

		cutsSettings.addObject(DECK_CUTS, new Integer(28), LanguageResource.getString("DECKCUTS_STR"));
		cutsSettings.addObject(DECK_RAIL_CUTS, new Integer(7), LanguageResource.getString("DECKRAILCUTS_STR"));
		cutsSettings.addObject(BOTTOM_CUTS, new Integer(26), LanguageResource.getString("BOTTOMCUTS_STR"));
		cutsSettings.addObject(BOTTOM_RAIL_CUTS, new Integer(0), LanguageResource.getString("BOTTOMRAILCUTS_STR"));
		cutsSettings.addObject(BOTTOM_CLEAN_UP_CUTS, new Integer(2), LanguageResource.getString("BOTTOMCLEANUPCUTS_STR"));
		cutsSettings.addObject(DECK_CLEAN_UP_CUTS, new Integer(2), LanguageResource.getString("DECKCLEANUPCUTS_STR"));
		
		cutsSettings.addObject(DECK_STRINGER_CUT, new Boolean(true), LanguageResource.getString("DECKSTRINGERCUT_STR"));
		cutsSettings.addObject(BOTTOM_STRINGER_CUT, new Boolean(true), LanguageResource.getString("BOTTOMSTRINGERCUT_STR"));

		cutsSettings.addObject(STAY_AWAY_FROM_STRINGER, new Boolean(true), LanguageResource.getString("STAYAWAYFROMSTRINGER_STR"));
		cutsSettings.addObject(STRINGER_WIDTH, generalSettings.new Measurement(0.5), LanguageResource.getString("STRINGERWIDTH_STR"));

		cutsSettings.addObject(DECK_OUTLINE_CUT, new Boolean(true), LanguageResource.getString("DECKOUTLINECUT_STR"));
		HashMap<Integer, String> outlineDepthTypes = new HashMap<Integer, String>();
		outlineDepthTypes.put(0, LanguageResource.getString("DECKOUTLINETOAPEXDEPTH_STR"));
		outlineDepthTypes.put(1, LanguageResource.getString("DECKOUTLINETOBOTTOMDEPTH_STR"));
		cutsSettings.addEnumeration(DECK_OUTLINE_CUT_DEPTH, 0, outlineDepthTypes, LanguageResource.getString("DECKOUTLINECUTDEPTH_STR"));
		
		cutsSettings.addObject(BOTTOM_OUTLINE_CUT, new Boolean(true), LanguageResource.getString("BOTTOMOUTLINECUT_STR"));

		cutsSettings.addObject(CUT_OFF_NOSE, new Boolean(false), LanguageResource.getString("CUTOFFNOSE_STR"));
		cutsSettings.addObject(CUT_OFF_TAIL, new Boolean(false), LanguageResource.getString("CUTOFFTAIL_STR"));

		cutsSettings.addObject(DECK_ANGLE, new Double(28), LanguageResource.getString("DECKDEFINITIONANGLE_STR"));
		cutsSettings.addObject(DECK_RAIL_ANGLE, new Double(130), LanguageResource.getString("DECKRAILDEFINITIONANGLE_STR"));
		cutsSettings.addObject(BOTTOM_ANGLE, new Double(90), LanguageResource.getString("BOTTOMDEFINITIONANGLE_STR"));
		cutsSettings.addObject(BOTTOM_RAIL_ANGLE, new Double(90), LanguageResource.getString("BOTTOMRAILDEFINITIONANGLE_STR"));
		
		Settings speedSettings = addCategory(LanguageResource.getString("SPEEDCATEGORY_STR"));
		speedSettings.addObject(CUTTING_SPEED,  new Double(80), LanguageResource.getString("CUTTINGSPEED_STR"));
		speedSettings.addObject(CUTTING_SPEED_STRINGER,  new Double(254), LanguageResource.getString("STRINGERCUTTINGSPEED_STR"));
		speedSettings.addObject(CUTTING_SPEED_RAIL,  new Double(254), LanguageResource.getString("RAILCUTTINGSPEED_STR"));
		speedSettings.addObject(CUTTING_SPEED_OUTLINE,  new Double(254), LanguageResource.getString("OUTLINECUTTINGSPEED_STR"));
		
		final Settings controlSettings = addCategory(LanguageResource.getString("CONTROLCATEGORY_STR"));
		HashMap<Integer, String> homeingTypes = new HashMap<Integer, String>();
		homeingTypes.put(0, LanguageResource.getString("NOTHING_STR"));
		homeingTypes.put(1, LanguageResource.getString("HOME_STR"));
		homeingTypes.put(2, LanguageResource.getString("HOMEREF_STR"));
		//homeingTypes.put(3, LanguageResource.getString("HOME_Z_STR"));	TODO: Dunno how to do home on z only, have to wait
		homeingTypes.put(3, LanguageResource.getString("LIFTCLEAR_STR"));
		homeingTypes.put(4, LanguageResource.getString("HOMESCRIPT_STR"));
		controlSettings.addEnumeration(BEFORE_DECK_CUT, 1, homeingTypes, LanguageResource.getString("BEFOREDECKCUT_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				Enumeration enu = (Enumeration)obj;
				switch(enu.getValue())
				{
				default:
				case 0:
				case 1:
				case 2:
					controlSettings.removeObject(BEFORE_DECK_CUT_SCRIPT);
					break;
				case 3:
					controlSettings.removeObject(BEFORE_DECK_CUT_SCRIPT);
					break;
					
				case 4:		
					controlSettings.addFileName(BEFORE_DECK_CUT_SCRIPT, "", LanguageResource.getString("BEFOREDECKCUTSCRIPT_STR"));
					break;
				}
				updateClearHeightSetting();
			}
		});
				
		HashMap<Integer, String> startOnDeckTypes = new HashMap<Integer, String>();
		startOnDeckTypes.put(0, LanguageResource.getString("NOSE_STR"));
		startOnDeckTypes.put(1, LanguageResource.getString("TAIL_STR"));
		controlSettings.addEnumeration(DECK_START_POS, 1, startOnDeckTypes, LanguageResource.getString("DECKSTARTPOS_STR"));
	
		controlSettings.addEnumeration(AFTER_DECK_CUT, 1, homeingTypes, LanguageResource.getString("AFTERDECKCUT_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				Enumeration enu = (Enumeration)obj;
				switch(enu.getValue())
				{
				default:
				case 0:
				case 1:
				case 2:
				case 3:
					controlSettings.removeObject(AFTER_DECK_CUT_SCRIPT);
					break;
					
				case 4:				
					controlSettings.addFileName(AFTER_DECK_CUT_SCRIPT, "", LanguageResource.getString("AFTERDECKCUTSCRIPT_STR"));
					break;
				}
				updateClearHeightSetting();
			}
		});

		controlSettings.addEnumeration(BEFORE_BOTTOM_CUT, 1, homeingTypes, LanguageResource.getString("BEFOREBOTTOMCUT_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				Enumeration enu = (Enumeration)obj;
				switch(enu.getValue())
				{
				default:
				case 0:
				case 1:
				case 2:
				case 3:
					controlSettings.removeObject(BEFORE_BOTTOM_CUT_SCRIPT);
					break;
					
				case 4:				
					controlSettings.addFileName(BEFORE_BOTTOM_CUT_SCRIPT, "", LanguageResource.getString("BEFOREBOTTOMCUTSCRIPT_STR"));
					break;
				}
				updateClearHeightSetting();
			}
		});

		HashMap<Integer, String> startOnBottomTypes = new HashMap<Integer, String>();
		startOnBottomTypes.put(0, LanguageResource.getString("NOSE_STR"));
		startOnBottomTypes.put(1, LanguageResource.getString("TAIL_STR"));
		startOnBottomTypes.put(2, LanguageResource.getString("PREVIOUS_STR"));
		controlSettings.addEnumeration(BOTTOM_START_POS, 0, startOnBottomTypes, LanguageResource.getString("BOTTOMSTARTPOS_STR"));

		controlSettings.addEnumeration(AFTER_BOTTOM_CUT, 1, homeingTypes, LanguageResource.getString("AFTERBOTTOMCUT_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{				
				Enumeration enu = (Enumeration)obj;
				switch(enu.getValue())
				{
				default:
				case 0:
				case 1:
				case 2:
				case 3:
					controlSettings.removeObject(AFTER_BOTTOM_CUT_SCRIPT);
					break;
					
				case 4:				
					controlSettings.addFileName(AFTER_BOTTOM_CUT_SCRIPT, "", LanguageResource.getString("AFTERBOTTOMCUTSCRIPT_STR"));
					break;
				}
				updateClearHeightSetting();
			}
		});
		
		Settings.SettingChangedCallback onCutterOffsetChanged = new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				Settings cutterOffsetSettings = getCategory(LanguageResource.getString("CUTTEROFFSETCATEGORY_STR"));
				mCutterOffset = new Point3d(cutterOffsetSettings.getMeasurement(CUTTER_X_OFFSET), cutterOffsetSettings.getMeasurement(CUTTER_Y_OFFSET), cutterOffsetSettings.getMeasurement(CUTTER_Z_OFFSET));
			    mMachineView.get2DView().setCutterOffset(mCutterOffset);
				mMachineView.get3DView().setCutterOffset(mCutterOffset);
			}
		};
		
		Settings cutterOffsetSettings = addCategory(LanguageResource.getString("CUTTEROFFSETCATEGORY_STR"));
		cutterOffsetSettings.addMeasurement(CUTTER_X_OFFSET, 0.0, LanguageResource.getString("CUTTER_X_OFFSET_STR"), onCutterOffsetChanged);
		cutterOffsetSettings.addMeasurement(CUTTER_Y_OFFSET, 0.0, LanguageResource.getString("CUTTER_Y_OFFSET_STR"), onCutterOffsetChanged);
		cutterOffsetSettings.addMeasurement(CUTTER_Z_OFFSET, 0.0, LanguageResource.getString("CUTTER_Z_OFFSET_STR"), onCutterOffsetChanged);
		
		mCutter = new SimpleBullnoseCutter(this);
		mBlankHoldingSystem = new SupportsBlankHoldingSystem(this);		
		mToolpathGenerator = new SurfaceSplitsToolpathGenerator(this, new GCodeWriterWithScaling(this), this.getMachineView());

		//Read cutter and blank holding system settings here so we don't overwrite with default values when getting the full set of settings result in changed of cutter or holding system
		getCategory(LanguageResource.getString("CUTTERCATEGORY_STR")).getPreferences();
		getCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR")).getPreferences();
		
		mMachineView.get3DView().setCutterModel(mCutter.get3DModel());
		mMachineView.get3DView().setBlankHoldingSystemModel(mBlankHoldingSystem.get3DModel());
	}
	
	public void updateClearHeightSetting()
	{
		final Settings controlSettings = getCategory(LanguageResource.getString("CONTROLCATEGORY_STR"));
		int bd = controlSettings.getEnumeration(BEFORE_DECK_CUT);
		int ad = controlSettings.getEnumeration(AFTER_DECK_CUT);
		int bb = controlSettings.getEnumeration(BEFORE_BOTTOM_CUT);
		int ab = controlSettings.getEnumeration(AFTER_BOTTOM_CUT);
		
		if(bd == 3 || ad == 3 || bb == 3 || ab == 3)
		{
			controlSettings.addMeasurement(CLEAR_HEIGHT, 20.0, LanguageResource.getString("CLEARHEIGHT_STR"));			
		}
		else
		{
			controlSettings.removeObject(CLEAR_HEIGHT);
		}
	}
	
	public void setBoard(BezierBoard brd)
	{
		mBrd = brd;
	}
	
	public BezierBoard getBoard()
	{
		return mBrd;
	}
	
	public BezierBoard getBlank()
	{
		return mBlank;
	}
	
	public AbstractCutter getCutter()
	{
		return mCutter;
	}
	
	public Point3d getCutterOffset()
	{
		return mCutterOffset;
	}
	
	public AbstractBlankHoldingSystem getBlankHoldingSystem()
	{
		return mBlankHoldingSystem;
	}

	public AbstractToolpathGenerator getToolpathGenerator()
	{
		return mToolpathGenerator;
	}
	
	public MachineView getMachineView()
	{
		return mMachineView;
	}
	
	public void setMachineView(MachineView view)
	{
		mMachineView = view;;
	}

}

