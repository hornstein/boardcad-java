package boardcad.gui.jdk;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import board.BezierBoard;

import cadcore.BezierSpline;
import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;

public class BoardSpec extends JPanel {

	private static final long serialVersionUID = 1L;
	private JRadioButton mOverCurveRadioButton = null;
	private JRadioButton mStraightLineRadioButton = null;
	private JButton mScaleButton = null;
	private JLabel Widepoint = null;
	private JLabel Thickpoint = null;	
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel ThicknessAtCenter = null;
	private JLabel RockerAtNose = null;
	private JLabel jLabel5 = null;
	private JLabel RockerAtTail = null;
	private JLabel Length = null;
	private JLabel jLabel8 = null;
	private JLabel jLabel9 = null;
	private JLabel WidthAtNose = null;
	private JLabel ThicknessAtNose = null;
	private JLabel RockerAtNose1 = null;
	private JLabel RockerAtTail1 = null;
	private JLabel LengthOverCurve = null;
	private JLabel WidthAtTail = null;
	private JLabel ThicknessAtTail = null;
	private JLabel RockerAtNose2 = null;
	private JLabel RockerAtTail2 = null;
	private JLabel Area = null;
	private JLabel Volume = null;
	private java.util.Timer mTimer;
	private java.util.Timer mIntegralTimer;
	private JLabel centerWidth = null;
	
	/**
	 * This is the default constructor
	 */
	public BoardSpec() {
		super();
		initialize();
	}


	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {

		Font table = new Font("Arial",Font.PLAIN,13);
		Font header = new Font("Arial",Font.BOLD,14);
		Font volAndArea = new Font ("Arial", Font.BOLD,12);
		
		// Center width measurement
		GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
		gridBagConstraints18.gridx = 2;
		gridBagConstraints18.gridy = 4;
		gridBagConstraints18.ipadx = 0;
		gridBagConstraints18.ipady = 0;
		gridBagConstraints18.gridwidth = 1;
		gridBagConstraints18.gridheight = 1;
		//gridBagConstraints18.weightx = 1;
		gridBagConstraints18.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints18.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints18.insets = new Insets(2, 30, 0, 0);
		centerWidth = new JLabel();
		centerWidth.setFont(table);
		centerWidth.setText(LanguageResource.getString("CENTERWIDTH_STR"));

		// Volume measurement
		GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
		gridBagConstraints42.gridx = 1;
		gridBagConstraints42.gridy = 5;
		gridBagConstraints42.ipadx = 0;
		gridBagConstraints42.ipady = 0;
		gridBagConstraints42.gridwidth = 1;
		gridBagConstraints42.gridheight = 1;
		//gridBagConstraints42.weightx = 1;
		gridBagConstraints42.anchor = GridBagConstraints.PAGE_START;
		gridBagConstraints42.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints42.insets = new Insets(15, 0, 5, 20);
		Volume = new JLabel();
		Volume.setFont(volAndArea);
		Volume.setText(LanguageResource.getString("VOLUME_STR"));

		// Area measurement
		GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
		gridBagConstraints32.gridx = 3;
		gridBagConstraints32.gridy = 5;
		gridBagConstraints32.ipadx = 0;
		gridBagConstraints32.ipady = 0;
		gridBagConstraints32.gridwidth = 1;
		gridBagConstraints32.gridheight = 1;
		//gridBagConstraints32.weightx = 1;
		gridBagConstraints32.anchor = GridBagConstraints.PAGE_START;
		gridBagConstraints32.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints32.insets = new Insets(15, 0, 5, 0);
		Area = new JLabel();
		Area.setFont(volAndArea);
		Area.setText(LanguageResource.getString("AREA_STR"));

		// Rocker Tail(2') measurement
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridx = 5;
		gridBagConstraints41.gridy = 3;
		gridBagConstraints41.ipadx = 0;
		gridBagConstraints41.ipady = 0;
		gridBagConstraints41.gridwidth = 1;
		gridBagConstraints41.gridheight = 1;
		//gridBagConstraints41.weightx = 1;
		gridBagConstraints41.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints41.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints41.insets = new Insets(2, 5, 0, 0);
		RockerAtTail2 = new JLabel();
		RockerAtTail2.setFont(table);
		RockerAtTail2.setText(LanguageResource.getString("TWOFEETFROMTAILROCKER_STR"));

		// Rocker Nose(2') measurement
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		gridBagConstraints31.gridx = 4;
		gridBagConstraints31.gridy = 3;
		gridBagConstraints31.ipadx = 0;
		gridBagConstraints31.ipady = 0;
		gridBagConstraints31.gridwidth = 1;
		gridBagConstraints31.gridheight = 1;
		//gridBagConstraints31.weightx = 1;
		gridBagConstraints31.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints31.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints31.insets = new Insets(2, 5, 0, 0);
		RockerAtNose2 = new JLabel();
		RockerAtNose2.setFont(table);
		RockerAtNose2.setText(LanguageResource.getString("TWOFEETFROMNOSEROCKER_STR"));

		// Thickness Tail(1') measurement
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 3;
		gridBagConstraints21.gridy = 3;
		gridBagConstraints21.ipadx = 0;
		gridBagConstraints21.ipady = 0;
		gridBagConstraints21.gridwidth = 1;
		gridBagConstraints21.gridheight = 1;
		//gridBagConstraints21.weightx = 1;
		gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints21.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints21.insets = new Insets(2, 0, 0, 0);
		ThicknessAtTail = new JLabel();
		ThicknessAtTail.setFont(table);
		ThicknessAtTail.setText(LanguageResource.getString("FOOTFROMTAILTHICKNESS_STR"));

		// Width Tail(1') measurement
		GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
		gridBagConstraints16.gridx = 2;
		gridBagConstraints16.gridy = 3;
		gridBagConstraints16.ipadx = 0;
		gridBagConstraints16.ipady = 0;
		gridBagConstraints16.gridwidth = 1;
		gridBagConstraints16.gridheight = 1;
		//gridBagConstraints16.weightx = 1;
		gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints16.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints16.insets = new Insets(2, 30, 2, 0);
		WidthAtTail = new JLabel();
		WidthAtTail.setFont(table);
		WidthAtTail.setText(LanguageResource.getString("TAILWIDTH_STR"));

		// Length Over Curve measurement
		GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
		gridBagConstraints15.gridx = 1;
		gridBagConstraints15.gridy = 3;
		gridBagConstraints15.ipadx = 0;
		gridBagConstraints15.ipady = 0;
		gridBagConstraints15.gridwidth = 1;
		gridBagConstraints15.gridheight = 1;
		//gridBagConstraints15.weightx = 1;
		gridBagConstraints15.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints15.insets = new Insets(0, 0, 0, 0);
		LengthOverCurve = new JLabel();
		LengthOverCurve.setFont(table);
		LengthOverCurve.setText(LanguageResource.getString("OVERCURVE_STR"));

		// Rocker Tail(1') measurement
		GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
		gridBagConstraints14.gridx = 5;
		gridBagConstraints14.gridy = 2;
		gridBagConstraints14.ipadx = 0;
		gridBagConstraints14.ipady = 0;
		gridBagConstraints14.gridwidth = 1;
		gridBagConstraints14.gridheight = 1;
		//gridBagConstraints14.weightx = 1;
		gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints14.anchor = GridBagConstraints.WEST;
		gridBagConstraints14.insets = new Insets(2, 5, 2, 20);
		RockerAtTail1 = new JLabel();
		RockerAtTail1.setFont(table);
		RockerAtTail1.setText(LanguageResource.getString("FOOTFROMTAILROCKER_STR"));

		// Rocker Nose(1') measurement
		GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
		gridBagConstraints13.gridx = 4;
		gridBagConstraints13.gridy = 2;
		gridBagConstraints13.ipadx = 0;
		gridBagConstraints13.ipady = 0;
		gridBagConstraints13.gridwidth = 1;
		gridBagConstraints13.gridheight = 1;
		//gridBagConstraints13.weightx = 1;
		gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints13.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints13.insets = new Insets(2, 5, 2, 35);
		RockerAtNose1 = new JLabel();
		RockerAtNose1.setFont(table);
		RockerAtNose1.setText(LanguageResource.getString("FOOTFROMNOSEROCKER_STR"));

		// Thickness Nose(1') measurement
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridx = 3;
		gridBagConstraints12.gridy = 2;
		gridBagConstraints12.ipadx = 0;
		gridBagConstraints12.ipady = 0;
		gridBagConstraints12.gridwidth = 1;
		gridBagConstraints12.gridheight = 1;
		//gridBagConstraints12.weightx = 1;
		gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints12.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints12.insets = new Insets(2, 0, 2, 15);
		ThicknessAtNose = new JLabel();
		ThicknessAtNose.setFont(table);
		ThicknessAtNose.setText(LanguageResource.getString("FOOTFROMNOSETHICKNESS_STR"));

		// Width Nose(1') measurement
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 2;
		gridBagConstraints11.gridy = 2;
		gridBagConstraints11.ipadx = 0;
		gridBagConstraints11.ipady = 0;
		gridBagConstraints11.gridwidth = 1;
		gridBagConstraints11.gridheight = 1;
		//gridBagConstraints11.weightx = 1;
		gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints11.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints11.insets = new Insets(2, 30, 2, 0);
		WidthAtNose = new JLabel();
		WidthAtNose.setFont(table);
		WidthAtNose.setText(LanguageResource.getString("NOSEWIDTH_STR"));

		// Length Straigh Line measurement
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		gridBagConstraints10.gridx = 1;
		gridBagConstraints10.gridy = 2;
		gridBagConstraints10.insets = new Insets(0, 0, 2, 0);
		gridBagConstraints10.ipadx = 0;
		gridBagConstraints10.ipady = 0;
		gridBagConstraints10.gridwidth = 1;
		gridBagConstraints10.gridheight = 1;
		//gridBagConstraints10.weightx = 1;
		gridBagConstraints10.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
		Length = new JLabel();
		Length.setFont(table);
		Length.setText(LanguageResource.getString("STRAIGHTLINE_STR"));

		// Rocker at tail measurement
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		gridBagConstraints9.gridx = 5;
		gridBagConstraints9.gridy = 1;
		gridBagConstraints9.ipadx = 0;
		gridBagConstraints9.ipady = 0;
		gridBagConstraints9.gridwidth = 1;
		gridBagConstraints9.gridheight = 1;
		//gridBagConstraints9.weightx = 1;
		gridBagConstraints9.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints9.insets = new Insets(0, 5, 2, 20);
		RockerAtTail = new JLabel();
		RockerAtTail.setFont(table);
		RockerAtTail.setText(LanguageResource.getString("TAILROCKER_STR"));

		// Rocker at Nose measurement
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 4;
		gridBagConstraints8.gridy = 1;
		gridBagConstraints8.ipadx = 0;
		gridBagConstraints8.ipady = 0;
		gridBagConstraints8.gridwidth = 1;
		gridBagConstraints8.gridheight = 1;
		//gridBagConstraints8.weightx = 1;
		gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints8.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints8.insets = new Insets(0, 5, 2, 0);
		RockerAtNose = new JLabel();
		RockerAtNose.setFont(table);
		RockerAtNose.setText(LanguageResource.getString("NOSEROCKER_STR"));

		// Thickness at center measurement
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridx = 3;
		gridBagConstraints7.gridy = 4;
		gridBagConstraints7.ipadx = 0;
		gridBagConstraints7.ipady = 0;
		gridBagConstraints7.gridwidth = 1;
		gridBagConstraints7.gridheight = 1;
		//gridBagConstraints7.weightx = 1;
		gridBagConstraints7.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints7.insets = new Insets(2, 0, 2, 0);
		ThicknessAtCenter = new JLabel();
		ThicknessAtCenter.setFont(table);
		ThicknessAtCenter.setText(LanguageResource.getString("CENTERTHICKNESS_STR"));

		// Widepoint measurement
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 2;
		gridBagConstraints6.gridy = 1;
		gridBagConstraints6.ipadx = 0;
		gridBagConstraints6.ipady = 0;
		gridBagConstraints6.gridwidth = 1;
		gridBagConstraints6.gridheight = 1;
		//gridBagConstraints6.weightx = 1;
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints6.insets = new Insets(0, 30, 2, 15);
		Widepoint = new JLabel();
		Widepoint.setFont(table);
		Widepoint.setText(LanguageResource.getString("WIDEPOINT_STR"));

		// Thickpoint measurement
		GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
		gridBagConstraints28.gridx = 3;
		gridBagConstraints28.gridy = 1;
		gridBagConstraints28.ipadx = 0;
		gridBagConstraints28.ipady = 0;
		gridBagConstraints28.gridwidth = 1;
		gridBagConstraints28.gridheight = 1;
		//gridBagConstraints6.weightx = 1;
		gridBagConstraints28.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints28.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints28.insets = new Insets(0, 0, 4, 15);
		Thickpoint = new JLabel();
		Thickpoint.setFont(table);
		Thickpoint.setText(LanguageResource.getString("THICKPOINT_STR"));

		// Length
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.gridx = 1;
		gridBagConstraints5.gridy = 1;
		gridBagConstraints5.ipadx = 0;
		gridBagConstraints5.ipady = 0;
		gridBagConstraints5.gridwidth = 1;
		gridBagConstraints5.gridheight = 1;
		//gridBagConstraints5.weightx = 1;
		gridBagConstraints5.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints5.insets = new Insets(0, 0, 4, 0);
		jLabel9 = new JLabel();
		jLabel9.setFont(header);
		jLabel9.setText(LanguageResource.getString("LENGTHHEADER_STR"));

		// Tail Rocker HEADER
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 5;
		gridBagConstraints4.gridy = 0;
		gridBagConstraints4.ipadx = 0;
		gridBagConstraints4.ipady = 0;
		gridBagConstraints4.gridwidth = 1;
		gridBagConstraints4.gridheight = 1;
		//gridBagConstraints4.weightx = 1;
		gridBagConstraints4.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.insets = new Insets(5, 5, 10, 100);
		jLabel8 = new JLabel();
		jLabel8.setFont(header);
		jLabel8.setText(LanguageResource.getString("TAILROCKERHEADER_STR"));

		// Nose Rocker HEADER
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 4;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.ipadx = 0;
		gridBagConstraints3.ipady = 0;
		gridBagConstraints3.gridwidth = 1;
		gridBagConstraints3.gridheight = 1;
		//gridBagConstraints3.weightx = 1;
		gridBagConstraints3.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints3.insets = new Insets(5, 5, 10, 50);
		jLabel1 = new JLabel();
		jLabel1.setFont(header);
		jLabel1.setText(LanguageResource.getString("NOSEROCKERHEADER_STR"));

		// Thickness HEADER
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 3;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.ipadx = 0;
		gridBagConstraints2.ipady = 0;
		gridBagConstraints2.gridwidth = 1;
		gridBagConstraints2.gridheight = 1;
		//gridBagConstraints2.weightx = 1;
		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints2.insets = new Insets(5, 0, 10, 50);
		jLabel5 = new JLabel();
		jLabel5.setFont(header);
		jLabel5.setText(LanguageResource.getString("THICKNESSHEADER_STR"));

		// Width HEADER
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.ipadx = 0;
		gridBagConstraints1.ipady = 0;
		gridBagConstraints1.gridwidth = 1;
		gridBagConstraints1.gridheight = 1;
		//gridBagConstraints1.weightx = 1;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints1.insets = new Insets(5, 30, 10, 90);
		jLabel2 = new JLabel();
		jLabel2.setFont(header);
		jLabel2.setText(LanguageResource.getString("WIDTHHEADER_STR"));

		// Scale BUTTON
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = -4;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		//gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new Insets(6, 0, 5, 25);

		// Over Curve Radio Button
		GridBagConstraints gridBagConstraints01 = new GridBagConstraints();
		gridBagConstraints01.gridx = 0;
		gridBagConstraints01.gridy = 3;
		gridBagConstraints01.ipadx = 0;
		gridBagConstraints01.ipady = -4;
		gridBagConstraints01.gridwidth = 1;
		gridBagConstraints01.gridheight = 1;
		//gridBagConstraints.weightx = 1;
		gridBagConstraints01.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints01.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints01.insets = new Insets(2, 3, 5, 0);

		// Straight Line Radio Button
		GridBagConstraints gridBagConstraints00 = new GridBagConstraints();
		gridBagConstraints00.gridx = 0;
		gridBagConstraints00.gridy = 2;
		gridBagConstraints00.ipadx = 0;
		gridBagConstraints00.ipady = -4;
		gridBagConstraints00.gridwidth = 1;
		gridBagConstraints00.gridheight = 1;
		//gridBagConstraints.weightx = 1;
		gridBagConstraints00.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints00.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints00.insets = new Insets(2, 3, 5, 0);


		this.setLayout(new GridBagLayout());
		this.setSize(745, 140);
		this.add(getOverCurveRadioButton(), gridBagConstraints01);
		this.add(getStraightLineRadioButton(), gridBagConstraints00);
		this.add(getMScaleButton(), gridBagConstraints);
		this.add(jLabel2, gridBagConstraints1);
		this.add(jLabel5, gridBagConstraints2);
		this.add(jLabel1, gridBagConstraints3);
		this.add(jLabel8, gridBagConstraints4);
		this.add(jLabel9, gridBagConstraints5);
		this.add(Widepoint, gridBagConstraints6);
		this.add(ThicknessAtCenter, gridBagConstraints7);
		this.add(RockerAtNose, gridBagConstraints8);
		this.add(RockerAtTail, gridBagConstraints9);
		this.add(Length, gridBagConstraints10);
		this.add(WidthAtNose, gridBagConstraints11);
		this.add(ThicknessAtNose, gridBagConstraints12);
		this.add(RockerAtNose1, gridBagConstraints13);
		this.add(RockerAtTail1, gridBagConstraints14);
		this.add(LengthOverCurve, gridBagConstraints15);
		this.add(WidthAtTail, gridBagConstraints16);
		this.add(ThicknessAtTail, gridBagConstraints21);
		this.add(RockerAtNose2, gridBagConstraints31);
		this.add(RockerAtTail2, gridBagConstraints41);
		this.add(Area, gridBagConstraints32);
		this.add(Volume, gridBagConstraints42);
		this.add(centerWidth, gridBagConstraints18);
		this.add(Thickpoint, gridBagConstraints28);


				final ButtonGroup choiceButtonGroup = new ButtonGroup();
							choiceButtonGroup.add(getOverCurveRadioButton());
							choiceButtonGroup.add(getStraightLineRadioButton());
			getStraightLineRadioButton().setSelected(true);

	}

	/**
	 * This method initializes mScaleButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getMScaleButton() {
		if (mScaleButton == null) {
			mScaleButton = new JButton();
			mScaleButton.setText( LanguageResource.getString("SCALEBUTTON_STR"));
			mScaleButton.setToolTipText(LanguageResource.getString("SCALEBUTTONTOOLTIP_STR"));
			mScaleButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					BrdScaleCommand scaleCmd = new BrdScaleCommand(BoardCAD.getInstance().getSelectedEdit());
					scaleCmd.execute();
				}
			});
		}
		return mScaleButton;
	}


private JRadioButton getOverCurveRadioButton() {
				if (mOverCurveRadioButton == null) {
					mOverCurveRadioButton = new JRadioButton();
					mOverCurveRadioButton.setText("");
					mOverCurveRadioButton.setToolTipText(LanguageResource.getString("OVERCURVERADIOBUTTONTOOLTIP_STR"));
					mOverCurveRadioButton.addActionListener(new java.awt.event.ActionListener() {
						
						public void actionPerformed(java.awt.event.ActionEvent e) {
							mOverCurveRadioButton.setSelected(true);
							setVisible(true);
							updateMeasurements();
							BoardCAD.getInstance().getFrame().repaint();
						}
					});
				}
				return mOverCurveRadioButton;
	}

	private JRadioButton getStraightLineRadioButton() {
					if (mStraightLineRadioButton == null) {
						mStraightLineRadioButton = new JRadioButton();
						mStraightLineRadioButton.setToolTipText(LanguageResource.getString("STRAIGHTLINERADIOBUTTONTOOLTIP_STR"));
						mStraightLineRadioButton.addActionListener(new java.awt.event.ActionListener() {
							
							public void actionPerformed(java.awt.event.ActionEvent e) {
								mStraightLineRadioButton.setSelected(true);
								setVisible(true);
								updateMeasurements();
								BoardCAD.getInstance().getFrame().repaint();
							}
						});
					}
				return mStraightLineRadioButton;
}



	public void updateInfo()
	{

		if(mTimer == null)
		{
			mTimer = new java.util.Timer();
			mTimer.schedule(new TimerTask(){
				public void run()
				{
					updateMeasurements();

					mTimer = null;
				}
			}, 200);

		}

		if(mIntegralTimer != null)
		{
			mIntegralTimer.cancel();
			mIntegralTimer = null;
		}

		mIntegralTimer = new java.util.Timer();
		mIntegralTimer.schedule(new TimerTask(){
			public void run()
			{
				updateAreaAndVolume();
				
				mIntegralTimer = null;
			}
		}, 500);

	}
	
	public void updateInfoInstantly()
	{
		updateMeasurements();
		updateAreaAndVolume();
	}

	public boolean isOverCurveSelected()
	{
		return mOverCurveRadioButton.isSelected();
	}
	
	void updateMeasurements()
	{
		BoardHandler board_handler=BoardCAD.getInstance().getBoardHandler();	
		if(!board_handler.is_empty())
		{
			Length.setText(LanguageResource.getString("STRAIGHTLINE_STR") + " " + UnitUtils.convertLengthToCurrentUnit(board_handler.get_board_length()/10, true));
			
			return;
		}
			
		BezierBoard brd = BoardCAD.getInstance().getFocusedBoard();		
		if(brd.isEmpty())
		{
			return;
		}

		Length.setText(LanguageResource.getString("STRAIGHTLINE_STR") + " " + UnitUtils.convertLengthToCurrentUnit(brd.getLength(), true));
		LengthOverCurve.setText(LanguageResource.getString("OVERCURVE_STR") + " " + UnitUtils.convertLengthToCurrentUnit(brd.getLengthOverCurve(), true));

		double foot = 12*UnitUtils.INCH;
		double centerPos = brd.getLength()/2.0;
		double widepointPos = brd.getMaxWidthPos() -(brd.getLength()/2.0);
		double thickpointPos = brd.getMaxThicknessPos() -(brd.getLength()/2.0);
		double oneFootFromNosePos = brd.getLength()-foot;
		double oneFootFromTailPos = foot;
		double nosePos=brd.getLength()-0.005;
		double tailPos=0.001;
		double twoFeetFromNosePos = brd.getLength()-(2*foot);
		double twoFeetFromTailPos = 2*foot;
		
		if(mOverCurveRadioButton.isSelected())
		{
			BezierSpline bottom = brd.getBottom();
			
			centerPos =  bottom.getPointByCurveLength(brd.getLengthOverCurve()/2.0).x;
			widepointPos =  bottom.getLengthByX(brd.getMaxWidthPos()) - (brd.getLengthOverCurve()/2.0);
			thickpointPos = bottom.getLengthByX(brd.getMaxThicknessPos()) - (brd.getLengthOverCurve()/2.0);
			oneFootFromNosePos =  bottom.getPointByCurveLength(brd.getLengthOverCurve()-foot).x;
			oneFootFromTailPos =  bottom.getPointByCurveLength(foot).x;
			twoFeetFromNosePos =  bottom.getPointByCurveLength(brd.getLengthOverCurve()-(2*foot)).x;
			twoFeetFromTailPos =  bottom.getPointByCurveLength(2*foot).x;
		}
		
		centerWidth.setText(centerWidth.getText().substring(0, centerWidth.getText().indexOf(":")+1) + "     " + UnitUtils.convertLengthToCurrentUnit(brd.getWidthAt(centerPos), false));
		Widepoint.setText(Widepoint.getText().substring(0, Widepoint.getText().indexOf(":")+1) + "      " + UnitUtils.convertLengthToCurrentUnit(brd.getMaxWidth(), false)+" @ " + UnitUtils.convertLengthToCurrentUnit(widepointPos, false));
		Thickpoint.setText(Thickpoint.getText().substring(0, Thickpoint.getText().indexOf(":")+1) + "      " + UnitUtils.convertLengthToCurrentUnit(brd.getMaxThickness(), false)+" @ " + UnitUtils.convertLengthToCurrentUnit(thickpointPos, false));		
		WidthAtNose.setText(WidthAtNose.getText().substring(0, WidthAtNose.getText().indexOf(":")+1) + "  " + UnitUtils.convertLengthToCurrentUnit(brd.getWidthAtPos(oneFootFromNosePos), false));
		WidthAtTail.setText(WidthAtTail.getText().substring(0, WidthAtTail.getText().indexOf(":")+1) + "     " + UnitUtils.convertLengthToCurrentUnit(brd.getWidthAtPos(oneFootFromTailPos), false));
		ThicknessAtCenter.setText(ThicknessAtCenter.getText().substring(0, ThicknessAtCenter.getText().indexOf(":")+1) + "     " + UnitUtils.convertLengthToCurrentUnit(brd.getThicknessAtPos(centerPos), false));
		ThicknessAtNose.setText(ThicknessAtNose.getText().substring(0, ThicknessAtNose.getText().indexOf(":")+1) + "  " + UnitUtils.convertLengthToCurrentUnit(brd.getThicknessAtPos(oneFootFromNosePos), false));
		ThicknessAtTail.setText(ThicknessAtTail.getText().substring(0, ThicknessAtTail.getText().indexOf(":")+1) + "     " + UnitUtils.convertLengthToCurrentUnit(brd.getThicknessAtPos(oneFootFromTailPos), false));
		RockerAtNose.setText(RockerAtNose.getText().substring(0, RockerAtNose.getText().indexOf(":")+1) + "       " + UnitUtils.convertLengthToCurrentUnit(brd.getRockerAtPos(nosePos), false));
		RockerAtTail.setText(RockerAtTail.getText().substring(0, RockerAtTail.getText().indexOf(":")+1) + "       " + UnitUtils.convertLengthToCurrentUnit(brd.getRockerAtPos(tailPos), false));
		RockerAtNose1.setText(RockerAtNose1.getText().substring(0, RockerAtNose1.getText().indexOf(":")+1) + "  " + UnitUtils.convertLengthToCurrentUnit(brd.getRockerAtPos(oneFootFromNosePos), false));
		RockerAtNose2.setText(RockerAtNose2.getText().substring(0, RockerAtNose2.getText().indexOf(":")+1) + "  " + UnitUtils.convertLengthToCurrentUnit(brd.getRockerAtPos(twoFeetFromNosePos), false));
		RockerAtTail1.setText(RockerAtTail1.getText().substring(0, RockerAtTail1.getText().indexOf(":")+1) + "  " + UnitUtils.convertLengthToCurrentUnit(brd.getRockerAtPos(oneFootFromTailPos), false));
		RockerAtTail2.setText(RockerAtTail2.getText().substring(0, RockerAtTail2.getText().indexOf(":")+1) + "  " + UnitUtils.convertLengthToCurrentUnit(brd.getRockerAtPos(twoFeetFromTailPos), false));

	}

	void updateAreaAndVolume()
	{
		BezierBoard brd = BoardCAD.getInstance().getFocusedBoard();		
		if(brd.isEmpty())
		{
			return;
		}
		
		Area.setText(Area.getText().substring(0, Area.getText().indexOf(":")+1) + " " + UnitUtils.convertAreaToCurrentUnit(brd.getArea()) );
		Volume.setText(Volume.getText().substring(0, Volume.getText().indexOf(":")+1) + " " + UnitUtils.convertVolumeToCurrentUnit(brd.getVolume()) );
	}
}  //  @jve:decl-index=0:visual-constraint="28,19"
