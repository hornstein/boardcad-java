package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;

import boardcad.i18n.LanguageResource;

class BoardCADSettingsDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 813932272331942542L;

	BoardCADSettingsDialog(final BoardCADSettings settings)
	{
		this.setTitle(LanguageResource.getString("BOARDCADSETTINGSTITLE_STR"));
		this.setSize(new Dimension(500, 500));
		this.setLayout(new BorderLayout());
		this.add(new CategorizedSettingsComponent(settings), BorderLayout.CENTER);
		this.setLocationRelativeTo(null);
	}


} 