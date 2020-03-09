package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import boardcad.i18n.LanguageResource;
import boardcad.settings.*;

public class SettingDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 813932272331942542L;
	boolean mWasCancelled = true;

	SettingDialog(final CategorizedSettings settings)
	{
		this.setTitle(LanguageResource.getString("SETTINGSTITLE_STR"));
		this.setSize(new Dimension(352, 221));
		this.setLayout(new BorderLayout());
				
				
		JButton okButton = new JButton();
		okButton.setText(LanguageResource.getString("OKBUTTON_STR"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setVisible(false);
				mWasCancelled = false;
			}
		});
		JButton cancelButton = new JButton();
		cancelButton.setText(LanguageResource.getString("CANCELBUTTON_STR"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setVisible(false);
			}
		});
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(okButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(cancelButton);
		
		this.add(new CategorizedSettingsComponent(settings), BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
	}


	boolean wasCancelled()
	{
		return mWasCancelled;

	}

}
