package boardcad.gui.jdk;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import cadcore.UnitUtils;

import board.BezierBoard;
import boardcad.i18n.LanguageResource;


public class BoardFinsDialog extends JDialog {
	static final long serialVersionUID=1L;

	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JPanel jPanel = null;
	private JLabel jLabel3 = null;
	private JLabel Depth = null;
	private JTextField CenterFront = null;
	private JTextField CenterRear = null;
	private JTextField CenterDepth = null;
	private JTextField FinType = null;
	private JLabel jLabel11 = null;
	private JLabel jLabelY = null;
	private JLabel jLabelX = null;
	private JTextField SideFinsFrontX = null;
	private JTextField SideFinsRearX = null;
	private JTextField SideFinsDepth = null;
	private JTextField SideFinsFrontY = null;
	private JTextField SideFinsRearY = null;
	private JTextField SideFinsSplay = null;
	private JButton OkButton = null;
	private JButton CancelButton = null;
	BezierBoard mBrd;

	private JLabel Splay = null;

	/**
	 * This method initializes
	 *
	 */
	public BoardFinsDialog(BezierBoard brd) {
		super();
		mBrd = brd;
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
        this.setSize(new Dimension(422, 289));
        this.setDefaultCloseOperation(1);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setTitle(LanguageResource.getString("FINSTITLE_STR"));
        this.setContentPane(getJContentPane());
        getFinType().setText(mBrd.getFinType());

//		x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of sidefin, splay angle
        double[] fins = mBrd.getFins();
        getCenterFront().setText(UnitUtils.convertLengthToCurrentUnit(fins[5], false));
		getCenterRear().setText(UnitUtils.convertLengthToCurrentUnit(fins[4], false));
		getCenterDepth().setText(UnitUtils.convertLengthToCurrentUnit(fins[6], false));

		getSideFinsFrontX().setText(UnitUtils.convertLengthToCurrentUnit(fins[2], false));
		getSideFinsFrontY().setText(UnitUtils.convertLengthToCurrentUnit(fins[3], false));
		getSideFinsRearX().setText(UnitUtils.convertLengthToCurrentUnit(fins[0], false));
		getSideFinsRearY().setText(UnitUtils.convertLengthToCurrentUnit(fins[1], false));
		getSideFinsDepth().setText(UnitUtils.convertLengthToCurrentUnit(fins[7], false));
		getSideFinsSplay().setText(UnitUtils.convertLengthToCurrentUnit(fins[8], false));
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel2 = new JLabel();
			jLabel2.setText(LanguageResource.getString("FRONTMARKOFFINS_STR"));
			jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			jLabel2.setBounds(new Rectangle(12, 61, 124, 16));
			jLabel1 = new JLabel();
			jLabel1.setText(LanguageResource.getString("CENTRALFIN_STR"));
			jLabel1.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setBounds(new Rectangle(123, 5, 120, 16));
			jLabel = new JLabel();
			jLabel.setText(LanguageResource.getString("TYPEOFFINS_STR"));
			jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			jLabel.setBounds(new Rectangle(12, 176, 124, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getOkButton(), null);
			jContentPane.add(getCancelButton(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			Splay = new JLabel();
			Splay.setBounds(new Rectangle(12, 149, 124, 16));
			Splay.setHorizontalAlignment(SwingConstants.RIGHT);
			Splay.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			Splay.setText(LanguageResource.getString("SPLAY_STR"));
			Depth = new JLabel();
			Depth.setText(LanguageResource.getString("DEPTHOFFINS_STR"));
			Depth.setHorizontalAlignment(SwingConstants.RIGHT);
			Depth.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			Depth.setBounds(new Rectangle(12, 121, 124, 16));
			jLabel3 = new JLabel();
			jLabel3.setText(LanguageResource.getString("REARMARKOFFINS_STR"));
			jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel3.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			jLabel3.setBounds(new Rectangle(12, 91, 124, 16));
			jLabelX = new JLabel();
			jLabelX.setText(LanguageResource.getString("XLABLE_STR"));
			jLabelX.setBounds(new Rectangle(279, 36, 15, 16));
			jLabelY = new JLabel();
			jLabelY.setText(LanguageResource.getString("YLABLE_STR"));
			jLabelY.setBounds(new Rectangle(349, 36, 15, 16));
			jLabel11 = new JLabel();
			jLabel11.setText(LanguageResource.getString("LATERALFINS_STR"));
			jLabel11.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel11.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabel11.setBounds(new Rectangle(256, 5, 134, 16));
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(3, 7, 406, 219));
			jPanel.add(jLabel1, null);
			jPanel.add(jLabel2, null);
			jPanel.add(jLabel3, null);
			jPanel.add(Depth, null);
			jPanel.add(getCenterFront(), null);
			jPanel.add(getCenterRear(), null);
			jPanel.add(getCenterDepth(), null);
			jPanel.add(jLabel11, null);
			jPanel.add(jLabelX, null);
			jPanel.add(jLabelY, null);
			jPanel.add(getSideFinsFrontY(), null);
			jPanel.add(getSideFinsFrontX(), null);
			jPanel.add(getSideFinsRearX(), null);
			jPanel.add(getSideFinsRearY(), null);
			jPanel.add(getSideFinsDepth(), null);
			jPanel.add(getSideFinsSplay(), null);
			jPanel.add(Splay, null);
			jPanel.add(jLabel, null);
			jPanel.add(getFinType(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes CenterFront
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCenterFront() {
		if (CenterFront == null) {
			CenterFront = new JTextField();
			CenterFront.setBounds(new Rectangle(147, 61, 70, 16));
		}
		return CenterFront;
	}

	/**
	 * This method initializes CenterRear
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCenterRear() {
		if (CenterRear == null) {
			CenterRear = new JTextField();
			CenterRear.setBounds(new Rectangle(147, 91, 70, 16));
		}
		return CenterRear;
	}

	/**
	 * This method initializes CenterDepth
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCenterDepth() {
		if (CenterDepth == null) {
			CenterDepth = new JTextField();
			CenterDepth.setBounds(new Rectangle(147, 121, 70, 16));
		}
		return CenterDepth;
	}

	/**
	 * This method initializes FinType
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getFinType() {
		if (FinType == null) {
			FinType = new JTextField();
			FinType.setBounds(new Rectangle(145, 176, 243, 16));
		}
		return FinType;
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */

	/**
	 * This method initializes SideFinsFrontX
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getSideFinsFrontX() {
		if (SideFinsFrontX == null) {
			SideFinsFrontX = new JTextField();
			SideFinsFrontX.setBounds(new Rectangle(255, 61, 65, 16));
		}
		return SideFinsFrontX;
	}

	/**
	 * This method initializes SideFinsRearX
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getSideFinsRearX() {
		if (SideFinsRearX == null) {
			SideFinsRearX = new JTextField();
			SideFinsRearX.setBounds(new Rectangle(255, 91, 65, 16));
		}
		return SideFinsRearX;
	}

	/**
	 * This method initializes SideFinsDepth
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getSideFinsDepth() {
		if (SideFinsDepth == null) {
			SideFinsDepth = new JTextField();
			SideFinsDepth.setBounds(new Rectangle(291, 121, 65, 16));
		}
		return SideFinsDepth;
	}

	/**
	 * This method initializes SideFinsFrontY
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getSideFinsFrontY() {
		if (SideFinsFrontY == null) {
			SideFinsFrontY = new JTextField();
			SideFinsFrontY.setBounds(new Rectangle(325, 61, 65, 16));
		}
		return SideFinsFrontY;
	}

	/**
	 * This method initializes SideFinsRearY
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getSideFinsRearY() {
		if (SideFinsRearY == null) {
			SideFinsRearY = new JTextField();
			SideFinsRearY.setBounds(new Rectangle(325, 91, 65, 16));
		}
		return SideFinsRearY;
	}

	/**
	 * This method initializes SideFinsSplay
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getSideFinsSplay() {
		if (SideFinsSplay == null) {
			SideFinsSplay = new JTextField();
			SideFinsSplay.setBounds(new Rectangle(291, 149, 65, 16));
		}
		return SideFinsSplay;
	}

	/**
	 * This method initializes OkButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (OkButton == null) {
			OkButton = new JButton();
			OkButton.setText(LanguageResource.getString("OKBUTTON_STR"));
			OkButton.setBounds(new Rectangle(161, 229, 111, 26));
			OkButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					setVisible(false);
				}
			});
			OkButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
//					x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of sidefin, splay angle
					double[] fins = mBrd.getFins();

					fins[0] = UnitUtils.convertInputStringToInternalLengthUnit(getSideFinsRearX().getText());
					fins[1] = UnitUtils.convertInputStringToInternalLengthUnit(getSideFinsRearY().getText());

					fins[2] = UnitUtils.convertInputStringToInternalLengthUnit(getSideFinsFrontX().getText());
					fins[3] = UnitUtils.convertInputStringToInternalLengthUnit(getSideFinsFrontY().getText());

					fins[4] = UnitUtils.convertInputStringToInternalLengthUnit(getCenterRear().getText());
					fins[5] = UnitUtils.convertInputStringToInternalLengthUnit(getCenterFront().getText());
					fins[6] = UnitUtils.convertInputStringToInternalLengthUnit(getCenterDepth().getText());

					fins[7] = UnitUtils.convertInputStringToInternalLengthUnit(getSideFinsDepth().getText());
					fins[8] = UnitUtils.convertInputStringToInternalLengthUnit(getSideFinsSplay().getText());

					mBrd.setFinType(getFinType().getText());

					setVisible(false);
				}
			});
		}
		return OkButton;
	}

	/**
	 * This method initializes CancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (CancelButton == null) {
			CancelButton = new JButton();
			CancelButton.setText(LanguageResource.getString("CANCELBUTTON_STR"));
			CancelButton.setBounds(new Rectangle(284, 229, 111, 26));
			CancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return CancelButton;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
