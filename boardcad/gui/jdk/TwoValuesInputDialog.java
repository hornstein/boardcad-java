package boardcad.gui.jdk;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import cadcore.UnitUtils;
import boardcad.i18n.LanguageResource;

public class TwoValuesInputDialog extends JDialog {

	private static final int MEASUREMENT = 1;
	private static final int INTEGER = 2;
	private static final int DOUBLE = 3;


	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel MessageText = null;

	private JLabel Value1Label = null;

	private JButton OkButton = null;

	private JButton CancelButton = null;

	private JTextField Value1TextField = null;

	private JTextField Value2TextField = null;

	private JLabel Value2Label = null;

	private boolean mWasCancelled = true;

	private int mType = MEASUREMENT;

	/**
	 * @param owner
	 */
	public TwoValuesInputDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(265, 198);
		this.setResizable(false);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			Value2Label = new JLabel();
			Value2Label.setBounds(new Rectangle(48, 87, 88, 20));
			Value2Label.setText("JLabel");
			Value1Label = new JLabel();
			Value1Label.setBounds(new Rectangle(48, 52, 89, 20));
			Value1Label.setText("JLabel");
			MessageText = new JLabel();
			MessageText.setText("JLabel");
			MessageText.setFont(new Font("Dialog", Font.BOLD, 14));
			MessageText.setHorizontalAlignment(SwingConstants.CENTER);
			MessageText.setBounds(new Rectangle(3, 11, 250, 16));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(MessageText, null);
			jContentPane.add(Value1Label, null);
			jContentPane.add(getOkButton(), null);
			jContentPane.add(getCancelButton(), null);
			jContentPane.add(getValue1TextField(), null);
			jContentPane.add(getValue2TextField(), null);
			jContentPane.add(Value2Label, null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes OkButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (OkButton == null) {
			OkButton = new JButton();
			OkButton.setBounds(new Rectangle(21, 139, 106, 25));
			OkButton.setText(LanguageResource.getString("OKBUTTON_STR") );
			OkButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					mWasCancelled = false;
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
			CancelButton.setBounds(new Rectangle(135, 139, 106, 25));
			CancelButton.setText(LanguageResource.getString("CANCELBUTTON_STR") );
			CancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return CancelButton;
	}

	/**
	 * This method initializes Value1TextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getValue1TextField() {
		if (Value1TextField == null) {
			Value1TextField = new JTextField();
			Value1TextField.setBounds(new Rectangle(139, 52, 80, 20));
		}
		return Value1TextField;
	}

	/**
	 * This method initializes Value2TextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getValue2TextField() {
		if (Value2TextField == null) {
			Value2TextField = new JTextField();
			Value2TextField.setBounds(new Rectangle(139, 87, 80, 20));
		}
		return Value2TextField;
	}

	void setMessageText(String text)
	{
		MessageText.setText(text);
	}

	void setValue1LabelText(String text)
	{
		Value1Label.setText(text);
	}

	void setValue2LabelText(String text)
	{
		Value2Label.setText(text);
	}

	void setValue1(double value)
	{
		Value1TextField.setText(UnitUtils.convertLengthToCurrentUnit(value, true));
	}

	void setValue2(double value)
	{
		Value2TextField.setText(UnitUtils.convertLengthToCurrentUnit(value, true));
	}

	void setValue1(int value)
	{
		mType = INTEGER;
		Value1TextField.setText(Integer.toString(value));
	}

	void setValue2(int value)
	{
		mType = INTEGER;
		Value2TextField.setText(Integer.toString(value));
	}

	double getValue1()
	{
		String val = Value1TextField.getText();

		switch(mType)
		{
		default:
		case MEASUREMENT:
			return UnitUtils.convertInputStringToInternalLengthUnit(val);
		case DOUBLE:
			return Double.parseDouble(val);
		case INTEGER:
			return (double)Integer.parseInt(val);
		}
	}

	double getValue2()
	{
		String val = Value2TextField.getText();

		switch(mType)
		{
		default:
		case MEASUREMENT:
			return UnitUtils.convertInputStringToInternalLengthUnit(val);
		case DOUBLE:
			return Double.parseDouble(val);
		case INTEGER:
			return (double)Integer.parseInt(val);
		}
	}

	boolean wasCancelled()
	{
		return mWasCancelled;

	}

}  //  @jve:decl-index=0:visual-constraint="10,10"