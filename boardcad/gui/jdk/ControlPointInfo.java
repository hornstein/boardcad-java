package boardcad.gui.jdk;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import cadcore.BezierKnot;
import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;

public class ControlPointInfo extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JTextField mEndPointX = null;
	private JTextField mEndPointY = null;
	private JTextField mTangent1X = null;
	private JTextField mTangent1Y = null;
	private JTextField mTangent2X = null;
	private JTextField mTangent2Y = null;
	private JCheckBox mContinous = null;
	private JButton SetButton = null;
	private boolean mBlockActions = false;

	BrdEditCommand mCmd;

	private BezierKnot mControlPoint = null;
	private JButton setControlPointVerticalButton = null;
	private JButton setControlPointHorizontalButton = null;
	/**
	 * This is the default constructor
	 */
	public ControlPointInfo() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 0;
		gridBagConstraints21.fill = GridBagConstraints.BOTH;
		gridBagConstraints21.insets = new Insets(3, 0, 3, 7);
		gridBagConstraints21.gridwidth = 1;
		gridBagConstraints21.ipadx = 0;
		gridBagConstraints21.gridy = 3;
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.fill = GridBagConstraints.BOTH;
		gridBagConstraints11.gridwidth = 1;
		gridBagConstraints11.ipadx = 0;
		gridBagConstraints11.insets = new Insets(3, 0, 3, 7);
		gridBagConstraints11.gridheight = 1;
		gridBagConstraints11.gridy = 2;
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		gridBagConstraints9.gridx = 2;
		gridBagConstraints9.ipadx = 6;
		gridBagConstraints9.gridwidth = 0;
		gridBagConstraints9.insets = new Insets(0, 13, 1, 13);
		gridBagConstraints9.fill = GridBagConstraints.NONE;
		gridBagConstraints9.ipady = 0;
		gridBagConstraints9.gridheight = 1;
		gridBagConstraints9.gridy = 4;
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 1;
		gridBagConstraints8.insets = new Insets(0, 0, 0, 2);
		gridBagConstraints8.fill = GridBagConstraints.NONE;
		gridBagConstraints8.gridy = 4;
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints7.gridy = 3;
		gridBagConstraints7.weightx = 0.0;
		gridBagConstraints7.ipadx = 0;
		gridBagConstraints7.insets = new Insets(0, 2, 0, 0);
		gridBagConstraints7.gridx = 2;
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints6.gridy = 3;
		gridBagConstraints6.weightx = 0.0;
		gridBagConstraints6.ipadx = 0;
		gridBagConstraints6.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints6.gridx = 1;
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints5.gridy = 2;
		gridBagConstraints5.weightx = 0.0;
		gridBagConstraints5.ipadx = 0;
		gridBagConstraints5.insets = new Insets(0, 2, 0, 0);
		gridBagConstraints5.gridx = 2;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints4.gridy = 2;
		gridBagConstraints4.weightx = 0.0;
		gridBagConstraints4.ipadx = 0;
		gridBagConstraints4.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints4.gridx = 1;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints3.gridy = 1;
		gridBagConstraints3.weightx = 0.0;
		gridBagConstraints3.ipadx = 0;
		gridBagConstraints3.ipady = 0;
		gridBagConstraints3.insets = new Insets(0, 2, 3, 0);
		gridBagConstraints3.gridwidth = 1;
		gridBagConstraints3.gridx = 2;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.ipadx = 0;
		gridBagConstraints2.insets = new Insets(0, 0, 4, 0);
		gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.ipady = 0;
		gridBagConstraints2.gridwidth = 1;
		gridBagConstraints2.gridx = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints1.ipadx = 0;
		gridBagConstraints1.gridy = 0;
		jLabel1 = new JLabel();
		jLabel1.setText("Y");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints.gridy = 0;
		jLabel = new JLabel();
		jLabel.setText("X");
		this.setSize(269, 148);
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), LanguageResource.getString("CONTROLPOINTINFOTITLE_STR"), TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 10), new Color(51, 51, 51)));
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		this.add(jLabel, gridBagConstraints);
		this.add(jLabel1, gridBagConstraints1);
		this.add(getMEndPointX(), gridBagConstraints2);
		this.add(getMEndPointY(), gridBagConstraints3);
		this.add(getMTangent1X(), gridBagConstraints4);
		this.add(getMTangent1Y(), gridBagConstraints5);
		this.add(getMTangent2X(), gridBagConstraints6);
		this.add(getMTangent2Y(), gridBagConstraints7);
		this.add(getMContinous(), gridBagConstraints8);
		this.add(getSetButton(), gridBagConstraints9);
		this.add(getSetControlPointVerticalButton(), gridBagConstraints11);
		this.add(getSetControlPointHorizontalButton(), gridBagConstraints21);
		setColors();
	}

	void setColors()
	{

		BoardCAD bc = BoardCAD.getInstance();
		getMEndPointX().setBackground(bc.getSelectedCenterControlPointColor());
		getMEndPointY().setBackground(bc.getSelectedCenterControlPointColor());
		getMTangent1X().setBackground(bc.getSelectedTangent1ControlPointColor());
		getMTangent1Y().setBackground(bc.getSelectedTangent1ControlPointColor());
		getMTangent2X().setBackground(bc.getSelectedTangent2ControlPointColor());
		getMTangent2Y().setBackground(bc.getSelectedTangent2ControlPointColor());

	}

	/**
	 * This method initializes mEndPointX
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMEndPointX() {
		if (mEndPointX == null) {
			mEndPointX = new JTextField();
		}
		return mEndPointX;
	}

	/**
	 * This method initializes mEndPointY
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMEndPointY() {
		if (mEndPointY == null) {
			mEndPointY = new JTextField();
		}
		return mEndPointY;
	}

	/**
	 * This method initializes mTangent1X
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMTangent1X() {
		if (mTangent1X == null) {
			mTangent1X = new JTextField();
		}
		return mTangent1X;
	}

	/**
	 * This method initializes mTangent1Y
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMTangent1Y() {
		if (mTangent1Y == null) {
			mTangent1Y = new JTextField();
		}
		return mTangent1Y;
	}

	/**
	 * This method initializes mTangent2X
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMTangent2X() {
		if (mTangent2X == null) {
			mTangent2X = new JTextField();
		}
		return mTangent2X;
	}

	/**
	 * This method initializes mTangent2Y
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getMTangent2Y() {
		if (mTangent2Y == null) {
			mTangent2Y = new JTextField();
		}
		return mTangent2Y;
	}

	/**
	 * This method initializes mContinous
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getMContinous() {
		if (mContinous == null) {
			mContinous = new JCheckBox();
			mContinous.setText(LanguageResource.getString("CONTROLPOINTCONTINOUS_STR"));
			mContinous.setToolTipText(LanguageResource.getString("CONTROLPOINTCONTINOUSTOOLTIP_STR"));
			mContinous.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(mBlockActions)
						return;

					((BrdEditCommand)BoardCAD.getInstance().getCurrentCommand()).setContinous(BoardCAD.getInstance().getSelectedEdit(),   e.getStateChange() == java.awt.event.ItemEvent.SELECTED);
				}
			});
		}
		return mContinous;
	}

	/**
	 * This method initializes SetButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getSetButton()
	{
		if (SetButton == null) {
			SetButton = new JButton();
			SetButton.setText(LanguageResource.getString("CONTROLPOINTSET_STR"));
			SetButton.setToolTipText(LanguageResource.getString("CONTROLPOINTSETTOOLTIP_STR"));
			SetButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(mBlockActions)
						return;

					((BrdEditCommand)BoardCAD.getInstance().getCurrentCommand()).setControlPoint(BoardCAD.getInstance().getSelectedEdit(), getValues());
				}
			});
		}
		return SetButton;
	}

	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		jLabel.setEnabled(enabled);
		jLabel1.setEnabled(enabled);
		getMEndPointX().setEnabled(enabled);
		getMEndPointY().setEnabled(enabled);
		getMTangent1X().setEnabled(enabled);
		getMTangent1Y().setEnabled(enabled);
		getMTangent2X().setEnabled(enabled);
		getMTangent2Y().setEnabled(enabled);
		getMContinous().setEnabled(enabled);
		getSetButton().setEnabled(enabled);
	}

	public void setWhich(int which)
	{
		getMEndPointX().setEnabled((which==0)?true:false);
		getMEndPointY().setEnabled((which==0)?true:false);
		getMTangent1X().setEnabled((which==1)?true:false);
		getMTangent1Y().setEnabled((which==1)?true:false);
		getMTangent2X().setEnabled((which==2)?true:false);
		getMTangent2Y().setEnabled((which==2)?true:false);
	}

	public void setControlPoint(BezierKnot ControlPoint)
	{
		mBlockActions = true;
		mControlPoint = ControlPoint;

		getMEndPointX().setText(UnitUtils.convertLengthToCurrentUnit(mControlPoint.getEndPoint().x, false));
		getMEndPointY().setText(UnitUtils.convertLengthToCurrentUnit(mControlPoint.getEndPoint().y, false));
		getMTangent1X().setText(UnitUtils.convertLengthToCurrentUnit(mControlPoint.getTangentToPrev().x, false));
		getMTangent1Y().setText(UnitUtils.convertLengthToCurrentUnit(mControlPoint.getTangentToPrev().y, false));
		getMTangent2X().setText(UnitUtils.convertLengthToCurrentUnit(mControlPoint.getTangentToNext().x, false));
		getMTangent2Y().setText(UnitUtils.convertLengthToCurrentUnit(mControlPoint.getTangentToNext().y, false));
		getMContinous().setSelected(mControlPoint.isContinous());
		mBlockActions = false;
	}

	public Point2D.Double getValues()
	{
		Point2D.Double point = new Point2D.Double();

		String svx, svy;

		if(getMEndPointX().isEnabled())
		{
			svx = getMEndPointX().getText();
			svy = getMEndPointY().getText();
		}
		else if(getMTangent1X().isEnabled())
		{
			svx = getMTangent1X().getText();
			svy = getMTangent1Y().getText();
		}
		else //if(getMTangent2X().isEnabled())
		{
			svx = getMTangent2X().getText();
			svy = getMTangent2Y().getText();
		}

		point.setLocation(UnitUtils.convertInputStringToInternalLengthUnit(svx), UnitUtils.convertInputStringToInternalLengthUnit(svy));

		return point;
	}

	boolean isEditing()
	{
		return (getMEndPointX().hasFocus() ||
				getMEndPointY().hasFocus() ||
				getMTangent1X().hasFocus() ||
				getMTangent1Y().hasFocus() ||
				getMTangent2X().hasFocus() ||
				getMTangent2Y().hasFocus() );
	}

	/**
	 * This method initializes setControlPointVerticalButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getSetControlPointVerticalButton() {
		if (setControlPointVerticalButton == null) {
			setControlPointVerticalButton = new JButton();
			setControlPointVerticalButton.setText("|");
			setControlPointVerticalButton.setToolTipText(LanguageResource.getString("CONTROLPOINTVERTICALTOOLTIP_STR"));
			setControlPointVerticalButton.setMnemonic(KeyEvent.VK_UNDEFINED);
			setControlPointVerticalButton.setHorizontalTextPosition(SwingConstants.CENTER);
			setControlPointVerticalButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							BrdCommand cmd = BoardCAD.getInstance().getCurrentCommand();

							if(cmd.getClass().getSimpleName().compareTo("BrdEditCommand") == 0)
							{
								((BrdEditCommand)cmd).rotateControlPointToVertical(BoardCAD.getInstance().getSelectedEdit());

							}
						}
					});
		}
		return setControlPointVerticalButton;
	}

	/**
	 * This method initializes setControlPointHorizontalButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getSetControlPointHorizontalButton() {
		if (setControlPointHorizontalButton == null) {
			setControlPointHorizontalButton = new JButton();
			setControlPointHorizontalButton.setText("__");
			setControlPointHorizontalButton.setToolTipText(LanguageResource.getString("CONTROLPOINTHORIZONTALTOOLTIP_STR"));
			setControlPointHorizontalButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e)
						{
							BrdCommand cmd = BoardCAD.getInstance().getCurrentCommand();

							if(cmd.getClass().getSimpleName().compareTo("BrdEditCommand") == 0)
							{
								((BrdEditCommand)cmd).rotateControlPointToHorizontal(BoardCAD.getInstance().getSelectedEdit());

							}
						}
					});
		}
		return setControlPointHorizontalButton;
	}

}  //  @jve:decl-index=0:visual-constraint="73,6"