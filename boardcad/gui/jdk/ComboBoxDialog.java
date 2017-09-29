package boardcad.gui.jdk;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import boardcad.i18n.LanguageResource;

public class ComboBoxDialog extends JDialog {
		

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JLabel MessageText = null;

	private JButton OkButton = null;

	private JButton CancelButton = null;

	private boolean mWasCancelled = true;
	
	private JComboBox mComboBox = null;

	/**
	 * @param owner
	 */
	public ComboBoxDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(273, 201);
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
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints3.gridwidth = 2;
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.ipadx = 165;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.insets = new Insets(11, 15, 12, 9);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(12, 3, 10, 4);
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.ipadx = 60;
			gridBagConstraints2.ipady = 15;
			gridBagConstraints2.gridwidth = 1;
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.insets = new Insets(12, 45, 10, 2);
			gridBagConstraints1.gridy = 2;
			gridBagConstraints1.ipadx = 60;
			gridBagConstraints1.ipady = 15;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(7, 14, 11, 9);
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 152;
			gridBagConstraints.ipady = 11;
			gridBagConstraints.gridwidth = 2;
			MessageText = new JLabel();
			MessageText.setText("JLabel");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(MessageText, gridBagConstraints);
			jContentPane.add(getOkButton(), gridBagConstraints1);
			jContentPane.add(getCancelButton(), gridBagConstraints2);
			jContentPane.add(getMComboBox(), gridBagConstraints3);
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
			OkButton.setText(LanguageResource.getString("OKBUTTON_STR") );
			OkButton.setMinimumSize(new Dimension(50, 14));
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
			CancelButton.setText(LanguageResource.getString("CANCELBUTTON_STR") );
			CancelButton.setMinimumSize(new Dimension(50, 14));
			CancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return CancelButton;
	}

	void setMessageText(String text)
	{
		MessageText.setText(text);
	}

	void setItems(String[] items)
	{
		for(int i = 0; i < items.length; i++)
		{
			mComboBox.addItem(items[i]);
		}
	}

	void setSelectedItem(String item)
	{
		mComboBox.setSelectedItem(item);
	}


	String getSelectedItem()
	{
		return (String)mComboBox.getSelectedItem();
	}


	boolean wasCancelled()
	{
		return mWasCancelled;

	}

	/**
	 * This method initializes mComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getMComboBox() {
		if (mComboBox == null) {
			mComboBox = new JComboBox();
		}
		return mComboBox;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"