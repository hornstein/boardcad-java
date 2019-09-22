package boardcad.gui.jdk;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.vecmath.Point3d;

import board.BezierBoard;
import cadcore.*;
import boardcad.i18n.LanguageResource;

public class WeightCalculatorDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	private JLabel mStringerWidthLabel = null;
	private JLabel mStringerDensityLabel = null;
	private JLabel mFoamDensityLabel = null;
	private JLabel mDeckGlassLabel = null;
	private JLabel mBottomGlassLabel = null;
	private JLabel jLabel = null;
	private JLabel mBottomLapWidtLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel mPlugsAndFinsLabel = null;
	private JLabel jLabel3 = null;
	private JTextField mStringerWidthEdit = null;
	private JTextField mStringerDensityEdit = null;
	private JPanel jPanel = null;
	private JTextField mFoamDensityEdit = null;
	private JTextField mDeckGlassEdit = null;
	private JTextField mDeckLapWidthEdit = null;
	private JTextField mBottomGlassEdit = null;
	private JTextField mBottomLapWidthEdit = null;
	private JTextField mResinRatioEdit = null;
	private JTextField mHotcoatWeightEdit = null;
	private JTextField mPlugsAndFinsWeightEdit = null;
	private JTextField mTotalBoardWeightEdit = null;
	private JTextField mFoamVolumeEdit = null;
	private JTextField mStringerVolumeEdit = null;
	private JTextField mResinWeightEdit = null;
	private JTextField mBottomLapWeightEdit = null;
	private JTextField mBottomGlassWeightEdit = null;
	private JTextField mDeckLapWeightEdit = null;
	private JTextField mDeckGlassWeightEdit = null;
	private JTextField mFoamWeightEdit = null;
	private JTextField mStringerWeightEdit = null;
	private JTextField mDeckAreaEdit = null;
	private JTextField mBottomAreaEdit = null;
	private JTextField mDeckLapAreaEdit = null;
	private JTextField mBottomLapAreaEdit = null;
	private JTextField mGlassWeightEdit = null;

	double mStringerWidth=0.3;
	double mStringerVolume = 0.0;
	double mStringerDensity = 0.4;
	double mStringerWeight = 0.0;
	double mFoamVolume = 0.0;
	double mFoamDensity = 0.045;
	double mFoamWeight = 0.0;
	double mDeckGlassArea = 0.0;
	double mDeckGlassUnitWeight = 0.270;	//Not really correct terminology, but it does the job
	double mDeckGlassWeight = 0.0;
	double mDeckLapArea = 0.0;
	double mDeckLapWidth = UnitUtils.INCH;
	double mDeckLapWeight = 0.0;
	double mBottomGlassArea = 0.0;
	double mBottomGlassUnitWeight = 0.135;	//Not really correct terminology, but it does the job
	double mBottomGlassWeight = 0.0;
	double mBottomLapArea = 0.0;
	double mBottomLapWidth = UnitUtils.INCH;
	double mBottomLapWeight = 0.0;
	double mTotalGlassWeight = 0.0;
	double mResinRatio = 1.5;
	double mResinWeight;
	double mHotcoatWeight = 0.5;
	double mPlugsAndFinsWeight = 0.2;
	double mTotalBoardWeight = 0;
	
	public interface Func {
	    public void func();
	}
	
	/**
	 * This method initializes 
	 * 
	 */
	public WeightCalculatorDialog() {
		super();
		setDefaults();
		initialize();
	}
	
	void setDefaults()
	{
		final BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		
		if(brd.getLength() > UnitUtils.FOOT*9.0)
		{
			mStringerWidth=0.8;
			mDeckGlassUnitWeight = 0.400;	
			mBottomGlassUnitWeight = 0.200;	
		}
		if(brd.getLength() > UnitUtils.FOOT*7.0)
		{
			mStringerWidth=0.5;
			mDeckGlassUnitWeight = 0.400;	
			mBottomGlassUnitWeight = 0.200;	
		}
		else{
			mStringerWidth=0.3;
			mDeckGlassUnitWeight = 0.270;
			mBottomGlassUnitWeight = 0.135;
			
		}
		mHotcoatWeight = 0.03*(brd.getLength()/UnitUtils.FOOT);
		
	}
	
	void updateAll()
	{
		mStringerWidthEdit.setText(UnitUtils.convertLengthToCurrentUnit(mStringerWidth, false));
		mStringerDensityEdit.setText(UnitUtils.convertDensityToCurrentUnit(mStringerDensity));
		mFoamDensityEdit.setText(UnitUtils.convertDensityToCurrentUnit(mFoamDensity));
		mDeckGlassEdit.setText(UnitUtils.convertWeightToCurrentUnit(mDeckGlassUnitWeight, true));
		mDeckLapWidthEdit.setText(UnitUtils.convertLengthToCurrentUnit(mDeckLapWidth, false));
		mBottomGlassEdit.setText(UnitUtils.convertWeightToCurrentUnit(mBottomGlassUnitWeight, true));
		mBottomLapWidthEdit.setText(UnitUtils.convertLengthToCurrentUnit(mBottomLapWidth, false));
		mResinRatioEdit.setText(Double.toString(mResinRatio));
		mHotcoatWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mHotcoatWeight, true));
		mPlugsAndFinsWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mPlugsAndFinsWeight, true));
		
		updateStringerVolume();
		updateStringerWeight();
		updateFoamVolume();
		updateFoamWeight();
		updateDeckGlassArea();
		updateDeckLapArea();
		updateDeckLapWeight();
		updateDeckGlassWeight();
		updateBottomGlassArea();
		updateBottomGlassWeight();
		updateBottomLapArea();
		updateBottomLapWeight();
		updateTotalGlassWeight();
		updateResinWeight();
		
		updateTotalWeight();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(465, 400));
        this.setResizable(false);
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(getJContentPane());
        this.setTitle(LanguageResource.getString("WeightCalculatorDialog.0")); //$NON-NLS-1$
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel3 = new JLabel();
			jLabel3.setText(LanguageResource.getString("WeightCalculatorDialog.1")); //$NON-NLS-1$
			jLabel3.setBounds(new Rectangle(30, 330, 123, 16));
			mPlugsAndFinsLabel = new JLabel();
			mPlugsAndFinsLabel.setText(LanguageResource.getString("WeightCalculatorDialog.2")); //$NON-NLS-1$
			mPlugsAndFinsLabel.setBounds(new Rectangle(30, 285, 148, 16));
			jLabel2 = new JLabel();
			jLabel2.setText(LanguageResource.getString("WeightCalculatorDialog.3")); //$NON-NLS-1$
			jLabel2.setBounds(new Rectangle(30, 255, 108, 16));
			jLabel1 = new JLabel();
			jLabel1.setText(LanguageResource.getString("WeightCalculatorDialog.4")); //$NON-NLS-1$
			jLabel1.setBounds(new Rectangle(30, 225, 136, 16));
			mBottomLapWidtLabel = new JLabel();
			mBottomLapWidtLabel.setText(LanguageResource.getString("WeightCalculatorDialog.5")); //$NON-NLS-1$
			mBottomLapWidtLabel.setBounds(new Rectangle(30, 195, 108, 16));
			jLabel = new JLabel();
			jLabel.setText(LanguageResource.getString("WeightCalculatorDialog.6")); //$NON-NLS-1$
			jLabel.setBounds(new Rectangle(30, 135, 89, 16));
			mBottomGlassLabel = new JLabel();
			mBottomGlassLabel.setText(LanguageResource.getString("WeightCalculatorDialog.7")); //$NON-NLS-1$
			mBottomGlassLabel.setBounds(new Rectangle(30, 165, 86, 16));
			mDeckGlassLabel = new JLabel();
			mDeckGlassLabel.setText(LanguageResource.getString("WeightCalculatorDialog.8")); //$NON-NLS-1$
			mDeckGlassLabel.setBounds(new Rectangle(30, 105, 93, 16));
			mFoamDensityLabel = new JLabel();
			mFoamDensityLabel.setText(LanguageResource.getString("WeightCalculatorDialog.9")); //$NON-NLS-1$
			mFoamDensityLabel.setBounds(new Rectangle(30, 75, 117, 16));
			mStringerDensityLabel = new JLabel();
			mStringerDensityLabel.setText(LanguageResource.getString("WeightCalculatorDialog.10")); //$NON-NLS-1$
			mStringerDensityLabel.setBounds(new Rectangle(30, 45, 103, 16));
			mStringerWidthLabel = new JLabel();
			mStringerWidthLabel.setText(LanguageResource.getString("WeightCalculatorDialog.11")); //$NON-NLS-1$
			mStringerWidthLabel.setBounds(new Rectangle(30, 15, 106, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			jContentPane.add(mStringerWidthLabel, null);
			jContentPane.add(mStringerDensityLabel, null);
			jContentPane.add(mFoamDensityLabel, null);
			jContentPane.add(mDeckGlassLabel, null);
			jContentPane.add(mBottomGlassLabel, null);
			jContentPane.add(jLabel, null);
			jContentPane.add(mBottomLapWidtLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(mPlugsAndFinsLabel, null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getMStringerWidthEdit(), null);
			jContentPane.add(getMStringerDensity(), null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getMFoamDensityEdit(), null);
			jContentPane.add(getMDeckGlassEdit(), null);
			jContentPane.add(getMDeckLapWidthEdit(), null);
			jContentPane.add(getMBottomGlassEdit(), null);
			jContentPane.add(getMBottomLapWidth(), null);
			jContentPane.add(getMResinRatioEdit(), null);
			jContentPane.add(getMHotcoatWeightEdit(), null);
			jContentPane.add(getMPlugsAndFinsWeightEdit(), null);
			jContentPane.add(getMTotalBoardWeightEdit(), null);
			jContentPane.add(getMFoamVolumeEdit(), null);
			jContentPane.add(getMStringerVolumeEdit(), null);
			jContentPane.add(getMResinWeightEdit(), null);
			jContentPane.add(getMBottomLapWeight(), null);
			jContentPane.add(getMBottomGlassWeightEdit(), null);
			jContentPane.add(getMDeckLapWeightEdit(), null);
			jContentPane.add(getMDeckGlassWeightEdit(), null);
			jContentPane.add(getMFoamWeightEdit(), null);
			jContentPane.add(getMStringerWeightEdit(), null);
			jContentPane.add(getMDeckAreaEdit(), null);
			jContentPane.add(getMBottomAreaEdit(), null);
			jContentPane.add(getMDeckLapAreaEdit(), null);
			jContentPane.add(getMBottomLapAreaEdit(), null);
			jContentPane.add(getMGlassWeight(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes mStringerWidthEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMStringerWidthEdit() {
		if (mStringerWidthEdit == null) {
			mStringerWidthEdit = new JTextField();
			mStringerWidthEdit.setBounds(new Rectangle(165, 15, 76, 20));
			mStringerWidthEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.13")); //$NON-NLS-1$
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mStringerWidth = UnitUtils.convertInputStringToInternalLengthUnit(mStringerWidthEdit.getText());
					mStringerWidthEdit.setText(UnitUtils.convertLengthToCurrentUnit(mStringerWidth, false));
					updateStringerVolume();
					updateStringerWeight();
					updateFoamVolume();
					updateFoamWeight();
				};
			};
			mStringerWidthEdit.addFocusListener(adapt);			
			mStringerWidthEdit.addActionListener((e) -> {adapt.focusLost(null);});

		}
		return mStringerWidthEdit;
	}

	/**
	 * This method initializes mStringerDensity	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMStringerDensity() {
		if (mStringerDensityEdit == null) {
			mStringerDensityEdit = new JTextField();
			mStringerDensityEdit.setBounds(new Rectangle(255, 45, 76, 20));
			mStringerDensityEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.14")); //$NON-NLS-1$
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mStringerDensity = UnitUtils.convertInputStringToInternalDensityUnit(mStringerDensityEdit.getText());
					mStringerDensityEdit.setText(UnitUtils.convertDensityToCurrentUnit(mStringerDensity));
					updateStringerWeight();
				}
			};
			mStringerDensityEdit.addFocusListener(adapt);			
			mStringerDensityEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mStringerDensityEdit;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBounds(new Rectangle(30, 315, 391, 8));
			jPanel.setBorder(BorderFactory.createLineBorder(Color.black, 5));
			jPanel.setPreferredSize(new Dimension(0, 1));
		}
		return jPanel;
	}

	/**
	 * This method initializes mFoamDensityEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMFoamDensityEdit() {
		if (mFoamDensityEdit == null) {
			mFoamDensityEdit = new JTextField();
			mFoamDensityEdit.setBounds(new Rectangle(255, 75, 76, 20));
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mFoamDensity = UnitUtils.convertInputStringToInternalDensityUnit(mFoamDensityEdit.getText());
					mFoamDensityEdit.setText(UnitUtils.convertDensityToCurrentUnit(mFoamDensity));
					updateFoamWeight();
				}
			};
			mFoamDensityEdit.addFocusListener(adapt);			
			mFoamDensityEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mFoamDensityEdit;
	}

	/**
	 * This method initializes mDeckGlassEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMDeckGlassEdit() {
		if (mDeckGlassEdit == null) {
			mDeckGlassEdit = new JTextField();
			mDeckGlassEdit.setBounds(new Rectangle(255, 105, 76, 20));
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mDeckGlassUnitWeight = UnitUtils.convertInputStringToInternalWeightUnit(mDeckGlassEdit.getText());
					mDeckGlassEdit.setText(UnitUtils.convertWeightToCurrentUnit(mDeckGlassUnitWeight, true));
					updateDeckGlassWeight();
					updateTotalGlassWeight();
					updateResinWeight();
				}
			};
			mDeckGlassEdit.addFocusListener(adapt);			
			mDeckGlassEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mDeckGlassEdit;
	}

	/**
	 * This method initializes mDeckLapWidthEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMDeckLapWidthEdit() {
		if (mDeckLapWidthEdit == null) {
			mDeckLapWidthEdit = new JTextField();
			mDeckLapWidthEdit.setBounds(new Rectangle(255, 135, 76, 20));
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mDeckLapWidth = UnitUtils.convertInputStringToInternalLengthUnit(mDeckLapWidthEdit.getText());
					mDeckLapWidthEdit.setText(UnitUtils.convertLengthToCurrentUnit(mDeckLapWidth, true));
					updateDeckLapArea();
					updateDeckLapWeight();
					updateTotalGlassWeight();
					updateResinWeight();
				}
			};
			mDeckLapWidthEdit.addFocusListener(adapt);			
			mDeckLapWidthEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mDeckLapWidthEdit;
	}

	/**
	 * This method initializes mBottomGlassEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMBottomGlassEdit() {
		if (mBottomGlassEdit == null) {
			mBottomGlassEdit = new JTextField();
			mBottomGlassEdit.setBounds(new Rectangle(255, 165, 76, 20));
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mBottomGlassUnitWeight = UnitUtils.convertInputStringToInternalWeightUnit(mBottomGlassEdit.getText());
					mBottomGlassEdit.setText(UnitUtils.convertWeightToCurrentUnit(mBottomGlassUnitWeight, true));
					updateBottomGlassWeight();
					updateTotalGlassWeight();
					updateResinWeight();
				}
			};
			mBottomGlassEdit.addFocusListener(adapt);			
			mBottomGlassEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mBottomGlassEdit;
	}

	/**
	 * This method initializes mBottomLapWidth	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMBottomLapWidth() {
		if (mBottomLapWidthEdit == null) {
			mBottomLapWidthEdit = new JTextField();
			mBottomLapWidthEdit.setBounds(new Rectangle(255, 195, 76, 20));
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mBottomLapWidth = UnitUtils.convertInputStringToInternalLengthUnit(mBottomLapWidthEdit.getText());
					mBottomLapWidthEdit.setText(UnitUtils.convertLengthToCurrentUnit(mBottomLapWidth, true));
					updateBottomLapArea();
					updateBottomLapWeight();
					updateTotalGlassWeight();
					updateResinWeight();
				}
			};
			mBottomLapWidthEdit.addFocusListener(adapt);			
			mBottomLapWidthEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mBottomLapWidthEdit;
	}

	/**
	 * This method initializes mResinRatioEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMResinRatioEdit() {
		if (mResinRatioEdit == null) {
			mResinRatioEdit = new JTextField();
			mResinRatioEdit.setBounds(new Rectangle(255, 225, 76, 20));
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mResinRatio = Double.valueOf(mResinRatioEdit.getText());
					mResinRatioEdit.setText(Double.toString(mResinRatio));
					updateResinWeight();
				}
			};
			mResinRatioEdit.addFocusListener(adapt);			
			mResinRatioEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mResinRatioEdit;
	}

	/**
	 * This method initializes mHotcoatWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMHotcoatWeightEdit() {
		if (mHotcoatWeightEdit == null) {
			mHotcoatWeightEdit = new JTextField();
			mHotcoatWeightEdit.setBounds(new Rectangle(345, 255, 76, 20));
			mHotcoatWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.15")); //$NON-NLS-1$
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mHotcoatWeight = UnitUtils.convertInputStringToInternalWeightUnit(mHotcoatWeightEdit.getText());
					mHotcoatWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mHotcoatWeight, true));
					
					updateTotalWeight();
				}
			};
			mHotcoatWeightEdit.addFocusListener(adapt);			
			mHotcoatWeightEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mHotcoatWeightEdit;
	}

	/**
	 * This method initializes mPlugsAndFinsWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMPlugsAndFinsWeightEdit() {
		if (mPlugsAndFinsWeightEdit == null) {
			mPlugsAndFinsWeightEdit = new JTextField();
			mPlugsAndFinsWeightEdit.setBounds(new Rectangle(345, 285, 76, 20));
			mPlugsAndFinsWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.16")); //$NON-NLS-1$
			java.awt.event.FocusAdapter adapt = new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) {
					mPlugsAndFinsWeight = UnitUtils.convertInputStringToInternalWeightUnit(mPlugsAndFinsWeightEdit.getText());
					mPlugsAndFinsWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mPlugsAndFinsWeight, true));

					updateTotalWeight();
				}
			};
			mPlugsAndFinsWeightEdit.addFocusListener(adapt);			
			mPlugsAndFinsWeightEdit.addActionListener((e) -> {adapt.focusLost(null);});
		}
		return mPlugsAndFinsWeightEdit;
	}

	/**
	 * This method initializes mTotalBoardWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMTotalBoardWeightEdit() {
		if (mTotalBoardWeightEdit == null) {
			mTotalBoardWeightEdit = new JTextField();
			mTotalBoardWeightEdit.setBounds(new Rectangle(345, 330, 76, 20));
			mTotalBoardWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.17")); //$NON-NLS-1$
			mTotalBoardWeightEdit.setEditable(false);
		}
		return mTotalBoardWeightEdit;
	}

	/**
	 * This method initializes mFoamVolumeEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMFoamVolumeEdit() {
		if (mFoamVolumeEdit == null) {
			mFoamVolumeEdit = new JTextField();
			mFoamVolumeEdit.setBounds(new Rectangle(165, 75, 76, 20));
			mFoamVolumeEdit.setEditable(false);
		}
		return mFoamVolumeEdit;
	}

	/**
	 * This method initializes mStringerVolumeEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMStringerVolumeEdit() {
		if (mStringerVolumeEdit == null) {
			mStringerVolumeEdit = new JTextField();
			mStringerVolumeEdit.setBounds(new Rectangle(165, 45, 76, 20));
			mStringerVolumeEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.18")); //$NON-NLS-1$
			mStringerVolumeEdit.setEditable(false);
		}
		return mStringerVolumeEdit;
	}

	/**
	 * This method initializes mResinWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMResinWeightEdit() {
		if (mResinWeightEdit == null) {
			mResinWeightEdit = new JTextField();
			mResinWeightEdit.setBounds(new Rectangle(345, 225, 76, 20));
			mResinWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.19")); //$NON-NLS-1$
			mResinWeightEdit.setEditable(false);
		}
		return mResinWeightEdit;
	}

	/**
	 * This method initializes mBottomLapWeight	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMBottomLapWeight() {
		if (mBottomLapWeightEdit == null) {
			mBottomLapWeightEdit = new JTextField();
			mBottomLapWeightEdit.setBounds(new Rectangle(345, 195, 76, 20));
			mBottomLapWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.20")); //$NON-NLS-1$
			mBottomLapWeightEdit.setEditable(false);
		}
		return mBottomLapWeightEdit;
	}

	/**
	 * This method initializes mBottomGlassWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMBottomGlassWeightEdit() {
		if (mBottomGlassWeightEdit == null) {
			mBottomGlassWeightEdit = new JTextField();
			mBottomGlassWeightEdit.setBounds(new Rectangle(345, 165, 76, 20));
			mBottomGlassWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.21")); //$NON-NLS-1$
			mBottomGlassWeightEdit.setEditable(false);
		}
		return mBottomGlassWeightEdit;
	}

	/**
	 * This method initializes mDeckLapWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMDeckLapWeightEdit() {
		if (mDeckLapWeightEdit == null) {
			mDeckLapWeightEdit = new JTextField();
			mDeckLapWeightEdit.setBounds(new Rectangle(345, 135, 76, 20));
			mDeckLapWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.22")); //$NON-NLS-1$
			mDeckLapWeightEdit.setEditable(false);
		}
		return mDeckLapWeightEdit;
	}

	/**
	 * This method initializes mDeckGlassWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMDeckGlassWeightEdit() {
		if (mDeckGlassWeightEdit == null) {
			mDeckGlassWeightEdit = new JTextField();
			mDeckGlassWeightEdit.setBounds(new Rectangle(345, 105, 76, 20));
			mDeckGlassWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.23")); //$NON-NLS-1$
			mDeckGlassWeightEdit.setEditable(false);
		}
		return mDeckGlassWeightEdit;
	}

	/**
	 * This method initializes mFoamWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMFoamWeightEdit() {
		if (mFoamWeightEdit == null) {
			mFoamWeightEdit = new JTextField();
			mFoamWeightEdit.setBounds(new Rectangle(345, 75, 76, 20));
			mFoamWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.24")); //$NON-NLS-1$
			mFoamWeightEdit.setEditable(false);
		}
		return mFoamWeightEdit;
	}

	/**
	 * This method initializes mStringerWeightEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMStringerWeightEdit() {
		if (mStringerWeightEdit == null) {
			mStringerWeightEdit = new JTextField();
			mStringerWeightEdit.setBounds(new Rectangle(345, 45, 76, 20));
			mStringerWeightEdit.setToolTipText(LanguageResource.getString("WeightCalculatorDialog.25")); //$NON-NLS-1$
			mStringerWeightEdit.setEditable(false);
		}
		return mStringerWeightEdit;
	}

	/**
	 * This method initializes mDeckAreaEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMDeckAreaEdit() {
		if (mDeckAreaEdit == null) {
			mDeckAreaEdit = new JTextField();
			mDeckAreaEdit.setBounds(new Rectangle(165, 105, 76, 20));
			mDeckAreaEdit.setEditable(false);
		}
		return mDeckAreaEdit;
	}

	/**
	 * This method initializes mBottomAreaEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMBottomAreaEdit() {
		if (mBottomAreaEdit == null) {
			mBottomAreaEdit = new JTextField();
			mBottomAreaEdit.setBounds(new Rectangle(165, 165, 76, 20));
			mBottomAreaEdit.setEditable(false);
		}
		return mBottomAreaEdit;
	}

	/**
	 * This method initializes mDeckLapAreaEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMDeckLapAreaEdit() {
		if (mDeckLapAreaEdit == null) {
			mDeckLapAreaEdit = new JTextField();
			mDeckLapAreaEdit.setBounds(new Rectangle(165, 135, 76, 20));
			mDeckLapAreaEdit.setEditable(false);
		}
		return mDeckLapAreaEdit;
	}

	/**
	 * This method initializes mBottomLapAreaEdit	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMBottomLapAreaEdit() {
		if (mBottomLapAreaEdit == null) {
			mBottomLapAreaEdit = new JTextField();
			mBottomLapAreaEdit.setBounds(new Rectangle(165, 195, 76, 20));
			mBottomLapAreaEdit.setEditable(false);
		}
		return mBottomLapAreaEdit;
	}

	/**
	 * This method initializes mGlassWeight	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMGlassWeight() {
		if (mGlassWeightEdit == null) {
			mGlassWeightEdit = new JTextField();
			mGlassWeightEdit.setBounds(new Rectangle(165, 225, 76, 20));
			mGlassWeightEdit.setEditable(false);
		}
		return mGlassWeightEdit;
	}
	
	void calcStringerVolume()
	{
		final BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(brd == null || brd.isEmpty())
			return;
		
		double stringerArea = brd.getDeck().getIntegral(0, brd.getLength(), 20) - 
								brd.getBottom().getIntegral(0, brd.getLength(), 20);
		
		mStringerVolume = stringerArea*mStringerWidth;		
	}

	void calcStringerWeight()
	{
		mStringerWeight = (mStringerVolume/UnitUtils.CUBICCENTIMETER_PR_LITRE)*mStringerDensity;
	}

	void calcFoamVolume()
	{
		final BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(brd == null || brd.isEmpty())
			return;
		
		double brdVolume = brd.getVolume();
		
		mFoamVolume = brdVolume - mStringerVolume;
	}

	void calcFoamWeight()
	{
		mFoamWeight = (mFoamVolume/UnitUtils.CUBICCENTIMETER_PR_LITRE)*mFoamDensity;				
	}
	
	void calcDeckGlassArea()
	{
		final BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(brd == null || brd.isEmpty())
			return;

	    MathUtils.Function curveLenghtFunc = new MathUtils.Function(){
		    @Override
			public double f(final double x)
		    {
			    MathUtils.FunctionXY deckFunc = new MathUtils.FunctionXY(){
			    	@Override
					public Point2D.Double f(double s){
			    		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(BoardCAD.getInstance().getCrossSectionInterpolationType()).getPointAt(brd,x,s,-90.0,90.0,true); 
			    		return new Point2D.Double(point.y,point.z);
			    	}
			    };
			    
			    double curveLength = MathUtils.CurveLength.getCurveLength(deckFunc, 0.0, 1.0);
			    return curveLength;
		    }
		};
		
		mDeckGlassArea =  MathUtils.Integral.getIntegral(curveLenghtFunc, BezierSpline.ZERO, brd.getLength()-BezierSpline.ZERO, BezierBoard.AREA_SPLITS)*2.0;
		
	}

	void calcDeckGlassWeight()
	{
		mDeckGlassWeight = (mDeckGlassArea/UnitUtils.SQUARECENTIMETER_PR_METER)*mDeckGlassUnitWeight;				
	}
	
	void calcDeckLapArea()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(brd == null || brd.isEmpty())
			return;
	
		mDeckLapArea = brd.getOutline().getLength()*mDeckLapWidth*2.0;
	}

	void calcDeckLapWeight()
	{		
		mDeckLapWeight = (mDeckLapArea/UnitUtils.SQUARECENTIMETER_PR_METER)*mDeckGlassUnitWeight;				
	}

	void calcBottomGlassArea()
	{
		final BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(brd == null || brd.isEmpty())
			return;

	    MathUtils.Function curveLenghtFunc = new MathUtils.Function(){
		    @Override
			public double f(final double x)
		    {
			    MathUtils.FunctionXY BottomFunc = new MathUtils.FunctionXY(){
			    	@Override
					public Point2D.Double f(double s){
			    		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(BoardCAD.getInstance().getCrossSectionInterpolationType()).getPointAt(brd,x,s,90.0,360.0,true); 
			    		return new Point2D.Double(point.y,point.z);
			    	}
			    };
			    
			    double curveLength = MathUtils.CurveLength.getCurveLength(BottomFunc, 0.0, 1.0);
			    return curveLength;
		    }
		};
		
		mBottomGlassArea =  MathUtils.Integral.getIntegral(curveLenghtFunc, BezierSpline.ZERO, brd.getLength()-BezierSpline.ZERO, BezierBoard.AREA_SPLITS)*2.0;
		
	}

	void calcBottomGlassWeight()
	{
		mBottomGlassWeight = (mBottomGlassArea/UnitUtils.SQUARECENTIMETER_PR_METER)*mBottomGlassUnitWeight;				
	}
	
	void calcBottomLapArea()
	{
		BezierBoard brd = BoardCAD.getInstance().getCurrentBrd();
		if(brd == null || brd.isEmpty())
			return;

		mBottomLapArea = brd.getOutline().getLength()*mBottomLapWidth*2.0;
	}
	
	
	void calcBottomLapWeight()
	{		
		mBottomLapWeight = (mBottomLapArea/UnitUtils.SQUARECENTIMETER_PR_METER)*mBottomGlassUnitWeight;				
	}

	void calcTotalGlassWeight()
	{
		mTotalGlassWeight = mDeckGlassWeight + mDeckLapWeight + mBottomGlassWeight + mBottomLapWeight;
	}

	void calcResinWeight()
	{
		mResinWeight = mTotalGlassWeight*mResinRatio;
	}

	void calcTotalWeight()
	{
		mTotalBoardWeight = mStringerWeight + mFoamWeight + mTotalGlassWeight + mResinWeight + mHotcoatWeight + mPlugsAndFinsWeight;
	}

	void updateStringerVolume()
	{
		calcStringerVolume();
		
		mStringerVolumeEdit.setText(UnitUtils.convertVolumeToCurrentUnit(mStringerVolume));

		updateTotalWeight();
	}

	void updateStringerWeight()
	{
		calcStringerWeight();
		
		mStringerWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mStringerWeight,false));

		updateTotalWeight();
	}

	void updateFoamVolume()
	{
		calcFoamVolume();
		
		mFoamVolumeEdit.setText(UnitUtils.convertVolumeToCurrentUnit(mFoamVolume));

		updateTotalWeight();
	}

	void updateFoamWeight()
	{
		calcFoamWeight();
		
		mFoamWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mFoamWeight,false));

		updateTotalWeight();
	}

	void updateDeckGlassArea()
	{
		calcDeckGlassArea();

		mDeckAreaEdit.setText(UnitUtils.convertAreaToCurrentUnit(mDeckGlassArea));

		updateTotalWeight();
	}

	void updateDeckGlassWeight()
	{
		calcDeckGlassWeight();

		mDeckGlassWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mDeckGlassWeight,false));

		updateTotalWeight();
	}

	void updateDeckLapArea()
	{
		calcDeckLapArea();

		mDeckLapAreaEdit.setText(UnitUtils.convertAreaToCurrentUnit(mDeckLapArea));

		updateTotalWeight();
	}

	void updateDeckLapWeight()
	{
		calcDeckLapWeight();

		mDeckLapWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mDeckLapWeight,false));

		updateTotalWeight();
	}
	void updateBottomGlassArea()
	{
		calcBottomGlassArea();

		mBottomAreaEdit.setText(UnitUtils.convertAreaToCurrentUnit(mBottomGlassArea));

		updateTotalWeight();
	}

	void updateBottomGlassWeight()
	{
		calcBottomGlassWeight();

		mBottomGlassWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mBottomGlassWeight,false));

		updateTotalWeight();
	}

	void updateBottomLapArea()
	{
		calcBottomLapArea();

		mBottomLapAreaEdit.setText(UnitUtils.convertAreaToCurrentUnit(mBottomLapArea));

		updateTotalWeight();
	}

	void updateBottomLapWeight()
	{
		calcBottomLapWeight();

		mBottomLapWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mBottomLapWeight,false));

		updateTotalWeight();
	}

	void updateTotalGlassWeight()
	{
		calcTotalGlassWeight();

		mGlassWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mTotalGlassWeight,false));

		updateTotalWeight();
	}

	void updateResinWeight()
	{
		calcResinWeight();

		mResinWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mResinWeight,false));

		updateTotalWeight();
	}

	void updateTotalWeight()
	{
		calcTotalWeight();

		mTotalBoardWeightEdit.setText(UnitUtils.convertWeightToCurrentUnit(mTotalBoardWeight,false));
	}

}  //  @jve:decl-index=0:visual-constraint="14,18"
