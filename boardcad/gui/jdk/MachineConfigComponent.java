package boardcad.gui.jdk;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;

import javax.swing.JPanel;

import boardcam.MachineConfig;

public class MachineConfigComponent extends JPanel
{
	static final long serialVersionUID=1L;
	MachineConfig mConfig;
	CategorizedSettingsComponent mConfigComponent;
	
	MachineConfigComponent(MachineConfig config){
		super();
		
		mConfig = config;
		
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(164, 300));
        this.setLayout(new BorderLayout());
        this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        mConfigComponent = new CategorizedSettingsComponent(mConfig);
		this.add(mConfigComponent);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
