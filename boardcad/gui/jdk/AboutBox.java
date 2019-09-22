package boardcad.gui.jdk;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import boardcad.i18n.LanguageResource;

public class AboutBox extends JDialog {

	private JTextArea mText = null;

	/**
	 * This method initializes
	 *
	 */
	public AboutBox() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		
		this.setSize(new Dimension(540, 557));
		this.setContentPane(getMText());
        this.setModal(true);
        this.setTitle(LanguageResource.getString("ABOUTBOX_STR"));
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes mText
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getMText() {
		if (mText == null) {
			mText = new JTextArea();
			mText.setText("                                     BoardCAD v3.1 (Feb 1, 2011)\n   \nProgramming by:\nJonas Hörnstein\nOla Helenius\nHåvard Nygård Jakobsen\nSven Wesley\n\nTranslation by:\nMocoloitam (Portugese)\nPere Antoni Comas (Spanish)\nStephen Kitchener (French)\nWouter Oosting (Dutch)\n\nThis program is free software: you can redistribute it and/or modify\nit under the terms of the GNU General Public License as published by\nthe Free Software Foundation, either version 3 of the License, or\n(at your option) any later version.\n\nThis program is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\nGNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License\nalong with this program.  If not, see http://www.gnu.org/licenses\n===================================================================\n Enjoy... ");
			mText.setEditable(false);
			mText.setBackground(Color.lightGray);
			mText.setFont(new Font("Dialog", Font.PLAIN, 14));
			mText.setPreferredSize(new Dimension(500, 500));
			mText.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		}
		return mText;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
