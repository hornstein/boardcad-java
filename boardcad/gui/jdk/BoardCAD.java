package boardcad.gui.jdk;

/*

 * Created on Sep 17, 2005

 *

 * To change the template for this generated file go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */



import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.Locale;
import java.util.prefs.*;

import javax.imageio.ImageIO;
import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.vecmath.*;

import cadcore.*;
import board.*;

import boardcad.settings.*;
import boardcad.gui.jdk.plugin.*;
import boardcad.DefaultBrds;
import boardcad.FileTools;
import boardcad.print.*;
import boardcad.settings.Settings.Enumeration;
import boardcad.settings.Settings.SettingChangedCallback;
import boardcad.settings.Settings;
import boardcad.export.DxfExport;
import boardcad.i18n.LanguageResource;

import boardcam.cutters.AbstractCutter;
import boardcam.MachineConfig;
import boardcam.Scan;
import boardcam.cutters.SimpleBullnoseCutter;
import boardcam.holdingsystems.SupportsBlankHoldingSystem;
import boardcam.toolpathgenerators.*;
import boardcam.toolpathgenerators.ext.SandwichCompensation;
import boardcam.writers.GCodeWriter;
import board.readers.*;
import board.writers.*;

import boardcad.ScriptLoader;

public class BoardCAD implements Runnable, ActionListener, ItemListener, KeyEventDispatcher {

	enum DeckOrBottom{DECK, BOTTOM, BOTH};

	protected BezierBoard mCurrentBrd;

	private BezierBoard mOriginalBrd;

	private BezierBoard mGhostBrd;

	static protected Locale[] mSupportedLanguages = {new Locale("en",""), new Locale("fr",""), new Locale("pt",""), new Locale("es",""), new Locale("no",""), new Locale("nl","")};

	private BrdCommand mCurrentCommand;

	private BrdCommand mPreviousCommand;

	private PrintBrd mPrintBrd;
	private PrintSpecSheet mPrintSpecSheet;
	private PrintSandwichTemplates mPrintSandwichTemplates;
	private PrintChamberedWoodTemplate mPrintChamberedWoodTemplate;
	private PrintHollowWoodTemplates mPrintHollowWoodTemplates;
	
	private boolean mBlockGUI = true;

	private QuadView fourView;
	public QuadView getFourView() {
        return fourView;
	}
	public String getFourViewName()
	{
		return getFourView().getActive().getName();
	}
	
	private BoardEdit view1;
	private BoardEdit view2;
	private BoardEdit view3;
	private BoardEdit view4;

	private BoardEdit mOutlineEdit;

	private BoardEdit mCrossSectionEdit;

	private BoardEdit mCrossSectionOutlineEdit;

	private BoardEdit mBottomAndDeckEdit;

	private BoardEdit mOutlineEdit2;

	DeckOrBottom mEditDeckorBottom = DeckOrBottom.DECK;

	private BoardSpec mBoardSpec;

	private JPanel panel;

	private ControlPointInfo mControlPointInfo;

	private JSplitPane mCrossSectionSplitPane;

	private BrdEditSplitPane mOutlineAndProfileSplitPane;

	JPanel mNurbspanel;
	JPanel mRenderedpanel;
	public JPanel mJOGLpanel;

	boolean mAlwaysApproximateNurbs = false;

	boolean mNeverApproximateNurbs = false;

	private JFrame mFrame;

	private JToolBar mToolBar;

	public JSplitPane mSplitPane;
	public JTabbedPane mTabbedPane;
	public JTabbedPane mTabbedPane2;

	private JCheckBoxMenuItem mIsPaintingGridMenuItem;

	private JCheckBoxMenuItem mIsPaintingOriginalBrdMenuItem;

	private JCheckBoxMenuItem mIsPaintingGhostBrdMenuItem;

	private JCheckBoxMenuItem mIsPaintingControlPointsMenuItem;

	private JCheckBoxMenuItem mIsPaintingNonActiveCrossSectionsMenuItem;

	private JCheckBoxMenuItem mIsPaintingGuidePointsMenuItem;

	private JCheckBoxMenuItem mIsPaintingCurvatureMenuItem;

	private JCheckBoxMenuItem mIsPaintingVolumeDistributionMenuItem;

	private JCheckBoxMenuItem mIsPaintingCenterOfMassMenuItem;

	private JCheckBoxMenuItem mIsPaintingSlidingInfoMenuItem;

	private JCheckBoxMenuItem mIsPaintingSlidingCrossSectionMenuItem;

	private JCheckBoxMenuItem mIsPaintingFinsMenuItem;

	private JCheckBoxMenuItem mIsPaintingBackgroundImageMenuItem;

	private JCheckBoxMenuItem mIsPaintingBaseLineMenuItem;

	private JCheckBoxMenuItem mIsPaintingCenterLineMenuItem;

	private JCheckBoxMenuItem mIsPaintingOverCurveMesurementsMenuItem;
	
	private JCheckBoxMenuItem mIsPaintingMomentOfInertiaMenuItem;

	private JCheckBoxMenuItem mIsPaintingCrossectionsPositionsMenuItem;

	private JCheckBoxMenuItem mIsPaintingFlowlinesMenuItem;

	private JCheckBoxMenuItem mIsPaintingApexlineMenuItem;

	private JCheckBoxMenuItem mIsPaintingTuckUnderLineMenuItem;

	private JCheckBoxMenuItem mIsPaintingFootMarksMenuItem;
		
	private JCheckBoxMenuItem mIsAntialiasingMenuItem;

	private JCheckBoxMenuItem mUseFillMenuItem;

	private final JMenu mRecentBrdFilesMenu = new JMenu();

	private AbstractAction mSaveBrdAs;
	private AbstractAction mNextCrossSection;
	private AbstractAction mPreviousCrossSection;

	JRadioButtonMenuItem mControlPointInterpolationButton;

	JRadioButtonMenuItem mSBlendInterpolationButton;

	private JCheckBoxMenuItem mShowRenderInwireframe;


	JToggleButton mLifeSizeButton;

	private boolean mBoardChanged = false;

	private boolean mBoardChangedFor3D = false;

	protected boolean mGhostMode = false;

	protected boolean mOrgFocus = false;

	protected static BoardCAD mInstance = null;

	private static final String appname = "BoardCAD v3.0";

	public static String defaultDirectory = "";

	public static double mPrintMarginLeft = 72 / 4;

	public static double mPrintMarginRight = 72 / 4;

	public static double mPrintMarginTop = 72 / 4;

	public static double mPrintMarginBottom = 72 / 4;

	protected DesignPanel design_panel;
	protected DesignPanel design_panel2;
	public JOGLPanel joglpanel;

	public StatusPanel status_panel;

	WeightCalculatorDialog mWeightCalculatorDialog;

	BoardGuidePointsDialog mGuidePointsDialog;

	protected BoardHandler board_handler;
	public JCheckBoxMenuItem mIsLockedX;
	public JCheckBoxMenuItem mIsLockedY;
	public JCheckBoxMenuItem mIsLockedZ;
	public JCheckBoxMenuItem mViewBlank;
	public JCheckBoxMenuItem mViewDeckCut;
	public JCheckBoxMenuItem mViewBottomCut;

	private JCheckBoxMenuItem mShowBezier3DModelMenuItem;

	private BoardCADSettings mSettings;
	private Settings mColorSettings;
	private Settings mSizeSettings;
	private Settings mMiscSettings;

	Switch mBezier3DOnSwitch;
	Shape3D mBezier3DModel;
	TransformGroup mScale;

	public JMenu scriptMenu;

	BezierBoardCrossSection mCrossSectionCopy;

	static private final String BACKGROUNDCOLOR = "backgroundcolor";

	static private final String STRINGERCOLOR = "stringercolor";

	static private final String FLOWLINESCOLOR = "flowlinescolor";

	static private final String APEXLINECOLOR = "apexlinecolor";
	
	static private final String TUCKUNDERLINECOLOR = "tuckunderlinecolor";

	static private final String CENTERLINECOLOR = "centerlinecolor";

	static private final String BRDCOLOR = "brdcolor";

	static private final String ORIGINALCOLOR = "originalcolor";

	static private final String GHOSTCOLOR = "ghostcolor";

	static private final String BLANKCOLOR = "blankcolor";

	static private final String GRIDCOLOR = "gridcolor";

	static private final String FINSCOLOR = "finscolor";

	static private final String CURVATURECOLOR = "curvaturecolor";

	static private final String VOLUMEDISTRIBUTIONCOLOR = "volumedistributioncolor";

	static private final String CENTEROFMASSCOLOR = "centerofmasscolor";

	static private final String SELECTEDTANGENTCOLOR = "selectedtangentcolor";

	static private final String SELECTEDCONTROLPOINTCENTERCOLOR = "selectedcontrolpointcentercolor";

	static private final String SELECTEDCONTROLPOINTTANGENT1COLOR = "selectedcontrolpointtangent1color";

	static private final String SELECTEDCONTROLPOINTTANGENT2COLOR = "selectedcontrolpointtangent2color";

	static private final String SELECTEDCONTROLPOINTCENTEROUTLINECOLOR = "selectedcontrolpointcenteroutlinecolor";

	static private final String SELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR = "selectedcontrolpointtangent1outlinecolor";

	static private final String SELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR = "selectedcontrolpointtangent2outlinecolor";

	static private final String UNSELECTEDTANGENTCOLOR = "unselectedtangentcolor";

	static private final String UNSELECTEDCONTROLPOINTCENTERCOLOR = "unselectedcontrolpointcentercolor";

	static private final String UNSELECTEDCONTROLPOINTTANGENT1COLOR = "unselectedcontrolpointtangent1color";

	static private final String UNSELECTEDCONTROLPOINTTANGENT2COLOR = "unselectedcontrolpointtangent2color";

	static private final String UNSELECTEDCONTROLPOINTCENTEROUTLINECOLOR = "unselectedcontrolpointcenteroutlinecolor";

	static private final String UNSELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR = "unselectedcontrolpointtangent1outlinecolor";

	static private final String UNSELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR = "unselectedcontrolpointtangent2outlinecolor";

	static private final String UNSELECTEDBACKGROUNDCOLOR = "unselectedbackgroundcolor";

	static private final String GUIDEPOINTCOLOR = "guidepointcolor";

	static private final String SELECTEDCONTROLPOINTSIZE = "selectedcontrolpointsize";
	static private final String SELECTEDCONTROLPOINTOUTLINETHICKNESS = "selectedctrlpntoutlinethickness";
	static private final String UNSELECTEDCONTROLPOINTSIZE = "unselectedcontrolpointsize";
	static private final String UNSELECTEDCONTROLPOINTOUTLINETHICKNESS = "unselectedctrlpntoutlinethickness";

	static private final String BEZIERTHICKNESS = "bezierthickness";

	static private final String CURVATURETHICKNESS = "curvaturethickness";

	static private final String VOLUMEDISTRIBUTIONTHICKNESS = "volumedistributionthickness";

	static private final String GUIDEPNTTHICKNESS = "guidepntthickness";

	static private final String BASELINETHICKNESS = "baselinethickness";

	static private final String BASELINECOLOR = "baselinecolor";
	
	static private final String LOOK_AND_FEEL = "lookandfeel";
	
	static private final String PRINTGUIDEPOINTS = "printguidepoints";

	static private final String PRINTFINS = "printfins";

	static private final String FRACTIONACCURACY = "fractionaccuracy";

	static private final String ROCKERSTICK = "rockerstick";

	static private final String OFFSETINTERPLOATION = "offsetinterpolation";

	public static BoardCAD getInstance() {
		if (mInstance == null) {
			mInstance = new BoardCAD();
		}
		return mInstance;
	}

	protected BoardCAD() {


        /*
		 *
		 * Tells the event-dispatching thread (used to
		 *
		 * display and handle events of a Swing GUI) to
		 *
		 * call the run method of "this" (the ClickMeApp
		 *
		 * object this constructor created). The
		 *
		 * argument to invokeLater must implement the
		 *
		 * Runnable interface, which guarantees that
		 *
		 * it defines the run method.
		 *
		 */
		LanguageResource.init(this);

		mCurrentBrd = new BezierBoard();
		mGhostBrd = new BezierBoard();
		mOriginalBrd = new BezierBoard();

		mSettings = new BoardCADSettings();

		mColorSettings = mSettings.addCategory(LanguageResource.getString("COLORS_STR"));

		mColorSettings.addColor(BACKGROUNDCOLOR, new Color(200, 200, 240),LanguageResource.getString("BACKGROUNDCOLOR_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				if(design_panel != null)
					design_panel.get3DView().setBackgroundColor(mColorSettings.getColor(BACKGROUNDCOLOR));
				if(design_panel2 != null)
					design_panel2.get3DView().setBackgroundColor(mColorSettings.getColor(BACKGROUNDCOLOR));
//				if(mMachineView != null)
//					mMachineView.get3DView().setBackgroundColor(mColorSettings.getColor(BACKGROUNDCOLOR));

			}
		});
		mColorSettings.addColor(UNSELECTEDBACKGROUNDCOLOR, new Color(220, 220, 245),LanguageResource.getString("UNSELECTEDBACKGROUNDCOLOR_STR"));
		mColorSettings.addColor(STRINGERCOLOR, new Color(100, 100, 100),LanguageResource.getString("STRINGERCOLOR_STR"));
		mColorSettings.addColor(FLOWLINESCOLOR, new Color(100, 150, 100),LanguageResource.getString("FLOWLINESCOLOR_STR"));
		mColorSettings.addColor(APEXLINECOLOR, new Color(80, 80, 200),LanguageResource.getString("APEXLINECOLOR_STR"));
		mColorSettings.addColor(TUCKUNDERLINECOLOR, new Color(150, 50, 50),LanguageResource.getString("TUCKUNDERCOLOR_STR"));
		mColorSettings.addColor(CENTERLINECOLOR, new Color(180, 180, 220),LanguageResource.getString("CENTERLINECOLOR_STR"));

		mColorSettings.addColor(BRDCOLOR, new Color(0, 0, 0),LanguageResource.getString("BRDCOLOR_STR"));
		mColorSettings.addColor(ORIGINALCOLOR, new Color(240, 240, 240),LanguageResource.getString("ORIGINALCOLOR_STR"));
		mColorSettings.addColor(GHOSTCOLOR, new Color(128, 128, 128),LanguageResource.getString("GHOSTCOLOR_STR"));
		mColorSettings.addColor(BLANKCOLOR, new Color(128, 128, 128), LanguageResource.getString("BLANKCOLOR_STR"));
		mColorSettings.addColor(GRIDCOLOR, new Color(128, 128, 128), LanguageResource.getString("GRIDCOLOR_STR"));
		mColorSettings.addColor(FINSCOLOR, new Color(205, 128, 128), LanguageResource.getString("FINSCOLOR_STR"));
		mColorSettings.addColor(CURVATURECOLOR, new Color(130, 130, 180),LanguageResource.getString("CURVATURECOLOR_STR"));
		mColorSettings.addColor(VOLUMEDISTRIBUTIONCOLOR, new Color(80, 80, 80),LanguageResource.getString("VOLUMEDISTRIBUTIONCOLOR_STR"));
		mColorSettings.addColor(CENTEROFMASSCOLOR, new Color(205, 10, 10),LanguageResource.getString("CENTEROFMASSCOLOR_STR"));
		mColorSettings.addColor(SELECTEDTANGENTCOLOR, new Color(0, 0, 0), LanguageResource.getString("SELECTEDTANGENTCOLOR_STR"));
		mColorSettings.addColor(SELECTEDCONTROLPOINTCENTERCOLOR, new Color(30, 30, 200),LanguageResource.getString("SELECTEDCONTROLPOINTCENTERCOLOR_STR"));
		mColorSettings.addColor(SELECTEDCONTROLPOINTTANGENT1COLOR, new Color(200, 200, 0),LanguageResource.getString("SELECTEDCONTROLPOINTTANGENT1COLOR_STR"));
		mColorSettings.addColor(SELECTEDCONTROLPOINTTANGENT2COLOR, new Color(200, 0, 0),LanguageResource.getString("SELECTEDCONTROLPOINTTANGENT2COLOR_STR"));

		mColorSettings.addColor(SELECTEDCONTROLPOINTCENTEROUTLINECOLOR, new Color(0, 0,0), LanguageResource.getString("SELECTEDCONTROLPOINTCENTEROUTLINECOLOR_STR"));
		mColorSettings.addColor(SELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR, new Color(0,0, 0), LanguageResource.getString("SELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR_STR"));
		mColorSettings.addColor(SELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR, new Color(0, 0, 0), LanguageResource.getString("SELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR_STR"));

		mColorSettings.addColor(UNSELECTEDTANGENTCOLOR, new Color(100, 100, 100), LanguageResource.getString("UNSELECTEDTANGENTCOLOR_STR"));
		mColorSettings.addColor(UNSELECTEDCONTROLPOINTCENTERCOLOR, new Color(200, 200, 240),LanguageResource.getString("UNSELECTEDCONTROLPOINTCENTERCOLOR_STR"));
		mColorSettings.addColor(UNSELECTEDCONTROLPOINTTANGENT1COLOR, new Color(200,200,240),LanguageResource.getString("UNSELECTEDCONTROLPOINTTANGENT1COLOR_STR"));
		mColorSettings.addColor(UNSELECTEDCONTROLPOINTTANGENT2COLOR, new Color(200,200,240),LanguageResource.getString("UNSELECTEDCONTROLPOINTTANGENT2COLOR_STR"));

		mColorSettings.addColor(UNSELECTEDCONTROLPOINTCENTEROUTLINECOLOR, new Color(80, 80, 150), LanguageResource.getString("UNSELECTEDCONTROLPOINTCENTEROUTLINECOLOR_STR"));
		mColorSettings.addColor(UNSELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR, new Color(150, 150, 80), LanguageResource.getString("UNSELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR_STR"));
		mColorSettings.addColor(UNSELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR, new Color(150, 80, 80), LanguageResource.getString("UNSELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR_STR"));

		mColorSettings.addColor(GUIDEPOINTCOLOR, new Color(255, 0, 0),LanguageResource.getString("GUIDEPOINTCOLOR_STR"));
		mColorSettings.addColor(BASELINECOLOR, new Color(0, 0, 0), LanguageResource.getString("GUIDEPOINTCOLOR_STR"));

		mSizeSettings = mSettings.addCategory(LanguageResource.getString("SIZE_AND_THICKNESS_STR"));

		mSizeSettings.addDouble(SELECTEDCONTROLPOINTSIZE, 4.0, LanguageResource.getString("SELECTEDCONTROLPOINTSIZE_STR"));
		mSizeSettings.addDouble(SELECTEDCONTROLPOINTOUTLINETHICKNESS, 1.5, LanguageResource.getString("SELECTEDCONTROLPOINTOUTLINETHICKNESS_STR"));
		mSizeSettings.addDouble(UNSELECTEDCONTROLPOINTSIZE, 3.0, LanguageResource.getString("UNSELECTEDCONTROLPOINTSIZE_STR"));
		mSizeSettings.addDouble(UNSELECTEDCONTROLPOINTOUTLINETHICKNESS, 0.8, LanguageResource.getString("UNSELECTEDCONTROLPOINTOUTLINETHICKNESS_STR"));

		mSizeSettings.addDouble(BEZIERTHICKNESS, 0.8, LanguageResource.getString("BEZIERTHICKNESS_STR"));
		mSizeSettings.addDouble(CURVATURETHICKNESS, 1.2, LanguageResource.getString("CURVATURETHICKNESS_STR"));
		mSizeSettings.addDouble(VOLUMEDISTRIBUTIONTHICKNESS, 1.2, LanguageResource.getString("VOLUMEDISTRIBUTIONTHICKNESS_STR"));
		mSizeSettings.addDouble(GUIDEPNTTHICKNESS, 1.2, LanguageResource.getString("GUIDEPNTTHICKNESS_STR"));
		mSizeSettings.addDouble(BASELINETHICKNESS, 1, LanguageResource.getString("BASELINETHICKNESS_STR"));
		
		HashMap<Integer, String> looks = new HashMap<Integer, String>();
		
		mMiscSettings = mSettings.addCategory(LanguageResource.getString("MISC_STR"));
		String systemLookAndFeelName = UIManager.getSystemLookAndFeelClassName();
		int systemLookAndFeelIndex = 0;
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ){
			looks.put(looks.size(), info.getName());
			if(systemLookAndFeelName == info.getClassName())
			{
				systemLookAndFeelIndex = looks.size()-1;
			}
		}

		mMiscSettings.addObject(LOOK_AND_FEEL, mMiscSettings.new Enumeration(systemLookAndFeelIndex, looks), LanguageResource.getString("LOOKANDFEEL_STR"), new Settings.SettingChangedCallback()
		{
			public void onSettingChanged(Object obj)
			{
				Enumeration e = (Enumeration)mMiscSettings.getObject(LOOK_AND_FEEL);
				
				String selectedLookAndFeelName = e.getAlternatives().get(e.getValue());

				if(mBlockGUI==false)
				{
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), String.format(LanguageResource.getString("LOOKANDFEELCHANGEDMSG_STR"), selectedLookAndFeelName),
							LanguageResource.getString("LOOKANDFEELCHANGEDTITLE_STR"), JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		mMiscSettings.getPreferences();

		try {
			Enumeration e = (Enumeration)mMiscSettings.getObject(LOOK_AND_FEEL);
			
			String selectedLookAndFeelName = e.getAlternatives().get(e.getValue());

			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ){
			    if (selectedLookAndFeelName.equals(info.getName())) {
			    	UIManager.setLookAndFeel(info.getClassName());
			    	break;
			    }
			}
        }
        catch (Exception e) {
            System.err.println("Couldn't find class for specified look and feel:"
                               + UIManager.getSystemLookAndFeelClassName());
            System.err.println("Using the default look and feel.");
        }

		mMiscSettings.addBoolean(PRINTGUIDEPOINTS, false, LanguageResource.getString("PRINTGUIDEPOINTS_STR"));
		mMiscSettings.addBoolean(PRINTFINS, false, LanguageResource.getString("PRINTFINS_STR"));

		mMiscSettings.addInteger(FRACTIONACCURACY, 16, LanguageResource.getString("FRACTIONACCURACY_STR"), new Settings.SettingChangedCallback() {
			
			@Override
			public void onSettingChanged(Object obj) {
				UnitUtils.setFractionAccuracy(((Integer)obj).intValue());
			}
		});

		mMiscSettings.addBoolean(ROCKERSTICK, false, LanguageResource.getString("ROCKERSTICK_STR"));

		mMiscSettings.addBoolean(OFFSETINTERPLOATION, false, LanguageResource.getString("OFFSETINTERPLOATION_STR"));

		mRecentBrdFilesMenu.setText(LanguageResource.getString("RECENTFILES_STR"));

        SwingUtilities.invokeLater(this);

	}

	public static void print(Object object) throws IntrospectionException {
		String objectName = object.getClass().getSimpleName();
		BeanInfo info = Introspector.getBeanInfo(object.getClass());
		System.out.println("--- Object " + objectName + " Begin ----");
		for (PropertyDescriptor property : info.getPropertyDescriptors()) {
			String name = property.getDisplayName();
			Object value = null;
			try {
				value = property.getReadMethod().invoke(object);
			} catch (Exception e) {
				value = "Error - " + e.getMessage();
			}
			System.out.println(name + ": " + value);
		}
		System.out.println("--- Object " + objectName + " End ----");
	}

		public void getPreferences() {
		// Preference keys for this package

		final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);

		defaultDirectory = prefs.get("defaultDirectory", "");
		mIsPaintingGridMenuItem.setSelected(prefs
				.getBoolean("mIsPaintingGridMenuItem", mIsPaintingGridMenuItem
						.isSelected()));
		mIsPaintingOriginalBrdMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingOriginalBrdMenuItem",
				mIsPaintingOriginalBrdMenuItem.isSelected()));
		mIsPaintingGhostBrdMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingGhostBrdMenuItem", mIsPaintingGhostBrdMenuItem
				.isSelected()));
		mIsPaintingControlPointsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingControlPointsMenuItem",
				mIsPaintingControlPointsMenuItem.isSelected()));
		mIsPaintingNonActiveCrossSectionsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingNonActiveCrossSectionsMenuItem",
				mIsPaintingNonActiveCrossSectionsMenuItem.isSelected()));
		mIsPaintingGuidePointsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingGuidePointsMenuItem",
				mIsPaintingGuidePointsMenuItem.isSelected()));
		mIsPaintingCurvatureMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingCurvatureMenuItem", mIsPaintingCurvatureMenuItem
				.isSelected()));
		mIsPaintingVolumeDistributionMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingVolumeDistributionMenuItem", mIsPaintingVolumeDistributionMenuItem
				.isSelected()));
		mIsPaintingCenterOfMassMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingCenterOfMassMenuItem",
				mIsPaintingCenterOfMassMenuItem.isSelected()));
		mIsPaintingSlidingInfoMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingSlidingInfoMenuItem",
				mIsPaintingSlidingInfoMenuItem.isSelected()));
		mIsPaintingSlidingCrossSectionMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingSlidingCrossSectionMenuItem",
				mIsPaintingSlidingCrossSectionMenuItem.isSelected()));
		mIsPaintingFinsMenuItem.setSelected(prefs
				.getBoolean("mIsPaintingFinsMenuItem", mIsPaintingFinsMenuItem
						.isSelected()));
		mIsPaintingBackgroundImageMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingBackgroundImageMenuItem",
				mIsPaintingBackgroundImageMenuItem.isSelected()));
		mIsAntialiasingMenuItem.setSelected(prefs
				.getBoolean("mIsAntialiasingMenuItem", mIsAntialiasingMenuItem
						.isSelected()));
		mUseFillMenuItem.setSelected(prefs
				.getBoolean("mUseFillMenuItem", mUseFillMenuItem
						.isSelected()));
		mIsPaintingBaseLineMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingBaseLineMenuItem", mIsPaintingBaseLineMenuItem
				.isSelected()));
		mIsPaintingCenterLineMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingCenterLineMenuItem", mIsPaintingCenterLineMenuItem
				.isSelected()));
		mIsPaintingOverCurveMesurementsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingoverCurveMesurementsMenuItem", mIsPaintingOverCurveMesurementsMenuItem
				.isSelected()));
		mIsPaintingMomentOfInertiaMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingMomentOfInertiaMenuItem", mIsPaintingMomentOfInertiaMenuItem
				.isSelected()));

		mIsPaintingCrossectionsPositionsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingCrossectionsPositionsMenuItem", mIsPaintingCrossectionsPositionsMenuItem
				.isSelected()));

		mIsPaintingFlowlinesMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingFlowlinesMenuItem", mIsPaintingFlowlinesMenuItem
				.isSelected()));

		mIsPaintingApexlineMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingApexlineMenuItem", mIsPaintingApexlineMenuItem
				.isSelected()));

		mIsPaintingTuckUnderLineMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingTuckUnderLineMenuItem", mIsPaintingTuckUnderLineMenuItem
				.isSelected()));
		mIsPaintingFootMarksMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingFootMarksMenuItem", mIsPaintingFootMarksMenuItem
				.isSelected()));

		mPrintMarginLeft = prefs
		.getDouble("mPrintMarginLeft", mPrintMarginLeft);
		mPrintMarginRight = prefs.getDouble("mPrintMarginRight",
				mPrintMarginRight);
		mPrintMarginTop = prefs.getDouble("mPrintMarginTop", mPrintMarginTop);
		mPrintMarginBottom = prefs.getDouble("mPrintMarginBottom",
				mPrintMarginBottom);

		final int type = prefs.getInt("CrossSectionInterpolationType", getCrossSectionInterpolationTypeAsInt());
		setCrossSectionInterpolationTypeFromInt(type);

		for(int i = 8; i >= 0; i--)
		{
			String id = "mRecentBrdFiles"+i;
			String string = prefs.get(id,"");
			if(string == null || string.compareTo("") == 0)
				continue;

			addRecentBoardFile(string);
		}

		mSettings.getPreferences();
	}

	public void putPreferences() {
		// Preference keys for this package
		final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);

		prefs.put("defaultDirectory", defaultDirectory);
		prefs.putBoolean("mIsPaintingGridMenuItem", mIsPaintingGridMenuItem
				.isSelected());
		prefs.putBoolean("mIsPaintingOriginalBrdMenuItem",
				mIsPaintingOriginalBrdMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingGhostBrdMenuItem",
				mIsPaintingGhostBrdMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingControlPointsMenuItem",
				mIsPaintingControlPointsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingNonActiveCrossSectionsMenuItem",
				mIsPaintingNonActiveCrossSectionsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingGuidePointsMenuItem",
				mIsPaintingGuidePointsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCurvatureMenuItem",
				mIsPaintingCurvatureMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingVolumeDistributionMenuItem",
				mIsPaintingVolumeDistributionMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCenterOfMassMenuItem",
				mIsPaintingCenterOfMassMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingSlidingInfoMenuItem",
				mIsPaintingSlidingInfoMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingSlidingCrossSectionMenuItem",
				mIsPaintingSlidingCrossSectionMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingFinsMenuItem", mIsPaintingFinsMenuItem
				.isSelected());
		prefs.putBoolean("mIsPaintingBackgroundImageMenuItem",
				mIsPaintingCrossectionsPositionsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingBaseLineMenuItem",
				mIsPaintingBaseLineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCenterLineMenuItem",
				mIsPaintingCenterLineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingOverCurveMesurementsMenuItem",
				mIsPaintingOverCurveMesurementsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingMomentOfInertiaMenuItem",
				mIsPaintingMomentOfInertiaMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCrossectionsPositionsMenuItem",
				mIsPaintingCrossectionsPositionsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingFlowlinesMenuItem",
				mIsPaintingFlowlinesMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingApexlineMenuItem",
				mIsPaintingApexlineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingTuckUnderLineMenuItem",
				mIsPaintingTuckUnderLineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingFootMarksMenuItem",
				mIsPaintingFootMarksMenuItem.isSelected());
		prefs.putBoolean("mIsAntialiasingMenuItem",
				mIsAntialiasingMenuItem.isSelected());		
		prefs.putBoolean("mUseFillMenuItem",
				mUseFillMenuItem.isSelected());		
		prefs.putInt("CrossSectionInterpolationType",
				getCrossSectionInterpolationTypeAsInt());

		prefs.putDouble("mPrintMarginLeft", mPrintMarginLeft);
		prefs.putDouble("mPrintMarginRight", mPrintMarginRight);
		prefs.putDouble("mPrintMarginTop", mPrintMarginTop);
		prefs.putDouble("mPrintMarginBottom", mPrintMarginBottom);

		for(int i = 0; i <mRecentBrdFilesMenu.getMenuComponentCount(); i++)
		{
			String str = ((JMenuItem)mRecentBrdFilesMenu.getMenuComponent(i)).getText();
			String id = "mRecentBrdFiles"+i;
			prefs.put(id, str);
		}

		mSettings.putPreferences();
	}

	void addRecentBoardFile(final String filename)
	{
		//Remove item if already exists
		for(int i = 0; i < mRecentBrdFilesMenu.getMenuComponentCount(); i++)
		{
			JMenuItem menuItem = (JMenuItem)mRecentBrdFilesMenu.getMenuComponent(i);
			String str = menuItem.getText();

			if(str.compareTo(filename) == 0)
			{
				mRecentBrdFilesMenu.remove(menuItem);
				break;
			}
		}

		final BoardLoadAction loadRecentBrd = new BoardLoadAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, filename);
				mBrd = BoardCAD.getInstance().getCurrentBrd();
				mCloneBrd = BoardCAD.getInstance().getOriginalBrd();
			};

			public void actionPerformed(ActionEvent event)
			{
				int r = saveChangedBoard();
				if(r == -1 || r==2)	//closed dialog or cancel button pressed
					return;

				String filename = (String)this.getValue(Action.NAME);

				super.load(filename);

				addRecentBoardFile(filename);

				fitAll();
				onBrdChanged();
				onControlPointChanged();
				mBoardChanged = false;
			    boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
			    if(selected && mTabbedPane.getSelectedComponent() == mRenderedpanel)
			    {
			    	updateBezier3DModel();
			    }
			    redraw();
			}
		};

		mRecentBrdFilesMenu.add(new JMenuItem(loadRecentBrd), 0);

		while(mRecentBrdFilesMenu.getMenuComponentCount() > 8)
		{
			mRecentBrdFilesMenu.remove(mRecentBrdFilesMenu.getMenuComponentCount()-1);
		}
	}

	public void setCurrentCommand(final BrdCommand command) {
		mCurrentCommand = command;
	}

	public BrdCommand getCurrentCommand() {
		return mCurrentCommand;
	}

	public void setSelectedEdit(final Component edit) {
		if (edit == mCrossSectionEdit) {
			mTabbedPane.setSelectedComponent(mCrossSectionSplitPane);
		} else if (edit == mOutlineEdit2) {
			mTabbedPane.setSelectedComponent(mOutlineAndProfileSplitPane);
		} else if (edit == mBottomAndDeckEdit) {
			mTabbedPane.setSelectedComponent(mBottomAndDeckEdit);
		} else if (edit == view1 || edit==view2 || edit == view3) {
			mTabbedPane.setSelectedComponent(fourView);
		} else {
			mTabbedPane.setSelectedComponent(edit);
		}
	}

	public JTabbedPane getmTabbedPane() {
		return mTabbedPane;
	}

	public BoardEdit getSelectedEdit() {
		try {
			final Component component = mTabbedPane.getSelectedComponent();

			if (component == mCrossSectionSplitPane) {
				return mCrossSectionEdit;
			}
			else if (component == mOutlineAndProfileSplitPane) {
				return mOutlineAndProfileSplitPane.getActive();
			}
			else if (component == fourView)
			{
				return fourView.getActive();
			}
			else if(component instanceof BoardEdit)
			{
				return (BoardEdit) component;
			}
			else
			{
				return null;
			}
		} catch (final Exception e) {
			System.out.println("BoardCAD.getSelectedEdit() Exception: " + e.toString());
			return null;
		}
	}

	public BoardGuidePointsDialog getGuidePointsDialog()
	{
		return mGuidePointsDialog;
	}

	public JFrame getFrame() {
		return mFrame;
	}

//	public MachineView getMachineView() {
//		return mMachineView;
//	}

	public ControlPointInfo getControlPointInfo() {
		return mControlPointInfo;
	}

	public Color getBrdColor() {

		return mColorSettings.getColor(BRDCOLOR);
	}

	public Color getOriginalBrdColor() {

		return mColorSettings.getColor(ORIGINALCOLOR);
	}

	public Color getGhostBrdColor() {

		return mColorSettings.getColor(GHOSTCOLOR);
	}

	public Color getBlankColor() {

		return mColorSettings.getColor(BLANKCOLOR);
	}

	public Color getBackgroundColor() {

		return mColorSettings.getColor(BACKGROUNDCOLOR);
	}

	public Color getUnselectedBackgroundColor() {

		return mColorSettings.getColor(UNSELECTEDBACKGROUNDCOLOR);
	}

	public Color getStringerColor() {

		return mColorSettings.getColor(STRINGERCOLOR);
	}

	public Color getFlowLinesColor() {

		return mColorSettings.getColor(FLOWLINESCOLOR);
	}

	public Color getApexLineColor() {

		return mColorSettings.getColor(APEXLINECOLOR);
	}

	public Color getTuckUnderLineColor() {

		return mColorSettings.getColor(TUCKUNDERLINECOLOR);
	}

	public Color getCenterLineColor() {

		return mColorSettings.getColor(CENTERLINECOLOR);
	}

	public Color getGridColor() {

		return mColorSettings.getColor(GRIDCOLOR);
	}

	public Color getFinsColor() {

		return mColorSettings.getColor(FINSCOLOR);
	}

	public Color getCurvatureColor() {

		return mColorSettings.getColor(CURVATURECOLOR);
	}

	public Color getVolumeDistributionColor() {

		return mColorSettings.getColor(VOLUMEDISTRIBUTIONCOLOR);
	}

	public Color getCenterOfMassColor() {

		return mColorSettings.getColor(CENTEROFMASSCOLOR);
	}

	public Color getSelectedTangentColor() {

		return mColorSettings.getColor(SELECTEDTANGENTCOLOR);
	}

	public Color getSelectedCenterControlPointColor() {

		return mColorSettings.getColor(SELECTEDCONTROLPOINTCENTERCOLOR);
	}

	public Color getSelectedTangent1ControlPointColor() {

		return mColorSettings.getColor(SELECTEDCONTROLPOINTTANGENT1COLOR);
	}

	public Color getSelectedOutlineTangent2ControlPointColor() {

		return mColorSettings.getColor(SELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR);
	}

	public Color getSelectedOutlineCenterControlPointColor() {

		return mColorSettings.getColor(SELECTEDCONTROLPOINTCENTEROUTLINECOLOR);
	}

	public Color getSelectedOutlineTangent1ControlPointColor() {

		return mColorSettings.getColor(SELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR);
	}

	public Color getSelectedTangent2ControlPointColor() {

		return mColorSettings.getColor(SELECTEDCONTROLPOINTTANGENT2COLOR);
	}

	public Color getUnselectedTangentColor() {

		return mColorSettings.getColor(UNSELECTEDTANGENTCOLOR);
	}

	public Color getUnselectedCenterControlPointColor() {

		return mColorSettings.getColor(UNSELECTEDCONTROLPOINTCENTERCOLOR);
	}

	public Color getUnselectedTangent1ControlPointColor() {

		return mColorSettings.getColor(UNSELECTEDCONTROLPOINTTANGENT1COLOR);
	}

	public Color getUnselectedOutlineTangent2ControlPointColor() {

		return mColorSettings.getColor(UNSELECTEDCONTROLPOINTTANGENT2OUTLINECOLOR);
	}

	public Color getUnselectedOutlineCenterControlPointColor() {

		return mColorSettings.getColor(UNSELECTEDCONTROLPOINTCENTEROUTLINECOLOR);
	}

	public Color getUnselectedOutlineTangent1ControlPointColor() {

		return mColorSettings.getColor(UNSELECTEDCONTROLPOINTTANGENT1OUTLINECOLOR);
	}

	public Color getUnselectedTangent2ControlPointColor() {

		return mColorSettings.getColor(UNSELECTEDCONTROLPOINTTANGENT2COLOR);
	}

	public Color getGuidePointColor() {

		return mColorSettings.getColor(GUIDEPOINTCOLOR);
	}

	public double getSelectedControlPointSize() {

		return mSizeSettings.getDouble(SELECTEDCONTROLPOINTSIZE);
	}

	public double getUnselectedControlPointSize() {

		return mSizeSettings.getDouble(UNSELECTEDCONTROLPOINTSIZE);
	}

	public int getFractionAccuracy() {

		return mMiscSettings.getInt(FRACTIONACCURACY);
	}

	public boolean isPaintingOriginalBrd() {
		return mIsPaintingOriginalBrdMenuItem.isSelected();
	}

	public boolean isPaintingGhostBrd() {
		return mIsPaintingGhostBrdMenuItem.isSelected();
	}

	public boolean isPaintingGrid() {
		return mIsPaintingGridMenuItem.isSelected();
	}

	public boolean isPaintingControlPoints() {
		return mIsPaintingControlPointsMenuItem.isSelected();
	}

	public boolean isPaintingNonActiveCrossSections() {
		return mIsPaintingNonActiveCrossSectionsMenuItem.isSelected();
	}

	public boolean isPaintingGuidePoints() {
		return mIsPaintingGuidePointsMenuItem.isSelected();
	}

	public boolean isPaintingCurvature() {
		return mIsPaintingCurvatureMenuItem.isSelected();
	}

	public boolean isPaintingVolumeDistribution() {
		return mIsPaintingVolumeDistributionMenuItem.isSelected();
	}

	public boolean isPaintingCenterOfMass() {
		return mIsPaintingCenterOfMassMenuItem.isSelected();
	}

	public boolean isPaintingSlidingInfo() {
		return mIsPaintingSlidingInfoMenuItem.isSelected();
	}

	public boolean isPaintingSlidingCrossSection() {
		return mIsPaintingSlidingCrossSectionMenuItem.isSelected();
	}

	public boolean isPaintingFins() {
		return mIsPaintingFinsMenuItem.isSelected();
	}

	public boolean isPaintingBackgroundImage() {
		return mIsPaintingBackgroundImageMenuItem.isSelected();
	}

	public boolean isAntialiasing() {
		return mIsAntialiasingMenuItem.isSelected();
	}

	public boolean isPaintingBaseLine() {
		return mIsPaintingBaseLineMenuItem.isSelected();
	}

	public boolean isPaintingCenterLine() {
		return mIsPaintingCenterLineMenuItem.isSelected();
	}

	public boolean isPaintingOverCurveMeasurements() {
		return mIsPaintingOverCurveMesurementsMenuItem.isSelected();
	}
	
	public boolean isPaintingMomentOfInertia() {
		return mIsPaintingMomentOfInertiaMenuItem.isSelected();
	}
	
	public boolean isPaintingCrossectionsPositions() {
		return mIsPaintingCrossectionsPositionsMenuItem.isSelected();
	}

	public boolean isPaintingFlowlines() {
		return mIsPaintingFlowlinesMenuItem.isSelected();
	}

	public boolean isPaintingApexline() {
		return mIsPaintingApexlineMenuItem.isSelected();
	}

	public boolean isPaintingTuckUnderLine() {
		return mIsPaintingTuckUnderLineMenuItem.isSelected();
	}

	public boolean isPaintingFootMarks() {
		return mIsPaintingFootMarksMenuItem.isSelected();
	}
	
	public boolean useFill() {
		return mUseFillMenuItem.isSelected();
	}

	public boolean isPrintingControlPoints() {
		return mMiscSettings.getBoolean(PRINTGUIDEPOINTS);
	}

	public boolean isPrintingFins() {
		return mMiscSettings.getBoolean(PRINTFINS);
	}

	public boolean isUsingRockerStickAdjustment() {
		return mMiscSettings.getBoolean(ROCKERSTICK);
	}

	public boolean isUsingOffsetInterpolation() {
		return mMiscSettings.getBoolean(OFFSETINTERPLOATION);
	}

	public boolean isGhostMode() {
		return mGhostMode;
	}
	public boolean isOrgFocus() {
		return mOrgFocus;
	}
	


	public AbstractBezierBoardSurfaceModel.ModelType getCrossSectionInterpolationType()
	{
		if(mControlPointInterpolationButton == null)
			return AbstractBezierBoardSurfaceModel.ModelType.SLinearInterpolation;

		if (mSBlendInterpolationButton.isSelected())
			return AbstractBezierBoardSurfaceModel.ModelType.SLinearInterpolation;
		else
			return AbstractBezierBoardSurfaceModel.ModelType.ControlPointInterpolation;
	}
	public int getCrossSectionInterpolationTypeAsInt()
	{
		AbstractBezierBoardSurfaceModel.ModelType type = getCrossSectionInterpolationType();
		switch(type)
		{
			default:
			case ControlPointInterpolation:
				return 2;
			case SLinearInterpolation:
				return 3;
		}
	}

	public void setCrossSectionInterpolationType(final AbstractBezierBoardSurfaceModel.ModelType type) {
		switch (type) {
		default:
		case ControlPointInterpolation:
			mControlPointInterpolationButton.doClick();
			break;
		case SLinearInterpolation:
			mSBlendInterpolationButton.doClick();
			break;
		}
	}

	public void setCrossSectionInterpolationTypeFromInt(int type) {
		switch (type) {
		default:
		case 2:
			mControlPointInterpolationButton.doClick();
			break;
		case 3:
			mSBlendInterpolationButton.doClick();
			break;
		}
		if(mCurrentBrd != null)
		{
			mCurrentBrd.setInterpolationType(getCrossSectionInterpolationType());
		}
	}

	public double getBezierThickness() {
		return mSizeSettings.getDouble(BEZIERTHICKNESS);
	}

	public double getCurvatureThickness() {
		return mSizeSettings.getDouble(CURVATURETHICKNESS);
	}

	public double getVolumeDistributionThickness() {
		return mSizeSettings.getDouble(VOLUMEDISTRIBUTIONTHICKNESS);
	}

	public double getSelectedControlPointOutlineThickness() {
		return mSizeSettings.getDouble(SELECTEDCONTROLPOINTOUTLINETHICKNESS);
	}

	public double getUnselectedControlPointOutlineThickness() {
		return mSizeSettings.getDouble(UNSELECTEDCONTROLPOINTOUTLINETHICKNESS);
	}

	public double getGuidePointThickness() {
		return mSizeSettings.getDouble(GUIDEPNTTHICKNESS);
	}

	public BezierBoard getCurrentBrd() {
		return mCurrentBrd;
	}

	public BezierBoard getOriginalBrd() {
		return mOriginalBrd;
	}

	public BezierBoard getGhostBrd() {
		return mGhostBrd;
	}

	public BoardHandler getBoardHandler(){
		return board_handler;
	}

	public void redraw()
	{
		mOutlineEdit.repaint();
		mBottomAndDeckEdit.repaint();
//		mOutlineEdit2.repaint();
//		mOutlineAndProfileSplitPane.getTopComponent().repaint();
//		mOutlineAndProfileSplitPane.getBottomComponent().repaint();
		view1.repaint();
		view2.repaint();
		view3.repaint();
		view4.repaint();
		design_panel.redraw();
//		fourView.repaint();
//		mFrame.repaint();
	}

	BezierBoard getFocusedBoard()
	{
		if(isGhostMode())
		{
			return BoardCAD.getInstance().getGhostBrd();
		}
		else if(mOrgFocus)
		{
			return BoardCAD.getInstance().getOriginalBrd();
		}
		else
		{
			return BoardCAD.getInstance().getCurrentBrd();
		}

	}

	public void fitAll() {
		mOutlineEdit.fit_all();
		//mOutlineEdit2.fit_all();
		mBottomAndDeckEdit.fit_all();
		mCrossSectionEdit.fit_all();

		view1.fit_all();
		view2.fit_all();
		view3.fit_all();
		view4.fit_all();

		design_panel.fit_all();
//		mMachineView.fit_all();

	}

	public void onBrdChanged() {
		updateScreenValues();
		
		mBoardChanged = true;
		mBoardChangedFor3D = true;
	}

	public void updateScreenValues() {
		if(getCurrentBrd().isEmpty())
		{
			return;
		}
		
		final double length = getCurrentBrd().getLength();

		final double maxWidth = getCurrentBrd().getMaxWidth();

		mFrame.setTitle(appname + " - " + getCurrentBrd().getFilename() + "  "
				+ UnitUtils.convertLengthToCurrentUnit(length, true) + " x "
				+ UnitUtils.convertLengthToCurrentUnit(maxWidth, false));

		mBoardSpec.updateInfo();

		if(mWeightCalculatorDialog.isVisible())
			mWeightCalculatorDialog.updateAll();

		if(mGuidePointsDialog.isVisible())
			mGuidePointsDialog.update();

	}

	protected void setCurrentUnit(int unitType)
	{
		UnitUtils.setCurrentUnit(unitType);
		if(mWeightCalculatorDialog != null)
			mWeightCalculatorDialog.updateAll();
		if(mGuidePointsDialog != null)
			mGuidePointsDialog.update();
		updateScreenValues();
		onControlPointChanged();
		redraw();
	}


	public void onControlPointChanged() {
		final String className = getCurrentCommand().getClass().getSimpleName();

		if (className.compareTo("BrdEditCommand") == 0) {
			final BoardEdit edit = getSelectedEdit();

			if ((edit != null) && edit.getSelectedControlPoints().size() == 1) {
				final BrdEditCommand cmd = (BrdEditCommand) getCurrentCommand();
				mControlPointInfo.mCmd = cmd;
				mControlPointInfo.setEnabled(true);
				final ArrayList<BezierKnot> controlPoints = edit
				.getSelectedControlPoints();
				final BezierKnot controlPoint = controlPoints.get(0);
				mControlPointInfo.setControlPoint(controlPoint);
				mControlPointInfo.setWhich(cmd.mWhich);
			} else {
				mControlPointInfo.setEnabled(false);
			}
		}
	}

	public void onSettingsChanged() {
		mControlPointInfo.setColors();
		mFrame.repaint();
	}

	private void saveAs(String filename)
	{

		final String ext = FileTools.getExtension(filename);
		if (ext != null && ext.compareTo("cad") == 0)
		{
			board_handler.save_board_as(filename);
		}
		else if (ext != null && (ext.compareTo("stp") == 0 || ext.compareTo("step") == 0) )
		{

			try
			{
				board_handler.export_board(new PrintStream(new File(filename)), filename);
			}
			catch(IOException excep2)
			{
				System.out.println("Problem creating file");
			}

			addRecentBoardFile(getCurrentBrd().getFilename());
			onBrdChanged();
			mBoardChanged = false; 
		}
		else
		{
			BrdWriter.saveFile(getCurrentBrd(), filename);

			addRecentBoardFile(getCurrentBrd().getFilename());

			onBrdChanged();
			mBoardChanged = false; // Made a call to onBrdChanged, but
			// we just saved the board
		}
	}

	private int saveChangedBoard()
	{
		if (mBoardChanged == true) {
			final Object[] options = { LanguageResource.getString("YESBUTTON_STR"), LanguageResource.getString("NOBUTTON_STR"), LanguageResource.getString("CANCELBUTTON_STR") };
			final int n = JOptionPane
			.showOptionDialog(
					mFrame,
					LanguageResource.getString("SAVECURRENTBOARDMSG_STR"),
					LanguageResource.getString("SAVECURRENTBOARDTITLE_STR"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null,
					options, options[0]);

			switch (n) {
			case 0:
				mSaveBrdAs.actionPerformed(null);
				return n;
			case -1:
				return n; // break out by close button
			case 1:
				return n;
			case 2:
				return n; // break out
			default:
				return n;

			}
		}
		return 0;
	}
	/**
	 *
	 * Creates and shows the GUI. This method should be
	 *
	 * invoked on the event-dispatching thread.
	 *
	 */

	public void run() {
		createAndShowGUI();
	}

	/**
	 *
	 * Brings up a window that contains a ClickMe component.
	 *
	 * For thread safety, this method should be invoked from
	 *
	 * the event-dispatching thread.
	 *
	 */

	private void createAndShowGUI() {

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(false);

		// Create and set up the window.
		mFrame = new JFrame(" " + appname);
		mFrame.setMinimumSize(new Dimension(1000,700));

		mFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				int r = saveChangedBoard();
				if(r == -1 || r==2)	//closed dialog or cancel button pressed
					return;
				putPreferences();
				System.exit(1);
			}
		});

		// Set up the layout manager.
		mFrame.getContentPane().setLayout(new BorderLayout());

		// Insert 16x16 Icon on JFrame
		try{
			ImageIcon icon = new ImageIcon(getClass().getResource("../../icons/BoardCAD png 16x16 upright.png"));
			mFrame.setIconImage(icon.getImage());
			}catch(Exception e) {
			System.out.println("Jframe Icon error:\n" + e.getMessage());
			}

		JMenuBar menuBar;
		JPopupMenu popupMenu;

		// Menu
		menuBar = new JMenuBar();
		popupMenu = new JPopupMenu();
		final JMenu fileMenu = new JMenu(LanguageResource.getString("FILEMENU_STR"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final AbstractAction newBrd = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("NEWBOARD_STR"));
				this.putValue(Action.SHORT_DESCRIPTION, LanguageResource.getString("NEWBOARD_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));

			};


			public void actionPerformed(ActionEvent arg0) {

				String str = (String) JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("NEWBOARDMSG_STR"),
						LanguageResource.getString("NEWBOARDTITLE_STR"),
						JOptionPane.PLAIN_MESSAGE, null, DefaultBrds
						.getInstance().getDefaultBoardsList(),
						DefaultBrds.getInstance().getDefaultBoardsList()[0]);

				if (str == null)
					return;

				BrdReader.loadFile(getCurrentBrd(), DefaultBrds.getInstance()
						.getBoardArray(str), str);
				mOriginalBrd.set(getCurrentBrd());
				fitAll();
				onBrdChanged();
				onControlPointChanged();

				BrdCommandHistory.getInstance().clear();
				mFrame.repaint();
				mBoardChanged = false;
			    boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
			    if(selected && mTabbedPane.getSelectedComponent() == mRenderedpanel)
			    {
			    	updateBezier3DModel();
			    }
			}

		};
		fileMenu.add(newBrd);

		final BoardLoadAction loadBrd = new BoardLoadAction() {
			static final long serialVersionUID=1L;
			{
				mBrd = mCurrentBrd;
				mCloneBrd = mOriginalBrd;
			};

			public void actionPerformed(ActionEvent event) {

				int r = saveChangedBoard();
				if(r == -1 || r==2)	//closed dialog or cancel button pressed
					return;

				super.actionPerformed(event);

				addRecentBoardFile(getCurrentBrd().getFilename());

				fitAll();
				onBrdChanged();
				onControlPointChanged();
				mBoardChanged = false;
			    boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
			    if(selected && mTabbedPane.getSelectedComponent() == mRenderedpanel)
			    {
			    	updateBezier3DModel();
			    }
			    redraw();
			    
			    
			}
		};
		loadBrd.putValue(AbstractAction.NAME, LanguageResource.getString("BOARDOPEN_STR"));
		loadBrd.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("BOARDOPEN_STR"));
		loadBrd.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		fileMenu.add(loadBrd);

		fileMenu.add(mRecentBrdFilesMenu);
		fileMenu.addSeparator();

		final AbstractAction saveBrd = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("BOARDSAVE_STR"));
				this.putValue(Action.SHORT_DESCRIPTION, LanguageResource.getString("BOARDSAVE_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {

				saveAs(getCurrentBrd().getFilename());
//				BrdWriter.saveFile(getCurrentBrd(), getCurrentBrd().getFilename());

				mBoardChanged = false;
			}

		};
		fileMenu.add(saveBrd);

		mSaveBrdAs = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("BOARDSAVEAS_STR"));
				this.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/save-as.png")));
			};

			public void actionPerformed(final ActionEvent arg0) {

				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setSelectedFile(new File(getCurrentBrd().getFilename()));

				final int returnVal = fc.showSaveDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				final File file = fc.getSelectedFile();

				final String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				saveAs(filename);

			}

		};
		fileMenu.add(mSaveBrdAs);

		final AbstractAction SaveBrd = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("BOARDSAVEANDREFRESH_STR"));
				this.putValue(Action.SHORT_DESCRIPTION, LanguageResource.getString("BOARDSAVEANDREFRESH_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {

				BrdWriter.saveFile(getCurrentBrd(), getCurrentBrd()
						.getFilename());

				mOriginalBrd = (BezierBoard)getCurrentBrd().clone();

				mBoardChanged = false;
			}

		};
		fileMenu.add(SaveBrd);
		fileMenu.addSeparator();

		final BoardLoadAction loadGhost = new BoardLoadAction(mGhostBrd){
			public void actionPerformed(ActionEvent event)
			{
				super.actionPerformed(event);
				mIsPaintingGhostBrdMenuItem.setSelected(true);
				getSelectedEdit().repaint();
			}
		};
		loadGhost.putValue(AbstractAction.NAME, LanguageResource.getString("OPENGHOSTBOARD_STR"));
		loadGhost.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
		fileMenu.add(loadGhost);

		final AbstractAction loadImage = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("LOADBACKGROUNDIMAGE_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {

				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showOpenDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				BoardEdit edit = getSelectedEdit();
				if (edit == null)
					return;

				edit.loadBackgroundImage(filename);
				mIsPaintingBackgroundImageMenuItem.setSelected(true);
				edit.repaint();
			}

		};
		fileMenu.add(loadImage);
		fileMenu.addSeparator();

		final JMenu printMenu = new JMenu(LanguageResource.getString("PRINTMENU_STR"));
		final JMenuItem printOutline = new JMenuItem(LanguageResource.getString("PRINTOUTLINE_STR"), KeyEvent.VK_O);
		printOutline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
				ActionEvent.ALT_MASK));
		printOutline.addActionListener(this);
		printMenu.add(printOutline);

		final JMenuItem printSpinTemplate = new JMenuItem(LanguageResource.getString("PRINTSPINTEMPLATE_STR"), KeyEvent.VK_T);
//		printOutline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
//				ActionEvent.ALT_MASK));
		printSpinTemplate.addActionListener(this);
		printMenu.add(printSpinTemplate);

		final JMenuItem printProfile = new JMenuItem(LanguageResource.getString("PRINTPROFILE_STR"), KeyEvent.VK_P);
		printProfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
				ActionEvent.ALT_MASK));
		printProfile.addActionListener(this);
		printMenu.add(printProfile);

		final JMenuItem printSlices = new JMenuItem(LanguageResource.getString("PRINTCROSSECTION_STR"),KeyEvent.VK_S);
		printSlices.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,ActionEvent.ALT_MASK));
		printSlices.addActionListener(this);
		printMenu.add(printSlices);

		printMenu.addSeparator();

		final JMenu printSandwichMenu = new JMenu(LanguageResource.getString("PRINTSANDWICHTEMPLATESMENU_STR"));

		final AbstractAction printProfileTemplate = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHPROFILETEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("SkinThickness", 0.3, LanguageResource.getString("SANDWICHSKINTHICKNESS_STR"));
				sandwichSettings.addBoolean("Flatten", false, LanguageResource.getString("SANDWICHFLATTEN_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHPROFILETEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printProfileTemplate(sandwichSettings.getMeasurement("SkinThickness"),sandwichSettings.getBoolean("Flatten"), 0.0);
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printProfileTemplate);

		final AbstractAction printRailTemplate = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHRAILTEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("SkinThickness", 0.3, LanguageResource.getString("SANDWICHSKINTHICKNESS_STR"));
				sandwichSettings.addMeasurement("ToRail", 2.54/2, LanguageResource.getString("SANDWICHDISTANCETORAIL_STR"));
				sandwichSettings.addMeasurement("TailOffset", 2.0,LanguageResource.getString("SANDWICHTAILOFFSET"));
				sandwichSettings.addMeasurement("NoseOffset", 6.0, LanguageResource.getString("SANDWICHNOSEOFFSET"));
				sandwichSettings.addBoolean("Flatten", false,  LanguageResource.getString("SANDWICHFLATTEN_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHRAILTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printRailTemplate(sandwichSettings.getMeasurement("ToRail"), sandwichSettings.getMeasurement("SkinThickness"),sandwichSettings.getMeasurement("TailOffset"),sandwichSettings.getMeasurement("NoseOffset"), sandwichSettings.getBoolean("Flatten"));
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printRailTemplate);

		final AbstractAction printDeckSkin = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHDECKSKINTEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("ToRail", 2.54/2, LanguageResource.getString("SANDWICHDISTANCETORAIL_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHDECKSKINTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printDeckSkinTemplate(sandwichSettings.getMeasurement("ToRail"));
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printDeckSkin);

		final AbstractAction printBottomSkin = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHBOTTOMSKINTEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("ToRail", 2.54/2, LanguageResource.getString("SANDWICHDISTANCETORAIL_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHBOTTOMSKINTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printDeckSkinTemplate(sandwichSettings.getMeasurement("ToRail"));
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printBottomSkin);

		printMenu.add(printSandwichMenu);

		printMenu.addSeparator();

		final JMenu printHWSMenu = new JMenu(LanguageResource.getString("PRINTHWSMENU_STR"));

		final AbstractAction printHWSSTringer = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSSTRINGER_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSSTRINGERTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printStringerTemplate(HWSSettings.getMeasurement("SkinThickness"),HWSSettings.getMeasurement("FrameThickness"), HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSSTringer);

		final AbstractAction printHWSRibs = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSRIBS_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0, LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSRIBSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printCrosssectionTemplates(HWSSettings.getMeasurement("DistanceFromRail"),HWSSettings.getMeasurement("SkinThickness"),HWSSettings.getMeasurement("FrameThickness"), HWSSettings.getMeasurement("Webbing"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSRibs);

		final AbstractAction printHWSRail = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSRAIL_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0, LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSRAILTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printRailTemplate(HWSSettings.getMeasurement("DistanceFromRail"),HWSSettings.getMeasurement("SkinThickness"),HWSSettings.getMeasurement("FrameThickness"), HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSRail);

		final AbstractAction printHWSNosePiece = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSTAILPIECE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0, LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSTAILPIECETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printNoseTemplate(HWSSettings.getMeasurement("DistanceFromRail"),HWSSettings.getMeasurement("SkinThickness"),HWSSettings.getMeasurement("FrameThickness"), HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSNosePiece);

		final AbstractAction printHWSTailPiece = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSNOSEPIECE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0, LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSNOSEPIECETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printTailTemplate(HWSSettings.getMeasurement("DistanceFromRail"),HWSSettings.getMeasurement("SkinThickness"),HWSSettings.getMeasurement("FrameThickness"), HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSTailPiece);

		final AbstractAction printHWSDeckTemplate = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSDECKTEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0, LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSDECKTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printDeckSkinTemplate(HWSSettings.getMeasurement("DistanceFromRail"), HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSDeckTemplate);

		final AbstractAction printHWSBottomTemplate = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSBOTTOMTEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0, LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSBOTTOMTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printBottomSkinTemplate(HWSSettings.getMeasurement("DistanceFromRail"), HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSBottomTemplate);

		printMenu.add(printHWSMenu);
		
		final JMenu printChamberedWoodMenu = new JMenu(LanguageResource.getString("PRINTCHAMBEREDWOODTEMPLATESMENU_STR"));

		final AbstractAction printChamberedWoodTemplate = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTCHAMBEREDWOODPROFILETEMPLATE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				final CategorizedSettings settings = new CategorizedSettings();
				final String categoryName = LanguageResource.getString("CHAMBEREDWOODPARAMETERSCATEGORY_STR");
				final Settings chamberedWoodSettings = settings.addCategory(categoryName);
				chamberedWoodSettings.addBoolean("Draw grid", true, LanguageResource.getString("DRAWGRID_STR"));
				chamberedWoodSettings.addMeasurement("Start Offset from center", 0.0, LanguageResource.getString("CHAMBEREDWOODOFFSETFROMCENTER_STR"));
				chamberedWoodSettings.addMeasurement("End Offset from center", mCurrentBrd.getMaxWidth()/2.0, LanguageResource.getString("CHAMBEREDWOODENDOFFSET_STR"));
				chamberedWoodSettings.addMeasurement("Plank thickness", 2.54, LanguageResource.getString("CHAMBEREDWOODPLANKTHICKNESS_STR"));
				chamberedWoodSettings.addMeasurement("Deck/Bottom thickness", 0.8, LanguageResource.getString("CHAMBEREDWOODDECKANDBOTTOMTHICKNESS_STR"));
				chamberedWoodSettings.addBoolean("Draw chambering", true, LanguageResource.getString("CHAMBEREDDRAWCHAMBERING_STR"));
				chamberedWoodSettings.addBoolean("Draw alignment marks", true, LanguageResource.getString("CHAMBEREDDRAWALIGNEMNETMARKS_STR"));

				chamberedWoodSettings.addBoolean("Print multiple", false, LanguageResource.getString("CHAMBEREDPRINTMULTIPLETEMPLATES_STR"));
				
				settings.getPreferences();
				
				if(chamberedWoodSettings.getMeasurement("End Offset from center") > mCurrentBrd.getMaxWidth()/2.0)
				{
					chamberedWoodSettings.setMeasurement("End Offset from center", mCurrentBrd.getMaxWidth()/2.0);
				}

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTCHAMBEREDWOODPROFILETEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				
				double start = chamberedWoodSettings.getMeasurement("Start Offset from center");
				double end = chamberedWoodSettings.getMeasurement("End Offset from center");
				double plankThickness = chamberedWoodSettings.getMeasurement("Plank thickness");

				boolean printMultiple = chamberedWoodSettings.getBoolean("Print multiple");
				if(printMultiple)
				{
					
					int numberOfTemplates =  (int)((end-start)/plankThickness);
					
					int selection = JOptionPane.showConfirmDialog(
							BoardCAD.getInstance().getFrame(),
							String.valueOf(numberOfTemplates) + " " + LanguageResource.getString("PRINTCHAMBEREDWOODMULTIPLETEMPLATESMSG_STR"),
							LanguageResource.getString("PRINTCHAMBEREDWOODMULTIPLETEMPLATESTITLE_STR"), JOptionPane.WARNING_MESSAGE,
							JOptionPane.YES_NO_OPTION);
					if (selection != JOptionPane.YES_OPTION) {

						return;

					}
				}

				mPrintChamberedWoodTemplate.printTemplate(chamberedWoodSettings.getBoolean("Draw grid"), start, end, plankThickness, chamberedWoodSettings.getMeasurement("Deck/Bottom thickness"), chamberedWoodSettings.getBoolean("Draw chambering"), chamberedWoodSettings.getBoolean("Draw alignment marks"), printMultiple);
				
				settingsDialog.dispose();
				
				settings.putPreferences();
			}

		};
		printChamberedWoodMenu.add(printChamberedWoodTemplate);

		printMenu.add(printChamberedWoodMenu);

		printMenu.addSeparator();

		/*		final JMenuItem printSpecSheet = new JMenuItem(LanguageResource.getString("PRINTSPECSHEET_STR"),KeyEvent.VK_H);
		printSpecSheet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5,ActionEvent.ALT_MASK));
		printSpecSheet.addActionListener(this);
		printMenu.add(printSpecSheet);
*/


		final AbstractAction printSpecSheet = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSPECSHEET_STR"));
				this.putValue(Action.SHORT_DESCRIPTION, LanguageResource.getString("PRINTSPECSHEET_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {
				mPrintSpecSheet.printSpecSheet();
			}

		};

		printMenu.add(printSpecSheet);






		mPrintBrd = new PrintBrd();
		mPrintSpecSheet = new PrintSpecSheet();
		mPrintChamberedWoodTemplate = new PrintChamberedWoodTemplate();
		mPrintSandwichTemplates = new PrintSandwichTemplates();
		mPrintHollowWoodTemplates = new PrintHollowWoodTemplates();

		final AbstractAction printSpecSheetToFile = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSPECSHEETTOFILE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				TwoValuesInputDialog resDialog = new TwoValuesInputDialog(
						mFrame);
				resDialog.setMessageText(LanguageResource.getString("PRINTSPECSHEETTOFILERESOLUTIONMSG_STR"));
				resDialog.setValue1(1200);
				resDialog.setValue2(1600);
				resDialog.setValue1LabelText(LanguageResource.getString("PRINTSPECSHEETTOFILEWIDTH_STR"));
				resDialog.setValue2LabelText(LanguageResource.getString("PRINTSPECSHEETTOFILEHEIGHT_STR"));
				resDialog.setModal(true);

				resDialog.setVisible(true);
				if (resDialog.wasCancelled()) {
					resDialog.dispose();
					return;
				}
				int width = 0, height = 0;
				try {
					width = (int) resDialog.getValue1();
					height = (int) resDialog.getValue2();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), LanguageResource.getString("PRINTSPECSHEETTOFILEINVALIDPARAMETERSMSG_STR"),
									LanguageResource.getString("PRINTSPECSHEETTOFILEINVALIDPARAMETERSTITLE_STR"), JOptionPane.ERROR_MESSAGE);
					return;
				}

				BufferedImage img = new BufferedImage(height, width,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = img.createGraphics();

				Graphics2D g2d = (Graphics2D) g.create();

				// Turn on antialiasing, so painting is smooth.

				g2d.setRenderingHint(

						RenderingHints.KEY_ANTIALIASING,

						RenderingHints.VALUE_ANTIALIAS_ON);

				Paper paper = new Paper();
				paper.setImageableArea(0, 0, width, height);
				paper.setSize(width, height);
				PageFormat myPageFormat = new PageFormat();
				myPageFormat.setPaper(paper);
				myPageFormat.setOrientation(PageFormat.LANDSCAPE);

				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, height - 1, width - 1);

				mPrintSpecSheet.print(g2d, myPageFormat, 0);

				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				// Create a file dialog box to prompt for a new file to display
				FileFilter filter = new FileFilter() {

					// Accept all directories and graphics files.
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null
								&& (extension.equals("png")
										|| extension.equals("gif")
										|| extension.equals("bmp") || extension
										.equals("jpg"))) {
							return true;
						}

						return false;
					}

					// The description of this filter
					public String getDescription() {
						return LanguageResource.getString("PRINTSPECSHEETTOFILEIMAGEFILES_STR");
					}
				};

				fc.setFileFilter(filter);

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				if(FileTools.getExtension(filename) == "")
				{
					filename = FileTools.setExtension(filename, "jpg");
				}

				BoardCAD.defaultDirectory = file.getPath();

				try {
					File outputfile = new File(filename);
					ImageIO.write(img, FileTools.getExtension(filename),outputfile);
				} catch (Exception e) {
					String str = LanguageResource.getString("PRINTSPECSHEETTOFILEERRORMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str, LanguageResource.getString("PRINTSPECSHEETTOFILEERRORTITLE_STR"),	JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		printMenu.add(printSpecSheetToFile);

		fileMenu.add(printMenu);

		final JMenu importMenu = new JMenu(LanguageResource.getString("IMPORTMENU_STR"));
		

		final JMenu importBezierMenu = new JMenu(LanguageResource.getString("IMPORTBEZIERMENU_STR"));
		importMenu.add(importBezierMenu);
		final AbstractAction importOutlineAction = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("IMPORTBEZIEROUTLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				BrdImportOutlineCommand cmd = new BrdImportOutlineCommand(mOutlineEdit);
				cmd.execute();
			}

		};
		importBezierMenu.add(importOutlineAction);
		final AbstractAction importProfileAction = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("IMPORTBEZIERPROFILE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				BrdImportProfileCommand cmd = new BrdImportProfileCommand(mBottomAndDeckEdit);
				cmd.execute();
			}

		};
		importBezierMenu.add(importProfileAction);
		final AbstractAction importCrossSectionAction = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("IMPORTBEZIERCROSSSECTION_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				BrdImportCrossSectionCommand cmd = new BrdImportCrossSectionCommand(mCrossSectionEdit);
				cmd.execute();
			}

		};
		importBezierMenu.add(importCrossSectionAction);

		fileMenu.add(importMenu);

		final JMenu exportMenu = new JMenu(LanguageResource.getString("EXPORTMENU_STR"));

		final AbstractAction exportStep = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTNURBSTOSTEP_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					board_handler.export_board(new PrintStream(new File(
							filename)), filename);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTNURBSTOSTEPFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTNURBSTOSTEPFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};

		exportMenu.add(exportStep);


		final AbstractAction exportDxf3D = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTNURBSTODXF_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					board_handler.export_board_dxf(new PrintStream(new File(
							filename)), filename);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTNURBSTODXFFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTNURBSTODXFFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportDxf3D);

		final AbstractAction exportStl3D = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTNURBSTOSTL_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					board_handler.export_board_stl(new PrintStream(new File(
							filename)), filename);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTNURBSTOSTLFAILEDTITLE_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTNURBSTOSTLFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};

		exportMenu.add(exportStl3D);
		exportMenu.addSeparator();

		JMenu beziersExportMenu = new JMenu(LanguageResource.getString("EXPORTBEZIERS_STR"));
		final AbstractAction exportBezierOutline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIEROUTLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setFileFilter(new FileFilter() {

					// Accept all directories and brd and s3d files.
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null && extension.equals("otl") )
						{
							return true;
						}

						return false;
					}

					// The description of this filter
					public String getDescription() {
						return LanguageResource.getString("OUTLINEFILES_STR");
					}
				});

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				try {
					if(BrdWriter.exportOutline(getCurrentBrd(), filename) == false)
					{
						throw new Exception();
					}
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIEROUTLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIEROUTLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}

				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		beziersExportMenu.add(exportBezierOutline);

		final AbstractAction exportBezierProfile = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERPROFILE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setFileFilter(new FileFilter() {

					// Accept all directories and pfl files.
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null && extension.equals("pfl") )
						{
							return true;
						}

						return false;
					}

					// The description of this filter
					public String getDescription() {
						return LanguageResource.getString("PROFILEFILES_STR");
					}
				});

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				try {
					if(BrdWriter.exportProfile(getCurrentBrd(), filename) == false)
					{
						throw new Exception();
					}
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIERFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}

				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		beziersExportMenu.add(exportBezierProfile);

		final AbstractAction exportBezierCrossection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERCROSSECTION_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setFileFilter(new FileFilter() {

					// Accept all directories and brd and s3d files.
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null && extension.equals("crs") )
						{
							return true;
						}

						return false;
					}

					// The description of this filter
					public String getDescription() {
						return LanguageResource.getString("CROSSECTIONFILES_STR");
					}
				});

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				try {
					if(BrdWriter.exportCrossection(getCurrentBrd(), getCurrentBrd().getCurrentCrossSectionIndex(), filename) == false)
					{
						throw new Exception();
					}
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIERFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}

				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		beziersExportMenu.add(exportBezierCrossection);

		exportMenu.add(beziersExportMenu);
		exportMenu.addSeparator();

		final AbstractAction exportProfileAsDxfSpline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERPROFILEASDXFSPLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd()
					.getBottom();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd()
					.getDeck();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint(
								(org.getNrOfControlPoints() - 1) - i).clone();
						controlPoint.switch_tangents();
						patches[1].append(controlPoint);
					}

					DxfExport.exportBezierSplines(filename, patches);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERPROFILEASDXFSPLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIERPROFILEASDXFSPLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportProfileAsDxfSpline);

		final AbstractAction exportOutlineAsDxfSpline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME,  LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFSPLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd()
					.getOutline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd()
					.getOutline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint(
								(org.getNrOfControlPoints() - 1) - i).clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().y = -controlPoint
						.getEndPoint().y;
						controlPoint.getTangentToPrev().y = -controlPoint
						.getTangentToPrev().y;
						controlPoint.getTangentToNext().y = -controlPoint
						.getTangentToNext().y;
						patches[1].append(controlPoint);
					}
					DxfExport.exportBezierSplines(filename, patches);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFSPLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFSPLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportOutlineAsDxfSpline);
		

		final AbstractAction exportCurrentCrossSectionAsDxfSpline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME,  LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFSPLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try
				{
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection().getBezierSpline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection().getBezierSpline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++)
					{
						BezierKnot controlPoint = (BezierKnot)org.getControlPoint((org.getNrOfControlPoints() - 1) - i).clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().x = -controlPoint.getEndPoint().x;
						controlPoint.getTangentToPrev().x = -controlPoint .getTangentToPrev().x;
						controlPoint.getTangentToNext().x = -controlPoint .getTangentToNext().x;
						patches[1].append(controlPoint);
					}
					DxfExport.exportBezierSplines(filename, patches);
				} 
				catch (Exception e) 
				{
					String str = LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFSPLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFSPLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportCurrentCrossSectionAsDxfSpline);
		
		exportMenu.addSeparator();


		final AbstractAction exportProfileAsDxfPolyline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERPROFILEASDXFPOLYLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd()
					.getBottom();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd()
					.getDeck();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint(
								(org.getNrOfControlPoints() - 1) - i).clone();
						controlPoint.switch_tangents();
						patches[1].append(controlPoint);
					}

					DxfExport.exportPolylineFromSplines(filename, patches, 100);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERPROFILEASDXFPOLYLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIERPROFILEASDXFPOLYLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportProfileAsDxfPolyline);

		final AbstractAction exportOutlineAsDxfPolyline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFPOLYLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd()
					.getOutline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd()
					.getOutline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint(
								(org.getNrOfControlPoints() - 1) - i).clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().y = -controlPoint
						.getEndPoint().y;
						controlPoint.getTangentToPrev().y = -controlPoint
						.getTangentToPrev().y;
						controlPoint.getTangentToNext().y = -controlPoint
						.getTangentToNext().y;
						patches[1].append(controlPoint);
					}
					DxfExport.exportPolylineFromSplines(filename, patches, 100);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFPOLYLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFPOLYLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportOutlineAsDxfPolyline);

		final AbstractAction exportCrossSectionAsDxfPolyline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFPOLYLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				try 
				{
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection().getBezierSpline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection().getBezierSpline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++)
					{
						BezierKnot controlPoint = (BezierKnot)org.getControlPoint((org.getNrOfControlPoints() - 1) - i).clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().x = -controlPoint.getEndPoint().x;
						controlPoint.getTangentToPrev().x = -controlPoint.getTangentToPrev().x;
						controlPoint.getTangentToNext().x = -controlPoint.getTangentToNext().x;
						patches[1].append(controlPoint);
					}
					DxfExport.exportPolylineFromSplines(filename, patches, 100);
				} 
				catch (Exception e)
				{
					String str = LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFPOLYLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("EXPORTBEZIERCROSSECTIONASDXFPOLYLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportCrossSectionAsDxfPolyline);
		
		fileMenu.add(exportMenu);

		final JMenu gcodeMenu = new JMenu(LanguageResource.getString("GCODEMENU_STR"));


		AbstractAction exportGcode = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, "Nurbs to Gcode deck");
			};

			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath();    // Load and display selection
				if(filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				if(board_handler.is_empty()) {
					board_handler.approximate_bezier(getCurrentBrd(), false);
				}
				board_handler.generateGCodeDeck(filename);
			}

		};
		gcodeMenu.add(exportGcode);


		AbstractAction exportGcodeBottom = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, "Nurbs to Gcode bottom");
			};

			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath();    // Load and display selection
				if(filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				if(board_handler.is_empty()) {
					board_handler.approximate_bezier(getCurrentBrd(), false);
				}
				board_handler.generateGCodeBottom(filename);
			}

		};
		gcodeMenu.add(exportGcodeBottom);

		gcodeMenu.addSeparator();

		final AbstractAction gcodeBezier = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEBEZIER_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				BezierBoard brd = getCurrentBrd();
				MachineConfig machineConfig = new MachineConfig();
				machineConfig.setBoard((BezierBoard)brd.clone());
				MachineDialog dialog = new MachineDialog(machineConfig);
				//dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
				//dialog.setModal(false);
				
				machineConfig.setMachineView(dialog.getMachineView());
				machineConfig.initialize();
				machineConfig.getPreferences();
				
				//Turn of sandwich compensation so we don't use sandwich compensation by accident (lesson learned the hard way)
				Settings sandwichCompensationSettings = machineConfig.getCategory(LanguageResource.getString("SANDWICHCOMPENSATIONCATEGORY_STR"));
				sandwichCompensationSettings.setBoolean(SandwichCompensation.SANDWICH_DECK_COMPENSATION_ON, false);
				sandwichCompensationSettings.setBoolean(SandwichCompensation.SANDWICH_BOTTOM_COMPENSATION_ON, false);
				sandwichCompensationSettings.setBoolean(SandwichCompensation.SANDWICH_OUTLINE_COMPENSATION_ON, false);

				Settings generalSettings = machineConfig.getCategory(LanguageResource.getString("GENERALCATEGORY_STR"));
				
				if(generalSettings.getBoolean(MachineConfig.USE_BRD_SETTINGS) == true)
				{
					System.out.printf("Using board settings");
					if(generalSettings.getEnumeration(MachineConfig.BLANKHOLDINGSYSTEM_TYPE) == 0)
					{
						//generalSettings.getDouble(MachineConfig.TAILSTOP_POS,  );

						Settings supportsSettings = machineConfig.getCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));

						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_1_POS, new Double(brd.getStrut1()[0]));
						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_2_POS, new Double(brd.getStrut2()[0]));
						
						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_1_HEIGHT, new Double(brd.getStrut1()[1]));
						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_2_HEIGHT, new Double(brd.getStrut2()[1]));
					}
							
					generalSettings.setObject(MachineConfig.BLANK, generalSettings.new FileName(brd.getBlankFile()));

					Settings cutsSettings = machineConfig.getCategory(LanguageResource.getString("CUTSCATEGORY_STR"));

					cutsSettings.setObject(MachineConfig.DECK_CUTS, new Integer(brd.getTopCuts()));
					cutsSettings.setObject(MachineConfig.DECK_RAIL_CUTS, new Integer(brd.getTopShoulderCuts()));
					cutsSettings.setObject(MachineConfig.BOTTOM_CUTS, new Integer(brd.getBottomCuts()));
					cutsSettings.setObject(MachineConfig.BOTTOM_RAIL_CUTS, new Integer(brd.getBottomRailCuts()));
					
					cutsSettings.setObject(MachineConfig.DECK_ANGLE, new Double(brd.getTopShoulderAngle()));
					cutsSettings.setObject(MachineConfig.DECK_RAIL_ANGLE, new Double(brd.getMaxAngle()));
					
					Settings speedSettings = machineConfig.getCategory(LanguageResource.getString("SPEEDCATEGORY_STR"));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED,  new Double(brd.getRegularSpeed()));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED_STRINGER,  new Double(brd.getStringerSpeed()));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED_RAIL,  new Double(brd.getRegularSpeed()));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED_OUTLINE,  new Double(brd.getRegularSpeed()));
					
				}
					
								
				dialog.setVisible(true);
				
				if(generalSettings.getBoolean(MachineConfig.USE_BRD_SETTINGS))
				{
					if(generalSettings.getEnumeration(MachineConfig.BLANKHOLDINGSYSTEM_TYPE) == 0)
					{
						//generalSettings.getDouble(MachineConfig.TAILSTOP_POS,  );

						Settings supportsSettings = machineConfig.addCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));

						//generalSettings.getDouble(MachineConfig.TAILSTOP_POS,  );
						brd.getStrut1()[0] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_1_POS);
						brd.getStrut2()[0] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_2_POS);
						
						brd.getStrut1()[1] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_1_HEIGHT);
						brd.getStrut2()[1] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_2_HEIGHT);
					}
							
					brd.setBlankFile(generalSettings.getFileName(MachineConfig.BLANK));

					Settings cutsSettings = machineConfig.getCategory(LanguageResource.getString("CUTSCATEGORY_STR"));

					brd.setTopCuts(cutsSettings.getInt(MachineConfig.DECK_CUTS));
					brd.setTopShoulderCuts(cutsSettings.getInt(MachineConfig.DECK_RAIL_CUTS));
					brd.setBottomCuts(cutsSettings.getInt(MachineConfig.BOTTOM_CUTS));
					brd.setBottomRailCuts(cutsSettings.getInt(MachineConfig.BOTTOM_RAIL_CUTS));
					
					brd.setTopShoulderAngle(cutsSettings.getDouble(MachineConfig.DECK_ANGLE));
					brd.setMaxAngle(cutsSettings.getDouble(MachineConfig.DECK_RAIL_ANGLE));
//					cutsSettings.getDouble(MachineConfig.BOTTOM_ANGLE, new Double(90));
//					cutsSettings.getDouble(MachineConfig.BOTTOM_RAIL_ANGLE, new Double(90));
					
					Settings speedSettings = machineConfig.getCategory(LanguageResource.getString("SPEEDCATEGORY_STR"));
					brd.setRegularSpeed((int)speedSettings.getDouble(MachineConfig.CUTTING_SPEED));
					brd.setStringerSpeed((int)speedSettings.getDouble(MachineConfig.CUTTING_SPEED_STRINGER));
					brd.setRegularSpeed((int)speedSettings.getDouble(MachineConfig.CUTTING_SPEED_RAIL));
//					speedSettings.getDouble(MachineConfig.CUTTING_SPEED_NOSE_REDUCTION,  new Double(0.5));
//					speedSettings.getDouble(MachineConfig.CUTTING_SPEED_TAIL_REDUCTION,  new Double(0.5));
//					brd.setNoseLength(speedSettings.getDouble(MachineConfig.CUTTING_SPEED_NOSE_REDUCTION_DIST));
//					brd.setTailLength(speedSettings.getDouble(MachineConfig.CUTTING_SPEED_TAIL_REDUCTION_DIST));
				}
			}
		};

		gcodeMenu.add(gcodeBezier);

		gcodeMenu.addSeparator();

		final AbstractAction gcodeOutline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEOUTLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HOTWIRECATEGORY_STR");
				Settings hotwireSettings = settings.addCategory(categoryName);
				hotwireSettings.addMeasurement("CuttingSpeed", 50.0, LanguageResource.getString("HOTWIRECUTTINGSPEED_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HOTWIREPARAMETERSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				settingsDialog.dispose();
				if (settingsDialog.wasCancelled()) {
					return;
				}

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

				filename = FileTools.setExtension(filename, "nc");

				BoardCAD.defaultDirectory = file.getPath();

				HotwireToolpathGenerator2 toolpathGenerator = new HotwireToolpathGenerator2(
						new AbstractCutter() {
							public double[] calcOffset(Point3d point, Vector3d normal,
									AbstractBoard board) {
								return new double[] { point.x, point.y, point.z };
							}

						}, new GCodeWriter(), hotwireSettings.getMeasurement("CuttingSpeed")*UnitUtils.MILLIMETER_PR_CENTIMETER);

				try {
					toolpathGenerator.writeOutline(filename, getCurrentBrd());
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEOUTLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("GCODEOUTLINEFAILEDTITLE_STR") ,
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeOutline);

		final AbstractAction gcodeProfile = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEPROFILE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HOTWIRECATEGORY_STR");
				final Settings hotwireSettings = settings.addCategory(categoryName);
				hotwireSettings.addMeasurement("CuttingSpeed", 50.0, LanguageResource.getString("HOTWIRECUTTINGSPEED_STR"));
				hotwireSettings.addMeasurement("AdditionalThickness", 0.0, LanguageResource.getString("ADDITIONALTHICKNESS_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HOTWIREPARAMETERSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				settingsDialog.dispose();
				if (settingsDialog.wasCancelled()) {
					return;
				}

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

				filename = FileTools.setExtension(filename, "nc");

				BoardCAD.defaultDirectory = file.getPath();
				
				HotwireToolpathGenerator2 toolpathGenerator = new HotwireToolpathGenerator2(
						new AbstractCutter() {
							public double[] calcOffset(Point3d point, Vector3d normal,
									AbstractBoard board) {

//								double additionalThickness = hotwireSettings.getMeasurement("AdditionalThickness")*UnitUtils.MILLIMETER_PR_CENTIMETER;
								
								Point3d offsetPoint = new Point3d(point);
//								Vector3d normalScaled = new Vector3d(normal);
//								normalScaled.scale(additionalThickness);
//								offsetPoint.add(normalScaled);
								
								return new double[] { offsetPoint.x, offsetPoint.y, offsetPoint.z };
							}

						}, new GCodeWriter(), hotwireSettings.getMeasurement("CuttingSpeed")*UnitUtils.MILLIMETER_PR_CENTIMETER, hotwireSettings.getMeasurement("AdditionalThickness")*UnitUtils.MILLIMETER_PR_CENTIMETER);

				try {
					toolpathGenerator.writeProfile(filename, getCurrentBrd());
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEPROFILEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("GCODEPROFILEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeProfile);

		final AbstractAction gcodeDeck = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEDECK_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				MachineConfig config = new MachineConfig();
				config.getPreferences();
				
				AbstractToolpathGenerator toolpathGenerator = new WidthSplitsToolpathGenerator(
						new AbstractCutter() {
							public double[] calcOffset(Point3d point, Vector3d normal,
									AbstractBoard board) {
								return new double[] { point.x, point.y, point.z };
							}

							public double calcSpeed(Point3d point, Vector3d normal,
									AbstractBoard board, boolean isCuttingStringer) {
								return 10;
							}
						
						}, null, new GCodeWriter(), config);

				try {
					toolpathGenerator.writeToolpath(filename, getCurrentBrd(), null);
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEDECKFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("GCODEDECKFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeDeck);

		final AbstractAction gcodeBottom = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEBOTTOM_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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
				
				MachineConfig config = new MachineConfig();
				config.getPreferences();

				AbstractToolpathGenerator toolpathGenerator = new WidthSplitsToolpathGenerator(
						new AbstractCutter() {
							public double[] calcOffset(Point3d point, Vector3d normal,
									AbstractBoard board) {
								return new double[] { point.x, point.y, point.z };
							}

							public double calcSpeed(Point3d point, Vector3d normal,
									AbstractBoard board, boolean isCuttingStringer) {
								return 10;
							}

						}, null, new GCodeWriter(), config);

				try {
					toolpathGenerator.writeToolpath(filename, getCurrentBrd(), null);
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEBOTTOMFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("GCODEBOTTOMFAILEDMSG_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeBottom);

		fileMenu.add(gcodeMenu);

		final JMenu extensionsMenu = new JMenu(LanguageResource.getString("EXTENSIONSMENU_STR"));
		final JMenu atuaCoresMenu = new JMenu(LanguageResource.getString("ATUACORESMENU_STR"));
		final AbstractAction atuaCoresProfile = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ATUACORESPROFILE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				
				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("ATUAPARAMETERSCATEGORY_STR");
				Settings atuaSettings = settings.addCategory(categoryName);
				atuaSettings.addBoolean("NoRotation", false, LanguageResource.getString("ATUANOROTATION_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("ATUAPARAMETERSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				settingsDialog.dispose();
				if (settingsDialog.wasCancelled()) {
					return;
				}
				
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

				filename = FileTools.setExtension(filename, "atua");

				BoardCAD.defaultDirectory = file.getPath();

				AtuaCoresToolpathGenerator toolpathGenerator = new AtuaCoresToolpathGenerator();
				try {
					toolpathGenerator.writeProfile(filename, getCurrentBrd(), atuaSettings.getBoolean("NoRotation"));
				} catch (Exception e) {
					String str = LanguageResource.getString("ATUACORESPROFILEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("ATUACORESPROFILEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		atuaCoresMenu.add(atuaCoresProfile);

		final AbstractAction atuaCoresOutline = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ATUACORESOUTLINE_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
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

				filename = FileTools.setExtension(filename, "atua");

				BoardCAD.defaultDirectory = file.getPath();

				AtuaCoresToolpathGenerator toolpathGenerator = new AtuaCoresToolpathGenerator();
				try {
					toolpathGenerator.writeOutline(filename, getCurrentBrd());
				} catch (Exception e) {
					String str = LanguageResource.getString("ATUACORESOUTLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), str, LanguageResource.getString("ATUACORESOUTLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		atuaCoresMenu.add(atuaCoresOutline);

		extensionsMenu.add(atuaCoresMenu);

		fileMenu.add(extensionsMenu);

		fileMenu.addSeparator();


		final AbstractAction loadscript = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Load script");
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {

				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showOpenDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;
				
//				ScriptLoader sl=new ScriptLoader();
//				sl.loadScript(filename);
				
				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		fileMenu.add(loadscript);

		fileMenu.addSeparator();




		final AbstractAction test = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Test");
			};

			public void actionPerformed(ActionEvent arg0)
			{
/*				CategorizedSettings settings = new CategorizedSettings();
				settings.addCategory("test");
				settings.getSettings("test").addBoolean("Test1", true, "Test 1");

				SettingDialog dialog = new SettingDialog(settings);
				dialog.setModal(true);
				dialog.setVisible(true);
				if(!dialog.wasCancelled())
				{
					boolean test1 = settings.getSettings("test").getBoolean("Test1");
					System.out.printf("Test1: %s", test1?"true":"false");
				}

				BezierSpline b = getCurrentBrd().getNearestCrossSection(getCurrentBrd().getLength()/2.0f).getBezierSpline();
				double startAngle = b.getNormalByS(BezierSpline.ZERO);
				double endAngle = b.getNormalByS(BezierSpline.ONE);

				System.out.printf("startAngle: %f endAngle: %f\n", startAngle/BezierBoard.DEG_TO_RAD,  endAngle/BezierBoard.DEG_TO_RAD);

				int steps = 20;

				System.out.printf("----------------------------------------\n");
				System.out.printf("----------------------------------------\n");
				System.out.printf("---------------TEST BEGIN---------------\n");
				System.out.printf("----------------------------------------\n");

				for(int i = 0; i < steps; i++)
				{
					double currentAngle = b.getNormalByS((double)i/(double)steps);
					System.out.printf("Angle:%f\n",  currentAngle/BezierBoard.DEG_TO_RAD);
				}

				System.out.printf("----------------------------------------\n");
				System.out.printf("----------------------------------------\n");

				double angleStep = (endAngle-startAngle) / steps;

				for(int i = 0; i < steps; i++)
				{
					System.out.printf("----------------------------------------\n");
					double currentAngle = startAngle + (angleStep*i);
					double s = b.getSByNormalReverse(currentAngle);
					double checkAngle = b.getNormalByS(s);
					System.out.printf("Target Angle:%f Result s:%f Angle for s:%f\n", currentAngle/BezierBoard.DEG_TO_RAD, s, checkAngle/BezierBoard.DEG_TO_RAD);
				}
*/

				System.out.printf("__________________________________\n");
				//Test SimpleBullnoseCutter
				SimpleBullnoseCutter cutter = new SimpleBullnoseCutter(50, 10, 100);
				System.out.printf("TEST!!! Cutter diam: 50 corner: 10 height: 100\n");

				Point3d point = new Point3d(0.0,0.0,0.0);

				Vector<Vector3d> testVectors = new Vector<Vector3d>();

				testVectors.add(new Vector3d(1.0,1.0,1.0));
//				testVectors.add(new Vector3d(1.0,0.0,1.0));
//				testVectors.add(new Vector3d(-1.0,0.0,1.0));
//				testVectors.add(new Vector3d(0.0,1.0,1.0));
//				testVectors.add(new Vector3d(0.0,-1.0,1.0));
//				testVectors.add(new Vector3d(0.0,-1.0,0.0));
//				testVectors.add(new Vector3d(1.0,0.0,1.0));

				System.out.printf("\n__________________________________\n");
				for(int i = 0; i < testVectors.size(); i++)
				{
					Vector3d vector = testVectors.elementAt(i);
					vector.normalize();
					System.out.printf("\nTEST!!! Vector%d: %f,%f,%f\n", i, vector.x, vector.y, vector.z);
					double[] result = cutter.calcOffset(point, vector, null);
					System.out.printf("Result: %f, %f, %f\n", result[0], result[1], result[2]);
				}
				System.out.printf("\n__________________________________\n");

			}
		};
//		fileMenu.add(test);

		final AbstractAction exit = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXIT_STR"));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				mFrame.dispose();
//				mFrame.setVisible(false);
			}

		};
		fileMenu.add(exit);

		menuBar.add(fileMenu);

		final JMenu editMenu = new JMenu(LanguageResource.getString("EDITMENU_STR"));
		editMenu.setMnemonic(KeyEvent.VK_E);
		final AbstractAction undo = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("UNDO_STR"));
				this.putValue(Action.SHORT_DESCRIPTION, LanguageResource.getString("UNDO_STR"));
				this.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/edit-undo.png")));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {
				BrdCommandHistory.getInstance().undo();
				mFrame.repaint();
			}

		};
		editMenu.add(undo);

		final AbstractAction redo = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("REDO_STR"));
				this.putValue(Action.SHORT_DESCRIPTION, LanguageResource.getString("REDO_STR"));
				this.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/edit-redo.png")));

				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {
				BrdCommandHistory.getInstance().redo();
				mFrame.repaint();
			}

		};
		editMenu.add(redo);

		menuBar.add(editMenu);

		final JMenu viewMenu = new JMenu( LanguageResource.getString("VIEWMENU_STR"));
		viewMenu.setMnemonic(KeyEvent.VK_V);

		mIsPaintingGridMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWGRID_STR"));
		mIsPaintingGridMenuItem.setMnemonic(KeyEvent.VK_R);
		mIsPaintingGridMenuItem.setSelected(true);
		mIsPaintingGridMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingGridMenuItem);

		mIsPaintingGhostBrdMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWGHOSTBOARD_STR"));
		mIsPaintingGhostBrdMenuItem.setMnemonic(KeyEvent.VK_G);
		mIsPaintingGhostBrdMenuItem.setSelected(true);
		mIsPaintingGhostBrdMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingGhostBrdMenuItem);

		mIsPaintingOriginalBrdMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWORIGINALBOARD_STR"));
		mIsPaintingOriginalBrdMenuItem.setMnemonic(KeyEvent.VK_O);
		mIsPaintingOriginalBrdMenuItem.setSelected(true);
		mIsPaintingOriginalBrdMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingOriginalBrdMenuItem);

		mIsPaintingControlPointsMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWCONTROLPOINTS_STR"));
		mIsPaintingControlPointsMenuItem.setMnemonic(KeyEvent.VK_C);
		mIsPaintingControlPointsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));// , KeyEvent.CTRL_DOWN_MASK));
		mIsPaintingControlPointsMenuItem.setSelected(true);
		mIsPaintingControlPointsMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingControlPointsMenuItem);

		mIsPaintingNonActiveCrossSectionsMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWNONEACTIVECROSSECTIONS_STR"));
		mIsPaintingNonActiveCrossSectionsMenuItem.setMnemonic(KeyEvent.VK_N);
		mIsPaintingNonActiveCrossSectionsMenuItem.setSelected(true);
		mIsPaintingNonActiveCrossSectionsMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingNonActiveCrossSectionsMenuItem);

		mIsPaintingGuidePointsMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWGUIDEPOINTS_STR"));
		mIsPaintingGuidePointsMenuItem.setMnemonic(KeyEvent.VK_P);
		mIsPaintingGuidePointsMenuItem.setSelected(true);
		mIsPaintingGuidePointsMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingGuidePointsMenuItem);

		mIsPaintingCurvatureMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWCURVATURE_STR"));
		mIsPaintingCurvatureMenuItem.setMnemonic(KeyEvent.VK_V);
		mIsPaintingCurvatureMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));// , KeyEvent.CTRL_DOWN_MASK));
		mIsPaintingCurvatureMenuItem.setSelected(true);
		mIsPaintingCurvatureMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingCurvatureMenuItem);

		mIsPaintingVolumeDistributionMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWVOLUMEDISTRIBUTION_STR"));
		mIsPaintingVolumeDistributionMenuItem.setMnemonic(KeyEvent.VK_V);
		mIsPaintingVolumeDistributionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));// , KeyEvent.CTRL_DOWN_MASK));
		mIsPaintingVolumeDistributionMenuItem.setSelected(true);
		mIsPaintingVolumeDistributionMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingVolumeDistributionMenuItem);

		mIsPaintingCenterOfMassMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWCENTEROFMASS_STR"));
		mIsPaintingCenterOfMassMenuItem.setMnemonic(KeyEvent.VK_M);
		mIsPaintingCenterOfMassMenuItem.setSelected(true);
		mIsPaintingCenterOfMassMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingCenterOfMassMenuItem);

		mIsPaintingSlidingInfoMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWSLIDINGINFO_STR"));
		mIsPaintingSlidingInfoMenuItem.setMnemonic(KeyEvent.VK_S);
		mIsPaintingSlidingInfoMenuItem.setSelected(true);
		mIsPaintingSlidingInfoMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingSlidingInfoMenuItem);

		mIsPaintingSlidingCrossSectionMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWSLIDINGCROSSECTION_STR"));
		mIsPaintingSlidingCrossSectionMenuItem.setMnemonic(KeyEvent.VK_X);
		mIsPaintingSlidingCrossSectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));// , KeyEvent.CTRL_DOWN_MASK));
		mIsPaintingSlidingCrossSectionMenuItem.setSelected(true);
		mIsPaintingSlidingCrossSectionMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingSlidingCrossSectionMenuItem);

		mIsPaintingFinsMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWFINS_STR"));
		mIsPaintingFinsMenuItem.setMnemonic(KeyEvent.VK_F);
		mIsPaintingFinsMenuItem.setSelected(true);
		mIsPaintingFinsMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingFinsMenuItem);

		mIsPaintingBackgroundImageMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWBACKGROUNDIMAGE_STR"));
		mIsPaintingBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_B);
		mIsPaintingBackgroundImageMenuItem.setSelected(true);
		mIsPaintingBackgroundImageMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingBackgroundImageMenuItem);

		mIsAntialiasingMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("USEANTIALIASING_STR"));
		mIsAntialiasingMenuItem.setMnemonic(KeyEvent.VK_A);
		mIsAntialiasingMenuItem.setSelected(true);
		mIsAntialiasingMenuItem.addItemListener(this);
		viewMenu.add(mIsAntialiasingMenuItem);

		mIsPaintingBaseLineMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWBASELINE_STR"));
		mIsPaintingBaseLineMenuItem.setMnemonic(KeyEvent.VK_L);
		mIsPaintingBaseLineMenuItem.setSelected(true);
		mIsPaintingBaseLineMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingBaseLineMenuItem);

		mIsPaintingCenterLineMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWCENTERLINE_STR"));
		mIsPaintingCenterLineMenuItem.setMnemonic(KeyEvent.VK_J);
		mIsPaintingCenterLineMenuItem.setSelected(true);
		mIsPaintingCenterLineMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingCenterLineMenuItem);

		mIsPaintingOverCurveMesurementsMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWOVERBOTTOMCURVEMEASUREMENTS_STR"));
		mIsPaintingOverCurveMesurementsMenuItem.setMnemonic(KeyEvent.VK_D);
		mIsPaintingOverCurveMesurementsMenuItem.setAccelerator( KeyStroke.getKeyStroke("shift V") );
		mIsPaintingOverCurveMesurementsMenuItem.setSelected(true);
		mIsPaintingOverCurveMesurementsMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingOverCurveMesurementsMenuItem);

		mIsPaintingMomentOfInertiaMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWMOMENTOFINERTIA_STR"));
		mIsPaintingMomentOfInertiaMenuItem.setMnemonic(KeyEvent.VK_D);
//		mIsPaintingMomentOfInertiaMenuItem.setAccelerator( KeyStroke.getKeyStroke("shift V") );
		mIsPaintingMomentOfInertiaMenuItem.setSelected(true);
		mIsPaintingMomentOfInertiaMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingMomentOfInertiaMenuItem);
		
		mIsPaintingCrossectionsPositionsMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWCROSSECTIONSPOSITIONS_STR"));
		mIsPaintingCrossectionsPositionsMenuItem.setMnemonic(KeyEvent.VK_D);
//		mIsPaintingCrossectionsPositionsMenuItem.setAccelerator( KeyStroke.getKeyStroke("shift V") );
		mIsPaintingCrossectionsPositionsMenuItem.setSelected(true);
		mIsPaintingCrossectionsPositionsMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingCrossectionsPositionsMenuItem);

		mIsPaintingFlowlinesMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWFLOWLINES_STR"));
		mIsPaintingFlowlinesMenuItem.setMnemonic(KeyEvent.VK_D);
//		mIsPaintingFlowlinesMenuItem.setAccelerator( KeyStroke.getKeyStroke("shift V") );
		mIsPaintingFlowlinesMenuItem.setSelected(true);
		mIsPaintingFlowlinesMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingFlowlinesMenuItem);

		mIsPaintingApexlineMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWAPEXLINE_STR"));
		mIsPaintingApexlineMenuItem.setMnemonic(KeyEvent.VK_D);
//		mIsPaintingApexlineMenuItem.setAccelerator( KeyStroke.getKeyStroke("shift V") );
		mIsPaintingApexlineMenuItem.setSelected(true);
		mIsPaintingApexlineMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingApexlineMenuItem);

		mIsPaintingTuckUnderLineMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWTUCKUNDERLINE_STR"));
		mIsPaintingTuckUnderLineMenuItem.setMnemonic(KeyEvent.VK_D);
//		mIsPaintingTuckUnderLineMenuItem.setAccelerator( KeyStroke.getKeyStroke("shift V") );
		mIsPaintingTuckUnderLineMenuItem.setSelected(true);
		mIsPaintingTuckUnderLineMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingTuckUnderLineMenuItem);

		mIsPaintingFootMarksMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWFOOTMARKS_STR"));
		mIsPaintingFootMarksMenuItem.setMnemonic(KeyEvent.VK_D);
		mIsPaintingFootMarksMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift F") );
		mIsPaintingFootMarksMenuItem.setSelected(false);
		mIsPaintingFootMarksMenuItem.addItemListener(this);
		viewMenu.add(mIsPaintingFootMarksMenuItem);

		mUseFillMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("USEFILL_STR"));
		//mUseFillMenuItem.setMnemonic(KeyEvent.VK_D);
		//mUseFillMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift F") );
		mUseFillMenuItem.setSelected(true);
		mUseFillMenuItem.addItemListener(this);
		viewMenu.add(mUseFillMenuItem);
	
		menuBar.add(viewMenu);

		final JMenu crossSectionsMenu = new JMenu(LanguageResource.getString("CROSSECTIONSMENU_STR"));
		crossSectionsMenu.setMnemonic(KeyEvent.VK_C);

		mNextCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("NEXTCROSSECTION_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				if (isGhostMode()) {
					BoardCAD.getInstance().getGhostBrd().nextCrossSection();
				} else if (mOrgFocus) {
					BoardCAD.getInstance().getOriginalBrd().nextCrossSection();
				} else {
					BoardCAD.getInstance().getCurrentBrd().nextCrossSection();
				}
				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(mNextCrossSection);

		mPreviousCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PREVIOUSCROSSECTION_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_MINUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				if (isGhostMode()) {
					BoardCAD.getInstance().getGhostBrd().previousCrossSection();
				} else if (mOrgFocus) {
					BoardCAD.getInstance().getOriginalBrd()
					.previousCrossSection();
				} else {
					BoardCAD.getInstance().getCurrentBrd()
					.previousCrossSection();
				}
				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(mPreviousCrossSection);
		crossSectionsMenu.addSeparator();

		final AbstractAction addCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ADDCROSSECTION_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				setSelectedEdit(mCrossSectionEdit);

				double pos = 0.0f;
				String posStr = JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("ADDCROSSECTIONMSG_STR"),
						LanguageResource.getString("ADDCROSSECTIONTITLE_STR"), JOptionPane.PLAIN_MESSAGE);

				if (posStr == null)
					return;

				pos = UnitUtils.convertInputStringToInternalLengthUnit(posStr);
				if (pos <= 0 || pos > getCurrentBrd().getLength()) {
					JOptionPane
					.showMessageDialog(
							getFrame(),
							LanguageResource.getString("ADDCROSSECTIONPOSITIONINVALIDMSG_STR"),
							LanguageResource.getString("ADDCROSSECTIONPOSITIONINVALIDTITLE_STR"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				BrdAddCrossSectionCommand cmd = new BrdAddCrossSectionCommand(
						mCrossSectionEdit, pos);
				cmd.execute();

				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(addCrossSection);

		final AbstractAction moveCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("MOVECROSSECTION_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				setSelectedEdit(mCrossSectionEdit);

				double pos = 0.0f;
				String posStr = JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("MOVECROSSECTIONMSG_STR"),
						LanguageResource.getString("MOVECROSSECTIONTITLE_STR"), JOptionPane.PLAIN_MESSAGE);

				if (posStr == null)
					return;

				pos = UnitUtils.convertInputStringToInternalLengthUnit(posStr);
				if (pos <= 0 || pos > getCurrentBrd().getLength()) {
					JOptionPane
					.showMessageDialog(
							getFrame(),
							LanguageResource.getString("MOVECROSSECTIONPOSITIONINVALIDMSG_STR"),
							LanguageResource.getString("MOVECROSSECTIONPOSITIONINVALIDTITLE_STR"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				BrdMoveCrossSectionCommand cmd = new BrdMoveCrossSectionCommand(
						mCrossSectionEdit, mCrossSectionEdit.getCurrentBrd()
						.getCurrentCrossSection(), pos);
				cmd.execute();

				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(moveCrossSection);

		final AbstractAction deleteCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("REMOVECROSSECTION_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				setSelectedEdit(mCrossSectionEdit);

				if (mCrossSectionEdit.getCurrentBrd().getCrossSections().size() <= 3) {
					JOptionPane.showMessageDialog(getFrame(),
							LanguageResource.getString("REMOVECROSSECTIONDELETELASTERRORMSG_STR"),
							LanguageResource.getString("REMOVECROSSECTIONDELETELASTERRORTITLE_STR"), JOptionPane.WARNING_MESSAGE);

					return;
				}

				BrdRemoveCrossSectionCommand cmd = new BrdRemoveCrossSectionCommand(
						mCrossSectionEdit, mCrossSectionEdit.getCurrentBrd()
						.getCurrentCrossSection());
				cmd.execute();

				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(deleteCrossSection);
		crossSectionsMenu.addSeparator();

		final AbstractAction copyCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("COPYCROSSECTION_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				mCrossSectionCopy = (BezierBoardCrossSection) getCurrentBrd()
				.getCurrentCrossSection().clone();

				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(copyCrossSection);

		final AbstractAction pasteCrossSection = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PASTECROSSECTION_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				if (mCrossSectionCopy == null)
					return;

				setSelectedEdit(mCrossSectionEdit);

				BrdPasteCrossSectionCommand cmd = new BrdPasteCrossSectionCommand(
						mCrossSectionEdit, getCurrentBrd()
						.getCurrentCrossSection(), mCrossSectionCopy);
				cmd.execute();

				mFrame.repaint();
			}

		};
		crossSectionsMenu.add(pasteCrossSection);

		menuBar.add(crossSectionsMenu);

		final JMenu boardMenu = new JMenu(LanguageResource.getString("BOARDMENU_STR"));
		boardMenu.setMnemonic(KeyEvent.VK_B);

		final AbstractAction scale = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALECURRENT_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {

				BrdScaleCommand cmd = new BrdScaleCommand(getSelectedEdit());
				cmd.execute();

				mFrame.repaint();
			}

		};
		boardMenu.add(scale);

		final AbstractAction scaleGhost = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALEGHOST_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				getGhostBrd().scale(getCurrentBrd().getLength(),
						getCurrentBrd().getCenterWidth(), getCurrentBrd().getThickness());

				mFrame.repaint();
			}

		};
		boardMenu.add(scaleGhost);


		final AbstractAction info = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("INFO_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {

				BoardInfo dialog = new BoardInfo(getCurrentBrd());
				dialog.setModal(true);
				//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.setVisible(true);
				dialog.dispose();
				mFrame.repaint();
			}

		};
		boardMenu.addSeparator();
		boardMenu.add(info);

		final AbstractAction fins = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME,  LanguageResource.getString("FINS_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {

				BoardFinsDialog dialog = new BoardFinsDialog(getCurrentBrd());
				dialog.setModal(true);
				//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.setVisible(true);
				dialog.dispose();
				mFrame.repaint();
			}

		};
		boardMenu.add(fins);

		final AbstractAction guidePoints = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GUIDEPOINTS_STR"));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				mGuidePointsDialog.setVisible(true);
				mFrame.repaint();
			}

		};
		boardMenu.addSeparator();
		boardMenu.add(guidePoints);

		final AbstractAction weightCalc = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("WEIGHTCALC_STR"));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				mWeightCalculatorDialog.setDefaults();
				mWeightCalculatorDialog.updateAll();
				mWeightCalculatorDialog.setVisible(true);
			}

		};
		boardMenu.addSeparator();
		boardMenu.add(weightCalc);

		final AbstractAction flip = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("FLIP_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {

				mOutlineEdit.setFlipped(!mOutlineEdit.isFlipped());
				if(mOutlineEdit2 != null)
					mOutlineEdit2.setFlipped(!mOutlineEdit2.isFlipped());
				mBottomAndDeckEdit.setFlipped(!mBottomAndDeckEdit.isFlipped());
				mCrossSectionOutlineEdit.setFlipped(!mCrossSectionOutlineEdit.isFlipped());

				view1.setFlipped(!view1.isFlipped());
				view3.setFlipped(!view2.isFlipped());

				fitAll();

				mFrame.repaint();
			}

		};
		boardMenu.addSeparator();
		boardMenu.add(flip);

		menuBar.add(boardMenu);

		final JMenu miscMenu = new JMenu(LanguageResource.getString("MISCMENU_STR"));
		miscMenu.setMnemonic(KeyEvent.VK_M);

		final AbstractAction settings = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SETTINGS_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				BoardCADSettingsDialog dlg = new BoardCADSettingsDialog(
						mSettings);
				dlg.setModal(true);
				dlg.setVisible(true);

				mFrame.repaint();
			}

		};

		miscMenu.add(settings);

		final AbstractAction language = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("LANGUAGE_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				ComboBoxDialog languageDlg = new ComboBoxDialog(mFrame);
				languageDlg.setTitle(LanguageResource.getString("LANGUAGE_STR"));
				languageDlg.setMessageText(LanguageResource.getString("SELECTLANGUAGE_STR"));

				String[] languages = new String[mSupportedLanguages.length];
				for(int i = 0; i < mSupportedLanguages.length; i++)
				{
					languages[i] = mSupportedLanguages[i].getDisplayName();
				}

				languageDlg.setItems(languages);

				final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);
				String languageStr = prefs.get("Language", "en");
				String selectedLanguage = "English";
				int i;
				for(i = 0; i < mSupportedLanguages.length; i++)
				{
					if(mSupportedLanguages[i].getLanguage().equals(languageStr))
					{
						selectedLanguage = mSupportedLanguages[i].getDisplayName();
						break;
					}
				}
				languageDlg.setSelectedItem(selectedLanguage);

				languageDlg.setModal(true);

				languageDlg.setVisible(true);
				if (!languageDlg.wasCancelled()) {
					selectedLanguage = languageDlg.getSelectedItem();

					for(i = 0; i < mSupportedLanguages.length; i++)
					{
						if(mSupportedLanguages[i].getDisplayName().equals(selectedLanguage))
						{
							languageStr = mSupportedLanguages[i].getLanguage();
							break;
						}
					}


					prefs.put("Language", languageStr );

					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), LanguageResource.getString("LANGUAGECHANGEDMSG_STR"),
							LanguageResource.getString("LANGUAGECHANGEDTITLE_STR"), JOptionPane.INFORMATION_MESSAGE);
				}

				languageDlg.dispose();
				mFrame.repaint();
			}

		};
		miscMenu.add(language);

		miscMenu.addSeparator();


		final JMenu crossSectionInterpolationMenu = new JMenu(
				LanguageResource.getString("CROSSECTIONINTERPOLATIONMENU_STR"));
		mControlPointInterpolationButton = new JRadioButtonMenuItem(
				LanguageResource.getString("CROSSECTIONINTERPOLATIONTYPECONTROLPOINT_STR"));
		mSBlendInterpolationButton = new JRadioButtonMenuItem(
				LanguageResource.getString("CROSSECTIONINTERPOLATIONTYPESBLEND_STR"));

		ActionListener interpolationTypeListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
			    if(selected && mTabbedPane.getSelectedComponent() == mRenderedpanel)
			    {
					updateBezier3DModel();
			    }
			    else
			    {
			    	mBoardChangedFor3D = true;
			    }
				if(mCurrentBrd != null)
				{
					mCurrentBrd.setInterpolationType(getCrossSectionInterpolationType());
				}
			}
		};

		mControlPointInterpolationButton.addActionListener(interpolationTypeListener);
		mSBlendInterpolationButton.addActionListener(interpolationTypeListener);


		final ButtonGroup interpolationButtonGroup = new ButtonGroup();
		interpolationButtonGroup.add(mControlPointInterpolationButton);
		interpolationButtonGroup.add(mSBlendInterpolationButton);

		crossSectionInterpolationMenu.add(mControlPointInterpolationButton);
		crossSectionInterpolationMenu.add(mSBlendInterpolationButton);

		miscMenu.add(crossSectionInterpolationMenu);


		menuBar.add(miscMenu);



		final JMenu scanMenu = new JMenu("Scan");
		scanMenu.setMnemonic(KeyEvent.VK_S);


		final AbstractAction scanBoard = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Read scanned board");
			};

			public void actionPerformed(ActionEvent arg0)
			{
				Scan myScan=new Scan();
				myScan.readScan();
				mFrame.repaint();
			}

		};
		scanMenu.add(scanBoard);

		scanMenu.addSeparator();

		final AbstractAction manual_scan = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Fit curve to guide points");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				Scan myScan=new Scan();
				myScan.approximateCurrentView();
				mFrame.repaint();
			}

		};
		scanMenu.add(manual_scan);

		scanMenu.addSeparator();


		final AbstractAction read_scanned_points = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Open scanned blank position");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				board_handler.read_guide_points();
				design_panel.fit_all();
				redraw();
			}

		};
		scanMenu.add(read_scanned_points);

		final AbstractAction close_scanned_points = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Hide scanned blank position");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				board_handler.close_guide_points();
				design_panel.fit_all();
				redraw();
			}

		};
		scanMenu.add(close_scanned_points);

		menuBar.add(scanMenu);



		final JMenu menu3D = new JMenu(LanguageResource.getString("3DMODELMENU_STR"));
		menu3D.setMnemonic(KeyEvent.VK_D);

		final AbstractAction approximate = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("3DAPPROXFROMBEZIER_STR") + " (closed model)");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				board_handler.approximate_bezier(getCurrentBrd(), true);
//				design_panel.update_3d();
//				design_panel.fit_all();
				mBoardSpec.updateInfoInstantly();
				panel.remove(mControlPointInfo);
				panel.repaint();
//				redraw();
//				design_panel.redraw();
			}

		};
		
		final AbstractAction approximate_open = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("3DAPPROXFROMBEZIER_STR") + " (open model)");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				board_handler.approximate_bezier(getCurrentBrd(), false);
//				design_panel.update_3d();
//				design_panel.fit_all();
				mBoardSpec.updateInfoInstantly();
				panel.remove(mControlPointInfo);
				panel.repaint();
//				redraw();
//				design_panel.redraw();
			}

		};


		final AbstractAction approximate2 = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Approximate outline and rocker");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				board_handler.approximate_bezier2(getCurrentBrd(), false);
//				design_panel.update_3d();
//				design_panel.fit_all();
				mBoardSpec.updateInfoInstantly();
				panel.remove(mControlPointInfo);
				panel.repaint();
//				redraw();
//				design_panel.redraw();
			}

		};

		final AbstractAction bezier_patch = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Create 3D model using Bezier patches");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {

	
				if(board_handler.approximate_bezier_patch(getCurrentBrd(), false)<0)
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), "All cross sections must have 4 or 5 control points", "Couldn't create bezier patch", JOptionPane.ERROR_MESSAGE);

				
//				design_panel.update_3d();
//				design_panel.fit_all();
				mBoardSpec.updateInfoInstantly();
				panel.remove(mControlPointInfo);
				panel.repaint();
//				redraw();
//				design_panel.redraw();
			}

		};
//PW Mods here
		final AbstractAction bezier_patch_num_bot_pts = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "   Set Number of Control Points on Bottom Surface between Stringer and Tuck Point");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {

			Object[] possibilities = {0,1,2,3,4};
			int s = (int)JOptionPane.showInputDialog(
                    mFrame,
                    "How many additional Control Points are between the\n"
					+ "Centre and Tuck on the Bottom of the Cross Sections?\n"
					+ "NOTE: All Cross Sections must have the same number of control points.",
                    "Number of Bottom Control Points",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities,
                    0);
			//board_handler.set_nr_of_bottom_ctrl_pts(s);
			mCurrentBrd.set_nr_of_bottom_ctrl_pts(s);

//JUNK?
//BoardCAD.getInstance().getFrame()

			}

		};
		
		final AbstractAction bezier_patch_channel_curved = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "   Set Channel Interpolation Behavior");
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {

				Object[] possibilities = {"Linear", "Curved"};
				String s = (String)JOptionPane.showInputDialog(
						mFrame,
						"How should the channels be interpolated?\n"
						+ "Linearly (default) = straight lines between corresponding cross section control points\n"
						+ "Curved = channels interpolation follows board width (e.g. Go Fish)",
						"Interpolation Algorithm",
						JOptionPane.PLAIN_MESSAGE,
						null,
						possibilities,
						"Linear");
				if (s == "Linear"){
					//board_handler.set_curved_channel_iterpolation(false);
					mCurrentBrd.set_curved_channel_iterpolation(false);
				} else {
					//board_handler.set_curved_channel_iterpolation(true);
					mCurrentBrd.set_curved_channel_iterpolation(true);
				}
			}

		};

		menu3D.add(bezier_patch);
		menu3D.add(bezier_patch_num_bot_pts);
		menu3D.add(bezier_patch_channel_curved);
		menu3D.addSeparator();
//PW Mods here		
		menu3D.add(approximate);
        menu3D.add(approximate_open);
        menu3D.add(approximate2);



		//3D object clear function
		final AbstractAction clearApproximate = new AbstractAction() {
			static final long serialVersionUID = 12345L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("CLEARAPPROXIMATION_STR"));
			};

			public void actionPerformed(ActionEvent arg0) {
				board_handler.clearBezier();
				mBoardSpec.updateInfoInstantly();
				panel.add(mControlPointInfo, BorderLayout.EAST);
				panel.repaint();
			}
		};
		menu3D.add(clearApproximate);

		menu3D.addSeparator();
		JRadioButtonMenuItem rbMenuItem;

/*		final ButtonGroup burbsViewGroup = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem(LanguageResource.getString("VIEW3D_STR"));
		rbMenuItem.setSelected(true);
		rbMenuItem.addActionListener(this);
		menu3D.add(rbMenuItem);
		burbsViewGroup.add(rbMenuItem);
		rbMenuItem = new JRadioButtonMenuItem(LanguageResource.getString("EDITNURBS_STR"));
		rbMenuItem.addActionListener(this);
		burbsViewGroup.add(rbMenuItem);
		menu3D.add(rbMenuItem);

		menu3D.addSeparator();
*/

		ButtonGroup bnurbsEditGroup=new ButtonGroup();
		rbMenuItem=new JRadioButtonMenuItem(LanguageResource.getString("SIMPLEEDITING_STR"));
		rbMenuItem.setSelected(true);
		rbMenuItem.addActionListener(this);
		menu3D.add(rbMenuItem);
		bnurbsEditGroup.add(rbMenuItem);
		rbMenuItem=new JRadioButtonMenuItem(LanguageResource.getString("ADVANCEDEDITING_STR"));
		rbMenuItem.addActionListener(this);
		bnurbsEditGroup.add(rbMenuItem);
		menu3D.add(rbMenuItem);



		menu3D.addSeparator();

		final AbstractAction addsegment = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ADDSEGMENT_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				String s = (String) JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("DISTANCEFROMTAIL_STR"));
				board_handler.add_segment(Double.parseDouble(s));

				design_panel.redraw();
			}

		};
		menu3D.add(addsegment);

		final AbstractAction taildesigner = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TAILDESIGNER_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				String s = (String) JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("SWALLOWTAIL_STR"));

				board_handler.set_tail(Double.parseDouble(s));
//				board_handler.set_tail(2);

				design_panel.redraw();
			}

		};
		menu3D.add(taildesigner);



		final AbstractAction setnrofsegments = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SETNROFSEGMENT_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				String s = (String) JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("NROFSEGMENT_STR"));

				board_handler.set_nr_of_segments(Integer.parseInt(s));
				board_handler.approximate_bezier(getCurrentBrd(), true);
				design_panel.update_3d();
				// design_panel.fit_all();
				design_panel.redraw();
			}

		};
		menu3D.add(setnrofsegments);

		final AbstractAction setnrofpoints = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SETNROFPOINTS_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				String s = (String) JOptionPane.showInputDialog(mFrame,
						LanguageResource.getString("NROFPOINTS_STR"));

				board_handler.set_nr_of_points(Integer.parseInt(s));
				board_handler.approximate_bezier(getCurrentBrd(), true);
				design_panel.update_3d();
				// design_panel.fit_all();
				design_panel.redraw();
			}

		};
		menu3D.add(setnrofpoints);


		menu3D.addSeparator();


		AbstractAction setasblank = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("SETASBLANK_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				board_handler.set_as_blank();
			}

		};
		menu3D.add(setasblank);



		final JMenu transform3DMenu = new JMenu("Transform");


		AbstractAction rotate = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("ROTATE_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("ROTATIONANGLE_STR"));
				NurbsEditCommand mnurbsCommand=new NurbsEditCommand();
				board_handler.rotate(Double.parseDouble(s));
				mnurbsCommand.execute();
				design_panel.redraw();
			}

		};
		transform3DMenu.add(rotate);


		AbstractAction translatex = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("TRANSLATEX_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("TRANLATIONINMM_STR"));

				NurbsEditCommand mnurbsCommand=new NurbsEditCommand();
				board_handler.translate(Double.parseDouble(s), 0.0, 0.0);
				mnurbsCommand.execute();
				design_panel.redraw();
			}

		};
		transform3DMenu.add(translatex);

		AbstractAction translatey = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("TRANSLATEY_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("TRANLATIONINMM_STR"));

				NurbsEditCommand mnurbsCommand=new NurbsEditCommand();
				board_handler.translate(0.0, Double.parseDouble(s), 0.0);
				mnurbsCommand.execute();
				design_panel.redraw();
			}

		};
		transform3DMenu.add(translatey);

		AbstractAction scale_length = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALELENGTH_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("SCALELENGTHFACTOR_STR"));

				board_handler.scale_length(Double.parseDouble(s));
				design_panel.redraw();
			}

		};
		transform3DMenu.add(scale_length);

		AbstractAction scale_width = new AbstractAction()
		{
			{
				this.putValue(Action.NAME,  LanguageResource.getString("SCALEWIDTH_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("SCALEWIDTHFACTOR_STR"));

				board_handler.scale_width(Double.parseDouble(s));
				design_panel.redraw();
			}

		};
		transform3DMenu.add(scale_width);

		AbstractAction scale_thickness = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALETHICKNESS_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("SCALETHICKNESSFACTOR_STR"));

				board_handler.scale_thickness(Double.parseDouble(s));
				design_panel.redraw();
			}

		};
		transform3DMenu.add(scale_thickness);

		AbstractAction scale_rocker = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALEROCKER_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{
				String s=(String)JOptionPane.showInputDialog(mFrame, LanguageResource.getString("SCALEROCKERFACTOR_STR"));

				board_handler.scale_rocker(Double.parseDouble(s));
				design_panel.redraw();
			}

		};
		transform3DMenu.add(scale_rocker);


		AbstractAction flipnurbs = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("FLIPBOARD_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{

				board_handler.flip();
				design_panel.redraw();
			}

		};
		transform3DMenu.add(flipnurbs);

		AbstractAction placeboard = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("PLACEBOARD_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{

				board_handler.place_board();
				design_panel.redraw();
			}

		};
		transform3DMenu.add(placeboard);

		AbstractAction placeblank = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("PLACEBLANK_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{

				board_handler.place_blank();
				design_panel.redraw();
			}

		};
		transform3DMenu.add(placeblank);

		menu3D.add(transform3DMenu);

/*

		AbstractAction repair = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, LanguageResource.getString("REPAIR_STR"));
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{

				board_handler.repair();
				design_panel.redraw();
			}

		};
		menu3D.add(repair);
*/
/*
		AbstractAction loadairbrush = new AbstractAction()
		{
			{
				this.putValue(Action.NAME, "Load airbrush");
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0)
			{

				design_panel2.set_airbrush("airbrush.jpg");
				design_panel2.redraw();
			}

		};
		menu3D.add(loadairbrush);
*/







		/*
		 * AbstractAction settolerance = new AbstractAction() { {
		 * this.putValue(Action.NAME, "Set tolerance"); //
		 * this.putValue(Action.ACCELERATOR_KEY,
		 * KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0)); };
		 *
		 * public void actionPerformed(ActionEvent arg0) { String
		 * s=(String)JOptionPane.showInputDialog(mFrame, "Set tolerance (mm)");
		 *
		 * board_handler.set_tolerance(Double.parseDouble(s)); }
		 *  }; menu3D.add(settolerance);
		 *
		 * AbstractAction setmaxiterations = new AbstractAction() { {
		 * this.putValue(Action.NAME, "Set max iterations"); //
		 * this.putValue(Action.ACCELERATOR_KEY,
		 * KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0)); };
		 *
		 * public void actionPerformed(ActionEvent arg0) { String
		 * s=(String)JOptionPane.showInputDialog(mFrame, "Max iterations");
		 *
		 * board_handler.set_iterations(Integer.parseInt(s)); }
		 *  }; menu3D.add(setmaxiterations);
		 */


		menuBar.add(menu3D);



		final JMenu menuRender = new JMenu(LanguageResource.getString("RENDERMENU_STR"));
		menuRender.setMnemonic(KeyEvent.VK_R);


		final AbstractAction update3d = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("RENDER_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				if(board_handler.is_empty()) {
					board_handler.approximate_bezier(getCurrentBrd(), false);
				}
				design_panel2.update_3d();
			}

		};
		menuRender.add(update3d);

		mShowRenderInwireframe = new JCheckBoxMenuItem( LanguageResource.getString("SHOWWIREFRAME_STR"));
		mShowRenderInwireframe.setMnemonic(KeyEvent.VK_S);
		mShowRenderInwireframe.setSelected(false);
		mShowRenderInwireframe.addItemListener(new ItemListener()
		{
			public void itemStateChanged(final ItemEvent e)
			{
				Appearance a = new Appearance();
				PolygonAttributes pa = new PolygonAttributes();
				if(mShowRenderInwireframe.isSelected())
				{
					Color3f ambient = new Color3f(0.1f, 0.5f, 0.1f);
					Color3f emissive = new Color3f(0.0f, 0.0f, 0.0f);
					Color3f diffuse = new Color3f(0.1f, 1.0f, 0.1f);
					Color3f specular = new Color3f(0.9f, 1.0f, 0.9f);

					// Set up the material properties
					a.setMaterial(new Material(ambient, emissive, diffuse, specular, 115.0f));

					pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
					pa.setCullFace(PolygonAttributes.CULL_BACK); // experiment with it
					a.setPolygonAttributes(pa);
				}
				else
				{
					Color3f ambient = new Color3f(0.4f, 0.4f, 0.45f);
					Color3f emissive = new Color3f(0.0f, 0.0f, 0.0f);
					Color3f diffuse = new Color3f(0.8f, 0.8f, 0.8f);
					Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);

					// Set up the material properties
					a.setMaterial(new Material(ambient, emissive, diffuse, specular, 115.0f));

					pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
					pa.setCullFace(PolygonAttributes.CULL_BACK); // experiment with it
					a.setPolygonAttributes(pa);
				}

				if(mBezier3DModel != null)
				{
					mBezier3DModel.setAppearance(a);
				}

				if(design_panel2.getShape() != null)
				{
					design_panel2.getShape().setAppearance(a);
				}

			}

		});
		menuRender.add(mShowRenderInwireframe);


		final AbstractAction loadairbrush = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, "Load airbrush");
//				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
			};

			public void actionPerformed(ActionEvent arg0) {

				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showOpenDialog(BoardCAD.getInstance()
						.getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				design_panel2.set_airbrush(filename);
				design_panel2.redraw();

/*
				BoardEdit edit = getSelectedEdit();
				if (edit == null)
					return;

				edit.loadBackgroundImage(filename);
				edit.repaint();
*/
			}

		};
		menuRender.add(loadairbrush);

		mShowBezier3DModelMenuItem = new JCheckBoxMenuItem( LanguageResource.getString("SHOWBEZIER3DMODEL_STR"));
		mShowBezier3DModelMenuItem.setMnemonic(KeyEvent.VK_B);
		mShowBezier3DModelMenuItem.setSelected(false);
		ActionListener showBezier3DListener = new ActionListener() {
			  public void actionPerformed(ActionEvent event) {
			    AbstractButton aButton = (AbstractButton) event.getSource();
			    boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
			    if(mBezier3DOnSwitch != null)
			    {
					mBezier3DOnSwitch.setWhichChild(selected?Switch.CHILD_ALL:Switch.CHILD_NONE);
				    if(selected && mBoardChangedFor3D && mTabbedPane.getSelectedComponent() == mRenderedpanel)
				    {
				    	updateBezier3DModel();
				    }
			    }
			  }
		};
		mShowBezier3DModelMenuItem.addActionListener(showBezier3DListener);
		menuRender.add(mShowBezier3DModelMenuItem);

		menuBar.add(menuRender);

		scriptMenu = new JMenu("Script");
		scriptMenu.setMnemonic(KeyEvent.VK_P);
		menuBar.add(scriptMenu);

		final JMenu helpMenu = new JMenu(LanguageResource.getString("HELPMENU_STR"));
		helpMenu.setMnemonic(KeyEvent.VK_H);

		final AbstractAction onlineHelp = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ONLINEHELP_STR"));
				this.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/Help16.gif")));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				BrowserControl
				.displayURL("http://boardcad.org/index.php/Help:Contents");
			}

		};
		helpMenu.add(onlineHelp);

		final AbstractAction about = new AbstractAction() {
			static final long serialVersionUID=1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ABOUT_STR"));
				this.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/Information16.gif")));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			public void actionPerformed(ActionEvent arg0) {
				AboutBox box = new AboutBox();
				box.setModal(true);
				box.setVisible(true);
				box.dispose();
			}

		};
		helpMenu.add(about);

		menuBar.add(helpMenu);

		mFrame.setJMenuBar(menuBar);

		mToolBar = new JToolBar();

		newBrd.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/new.png")));
		mToolBar.add(newBrd);

		loadBrd.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/open.png")));
		mToolBar.add(loadBrd);

		saveBrd.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/save.png")));
		mToolBar.add(saveBrd);

		SaveBrd.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/save-refresh.png")));
		mToolBar.add(SaveBrd);

		printSpecSheet.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/print.png")));
		mToolBar.add(printSpecSheet);

		mToolBar.addSeparator();
		mToolBar.addSeparator();

		final SetCurrentOneShotCommandAction zoom = new SetCurrentOneShotCommandAction(
				new BrdZoomCommand());
		zoom.putValue(AbstractAction.NAME, LanguageResource.getString("ZOOMBUTTON_STR"));
		zoom.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("ZOOMBUTTON_STR"));
		zoom.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/zoom-in.png")));
		mToolBar.add(zoom);


		final AbstractAction fit = new AbstractAction() {
			static final long serialVersionUID=1L;
			public void actionPerformed(ActionEvent event) {
				fitAll();
				mLifeSizeButton.getModel().setPressed(false);
				mTabbedPane.repaint();
			}

		};
		fit.putValue(AbstractAction.NAME, LanguageResource.getString("FITBUTTON_STR"));
		fit.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("FITBUTTON_STR"));
		fit.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/zoom-fit-best.png")));
		mToolBar.add(fit);
		popupMenu.add(fit);

		mLifeSizeButton = new JToggleButton();
		mLifeSizeButton.setIcon(new ImageIcon(getClass().getResource("../../icons/zoom-1to1.png")));

		mLifeSizeButton.addMouseListener(new MouseAdapter(){
		      public void mouseClicked(MouseEvent e){
		    	  if(e.getID() == MouseEvent.MOUSE_CLICKED && e.getButton() == MouseEvent.BUTTON3)
		    	  {
						BoardEdit edit = getSelectedEdit();
						if(edit != null)
						{
							edit.setCurrentAsLifeSizeScale();
						}
		    	  }
		      }

		});

		final ChangeListener lifeSizeChangeListner = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean isLifeSize = mLifeSizeButton.isSelected();
				BoardEdit edit = getSelectedEdit();
				if(edit != null)
				{
					if(edit.isLifeSize() == isLifeSize)
						return;

					edit.setLifeSize(isLifeSize);

					if(!isLifeSize)
						edit.resetToPreviousScale();

					edit.repaint();
				}

			}
		};

		mLifeSizeButton.addChangeListener(lifeSizeChangeListner);
//		lifeSize.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("LIFESIZEBUTTON_STR"));
//		lifeSize.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/zoom-fit-best.png")));
		mToolBar.add(mLifeSizeButton);

		mToolBar.addSeparator();
		mToolBar.addSeparator();

		board_handler = new BoardHandler();

		final SetCurrentCommandAction edit = new SetCurrentCommandAction(
				new BrdEditCommand());
		edit.putValue(AbstractAction.NAME, LanguageResource.getString("EDITBUTTON_STR"));
		edit.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("EDITBUTTON_STR"));
		edit.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/BoardCADedit24.gif")));
		mToolBar.add(edit);
//		popupMenu.add(edit);


//		JButton bt = new JButton(new ImageIcon("../../icons/Zoom24.gif"));
//		mToolBar.add(bt);

		final SetCurrentCommandAction pan = new SetCurrentCommandAction(
				new BrdPanCommand());
		pan.putValue(AbstractAction.NAME, LanguageResource.getString("PANBUTTON_STR"));
		pan.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("PANBUTTON_STR"));
		pan.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/BoardCADpan24.gif")));

		mToolBar.add(pan);


		final SetCurrentCommandAction rotate_view = new SetCurrentCommandAction(
				new BrdRotateViewCommand());
		rotate_view.putValue(AbstractAction.NAME, LanguageResource.getString("ROTATEVIEWBUTTON_STR"));
		rotate_view.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("ROTATEVIEWBUTTON_STR"));
		rotate_view.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/BoardCADrotateview24.gif")));

		mToolBar.add(rotate_view);

		mToolBar.addSeparator();

		popupMenu.addSeparator();

		mIsLockedX = new JCheckBoxMenuItem(LanguageResource.getString("NURBSCONTROLPOINTXLOCKED_STR"));
		mIsLockedX.setSelected(true);
		mIsLockedX.addItemListener(this);
		popupMenu.add(mIsLockedX);

		mIsLockedY = new JCheckBoxMenuItem(LanguageResource.getString("NURBSCONTROLPOINTYLOCKED_STR"));
		mIsLockedY.setSelected(false);
		mIsLockedY.addItemListener(this);
		popupMenu.add(mIsLockedY);

		mIsLockedZ = new JCheckBoxMenuItem(LanguageResource.getString("NURBSCONTROLPOINTZLOCKED_STR"));
		mIsLockedZ.setSelected(false);
		mIsLockedZ.addItemListener(this);
		popupMenu.add(mIsLockedZ);

		popupMenu.addSeparator();

		mViewBlank = new JCheckBoxMenuItem(LanguageResource.getString("VIEWBLANK_STR"));
		mViewBlank.setSelected(false);
		mViewBlank.addItemListener(this);
		popupMenu.add(mViewBlank);

		mViewDeckCut = new JCheckBoxMenuItem(LanguageResource.getString("VIEWDECKCUT_STR"));
		mViewDeckCut.setSelected(false);
		mViewDeckCut.addItemListener(this);
		popupMenu.add(mViewDeckCut);

		mViewBottomCut = new JCheckBoxMenuItem(LanguageResource.getString("VIEWBOTTOMCUT_STR"));
		mViewBottomCut.setSelected(false);
		mViewBottomCut.addItemListener(this);
		popupMenu.add(mViewBottomCut);


		popupMenu.addSeparator();


		final JButton spotCheck = new JButton();
		final JButton spotCheck2 = new JButton();

		spotCheck.setText(LanguageResource.getString("SPOTCHECKBUTTON_STR"));
		spotCheck2.setText(LanguageResource.getString("SPOTCHECKBUTTON_STR"));

		final ChangeListener spotCheckChangeListner = new ChangeListener() {
			BrdSpotCheckCommand cmd = new BrdSpotCheckCommand();

			boolean mIsSpotChecking = false;

			public void stateChanged(ChangeEvent e) {
				ButtonModel model = ((JButton) e.getSource()).getModel();
				if (model.isPressed()) {
					cmd.spotCheck();
					mIsSpotChecking = true;
				} else if (mIsSpotChecking == true) {
					cmd.restore();
					mIsSpotChecking = false;
				}

			}
		};

		spotCheck.addChangeListener(spotCheckChangeListner);
		spotCheck2.addChangeListener(spotCheckChangeListner);

//		mToolBar.add(spotCheck);
		popupMenu.add(spotCheck2);

		mToolBar.add(undo);
		mToolBar.add(redo);

		mToolBar.addSeparator();


		final SetCurrentCommandAction toggleDeckAndBottom = new SetCurrentCommandAction() {
			static final long serialVersionUID=1L;
			public void actionPerformed(ActionEvent event) {
				toggleBottomAndDeck();
			}

		};

		toggleDeckAndBottom.putValue(AbstractAction.NAME, LanguageResource.getString("TOGGLEDECKBOTTOMBUTTON_STR"));
		toggleDeckAndBottom.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("TOGGLEDECKBOTTOMBUTTON_STR"));
		toggleDeckAndBottom.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/BoardCADtoggle24x35.png")));
		mToolBar.add(toggleDeckAndBottom);
		popupMenu.add(toggleDeckAndBottom);



		mToolBar.addSeparator();

		final SetCurrentCommandAction addGuidePoint = new SetCurrentCommandAction(
				new BrdAddGuidePointCommand());
		addGuidePoint.putValue(AbstractAction.NAME, LanguageResource.getString("ADDGUIDEPOINTBUTTON_STR"));
		addGuidePoint.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("ADDGUIDEPOINTBUTTON_STR"));
		addGuidePoint.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/add-guidepoint.png")));
		mToolBar.add(addGuidePoint);
		popupMenu.add(addGuidePoint);
		popupMenu.add(guidePoints);

		final SetCurrentCommandAction addControlPoint = new SetCurrentOneShotCommandAction(
				new BrdAddControlPointCommand());
		addControlPoint.putValue(AbstractAction.NAME, LanguageResource.getString("ADDCONTROLPOINTBUTTON_STR"));
		addControlPoint.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("ADDCONTROLPOINTBUTTON_STR"));
		addControlPoint.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/add-controlpoint.png")));
		mToolBar.add(addControlPoint);
		popupMenu.add(addControlPoint);

		final SetCurrentCommandAction deleteControlPoint = new SetCurrentCommandAction() {
			static final long serialVersionUID=1L;
			public void actionPerformed(ActionEvent event) {
				BoardEdit edit = getSelectedEdit();
				if(edit == null)
					return;
				ArrayList<BezierKnot> selectedControlPoints = edit
				.getSelectedControlPoints();

				if (selectedControlPoints.size() == 0) {
					JOptionPane.showMessageDialog(BoardCAD.getInstance()
							.getFrame(), LanguageResource.getString("NOCONTROLPOINTSELECTEDMSG_STR"),
							LanguageResource.getString("NOCONTROLPOINTSELECTEDTITLE_STR"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				int selection = JOptionPane.showConfirmDialog(
						BoardCAD.getInstance().getFrame(),
						LanguageResource.getString("DELETECONTROLPOINTSMSG_STR"),
						LanguageResource.getString("DELETECONTROLPOINTSTITLE_STR"), JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_OPTION);

				if (selection == JOptionPane.NO_OPTION) {

					return;

				}

				BrdMacroCommand macroCmd = new BrdMacroCommand();
				macroCmd.setSource(edit);
				BezierSpline[] splines = edit.getActiveBezierSplines(edit
						.getCurrentBrd());

				for(int j=0; j < splines.length; j++)
				{
					for (int i = 0; i < selectedControlPoints.size(); i++) {
						BezierKnot ControlPoint = selectedControlPoints.get(i);

						if (ControlPoint == splines[j].getControlPoint(0)
								|| ControlPoint == splines[j].getControlPoint(splines[j]
										.getNrOfControlPoints() - 1)) {
							continue;
						}

						BrdDeleteControlPointCommand deleteControlPointCommand = new BrdDeleteControlPointCommand(
								edit, ControlPoint, splines[j]);

						macroCmd.add(deleteControlPointCommand);

					}
				}

				macroCmd.execute();

				mTabbedPane.repaint();
			}

		};
		deleteControlPoint.putValue(AbstractAction.NAME, LanguageResource.getString("DELETECONTROLPOINTSBUTTON_STR"));
		deleteControlPoint.putValue(AbstractAction.SHORT_DESCRIPTION, LanguageResource.getString("DELETECONTROLPOINTSBUTTON_STR"));
		deleteControlPoint.putValue(AbstractAction.SMALL_ICON, new ImageIcon(getClass().getResource("../../icons/remove-controlpoint.png")));
		mToolBar.add(deleteControlPoint);
		popupMenu.add(deleteControlPoint);

		mToolBar.addSeparator();
/*
		final JRadioButton millimeterButton = new JRadioButton(LanguageResource.getString("MILLIMETERSRADIOBUTTON_STR"));
		millimeterButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setCurrentUnit(UnitUtils.MILLIMETERS);
			}
		});
		final JRadioButton imperialButton = new JRadioButton(LanguageResource.getString("FEETINCHESRADIOBUTTON_STR"));
		imperialButton.setSelected(true);
		imperialButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setCurrentUnit(UnitUtils.INCHES);
			}
		});
		final JRadioButton imperialDecimalButton = new JRadioButton(LanguageResource.getString("DECIMALFEETINCHESRADIOBUTTON_STR"));
		imperialDecimalButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setCurrentUnit(UnitUtils.INCHES_DECIMAL);
			}
		});
		final JRadioButton centimeterButton = new JRadioButton(LanguageResource.getString("CENTIMETERSRADIOBUTTON_STR"));
		centimeterButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setCurrentUnit(UnitUtils.CENTIMETERS);
			}
		});

		final ButtonGroup unitButtonGroup = new ButtonGroup();
		unitButtonGroup.add(imperialButton);
		unitButtonGroup.add(millimeterButton);
		unitButtonGroup.add(centimeterButton);
		unitButtonGroup.add(imperialDecimalButton);
		mToolBar.add(imperialButton);
		//mToolBar.addSeparator();
		mToolBar.add(millimeterButton);
		//mToolBar.addSeparator();
		mToolBar.add(centimeterButton);
		//mToolBar.addSeparator();
		mToolBar.add(imperialDecimalButton);
*/
		JLabel unitLabel = new JLabel(LanguageResource.getString("UNIT_STR"));
		mToolBar.add(unitLabel);
		
		String[] unitsStrList = new String[]{LanguageResource.getString("FEETINCHESRADIOBUTTON_STR"), 
											LanguageResource.getString("DECIMALFEETINCHESRADIOBUTTON_STR"),
											LanguageResource.getString("MILLIMETERSRADIOBUTTON_STR"),
											LanguageResource.getString("CENTIMETERSRADIOBUTTON_STR"),
											LanguageResource.getString("METERSRADIOBUTTON_STR")
											};
		JComboBox unitComboBox = new JComboBox(unitsStrList);
		unitComboBox.setEditable(false);
		unitComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
		        switch(cb.getSelectedIndex())
		        {
		        default:
		        case 0:
		        	setCurrentUnit(UnitUtils.INCHES);
		        	break;
		        case 1:
		        	setCurrentUnit(UnitUtils.INCHES_DECIMAL);
		        	break;
		        case 2:
		        	setCurrentUnit(UnitUtils.MILLIMETERS);
		        	break;
		        case 3:
		        	setCurrentUnit(UnitUtils.CENTIMETERS);
		        	break;
		        case 4:
		        	setCurrentUnit(UnitUtils.METERS);
		        	break;
		        }
			}
		});
		mToolBar.addSeparator(new Dimension(5,0));
		mToolBar.add(unitComboBox);
		unitComboBox.setMaximumSize(new Dimension(140, 30));

		mFrame.getContentPane().add(mToolBar, BorderLayout.NORTH);


		final JMenu crossSectionsForPopupMenu = new JMenu(LanguageResource.getString("CROSSECTIONSMENU_STR"));
		crossSectionsForPopupMenu.add(mNextCrossSection);
		crossSectionsForPopupMenu.add(mPreviousCrossSection);
		crossSectionsForPopupMenu.add(addCrossSection);
		crossSectionsForPopupMenu.add(moveCrossSection);
		crossSectionsForPopupMenu.add(deleteCrossSection);
		crossSectionsForPopupMenu.add(copyCrossSection);
		crossSectionsForPopupMenu.add(pasteCrossSection);
		popupMenu.add(crossSectionsForPopupMenu);




		mTabbedPane = new JTabbedPane();
		
		fourView=new QuadView();

		view1 = new BoardEdit()
		{
			static final long serialVersionUID=1L;
			{
				setPreferredSize(new Dimension(300, 200));
				mDrawControl = BezierBoardDrawUtil.MirrorY;
			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				return new BezierSpline[]{brd.getOutline()};
			}

			public ArrayList<Point2D.Double> getGuidePoints() {
				return BoardCAD.getInstance().getCurrentBrd()
				.getOutlineGuidePoints();
			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {
				super.drawPart(g, color, stroke, brd, fill);
				if(isPaintingCenterLine())
				{
					drawCenterLine(g, getCenterLineColor(), stroke, brd.getLength()/2.0, brd.getCenterWidth()*1.1);
				}
				if(isPaintingCrossectionsPositions())
					drawOutlineCrossections(this,g,color,stroke,brd);
				if(isPaintingFlowlines())
					drawOutlineFlowlines(this,g,getFlowLinesColor(),stroke,brd);
				if(isPaintingTuckUnderLine())
					drawOutlineTuckUnderLine(this,g,getTuckUnderLineColor(),stroke,brd);
				if(isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode()) || (brd == getOriginalBrd() && isOrgFocus())))
					drawOutlineFootMarks(this,g,new BasicStroke(2.0f/(float)this.mScale),brd);
				drawStringer(g, getStringerColor(),
						stroke, brd);
				if (isPaintingFins()) {
					drawFins(g, getFinsColor(), stroke, brd);
				}
			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			public void onBrdChanged() {
				getCurrentBrd().onOutlineChanged();

				super.onBrdChanged();
				view2.repaint();
			}

			public void mousePressed(final MouseEvent e) {
				super.mousePressed(e);

				if(mSelectedControlPoints.size() == 0)
				{
					final Point pos = e.getPoint();
					final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
					final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
					double tolerance = 5.0;
					if (index != -1 && Math.abs(getCurrentBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance)
					{
						getCurrentBrd().setCurrentCrossSection(index);
					}
					if(getOriginalBrd() != null)
					{
						final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(getOriginalBrd().getCrossSections().get(indexOriginal).getPosition() - brdPos.x) < tolerance) {
							getOriginalBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					if(getGhostBrd() != null)
					{
						final int indexGhost = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexGhost != -1 && Math.abs(getGhostBrd().getCrossSections().get(indexGhost).getPosition() - brdPos.x) < tolerance) {
							getGhostBrd().setCurrentCrossSection(indexGhost);
						}
					}
					view2.repaint();
				}

			}

			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				view2.repaint();
			}

		};
		view1.add(popupMenu);


		view2 = new BoardEdit() {

			static final long serialVersionUID=1L;

			{
				setPreferredSize(new Dimension(300, 200));
				mDrawControl = BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 25;
			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				final BezierBoardCrossSection currentCrossSection = brd.getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return new BezierSpline[]{brd.getCurrentCrossSection().getBezierSpline()};
			}

			public ArrayList<Point2D.Double> getGuidePoints() {
				final BezierBoardCrossSection currentCrossSection = BoardCAD.getInstance()
				.getCurrentBrd().getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return currentCrossSection.getGuidePoints();
			}

			protected boolean isPaintingVolumeDistribution()
			{
				return false;
			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {

				if(brd.isEmpty())
					return;

				if (isPaintingNonActiveCrossSections()) {
					final ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();

					final BasicStroke bs = (BasicStroke) stroke;

					final float[] dashPattern = new float[] { 0.8f, 0.2f };
					final BasicStroke stapled = new BasicStroke((float) (bs
							.getLineWidth() / 2.0), bs.getEndCap(), bs
							.getLineJoin(), bs.getMiterLimit(), dashPattern, 0f);
					final Color noneActiveColor = color.brighter();

					double currentCrossSectionRocker = brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition());

					JavaDraw d = new JavaDraw(g);
					for (int i = 0; i < crossSections.size(); i++) {
						if (crossSections.get(i) != brd
								.getCurrentCrossSection()) {

							double rockerOffset = 0;
							if(BoardCAD.getInstance().isUsingOffsetInterpolation())
							{
								rockerOffset = brd.getRockerAtPos(crossSections.get(i).getPosition()) - currentCrossSectionRocker;
								rockerOffset *= this.mScale;
							}

							BezierBoardDrawUtil.paintBezierSpline(d, mOffsetX, mOffsetY-rockerOffset,
									mScale, noneActiveColor, stapled,
									crossSections.get(i).getBezierSpline(),
									mDrawControl, fill);
						}

					}

				}
				
				if (isPaintingSlidingCrossSection()) {

					final Color col = (isGhostMode()) ? color : Color.GRAY;

					double pos = view3.hasMouse()?view3.mBrdCoord.x:view1.mBrdCoord.x;

					double rockerOffset = 0;
					if(BoardCAD.getInstance().isUsingOffsetInterpolation())
					{
						double currentCrossSectionRocker = brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition());
						rockerOffset = brd.getRockerAtPos(pos) - currentCrossSectionRocker;
						rockerOffset *= this.mScale;
					}


//DEBUG					System.out.printf("rockerOffset: %f\n", rockerOffset);

					BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX, mOffsetY-rockerOffset,
							0.0, mScale, col, stroke,
							(mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							(mDrawControl & BezierBoardDrawUtil.FlipY) != 0,
							pos, brd);

					if (isGhostMode()) {
						if(BoardCAD.getInstance().isUsingOffsetInterpolation())
						{
							double currentCrossSectionRocker = getCurrentBrd().getRockerAtPos(getCurrentBrd().getCurrentCrossSection().getPosition());
							rockerOffset = getCurrentBrd().getRockerAtPos(pos) - currentCrossSectionRocker;
							rockerOffset *= this.mScale;
						}
						BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX,
								mOffsetY-rockerOffset, 0.0, mScale, getGhostBrdColor(), stroke,
								(mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
								(mDrawControl & BezierBoardDrawUtil.FlipY) != 0,
								pos,
								getCurrentBrd());
					}

				}

				super.drawPart(g, color, stroke, brd, fill);

				if(isPaintingCenterLine())
					drawCrossSectionCenterline(this, g, getCenterLineColor(), stroke, brd);
				if(isPaintingTuckUnderLine())
					drawCrossSectionTuckUnderLine(this, g, getTuckUnderLineColor(), stroke, brd);
				if(isPaintingFlowlines())
					drawCrossSectionFlowlines(this, g, getFlowLinesColor(), stroke, brd);
				if(isPaintingApexline())
					drawCrossSectionApexline(this, g, getApexLineColor(), stroke, brd);

			}
			
			public void drawBrdCoordinate(Graphics2D g) 
			{
				super.drawBrdCoordinate(g);

				BezierBoard brd = getCurrentBrd();
				if(brd.isEmpty())
					return;
				
				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				if(crs == null)
					return;
				
				g.setColor(Color.BLACK);

				//	 get metrics from the graphics
				FontMetrics metrics = g.getFontMetrics(mBrdCoordFont);
				
				// get the height of a line of text in this font and render context
				int hgt = metrics.getHeight();

				String posStr = LanguageResource.getString("CROSSECTIONPOS_STR") + UnitUtils.convertLengthToCurrentUnit(mBoardSpec.isOverCurveSelected()?brd.getBottom().getLengthByX(crs.getPosition()):crs.getPosition(), false) + (mBoardSpec.isOverCurveSelected()?" O.C":"");

				g.drawString(posStr, 10, hgt*3);

				// get the height of a line of text in this font and render context

				String widthStr = LanguageResource.getString("CROSSECTIONWIDTH_STR") + UnitUtils.convertLengthToCurrentUnit(crs.getWidth(), false) ;

				g.drawString(widthStr, 10, hgt*4);
				
				final Dimension dim = getSize();
				
				String releaseAngleStr = LanguageResource.getString("RELEASEANGLE_STR") + String.format("%1$.1f degrees", crs.getReleaseAngle()/MathUtils.DEG_TO_RAD);

				final int releaseAngleStrLength = metrics.stringWidth(releaseAngleStr);

				g.drawString(releaseAngleStr, dim.width - releaseAngleStrLength - 10, hgt*1);

				String tuckUnderRadiusStr = LanguageResource.getString("TUCKRADIUS_STR") + UnitUtils.convertLengthToCurrentUnit(crs.getTuckRadius(),false);

				final int tuckUnderRadiusStrLength = metrics.stringWidth(tuckUnderRadiusStr);

				g.drawString(tuckUnderRadiusStr, dim.width - tuckUnderRadiusStrLength - 10, hgt*2);

			}
			
			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				
				if (brd.getCrossSections().size() == 0)
					return;


				this.setName("QuadViewCrossSection");

				if (brd.getCurrentCrossSection() == null)
					return;

				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				final double thickness = crs.getThicknessAtPos(Math.abs(mBrdCoord.x));

				if (thickness <= 0)
					return;

				final double bottom = crs.getBottomAtPos(Math.abs(mBrdCoord.x));
				final double centerThickness = crs.getThicknessAtPos(BezierSpline.ZERO);

				final double mulX = (mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
				final double mulY = (mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

				// get metrics from the graphics
				final FontMetrics metrics = g.getFontMetrics(mSlidingInfoFont);
				// get the height of a line of text in this font and render
				// context
				final int hgt = metrics.getHeight();

				final Dimension dim = getSize();

				String thicknessStr = LanguageResource.getString("CROSSECTIONSLIDINGINFOTHICKNESS_STR");
				mSlidingInfoString = thicknessStr
					+ UnitUtils.convertLengthToCurrentUnit(thickness, false) + String.format("(%02d%%)", (int)((thickness*100)/centerThickness));

				g.setColor(Color.BLUE);

				// get the advance of my text in this font and render context
				final int adv = metrics.stringWidth(mSlidingInfoString);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension size = new Dimension(adv, hgt + 1);

				// get the advance of my text in this font and render context
				final int advOfThicknessStr = metrics.stringWidth(thicknessStr);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension sizeOfThicknessStr = new Dimension(advOfThicknessStr, hgt + 1);

				int textX = mScreenCoord.x - (sizeOfThicknessStr.width);
				if (textX < 10)
					textX = 10;

				if (textX + size.width + 10 > dim.width)
					textX = dim.width - size.width - 10;


				g.setStroke(new BasicStroke((float) (1.0 / mScale)));
				g.drawString(mSlidingInfoString, textX, dim.height
						- (size.height * 2 + 5));


				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOBOTTOM_STR")
					+ UnitUtils.convertLengthToCurrentUnit(bottom, false);
				
				g.setColor(Color.RED);

				g.drawString(mSlidingInfoString, textX, dim.height
						- size.height);

				g.setColor(Color.BLACK);

				final double fromCenter = Math.abs(mBrdCoord.x);

				final double fromRail = crs.getWidth()/2 - Math.abs(mBrdCoord.x);


				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMRAIL_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromRail, false);


				g.drawString(mSlidingInfoString, textX, dim.height
						- (size.height + 2) * 4);

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMCENTER_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromCenter, false);


				g.drawString(mSlidingInfoString, textX, dim.height
						- (size.height + 2) * 3);


				//sets the color of the +ve sliding info (above Y base line)
				g.setColor(Color.BLUE);


				final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g),
						mOffsetX, mOffsetY, mScale);


				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, bottom * mulY,
						mBrdCoord.x * mulX, (bottom + thickness) * mulY);
				g.draw(mSlidingInfoLine);

				//sets the color of the Bottom sliding info (-ve# when concaved +ve# when Vee)
				g.setColor(Color.RED);

				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, 0 * mulY,
						mBrdCoord.x * mulX, bottom * mulY);
				g.draw(mSlidingInfoLine);


				g.setTransform(savedTransform);

			}

			public void fitBrd() {
				final BezierBoard brd = getCurrentBrd();
				final Dimension dim = getSize();

				double width=board_handler.get_segment_width()/10;
				if (width<10)
					width=10;

				if (width<brd.getCenterWidth())
					width=brd.getCenterWidth();


				mScale = (dim.width - ((BORDER * dim.width / 100) * 2)) / width;

				mOffsetX = dim.width * 1 / 2;
				mOffsetY = dim.height * 1 / 2 + (brd.getThicknessAtPos(brd.getLength() / 2.0f) * mScale);
//				mOffsetY=board_handler.get_edge_offset()/10*mScale+2*dim.height/3;

				mLastWidth = dim.width;
			}

			public void onBrdChanged() {
				getCurrentBrd().onCrossSectionChanged();

				view1.repaint();
				view3.repaint();
				super.onBrdChanged();
			}

			Point2D.Double getTail() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0).getEndPoint().clone();

				return tail;
			}

			Point2D.Double getNose() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0).getEndPoint().clone();
				final Point2D.Double nose = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(getActiveBezierSplines(brd)[0].getNrOfControlPoints() - 1)
						.getEndPoint().clone();
				nose.y = tail.y;
				nose.x = getActiveBezierSplines(brd)[0].getMaxX();
				return nose;
			}

			public void repaint()
			{
				super.repaint();
				if(mCrossSectionOutlineEdit != null)
					mCrossSectionOutlineEdit.repaint();
			}

		};
		view2.add(popupMenu);

		view3 = new BoardEdit() {
			static final long serialVersionUID=1L;

			{
				setPreferredSize(new Dimension(300, 200));
				mDrawControl = BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 1000;
			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd)
			{
				switch(mEditDeckorBottom)
				{
					case DECK:
						return new BezierSpline[]{brd.getDeck()};
					case BOTTOM:
						return new BezierSpline[]{brd.getBottom()};
					case BOTH:
					default:
						return new BezierSpline[]{brd.getDeck(),brd.getBottom()};
				}
			}

			public ArrayList<Point2D.Double> getGuidePoints()
			{
				switch(mEditDeckorBottom)
				{
					case DECK:
						return BoardCAD.getInstance().getCurrentBrd()
						.getDeckGuidePoints();
					case BOTTOM:
						return BoardCAD.getInstance().getCurrentBrd()
						.getBottomGuidePoints();
					case BOTH:
					default:
					{
						ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();
						list.addAll(BoardCAD.getInstance().getCurrentBrd()
								.getDeckGuidePoints());
						list.addAll(BoardCAD.getInstance().getCurrentBrd()
								.getBottomGuidePoints());
						return list;
					}
				}

			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {
			
				if (isPaintingBaseLine()) {
					drawStringer(g, mColorSettings.getColor(BASELINECOLOR),
							new BasicStroke((float) (mSizeSettings.getDouble(BASELINETHICKNESS) / mScale)),
							brd);
				}
				if(isPaintingFlowlines())
					drawProfileFlowlines(this,g,getFlowLinesColor(),stroke,brd);
				if(isPaintingApexline())
					drawProfileApexline(this,g,getApexLineColor(),stroke,brd);
				if(isPaintingTuckUnderLine())
					drawProfileTuckUnderLine(this,g,getTuckUnderLineColor(),stroke,brd);
				if(isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode()) || (brd == getOriginalBrd() && isOrgFocus())))
					drawProfileFootMarks(this,g,new BasicStroke(2.0f/(float)this.mScale),brd);
				if (isPaintingBaseLine()) {
					drawStringer(g, mColorSettings.getColor(BASELINECOLOR),
							new BasicStroke((float) (mSizeSettings.getDouble(BASELINETHICKNESS) / mScale)),
							brd);
				}
				if(isPaintingCenterLine())
				{
					drawCenterLine(g, getCenterLineColor(), stroke, brd.getLength()/2.0, brd.getThickness()*2.2);
				}

				BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX, mOffsetY, mScale,
						color, stroke, new BezierSpline[]{brd.getBottom(), brd.getDeck()}, mDrawControl, fill);

				super.drawPart(g, color, stroke, brd, false);
			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				drawProfileSlidingInfo(this, g, color, stroke, brd);
			}

			public void onBrdChanged() {
				getCurrentBrd().onRockerChanged();

				super.onBrdChanged();
				view2.repaint();
			}

			public void mousePressed(final MouseEvent e) {
				super.mousePressed(e);

				if(mSelectedControlPoints.size() == 0)
				{
					final Point pos = e.getPoint();
					final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
					final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
					double tolerance = 5.0;
					if (index != -1 && Math.abs(getCurrentBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance)
					{
						getCurrentBrd().setCurrentCrossSection(index);
					}
					if(getOriginalBrd() != null)
					{
						final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(getOriginalBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
							getOriginalBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					if(getGhostBrd() != null)
					{
						final int indexOriginal = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(getGhostBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
							getGhostBrd().setCurrentCrossSection(indexOriginal);
						}
					}

					view2.repaint();
				}

			}

			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				view2.repaint();
			}

		};
		view3.add(popupMenu);



		view4 = new BoardEdit()
		{
			static final long serialVersionUID=1L;
			{
				setPreferredSize(new Dimension(300, 200));
				//mDrawControl = BezierBoardDrawUtil.MirrorY;
				mDrawControl=0;

			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				return new BezierSpline[]{brd.getOutline()};
			}

			public ArrayList<Point2D.Double> getGuidePoints() {
				return BoardCAD.getInstance().getCurrentBrd()
				.getOutlineGuidePoints();
			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {
				super.drawPart(g, color, stroke, brd, fill);
				if(isPaintingCenterLine())
				{
					drawCenterLine(g, getCenterLineColor(), stroke, brd.getLength()/2.0, brd.getCenterWidth()*1.1);
				}
				if(isPaintingCrossectionsPositions())
					drawOutlineCrossections(this,g,color,stroke,brd);
				if(isPaintingFlowlines())
					drawOutlineFlowlines(this,g,getFlowLinesColor(),stroke,brd);
				if(isPaintingTuckUnderLine())
					drawOutlineTuckUnderLine(this,g,getTuckUnderLineColor(),stroke,brd);
				if(isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode()) || (brd == getOriginalBrd() && isOrgFocus())))
					drawOutlineFootMarks(this,g,new BasicStroke(2.0f/(float)this.mScale),brd);
				drawStringer(g, getStringerColor(),
						stroke, brd);
				if (isPaintingFins()) {
					drawFins(g, getFinsColor(), stroke, brd);
				}
			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			public void onBrdChanged() {
				getCurrentBrd().onOutlineChanged();

				super.onBrdChanged();
				view2.repaint();
			}

			public void mousePressed(final MouseEvent e) {
				super.mousePressed(e);

				if(mSelectedControlPoints.size() == 0)
				{
					final Point pos = e.getPoint();
					final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
					final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
					double tolerance = 5.0;
					if (index != -1 && Math.abs(getCurrentBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance)
					{
						getCurrentBrd().setCurrentCrossSection(index);
					}
					if(getOriginalBrd() != null)
					{
						final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(getOriginalBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
							getOriginalBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					if(getGhostBrd() != null)
					{
						final int indexOriginal = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(getGhostBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
							getGhostBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					view2.repaint();
				}

			}

			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				view2.repaint();
			}

		};
		view4.add(popupMenu);



		mNurbspanel = new JPanel();
		mNurbspanel.setLayout(new BorderLayout());

//		board_handler = new BoardHandler();

		// window.setMenuBar(menu);
		// window.setBackground(Color.lightGray);
		status_panel = new StatusPanel();
		// window.add("South", status_panel);
		design_panel = new DesignPanel(board_handler, status_panel);
		design_panel.view_3d();

		mNurbspanel.add(design_panel, BorderLayout.CENTER);
		mNurbspanel.add(status_panel, BorderLayout.SOUTH);

		// window.add("Center", design_panel);

//		mTabbedPane.addTab(LanguageResource.getString("3DMODELEDIT_STR"), mNurbspanel);
		// board_handler.new_board();
		// design_panel.update_3d();


		view1.setParentContainer(fourView);
		view2.setParentContainer(fourView);
		view3.setParentContainer(fourView);
		fourView.add(view1);
		fourView.add(view2);
		fourView.add(view3);
		fourView.add(view4);
//		fourView.add(mNurbspanel);
		mTabbedPane.add(LanguageResource.getString("QUADVIEW_STR"), fourView);


		mOutlineEdit = new BoardEdit()
		{
			static final long serialVersionUID=1L;
			{
				mDrawControl = BezierBoardDrawUtil.MirrorY;
			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				return new BezierSpline[]{brd.getOutline()};
			}

			public ArrayList<Point2D.Double> getGuidePoints() {
				return BoardCAD.getInstance().getCurrentBrd()
				.getOutlineGuidePoints();
			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {
				super.drawPart(g, color, stroke, brd, fill);
				if(isPaintingFlowlines())
					drawOutlineFlowlines(this,g,getFlowLinesColor(),stroke,brd);
				if(isPaintingTuckUnderLine())
					drawOutlineTuckUnderLine(this,g,getTuckUnderLineColor(),stroke,brd);
				if(isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode()) || (brd == getOriginalBrd() && isOrgFocus())))
					drawOutlineFootMarks(this,g,new BasicStroke(2.0f/(float)this.mScale),brd);
				if(isPaintingCenterLine())
				{
					drawCenterLine(g, getCenterLineColor(), stroke, brd.getLength()/2.0, brd.getCenterWidth()*1.1);
				}
				drawStringer(g, getStringerColor(),
						stroke, brd);
				if (isPaintingFins()) {
					drawFins(g, getFinsColor(), stroke, brd);
				}
			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			public void onBrdChanged() {
				getCurrentBrd().onOutlineChanged();

				super.onBrdChanged();
			}

		};
		mOutlineEdit.add(popupMenu);
		mTabbedPane.add(LanguageResource.getString("OUTLINEEDIT_STR"), mOutlineEdit);

		mBottomAndDeckEdit = new BoardEdit() {
			static final long serialVersionUID=1L;

			{
				setPreferredSize(new Dimension(400, 150));
				mDrawControl = BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 1000;
			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd)
			{
				switch(mEditDeckorBottom)
				{
					case DECK:
						return new BezierSpline[]{brd.getDeck()};
					case BOTTOM:
						return new BezierSpline[]{brd.getBottom()};
					case BOTH:
					default:
						return new BezierSpline[]{brd.getDeck(),brd.getBottom()};
				}
			}

			public ArrayList<Point2D.Double> getGuidePoints()
			{
				switch(mEditDeckorBottom)
				{
					case DECK:
						return BoardCAD.getInstance().getCurrentBrd()
						.getDeckGuidePoints();
					case BOTTOM:
						return BoardCAD.getInstance().getCurrentBrd()
						.getBottomGuidePoints();
					case BOTH:
					default:
					{
						ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();
						list.addAll(BoardCAD.getInstance().getCurrentBrd()
								.getDeckGuidePoints());
						list.addAll(BoardCAD.getInstance().getCurrentBrd()
								.getBottomGuidePoints());
						return list;
					}
				}

			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {
				if(isPaintingFlowlines())
					drawProfileFlowlines(this,g,getFlowLinesColor(),stroke,brd);
				if(isPaintingApexline())
					drawProfileApexline(this,g,getApexLineColor(),stroke,brd);
				if(isPaintingTuckUnderLine())
					drawProfileTuckUnderLine(this,g,getTuckUnderLineColor(),stroke,brd);
				if(isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode()) || (brd == getOriginalBrd() && isOrgFocus())))
					drawProfileFootMarks(this,g,new BasicStroke(2.0f/(float)this.mScale),brd);
				if (isPaintingBaseLine()) {
					drawStringer(g, mColorSettings.getColor(BASELINECOLOR),
							new BasicStroke((float) (mSizeSettings.getDouble(BASELINETHICKNESS) / mScale)),
							brd);
				}
				if(isPaintingCenterLine())
				{
					drawCenterLine(g, getCenterLineColor(), stroke, brd.getLength()/2.0, brd.getThickness()*2.2);
				}

				BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX, mOffsetY, mScale,
						color, stroke, new BezierSpline[]{brd.getBottom(), brd.getDeck()}, mDrawControl, fill);

				
				super.drawPart(g, color, stroke, brd, false);
			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				drawProfileSlidingInfo(this, g, color, stroke, brd);
			}

			public void onBrdChanged() {
				getCurrentBrd().onRockerChanged();

				super.onBrdChanged();
			}

		};
		mBottomAndDeckEdit.add(popupMenu);

		mTabbedPane.add(LanguageResource.getString("BOTTOMANDDECKEDIT_STR"), mBottomAndDeckEdit);



/*		mOutlineAndProfileSplitPane = new BrdEditSplitPane(
				JSplitPane.VERTICAL_SPLIT, mOutlineEdit2, mBottomAndDeckEdit);
		mOutlineAndProfileSplitPane.setOneTouchExpandable(true);
		mOutlineAndProfileSplitPane.setResizeWeight(0.7);

		mTabbedPane.add(LanguageResource.getString("OUTLINEPROFILEEDIT_STR"), mOutlineAndProfileSplitPane);
*/

		mCrossSectionEdit = new BoardEdit() {
			static final long serialVersionUID=1L;

			{
				setPreferredSize(new Dimension(400, 200));
				mDrawControl = BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 25;
			}

			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				final BezierBoardCrossSection currentCrossSection = brd.getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return new BezierSpline[]{brd.getCurrentCrossSection().getBezierSpline()};
			}

			public ArrayList<Point2D.Double> getGuidePoints() {
				final BezierBoardCrossSection currentCrossSection = BoardCAD.getInstance()
				.getCurrentBrd().getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return currentCrossSection.getGuidePoints();
			}

			protected boolean isPaintingVolumeDistribution()
			{
				return false;
			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {
				if(brd.isEmpty())
					return;

				if (isPaintingNonActiveCrossSections()) {
					final ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();

					final BasicStroke bs = (BasicStroke) stroke;

					final float[] dashPattern = new float[] { 0.8f, 0.2f };
					final BasicStroke stapled = new BasicStroke((float) (bs
							.getLineWidth() / 2.0), bs.getEndCap(), bs
							.getLineJoin(), bs.getMiterLimit(), dashPattern, 0f);
					final Color noneActiveColor = color.brighter();

					double currentCrossSectionRocker = brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition());

					for (int i = 0; i < crossSections.size(); i++) {
						if (crossSections.get(i) != brd
								.getCurrentCrossSection()) {

							double rockerOffset = 0;
							if(BoardCAD.getInstance().isUsingOffsetInterpolation())
							{
								rockerOffset = brd.getRockerAtPos(crossSections.get(i).getPosition()) - currentCrossSectionRocker;
								rockerOffset *= this.mScale;
							}

							BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY-rockerOffset,
									mScale, noneActiveColor, stapled,
									crossSections.get(i).getBezierSpline(),
									mDrawControl, fill);
						}

					}
				}
				

				if (isPaintingSlidingCrossSection()) {

					final Color col = (isGhostMode()) ? color : Color.GRAY;


					double rockerOffset = 0;
					if(BoardCAD.getInstance().isUsingOffsetInterpolation())
					{
						double currentCrossSectionRocker = brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition());
						rockerOffset = brd.getRockerAtPos(mCrossSectionOutlineEdit.mBrdCoord.x) - currentCrossSectionRocker;
						rockerOffset *= this.mScale;
					}

					BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX, mOffsetY-rockerOffset,
							0.0, mScale, col, stroke,
							(mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							(mDrawControl & BezierBoardDrawUtil.FlipY) != 0,
							mCrossSectionOutlineEdit.mBrdCoord.x, brd);

					if (isGhostMode()) {
						if(BoardCAD.getInstance().isUsingOffsetInterpolation())
						{
							double currentCrossSectionRocker = getCurrentBrd().getRockerAtPos(getCurrentBrd().getCurrentCrossSection().getPosition());
							rockerOffset = getCurrentBrd().getRockerAtPos(mCrossSectionOutlineEdit.mBrdCoord.x) - currentCrossSectionRocker;
							rockerOffset *= this.mScale;
						}
						BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX,
								mOffsetY-rockerOffset, 0.0, mScale, getGhostBrdColor(), stroke,
								(mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
								(mDrawControl & BezierBoardDrawUtil.FlipY) != 0,
								mCrossSectionOutlineEdit.mBrdCoord.x,
								getCurrentBrd());
					}

				}
				super.drawPart(g, color, stroke, brd, fill);

				
				if(isPaintingTuckUnderLine())
					drawCrossSectionTuckUnderLine(this, g, getTuckUnderLineColor(), stroke, brd);
				if(isPaintingApexline())
					drawCrossSectionApexline(this, g, getApexLineColor(), stroke, brd);
				if(isPaintingFlowlines())
					drawCrossSectionFlowlines(this, g, getFlowLinesColor(), stroke, brd);
			}

			public void drawBrdCoordinate(Graphics2D g) 
			{
				super.drawBrdCoordinate(g);

				BezierBoard brd = getCurrentBrd();
				if(brd.isEmpty())
					return;
				
				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				if(crs == null)
					return;
				
				g.setColor(Color.BLACK);

				//	 get metrics from the graphics
				FontMetrics metrics = g.getFontMetrics(mBrdCoordFont);
				
				// get the height of a line of text in this font and render context
				int hgt = metrics.getHeight();

				String posStr = LanguageResource.getString("CROSSECTIONPOS_STR") + UnitUtils.convertLengthToCurrentUnit(mBoardSpec.isOverCurveSelected()?brd.getBottom().getLengthByX(crs.getPosition()):crs.getPosition(), false) + (mBoardSpec.isOverCurveSelected()?" O.C":"");

				g.drawString(posStr, 10, hgt*3);

				// get the height of a line of text in this font and render context

				String widthStr = LanguageResource.getString("CROSSECTIONWIDTH_STR") + UnitUtils.convertLengthToCurrentUnit(crs.getWidth(), false) ;

				g.drawString(widthStr, 10, hgt*4);
				
				final Dimension dim = getSize();
				
				String releaseAngleStr = LanguageResource.getString("RELEASEANGLE_STR") + String.format("%1$.1f degrees", crs.getReleaseAngle()/MathUtils.DEG_TO_RAD);

				final int releaseAngleStrLength = metrics.stringWidth(releaseAngleStr);

				g.drawString(releaseAngleStr, dim.width - releaseAngleStrLength - 10, hgt*1);

				String tuckUnderRadiusStr = LanguageResource.getString("TUCKRADIUS_STR") + UnitUtils.convertLengthToCurrentUnit(crs.getTuckRadius(),false);

				final int tuckUnderRadiusStrLength = metrics.stringWidth(tuckUnderRadiusStr);

				g.drawString(tuckUnderRadiusStr, dim.width - tuckUnderRadiusStrLength - 10, hgt*2);
			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				if (brd.getCrossSections().size() == 0)
					return;


				if (brd.getCurrentCrossSection() == null)
					return;

				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				final double thickness = crs.getThicknessAtPos(Math.abs(mBrdCoord.x));

				if (thickness <= 0)
					return;

				final double bottom = crs.getBottomAtPos(Math.abs(mBrdCoord.x));
				final double centerThickness = crs.getThicknessAtPos(BezierSpline.ZERO);

				final double mulX = (mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
				final double mulY = (mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

				// get metrics from the graphics
				final FontMetrics metrics = g.getFontMetrics(mSlidingInfoFont);
				// get the height of a line of text in this font and render
				// context
				final int hgt = metrics.getHeight();

				final Dimension dim = getSize();

				String thicknessStr = LanguageResource.getString("CROSSECTIONSLIDINGINFOTHICKNESS_STR");
				mSlidingInfoString = thicknessStr
					+ UnitUtils.convertLengthToCurrentUnit(thickness, false) + String.format("(%02d%%)", (int)((thickness*100)/centerThickness));

				g.setColor(Color.BLUE);


				// get the advance of my text in this font and render context
				final int adv = metrics.stringWidth(mSlidingInfoString);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension size = new Dimension(adv, hgt + 1);

				// get the advance of my text in this font and render context
				final int advOfThicknessStr = metrics.stringWidth(thicknessStr);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension sizeOfThicknessStr = new Dimension(advOfThicknessStr, hgt + 1);

				int textX = mScreenCoord.x - (sizeOfThicknessStr.width);
				if (textX < 10)
					textX = 10;

				if (textX + size.width + 10 > dim.width)
					textX = dim.width - size.width - 10;


				g.setStroke(new BasicStroke((float) (1.0 / mScale)));
				g.drawString(mSlidingInfoString, textX, dim.height
						- (size.height * 2 + 5));


				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOBOTTOM_STR")
					+ UnitUtils.convertLengthToCurrentUnit(bottom, false);
				g.setColor(Color.RED);

				g.drawString(mSlidingInfoString, textX, dim.height
						- size.height);

				g.setColor(Color.BLACK);

				final double fromCenter = Math.abs(mBrdCoord.x);

				final double fromRail = crs.getWidth()/2.0 - Math.abs(mBrdCoord.x);


				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMRAIL_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromRail, false);


				g.drawString(mSlidingInfoString, textX, dim.height
						- (size.height + 2) * 4);

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMCENTER_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromCenter, false);


				g.drawString(mSlidingInfoString, textX, dim.height
						- (size.height + 2) * 3);

				//sets the color of the +ve sliding info (above Y base line)
				g.setColor(Color.BLUE);


				final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g),
						mOffsetX, mOffsetY, mScale);


				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, bottom * mulY,
						mBrdCoord.x * mulX, (bottom + thickness) * mulY);
				g.draw(mSlidingInfoLine);

				//sets the color of the Bottom sliding info (-ve# when concaved +ve# when Vee)
				g.setColor(Color.RED);

				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, 0 * mulY,
						mBrdCoord.x * mulX, bottom * mulY);
				g.draw(mSlidingInfoLine);


				g.setTransform(savedTransform);

			}

			public void fitBrd() {
				final BezierBoard brd = getCurrentBrd();
				final Dimension dim = getSize();

				double width=board_handler.get_segment_width()/10;
				if (width<10)
					width=10;

				if (width<brd.getCenterWidth())
					width=brd.getCenterWidth();


				mScale = (dim.width - ((BORDER * dim.width / 100) * 2)) / width;

				mOffsetX = dim.width * 1 / 2;
				mOffsetY = dim.height * 1 / 2 + (brd.getThicknessAtPos(brd.getLength() / 2.0f) * mScale);
//				mOffsetY=board_handler.get_edge_offset()/10*mScale+2*dim.height/3;

				mLastWidth = dim.width;
			}

			public void onBrdChanged() {
				getCurrentBrd().onCrossSectionChanged();

				super.onBrdChanged();
			}

			Point2D.Double getTail() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0).getEndPoint().clone();

				return tail;
			}

			Point2D.Double getNose() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0).getEndPoint().clone();
				final Point2D.Double nose = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(getActiveBezierSplines(brd)[0].getNrOfControlPoints() - 1)
						.getEndPoint().clone();
				nose.y = tail.y;
				nose.x = getActiveBezierSplines(brd)[0].getMaxX();
				return nose;
			}

			public void repaint()
			{
				super.repaint();
				if(mCrossSectionOutlineEdit != null)
					mCrossSectionOutlineEdit.repaint();
			}

		};
		mCrossSectionEdit.add(popupMenu);

		mCrossSectionOutlineEdit = new BoardEdit() {
			static final long serialVersionUID=1L;

			static final double fixedHeightBorder = 0;
			{
				setPreferredSize(new Dimension(400, 100));
				mDrawControl = BezierBoardDrawUtil.MirrorY;
			};

			public void paintComponent(final Graphics g) {
				fitBrd();
				super.paintComponent(g);
			}

			public void fitBrd() {
				super.fitBrd();

				final Dimension dim = getSize();

				final BezierBoard brd = getCurrentBrd();
				final double width = brd.getCenterWidth() + brd.getMaxRocker() * 2;
				if (dim.height - (fixedHeightBorder * 2) < width * mScale) {
					mScale = (dim.height - (fixedHeightBorder * 2)) / width;

					if ((mDrawControl & BezierBoardDrawUtil.FlipX) == 0) {
						mOffsetX = (dim.width - (brd.getLength() * mScale)) / 2;
					} else {
						mOffsetX = (dim.width - (brd.getLength() * mScale)) / 2
						+ brd.getLength() * mScale;
					}
				}

				mOffsetY -= brd.getMaxRocker() / 2 * mScale;

			}

			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd, boolean fill) {

				Color brdColor = BoardCAD.getInstance().getBrdColor();
				Color current = BoardCAD.getInstance().isGhostMode()?BoardCAD.getInstance().getGhostBrdColor():color;
				current = BoardCAD.getInstance().isOrgFocus()?BoardCAD.getInstance().getOriginalBrdColor():current;

				BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY, mScale, current,
						stroke, brd.getOutline(), mDrawControl, fill);

				if(isPaintingFlowlines())
					BezierBoardDrawUtil.paintOutlineFlowLines(new JavaDraw(g), mOffsetX, mOffsetY, mScale,
							getFlowLinesColor(), stroke, brd, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);

				if(isPaintingTuckUnderLine())
					BezierBoardDrawUtil.paintOutlineTuckUnderLine(new JavaDraw(g), mOffsetX, mOffsetY, mScale,
							getTuckUnderLineColor(), stroke, brd, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);

				BezierBoardDrawUtil.paintBezierSplines(
						new JavaDraw(g),
						mOffsetX,
						mOffsetY
						+ ((brd.getCenterWidth()/2.0 + brd
								.getMaxRocker() ) * mScale),
								mScale, current, stroke, new BezierSpline[]{brd.getDeck(), brd.getBottom()},
								(mDrawControl & BezierBoardDrawUtil.FlipX) | BezierBoardDrawUtil.FlipY, fill);
				
				
				if(isPaintingFlowlines())
					BezierBoardDrawUtil.paintProfileFlowLines(new JavaDraw(g), mOffsetX, mOffsetY+ (((brd.getCenterWidth()/ 2 + brd
							.getMaxRocker())) * mScale), mScale,
							getFlowLinesColor(), stroke, brd, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);

				if(isPaintingApexline())
					BezierBoardDrawUtil.paintProfileApexline(new JavaDraw(g), mOffsetX, mOffsetY+ (((brd.getCenterWidth()/ 2 + brd
							.getMaxRocker())) * mScale), mScale,
							getApexLineColor(), stroke, brd, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);

				if(isPaintingTuckUnderLine())
					BezierBoardDrawUtil.paintProfileTuckUnderLine(new JavaDraw(g), mOffsetX, mOffsetY+ (((brd.getCenterWidth()/ 2 + brd
							.getMaxRocker()) ) * mScale), mScale,
							getTuckUnderLineColor(), stroke, brd, (mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);




				BezierBoard ghost = BoardCAD.getInstance().getGhostBrd();
				if(BoardCAD.getInstance().isGhostMode() && ghost != null && !ghost.isEmpty())
				{
					BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY, mScale, brdColor,
							stroke, ghost.getOutline(), mDrawControl, fill);

					BezierBoardDrawUtil.paintBezierSplines(
							new JavaDraw(g),
							mOffsetX,
							mOffsetY
							+ (((brd.getCenterWidth()/ 2 + brd
									.getMaxRocker()) ) * mScale),
									mScale, brdColor, stroke, new BezierSpline[]{ghost.getDeck(),ghost.getBottom()},
									(mDrawControl & BezierBoardDrawUtil.FlipX) | BezierBoardDrawUtil.FlipY, fill);

				}

				BezierBoard org = BoardCAD.getInstance().getOriginalBrd();
				if(BoardCAD.getInstance().isOrgFocus() && org != null && !org.isEmpty())
				{
					BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY, mScale, brdColor,
							stroke, org.getOutline(), mDrawControl, fill);

					BezierBoardDrawUtil.paintBezierSplines(
							new JavaDraw(g),
							mOffsetX,
							mOffsetY
							+ (((brd.getCenterWidth()/ 2 + brd
									.getMaxRocker()) ) * mScale),
									mScale, brdColor, stroke, new BezierSpline[]{org.getDeck(), org.getBottom()},
									(mDrawControl & BezierBoardDrawUtil.FlipX) | BezierBoardDrawUtil.FlipY, fill);

				}


				final AffineTransform savedTransform = g.getTransform();

				g.setColor(color);

				g.setStroke(stroke);

				final AffineTransform at = new AffineTransform();

				at.setToTranslation(mOffsetX, mOffsetY);

				g.transform(at);

				at.setToScale(mScale, mScale);

				g.transform(at);

				final double mulX = ((mDrawControl & BezierBoardDrawUtil.FlipX) != 0) ? -1 : 1;
				final double mulY = ((mDrawControl & BezierBoardDrawUtil.FlipY) != 0) ? -1 : 1;

				final ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
				final Line2D line = new Line2D.Double();
				for (int i = 1; i < crossSections.size() - 1; i++) {
					final double pos = crossSections.get(i).getPosition();
					double width = brd.getWidthAtPos(pos);

					if (crossSections.get(i) == brd.getCurrentCrossSection()) {
						g.setColor(Color.RED);
					} else {
						g.setColor(color);
					}
					line.setLine(pos * mulX, (-width / 2) * mulY, pos * mulX,
							(width / 2) * mulY);
					g.draw(line);
				}

				if(BoardCAD.getInstance().isGhostMode() && ghost != null && !ghost.isEmpty())
				{
					final ArrayList<BezierBoardCrossSection> ghostCrossSections = ghost.getCrossSections();
					for (int i = 1; i < ghostCrossSections.size() - 1; i++) {
						final double pos = ghostCrossSections.get(i).getPosition();
						double width = ghost.getWidthAtPos(pos);

						if (ghostCrossSections.get(i) == ghost.getCurrentCrossSection()) {
							g.setColor(Color.RED);
						} else {
							g.setColor(color);
						}
						line.setLine(pos * mulX, (-width / 2) * mulY, pos * mulX,
								(width / 2) * mulY);
						g.draw(line);
					}

				}

				if(BoardCAD.getInstance().isOrgFocus() && org != null && !org.isEmpty())
				{
					final ArrayList<BezierBoardCrossSection> orgCrossSections = org.getCrossSections();
					for (int i = 1; i < orgCrossSections.size() - 1; i++) {
						final double pos = orgCrossSections.get(i).getPosition();
						double width = org.getWidthAtPos(pos);

						if (orgCrossSections.get(i) == org.getCurrentCrossSection()) {
							g.setColor(Color.RED);
						} else {
							g.setColor(color);
						}
						line.setLine(pos * mulX, (-width / 2) * mulY, pos * mulX,
								(width / 2) * mulY);
						g.draw(line);
					}

				}

				g.setTransform(savedTransform);

			}

			public void drawSlidingInfo(final Graphics2D g, final Color color,
					final Stroke stroke, final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			public void mousePressed(final MouseEvent e) {
				final Point pos = e.getPoint();
				final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
				final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
				if (index != -1)
				{
					getCurrentBrd().setCurrentCrossSection(index);
				}
				if(getOriginalBrd() != null)
				{
					final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
					if (indexOriginal != -1) {
						getOriginalBrd().setCurrentCrossSection(indexOriginal);
					}
				}
				if(getGhostBrd() != null)
				{
					final int indexOriginal = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
					if (indexOriginal != -1) {
						getGhostBrd().setCurrentCrossSection(indexOriginal);
					}
				}
				mCrossSectionSplitPane.repaint();

			}

			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				mCrossSectionEdit.repaint();
			}

		};

		mCrossSectionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				mCrossSectionEdit, mCrossSectionOutlineEdit);
		mCrossSectionSplitPane.setOneTouchExpandable(true);
		mCrossSectionSplitPane.setResizeWeight(0.7);

		mTabbedPane.add(LanguageResource.getString("CROSSECTIONEDIT_STR"), mCrossSectionSplitPane);


		mRenderedpanel = new JPanel();
		mRenderedpanel.setLayout(new BorderLayout());

		design_panel2 = new DesignPanel(board_handler, status_panel);
		design_panel2.view_rendered();

		mRenderedpanel.add(design_panel2, BorderLayout.CENTER);
		mRenderedpanel.add(status_panel, BorderLayout.SOUTH);

		// window.add("Center", design_panel);

		mTabbedPane.addTab(LanguageResource.getString("3DRENDEREDVIEW_STR"), mRenderedpanel);
		// board_handler.new_board();
		// design_panel.update_3d();

		//DEBUG!

		mJOGLpanel = new JPanel();
		mJOGLpanel.setLayout(new BorderLayout());
		joglpanel = new JOGLPanel(board_handler);
		mJOGLpanel.add(joglpanel, BorderLayout.CENTER);
		mJOGLpanel.add(status_panel, BorderLayout.SOUTH);
//		mTabbedPane.addTab("JOGL view", mJOGLpanel);
		joglpanel.redraw();



//		mTabbedPane.add("PrintBrd",mPrintBrd); //Only for debugging
//		mTabbedPane.add("PrintSpecSheet",mPrintSpecSheet); //Only for debugging
//		mTabbedPane.add("PrintChamberedWood",mPrintChamberedWoodTemplate); //Only for debugging
//		mTabbedPane.add("PrintSandwich",mPrintSandwichTemplates); //Only for debugging
//		mTabbedPane.add("PrintHWS",mPrintHollowWoodTemplates); //Only for debugging

		mTabbedPane.addChangeListener(new ChangeListener() {
			boolean setModel = false;
			public void stateChanged(final ChangeEvent e)
			{
				mGuidePointsDialog.update();

				if (mTabbedPane.getSelectedComponent() == mRenderedpanel)
				{
					if(!setModel)
					{
						BranchGroup root = new BranchGroup();
						mBezier3DOnSwitch = new Switch();
						mBezier3DOnSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
						mBezier3DOnSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
					    boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
						mBezier3DOnSwitch.setWhichChild(selected?Switch.CHILD_ALL:Switch.CHILD_NONE);
						root.addChild(mBezier3DOnSwitch);
						mScale = new TransformGroup();
						mScale.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
						mBezier3DOnSwitch.addChild(mScale);
						mBezier3DModel = new Shape3D();
						mBezier3DModel.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
						mBezier3DModel.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
						// Create an Appearance.
						Appearance a = new Appearance();
						Color3f ambient = new Color3f(0.4f, 0.4f, 0.45f);
						Color3f emissive = new Color3f(0.0f, 0.0f, 0.0f);
						Color3f diffuse = new Color3f(0.8f, 0.8f, 0.8f);
						Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);

						// Set up the material properties
						a.setMaterial(new Material(ambient, emissive, diffuse, specular, 115.0f));
						mBezier3DModel.setAppearance(a);
						mScale.addChild(mBezier3DModel);

						design_panel2.addModel(root);

						setModel = true;
					}

					boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
					if(selected && mBoardChangedFor3D)
				    {
						updateBezier3DModel();
				    }

//					design_panel2.update_3d();
					design_panel2.fit_all();
					design_panel2.redraw();
				}
/*
				if (mTabbedPane.getSelectedComponent() == mNurbspanel) {
					if (mNeverApproximateNurbs == true)
						return;

					if (mAlwaysApproximateNurbs == false) {

						final Object[] options = { "Always", "Yes", "No", "Never" };
						final int n = JOptionPane.showOptionDialog(mFrame,
								"Approximate Nurbs/3D from beziers?",
								"Nurbs/3D", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[1]);

						switch (n) {
						case 0:
							mAlwaysApproximateNurbs = true;
							break;
						case 1:
							break;
						case 3:
							mNeverApproximateNurbs = true;
						case 2:
							return; // break out

						}
					}
					board_handler.approximate_bezier(getCurrentBrd(), false);
					design_panel.update_3d();
					// design_panel.fit_all();
					design_panel.redraw();

				}
*/
			}
		});

		final JMenu pluginMenu = new JMenu(LanguageResource.getString("PLUGINSMENU_STR"));

		final AbstractPluginHandler pluginLoader = new AbstractPluginHandler() {
			static final long serialVersionUID=1L;
			public void onNewPluginMenu(JMenu menu) {
				pluginMenu.add(menu);
			}

			public void onNewPluginComponent(JComponent component) {
				mTabbedPane.add(component);
			}
		};
		//pluginLoader.loadPlugins("plugins");
		if (pluginMenu.getItemCount() > 0) {
			menuBar.add(pluginMenu);
		}

		//mFrame.getContentPane().add(mTabbedPane, BorderLayout.CENTER);

		getPreferences();

		mTabbedPane2 = new JTabbedPane(JTabbedPane.BOTTOM);

		panel=new JPanel();

		panel.setLayout(new BorderLayout());

		panel.add(status_panel, BorderLayout.NORTH);

		mControlPointInfo = new ControlPointInfo();
		panel.add(mControlPointInfo, BorderLayout.EAST);

		mBoardSpec = new BoardSpec();
		panel.add(mBoardSpec, BorderLayout.WEST);


		mTabbedPane2 = new JTabbedPane(JTabbedPane.BOTTOM);
		mTabbedPane2.addTab("Board specification", panel);

//		mFrame.getContentPane().add(panel, BorderLayout.SOUTH);
//		mFrame.getContentPane().add(mTabbedPane2, BorderLayout.SOUTH);
		//mFrame.getContentPane().add(mTabbedPane, BorderLayout.CENTER);		
		mSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT, mTabbedPane, mTabbedPane2);
		mSplitPane.setResizeWeight(1.0);
		mSplitPane.setOneTouchExpandable(true);
		mFrame.getContentPane().add(mSplitPane, BorderLayout.CENTER);
		Dimension mindim=new Dimension(0,0);
		mTabbedPane.setMinimumSize(mindim);
		mTabbedPane2.setMinimumSize(mindim);
		mTabbedPane.setPreferredSize(new Dimension(600,230));
		mTabbedPane2.setPreferredSize(new Dimension(600,230));
		
		//load jython script
		String scriptname="boardcad_init.py";
		File file=new File(scriptname);
    		if (file.exists()) 
    		{
			ScriptLoader.loadScript(scriptname);
		}		
				
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
		.addKeyEventDispatcher(this);


		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width * 9 / 10;
		int height = screenSize.height * 9 / 10;
    	mFrame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);

    	mFrame.setSize(width, height);

		mFrame.setVisible(true);

		edit.actionPerformed(null);

		mWeightCalculatorDialog = new WeightCalculatorDialog();
		mWeightCalculatorDialog.setModal(false);
		mWeightCalculatorDialog.setAlwaysOnTop(true);
		mWeightCalculatorDialog.setVisible(false);

		mGuidePointsDialog = new BoardGuidePointsDialog();
		mGuidePointsDialog.setModal(false);
		mGuidePointsDialog.setAlwaysOnTop(true);
		mGuidePointsDialog.setVisible(false);
		
		//Set current unit after all and everything is initialized
		unitComboBox.setSelectedIndex(1);

		// DEBUG
		/*
		 * mIsPaintingGridMenuItem.setSelected(false);
		 * mIsPaintingOriginalBrdMenuItem.setSelected(false);
		 * mIsPaintingGhostBrdMenuItem.setSelected(false);
		 * mIsPaintingControlPointsMenuItem.setSelected(true);
		 * mIsPaintingNonActiveCrossSectionsMenuItem.setSelected(false);
		 * mIsPaintingGuidePointsMenuItem.setSelected(false);
		 * mIsPaintingCurvatureMenuItem.setSelected(false);
		 * mIsPaintingSlidingInfoMenuItem.setSelected(true);
		 * mIsPaintingSlidingCrossSectionMenuItem.setSelected(false);
		 * mIsPaintingFinsMenuItem.setSelected(false);
		 * mIsPaintingBackgroundImageMenuItem.setSelected(true);
		 * mIsAntialiasingMenuItem.setSelected(false);
		 * BrdReader.loadFile(getCurrentBrd(),
		 * DefaultBrds.getInstance().getBoardArray("Funboard"), "funboard");
		 * mOriginalBrd.set(getCurrentBrd()); fitAll(); onBrdChanged();
		 *
		 * getSelectedEdit().loadBackgroundImage("F:\\Gfx\\Misc\\Surfboards\\horan
		 * 6'8 keelboard outline.jpg");
		 */
		
		mBlockGUI = false;
	}

	public void actionPerformed(final ActionEvent e) {

		final String cmdStr = e.getActionCommand();
		if (cmdStr == LanguageResource.getString("PRINTOUTLINE_STR"))
		{
			CategorizedSettings settings = new CategorizedSettings();
			String categoryName = LanguageResource.getString("PRINTOUTLINEPARAMETERSCATEGORY_STR");
			Settings printOutlineSettings = settings.addCategory(categoryName);
			printOutlineSettings.addBoolean("PrintGrid", true, LanguageResource.getString("PRINTGRID_STR"));
			printOutlineSettings.addBoolean("OverCurve", false, LanguageResource.getString("PRINTOVERCURVE_STR"));
			SettingDialog settingsDialog = new SettingDialog(settings);
			settingsDialog.setTitle(LanguageResource.getString("PRINTOUTLINEPARAMETERSTITLE_STR"));
			settingsDialog.setModal(true);
			settingsDialog.setVisible(true);
			settingsDialog.dispose();
			if (settingsDialog.wasCancelled()) {
				return;
			}

			mPrintBrd.printOutline(printOutlineSettings.getBoolean("PrintGrid"),printOutlineSettings.getBoolean("OverCurve"));

		}
		else if (cmdStr == LanguageResource.getString("PRINTSPINTEMPLATE_STR"))
		{
			CategorizedSettings settings = new CategorizedSettings();
			String categoryName = LanguageResource.getString("PRINTSPINTEMPLATEPARAMETERSCATEGORY_STR");
			Settings printOutlineSettings = settings.addCategory(categoryName);
			printOutlineSettings.addBoolean("PrintGrid", true, LanguageResource.getString("PRINTGRID_STR"));
			printOutlineSettings.addBoolean("OverCurve", false, LanguageResource.getString("OVERCURVE_STR"));
			SettingDialog settingsDialog = new SettingDialog(settings);
			settingsDialog.setTitle(LanguageResource.getString("PRINTSPINTEMPLATEPARAMETERSTITLE_STR"));
			settingsDialog.setModal(true);
			settingsDialog.setVisible(true);
			settingsDialog.dispose();
			if (settingsDialog.wasCancelled()) {
				return;
			}

			mPrintBrd.printSpinTemplate(printOutlineSettings.getBoolean("PrintGrid"),printOutlineSettings.getBoolean("OverCurve"));
		}
		else if (cmdStr == LanguageResource.getString("PRINTPROFILE_STR")) {
			mPrintBrd.printProfile();
		} else if (cmdStr == LanguageResource.getString("PRINTCROSSECTION_STR")) {
			mPrintBrd.printSlices();
//		} else if (cmdStr == LanguageResource.getString("PRINTSPECSHEET_STR")) {
//			mPrintBrd.printSpecSheet();
//		} else if (cmdStr == LanguageResource.getString("VIEW3D_STR")) {
//			design_panel.view_3d();
//		} else if (cmdStr == LanguageResource.getString("EDITNURBS_STR")) {
//			design_panel.view_all();
//			design_panel.fit_all();
		} else if (cmdStr == LanguageResource.getString("SIMPLEEDITING_STR")) {
			board_handler.set_single_point_editing(false);
			//design_panel.fit_all();
			redraw();
		} else if (cmdStr == LanguageResource.getString("ADVANCEDEDITING_STR")) {
			board_handler.set_single_point_editing(true);
			//design_panel.fit_all();
			redraw();
		}

	}

	public void itemStateChanged(final ItemEvent e) {

		mFrame.repaint();

	}

	public boolean dispatchKeyEvent(final KeyEvent e) {
		if (mControlPointInfo != null && mControlPointInfo.isEditing())
			return false;

		if (mGuidePointsDialog != null && mGuidePointsDialog.isVisible() && mGuidePointsDialog.isFocused())
			return false;

		if (mWeightCalculatorDialog != null && mWeightCalculatorDialog.isVisible() && mWeightCalculatorDialog.isFocused())
			return false;

		BoardEdit edit = getSelectedEdit();
		if(edit == null)
			return false;
		
		if(this.getFrame().getFocusOwner() == null)
			return false;
		
//		System.out.printf("dispatchKeyEvent() event %s\n",e.toString());

		switch (e.getKeyCode()) {
		case KeyEvent.VK_ADD:
			if (e.getID() == KeyEvent.KEY_PRESSED) 
			{
				mNextCrossSection.actionPerformed(null);
			}
			break;

		case KeyEvent.VK_SUBTRACT:
			if (e.getID() == KeyEvent.KEY_PRESSED) 
			{
				mPreviousCrossSection.actionPerformed(null);
			}
			break;

		case KeyEvent.VK_G:
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.isControlDown())
					break;

				if(mGhostMode == false)
				{
					mGhostMode = true;
					if (mPreviousCommand == null) {
						mPreviousCommand = getCurrentCommand();
						setCurrentCommand(new GhostCommand());
					}
					if(edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if(mGhostMode == true)
				{
					mGhostMode = false;
					if (mPreviousCommand != null) {
						setCurrentCommand(mPreviousCommand);
						mPreviousCommand = null;
					}
					if(edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			}
			return true;

		case KeyEvent.VK_O:

			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.isControlDown())
					break;

				if(mOrgFocus != true)
				{
					mOrgFocus = true;
					if(edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if(mOrgFocus != false)
				{
					mOrgFocus = false;
					if(edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			}
			return true;
		case KeyEvent.VK_ESCAPE:
			setCurrentCommand(new BrdEditCommand());
			if(edit != null)
				edit.repaint();
			break;
		}

		if (isGhostMode()) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if(edit != null)
						edit.mGhostOffsetY -= (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_DOWN:
					if(edit != null)
						edit.mGhostOffsetY += (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_LEFT:
					if(edit != null)
						edit.mGhostOffsetX -= (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_RIGHT:
					if(edit != null)
						edit.mGhostOffsetX += (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_Q:
					if(edit != null)
						edit.mGhostRot -= ((double)Math.PI/180.0f)*(e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_W:
					if(edit != null)
						edit.mGhostRot += ((double)Math.PI/180.0f)*(e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				default:
					return false;
				}
			}

		}
		if (mOrgFocus)
		{
			if (e.getID() == KeyEvent.KEY_PRESSED) {

				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if(edit != null)
						edit.mOriginalOffsetY -= (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_DOWN:
					if(edit != null)
						edit.mOriginalOffsetY += (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_LEFT:
					if(edit != null)
						edit.mOriginalOffsetX -= (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_RIGHT:
					if(edit != null)
						edit.mOriginalOffsetX += (e.isAltDown() ? .1f : 1f)/edit.getScale();
					mFrame.repaint();
					return true;
				default:
					return false;
				}
			}
		}

		if (edit == null)
			return false;

		final BrdInputCommand cmd = (BrdInputCommand) edit.getCurrentCommand();
		if (cmd != null) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_T:
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					if (mPreviousCommand == null) {
						mPreviousCommand = getCurrentCommand();
						setCurrentCommand(new SetImageTailCommand());
					}
				} else if (e.getID() == KeyEvent.KEY_RELEASED) {
					if (mPreviousCommand != null) {
						setCurrentCommand(mPreviousCommand);
						mPreviousCommand = null;
					}
				}
				return true;

			case KeyEvent.VK_N:
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					if (mPreviousCommand == null) {
						mPreviousCommand = getCurrentCommand();
						setCurrentCommand(new SetImageNoseCommand());
					}
				} else if (e.getID() == KeyEvent.KEY_RELEASED) {
					if (mPreviousCommand != null) {
						setCurrentCommand(mPreviousCommand);
						mPreviousCommand = null;
					}
				}
				return true;

			}

			return cmd.onKeyEvent(edit, e);
		} else {
			return false;
		}
	}

	public void toggleBottomAndDeck() {
		switch(mEditDeckorBottom)
		{
		case DECK:
			mEditDeckorBottom = DeckOrBottom.BOTTOM;
			break;
		case BOTTOM:
			mEditDeckorBottom = DeckOrBottom.BOTH;
			break;
		case BOTH:
			mEditDeckorBottom = DeckOrBottom.DECK;
			break;
		}
		mBottomAndDeckEdit.repaint();

		mBottomAndDeckEdit.mSelectedControlPoints.clear();

		redraw();
	}

	public static void main(final String[] args) {
		BoardCAD.getInstance();
	}

	public void drawOutlineFootMarks(final BoardEdit source, final Graphics2D g,
			final Stroke stroke, final BezierBoard brd) {
		
		if(brd.isEmpty())
			return;

		g.setStroke(stroke);

		Point centerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(brd.getLength()/2.0, 0.0));
		Point widthPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0,brd.getMaxWidth()/2));

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		for(int i = 0; i < 7; i++)
		{
			double pos = 0.0;
			String label;

			if(i < 3)
			{
				pos = (i==0)?UnitUtils.INCH:i*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
				label = UnitUtils.convertLengthToCurrentUnit(pos, false);
			}
			else if( i == 3)
			{
				pos = brd.getMaxWidthPos();
				label = "W.P:" + UnitUtils.convertLengthToCurrentUnit(mBoardSpec.isOverCurveSelected()? brd.getBottom().getPointByCurveLength(pos).x - brd.getBottom().getPointByCurveLength(brd.getLength()/2.0).x :
																				  pos - brd.getLength()/2.0,
																				  false);
			}
			else
			{
				pos = (mBoardSpec.isOverCurveSelected()?brd.getBottom().getLength():brd.getLength()) - ((i==6)?UnitUtils.INCH:(6-i)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT);
				label = UnitUtils.convertLengthToCurrentUnit(-((i==6)?UnitUtils.INCH:(6-i)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT), false);
			}

			if(mBoardSpec.isOverCurveSelected())
			{
				pos = brd.getBottom().getPointByCurveLength(pos).x;

				label = label.concat(" O.C");
			}


			double width = brd.getWidthAt(pos);

			String widthStr = UnitUtils.convertLengthToCurrentUnit(width, false);

			g.setColor(Color.BLUE);


			// get the advance of my text in this font and render context
			final int labelWidth = metrics.stringWidth(label);

			// get the advance of my text in this font and render context
			final int widthOfWidthString = metrics.stringWidth(widthStr);

			Point outlinePoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos,width/2.0));
			Point upperOutlinePoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos,-width/2.0));

			g.setColor(Color.BLACK);

			g.drawString(label, outlinePoint.x - (labelWidth/2), centerPoint.y);

			g.setColor(Color.BLUE);

			g.drawLine(outlinePoint.x, upperOutlinePoint.y, outlinePoint.x, outlinePoint.y);

			g.drawString(widthStr, outlinePoint.x - (widthOfWidthString/2), widthPoint.y + hgt);

			g.setColor(Color.DARK_GRAY);

			g.drawLine(outlinePoint.x, outlinePoint.y, outlinePoint.x, widthPoint.y);
		}

	}

	public void drawProfileFootMarks(final BoardEdit source, final Graphics2D g,
			final Stroke stroke, final BezierBoard brd) {

		g.setStroke(stroke);

		Point centerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(brd.getLength()/2.0, 0.0));
		Point maxThicknessPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0,brd.getMaxThickness()));
		Point maxRockerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0,brd.getMaxRocker()));
		Point bottomPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0,0));

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		for(int i = 0; i < 7; i++)
		{
			double pos = 0.0;
			String label;

			if(i < 3)
			{
				pos = (i==0)?0.001:i*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT;
				label = (i==0)?"":UnitUtils.convertLengthToCurrentUnit(pos, false);
			}
			else if( i == 3)
			{
				pos = brd.getLength()/2.0;
				label = "Center: " + UnitUtils.convertLengthToCurrentUnit(mBoardSpec.isOverCurveSelected()? brd.getBottom().getPointByCurveLength(pos).x: pos, false);
			}
			else
			{
				pos = (mBoardSpec.isOverCurveSelected()?brd.getBottom().getLength():brd.getLength()) - ((i==6)?0.005:(6-i)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT);
				label = (i==6)?"":UnitUtils.convertLengthToCurrentUnit(-(6-i)*UnitUtils.INCH*UnitUtils.INCHES_PR_FOOT, false);
			}

			if(mBoardSpec.isOverCurveSelected())
			{
				pos = brd.getBottom().getPointByCurveLength(pos).x;

				if(label != "")
					label = label.concat(" O.C");
			}


			double thickness = brd.getThicknessAtPos(pos);
			double rocker = brd.getRockerAtPos(pos);

			String thicknessStr = UnitUtils.convertLengthToCurrentUnit(thickness, false);
			String rockerStr = UnitUtils.convertLengthToCurrentUnit(rocker, false);

			g.setColor(Color.BLUE);

			// get the advance of my text in this font and render context
			final int labelWidth = metrics.stringWidth(label);

			// get the advance of my text in this font and render context
			final int widthOfThicknessString = metrics.stringWidth(thicknessStr);
			final int widthOfRockerString = metrics.stringWidth(rockerStr);

			Point deckPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos,rocker+thickness));
			Point rockerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos,rocker));

			g.setColor(Color.BLACK);

			g.drawString(label, deckPoint.x - (labelWidth/2), (maxThicknessPoint.y + maxRockerPoint.y)/2);

			g.setColor(Color.RED);

			g.drawString(thicknessStr, deckPoint.x - (widthOfThicknessString/2), bottomPoint.y + hgt);

			g.drawLine(deckPoint.x, deckPoint.y, rockerPoint.x, rockerPoint.y);

			g.setColor(Color.BLUE);

			g.drawString(rockerStr, deckPoint.x - (widthOfRockerString/2), bottomPoint.y + hgt*2);

			g.drawLine(rockerPoint.x, rockerPoint.y, rockerPoint.x, bottomPoint.y);
		}

	}

	public void drawOutlineSlidingInfo(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd) {

		final double width = brd.getWidthAtPos(source.mBrdCoord.x);
		if (width <= 0.0)
			return;

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

		String widthStr = LanguageResource.getString("OUTLINESLIDINGINFOWIDTH_STR");
		source.mSlidingInfoString = widthStr
			+ UnitUtils.convertLengthToCurrentUnit(width, false);

		g.setColor(Color.BLUE);

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		// get the advance of my text in this font and render context
		final int adv = metrics.stringWidth(source.mSlidingInfoString);

		// calculate the size of a box to hold the text with some padding.
		final Dimension size = new Dimension(adv, hgt + 1);
		final Dimension dim = source.getSize();

		// get the advance of my text in this font and render context
		final int advOfWidthStr = metrics.stringWidth(widthStr);

		// calculate the size of a box to hold the text with some padding.
		final Dimension sizeOfWidthStr = new Dimension(advOfWidthStr, hgt + 1);


		int textX = source.mScreenCoord.x - (sizeOfWidthStr.width);
		if (textX < 10)
			textX = 10;

		if (textX + size.width + 10 > dim.width)
			textX = dim.width - size.width - 10;

		if(BoardCAD.getInstance().isPaintingOverCurveMeasurements())
		{
			source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOOVERCURVE_STR");

			g.setColor(Color.BLACK);

			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * 4);



			final double fromNose = brd.getFromNoseOverBottomCurveAtPos(source.mBrdCoord.x);

			final double fromTail = brd.getFromTailOverBottomCurveAtPos(source.mBrdCoord.x);


			source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOFROMTAIL_STR")
				+ UnitUtils.convertLengthToCurrentUnit(fromTail, false);


			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * 3);

			source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOFROMNOSE_STR")
				+(" ")+ UnitUtils.convertLengthToCurrentUnit(fromNose, false);


			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * 2);


		}
		
		if(BoardCAD.getInstance().isPaintingMomentOfInertia())
		{
			final double momentOfInertia = brd.getMomentOfInertia(source.mBrdCoord.x, source.mBrdCoord.y);

			source.mSlidingInfoString = LanguageResource.getString("SLIDINGINFOMOMENTOFINERTIA_STR")
				+ UnitUtils.convertMomentOfInertiaToCurrentUnit(momentOfInertia);

			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * (BoardCAD.getInstance().isPaintingOverCurveMeasurements()?5:2));
		}

		source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOWIDTH_STR")
			+ UnitUtils.convertLengthToCurrentUnit(width, false);

		g.setColor(Color.BLUE);

		g.drawString(source.mSlidingInfoString, textX, dim.height
				- (size.height + 2) * 1);


		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g),
				source.mOffsetX, source.mOffsetY, source.mScale);

		source.mSlidingInfoLine.setLine(source.mBrdCoord.x * mulX, -(width / 2)
				* mulY, source.mBrdCoord.x * mulX, (width / 2) * mulY);


		g.draw(source.mSlidingInfoLine);

		g.setTransform(savedTransform);
	}

	public void drawOutlineCrossections(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd) {

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;


		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g),
				source.mOffsetX, source.mOffsetY, source.mScale);

		Line2D.Double crossSectionLine = new Line2D.Double();  //  @jve:decl-index=0:

		BezierBoardCrossSection currentCrossSection =  brd.getCurrentCrossSection();

		final float[] dashPattern = new float[] { 5.0f, 1.0f };
		final BasicStroke bs = (BasicStroke)stroke;
		final BasicStroke stapled = new BasicStroke((float) (bs
				.getLineWidth() / 2.0), bs.getEndCap(), bs
				.getLineJoin(), bs.getMiterLimit(), dashPattern, 0f);
		final Color noneActiveColor = color.brighter();

		for(int i = 0; i < brd.getCrossSections().size(); i++)
		{
			BezierBoardCrossSection tmp =  brd.getCrossSections().get(i);

			if(tmp == currentCrossSection)
			{
				g.setColor(color);
				g.setStroke(stroke);
			}
			else
			{
				g.setColor(noneActiveColor);
				g.setStroke(stapled);
			}

			double pos = tmp.getPosition();

			final double width = brd.getWidthAtPos(pos);
			if (width <= 0.0)
				continue;

			crossSectionLine.setLine(pos * mulX, -(width / 2) * mulY, pos * mulX, (width / 2) * mulY);

			g.draw(crossSectionLine);
		}

		g.setTransform(savedTransform);
	}

	public void drawProfileSlidingInfo(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd) {

		final double thickness = brd.getThicknessAtPos(source.mBrdCoord.x);
		if (thickness <= 0.0)
			return;

		final double rocker = brd.getRockerAtPos(source.mBrdCoord.x);
		final double curvature = source.getActiveBezierSplines(brd)[0].getCurvatureAt(source.mBrdCoord.x);
		final double radius = 1 / curvature;

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		final Dimension dim = source.getSize();

		String thicknessStr = LanguageResource.getString("PROFILESLIDINGINFOTHICKNESS_STR");
		source.mSlidingInfoString = thicknessStr
			+ UnitUtils.convertLengthToCurrentUnit(thickness, false);


		// get the advance of my text in this font and render context
		final int adv = metrics.stringWidth(source.mSlidingInfoString);


		// calculate the size of a box to hold the text with some padding.
		final Dimension size = new Dimension(adv, hgt + 1);

		// get the advance of my text in this font and render context
		final int advOfThicknessStr = metrics.stringWidth(thicknessStr);


		// calculate the size of a box to hold the text with some padding.
		final Dimension sizeOfThicknessStr = new Dimension(advOfThicknessStr, hgt + 1);

		int textX = source.mScreenCoord.x - (sizeOfThicknessStr.width);
		if (textX < 10)
			textX = 10;

		if (textX + size.width + 10 > dim.width)
			textX = dim.width - size.width - 10;


		g.setColor(Color.BLUE);


		g.drawString(source.mSlidingInfoString, textX, dim.height
				- (size.height + 2) * 3);

		g.setColor(Color.RED);

		source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOROCKER_STR")
			+ UnitUtils.convertLengthToCurrentUnit(rocker, false);

		g.drawString(source.mSlidingInfoString, textX, dim.height
				- (size.height + 2) * 2);


		source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFORADIUS_STR")
			+ UnitUtils.convertLengthToCurrentUnit(radius, true);

		g.setColor(new Color(102,102,102));

		g.drawString(source.mSlidingInfoString, textX, dim.height
				- (size.height + 2) * 1);



		if(BoardCAD.getInstance().isPaintingOverCurveMeasurements())
		{
			source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOOVERCURVE_STR");

			g.setColor(Color.BLACK);

			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * 6);


			final double fromNose = brd.getFromNoseOverBottomCurveAtPos(source.mBrdCoord.x);

			final double fromTail = brd.getFromTailOverBottomCurveAtPos(source.mBrdCoord.x);

			source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOFROMTAIL_STR")
				+ UnitUtils.convertLengthToCurrentUnit(fromTail, false);

			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * 5);


			source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOFROMNOSE_STR")
				+ UnitUtils.convertLengthToCurrentUnit(fromNose, false);

			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * 4);

		}

		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g),
				source.mOffsetX, source.mOffsetY, source.mScale);

		// sets the color of the thickness sliding info bar (inside board)
		g.setColor(Color.BLUE);

		source.mSlidingInfoLine.setLine(source.mBrdCoord.x * mulX, rocker
				* mulY, source.mBrdCoord.x * mulX, (rocker + thickness) * mulY);

		g.draw(source.mSlidingInfoLine);

		// sets the color of the rocker sliding info bar (outside board)
		g.setColor(Color.RED);

		source.mSlidingInfoLine.setLine(source.mBrdCoord.x * mulX, 0 * mulY,
				source.mBrdCoord.x * mulX, rocker * mulY);

		g.draw(source.mSlidingInfoLine);

		g.setTransform(savedTransform);


	}

	public void drawProfileCrossections(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd) {

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;


		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g),
				source.mOffsetX, source.mOffsetY, source.mScale);

		Line2D.Double crossSectionLine = new Line2D.Double();  //  @jve:decl-index=0:

		BezierBoardCrossSection currentCrossSection =  brd.getCurrentCrossSection();

		final float[] dashPattern = new float[] { 5.0f, 1.0f };
		final BasicStroke bs = (BasicStroke)stroke;
		final BasicStroke stapled = new BasicStroke((float) (bs
				.getLineWidth() / 2.0), bs.getEndCap(), bs
				.getLineJoin(), bs.getMiterLimit(), dashPattern, 0f);
		final Color noneActiveColor = color.brighter();

		for(int i = 0; i < brd.getCrossSections().size(); i++)
		{
			BezierBoardCrossSection tmp =  brd.getCrossSections().get(i);

			if(tmp == currentCrossSection)
			{
				g.setColor(color);
				g.setStroke(stroke);
			}
			else
			{
				g.setColor(noneActiveColor);
				g.setStroke(stapled);
			}

			double pos = tmp.getPosition();
			final double deck = brd.getDeckAtPos(pos);
			final double rocker = brd.getRockerAtPos(pos);

			if (deck <= 0.0)
				continue;

			crossSectionLine.setLine(pos * mulX, deck * mulY,
					pos * mulX, rocker * mulY);

			g.draw(crossSectionLine);
		}

		g.setTransform(savedTransform);
	}

	public void drawOutlineFlowlines(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		BezierBoardDrawUtil.paintOutlineFlowLines(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, color, stroke, brd, (source.mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);
	}

	public void drawOutlineTuckUnderLine(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		BezierBoardDrawUtil.paintOutlineTuckUnderLine(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, color, stroke, brd, (source.mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);
	}

	public void drawProfileFlowlines(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		BezierBoardDrawUtil.paintProfileFlowLines(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, color, stroke, brd, (source.mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);
	}

	public void drawProfileApexline(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		BezierBoardDrawUtil.paintProfileApexline(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, color, stroke, brd, (source.mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);
	}

	public void drawProfileTuckUnderLine(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		BezierBoardDrawUtil.paintProfileTuckUnderLine(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, color, stroke, brd, (source.mDrawControl&BezierBoardDrawUtil.FlipX)!=0, true);
	}

	public void drawCrossSectionCenterline(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		boolean isCompensatedForRocker = BoardCAD.getInstance().isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionCenterline(new JavaDraw(g), source.mOffsetX, source.mOffsetY + (isCompensatedForRocker?brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition())*source.mScale:0), source.mScale, color, stroke, brd, true, !isCompensatedForRocker);
	}

	public void drawCrossSectionFlowlines(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		boolean isCompensatedForRocker = BoardCAD.getInstance().isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionFlowLines(new JavaDraw(g), source.mOffsetX, source.mOffsetY + (isCompensatedForRocker?brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition())*source.mScale:0), source.mScale, color, stroke, brd, true, !isCompensatedForRocker);
	}

	public void drawCrossSectionApexline(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		boolean isCompensatedForRocker = BoardCAD.getInstance().isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionApexline(new JavaDraw(g), source.mOffsetX, source.mOffsetY + (isCompensatedForRocker?brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition())*source.mScale:0), source.mScale, color, stroke, brd, true, !isCompensatedForRocker);
	}

	public void drawCrossSectionTuckUnderLine(final BoardEdit source, final Graphics2D g,
			final Color color, final Stroke stroke, final BezierBoard brd)
	{
		boolean isCompensatedForRocker = BoardCAD.getInstance().isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionTuckUnderLine(new JavaDraw(g), source.mOffsetX, source.mOffsetY + (isCompensatedForRocker?brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition())*source.mScale:0), source.mScale, color, stroke, brd, true, !isCompensatedForRocker);
	}

	protected void updateBezier3DModel()
	{
		if(getCurrentBrd().isEmpty())
			return;
		
		double scale = 0.0075;
		Transform3D transform = new Transform3D();
		transform.setScale(scale);
		transform.setTranslation(new Vector3d(-getCurrentBrd().getLength()*scale/2.0,0.0,0.0));
		transform.setRotation(new AxisAngle4d(1.0, 0.0, 0.0, -Math.PI/2.0));
		mScale.setTransform(transform);

		getCurrentBrd().update3DModel(mBezier3DModel);
		mBoardChangedFor3D = false;
	}

	static public Frame findParentFrame(Container container) {
		while (container != null) {
			if (container instanceof Frame) {
				return (Frame) container;
			}

			container = container.getParent();
		}
		return (Frame) null;
	}
}

class BrowserControl {
	/**
	 * Display a file in the system browser. If you want to display a file, you
	 * must include the absolute path name.
	 *
	 * @param url
	 *            the file's url (the url must start with either "http://" or
	 *            "file://").
	 */
	// Used to identify the windows platform.
	private static final String WIN_ID = "Windows";

	// The default system browser under windows.
	private static final String WIN_PATH = "rundll32";

	// The flag to display a url.
	private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

	// The default browser under unix.
	private static final String UNIX_PATH = "netscape";

	// The flag to display a url.
	private static final String UNIX_FLAG = "-remote openURL";

	public static void displayURL(final String url) {
		final boolean windows = isWindowsPlatform();
		String cmd = null;
		try {
			if (windows) {
				// cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
				cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
				final Process p = Runtime.getRuntime().exec(cmd);
			} else {
				// Under Unix, Netscape has to be running for the "-remote"
				// command to work. So, we try sending the command and
				// check for an exit value. If the exit command is 0,
				// it worked, otherwise we need to start the browser.
				// cmd = 'netscape -remote openURL(http://www.javaworld.com)'
				cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
				Process p = Runtime.getRuntime().exec(cmd);
				try {
					// wait for exit code -- if it's 0, command worked,
					// otherwise we need to start the browser up.
					final int exitCode = p.waitFor();
					if (exitCode != 0) {
						// Command failed, start up the browser
						// cmd = 'netscape http://www.javaworld.com'
						cmd = UNIX_PATH + " " + url;
						p = Runtime.getRuntime().exec(cmd);
					}
				} catch (final InterruptedException x) {
					System.err.println("Error bringing up browser, cmd='" + cmd
							+ "'");
					System.err.println("Caught: " + x);
				}
			}
		} catch (final IOException x) {
			// couldn't exec browser
			System.err.println("Could not invoke browser, command=" + cmd);
			System.err.println("Caught: " + x);
		}
	}

	/**
	 * Try to determine whether this application is running under Windows or
	 * some other platform by examing the "os.name" property.
	 *
	 * @return true if this application is running under a Windows OS
	 */
	public static boolean isWindowsPlatform() {
		final String os = System.getProperty("os.name");
		if (os != null && os.startsWith(WIN_ID))
			return true;
		else
			return false;
	}
}

class SetCurrentCommandAction extends AbstractAction {
	static final long serialVersionUID=1L;
	BrdCommand mCommand;

	SetCurrentCommandAction() {

	}

	SetCurrentCommandAction(final BrdCommand command) {
		mCommand = command;
	}

	public void actionPerformed(final ActionEvent event) {
		if (BoardCAD.getInstance().getCurrentCommand() != null) {
			BoardCAD.getInstance().getCurrentCommand().onCurrentChanged();
		}

		BoardCAD.getInstance().setCurrentCommand(mCommand);

		mCommand.onSetCurrent();

		BoardCAD.getInstance().getFrame().repaint();
	}

}

class SetCurrentOneShotCommandAction extends SetCurrentCommandAction {
	static final long serialVersionUID=1L;
	SetCurrentOneShotCommandAction(final BrdCommand command) {
		super(command);
	}

	public void actionPerformed(final ActionEvent event) {
		mCommand.setPreviousCommand(BoardCAD.getInstance().getCurrentCommand());

		super.actionPerformed(event);
	}


}
