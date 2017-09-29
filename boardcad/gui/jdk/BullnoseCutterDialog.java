package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class BullnoseCutterDialog extends JDialog {

	private JPanel jContentPane = null;
	private JButton mOkButton = null;
	private JButton mCancelButton = null;
	private JPanel mEditorPanel = null;
	private JPanel jPanel = null;

	public BullnoseCutterDialog() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(682, 381));
        this.setContentPane(getJContentPane());
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getMEditorPanel(), BorderLayout.WEST);
			jContentPane.add(getJPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes mOkButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMOkButton() {
		if (mOkButton == null) {
			mOkButton = new JButton();
			mOkButton.setText("OK");
		}
		return mOkButton;
	}

	/**
	 * This method initializes mCancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMCancelButton() {
		if (mCancelButton == null) {
			mCancelButton = new JButton();
			mCancelButton.setText("Cancel");
		}
		return mCancelButton;
	}

	/**
	 * This method initializes mEditorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMEditorPanel() {
		if (mEditorPanel == null) {
			mEditorPanel = new JPanel();
			mEditorPanel.setLayout(new BoxLayout(getMEditorPanel(), BoxLayout.X_AXIS));
		}
		return mEditorPanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getMOkButton(), gridBagConstraints);
			jPanel.add(getMCancelButton(), gridBagConstraints1);
		}
		return jPanel;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"